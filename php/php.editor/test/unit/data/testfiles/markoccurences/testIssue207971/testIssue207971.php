
<?php
class ClassName {

    private $field1;
    private $field3;
    private $object2;

    function __construct() {
        $sql = " {$this->field1} {$this->object2->xxx} {$this->field3['array1']} ";
    }

}
?>