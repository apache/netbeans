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

package org.netbeans.modules.project.ui.actions;

import java.awt.EventQueue;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.KeyStroke;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.ProjectsRootNode;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static junit.framework.TestCase.*;

/**
 * Help set up org.netbeans.api.project.*Test.
 * @author Jesse Glick
 */
public final class TestSupport {
    public static interface ActionCreator {
        public LookupSensitiveAction create(Lookup l);
    }
    
    public static void doTestAcceleratorsPropagated(ActionCreator creator, boolean testMenus) {
        Lookup l1 = Lookups.fixed(new Object[] {"1"});
        Lookup l2 = Lookups.fixed(new Object[] {"2"});
        
        Action a1 = creator.create(l1);
        
        KeyStroke k1 = KeyStroke.getKeyStroke("shift pressed A");
        KeyStroke k2 = KeyStroke.getKeyStroke("shift pressed A");
        
        assertNotNull(k1);
        assertNotNull(k2);
        
        a1.putValue(Action.ACCELERATOR_KEY, k1);
        
        LookupSensitiveAction a2 = creator.create(l2);
        
        assertEquals(k1, a2.getValue(Action.ACCELERATOR_KEY));
        
        a2.putValue(Action.ACCELERATOR_KEY, k2);
        
        assertEquals(k2, a1.getValue(Action.ACCELERATOR_KEY));
        
        if (testMenus) {
            assertEquals(k2, a2.getMenuPresenter().getAccelerator());
        }

        a1.putValue(Action.ACCELERATOR_KEY, k1);
        assertEquals(k1, a2.getValue(Action.ACCELERATOR_KEY));
        
        if (testMenus) {
            assertEquals(k1, a2.getMenuPresenter().getAccelerator());
        }
    }
    
    public static FileObject createTestProject( FileObject workDir, String name ) throws IOException {
        FileObject p = workDir.createFolder( name );
        p.createFolder( "testproject" );
        return p;
    }
            
    public static void notifyDeleted(Project p) {
        ((TestProject) p).state.notifyDeleted();
    }
        
    /**
     * A testing project factory which recognizes directories containing
     * a subdirectory called "testproject".
     * If that subdirectory contains a file named "broken" then loading the project
     * will fail with an IOException.
     */
    public static final class TestProjectFactory implements ProjectFactory {
        
        public TestProjectFactory() {}
        
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            FileObject testproject = projectDirectory.getFileObject("testproject");
            if (testproject != null && testproject.isFolder()) {
                return new TestProject(projectDirectory, state);
            }
            else {
                return null;
            }
        }
        
        public void saveProject(Project project) throws IOException, ClassCastException {
            TestProject p = (TestProject)project;
            Throwable t = p.error;
            if (t != null) {
                p.error = null;
                if (t instanceof IOException) {
                    throw (IOException)t;
                } else if (t instanceof Error) {
                    throw (Error)t;
                } else {
                    throw (RuntimeException)t;
                }
            }
        }
        
        public boolean isProject(FileObject dir) {
            FileObject testproject = dir.getFileObject("testproject");
            return testproject != null && testproject.isFolder();
        }
        
    }
    
    public static final class TestProject implements Project {

        static {
            // Suppress warning about missing LogicalViewProvider.
            Logger.getLogger(ProjectsRootNode.class.getName()).setLevel(Level.SEVERE);
        }
        
        private Lookup lookup;
        private final FileObject dir;
        final ProjectState state;
        Throwable error;
        int saveCount = 0;
        
        public TestProject(FileObject dir, ProjectState state) {
            this.dir = dir;
            this.state = state;
        }
        
        public void setLookup( Lookup lookup ) {
            this.lookup = lookup;
        }
        
        public Lookup getLookup() {
            if ( lookup == null ) {
                return Lookup.EMPTY;
            }
            else {
                return lookup;
            }
        }
        
        public FileObject getProjectDirectory() {
            return dir;
        }
        
        @Override
        public String toString() {
            return "testproject:" + getProjectDirectory().getNameExt();
        }
        
    }
        
    public static class ChangeableLookup extends ProxyLookup implements Runnable {
        
        public ChangeableLookup(Object... objects) {
            super( new Lookup[] { Lookups.fixed( objects ) } );
        }
        
        public void change(Object... objects) throws InterruptedException, InvocationTargetException {
            setLookups( new Lookup[] { Lookups.fixed( objects ) } );                       
            if (!EventQueue.isDispatchThread()) {
                EventQueue.invokeAndWait(this);
            }
        }

        public void run() {
        }
    }
    
    public static AuxiliaryConfiguration createAuxiliaryConfiguration () {
        return new MemoryAuxiliaryConfiguration ();
    }
    
    private static class MemoryAuxiliaryConfiguration implements AuxiliaryConfiguration {
        private Document xml = XMLUtil.createDocument ("private", "http://www.netbeans.org/ns/test-support-project-private/1", null, null);

        public Element getConfigurationFragment (String elementName, String namespace, boolean shared) {
            if (shared) {
                assert false : "Shared not implemented";
            }
            Element root = xml.getDocumentElement ();
            Element data = XMLUtil.findElement (root, elementName, namespace);
            if (data != null) {
                return  (Element) data.cloneNode (true);
            } else {
                //return xml.createElementNS (namespace, elementName);
                return null;
            }
        }
        
        public void putConfigurationFragment(Element fragment, boolean shared) throws IllegalArgumentException {
            if (shared) {
                assert false : "Shared not implemented";
            }
            
            Element root = xml.getDocumentElement ();
            Element existing = XMLUtil.findElement (root, fragment.getLocalName (), fragment.getNamespaceURI ());
            // XXX first compare to existing and return if the same
            if (existing != null) {
                root.removeChild (existing);
            }
            // the children are alphabetize: find correct place to insert new node
            Node ref = null;
            NodeList list = root.getChildNodes ();
            for (int i=0; i<list.getLength (); i++) {
                Node node  = list.item (i);
                if (node.getNodeType () != Node.ELEMENT_NODE) {
                    continue;
                }
                int comparison = node.getNodeName ().compareTo (fragment.getNodeName ());
                if (comparison == 0) {
                    comparison = node.getNamespaceURI ().compareTo (fragment.getNamespaceURI ());
                }
                if (comparison > 0) {
                    ref = node;
                    break;
                }
            }
            root.insertBefore (root.getOwnerDocument ().importNode (fragment, true), ref);
        }
        
        public boolean removeConfigurationFragment (String elementName, String namespace, boolean shared) throws IllegalArgumentException {
            if (shared) {
                assert false : "Shared not implemented";
            }

            Element root = xml.getDocumentElement ();
            Element data = XMLUtil.findElement (root, elementName, namespace);
            if (data != null) {
                root.removeChild (data);
                return true;
            } else {
                return false;
            }
        }
    }
}
