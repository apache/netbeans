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

package org.netbeans.api.java.queries;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Jesse Glick
 */
public class SourceLevelQueryTest extends NbTestCase {

    public SourceLevelQueryTest(String n) {
        super(n);
    }

    private static Map<FileObject, String> slq2Files  = new HashMap<FileObject, String>();
    private static String LEVEL;
    private FileObject f;

    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(SLQ.class,SLQ2.class);        
        f = FileUtil.createMemoryFileSystem().getRoot();
    }

    @Override
    protected void tearDown() throws Exception {
        LEVEL = null;
        slq2Files.clear();
        super.tearDown();
    }


    public void testBasicUsage() throws Exception {
        assertNull(SourceLevelQuery.getSourceLevel(f));
        LEVEL = "1.3";
        assertEquals("1.3", SourceLevelQuery.getSourceLevel(f));
        LEVEL = "1.5";
        assertEquals("1.5", SourceLevelQuery.getSourceLevel(f));
        MockServices.setServices();
        assertNull(SourceLevelQuery.getSourceLevel(f));
    }

    public void testRobustness() throws Exception {
        // #83994: should only return well-formed source levels.
        LEVEL = "${default.javac.source}";
        assertNull(SourceLevelQuery.getSourceLevel(f));
    }

    public void testRobustness2() throws Exception {
        // #83994: should only return well-formed source levels.
        slq2Files.put(f, "1.8");        //NOI18N
        assertEquals("1.8",SourceLevelQuery.getSourceLevel2(f).getSourceLevel());
        slq2Files.put(f, "8");          //NOI18N
        assertEquals("1.8",SourceLevelQuery.getSourceLevel2(f).getSourceLevel());
        slq2Files.put(f, "osm");        //NOI18N
        assertNull(SourceLevelQuery.getSourceLevel2(f).getSourceLevel());
        slq2Files.put(f, null);
        assertNull(SourceLevelQuery.getSourceLevel2(f).getSourceLevel());
    }

    public void testSLQ2() throws Exception {
        LEVEL = "1.3";
        FileObject f1 = f.createFolder("f1");   //NOI18N
        FileObject f2 = f.createFolder("f2");   //NOI18N
        assertEquals("1.3", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.3", SourceLevelQuery.getSourceLevel(f2));   //NOI18N
        slq2Files.put(f1, "1.5");   //NOI18N
        assertEquals("1.5", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.3", SourceLevelQuery.getSourceLevel(f2));   //NOI18N
        assertEquals("1.5", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel().toString());   //NOI18N
        assertTrue(SourceLevelQuery.getSourceLevel2(f1).supportsChanges());
        SourceLevelQuery.Result sourceLevel2 = SourceLevelQuery.getSourceLevel2(f2);
        assertEquals("1.3",sourceLevel2.getSourceLevel().toString());   //NOI18N
        assertFalse(sourceLevel2.supportsChanges());
        ChangeListener dummy = new ChangeListener() {@Override public void stateChanged(ChangeEvent e) {}};
        sourceLevel2.addChangeListener(dummy);
        sourceLevel2.removeChangeListener(dummy);
        LEVEL = "1.5";
        assertEquals("1.5",sourceLevel2.getSourceLevel().toString());   //NOI18N
    }
    
    public void testSynonyms() throws IOException {        
        final FileObject f1 = f.createFolder("f1");   //NOI18N
        //Test SLQ
        LEVEL = "1.5";    //NOI18N
        assertEquals("1.5", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.5", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        LEVEL = "5";    //NOI18N
        assertEquals("1.5", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.5", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        LEVEL = "1.6";    //NOI18N
        assertEquals("1.6", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.6", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        LEVEL = "6";    //NOI18N
        assertEquals("1.6", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.6", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        LEVEL = "1.7";    //NOI18N
        assertEquals("1.7", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.7", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        LEVEL = "7";    //NOI18N
        assertEquals("1.7", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.7", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        LEVEL = "1.8";    //NOI18N
        assertEquals("1.8", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.8", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        LEVEL = "8";    //NOI18N
        assertEquals("1.8", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.8", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        LEVEL = "1.9";
        assertEquals("9", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("9", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        LEVEL = "9";
        assertEquals("9", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("9", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        //Test SLQ2
        slq2Files.put(f1, "1.5");   //NOI18N
        assertEquals("1.5", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.5", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        slq2Files.put(f1, "5");   //NOI18N
        assertEquals("1.5", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.5", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        slq2Files.put(f1, "1.6");   //NOI18N
        assertEquals("1.6", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.6", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        slq2Files.put(f1, "6");   //NOI18N
        assertEquals("1.6", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.6", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        slq2Files.put(f1, "1.7");   //NOI18N
        assertEquals("1.7", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.7", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        slq2Files.put(f1, "7");   //NOI18N
        assertEquals("1.7", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.7", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        slq2Files.put(f1, "1.8");   //NOI18N
        assertEquals("1.8", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.8", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        slq2Files.put(f1, "8");   //NOI18N
        assertEquals("1.8", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("1.8", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        slq2Files.put(f1, "1.9");   //NOI18N
        assertEquals("9", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("9", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
        slq2Files.put(f1, "9");   //NOI18N
        assertEquals("9", SourceLevelQuery.getSourceLevel(f1));   //NOI18N
        assertEquals("9", SourceLevelQuery.getSourceLevel2(f1).getSourceLevel());   //NOI18N
    }

    public static final class SLQ implements SourceLevelQueryImplementation {

        public SLQ() {}

        public String getSourceLevel(FileObject javaFile) {
            return LEVEL;
        }
    }

    public static final class SLQ2 implements SourceLevelQueryImplementation2 {

        @Override
        public Result getSourceLevel(FileObject javaFile) {
            final String sl = slq2Files.get(javaFile);
            if (sl != null) {
                return new SourceLevelQueryImplementation2.Result() {
                    @Override
                    public String getSourceLevel() {
                        return sl;
                    }
                    @Override
                    public void addChangeListener(ChangeListener listener) {
                    }
                    @Override
                    public void removeChangeListener(ChangeListener listener) {
                    }
                };
            }
            return null;
        }

    }

}
