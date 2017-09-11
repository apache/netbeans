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

/*
 * FieldElem.java
 *
 * Created on June 26, 2000, 9:29 AM
 */

package org.netbeans.test.java.generating;

import org.netbeans.test.java.Common;
import java.util.EnumSet;
import javax.lang.model.element.Modifier;
import junit.framework.Test;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.junit.*;
import org.openide.filesystems.FileObject;

/** <B>Java Module General API Test: MethodElement</B>
 * <BR><BR><I>What it tests:</I><BR>
 * Creating and handling with MethodElement.
 * Test is focused on checking of correctness of generated code.
 * <BR><BR><I>How it works:</I><BR>
 * New class is created using DataObject.createFromTemplate() and also some MethodElements are created.
 * These are customized using setters and then added using ClassElement.addMethod() into ClassElement.
 * These actions cause generating of .java code. This code is compared with supposed one.
 * <BR><BR><I>Output:</I><BR>
 * Generated Java code.
 * <BR><BR><I>Possible reasons of failure:</I><BR>
 * <U>Methods are not inserted properly</U><BR>
 * If there are some Interfaces in .diff file.
 * <BR><BR><U>Methods have/return bad properies</U><BR>
 * See .diff file to get which ones
 * <BR><BR><U>Bad indentation</U><BR>
 * This is probably not a bug of Java Module. (Editor Bug)
 * In .diff file could be some whitespaces.
 * <BR><BR><I>Exception occured:</I><BR>
 * See .log file for StackTrace
 *
 * @author Jan Becicka <Jan.Becicka@sun.com>
 */


public class MethodElem extends org.netbeans.test.java.XRunner {
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public MethodElem() {
        super("");
    }
    
    public MethodElem(java.lang.String testName) {
        super(testName);
    }
    
    /** "body" of this TestCase
     * @param o SourceElement - target for generating
     * @param log log is used for logging StackTraces
     * @throws Exception
     * @return true if test passed
     * false if failed
     */
    public boolean go(Object o, java.io.PrintWriter log) throws Exception {
        
        //        org.openide.src.ClassElement clazz = ((org.openide.src.SourceElement) o).getClasses()[0];
        boolean passed = true;
        FileObject fo = (FileObject) o;
        JavaSource js = JavaSource.forFileObject(fo);
        
        Common.addMethod(js, "method1",Common.PARS1,"void", EnumSet.of(Modifier.PUBLIC,Modifier.STATIC));        
        Common.addMethod(js, "method1",Common.PARS2,"int", EnumSet.of(Modifier.PRIVATE,Modifier.SYNCHRONIZED));        
        Common.addMethod(js, "method1",Common.PARS3,"float", EnumSet.of(Modifier.PRIVATE,Modifier.FINAL));        
        Common.addMethod(js, "method2",Common.PARS1,"double", EnumSet.of(Modifier.PUBLIC,Modifier.STATIC));
        Common.addMethod(js, "method2",Common.PARS2,"boolean", EnumSet.of(Modifier.PUBLIC,Modifier.STATIC));        
        Common.addMethod(js, "method2",Common.PARS3,"void", EnumSet.of(Modifier.PUBLIC,Modifier.STATIC));
        
        return passed;
    }
    
    /**
     */
    protected void setUp() {
        super.setUp();
        name = "JavaTestSourceMethodElem";
        packageName = "org.netbeans.test.java.testsources";
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(MethodElem.class).enableModules(".*").clusters(".*"));
    }
    
}
