<?php
class BlaBla {
    private $count = array();//see this line
    function count() {
        $this->count[0]="huh";
        echo $this->count[0];
    }

}
?>