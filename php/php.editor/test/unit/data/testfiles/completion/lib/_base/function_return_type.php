
<?php

  class TestClass{
      public $tst;

      /**
       * @return TestClass
       */
      function foo(){}

      /**
       * @return TestClass
       */
      static function foo_static(){
          $result_from_self = self::foo_static();
          $result_from_self->foo();
      }
  }

  /**
   * @return TestClass
   */
  function testClassReturningFunction(){}
  $result_from_standalone_function = testClassReturningFunction();
  $result_from_standalone_function->foo();
  $result_from_static_method = TestClass::foo_static();
  $result_from_static_method->foo();
  $result_from_dyn_method = $result_from_static_method->foo();
  $result_from_dyn_method->foo();

?>