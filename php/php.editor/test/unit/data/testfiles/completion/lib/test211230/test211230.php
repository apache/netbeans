<?php
class Ondrej {

    function fooMethod() {
    }

}
/**
 * @method Ondrej|Brejla method() This is my cool magic method description.
 */
class Brejla {
    function barMethod() {
    }
}
$b = new Brejla();
$b->method()->fooMethod();
?>