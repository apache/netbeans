<?php
class ClsFromFactory {
    private $myfld = 0;
    public function echome() {
        echo ++$this->myfld."\n";//not working
    }
}
?>