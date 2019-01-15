<?php

class First
{
   public function fMethod()
   {
   }
}

class Second
{
   /**
    * The First class
    * @var First
    */
   public static $first;

   /**
    * @return First
    */
   public static function sMethod()
   {
      self::$first->fMethod();
      static::$first->fMethod();
      Second::$first->fMethod();
   }
}
?>