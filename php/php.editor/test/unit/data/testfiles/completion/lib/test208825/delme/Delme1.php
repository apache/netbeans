<?php
namespace delme;

require_once 'Delme2.php';

class Delme1 {

    /**
     *
     * @return \Delme2 
     */
    public function get_delme2() {
        $rval = new \Delme2;
        return $rval;
    }
}