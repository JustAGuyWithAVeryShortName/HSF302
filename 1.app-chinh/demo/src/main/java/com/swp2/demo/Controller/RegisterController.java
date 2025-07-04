package com.swp2.demo.Controller;

import com.swp2.demo.entity.Role;

import com.swp2.demo.entity.User;
import com.swp2.demo.service.UserService;
import com.swp2.demo.web.RegisterUser;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/register")
public class RegisterController {

    private final UserService userService;

    @Autowired
    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/showRegisterForm")
    public String showRegister(Model model) {
        model.addAttribute("registerUser", new RegisterUser());
        return "register/form";
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @PostMapping("/process")
    public String processRegister(
            @Valid @ModelAttribute("registerUser") RegisterUser registerUser,
            BindingResult result,
            Model model,
            HttpSession session
    ) {
        String username = registerUser.getUsername();

        if (result.hasErrors()) {
            return "register/form";
        }

        User existingUser = userService.findByUsername(username);
        if (existingUser != null) {
            model.addAttribute("registerUser", new RegisterUser());
            model.addAttribute("my_error", "Tài khoản đã tồn tại");
            return "register/form";
        }

        User existingEmail = userService.findByEmail(registerUser.getEmail());
        if (existingEmail != null) {
            model.addAttribute("my_error", "Email đã được sử dụng");
            return "register/form";
        }


        // BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

        User user = new User();
        user.setUsername(username);
        user.setPassword(registerUser.getPassword());
        //user.setPassword(bcrypt.encode(registerUser.getPassword()));
        user.setEmail(registerUser.getEmail());
        user.setFirstName(registerUser.getFirstName());
        user.setLastName(registerUser.getLastName());
        user.setGender(registerUser.getGender());
        user.setDateOfBirth(registerUser.getDateOfBirth());


        user.setRole(Role.Guest);  // hoặc Role.Guest nếu bạn dùng enum dạng đó

        userService.save(user);

        session.setAttribute("user", user);
        return "register/confirmation";
    }
}
