/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

"use strict";

class Point {
    constructor(x, y) {
        //private x = x;
        //private y = y;
        //public sum = x + y;
        this.x = x;
        this.y = y;
    }
    
    //get X() { return private(this).x; }
    //get Y() { return private(this).y; }
    getX() { return this.x; }
    getY() { return this.y; }
    
    add(a) { this.x += a; this.y += a; }
    
    //public const Point0 = new Point(0, 0);
};

var p = new Point(10, 20);
p.add(5);
p.getX();
p.getY();   // breakpoint


