/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.editor.imports;

import org.netbeans.modules.java.editor.base.imports.UnusedImports;

import com.sun.source.util.TreePath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;

/**
 *
 * @author lahvac
 */
public class UnusedImportsTest extends NbTestCase {

    public void testUnusedImports() throws Exception {
        performUnusedImportsTest(new FileDescription("test/UnusedImports.java", "package test;\n" +
                                                                                "|import java.beans.*;| |import static java.lang.Math.min;| import static java.lang.Math.max; |import static java.lang.System.*;| import java.lang.reflect.*;\n" +
                                                                                "|import java.io.FileOutputStream;|\n" +
                                                                                "|import java.io.File;|\n" +
                                                                                "import java.io.Serializable;\n" +
                                                                                "import java.util.List;\n" +
                                                                                "import javax.swing.JTable;\n" +
                                                                                "|import java.util.Collection;|\n" +
                                                                                "import java.util.Collections;\n" +
                                                                                "import java.util.Iterator;\n" +
                                                                                "|import java.util.Stack;|\n" +
                                                                                "import java.util.Set;\n" +
                                                                                "|import java.util.HashSet;|\n" +
                                                                                "|import java.util.Map;|\n" +
                                                                                "import java.util.Map.Entry;\n" +
                                                                                "|import java.util.HashMap;|\n" +
                                                                                "import javax.swing.text.BadLocationException;\n" +
                                                                                "import java.util.Date;\n" +
                                                                                "\n" +
                                                                                "public class UnusedImports<E, F> extends JTable implements Serializable {\n" +
                                                                                "    \n" +
                                                                                "    Entry e;\n" +
                                                                                "    \n" +
                                                                                "    private List list;\n" +
                                                                                "    \n" +
                                                                                "    private static final List l = Collections.EMPTY_LIST;\n" +
                                                                                "    \n" +
                                                                                "    public static void main(String[] args) throws BadLocationException {\n" +
                                                                                "        Set<Iterator> l;\n" +
                                                                                "        Collections.<Date>emptyList();\n" +
                                                                                "        \n" +
                                                                                "        Constructor c;\n" +
                                                                                "        max(1, 2);\n" +
                                                                                "    }\n" +
                                                                                "    \n" +
                                                                                "}\n" +
                                                                                ""));
    }

    public void testColorings1() throws Exception {
        performUnusedImportsTest(new FileDescription("test/Colorings1.java", "package test;\n" +
                                                                             "\n" +
                                                                             "|import java.util.Iterator;|\n" +
                                                                             "import java.util.List;\n" +
                                                                             "|import java.util.Set;|\n" +
                                                                             "|import javax.swing.text.BadLocationException;|\n" +
                                                                             "\n" +
                                                                             "public class Colorings1 {\n" +
                                                                             "    \n" +
                                                                             "    private List list1;\n" +
                                                                             "    private static List list2;\n" +
                                                                             "    private static final List list3 = null;\n" +
                                                                             "    \n" +
                                                                             "    /**@deprecated*/\n" +
                                                                             "    private List list4;\n" +
                                                                             "    \n" +
                                                                             "    @Deprecated\n" +
                                                                             "    public List list5;\n" +
                                                                             "    \n" +
                                                                             "           List list6;\n" +
                                                                             "    \n" +
                                                                             "    protected List list7;\n" +
                                                                             "    \n" +
                                                                             "    public static void main(String[] args) {\n" +
                                                                             "        List list8 = null;\n" +
                                                                             "        \n" +
                                                                             "        list2.add(\"\");\n" +
                                                                             "    }\n" +
                                                                             "    \n" +
                                                                             "    private void test1() {\n" +
                                                                             "        Test1 test1;\n" +
                                                                             "        Test2 test2;\n" +
                                                                             "        Test3 test3;\n" +
                                                                             "        Test4 test4;\n" +
                                                                             "        Test5 test5;\n" +
                                                                             "    }\n" +
                                                                             "\n" +
                                                                             "    private static void test2() {\n" +
                                                                             "        \n" +
                                                                             "    }\n" +
                                                                             "    \n" +
                                                                             "    /**@deprecated*/\n" +
                                                                             "    public final void test3() {\n" +
                                                                             "        \n" +
                                                                             "    }\n" +
                                                                             "    \n" +
                                                                             "    @Deprecated\n" +
                                                                             "    private static void test4() {\n" +
                                                                             "        \n" +
                                                                             "    }\n" +
                                                                             "    \n" +
                                                                             "    private static class Test1 {\n" +
                                                                             "        \n" +
                                                                             "    }\n" +
                                                                             "\n" +
                                                                             "    /**@deprecated*/\n" +
                                                                             "    private static final class Test2 {\n" +
                                                                             "        \n" +
                                                                             "    }\n" +
                                                                             "\n" +
                                                                             "    static class Test3 {\n" +
                                                                             "        \n" +
                                                                             "    }\n" +
                                                                             "\n" +
                                                                             "    class Test4 {\n" +
                                                                             "        \n" +
                                                                             "    }\n" +
                                                                             "\n" +
                                                                             "    @Deprecated\n" +
                                                                             "    public class Test5 {\n" +
                                                                             "        \n" +
                                                                             "    }\n" +
                                                                             "    \n" +
                                                                             "}\n" +
                                                                             ""));
    }

