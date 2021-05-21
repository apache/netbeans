<?php
// I'm in default namespace, I shouldn't redeclare Exception class -> PHP Fatal error
class Exception {} // should be in signature files in default namespace

class ExMyException extends Exception {}

class EmptyClass {}

class InvisibleClass {}

class TestClass {
    function functionName($param) {
        try {
            throw new ExMyException();
        } catch (ExMyException $ex) {

        }
    }
}