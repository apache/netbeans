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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.swing.text.Document;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.source.transform.Transformer;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.TestUtil;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Lahoda
 */
public class MemberAdditionTest extends NbTestCase {
    
    /** Creates a new instance of MemberAdditionTest */
    public MemberAdditionTest(String name) {
        super(name);
    }

    public void testSynteticDefaultConstructor() throws Exception {
        performTest("SynteticDefaultConstructor");
        
        source.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(Phase.RESOLVED);
                ClassTree topLevel = findTopLevelClass(copy);                
                SourceUtilsTestUtil2.run(copy, new AddSimpleField(), topLevel);
            }
        }).commit();

        JavaSourceAccessor.getINSTANCE().revalidate(source);
        
        CompilationInfo check = SourceUtilsTestUtil.getCompilationInfo(source, Phase.RESOLVED);
        CompilationUnitTree cu = check.getCompilationUnit();

        assertEquals(check.getDiagnostics().toString(), 0, check.getDiagnostics().size());

        ClassTree newTopLevel = findTopLevelClass(check);
        Element clazz = check.getTrees().getElement(TreePath.getPath(cu, newTopLevel));
        Element pack = clazz.getEnclosingElement();

        assertEquals(ElementKind.PACKAGE, pack.getKind());
        assertEquals("test", ((PackageElement) pack).getQualifiedName().toString());
        assertEquals(clazz.getEnclosedElements().toString(), 2 + 1/*syntetic default constructor*/, clazz.getEnclosedElements().size());
    }

    public void testEmptyClass() throws Exception {
        performTest("EmptyClass");

        source.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(Phase.RESOLVED);
                ClassTree topLevel = findTopLevelClass(copy);
                SourceUtilsTestUtil2.run(copy, new AddSimpleField(), topLevel);
            }
        }).commit();

        JavaSourceAccessor.getINSTANCE().revalidate(source);

        CompilationInfo check = SourceUtilsTestUtil.getCompilationInfo(source, Phase.RESOLVED);
        CompilationUnitTree cu = check.getCompilationUnit();

        assertEquals(check.getDiagnostics().toString(), 0, check.getDiagnostics().size());
        
        ClassTree newTopLevel = findTopLevelClass(check);
        Element clazz = check.getTrees().getElement(TreePath.getPath(cu, newTopLevel));
        Element pack = clazz.getEnclosingElement();
        
        assertEquals(ElementKind.PACKAGE, pack.getKind());
        assertEquals("test", ((PackageElement) pack).getQualifiedName().toString());
        assertEquals(clazz.getEnclosedElements().toString(), 1 + 1/*syntetic default constructor*/, clazz.getEnclosedElements().size());
    }

    public void testClassImplementingList() throws Exception {
        performTest("ClassImplementingList");

        source.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(Phase.RESOLVED);
                ClassTree topLevel = findTopLevelClass(copy);
                SourceUtilsTestUtil2.run(copy, new AddSimpleField(), topLevel);
            }
        }).commit();

        JavaSourceAccessor.getINSTANCE().revalidate(source);
        
        CompilationInfo check = SourceUtilsTestUtil.getCompilationInfo(source, Phase.RESOLVED);
        CompilationUnitTree cu = check.getCompilationUnit();

        assertEquals(check.getDiagnostics().toString(), 1, check.getDiagnostics().size());
        assertEquals("compiler.err.does.not.override.abstract", check.getDiagnostics().get(0).getCode());
        
        ClassTree newTopLevel = findTopLevelClass(check);
        Element clazz = check.getTrees().getElement(TreePath.getPath(cu, newTopLevel));
        Element pack = clazz.getEnclosingElement();
        
        assertEquals(ElementKind.PACKAGE, pack.getKind());
        assertEquals("test", ((PackageElement) pack).getQualifiedName().toString());
        assertEquals(clazz.getEnclosedElements().toString(), 1 + 1/*syntetic default constructor*/, clazz.getEnclosedElements().size());
    }

    public void testClassWithInnerClass() throws Exception {
        performTest("ClassWithInnerClass");

        source.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws IOException {
                copy.toPhase(Phase.RESOLVED);
                ClassTree topLevel = findTopLevelClass(copy);
                
                FindVariableDeclaration d = new FindVariableDeclaration();
                
                CompilationUnitTree cu = copy.getCompilationUnit();
                d.scan(cu, true);
                
                VariableTree var = d.var;
                
                SourceUtilsTestUtil2.run(copy, new CreateConstructor(Collections.singletonList((VariableElement) copy.getTrees().getElement(TreePath.getPath(cu, var))), (TypeElement) copy.getTrees().getElement(TreePath.getPath(cu, topLevel)), var), topLevel);
            }
        }).commit();

        JavaSourceAccessor.getINSTANCE().revalidate(source);

        CompilationInfo check = SourceUtilsTestUtil.getCompilationInfo(source, Phase.RESOLVED);
        CompilationUnitTree cu = check.getCompilationUnit();

        assertEquals(check.getDiagnostics().toString(), 0, check.getDiagnostics().size());
        
        ClassTree newTopLevel = findTopLevelClass(check);
        Element clazz = check.getTrees().getElement(TreePath.getPath(cu, newTopLevel));
        Element pack = clazz.getEnclosingElement();

        assertEquals(ElementKind.PACKAGE, pack.getKind());
        assertEquals("test", ((PackageElement) pack).getQualifiedName().toString());
        assertEquals(clazz.getEnclosedElements().toString(), 3, clazz.getEnclosedElements().size());
    }

    private ClassTree findTopLevelClass(CompilationInfo info) {
        TopLevelClassInfo i = new TopLevelClassInfo();

        i.scan(info.getCompilationUnit(), null);

        return i.topLevel;
    }

    private static class TopLevelClassInfo extends TreeScanner<Void, Boolean> {

        private ClassTree topLevel;

        public Void visitClass(ClassTree node, Boolean p) {
            topLevel = node;
            return null;
        }

    }

    private static class FindVariableDeclaration extends TreeScanner<Void, Boolean> {

        private VariableTree var;

        public Void visitVariable(VariableTree node, Boolean p) {
            var = node;
            return null;
        }

    }

    private static class AddSimpleField extends Transformer<Void, Boolean> {

        public Void visitClass(ClassTree node, Boolean p) {
            TypeElement te = (TypeElement)model.getElement(node);
            if (te != null) {
                List<Tree> members = new ArrayList<Tree>();
                for(Tree m : node.getMembers())
                    members.add(m);
                members.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "test", make.PrimitiveType(TypeKind.INT), null));
                ClassTree decl = make.Class(node.getModifiers(), node.getSimpleName(), node.getTypeParameters(), node.getExtendsClause(), (List<ExpressionTree>)node.getImplementsClause(), members);
                model.setElement(decl, te);
                model.setType(decl, model.getType(node));
                model.setPos(decl, model.getPos(node));
                copy.rewrite(node, decl);
            }
            
            return null;
        }

    }

    public static class CreateConstructor extends Transformer<Void, Object> {
        
        private List<VariableElement> fields;
        private TypeElement parent;
        private Tree putBefore;
        
        public CreateConstructor(List<VariableElement> fields, TypeElement parent, Tree putBefore) {
            this.fields = fields;
            this.parent = parent;
            this.putBefore = putBefore;
        }
        
        public Void visitClass(ClassTree node, Object p) {
            TypeElement te = (TypeElement)model.getElement(node);
            if (te != null) {
                List<Tree> members = new ArrayList<Tree>(node.getMembers());
                int pos = putBefore != null ? members.indexOf(putBefore) + 1: members.size();
                
                Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
                
                //create body:
                List<StatementTree> statements = new ArrayList();
                List<VariableTree> arguments = new ArrayList();
                
                for (VariableElement ve : fields) {
                    AssignmentTree a = make.Assignment(make.MemberSelect(make.Identifier("this"), ve.getSimpleName()), make.Identifier(ve.getSimpleName()));
                    
                    statements.add(make.ExpressionStatement(a));
                    arguments.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), ve.getSimpleName(), make.Type(ve.asType()), null));
                }
                
                BlockTree body = make.Block(statements, false);
                
                members.add(pos, make.Method(make.Modifiers(mods), "<init>", null, Collections.<TypeParameterTree> emptyList(), arguments, Collections.<ExpressionTree>emptyList(), body, null));
                ClassTree decl = make.Class(node.getModifiers(), node.getSimpleName(), node.getTypeParameters(), node.getExtendsClause(), (List<ExpressionTree>)node.getImplementsClause(), members);
                model.setElement(decl, te);
                model.setType(decl, model.getType(node));
                model.setPos(decl, model.getPos(node));
                copy.rewrite(node, decl);
            }
            return null;
        }
        
    }

    private FileObject testSourceFO;
    private JavaSource source;
    private Document doc;
    
    private void copyToWorkDir(File resource, File toFile) throws IOException {
        //TODO: finally:
        InputStream is = new FileInputStream(resource);
        OutputStream outs = new FileOutputStream(toFile);
        
        int read;
        
        while ((read = is.read()) != (-1)) {
            outs.write(read);
        }
        
        outs.close();
        
        is.close();
    }
    
    private void performTest(String fileName) throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        
        FileObject scratch = SourceUtilsTestUtil.makeScratchDir(this);
        FileObject cache   = scratch.createFolder("cache");
        
        File wd         = getWorkDir();
        File testSource = new File(wd, "test/" + fileName + ".java");
        
        testSource.getParentFile().mkdirs();
        
        File dataFolder = new File(getDataDir(), "org/netbeans/test/codegen/");
        File targetFolder = new File(wd, "test");
        TestUtil.copyContents(dataFolder, targetFolder);

        testSourceFO = FileUtil.toFileObject(testSource);
        
        assertNotNull(testSourceFO);
        
        File testBuildTo = new File(wd, "test-build");
        
        testBuildTo.mkdirs();
        
        SourceUtilsTestUtil.prepareTest(FileUtil.toFileObject(dataFolder), FileUtil.toFileObject(testBuildTo), cache);
        SourceUtilsTestUtil.compileRecursively(FileUtil.toFileObject(dataFolder));
        
        source = JavaSource.forFileObject(testSourceFO);

        DataObject d = DataObject.find(testSourceFO);
        EditorCookie ec = (EditorCookie) d.getCookie(EditorCookie.class);

        doc = ec.openDocument();
    }

}
