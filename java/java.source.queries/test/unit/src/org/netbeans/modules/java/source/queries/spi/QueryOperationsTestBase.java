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
package org.netbeans.modules.java.source.queries.spi;
import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import junit.framework.TestCase;
import org.netbeans.modules.java.source.queries.api.Function;
import org.netbeans.modules.java.source.queries.api.Queries;
import org.netbeans.modules.java.source.queries.api.QueryException;
import org.netbeans.modules.java.source.queries.api.Updates;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
public abstract class QueryOperationsTestBase extends TestCase {

    protected static final String[] TEST_1 = {
        "org.me.test",
        "Test1",
        "package org.me.test;\n"+
        "import java.util.Map;\n"+
        "public class Test1{\n"+
        "    int a, b;\n"+
        "    float c\n;"+
        "    Test1 d\n;"+
        "    java.util.List e;\n"+
        "    Map<String,String> f;\n"+
        "}"
    };

    protected static final String[] TEST_2 = {
        "org.me.test",
        "Test2",
        "package org.me.test;\n"+
        "public class Test2 extends Exception implements Runnable {\n"+
        "    public Test2(int a){}\n"+
        "    public void run(){}\n"+
        "}\n"+
        "class Test2Other implements Runnable,java.io.Serializable{\n" +
        "    public void run(){}\n"+
        "}"
    };

    protected static final String[] TEST_3 = {
        "org.me.test",
        "Test3",
        "package org.me.test;\n"+
        "public class Test3{\n"+
        "    Test3(){}\n"+
        "}"
    };

    protected static final String[] TEST_4 = {
        "org.me.test",
        "Test4",
        "package org.me.test;\n"+
        "import java.util.Map;\n"+
        "public class Test4{\n"+
        "    void m1() {}\n"+
        "    void m2(int a){}\n"+
        "    int m3(int a, int b){}\n"+
        "    java.util.List m4(Map<String,String>a, Map<String,String>b){}\n"+
        "    java.util.List<String> m5(Map<String,java.util.List<String>>a, String b){}\n"+
        "    void m6(int... a){}\n"+
        "    int m7(int... a){}\n" +
        "}"
    };

    protected static final String[] TEST_5 = {
        "org.me.test",
        "Test5",
        "package org.me.test;\n"+
        "import java.util.Map;\n"+
        "public class Test5{\n"+
        "    void m1(){\n}\n"+
        "    @SuppressWarnings void m1(int a){\n}\n"+
        "    /**\n" +
        "     Some comment*/\n"+
        "    public final @SuppressWarnings @Override int m2(int a, int b){}\n"+
        "    final public void m2(){}}"
    };

    protected static final String[] TEST_6 = {
        "org.me.test",
        "Test6",
        "package org.me.test;\n"+
        "public class Test6{\n"+
        "    int var_a;\n"+
        "void test() {\n"+
        "var_a = var_a + 1;"+
        "}\n"+
        "}"
    };

    protected static final String[] TEST_7 = {
        "org.me.test",
        "Test7",
        "package org.me.test;\n"+
        "public class Test7{\n"+
        "    java.util.Map<String,java.lang.String> m1 = new java.util.HashMap<String,java.lang.String>();\n"+
        "    java.util.Map<String,java.lang.String> m2 = new java.util.HashMap<String,java.lang.String>();\n"+
        "    public void run() {\n"+
        "        final java.util.Map<String,String> res = new java.util.HashMap<String,String>(m1);\n"+
        "    }\n"+
        "    public java.util.Map<String,String> call(final java.util.List<String> l){\n"+
        "        final java.util.Map<String,String> res = new java.util.HashMap<String,String>(m1);\n"+
        "        final javax.swing.ComboBoxModel cbm = new javax.swing.DefaultComboBoxModel(java.util.Arrays.copyOf(l.toArray(), l.size()));\n"+
        "        return res;\n"+
        "    }\n"+
        "}"
    };

