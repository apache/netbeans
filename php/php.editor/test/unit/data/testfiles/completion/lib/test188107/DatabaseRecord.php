<?php



class DatabaseRecord {

	/**
	 *
	 * @param type $what
	 * @return object
	 */
	function select($what) {
		return $this;
	}
	
	/**
	 *
	 * @param type $where
	 * @return object 
	 */
	function from($where) {
		return $this;
	}
	
	function where($param) {
		return $this;
	}

}
?>