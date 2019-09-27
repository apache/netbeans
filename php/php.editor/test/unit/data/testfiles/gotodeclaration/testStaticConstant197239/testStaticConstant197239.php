<?php
class AA {
    const LETTER22 = 'a';


    public function display() {
        echo static::LETTER22;
        echo self::LETTER22;
        echo AA::LETTER22;
    }
}

?>