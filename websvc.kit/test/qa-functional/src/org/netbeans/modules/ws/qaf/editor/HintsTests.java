/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        Collections.sort(problems, new Comparator<ErrorDescription>() {

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
        Collections.sort(fixes, new Comparator<Fix>() {

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
