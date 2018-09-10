<?php

class test {

    function getTestName() {
        return "bla from testName";
    }

}

$variable = new test;

class some_class {

    public function some_method() {
        global $variable;
        echo $variable->getTestName();
    }

}