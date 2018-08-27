<?php

namespace Brite\Controller;

class ViewController extends Controller {
    public function alsoWorksHere() {
        $this->_request->methodOne();
    }
}

