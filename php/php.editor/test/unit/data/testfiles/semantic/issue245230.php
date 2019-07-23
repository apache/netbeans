<?php
class Unused_Bug {
	public function __construct() {
		$this->capitalizedwrongfunction();
	}
	private function capitalizedWrongFunction() {} // no unused!
}
?>