package com.swp2.demo.service;

import com.swp2.demo.entity.Member;
import com.swp2.demo.entity.User;
import com.swp2.demo.entity.dto.PaymentRequestDTO;
import com.swp2.demo.entity.dto.PaymentResponeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final PayOSService payOSService;
    private final UserService userService;

    // Membership pricing configuration
    private static final Map<Member, Integer> MEMBERSHIP_PRICES = Map.of(
        Member.FREE, 0,
        Member.VIP, 5000,
        Member.PREMIUM, 10000
    );

    public PaymentResponeDTO createMembershipPayment(User user, Member targetMember) {
        if (targetMember == Member.FREE) {
            // Free membership - no payment required
            upgradeMembership(user, targetMember);
            return null;
        }

        Integer amount = MEMBERSHIP_PRICES.get(targetMember);
        if (amount == null) {
            throw new IllegalArgumentException("Invalid membership type: " + targetMember);
        }

        // Create a short description within 25 character limit
        String description = targetMember.name() + " Plan";
        // Ensure description is within PayOS limit (25 characters)
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        PaymentRequestDTO paymentRequest = new PaymentRequestDTO(amount, description);

        log.info("Creating payment for user {} to upgrade to {} membership",
            user.getEmail(), targetMember);

        return payOSService.createPaymentLink(paymentRequest);
    }

    @Transactional
    public void upgradeMembership(User user, Member newMember) {
        user.setMember(newMember);
        userService.save(user);

        log.info("User {} successfully upgraded to {} membership",
            user.getEmail(), newMember);
    }

    public boolean canUpgradeToMembership(User user, Member targetMember) {
        Member currentMember = user.getMember();

        // Can always downgrade to FREE
        if (targetMember == Member.FREE) {
            return true;
        }

        // Can't upgrade to same membership
        if (currentMember == targetMember) {
            return false;
        }

        return true;
    }

    public Member getMembershipByOrderCode(long orderCode) {
        // You might want to store order-to-membership mapping in database
        // For now, we'll extract from payment description or use a simple mapping
        // This is a simplified approach - in production, store this in database
        return Member.VIP; // Default fallback
    }
}
