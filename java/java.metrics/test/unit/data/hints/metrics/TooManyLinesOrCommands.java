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
public class TooManyLinesOrCommands {
    private Collection col;

    public int tooManyLines() {
        @Deprecated 
        int a = 
                0; // 1  
        OUTER: for (
                int i = 0; 
                i < 10; 
                i++) { // 2
            int 
                j = 
                0; // 3
            try { 
                do { // 4
                    if 
                        (j % 2 == 0) { // 5
                        a = 
                                a + 
                                Math.random() 
                                > 0.5 ? 
                                1 : 
                                2; // 6
                    }
                    if 
                        (i 
                        % 2 
                        == 0) { // 7
                        a 
                        *= 
                        2; // 8
                        throw 
                            new RuntimeException(); // 9
                    }
                    if 
                        (j 
                            == 
                            i / 2) { // 10
                        break; // 11
                    } else 
                    if (j 
                            == 
                            i / 3) { // 12
                        continue OUTER; // 13
                    }
                    j++; // 14
                } while 
                    (j < i); 
            } catch (IllegalArgumentException ex) { // 15
                a--; // 16
            } catch (NullPointerException ex) { // 17
                a -= 
                    this.tooManyStatements(); // 18
            }
            for (Object o : col) { // 19
                int x 
                        = 0; // 20
                while 
                    (x < 10) { // 21
                    x++; // 22
                    switch (x) { // 23
                        case 1: 
                            continue; // 24
                    }
                }
            }
        }
        assert a >= 0; // -
        if 
            (a > 0) { // 25
            a += 
                3; // 26
            tooManyStatements(); // 27
            while 
                (a > 1) { // 28
                return 1; // 29
            }
        }
        return 
                a; // 30
    }

    public int tooManyStatements() {
        @Deprecated 
        int a = 0; // 1  
        OUTER: for (int i = 0; i < 10; i++) { // 2
            int j = 0; // 3
            try { 
                do { // 4
                    if (j % 2 == 0) { // 5
                        a = a + Math.random() > 0.5 ? 1 : 2; // 6
                    }
                    if (i % 2 == 0) { // 7
                        a *= 2; // 8
                        throw new RuntimeException(); // 9
                    }
                    if (j == i / 2) { // 10
                        break; // 11
                    } else if (j == i / 3) { // 12
                        continue OUTER; // 13
                    }
                    j++; // 14
                } while (j < i); 
            } catch (IllegalArgumentException ex) { // 15
                a--; // 16
            } catch (NullPointerException ex) { // 17
                a -= this.tooManyStatements(); // 18
            }
            for (Object o : col) { // 19
                int x = 0; // 20
                while (x < 10) { // 21
                    x++; // 22
                    switch (x) { // 23
                        case 1: 
                            continue; // 24
                    }
                }
            }
        }
        assert a >= 0; // -
        if (a > 0) { // 25
            a += 3; // 26
            tooManyStatements(); // 27
            while (a > 1) { // 28
                return 1; // 29
            }
            a++; // 30
        }
        return a; // 31
    }
}
