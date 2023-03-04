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

package org.netbeans.lib.editor.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import junit.framework.TestCase;

/**
 * Random test of GapList correctness.
 *
 * @author mmetelka
 */
public class GapListRandomTest extends TestCase {

    private static final boolean debug = false;

    private static final int OP_COUNT_1 = 10000;
    private static final int ADD_RATIO_1 = 100;
    private static final int ADD_ALL_RATIO_1 = 10;
    private static final int ADD_ALL_MAX_COUNT_1 = 10;
    private static final int REMOVE_RATIO_1 = 100;
    private static final int REMOVE_RANGE_RATIO_1 = 10;
    private static final int CLEAR_RATIO_1 = 5;
    private static final int SET_RATIO_1 = 50;
    private static final int COPY_RATIO_1 = 50;
    
    private static final int OP_COUNT_2 = 10000;
    private static final int ADD_RATIO_2 = 50;
    private static final int ADD_ALL_RATIO_2 = 20;
    private static final int ADD_ALL_MAX_COUNT_2 = 5;
    private static final int REMOVE_RATIO_2 = 100;
    private static final int REMOVE_RANGE_RATIO_2 = 10;
    private static final int CLEAR_RATIO_2 = 3;
    private static final int SET_RATIO_2 = 50;
    private static final int COPY_RATIO_2 = 50;
    
    private ArrayList<Object> al;
    
    private GapList<Object> gl;
    
    public GapListRandomTest(String testName) {
        super(testName);
    }
    
    public void test() {
        testFresh(0);
    }

    public void testFresh(long seed) {
        Random random = new Random();
        if (seed != 0) {
            System.err.println("TESTING with SEED=" + seed);
            random.setSeed(seed);
        }
        
        gl = new GapList<Object>();
        al = new ArrayList<Object>();
        
        
        testRound(random, OP_COUNT_1, ADD_RATIO_1, ADD_ALL_RATIO_1, ADD_ALL_MAX_COUNT_1,
            REMOVE_RATIO_1, REMOVE_RANGE_RATIO_1, CLEAR_RATIO_1, SET_RATIO_1, COPY_RATIO_1);
        testRound(random, OP_COUNT_2, ADD_RATIO_2, ADD_ALL_RATIO_2, ADD_ALL_MAX_COUNT_2,
            REMOVE_RATIO_2, REMOVE_RANGE_RATIO_2, CLEAR_RATIO_2, SET_RATIO_2, COPY_RATIO_2);
    }
    
