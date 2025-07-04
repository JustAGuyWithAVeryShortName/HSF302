package com.swp2.demo.Controller;

import com.swp2.demo.entity.Member;
import com.swp2.demo.entity.User;
import com.swp2.demo.security.CustomUserDetails;
import com.swp2.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MemberController {
    @Autowired
    private UserService userService;
    @GetMapping("/member")
    public String memberPage(@AuthenticationPrincipal Object principal, Model model) {
        User user = null;

        if (principal instanceof CustomUserDetails userDetails) {
            user = userService.findById(userDetails.getId());
        } else if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            user = userService.findByEmail(email);
        }

        if (user != null) {
            model.addAttribute("currentMember", user.getMember());
        } else {
            model.addAttribute("currentMember", null);
        }



        return "member"; // Trả về file member.html
    }
    @PostMapping("/member/checkout")
    public String checkout(@RequestParam("plan") Member plan) {
        if(plan==Member.FREE){
            return "redirect:/success";
        }

        // TODO: Sau bước này → sinh QR code thanh toán, hoặc chuyển đến trang payment
        return "redirect:/payment?plan=" + plan.name();
    }
}
