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
package org.netbeans.test.syntax;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Caret;
import junit.framework.Test;
import org.netbeans.editor.Utilities;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.junit.AssertionFailedErrorException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.test.web.FileObjectFilter;
import org.netbeans.test.web.RecurrentSuiteFactory;
import org.netbeans.test.web.TextGraphics2D;
import org.openide.actions.UndoAction;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditorCookie.Observable;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 * Test goes throught files and looking for CC Test Steps.
 * The CC Test Steps are writen in document body as three lines:
 * JSP comments which start with '<%--CC' prefix, where:
 *<ul>
 *<li> first line contains ccPrefix with optional '|' character which represents
 * cursor position
 *<li> second line contains ccChoice item which will be used for CC substitution
 *<li> third line contains ccResult
 *</ul>
 *
 * For example:<p>
 * <pre><%--CC
 * <%@ taglib |
 * uri
 * <%@ taglib uri=""
 * --%>
 * </pre><p>
 * does:
 * <ul>
 * <li> inserts '<%@ taglib ' string into new line
 * <li> invokes CC
 * <li> dumps Completion Query Result
 * <li> choses "uri" item from the query result and substitute it
 * <li> checks if subtituted line is: '<%@ taglib uri=""'
 * <li> undoes all changes
 * </ul>
 * @author ms113234
 *
 */
public class CompletionTest extends J2eeTestCase {

    private static final boolean GENERATE_GOLDEN_FILES = false;//generate golden files, or test
    private static final int COMPLETION_PREFIX_LENGHT = 40;
    private static boolean projectsOpened = false;//open test projects
    protected static final List XML_EXTS = Arrays.asList(new String[]{"html", "tld", "xhtml"});
    protected static final List JSP_EXTS = Arrays.asList(new String[]{"jsp", "tag", "jspf", "tagf", "jspx", "tagx"});
    protected static List ignored_tests = Arrays.asList(new String[]{"testJS.js", "testJS2.js", "testInjection.xhtml", "testInjection.jsp", "testHTML.jsp",
    "testScriptletsImplicitObjects.jsp", "testExpression.jsp", "testJSPDocumentHTML.jspx", "testHTML.tag", "testScriptletsJavaIssue.tag"});
    protected static final List JS_EXTS = Arrays.asList(new String[]{"js"/*,"java"*/});
    public static final Logger LOG = Logger.getLogger(CompletionTest.class.getName());
    protected FileObject testFileObj;
    public static boolean isJDK8 = System.getProperty("java.version").startsWith("1.8");

    public CompletionTest() {
        super("CompletionTest");
    }

    /** Need to be defined because of JUnit */
    public CompletionTest(String name, FileObject testFileObj) {
        super(name);
        this.testFileObj = testFileObj;
    }

