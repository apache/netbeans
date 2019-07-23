<?php
namespace Zend\Stdlib2 {

    interface DispatchableInterface2 {
        public function dispatch();
    }

}

namespace Zend\Mvc\Controller2 {

    use Zend\Stdlib2\DispatchableInterface2 as Dispatchable2;

    class AbstractController implements Dispatchable2 {
        public function dispatch() {}
    }
}
?>