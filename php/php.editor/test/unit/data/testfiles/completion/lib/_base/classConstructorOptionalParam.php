<<?php

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

class A {

    private $var;
    private $var2;
    
    function __construct($var, $var2 = 10) {
        $this->var = $var;
    }
    
    function m1($param1, $param2 = 20) {
        
    }
    
    function printVar() {
        echo "$this->var + $this->var2\n";
    }

}

$var = new A($var, $var2);
$var->printVar();

?>
