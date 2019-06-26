package net.slipp.web;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import net.slipp.domain.User;
import net.slipp.domain.UserRepository;

@Controller
@RequestMapping("/users")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("/loginForm")
	public String loginForm() {
		return "/user/login";
	}
	
	@PostMapping("/login")
	public String login(String userId, String password, HttpSession session) {
		User user = userRepository.findByUserId(userId);
		if(user == null) {
			System.out.println("login Failed");
			return "redirect:/users/loginForm";			
		}
		
		if (!user.matchPassword(password)) {
			System.out.println("login Failed");
			return "redirect:/users/loginForm";
		}
		System.out.println("login Success");
		session.setAttribute(HttpSessionUtils.USER_SESSION_KEY, user);
		
		return "redirect:/";
	}
	
	@GetMapping("/form")
	public String form() {
		return "/user/form";
	}
	
	@PostMapping("")
	public String create(User user) {
		System.out.println("user : " + user);
		userRepository.save(user);
		return "redirect:/users";
	}
	
	@GetMapping("")
	public String list(Model model, HttpSession session) {
		Object tempUser = session.getAttribute("sessionedUser");
		if (tempUser == null) {
			return "redirect:/users/loginForm";
		}
		
		model.addAttribute("users", userRepository.findAll());
		return "/user/list";
	}
	
	@GetMapping("{id}/form")
	public String updateForm(@PathVariable Long id, Model model, HttpSession session) {
		
		if (HttpSessionUtils.isLoginUser(session)) {
			return "redirect:/users/loginForm";
		}
		
		User sessionedUser = HttpSessionUtils.getUserFromSession(session);
		if (!id.equals(sessionedUser.getId())) {
			throw new IllegalStateException("You can't update another user");
		}
		
		User user = userRepository.findById(id).orElse(null);
		model.addAttribute("user", user);
		return "/user/updateForm";
	}

	@PutMapping("/{id}")
	public String update(@PathVariable Long id, User updatedUser, HttpSession session) {
		Object tempUser = session.getAttribute("sessionedUser");
		if (tempUser == null) {
			return "redirect:/users/loginForm";
		}
		
		User sessionedUser = (User) tempUser;
		if (!id.equals(sessionedUser.getId())) {
			throw new IllegalStateException("You can't update another user");
		}
		
		User user = userRepository.findById(id).orElse(null);
		user.update(updatedUser);
		userRepository.save(user);
		return "redirect:/users";
	}
	
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/";
	}
	
}
