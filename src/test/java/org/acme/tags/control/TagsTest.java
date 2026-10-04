package org.acme.tags.control;

import static org.acme.tags.TagsRequirement.Rn.R1_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.stream.Stream;
import org.acme.article.entity.Article;
import org.acme.tags.TagsRequirement;
import org.hibernate.Session;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
class TagsTest {

    @Inject
    Session session;

    @Inject
    Tags tags;

    @ParameterizedTest(name = "{0}")
    @MethodSource("emptyCases")
    @TestTransaction
    void empty(TagsRequirement.Rn requirement) {
        session.createMutationQuery("delete from Comment").executeUpdate();
        session.createMutationQuery("delete from Favorite").executeUpdate();
        session.createSelectionQuery("from Article", Article.class)
                .getResultList()
                .forEach(session::remove);
        session.flush();
        assertThat(tags.list())
                .as(requirement + " — " + requirement.statement())
                .isEmpty();
    }

    static Stream<Arguments> emptyCases() {
        return Stream.of(arguments(R1_3));
    }
}
