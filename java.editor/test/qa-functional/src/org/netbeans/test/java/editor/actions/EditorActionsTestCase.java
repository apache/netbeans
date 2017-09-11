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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
