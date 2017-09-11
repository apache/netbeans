/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.parsing.api;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import javax.swing.event.ChangeListener;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author hanz
 */
public class RunUserActionTaskTest extends ParsingTestBase {
    
    public RunUserActionTaskTest (String testName) {
        super (testName);
    }

    /**
     * Creates one embedding and calls user task on it. Tests ordering of calls:
     * 1) EmbeddingProvider.getEmbeddings
     * 2) ParserFactory.createParser
     * 3) Parser.parse
     * 4) Parser.getResult
     * 5) run user action task
     * 6) continue...
     */
    public void testEmbedding () throws Exception {
        
        // 1) register tasks and parsers
        final Counter counter = new Counter ();
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), 
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    counter.check (2);
                    return Arrays.asList (new SchedulerTask[] {
                        new EmbeddingProvider() {
                            public List<Embedding> getEmbeddings (Snapshot snapshot) {
                                assertEquals ("text/foo", snapshot.getMimeType ());
                                counter.check (3);
                                return Arrays.asList (new Embedding[] {
                                    snapshot.create (10, 10, "text/boo")
                                });
                            }

                            public int getPriority () {
                                return 10;
                            }

                            public void cancel () {
                            }
                        }
                    });
                }
            }
        );
        final Snapshot[] snapshots = new Snapshot [1];
        MockMimeLookup.setInstances (
            MimePath.get ("text/boo"), 
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    assertEquals (1, snapshots2.size ());
                    snapshots[0] = snapshots2.iterator ().next ();
                    assertEquals ("text/boo", snapshots[0].getMimeType ());
                    assertEquals ("stovaci fi", snapshots[0].getText ().toString ());
                    counter.check (4);
                    return new Parser () {
                        
                        private Snapshot last;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            assertEquals (snapshot, snapshots [0]);
                            counter.check (5);
                            last = snapshot;
                        }

                        public Result getResult (Task task) throws ParseException {
                            counter.check (6);
                            return new Result (last) {
                                public void invalidate () {
                                    counter.check (8);
                                }
                            };
                        }

                        public void cancel () {
                        }

                        public void addChangeListener (ChangeListener changeListener) {
                        }

                        public void removeChangeListener (ChangeListener changeListener) {
                        }
                    };
                }
            }
        );
        
        // 2) create source file
        clearWorkDir ();
        FileUtil.setMIMEType ("foo", "text/foo");
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla.foo");
        OutputStream outputStream = testFile.getOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        Source source = Source.create (testFile);
        
        // 3) call user action task
        counter.check (1);
        ParserManager.parse (Collections.<Source>singleton (source), new UserTask() {
            public void run (ResultIterator resultIterator) throws Exception {
                resultIterator.getParserResult (15);
                counter.check (7);
            }
        });
        counter.check (9);
    }

    /**
     * Calls two user action tasks on the same souurce (unchanged), and tests
     * if Parser.parse method is calle only once.
     */
    public void testCachingOfTopLevelParser () throws Exception {

        // 1) register tasks and parsers
        final Counter counter = new Counter ();
        final Snapshot[] snapshots = new Snapshot [1];
        final Parser[] parser = new Parser [1];
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), 
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    assertEquals (1, snapshots2.size ());
                    snapshots[0] = snapshots2.iterator ().next ();
                    assertEquals ("text/foo", snapshots[0].getMimeType ());
                    counter.check (2);
                    Parser p = new Parser () {
                        
                        private Snapshot last;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            assertEquals (snapshot, snapshots [0]);
                            assertEquals (parser [0], this);
                            counter.check (3);
                            last = snapshot;
                        }

                        Stack<Integer> s1 = new Stack<Integer> ();
                        {s1.push (8);s1.push (4);}
                        
                        Stack<Integer> s2 = new Stack<Integer> ();
                        {s2.push (10);s2.push (6);}

                        public Result getResult (Task task) throws ParseException {
                            assertEquals (parser [0], this);
                            counter.check (s1.pop ());
                            return new Result (last) {
                                public void invalidate () {
                                    counter.check (s2.pop ());
                                }
                            };
                        }

                        public void cancel () {
                        }

                        public void addChangeListener (ChangeListener changeListener) {
                        }

                        public void removeChangeListener (ChangeListener changeListener) {
                        }
                    };
                    parser[0] = p;
                    return p;
                }
            }
        );

        // 2) create source file
        clearWorkDir ();
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla.foo");
        FileUtil.setMIMEType ("foo", "text/foo");
        OutputStream outputStream = testFile.getOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        Source source = Source.create (testFile);

        // 3) call user action task
        counter.check (1);
        ParserManager.parse (Collections.<Source>singleton (source), new UserTask() {
            public void run (ResultIterator resultIterator) throws Exception {
                resultIterator.getParserResult (15);
                counter.check (5);
            }
        });

        // 4) call user action task again
        counter.check (7); 
        ParserManager.parse (Collections.<Source>singleton (source), new UserTask() {
            public void run (ResultIterator resultIterator) throws Exception {
                resultIterator.getParserResult (15);
                counter.check (9); 
            }
        });
        counter.check (11); 
    }
    
    /**
     * Creates simple source with one embedded block. Calls two user action tasks
     * for some position in the inner language. Tests, if Parser.parse methods
     * are called only once.
     * 
     * @throws java.lang.Exception
     */
    public void testCachingOfSecondLevelParser () throws Exception {
        
        // 1) register tasks and parsers
        final Counter counter = new Counter ();
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), 
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new EmbeddingProvider() {
                            public List<Embedding> getEmbeddings (Snapshot snapshot) {
                                assertEquals ("text/foo", snapshot.getMimeType ());
                                counter.check (2);
                                return Arrays.asList (new Embedding[] {
                                    snapshot.create (10, 10, "text/boo")
                                });
                            }

                            public int getPriority () {
                                return 10;
                            }

                            public void cancel () {
                            }
                        }
                    });
                }
            }
        );
        final Snapshot[] snapshots = new Snapshot [1];
        MockMimeLookup.setInstances (
            MimePath.get ("text/boo"), 
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    assertEquals (1, snapshots2.size ());
                    snapshots[0] = snapshots2.iterator ().next ();
                    assertEquals ("text/boo", snapshots[0].getMimeType ());
                    assertEquals ("stovaci fi", snapshots[0].getText ().toString ());
                    counter.check (3);
                    return new Parser () {
                        
                        private Snapshot last;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            assertEquals (snapshot, snapshots [0]);
                            counter.check (4);
                        }

                        Stack<Integer> s1 = new Stack<Integer> ();
                        {s1.push (9);s1.push (5);}

                        Stack<Integer> s2 = new Stack<Integer> ();
                        {s2.push (11);s2.push (7);}
                        
                        public Result getResult (Task task) throws ParseException {
                            counter.check (s1.pop ());
                            return new Result (last) {
                                public void invalidate () {
                                    counter.check (s2.pop ());
                                }
                            };
                        }

                        public void cancel () {
                        }

                        public void addChangeListener (ChangeListener changeListener) {
                        }

                        public void removeChangeListener (ChangeListener changeListener) {
                        }
                    };
                }
            }
        );

        // 2) create source file
        clearWorkDir ();
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla.foo");
        FileUtil.setMIMEType ("foo", "text/foo");
        OutputStream outputStream = testFile.getOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        Source source = Source.create (testFile);

        // 3) call user action task
        counter.check (1);
        ParserManager.parse (Collections.<Source>singleton (source), new UserTask() {
            public void run (ResultIterator resultIterator) throws Exception {
                resultIterator.getParserResult (15);
                counter.check (6);
            }
        });

        // 4) call user action task again
        counter.check (8);
        ParserManager.parse (Collections.<Source>singleton (source), new UserTask() {
            public void run (ResultIterator resultIterator) throws Exception {
                resultIterator.getParserResult (15);
                counter.check (10);
            }
        });
        counter.check (12);
    }
    
    /**
     * Creates simple file with one embedding. Calls user task two times on this
     * source, and tests if Parser.parse method is called only one time. Than
     * it rewrites source file, and tests if cache is changed, and Parser.parse 
     * method is called once more.
     * 
     * @throws java.lang.Exception
     */
    public void testCachingOfSecondLevelParserAfterChange () throws Exception {
        
        // 1) register tasks and parsers
        final Counter counter = new Counter ();
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), 
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new EmbeddingProvider() {
                            Stack<Integer> s1 = new Stack<Integer> ();
                            {s1.push (13);s1.push (2);}
                            
                            public List<Embedding> getEmbeddings (Snapshot snapshot) {
                                assertEquals ("text/foo", snapshot.getMimeType ());
                                counter.check (s1.pop ());
                                return Arrays.asList (new Embedding[] {
                                    snapshot.create (10, 10, "text/boo")
                                });
                            }

                            public int getPriority () {
                                return 10;
                            }

                            public void cancel () {
                            }
                        }
                    });
                }
            }
        );
        final Snapshot[] snapshots = new Snapshot [1];
        MockMimeLookup.setInstances (
            MimePath.get ("text/boo"), 
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    assertEquals (1, snapshots2.size ());
                    snapshots[0] = snapshots2.iterator ().next ();
                    assertEquals ("text/boo", snapshots[0].getMimeType ());
                    assertEquals ("stovaci fi", snapshots[0].getText ().toString ());
                    counter.check (3);
                    return new Parser () {
                        
                        private Snapshot last;

                        Stack<Integer> s3 = new Stack<Integer> ();
                        {s3.push (14);s3.push (4);}
                        Stack<String> s4 = new Stack<String> ();
                        {s4.push ("stovaci2 f");s4.push ("stovaci fi");}
                        
                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            counter.check (s3.pop ());
                            last = snapshot;
                        }

                        Stack<Integer> s1 = new Stack<Integer> ();
                        {s1.push (15);s1.push (9);s1.push (5);}

                        Stack<Integer> s2 = new Stack<Integer> ();
                        {s2.push (17);s2.push (11);s2.push (7);}
                        
                        public Result getResult (Task task) throws ParseException {
                            counter.check (s1.pop ());
                            return new Result (last) {
                                public void invalidate () {
                                    counter.check (s2.pop ());
                                }
                            };
                        }

                        public void cancel () {
                        }

                        public void addChangeListener (ChangeListener changeListener) {
                        }

                        public void removeChangeListener (ChangeListener changeListener) {
                        }
                    };
                }
            }
        );
        
        // 2) create source file
        clearWorkDir ();
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla.foo");
        FileUtil.setMIMEType ("foo", "text/foo");
        OutputStream outputStream = testFile.getOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
