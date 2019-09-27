<?php



namespace test\sub;



class MyClass203419
{

/**
 * @var \test\sub\MyClass203419
 */
 private $_test;
 
 public function test($param) {
     
     echo $param;
 }
 
 public function test2(MyClass203419 $param) {
     
     echo $param;
 }

}

$v1 = new \test\sub\MyClass203419();
$v1->test("v1\n");
$v2 = new MyClass203419();
$v2->test("v2\n");

use \test\sub;

$v3 = new sub\MyClass203419();
$v3->test("v3\n");


use \test\sub as baf;

$v4 = new baf\MyClass203419();
$v4->test("v4\n");

?>