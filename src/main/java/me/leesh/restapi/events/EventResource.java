package me.leesh.restapi.events;

import me.leesh.restapi.index.IndexController;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.validation.Errors;

import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class EventResource extends EntityModel<Event> {

    public static EntityModel<Event> modelOf(Event event, Link... links) {
        EntityModel<Event> eventModel = EntityModel.of(event);
        eventModel.add(linkTo(EventController.class).slash(event.getId()).withSelfRel());

        Iterable<Link> iterable = Arrays.asList(links);
        iterable.forEach(eventModel::add);

        return eventModel;
    }
}
