<?php

class A
{
    public $CLASS_FOO;
    const CLASS_FOO = "FOO";
    const CLASS_FOO = "FOO2";
    const class_foo = "foo";

    public function class_foo(){
    }

    const CLASS_FOO = "FOO3";
}

abstract class B
{
    const ABST_CLASS_BAR = 'BAR';
    const ABST_CLASS_BAR = 'BAR2';
    const ABST_CLASS_BAR = 'BAR3';
    const abst_class_bar = 'bar';
    const abst_class_bar = 'bar2';
}

interface C
{
    const INTERFACE_BAZ = 'BAZ';
    const INTERFACE_BAZ = 'BAZ2';
}
