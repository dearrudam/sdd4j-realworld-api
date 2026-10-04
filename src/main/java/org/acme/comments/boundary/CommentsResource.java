package org.acme.comments.boundary;

import static org.acme.comments.CommentsRequirement.Rn.R1_1;
import static org.acme.comments.CommentsRequirement.Rn.R1_2;
import static org.acme.comments.CommentsRequirement.Rn.R1_3;
import static org.acme.comments.CommentsRequirement.Rn.R1_4;
import static org.acme.comments.CommentsRequirement.Rn.R1_5;
import static org.acme.comments.CommentsRequirement.Rn.R1_6;
import static org.acme.comments.CommentsRequirement.Rn.R2_1;
import static org.acme.comments.CommentsRequirement.Rn.R2_2;
import static org.acme.comments.CommentsRequirement.Rn.R2_3;
import static org.acme.comments.CommentsRequirement.Rn.R2_4;
import static org.acme.comments.CommentsRequirement.Rn.R3_1;
import static org.acme.comments.CommentsRequirement.Rn.R3_2;
import static org.acme.comments.CommentsRequirement.Rn.R3_3;
import static org.acme.comments.CommentsRequirement.Rn.R3_4;
import static org.acme.comments.CommentsRequirement.Rn.R3_5;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.comments.CommentsRequirement;
import org.acme.comments.control.Comments;
import org.acme.user.control.SessionTokens;
import org.acme.user.control.Users;
import org.acme.user.entity.User;

@Path("/articles")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CommentsResource {

    @Inject
    Users users;

    @Inject
    SessionTokens tokens;

    @Inject
    Comments comments;

    @GET
    @Path("/{slug}/comments")
    @PermitAll
    @CommentsRequirement({ R1_1, R1_2, R1_3, R1_4, R1_5, R1_6 })
    public CommentsResponse listComments(@PathParam("slug") String slug,
            @HeaderParam("Authorization") String authorization) {
        var caller = authorization == null ? null : caller(authorization);
        return CommentsResponse.of(comments.list(caller, slug));
    }

    @POST
    @Path("/{slug}/comments")
    @PermitAll
    @CommentsRequirement({ R2_1, R2_2, R2_3, R2_4 })
    public Response createComment(@PathParam("slug") String slug,
            @HeaderParam("Authorization") String authorization, @Valid NewCommentRequest request) {
        var created = comments.create(caller(authorization), slug, request.comment().body());
        return Response.status(Response.Status.CREATED).entity(CommentResponse.of(created)).build();
    }

    @DELETE
    @Path("/{slug}/comments/{id}")
    @PermitAll
    @CommentsRequirement({ R3_1, R3_2, R3_3, R3_4, R3_5 })
    public Response deleteComment(@PathParam("slug") String slug, @PathParam("id") long id,
            @HeaderParam("Authorization") String authorization) {
        comments.delete(caller(authorization), slug, id);
        return Response.noContent().build();
    }

    User caller(String authorization) {
        return users.find(tokens.verify(authorization));
    }
}
