package com.onion.watchlist;

import com.onion.user.User;
import com.onion.product.product.Product;
import com.onion.infra.MockMvcTest;
import com.onion.location.LocationRepository;
import static org.assertj.core.api.Assertions.assertThat;
import com.onion.product.ProductService;
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
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class WatchlistRestControllerTests {

    @Autowired MockMvc mockMvc;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductService productService;
    @Autowired
    LocationRepository locationRepository;

    @Autowired
    RoleService roleService;

    @Autowired WatchlistRepository watchlistRepository;

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
    @DisplayName("?????? ?????? ??????????????? ??????")
    @Test
    void addProductToWatchlist() throws Exception {

        User newUser=userService.getUserByEmail("test@email.com");
        Product newProduct=new Product();
        newProduct.setName("???????????????");
        newProduct.setPrice(100);
        newProduct.setMainImage("abc.jpg");
        newProduct.setShortDescription("????????? ?????? ??????");
        newProduct.setFullDescription("????????? ??? ??????");
        newProduct.setLocation(newUser.getLocation());
        Product savedProduct = productService.save(newProduct, newUser);

        mockMvc.perform(post("/watchlist/add/"+savedProduct.getId())
                        .with(csrf()))
                        .andExpect(status().isOk())
                        .andDo(print())
                        .andReturn();


        boolean result = newProduct.getWatchlist().contains(savedProduct);
        assertThat(result).isNotNull();

    }

    @WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("?????????????????? ?????? ?????? ??????")
    @Test
    void removeProductToWatchlist() throws Exception {

        User newUser=userService.getUserByEmail("test@email.com");
        Product newProduct=new Product();
        newProduct.setName("???????????????");
        newProduct.setPrice(100);
        newProduct.setMainImage("abc.jpg");
        newProduct.setShortDescription("????????? ?????? ??????");
        newProduct.setFullDescription("????????? ??? ??????");
        newProduct.setLocation(newUser.getLocation());
        Product savedProduct = productService.save(newProduct, newUser);


        mockMvc.perform(delete("/watchlist/remove/"+savedProduct.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();


        Watchlist result = watchlistRepository.findByUserAndProduct(newUser, savedProduct);
        assertThat(result).isNull();

    }

    @DisplayName("??????????????? ?????? ????????? ?????? ?????? ??????????????? ?????? ??????")
    @Test
    void addProductToWatchlistNotLogin() throws Exception {
        User newUser=userService.getUserByEmail("test@email.com");
        Product newProduct=new Product();
        newProduct.setName("???????????????");
        newProduct.setPrice(100);
        newProduct.setMainImage("abc.jpg");
        newProduct.setShortDescription("????????? ?????? ??????");
        newProduct.setFullDescription("????????? ??? ??????");
        newProduct.setLocation(newUser.getLocation());
        Product savedProduct = productService.save(newProduct, newUser);

        MvcResult mvcResult=mockMvc.perform(post("/watchlist/add/"+savedProduct.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();


        assertThat(mvcResult.getResponse().getContentAsString()).contains("???????????? ???????????????.");


    }

    @DisplayName("??????????????? ?????? ????????? ?????????????????? ?????? ?????? ?????? ??????")
    @Test
    void removeProductToWatchlistNotLogin() throws Exception {
        User newUser=userService.getUserByEmail("test@email.com");
        Product newProduct=new Product();
        newProduct.setName("???????????????");
        newProduct.setPrice(100);
        newProduct.setMainImage("abc.jpg");
        newProduct.setShortDescription("????????? ?????? ??????");
        newProduct.setFullDescription("????????? ??? ??????");
        newProduct.setLocation(newUser.getLocation());
        Product savedProduct = productService.save(newProduct, newUser);


        MvcResult mvcResult=mockMvc.perform(delete("/watchlist/remove/"+savedProduct.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();


        assertThat(mvcResult.getResponse().getContentAsString()).contains("???????????? ???????????????.");


    }
}