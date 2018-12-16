<?php

interface Bar {}
interface Baz {}

class Foo {
    /**
     * @var Bar|Baz
     */
    private $barBaz;
}
