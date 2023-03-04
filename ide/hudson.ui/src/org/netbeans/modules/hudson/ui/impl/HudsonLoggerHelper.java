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
