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
package org.netbeans.modules.parsing.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.IndexingAwareTestCase;
import org.netbeans.modules.parsing.api.MyScheduler;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.Scheduler;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author hanz
 */
public class CachingTest extends IndexingAwareTestCase {
    
    public CachingTest (String testName) {
        super (testName);
    }

    @Override
    protected Class[] getServices() {
        return new Class[] { MyScheduler.class };
    }
    
    

    @RandomlyFails // usually fails for jglick
    public void testCaching () throws Exception {

        // 1) register tasks and parsers
        final CountDownLatch        latch1 = new CountDownLatch (1);
        final CountDownLatch        latch2 = new CountDownLatch (3);

        final int[]                 fooParser = {1};
        final int[]                 fooParserResult = {1};
        final int[]                 fooEmbeddingProvider = {1};
        final int[]                 fooTask = {1};
        final int[]                 booParser = {1};
        final int[]                 booParserResult = {1};
        final int[]                 booTask = {1};
        final TestComparator        test = new TestComparator (
            "1 - schedule SchedulerEvent 1\n" +
            "foo get embeddings 1 (Snapshot 1), \n" +
            "foo parse 1 (Snapshot 1, FooParserResultTask 1, SourceModificationEvent -1:-1), \n" +
            "foo get result 1 (FooParserResultTask 1), \n" +
            "foo task 1 (FooResult 1 (Snapshot 1), SchedulerEvent 1), \n" +
            "foo invalidate 1, \n" +
            "boo parse 1 (Snapshot 2, BooParserResultTask 1, SourceModificationEvent -1:-1), \n" +
            "boo get result 1 (BooParserResultTask 1), \n" +
            "boo task 1 (BooResult 1 (Snapshot 2), SchedulerEvent 1), \n" +
            "boo invalidate 1, \n" +
            "2 - run user task\n" +
            "foo get result 1 (MyUserTask), \n" +
            "user task (Snapshot 1, FooResult 2 (Snapshot 1)), \n" +
            "boo get result 1 (MyUserTask), \n" +
            "user task - embedding text/boo (Snapshot 2, BooResult 2 (Snapshot 2)), \n" +
            "false, \n" +
            "foo invalidate 2, \n" +
            "boo invalidate 2, \n" +
            "3 - schedule SchedulerEvent 2\n" +
            "foo get embeddings 1 (Snapshot 1), \n" + //XXX:DELETE!
            "foo get result 1 (FooParserResultTask 1), \n" +
            "foo task 1 (FooResult 3 (Snapshot 1), SchedulerEvent 2), \n" +
            "foo invalidate 3, \n" +
            "boo get result 1 (BooParserResultTask 1), \n" +
            "boo task 1 (BooResult 3 (Snapshot 2), SchedulerEvent 2), \n" +
            "boo invalidate 3, \n" +
            "4 - end\n"
        );

        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"),
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        private Snapshot last;
                        private int i = fooParser [0]++;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            test.check ("foo parse " + i + " (Snapshot " + test.get (snapshot) + ", " + task + ", " + event + "), \n");
                            last = snapshot;

                        }

                        public Result getResult (Task task) throws ParseException {
                            test.check ("foo get result " + i + " (" + task + "), \n");
                            return new Result (last) {

                                private int i = fooParserResult [0]++;

                                public void invalidate () {
                                    test.check ("foo invalidate " + i + ", \n");
                                }

                                @Override
                                public String toString () {return "FooResult " + i + " (Snapshot " + test.get (getSnapshot ()) + ")";}
                            };
                        }

                        public void cancel () {}

                        public void addChangeListener (ChangeListener changeListener) {}

                        public void removeChangeListener (ChangeListener changeListener) {}

