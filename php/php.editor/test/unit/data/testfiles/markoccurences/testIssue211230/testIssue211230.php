<?php
class Foo {

    function fooMethod() {
    }

}
/**
 * @method Foo|Bar method() This is my cool magic method description.
 */
class Bar {
    function barMethod() {
    }
}
$b = new Bar();
$b->method()->fooMethod();
?>