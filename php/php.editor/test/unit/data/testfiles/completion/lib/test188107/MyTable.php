<?php

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
class MyTable {

	/**
	 *
	 * @var DatabaseRecord
	 */
	private $db;
	
	function getAllPersons() {
		$this->db->select('Baf')->from('Person');
	}

}
?>