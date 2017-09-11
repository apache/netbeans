/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.apisupport.project.universe;

import java.util.Comparator;

/**
 * Represents one test dependency. I.e. <em>&lt;test-dependency&gt;</em>
 * element in module's <em>project.xml</em>.
 * 
 * @author pzajac
 */
public class TestModuleDependency implements Comparable {
    
    public static final String UNIT = "unit"; // NOI18N
    public static final String QA_FUNCTIONAL = "qa-functional"; // NOI18N 
    
    private final ModuleEntry module;
    // depends also on tests of modules
    private boolean test;
    // depends on execution classpath of the modules
    private boolean recursive;
    // compilation dependency 
    private boolean compile;
    
    public static final Comparator<TestModuleDependency> CNB_COMPARATOR = new Comparator<TestModuleDependency>() {
        public int compare(TestModuleDependency tmd1, TestModuleDependency tmd2) {
            return (tmd1).module.getCodeNameBase().compareTo((tmd2).module.getCodeNameBase());
        }
    };
    
    /**
     * Creates a new instance of TestModuleDependency
     */
    public TestModuleDependency(ModuleEntry me,boolean test,boolean recursive,boolean compile) {
        this.module = me;
        this.test = test;
        this.recursive = recursive;
        this.compile = compile;
    }

    public ModuleEntry getModule() {
        return module;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isCompile() {
        return compile;
    }

    public void setCompile(boolean compile) {
        this.compile = compile;
    }
    
    // td1 equals td2 iff cnb and all three boolean fileds of td are the same
    public boolean equals(Object o){
        if(o instanceof TestModuleDependency) {
            TestModuleDependency tmd = (TestModuleDependency) o;
            return tmd.isCompile() == this.isCompile()
                    && tmd.isRecursive() == this.isRecursive()
                    && tmd.isTest() == this.isTest()
                    && tmd.getModule().getCodeNameBase().equals(this.getModule().getCodeNameBase());
        } else {
            return false;
}
    }
    
    //compare only on cnb. ATTENTION, compareTo is not consistent with equals method!
    //i.e. two instances of TestModuleDependency can be nonequal, and tmd1.compareTo(tmd2)
    // can return 0
    public int compareTo(Object o) {
        TestModuleDependency tmd = (TestModuleDependency) o;
        return this.module.getCodeNameBase().compareTo(tmd.module.getCodeNameBase());
    }
    
    //hash from CNB only
    public int hashCode(){
        int hash = module.getCodeNameBase().hashCode();
//        if(test)  hash*=5;
//        if(recursive)  hash*=7;
//        if(compile) hash*=11;
        return hash;
    }

    public @Override String toString() {
        return module.getCodeNameBase() + (test ? ";test" : "") + (recursive ? ";recursive" : "") + (compile ? ";compile" : ""); // NOI18N
    }

}
