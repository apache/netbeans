<?php

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
namespace Libs\Bar;

include 'Libs/Bar/IBuz.php';

/**
 * Description of Buz
 *  
 * @author cesilko
 */  
class Buz implements Foo\IBuz {
    //put your code here  
    public function barMoje() {
        return "Do something";
    }
    
    function printMe() {
        echo " ja jsem z Bar";
    }
} 


?>