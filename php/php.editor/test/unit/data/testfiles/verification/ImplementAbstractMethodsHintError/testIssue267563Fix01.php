<?php

interface FooInterface
{
   public function someMethod(): self;
}

class Foo implements FooInterface
{
}
