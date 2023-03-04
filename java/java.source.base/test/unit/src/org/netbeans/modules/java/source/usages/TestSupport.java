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

package org.netbeans.modules.java.source.usages;

import java.awt.EventQueue;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
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

/**
 * Help set up org.netbeans.api.project.*Test.
 * @author Jesse Glick
 */
public final class TestSupport {
    
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