    public void testReadUseInstanceOf() throws Exception {
        performUnusedImportsTest(new FileDescription("test/ReadUseInstanceOf.java", "package test;\n" +
                                                                                    "\n" +
                                                                                    "import java.util.List;\n" +
                                                                                    "import java.util.LinkedList;\n" +
                                                                                    "\n" +
                                                                                    "public class ReadUseInstanceOf {\n" +
                                                                                    "\n" +
                                                                                    "    public void test() {\n" +
                                                                                    "        List l = null;\n" +
                                                                                    "        \n" +
                                                                                    "        if (l instanceof LinkedList) {\n" +
                                                                                    "            //nothing;\n" +
                                                                                    "        }\n" +
                                                                                    "    }\n" +
                                                                                    "    \n" +
                                                                                    "}\n" +
                                                                                    ""));
    }

    public void testReadUseTypeCast() throws Exception {
        performUnusedImportsTest(new FileDescription("test/ReadUseTypeCast.java", "package test;\n" +
                                                                                  "\n" +
                                                                                  "import java.util.List;\n" +
                                                                                  "import java.util.LinkedList;\n" +
                                                                                  "\n" +
                                                                                  "public class ReadUseTypeCast {\n" +
                                                                                  "\n" +
                                                                                  "    public void test() {\n" +
                                                                                  "        List l = null;\n" +
                                                                                  "        LinkedList l2 = (LinkedList) l;\n" +
                                                                                  "    }\n" +
                                                                                  "    \n" +
                                                                                  "}\n" +
                                                                                  ""));
    }

    public void testForEach() throws Exception {
        performUnusedImportsTest(new FileDescription("test/ForEach.java", "package test;\n" +
                                                                          "import java.util.List;\n" +
                                                                          "\n" +
                                                                          "\n" +
                                                                          "public class ForEach {\n" +
                                                                          "    \n" +
                                                                          "    public static void main(String[] args) {\n" +
                                                                          "        List<String> s = null;\n" +
                                                                          "        \n" +
                                                                          "        for (String t : s) {\n" +
                                                                          "            System.err.println(t);\n" +
                                                                          "        }\n" +
                                                                          "    }\n" +
                                                                          "    \n" +
                                                                          "}\n" +
                                                                          ""));
    }

    public void testReturnType() throws Exception {
        performUnusedImportsTest(new FileDescription("test/ReturnType.java", "package test;\n" +
                                                                             "\n" +
                                                                             "import java.util.Iterator;\n" +
                                                                             "\n" +
                                                                             "public class ReturnType {\n" +
                                                                             "    \n" +
                                                                             "    public Iterator test3() {\n" +
                                                                             "        return null;\n" +
                                                                             "    }\n" +
                                                                             "    \n" +
                                                                             "}\n" +
                                                                             ""));
    }

    public void testCommentedGenerics() throws Exception {
        performUnusedImportsTest(new FileDescription("test/CommentedGenerics.java", "package test;\n" +
                                                                                    "\n" +
                                                                                    "import java.util.List;\n" +
                                                                                    "\n" +
                                                                                    "public abstract class CommentedGenerics {\n" +
                                                                                    "\n" +
                                                                                    "    public abstract List/*<String>*/ getList();\n" +
                                                                                    "\n" +
                                                                                    "    private List/*<String>*/ field;\n" +
                                                                                    "}\n" +
                                                                                    ""));
    }

