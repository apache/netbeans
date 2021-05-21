<?php

abstract class Foo {

    public function __construct($foo) {}

    public abstract function baz();

    public function bat() {}

}

class Bar extends Foo {

// CC HERE

}

?>