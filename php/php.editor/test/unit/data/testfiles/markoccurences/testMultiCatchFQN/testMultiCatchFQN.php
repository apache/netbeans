<?php

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

namespace Test\Sub2;

try {
    echo "Multi Catch";
} catch (\Test\Sub\ExceptionType1 | \Test\Sub\ExceptionType2 $ex) {
    $ex->getTraceAsString();
} catch (\Test\Sub\ExceptionType3 $ex) {
    $ex->getTraceAsString();
}

try {
    echo "Multi Catch";
} catch (\Test\Sub\ExceptionType1 | \Test\Sub\ExceptionType2 | \Test\Sub\ExceptionType3 $ex) {
    $ex->getTraceAsString();
} finally {
    echo "finally";
}

try {
    echo "Multi Catch";
} catch (\Test\Sub\ExceptionType1 $ex) {
    $ex->getTraceAsString();
} catch (\Test\Sub\ExceptionType2 | \Test\Sub\ExceptionType3 $ex) {
    $ex->getTraceAsString();
}
