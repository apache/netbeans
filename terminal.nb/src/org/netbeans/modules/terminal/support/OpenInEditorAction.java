/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
                    lc = (LineCookie) dobj.getLookup().lookup(LineCookie.class);
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
