<?php

class ClassName {

    const MY_CONS = 5;

    function __construct() {
        self::MY_CONST;
        static::MY_CONST;
        ClassName::MY_CONST;
    }

}

?>