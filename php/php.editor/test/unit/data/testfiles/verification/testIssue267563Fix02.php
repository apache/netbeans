<?php

interface FooInterface
{
   public function someMethod(self $baz);
}

class Foo implements FooInterface
{
}
