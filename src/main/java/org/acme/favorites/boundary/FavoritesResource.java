package org.acme.favorites.boundary;

import static org.acme.favorites.FavoritesRequirement.Rn.R1_1;
import static org.acme.favorites.FavoritesRequirement.Rn.R1_2;
import static org.acme.favorites.FavoritesRequirement.Rn.R1_3;
import static org.acme.favorites.FavoritesRequirement.Rn.R1_4;
import static org.acme.favorites.FavoritesRequirement.Rn.R1_5;
import static org.acme.favorites.FavoritesRequirement.Rn.R2_1;
import static org.acme.favorites.FavoritesRequirement.Rn.R2_2;
import static org.acme.favorites.FavoritesRequirement.Rn.R2_3;
import static org.acme.favorites.FavoritesRequirement.Rn.R2_4;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.article.boundary.ArticleResponse;
import org.acme.favorites.FavoritesRequirement;
import org.acme.favorites.control.Favorites;
import org.acme.user.control.SessionTokens;
import org.acme.user.control.Users;
import org.acme.user.entity.User;

@Path("/articles")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FavoritesResource {

    @Inject
    Users users;

    @Inject
    SessionTokens tokens;

    @Inject
    Favorites favorites;

    @POST
    @Path("/{slug}/favorite")
    @PermitAll
    @FavoritesRequirement({ R1_1, R1_2, R1_3, R1_4, R1_5 })
    public ArticleResponse favoriteArticle(@PathParam("slug") String slug,
            @HeaderParam("Authorization") String authorization) {
        return ArticleResponse.of(favorites.mark(caller(authorization), slug));
    }

    @DELETE
    @Path("/{slug}/favorite")
    @PermitAll
    @FavoritesRequirement({ R2_1, R2_2, R2_3, R2_4 })
    public ArticleResponse unfavoriteArticle(@PathParam("slug") String slug,
            @HeaderParam("Authorization") String authorization) {
        return ArticleResponse.of(favorites.unmark(caller(authorization), slug));
    }

    User caller(String authorization) {
        return users.find(tokens.verify(authorization));
    }
}
