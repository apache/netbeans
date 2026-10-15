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
package org.netbeans.modules.parsing.api;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;


/**
 *
 * @author hanz
 */
public class SnapshotTest extends ParsingTestBase {

    public SnapshotTest (String testName) {
        super (testName);
    }            
//
//    public void testSnapshotEmbedding () throws IOException {
//        clearWorkDir ();
//        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
//        FileObject testFile = FileUtil.createData (workDir, "bla");
//        OutputStream outputStream = testFile.getOutputStream ();
//        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
//        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
//        writer.close ();
//        Source source = Source.create (testFile);
//        Snapshot originalSnapshot = source.createSnapshot ();
//        assertEquals (0, originalSnapshot.getOriginalOffset (0));
//        assertEquals (10, originalSnapshot.getOriginalOffset (10));
//        assertEquals(originalSnapshot.getText ().length (),originalSnapshot.getOriginalOffset (originalSnapshot.getText ().length ()));
//        try {
//            assertEquals(-1, originalSnapshot.getOriginalOffset (originalSnapshot.getText ().length ()+1));
// //            assert (false);
//        } catch (IndexOutOfBoundsException ex) {
//        }
//        assertEquals (0, originalSnapshot.getEmbeddedOffset (0));
//        assertEquals (10, originalSnapshot.getEmbeddedOffset (10));
// //        try {
// //            originalSnapshot.getEmbeddedOffset (originalSnapshot.getText ().length ());
// //            assert (false);
// //        } catch (ArrayIndexOutOfBoundsException ex) {
// //        }
//        assertEquals("stovaci fi", originalSnapshot.create (10, 10, "text/jedna").getSnapshot ().getText ());
//        assertEquals("1234567890", originalSnapshot.create ("1234567890", "text/jedna").getSnapshot ().getText ());
//
//        Embedding languageJednaEmbedding = Embedding.create (Arrays.asList (new Embedding[] {
//            originalSnapshot.create (10, 10, "text/jedna"),
//            originalSnapshot.create ("1234567890", "text/jedna"),
//            originalSnapshot.create (30, 10, "text/jedna"),
//        }));
//        assertEquals ("text/jedna", languageJednaEmbedding.getMimeType ());
//        Snapshot languageJednaSnapshot = languageJednaEmbedding.getSnapshot ();
//        assertEquals ("text/jedna", languageJednaSnapshot.getMimeType ());
//        assertEquals ("stovaci fi1234567890rem se bud", languageJednaSnapshot.getText ().toString ());
//        assertEquals (10, languageJednaSnapshot.getOriginalOffset (0));
//        assertEquals (12, languageJednaSnapshot.getOriginalOffset (2));
//        assertEquals (20, languageJednaSnapshot.getOriginalOffset (10));
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (11));
//        assertEquals (30, languageJednaSnapshot.getOriginalOffset (20));
//        assertEquals (33, languageJednaSnapshot.getOriginalOffset (23));
//        assertEquals (40, languageJednaSnapshot.getOriginalOffset (30));
// //        try {
//            assertEquals (-1, languageJednaSnapshot.getOriginalOffset (31));
// //            assert (false);
// //        } catch (IndexOutOfBoundsException ex) {
// //        }
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (0));
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (5));
//        assertEquals (0, languageJednaSnapshot.getEmbeddedOffset (10));
//        assertEquals (5, languageJednaSnapshot.getEmbeddedOffset (15));
//        assertEquals (10, languageJednaSnapshot.getEmbeddedOffset (20));
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (21));
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (25));
//        assertEquals (20, languageJednaSnapshot.getEmbeddedOffset (30));
//        assertEquals (25, languageJednaSnapshot.getEmbeddedOffset (35));
//        assertEquals (30, languageJednaSnapshot.getEmbeddedOffset (40));
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (41));
// //        try {
// //            languageJednaSnapshot.getEmbeddedOffset (50);
// //            assert (false);
// //        } catch (ArrayIndexOutOfBoundsException ex) {
// //        }
//
//        Embedding petaEmbedding = languageJednaSnapshot.create (5, 20, "text/peta");
//        Snapshot petaSnapshot = petaEmbedding.getSnapshot ();
//        assertEquals ("ci fi1234567890rem s", petaSnapshot.getText ().toString ());
//        assertEquals (15, petaSnapshot.getOriginalOffset (0));
//        assertEquals (18, petaSnapshot.getOriginalOffset (3));
//        assertEquals (20, petaSnapshot.getOriginalOffset (5));
//        assertEquals (-1, petaSnapshot.getOriginalOffset (6));
//        assertEquals (-1, petaSnapshot.getOriginalOffset (10));
//        assertEquals (30, petaSnapshot.getOriginalOffset (15));
//        assertEquals (34, petaSnapshot.getOriginalOffset (19));
//        assertEquals (35, petaSnapshot.getOriginalOffset (20));
//        try {
//            assertEquals (-1, petaSnapshot.getOriginalOffset (21));
// //            assert (false);
//        } catch (IndexOutOfBoundsException ex) {
//        }
//        assertEquals (-1, petaSnapshot.getEmbeddedOffset (0));
//        assertEquals (-1, petaSnapshot.getEmbeddedOffset (10));
//        assertEquals (0, petaSnapshot.getEmbeddedOffset (15));
//        assertEquals (4, petaSnapshot.getEmbeddedOffset (19));
//        assertEquals (5, petaSnapshot.getEmbeddedOffset (20));
//        assertEquals (-1, petaSnapshot.getEmbeddedOffset (21));
//        assertEquals (15, petaSnapshot.getEmbeddedOffset (30));
//        assertEquals (20, petaSnapshot.getEmbeddedOffset (35));
//        assertEquals (-1, petaSnapshot.getEmbeddedOffset (36));
//
//        Embedding fullSpanEmbedding = originalSnapshot.create (0, originalSnapshot.getText().length(), "text/peta");
//        Snapshot fullSpanSnapshot = fullSpanEmbedding.getSnapshot ();
//        assertEquals(originalSnapshot.getText().toString(), fullSpanSnapshot.getText().toString());
//    }
//
//    public void testSnapshotEmbedding2 () throws IOException { // see issue #154444
//        clearWorkDir ();
//        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
//        FileObject testFile = FileUtil.createData (workDir, "bla");
//        OutputStream outputStream = testFile.getOutputStream ();
//        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
//        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
//        writer.close ();
//        Source source = Source.create (testFile);
//        Snapshot originalSnapshot = source.createSnapshot ();
//        Embedding languageJednaEmbedding = Embedding.create (Arrays.asList (new Embedding[] {
//            originalSnapshot.create (10, 10, "text/jedna"),
//            originalSnapshot.create ("12345", "text/jedna"),
//            originalSnapshot.create ("67890", "text/jedna"),
//            originalSnapshot.create (30, 10, "text/jedna"),
//        }));
//        assertEquals ("text/jedna", languageJednaEmbedding.getMimeType ());
//        Snapshot languageJednaSnapshot = languageJednaEmbedding.getSnapshot ();
//        assertEquals ("text/jedna", languageJednaSnapshot.getMimeType ());
//        assertEquals ("stovaci fi1234567890rem se bud", languageJednaSnapshot.getText ().toString ());
//        assertEquals (10, languageJednaSnapshot.getOriginalOffset (0));
//        assertEquals (12, languageJednaSnapshot.getOriginalOffset (2));
//        assertEquals (20, languageJednaSnapshot.getOriginalOffset (10));
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (11));
//        assertEquals (30, languageJednaSnapshot.getOriginalOffset (20));
//        assertEquals (33, languageJednaSnapshot.getOriginalOffset (23));
//        assertEquals (40, languageJednaSnapshot.getOriginalOffset (30));
//        try {
//            languageJednaSnapshot.getOriginalOffset (31);
// //            assert (false);
//        } catch (IndexOutOfBoundsException ex) {
//        }
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (0));
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (5));
//        assertEquals (0, languageJednaSnapshot.getEmbeddedOffset (10));
//        assertEquals (5, languageJednaSnapshot.getEmbeddedOffset (15));
//        assertEquals (10, languageJednaSnapshot.getEmbeddedOffset (20));
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (21));
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (25));
//        assertEquals (20, languageJednaSnapshot.getEmbeddedOffset (30));
//        assertEquals (25, languageJednaSnapshot.getEmbeddedOffset (35));
//        assertEquals (30, languageJednaSnapshot.getEmbeddedOffset (40));
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (41));
// //        try {
// //            languageJednaSnapshot.getEmbeddedOffset (50);
// //            assert (false);
// //        } catch (ArrayIndexOutOfBoundsException ex) {
// //        }
//    }
//
//    public void testSnapshotEmbedding159626 () throws IOException { // see issue #154444
//        clearWorkDir ();
//        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
//        FileObject testFile = FileUtil.createData (workDir, "bla");
//        OutputStream outputStream = testFile.getOutputStream ();
//        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
//        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!! asdfghjklqwertyuio");
//        writer.close ();
//        Source source = Source.create (testFile);
//        Snapshot originalSnapshot = source.createSnapshot ();
//        //System.out.println (originalSnapshot.getText ().length ());
//        Embedding languageJednaEmbedding = Embedding.create (Arrays.asList (new Embedding[] {
//            originalSnapshot.create (0, 4, "text/jedna"),
//            originalSnapshot.create ("123", "text/jedna"),
//            originalSnapshot.create (24, 23, "text/jedna"),
//            originalSnapshot.create ("456", "text/jedna"),
//            originalSnapshot.create (67, 17, "text/jedna"),
//        }));
//        assertEquals ("text/jedna", languageJednaEmbedding.getMimeType ());
//        Snapshot languageJednaSnapshot = languageJednaEmbedding.getSnapshot ();
//        assertEquals ("text/jedna", languageJednaSnapshot.getMimeType ());
//        assertEquals ("Toto123na kterem se budou dela456sdfghjklqwertyuio", languageJednaSnapshot.getText ().toString ());
//
//        Embedding languageDvaEmbedding = Embedding.create (Arrays.asList (new Embedding[] {
//            languageJednaSnapshot.create (21, 17, "text/dva")
//        }));
//        assertEquals ("text/dva", languageDvaEmbedding.getMimeType ());
//        Snapshot languageDvaSnapshot = languageDvaEmbedding.getSnapshot ();
//        assertEquals ("text/dva", languageDvaSnapshot.getMimeType ());
//        assertEquals ("udou dela456sdfgh", languageDvaSnapshot.getText ().toString ());
//        assertEquals (38, languageDvaSnapshot.getOriginalOffset (0));
//        assertEquals (47, languageDvaSnapshot.getOriginalOffset (9));
//        assertEquals (-1, languageDvaSnapshot.getOriginalOffset (10));
//        assertEquals (67, languageDvaSnapshot.getOriginalOffset (12));
//        assertEquals (72, languageDvaSnapshot.getOriginalOffset (17));
//        assertEquals (-1, languageDvaSnapshot.getEmbeddedOffset (0));
//        assertEquals (-1, languageDvaSnapshot.getEmbeddedOffset (37));
//        assertEquals (0, languageDvaSnapshot.getEmbeddedOffset (38));
//        assertEquals (9, languageDvaSnapshot.getEmbeddedOffset (47));
//        assertEquals (-1, languageDvaSnapshot.getEmbeddedOffset (48));
//        assertEquals (-1, languageDvaSnapshot.getEmbeddedOffset (66));
//        assertEquals (12, languageDvaSnapshot.getEmbeddedOffset (67));
//        assertEquals (17, languageDvaSnapshot.getEmbeddedOffset (72));
//        assertEquals (-1, languageDvaSnapshot.getEmbeddedOffset (73));
//    }
//
//    public void testSnapshotEmbedding159927 () throws IOException { // see issue #154444
//        clearWorkDir ();
//        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
//        FileObject testFile = FileUtil.createData (workDir, "bla");
//        OutputStream outputStream = testFile.getOutputStream ();
//        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
//        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!! asdfghjklqwertyuio");
//        writer.close ();
//        Source source = Source.create (testFile);
//        Snapshot originalSnapshot = source.createSnapshot ();
//        //System.out.println (originalSnapshot.getText ().length ());
//        Embedding languageJednaEmbedding = Embedding.create (Arrays.asList (new Embedding[] {
//            originalSnapshot.create ("123", "text/jedna"),
//        }));
//        assertEquals ("text/jedna", languageJednaEmbedding.getMimeType ());
//        Snapshot languageJednaSnapshot = languageJednaEmbedding.getSnapshot ();
//        assertEquals ("text/jedna", languageJednaSnapshot.getMimeType ());
//        assertEquals ("123", languageJednaSnapshot.getText ().toString ());
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (0));
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (2));
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (5));
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (0));
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (10));
//
//        Embedding languageDvaEmbedding = Embedding.create (Arrays.asList (new Embedding[] {
//            languageJednaSnapshot.create (1, 2, "text/dva")
//        }));
//        assertEquals ("text/dva", languageDvaEmbedding.getMimeType ());
//        Snapshot languageDvaSnapshot = languageDvaEmbedding.getSnapshot ();
//        assertEquals ("text/dva", languageDvaSnapshot.getMimeType ());
//        assertEquals ("23", languageDvaSnapshot.getText ().toString ());
//        assertEquals (-1, languageDvaSnapshot.getOriginalOffset (0));
//        assertEquals (-1, languageDvaSnapshot.getOriginalOffset (2));
//        assertEquals (-1, languageDvaSnapshot.getOriginalOffset (5));
//        assertEquals (-1, languageDvaSnapshot.getEmbeddedOffset (0));
//        assertEquals (-1, languageDvaSnapshot.getEmbeddedOffset (10));
//    }
//
//    public void testSnapshotEmbedding160360 () throws IOException { // see issue #154444
//        clearWorkDir ();
//        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
//        FileObject testFile = FileUtil.createData (workDir, "bla");
//        OutputStream outputStream = testFile.getOutputStream ();
//        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
//        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!! asdfghjklqwertyuio");
//        writer.close ();
//        Source source = Source.create (testFile);
//        Snapshot originalSnapshot = source.createSnapshot ();
//        //System.out.println (originalSnapshot.getText ().length ());
//        Embedding languageJednaEmbedding = Embedding.create (Arrays.asList (new Embedding[] {
//            originalSnapshot.create ("123", "text/jedna"),
//            originalSnapshot.create (50, 20, "text/jedna"),
//            originalSnapshot.create ("456", "text/jedna"),
//        }));
//        assertEquals ("text/jedna", languageJednaEmbedding.getMimeType ());
//        Snapshot languageJednaSnapshot = languageJednaEmbedding.getSnapshot ();
//        assertEquals ("text/jedna", languageJednaSnapshot.getMimeType ());
//        assertEquals ("123nusne pokusy!!! asdf456", languageJednaSnapshot.getText ().toString ());
//
//        Embedding languageDvaEmbedding = Embedding.create (Arrays.asList (new Embedding[] {
//            languageJednaSnapshot.create (20, 5, "text/dva")
//        }));
//        assertEquals ("text/dva", languageDvaEmbedding.getMimeType ());
//        Snapshot languageDvaSnapshot = languageDvaEmbedding.getSnapshot ();
//        assertEquals ("text/dva", languageDvaSnapshot.getMimeType ());
//        assertEquals ("sdf45", languageDvaSnapshot.getText ().toString ());
//        assertEquals (67, languageDvaSnapshot.getOriginalOffset (0));
//        assertEquals (70, languageDvaSnapshot.getOriginalOffset (3));
//        assertEquals (-1, languageDvaSnapshot.getOriginalOffset (4));
//        assertEquals (-1, languageDvaSnapshot.getOriginalOffset (10));
//        assertEquals (-1, languageDvaSnapshot.getEmbeddedOffset (60));
//        assertEquals (0, languageDvaSnapshot.getEmbeddedOffset (67));
//        assertEquals (3, languageDvaSnapshot.getEmbeddedOffset (70));
//        assertEquals (-1, languageDvaSnapshot.getEmbeddedOffset (71));
//        assertEquals (-1, languageDvaSnapshot.getEmbeddedOffset (80));
//    }
//
//    public void testSnapshotEmbedding1 () throws IOException { // see issue #154444
//        clearWorkDir ();
//        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
//        FileObject testFile = FileUtil.createData (workDir, "bla");
//        OutputStream outputStream = testFile.getOutputStream ();
//        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
//        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!! asdfghjklqwertyuio");
//        writer.close ();
//        Source source = Source.create (testFile);
//        Snapshot originalSnapshot = source.createSnapshot ();
//        //System.out.println (originalSnapshot.getText ().length ());
//        Embedding languageJednaEmbedding = Embedding.create (Arrays.asList (new Embedding[] {
//            originalSnapshot.create ("123", "text/jedna"),
//            originalSnapshot.create ("456", "text/jedna"),
//            originalSnapshot.create ("789", "text/jedna"),
//        }));
//        assertEquals ("text/jedna", languageJednaEmbedding.getMimeType ());
//        Snapshot languageJednaSnapshot = languageJednaEmbedding.getSnapshot ();
//        assertEquals ("text/jedna", languageJednaSnapshot.getMimeType ());
//        assertEquals ("123456789", languageJednaSnapshot.getText ().toString ());
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (0));
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (2));
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (5));
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (8));
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (10));
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (0));
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (10));
//
//        Embedding languageDvaEmbedding = Embedding.create (Arrays.asList (new Embedding[] {
//            languageJednaSnapshot.create (2, 3, "text/dva")
//        }));
//        assertEquals ("text/dva", languageDvaEmbedding.getMimeType ());
//        Snapshot languageDvaSnapshot = languageDvaEmbedding.getSnapshot ();
//        assertEquals ("text/dva", languageDvaSnapshot.getMimeType ());
//        assertEquals ("345", languageDvaSnapshot.getText ().toString ());
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (0));
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (2));
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (5));
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (8));
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (10));
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (0));
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (10));
//    }
//
//    public void testSnapshotCreationDeadlock () throws Exception {  //Originally JavaSourceTest.testRTB_005
//        MockMimeLookup.setInstances(MimePath.get("text/foo"), new ParserFactory(){
//            public Parser createParser (Collection<Snapshot> snapshots) {
//                return new Parser () {
//
//                    private Snapshot last;
//
//                    public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
//                        last = snapshot;
//                    }
//
//                    public Result getResult (Task task) throws ParseException {
//                        return new Result (last) {
//                            protected void invalidate (){}
//                        };
//                    }
//
//                    public void cancel () {
//
//                    }
//
//                    public void addChangeListener (ChangeListener changeListener) {
//
//                    }
//
//                    public void removeChangeListener (ChangeListener changeListener) {
//
//                    }
//                };
//            }
//        });
//        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
//        FileObject testFile = FileUtil.createData (workDir, "bla.foo");
//        FileUtil.setMIMEType ("foo", "text/foo");
//        final Object lock = new Object ();
//        Logger.getLogger(Source.class.getName()).setLevel(Level.FINEST);
//        Logger.getLogger(Source.class.getName()).addHandler(new Handler() {
//            public void publish(LogRecord record) {
//                synchronized (lock) {
//                    lock.getClass();
//                }
//            }
//            public void flush() {}
//            public void close() throws SecurityException {}
//        });
//
//        final Source src = Source.create(testFile);
//        final ParserResultTask<Parser.Result> pr = new ParserResultTask<Parser.Result>() {
//            public void run (Parser.Result r, SchedulerEvent event) {
//
//            }
//
//            public Class<? extends Scheduler> getSchedulerClass () {
//                return null;
//            }
//
//            public void cancel () {}
//
//            public int getPriority () {
//                return 1;
//            }
//
//        };
//        Utilities.addParserResultTask(pr, src);
//        synchronized (lock) {
//            Utilities.revalidate(src);
//            Thread.sleep(2000);
//             TaskProcessor.runUserTask(new Mutex.ExceptionAction<Void>() {
//                    public Void run () throws ParseException {
//                        return null;
//                    }
//                }, Collections.singletonList(src));
//        }
//        Utilities.removeParserResultTask(pr, src);
//    }
//
//    public void testSnapshotEmbedding168725 () throws IOException {
//        clearWorkDir ();
//        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
//        FileObject testFile = FileUtil.createData (workDir, "bla");
//        OutputStream outputStream = testFile.getOutputStream ();
//        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
//        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
//        writer.close ();
//        Source source = Source.create (testFile);
//        Snapshot originalSnapshot = source.createSnapshot ();
//        Embedding languageJednaEmbedding = Embedding.create (Arrays.asList (new Embedding[] {
//            originalSnapshot.create ("Pozor, ", "text/jedna"),
//            originalSnapshot.create (56, 6, "text/jedna"),
//            originalSnapshot.create (" ", "text/jedna"),
//            originalSnapshot.create (34, 14, "text/jedna"),
//            originalSnapshot.create ("!!!", "text/jedna"),
//        }));
//        assertEquals ("text/jedna", languageJednaEmbedding.getMimeType ());
//        Snapshot languageJednaSnapshot = languageJednaEmbedding.getSnapshot ();
//        assertEquals ("text/jedna", languageJednaSnapshot.getMimeType ());
//        assertEquals ("Pozor, pokusy se budou delat!!!", languageJednaSnapshot.getText ().toString ());
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (0));
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (6));
//        assertEquals (56, languageJednaSnapshot.getOriginalOffset (7));
//        assertEquals (62, languageJednaSnapshot.getOriginalOffset (13));
//        assertEquals (34, languageJednaSnapshot.getOriginalOffset (14));
//        assertEquals (43, languageJednaSnapshot.getOriginalOffset (23));
//        assertEquals (48, languageJednaSnapshot.getOriginalOffset (28));
//        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (30));
//
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (0));
//        assertEquals (14, languageJednaSnapshot.getEmbeddedOffset (34));
//        assertEquals (28, languageJednaSnapshot.getEmbeddedOffset (48));
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (49));
//        assertEquals (7, languageJednaSnapshot.getEmbeddedOffset (56));
//        assertEquals (13, languageJednaSnapshot.getEmbeddedOffset (62));
//        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (63));
//    }

    public void testSnapshotEmbedding166592 () throws IOException {
        clearWorkDir ();
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla");
        OutputStream outputStream = testFile.getOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        Source source = Source.create (testFile);
        Snapshot originalSnapshot = source.createSnapshot ();
        Embedding languageJednaEmbedding = Embedding.create (Arrays.asList (new Embedding[] {
            originalSnapshot.create (18, 4, "text/jedna"),
            originalSnapshot.create (33, 15, "text/jedna"),
        }));
        assertEquals ("text/jedna", languageJednaEmbedding.getMimeType ());
        Snapshot languageJednaSnapshot = languageJednaEmbedding.getSnapshot ();
        assertEquals ("text/jedna", languageJednaSnapshot.getMimeType ());
        assertEquals ("file se budou delat", languageJednaSnapshot.getText ().toString ());
        assertEquals (18, languageJednaSnapshot.getOriginalOffset (0));
        assertEquals (21, languageJednaSnapshot.getOriginalOffset (3));
        assertEquals (33, languageJednaSnapshot.getOriginalOffset (4));
        assertEquals (43, languageJednaSnapshot.getOriginalOffset (14));
        assertEquals (48, languageJednaSnapshot.getOriginalOffset (19));
        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (20));

        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (0));
        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (17));
        assertEquals (0, languageJednaSnapshot.getEmbeddedOffset (18));
        assertEquals (3, languageJednaSnapshot.getEmbeddedOffset (21));
        assertEquals (4, languageJednaSnapshot.getEmbeddedOffset (22));
        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (23));
        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (32));
        assertEquals (4, languageJednaSnapshot.getEmbeddedOffset (33));
        assertEquals (5, languageJednaSnapshot.getEmbeddedOffset (34));
        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (32));
    }
}





