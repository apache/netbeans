<?php
interface If1 { public function testIf1(); }
interface If2 { public function testIf2(); }
class TestClass {
	/**
	 * @var If1|If2
	 */
	public $_testObject;
}
$test = new TestClass();
$test->_testObject->testIf1();
?>