                        @Override public String toString () {return "FooParser";}
                    };
                }
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new EmbeddingProvider() {

                            private int i = fooEmbeddingProvider [0]++;

                            public List<Embedding> getEmbeddings (Snapshot snapshot) {
                                test.check ("foo get embeddings " + i + " (Snapshot " + test.get (snapshot) + "), \n");
                                Embedding embedding = snapshot.create (10, 10, "text/boo");
                                test.get (embedding.getSnapshot ());
                                return Arrays.asList (new Embedding[] {
                                    embedding
                                });
                            }

                            public int getPriority () {return 10;}

                            public void cancel () {}

                            @Override public String toString () {return "FooEmbeddingProvider " + getPriority ();}
                        },
                        new ParserResultTask () {

                            private int i = fooTask [0]++;

                            public void run (Result result, SchedulerEvent event) {
                                test.check ("foo task " + i + " (" + result + ", SchedulerEvent " + test.get (event) + "), \n");
                            }

                            public int getPriority () {
                                return 100;
                            }

                            public Class<? extends Scheduler> getSchedulerClass () {
                                return MyScheduler.class;
                            }

                            public void cancel () {
                            }

                            @Override
                            public String toString () {
                                return "FooParserResultTask " + i;
                            }
                        }

                    });
                }
            }

        );
        MockMimeLookup.setInstances (
            MimePath.get ("text/boo"),
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        private Snapshot last;
                        private int i = booParser [0]++;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            test.check ("boo parse " + i + " (Snapshot " + test.get (snapshot) + ", " + task + ", " + event + "), \n");
                            last = snapshot;
                        }

                        public Result getResult (Task task) throws ParseException {
                            test.check ("boo get result " + i + " (" + task + "), \n");
                            return new Result (last) {

                                private int i = booParserResult [0]++;

                                public void invalidate () {
                                    test.check ("boo invalidate " + i + ", \n");
                                    latch1.countDown ();
                                    latch2.countDown ();
                                }

                                @Override public String toString () {return "BooResult " + i + " (Snapshot " + test.get (getSnapshot ()) + ")";}
                            };
                        }

                        public void cancel () {}

                        public void addChangeListener (ChangeListener changeListener) {}

                        public void removeChangeListener (ChangeListener changeListener) {}

                        @Override public String toString () {return "BooParser";}
                    };
                }
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new ParserResultTask () {

                            private int i = booTask [0]++;

                            public void run (Result result, SchedulerEvent event) {
                                test.check ("boo task " + i + " (" + result + ", SchedulerEvent " + test.get (event) + "), \n");
                            }

                            public int getPriority () {return 150;}

                            public Class<? extends Scheduler> getSchedulerClass () {return MyScheduler.class;}

                            public void cancel () {}

                            @Override public String toString () {return "BooParserResultTask " + i;}
                        }
                    });
                }
            }
        );

        // 2) create source file
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
        SchedulerEvent event1 = new ASchedulerEvent ();
        test.check ("1 - schedule SchedulerEvent " + test.get (event1) + "\n");

        MyScheduler.schedule2 (source, event1);
        latch1.await ();
        test.check ("2 - run user task\n");

        ParserManager.parse (
            Collections.<Source>singleton (source),
            new UserTask () {
                @Override
                public void run (ResultIterator resultIterator) throws Exception {
                    test.check ("user task (Snapshot " + test.get (resultIterator.getSnapshot ()) + ", " + resultIterator.getParserResult () + "), \n");
                    Iterator<Embedding> it = resultIterator.getEmbeddings ().iterator ();
                    Embedding embedding = it.next ();
                    ResultIterator resultIterator1 = resultIterator.getResultIterator (embedding);
                    test.check ("user task - embedding " + embedding.getMimeType () + " (Snapshot " + test.get (resultIterator1.getSnapshot ()) + ", " + resultIterator1.getParserResult () + "), \n");
                    test.check ("" + it.hasNext () + ", \n");
                }
                @Override public String toString () {return "MyUserTask";}
            }
        );
        SchedulerEvent event2 = new ASchedulerEvent ();
        test.check ("3 - schedule SchedulerEvent " + test.get (event2) + "\n");

        MyScheduler.schedule2 (source, event2);
        latch2.await ();
        test.check ("4 - end\n");
        assertEquals ("", test.getResult ());
    }


    public void testPreservesEmbeddingOrder() {
        final String mimeType = "text/x-" + getName();
        final String embeddedMimeTypeFirst = "text/x-embedded-first-" + getName();
        final AtomicReference<EmbeddingProvider> ep1Ref = new AtomicReference<EmbeddingProvider>();
        final TaskFactory tf1 = new TaskFactory() {
            public @Override Collection<? extends SchedulerTask> create(Snapshot snapshot) {
                EmbeddingProvider ep1 = ep1Ref.get();
                if (ep1 == null) {
                    ep1 = new EmbeddingProvider() {
                    public @Override List<Embedding> getEmbeddings(Snapshot snapshot) {
                        List<Embedding> embeddings = new ArrayList<Embedding>();
                        embeddings.add(snapshot.create("1", embeddedMimeTypeFirst));
                        embeddings.add(snapshot.create("2", embeddedMimeTypeFirst));
                        embeddings.add(snapshot.create("3", embeddedMimeTypeFirst));
                        return embeddings;
                    }

                    public @Override int getPriority() {
                        return Integer.MAX_VALUE;
                    }

                    public @Override void cancel() {
                    }
                    };
                    ep1Ref.set(ep1);
                }
                return Collections.singleton(ep1);
            }
        };
        final String embeddedMimeTypeSecond = "text/x-embedded-second" + getName();
        final TaskFactory tf2 = new TaskFactory() {
            public @Override Collection<? extends SchedulerTask> create(Snapshot snapshot) {
                return Collections.singleton(new EmbeddingProvider() {
                    public @Override List<Embedding> getEmbeddings(Snapshot snapshot) {
                        List<Embedding> embeddings = new ArrayList<Embedding>();
                        embeddings.add(snapshot.create("4", embeddedMimeTypeSecond));
                        embeddings.add(snapshot.create("5", embeddedMimeTypeSecond));
                        embeddings.add(snapshot.create("6", embeddedMimeTypeSecond));
                        return embeddings;
                    }

                    public @Override int getPriority() {
                        return Integer.MIN_VALUE;
                    }

                    public @Override void cancel() {
                    }
                });
            }
        };
        MockMimeLookup.setInstances(MimePath.parse(mimeType),tf1, tf2);
        final EditorKit kit = new DefaultEditorKit();
        final Document doc = kit.createDefaultDocument();
        doc.putProperty("mimeType", mimeType);

        final Source source = Source.create(doc);
        final SourceCache hanz = SourceAccessor.getINSTANCE().getCache(source);
        Iterable<? extends Embedding> embeddings = hanz.getAllEmbeddings();
        assertEquals(Arrays.asList(new Integer[]{4,5,6,1,2,3}),asContentNumber(embeddings));
        final EmbeddingProvider toRefresh = ep1Ref.get();
        assertNotNull(toRefresh);
        hanz.refresh(toRefresh, toRefresh.getSchedulerClass());
        embeddings = hanz.getAllEmbeddings();
        assertEquals(Arrays.asList(new Integer[]{4,5,6,1,2,3}),asContentNumber(embeddings));
    }

    private Collection<? extends Integer> asContentNumber(final Iterable<? extends Embedding> embeddings) {
        final Collection<Integer> result = new ArrayList<Integer>();
        for (Embedding e : embeddings) {
            result.add(Integer.parseInt(e.getSnapshot().getText().toString()));
        }
        return result;
    }
}







