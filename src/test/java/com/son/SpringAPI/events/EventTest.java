package com.son.SpringAPI.events;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EventTest {

    @Test
    public void builder() throws Exception {
        Event event = Event.builder()
                .name("REST API")
                .description("development with Spring")
                .build();
        assertThat(event).isNotNull();
    }

    @Test
    public void javaBean() throws Exception {
        // Given
        String name = "Event";
        String description = "Spring";
        // When
        Event event = new Event();
        event.setName(name);
        event.setDescription(description);
        // Then
        assertThat(event.getName()).isEqualTo(name);
        assertThat(event.getDescription()).isEqualTo(description);
    }

}