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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.openide.text;


import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JEditorPane;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.lookup.Lookups;

/** Testing the behavior of editor reusal framework. It uses new Line.show API.
 * The behavior was discussed thoroughly at issue 94607.
 *
 * @author Petr Nejedly, Marek Slama
 */
public class ReusableEditor2Test extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ReusableEditor2Test.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    CES c1, c2, c3;
    
    /**
     * Test ctor
     * @param testName 
     */
    public ReusableEditor2Test(java.lang.String testName) {
        super(testName);
    }
            
    @Override
    protected int timeOut() {
        return 15000;
    }


    /**
     * Prepares few editors at the test dispoition.
     */
    protected void setUp () {
        c1 = createSupport("c1");
        c2 = createSupport("c2");
        c3 = createSupport("c3");
    }

    /**
     * Closes any precreated editors left open.
     */
    @Override
    protected void tearDown() {
        forceClose(c1);
        forceClose(c2);
        forceClose(c3);
    }
    
    /**
     * Test that verifies ShowOpenType.REUSE closes original tab (keeps only one)
     * Scenario:
     * 1. Open first file with ShowOpenType.REUSE
     * 2. Open second file with ShowOpenType.REUSE
     * 3. Verify first is closed
     * 4. Open first file with ShowOpenType.REUSE
     * 5. Verify second is closed
     */
    public void testReuse() {
        openAndCheck(c1, Line.ShowOpenType.REUSE, Line.ShowVisibilityType.NONE); // 1
        openAndCheck(c2, Line.ShowOpenType.REUSE, Line.ShowVisibilityType.NONE); // 2
        assertClosed(c1); // 3
        openAndCheck(c1, Line.ShowOpenType.REUSE, Line.ShowVisibilityType.NONE); // 4
        assertClosed(c2); // 5
    }
    
    /** Test that verifies ShowOpenType.REUSE doesn't reuse modified, even saved tab
     * 1. Open first file with ShowOpenType.REUSE
     * 2. Modify it
     * 3. Open second file with ShowOpenType.REUSE
     * 4. Verify first still open
     * 5. Modify second file
     * 6. Unmodify second file
     * 7. Open third file with ShowOpenType.REUSE
     * 8. Verify second still open
     */
    public void testKeepTouched() {
        openAndCheck(c1, Line.ShowOpenType.REUSE, Line.ShowVisibilityType.NONE); // 1
        c1.notifyModified(); // 2
        openAndCheck(c2, Line.ShowOpenType.REUSE, Line.ShowVisibilityType.NONE); // 3
        assertOpened(c1); // 4
        c2.notifyModified(); // 5
        c2.notifyUnmodified(); // 6
        openAndCheck(c3, Line.ShowOpenType.REUSE, Line.ShowVisibilityType.NONE); // 7
        assertOpened(c2); // 8
        assertOpened(c1);
    }
    
    /** Test that verifies ShowOpenType.REUSE don't consider non-reusable tabs.
     * There are three things tested:
     * A) Don't replace ordinary tabs
     * B) Don't mark ordinary tabs as reusable if switched to
     * C) Keep reusable tab mark even through (B)
     * 
     * Scenario:
     * 1. Open first file using ShowOpenType.OPEN and ShowVisibilityType.FOCUS
     * 2. Open second file using ShowOpenType.REUSE
     * 3. Verify first still opened (A)
     * 4. open first using ShowOpenType.REUSE
     * 5. verify second still opened
     * 6. open third file using ShowOpenType.REUSE
     * 7. verify first still opened (B)
     * 8. verify second closed (C)
     */
    public void testLeaveNonreusable() {
        openAndCheck(c1, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS); // 1
        openAndCheck(c2, Line.ShowOpenType.REUSE, Line.ShowVisibilityType.NONE); // 2
        assertOpened(c1); // 3
        
        openAndCheck(c1, Line.ShowOpenType.REUSE, Line.ShowVisibilityType.NONE); // 4
        assertOpened(c2); // 5
        openAndCheck(c3, Line.ShowOpenType.REUSE, Line.ShowVisibilityType.NONE); // 6
        assertOpened(c1); // 7
        
        assertClosed(c2); // 8
    }
    
    /** Test that verifies ShowOpenType.REUSE_NEW don't close existing reusable tab,
     * but can be reused itself
     * 
     * Scenario:
     * 1. Open first file using ShowOpenType.REUSE
     * 2. Open second file using ShowOpenType.REUSE_NEW
     * 3. Verify first still opened
     * 4. Open third using ShowOpenType.REUSE
     * 5. verify second closed
     */
    public void testReuseNewKeepsOld() {
        openAndCheck(c1, Line.ShowOpenType.REUSE, Line.ShowVisibilityType.NONE); // 1
        openAndCheck(c2, Line.ShowOpenType.REUSE_NEW, Line.ShowVisibilityType.NONE); // 2
        assertOpened(c1); // 3
        openAndCheck(c3, Line.ShowOpenType.REUSE, Line.ShowVisibilityType.NONE); // 4
        assertClosed(c2); // 5
    }

    /**
     * Test that specifies behaviour of ShowOpenType.REUSE_NEW in case currently
     * reusable tab is not the selected one.
     * 
     * Scenario:
     * 1. Open first file using ShowOpenType.REUSE
     * 2. Open second file using ShowOpenType.OPEN + ShowVisibilityType.FOCUS
     * 3. Open third file using ShowOpenType.REUSE_NEW
     * 4. Verify first still open.
     */
    public void testReuseNewKeepsOldEvenWhenNotFocused() {
        openAndCheck(c1, Line.ShowOpenType.REUSE, Line.ShowVisibilityType.NONE); // 1
        openAndCheck(c2, Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS); // 2
        openAndCheck(c3, Line.ShowOpenType.REUSE_NEW, Line.ShowVisibilityType.NONE); // 3
        assertOpened(c1); // 4
    }
     
    private CES createSupport(String txt) {
        Env env = new Env();
        env.content = txt;
        CES c = new CES(env, Lookups.singleton(txt));
        env.support = c;
        return c;
    }
    
    private void openAndCheck(final CES ces, final Line.ShowOpenType openType, final Line.ShowVisibilityType visibilityType) {
        Mutex.EVENT.readAccess(new Mutex.Action<Void>() {
            public Void run() {
                ces.getLineSet().getCurrent(0).show(openType, visibilityType);
                return null;
            }

        });
        assertOpened(ces);
    }

    private void forceClose(CES ces) {
        if (ces.isModified()) ces.notifyUnmodified();
        ces.close();
    }

    private void assertClosed(CES ces) {
        assertEquals(0, getOpenedCount(ces));
    }

    private void assertOpened(CES ces) {
        assertEquals(1, getOpenedCount(ces));
    }

    private int getOpenedCount(final CES ces) {
        return Mutex.EVENT.readAccess(new Mutex.Action<Integer>() {
            public Integer run() {
                JEditorPane[] panes = ces.getOpenedPanes();
                return panes == null ? 0 : panes.length;
            }
        });
    }
    
    
    
    //
    // Implementation of the CloneableEditorSupport.Env
    //
    private class Env implements CloneableEditorSupport.Env {
        // Env variables
        private String content = "";
        private boolean valid = true;
        private boolean modified = false;
        private java.util.Date date = new java.util.Date ();
        private List<PropertyChangeListener> propL = new ArrayList<PropertyChangeListener>();
        private java.beans.VetoableChangeListener vetoL;
        /** the support to work with */
        CloneableEditorSupport support;

        public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
            propL.add (l);
        }    
        public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
            propL.remove (l);
        }

        public synchronized void addVetoableChangeListener(java.beans.VetoableChangeListener l) {
            assertNull ("This is the first veto listener", vetoL);
            vetoL = l;
        }
        public void removeVetoableChangeListener(java.beans.VetoableChangeListener l) {
            assertEquals ("Removing the right veto one", vetoL, l);
            vetoL = null;
        }
    
        public org.openide.windows.CloneableOpenSupport findCloneableOpenSupport() {
            return support;
        }
    
        public String getMimeType() {
            return "text/plain";
        }
    
        public java.util.Date getTime() {
            return date;
        }
    
        public java.io.InputStream inputStream() throws java.io.IOException {
            return new java.io.ByteArrayInputStream (content.getBytes ());
        }
        public java.io.OutputStream outputStream() throws java.io.IOException {
            class ContentStream extends java.io.ByteArrayOutputStream {
                public void close () throws java.io.IOException {
                    super.close ();
                    content = new String (toByteArray ());
                }
            }

            return new ContentStream ();
        }

        public boolean isValid() {
            return valid;
        }

        public boolean isModified() {
            return modified;
        }

        public void markModified() throws java.io.IOException {
            modified = true;
        }

        public void unmarkModified() {
            modified = false;
        }
    }
    
    /** Implementation of the CES */
    private static final class CES extends CloneableEditorSupport {
        public CES (Env env, Lookup l) {
            super (env, l);
        }
        
        protected String messageName() {
            return "Name";
        }
        
        protected String messageOpened() {
            return "Opened";
        }
        
        protected String messageOpening() {
            return "Opening";
        }
        
        protected String messageSave() {
            return "Save";
        }
        
        protected String messageToolTip() {
            return "ToolTip";
        }
        
    }

}
