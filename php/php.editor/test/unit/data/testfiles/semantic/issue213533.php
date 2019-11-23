<?php

const BAR_NO = 5;
echo BAR_NO; // black, normal
echo Foo::ERR_NO; // green, italic
class Foo {
    private $field;
    const ERR_NO = 1;
    function functionName($param) {
	echo self::ERR_NO;
    }
}

?>