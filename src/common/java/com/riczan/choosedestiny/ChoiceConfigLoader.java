package com.riczan.choosedestiny;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class ChoiceConfigLoader {
    private final Gson gson;

    public ChoiceConfigLoader(Gson gson) {
        this.gson = Objects.requireNonNull(gson, "gson");
    }

    public ChoiceConfig load(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            ChoiceConfig config = gson.fromJson(reader, ChoiceConfig.class);
            if (config == null) {
                throw new JsonParseException("Choice config is empty");
            }
            return config;
        }
    }
}
