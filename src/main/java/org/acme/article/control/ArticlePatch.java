package org.acme.article.control;

import java.util.List;

public record ArticlePatch(String title, String description, String body, List<String> tagList) {

    public boolean isEmpty() {
        return title == null && description == null && body == null && tagList == null;
    }
}
