<?php
namespace MySpace; // here I can redeclare Exception class without any problem
class Exception {} // should be in signature files in default namespace

namespace Extra;
class ExMyExceptionFromNs extends MySpace\Exception {}

class EmptyClassFromNs {}

class InvisibleClassFromNs {}

class TestClass {
    function functionName($param) {
        try {
            new ExMyException();
            // It shouldn't be suggested, because they are not extending Exception class from default namespace
        } catch (ExMyException $ex) {

        }
    }
}