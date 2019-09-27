<?php
namespace pl\dagguh\buildings;

interface Building {

	/**
	 * This full qualified name WILL NOT get shortened.
	 * @param \pl\dagguh\buildings\Room $room
	 */
	function addRoom(\pl\dagguh\buildings\Room $room);
}

interface Room {

}
?>