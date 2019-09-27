<?php

/**
 * @property T1|T2|T3 $multi
 * @property T1 $single
 */
class Test
{
    /**
     * @var T1|T2|T3
     */
    public $v;
}

class T1
{
    public $t1;
}

class T2
{
    public $t2;
}

class T3
{
    public $t3;
}

$test = new Test();

$test->single->t1;
$test->multi->t1;
