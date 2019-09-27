<?php

interface A {
    public function functionName(A $param);
}

class ClassName {

    function __construct() {
    }

}

__halt_compiler();?>

<?php
class ThisIsNotParsed {

    function __construct() {

    }

}
