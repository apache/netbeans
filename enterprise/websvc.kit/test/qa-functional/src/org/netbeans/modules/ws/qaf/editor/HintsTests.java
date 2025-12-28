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
package org.netbeans.modules.ws.qaf.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.editor.hints.AnnotationHolder;
import org.netbeans.modules.ws.qaf.WebServicesTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 *  Duration of this test suite: aprox. 2min
 *
 * @author Jindrich Sedek
 */
public class HintsTests extends WebServicesTestBase {

    private List<Fix> fixes;
    private List<ErrorDescription> problems;

    /**
     * Creates a new instance of WebServices
     */
    public HintsTests(String S) {
        super(S);
    }

    public void testEndpointInterface() throws Exception {
        hintTest(new File(getDataDir(), "projects/Hints/src/java/hints/EndpointInterface.java"));
    }

    public void testExceptions() throws Exception {
        hintTest(new File(getDataDir(), "projects/Hints/src/java/hints/Exceptions.java"));
    }

    public void testHandlers() throws Exception {
        hintTest(new File(getDataDir(), "projects/Hints/src/java/hints/Handlers.java"));
    }

    public void testHandlers2() throws Exception {
        hintTest(new File(getDataDir(), "projects/Hints/src/java/hints/Handlers.java"), 1, null, 2);
    }

    public void testIOParameters() throws Exception {
        hintTest(new File(getDataDir(), "projects/Hints/src/java/hints/IOParametrs.java"));
    }

    public void testReturnValue() throws Exception {
        hintTest(new File(getDataDir(), "projects/Hints/src/java/hints/ReturnValue.java"));
    }

    public void testServiceName() throws Exception {
        hintTest(new File(getDataDir(), "projects/Hints/src/java/hints/ServiceName.java"));
    }

    private void hintTest(File file) throws Exception {
        hintTest(file, 0, null, 1);
    }

    @Override
    protected String getProjectName() {
        return "Hints";
    }

    protected void hintTest(File testedFile, int fixOrder, String captionDirToClose, int size) throws Exception {
        String result = null;
        try {
            log("STARTING HINT TEST");
            FileObject fileToTest = FileUtil.toFileObject(testedFile);
            DataObject dataToTest = DataObject.find(fileToTest);
            EditorCookie editorCookie = dataToTest.getCookie(EditorCookie.class);
            editorCookie.open();
            EditorOperator operator = new EditorOperator(testedFile.getName());
            assertNotNull(operator);
            String text = operator.getText();
            assertNotNull(text);
            assertFalse(text.length() == 0);
            waitHintsShown(fileToTest, size);
            for (ErrorDescription errorDescription : problems) {
                ref(errorDescription.toString());
            }
            for (Fix fix : fixes) {
                ref(fix.getText());
            }
            final Fix fix = fixes.get(fixOrder);
            RequestProcessor.Task task = RequestProcessor.getDefault().post(new Runnable() {

                public void run() {
                    try {
                        fix.implement();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        fail("IMPLEMENT" + ex.toString());
                    }
                }
            });
            closeDialog(captionDirToClose);
            task.waitFinished(1000);
            int count = 0;
            while (!editorCookie.isModified()) {
                log("WAITING FOR MODIFICATION:" + count);
                Thread.sleep(1000);
                if (++count == 10) {
                    throw new AssertionError("NO CODE EDITED");
                }
            }
            ref("---------------------");
            result = operator.getText();
            assertFalse(text.equals(result));
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        } finally {
            ref(result);
            EditorOperator.closeDiscardAll();
            Thread.sleep(1000);
        }
    }

    public List<ErrorDescription> getProblems(FileObject fileToTest) {
        problems = AnnotationHolder.getInstance(fileToTest).getErrors();
        problems.sort(new Comparator<ErrorDescription>() {

            public int compare(ErrorDescription o1, ErrorDescription o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return problems;
    }
    
    public static Test suite() {
        return NbModuleSuite.create(addServerTests(NbModuleSuite.createConfiguration(HintsTests.class),
                "testEndpointInterface",
                "testExceptions", 
                "testHandlers",
                "testHandlers2",
                "testIOParameters",
                "testReturnValue",
                "testServiceName"
                ).enableModules(".*").clusters(".*"));
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
                Logger.getLogger("TEST").info("RESCHEDULING");
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

    protected void waitHintsShown(FileObject fileToTest, int size) {
        final int delay = 1000;
        int repeat = 20;
        final Object lock = new Object();
        Runnable posted = new Runnable() {

            public void run() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        };
        RequestProcessor RP = new RequestProcessor("TEST REQUEST PROCESSOR");
        final RequestProcessor.Task task = RP.create(posted);
        HintsHandler handl = new HintsHandler(delay, task);
        Logger logger = Logger.getLogger(AnnotationHolder.class.getName());
        logger.setLevel(Level.FINE);
        try {
            do {
                synchronized (lock) {
                    task.schedule(delay);
                    logger.addHandler(handl);
                    lock.wait(repeat * delay);
                }
            } while ((repeat-- > 0) && (getFixes(fileToTest).size() < size));
        } catch (InterruptedException intExc) {
            throw new JemmyException("REFRESH DID NOT FINISHED IN " + repeat * delay + " SECONDS", intExc);
        } finally {
            logger.removeHandler(handl);
        }
    }

    public List<Fix> getFixes(FileObject fileToTest) {
        fixes = new ArrayList<Fix>();
        for (ErrorDescription errorDescription : getProblems(fileToTest)) {
            fixes.addAll(errorDescription.getFixes().getFixes());
        }
        fixes.sort(new Comparator<Fix>() {

            public int compare(Fix o1, Fix o2) {
                return o1.getText().compareTo(o2.getText());
            }
        });
        return fixes;
    }

    private void closeDialog(String dialogName) {
        if (dialogName == null) {
            return;
        }
        new NbDialogOperator(dialogName).ok();
    }

}
