package com.onion.user;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.onion.domain.Tag;
import com.onion.domain.User;

import com.onion.infra.MockMvcTest;
import com.onion.location.LocationRepository;
import com.onion.tag.TagForm;
import com.onion.tag.TagRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class SettingControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired UserService userService;
    @Autowired UserRepository userRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired LocationRepository locationRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired RoleService roleService;
    @Autowired
    TagRepository tagRepository;


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
    @DisplayName("유저의 정보 폼")
    @Test
    void viewUserDetailForm() throws Exception {
        mockMvc.perform(get("/user_details"))
                .andExpect(view().name("users/settings/view_profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("listLocations"));
    }

    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("판매자의 간단 정보를 조회")
    @Test
    void viewSellerDetailForm() throws Exception {
        mockMvc.perform(get("/seller_details/28"))
                .andExpect(view().name("users/seller_detail"))
                .andExpect(model().attributeExists("user"));
    }

    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("유저의 알림 수정 폼")
    @Test
    void updateNotificationForm() throws Exception {
        mockMvc.perform(get("/notification"))
                .andExpect(view().name("users/settings/notification"))
                .andExpect(model().attributeExists("user"));
    }


    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("유저의 알림 수정")
    @Test
    void updateNotification() throws Exception {
        mockMvc.perform(get("/notification/enabled/false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notification"))
                .andExpect(flash().attributeExists("message"));
    }

    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("유저활성화 업데이트 by 관리자")
    @Test
    void updateEnabledUser() throws Exception {
        User user=userService.getUserByEmail("test@email.com");
        Integer id = user.getId();
        mockMvc.perform(get("/users/"+id+"/enabled/false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/page/1?sortField=id&sortDir=asc"))
                .andExpect(flash().attributeExists("message"));
    }

    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("유저수정폼 by 관리자")
    @Test
    void updateUserByAdmin() throws Exception {
        User user=userService.getUserByEmail("test@email.com");
        Integer id = user.getId();
        mockMvc.perform(get("/users/edit/"+id))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("listLocations"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(view().name("users/register_form"));

    }

    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("유저삭제 by 관리자")
    @Test
    void deleteUserByAdmin() throws Exception {
        User user=userService.getUserByEmail("test@email.com");
        Integer id = user.getId();
        mockMvc.perform(get("/users/delete/"+id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/page/1?sortField=id&sortDir=asc"))
                .andExpect(flash().attributeExists("message"));
        assertThat(userService.getUserByEmail("test@email.com")).isNull();

    }

    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("태그 등록 폼")
    @Test
    void updateTagsView() throws Exception {
        User user=userService.getUserByEmail("test@email.com");
        Integer id = user.getId();
        mockMvc.perform(get("/tags"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(view().name("users/settings/tags"));
    }

    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("태그 등록")
    @Test
    void addTags() throws Exception {
        TagForm newTag = new TagForm();
        newTag.setTagTitle("newTag");
        mockMvc.perform(post("/tags/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTag))
                        .with(csrf()))
                .andExpect(status().isOk());
        Tag result = tagRepository.findByTitle("newTag");
        assertNotNull(result);
        User user = userRepository.findByEmail("test@email.com");

        assertTrue(user.getTags().contains(result));

    }

    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("태그 삭제")
    @Test
    void removeTag() throws Exception {
        User user = userRepository.findByEmail("test@email.com");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        userService.addTag(user, newTag);

        assertTrue(user.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post("/tags/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagForm))
                        .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(user.getTags().contains(newTag));
    }

    
    

}