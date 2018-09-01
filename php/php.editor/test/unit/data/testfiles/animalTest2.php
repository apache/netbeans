<?php
class Fish extends Animal {
    public static $count = 0;
    function isMammal() {
        return false;
    }
    function __construct($info) {
        parent::__construct();
        Fish::$count++;
        parent::$count;
        $this->getCount("");
        parent::getCount("");
        self::getCount("");
        echo Animal::KIND;
        echo Mammal::KIND;
        echo Cat::KIND;
        echo Animal::kindInfo();
        echo Mammal::kindInfo();
        echo Cat::kindInfo();
        echo self::kindInfo();
        echo parent::kindInfo();
    }
}

class Shark extends Fish {
    public static $count = 0;
    function __construct() {
        parent::__construct("");
        Shark::$count++;
        echo "".self::kindInfo();
    }
    public function getCount($sharkLogging) {
        return Shark::$count;
    }
    function getAnimalCount() {
        return Animal::$count;
    }
    public static function kindInfo() {return "shark is ...";}
}
$mammal = new Mammal;
$mammal->getCount("");
$cat = new Cat;
$cat->getCount("");
$fish = new Fish;
$fish->getCount("");
$shark = new Shark;
$shark->getCount("");
print Animal::KIND;
print Mammal::KIND;
print Cat::KIND;

?>
