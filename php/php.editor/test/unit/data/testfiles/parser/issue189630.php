<?php
class Father {
    static function define() { return 1; }
}

class Child extends Father {
   static function define() { return parent::define(); }
}
?>