/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.ui.impl;

import java.awt.EventQueue;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.hudson.spi.HudsonLogger;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;

/**
 * Utilities for use by a logger.
 */
public class HudsonLoggerHelper {

    private static final Logger LOG = Logger.getLogger(HudsonLogger.class.getName());

    /**
     * Try to open a file, possibly at a given position.
     *
     * @param f a file to open
     * @param row the row number (zero-based), or -1 for none
     * @param col the column number (zero-based), or -1 for none
     * @param force true to forcibly open the file, false to just scroll if
     * already open
     */
    public static void openAt(FileObject f, int row, final int col, final boolean force) {
        try {
            DataObject d = DataObject.find(f);
            if (row == -1) {
                if (force) {
                    Runnable r;
                    final EditorCookie c = d.getLookup().lookup(EditorCookie.class);
                    if (c != null) {
                        r = new Runnable() {
                            public void run() {
                                try {
                                    c.openDocument();
                                } catch (IOException x) {
                                    LOG.log(Level.INFO, null, x);
                                }
                            }
                        };
                    } else {
                        LOG.fine("no EditorCookie found for " + f);
                        final OpenCookie o = d.getLookup().lookup(OpenCookie.class);
                        if (o == null) {
                            LOG.fine("no OpenCookie found for " + f);
                            return;
                        }
                        r = new Runnable() {
                            public void run() {
                                o.open();
                            }
                        };
                    }
                    EventQueue.invokeLater(r);
                }
                return;
            }
            LineCookie c = d.getLookup().lookup(LineCookie.class);
            if (c == null) {
                LOG.fine("no LineCookie found for " + f);
                openAt(f, -1, -1, force);
                return;
            }
            try {
                final Line l = c.getLineSet().getOriginal(row);
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        l.show(force ? Line.ShowOpenType.REUSE : Line.ShowOpenType.NONE,
                                force ? Line.ShowVisibilityType.FOCUS : Line.ShowVisibilityType.FRONT, col);
                    }
                });
            } catch (IndexOutOfBoundsException x) {
                LOG.log(Level.INFO, null, x);
            }
        } catch (IOException x) {
            LOG.log(Level.INFO, null, x);
        }
    }

}
