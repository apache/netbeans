<?php

namespace AdminModule {

    class PagePresenter extends SecuredPresenter {
        protected function beforeRender() {
            parent::beforeRender();//TEST
        }
    }

    class SecuredPresenter extends AdminPresenter {
        protected function startup() {
        }
    }

    abstract class AdminPresenter extends \BasePresenter {
        protected function beforeRender() {
            parent::beforeRender();
        }
    }
}

namespace {

    class BasePresenter {
        protected function beforeRender() {
        }
    }

}

?>