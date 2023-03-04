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

package org.netbeans.lib.profiler.charts.swing;

/**
 *
 * @author Jiri Sedlacek
 */
public final class LongRect {
    
    public long x;
    public long y;
    public long width;
    public long height;
    
    
    public LongRect() {
        this(0, 0, 0, 0);
    }
    
    public LongRect(LongRect longRect) {
        this(longRect.x, longRect.y, longRect.width, longRect.height);
    }
    
    public LongRect(long x, long y, long width, long height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    
    public String toString() {
        return "LongRect: [" + x + ", " + y + ", " + width + ", " + height + "]"; // NOI18N
    }
    
    
    // Sets lr to [0, 0, 0, 0]
    public static void clear(LongRect lr) {
        lr.x = 0;
        lr.y = 0;
        lr.width = 0;
        lr.height = 0;
    }

    // Returns true if lr.x == 0 and lr.y == 0 and lr.width == 0 and lr.height == 0
    public static boolean isClear(LongRect lr) {
        if (lr.x != 0) return false;
        if (lr.y != 0) return false;
        if (lr.width != 0) return false;
        if (lr.height != 0) return false;
        return true;
    }
    
    // Returns true if lr.width <= 0 or lr.height <= 0
    public static boolean isEmpty(LongRect lr) {
        if (lr.width <= 0) return true;
        if (lr.height <= 0) return true;
        return false;
    }

    public static void set(LongRect lr1, LongRect lr2) {
        set(lr1, lr2.x, lr2.y, lr2.width, lr2.height);
    }

    public static void set(LongRect lr, long x, long y, long width, long height) {
        lr.x = x;
        lr.y = y;
        lr.width = width;
        lr.height = height;
    }
    
    // Does nothing if isEmpty(lr2) == true,
    // sets lr1 to be lr2 if isEmpty(lr1) == true,
    // sets lr1 to be lr1 + lr2 otherwise
    public static void add(LongRect lr1, LongRect lr2) {
        if (isEmpty(lr2)) return;
        if (isEmpty(lr1)) {
            lr1.x = lr2.x;
            lr1.y = lr2.y;
            lr1.width = lr2.width;
            lr1.height = lr2.height;
        } else {
            long xx = lr1.x;
            long yy = lr1.y;
            lr1.x = Math.min(lr1.x, lr2.x);
            lr1.y = Math.min(lr1.y, lr2.y);
            lr1.width = Math.max(xx + lr1.width, lr2.x + lr2.width) - lr1.x;
            lr1.height = Math.max(yy + lr1.height, lr2.y + lr2.height) - lr1.y;
        }
    }
    
//    // If isEmpty(lr) == true sets the lr to [x, y, 1, 1],
//    // otherwise extends the lr to contain [x, y] if necessary
    public static void add(LongRect lr, long x, long y) {
//        if (isEmpty(lr)) {
//            lr.x = x;
//            lr.y = y;
//            lr.width = 1;
//            lr.height = 1;
//        } else {
            long xx = lr.x;
            long yy = lr.y;
            lr.x = Math.min(lr.x, x);
            lr.y = Math.min(lr.y, y);
            lr.width = Math.max(xx + lr.width, x) - lr.x;
            lr.height = Math.max(yy + lr.height, y) - lr.y;
//        }
    }
    
    // Returns true if lr1 fully contains lr2
    public static boolean contains(LongRect lr1, LongRect lr2) {
        if (isEmpty(lr1) || isEmpty(lr2)) return false;
        if (lr1.x > lr2.x) return false;
        if (lr1.y > lr2.y) return false;
        if (lr1.x + lr1.width < lr2.x + lr2.width) return false;
        if (lr1.y + lr1.height < lr2.y + lr2.height) return false;
        return true;
    }
    
    // Returns true if lr1 describes the same bounds as lr2
    public static boolean equals(LongRect lr1, LongRect lr2) {
        if (lr1.x != lr2.x) return false;
        if (lr1.y != lr2.y) return false;
        if (lr1.width != lr2.width) return false;
        if (lr1.height != lr2.height) return false;
        return true;
    }
    
    // Returns true if at least one side of lr1 touches a side of lr2
    // Requires that contains(lr2, lr1) == true
    public static boolean touches(LongRect lr1, LongRect lr2) {
        if (isEmpty(lr1) || isEmpty(lr2)) return false;
        if (lr1.x == lr2.x) return true;
        if (lr1.y == lr2.y) return true;
        if (lr1.x + lr1.width == lr2.x + lr2.width) return true;
        if (lr1.y + lr1.height == lr2.y + lr2.height) return true;
        return false;
    }
    
    public static void addBorder(LongRect lr, long border) {
//        if (isEmpty(lr)) return;
        lr.x -= border;
        lr.y -= border;
        lr.width += border * 2;
        lr.height += border * 2;
    }
    
}
