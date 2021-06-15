<?php

/**
 * @param int $a
 * @property double $b Description
 * @property-read string $c
 * @property-write string $d Description
 */
class Test1 {

    public $a;
    private $b;
    protected $c;
    protected $d;

}

/**
 * @param int $a
 * @property double $b Description
 * @property-read string $c
 * @property-write string $d Description
 */
class Test2 {

    public static $a;
    private static $b;
    protected static $c;
    protected static $d;

}

/**
 * @param $a
 * @property $b Description
 * @property-read $c
 * @property-write $d Description
 */
class Test3 {

    public $a;
    private $b;
    protected $c;
    protected $d;

}

/**
 * @param int $a
 * @property double $b Description
 * @property-read string $c
 * @property-write string $d Description
 */
trait Trait1 {

    public $a;
    private $b;
    protected $c;
    protected $d;

}

/**
 * @param int $a
 * @property double $b Description
 * @property-read string $c
 * @property-write string $d Description
 */
trait Trait2 {

    public static $a;
    private static $b;
    protected static $c;
    protected static $d;

}

/**
 * @param $a
 * @property $b Description
 * @property-read $c
 * @property-write $d Description
 */
trait Trait3 {

    public $a;
    private $b;
    protected $c;
    protected $d;

}
