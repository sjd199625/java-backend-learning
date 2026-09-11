package com.example.warehouse.product;

import com.example.warehouse.common.PageResult;
import com.example.warehouse.common.Result;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品 REST 接口，对应前端“商品管理”页。
 *
 * 注解怎么对应 HTTP：
 * - @GetMapping    → GET    查询，不应修改数据
 * - @PostMapping   → POST   新增
 * - @PutMapping    → PUT    按 id 全量更新资料
 * - @DeleteMapping → DELETE 删除
 *
 * 参数从哪里来：
 * - @PathVariable  路径里的 {id}
 * - @RequestParam  URL 查询串 ?keyword=鼠标&page=1
 * - @RequestBody   JSON 请求体
 */
@Validated
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    public ResponseEntity<Result<ProductResponse>> create(
            @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductResponse created = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.success("商品已创建", created));
    }

    @GetMapping("/productList/{id}")
    public Result<ProductResponse> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @GetMapping("/productList")
    public Result<PageResult<ProductResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page 从 1 开始") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "size 至少为 1")
            @Max(value = 100, message = "size 不能超过 100") Integer size
    ) {
        return Result.success(productService.list(keyword, page, size));
    }

    @PutMapping("/update/{id}")
    public Result<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return Result.success("商品已更新", productService.update(id, request));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success("商品已删除", null);
    }
}
