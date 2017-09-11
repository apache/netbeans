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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;
import junit.framework.Test;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.*;
import org.netbeans.test.java.Common;
import org.openide.filesystems.FileObject;

/** <B>Java Module General API test: InnerClasses</B>
 * <BR><BR><I>What it tests:</I><BR>
 * Creating and handling with ClassElement.
 * Test is focused on checking of correctness of generated code.
 * <BR><BR><I>How it works</I><BR>
 * New class is created using DataObject.createFromTemplate() and also some ClassElements are created.
 * These are customized using setters and filled with fields, methods etc.
 * Then these ClassElements are added using ClassElement.addClass() into ClassElement.
 * These action cause generating of .java code. This code is compared with supposed one.
 * <BR><BR><I>Output</I><BR>
 * Generated Java code.
 * <BR><BR><I>Possible reasons of failure</I><BR>
 * <U>Classes are not inserted properly:</U><BR>
 * If there are some Initialzers in .diff file.
 * <BR><BR><U>Classes have/return bad properies</U><BR>
 * See .diff file to get which ones
 * <BR><BR><U>Bad indentation</U><BR>
 * This is probably not a bug of Java Module. (->Editor Bug)
 * In .diff file could be some whitespaces.
 * <BR><BR><U>Exception occured</U><BR><BR>
 * See .log file for StackTrace
 *
 * @author Jan Becicka <Jan.Becicka@sun.com>
 */


public class InnerClasses extends org.netbeans.test.java.XRunner {
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public InnerClasses() {
        super("");
    }
    
    public InnerClasses(java.lang.String testName) {
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
        
        boolean passed = true;
        
        FileObject fo = (FileObject) o;
        JavaSource js = JavaSource.forFileObject(fo);
        createInnerClass(js);
        
        //        ClassElement innerClass = new ClassElement();
        //
        //        simpleJavaSourceEtalonGenerator(innerClass);
        //
        //        innerClass.setModifiers(Modifier.PUBLIC | Modifier.SYNCHRONIZED);
        //        innerClass.setSuperclass(Identifier.create("Object"));
        //        innerClass.setName(Identifier.create("InnerClass","InnerClass"));
        //        clazz.addClass(innerClass);
        //
        //        innerClass = clazz.getClass(Identifier.create("InnerClass"));
        //
        //        if (!(innerClass.getDeclaringClass().getName().getFullName().equals(packageName + "." + name))){
        //            passed = false;
        //            log("getDeclaringClass failed");
        //        }
        //
        //        DataObject DO = (DataObject)  clazz.getSource().getCookie(DataObject.class);
        //        //DO.rename("RenamedJavaTestSource");
        //        clazz.setName(Identifier.create("RenamedJavaTestSource"));
        //
        //        if (!(innerClass.getDeclaringClass().getName().getFullName().equals(packageName + ".RenamedJavaTestSource"))){
        //            passed = false;
        //            log("getDeclaringClass failed");
        //        }
        //
        //        if (!(innerClass.getMethods()[0].getDeclaringClass().getName().getFullName().equals(packageName + ".RenamedJavaTestSource.InnerClass"))){
        //            passed = false;
        //            log("getDeclaringClass failed: " + innerClass.getMethods()[0].getDeclaringClass().getName().getFullName());
        //        }
        //
        //        clazz.setName(Identifier.create("Foo"));
        //        if (!(innerClass.getMethods()[0].getDeclaringClass().getName().getFullName().equals(packageName + ".Foo.InnerClass"))){
        //            passed = false;
        //            log("getDeclaringClass failed:" + innerClass.getMethods()[0].getDeclaringClass().getName().getFullName());
        //        }
        //
        //        clazz.setName(Identifier.create("RenamedJavaTestSource"));
        
        return passed;
    }
    
