package com.onion.user;

import com.onion.domain.Location;
import com.onion.domain.Role;
import com.onion.domain.User;
import com.onion.exception.UserNotFoundException;
import com.onion.location.LocationService;
import com.onion.paging.PagingAndSortingHelper;
import com.onion.paging.PagingAndSortingParam;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {
    private String defaultRedirectURL = "redirect:/users/page/1?sortField=id&sortDir=asc";
    private final UserService userService;
    private final LocationService locationService;

    private final UserRepository userRepository;

    //유저 목록 GET
    @GetMapping("/users")
    public String listFirstPage() {
        return defaultRedirectURL;
    }

    //유저 목록 페이징 GET
    @GetMapping("/users/page/{pageNum}")
    public String listByPage(
            @PagingAndSortingParam(listName = "listUsers", moduleURL = "/users") PagingAndSortingHelper helper,
            @PathVariable(name = "pageNum") int pageNum
    ) {
        userService.listByPage(pageNum, helper);
        return "users/users";
    }

    //유저 회원가입 폼 GET
    @GetMapping("/register")
    public String newUser(Model model) {
        List<Location> listLocations = locationService.listLocationsUsedInForm();

        model.addAttribute("listLocations", listLocations);
        model.addAttribute("user", new User());
        model.addAttribute("pageTitle", "유저 추가");

        return "users/register_form";
    }

    // 회원 가입 POST
    @PostMapping("/create_user")
    public String createCustomer(User user, Model model,
                                 HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {
        // 회원 가입
        userService.registerUser(user);
        // 인증 이메일 발송
        userService.sendVerificationEmail(request, user);

        model.addAttribute("pageTitle", "회원가입이 완료되었습니다.");

        return "users/register_success";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {
        User newUser = userRepository.findByEmail(email);
        String view = "users/checked-email";
        if (newUser == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }

        if (!newUser.isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }

        userService.completeSignUp(newUser);
        model.addAttribute("nickname", newUser.getNickname());
        return view;
    }



    //유저 수정 폼
    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable(name = "id") Integer id, Model model,
                            RedirectAttributes ra) {
        try {
            User user = userService.get(id);
            List<Location> listLocations = locationService.listLocationsUsedInForm();

            model.addAttribute("user", user);
            model.addAttribute("listLocations", listLocations);
            model.addAttribute("pageTitle", "유저 수정 (ID: " + id + ")");

            return "users/register_form";
        } catch (UserNotFoundException ex) {
            ra.addFlashAttribute("message", ex.getMessage());
            return defaultRedirectURL;
        }
    }

    //유저 삭제
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable(name = "id") Integer id,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            userService.delete(id);
            redirectAttributes.addFlashAttribute("message",
                    "유저 ID " + id + " 가 삭제되었습니다.");
        } catch (UserNotFoundException ex) {
            redirectAttributes.addFlashAttribute("message", ex.getMessage());
        }

        return defaultRedirectURL;
    }

    // 유저 활성화 여부 업데이트 GET
    @GetMapping("/users/{id}/enabled/{status}")
    public String updateUserEnabledStatus(@PathVariable("id") Integer id,
                                          @PathVariable("status") boolean enabled, RedirectAttributes redirectAttributes) {
        userService.updateUserEnabledStatus(id, enabled);
        String status = enabled ? "enabled" : "disabled";
        String message = "유저 ID: " + id + "가" +status+"되었습니다." ;
        redirectAttributes.addFlashAttribute("message", message);

        return defaultRedirectURL;
    }


}
