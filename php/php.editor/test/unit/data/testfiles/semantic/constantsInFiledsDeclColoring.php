<?php

namespace Sux;

class DateTimePicker extends BaseControl  {

    const CSS_CLASS = 'datepicker';

    private static $foo = array(self::CSS_CLASS => "");

    public function __construct($label = null, $type = self::TYPE_DATETIME_LOCAL) {
        $this->controlPrototype->class(self::CSS_CLASS);
    }
}
?>