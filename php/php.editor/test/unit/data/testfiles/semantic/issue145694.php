<?php

class Country {
    public $name;
    public $code;
}

class Address {
    public $street;
    public $city;
    /**
     *
     * @var Country
     */
    public $country;
}

class Person {
    /**
     *
     * @var Address
     */
    private $address;

    function getCity() {
        $this->address->city;
        $this->address->country->name;
    }
}
?>