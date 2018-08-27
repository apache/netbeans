<?php

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
namespace Libs\Bar2;

include 'Libs/Bar/IBuz.php';

use Libs\Bar;


/**
 * Description of Buz
 *  
 * @author cesilko
 */  
class Buz implements Bar\IBuz {
    //put your code here  
    public function barMoje() {
        return "Do something";
    }
    
    function printMe() {
        echo " ja jsem z Bar";
    }
} 


?>