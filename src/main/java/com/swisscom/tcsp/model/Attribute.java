package com.swisscom.tcsp.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record Attribute(String name, String value, String operation) {}
