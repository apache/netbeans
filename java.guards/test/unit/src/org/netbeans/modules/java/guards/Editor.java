/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
