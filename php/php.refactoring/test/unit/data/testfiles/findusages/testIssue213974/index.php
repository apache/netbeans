<?php

namespace pl\dagguh\someproject\gui;

use pl\dagguh\someproject\rooms\Kitchen;

class RoomViewer {

    public function getRoomSize() {
		echo Kitchen::$aStaticField;
		echo Kitchen::getDefaultSize();
        echo Kitchen::SIZE;
    }

}

?>