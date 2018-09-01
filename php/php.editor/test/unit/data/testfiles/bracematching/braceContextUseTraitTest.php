<?php
trait MyTrait
{
    public function hello() {
        echo "hello";
    }
}

class MyClass {

    use MyTrait
    {
        hello as protected;
    } // use
}
