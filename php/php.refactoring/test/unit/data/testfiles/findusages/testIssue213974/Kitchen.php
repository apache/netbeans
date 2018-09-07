<?php

namespace pl\dagguh\someproject\rooms;

class Kitchen {

    const SIZE = 3;

    public static $aStaticField;

    public static function getDefaultSize() {
        return self::SIZE;
    }

}

?>