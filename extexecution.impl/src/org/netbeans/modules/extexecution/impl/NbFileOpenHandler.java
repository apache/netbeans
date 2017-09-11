/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
