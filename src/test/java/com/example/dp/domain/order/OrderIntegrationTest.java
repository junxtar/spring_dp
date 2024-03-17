package com.example.dp.domain.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.dp.domain.admin.service.impl.AdminMenuServiceImpl;
import com.example.dp.domain.cart.dto.request.CartRequestMenuDto;
import com.example.dp.domain.cart.entity.Cart;
import com.example.dp.domain.cart.repository.CartRepository;
import com.example.dp.domain.cart.service.impl.CartServiceImpl;
import com.example.dp.domain.category.entity.Category;
import com.example.dp.domain.category.repository.CategoryRepository;
import com.example.dp.domain.menu.entity.Menu;
import com.example.dp.domain.menu.repository.MenuRepository;
import com.example.dp.domain.menucategory.entity.MenuCategory;
import com.example.dp.domain.menucategory.repository.MenuCategoryRepository;
import com.example.dp.domain.order.entity.Order;
import com.example.dp.domain.order.entity.OrderState;
import com.example.dp.domain.order.repository.OrderRepository;
import com.example.dp.domain.order.service.impl.OrderServiceImpl;
import com.example.dp.domain.ordermenu.repository.OrderMenuRepository;
import com.example.dp.domain.user.entity.User;
import com.example.dp.domain.user.entity.UserRole;
import com.example.dp.domain.user.repository.UserRepository;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderIntegrationTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    CartRepository cartRepository;
    @Autowired
    OrderMenuRepository orderMenuRepository;
    @Autowired
    MenuRepository menuRepository;
    @Autowired
    CartServiceImpl cartService;
    @Autowired
    OrderServiceImpl orderService;
    @Autowired
    AdminMenuServiceImpl adminMenuService;
    @Autowired
    MenuCategoryRepository menuCategoryRepository;
    @Autowired
    CategoryRepository categoryRepository;

    private User user;
    private User user2;

    private Menu menu1;
    private Menu menu2;

    private Category category1;
    private Category category2;

    private Cart cart1;
    private Cart cart2;


    @BeforeEach
    void setup() {
        user = userRepository.save(User.builder()
            .id(1L)
            .username("홍길동")
            .password("123456789")
            .email("junyeong237@gmail.com")
            .role(UserRole.USER)
            .build());

        user2 = userRepository.save(User.builder()
            .id(2L)
            .username("홍길동1")
            .password("123456789")
            .email("junyeong37@gmail.com")
            .role(UserRole.USER)
            .build());

        category1 = categoryRepository.save(Category.builder()
            .id(1L)
            .type("버거")
            .build());

        category2 = categoryRepository.save(Category.builder()
            .id(2L)
            .type("치킨")
            .build());

        menu1 = menuRepository.save(Menu.builder()
            .id(1L)
            .name("햄버거")
            .price(3000)
            .description("맛있는 햄버거")
            .quantity(30)
            .status(true)
            .build());

        menuCategoryRepository.save(MenuCategory.builder()
            .menu(menu1)
            .category(category1)
            .build());

        menu2 = menuRepository.save(Menu.builder()
            .id(2L)
            .name("치킨")
            .description("맛있는 치킨")
            .price(13000)
            .quantity(50)
            .status(true)
            .build());

        menuCategoryRepository.save(MenuCategory.builder()
            .menu(menu2)
            .category(category2)
            .build());

        cart1 = cartRepository.save(Cart.builder()
            .user(user)
            .menuCount(15)
            .totalPrice(10 * menu1.getPrice())
            .menu(menu1)
            .build());

        cart2 = cartRepository.save(Cart.builder()
            .user(user2)
            .totalPrice(10 * menu1.getPrice())
            .menuCount(15)
            .menu(menu1)
            .build());
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("주문하기_동시성_테스트")
    void 장바구니_생성후_주문하기() throws InterruptedException {
        int numberOfThreads = 2;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        log.info("주문하기 동시성 테스트 진행");
        service.execute(() -> {
            log.info("1번쓰레드 시작");
            log.info("1번쓰레드 초기 재고: " + menuRepository.findById(1L).get().getQuantity());

            orderService.createOrder(user);
            log.info("1번쓰레드 차감 재고: " + menuRepository.findById(1L).get().getQuantity());
            latch.countDown();
        });

        service.execute(() -> {
            log.info("2번쓰레드 시작");
            log.info("2번쓰레드 초기 재고: " + menuRepository.findById(1L).get().getQuantity());

            orderService.createOrder(user2);
            log.info("2번쓰레드 차감 재고: " + menuRepository.findById(1L).get().getQuantity());

            latch.countDown();
        });
        latch.await();

        Menu menu = menuRepository.findById(1L).get();
        assertEquals(0, menu.getQuantity());
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("주문취소")
    @Disabled
    void 주문취소() {
        Order order = orderRepository.findById(1L).orElse(null);
        assertNotNull(order);
        orderService.cancelOrder(user, order.getId());

        List<Order> orderList = orderRepository.findByUserAndState(user, OrderState.CANCELLED);

        //취소된 주문건무
        assertEquals(1, orderList.size());
        assertEquals(OrderState.CANCELLED, orderList.get(0).getState());
    }


    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("주문조회")
    @Disabled
    void 사용자_주문_조회() {
        CartRequestMenuDto cartRequestMenuDto = new CartRequestMenuDto("햄버거", 3);

        cartService.postCart(user, cartRequestMenuDto);
        orderService.createOrder(user);

        List<Order> orderList = orderRepository.findByUser(user);

        //취소된 주문건무
        assertNotNull(orderList);
        assertEquals(2, orderList.size());
        assertEquals(OrderState.CANCELLED, orderList.get(0).getState());
        assertEquals(OrderState.PENDING, orderList.get(1).getState());
    }
}
