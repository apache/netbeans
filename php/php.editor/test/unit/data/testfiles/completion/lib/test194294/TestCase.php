<?php

class Cls1 {
	
	static public function getInstance() {
		return new Cls1();
	}
	
	public function getName(){
		return new Cls1();
	}
        
        
	
}


class Cls2 {

	public function doSomething() {
		return Cls1::getInstance();
	}

}

?>