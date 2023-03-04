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
import javax.swing.event.ChangeListener;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
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
public class VyletelaZezulickaTest extends IndexingAwareTestCase {
    
    public VyletelaZezulickaTest (String testName) {
        super (testName);
    }

    @Override
    protected Class[] getServices() {
        return new Class[] { MyScheduler.class };
    }
    
    

    public void testEmbedding () throws Exception {
        
        // 1) register tasks and parsers
        final Counter counter = new Counter (8);
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), 
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {                                                            
                    return new Parser () {
                        
                        private Snapshot last;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            last = snapshot;
                        }

                        public Result getResult (Task task) throws ParseException {
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
    
                        public String toString () {
                            return "FooParser";
                        }
                    };
                }
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new ParserResultTask () {

                            boolean done = false;
                            
                            public void run (Result result, SchedulerEvent event) {
                                if (!done) {
                                    counter.check ("text/foo", result.getSnapshot().getMimeType ());
                                    counter.check (1);
                                    counter.wait (4);
                                    try {
                                        final String original = result.getSnapshot().getText ().toString ();
                                        ParserManager.parse (
                                            Collections.<Source>singleton (result.getSnapshot().getSource ()), 
                                            new UserTask() {
                                                public void run (ResultIterator resultIterator) throws Exception {
                                                    Result result = resultIterator.getParserResult (1);
                                                    counter.check (original, result.getSnapshot().getText ().toString ());
                                                    counter.check (5);
                                                }
                                            }
                                        );
                                        counter.check (6);
                                    } catch (ParseException ex) {
                                    }
                                }
                                done = true;
                            }

                            public int getPriority () {
                                return 100;
                            }

                            public Class<? extends Scheduler> getSchedulerClass () {
                                return MyScheduler.class;
                            }

                            public void cancel () {
                            }
    
                            public String toString () {
                                return "FooParserResultTask " + getPriority ();
                            }
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

        MyScheduler.schedule2 (source, new ASchedulerEvent ());

        counter.wait (2);

        outputStream = testFile.getOutputStream ();
        writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci2 file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();

        counter.check (3);
        
        ParserManager.parse (
            Collections.<Source> singleton (source),
            new UserTask () {
                public void run (ResultIterator resultIterator) throws Exception {
                    Result result = resultIterator.getParserResult (1);
                    counter.check ("Toto je testovaci2 file, na kterem se budou delat hnusne pokusy!!!", result.getSnapshot().getText ().toString ());
                    counter.wait (7);
                }
            }
        );
        
        counter.wait (8);
        assertEquals (null, counter.errorMessage (true));
    }
}







