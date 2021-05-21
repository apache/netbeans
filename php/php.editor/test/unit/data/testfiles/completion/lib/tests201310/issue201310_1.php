<?php
namespace MySpaceThrow; // here I can redeclare Exception class without any problem
class Exception {} // should be in signature files in default namespace

namespace ExtraThrow;
class ExMyExceptionFromNs extends MySpace\Exception {}

class EmptyClassFromNs {}

class InvisibleClassFromNs {}

class TestClassFromNs {
    function functionName($param) {
        try {
            // It shouldn't be suggested, because they are not extending Exception class from default namespace
            throw new ExMyException();
        } catch (ExMyException $ex) {

        }
    }
}