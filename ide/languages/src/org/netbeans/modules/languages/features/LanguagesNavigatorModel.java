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

package org.netbeans.modules.languages.features;

import java.util.LinkedList;
import javax.swing.event.TreeModelListener;
import javax.swing.text.StyledDocument;
import javax.swing.tree.TreeModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.ParserManagerListener;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.text.NbDocument;


/**
 *
 * @author Jan Jancura
 */
class LanguagesNavigatorModel implements TreeModel {

    private NbEditorDocument            document;
    private ASTNode                     astNode;
    private NavigatorNode               root;
    private EventListenerList           listenerList = new EventListenerList ();
    private static NavigatorComparator  navigatorComparator;


    // TreeModel implementation ............................................

    LanguagesNavigatorModel () {
        root = new NavigatorNode (
            "","", null, true
        );
    }
    
    public Object getRoot () {
        return root;
    }

    public Object getChild (Object parent, int index) {
        return ((NavigatorNode) parent).getNodes (null).get (index);
    }

    public int getChildCount (Object parent) {
        return ((NavigatorNode) parent).getNodes (null).size ();
    }

    public boolean isLeaf (Object node) {
        return ((NavigatorNode) node).getNodes (null).isEmpty ();
    }

    public void valueForPathChanged (TreePath path, Object newValue) {
    }

    public int getIndexOfChild (Object parent, Object child) {
        return ((NavigatorNode) parent).getNodes (null).indexOf (child);
    }

    public void addTreeModelListener (TreeModelListener l) {
        listenerList.add (TreeModelListener.class, l);
    }

    public void removeTreeModelListener (TreeModelListener l) {
        listenerList.remove (TreeModelListener.class, l);
    }


    // other methods .......................................................

    void setContext (
        NbEditorDocument    doc
    ) {
        this.document = doc;
        if (doc == null) {
            root = new NavigatorNode ("", "", null, true);
            astNode = null;
            setParserManager (null);
            fire ();
            return;
        }
        ParserManagerImpl parserManager = ParserManagerImpl.getImpl (doc);
        setParserManager (parserManager);
        refreshASTNode ();
    }
    
    private ParserListener      parserListener;
    private ParserManagerImpl   parserManager;
    
    private void setParserManager (ParserManagerImpl parserManager) {
        if (parserManager == this.parserManager) return;
        if (parserListener == null)
            parserListener = new ParserListener ();
        if (this.parserManager != null)
            this.parserManager.removeListener (parserListener);
        if (parserManager != null)
            parserManager.addListener (parserListener);
        this.parserManager = parserManager;
    }
    
    private void refreshASTNode () {
        astNode = parserManager.getAST ();
        if (astNode == null) {
            root = new NavigatorNode ("", "", null, true);
            fire ();
        } else {
            List<ASTItem> path = new ArrayList<ASTItem> ();
            path.add (astNode);
            List rootNodes = root.getNodes(this);
            if (root instanceof ASTNavigatorNode && ((ASTNavigatorNode)root).document == document &&
                    rootNodes != null && rootNodes.size() > 0) {
                if (parserManager.getState () == State.PARSING) return;
                ASTNavigatorNode newASTNode = new ASTNavigatorNode (document, astNode, path, "Root", "", null, false);
                ((ASTNavigatorNode)root).refreshNode(this, newASTNode, new LinkedList<ASTNavigatorNode>());
            } else {
                ASTNavigatorNode newASTNode = new ASTNavigatorNode (document, astNode, path, "Root", "", null, false);
                newASTNode.getNodes (this);
                if (parserManager.getState () == State.PARSING) return;
                root = newASTNode;
                fire();
            }
        }
    }
    
    private void fire () {
        TreeModelListener[] listeners = listenerList.getListeners (TreeModelListener.class);
        if (listeners.length == 0) return;
        TreeModelEvent e = new TreeModelEvent (this, new Object[] {getRoot ()});
        for (int i = 0; i < listeners.length; i++)
            listeners [i].treeStructureChanged (e);
    }

    private void fireRemove(ASTNavigatorNode node, int[] indices, ASTNavigatorNode[] children,
            LinkedList<ASTNavigatorNode> nodePath) {
        TreeModelListener[] listeners = listenerList.getListeners (TreeModelListener.class);
        if (listeners.length == 0) return;
        TreePath path = new TreePath(nodePath.toArray());
        TreeModelEvent e = new TreeModelEvent (this, path, indices, children);
        for (int i = 0; i < listeners.length; i++)
            listeners [i].treeNodesRemoved (e);
    }
    
    private void fireInsert(ASTNavigatorNode node, int[] indices, ASTNavigatorNode[] children,
            LinkedList<ASTNavigatorNode> nodePath) {
        TreeModelListener[] listeners = listenerList.getListeners (TreeModelListener.class);
        if (listeners.length == 0) return;
        TreePath path = new TreePath(nodePath.toArray());
        TreeModelEvent e = new TreeModelEvent (this, path, indices, children);
        for (int i = 0; i < listeners.length; i++)
            listeners [i].treeNodesInserted (e);
    }
    
