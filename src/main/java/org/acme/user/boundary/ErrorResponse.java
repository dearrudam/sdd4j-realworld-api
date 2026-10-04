package org.acme.user.boundary;

import java.util.List;
import java.util.Map;

public record ErrorResponse(Map<String, List<String>> errors) {
}
