<?php

namespace App\Test;

class IndexController extends \Brite\Controller\ViewController {
    public function doesNotWorkHere() {
        $this->_request->methodOne();
    }
}
