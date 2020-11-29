<?php

class Foo {

    public const PUBLIC_CONST = 1;
    protected const PROTECTED_CONST = 2;
    private const USED_PRIVATE = 3;
    private const UNUSED_PRIVATE = 4;

    function bar() {
        return self::USED_PRIVATE;
    }
}
