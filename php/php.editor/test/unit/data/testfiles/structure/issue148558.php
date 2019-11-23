<?php
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

class Address {
    private $street;
    private $city;
    private $zip;

    function __construct($street, $city, $zip) {
        $this->street = $street;
        $this->city = $city;
        $this->zip = $zip;
    }

    public function getStreet() {
        return $this->street;
    }

    public function getCity() {
        return $this->city;
    }

    public function getZip() {
        return $this->zip;
    }

}

define("KOLESA", 53314);
define("KLADRUBY", 53316);
define("PRELOUC", 53316);

?>