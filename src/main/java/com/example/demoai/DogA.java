package com.example.demoai;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DogA {
    private final ObjectMapper objectMapper;


    DogA(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public class Dog {
        private final String name;

        public Dog(String name) {
            this.name = name;
        }

        public String name() {
            return name;
        }
    }


    private final List<Dog> dogs = List.of(
            new Dog("Buddy"),
            new Dog("Bella"),
            new Dog("Charlie"),
            new Dog("Lucy"),
            new Dog("Max"),
            new Dog("Daisy"),
            new Dog("Bailey"),
            new Dog("Lola"),
            new Dog("Rocky"),
            new Dog("Molly")
    );

    @Tool(description = "Get a dog by name")
    public Dog getDogByName(String dogName) {
        System.out.println("Searching for dog: " + dogName);
        return dogs.stream()
                .filter(dog -> dog.name().equals(dogName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(String.format("Dog %s not found", dogName)));
    }
}
