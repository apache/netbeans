<?php
declare(strict_types=1);
namespace MyProject;
use YourProject;
use My\Full\Classname as Another;
// this is the same as use My\Full\NSname as NSname
use My\Full\NSname;
// importing a global class
use \ArrayObject;
const CONNECT_OK = 1;
class Connection {
private $field1;
private $field2 = "example";
public function method($text, $number){
}
}
function connect() {
}
namespace AnotherProject;
const CONNECT_OK = 1;
class Connection {
    /**
     * comment for field1
     */
    public $field1;
/**
     * comment for field2
     */
    public $field2;
}
function connect() {
}
?>
