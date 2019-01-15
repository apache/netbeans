<?php

abstract class GoogleChart {


    public $colorMap = array(
        "Black"    => "000000",
        "LightSeaGreen"    => "20B2AA"    ,
        "LightYellow"    => "FFFFE0" // <- syntax error, ',' is missing
        "Ivory"    => "FFFFF0",
        "White"    => "FFFFFF"
    );

   abstract function createGraph();

   function graph() {
   
   }
}

?>