<?php
$ahoj = 10;
class PhpDoc01Magazine {
    public $pages;
}

class PhpDoc01News {
    public $content;
}

/**
 *
 * @return PhpDoc01News|PhpDoc01   text|@ text
 */
function test() {
    return 'ahoj';
}
