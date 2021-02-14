<?php

class A
{
    public $foo;
    private $foo = "foo";
    protected $foo;
    var $foo;
    public static $foo;
    private static $foo;
    protected static $foo;
    static $foo;
    public $FOO;

}

abstract class B
{
    private $bar;
    protected $bar;
    var $bar;
    public $bar = 'bar';
    public static $bar;
    private static $bar;
    protected static $bar;
    static $bar;
    public $BAR;
}

trait T
{
    protected $baz;
    public $baz;
    var $baz;
    private $baz;
    public static $baz;
    protected static $baz;
    private static $baz;
    static $baz;
    protected $BAZ;
}
