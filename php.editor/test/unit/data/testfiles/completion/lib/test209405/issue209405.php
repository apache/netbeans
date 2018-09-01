<?php

class Abc {
  function f(){}
}
/* @var $b['y'] Abc */
$b['y']->f();

/* @var $c["y"] Abc */
$c["y"]->f();

?>