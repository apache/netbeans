<?php

interface FooInterface
{
   /**
    * @return Bar|Baz
    */
   public function someMethod(int $baz);
}

class Foo implements FooInterface
{
}
