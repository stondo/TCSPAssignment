package com.swisscom.tcsp.model;

import com.fasterxml.jackson.annotation.*;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record TreeItem(
        @JsonProperty("identifier")
        @JsonAlias("id")
        String id,

        @JsonProperty("brickId")
        @JsonAlias("type")
        String type,

        String operation,

        @JsonSetter(nulls = Nulls.AS_EMPTY)
        List<Attribute> attributes,

        @JsonSetter(nulls = Nulls.AS_EMPTY)
        List<TreeItem> children,

        @JsonSetter(nulls = Nulls.AS_EMPTY)
        List<String> relations
) {}