    private static final String GOLDEN_7 =
        "package org.me.test;\n"+
        "\n"+
        "import java.util.Arrays;\n"+
        "import java.util.HashMap;\n"+
        "import java.util.List;\n"+
        "import java.util.Map;\n"+
        "import javax.swing.ComboBoxModel;\n"+
        "import javax.swing.DefaultComboBoxModel;\n"+
        "\n"+
        "public class Test7{\n"+
        "    Map<String, String> m1 = new HashMap<String, String>();\n"+
        "    Map<String, String> m2 = new HashMap<String, String>();\n"+
        "    public void run() {\n"+
        "        final java.util.Map<String,String> res = new java.util.HashMap<String,String>(m1);\n"+
        "    }\n"+
        "    public Map<String,String> call(final List<String> l){\n"+
        "        final Map<String,String> res = new HashMap<String, String>(m1);\n"+
        "        final ComboBoxModel cbm = new DefaultComboBoxModel(Arrays.copyOf(l.toArray(), l.size()));\n"+
        "        return res;\n"+
        "    }\n"+
        "}";

    protected FileObject srcRoot;

    public QueryOperationsTestBase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        this.clearWorkDir();
        File f = new File(getWorkDir(), "src");
        f.mkdirs();
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(f);
        srcRoot = lfs.getRoot();
    }

    public void testGetTopLevelClasses() throws Exception {
        doTestGetTopLevelClasses(TEST_1, Arrays.asList("org.me.test.Test1"));
        doTestGetTopLevelClasses(TEST_2, Arrays.asList("org.me.test.Test2","org.me.test.Test2Other"));
    }

    private void doTestGetTopLevelClasses(
            final String[] testCase,
            final Collection<? extends String> expected) throws Exception {
        final URL fo1 = prepareTest(
            srcRoot,
            testCase[0],
            testCase[1],
            testCase[2]);
        final Collection<? extends String> tlc = Queries.query(
            fo1,
            new Function<Queries, Collection<? extends String>>(){
                @Override
                public Collection<? extends String> apply(Queries param) throws QueryException {
                    return param.getTopLevelClasses();
                }
            });
        assertContentEquals(expected,tlc);
    }

    public void testGetSuperClass() throws Exception {
        doTestGetSuperClass(TEST_1, "org.me.test.Test1", "java.lang.Object");
        doTestGetSuperClass(TEST_2, "org.me.test.Test2", "java.lang.Exception");
        doTestGetSuperClass(TEST_3, "java.lang.RuntimeException", "java.lang.Exception");
        doTestGetSuperClass(TEST_3, "java.lang.Object", null);
    }

    private void doTestGetSuperClass(
            final String[] testCase,
            final String fqn,
            final String expected) throws Exception {
        final URL file = prepareTest(
            srcRoot,
            testCase[0],
            testCase[1],
            testCase[2]);
        final String res = Queries.query(
            file,
            new Function<Queries, String>(){
                @Override
                public String apply(Queries param) throws QueryException {
                    return param.getSuperClass(fqn);
                }
            });
        assertEquals(expected, res);
    }

    public void testGetInterfaces() throws Exception {
        doTestGetInterfaces(TEST_1, "org.me.test.Test1", Collections.<String>emptyList());
        doTestGetInterfaces(TEST_2, "org.me.test.Test2", Collections.singleton("java.lang.Runnable"));
        doTestGetInterfaces(TEST_2, "org.me.test.Test2Other",
            Arrays.asList("java.lang.Runnable","java.io.Serializable"));
    }

    private void doTestGetInterfaces(
            final String[] testCase,
            final String fqn,
            final Collection< ? extends String> expected) throws Exception {
        final URL file = prepareTest(
            srcRoot,
            testCase[0],
            testCase[1],
            testCase[2]);
        final Collection< ? extends String> res = Queries.query(
            file,
            new Function<Queries, Collection<? extends String>>(){
                @Override
                public Collection<? extends String> apply(Queries param) throws QueryException {
                    return param.getInterfaces(fqn);
                }
            });
        assertContentEquals(expected, res);
    }

    public void testGetBinaryName() throws Exception {
        doTestGetBinaryName(TEST_1,"org.me.test.Test1","org.me.test.Test1");
        doTestGetBinaryName(TEST_2,"org.me.test.Test2Other","org.me.test.Test2Other");
        doTestGetBinaryName(TEST_2,"java.util.Map","java.util.Map");
        doTestGetBinaryName(TEST_2,"java.util.Map.Entry","java.util.Map$Entry");
    }

    private void doTestGetBinaryName(
            final String[] testCase,
            final String fqn,
            final String expected) throws Exception {
        final URL file = prepareTest(
            srcRoot,
            testCase[0],
            testCase[1],
            testCase[2]);
        final String res = Queries.query(
            file,
            new Function<Queries, String>(){
                @Override
                public String apply(Queries param) throws QueryException {
                    return param.getClassBinaryName(fqn);
                }
            });
        assertEquals(expected, res);
    }

    public void testGetFieldNames() throws Exception {
        doTestGetFieldNames(TEST_1,"org.me.test.Test1", false, "org.me.test.Test1", Collections.singletonList("d"));
        doTestGetFieldNames(TEST_1,"org.me.test.Test1", false, "float", Collections.singletonList("c"));
        doTestGetFieldNames(TEST_1,"org.me.test.Test1", false, "int", Arrays.asList("a","b"));
        doTestGetFieldNames(TEST_1,"org.me.test.Test1", false, "java.util.List", Collections.singletonList("e"));
        doTestGetFieldNames(TEST_1,"org.me.test.Test1", true, "java.util.List", Collections.singletonList("e"));
        doTestGetFieldNames(TEST_1,"org.me.test.Test1", false, "java.util.List<java.lang.String>", Collections.<String>emptyList());
        doTestGetFieldNames(TEST_1,"org.me.test.Test1", true, "java.util.List<java.lang.String>", Collections.singletonList("e"));
        doTestGetFieldNames(TEST_1,"org.me.test.Test1", false, "java.util.Map", Collections.<String>emptyList());
        doTestGetFieldNames(TEST_1,"org.me.test.Test1", true, "java.util.Map", Collections.singletonList("f"));
        doTestGetFieldNames(TEST_1,"org.me.test.Test1", false, "java.util.Map<java.lang.String,java.lang.String>", Collections.singletonList("f"));
        doTestGetFieldNames(TEST_1,"org.me.test.Test1", false, null, Arrays.asList("a","b","c","d","e","f"));
    }

    private void doTestGetFieldNames(
            final String[] testCase,
            final String fqn,
            final boolean useRT,
            final String type,
            final Collection<? extends String> expected) throws Exception {
        final URL file = prepareTest(
            srcRoot,
            testCase[0],
            testCase[1],
            testCase[2]);
        final Collection<? extends String> res = Queries.query(
            file,
            new Function<Queries, Collection<? extends String>>(){
                @Override
                public Collection< ? extends String> apply(Queries param) throws QueryException {
                    return param.getFieldNames(fqn, useRT, type);
                }
            });
        assertContentEquals(expected, res);
    }

    public void testGetMethodNames() throws Exception {
        doTestGetMethodNames(TEST_4,"org.me.test.Test4", false,
                "void",
                Collections.<String>emptyList(),
                Collections.singletonList("m1"));
        doTestGetMethodNames(TEST_4,"org.me.test.Test4", false,
                "void",
                Collections.<String>singletonList("int"),
                Collections.singletonList("m2"));
        doTestGetMethodNames(TEST_4,"org.me.test.Test4", false,
                "int",
                Arrays.asList(new String[]{"int","int"}),
                Collections.singletonList("m3"));
        doTestGetMethodNames(TEST_4,"org.me.test.Test4", true,
                "java.util.List",
                Arrays.asList("java.util.Map","java.util.Map"),
                Collections.singletonList("m4"));
        doTestGetMethodNames(TEST_4,"org.me.test.Test4", false,
                "java.util.List",
                Arrays.asList("java.util.Map","java.util.Map"),
                Collections.<String>emptyList());
        doTestGetMethodNames(TEST_4,"org.me.test.Test4", false,
                "java.util.List<java.lang.String>",
                Arrays.asList("java.util.Map<java.lang.String,java.lang.String>","java.util.Map<java.lang.String,java.lang.String>"),
                Collections.<String>emptyList());
        doTestGetMethodNames(TEST_4,"org.me.test.Test4", false,
                "java.util.List",
                Arrays.asList("java.util.Map<java.lang.String,java.lang.String>","java.util.Map<java.lang.String,java.lang.String>"),
                Collections.singletonList("m4"));
        doTestGetMethodNames(TEST_4,"org.me.test.Test4", true,
                "java.util.List",
                Arrays.asList("java.util.Map","java.lang.String"),
                Collections.singletonList("m5"));
        doTestGetMethodNames(TEST_4,"org.me.test.Test4", false,
                "java.util.List",
                Arrays.asList("java.util.Map","java.lang.String"),
                Collections.<String>emptyList());
        doTestGetMethodNames(TEST_4,"org.me.test.Test4", false,
                "java.util.List",
                Arrays.asList("java.util.Map<java.lang.String,java.util.List<java.lang.String>>","java.lang.String"),
                Collections.<String>emptyList());
        doTestGetMethodNames(TEST_4,"org.me.test.Test4", false,
                "java.util.List<java.lang.String>",
                Arrays.asList("java.util.Map","java.lang.String"),
                Collections.<String>emptyList());
        doTestGetMethodNames(TEST_4,"org.me.test.Test4", false,
                "java.util.List<java.lang.String>",
                Arrays.asList("java.util.Map<java.lang.String,java.util.List<java.lang.String>>","java.lang.String"),
                Collections.singletonList("m5"));
        doTestGetMethodNames(TEST_4,"org.me.test.Test4", false,
                "void",
                Collections.singletonList("int[]"),
                Collections.singletonList("m6"));
        doTestGetMethodNames(TEST_4,"org.me.test.Test4", false,
                null,
                Collections.singletonList("int[]"),
                Arrays.asList("m6","m7"));
                doTestGetMethodNames(TEST_4,"org.me.test.Test4", false,
                "void",
                null,
                Arrays.asList("m1","m2","m6"));
        doTestGetMethodNames(TEST_4,"org.me.test.Test4", false,
                null,
                null,
                Arrays.asList("m1","m2","m3","m4","m5","m6","m7"));
    }

    private void doTestGetMethodNames(
            final String[] testCase,
            final String fqn,
            final boolean useRT,
            final String retType,
            final List<? extends String> paramTypes,
            final Collection<? extends String> expected) throws Exception {
        final URL file = prepareTest(
            srcRoot,
            testCase[0],
            testCase[1],
            testCase[2]);
        final Collection<? extends String> res = Queries.query(
            file,
            new Function<Queries, Collection<? extends String>>(){
                @Override
                public Collection< ? extends String> apply(Queries param) throws QueryException {
                    return param.getMethodNames(
                        fqn,
                        useRT,
                        retType,
                        paramTypes == null ? null : paramTypes.toArray(new String[0]));
                }
            });
        assertContentEquals(expected, res);
    }

    public void testGetMethodSpan() throws Exception {
        doTestGetMethodSpan(TEST_5,"org.me.test.Test5","m1", false,
                "void",
                Collections.<String>emptyList(),
                new int[] {67,79});
        doTestGetMethodSpan(TEST_5,"org.me.test.Test5", "m1", false,
                "void",
                Collections.singletonList("int"),
                new int[] {84,119});
        doTestGetMethodSpan(TEST_5,"org.me.test.Test5", "m2",false,
                "int",
                Arrays.asList("int","int"),
                new int[] {124,215});
    }

    private void doTestGetMethodSpan(
            final String[] testCase,
            final String fqn,
            final String methodName,
            final boolean useRT,
            final String retType,
            final List<? extends String> paramTypes,
            final int[] expected) throws Exception {
        final URL file = prepareTest(
            srcRoot,
            testCase[0],
            testCase[1],
            testCase[2]);
        final int[] res = Queries.query(
            file,
            new Function<Queries, int[]>(){
                @Override
                public int[] apply(Queries param) throws QueryException {
                    return param.getMethodSpan(
                        fqn,
                        methodName,
                        useRT,
                        retType,
                        paramTypes == null ? null : paramTypes.toArray(new String[0]));
                }
            });
        assertEquals(expected, res);
    }

    public void testModifyInterfaces() throws Exception {
        doTestModifyInterfaces(TEST_5,"org.me.test.Test5",
                Collections.singletonList("java.lang.Runnable"),
                Collections.<String>emptySet(),
                Collections.singletonList("java.lang.Runnable"));
        doTestModifyInterfaces(TEST_5,"org.me.test.Test5",
                Collections.singletonList("java.io.Serializable"),
                Collections.<String>emptySet(),
                Arrays.asList("java.lang.Runnable","java.io.Serializable"));
        doTestModifyInterfaces(TEST_5,"org.me.test.Test5",
                Collections.singletonList("java.io.Externalizable"),
                Collections.singletonList("java.io.Serializable"),
                Arrays.asList("java.lang.Runnable","java.io.Externalizable"));
        doTestModifyInterfaces(TEST_5,"org.me.test.Test5",
                Collections.<String>emptySet(),
                Arrays.asList("java.lang.Runnable","java.io.Externalizable"),
                Collections.<String>emptySet());
    }

    private void doTestModifyInterfaces(
            final String[] testCase,
            final String clz,
            final Collection<? extends String> toAdd,
            final Collection<? extends String> toRemove,
            final Collection<? extends String> expected
            ) throws Exception {
        final URL file = prepareTest(
            srcRoot,
            testCase[0],
            testCase[1],
            testCase[2]);
        Updates.update(
            file,
            new Function<Updates, Boolean>(){
                @Override
                public Boolean apply(final Updates param) throws QueryException {
                        param.modifyInterfaces(
                            clz,
                            toAdd,
                            toRemove);
                        return true;
                }
            });
        final Collection< ? extends String> res = Queries.query(
            file,
            new Function<Queries, Collection<? extends String>>(){
                @Override
                public Collection<? extends String> apply(Queries param) throws QueryException {
                    return param.getInterfaces(clz);
                }
            });
        assertContentEquals(expected, res);
    }

    public void testSetSuperClass() throws Exception {
        doTestSetSuperClass(TEST_5,"org.me.test.Test5",
                "java.util.ArrayList",
                "java.util.ArrayList");
        doTestSetSuperClass(TEST_5,"org.me.test.Test5",
                "java.lang.Object",
                "java.lang.Object");
    }

    private void doTestSetSuperClass(
        final String[] testCase,
        final String clz,
        final String superClz,
        final String expected) throws Exception {
        final URL file = prepareTest(
            srcRoot,
            testCase[0],
            testCase[1],
            testCase[2]);
        Updates.update(
            file,
            new Function<Updates, Boolean>(){
                @Override
                public Boolean apply(final Updates param) throws QueryException {
                    param.setSuperClass(
                        clz,
                        superClz);
                    return true;
                }
            });
        final String res = Queries.query(
            file,
            new Function<Queries, String>(){
                @Override
                public String apply(Queries param) throws QueryException {
                    return param.getSuperClass(clz);
                }
            });
        assertEquals(expected, res);
    }

    public void testRenameField() throws Exception {
        doTestRenameField(TEST_6,"org.me.test.Test6",
                "var_a",
                "var_b");
    }

    private void doTestRenameField(
            final String[] testCase,
            final String fqn,
            final String oldFieldName,
            final String newFieldName) throws Exception {
        final URL file = prepareTest(
            srcRoot,
            testCase[0],
            testCase[1],
            testCase[2]);
        Updates.update(
            file,
            new Function<Updates, Boolean>(){
                @Override
                public Boolean apply(final Updates param) throws QueryException {
                    param.renameField(
                        fqn,
                        oldFieldName,
                        newFieldName);
                    return true;
                }
            });
        final Collection<? extends String> res = Queries.query(
            file,
            new Function<Queries, Collection<? extends String>>(){
                @Override
                public Collection<? extends String> apply(Queries param) throws QueryException {
                    return param.getFieldNames(fqn, true, null);
                }
            });
        assertContentEquals(Collections.singleton(newFieldName), res);
        assertFalse(readFile(file).contains(oldFieldName));
    }

    public void testFixImports() throws Exception {
        doTestFixImports(TEST_7,new int[][]{{45,236},{358,684}},GOLDEN_7);
    }

    private void doTestFixImports(
            final String[] testCase,
            final int[][] ranges,
            final String expected) throws Exception {
        final URL file = prepareTest(
            srcRoot,
            testCase[0],
            testCase[1],
            testCase[2]);
        Updates.update(
            file,
            new Function<Updates, Boolean>(){
                @Override
                public Boolean apply(final Updates param) throws QueryException {
                    param.fixImports(ranges);
                    return true;
                }
            });
        assertEquals(
            removeWhiteSpaces(expected),
            removeWhiteSpaces(readFile(file)));
    }

    protected final <T> void assertContentEquals(
            final Collection<? extends T> expected,
            final Collection<? extends T> result) {
        if (expected == null) {
            assertNull("Expected null but got: " + result ,result);
        } else {
            final Set<T> e = new HashSet<T>(expected);
            for (T r : result) {
                if (!e.remove(r)) {
                    throw new AssertionError("Expected: " + expected +" got:" + result);
                }
            }
            if (!e.isEmpty()) {
                throw new AssertionError("Expected: " + expected +" got:" + result);
            }
        }
    }

    protected final void assertEquals(int[] expected, int[] result) {
        assertEquals("Expected: " + Arrays.toString(expected) + " got: " + Arrays.toString(result),
            expected.length,
            result.length);
        for (int i=0; i< expected.length; i++) {
            assertEquals("Expected: " + Arrays.toString(expected) + " got: " + Arrays.toString(result),
                expected[i],
                result[i]);
        }
    }

    protected final URL prepareTest(
            final FileObject root,
            final String pkg,
            final String name,
            final String content) throws IOException {
        if (Lookup.getDefault().lookup(QueriesController.class) == null) {
            throw new IllegalStateException("Run the ModelOperationsTest subclass in impl module.");
        }
        assert root != null;
        assert pkg != null;
        assert name != null;
        assert content != null;
        final String fileName = String.format("%s/%s.java",
            pkg.replace('.', '/'),
            name);
        FileObject fo = root.getFileObject(fileName);
        if (fo != null) {
            return fo.getURL();
        }
        fo = FileUtil.createData(
            srcRoot,
            fileName);
        final FileLock lock = fo.lock();
        try {
            final PrintWriter out = new PrintWriter (new OutputStreamWriter(fo.getOutputStream(lock)));
            try {
                out.print(content);
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
        return fo.getURL();
    }

    protected final void clearWorkDir() throws IOException {
        final File workDir = getWorkDir();
        deleteSubFiles (workDir);
    }

    private void deleteSubFiles(final File folder) throws IOException {
        final File[] children = folder.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteFiles(child);
            }
        }
    }

    private void deleteFiles (final File file) throws IOException {
        if (file.isDirectory()) {
            final File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteFiles(child);
                }
            }
        }
        file.delete();
    }

    private String readFile(URL url) throws IOException {
        FileReader fr = null;
        try {
            File f = new File(url.getFile());
            fr = new FileReader(f);
            char[] buf = new char[(int)f.length()];
            fr.read(buf);
            return new String(buf);
        } finally {
            if (fr != null) {
                fr.close();
            }
        }
    }

    protected abstract File getWorkDir() throws IOException;

    private static String removeWhiteSpaces(final String str) {
        final StringBuilder sb = new StringBuilder();
        for (int i=0; i< str.length(); i++) {
            final char c = str.charAt(i);
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
