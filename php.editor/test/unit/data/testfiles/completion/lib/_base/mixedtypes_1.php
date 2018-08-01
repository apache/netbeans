<?php
class Author_1 {
    public $name;
}

class Book_1 {
    /**
     * Title of the book.
     * @var string
     */
    public $title;

    /**
     *
     * @var Author_1|null
     */
    public $author;

    private function __construct($title) {
        $this->title = $title;
    }

    /**
     *
     * @param string $title
     * @return Book_1
     */
    public static function createBook_1($title) {
        return new Book_1($title);
    }
}

class Magazine_1 {
    public $pages;
}

/**
 * @return Book_1|Magazine_1
 */
function getBookMagazine() {

}

getBookMagazine()->pages;

$bm = getBookMagazine();

$bm->author;


/**
 * @return Book_1|null
 */
function getBook(){

}

getBook()->author;

?>