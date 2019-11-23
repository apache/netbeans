<?php
class SmartCompletion {

    /**
     *
     * @param string $string
     */
    private function myStringFnc($string) {}

    /**
     *
     * @param float $float
     */
    private function myFloatFnc($float) {}

    /**
     *
     * @param int $int
     */
    private function myIntFnc($int) {}

    /**
     *
     * @param string $first
     * @param int $second
     */
    private function matchNames($first, $second) {}

    /**
     *
     * @param float $first
     */
    private function dontMatchNames($first) {}

    /**
     *
     * @param array $array
     */
    private function myArrayFnc($array) {}

    /**
     *
     * @param mixed $mixed
     */
    private function myMixedFnc($mixed) {}

    private function testContext() {
        $first = 'str';
        $second = 5;
        $myString = 'str';
        $myFloat = 12.5;
        $myInt = 4;
        $myArray = array();

        $this->myStringFnc($myString);
        $this->myFloatFnc($myFloat);
        $this->myIntFnc($myInt);
        $this->myArrayFnc($myArray);

        $this->matchNames($first, $second);
        $this->dontMatchNames($myFloat);

        $this->myMixedFnc($myArray);
    }

}
