<?php
  class Test144409{
      function foo(){}
  }

  /**
   * @return Test144409;
   */
  function test144409Func(){}

  $tmp = test144409Func();
  $tmp->foo();
?>