    @Override
    public void setUp() throws IOException {
        if (!projectsOpened) {
            log("Opening files from " + getProjectsDir().getAbsolutePath());
            for (File file : getProjectsDir().listFiles()) {
                openProjects(file.getAbsolutePath());
                resolveServer(file.getName());
            }
            finalizeProjectsOpening();
            projectsOpened = true;
        }
        System.out.println("########  " + getName() + "  #######");
    }

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(CompletionTest.class);
        addServerTests(Server.GLASSFISH, conf, new String[0]);//register server
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf.addTest(SuiteCreator.class));
    }

    public static final class SuiteCreator extends NbTestSuite {

        public SuiteCreator() {
            super();
            FileObjectFilter filter = new FileObjectFilter() {

                public boolean accept(FileObject fo) {
                    String ext = fo.getExt();
                    String name = fo.getName();
                    return (name.startsWith("test") || name.startsWith("Test"))
                            && (XML_EXTS.contains(ext) || JSP_EXTS.contains(ext) || JS_EXTS.contains(ext))
                            && !CompletionTest.ignored_tests.contains(name + "." + ext);
                }
            };
            addTest(RecurrentSuiteFactory.createSuite(CompletionTest.class,
                    new CompletionTest().getProjectsDir(), filter));
        }
    }

    protected File getProjectsDir() {
        File datadir = new CompletionTest().getDataDir();
        return new File(datadir, "CompletionTestProjects");
    }

    protected void finalizeProjectsOpening() {
    }

    @Override
    public void runTest() throws Exception {
        if (testFileObj == null) {
            return;
        }
        String ext = testFileObj.getExt();
        if (JSP_EXTS.contains(ext)) {
            test(testFileObj, "<%--CC", "--%>");
        } else if (XML_EXTS.contains(ext)) {
            test(testFileObj, "<!--CC", "-->", false);
        } else if (JS_EXTS.contains(ext)) {
            test(testFileObj, "/**CC", "*/");
        } else {
            throw new JemmyException("File extension of: " + testFileObj.getNameExt() + " is unsupported.");
        }
    }

    private boolean isJavaScript() {
        return JS_EXTS.contains(testFileObj.getExt());
    }

    private void test(FileObject fileObj, String stepStart, String stepEnd) throws Exception {
        test(fileObj, stepStart, stepEnd, true);
    }

    public static BaseDocument openFile(FileObject fileObject) throws Exception {
        DataObject dataObj = DataObject.find(fileObject);
        final EditorCookie.Observable ed = dataObj.getCookie(Observable.class);
        BaseDocument doc = (BaseDocument) ed.openDocument();
        ed.open();
        new EditorOperator(fileObject.getName());
        return doc;
    }

    public static void waitTypingFinished(BaseDocument doc) {
        final int delay = 2000;
        final int repeat = 20;
        final Object lock = new Object();
        Runnable posted = new Runnable() {

            public void run() {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        };
        RequestProcessor RP = new RequestProcessor("TEST REQUEST PROCESSOR");
        final RequestProcessor.Task task = RP.post(posted, delay);
        DocumentListener listener = new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                task.schedule(delay);
            }

            public void removeUpdate(DocumentEvent e) {
            }

            public void changedUpdate(DocumentEvent e) {
            }
        };
        doc.addDocumentListener(listener);
        try {
            synchronized (lock) {
                lock.wait(repeat * delay);
            }
        } catch (InterruptedException intExc) {
            throw new JemmyException("TYPING DID NOT FINISHED IN " + repeat * delay + " SECONDS", intExc);
        } finally {
            doc.removeDocumentListener(listener);
        }
    }

    protected void test(FileObject fileObj, String stepStart, String stepEnd, boolean ignoreComment) throws Exception {
        boolean inStepData = false;
        String[] stepData = new String[3];
        int dataLineIdx = 0;

        try {
            // get token chain
            DataObject dataObj = DataObject.find(fileObj);
            final EditorCookie.Observable ed = dataObj.getCookie(Observable.class);
            BaseDocument doc = openFile(fileObj);
            final List<JEditorPane> editorPane = new LinkedList<JEditorPane>();
            Runnable runnable = new Runnable() {

                public void run() {
                    editorPane.add(ed.getOpenedPanes()[0]);
                }
            };
            EventQueue.invokeAndWait(runnable);
            JEditorPane editor = editorPane.get(0);
            ExtSyntaxSupport ess = (ExtSyntaxSupport) doc.getSyntaxSupport();
            TokenItem token = ess.getTokenChain(0, doc.getLength());
            List<TestStep> steps = new java.util.ArrayList<TestStep>();
            // go through token chain an look for CC test steps
            while (token != null) {
                TokenID tokenID = token.getTokenID();
                if (tokenID.getName().indexOf("EOL") != -1) {
                    token = token.getNext();
                    continue;
                }
                String tImage = token.getImage();
                int tEnd = token.getOffset() + tImage.length();
                LOG.fine("# [" + token.getOffset() + "," + tEnd + "] " + tokenID.getName() + " :: " + token.getImage());
                String commentBlock;
                if (isJavaScript()) {
                    commentBlock = "text";
                } else {
                    commentBlock = "comment";
                }
                if ((ignoreComment) && (tokenID.getName().indexOf(commentBlock) == -1)) {
                    token = token.getNext();
                    continue;
                }
                if (inStepData) {
                    // probably end of step data
                    if (token.getImage().indexOf(stepEnd) > -1) {
                        inStepData = false;
                        // check obtained CC data and create test step CCsecs
                        if (dataLineIdx == 3) {
                            int offset = token.getOffset() + token.getImage().length();
                            TestStep step = new TestStep(stepData, offset);
                            steps.add(step);
                        } else {
                            ref("EE: expected data lines number: 3  but was: " + dataLineIdx);
                        }
                    } else {
                        // assert CC TEst Data lenght
                        if (dataLineIdx > 2) {
                            String msg = "EE: to much lines in CC Test Data";
                            ref(msg);
                            ref(dumpToken(token));
                            fail(msg);
                        }
                        String str = token.getImage();
                        // suppress new lines
                        if (str.endsWith("\n")) {
                            str = str.substring(0, str.length() - 1);
                        }
                        stepData[dataLineIdx++] = str;
                    }
                } else {
                    String text = token.getImage();
                    if (text.startsWith(stepStart)) {
                        if (text.endsWith(stepEnd)) {
                            // all steps line in one toke as .java does
                            String[] lines = text.split("\n\r?|\r\n?");
                            if (lines.length == 5) {
                                int offset = token.getOffset() + token.getImage().length();
                                for (int i = 0; i < 3; i++) {
                                    stepData[i] = lines[i + 1];
                                }
                                TestStep step = new TestStep(stepData, offset);
                                steps.add(step);
                            } else {
                                String msg = "EE: expected 5 lines lenght token but got: " + lines.length;
                                ref(msg);
                                ref(text);
                                for (int i = 0; i < lines.length; i++) {
                                    ref(i + "::" + lines[i]);
                                }
                            }
                        } else {
                            // each step line in separate line as .jsp does
                            inStepData = true;
                            dataLineIdx = 0;
                        }
                    }
                }
                token = token.getNext();
            } // while (token != null)
            LOG.info("Steps count:" + Integer.toString(steps.size()));
            run(editor, steps);
        } catch (Exception ex) {
            throw new AssertionFailedErrorException(ex);
        }
        ending();
    }

    protected void run(JEditorPane editor, List<TestStep> steps) throws Exception {
        Iterator<TestStep> it = steps.iterator();
        while (it.hasNext()) {
            exec(editor, it.next());
        }
    }

    protected void exec(JEditorPane editor, TestStep step) throws Exception {
        try {
            BaseDocument doc = (BaseDocument) editor.getDocument();
            ref(step.toString());
            Caret caret = editor.getCaret();
            caret.setDot(step.getOffset() + 1);
            EditorOperator eo = new EditorOperator(testFileObj.getNameExt());
            eo.insert(step.getPrefix());
            if (!isJavaScript()) {
                caret.setDot(step.getCursorPos());
            }

            waitTypingFinished(doc);
            CompletionJListOperator comp = null;
            boolean print = false;
            int counter = 0;
            while (!print) {
                ++counter;
                if (counter > 5) {
                    print = true;
                }
                try {
                    comp = CompletionJListOperator.showCompletion();
                } catch (JemmyException e) {
                    log("EE: The CC window did not appear");
                    e.printStackTrace(getLog());
                }
                if (comp != null) {
                    print = dumpCompletion(comp, step, editor, print) || print;
                    waitTypingFinished(doc);
                    CompletionJListOperator.hideAll();
                    if (!print) {// wait for refresh
                        Thread.sleep(1000);
                        if (!isJavaScript()) {
                            caret.setDot(step.getCursorPos());
                        }
                    }
                } else {
                    long time = System.currentTimeMillis();
                    String screenFile = time + "-screen.png";
                    log("CompletionJList is null");
                    log("step: " + step);
                    log("captureScreen:" + screenFile);
                    try {
                        PNGEncoder.captureScreen(getWorkDir().getAbsolutePath() + File.separator + screenFile);
                    } catch (Exception e1) {
                        e1.printStackTrace(getLog());
                    }
                }
            }
            waitTypingFinished(doc);
            int rowStart = Utilities.getRowStart(doc, step.getOffset() + 1);
            int rowEnd = Utilities.getRowEnd(doc, step.getOffset() + 1);
            String fullResult = doc.getText(new int[]{rowStart, rowEnd});
            String result = fullResult.trim();
            int removed_whitespaces = fullResult.length() - result.length();
            if (!result.equals(step.getResult().trim())) {
                ref("EE: unexpected CC result:\n< " + result + "\n> " + step.getResult());
            }
            ref("End cursor position = " + (caret.getDot() - removed_whitespaces));
        } finally {
            // undo all changes
            final UndoAction ua = SystemAction.get(UndoAction.class);
            assertNotNull("Cannot obtain UndoAction", ua);
            while (ua.isEnabled()) {
                runInAWT(new Runnable() {

                    public void run() {
                        ua.performAction();
                    }
                });
            }
        }
    }

    /**
     * 
     * @param printDirectly if to print directly
     * @return whether the selected code completion item was found
     * @throws java.lang.Exception
     */
    protected boolean dumpCompletion(CompletionJListOperator comp, TestStep step,
            final JEditorPane editor, boolean printDirectly) throws Exception {
        List<String> finalItems = new ArrayList<String>();
        if (comp != null) {
            // dump CC result to golden file
            Iterator items = comp.getCompletionItems().iterator();
            CompletionItem selectedItem = null;
            boolean startsWith = false;
            while (items.hasNext()) {
                TextGraphics2D g = new TextGraphics2D(comp.getSource());
                Object next = items.next();
                String dispText = null;
                if (next instanceof CompletionItem) {
                    CompletionItem cItem = (CompletionItem) next;
                    Font default_font = new JLabel(cItem.getSortText().toString()).getFont();
                    FontMetrics fm = g.getFontMetrics(default_font);
                    assertNotNull(fm);
                    cItem.render(g, default_font, Color.BLACK, Color.WHITE, 400, 30, false);
                } else {
                    g.drawString(next.toString(), 0, 0);
                }
                dispText = getPrefix(g.getTextUni().trim());
                // find choice item
                if ((selectedItem == null || !startsWith) && (dispText.startsWith(step.getChoice()))) {
                    startsWith = true;
                    selectedItem = (CompletionItem) next;
                }
                if ((selectedItem == null) && (dispText.contains(step.getChoice()))) {
                    selectedItem = (CompletionItem) next;
                }
                if (printDirectly && !isJavaScript()) {
                    logIntoRef(dispText);
                } else {
                    finalItems.add(dispText);
                }
            }
            if (printDirectly && isJavaScript()) {
                Collections.sort(finalItems);
                for (String str : finalItems) {
                    logIntoRef(str);
                }
            }
            class DefaultActionRunner implements Runnable {

                CompletionItem item;
                JEditorPane editor;

                public DefaultActionRunner(CompletionItem item,
                        JEditorPane editor) {
                    this.item = item;
                    this.editor = editor;
                }

                public void run() {
                    item.defaultAction(editor);
                }
            }
            // substitute completion  and check result
            if (selectedItem != null) {
                // move to separate class
                if (!printDirectly) {
                    if (isJavaScript()) {
                        Collections.sort(finalItems);
                    }
                    for (String str : finalItems) {
                        logIntoRef(str);
                    }
                }
                runInAWT(new DefaultActionRunner(selectedItem, editor));
                return true;
            } else {
                if (printDirectly) {
                    ref("EE: cannot find completion item: " + step.getChoice());
                }
            }
        } else {
            // comp == null && ccError == false => instant substitution
            ref("Instant substitution performed");
        }
        return false;
    }

    private void logIntoRef(String message) {
        message = message.replaceAll("<\\?>", "");
        message = message.replaceAll("<\\? >", "");
        if (message.length() > 30) {
            message = message.substring(0, 30);
        }
        ref(message);
    }

    private String getPrefix(String completionText) {
        if (completionText.length() > COMPLETION_PREFIX_LENGHT) {
            return completionText.substring(0, COMPLETION_PREFIX_LENGHT);
        } else {
            return completionText;
        }
    }

    protected static void runInAWT(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(r);
            } catch (Exception exc) {
                throw new JemmyException("INVOKATION FAILED", exc);
            }
        }
    }

    protected Method findMethod(Class clazz, String name, Class[] paramTypes) {
        Method method = null;
        for (Class cls = clazz; cls.getSuperclass() != null; cls = cls.getSuperclass()) {
            try {
                method = cls.getDeclaredMethod(name, paramTypes);
            } catch (NoSuchMethodException e) {
                // ignore
            }
        }
        return method;
    }

    protected void assertInstanceOf(Class<?> expectedType, Object actual) {
        if (!expectedType.isAssignableFrom(actual.getClass())) {
            fail("Expected type: " + expectedType.getName() + "\nbut was: "
                    + actual.getClass().getName());
        }
    }

    protected static class TestStep {

        private String prefix;
        private String choice;
        private String result;
        private int offset;
        private int cursorPos;

        public TestStep(String data[], int offset) {
            this.prefix = data[0];
            this.choice = data[1];
            this.result = data[2];
            this.offset = offset;

            cursorPos = prefix.indexOf('|');
            if (cursorPos != -1) {
                prefix = prefix.replaceFirst("\\|", "");
            } else {
                cursorPos = prefix.length();
            }
            cursorPos += offset + 1;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer(prefix);
            sb.insert(cursorPos - offset - 1, '|');
            return "[" + sb + ", " + choice + ", " + result + ", " + offset + "]";
        }

        public String getPrefix() {
            return prefix;
        }

        public String getChoice() {
            return choice;
        }

        public String getResult() {
            return result;
        }

        public int getOffset() {
            return offset;
        }

        public int getCursorPos() {
            return cursorPos;
        }
    }

    protected String dumpToken(TokenItem tokenItem) {
        StringBuffer sb = new StringBuffer();
        sb.append("<token \name='");
        sb.append(tokenItem.getTokenID().getName());
        sb.append("'>\n");
        sb.append(tokenItem.getTokenContextPath());
        sb.append("</token>");
        return sb.toString();
    }

    static void generateGoldenFiles(JellyTestCase test) throws Exception {
        test.getRef().flush();
        File ref = new File(test.getWorkDir(), test.getName() + ".ref");
        String fullClassName = test.getClass().getName();
        String goldenFilePath = fullClassName.replace('.', '/') + "/" + test.getName();
        File goldenFile = new File(test.getDataDir() + "/goldenfiles/" + goldenFilePath);
        goldenFile = new File(goldenFile.getAbsolutePath().replace("build/", "") + ".pass");
        goldenFile.getParentFile().mkdirs();
        if (!ref.renameTo(goldenFile)) {
            throw new AssertionError("Generating golden files to " + goldenFile.getAbsolutePath() + " failed");
        }
        assertTrue("Generating golden files to " + goldenFile.getAbsolutePath(), false);
    }

    protected void ending() throws Exception {
        if (GENERATE_GOLDEN_FILES) {
            generateGoldenFiles(this);
        } else {
            if (CompletionTest.isJDK8 && this.alternativeGoldenFileExists(this.getName() + "_jdk8.pass")) {
                compareReferenceFiles(this.getName() + ".ref", this.getName() + "_jdk8.pass", this.getName() + ".diff");
            } else {
                compareReferenceFiles();
            }
        }
    }
    
    protected boolean alternativeGoldenFileExists(String filename){
        String fullClassName = this.getClass().getName();
        String goldenFileName = fullClassName.replace('.', '/')+"/"+filename;
        File goldenFile = new File(getDataDir()+"/goldenfiles/"+goldenFileName);
        return goldenFile.exists();
    }
}
