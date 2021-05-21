<?php
namespace Package2\User;

use Package1\Types;

class User
{
    private $type;

    public function initType()
    {
        $this->type = Types::BAR;
    }
}

namespace Sux;

class DateTimePicker extends BaseControl  {

    const CSS_CLASS = 'datepicker';

    private static $foo = array(self::CSS_CLASS => "");

    public function __construct($label = null) {
        $this->controlPrototype->class(self::CSS_CLASS);
    }
}

?>