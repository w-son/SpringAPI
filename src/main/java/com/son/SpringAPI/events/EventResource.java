package com.son.SpringAPI.events;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

public class EventResource extends Resource<Event> {

    /*
     ResouceSupport를 상속받은 클래스를 정의하면
     이 클래스를 통해서 link 관련 메서드를 이용하여 Api 스펙에 추가할 수 있다
     */

    /* v1 , extends ResourceSupport
    @JsonUnwrapped 왜 필요?? wrapped 된 상태이면 identifier를 인식할 수 없다
    private Event event;

    public EventResource(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return this.event;
    }
    */

    public EventResource(Event event, Link... links) {
        super(event, links);
        // 보통 링크들은 Resource에 넣어서 관리하는게 좋다
        add(linkTo(EventController.class).slash(event.getId()).withSelfRel());
    }

}
