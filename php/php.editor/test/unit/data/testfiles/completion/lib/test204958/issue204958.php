<?php
// I'm in default namespace, I shouldn't redeclare Exception class -> PHP Fatal error
class Exception1 {} // should be in signature files in default namespace

class ExMyException1 extends Exception1 {}

class InstanceOfFoo
{
}

class InstanceOfBar
{
    function test($obj)
    {
        if ($obj instanceof I
    }
}

?>