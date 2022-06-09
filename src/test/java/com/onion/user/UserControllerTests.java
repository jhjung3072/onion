package com.onion.user;


import com.onion.config.mail.EmailMessage;
import com.onion.config.mail.EmailService;
import com.onion.domain.Location;
import com.onion.domain.User;
import com.onion.infra.MockMvcTest;
import com.onion.location.LocationRepository;
import com.onion.location.LocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.data.domain.Sort;

import java.util.List;


@MockMvcTest
class UserControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleService roleService;
    @MockBean
    EmailService emailService;

    @Autowired
    LocationRepository locationRepository;

    @Autowired
    LocationService locationService;

    @DisplayName("인증 메일 확인 - 입력값 오류")
    @Test
    void checkEmailToken_with_wrong_input() throws Exception {
        mockMvc.perform(get("/check-email-token")
                .param("token", "sdfjslwfwef")
                .param("email", "email@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("users/checked-email"))
                .andExpect(unauthenticated());
    }

    @DisplayName("인증 메일 확인 - 입력값 정상")
    @Test
    void checkEmailToken() throws Exception {
        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPassword("1234");
        user.setNickname("jaeho");

        User newAccount = userRepository.save(user);
        newAccount.generateEmailCheckToken();

        mockMvc.perform(get("/check-email-token")
                .param("token", newAccount.getEmailCheckToken())
                .param("email", newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(view().name("users/checked-email"));
    }

    @DisplayName("회원 가입 화면 보이는지 테스트")
    @Test
    void signUpForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("users/register_form"))
                .andExpect(model().attributeExists("listLocations"))
                .andExpect(unauthenticated());
    }


    @DisplayName("회원 가입 처리")
    @Test
    void signUpSubmit_with_correct_input() throws Exception {
        mockMvc.perform(post("/create_user")
                .param("nickname", "jaeho")
                .param("email", "jasdklas@email.com")
                .param("password", "1234")
                .param("listLocations", String.valueOf(locationRepository.findById(1)))
                .with(csrf()))
                .andExpect(view().name("users/register_success"));
        User user = userRepository.findByEmail("jasdklas@email.com");
        assertNotNull(user);
        assertNotEquals(user.getPassword(), "1234");
        assertNotNull(user.getEmailCheckToken());
        then(emailService).should().sendEmail(any(EmailMessage.class));
    }

}