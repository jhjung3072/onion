package com.onion.location;


import com.onion.infra.MockMvcTest;
import com.onion.user.RoleService;
import com.onion.user.User;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class LocationControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

    @Autowired LocationService locationService;
    @Autowired LocationRepository locationRepository;
  

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
    @DisplayName("지역리스트 뷰 by 관리자")
    @Test
    void viewLocations() throws Exception {
        mockMvc.perform(get("/locations/page/1"))
                .andExpect(view().name("locations/locations"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("totalItems"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("sortField"))
                .andExpect(model().attributeExists("sortDir"))
                .andExpect(model().attributeExists("endCount"))
                .andExpect(model().attributeExists("listLocations"))
                .andExpect(model().attributeExists("listLocations"))
                .andExpect(model().attributeExists("reverseSortDir"))
                .andExpect(model().attributeExists("moduleURL"));
    }

    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("지역 생성 폼 by 관리자")
    @Test
    void createLocationForm() throws Exception {
        mockMvc.perform(get("/locations/new"))
                .andExpect(view().name("locations/location_form"))
                .andExpect(model().attributeExists("location"))
                .andExpect(model().attributeExists("listLocations"))
                .andExpect(model().attributeExists("pageTitle"));
    }

    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("지역 생성 by 관리자")
    @Test
    void createLocation() throws Exception {
        mockMvc.perform(post("/locations/save")
                        .param("name", "테스트지역2")
                        .param("enabled", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/locations"));

        Location location = locationService.getByName("테스트지역2");
        assertNotNull(location);
    }

    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("지역 수정 폼 by 관리자")
    @Test
    void updateLocationForm() throws Exception {
        Location location=new Location();
        location.setName("테스트지역");
        location.setEnabled(true);
        locationService.save(location);
        Location testLocation = locationService.getByName("테스트지역");
        Integer id = testLocation.getId();

        mockMvc.perform(get("/locations/edit/"+ id))
                .andExpect(view().name("locations/location_form"))
                .andExpect(model().attributeExists("location"))
                .andExpect(model().attributeExists("listLocations"))
                .andExpect(model().attributeExists("pageTitle"));

    }

    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("지역 활성화/비활성화 by 관리자")
    @Test
    void updateEnabledLocation() throws Exception {
        Location location=new Location();
        location.setName("테스트지역");
        location.setEnabled(true);
        locationService.save(location);
        Location testLocation = locationService.getByName("테스트지역");
        Integer id = testLocation.getId();

        mockMvc.perform(get("/locations/"+id+"/enabled/false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/locations"))
                .andExpect(flash().attributeExists("message"));;

    }

    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("지역 삭제 by 관리자")
    @Test
    void deleteLocation() throws Exception {
        Location location=new Location();
        location.setName("테스트지역");
        location.setEnabled(true);
        locationService.save(location);
        Location testLocation = locationService.getByName("테스트지역");
        Integer id = testLocation.getId();

        mockMvc.perform(get("/locations/delete/"+id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/locations"))
                .andExpect(flash().attributeExists("message"));

        assertThat(locationService.getByName("테스트지역")).isNull();

    }



    
    

}