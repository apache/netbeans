<?php
  interface ParentInterface{
      function parentInterfaceFunction();
  }

  interface ChildInterface extends ParentInterface{
      function childInterfaceFunction();
  }

  function testFunc(ChildInterface $arg){
      $arg->parentInterfaceFunction();
  }
?>