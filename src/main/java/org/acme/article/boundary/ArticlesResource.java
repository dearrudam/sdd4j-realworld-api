package org.acme.article.boundary;

import static org.acme.article.ArticleRequirement.Rn.R1_1;
import static org.acme.article.ArticleRequirement.Rn.R1_10;
import static org.acme.article.ArticleRequirement.Rn.R1_11;
import static org.acme.article.ArticleRequirement.Rn.R1_12;
import static org.acme.article.ArticleRequirement.Rn.R1_2;
import static org.acme.article.ArticleRequirement.Rn.R1_3;
import static org.acme.article.ArticleRequirement.Rn.R1_4;
import static org.acme.article.ArticleRequirement.Rn.R1_5;
import static org.acme.article.ArticleRequirement.Rn.R1_6;
import static org.acme.article.ArticleRequirement.Rn.R1_7;
import static org.acme.article.ArticleRequirement.Rn.R1_8;
import static org.acme.article.ArticleRequirement.Rn.R1_9;
import static org.acme.article.ArticleRequirement.Rn.R2_1;
import static org.acme.article.ArticleRequirement.Rn.R2_2;
import static org.acme.article.ArticleRequirement.Rn.R2_3;
import static org.acme.article.ArticleRequirement.Rn.R2_4;
import static org.acme.article.ArticleRequirement.Rn.R2_5;
import static org.acme.article.ArticleRequirement.Rn.R2_6;
import static org.acme.article.ArticleRequirement.Rn.R3_1;
import static org.acme.article.ArticleRequirement.Rn.R3_2;
import static org.acme.article.ArticleRequirement.Rn.R3_3;
import static org.acme.article.ArticleRequirement.Rn.R3_4;
import static org.acme.article.ArticleRequirement.Rn.R3_5;
import static org.acme.article.ArticleRequirement.Rn.R3_6;
import static org.acme.article.ArticleRequirement.Rn.R3_7;
import static org.acme.article.ArticleRequirement.Rn.R4_1;
import static org.acme.article.ArticleRequirement.Rn.R4_2;
import static org.acme.article.ArticleRequirement.Rn.R4_3;
import static org.acme.article.ArticleRequirement.Rn.R4_4;
import static org.acme.article.ArticleRequirement.Rn.R4_5;
import static org.acme.article.ArticleRequirement.Rn.R4_6;
import static org.acme.article.ArticleRequirement.Rn.R5_1;
import static org.acme.article.ArticleRequirement.Rn.R5_2;
import static org.acme.article.ArticleRequirement.Rn.R5_3;
import static org.acme.article.ArticleRequirement.Rn.R5_4;
import static org.acme.article.ArticleRequirement.Rn.R5_5;
import static org.acme.article.ArticleRequirement.Rn.R5_6;
import static org.acme.article.ArticleRequirement.Rn.R5_7;
import static org.acme.article.ArticleRequirement.Rn.R5_8;
import static org.acme.article.ArticleRequirement.Rn.R6_1;
import static org.acme.article.ArticleRequirement.Rn.R6_2;
import static org.acme.article.ArticleRequirement.Rn.R6_3;
import static org.acme.article.ArticleRequirement.Rn.R6_4;
import static org.acme.article.ArticleRequirement.Rn.R6_5;
import static org.acme.article.ArticleRequirement.Rn.R6_6;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import org.acme.article.ArticleRequirement;
import org.acme.article.control.ArticlePatch;
import org.acme.article.control.Articles;
import org.acme.user.control.Rejection;
import org.acme.user.control.SessionTokens;
import org.acme.user.control.Users;
import org.acme.user.entity.User;

