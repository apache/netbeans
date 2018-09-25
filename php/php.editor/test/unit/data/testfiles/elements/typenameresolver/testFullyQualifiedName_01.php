<?php

namespace Test2;
use Test\Omg;
class Foo2 implements Foo, Omg
{
    /*^*/
}
namespace Test;
interface Omg {
    public function aaa(Omg $param);
}

?>