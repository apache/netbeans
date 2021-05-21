<?php
namespace pl\dagguh\buildings;

use pl\dagguh\people\Person;

interface Building {

	/**
	 * I often end up with fully qualified names (FQNs), because I use type hinting (Room $room) and NetBeans' automatic PHPdoc generation.
	 * NetBeans sees the type hinting and fills up @param with FQN.
	 *
	 * @param \pl\dagguh\buildings\Room $room This will NOT get shortened.
	 * @param \pl\dagguh\people\Person $roomOwner This will get shortened.
	 */
	function assignRoom(Room $room, Person $roomOwner);

}

interface Room {

}

namespace pl\dagguh\people;

interface Person {

}
?>