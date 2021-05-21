<?php
class myClass {
    /**
     * @return myClass[]
     */
    private $retObjArray;

    public function testHinting()
    {
        $myArr = $this->retObjArray();

        foreach ($myArr as $a)
        {
            $a->otherFunc(); // myClass type hinting works here (yay!)
        }

        foreach ($this->retObjArray as $b)
        {
            $b->testHinting();  // myClass type hinting doesn't work here
        }
    }
}
?>