package com.son.SpringAPI.events;

import com.son.SpringAPI.accounts.Account;
import com.son.SpringAPI.accounts.AccountAdapter;
import com.son.SpringAPI.accounts.CurrentUser;
import com.son.SpringAPI.common.ErrorsResource;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_UTF8_VALUE)
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto,
                                      Errors errors,
                                      @CurrentUser Account currentUser) {

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
        if(errors.hasErrors()) { // Valid의 에러
            return ResponseEntity.badRequest().body(new ErrorsResource(errors));
        }
        eventValidator.validate(eventDto, errors);
        if(errors.hasErrors()) { // EventValidator의 에러
            return ResponseEntity.badRequest().body(new ErrorsResource(errors));
        }

        Event event = modelMapper.map(eventDto, Event.class);
        // 사실 비즈니스 로직을 호출하는 부분은 Service 계층에 위임하면 좋다
        event.update();
        event.setManager(currentUser);
        Event newEvent = eventRepository.save(event);
        // 아래의 redirectUri를 생성해서 리턴해준다
        ControllerLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI createdUri = selfLinkBuilder.toUri();

        // 링크를 생성할 수 있는 Event<Resource>를 상속받은 클래스로 감싸준다
        EventResource eventResource = new EventResource(event);
        eventResource.add(linkTo(EventController.class).withRel("query-events"));
        eventResource.add(selfLinkBuilder.withRel("update-event"));
        eventResource.add(new Link("/docs/index.html#resources-events-create").withRel("profile"));

        return ResponseEntity.created(createdUri).body(eventResource);
    }

    private ResponseEntity badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }

    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable,
                                      PagedResourcesAssembler<Event> assembler,
                                      @CurrentUser Account account) {
        /*
         현재 로그인 된, 즉 SecurityContextHolder (ThreadLocal에 저장되어 있는) 에 존재하는 인증 정보를 불러오는 두가지 방법이 존재한다
         1) Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User principal = (User) authentication.getPrincipal(); // UserDetails에서 loadbyUsername으로 리턴했던 객체가 나온다
         2) 메서드 파라미터로 @AuthenticationPrincipal User user 를 받는다
         3) User를 상속받은 Adapter를 UserDetailsService에 새로이 정의한 다음
            @AuthenticationPrincipal AccountAdapter account 을 통해 이 Adapter를 받아온다
         4) SpEL 을 통해서 AccountAdapter의 account 필드를 불러들인다
         5) 4번의 과정을 CurrentUser annotation으로 수정
         */

        Page<Event> page = eventRepository.findAll(pageable);
        PagedResources<Resource<Event>> pagedResources = assembler.toResource(page, e -> new EventResource(e));
        pagedResources.add(new Link("/docs/index.html#resources-events-list").withRel("profile"));
        if(account != null) {
            pagedResources.add(linkTo(EventController.class).withRel("create-event"));
        }
        return ResponseEntity.ok(pagedResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity getevent(@PathVariable("id") Integer id,
                                   @CurrentUser Account currentUser) {

        Optional<Event> optionalEvent = eventRepository.findById(id);
        if(optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Event event = optionalEvent.get();
        EventResource eventResource = new EventResource(event);
        eventResource.add(new Link("/docs/index.html#resources-events-get").withRel("profile"));
        if(event.getManager().equals(currentUser)) {
            eventResource.add(linkTo(EventController.class).slash(event.getId()).withRel("update-event"));
        }
        return ResponseEntity.ok(eventResource);

    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable("id") Integer id,
                                      @RequestBody @Valid EventDto eventDto,
                                      Errors errors,
                                      @CurrentUser Account currentUser) {

        Optional<Event> optionalEvent = eventRepository.findById(id);
        if(optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if(errors.hasErrors()) { // @Valid에 의한 에러 검출
            return ResponseEntity.badRequest().body(new ErrorsResource(errors));
        }
        eventValidator.validate(eventDto, errors);
        if(errors.hasErrors()) { // ErrorValidator에 의한 에러 검출
            return ResponseEntity.badRequest().body(new ErrorsResource(errors));
        }

        Event event = optionalEvent.get();
        if(!event.getManager().equals(currentUser)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
        // 검증만 끝난다면 변경된 값들은 ModelMapper로 수정해준다
        modelMapper.map(eventDto, event);
        Event updatedEvent = eventRepository.save(event);

        EventResource eventResource = new EventResource(updatedEvent);
        eventResource.add(new Link("/docs/index.html#resources-events-update").withRel("profile"));
        return ResponseEntity.ok(eventResource);
    }

}
