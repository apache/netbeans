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
package org.netbeans.test.syntax.performance;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.text.StyledDocument;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.navigator.NavigatorTC;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditorCookie.Observable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 *
 * @author Jindrich Sedek
 */
public class PerformanceTest extends NbTestCase {

    private static final int TIMEOUT = 5000;
    private final List<Failure> failures = new LinkedList<Failure>();
    private TimerHandler timerHandler = new TimerHandler();
    private RequestProcessor.Task waiter;
    private Formatter shortFormat = new SimpleFormatter();

    public PerformanceTest(String name) {
        super(name);
        waiter = RequestProcessor.getDefault().post(Task.EMPTY);
        Logger logger = Logger.getLogger("TIMER");
        logger.addHandler(timerHandler);
        logger.setLevel(Level.FINEST);
    }

    public static Test suite(){
        return NbModuleSuite.allModules(PerformanceTest.class);
    }    

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        openNavigator();
        timerHandler.flush();
        failures.clear();
        waitTimeout();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (failures.size() > 0){
            String message = new String();
            for (Failure failure : failures) {
                message = message + "\n" + shortFormat.format(failure.rec) + " Reached:" + failure.time;
            }
            fail("some logs reached the boundary \n" + message);
        }
    }

    public void testOpenHTML() throws Exception {
        StyledDocument doc = prepare("performance.html");
        doc.insertString(0, "<table></table>", null);
        waitTimeout();
        doc.insertString(doc.getEndPosition().getOffset() - 1, "<tab", null);
        waitTimeout();
        for (LogRecord log : timerHandler.logs) {
            String message = log.getMessage();
            if (message.contains("Navigator Initialization")) {
                verify(log, 500, 2000);
            } else if (message.contains("Parsing (text/html)") || message.contains("Navigator Merge") || 
                    message.contains("Open Editor")|| message.contains("Folds - 2")){
                verify(log, 400, 1500);
            }else {
                verify(log, 200, 800);
            }
        }
    }

    public void testOpenJSP() throws Exception {
        StyledDocument doc = prepare("performance.jsp");
        doc.insertString(0, "${\"hello\"}", null);
        waitTimeout();
        doc.insertString(doc.getEndPosition().getOffset() - 1, "<%= \"hello\" %>", null);
        waitTimeout();
        for (LogRecord log : timerHandler.logs) {
            if (log.getMessage().contains("Navigator Initialization")) {
                verify(log, 500, 2000);
            } else {
                verify(log, 200, 800);
            }
        }
    }

    public void testOpenCSS() throws Exception {
        StyledDocument doc = prepare("performance.css");
        doc.insertString(0, "selector{color:green}", null);
        waitTimeout();
        doc.insertString(doc.getEndPosition().getOffset() - 1, "sx{c:red}", null);
        waitTimeout();
        for (LogRecord log : timerHandler.logs) {
            verify(log, 200, 800);
        }
    }

    private StyledDocument prepare(String fileName) throws Exception {
        File testFile = new File(getDataDir(), fileName);
        FileObject testObject = FileUtil.createData(testFile);
        DataObject dataObj = DataObject.find(testObject);
        EditorCookie.Observable ed = dataObj.getCookie(Observable.class);
        waitTimeout();
        ed.openDocument();
        ed.open();
        waitTimeout();
        return ed.getDocument();
    }

    private void waitTimeout(){
        waiter.schedule(TIMEOUT);
        waiter.waitFinished();
    }

    private void verify(LogRecord log, int expected, int boundary) {
        Object[] params = log.getParameters();
        if (params.length < 2){
            return;
        }
        if (!(params[1] instanceof Number)) {
            return;
        }
        Number nTime = (Number) params[1];
        Integer time = nTime.intValue();
        if (time > expected * 2) {
            System.err.println(log.getMessage() + " Reached:" + time);
        }
        if (time > boundary * 2) {
            failures.add(new Failure(log, time));
        }
    }

    private class Failure{
        LogRecord rec;
        Integer time;

        public Failure(LogRecord rec, Integer time) {
            this.rec = rec;
            this.time = time;
        }
    }

    private class TimerHandler extends Handler {

        ArrayList<LogRecord> logs = new ArrayList<LogRecord>();

        @Override
        public void publish(LogRecord record) {
            waiter.schedule(TIMEOUT);
            logs.add(record);
        }

        @Override
        public void flush() {
            logs.clear();
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    public static void openNavigator() throws Exception{
        EventQueue.invokeAndWait(new Runnable() {

            public void run() {
                NavigatorTC.getInstance().open();
            }
        });
    }
}




