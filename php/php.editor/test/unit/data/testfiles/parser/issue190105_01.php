<?php

interface A {
    public function functionName(A $param);
}

class ClassName {

    function __construct() {
    }

}

__halt_compiler();

class ThisIsNotParsed {

    function __construct() {

    }

}