    public void testRetentionPolicy() throws Exception {
        performUnusedImportsTest(new FileDescription("test/RetentionPolicyTest.java", "package test;\n" +
                                                                                      "\n" +
                                                                                      "import java.lang.annotation.Retention;\n" +
                                                                                      "import java.lang.annotation.RetentionPolicy;\n" +
                                                                                      "\n" +
                                                                                      "@Retention(RetentionPolicy.CLASS)\n" +
                                                                                      "public @interface RetentionPolicyTest {\n" +
                                                                                      "    \n" +
                                                                                      "}\n" +
                                                                                      ""));
    }

    public void testSimpleGeneric() throws Exception {
        performUnusedImportsTest(new FileDescription("test/SimpleGeneric.java", "package test;\n" +
                                                                                "import java.util.List;\n" +
                                                                                "\n" +
                                                                                "\n" +
                                                                                "public class SimpleGeneric {\n" +
                                                                                "    \n" +
                                                                                "    public static void main(String[] args) {\n" +
                                                                                "        List<String> s;\n" +
                                                                                "    }\n" +
                                                                                "    \n" +
                                                                                "}\n" +
                                                                                ""));
    }

    public void testUseInGenerics() throws Exception {
        performUnusedImportsTest(new FileDescription("test/UseInGenerics.java", "package test;\n" +
                                                                                "\n" +
                                                                                "import java.util.List;\n" +
                                                                                "\n" +
                                                                                "public class UseInGenerics<T extends List> {\n" +
                                                                                "    \n" +
                                                                                "}\n" +
                                                                                ""));
    }

    public void testConstructorsAreMethods() throws Exception {
        performUnusedImportsTest(new FileDescription("test/ConstructorsAreMethods.java", "package test;\n" +
                                                                                         "\n" +
                                                                                         "import java.util.ArrayList;\n" +
                                                                                         "import java.util.List;\n" +
                                                                                         "\n" +
                                                                                         "public class ConstructorsAreMethods {\n" +
                                                                                         "    \n" +
                                                                                         "    public static void main(String[] args) {\n" +
                                                                                         "        List<String> s = new ArrayList<String>();\n" +
                                                                                         "    }\n" +
                                                                                         "    \n" +
                                                                                         "}\n" +
                                                                                         ""));
    }

    public void testConstructorsAreMethods3() throws Exception {
        performUnusedImportsTest(new FileDescription("test/ConstructorsAreMethods3.java", "package test;\n" +
                                                                                          "\n" +
                                                                                          "import java.util.HashSet;\n" +
                                                                                          "\n" +
                                                                                          "public class ConstructorsAreMethods3 {\n" +
                                                                                          "    \n" +
                                                                                          "    public ConstructorsAreMethods3() {\n" +
                                                                                          "        new HashSet<String>();\n" +
                                                                                          "    }\n" +
                                                                                          "    \n" +
                                                                                          "}\n" +
                                                                                          ""));
    }

    public void testNewArrayIsClassUse() throws Exception {
        performUnusedImportsTest(new FileDescription("test/NewArrayIsClassUse.java", "package test;\n" +
                                                                                     "\n" +
                                                                                     "import java.util.ArrayList;\n" +
                                                                                     "\n" +
                                                                                     "public class NewArrayIsClassUse {\n" +
                                                                                     "    \n" +
                                                                                     "    public static void test() {\n" +
                                                                                     "        Object nue = new ArrayList[0];\n" +
                                                                                     "    }\n" +
                                                                                     "    \n" +
                                                                                     "}\n" +
                                                                                     ""));
    }

    public void testGenericBoundIsClassUse() throws Exception {
        performUnusedImportsTest(new FileDescription("test/GenericBoundIsClassUse.java", "package test;\n" +
                                                                                         "\n" +
                                                                                         "import java.util.ArrayList;\n" +
                                                                                         "import java.util.Comparator;\n" +
                                                                                         "import java.util.HashMap;\n" +
                                                                                         "import java.util.List;\n" +
                                                                                         "\n" +
                                                                                         "public class GenericBoundIsClassUse<T extends List> {\n" +
                                                                                         "    \n" +
                                                                                         "    public static <T extends ArrayList> void test1(T t) {\n" +
                                                                                         "    }\n" +
                                                                                         "    \n" +
                                                                                         "    public static <T extends HashMap & Comparator> void test1(T t) {\n" +
                                                                                         "    }\n" +
                                                                                         "    \n" +
                                                                                         "}\n" +
                                                                                         ""));
    }

