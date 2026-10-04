package org.acme.tags.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.acme.article.control.Articles;

@ApplicationScoped
public class Tags {

    @Inject
    Articles articles;

    @Transactional
    public List<String> list() {
        return articles.distinctTags();
    }
}
