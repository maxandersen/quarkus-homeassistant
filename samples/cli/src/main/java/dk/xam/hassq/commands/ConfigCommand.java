package dk.xam.hassq.commands;

import static dk.xam.hassq.Util.column;
import static dk.xam.hassq.Util.str;

import java.util.List;

import io.quarkiverse.homeassistant.runtime.HomeAssistantClient;
import io.quarkiverse.homeassistant.runtime.model.Config;
import jakarta.inject.Inject;
import picocli.CommandLine.Command;

import dk.xam.hassq.PPrinter;
import dk.xam.hassq.Util;

@Command(name = "config")
public class ConfigCommand extends BaseCommand {

    @Inject
    HomeAssistantClient hass;

    @Inject
    PPrinter pretty;
    
    @Command
    void full() {

       var data = List.of(hass.getConfig());
       
       if(parent.json()) {
       System.out.println(pretty.string(data));
       } else {
       var result = Util.table()
                    .data(data,
                    List.of(column("VERSION").with(e -> e.version()),
                            column("DIR").with(e -> e.configDir()),
                            column("LATITUDE").with(e -> str(e.latitude())),
                            column("LONGITUDE").with(e -> str(e.longitude())),
                            column("ELEVATION").with(e -> str(e.elevation())),
                            column("TZ").with(e -> e.timeZone()),
                            column("COMPONENTS").with(e -> str(e.components().size()))
        ));

        System.out.println(result);
       }

    }

    @Command
    void components() {

       var data = hass.getConfig().components();
       
       var result = Util.table()
                    .data(data,
                    List.of(column("COMPONENT").with(e -> e)));

        System.out.println(result);
    }
    void render(List<Config> data) {
        
    }


}