    public void testBLE91246() throws Exception {
        performUnusedImportsTest(new FileDescription("test/BLE91246.java", "package test;\n" +
                                                                           "\n" +
                                                                           "import java.lang.ref.WeakReference;\n" +
                                                                           "import javax.swing.text.Document;\n" +
                                                                           "\n" +
                                                                           "public class BLE91246 {\n" +
                                                                           "    \n" +
                                                                           "    public void test() {\n" +
                                                                           "        Document doc = null;\n" +
                                                                           "        \n" +
                                                                           "        doc.putProperty(Document.class, new WeakReference<Document>(null) {});\n" +
                                                                           "    }\n" +
                                                                           "    \n" +
                                                                           "}\n" +
                                                                           ""));
    }

    public void test88119() throws Exception {
        performUnusedImportsTest(new FileDescription("test/package-info.java", "@Documented\n" +
                                                                               "package test;\n" +
                                                                               "\n" +
                                                                               "import java.lang.annotation.Documented;\n" +
                                                                               ""));
    }

    public void test111113() throws Exception {
        performUnusedImportsTest(new FileDescription("test/UnusedImport111113.java", "package test;\n" +
                                                                                     "\n" +
                                                                                     "import java.util.List;\n" +
                                                                                     "\n" +
                                                                                     "public class UnusedImport111113 {\n" +
                                                                                     "    \n" +
                                                                                     "    public static void main() {\n" +
                                                                                     "        System.out.println(List[].class.getName());\n" +
                                                                                     "    }\n" +
                                                                                     "    \n" +
                                                                                     "}\n" +
                                                                                     ""));
    }

    public void test89356() throws Exception {
        performUnusedImportsTest(new FileDescription("test/SerialVersionUID89356.java", "package test;\n" +
                                                                                        "\n" +
                                                                                        "import java.io.Serializable;\n" +
                                                                                        "\n" +
                                                                                        "public class SerialVersionUID89356 implements Serializable {\n" +
                                                                                        "    private static final long serialVersionUID = 3292276783870598274L;\n" +
                                                                                        "    \n" +
                                                                                        "    private SerialVersionUID89356() {\n" +
                                                                                        "    }\n" +
                                                                                        "}\n" +
                                                                                        ""));
    }

    public void testCastIsClassUse() throws Exception {
        performUnusedImportsTest(new FileDescription("test/CastIsClassUse.java", "package test;\n" +
                                                                                 "\n" +
                                                                                 "import java.util.List;\n" +
                                                                                 "\n" +
                                                                                 "public class CastIsClassUse {\n" +
                                                                                 "    \n" +
                                                                                 "    public void main(Object args) {\n" +
                                                                                 "        Object a = (List) args;\n" +
                                                                                 "    }\n" +
                                                                                 "    \n" +
                                                                                 "}\n" +
                                                                                 ""));
    }

    public void testWildcardBoundIsClassUse() throws Exception {
        performUnusedImportsTest(new FileDescription("test/WildcardBoundIsClassUse.java", "package test;\n" +
                                                                                          "\n" +
                                                                                          "import java.text.Collator;\n" +
                                                                                          "import java.util.Iterator;\n" +
                                                                                          "import java.util.Locale;\n" +
                                                                                          "\n" +
                                                                                          "public class WildcardBoundIsClassUse {\n" +
                                                                                          "    \n" +
                                                                                          "    public Iterable<? extends Iterator> test1() {\n" +
                                                                                          "        return null;\n" +
                                                                                          "    }\n" +
                                                                                          "    \n" +
                                                                                          "    public Iterable<Locale> test2() {\n" +
                                                                                          "        return null;\n" +
                                                                                          "    }\n" +
                                                                                          "    \n" +
                                                                                          "    public Iterable<? super Collator> test3() {\n" +
                                                                                          "        return null;\n" +
                                                                                          "    }\n" +
                                                                                          "    \n" +
                                                                                          "}\n" +
                                                                                          ""));
    }

    public void testStaticImport128662() throws Exception {
        performUnusedImportsTest(new FileDescription("test/StaticImport128662.java", "package test;\n" +
                                                                                     "\n" +
                                                                                     "import static test.B.*;\n" +
                                                                                     "\n" +
                                                                                     "public class StaticImport128662 {\n" +
                                                                                     "\n" +
                                                                                     "    public static void doStuff() {\n" +
                                                                                     "        a();\n" +
                                                                                     "    }\n" +
                                                                                     "    \n" +
                                                                                     "}\n" +
                                                                                     "\n" +
                                                                                     "class A {\n" +
                                                                                     "    public static void a() {}\n" +
                                                                                     "}\n" +
                                                                                     "\n" +
                                                                                     "class B extends A {\n" +
                                                                                     "    public static void b() {}\n" +
                                                                                     "}\n" +
                                                                                     ""));
    }

