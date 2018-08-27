<?php
class Foo
{
    private $callable;

    public function __construct(callable $callable)
    {
        $this->callable = $callable;
    }

    public function doSomething()
    {
        ($this->callable)();
    }
}
