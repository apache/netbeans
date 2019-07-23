<?php

namespace Foo\Bar;

trait FirstTrait {}

namespace Baz\Bat;

trait SecondTrait {}

namespace Fom\Bom;

trait MyTrait {}

class ClassName {

    use FirstTrait, SecondTrait, MyTrait;

}


?>