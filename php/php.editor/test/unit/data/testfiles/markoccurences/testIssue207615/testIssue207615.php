<?php

namespace {
class Test {

    protected static $_var = true;

    public static function getVar() {
        self::$_var;
        return static::$_var;
    }
}
}

?>