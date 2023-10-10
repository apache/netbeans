<?php
declare(strict_types=1);
namespace MyProject;
use YourProject;
use My\Full\Classname as Another;
// this is the same as use My\Full\NSname as NSname
use My\Full\NSname;
// importing a global class
use \ArrayObject;
use function My\Functions\my_function;
use function My\Functions\Grouped\{
    A\function_a,
    B\function_b,
};
use const My\Constants\MY_CONSTANT;
const CONNECT_OK = 1;
class Connection {
private $field1;
private $field2 = "example";
public function method($text, $number){
}
}
function connect() {
}
enum Enumeration: int implements I {
    case A = 1;
    case B = 2;
    const CONSTANT = self::A;
    public function impl(): void {
    }
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
    #[Attr(1, "param")]
    public $field3;
}
function connect() {
}
?>
