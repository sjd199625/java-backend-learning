package com.example.warehouse.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用 MockMvc 模拟 HTTP 调用，不需要真的启动浏览器。
 * 测试顺序刻意覆盖：未登录、登录失败、登录成功、CRUD、校验、库存不足。
 */
@SpringBootTest
@AutoConfigureMockMvc
class WarehouseApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void healthShouldBePublic() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.application").value("java-backend-learning"));
    }

    @Test
    void productsShouldRejectAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void loginShouldFailWhenPasswordWrong() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void shouldLoginAndManageProductsAndStock() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());

        String sku = "SKU-TEST-" + UUID.randomUUID().toString().substring(0, 8);
        MvcResult createResult = mockMvc.perform(post("/api/products/create")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "sku", sku,
                                "name", "测试商品",
                                "unit", "件",
                                "price", 12.5
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sku").value(sku))
                .andExpect(jsonPath("$.data.stock").value(0))
                .andReturn();

        long productId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .at("/data/id")
                .asLong();

        mockMvc.perform(post("/api/products/create")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "sku", sku,
                                "name", "重复编号",
                                "unit", "件",
                                "price", 1
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_SKU_DUPLICATE"));

        mockMvc.perform(post("/api/products/create")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"X","name":"","unit":"件","price":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        mockMvc.perform(put("/api/products/update/" + productId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "测试商品-改名",
                                "unit", "盒",
                                "price", 18
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("测试商品-改名"));

        mockMvc.perform(post("/api/inventory/in")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "productId", productId,
                                "quantity", 10,
                                "remark", "采购入库"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.type").value("IN"))
                .andExpect(jsonPath("$.data.stockAfter").value(10));

        mockMvc.perform(post("/api/inventory/out")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "productId", productId,
                                "quantity", 4,
                                "remark", "领用出库"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.stockAfter").value(6));

        mockMvc.perform(post("/api/inventory/out")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "productId", productId,
                                "quantity", 99
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("STOCK_NOT_ENOUGH"));

        mockMvc.perform(get("/api/products/productList/" + productId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stock").value(6));

        mockMvc.perform(delete("/api/products/delete/" + productId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        mockMvc.perform(get("/api/products/productList/" + productId).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    private String loginAndGetToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"admin123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .at("/data/token")
                .asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
