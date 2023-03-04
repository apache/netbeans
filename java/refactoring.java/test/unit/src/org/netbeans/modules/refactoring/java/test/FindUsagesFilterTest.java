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
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.refactoring.java.WhereUsedElement;
import org.netbeans.modules.refactoring.java.plugins.FindUsagesVisitor;
import org.netbeans.modules.refactoring.java.spi.JavaWhereUsedFilters;
import static org.netbeans.modules.refactoring.java.test.RefactoringTestBase.writeFilesAndWaitForScan;
import org.openide.filesystems.FileObject;
import org.openide.util.Pair;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
public class FindUsagesFilterTest extends RefactoringTestBase {

    public FindUsagesFilterTest(String name) {
        super(name, "1.8");
    }
    
    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
    
    public void testSortedMapReadWrite() throws Exception {
        String source;
        writeFilesAndWaitForScan(src, new RefactoringTestBase.File("t/A.java", source = "package t;\n"
                + "import java.util.SortedMap;\n"
                + "import java.util.TreeMap;\n"
                + "public class A {\n"
                + "    \n"
                + "    public static void main(String... args) {\n"
                + "        SortedMap<String, String> lijst = new TreeMap<>();\n"
                + "        \n"
                + "        lijst.put(\"1\", \"2\");\n"
                + "        lijst.putAll(lijst);\n"
                + "        \n"
                + "        String a = lijst.firstKey();\n"
                + "        lijst.lastKey();\n"
                + "        lijst.containsKey(\"1\");\n"
                + "        lijst.containsValue(\"1\");\n"
                + "        lijst.get(\"1\");\n"
                + "        lijst.isEmpty();\n"
                + "        lijst.size();\n"
                + "        \n"
                + "        lijst.remove(\"1\");\n"
                + "        lijst.clear();\n"
                + "        \n"
                + "        lijst.equals(null);\n"
                + "        lijst = null;\n"
                + "    }\n"
                + "}\n"));
        performFind(src.getFileObject("t/A.java"), source.indexOf("lijst") + 1, true, false, false,
                Pair.of("lijst.put(\"1\", \"2\");", JavaWhereUsedFilters.ReadWrite.READ_WRITE),
                Pair.of("lijst.putAll(lijst);", JavaWhereUsedFilters.ReadWrite.WRITE),
                Pair.of("lijst.putAll(lijst);", (JavaWhereUsedFilters.ReadWrite)null),
                
                Pair.of("String a = lijst.firstKey();", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst.lastKey();", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst.containsKey(\"1\");", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst.containsValue(\"1\");", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst.get(\"1\");", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst.isEmpty();", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst.size();", JavaWhereUsedFilters.ReadWrite.READ),
                
                Pair.of("lijst.remove(\"1\");", JavaWhereUsedFilters.ReadWrite.WRITE),
                Pair.of("lijst.clear();", JavaWhereUsedFilters.ReadWrite.WRITE),
                
                Pair.of("lijst.equals(null);", (JavaWhereUsedFilters.ReadWrite)null),
                Pair.of("lijst = null;", (JavaWhereUsedFilters.ReadWrite)null));
    }
    
    public void testSortedSetReadWrite() throws Exception {
        String source;
        writeFilesAndWaitForScan(src, new RefactoringTestBase.File("t/A.java", source = "package t;\n"
                + "import java.util.SortedSet;\n"
                + "import java.util.TreeSet;\n"
                + "public class A {\n"
                + "    \n"
                + "    public static void main(String... args) {\n"
                + "        SortedSet<String> lijst = new TreeSet<>();\n"
                + "        \n"
                + "        lijst.add(\"1\");\n"
                + "        lijst.addAll(lijst);\n"
                + "        \n"
                + "        String a = lijst.first();\n"
                + "        lijst.last();\n"
                + "        lijst.contains(\"1\");\n"
                + "        lijst.containsAll(lijst);\n"
                + "        lijst.isEmpty();\n"
                + "        lijst.size();\n"
                + "        \n"
                + "        lijst.retainAll(lijst);\n"
                + "        lijst.remove(\"1\");\n"
                + "        lijst.removeAll(lijst);\n"
                + "        lijst.clear();\n"
                + "        \n"
                + "        lijst.equals(null);\n"
                + "        lijst = null;\n"
                + "    }\n"
                + "}\n"));
        performFind(src.getFileObject("t/A.java"), source.indexOf("lijst") + 1, true, false, false,
                Pair.of("lijst.add(\"1\");", JavaWhereUsedFilters.ReadWrite.WRITE),
                Pair.of("lijst.addAll(lijst);", JavaWhereUsedFilters.ReadWrite.WRITE),
                Pair.of("lijst.addAll(lijst);", (JavaWhereUsedFilters.ReadWrite)null),
                
                Pair.of("String a = lijst.first();", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst.last();", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst.contains(\"1\");", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst.containsAll(lijst);", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst.containsAll(lijst);", (JavaWhereUsedFilters.ReadWrite)null),
                Pair.of("lijst.isEmpty();", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst.size();", JavaWhereUsedFilters.ReadWrite.READ),
                
                Pair.of("lijst.retainAll(lijst);", JavaWhereUsedFilters.ReadWrite.WRITE),
                Pair.of("lijst.retainAll(lijst);", (JavaWhereUsedFilters.ReadWrite)null),
                Pair.of("lijst.remove(\"1\");", JavaWhereUsedFilters.ReadWrite.WRITE),
                Pair.of("lijst.removeAll(lijst);", JavaWhereUsedFilters.ReadWrite.WRITE),
                Pair.of("lijst.removeAll(lijst);", (JavaWhereUsedFilters.ReadWrite)null),
                Pair.of("lijst.clear();", JavaWhereUsedFilters.ReadWrite.WRITE),
                
                Pair.of("lijst.equals(null);", (JavaWhereUsedFilters.ReadWrite)null),
                Pair.of("lijst = null;", (JavaWhereUsedFilters.ReadWrite)null));
    }
    
    public void testListReadWrite() throws Exception {
        String source;
        writeFilesAndWaitForScan(src, new RefactoringTestBase.File("t/A.java", source = "package t;\n"
                + "import java.util.List;\n"
                + "import java.util.ArrayList;\n"
                + "public class A {\n"
                + "    \n"
                + "    public static void main(String... args) {\n"
                + "        List<String> lijst = new ArrayList<>();\n"
                + "        lijst.add(\"1\");\n"
                + "        lijst.add(0, \"2\");\n"
                + "        lijst.addAll(lijst);\n"
                + "        \n"
                + "        String a = lijst.get(0);\n"
                + "        lijst.contains(\"1\");\n"
                + "        lijst.isEmpty();\n"
                + "        lijst.size();\n"
                + "        lijst.indexOf(\"3\");\n"
                + "        \n"
                + "        lijst.set(1, \"5\");\n"
                + "        \n"
                + "        lijst.remove(\"1\");\n"
                + "        lijst.removeAll(lijst);\n"
                + "        lijst.clear();\n"
                + "        \n"
                + "        lijst.equals(null);\n"
                + "        lijst = null;\n"
                + "    }\n"
                + "}\n"));
        performFind(src.getFileObject("t/A.java"), source.indexOf("lijst") + 1, true, false, false,
                Pair.of("lijst.add(\"1\");", JavaWhereUsedFilters.ReadWrite.WRITE),
                Pair.of("lijst.add(0, \"2\");", JavaWhereUsedFilters.ReadWrite.WRITE),
                Pair.of("lijst.addAll(lijst);", JavaWhereUsedFilters.ReadWrite.WRITE),
                Pair.of("lijst.addAll(lijst);", (JavaWhereUsedFilters.ReadWrite)null),
                
                Pair.of("String a = lijst.get(0);", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst.contains(\"1\");", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst.isEmpty();", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst.size();", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst.indexOf(\"3\");", JavaWhereUsedFilters.ReadWrite.READ),
                
                Pair.of("lijst.set(1, \"5\");", JavaWhereUsedFilters.ReadWrite.READ_WRITE),
                
                Pair.of("lijst.remove(\"1\");", JavaWhereUsedFilters.ReadWrite.WRITE),
                Pair.of("lijst.removeAll(lijst);", JavaWhereUsedFilters.ReadWrite.WRITE),
                Pair.of("lijst.removeAll(lijst);", (JavaWhereUsedFilters.ReadWrite)null),
                Pair.of("lijst.clear();", JavaWhereUsedFilters.ReadWrite.WRITE),
                
                Pair.of("lijst.equals(null);", (JavaWhereUsedFilters.ReadWrite)null),
                Pair.of("lijst = null;", (JavaWhereUsedFilters.ReadWrite)null));
    }
    
    @SuppressWarnings("NestedAssignment")
    public void testArrayReadWrite() throws Exception {
        String source;
        writeFilesAndWaitForScan(src, new RefactoringTestBase.File("t/A.java", source = "package t;\n"
                + "public class A {\n"
                + "    \n"
                + "    public static void main(String... args) {\n"
                + "        int[] lijst = new int[10];\n"
                + "        lijst[0] = 1;\n"
                + "        int a = lijst[1];\n"
                + "        lijst[2]++;\n"
                + "        ++lijst[3];\n"
                + "        lijst[4] = lijst[5];\n"
                + "        lijst = null;\n"
                + "    }\n"
                + "}\n"));
        performFind(src.getFileObject("t/A.java"), source.indexOf("lijst") + 1, true, false, false,
                Pair.of("lijst[0] = 1;", JavaWhereUsedFilters.ReadWrite.WRITE),
                Pair.of("int a = lijst[1];", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst[2]++;", JavaWhereUsedFilters.ReadWrite.READ_WRITE),
                Pair.of("++lijst[3];", JavaWhereUsedFilters.ReadWrite.READ_WRITE),
                Pair.of("lijst[4] = lijst[5];", JavaWhereUsedFilters.ReadWrite.WRITE),
                Pair.of("lijst[4] = lijst[5];", JavaWhereUsedFilters.ReadWrite.READ),
                Pair.of("lijst = null;", JavaWhereUsedFilters.ReadWrite.WRITE));
    }
    
    @SuppressWarnings("null")
    private void performFind(FileObject source, final int absPos, final boolean searchInComments,
                             boolean inImport, boolean inComment, Pair<String, JavaWhereUsedFilters.ReadWrite>... expected) throws Exception {
        final FindUsagesVisitor[] r = new FindUsagesVisitor[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController javac) throws Exception {
                javac.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = javac.getCompilationUnit();

                TreePath tp = javac.getTreeUtilities().pathFor(absPos);
                Element el = javac.getTrees().getElement(tp);
                AtomicBoolean isCancelled = new AtomicBoolean();
                AtomicBoolean inImport = new AtomicBoolean();
                r[0] = new FindUsagesVisitor(javac, isCancelled, searchInComments, false, false, false, false, inImport);
                r[0].scan(cut, el);
            }
        }, true);
        
        assertEquals(inImport, r[0].isInImport());
        assertEquals(inComment, r[0].usagesInComments());
        final Collection<WhereUsedElement> elements = r[0].getElements();
        Iterator<WhereUsedElement> iterator = elements.iterator();
        for (int i = 0; i < expected.length; i++) {
            WhereUsedElement element = iterator.next();
            assertEquals(expected[i].first(), element.getText());
            assertEquals(element.getText(), expected[i].second(), element.getAccess());
        }
        assertFalse("Expected " + expected.length + " elements, but found more.", iterator.hasNext());
    }
}
