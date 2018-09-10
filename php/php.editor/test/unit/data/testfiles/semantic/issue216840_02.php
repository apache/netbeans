<?php

class test {

    private $age;

    public function getName() {
        return $this->name;
    }

    public static  function getCount() {
        return self::getHelpCount();
    }

    private static function getHelpCount() {
        return test::$count;
    }

    static private $count = 5;
    private $name = "petr";
}

echo "Number: ".test::getCount()."\n";
$myvar = new test("Pepa");
echo "Name: ".$myvar->getName()."\n";
?>
