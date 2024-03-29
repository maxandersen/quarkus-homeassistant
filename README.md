# Quarkus HomeAssistant

[![Version](https://img.shields.io/maven-central/v/io.quarkiverse.homeassistant/quarkus-homeassistant?logo=apache-maven&style=flat-square)](https://search.maven.org/artifact/io.quarkiverse.homeassistant/quarkus-homeassistant)

This extension allows you to interact with [Home Assistant](https://www.home-assistant.io/) from Quarkus applications.

## Work-in-progress

This extension is currently a work-in-progress and although it is functional, expect its API's and approach to change as we refine it.

## Features

Below are the features that are currently available and planned for this extension:

- [x] Home Assistant REST API client
- [ ] Home Assistant WebSocket API client
- [x] Home Assistant DevService (using Home Assistant demo server)
- [ ] Subscribe/Listen to Home Assistant events/state changes
- [ ] Injecting generic Home Assistant entities as CDI beans (`@HassEntity("entity_id") Light kitchenLight;`)
- [ ] Code Generator for completion and type-safe friendly API (`@Inject Home home; home.lights.kitchenLight.turnOn();)

## Samples

Check out the [samples](samples) directory for examples on how to use this extension.

## Getting Started 

To incorporate Quarkus HomeAssistant into your Quarkus project, add the following Maven dependency:

```xml
<dependency>
    <groupId>io.quarkiverse.homeassistant</groupId>
    <artifactId>quarkus-homeassistant</artifactId>
    <version>{latest-version}</version>
</dependency>
```

Make sure to replace `{latest-version}` with the most recent release version available on [Maven Central](https://search.maven.org/artifact/io.quarkiverse.homeassistant/quarkus-homeassistant).

## Contributing

Feel free to contribute to this project by submitting issues or pull requests.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

