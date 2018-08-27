<?php
namespace Package1;
class Types
{
    const FOO = 1;
    const BAR = 2;

    public function getTypes()
    {
        return array(self::FOO, self::BAR);
    }
}
