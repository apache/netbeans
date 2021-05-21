<?php
/**
 * @method string blah blah($whatever = null) Desc
 */
class TestMethod {

    function __construct() {

    }

    /**
     * @method int foo foo(string $myStr) Desc
     *
     * @param type $name
     * @param type $param
     */
    function __call($name, $param) {

    }

}

$test = new TestMethod();
$test->
?>