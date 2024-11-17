package nl.growguru.app.api.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import nl.growguru.app.models.shop.Category;
import nl.growguru.app.models.shop.Product;
import nl.growguru.app.services.ProductService;
import nl.growguru.app.utils.SecurityContextUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/product")

@RequiredArgsConstructor

@SecurityRequirement(name = "Authorization")
public class ProductController {
    private final ProductService productService;

    @GetMapping("/all")
    public List<Product> getAllProducts() {
        return productService.findAll();
    }

    @GetMapping("/available")
    public List<Product> getAllAvailable() {
        return productService.findAllExcludingOwned();
    }

    @GetMapping("/allFromUser")
    public List<Product> getAllProductsOwnedByUser() {
        return productService.findAllOwnedByUser(SecurityContextUtil.getCurrentGrowGuru());
    }

    @GetMapping("/id/{id}")
    public Product findById(@PathVariable UUID id) {
        return productService.findById(id);
    }

    @GetMapping("/name/{name}")
    public Product findByName(@PathVariable String name) {
        return productService.findByName(name);
    }

    @GetMapping("/category/{category}")
    public List<Product> findByCategory(@PathVariable Category category) {
        return productService.findAllWithSameCategory(category);
    }

    @PostMapping("/{id}")
    @Operation(summary = "Buy a product", description = "Process a product purchase or initiate a payment process")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Product purchase accepted."),
            @ApiResponse(responseCode = "201", description = "Redirect to payment gateway required, link in location header."),
            @ApiResponse(responseCode = "409", description = "Product (non-currency) is already owned."),
            @ApiResponse(responseCode = "418", description = "Too broke."),
    })
    public ResponseEntity<Void> buyProduct(@PathVariable UUID id) {
        return productService.buyProduct(id);
    }

}