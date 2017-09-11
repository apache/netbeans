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
