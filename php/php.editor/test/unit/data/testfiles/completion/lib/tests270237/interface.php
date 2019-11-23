<?php
class Bar {}
class Baz {}
class Qux {}
interface FooInterface
{
    public function test71_01(?string $string, int $int): ?Bar;
    /**
     * @return string|Baz
     */
    public function test71_02(?string $string, int $int): ?Bar;
    public function test71_03(?string $string, int $int): ?self;
    public function test71_04(?self $self);
    public function test70_01(string $string, int $int): Bar;
    /**
     * @return Baz|Bar
     */
    public function test70_02(string $string, int $int): Bar;
    /**
     * @return Baz|Bar
     */
    public function test70_03(string $string, int $int);
    /**
     * @return Bar
     */
    public function test70_04(string $string, int $int);
    public function test70_05(): self;
    public function test70_06(self $self);
}
