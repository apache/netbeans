<?php

trait foo {
  public $bar;

  public function &getBar() {
    return $this->bar;
  }
}
?>