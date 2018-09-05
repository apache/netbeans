<?php

interface Punchable {

    public function punch();

    public function isStanding();
}

class Boxer implements Punchable {
    private $hitpoints;

    public function __construct() {
        $this->hitpoints = 10;
    }

    public function punch() {
        $this->hitpoints--;
    }

    public function isStanding() {
        return $this->hitpoints>0;
    }
}

function fight(Punchable $p) {
    while($p->isStanding()) {
    	$p->punch();
        echo "Hit!\n";
    }
    echo "Victory!";
}

fight(new Boxer());

?>