//        outputStream.close ();
        writer.close ();
        Source source = Source.create (testFile);

        // 3) call user action task
        counter.check (1);
        final String text[] = new String [1];
        ParserManager.parse (Collections.<Source>singleton (source), new UserTask() {
            public void run (ResultIterator resultIterator) throws Exception {
                Result result = resultIterator.getParserResult (15);
                counter.check (6);
                text[0] = result.getSnapshot().getText ().toString ();
            }
        });

        // 4) call user action task again
        counter.check (8);
        ParserManager.parse (Collections.<Source>singleton (source), new UserTask() {
            public void run (ResultIterator resultIterator) throws Exception {
                Result result = resultIterator.getParserResult (15);
                counter.check (10);
                assertEquals (text [0], result.getSnapshot().getText ().toString ());
            }
        });
        
        try {
            synchronized (this) {
                wait (1000);
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace ();
        }

        // 5) change file
        counter.check (12);
        outputStream = testFile.getOutputStream ();
        writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci2 file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        
        // 6) call user action task
        ParserManager.parse (Collections.<Source>singleton (source), new UserTask() {
            public void run (ResultIterator resultIterator) throws Exception {
                Result result = resultIterator.getParserResult (15);
                counter.check (16);
                assertNotSame (text [0], result.getSnapshot().getText ().toString ());
            }
        });
        counter.check (18);
    }
    
    private static class Counter {
        private int counter = 1;
        
        void check (int count) {
            assertEquals (count, counter++);
        }
    }
}







