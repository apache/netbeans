<?php

interface Foo {
    function myFoo(?string $string, int $int): ?Foo;
}

class Bar implements Foo {

}
