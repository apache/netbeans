<?php
class Article {
function functionName($param) {
}
}

/**
 * @param Article[] $articles
 */
function parseArticles_01($articles) {
    foreach ($articles as $article) {
        $article-> //no array type hint
    }
}

/**
 * @param Article[] $articles
 */
function parseArticles_02(array $articles) {
    foreach ($articles as $article) {
        $article-> //array type hint
    }
}
?>