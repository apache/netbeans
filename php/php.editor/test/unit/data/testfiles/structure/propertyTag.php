<?php
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/** * @author Petr
 * @since 1.5
 *
 *
 *
 */
interface Shape {
    /**
     * Draws the shape
     *
     *
     * @return string the shape
     */
    function draw();
}

/**
 * Represent one 2d point
 * @property int $x
 * @property int $y
 */
class Point implements Shape {

    public $z;
    /**
     *
     * @return string
     */
    public function draw() {
        return "Point [".$this->x.",".$this->y."]";
    }
}

/**
 * @property Point $point
 * @property int $radius
 */
class Circle implements Shape {
    public function draw() {
        return "Circle:\n"."  Center: ".$this->point->draw()."\n  Radius: ".$this->radius."\n";
    }
}

$point = new Point;
$point->x = 100;
$point->y = 200;

$circle = new Circle;
$circle->radius = 10;
$circle->point = $point;

echo $circle->draw();
?>