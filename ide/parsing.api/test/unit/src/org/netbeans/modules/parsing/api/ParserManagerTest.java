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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author tom
 */
public class ParserManagerTest extends ParsingTestBase {

    public ParserManagerTest (String name) {
        super (name);
    }

    @Override
    public void setUp () throws Exception {
        super.setUp();
        clearWorkDir ();
        // 1) register tasks and parsers
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), new FooParserFactory());
    }

    public void testParseCache () throws Exception {
        final boolean[] called = new boolean[] {false};
        FooParser.getResultCount = 0;
        FooParser.parseCount = 0;
        FooParserFactory.createParserCount = 0;
        ParserManager.parse ("text/foo", new UserTask () {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                called[0]=true;
            }
        });
        assertTrue(called[0]);
        assertEquals(1, FooParserFactory.createParserCount);
        assertEquals(1, FooParser.parseCount);
        assertEquals(1, FooParser.getResultCount);
        called[0] = false;
        ParserManager.parse("text/foo", new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                called[0]=true;
            }
        });
        assertTrue(called[0]);
        assertEquals(1, FooParserFactory.createParserCount);
        assertEquals(2, FooParser.parseCount);
        assertEquals(2, FooParser.getResultCount);

        try {
            byte[] dummy = new byte[(int) Runtime.getRuntime().maxMemory()];
        } catch (Throwable e) {
            // Ignore OME
        }
        System.gc(); System.gc();


        called[0] = false;
        ParserManager.parse("text/foo", new UserTask() {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                called[0]=true;
            }
        });
        assertTrue(called[0]);
        assertEquals(2, FooParserFactory.createParserCount);
        assertEquals(3, FooParser.parseCount);
        assertEquals(3, FooParser.getResultCount);


    }

    public void testParseDoesNotScheduleTasks () throws Exception {
        final CountDownLatch l = new CountDownLatch(1);
        MockServices.setServices (MockMimeLookup.class, TestEnvironmentFactory.class, MyScheduler.class);
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), new FooParserFactory(),
                        new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new ParserResultTask() {
                            @Override
                            public void run(Result result, SchedulerEvent event) {
                                l.countDown();
                            }
                            @Override
                            public int getPriority() {
                                return 100;
                            }
                            @Override
                            public Class<? extends Scheduler> getSchedulerClass() {
                                return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
                            }
                            @Override
                            public void cancel() {}
                        }
                    });
                }
        });

        clearWorkDir ();
        //Collection c = MimeLookup.getLookup("text/boo").lookupAll (ParserFactory.class);
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla.foo");
        FileUtil.setMIMEType ("foo", "text/foo");
        OutputStream outputStream = testFile.getOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        Source source = Source.create (testFile);

        ParserManager.parse (Collections.singleton(source), new UserTask () {
            @Override
            public void run(ResultIterator resultIterator) throws Exception {
            }
        });

        DataObject.find(testFile).getLookup().lookup(EditorCookie.class).openDocument();

        assertFalse("Should not schedule the task", l.await(2, TimeUnit.SECONDS));
    }

    private static class FooParser extends Parser {

        static int getResultCount = 0;
        
        static int parseCount = 0;

        private Snapshot last;

        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
            parseCount++;
            last = snapshot;
        }

        public Result getResult (Task task) throws ParseException {
            getResultCount++;
            return new Result (last) {
                public void invalidate () {
                }
            };
        }

        public void cancel () {
        }

        public void addChangeListener (ChangeListener changeListener) {
        }

        public void removeChangeListener (ChangeListener changeListener) {
        }

        public @Override String toString () {
            return "FooParser";
        }
    }

    private static class FooParserFactory extends ParserFactory {

        static int createParserCount = 0;

        public Parser createParser (Collection<Snapshot> snapshots2) {
            createParserCount++;
            return new FooParser();
        }
    }

}
