package org.acme.user.boundary;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Provider
public class ViolationMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Map<String, List<String>> errors = new TreeMap<>();
        for (var violation : exception.getConstraintViolations()) {
            errors.computeIfAbsent(field(violation), _ -> new ArrayList<>())
                    .add(violation.getMessage());
        }
        return Response.status(422).entity(new ErrorResponse(errors)).build();
    }

    String field(ConstraintViolation<?> violation) {
        String field = "user";
        for (Path.Node node : violation.getPropertyPath()) {
            if (node.getName() != null) {
                field = node.getName();
            }
        }
        return field;
    }
}
