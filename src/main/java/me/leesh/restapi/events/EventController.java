package me.leesh.restapi.events;

import me.leesh.restapi.accounts.Account;
import me.leesh.restapi.accounts.AccountAdapter;
import me.leesh.restapi.accounts.CurrentUser;
import me.leesh.restapi.common.ErrorsResource;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.config.ScheduledTaskHolder;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    public EventController(EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator) {
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
        this.eventValidator = eventValidator;
    }

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors,
                                      @CurrentUser Account currentUser) {



        // JSR303으로 바인딩 시 에러 확인
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        // JSR303 검증을 통과하면, 이제 인풋 값의 데이터 유효성 검증
        eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        Event event = modelMapper.map(eventDto, Event.class);
        event.update(); // service
        event.setManager(currentUser);
        Event newEvent = this.eventRepository.save(event);
        WebMvcLinkBuilder selfLinkBuilder = linkTo(EventController.class).slash(newEvent.getId());
        URI uri = selfLinkBuilder.toUri();
//        EntityModel<Event> eventEntityModel = EntityModel.of(event
//                , selfLinkBuilder.slash(event.getId()).withSelfRel()
//                , selfLinkBuilder.withRel("query-events")
//                , selfLinkBuilder.withRel("update-event")
//                , new Link("/docs/index.html#resources-events-create").withRel("profile")
//        );

        EntityModel<Event> eem = EventResource.modelOf(event);
        eem.add(selfLinkBuilder.withRel("query-events"));
        eem.add(selfLinkBuilder.withRel("update-event"));
        eem.add(Link.of("/docs/index.html#resources-events-create").withRel("profile"));

        return ResponseEntity.created(uri).body(eem);
    }

    @GetMapping
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler,
                                      // 필드에 있는 account를 주입해준다.
                                      @CurrentUser Account account) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        Object principal = authentication.getPrincipal(); // 여기서 principal은 UserDetails이다. (스프링 시큐리티의 유저)

        Page<Event> page = this.eventRepository.findAll(pageable);
        var pagedResources = assembler.toModel(page, EventResource::modelOf);
        pagedResources.add(Link.of("/docs/index.html#resources-events-list").withRel("profile"));

        // DB 조회하지 않고도, 현재 사용자의 정보를 꺼낼 수 있음
        if (account != null) {
            pagedResources.add(linkTo(EventController.class).withRel("create-event"));
        }

        return ResponseEntity.ok(pagedResources);
    }

    @GetMapping("/{id}")
    public ResponseEntity getEvent(@PathVariable Integer id,
                                   @CurrentUser Account currentUser) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);

//        if (optionalEvent.isEmpty()) {
//            return ResponseEntity.notFound().build();
//        }
//
//        Event event = optionalEvent.get();
//        EntityModel<Event> eventResource = EventResource.modelOf(event);
//        eventResource.add(Link.of("/docs/index.html#resources-events-get").withRel("profile"));
//
//        return ResponseEntity.ok(eventResource);

//        return optionalEvent.ifPresentOrElse(
//                event -> {
//                    EntityModel<Event> eventResource = EventResource.modelOf(event);
//                    eventResource.add(Link.of("/docs/index.html#resources-events-get").withRel("profile"));
//                    return ResponseEntity.ok(eventResource);
//                },
//                () -> {
//                    return ResponseEntity.notFound().build();
//                }
//        );

        return optionalEvent
                .map(event -> {
                    EntityModel<Event> eventResource = EventResource.modelOf(event);
                    eventResource.add(Link.of("/docs/index.html#resources-events-get").withRel("profile"));
                    if (event.getManager().equals(currentUser)) {
                        eventResource.add(linkTo(EventController.class).slash(event.getId()).withRel("update-event"));
                    }
                    return ResponseEntity.ok(eventResource);
                })
                .orElseGet(() -> {
                    return ResponseEntity.notFound().build();
                });
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable Integer id,
                                      @RequestBody @Valid EventDto eventDto,
                                      Errors errors,
                                      @CurrentUser Account currentUser) {

        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        this.eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }

        Event event = optionalEvent.get();

        if (!event.getManager().equals(currentUser)) {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }

        this.modelMapper.map(eventDto, event);
        // service가 없기 때문에 임시로 save
        Event updateEvent = this.eventRepository.save(event);

        EntityModel<Event> eventEntityModel = EventResource.modelOf(updateEvent);
        eventEntityModel.add(Link.of("/docs/index.html#resources-events-update").withRel("profile"));

        return ResponseEntity.ok(eventEntityModel);
    }

    private ResponseEntity badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(ErrorsResource.modelOf(errors));
    }

}
