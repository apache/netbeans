"use strict";   

class Point {
    constructor(x, y) {
        this.x = x;
        this.y = y;
    }

    /**
     * 
     * @param {Point} a 
     * @param {Point} b 
     */
    static distance(a, b) {
        const dx = a.x - b.x;
        const dy = a.y - b.y;

        return Math.sqrt(dx*dx + dy*dy);
    }
}
  
const p1 = new Point(5, 5);
const p2 = new Point(10, 10);

Point.