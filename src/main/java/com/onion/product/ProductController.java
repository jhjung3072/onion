package com.onion.product;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onion.ControllerHelper;
import com.onion.FileUploadUtil;
import com.onion.location.Location;
import com.onion.tag.Tag;
import com.onion.user.User;
import com.onion.product.product.Product;
import com.onion.exception.ProductNotFoundException;
import com.onion.location.LocationService;

import com.onion.paging.PagingAndSortingHelper;
import com.onion.paging.PagingAndSortingParam;
import com.onion.tag.TagForm;
import com.onion.tag.TagRepository;
import com.onion.tag.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;


@Controller
@RequiredArgsConstructor
public class ProductController {
	private String defaultRedirectURL = "redirect:/products/page/1?sortField=createdTime&sortDir=desc&locationId=";
	private final ProductService productService;
	private final LocationService locationService;
	private final ControllerHelper controllerHelper;
	private final TagRepository tagRepository;
	private final ObjectMapper objectMapper;
	private final TagService tagService;

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

			String productExtraImagesDir = "../product-images/" + id + "/extras";
			String productImagesDir = "../product-images/" + id;

			FileUploadUtil.removeDir(productExtraImagesDir);
			FileUploadUtil.removeDir(productImagesDir);

			productService.delete(id);
			// 물건에 해당하는 메인 이미지와 보조이미지 삭제
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

	// 물건 키워드 검색
	@GetMapping("/search")
	public String searchFirstPage(String keyword, Model model) {
		return searchByPage(keyword, 1, model);
	}

	// 키워드 검색 페이징
	@GetMapping("/search/page/{pageNum}")
	public String searchByPage(String keyword,
							   @PathVariable("pageNum") int pageNum,
							   Model model) {
		Page<Product> pageProducts = productService.search(keyword, pageNum);
		List<Product> listResult = pageProducts.getContent();

		long startCount = (pageNum - 1) * ProductService.PRODUCTS_PER_PAGE + 1;
		long endCount = startCount + ProductService.PRODUCTS_PER_PAGE - 1;
		if (endCount > pageProducts.getTotalElements()) {
			endCount = pageProducts.getTotalElements();
		}

		model.addAttribute("currentPage", pageNum);
		model.addAttribute("totalPages", pageProducts.getTotalPages());
		model.addAttribute("startCount", startCount);
		model.addAttribute("endCount", endCount);
		model.addAttribute("totalItems", pageProducts.getTotalElements());
		model.addAttribute("pageTitle", keyword + " - 검색 결과");

		model.addAttribute("keyword", keyword);
		model.addAttribute("searchKeyword", keyword);
		model.addAttribute("listResult", listResult);

		return "products/search_result";
	}


	@GetMapping("/my-products/edit/{id}/tags")
	public String ProductTagsForm(HttpServletRequest request, @PathVariable("id") Integer id,Model model)
			throws JsonProcessingException, ProductNotFoundException {
		User user=controllerHelper.getAuthenticatedUser(request);
		Product product = productService.get(id);
		model.addAttribute(user);
		model.addAttribute(product);

		model.addAttribute("tags", product.getTags().stream()
				.map(Tag::getTitle).collect(Collectors.toList()));
		List<String> allTagTitles = tagRepository.findAll().stream()
				.map(Tag::getTitle).collect(Collectors.toList());
		model.addAttribute("whitelist", objectMapper.writeValueAsString(allTagTitles));
		return "products/product_tags";
	}

	@PostMapping("/my-products/edit/{id}/tags/add")
	@ResponseBody
	public ResponseEntity addTag(@PathVariable("id") Integer id, @RequestBody TagForm tagForm) throws ProductNotFoundException {
		Product product = productService.get(id);
		Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
		productService.addTag(product, tag);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/my-products/edit/{id}/tags/remove")
	@ResponseBody
	public ResponseEntity removeTag(@PathVariable("id") Integer id, @RequestBody TagForm tagForm) throws ProductNotFoundException {
		Product product = productService.get(id);
		Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
		if (tag == null) {
			return ResponseEntity.badRequest().build();
		}

		productService.removeTag(product, tag);
		return ResponseEntity.ok().build();
	}

}
