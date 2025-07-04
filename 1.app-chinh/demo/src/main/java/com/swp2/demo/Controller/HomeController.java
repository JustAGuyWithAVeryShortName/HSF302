package com.swp2.demo.Controller;

import com.swp2.demo.repository.MessageHomeRepository;
import com.swp2.demo.repository.NotificationRepository;
import com.swp2.demo.repository.UserRepository;
import com.swp2.demo.entity.Notification;
import com.swp2.demo.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {

    // ========== Inner Class ==========
    static class LeaderboardUser {
        private final int rank;
        private final String name;
        private final int daysSmokeFree;

        public LeaderboardUser(int rank, String name, int daysSmokeFree) {
            this.rank = rank;
            this.name = name;
            this.daysSmokeFree = daysSmokeFree;
        }

        public int getRank() { return rank; }
        public String getName() { return name; }
        public int getDaysSmokeFree() { return daysSmokeFree; }
    }

    static class BlogPost {
        private final int id;
        private final String title;
        private final String excerpt;
        private final String imageUrl;
        private final String content;

        public BlogPost(int id, String title, String excerpt, String imageUrl, String content) {
            this.id = id;
            this.title = title;
            this.excerpt = excerpt;
            this.imageUrl = imageUrl;
            this.content = content;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getExcerpt() { return excerpt; }
        public String getImageUrl() { return imageUrl; }
        public String getContent() { return content; }
    }

    // ========== Dummy Blog Data ==========
    private final List<BlogPost> blogPosts = Arrays.asList(
            new BlogPost(
                    1,
                    "5 Tips to Overcome Cigarette Cravings",
                    "Quick and powerful strategies to deal with nicotine cravings — and stay in control.",
                    "/images/11.png",
                    "<p>Cravings are normal when you’re quitting smoking — but they don’t have to control you. Here are 5 proven strategies to help you beat cravings before they beat you:</p>" +

                            "<h3>1. Drink a Glass of Cold Water</h3>" +
                            "<p>Cold water not only distracts you, but it also refreshes your system. Sipping slowly can reduce anxiety and give you something to focus on.</p>" +

                            "<h3>2. Breathe Deeply</h3>" +
                            "<p>Take 5 deep breaths — in through your nose, out through your mouth. Slowing your breathing helps calm your nervous system and re-center your mind.</p>" +

                            "<h3>3. Change Your Environment</h3>" +
                            "<p>Move to a different room. Go for a short walk. Stand up. Changing physical space often helps shift the mental urge as well.</p>" +

                            "<h3>4. Chew Something</h3>" +
                            "<p>Keep sugar-free gum, carrots, or sunflower seeds nearby. Keeping your mouth busy satisfies the oral habit without reaching for a cigarette.</p>" +

                            "<h3>5. Talk to Someone</h3>" +
                            "<p>Call or message a friend who supports your goal. Talking about the craving — even for a minute — reduces its power.</p>" +

                            "<p><strong>Remember:</strong> Each craving only lasts a few minutes. Ride it out — and you’re one step closer to freedom.</p>"
            ),
            new BlogPost(
                    2,
                    "How I Quit Smoking After 10 Years",
                    "A personal story of how I finally broke free from smoking after a decade of addiction.",
                    "/images/12.png",
                    "<p>I started smoking when I was 18. At first, it was social, something I did with friends at parties or during breaks. But by the time I turned 21, it had become a habit — one I relied on to deal with stress, boredom, and even happiness.</p>" +

                            "<p>For years, I tried to quit. I attempted cold turkey, nicotine patches, even hypnosis — nothing seemed to work. I’d go a few days or a week without smoking, only to relapse during a stressful event or a casual night out.</p>" +

                            "<h3>My Turning Point</h3>" +
                            "<p>Everything changed after a conversation with my 8-year-old niece. She asked me why I always smelled like smoke and whether I was going to die soon like the man on the cigarette ad. That hit me hard.</p>" +

                            "<p>I decided to commit for real. No excuses. No fallback plans.</p>" +

                            "<h3>What Helped Me</h3>" +
                            "<ul>" +
                            "<li><strong>Tracking progress:</strong> I used an app to log every day I stayed smoke-free. Watching the days stack up was powerful motivation.</li>" +
                            "<li><strong>Reframing triggers:</strong> Instead of associating stress with smoking, I began to go for a walk, journal, or call a friend.</li>" +
                            "<li><strong>Social support:</strong> I joined an online quit-smoking group. Sharing my journey — and reading others' — kept me accountable.</li>" +
                            "<li><strong>Professional help:</strong> I worked with a therapist who helped me uncover emotional roots tied to the habit.</li>" +
                            "</ul>" +

                            "<h3>One Year Later</h3>" +
                            "<p>It’s been a full year since my last cigarette. I breathe easier, sleep better, and feel proud of myself in a way I haven’t in years. If you’re thinking about quitting, know this: the first week is hard, the first month is harder — but the freedom you gain is worth every moment of discomfort.</p>" +

                            "<p><em>You can do this.</em></p>"
            ),
            new BlogPost(
                    3,
                    "Health Benefits After 24 Hours Smoke-Free",
                    "Your body begins to heal the moment you stop smoking — here’s what happens in just one day.",
                    "/images/13.png",
                    "<p>Quitting smoking has immediate and powerful effects. Within the first 24 hours, your body starts to repair the damage caused by nicotine and toxins.</p>" +

                            "<h3>After 20 Minutes</h3>" +
                            "<p>Your blood pressure and heart rate begin to drop. Circulation improves almost instantly.</p>" +

                            "<h3>After 8 Hours</h3>" +
                            "<p>Oxygen levels return to normal. Carbon monoxide, a toxic gas from cigarette smoke, is reduced by half.</p>" +

                            "<h3>After 12 Hours</h3>" +
                            "<p>The carbon monoxide in your bloodstream drops to normal levels, allowing more oxygen to nourish your organs and brain.</p>" +

                            "<h3>After 24 Hours</h3>" +
                            "<p>The risk of a heart attack begins to decrease. Your immune system gets a boost, and you may start to notice better breathing and energy levels.</p>" +

                            "<p><strong>Every hour smoke-free is a victory.</strong> Your body is working for you — give it the chance it deserves.</p>"
            )
    );
    @Autowired
    private MessageHomeRepository messageRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private NotificationRepository notificationRepository;

    // ========== Trang chủ ==========
    @GetMapping({"/", "/home"})
    public String home(Model model, Authentication authentication) {
        String finalUsername = "anonymous";
        User user = null;

        if (authentication instanceof OAuth2AuthenticationToken oauth) {
            finalUsername = (String) oauth.getPrincipal().getAttributes().get("email");
            user = userRepo.findByEmail(finalUsername);
        } else if (authentication != null) {
            finalUsername = authentication.getName();
            user = userRepo.findByUsername(finalUsername);        }

        model.addAttribute("finalUsername", finalUsername);

        //thông báo
      /*  if (user != null) {
            List<Notification> allNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

            List<Notification> topNotifications = allNotifications.size() > 3
                    ? allNotifications.subList(0, 3)
                    : allNotifications;

            model.addAttribute("notifications", topNotifications); // Cho dropdown ở home.html
            model.addAttribute("allNotifications", allNotifications); // Cho trang /notice
        }*/

        List<LeaderboardUser> leaderboard = Arrays.asList(
                new LeaderboardUser(1, "Thanh Nguyen", 152),
                new LeaderboardUser(2, "Minh Tran", 140),
                new LeaderboardUser(3, "An Le", 118),
                new LeaderboardUser(4, "Hoa Pham", 95)
        );

        model.addAttribute("leaderboardUsers", leaderboard);
        model.addAttribute("blogPosts", blogPosts);


        return "home";
    }

    @GetMapping("/notice")
    public String viewAllNotifications(Model model, Authentication authentication) {
        if (authentication == null) return "redirect:/login";

        String username = authentication.getName();
        User user = userRepo.findByUsername(username);

        if (user != null) {
            List<Notification> allNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
            model.addAttribute("allNotifications", allNotifications);
        }

        return "notice"; // dùng templates/notice.html
    }



    // ========== Trang chi tiết bài viết ==========
    @GetMapping("/blog/{id}")
    public String blogDetail(@PathVariable int id, Model model) {
        for (BlogPost post : blogPosts) {
            if (post.getId() == id) {
                model.addAttribute("post", post);
                return "blog_detail"; // blog_detail.html
            }
        }
        return "redirect:/home";
    }
}
