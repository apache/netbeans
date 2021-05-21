<?php

Class A
{
    /** @var B */
    public $b;

    function  __construct()
    {
      $this->b = new B();
    }
}

Class B
{
    function testB()
    {
        return "test";
    }
}

/**
 * @global A $GLOBALS['a']
 * @name $a
 */
$GLOBALS['a'] = new A();


/**
 * @global A $a
 */
function test()
{
    global $a;

    //completion on Class A
    //$a->

    //CC of class B
    /*1*/$a->b->

    //NO CC HERE - OK
    /*2*/$a->b->b->
}

global $a;
//CC of class B
/*3*/$a->b->

?>