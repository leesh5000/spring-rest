package me.leesh.restapi.events;

import me.leesh.restapi.common.ErrorsResource;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) {

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
    public ResponseEntity queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler) {

        Page<Event> page = this.eventRepository.findAll(pageable);
        var pagedResources = assembler.toModel(page, EventResource::modelOf);
        pagedResources.add(Link.of("/docs/index.html#resources-events-list").withRel("profile"));
        return ResponseEntity.ok(pagedResources);

    }

    @GetMapping("/{id}")
    public ResponseEntity getEvent(@PathVariable Integer id) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Event event = optionalEvent.get();
        EntityModel<Event> eventResource = EventResource.modelOf(event);
        eventResource.add(Link.of("/docs/index.html#resources-events-get").withRel("profile"));
        return ResponseEntity.ok(eventResource);
    }

    @PutMapping("/{id}")
    public ResponseEntity updateEvent(@PathVariable Integer id,
                                      @RequestBody @Valid EventDto eventDto,
                                      Errors errors) {

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
