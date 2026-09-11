# 接口联调与异常处理学习目标

> 目标：能够独立开发和调试 Spring Boot REST API，并完成参数绑定、日期处理、CORS、敏感字段保护、统一响应、参数校验和异常处理闭环。

## 1. 最终应具备的能力

- 使用 Postman、Apifox、IDEA HTTP Client 或 curl 调试接口。
- 理解 HTTP 请求进入 Spring Boot 后的处理链路。
- 正确接收 Path、Query、Header 和 JSON Body 参数。
- 根据状态码、响应、日志和 SQL 定位问题。
- 处理日期格式、CORS 和敏感字段泄露。
- 使用请求 DTO、响应 DTO 和 `@Valid`。
- 定义业务异常并进行全局异常处理。
- 实现统一响应，同时正确使用 HTTP 状态码。
- 完成商品 CRUD 的前后端联调。

## 2. 请求处理链路

```text
前端 Axios / fetch
    ↓ HTTP 请求
Tomcat
    ↓
Filter / Interceptor
    ↓
DispatcherServlet
    ↓ 路由匹配、参数绑定、参数校验
Controller
    ↓
Service
    ↓
Repository / Mapper
    ↓
数据库
    ↓
Java 对象
    ↓ Jackson 序列化
HTTP 状态码 + JSON 响应
    ↓
前端
```

需要能够解释：

1. URL 如何匹配到 Controller 方法？
2. Query、Path 和 JSON 如何转换为 Java 参数？
3. Java 对象为什么会自动转换成 JSON？
4. Service 抛出异常后，前端为什么还能收到 JSON？
5. 请求成功但数据没有变化时，应该检查哪些环节？

## 3. API 调试工具

至少熟练使用一种图形化工具，并会使用 curl：

- Postman 或 Apifox
- IDEA HTTP Client
- curl

每次调试检查：

- 请求方法、URL、端口。
- Query、Path、Header 参数。
- `Content-Type` 和 JSON Body。
- HTTP 状态码、响应头和响应体。
- 后端日志和实际 SQL。
- 数据库记录是否发生预期变化。

## 4. Spring MVC 参数绑定

| 注解 | 使用场景 |
|---|---|
| `@RequestParam` | URL 查询参数 |
| `@PathVariable` | URL 路径参数 |
| `@RequestBody` | JSON 请求体 |
| `@RequestHeader` | 请求头 |
| `@RequestPart` | 文件上传 |
| `@DateTimeFormat` | Query、Path、表单日期 |
| `@JsonFormat` | JSON 日期转换 |
| `@Valid` | 请求 DTO 校验 |

示例：

```java
@GetMapping("/{id}")
public ProductResponse getById(@PathVariable Long id) {
    return productService.getById(id);
}

@GetMapping
public List<ProductResponse> list(
        @RequestParam(required = false) String name,
        @RequestParam(defaultValue = "1") Integer page) {
    return productService.list(name, page);
}

@PostMapping
public ProductResponse create(@RequestBody ProductCreateRequest request) {
    return productService.create(request);
}
```

## 5. HTTP 状态码

| 状态码 | 含义 | 常见场景 |
|---:|---|---|
| 200 | 成功 | 查询、修改成功 |
| 201 | 创建成功 | 新增商品成功 |
| 400 | 参数错误 | JSON、类型或校验错误 |
| 401 | 未认证 | 未登录或 Token 无效 |
| 403 | 无权限 | 已登录但权限不足 |
| 404 | 不存在 | 路径或业务数据不存在 |
| 405 | 方法不支持 | GET、POST 使用错误 |
| 409 | 数据冲突 | 编号重复、状态冲突 |
| 415 | 媒体类型错误 | `Content-Type` 不正确 |
| 500 | 服务异常 | 未处理的程序异常 |

## 6. 日期时间处理

Query 参数使用 `@DateTimeFormat`：

```java
@GetMapping
public List<OrderResponse> list(
        @RequestParam
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime startTime) {
    return orderService.list(startTime);
}
```

JSON 日期可以使用 `@JsonFormat`：

```java
public record CreateOrderRequest(
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime expectedTime
) {
}
```

推荐优先使用 ISO 8601：

```json
{
  "expectedTime": "2026-08-18T10:30:00"
}
```

需要学习：`LocalDate`、`LocalTime`、`LocalDateTime`、`Instant`、`ZoneId`、ISO 8601、UTC 和本地时区。

## 7. CORS 跨域

当前端为 `http://localhost:5173`、后端为 `http://localhost:8080` 时，因为端口不同，浏览器判定为不同源。

临时局部配置：

```java
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/products")
public class ProductController {
}
```

推荐全局配置：

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

需要理解同源策略、简单请求、`OPTIONS` 预检请求、CORS 响应头、Cookie 跨域和 Spring Security CORS。

> `origins = "*"` 只适合临时学习。正式环境应明确配置允许的前端域名。

## 8. 敏感字段保护与 DTO

`@JsonIgnore` 可以防止字段被序列化：

```java
@JsonIgnore
private String password;
```

更推荐分离数据库对象、请求 DTO 和响应 DTO：

