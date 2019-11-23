<?php

class ClassName {
    /**
     * Nejaky popis.
     *
     * @var int
     */
    public $fieldWithDesc;
    
    /**
     * Nejaky popis.
     *
     * @var int[]
     */
    public $arrayFieldWithDesc;

}

$c = new ClassName();
$c->fieldWithDesc;
$c->arrayFieldWithDesc;

class ClassName1 {

    /**
     * @var int
     */
    public $fieldWithoutDesc;

    /**
     * @var int[]
     */
    public $arrayFieldWithoutDesc;

}


$c1 = new ClassName1();
$c1->fieldWithoutDesc;
$c1->arrayFieldWithoutDesc;

?>