    public void testUsedImport129988() throws Exception {
        performUnusedImportsTest(new FileDescription("test/UsedImport129988.java", "package test;\n" +
                                                                                   "\n" +
                                                                                   "import java.util.List;\n" +
                                                                                   "\n" +
                                                                                   "public class UsedImport129988<T> {   \n" +
                                                                                   "    void go() {\n" +
                                                                                   "        new UsedImport129988<List<String>>();\n" +
                                                                                   "    }    \n" +
                                                                                   "}\n" +
                                                                                   ""));
    }

    public void testUsedImport132980() throws Exception {
        performUnusedImportsTest(new FileDescription("test/UsedImport132980.java", "package test;\n" +
                                                                                   "\n" +
                                                                                   "import java.util.Collections;\n" +
                                                                                   "import java.util.Comparator;\n" +
                                                                                   "import java.util.LinkedList;\n" +
                                                                                   "\n" +
                                                                                   "public class UsedImport132980 {   \n" +
                                                                                   "    {\n" +
                                                                                   "        new LinkedList(Collections.<Comparator<? super String>>emptyList());\n" +
                                                                                   "    }\n" +
                                                                                   "}\n" +
                                                                                   ""));
    }

    public void testUsedImport159773() throws Exception {
        performUnusedImportsTest(new FileDescription("test/UsedImport159773.java", "package test;\n" +
                                                                                   "\n" +
                                                                                   "import java.util.ArrayList;\n" +
                                                                                   "import static java.util.Collections.sort; // Import has \"Remove Unused Import\" hint.\n" +
                                                                                   "import static java.util.Arrays.sort;\n" +
                                                                                   "\n" +
                                                                                   "public class UsedImport159773 {\n" +
                                                                                   "\n" +
                                                                                   "    public static void main(String[] args) {\n" +
                                                                                   "        sort(new Object[0]);\n" +
                                                                                   "        sort(new ArrayList());\n" +
                                                                                   "    }\n" +
                                                                                   "}\n" +
                                                                                   ""));
    }

    public void testAttributeDefaultValue() throws Exception {
        performUnusedImportsTest(new FileDescription("test/AttributeDefaultValue.java", "package test;\n" +
                                                                                        "\n" +
                                                                                        "import java.util.List;\n" +
                                                                                        "\n" +
                                                                                        "public class AttributeDefaultValue {\n" +
                                                                                        "\n" +
                                                                                        "    public static @interface StopAt {\n" +
                                                                                        "        Class value() default StopAtCurrentClass.class;\n" +
                                                                                        "        Class valuex() default List.class;\n" +
                                                                                        "    }\n" +
                                                                                        "    \n" +
                                                                                        "    private static interface StopAtCurrentClass {}\n" +
                                                                                        "\n" +
                                                                                        "}\n" +
                                                                                        ""));
    }

    public void testStaticImport189226() throws Exception {
        performUnusedImportsTest(new FileDescription("test/StaticImport189226.java", "package test;\n" +
                                                                                     "\n" +
                                                                                     "import javax.swing.GroupLayout.Group;\n" +
                                                                                     "import static javax.swing.GroupLayout.*;\n" +
                                                                                     "\n" +
                                                                                     "class StaticImport189226 {\n" +
                                                                                     "\n" +
                                                                                     "    Group g;\n" +
                                                                                     "    int s = PREFERRED_SIZE;\n" +
                                                                                     "}\n" +
                                                                                     ""));
    }

    public void testImportDisambiguation203874() throws Exception {
        performUnusedImportsTest(new FileDescription("test/ImportDisambiguation.java", "package test;\n" +
                                                                                       "\n" +
                                                                                       "import java.util.List;\n" +
                                                                                       "import java.util.*;\n" +
                                                                                       "import java.awt.*;\n" +
                                                                                       "\n" +
                                                                                       "public class ImportDisambiguation {\n" +
                                                                                       "\n" +
                                                                                       "    List l = new ArrayList();\n" +
                                                                                       "    Component c;\n" +
                                                                                       "}\n" +
                                                                                       ""));
    }
    
