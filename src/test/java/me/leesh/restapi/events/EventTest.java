package me.leesh.restapi.events;

import lombok.Builder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {

    /**
     * 빌더의 장점
     * 1. 어떤 값을 넣어야하는지 알 수 있다.
     * 2. 어떤 값이 들어가는지 알 수 있다.
     */
    @Test
    @DisplayName("빌더가 있는지 확인")
    public void builder() {
        Event event = Event.builder().build();
        assertThat(event).isNotNull();
    }

    @Test
    @DisplayName("자바빈 스펙을 준수하는지 확인 - 기본 생성자 존재, Getter/Setter 존재")
    public void javaBean() {
        Event event = new Event();

        String name = "Event";
        String description = "Spring";

        event.setName(name);
        event.setDescription(description);

        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);
    }

    static Stream<Arguments> paramsForTestFree() {
        return Stream.of(
                Arguments.arguments(0, 0, true),
                Arguments.arguments(100, 0, false),
                Arguments.arguments(0, 100, false)
        );
    }

    @ParameterizedTest
    @MethodSource("paramsForTestFree")
    public void testFree(int basePrice, int maxPrice, boolean isFree) {
        // given
        Event event = Event.builder()
                .basePrice(basePrice)
                .maxPrice(maxPrice)
                .build();
        // when
        event.update();
        // then
        assertThat(event.isFree()).isEqualTo(isFree);
    }

    static Stream<Arguments> paramsForTestOffline() {
        return Stream.of(
                Arguments.arguments("강남역 네이버", true),
                Arguments.arguments("    ", false),
                Arguments.arguments(null, false)
        );
    }

    @ParameterizedTest
    @MethodSource("paramsForTestOffline")
    public void testOffline(String location, boolean isOffline) {
        // given
        Event event = Event.builder()
                .location(location)
                .build();
        // when
        event.update();

        // then
        assertThat(event.isOffline()).isEqualTo(isOffline);

    }

}