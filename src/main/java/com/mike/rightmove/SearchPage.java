package com.mike.rightmove;

import java.util.List;

public record SearchPage(List<Property> properties, int totalResultCount) {}
