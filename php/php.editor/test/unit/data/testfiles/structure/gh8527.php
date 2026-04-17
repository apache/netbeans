<?php

trait MyTrait
{
    abstract function doSomething();

    public function doNothing()
    {

    }
}

class MysClass
{
    public function getObject()
    {
        return new class() {

            public function getMyTraitAwareObject()
            {
                return new class() {
                    use MyTrait;

                    #[\Override]
                    public function doSomething()
                    {

                    }
                };
            }
        };
    }
}
