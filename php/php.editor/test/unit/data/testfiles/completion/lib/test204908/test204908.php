<?php
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Enter description here ...
 * @author Harunaga
 *
 */
class ObjectTracker {
    private  $name;

    function setName($name) {
        $this->name = $name;
    }

    function getName(){
        return $this->name;
    }
}

$ot = new ObjectTracker("Zeev's Objcet");
$ot2 = clone $ot;
$ot2->setName("harunaga");
?>