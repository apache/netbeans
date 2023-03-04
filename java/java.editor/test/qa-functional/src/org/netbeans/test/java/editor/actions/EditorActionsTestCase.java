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
package org.netbeans.test.java.editor.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.test.java.editor.lib.EditorTestCase;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.junit.Manager;
import org.netbeans.junit.diff.Diff;

/**
 * Basic Editor Actions Test class.
 * It contains basic editor actions functionality methods.
 *
 *
 * @author Martin Roskanin
 */
public class EditorActionsTestCase extends EditorTestCase {

    // private PrintStream wrapper for System.out
    private PrintStream systemOutPSWrapper = new PrintStream(System.out);
    private int index = 0;
    public static final int WAIT_MAX_MILIS_FOR_UNDO_REDO = 2000;

    /** Creates a new instance of Main */
    public EditorActionsTestCase(String testMethodName) {
        super(testMethodName);
    }

    private String getIndexAsString() {
        String ret = String.valueOf(index);
        if (ret.length() == 1) {
            ret = "0" + ret;
        }
        return ret;
    }

    private String getRefFileName() {
        return this.getName() + getIndexAsString() + ".ref"; //NOI18N
    }

    private String getGoldenFileName() {
        return this.getName() + getIndexAsString() + ".pass"; //NOI18N
    }

    private String getDiffFileName() {
        return this.getName() + getIndexAsString() + ".diff"; //NOI18N
    }
    // hashtable holding all already used logs and correspondig printstreams
    private Hashtable logStreamTable = null;

    private PrintStream getFileLog(String logName) throws IOException {
        OutputStream outputStream;
        FileOutputStream fileOutputStream;

        if ((logStreamTable == null) | (hasTestMethodChanged())) {
            // we haven't used logging capability - create hashtables
            logStreamTable = new Hashtable();
        //System.out.println("Created new hashtable");
        } else {
            if (logStreamTable.containsKey(logName)) {
                //System.out.println("Getting stream from cache:"+logName);
                return (PrintStream) logStreamTable.get(logName);
            }
        }
        // we didn't used this log, so let's create it
        FileOutputStream fileLog = new FileOutputStream(new File(getWorkDir(), logName));
        PrintStream printStreamLog = new PrintStream(fileLog, true);
        logStreamTable.put(logName, printStreamLog);
        //System.out.println("Created new stream:"+logName);
        return printStreamLog;
    }
    private String lastTestMethod = null;

    private boolean hasTestMethodChanged() {
        if (!this.getName().equals(lastTestMethod)) {
            lastTestMethod = this.getName();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public PrintStream getRef() {
        String refFilename = getRefFileName();
        try {
            return getFileLog(refFilename);
        } catch (IOException ioe) {
            // canot get ref file - return system.out
            //System.err.println("Test method "+this.getName()+" - cannot open ref file:"+refFilename
            //                                +" - defaulting to System.out and failing test");
            fail("Could not open reference file: " + refFilename);
            return systemOutPSWrapper;
        }
    }

    protected void compareToGoldenFile(EditorOperator editor, String RefFileName, String GoldenFileName, String DiffFileName) {
            ref(editor.getText());
            compareReferenceFiles(RefFileName + ".ref", GoldenFileName + ".pass", DiffFileName + ".diff");
    }

    protected void waitForMilis(int maxMiliSeconds) {
        int time = (int) maxMiliSeconds / 100;
        while (time > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                time = 0;
            }
            time--;

        }
    }

    protected ValueResolver getFileLengthChangeResolver(final JEditorPaneOperator txtOper, final int oldLength) {
        log("");
        log("oldLength:" + oldLength);
        ValueResolver fileLengthValueResolver = new ValueResolver() {

            public Object getValue() {
                int newLength = txtOper.getDocument().getLength();
                log("newLength:" + newLength);
                return (newLength == oldLength) ? Boolean.TRUE : Boolean.FALSE;
            }
        };

        return fileLengthValueResolver;
    }

    protected void resetCounter() {
        index = 0;
    }
}
