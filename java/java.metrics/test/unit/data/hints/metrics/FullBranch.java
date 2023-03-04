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
package test;

import java.util.Collection;
import java.util.List;

/**
 *
 * @author sdedic
 */
public class FullBranch<A extends java.util.Iterator> {
    private Collection col;
    private Collection<String> stringCol;
    private int d[][] = new int[5][];
    
    @Deprecated
    public void n() {
        if (col instanceof List) {
            d[0][0] = (Integer)((List)col).get(1) + d[0][1];
        } else {
            Collection<?> o = col;
            d[2][0] = col.size();
        }
        d[1][0] = other.m(2);
    }
    
    public int m(int input) {
        @Deprecated
        int a = 0;
        OUTER: for (int i = 0; i < 10; i++) {
            int j = 0;
            try {
                do {
                    if (j % 2 == 0) {
                        a = a + Math.random() > 0.5 ? 1 : 2;
                    }
                    if (i % 2 == 0) {
                        a *= 2;
                        throw new RuntimeException();
                    }
                    if (j == i / 2) {
                        break;
                    } else if (j == i / 3) {
                        continue OUTER;
                    }
                    j++;
                } while (j < i);
            } catch (IllegalArgumentException ex) {
                a--; 
            } catch (NullPointerException ex) {
                a -= this.m(a);
            }
            for (Object o : col) {
                int x = 0;
                while (x < 10) {
                    x++;
                    switch (x) {
                        case 1:
                    }
                }
            }
        }
        assert a >= 0;
        return a;
    }
    
    private FullBranch other;
}
