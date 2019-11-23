<?php

class ClassName {

    function __construct() {
        $foo = "omg";

        $this->$foo();
        self::$foo();
        static::$foo();
        parent::$foo();
    }

}
?>