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
package org.openide.text;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import org.netbeans.junit.NbTestCase;
import org.openide.text.CloneableEditorSupport.Env;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.test.MockLookup;
import org.openide.windows.CloneableOpenSupport;

/**
 *
 * @author alsimon
 */
public class XMLCESTest extends NbTestCase implements Env, InstanceContent.Convertor<String,CloneableEditorSupport> {
    static {
        System.setProperty ("org.openide.util.Lookup", "org.openide.text.DataEditorSupportTest$Lkp");
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    private AbstractLookup lkp;

    public XMLCESTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.init();
        final InstanceContent ic = new InstanceContent();
        lkp = new AbstractLookup(ic);
        ic.add("", this);
    }

    public void testCES() throws Exception {
        MockLookup.setInstances(new XMLCESTest.Redirector());

        CloneableEditorSupport ces = lkp.lookup(CloneableEditorSupport.class);
        assertNotNull("CES found", ces);
    }

    @Override
    public CloneableEditorSupport convert(String obj) {
        return new CES(this, lkp);
    }

    @Override
    public Class<? extends CloneableEditorSupport> type(String obj) {
        return CloneableEditorSupport.class;
    }

    @Override
    public String id(String obj) {
        return "myId";
    }

    @Override
    public String displayName(String obj) {
        return "myName";
    }

    @Override
    public InputStream inputStream() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public OutputStream outputStream() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Date getTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getMimeType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void addVetoableChangeListener(VetoableChangeListener l) {
    }

    @Override
    public void removeVetoableChangeListener(VetoableChangeListener l) {
    }

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isModified() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void markModified() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unmarkModified() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CloneableOpenSupport findCloneableOpenSupport() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public static final class Redirector extends CloneableEditorSupportRedirector {
    
        @Override
        protected CloneableEditorSupport redirect(Lookup ces) {
            return ces.lookup(CloneableEditorSupport.class);
        }
    }
    
    static final class CES extends CloneableEditorSupport {
        public CES(Env env, Lookup l) {
            super(env, l);
            setMIMEType("text/xml");
        }
        
        
        @Override
        protected String messageSave() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String messageName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String messageToolTip() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String messageOpening() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String messageOpened() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
