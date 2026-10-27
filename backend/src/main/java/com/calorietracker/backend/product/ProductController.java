package com.calorietracker.backend.product;

import com.calorietracker.backend.auth.CurrentUser;
import com.calorietracker.backend.product.dto.CreateCustomProductRequest;
import com.calorietracker.backend.product.dto.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/search")
    public List<ProductResponse> search(@RequestParam("q") String query,
                                        @AuthenticationPrincipal Jwt jwt) {
        return productService.search(query, CurrentUser.id(jwt));
    }

    @PostMapping("/custom")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createCustom(@Valid @RequestBody CreateCustomProductRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return productService.createCustom(request, CurrentUser.id(jwt));
    }
}