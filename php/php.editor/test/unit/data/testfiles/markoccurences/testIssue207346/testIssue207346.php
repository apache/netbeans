<?php

namespace Nette\Application\UI {

    class Presenter {

        public $invalidLinkMode;

        function __construct() {

        }

    }

}

namespace {
    abstract class BasePresenter extends \Nette\Application\UI\Presenter {

        public function __construct() {
            $this->invalidLinkMode = 10;
            $this->invalidLinkMode;
        }

    }
}
?>