package com.onion.product;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.onion.ControllerHelper;
import com.onion.FileUploadUtil;
import com.onion.config.OnionUserDetails;
import com.onion.config.Utility;
import com.onion.domain.Location;
import com.onion.domain.User;
import com.onion.domain.product.Product;
import com.onion.exception.ProductNotFoundException;
import com.onion.location.LocationService;

import com.onion.paging.PagingAndSortingHelper;
import com.onion.paging.PagingAndSortingParam;
import com.onion.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;


@Controller
public class ProductController {
	private String defaultRedirectURL = "redirect:/products/page/1?sortField=createdTime&sortDir=desc&locationId=";
	@Autowired private ProductService productService;
	@Autowired private LocationService locationService;
	@Autowired private ControllerHelper controllerHelper;
	
	// 물건 목록 페이지 GET
	@GetMapping("/products")
	public String listFirstPage(Model model,HttpServletRequest request) {
		User user = controllerHelper.getAuthenticatedUser(request);
		Integer locationId=user.getLocation().getId();

		return defaultRedirectURL+locationId;
	}
	
	// 물건 목록 페이징 GET
	@GetMapping("/products/page/{pageNum}")
	public String listByPage(
			@PagingAndSortingParam(listName = "listProducts", moduleURL = "/products") PagingAndSortingHelper helper,
			@PathVariable(name = "pageNum") int pageNum, Model model,
			Integer locationId
			) {
		
		productService.listByPage(pageNum, helper, locationId);
		
		List<Location> listLocations = locationService.listLocationsUsedInForm();
		
		if (locationId != null) model.addAttribute("locationId", locationId);
		model.addAttribute("listLocations", listLocations);
		
		return "products/products";		
	}
	
	// 물건 추가 폼 GET
	@GetMapping("/my-products/new")
	public String newProduct(Model model, HttpServletRequest request) {
		User user = controllerHelper.getAuthenticatedUser(request);
		Location location = user.getLocation();
		
		Product product = new Product();

		model.addAttribute("user",user);
		model.addAttribute("product", product);
		model.addAttribute("location", location);
		model.addAttribute("pageTitle", "내 물건 팔기");
		model.addAttribute("numberOfExistingExtraImages", 0);
		
		return "products/product_form";
	}
	
	// 물건 추가 POST
	@PostMapping("/my-products/save")
	public String saveProduct(Product product, RedirectAttributes ra,
			@RequestParam(value = "fileImage", required = false) MultipartFile mainImageMultipart,
			@RequestParam(value = "extraImage", required = false) MultipartFile[] extraImageMultiparts,
			@RequestParam(name = "imageIDs", required = false) String[] imageIDs,
			@RequestParam(name = "imageNames", required = false) String[] imageNames, HttpServletRequest request
			) throws IOException {

		User seller=controllerHelper.getAuthenticatedUser(request);


		ProductSaveHelper.setMainImageName(mainImageMultipart, product);
		ProductSaveHelper.setExistingExtraImageNames(imageIDs, imageNames, product);
		ProductSaveHelper.setNewExtraImageNames(extraImageMultiparts, product);

		Product savedProduct = productService.save(product,seller);

		ProductSaveHelper.saveUploadedImages(mainImageMultipart, extraImageMultiparts, savedProduct);

		ProductSaveHelper.deleteExtraImagesWeredRemovedOnForm(product);

		ra.addFlashAttribute("message", "물건이 저장되었습니다.");

		return defaultRedirectURL+seller.getLocation().getId();
	}

	// 물건 목록 페이지 GET
	@GetMapping("/my-products")
	public String listMyProductsFirstPage(Model model,HttpServletRequest request) {
		User user = controllerHelper.getAuthenticatedUser(request);
		Integer locationId=user.getLocation().getId();

		return "redirect:/my-products/page/1?sortField=createdTime&sortDir=desc";
	}

	// 로그인한 유저의 물건 목록 페이지 GET
	@GetMapping("/my-products/page/{pageNum}")
	public String listProductsForSeller(Model model, HttpServletRequest request,
										@PathVariable(name = "pageNum") int pageNum,
										String keyword, String sortField, String sortDir) {
		User user = controllerHelper.getAuthenticatedUser(request);
		Page<Product> page=productService.listProductsBySeller(user,pageNum, sortField, sortDir);
		List<Product> listProducts=page.getContent();

		model.addAttribute("totalPages", page.getTotalPages());
		model.addAttribute("totalItems", page.getTotalElements());
		model.addAttribute("currentPage", pageNum);
		model.addAttribute("sortField", sortField);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("keyword", keyword);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		model.addAttribute("moduleURL", "/my-products");


		long startCount = (pageNum - 1) * productService.PRODUCTS_PER_PAGE + 1;
		model.addAttribute("startCount", startCount);

		long endCount = startCount + productService.PRODUCTS_PER_PAGE - 1;
		if (endCount > page.getTotalElements()) {
			endCount = page.getTotalElements();
		}

		model.addAttribute("endCount", endCount);
		model.addAttribute("pageTitle", "내 물건 목록");
		model.addAttribute("listProducts", listProducts);

		return "products/products_by_seller";
	}



	// 로그인한 유저의 물건 삭제 GET
	@GetMapping("/my-products/delete/{id}")
	public String deleteProduct(@PathVariable(name = "id") Integer id,
			Model model, RedirectAttributes redirectAttributes) {
		try {
			productService.delete(id);
			// 물건에 해당하는 메인 이미지와 보조이미지 삭제
			String productExtraImagesDir = "../product-images/" + id + "/extras";
			String productImagesDir = "../product-images/" + id;

			FileUploadUtil.removeDir(productExtraImagesDir);
			FileUploadUtil.removeDir(productImagesDir);

			redirectAttributes.addFlashAttribute("message",
					"물건 ID: " + id + "가 삭제되었습니다.");
		} catch (ProductNotFoundException ex) {
			redirectAttributes.addFlashAttribute("message", ex.getMessage());
		}

		return defaultRedirectURL;
	}

	// 물건 수정
	@GetMapping("/my-products/edit/{id}")
	public String editProduct(@PathVariable("id") Integer id, Model model,
			RedirectAttributes ra, HttpServletRequest request) {
		try {
			User user = controllerHelper.getAuthenticatedUser(request);
			Product product = productService.get(id);
			Integer numberOfExistingExtraImages = product.getImages().size();

			model.addAttribute("user", user);
			model.addAttribute("product", product);
			model.addAttribute("pageTitle", "물건 수정 (ID: " + id + ")");
			model.addAttribute("numberOfExistingExtraImages", numberOfExistingExtraImages);

			return "products/product_form";

		} catch (ProductNotFoundException e) {
			ra.addFlashAttribute("message", e.getMessage());

			return defaultRedirectURL;
		}
	}

	// 물건 상세 페이지 GET
	@GetMapping("/p/{product_id}")
	public String viewProductDetail(@PathVariable("product_id") Integer id, Model model,
									HttpServletRequest request) {

		try {
			Product product = productService.get(id);

			User user = controllerHelper.getAuthenticatedUser(request);

			boolean canEdit=true;

			if (user==null || (user.getId() != product.getSeller().getId())) {
				model.addAttribute("canEdit",false);
			}else{
				model.addAttribute("canEdit",true);
			}

			model.addAttribute("product", product);
			model.addAttribute("pageTitle", product.getName());

			return "products/product_detail";
		} catch (ProductNotFoundException e) {
			return "error/404";
		}
	}

}
