<?php
class Author {
    public $name;
    function __construct() {}//Author
}

/**
 * @property Author $author hello this is doc
 */
class Book {
    public $Title;
    function __construct() {}//Book
    function test($hello) {//method
        $tmp = $hello;
        $this->author;
    }
}

/**
 * @param Book $hello
 * @return Author
 */
function test($hello) {//function
}
?>
