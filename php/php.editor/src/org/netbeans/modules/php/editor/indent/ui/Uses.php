<?php

use Foo\Bar\Baz;

// "Start Use Statements with Namespace Separator"
use \Foo\Bar\Bat;

// "Prefer Multiple Use Statements Combined"
use \Foo\Bar\Baz,
    \Foo\Bar\Bat;

// "Prefer Group Use Statements"
use \Foo\Bar\ {
    Baz,
    Bat
};

class Bat {

    function __construct() {
        Baz::getInstance();
        // "Prefer Fully Qualified Names over Use of Unqualified Names"
        \Foo\Bar\Baz::getInstance();
    }

}

?>
