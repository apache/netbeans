<?php
namespace XUser;
require_once 'XUserAbstract.php';

class XUser extends XUserAbstract{
    public function getLastName() {
        return $this->_lastName;
    }
}

