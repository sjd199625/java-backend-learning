package com.example.warehouse.product;

import com.example.warehouse.common.BusinessException;
import com.example.warehouse.common.ErrorCode;
import com.example.warehouse.common.PageResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * 商品业务。
 * 这里放“编号不能重复、商品必须存在”等规则。Controller 不写这些 if。
 */
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse create(ProductCreateRequest request) {
        String sku = request.sku().trim();
        if (productRepository.existsBySku(sku)) {
            throw BusinessException.conflict(ErrorCode.PRODUCT_SKU_DUPLICATE, "商品编号已存在: " + sku);
        }

        LocalDateTime now = LocalDateTime.now();
        Product product = new Product();
        product.setSku(sku);
        product.setName(request.name().trim());
        product.setUnit(request.unit().trim());
        product.setPrice(request.price());
        product.setStock(0);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        return ProductResponse.from(productRepository.save(product));
    }

    public ProductResponse getById(Long id) {
        return ProductResponse.from(requireProduct(id));
    }

    public PageResult<ProductResponse> list(String keyword, int page, int size) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<ProductResponse> all = productRepository.findAll().stream()
                .filter(product -> matches(product, normalizedKeyword))
                .map(ProductResponse::from)
                .toList();

        int fromIndex = Math.min((page - 1) * size, all.size());
        int toIndex = Math.min(fromIndex + size, all.size());
        return new PageResult<>(all.subList(fromIndex, toIndex), all.size(), page, size);
    }

    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = requireProduct(id);
        product.setName(request.name().trim());
        product.setUnit(request.unit().trim());
        product.setPrice(request.price());
        product.setUpdatedAt(LocalDateTime.now());
        return ProductResponse.from(productRepository.save(product));
    }

    public void delete(Long id) {
        requireProduct(id);
        productRepository.deleteById(id);
    }

    public Product requireProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound(ErrorCode.PRODUCT_NOT_FOUND, "商品不存在"));
    }

    private boolean matches(Product product, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        return product.getSku().toLowerCase(Locale.ROOT).contains(keyword)
                || product.getName().toLowerCase(Locale.ROOT).contains(keyword);
    }
}
