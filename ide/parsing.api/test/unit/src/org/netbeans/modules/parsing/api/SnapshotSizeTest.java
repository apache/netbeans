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

package org.netbeans.modules.parsing.api;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.TestFileUtils;

/**
 *
 * @author Tomas Zezula
 */
public class SnapshotSizeTest extends ParsingTestBase {

    private static final String EXT = "foo";    //NOI18N
    private static final String MIME = "text/x-foo";    //NOI18N
    private static final String MIME_INNER = "text/x-inner";    //NOI18N
    private static final String FILE_CONTENT = "0123456789";    //NOI18N
    
    private FileObject file1;
    private FileObject file2;

    public SnapshotSizeTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockMimeLookup.setLookup(MimePath.get(MIME),
            Lookups.fixed(
                new FooEmbeddingProviderFactory(),
                new FooParserFactory()));
        FileUtil.setMIMEType(EXT, MIME);
        file1 = FileUtil.toFileObject(TestFileUtils.writeFile(
            new File(getWorkDir(),"test1.foo"),  //NOI18N
            FILE_CONTENT));
        file2 = FileUtil.toFileObject(TestFileUtils.writeFile(
            new File(getWorkDir(),"test2.foo"),  //NOI18N
            FILE_CONTENT+FILE_CONTENT));
        setBigFileSize(FILE_CONTENT.length());
    }

    @Override
    protected void tearDown() throws Exception {
        setBigFileSize(-1);
        super.tearDown(); //To change body of generated methods, choose Tools | Templates.
    }



    public void testBigFile() throws Exception {
        assertNotNull(file1);
        Source s = Source.create(file1);
        Snapshot snapShot = s.createSnapshot();
        assertEquals(FILE_CONTENT, snapShot.getText().toString());
        assertNotNull(file2);
        s = Source.create(file2);
        snapShot = s.createSnapshot();
        assertEquals("", snapShot.getText().toString());
    }

    public void testBigEmbeddingFromSmallFile() throws Exception {
        assertNotNull(file1);
        Source s = Source.create(file1);
        Snapshot snapShot = s.createSnapshot();
        assertEquals(FILE_CONTENT, snapShot.getText().toString());
        ParserManager.parse(Collections.singleton(s), new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                int i = 0;
                for (Embedding e : resultIterator.getEmbeddings()) {
                    switch (i) {
                        case 0:
                            assertEquals(FILE_CONTENT, e.getSnapshot().getText().toString());
                            break;
                        case 1:
                            assertEquals("", e.getSnapshot().getText().toString());
                            break;
                        default:
                            throw new AssertionError(i);
                    }
                    i = i + 1;
                }
                assertEquals(2, i);
            }
        });
    }

    private static void setBigFileSize(int size) {
        if (size >= 0) {
            System.setProperty("parse.max.file.size", Integer.toString(size));  //NOI18N
            assertEquals(Utilities.getMaxFileSize(), size);
        } else {
            System.getProperties().remove("parse.max.file.size");   //NOI18N
        }
    }

    public static final class FooEmbeddingProviderFactory extends TaskFactory {

        public FooEmbeddingProviderFactory() {}

        private static class FooEmbeddingProvider extends EmbeddingProvider {

            private FooEmbeddingProvider() {}

            @Override
            public List<Embedding> getEmbeddings(Snapshot snapshot) {
                return Arrays.asList(
                    snapshot.create(snapshot.getText(), MIME_INNER),
                    snapshot.create(FILE_CONTENT+FILE_CONTENT, MIME_INNER));
            }

            @Override
            public int getPriority() {
                return 1;
            }

            @Override
            public void cancel() {
            }
        }

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            return Collections.singleton(new FooEmbeddingProvider());
        }
    }

    public static final class FooParserFactory extends ParserFactory {

        private static class FooParser extends Parser {

            private volatile Snapshot snapshot;

            private FooParser(@NonNull Collection<? extends Snapshot> snapshots) {
                
            }

            @Override
            public void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                this.snapshot = snapshot;
            }

            @Override
            public Result getResult(Task task) throws ParseException {
                assert snapshot != null;
                return new Result(snapshot) {
                    @Override
                    protected void invalidate() {
                    }
                };
            }

            @Override
            public void addChangeListener(ChangeListener changeListener) {
            }

            @Override
            public void removeChangeListener(ChangeListener changeListener) {
            }
        }

        @Override
        public Parser createParser(Collection<Snapshot> snapshots) {
            return new FooParser(snapshots);
        }

    }

}
