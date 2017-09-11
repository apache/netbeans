/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.updater;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public final class UpdaterInternal {
    public static final String RUNNING = "RUNNING"; // NOI18N
    public static final String FINISHED = "FINISHED"; // NOI18N
    
    private UpdaterInternal() {
    }
    
    public static void update(final Collection<File> files, final PropertyChangeListener l, String branding) throws InterruptedException {
        Localization.setBranding (branding);
        
        class Context implements UpdatingContext {
            private Map<File,Long> modified = new HashMap<File,Long>();
            private String label;
            
            @Override
            public Collection<File> forInstall() {
                return files;
            }

            @Override
            public boolean isFromIDE() {
                return true;
            }

            @Override
            public void unpackingIsRunning () {
                firePropertyChange (RUNNING, null, label);
            }

            @Override
            public void unpackingFinished() {
                runningFinished();
            }

            @Override
            public void runningFinished() {
                firePropertyChange (FINISHED, null, modified);
            }

            @Override
            public void setProgressValue(long bytesRead) {
            }

            @Override
            public void setLabel(String string) {
                label = string;
            }

            @Override
            public void setProgressRange(long i, long totalLength) {
            }

            @Override
            public void disposeSplash() {
            }

            private void firePropertyChange(String name, Object oldV, Object newV) {
                if (l != null) {
                    PropertyChangeEvent ev = new PropertyChangeEvent(this, name, oldV, newV);
                    l.propertyChange(ev);
                }
            }

            @Override
            public OutputStream createOS(final File file) throws FileNotFoundException {
                if (modified.get(file) == null) {
                    modified.put(file, file.lastModified());
                }
                XMLUtil.LOG.log(Level.FINE, "Creating output stream for {0}", file);
                return new FileOutputStream(file) {
                    boolean closed;
                    
                    @Override
                    public void close() throws IOException {
                        if (closed) {
                            return;
                        }
                        closed = true;
                        XMLUtil.LOG.log(Level.FINE, "Closing output stream for {0}", file);
                        super.close();
                        XMLUtil.LOG.log(
                            Level.INFO, "File installed {0}@{1}", 
                            new Object[] { file, modified.get(file) }
                        );
                    }
                };
            }
        }

        Context c = new Context();
        ModuleUpdater mu = new ModuleUpdater(c);
        mu.start();
        mu.join();
    }    
}
