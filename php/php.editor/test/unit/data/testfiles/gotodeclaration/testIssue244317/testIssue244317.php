<?php

class TestClass {

   const testConstant = "test";

   public function example() {
      $variable = self::testConstant;
  
      $this->useCallback(function () {
         echo self::testConstant;
      });
   }

}

?>