package dk.xam.hassq.commands;

import static dk.xam.hassq.Util.column;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.homeassistant.runtime.HomeAssistantClient;
import io.quarkiverse.homeassistant.runtime.events.HAEvent;
import io.quarkiverse.homeassistant.runtime.model.Area;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import dk.xam.hassq.Util;

@Command(name = "event")
public class EventCommand extends BaseCommand {

    @Inject
    HomeAssistantClient hass;

    @Inject
    ObjectMapper mapper;

    @Command
    void watch(@Parameters(description="Event(s) name to monitor") List<String> event) {

        if(event == null || event.isEmpty()) {
            hass.subscribeToEvents(null, this::callback);
        } else {
            event.forEach(e -> {
                System.out.println("Watching for event: " + e);
                hass.subscribeToEvents(e, this::callback);
            });
        }
    }

    void callback(HAEvent msg) {
        System.out.println("Event: " + msg);
    }
    
    @Inject
    dk.xam.hassq.PPrinter pretty;

    void render(List<Area> data) {

        if(parent.json()) {
            System.out.println(pretty.string(data));
            return;
        } else {
            var result = Util.table().data(data,
                        List.of(column("ID").with(e -> e.id()),
                                column("NAME").with(e -> e.name())));

            System.out.println(result);
        }
    }
}
