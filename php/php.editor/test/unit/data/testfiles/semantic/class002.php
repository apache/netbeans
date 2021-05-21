<?php
class test {
  protected $x;

  static private $test = NULL;
  static private $cnt = 0;

  static function factory($x) {
    if (test::$test) {
      return test::$test;
    } else {
      test::$test = new test($x);
      return test::$test;
    }
  }

  protected function __construct($x) {
    test::$cnt++;
    $this->x = $x;
  }

  static function destroy() {
    test::$test = NULL;
  }

  protected function __destruct() {
  	test::$cnt--;
  }

  public function get() {
    return $this->x;
  }

  static public function getX() {
    if (test::$test) {
      return test::$test->x;
    } else {
      return NULL;
    }
  }

  static public function count() {
    return test::$cnt;
  }
}

test::getX();

?>