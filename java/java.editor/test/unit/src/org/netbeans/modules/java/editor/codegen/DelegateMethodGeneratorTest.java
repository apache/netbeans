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
package org.netbeans.modules.java.editor.codegen;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ScanUtils;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.editor.codegen.ui.ElementNode;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class DelegateMethodGeneratorTest extends NbTestCase {
    
    public DelegateMethodGeneratorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        SourceUtilsTestUtil2.disableConfinementTest();
    }

    public void testFindUsableFields() throws Exception {
        prepareTest("FindUsableFieldsTest");

        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(source, Phase.RESOLVED);
        TypeElement clazz = info.getElements().getTypeElement("test.FindUsableFieldsTest");

        List<ElementNode.Description> variables = DelegateMethodGenerator.computeUsableFieldsDescriptions(info, info.getTrees().getPath(clazz));

        assertEquals(1, variables.size());
        
        variables = variables.get(0).getSubs();
        
        assertEquals(2, variables.size());
        assertTrue("s".contentEquals(variables.get(0).getName()));
        assertTrue("l".contentEquals(variables.get(1).getName()));
    }

    public void testMethodProposals1() throws Exception {
        performMethodProposalsTest("a");
    }

    public void testMethodProposals2() throws Exception {
        performMethodProposalsTest("l");
    }

    public void testMethodProposals3() throws Exception {
        performMethodProposalsTest("s");
    }

    public void testGenerate129140() throws Exception {
        performTestGenerateTest("ll", "toArray", 1);
    }

    public void testGenerate133625a() throws Exception {
        performTestGenerateTest("lext", "add", 1);
    }

    public void testGenerate133625b() throws Exception {
        performTestGenerateTest("lsup", "add", 1);
    }

    public void testGenerate133625c() throws Exception {
        performTestGenerateTest("lsup", "addAll", 1);
    }

    public void testGenerate133625d() throws Exception {
        performTestGenerateTest("lub", "add", 1);
    }

    private void performTestGenerateTest(String field, final String methodName, final int paramCount) throws Exception {
        prepareTest("MethodProposals");

        CompilationInfo info = SourceUtilsTestUtil.getCompilationInfo(source, Phase.RESOLVED);
        TypeElement clazz = info.getElements().getTypeElement("test.MethodProposals");
        VariableElement variable = null;

        for (VariableElement v : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
            if (field.contentEquals(v.getSimpleName())) {
                variable = v;
            }
        }

        assertNotNull(variable);

        final VariableElement variableFinal = variable;

        final TreePath ct = info.getTrees().getPath(clazz);

        source.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy wc) throws Exception {
                wc.toPhase(Phase.RESOLVED);
                VariableElement var = ElementHandle.create(variableFinal).resolve(wc);
                TreePath tp = TreePathHandle.create(ct, wc).resolve(wc);
                TypeElement type = (TypeElement) ((DeclaredType) var.asType()).asElement();
                for (ExecutableElement ee : ElementFilter.methodsIn(type.getEnclosedElements())) {
                    if (methodName.equals(ee.getSimpleName().toString()) && ee.getParameters().size() == paramCount) {
                        DelegateMethodGenerator.generateDelegatingMethods(wc, tp, var, Collections.singletonList(ee), -1);
                    }
                }
            }
        }).commit();

        source.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {
                getRef().print(parameter.getText());
            }
        }, true);

        String version = System.getProperty("java.specification.version") + "/";
        compareReferenceFiles(this.getName()+".ref", findGoldenFile(), this.getName()+".diff");
    }
     
    private void performMethodProposalsTest(final String name) throws Exception {
        prepareTest("MethodProposals");

        ScanUtils.waitUserActionTask(source, new Task<CompilationController>() {

            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(Phase.RESOLVED);
                TypeElement clazz = info.getElements().getTypeElement("test.MethodProposals");
                VariableElement variable = null;

                for (VariableElement v : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
                    if (name.contentEquals(v.getSimpleName())) {
                        variable = v;
                    }
                }

                assertNotNull(variable);

                ClassTree ct = info.getTrees().getTree(clazz);
                int offset = (int) (info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), ct) - 1);
                compareMethodProposals(info, DelegateMethodGenerator.getAvailableMethods(info, offset,
                        ElementHandle.create(clazz), ElementHandle.create(variable)));
            }
        });
    }
     
    private void compareMethodProposals(CompilationInfo info, ElementNode.Description proposal) {
        List<ElementNode.Description> proposals = new LinkedList<ElementNode.Description>();
        Queue<ElementNode.Description> q = new LinkedList<ElementNode.Description>();
        
        q.offer(proposal);
        
        while (!q.isEmpty()) {
            ElementNode.Description en = q.remove();
            
            if (en == null) {
                continue;
            }
            if (en.getSubs() == null || en.getSubs().isEmpty()) {
                proposals.add(en);
                continue;
            }
            
            if (en.getElementHandle() != null && en.getElementHandle().getKind() == ElementKind.CLASS && "java.lang.Object".equals(en.getElementHandle().getBinaryName())) {
                continue;
            }
            
            for (ElementNode.Description d : en.getSubs()) {
                q.offer(d);
            }
        }
        
        List<String> result = new ArrayList<String>();
        
        for (ElementNode.Description d : proposals) {
            ExecutableElement resolved = (ExecutableElement) d.getElementHandle().resolve(info);
            result.add(dump(resolved));
        }
        
        Collections.sort(result);
        
        for (String s : result) {
            ref(s);
        }
        
        compareReferenceFiles(this.getName()+".ref", findGoldenFile(), this.getName()+".diff");
    }
    
    private String dump(ExecutableElement ee) {
        StringBuilder result = new StringBuilder();
        
        for (Modifier m : ee.getModifiers()) {
            result.append(m.toString());
            result.append(' ');
        }
        
        result.append(ee.getReturnType().toString() + " " + ee.toString());
        
        return result.toString();
    }

    public String findGoldenFile() {
        String version = System.getProperty("java.specification.version");
        for (String variant : computeVersionVariantsFor(version)) {
            String path = variant + "/" + this.getName() + ".pass";
            File goldenFile = new File(getDataDir()+"/goldenfiles/"+ getClass().getName().replace(".", "/") + "/" + path);
            if (goldenFile.exists())
                return path;
        }
        throw new AssertionError();
    }

    private List<String> computeVersionVariantsFor(String version) {
        int dot = version.indexOf('.');
        version = version.substring(dot + 1);
        int versionNum = Integer.parseInt(version);
        List<String> versions = new ArrayList<>();

        for (int v = versionNum; v >= 8; v--) {
            versions.add(v != 8 ? "" + v : "1." + v);
        }

        return versions;
    }

    private FileObject testSourceFO;
    private JavaSource source;
    
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
    
    private void prepareTest(String fileName) throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        
        FileObject scratch = SourceUtilsTestUtil.makeScratchDir(this);
        FileObject cache   = scratch.createFolder("cache");
        
        File wd         = getWorkDir();
        File testSource = new File(wd, "test/" + fileName + ".java");
        
        testSource.getParentFile().mkdirs();
        
        File dataFolder = new File(getDataDir(), "org/netbeans/modules/java/editor/codegen/data/");
        
        for (File f : dataFolder.listFiles()) {
            copyToWorkDir(f, new File(wd, "test/" + f.getName()));
        }
        
        testSourceFO = FileUtil.toFileObject(testSource);
        
        assertNotNull(testSourceFO);
        
        File testBuildTo = new File(wd, "test-build");
        
        testBuildTo.mkdirs();
        
        SourceUtilsTestUtil.prepareTest(FileUtil.toFileObject(dataFolder), FileUtil.toFileObject(testBuildTo), cache);
        SourceUtilsTestUtil.compileRecursively(FileUtil.toFileObject(dataFolder));
        
        source = JavaSource.forFileObject(testSourceFO);
    }
}
