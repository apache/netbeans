<?php
  class ParentClass{
      const ParentClassConst = 1;
      public function publicParentMethod(){
          self::ParentClassConst;
      }

      protected function protectedParentMethod(){}

      private function privateParentMethod(){}
  }

  class ChildClass extends ParentClass{
      function publicChildMethod(){
          parent::protectedParentMethod();
      }

      private function privateChildMethod(){}
  }

  $tst = new ChildClass;
  $tst->publicChildMethod();
?>