@Path("/articles")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ArticlesResource {

    @Inject
    Users users;

    @Inject
    SessionTokens tokens;

    @Inject
    Articles articles;

    @GET
    @PermitAll
    @ArticleRequirement({ R1_1, R1_2, R1_3, R1_4, R1_5, R1_6, R1_7, R1_8, R1_9, R1_10, R1_11, R1_12 })
    public ArticlesResponse listArticles(@QueryParam("tag") String tag, @QueryParam("author") String author,
            @QueryParam("favorited") String favorited, @DefaultValue("20") @QueryParam("limit") int limit,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @HeaderParam("Authorization") String authorization) {
        var caller = authorization == null ? null : caller(authorization);
        return ArticlesResponse.of(articles.list(caller, tag, author, favorited, limit, offset));
    }

    @GET
    @Path("/feed")
    @PermitAll
    @ArticleRequirement({ R2_1, R2_2, R2_3, R2_4, R2_5, R2_6 })
    public ArticlesResponse getFeed(@DefaultValue("20") @QueryParam("limit") int limit,
            @DefaultValue("0") @QueryParam("offset") int offset,
            @HeaderParam("Authorization") String authorization) {
        return ArticlesResponse.of(articles.feed(caller(authorization), limit, offset));
    }

    @POST
    @PermitAll
    @ArticleRequirement({ R4_1, R4_2, R4_3, R4_4, R4_5, R4_6 })
    public Response createArticle(@HeaderParam("Authorization") String authorization,
            @Valid NewArticleRequest request) {
        var draft = request.article();
        var article = articles.create(caller(authorization), draft.title(), draft.description(), draft.body(),
                draft.tagList());
        return Response.status(Response.Status.CREATED).entity(ArticleResponse.of(article)).build();
    }

    @GET
    @Path("/{slug}")
    @PermitAll
    @ArticleRequirement({ R3_1, R3_2, R3_3, R3_4, R3_5, R3_6, R3_7 })
    public ArticleResponse getArticle(@PathParam("slug") String slug,
            @HeaderParam("Authorization") String authorization) {
        var caller = authorization == null ? null : caller(authorization);
        return ArticleResponse.of(articles.get(caller, slug));
    }

    @PUT
    @Path("/{slug}")
    @PermitAll
    @ArticleRequirement({ R5_1, R5_2, R5_3, R5_4, R5_5, R5_6, R5_7, R5_8 })
    public ArticleResponse updateArticle(@PathParam("slug") String slug,
            @HeaderParam("Authorization") String authorization, JsonObject body) {
        if (body == null || !(body.get("article") instanceof JsonObject article)) {
            throw Rejection.invalid("article", "is required");
        }
        var patch = new ArticlePatch(text(article, "title"), text(article, "description"),
                text(article, "body"), tags(article));
        return ArticleResponse.of(articles.update(caller(authorization), slug, patch));
    }

    @DELETE
    @Path("/{slug}")
    @PermitAll
    @ArticleRequirement({ R6_1, R6_2, R6_3, R6_4, R6_5, R6_6 })
    public Response deleteArticle(@PathParam("slug") String slug,
            @HeaderParam("Authorization") String authorization) {
        articles.delete(caller(authorization), slug);
        return Response.noContent().build();
    }

    User caller(String authorization) {
        return users.find(tokens.verify(authorization));
    }

    static String text(JsonObject article, String field) {
        if (!article.containsKey(field)) {
            return null;
        }
        if (article.isNull(field)) {
            throw Rejection.invalid(field, "can't be blank");
        }
        if (!(article.get(field) instanceof JsonString string)) {
            throw Rejection.invalid(field, "must be a string");
        }
        var value = string.getString();
        if (value.isBlank()) {
            throw Rejection.invalid(field, "can't be blank");
        }
        return value;
    }

    static List<String> tags(JsonObject article) {
        if (!article.containsKey("tagList")) {
            return null;
        }
        if (!(article.get("tagList") instanceof JsonArray array)) {
            throw Rejection.invalid("tagList", "can't be blank");
        }
        var tags = new ArrayList<String>();
        for (var element : array) {
            if (!(element instanceof JsonString string)) {
                throw Rejection.invalid("tagList", "must contain only strings");
            }
            tags.add(string.getString());
        }
        return tags;
    }
}
