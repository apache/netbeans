/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
                children.add(filterNodes.toArray(new Node[0]));
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
