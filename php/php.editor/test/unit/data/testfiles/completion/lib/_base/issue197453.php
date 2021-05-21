<?php
class Book {
    public $name;
    const TEST = 'Simon';

    function __construct($name) {
        $this->name = $name;
    }

    public function getName() {
        return $this->name;
    }

    public function setName($name) {
        $this->name = $name;
        echo "class property: $this->name";
    }
}