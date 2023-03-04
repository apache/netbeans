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
package org.netbeans.modules.extexecution.impl;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.spi.extexecution.open.FileOpenHandler;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Petr Hejl
 */
@ServiceProvider(service=FileOpenHandler.class)
public class NbFileOpenHandler implements FileOpenHandler {

    private static final Logger LOGGER = Logger.getLogger(NbFileOpenHandler.class.getName());

    @Override
    public void open(final FileObject file, final int lineno) {
        // FIXME this should not be needed
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        open(file, lineno);
                    }
                });

            return; // not exactly accurate, but....
        }

        try {
            DataObject od = DataObject.find(file);
            EditorCookie ec = od.getCookie(EditorCookie.class);
            LineCookie lc = od.getCookie(LineCookie.class);

            if ((ec != null) && (lc != null)) {
                Document doc = ec.openDocument();

                if (doc != null) {
                    int line = lineno;

                    if (line < 1) {
                        line = 1;
                    }

                    // XXX .size() call is super-slow for large files, see issue
                    // #126531. So we fallback to catching IOOBE
//                    int nOfLines = lines.getLines().size();
//                    if (line > nOfLines) {
//                        line = nOfLines;
//                    }
                    try {
                        Line.Set lines = lc.getLineSet();
                        Line l = lines.getCurrent(line - 1);
                        if (l != null) {
                            l.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS);
                            return;
                        }
                    } catch (IndexOutOfBoundsException ioobe) {
                        // OK, since .size() cannot be used, see above
                    }
                }
            }

            OpenCookie oc = od.getCookie(OpenCookie.class);

            if (oc != null) {
                oc.open();
                return;
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, null, e);
        }
    }
}
