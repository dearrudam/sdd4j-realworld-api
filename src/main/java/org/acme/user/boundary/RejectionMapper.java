package org.acme.user.boundary;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.acme.user.control.Rejection;

@Provider
public class RejectionMapper implements ExceptionMapper<Rejection> {

    @Override
    public Response toResponse(Rejection rejection) {
        return Response.status(rejection.status)
                .entity(new ErrorResponse(rejection.errors))
                .build();
    }
}
