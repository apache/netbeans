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
package org.netbeans.modules.terminal.support;

import java.awt.Toolkit;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.SwingUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author igromov
 */
public final class OpenInEditorAction implements Runnable {

    private static final RequestProcessor RP = new RequestProcessor("Open in Editor"); //NOI18N

    private final URL url;

    private final int lineNumber;
    private LineCookie lc;

    public static void post(URL url, int lineNumber) {
        RP.post(new OpenInEditorAction(url, lineNumber));
    }

    public static void post(String filePath, int lineNumber) {
        try {
            RP.post(new OpenInEditorAction(new URL("file://" + filePath), lineNumber)); //NOI18N
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private OpenInEditorAction(URL url, int lineNumber) {
        this.url = url;
        this.lineNumber = lineNumber;
    }

    @Override
    public void run() {
        if (SwingUtilities.isEventDispatchThread()) {
            doEDT();
        } else {
            doWork();
        }
    }

    private void doEDT() {
        if (lc != null) {
            Line l = lc.getLineSet().getOriginal(lineNumber - 1);
            l.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
        }
    }

    private void doWork() {
        if (url == null) {
            return;
        }
        try {
            FileObject fo;
            if (url.getProtocol().equals("file")) { //NOI18N
                fo = FileUtil.toFileObject(new File(url.getPath()));
            } else {
                fo = URLMapper.findFileObject(url); //NOI18N
            }
            DataObject dobj = DataObject.find(fo);
            EditorCookie ed = dobj.getLookup().lookup(EditorCookie.class);
            if (ed != null && fo == dobj.getPrimaryFile()) {
                if (lineNumber == -1) {
                    ed.open();
                } else {
                    lc = dobj.getLookup().lookup(LineCookie.class);
                    SwingUtilities.invokeLater(this);
                }
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        } catch (Exception ex) {
            // ignore
        }
    }

}