```java
public class UserEntity {
    private Long id;
    private String username;
    private String passwordHash;
}

public record LoginRequest(String username, String password) {
}

public record UserResponse(Long id, String username) {
}
```

安全要求：

- 数据库不保存明文密码，使用 BCrypt。
- 响应不返回密码或密码哈希。
- 日志不输出密码、Token 和密钥。
- 手机号、身份证号按业务要求脱敏。
- 密钥不能写死在代码或提交到 Git。

## 9. 统一响应结构

```java
public record Result<T>(String code, String message, T data) {

    public static <T> Result<T> success(T data) {
        return new Result<>("SUCCESS", "操作成功", data);
    }

    public static <T> Result<T> failure(String code, String message) {
        return new Result<>(code, message, null);
    }
}
```

失败响应：

```json
{
  "code": "PRODUCT_NOT_FOUND",
  "message": "商品不存在",
  "data": null
}
```

统一响应不能替代 HTTP 状态码。例如商品不存在时应使用 HTTP 404，并返回业务码 `PRODUCT_NOT_FOUND`。

## 10. 参数校验

添加依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

请求 DTO：

```java
public record ProductCreateRequest(
        @NotBlank(message = "商品名称不能为空")
        String name,

        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.01", message = "价格必须大于0")
        BigDecimal price
) {
}
```

Controller：

```java
@PostMapping
public Result<ProductResponse> create(
        @Valid @RequestBody ProductCreateRequest request) {
    return Result.success(productService.create(request));
}
```

常用注解：`@NotNull`、`@NotBlank`、`@Size`、`@Min`、`@Max`、`@DecimalMin`、`@Positive`、`@Email`、`@Pattern`、`@Valid`。

## 11. 业务异常与全局处理

业务异常：

```java
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
```

Service 中抛出：

```java
Product product = repository.findById(id)
        .orElseThrow(() -> new BusinessException(
                "PRODUCT_NOT_FOUND",
                "商品不存在"
        ));
```

全局异常处理：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusinessException(
            BusinessException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Result.failure(
                        exception.getCode(),
                        exception.getMessage()
                ));
    }
}
```

未知异常需要记录完整日志，但只向前端返回安全信息：

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<Result<Void>> handleUnknownException(Exception exception) {
    log.error("系统异常", exception);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Result.failure("INTERNAL_ERROR", "系统暂时不可用"));
}
```

至少分别处理参数校验失败、JSON 格式错误、类型转换错误、业务数据不存在、唯一约束冲突和未知异常。

## 12. 实战任务

实现内存版商品管理接口：

```http
POST   /api/products
GET    /api/products/{id}
GET    /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}
```

推荐顺序：

1. 商品 CRUD：学习类、`record`、`Map`、分层和依赖注入。
2. 参数校验：测试名称为空、价格为负数、JSON 错误、ID 类型错误。
3. 业务异常：测试商品不存在、商品编号重复。
4. 统一响应：实现 `Result<T>`，配合正确状态码。
5. 日期处理：加入 `createdAt`，测试 JSON、Query 和错误格式。
6. 前后端联调：处理 CORS、字段差异、错误提示、空值和分页。
7. 敏感字段保护：确保用户响应和日志不泄露密码。

## 13. 每个接口的检查流程

```text
请求 JSON / 参数
    ↓
Controller 实际接收的数据
    ↓
Service 业务逻辑
    ↓
Repository / SQL
    ↓
数据库或内存中的最终数据
    ↓
HTTP 状态码和响应 JSON
```

- [ ] 请求方法、URL 和参数位置正确。
- [ ] JSON 字段和 Java DTO 对应。
- [ ] 参数校验生效。
- [ ] 业务异常有明确错误码。
- [ ] HTTP 状态码正确。
- [ ] 响应和日志不包含敏感信息。
- [ ] 测试覆盖成功和失败场景。

## 14. 最终验收标准

- [ ] 能使用图形化 API 工具和 curl 调试接口。
- [ ] 能区分 Query、Path、Header 和 JSON Body。
- [ ] 能根据 HTTP 状态码判断问题方向。
- [ ] 能处理 Query 和 JSON 中的日期。
- [ ] 能解释 CORS 和 `OPTIONS` 预检请求。
- [ ] 使用请求 DTO 和响应 DTO。
- [ ] 响应不泄露密码等敏感字段。
- [ ] 实现 `Result<T>` 并正确搭配 HTTP 状态码。
- [ ] 使用 `@Valid` 校验参数。
- [ ] 定义和抛出业务异常。
- [ ] 使用 `@RestControllerAdvice` 统一处理异常。
- [ ] 未知异常记录日志但不向前端暴露堆栈。
- [ ] 调通 5 个商品 CRUD 接口。
- [ ] 为成功、参数错误和业务错误编写测试。
- [ ] 能结合请求、日志、SQL 和数据变化定位问题。

## 15. 核心学习主线

```text
Spring MVC 参数绑定
    ↓
API 调试与商品 CRUD
    ↓
DTO 与参数校验
    ↓
业务异常与全局处理
    ↓
统一响应与 HTTP 状态码
    ↓
日期处理与 CORS
    ↓
敏感字段保护
    ↓
完整前后端联调
```

完成本阶段后，再进入 MySQL 持久化、事务和库存核心业务。
