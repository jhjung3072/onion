package com.onion.notification;

import com.onion.user.User;
import com.onion.infra.MockMvcTest;
import com.onion.location.LocationRepository;
import com.onion.location.LocationService;
import com.onion.user.RoleService;
import com.onion.user.UserRepository;
import com.onion.user.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class NotificationControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

    @Autowired
    LocationService locationService;
    @Autowired
    LocationRepository locationRepository;

    @Autowired NotificationService notificationService;
    @Autowired NotificationRepository notificationRepository;

    @Autowired
    RoleService roleService;


    @BeforeEach
    void beforeEach() {
        User newUser = new User();
        newUser.setNickname("test");
        newUser.setEmail("test@email.com");
        newUser.setBio("hi");
        newUser.setPassword("1234");
        newUser.setEnabled(true);
        newUser.setEmailVerified(true);
        newUser.setLocation(locationRepository.findById(5).get());
        userService.registerUser(newUser);

    }

    @AfterEach
    void afterEach(){
        userRepository.deleteAll();
    }


    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("알림 목록")
    @Test
    void NotificationView() throws Exception {
        User user=userService.getUserByEmail("test@email.com");

        Notification noti1=createNoti(user, false);
        List<Notification> listNoti = notificationRepository.findByUserAndCheckedOrderByCreatedDateTimeDesc(user, false);
        mockMvc.perform(get("/notifications"))
                .andExpect(view().name("notification/list"))
                .andExpect(model().attributeExists("isNew"));

        boolean result = listNoti.get(0).getTitle().equals("테스트");
        assertThat(result).isEqualTo(true);
    }

    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("알림 목록-읽은것들")
    @Test
    void NotificationReadViewView() throws Exception {
        User user=userService.getUserByEmail("test@email.com");

        Notification noti1=createNoti(user, false);
        Notification noti2=createNoti(user, false);

        List<Notification> listNoti = notificationRepository.findByUserAndCheckedOrderByCreatedDateTimeDesc(user, false);

        mockMvc.perform(get("/notifications/old"))
                .andExpect(view().name("notification/list"))
                .andExpect(model().attributeExists("isNew"));

        notificationService.markAsRead(listNoti);
        boolean checked = noti1.isChecked();
        assertThat(checked).isEqualTo(true);
    }

    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("읽은 알림메시지 삭제")
    @Test
    void deleteReadNotificationViewView() throws Exception {
        User user=userService.getUserByEmail("test@email.com");

        Notification noti1=createNoti(user, false);
        List<Notification> listNoti = notificationRepository.findByUserAndCheckedOrderByCreatedDateTimeDesc(user, false);
        notificationService.markAsRead(listNoti);

        mockMvc.perform(delete("/notifications")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notifications"));

        List<Notification> result = notificationRepository.findByUserAndCheckedOrderByCreatedDateTimeDesc(user, true);
        assertThat(result).isEmpty();
    }

    private Notification createNoti(User user, boolean checked){
        Notification noti=new Notification();
        noti.setNotificationType(NotificationType.PRODUCT_CREATED);
        noti.setTitle("테스트");
        noti.setUser(user);
        noti.setChecked(checked);
        noti.setCreatedDateTime(LocalDateTime.now());
        noti.setMessage("테스트입니다.");
        return notificationRepository.save(noti);
    }

}