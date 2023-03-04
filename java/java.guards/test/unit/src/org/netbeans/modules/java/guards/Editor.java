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
package org.netbeans.modules.java.guards;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.openide.text.CloneableEditorSupport;
import org.openide.windows.CloneableOpenSupport;

/**
 * minimal impl of an editor support
 */
final class Editor implements GuardedEditorSupport {
    
    CloneableEditorSupport support = new EditorSupport();
    InputStream is = null;
    StyledDocument doc = null;
    
    /**
     * here you can pass document content
     */
    public void setStringContent(String txt) {
        is = new ByteArrayInputStream(txt.getBytes());
    }
    
    public StyledDocument getDocument() {
        return doc;
    }
    
    class EditorSupport extends CloneableEditorSupport {
        
        EditorSupport() {
            super(new CESEnv());
        }
        
        protected String messageSave() {
            throw new UnsupportedOperationException();
        }
        
        protected String messageName() {
            return "";
        }
        
        protected String messageToolTip() {
            throw new UnsupportedOperationException();
        }
        
        protected String messageOpening() {
            throw new UnsupportedOperationException();
        }
        
        protected String messageOpened() {
            throw new UnsupportedOperationException();
        }
        
        protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit)
        throws IOException, BadLocationException {
            Editor.this.doc = doc;
            kit.read(stream, doc, 0);
        }
        
    }
    
    class CESEnv implements CloneableEditorSupport.Env {
        public InputStream inputStream() throws IOException {
            return Editor.this.is;
        }
        
        public OutputStream outputStream() throws IOException {
            throw new UnsupportedOperationException();
        }
        
        public Date getTime() {
            throw new UnsupportedOperationException();
        }
        
        public String getMimeType() {
            return "text/x-java";
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
        
        public void addVetoableChangeListener(VetoableChangeListener l) {
        }
        
        public void removeVetoableChangeListener(VetoableChangeListener l) {
        }
        
        public boolean isValid() {
            return true;
        }
        
        public boolean isModified() {
            throw new UnsupportedOperationException();
        }
        
        public void markModified() throws IOException {
            throw new UnsupportedOperationException();
        }
        
        public void unmarkModified() {
            throw new UnsupportedOperationException();
        }
        
        public CloneableOpenSupport findCloneableOpenSupport() {
            return Editor.this.support;
        }
        
    }
    
}
