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

package org.netbeans.test.java.hints.introduce.RefFinderTest;

/**
 *
 * @author sdedic
 */
public class Test extends Base {
    private int fieldOne;
    private String fieldTwo;
    
    public int m1() {
        return 0;
    }
    
    public int m2() {
       return 0;
    }
    
    public int b2() {
        return 0;
    }
    
    public static void s1() {
    }
    
    public void staticMethod() {
        s1();
    }
    
    public void staticThisMethod() {
        this.s1();
    }
    
    public void staticInheritedMethod() {
        is1();
        
    }
    
    public void staticThisInheritedMethod() {
        this.is1();
    }
    
    public void simpleMethod() {
        m1();
    }
    
    public void simpleInheritedMethod() {
        b1();
    }
    
    public void superMethod() {
        super.b2();
    }
    
    public void thisMethod() {
        this.m1();
    }
    
    public void qualifiedThisMethod() {
        Test.this.m1();
    }
    
    public void qualifiedSuperMethod() {
        Test.super.b2();
    }
    
    public void qualifiedThisInheritedMethod() {
        Test.this.b1();
    }
    
    public class I {
        public void m3() {
            
        }
        
        public void outerThisMethod() {
            Test.this.simpleMethod();
        }

        public void outerSuperMethod() {
            Test.super.b1();
        }

        public void outerThisInheritedMethod() {
            Test.this.b1();
        }
        
        public void outerInheritedMethod() {
            b1();
        }
        
        public void outerMethod() {
            m1();
        }
    }
    
    public void enclosingMethod() {
        new Runnable() {
            public void run() {
                m1();
            }
        };
    }
    
    public void enclosingThisMethod() {
        new Runnable() {
            public void run() {
                Test.this.m1();
            }
        };
    }

    public void enclosingInheritedMethod() {
        new Runnable() {
            public void run() {
                b1();
            }
        };
    }

    public void enclosingSuperMethod() {
        new Runnable() {
            public void run() {
                Test.super.b2();
            }
        };
    }
    
    public void localClassReference() {
        class L { public int foo() { return 0; }}
        L var = new L();
        var.foo();
    }
}
