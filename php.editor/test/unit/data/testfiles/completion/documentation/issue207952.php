<?php
namespace Foo;
/**
 * @method MyBarek aMagicMethod(\Foo\BarType $paramName) My Description.
 */
class MyBarek {

    /**
     * My Description.
     *
     * @param \Foo\BarType $paramName
     * @return MyBarek
     */
    function nonMagicMethod(\Foo\BarType $paramName) {

    }

}

$my = new MyBarek();
$my->aMagicMethod($paramName);
$my->nonMagicMethod($paramName);
?>