/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
