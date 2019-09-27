<?php
namespace test\ns\cls\ctx {
    interface Vehicle {
        function getBrand();
    }
    class Car {
        function getEngine() {
        }
    }
}

class Lorry extends \test\ns\cls\ctx\Car implements /**/cls\ctx\Vehicle {
}
?>