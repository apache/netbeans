/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 *
 * @author mjayan
 */
public class RecordPattern {  
    public void op(Object o) {
        if(o instanceof Person) {
            System.out.println("Hello");
        }
        if(o instanceof Rect r){
            System.out.println("Hy");
        }
        if(o instanceof Rect(ColoredPoint upperLeft,ColoredPoint lr)){
            System.out.println("Hy");
        }
    }
}
record Person(String name, int a) {}
record Point(int x, int y) {}
record Check(int x, String y) {}
record Rect(ColoredPoint upperLeft,ColoredPoint lr) {}
record ColoredPoint(Point p, Color c) {}
enum Color {
       RED,GREEN,BLUE
    }
