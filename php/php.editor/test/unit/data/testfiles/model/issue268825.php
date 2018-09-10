<?php
class AutoPopup {
    public static function test(string $string, callable $callable) {
    }

    public function something() {
    }
}

class Anon {

    public function test() {
        AutoPopup::test('test', function(AutoPopup $test) {
            //
        });
    }

}