    private void testRound(Random random, int opCount,
    int addRatio, int addAllRatio, int addAllMaxCount,
    int removeRatio, int removeRangeRatio, int clearRatio, int setRatio, int copyRatio) {
        
        int ratioSum = addRatio + addAllRatio + removeRatio + removeRangeRatio
            + clearRatio + setRatio;
        
        for (int op = 0; op < opCount; op++) {
            double r = random.nextDouble() * ratioSum;

            if ((r -= addRatio) < 0) {
                Object o = new Object();
                int index = (int)(al.size() * random.nextDouble());
                al.add(index, o);
                if (debug) {
                    debugOp(op, "add() at index=" + index); // NOI18N
                }
                gl.add(index, o);

            } else if ((r -= addAllRatio) < 0) {
                int count = (int)(random.nextDouble() * addAllMaxCount);
                int index = (int)(random.nextDouble() * (al.size() + 1));
                int off = (int)(random.nextDouble() * count);
                int len = (int)(random.nextDouble() * (count + 1 - off));
                ArrayList<Object> l = new ArrayList<Object>();
                for (int i = count; i > 0; i--) {
                    l.add(new Object());
                }
                int methodType = (int)(random.nextDouble() * 5);
                switch (methodType) {
                    case 0: // addAll(Collection<? extends E> c)
                        al.addAll(l);
                        if (debug) {
                            debugOp(op, "addAll(index, collection)"); // NOI18N
                        }
                        gl.addAll(l);
                        break;
                    case 1: // addAll(Collection<? extends E> c, int off, int len)
                        al.addAll(l.subList(off, off + len));
                        if (debug) {
                            debugOp(op, "addAll(collection, off, len)"); // NOI18N
                        }
                        gl.addAll(l, off, len);
                        break;
                    case 2: // addAll(int index, Collection<? extends E> c)
                        al.addAll(index, l);
                        if (debug) {
                            debugOp(op, "addAll(index, collection)"); // NOI18N
                        }
                        gl.addAll(index, l);
                        break;
                    case 3: // addAll(int index, Collection<? extends E> c, int off, int len)
                        al.addAll(index, l.subList(off, off + len));
                        if (debug) {
                            debugOp(op, "addAll(index, collection, off, len)"); // NOI18N
                        }
                        gl.addAll(index, l, off, len);
                        break;
                    case 4: // addArray(Object[] elements)
                        al.addAll(l);
                        if (debug) {
                            debugOp(op, "addArray(array)"); // NOI18N
                        }
                        gl.addArray(l.toArray());
                        break;
                        
                    default:
                        throw new AssertionError();
                }


            } else if ((r -= removeRatio) < 0) {
                if (al.size() > 0) { // is anything to remove
                    int index = (int)(al.size() * random.nextDouble());
                    al.remove(index);
                    if (debug) {
                        debugOp(op, "remove() at index=" + index); // NOI18N
                    }
                    gl.remove(index);
                }

            } else if ((r -= removeRangeRatio) < 0) {
                if (al.size() > 0) { // is anything to remove
                    int index = (int)(al.size() * random.nextDouble());
                    int length = (int)((al.size() - index + 1) * random.nextDouble());
                    for (int count = length; count > 0; count--) {
                        al.remove(index);
                    }
                    if (debug) {
                        debugOp(op, "remove() at index=" + index + ", length=" + length); // NOI18N
                    }
                    gl.remove(index, length);
                }
                
            } else if ((r -= clearRatio) < 0) {
                al.clear();
                if (debug) {
                    debugOp(op, "clear()"); // NOI18N
                }
                gl.clear();
                
            } else if ((r -= setRatio) < 0) {
                if (al.size() > 0) { // is anything to remove
                    int index = (int)(al.size() * random.nextDouble());
                    Object o = new Object();
                    al.set(index, o);
                    if (debug) {
                        debugOp(op, "set() at index=" + index); // NOI18N
                    }
                    gl.set(index, o);
                }
            } else if ((r -= copyRatio) < 0) {
                int methodType = (int)(random.nextDouble() * 3);
                int alSize = al.size();
                int off = (int)(random.nextDouble() * alSize);
                int len = (int)(random.nextDouble() * (alSize + 1 - off));
                switch (methodType) {
                    case 0:
                        GapList copyList = gl.copy();
                        assertEquals("Lists differ", al, copyList);
                        break;
                    case 1:
                        ArrayList<Object> targetList = new ArrayList<Object>();
                        gl.copyElements(off, len, targetList);
                        assertEquals("Lists differ", al.subList(off, off + len), targetList);
                        break;
                    case 2:
                        Object[] targetArray = new Object[len];
                        gl.copyElements(off, len, targetArray, 0);
                        assertEquals("Lists differ", al.subList(off, off + len), Arrays.asList(targetArray));
                        break;
                    default:
                        throw new AssertionError();
                }
            }

            checkConsistency();
        }
        
    }
        
    private void debugOp(int op, String s) {
        System.err.println("op: " + op + ", " + s + ", " + gl.dumpInternals());
    }
    
    private void checkConsistency() {
        gl.consistencyCheck();

        assertEquals(gl.size(), al.size());
        
        int size = al.size();
        for (int i = 0; i < size; i++) {
            assertTrue("Contents differ at index " + i + ", gl: " + gl.get(i) // NOI18N
                + ", al:" + al.get(i), // NOI18N
                (gl.get(i) == al.get(i)));
        }
    }
    
    
}
