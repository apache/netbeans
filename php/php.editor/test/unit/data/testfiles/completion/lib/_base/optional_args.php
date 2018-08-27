<?php
  class TestOptionalArgsClass{
      static function test($a, $b = 1, $c = 1){}
  }

  function testOptionalArgsFunc($a, $b = 1){}

  TestOptionalArgsClass::test($b, $d);
  $foo = testOptionalArgsFunc($a);
?>