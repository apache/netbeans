<?php

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
namespace Libs\Bar;

include 'Libs/Bar/IBuz.php';

use Libs\Bar as Gogo;
/**
 * Description of Buz
 *  
 * @author cesilko
 */  
class Buz implements Gogo\IBuz {
    //put your code here  
    public function barMoje() {
        return "Do something";
    }
    
    function printMe() {
        echo " ja jsem z Bar";
    }
} 


?>