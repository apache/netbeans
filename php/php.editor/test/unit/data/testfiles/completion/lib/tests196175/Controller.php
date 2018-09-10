<?php

namespace Brite\Controller;

class Controller {
    /**
     * @var \Brite\Base\Request
     */
    protected $_request;

    public function worksHere() {
        $this->_request->methodOne();
    }
}