    private boolean cancel () {
        return parserManager.getState () == State.PARSING;
    }

    TreePath getTreePath (int position) {
        if (astNode == null) return null;
        if (!(root instanceof ASTNavigatorNode)) return null;
        ASTPath astPath = astNode.findPath (position);
        if (astPath == null) return null;
        List<ASTNavigatorNode> nodePath = new ArrayList<ASTNavigatorNode> ();
        ASTNavigatorNode n = (ASTNavigatorNode) root;
        Iterator<ASTItem> it = astPath.listIterator ();
        if (it.next () != n.item) return null;
        nodePath.add (n);
        while (it.hasNext ()) {
            ASTItem astItem = it.next ();
            Iterator<ASTNavigatorNode> it2 = n.getNodes (null).iterator ();
            while (it2.hasNext ()) {
                ASTNavigatorNode nn = it2.next ();
                if (nn.item != astItem) continue;
                n = nn;
                nodePath.add (nn);
                break;
            }
        }
        if (nodePath.isEmpty ()) return null;
        return new TreePath (nodePath.toArray ());
    }
    
    
    
    String getTooltip (Object node) {
        return ((NavigatorNode) node).tooltip;
    }

    void show (Object node) {
        ((NavigatorNode) node).show ();
    }
    
    String getIcon (Object node) {
        return ((NavigatorNode) node).icon;
    }
    
    String getDisplayName (Object node) {
        return ((NavigatorNode) node).displayName;
    }


    // innerclasses ........................................................

    class ParserListener implements ParserManagerListener {
        
        public void parsed (State state, ASTNode ast) {
            if (state == State.PARSING) return;
            refreshASTNode ();
        }
    }

    static class NavigatorNode {

        String          displayName;
        String          tooltip;
        String          icon;
        boolean         isLeaf;

        /** Creates a new instance of NavigatorNode */
        NavigatorNode (
            String          displayName,
            String          tooltip,
            String          icon,
            boolean         isLeaf
        ) {
            this.displayName = displayName;
            this.tooltip =  tooltip;
            this.icon =     icon;
            this.isLeaf =   isLeaf;
        }
        
        void show () {}
        
        List<ASTNavigatorNode> getNodes (LanguagesNavigatorModel model) {
            return Collections.<ASTNavigatorNode>emptyList ();
        }

        boolean compareNodes(NavigatorNode nodeA, NavigatorNode nodeB) {
            if (nodeA.displayName == null) {
                if (nodeB.displayName != null) {
                    return false;
                } // if
            } else if (!nodeA.displayName.equals(nodeB.displayName)) {
                return false;
            }
            if (nodeA.icon == null) {
                if (nodeB.icon != null) {
                    return false;
                } // if
            } else if (!nodeA.icon.equals(nodeB.icon)) {
                return false;
            }
            return nodeA.isLeaf == nodeB.isLeaf;
        }

    }
    
    static class ASTNavigatorNode extends NavigatorNode {

        ASTItem         item;
        List<ASTItem>   path;
        private StyledDocument document;

        /** Creates a new instance of NavigatorNode */
        ASTNavigatorNode (
            StyledDocument  document,
            ASTItem         item,
            List<ASTItem>   path,
            String          displayName,
            String          tooltip,
            String          icon,
            boolean         isLeaf
        ) {
            super (displayName, tooltip, icon, isLeaf);
            this.document = document;
            this.item =     item;
            this.path =     path;
        }
        
        void show () {
            DataObject dataObject = NbEditorUtilities.getDataObject (document);
            LineCookie lineCookie = dataObject.getCookie (LineCookie.class);
            Line.Set lineSet = lineCookie.getLineSet ();
            Line line = lineSet.getCurrent (NbDocument.findLineNumber (document, item.getOffset ()));
            int column = NbDocument.findLineColumn (document, item.getOffset ());
            line.show (ShowOpenType.OPEN, ShowVisibilityType.FOCUS, column);
        }

        private List<ASTNavigatorNode> nodes;
        
        List<ASTNavigatorNode> getNodes (LanguagesNavigatorModel model) {
            if (nodes != null) return nodes;
            if (isLeaf)
                return nodes = Collections.<ASTNavigatorNode>emptyList ();
            nodes = new ArrayList<ASTNavigatorNode> ();
            getNavigatorNodes (
                item, 
                new ArrayList<ASTItem> (path), 
                nodes,
                model
            );
            Language language = (Language) item.getLanguage ();
            if (language != null) {
                Feature properties = language.getFeatureList ().getFeature ("PROPERTIES");
                if (properties != null &&
                    properties.getBoolean ("navigator-sort", false)
                ) {
                    if (navigatorComparator == null)
                        navigatorComparator = new NavigatorComparator ();
                    Collections.<ASTNavigatorNode>sort (nodes, navigatorComparator);
                }
            }
            return nodes;
        }

