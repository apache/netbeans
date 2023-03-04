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

/**
 *
 * @author sdedic
 */
public class MethodTooDeep {
    private Collection col;
    
    public int m() {
        int a = 0;
        OUTER: for (int i = 0; i < 10; i++) {
            int j = 0;
            try {
                do {
                    if (i % 2 == 0) {
                        a *= 2;
                        if (j == i / 2) {
                            if (j % 2 == 0) {
                                a = a + Math.random() > 0.5 ? 1 : 2;
                            }
                            break;
                        } else if (j == i / 3) {
                            for (Object o : col) {
                                int x = 0;
                                while (x < 10) {
                                    x++;
                                    switch (x) {
                                        case 1:
                                    }
                                }
                            }
                            continue OUTER;
                        }
                    }
                    j++;
                } while (j < i);
            } catch (IllegalArgumentException ex) {
                a--;
            } catch (NullPointerException ex) {
                a -= this.m();
            }
        }
        return a;
    }
}
