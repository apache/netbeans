<?php

abstract class Person {
    $this->age; //a mistake
    abstract function customizedFunction() {}
    function getAge() {
        return $this->age;
    }
    function setAge($age) {
        $this->age = $age;
    }
}

?>