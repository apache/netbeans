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
