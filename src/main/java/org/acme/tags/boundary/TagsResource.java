package org.acme.tags.boundary;

import static org.acme.tags.TagsRequirement.Rn.R1_1;
import static org.acme.tags.TagsRequirement.Rn.R1_2;
import static org.acme.tags.TagsRequirement.Rn.R1_3;
import static org.acme.tags.TagsRequirement.Rn.R1_4;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.tags.TagsRequirement;
import org.acme.tags.control.Tags;

@Path("/tags")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TagsResource {

    @Inject
    Tags tags;

    @GET
    @PermitAll
    @TagsRequirement({ R1_1, R1_2, R1_3, R1_4 })
    public TagsResponse listTags() {
        return new TagsResponse(tags.list());
    }
}
