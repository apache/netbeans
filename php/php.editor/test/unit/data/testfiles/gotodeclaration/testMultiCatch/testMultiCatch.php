<?php

class Exception {} // need Exception class because only extending classes are checked

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

try {
    echo "Multi Catch";
} catch (ExceptionType1 | ExceptionType2 $ex) {
    $ex->getTraceAsString();
} catch (ExceptionType3 $ex) {
    $ex->getTraceAsString();
}

try {
    echo "Multi Catch";
} catch (ExceptionType1 | ExceptionType2 | ExceptionType3 $ex) {
    $ex->getTraceAsString();
} finally {
    echo "finally";
}

try {
    echo "Multi Catch";
} catch (ExceptionType1 $ex) {
    $ex->getTraceAsString();
} catch (ExceptionType2 | ExceptionType3 $ex) {
    $ex->getTraceAsString();
}
