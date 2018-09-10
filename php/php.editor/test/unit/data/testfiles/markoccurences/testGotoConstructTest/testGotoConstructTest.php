<?php
class MyClassConstr  {
    public function __construct() {//MyClassConstr
        ;
    }
}
class MyClassConstr2 extends MyClassConstr  {}//MyClassConstr2

$a = new MyClassConstr();
$b = new MyClassConstr2();

?>
