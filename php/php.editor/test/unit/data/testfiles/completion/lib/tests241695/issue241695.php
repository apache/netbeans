<?php

class A {

  public $prop;

  /**
   * @return this
   */
  public function fn() {
    return $this;
  }

}

trait B {

  /**
   * @var A
   */
  public $a;

}

class C {
  use B;

  public function fn() {
    $this
      ->a    // Auto-completion is fine at this point
      ->fn() // No auto-completion at all
      ->prop // No auto-completion at all
    ;
  }

}
?>