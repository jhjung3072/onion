package com.onion.product;

import com.onion.user.User;
import com.onion.product.product.Product;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
public class ProductControllerTests {

	@Autowired MockMvc mockMvc;
	@Autowired UserService userService;
	@Autowired UserRepository userRepository;

	@Autowired LocationService locationService;
	@Autowired LocationRepository locationRepository;
	@Autowired ProductService productService;
	@Autowired ProductRepository productRepository;

	@Autowired RoleService roleService;

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
		productRepository.deleteAll();
	}

	@WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	@DisplayName("????????????????????? ???")
	@Test
	void viewProducts() throws Exception {
		User user=userService.getUserByEmail("test@email.com");
		Integer id = user.getLocation().getId();
		mockMvc.perform(get("/products"))
				.andExpect(redirectedUrl("/products/page/1?sortField=createdTime&sortDir=desc&locationId="+id));
	}

	@WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	@DisplayName("??? ?????? ?????? ???")
	@Test
	void viewSellMyProduct() throws Exception {
		mockMvc.perform(get("/my-products/new"))
				.andExpect(view().name("products/product_form"))
				.andExpect(model().attributeExists("user"))
				.andExpect(model().attributeExists("product"))
				.andExpect(model().attributeExists("location"))
				.andExpect(model().attributeExists("pageTitle"))
				.andExpect(model().attributeExists("numberOfExistingExtraImages"));
	}

	@Test
	@WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	@DisplayName("??? ?????? ??????")
	void sellMyProduct() throws Exception {
		User user=userService.getUserByEmail("test@email.com");
		Integer id = user.getLocation().getId();
		Product newProduct=new Product();
		newProduct.setName("???????????????");
		newProduct.setPrice(100);
		newProduct.setMainImage("asdad.jpg");
		newProduct.setShortDescription("????????? ?????? ??????");
		newProduct.setFullDescription("????????? ??? ??????");
		newProduct.setLocation(user.getLocation());

		Product savedProduct = productService.save(newProduct, user);

		assertThat(savedProduct).isNotNull();
	}

	@WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	@DisplayName("?????? ???????????? ?????? ?????????")
	@Test
	void viewMyProducts() throws Exception {
		mockMvc.perform(get("/my-products"))
				.andExpect(redirectedUrl("/my-products/page/1?sortField=createdTime&sortDir=desc"));
	}

	@WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	@DisplayName("?????? ?????? by ??????")
	@Test
	void deleteProduct() throws Exception {
		User newUser=userService.getUserByEmail("test@email.com");
		Product newProduct=new Product();
		newProduct.setName("???????????????");
		newProduct.setPrice(100);
		newProduct.setMainImage("abc.jpg");
		newProduct.setShortDescription("????????? ?????? ??????");
		newProduct.setFullDescription("????????? ??? ??????");
		newProduct.setLocation(newUser.getLocation());
		Product savedProduct = productService.save(newProduct, newUser);


		mockMvc.perform(get("/my-products/delete/"+savedProduct.getId()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/products/page/1?sortField=createdTime&sortDir=desc&locationId="))
				.andExpect(flash().attributeExists("message"));

		assertThat(productRepository.findById(savedProduct.getId())).isEmpty();

	}

	@WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	@DisplayName("??? ?????? ?????? ???")
	@Test
	void viewUpdateMyProductForm() throws Exception {
		User newUser=userService.getUserByEmail("test@email.com");
		Product newProduct=new Product();
		newProduct.setName("???????????????");
		newProduct.setPrice(100);
		newProduct.setShortDescription("????????? ?????? ??????");
		newProduct.setMainImage("main image.jpg");
		newProduct.setFullDescription("????????? ??? ??????");
		newProduct.setLocation(newUser.getLocation());
		Product savedProduct = productService.save(newProduct, newUser);

		mockMvc.perform(get("/my-products/edit/"+savedProduct.getId()))
				.andExpect(view().name("products/product_form"))
				.andExpect(model().attributeExists("user"))
				.andExpect(model().attributeExists("product"))
				.andExpect(model().attributeExists("pageTitle"))
				.andExpect(model().attributeExists("numberOfExistingExtraImages"));
	}

	@WithUserDetails(value = "test@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	@DisplayName("?????? ?????? ?????????")
	@Test
	void viewProductDetail() throws Exception {
		User newUser=userService.getUserByEmail("test@email.com");
		Product newProduct=new Product();
		newProduct.setName("???????????????");
		newProduct.setPrice(100);
		newProduct.setShortDescription("????????? ?????? ??????");
		newProduct.setMainImage("main image.jpg");
		newProduct.setLocation(newUser.getLocation());
		newProduct.setFullDescription("????????? ??? ??????");
		newProduct.setLocation(newUser.getLocation());
		Product savedProduct = productService.save(newProduct, newUser);

		mockMvc.perform(get("/p/"+savedProduct.getId()))
				.andExpect(view().name("products/product_detail"))
				.andExpect(model().attributeExists("product"))
				.andExpect(model().attributeExists("pageTitle"))
				.andExpect(model().attributeExists("canEdit"));
	}

}
	

