package com.swp2.demo.Controller;

import com.swp2.demo.entity.Member;
import com.swp2.demo.entity.User;
import com.swp2.demo.entity.dto.PaymentResponeDTO;
import com.swp2.demo.security.CustomUserDetails;
import com.swp2.demo.service.MemberService;
import com.swp2.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MemberController {
    @Autowired
    private UserService userService;

    @Autowired
    private MemberService memberService;

    @GetMapping("/member")
    public String memberPage(@AuthenticationPrincipal Object principal, Model model) {
        User user = null;

        if (principal instanceof CustomUserDetails userDetails) {
            user = userService.findById(userDetails.getId());
            model.addAttribute("fullName", user.getFirstName());
            model.addAttribute("isUsernameLogin", true);
        } else if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            user = userService.findByEmail(email);
            if (user != null) {
                model.addAttribute("fullName", user.getFirstName());
            }
            model.addAttribute("isUsernameLogin", false);
        }

        if (user != null) {
            model.addAttribute("currentMember", user.getMember());
        } else {
            model.addAttribute("currentMember", null);
        }

        return "member";
    }

    @PostMapping("/member/purchase")
    public String purchaseMembership(@AuthenticationPrincipal Object principal,
                                   @RequestParam("plan") Member plan,
                                   @RequestParam("amount") Integer amount,
                                   @RequestParam("description") String description,
                                   RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(principal);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Please login to purchase membership");
                return "redirect:/login";
            }

            // Check if user can upgrade to this membership
            if (!memberService.canUpgradeToMembership(user, plan)) {
                redirectAttributes.addFlashAttribute("error", "You already have this membership plan");
                return "redirect:/member";
            }

            // Handle FREE membership
            if (plan == Member.FREE) {
                memberService.upgradeMembership(user, plan);
                redirectAttributes.addFlashAttribute("success", "Successfully downgraded to FREE membership");
                return "redirect:/member";
            }

            // Create payment for paid memberships
            PaymentResponeDTO paymentResponse = memberService.createMembershipPayment(user, plan);

            if (paymentResponse != null) {
                // Store membership info in session or database for later processing
                redirectAttributes.addFlashAttribute("orderCode", paymentResponse.getOrderCode());
                redirectAttributes.addFlashAttribute("membershipPlan", plan.name());

                // Redirect to PayOS payment page
                return "redirect:" + paymentResponse.getCheckoutUrl();
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to create payment");
                return "redirect:/member";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing membership purchase: " + e.getMessage());
            return "redirect:/member";
        }
    }

    @GetMapping("/member/success")
    public String paymentSuccess(@RequestParam(required = false) Long orderCode,
                               @AuthenticationPrincipal Object principal,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser(principal);
            if (user == null) {
                return "redirect:/login";
            }

            if (orderCode != null) {
                // In a real implementation, you would verify the payment status with PayOS
                // and get the membership type from your database based on orderCode

                // For now, we'll assume VIP membership (you should implement proper order tracking)
                Member newMembership = Member.VIP; // This should be retrieved from your order tracking

                memberService.upgradeMembership(user, newMembership);

//                Member newMembership = memberService.getMembershipByOrderCode(orderCode);
//                memberService.upgradeMembership(user, newMembership);
                model.addAttribute("success", true);
                model.addAttribute("membershipType", newMembership.name());
                model.addAttribute("orderCode", orderCode);
            }

            return "success";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing successful payment");
            return "redirect:/member";
        }
    }

    @GetMapping("/member/cancel")
    public String paymentCancel(@RequestParam(required = false) Long orderCode) {
        return "redirect:/api/payments/cancel?orderCode=" + orderCode;
    }


    private User getCurrentUser(Object principal) {
        if (principal instanceof CustomUserDetails userDetails) {
            return userService.findById(userDetails.getId());
        } else if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            return userService.findByEmail(email);
        }
        return null;
    }

    // ...existing code...
}
