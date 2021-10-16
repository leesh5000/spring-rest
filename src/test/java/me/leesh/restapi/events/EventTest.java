package me.leesh.restapi.events;

import lombok.Builder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventTest {

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

}