<?php

namespace UserManager;

class EditForm extends \Zend_Form {
    
}

namespace StudentManager;

class EditForm extends \Zend_Fr {
    
}

namespace {

    use UserManager\EditForm;

    class IndexController extends BaseController {

        public function indexAction() {
            $form = new EditForm();
        }
    }

    use UserManager\EditForm as EF;

    class IndexController2 extends BaseController {

        public function indexAction() {
            $form = new EF();
        }

    }

    use StudentManager\EditForm;

    class StudenDeal extends BaseController {

        public function create() {
            $fr = new EditForm();
        }

    }

}