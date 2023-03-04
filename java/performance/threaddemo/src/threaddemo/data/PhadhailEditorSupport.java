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

package threaddemo.data;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.openide.windows.CloneableOpenSupport;
import threaddemo.model.Phadhail;

/**
 * An editor for phadhails.
 * @author Jesse Glick
 */
final class PhadhailEditorSupport extends CloneableEditorSupport implements OpenCookie, CloseCookie, EditorCookie.Observable, PrintCookie {
    
    private final Phadhail ph;
    private SaveCookie modified = null;
    
    public PhadhailEditorSupport(Phadhail ph) {
        this(ph, new PhadhailEnv(ph));
    }
    
    private PhadhailEditorSupport(Phadhail ph, PhadhailEnv env) {
        super(env);
        if (ph.hasChildren()) throw new IllegalArgumentException();
        this.ph = ph;
        env.associate(this);
    }
    
    protected String messageName() {
        // XXX should listen for PhadhailNameEvent too
        String n = ph.getName();
        if (modified != null) {
            n += " *";
        }
        return n;
    }
    
    protected String messageOpened() {
        return "Opened " + ph.getPath() + ".";
    }
    
    protected String messageOpening() {
        return "Opening " + ph.getPath() + "...";
    }
    
    protected String messageSave() {
        return "Saved " + ph.getPath() + ".";
    }
    
    protected String messageToolTip() {
        return ph.getPath();
    }
    
    /**
     * Same as default behavior in CES except solves a deadlock.
     * The problem arises e.g. when first calling openDocument while
     * holding a write lock: the prepareDocument task is posted to RP,
     * where it starts to run but then Env.inputStream blocks getting
     * a read lock.
     */
    public Task prepareDocument() {
        // XXX hack no longer works after patch to CES; pD() not called from oD() + gD()
        ((PhadhailEnv)env).preloadInputStream();
        Task t = super.prepareDocument();
        t.addTaskListener(new TaskListener() {
            public void taskFinished(Task t) {
                ((PhadhailEnv)env).forgetPreloadedInputStream();
            }
        });
        return t;
    }
    
    private static final class PhadhailEnv implements CloneableEditorSupport.Env {
        
        private final Phadhail ph;
        private PhadhailEditorSupport supp;
        private final long createTime;
        private byte[] preloadedContents = null;
        
        public PhadhailEnv(Phadhail ph) {
            this.ph = ph;
            createTime = System.currentTimeMillis();
        }
        
        void associate(PhadhailEditorSupport supp) {
            this.supp = supp;
        }
        
        public CloneableOpenSupport findCloneableOpenSupport() {
            return supp;
        }
        
        public String getMimeType() {
            return "text/plain";
        }
        
        public Date getTime() {
            return new Date(createTime);
        }
        
        public void preloadInputStream() {
            if (preloadedContents == null) {
                try {
                    InputStream is = ph.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(is.available());
                    byte[] buf = new byte[4096];
                    int read;
                    while ((read = is.read(buf)) != -1) {
                        baos.write(buf, 0, read);
                    }
                    preloadedContents = baos.toByteArray();
                } catch (IOException e) {
                    // ignore, will show up later
                    e.printStackTrace();
                }
            }
        }
        
        public void forgetPreloadedInputStream() {
            preloadedContents = null;
        }
        
        public InputStream inputStream() throws IOException {
            if (preloadedContents != null) {
                return new ByteArrayInputStream(preloadedContents);
            } else {
                return ph.getInputStream();
            }
        }
        
        public OutputStream outputStream() throws IOException {
            return ph.getOutputStream();
        }
        
        public boolean isModified() {
            return supp.modified != null;
        }
        
        public boolean isValid() {
            // XXX a better model for Phadhail would have an isValid method
            // and deleting it would set it to false...
            return true;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }
        
        public void addVetoableChangeListener(VetoableChangeListener l) {
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
        
        public void removeVetoableChangeListener(VetoableChangeListener l) {
        }
        
        public void markModified() throws IOException {
            // For some mysterious reason this can be called repeatedly?
            //assert supp.modified == null;
            if (supp.modified == null) {
                supp.modified = new Save();
                PhadhailLookups.modified(ph, supp.modified);
            }
        }
        
        private final class Save implements SaveCookie {
            
            public void save() throws IOException {
                supp.saveDocument();
            }
            
            public String toString() {
                return "Save[" + ph + "]";
            }
            
        }
        
        public void unmarkModified() {
            if (supp.modified != null) {
                PhadhailLookups.saved(ph, supp.modified);
                supp.modified = null;
            }
        }
        
    }
    
}
