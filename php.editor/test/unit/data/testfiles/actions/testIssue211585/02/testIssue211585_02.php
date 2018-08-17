<?php

namespace Foo\Bar;
class ClassName {}

namespace Fom\Bom;
class ClassName {}

namespace Baz\Bat;
class ClassName {}

namespace Omg;

$a = new ClassName();//HERE
$b = new \Baz\Bat\ClassName();
$b = new \Fom\Bom\ClassName();
?>