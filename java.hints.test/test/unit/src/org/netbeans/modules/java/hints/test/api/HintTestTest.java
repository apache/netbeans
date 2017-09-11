/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.test.api;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import javax.swing.text.Document;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.JavaFix.TransformationContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author lahvac
 */
public class HintTestTest {

    public HintTestTest() {
    }

    @Test
    public void testNonJavaChanges() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test { }\n")
                .input("test/test.txt", "1\n2\n", false)
                .run(NonJavaChanges.class)
                .findWarning("1:13-1:17:verifier:Test")
                .applyFix()
                .assertVerbatimOutput("test/test.txt", "2\n3\n");
    }

    @Test
    public void test220070() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test { }\n")
                .input("test/test.txt", "1\n2\n", false)
                .run(NonJavaChanges.class)
                .findWarning("1:13-1:17:verifier:Test")
                .applyFix()
                .assertOutput("test/test.txt", "2\r3\r");
    }
    
    @Test
    public void testNonJavaChangesOpenedInEditor() throws Exception {
        HintTest ht = HintTest.create()
                              .input("package test;\n" +
                                     "public class Test { }\n")
                              .input("test/test.txt", "1\n2\n", false);
        FileObject resource = ht.getSourceRoot().getFileObject("test/test.txt");
        DataObject od = DataObject.find(resource);
        EditorCookie ec = od.getLookup().lookup(EditorCookie.class);
        Document doc = ec.openDocument();
        doc.remove(0, doc.getLength());
        doc.insertString(0, "5\n6\n", null);
        ht.run(NonJavaChanges.class)
          .findWarning("1:13-1:17:verifier:Test")
          .applyFix(false)
          .assertVerbatimOutput("test/test.txt", "6\n7\n");
        Assert.assertEquals("1\n2\n", resource.asText("UTF-8"));
        Assert.assertEquals("6\n7\n", doc.getText(0, doc.getLength()));
    }

    @Hint(displayName="testingNonJavaChanges", description="testingNonJavaChanges", category="test")
    public static final class NonJavaChanges {
        @TriggerTreeKind(Kind.CLASS)
        public static ErrorDescription hint(HintContext ctx) {
            return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), "Test", new TestingNonJavaChangesFix(ctx.getInfo(), ctx.getPath()).toEditorFix());
        }
    }

    private static final class TestingNonJavaChangesFix extends JavaFix {

        public TestingNonJavaChangesFix(CompilationInfo info, TreePath tp) {
            super(info, tp);
        }

        @Override protected String getText() {
            return "Test";
        }

        @Override protected void performRewrite(TransformationContext ctx) {
            try {
                FileObject resource = ctx.getWorkingCopy().getFileObject().getParent().getFileObject("test.txt");
                Assert.assertNotNull(resource);
                Reader r = new InputStreamReader(ctx.getResourceContent(resource), "UTF-8");
                ByteArrayOutputStream outData = new ByteArrayOutputStream();
                Writer w = new OutputStreamWriter(outData, "UTF-8");
                int read;

                while ((read = r.read()) != -1) {
                    if (read != '\n') read++;
                    w.write(read);
                }

                r.close();
                w.close();

                OutputStream out = ctx.getResourceOutput(resource);

                out.write(outData.toByteArray());

                out.close();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        }

    }

    @Test
    public void testMeaningfullSourcePath() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test { }\n")
                .run(MeaningfullSourcePath.class)
                .assertWarnings();
    }

    @Hint(displayName="meaningfullSourcePath", description="meaningfullSourcePath", category="test")
    public static final class MeaningfullSourcePath {
        @TriggerTreeKind(Kind.CLASS)
        public static ErrorDescription hint(HintContext ctx) {
            if (ctx.getInfo().getClasspathInfo().getClassPath(PathKind.SOURCE).findOwnerRoot(ctx.getInfo().getFileObject()) == null) {
                return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), "Broken Source Path");
            }

            return null;
        }
    }

    @Test
    public void testCompilationClassPath() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test { }\n")
                .classpath(FileUtil.getArchiveRoot(JavaSource.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(CompilationClassPath.class)
                .assertWarnings();
    }

    @Hint(displayName="compilationClassPath", description="compilationClassPath", category="test")
    public static final class CompilationClassPath {
        @TriggerTreeKind(Kind.CLASS)
        public static ErrorDescription hint(HintContext ctx) {
            FileObject clazz = ctx.getInfo().getClasspathInfo().getClassPath(PathKind.COMPILE).findResource("org/netbeans/api/java/source/JavaSource.class");

            if (clazz == null) {
                return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), "Broken Compilation ClassPath");
            }

            return null;
        }
    }

    @Test
    public void testHintThrowsException() throws Exception {
        HintTest ht = HintTest.create()
                              .input("package test;\n" +
                                     "public class Test { }\n");
        try {
            ht.run(HintThrowsException.class);
            Assert.fail("No exception thrown");
        } catch (Exception ex) {
            //ok
            Assert.assertEquals(IllegalStateException.class, ex.getClass());
            Assert.assertNotNull(ex.getCause());
            Assert.assertEquals(NullPointerException.class, ex.getCause().getClass());
            Assert.assertEquals("a", ex.getCause().getMessage());
        }
    }
    
    @Hint(displayName="hintThrowsException", description="hintThrowsException", category="test")
    public static final class HintThrowsException {
        @TriggerTreeKind(Kind.CLASS)
        public static ErrorDescription hint(HintContext ctx) {
            throw new NullPointerException("a");
        }
    }

    @Test
    public void testNonJavaFix() throws Exception {
        HintTest ht = HintTest.create()
                              .input("package test;\n" +
                                     "public class Test { }\n");
        try {
            ht.run(NonJavaFix.class)
              .findWarning("1:0-1:21:verifier:Test")
              .applyFix();
            Assert.fail("No exception thrown");
        } catch (AssertionError ae) {
            //ok
            Assert.assertEquals("The fix must be a JavaFix", ae.getMessage());
        }
    }
    
    @Hint(displayName="nonJavaFix", description="nonJavaFix", category="test")
    public static final class NonJavaFix {
        @TriggerTreeKind(Kind.CLASS)
        public static ErrorDescription hint(HintContext ctx) {
            return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), "Test", new Fix() {
                @Override public String getText() {
                    return "Fix";
                }
                @Override public ChangeInfo implement() throws Exception {
                    return null;
                }
            });
        }
    }
    
    @Test
    public void testNotRepeatableJavaFix() throws Exception {
        HintTest ht = HintTest.create()
                              .input("package test;\n" +
                                     "public class Test { }\n");
        try {
            ht.run(NotRepeatableJavaFix.class)
              .findWarning("1:0-1:21:verifier:Test")
              .applyFix();
            Assert.fail("No exception thrown");
        } catch (AssertionError ae) {
            //ok
            Assert.assertTrue(ae.getMessage().startsWith("The fix must be repeatable"));
        }
    }
    
    @Hint(displayName="notRepeatableJavaFix", description="notRepeatableJavaFix", category="test")
    public static final class NotRepeatableJavaFix {
        @TriggerTreeKind(Kind.CLASS)
        public static ErrorDescription hint(HintContext ctx) {
            Fix f = new JavaFix(ctx.getInfo(), ctx.getPath()) {
                private boolean wasRun;
                @Override protected String getText() {
                    return "Fix";
                }
                @Override protected void performRewrite(TransformationContext ctx) throws Exception {
                    if (wasRun) return ;
                    ctx.getWorkingCopy().rewrite(ctx.getPath().getLeaf(), ctx.getWorkingCopy().getTreeMaker().setLabel(ctx.getPath().getLeaf(), "Nue"));
                    wasRun = true;
                }
            }.toEditorFix();
            return ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), "Test", f);
        }
    }

    @Test
    public void testNonJavaChangesOpenedInEditor214197() throws Exception {
        HintTest ht = HintTest.create()
                              .input("package test;\n" +
                                     "public class Test { }\n")
                              .input("test/test.txt", "1\n#foobar\n\n2", false);
        FileObject resource = ht.getSourceRoot().getFileObject("test/test.txt");
        DataObject od = DataObject.find(resource);
        EditorCookie ec = od.getLookup().lookup(EditorCookie.class);
        Document doc = ec.openDocument();
        doc.remove(0, doc.getLength());
        doc.insertString(0, "5\n6\n", null);
        ht.run(NonJavaChanges.class)
          .findWarning("1:13-1:17:verifier:Test")
          .applyFix(false)
          .assertVerbatimOutput("test/test.txt", "6\n7\n");
        Assert.assertEquals("1\n#foobar\n\n2", resource.asText("UTF-8"));
        Assert.assertEquals("6\n7\n", doc.getText(0, doc.getLength()));
    }

}
