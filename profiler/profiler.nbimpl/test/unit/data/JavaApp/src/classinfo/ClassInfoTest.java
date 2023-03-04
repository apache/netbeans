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

package classinfo;

import java.io.Serializable;
import java.util.ArrayList;
import sun.org.mozilla.javascript.Callable;
import sun.org.mozilla.javascript.Context;
import sun.org.mozilla.javascript.Scriptable;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ClassInfoTest implements Serializable, Cloneable {
    private Runnable anonymous = new Runnable() {

        public void run() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
    
    public ClassInfoTest() {
        
    }
    
    private ClassInfoTest(int x) {
        
    }
    
    public static void staticMethod() {
        
    }
    
    public int retMethod() {
        return 10;
    }
    
    private int calc(int x, int y) {
        return x+y;
    }
    
    public static class StaticInner extends ClassInfoTest {
        public void doit() {
            
        }
    }
    
    public class Inner {
        public void doit() {
            
        }
    }
    
    public static class CallableTest implements Callable {
        public Object call(Context cntxt, Scriptable s, Scriptable s1, Object[] os) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
