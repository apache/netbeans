<?php

class Foo
{
    protected $status = 200;

    public function __construct()
    {
        echo((string)$this->status)[0] . PHP_EOL;
        echo((string)$this->status)[0][0];
        echo((string)($this->status))[0];
        echo((string)($this->status))[0][0];
    }
}

new Foo();

$int = 300;
echo((string) $int)[0][0];

$string = "400";
echo ($string)[0];
