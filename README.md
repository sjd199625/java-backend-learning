# Java 后端从零学习项目

这是一个迷你仓储后台。当前已经包含登录、商品 CRUD 和出入库，方便对照前端学习接口是怎么写出来的。

## 请求怎么走进后端

```text
浏览器 Axios
    ↓  Authorization: Bearer <JWT>
Tomcat
    ↓
AuthInterceptor          校验 Token，把当前用户放进 ThreadLocal
    ↓
DispatcherServlet        按 URL 找到 Controller 方法
    ↓
参数绑定 + @Valid         JSON / Path / Query 转成 Java 对象
    ↓
Controller               只负责收参数、返回 Result
    ↓
Service                  业务规则：编号不能重复、库存不能为负
    ↓
Repository               JdbcTemplate 读写本机 MySQL（库名 warehouse）
    ↓
Result<T> + HTTP 状态码
    ↓
Jackson 序列化成 JSON
```

对应代码：

| 环节 | 类 |
|---|---|
| 统一响应 | `common/Result.java` |
| 业务异常 | `common/BusinessException.java` |
| 异常转 JSON | `common/GlobalExceptionHandler.java` |
| 登录拦截 | `auth/AuthInterceptor.java` |
| JWT | `auth/JwtService.java` |
| 登录接口 | `auth/AuthController.java` |
| 商品接口 | `product/ProductController.java` |
| 出入库 | `inventory/InventoryController.java` |

为什么 Controller 里几乎没有 if：校验失败由 `@Valid` 拦下，商品不存在由 Service 抛 `BusinessException`，最后都进 `GlobalExceptionHandler`。前端永远拿到 `{ code, message, data }`。

## 接口一览

演示账号：`admin` / `admin123`

所有业务接口（除登录和健康检查）都要带：

```text
Authorization: Bearer <登录返回的 token>
```

| 方法 | 路径 | 说明 | 成功状态码 |
|---|---|---|---:|
| GET | `/api/health` | 健康检查，无需登录 | 200 |
| POST | `/api/auth/login` | 登录，Body: `{username, password}` | 200 |
| GET | `/api/auth/me` | 当前用户，响应不含密码 | 200 |
| POST | `/api/auth/logout` | 注销当前 Token | 200 |
| GET | `/api/products` | 商品分页，Query: `keyword,page,size` | 200 |
| GET | `/api/products/{id}` | 商品详情 | 200 |
| POST | `/api/products` | 新增商品，库存从 0 开始 | 201 |
| PUT | `/api/products/{id}` | 改名称/单位/单价，不能改编号 | 200 |
| DELETE | `/api/products/{id}` | 删除商品 | 200 |
| POST | `/api/inventory/in` | 入库 | 201 |
| POST | `/api/inventory/out` | 出库，库存不足返回 409 | 201 |
| GET | `/api/inventory/records` | 库存流水 | 200 |

常见失败码：`401 UNAUTHORIZED`、`400 VALIDATION_ERROR`、`404 PRODUCT_NOT_FOUND`、`409 PRODUCT_SKU_DUPLICATE`、`409 STOCK_NOT_ENOUGH`。

## 运行

需要 JDK 17+、Maven 3.9+。在本目录执行：

```bash
mvn test
mvn spring-boot:run
```

登录示例：

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
```

把返回的 `data.token` 填到下面：

```bash
curl http://localhost:8080/api/products/productList \
  -H 'Authorization: Bearer 这里换成 token'
```

数据存在本机 MySQL 的 `warehouse` 库里，重启后端不会丢。库空时会写入演示账号 `admin` / `admin123`。

需要本机已启动 MySQL：`brew services start mysql`。

## 学习时建议按这个顺序看代码

1. `AuthController` + `LoginRequest`：`@RequestBody` 和 `@Valid` 怎么接 JSON。
2. `JwtService` + `AuthInterceptor`：登录后请求如何带上身份。
3. `ProductController` + `ProductService`：Path、Query、CRUD、DTO 和实体为什么要分开。
4. `InventoryService`：出库为什么不能直接 `stock - n`。
5. `GlobalExceptionHandler`：Service 抛异常后，前端为什么还能收到 JSON。
