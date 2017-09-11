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

package org.netbeans.modules.xml.wizard.impl;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import org.netbeans.modules.xml.util.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.xml.sax.InputSource;


/**
 * Represents the collection of files belonging to a common namespace.
 *
 * @author Ajit Bhate
 */
public class NamespaceChildren extends Children.Keys {
    /** Map of namespace to a list of files in that namespace. */
    private java.util.Map nsFilesMap;
    /** Set of folders containing referencable files. */
    private FileObject[] rootFolders;
    private ExternalReferenceDecorator decorator;
    
    /**
     * Creates a new instance of NamespaceChildren.
     *
     * @param  roots      set of root folders.
     * @param  decorator  used to decorate the nodes.
     */
    public NamespaceChildren(FileObject[] roots, ExternalReferenceDecorator decorator) {
        super();
        rootFolders = roots;
        this.decorator=decorator;
        nsFilesMap = new TreeMap();
    }

    protected Node[] createNodes(Object key) {
        if (key == WaitNode.WAIT_KEY) {
            return WaitNode.createNode();
        } else if (key instanceof String) {
            List fobjs = (List)nsFilesMap.get(key);
            if (fobjs != null && !fobjs.isEmpty()) {
                List<Node>filterNodes = new ArrayList<Node>(fobjs.size());
                int i = 0;
                for (int j=0; j < fobjs.size();j++) {
                    try {
                        filterNodes.add(createSchemaNode(fobjs.get(j)));
                    } catch (DataObjectNotFoundException donfe) {
                        // ignore
                    }
                }
                Children.Array children = new Children.Array();
                children.add(filterNodes.toArray(new Node[filterNodes.size()]));
                Node node = new NamespaceNode(children, (String) key);
                return new Node[] { node };
            }
        }
        return new Node[] { };
    }
    
    private Node createSchemaNode(Object o) throws DataObjectNotFoundException {
        boolean catalog = false;
        FileObject fobj;
        
        if (o instanceof FileObject) {
            fobj = (FileObject)o;
        } else if (o instanceof InputSource) {
            InputSource src = (InputSource)o;
            fobj =  Util.toFileObject(src);
            catalog = true;
        } else {
            return null;
        }
        
        Node node = DataObject.find(fobj).getNodeDelegate();
        return decorator.createExternalReferenceNode(node, catalog);
    }

    protected void addNotify() {
        super.addNotify();
        setKeys(WaitNode.getKeys());
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                for (int i =0; i < rootFolders.length;i++) {
                    FileObject root = rootFolders[i];
                    java.util.Map map =Util.getFiles2NSMappingInProj(FileUtil.toFile(root), Util.getDocumentType());
                    java.util.Map nsMap = Util.getCatalogSchemaNSMappings();
                    
                    map.putAll(nsMap);
                    java.util.Set set= map.entrySet();  
                    Iterator it = set.iterator();
                    while(it.hasNext()){
                        java.util.Map.Entry entry = (java.util.Map.Entry)it.next();
                        String ns = (String)entry.getValue();
                        List fobjs = (List)nsFilesMap.get(ns);
                        if (fobjs == null) {
                            fobjs = new ArrayList();
                        }
                        fobjs.add(entry.getKey());
                        nsFilesMap.put(ns, fobjs);
                    }
                }
                // Set the keys on the EDT to avoid clobbering the JTree
                // and causing an AIOOBE (issue 94498).
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        setKeys(nsFilesMap.keySet());
                    }
                });
            }
        });
    }


   // @Override
    protected void removeNotify() {
        setKeys(Collections.emptySet());
    }

    private static class NamespaceNode extends FolderNode {
        /** Controls the appearance of this node. */
        public static final String NO_NAME_SPACE = "NO_NAME_SPACE"; 
        
        NamespaceNode(Children children, String myNamespace) {
            super(children);
            setName(myNamespace);
            if (NO_NAME_SPACE.equals(myNamespace)) {
                setDisplayName("NoTargetNameSpace");
            }
        }

        public String getHtmlDisplayName() {
            String name = getDisplayName();
            return name;
        }

       public String getNamespace() {
            // Our name is our namespace.
            return getName();
        }
    
  }
    
}
