package com.swisscom.tcsp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TreeItem(
        @JsonProperty("identifier") String id,
        @JsonProperty("brickId") String type,
        String operation,
        List<Attribute> attributes,
        List<TreeItem> children,
        List<String> relations
) {}
