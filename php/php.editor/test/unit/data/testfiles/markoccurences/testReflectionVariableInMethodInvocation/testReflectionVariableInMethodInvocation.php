<?php
namespace Foo;

class MyBarek {
    private $context;

    function nonMagicMethod(\Foo\BarType $paramName) {
        $this->{"context".$this->context[0]}();
    }

}
?>