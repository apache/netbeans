<?php
  TestCCOnMethods::create();
  class TestCCOnMethodsParentClass{
      /**
       * @return TestCCOnMethodsParentClass
       */
      function parentInstance(){}
  }

  class TestCCOnMethods extends TestCCOnMethodsParentClass {

  /**
   * @return TestCCOnMethods
   */
  function newInstance()
  {
      parent::parentInstance()->parentInstance();
      return self::create()->newInstance();
  }

  /**
   * @return TestCCOnMethods
   */
  static function create()
  {

  }
};

{
    $tst1 = new TestCCOnMethods();
    $tst2 = $tst1->newInstance()->newInstance();
}

TestCCOnMethods::create()->newInstance();

/**
* @return TestCCOnMethods
*/
function foo_TestCCOnMethods(){

}

foo_TestCCOnMethods()->newInstance()->newInstance();

?>
