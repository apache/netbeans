<?php

class Dummy {}

class Exception {} // need Exception class because only extending classes are checked

namespace Test\Sub;

class ExceptionType1 extends \Exception {

    public function exception1() {
    }

}

class ExceptionType2 extends \Exception {

    public function exception2() {
    }

}

class ExceptionType3 extends \Exception {

    public function exception3() {
    }

}
