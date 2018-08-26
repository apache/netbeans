<?php
echo Mammal::$count;
abstract class Animal {
    const KIND=1;
    static $animalSpecies = array();
    public static $count = 0, $animal;
    public abstract function isMammal();
    function __construct() {
        self::$count++;
        self::$animal = $this;
        self::getCount("calling animal's getCount 0");
        echo self::KIND;
    }
    public function getCount($animalLogging) {
        echo $animalLogging;
        return Animal::$count;
    }
    public static function getAnimal() {
        $species = self::$animalSpecies;
        $first = self::$animalSpecies[0];
        return self::$animal;
    }

    public static function kindInfo() {return "animal is ...";}

}

class Mammal extends Animal {
    //const KIND=2;
    public function isMammal() {
        return false;
    }
    function __construct() {//Mammal
        parent::__construct();
        Mammal::$count++;
        echo parent::$count;
        echo self::$count;
        echo parent::getCount("calling animal's getCount 1");
        $mammalKind = Mammal::KIND;
        $animalKind = Animal::KIND;
        $isMe = (self::KIND == $mammalKind);
        $isParentAnimal = (parent::KIND == $animalKind);
    }
}

class Cat extends Mammal {
    const KIND=3;
    public static $count = 0, $cat;
    function __construct() {
        parent::__construct();
        Cat::$count++;
        echo parent::getCount("calling animal's getCount 2");
        echo $this->getCount("calling cat's getCount");
        $catKind = self::KIND;
        echo Animal::KIND;
        echo Mammal::KIND;
        echo Cat::KIND;
        echo Animal::kindInfo();
        echo Mammal::kindInfo();
        echo Cat::kindInfo();
        echo self::kindInfo();
        echo parent::kindInfo();

    }
    public function getCount($catLogging) {
        echo $catLogging;
        return Cat::$count;
    }
    public static function kindInfo() {return "cat is ...";}
}
Animal::$count--;
Mammal::$count--;
Cat::$count--;
print Animal::KIND;
print Mammal::KIND;
print Cat::KIND;
print Animal::kindInfo();
print Mammal::kindInfo();
print Cat::kindInfo();


$mammal = new Mammal;
$mammal->getCount("calling animal's getCount 3");
$cat = new Cat;
$cat->getCount("calling cat's getCount 1");
?>
