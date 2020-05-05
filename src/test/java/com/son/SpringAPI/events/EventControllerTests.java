package com.son.SpringAPI.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.son.SpringAPI.common.TestDescription;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EventControllerTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @TestDescription("정상적으로 이벤트를 생성하는 테스트")
    public void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 5, 4, 18, 32))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 5, 4, 23, 59))
                .beginEventDateTime(LocalDateTime.of(2020, 5, 4, 18, 32))
                .endEventDateTime(LocalDateTime.of(2020, 5, 4, 23, 59))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남의 D2 스타트업 팩토리")
                .build();

        /*
        @WebMvcTest 의 경우
        디비랑 연결이 안되어있으니까 save가 제대로 동작하지 않을 것이기 때문에
        Mockito를 통해서 when 안의 구문이 실행될때 event가 리턴이 되게끔 만들어준다
        Mockito.when(eventRepository.save(event)).thenReturn(event);
        */
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").exists())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaTypes.HAL_JSON_UTF8_VALUE))
                .andExpect(jsonPath("free").value(false))
                .andExpect(jsonPath("free").value(true))
                .andExpect(jsonPath("eventStatus").value(Matchers.not(EventStatus.PUBLISHED)));
    }

    @Test
    @TestDescription("입력 받을 수 없는 값을 사용한 경우의 테스트")
    public void badRequest() throws Exception {
        Event event = Event.builder()
                .id(100)
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 5, 4, 18, 32))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 5, 4, 23, 59))
                .beginEventDateTime(LocalDateTime.of(2020, 5, 4, 18, 32))
                .endEventDateTime(LocalDateTime.of(2020, 5, 4, 23, 59))
                .basePrice(100)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남의 D2 스타트업 팩토리")
                .free(true)
                .offline(false)
                .eventStatus(EventStatus.PUBLISHED)
                .build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isBadRequest());
        /*
         serialization : 객체 -> json
         deserialization : json -> 객체
         */
    }

    @Test
    @TestDescription("입력값이 비어 있는 경우의 테스트")
    public void emptyInput() throws Exception {
        EventDto eventDto = EventDto.builder().build();
        this.mockMvc.perform(post("/api/events")
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(this.objectMapper.writeValueAsString(eventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @TestDescription("입력값이 잘못된 경우의 테스트")
    public void wrongInput() throws Exception {
        EventDto eventDto = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 5, 26, 18, 32))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 5, 25, 23, 59))
                .beginEventDateTime(LocalDateTime.of(2020, 5, 24, 18, 32))
                .endEventDateTime(LocalDateTime.of(2020, 5, 23, 23, 59))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남의 D2 스타트업 팩토리")
                .build();

        this.mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(this.objectMapper.writeValueAsString(eventDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists());
    }

}