    /**
     */
    protected void setUp() {
        super.setUp();
        name = "JavaTestSourceInnerClasses";
        packageName = "org.netbeans.test.java.testsources";
    }
    
    
    public void createInnerClass(JavaSource js) throws IOException {
        CancellableTask task = new CancellableTask<WorkingCopy>() {
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = null;
                for (Tree typeDecl : cut.getTypeDecls()) {
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        clazz = (ClassTree) typeDecl;
                    }
                } // end for
                EnumSet<Modifier> modifiers = EnumSet.of(Modifier.STATIC,Modifier.PUBLIC);
                Tree extendsTree = make.QualIdent(workingCopy.getElements().getTypeElement("java.util.List"));
                List<ExpressionTree> implementsList = Collections.<ExpressionTree> singletonList(make.Identifier("Serializable"));
                Map<String,String> params = new HashMap<String, String>();
                params.put("param1", "String");
                MethodTree mt = Common.createMethod(make, "method", params);
                VariableTree vt = Common.createField(make, "variable", EnumSet.of(Modifier.PROTECTED), "double");
                List<? extends Tree> members = new ArrayList<Tree>();                
                ClassTree innerClass = make.Class(
                        make.Modifiers(modifiers),
                        "MyInner",
                        Collections.EMPTY_LIST,
                        extendsTree,
                        implementsList,
                        Collections.EMPTY_LIST);                
                innerClass = make.addClassMember(innerClass, mt);
                innerClass = make.addClassMember(innerClass, vt);
                innerClass = make.addClassMember(innerClass, make.Block(Collections.EMPTY_LIST, false));                
                ClassTree modifiedClazz = make.addClassMember(clazz, innerClass);
                workingCopy.rewrite(clazz, modifiedClazz);
            }
        };
        js.runModificationTask(task).commit();
    }
    
    /** Generates SimpleJavaTestSourceEtalon
     * @param clazz where to generate
     * @throws Exception
     */
    //    public static void simpleJavaSourceEtalonGenerator(org.openide.src.ClassElement clazz) throws Exception {
    //
    //        clazz.removeConstructors(clazz.getConstructors());
    //        clazz.addMethod(Common.createMethod("method1", Modifier.PUBLIC | Modifier.STATIC, Type.INT, Common.PARS1));
    //        clazz.addField(Common.createField("field1",Modifier.PUBLIC | Modifier.STATIC, Type.FLOAT));
    //        clazz.addInitializer(Common.createInitializer());
    //        clazz.addMethod(Common.createMethod("method1", Modifier.PUBLIC | Modifier.STATIC, Type.INT, Common.PARS2));
    //        clazz.addField(Common.createField("field2",Modifier.PUBLIC | Modifier.STATIC, Type.INT));
    //        clazz.addConstructor(Common.createConstructor(clazz.getName()));
    //        clazz.addConstructor(Common.createConstructor(clazz.getName(),Common.PARS1));
    //
    //        clazz.addMethod(Common.createMethod("method1", Modifier.PUBLIC | Modifier.STATIC, Type.INT, Common.PARS3));
    //        clazz.addInitializer(Common.createInitializer());
    //        clazz.addMethod(Common.createMethod("method2", Modifier.PUBLIC | Modifier.STATIC, Type.INT, Common.PARS1));
    //        clazz.addField(Common.createField("field3",Modifier.PUBLIC | Modifier.STATIC, Type.SHORT));
    //        clazz.addConstructor(Common.createConstructor(clazz.getName(),Common.PARS2));
    //        clazz.addMethod(Common.createMethod("method2", Modifier.PUBLIC | Modifier.STATIC, Type.SHORT, Common.PARS2));
    //        clazz.addInitializer(Common.createInitializer());
    //        clazz.addMethod(Common.createMethod("method2", Modifier.PUBLIC | Modifier.STATIC, Type.LONG, Common.PARS3));
    //
    //    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(InnerClasses.class).enableModules(".*").clusters(".*"));
    }
    
}
