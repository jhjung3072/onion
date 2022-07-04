package com.onion.main;

import com.onion.ControllerHelper;
import com.onion.product.ProductRepository;
import com.onion.user.RoleService;
import com.onion.user.User;
import com.onion.product.product.Product;
import com.onion.location.LocationService;
import com.onion.product.ProductService;
import com.onion.user.RoleRepository;
import com.onion.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class MainController {

    private String defaultRedirectURL = "redirect:/products/page/1?sortField=name&sortDir=asc&locationId=";
    @Autowired private ProductService productService;
    @Autowired private LocationService locationService;
    @Autowired private ControllerHelper controllerHelper;
    @Autowired private RoleService roleService;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;

    // 인증받지 않은 사용자(비로그인 사용자)일 경우에 로그인 페이지 GET
    @GetMapping("/login")
    public String viewLoginPage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "login";
        }

        return "redirect:/";
    }

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request){
        User user = controllerHelper.getAuthenticatedUser(request);
        if (user != null) {
            model.addAttribute("listProducts", productService.list9RecentlyRegisteredProductForUser(user));
            return "index-after-login";
        }

        Page<Product> listProductsForAll=productService.list9RecentlyRegisteredProductForAll();
        model.addAttribute("listProducts", listProductsForAll);
        return "index";
    }
}
