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
package org.netbeans.test.j2ee.hints;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.editor.hints.AnnotationHolder;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jindrich Sedek
 */
public class EntityRelations extends J2eeTestCase {

    private String goldenFilePath;
    private Writer goldenWriter;
    private List<Fix> fixes;
    private List<ErrorDescription> problems;
    private File secondFile = null;
    private static boolean projectsOpened = false;
    private static final Logger LOG = Logger.getLogger(EntityRelations.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(EntityRelations.class.getName());

    /** Creates a new instance of EntityRelations */
    public EntityRelations(String name) {
        super(name);
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = emptyConfiguration();
        conf = conf.addTest(EntityRelations.class);
        conf = addServerTests(Server.GLASSFISH, conf);//register server
        return conf.suite();
    }

    @Override
    public void setUp() throws IOException {
        if (!projectsOpened) {
            for (File file : getProjectsDirs()) {
                JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 180000);
                openProjects(file.getAbsolutePath());
                resolveServer(file.getName());
            }
            projectsOpened = true;
        }
        System.out.println("########  " + getName() + "  #######");
    }

    private boolean generateGoldenFiles() {
        return false;
    }

    private File[] getProjectsDirs() {
        return new File[]{
                    new File(getDataDir(), "projects/EntityHintsApp"),
                    new File(getDataDir(), "projects/EntityHintsEJB")
                };
    }

    private EditorOperator openFile(String fileName) throws Exception {
        secondFile = new File(getDataDir(), fileName);
        DataObject dataObj = DataObject.find(FileUtil.toFileObject(secondFile));
        EditorCookie ed = dataObj.getCookie(EditorCookie.class);
        ed.open();
        return new EditorOperator(secondFile.getName()); // wait for opening
    }

