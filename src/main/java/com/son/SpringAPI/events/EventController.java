package com.son.SpringAPI.events;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) {
        /*
         Request의 원하는 정보만 받아서 쓰기 위해 Dto 클래스를 생성했다
         받아온 정보를 쉽게 매핑하기 위해 ModelMapper 라이브러리를 받아 온 후에 빈에 등록하여 사용하였다

         @Valid
         Request를 Dto에 바인딩 할 시에 검증을 할 수 있게 해준다
         Dto 내 각 필드에 설정 해 놓은 Annotation을 모두 검증하게 된다
         그 결과를 Valid를 사용한 객체 바로 """오른쪽"""의 객체 Errors에 담아준다

         그 외의 세부적인 값들을 검증하려면
         Validator을 빈에 등록해서 Dto를 넘긴 후에 에러를 검출하면 된다
         */
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }
        eventValidator.validate(eventDto, errors);
        if(errors.hasErrors()) {
            return ResponseEntity.badRequest().body(errors);
        }

        Event event = modelMapper.map(eventDto, Event.class);
        Event newEvent = eventRepository.save(event);
        // 아래의 redirectUri를 생성해서 리턴해준다
        URI createdUri = linkTo(EventController.class).slash(newEvent.getId()).toUri();
        event.setId(10);
        return ResponseEntity.created(createdUri).body(event);
    }

}
