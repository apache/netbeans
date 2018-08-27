<?php
class myClass {
    /**
     * @return myClass[]
     */
    public function retObjArray()
    {
        return array(new myClass());
    }

    public function testHinting()
    {
        $myArr = $this->retObjArray();

        foreach ($myArr as $a)
        {
            $a->otherFunc(); // myClass type hinting works here (yay!)
        }

        foreach ($this->retObjArray() as $b)
        {
            $b->retObjArray();  // myClass type hinting doesn't work here
        }
    }
}
?>