    private void testEntityHintsBidirectional(int fixOrder) throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsEJB/src/java/hints/B.java");
        openFile("projects/EntityHintsEJB/src/java/hints/A.java");
        hintTest(f, fixOrder, "Create", 12);
    }

    private void testEntityHintsUnidirectional(int fixOrder) throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsEJB/src/java/hints/B.java");
        openFile("projects/EntityHintsEJB/src/java/hints/A.java");
        hintTest(f, fixOrder, null, 12);
    }

    public void testManyToManyBidirectional() throws Exception {
        testEntityHintsBidirectional(4);
    }

    public void testManyToManyBidirectional2() throws Exception {
        testEntityHintsBidirectional(5);
    }

    public void testManyToOneBidirectional() throws Exception {
        testEntityHintsBidirectional(6);
    }

    public void testOneToManyBidirectional() throws Exception { // should be O2M
        testEntityHintsBidirectional(7);
    }

    public void testOneToManyBidirectional2() throws Exception { // should be O2M2
        testEntityHintsBidirectional(8);
    }

    public void testOneToOneBidirectional() throws Exception {
        testEntityHintsBidirectional(9);
    }

    public void testManyToOneUnidirectional() throws Exception {
        testEntityHintsUnidirectional(10);
    }

    public void testOneToManyUnidirectional() throws Exception { // should be O2M
        testEntityHintsUnidirectional(11);
    }

    public void testOneToManyUnidirectional2() throws Exception { // new
        testEntityHintsUnidirectional(12);
    }
    
    public void testOneToOneUnidirectional() throws Exception {
        testEntityHintsUnidirectional(13);
    }

    public void testAARelation() throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsApp/src/java/hints/CC.java");
        hintTest(f, 4, "Create", 7);
    }

    public void testAARelation2() throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsApp/src/java/hints/CC.java");
        hintTest(f, 5, "Create", 7);
    }

    public void testAARelation3() throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsApp/src/java/hints/CC.java");
        hintTest(f, 6, null, 7);
    }

    public void testAARelation4() throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsApp/src/java/hints/CC.java");
        hintTest(f, 7, null, 7);
    }
    
    public void testCreateID() throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsEJB/src/java/hints/CreateID.java");
        hintTest(f, 0, "Create", 2);
    }

    public void testDefaultConstructor() throws Exception {
        File f = new File(getDataDir(), "projects/EntityHintsEJB/src/java/hints/DefaultConstructor.java");
        hintTest(f, 0, null, 1);
    }

    ///@param size size is the expected size of fixes list length
    private void hintTest(File testedFile, int fixOrder, String captionDirToClose, int size) throws Exception {
        String result = null;
        try {
            LOG.fine("starting hint test");
            FileObject fileToTest = FileUtil.toFileObject(testedFile);
            DataObject dataToTest;
            dataToTest = DataObject.find(fileToTest);
            EditorCookie editorCookie = dataToTest.getCookie(EditorCookie.class);
            editorCookie.open();
            EditorOperator operator = new EditorOperator(testedFile.getName());
            assertNotNull(operator);
            String text = operator.getText();
            assertNotNull(text);
            assertFalse(text.length() == 0);
            waitHintsShown(fileToTest, size, operator);
            for (ErrorDescription errorDescription : problems) {
                write(errorDescription.toString());
            }
            for (Fix fix : fixes) {
                write(fix.getText());
            }
            assertTrue("All fixes should be initialized (expected " + size + " but was " + fixes.size() + ").", fixes.size() >= size);
            final Fix fix = fixes.get(fixOrder);
            RequestProcessor.Task task = RP.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        fix.implement();
                    } catch (Exception ex) {
                        ex.printStackTrace(System.err);
                        fail("IMPLEMENT" + ex.toString());
                    }
                }
            });
            if (captionDirToClose != null) {
                new NbDialogOperator(captionDirToClose).ok();
            }
            task.waitFinished(1000);
            int count = 0;
            while (!editorCookie.isModified()) {
                LOG.log(Level.FINE, "wait for modifications :{0}", count);
                Thread.sleep(1000);
                if (++count == 10) {
                    throw new AssertionError("NO CODE EDITED");
                }
            }
            write("---------------------");
            result = operator.getText();
            assertFalse(text.equals(result));
        } finally {
            write(result);
            if (secondFile != null) {
                write("----SECOND FILE-----");
                write(new EditorOperator(secondFile.getName()).getText());
            }
            EditorOperator.closeDiscardAll();
            Thread.sleep(1000);
        }
        if (generateGoldenFiles()) {
            if (goldenWriter != null) {
                goldenWriter.close();
            }
            fail("GENERATING GOLDEN FILES: " + goldenFilePath);
        } else {
            compareReferenceFiles();
        }
    }

    private void write(String str) {
        ref(str);
        if (generateGoldenFiles()) {
            try {
                if (goldenWriter == null) {
                    goldenFilePath = getGoldenFile().getPath().replace("work/sys", "qa-functional");
                    File gFile = new File(goldenFilePath);
                    gFile.createNewFile();
                    goldenWriter = new FileWriter(gFile);
                }
                goldenWriter.append(str + "\n");
                goldenWriter.flush();
            } catch (java.io.IOException exc) {
                exc.printStackTrace(System.err);
                fail("IMPOSSIBLE TO GENERATE GOLDENFILES");
            }
        }
    }

    private List<ErrorDescription> getProblems(FileObject fileToTest) {
        problems = AnnotationHolder.getInstance(fileToTest).getErrors();
        problems.sort(new Comparator<ErrorDescription>() {

            @Override
            public int compare(ErrorDescription o1, ErrorDescription o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return problems;
    }

    private static class HintsHandler extends Handler {

        RequestProcessor.Task task;
        int delay;

        public HintsHandler(int delay, RequestProcessor.Task task) {
            this.task = task;
            this.delay = delay;
        }

        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().contains("updateAnnotations")) {
                LOG.fine("rescheduling");
                task.schedule(delay);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    private void waitHintsShown(FileObject fileToTest, int size, EditorOperator editorOperator) {
        final int delay = 1000;
        int repeat = 20;
        final Object lock = new Object();
        Runnable posted = new Runnable() {

            @Override
            public void run() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        };
        final RequestProcessor.Task task = RP.create(posted);
        HintsHandler handl = new HintsHandler(delay, task);
        Logger logger = Logger.getLogger(AnnotationHolder.class.getName());
        Level oldLevel = logger.getLevel();
        logger.setLevel(Level.FINE);
        try {
            boolean notReady = true;
            do {
                synchronized (lock) {
                    task.schedule(delay);
                    logger.addHandler(handl);
                    lock.wait(repeat * delay);
                }
                notReady = getFixes(fileToTest).size() < size;
                if (notReady) {
                    // modify file to refresh hints (see bug 198171)
                    editorOperator.replace("package", "package");  //NOI18N
                }
            } while ((repeat-- > 0) && notReady);
        } catch (InterruptedException intExc) {
            throw new JemmyException("REFRESH DID NOT FINISHED IN " + repeat * delay + " SECONDS", intExc);
        } finally {
            logger.removeHandler(handl);
            logger.setLevel(oldLevel);
        }
    }

    private List<Fix> getFixes(FileObject fileToTest) {
        fixes = new ArrayList<Fix>();
        for (ErrorDescription errorDescription : getProblems(fileToTest)) {
            fixes.addAll(errorDescription.getFixes().getFixes());
        }
        fixes.sort(new Comparator<Fix>() {

            @Override
            public int compare(Fix o1, Fix o2) {
                return o1.getText().compareTo(o2.getText());
            }
        });
        return fixes;
    }
}
