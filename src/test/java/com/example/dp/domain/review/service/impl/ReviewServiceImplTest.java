package com.example.dp.domain.review.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.dp.domain.order.entity.Order;
import com.example.dp.domain.order.repository.OrderRepository;
import com.example.dp.domain.review.dto.request.ReviewRequestDto;
import com.example.dp.domain.review.dto.response.ReviewResponseDto;
import com.example.dp.domain.review.entity.Review;
import com.example.dp.domain.review.repository.ReviewRepository;
import com.example.dp.domain.review.service.ReviewService;
import com.example.dp.domain.user.entity.User;
import com.example.dp.domain.user.repository.UserRepository;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ReviewServiceImplTest {

    @Autowired
    ReviewService reviewService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    ReviewRepository reviewRepository;

    FixtureMonkey fixtureMonkey;

    @BeforeEach
    void setup() {
        fixtureMonkey = FixtureMonkey.builder()
            .plugin(new JakartaValidationPlugin())
            .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
            .build();
    }

    @Nested
    @DisplayName("리뷰 생성 테스트")
    class CreateReviewTest {

        @Test
        @DisplayName("주문자가 리뷰를 생성하는 경우")
        void 주문자_리뷰_생성() {
            // given
            User user = createUser();
            user = userRepository.save(user);

            Order order = createOrder(user);
            order = orderRepository.save(order);

            ReviewRequestDto requestDto = new ReviewRequestDto("테스트 내용");

            // when
            ReviewResponseDto responseDto = reviewService.createReview(order.getId(), requestDto,
                user);

            // then
            Review findReview = reviewRepository.findById(responseDto.getId())
                .orElseThrow(RuntimeException::new);
            assertThat(findReview.getId()).isEqualTo(responseDto.getId());
            assertThat(findReview.getContent()).isEqualTo(responseDto.getContent());
        }

        @Test
        @DisplayName("주문자가 아닌 사람이 리뷰를 생성하는 경우")
        void 다른_사람이_리뷰_생성() {
            // given
            User user = createUser();
            User anotherUser = createUser();
            user = userRepository.save(user);

            Order order = createOrder(user);
            order = orderRepository.save(order);

            ReviewRequestDto requestDto = new ReviewRequestDto("테스트 내용");

            // when - then
            Order finalOrder = order;
            assertThatThrownBy(
                () -> reviewService.createReview(finalOrder.getId(), requestDto, anotherUser))
                .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("이미 리뷰가 존재한 상태에서 리뷰를 생성하는 경우")
        void 리뷰_중복_생성() {
            // given
            User user = createUser();
            user = userRepository.save(user);

            Order order = createOrder(user);
            order = orderRepository.save(order);

            Review review = createReview(order);
            reviewRepository.save(review);

            ReviewRequestDto requestDto = new ReviewRequestDto("테스트 내용");

            // when - then
            Order finalOrder = order;
            User finalUser = user;
            assertThatThrownBy(
                () -> reviewService.createReview(finalOrder.getId(), requestDto, finalUser))
                .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("리뷰 수정 테스트")
    class UpdateReviewTest {

        @Test
        @DisplayName("주문자가 리뷰를 수정하는 경우")
        void 주문자_리뷰_수정() {
            // given
            User user = createUser();
            user = userRepository.save(user);

            Order order = createOrder(user);
            order = orderRepository.save(order);

            Review review = createReview(order);
            review = reviewRepository.save(review);

            ReviewRequestDto requestDto = new ReviewRequestDto("테스트 내용");

            // when
            ReviewResponseDto responseDto = reviewService.updateReview(review.getId(), requestDto,
                user);

            // then
            Review findReview = reviewRepository.findById(responseDto.getId())
                .orElseThrow(RuntimeException::new);
            assertThat(findReview.getId()).isEqualTo(responseDto.getId());
            assertThat(findReview.getContent()).isEqualTo(responseDto.getContent());
        }

        @Test
        @DisplayName("주문자가 아닌 사람이 리뷰를 수정하는 경우")
        void 다른_사람이_리뷰_수정() {
            // given
            User user = createUser();
            User anotherUser = createUser();
            user = userRepository.save(user);

            Order order = createOrder(user);
            order = orderRepository.save(order);

            Review review = createReview(order);
            review = reviewRepository.save(review);

            ReviewRequestDto requestDto = new ReviewRequestDto("테스트 내용");

            // when - then
            Review finalReview = review;
            assertThatThrownBy(
                () -> reviewService.updateReview(finalReview.getId(), requestDto, anotherUser))
                .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("존재하지 않는 리뷰를 수정하는 경우")
        void 없는_리뷰_수정() {
            // given
            User user = createUser();
            user = userRepository.save(user);

            Order order = createOrder(user);
            order = orderRepository.save(order);

            long noExistReviewId = 1;

            ReviewRequestDto requestDto = new ReviewRequestDto("테스트 내용");

            // when - then
            User finalUser = user;
            assertThatThrownBy(
                () -> reviewService.updateReview(noExistReviewId, requestDto, finalUser))
                .isInstanceOf(RuntimeException.class);
        }
    }

    private User createUser() {
        return fixtureMonkey.giveMeBuilder(User.class)
            .setNotNull("*")
            .sample();
    }

    private Order createOrder(User user) {
        return fixtureMonkey.giveMeBuilder(Order.class)
            .setNotNull("*")
            .setNull("orderMenuList")
            .set("user", user)
            .sample();
    }

    private Review createReview(Order order) {
        return fixtureMonkey.giveMeBuilder(Review.class)
            .setNotNull("*")
            .set("order", order)
            .sample();
    }


}