        private void refreshNode(LanguagesNavigatorModel model, ASTNavigatorNode newNode,
                LinkedList<ASTNavigatorNode> nodePath) {
            item = newNode.item;
            path = newNode.path;
            if (nodes == null) {
                return;
            }
            
            nodePath.add(this);
            List<ASTNavigatorNode> newChildren = newNode.getNodes(model);
            List<ASTNavigatorNode> newNodes = new ArrayList<ASTNavigatorNode>(newChildren.size());
            
            int index = 0;
            int lastIndex = 0;
            int insertPos = 0;
            List<Integer> removed = new ArrayList<Integer>();
            List<Integer> inserted = new ArrayList<Integer>();
            for (ASTNavigatorNode node : newChildren) {
                ASTNavigatorNode found = null;
                for (int x = index; x < nodes.size(); x++) {
                    if (compareNodes(node, nodes.get(x))) {
                        found = nodes.get(x);
                        index = x + 1;
                        break;
                    }
                }
                if (found != null) {
                    newNodes.add(found);
                    for (int x = lastIndex; x < index - 1; x++) {
                        removed.add(x);
                    }
                    lastIndex = index;
                    found.refreshNode(model, node, nodePath);
                } else {
                    newNodes.add(node);
                    inserted.add(insertPos);
                }
                insertPos++;
            } // for
            for (int x = index; x < nodes.size(); x++) {
                removed.add(x);
            }
            
            int[] removedIndices = new int[removed.size()];
            ASTNavigatorNode[] removedNodes = new ASTNavigatorNode[removed.size()];
            for (int x = 0; x < removedIndices.length; x++) {
                removedIndices[x] = removed.get(x);
                removedNodes[x] = nodes.get(removedIndices[x]);
            }
            
            int[] insertedIndices = new int[inserted.size()];
            ASTNavigatorNode[] insertedNodes = new ASTNavigatorNode[inserted.size()];
            for (int x = 0; x < insertedIndices.length; x++) {
                insertedIndices[x] = inserted.get(x);
                insertedNodes[x] = newChildren.get(insertedIndices[x]);
            }
            
            nodes = newNodes;
            
            if (removedIndices.length > 0) {
                model.fireRemove(this, removedIndices, removedNodes, nodePath);
            }
            if (insertedIndices.length > 0) {
                model.fireInsert(this, insertedIndices, insertedNodes, nodePath);
            }
            
            nodePath.removeLast();
        }
        
        private void getNavigatorNodes (
            ASTItem                     item, 
            List<ASTItem>               path, 
            List<ASTNavigatorNode>      nodes,
            LanguagesNavigatorModel     model

        ) {
            Iterator<ASTItem> it = item.getChildren ().iterator ();
            while (it.hasNext ()) {
                if (model != null && model.cancel ()) {
                    //S ystem.out.println("cancelled");
                    return;
                }
                ASTItem item2 = it.next ();
                path.add (item2);
                ASTNavigatorNode navigatorNode = createNavigatorNode (
                    item2,
                    path
                );
                if (navigatorNode != null) 
                    nodes.add (navigatorNode);
                else
                    getNavigatorNodes (item2, path, nodes, model);
                path.remove (path.size () - 1);
            }
            return;
        }

        private ASTNavigatorNode createNavigatorNode (
            ASTItem             item,
            List<ASTItem>       path
        ) {
            ASTPath astPath = ASTPath.create (path);
            Feature navigator = null;
            Language language = (Language) item.getLanguage ();
            if (language == null) return null;
            navigator = language.getFeatureList ().getFeature ("NAVIGATOR", astPath);
            if (navigator == null) return null;
            Context context = SyntaxContext.create (document, astPath);
            String displayName = (String) navigator.getValue ("display_name", context);
            if (displayName == null || displayName.trim().length() == 0) {
                return null;
            }
            String tooltip = (String) navigator.getValue ("tooltip", context);
            String icon = (String) navigator.getValue ("icon", context);
            if (icon == null)
                icon = "org/netbeans/modules/languages/resources/node.gif";
            boolean isLeaf = navigator.getBoolean ("isLeaf", context, false);
            return new ASTNavigatorNode (
                document,
                item,
                new ArrayList<ASTItem> (path),
                displayName, tooltip, icon,
                isLeaf
            );
        }
    }
    
    static class NavigatorComparator implements Comparator<NavigatorNode> {
        public int compare (NavigatorNode o1, NavigatorNode o2) {
            return o1.displayName.compareToIgnoreCase (o2.displayName);
        }
    }
}
