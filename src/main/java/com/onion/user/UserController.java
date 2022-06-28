package com.onion.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onion.config.OnionUserDetails;
import com.onion.config.Utility;
import com.onion.config.oauth.CustomerOAuth2User;
import com.onion.domain.Location;
import com.onion.domain.Tag;
import com.onion.domain.User;
import com.onion.exception.UserNotFoundException;
import com.onion.location.LocationRepository;
import com.onion.location.LocationService;
import com.onion.paging.PagingAndSortingHelper;
import com.onion.paging.PagingAndSortingParam;

import com.onion.tag.TagForm;
import com.onion.tag.TagRepository;
import com.onion.tag.TagService;
import com.onion.user.exporter.UserExcelExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UserController {
    private String defaultRedirectURL = "redirect:/users/page/1?sortField=id&sortDir=asc";
    private final UserService userService;
    private final LocationService locationService;

    private final UserRepository userRepository;

    private final TagRepository tagRepository;

    private final TagService tagService;

    private final ObjectMapper objectMapper;



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
        Iterable<Location> listChildLocations= locationService.listChildLocations();

        model.addAttribute("listLocations", listLocations);
        model.addAttribute("listChildLocations", listChildLocations);
        model.addAttribute("user", new User());
        model.addAttribute("pageTitle", "유저 추가");

        return "users/register_form";
    }

    // 회원 가입 POST
    @PostMapping("/create_user")
    public String createUser(User user, Model model,
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



    //유저 수정 폼 by 관리자
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

    //유저 삭제 by 관리자
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

    // 유저 활성화 여부 업데이트 GET by 관리자
    @GetMapping("/users/{id}/enabled/{status}")
    public String updateUserEnabledStatus(@PathVariable("id") Integer id,
                                          @PathVariable("status") boolean enabled, RedirectAttributes redirectAttributes) {
        userService.updateUserEnabledStatus(id, enabled);
        String status = enabled ? "enabled" : "disabled";
        String message = "유저 ID: " + id + "가" +status+"되었습니다." ;
        redirectAttributes.addFlashAttribute("message", message);

        return defaultRedirectURL;
    }

    // 유저 목록 엑셀 파일로 내보내기 by 관리자
    @GetMapping("/users/export/excel")
    public void exportToPDF(HttpServletResponse response) throws IOException {
        List<User> listUsers = userService.listAll();

        UserExcelExporter exporter = new UserExcelExporter();
        exporter.export(listUsers, response);
    }

    // 회원이 자신의 계정 정보 보기
    @GetMapping("/user_details")
    public String viewAccountDetails(Model model, HttpServletRequest request) {
        //승인된 회원 이메일 불러오기
        String email = Utility.getEmailOfAuthenticatedCustomer(request);
        User user = userService.getUserByEmail(email);
        Location listLocationsForUser=user.getLocation();

        model.addAttribute("user", user);
        model.addAttribute("listLocations", listLocationsForUser);

        return "users/settings/view_profile";
    }

    // 물건 목록에서 다른 회원이 판매자의 간단 정보를 조회
    @GetMapping("/seller_details/{id}")
    public String viewSellerDetails(Model model, @PathVariable("id") Integer id) {
        User user = userRepository.findById(id).get();
        model.addAttribute("user", user);
        return "users/seller_detail";
    }

    // 회원이 자신의 계정 수정 폼
    @GetMapping("/user_update")
    public String viewAccountDetailsForUpdate(Model model, HttpServletRequest request) {
        //승인된 회원 이메일 불러오기
        String email = Utility.getEmailOfAuthenticatedCustomer(request);
        User user = userService.getUserByEmail(email);
        List<Location> listLocations = locationService.listLocationsUsedInForm();
        Iterable<Location> listChildLocations= locationService.listChildLocations();

        model.addAttribute("user", user);
        model.addAttribute("listLocations",listLocations);
        model.addAttribute("listChildLocations", listChildLocations);

        return "users/settings/update_profile";
    }

    // 회원이 자신의 정보를 수정하기 post
    @PostMapping("/user_update")
    public String updateAccountDetails(Model model,User user, RedirectAttributes ra,
                                       HttpServletRequest request) {
        userService.update(user);
        ra.addFlashAttribute("message", "계정정보가 수정되었습니다.");

        // 인증된 회원 이름 수정
        updateNameForAuthenticatedUser(user, request);


        return "redirect:/user_details";
    }

    // 알림설정 뷰
    @GetMapping("/notification")
    public String updateNotificationsForm(Model model, HttpServletRequest request) {
        String email = Utility.getEmailOfAuthenticatedCustomer(request);
        User user = userService.getUserByEmail(email);

        model.addAttribute("user", user);
        return "users/settings/notification";
    }

    //알림받기 활성화 여부 업데이트 GET
    @GetMapping("/notification/enabled/{status}")
    public String updateNotificationStatus(@PathVariable("status") boolean enabled, RedirectAttributes redirectAttributes,
                                           @CurrentAccount User user) {

        userService.updateNotificationStatus(user,enabled);
        String status = enabled ? "알림이 허용" : "알림이 취소";
        String message = status+"되었습니다." ;
        redirectAttributes.addFlashAttribute("message", message);

        return "redirect:/notification";
    }

    // 태그 수정 뷰
    @GetMapping("/tags")
    public String updateTags(HttpServletRequest request, Model model) throws JsonProcessingException {
        String email = Utility.getEmailOfAuthenticatedCustomer(request);
        User user = userService.getUserByEmail(email);
        model.addAttribute(user);

        Set<Tag> tags = userService.getTags(user);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return "users/settings/tags";
    }

    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(HttpServletRequest request, @RequestBody TagForm tagForm) {
        String email = Utility.getEmailOfAuthenticatedCustomer(request);
        User user = userService.getUserByEmail(email);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        userService.addTag(user, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(HttpServletRequest request, @RequestBody TagForm tagForm) {
        String email = Utility.getEmailOfAuthenticatedCustomer(request);
        User user = userService.getUserByEmail(email);

        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        userService.removeTag(user, tag);
        return ResponseEntity.ok().build();
    }



    // 인증된 회원 이름 수정, 가입 유형마다 방법 다름
    private void updateNameForAuthenticatedUser(User user, HttpServletRequest request) {
        Object principal = request.getUserPrincipal();

        // 폼 로그인 회원이거나 쿠기로그인(로그인 기억) 회원일 경우
        if (principal instanceof UsernamePasswordAuthenticationToken
                || principal instanceof RememberMeAuthenticationToken) {
            OnionUserDetails userDetails = getUserDetailsObject(principal);
            User authenticatedUser = userDetails.getUser();
            authenticatedUser.setNickname(user.getNickname());

        } else if (principal instanceof OAuth2AuthenticationToken) { // 소셜 로그인일 경우 회원 이메일 리턴
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) principal;
            CustomerOAuth2User oauth2User = (CustomerOAuth2User) oauth2Token.getPrincipal();
            String nickname = user.getNickname();
            oauth2User.setNickname(nickname);
        }
    }

    // 폼 로그인 회원이거나 쿠기로그인(로그인 기억) 회원의 UserDetails 가져오기
    private OnionUserDetails getUserDetailsObject(Object principal) {
        OnionUserDetails userDetails = null;
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
            userDetails = (OnionUserDetails) token.getPrincipal();
        } else if (principal instanceof RememberMeAuthenticationToken) {
            RememberMeAuthenticationToken token = (RememberMeAuthenticationToken) principal;
            userDetails = (OnionUserDetails) token.getPrincipal();
        }

        return userDetails;
    }


}
