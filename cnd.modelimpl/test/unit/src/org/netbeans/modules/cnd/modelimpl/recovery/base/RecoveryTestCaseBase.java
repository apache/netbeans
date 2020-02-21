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
package org.netbeans.modules.cnd.modelimpl.recovery.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;

/**
 *
 */
public class RecoveryTestCaseBase extends ProjectBasedTestCase {

    private static String goldenModel;
    private static String goldenAST;
    private final boolean isGolden;
    private boolean isNew = false;
    private final Diff annotation;
    private final Grammar grammar;
    private static final boolean EVALUATE_CLANG = false;

    public RecoveryTestCaseBase(String testName, Grammar gramma, Diff diff, Golden golden) {
        super(testName, true);
        this.annotation = diff;
        isGolden = golden != null;
        this.grammar = gramma;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public String getName() {
        StringBuilder buf = new StringBuilder(super.getName());
        if (grammar != null) {
            if (grammar.newGrammar()) {
                buf.append("-new");
            } else {
                buf.append("-old");
            }
        }
        return buf.toString();
    }

    boolean isNewGramma() {
        return isNew;
    }
    
    boolean isGolden() {
        return isGolden;
    }
    
    @Override
    protected File[] changeDefProjectDirBeforeParsingProjectIfNeeded(File projectDir) {
        if (annotation != null) {
            try {
                applyChanges(annotation);
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (grammar != null) {
            if (grammar.newGrammar()) {
                isNew = true;
                TraceFlags.validate("cnd.modelimpl.cpp.parser.action", true);
                TraceFlags.validate("cnd.modelimpl.cpp.parser.new.grammar", true);
                TraceFlags.validate("cnd.modelimpl.parse.headers.with.sources", true);
                if (grammar.traceAST()) {
                    TraceFlags.validate("cnd.modelimpl.cpp.parser.action.trace", true);
                } else {
                    TraceFlags.validate("cnd.modelimpl.cpp.parser.action.trace", false);
                }
                if (grammar.traceRules()) {
                    TraceFlags.validate("cnd.modelimpl.cpp.parser.rules.trace", true);
                } else {
                    TraceFlags.validate("cnd.modelimpl.cpp.parser.rules.trace", false);
                }
            } else {
                isNew = false;
                TraceFlags.validate("cnd.modelimpl.cpp.parser.action", false);
                TraceFlags.validate("cnd.modelimpl.cpp.parser.new.grammar", false);
                TraceFlags.validate("cnd.modelimpl.parse.headers.with.sources", false);
            }
        }
        return super.changeDefProjectDirBeforeParsingProjectIfNeeded(projectDir); //To change body of generated methods, choose Tools | Templates.
    }

    protected void implTest(String source) throws IOException {
        CsmProject project = getProject();
        CsmFile target = null;
        for (CsmFile file : project.getAllFiles()) {
            if (file.getAbsolutePath().toString().endsWith(source)) {
                target = file;
            }
        }
        ProcessUtils.ExitStatus execute = null;
        if (EVALUATE_CLANG) {
            final File dataFile = getDataFile(source);
            ProcessBuilder builder = new ProcessBuilder("/export/home/as204739/parfait-tools-1.0.1/bin/clang", "-cc1", "-ast-dump", dataFile.getAbsolutePath());
            execute = ProcessUtils.execute(builder);
            //System.err.println(execute.output);
        }

        assertNotNull(target);
        StringWriter w = new StringWriter();
        printTree(target, w, 0);
        if (isGolden) {
            goldenModel = w.toString();
            System.err.println("Inited golden content");
            if (EVALUATE_CLANG) {
                goldenAST = execute.getOutputString();
            }
        } else {
            String diff = annotation.file() + "[" + annotation.line() + ":" + annotation.column() + "," + annotation.length() + "]" + annotation.insert();
            if (EVALUATE_CLANG) {
                if (!isNew) {
                    assertModel(target, "Recovery clang " + source + " " + diff, goldenAST, execute.getOutputString());
                }
            } else {
                assertModel(target, "Recovery " + (isNew ? "new" : "old") + " " + source + " " + diff, goldenModel, w.toString());
            }
        }
    }

    protected void assertModel(CsmFile target, String msg, String expectedText, String actualText) {
        if (!actualText.equals(expectedText)) {
            StringBuilder sb = new StringBuilder();
            sb.append(msg);
            sb.append("\n----- expected model:\n");
            sb.append(expectedText);
            sb.append("\n----- actual model:\n");
            sb.append(actualText);
            sb.append("\n-----\n");
            int startLine = 1;
            for (int i = 0; i < actualText.length() && i < expectedText.length(); i++) {
                if (expectedText.charAt(i) == '\n') {
                    startLine++;
                }
                if (expectedText.charAt(i) != actualText.charAt(i)) {
                    sb.append("Diff starts in line ").append(startLine).append("\n");
                    String context = expectedText.substring(i);
                    if (context.length() > 40) {
                        context = context.substring(0, 40);
                    }
                    sb.append("Expected:").append(context).append("\n");
                    context = actualText.substring(i);
                    if (context.length() > 40) {
                        context = context.substring(0, 40);
                    }
                    sb.append("   Found:").append(context).append("\n");
                    break;
                }
            }
            System.err.println(sb.toString());
            if (target instanceof FileImpl) {
                FileImpl impl = (FileImpl) target;
                List<CsmParserProvider.ParserError> result = new ArrayList<>();
                impl.getErrors(result);
                for(CsmParserProvider.ParserError error : result) {
                    if (error.message != null) {
                        System.err.println(error.message+", col="+error.getColumn());
                    } else {
                        System.err.println(error.getTokenText()+", line="+error.getLine()+", col="+error.getColumn());
                    }
                }
            }
            fail(sb.toString());
        }
    }

    private void applyChanges(Diff diff) throws FileNotFoundException, IOException {
        File dataFile = getDataFile(diff.file());
        final Charset charset = Charset.forName("UTF-8");
        List<String> list = Files.readAllLines(dataFile.toPath(), charset);
        try (BufferedWriter out = Files.newBufferedWriter(dataFile.toPath(), charset)) {
            for (int i = 0; i < list.size(); i++) {
                String s = list.get(i);
                if (diff.line() == i + 1) {
                    s = s.substring(0, diff.column() - 1) + diff.insert() + s.substring(diff.column() - 1 + diff.length());
                }
                out.write(s);
                out.write("\n");
            }
            out.flush();
        }
    }

    private void printTree(CsmFile file, Writer out, int level) throws IOException {
        for (CsmDeclaration decl : file.getDeclarations()) {
            printTree(decl, out, level);
        }
    }

    private void printTree(CsmDeclaration decl, Writer out, int level) throws IOException {
        out.append(indent(level) + decl.getKind() + " " + decl.getName()).append('\n');
        if (CsmKindUtilities.isNamespaceDefinition(decl)) {
            printNamespace((CsmNamespaceDefinition) decl, out, level + 1);
        } else if (CsmKindUtilities.isCompoundClassifier(decl)) {
            printClassifier((CsmCompoundClassifier) decl, out, level + 1);
        }
    }

    private void printNamespace(CsmNamespaceDefinition ns, Writer out, int level) throws IOException {
        for (CsmOffsetableDeclaration d : ns.getDeclarations()) {
            printTree(d, out, level);
        }
    }

    private void printClassifier(CsmCompoundClassifier cls, Writer out, int level) throws IOException {
        for (CsmScopeElement d : cls.getScopeElements()) {
            if (CsmKindUtilities.isCompoundClassifier(d)) {
                final CsmCompoundClassifier c = (CsmCompoundClassifier) d;
                out.append(indent(level) + c.getKind() + " " + c.getName()).append('\n');
                printClassifier(c, out, level + 1);
            } else if (CsmKindUtilities.isDeclaration(d)) {
                printTree((CsmDeclaration) d, out, level);
            }
        }
        if (CsmKindUtilities.isClass(cls)) {
            CsmClass c = (CsmClass) cls;
            for (CsmFriend f : c.getFriends()) {
                printTree(f, out, level);
            }
        }
    }

    private String indent(int level) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < level; i++) {
            buf.append("  ");
        }
        return buf.toString();
    }
}
