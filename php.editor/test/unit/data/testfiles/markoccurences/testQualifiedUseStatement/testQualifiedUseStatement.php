<?php
namespace pl\dagguh\someproject\rooms;

class Kitchen {
    const DEFAULT_SIZE = 3;
}

namespace pl\dagguh\someproject\gui;

use pl\dagguh\someproject\rooms\Kitchen;
use pl\dagguh\someproject\rooms\Kitchen as Alias;

class RoomViewer {
    public function getDefaultRoom() {
        Kitchen::DEFAULT_SIZE;
    }
}

?>