    public void testImportsNeededForJavadoc() throws Exception {
        performUnusedImportsTest(new FileDescription("test/ImportDisambiguation.java",
                                                     "package javaapplication9;\n" +
                                                     "import java.util.ArrayList;\n" +
                                                     "import java.util.Collections;\n" +
                                                     "|import java.util.LinkedList;|\n" +
                                                     "import java.util.List;\n" +
                                                     "/**\n" +
                                                     " * {@link List}\n" +
                                                     " *\n" +
                                                     " * @author lahvac\n" +
                                                     " */\n" +
                                                     "public class ImportsNeededForJavadoc {\n" +
                                                     "    /**\n" +
                                                     "     * {@link Collections#sort(java.util.List) }\n" +
                                                     "     */\n" +
                                                     "    private void mt1() {}\n" +
                                                     "    /**\n" +
                                                     "     * {@link ArrayList}\n" +
                                                     "     */\n" +
                                                     "    private int ft1;\n" +
                                                     "}"));
    }
    
    public void testImportsNeededForJavadoc2() throws Exception {
        performUnusedImportsTest(new FileDescription("test/ImportDisambiguation.java",
                                                     "package javaapplication9;\n" +
                                                     "import java.util.Collections;\n" +
                                                     "import java.util.List;\n" +
                                                     "public class ImportsNeededForJavadoc {\n" +
                                                     "    /**\n" +
                                                     "     * {@link Collections#sort(List) }\n" +
                                                     "     */\n" +
                                                     "    private void mt1() {}\n" +
                                                     "}"));
    }

    public UnusedImportsTest(String name) {
        super(name);
    }

    protected final void copyToWorkDir(File resource, File toFile) throws IOException {
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

    protected void performUnusedImportsTest(FileDescription... files) throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[]{"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[]{new MIMEResolverImpl()});

        FileObject scratch = SourceUtilsTestUtil.makeScratchDir(this);
        FileObject cache = scratch.createFolder("cache");
        FileObject src = scratch.createFolder("src");
        FileObject mainFile = null;
        List<int[]> goldenSpans = new ArrayList<int[]>();

        for (FileDescription testFile : files) {
            FileObject testFO = FileUtil.createData(src, testFile.fileName);
            String code = testFile.code;

            if (mainFile == null) {
                mainFile = testFO;
                String[] split = code.split(Pattern.quote("|"));
                StringBuilder stripped = new StringBuilder();
                int c = 0;

                for (int i = 0; i + 1 < split.length; i += 2) {
                    goldenSpans.add(new int[]{c += split[i].length(), c += split[i + 1].length()});
                    stripped.append(split[i]);
                    stripped.append(split[i + 1]);
                }
                
                stripped.append(split[split.length - 1]);
                
                code = stripped.toString();
            }

            TestUtilities.copyStringToFile(testFO, code);
        }

        assertNotNull(mainFile);

        if (sourceLevel != null) {
            SourceUtilsTestUtil.setSourceLevel(mainFile, sourceLevel);
        }

        SourceUtilsTestUtil.prepareTest(src, FileUtil.createFolder(scratch, "test-build"), cache);

        JavaSource source = JavaSource.forFileObject(mainFile);

        assertNotNull(source);

        final List<int[]> realSpans = new ArrayList<int[]>();

        source.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws IOException {
                parameter.toPhase(Phase.UP_TO_DATE);

                parameter.getSnapshot().getSource().getDocument(true);//XXX
                
                for (TreePath unused : new UnusedImports().process(parameter, new AtomicBoolean())) {
                    realSpans.add(new int[]{
                                (int) parameter.getTrees().getSourcePositions().getStartPosition(unused.getCompilationUnit(), unused.getLeaf()),
                                (int) parameter.getTrees().getSourcePositions().getEndPosition(unused.getCompilationUnit(), unused.getLeaf())
                    });
                }
            }
        }, true);

        realSpans.sort((int[] o1, int[] o2) -> o1[0] - o2[0]);
        
        int[][] goldenSpansArray = goldenSpans.toArray(new int[0][]);
        int[][] realSpansArray = realSpans.toArray(new int[0][]);
        
        assertTrue(Arrays.deepToString(goldenSpansArray) + " was: " + Arrays.deepToString(realSpansArray), Arrays.deepEquals(goldenSpansArray, realSpansArray));
    }

    private String sourceLevel;

    protected final void setSourceLevel(String sourceLevel) {
        this.sourceLevel = sourceLevel;
    }

    static class MIMEResolverImpl extends MIMEResolver {

        public String findMIMEType(FileObject fo) {
            if ("java".equals(fo.getExt())) {
                return "text/x-java";
            } else {
                return null;
            }
        }
    }

    public static final class FileDescription {

        final String fileName;
        final String code;

        public FileDescription(String fileName, String code) {
            this.fileName = fileName;
            this.code = code;
        }
    }
}
