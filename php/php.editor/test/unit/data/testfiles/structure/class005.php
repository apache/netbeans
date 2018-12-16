<?php
class person {                                // class name
    private $name;                            // class field declaration
    public $me = "mydefaultname";             // class field declaration
    private $you;                             // unused private class field
    static private $count = 0;                // static private class field
    static private $test = 1;                 // unused static private filed

    public function __construct($name) {      // method name
        $this->name = $name;                  // usage of class field
        echo $this->$name."\n";               // $name is on class field
        echo $this->name."\n";                // usage of class field
        person::$count = person::$count + 1;
    }

    private function yourName() {             // unused method
        return "yourName";
    }

    public function name() {                  // method name
        return $this->name;                   // usage of class field
    }

    public static function getCount() {       // static method name
        return person::$count;                 // usage of static field
    }

    private static function getCount2() {     // unused static method name
        // TODO addd the content
    }
}

$p = new person("me");
echo "persons: ".person::getCount();          // usage of static method
?>
