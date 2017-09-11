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
package org.netbeans.modules.csl.navigation;


import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.core.GsfHtmlFormatter;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.modules.csl.navigation.actions.OpenAction;
import org.netbeans.modules.csl.spi.ParserResult;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/** 
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible. 
 * <p>
 * GSF changes made: Instead of accessing fields on Description object,
 * replace references to Description with StructureItem interface (descriptions
 * supplied by language plugins), make method calls on this interface rather
 * than accessing fields directly. Some data such as the "ui" field was moved
 * into ElementNode itself rather than sitting on the description object which
 * is no longer under our control.
 * <p>
 * Node representing an element
 * 
 * 
 * @author Petr Hrebejk
 */
public class ElementNode extends AbstractNode {

    
    private static Node WAIT_NODE;
    
    private OpenAction openAction;
    private StructureItem description;
    private ClassMemberPanelUI ui;
    private final FileObject fileObject; // For the root description
           
    /** Creates a new instance of TreeNode */
    public ElementNode( StructureItem description, ClassMemberPanelUI ui, FileObject fileObject) {
        super(description.isLeaf() ? Children.LEAF: new ElementChildren(description, ui, fileObject), Lookups.fixed(fileObject));
        this.description = description;
        setDisplayName( description.getName() ); 
        this.ui = ui;
        this.fileObject = fileObject;
    }
    
    @Override
    public Image getIcon(int type) {
        if (description.getCustomIcon() != null) {
            return ImageUtilities.icon2Image(description.getCustomIcon());
        }
        Icon icon = Icons.getElementIcon(description.getKind(), description.getModifiers());
        if (icon != null) {
            return ImageUtilities.icon2Image(icon);
        } else {
            return super.getIcon(type);
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
                   
    @Override
    public java.lang.String getDisplayName() {
        if (description.getName() == null) {
            return fileObject.getNameExt();
        } else {
            return description.getName();
        }
    }
            
    @Override
    public String getHtmlDisplayName() {
        return description.getHtml(new NavigatorFormatter());
    }
    
    @Override
    public Action[] getActions( boolean context ) {
        
        if ( context || description.getName() == null ) {
            return ui.getActions();
        }
        else {
            Action panelActions[] = ui.getActions();
            
            Action actions[]  = new Action[ 2 + panelActions.length ];
            actions[0] = getOpenAction();
            actions[1] = null;
            for( int i = 0; i < panelActions.length; i++ ){
                actions[2 + i] = panelActions[i];
            }
            return actions;
        }
    }        
    
    @Override
    public Action getPreferredAction() {
        return getOpenAction();
    }
    
    
    private synchronized Action getOpenAction() {
        if ( openAction == null ) {
            FileObject fo = ui.getFileObject();
            try {
                openAction = new OpenAction(description.getElementHandle(), fo,
                        description.getPosition());
            } catch (UnsupportedOperationException uo) {
                return null; // root node does not have element handle
            }
        }
        return openAction;
    }
    
    static synchronized Node getWaitNode() {
        if ( WAIT_NODE == null ) {
            WAIT_NODE = new WaitNode();
        }
        return WAIT_NODE;
    }

    /**
     * Refreshes the Node recursively. Only initiates the refresh; the refresh
     * itself may happen asynchronously.
     */
    public void refreshRecursively() {
        List<Node> toExpand = new ArrayList<Node>();
        refreshRecursively(Collections.singleton(this), toExpand);
        ui.performExpansion(toExpand, Collections.<Node>emptyList());
    }

    private void refreshRecursively(Collection<ElementNode> toDo, final Collection<Node> toExpand) {
        for (ElementNode elnod : toDo) {
            final Children ch = elnod.getChildren();
            if ( ch instanceof ElementChildren ) {
                ((ElementChildren)ch).resetKeys((List<StructureItem>)elnod.description.getNestedItems(), elnod.ui.getFilters());

                Collection<ElementNode> children = (Collection<ElementNode>)(List)Arrays.asList((Node[])ch.getNodes());
                toExpand.addAll(children);
                refreshRecursively(children, toExpand);
            }
        }
    }

    public ElementNode getMimeRootNodeForOffset(ParserResult info, int offset) {
        if (getDescription().getPosition() > offset) {
            return null;
        }
        
        // Look up the current mime type
        Document document = info.getSnapshot().getSource().getDocument(false);
        if (document == null) {
            return null;
        }
        BaseDocument doc = (BaseDocument)document;
        return getMimeRootNodeForOffset(doc, offset);
    }
    
    ElementNode getMimeRootNodeForOffset(BaseDocument doc, int offset) {
        List<Language> languages = LanguageRegistry.getInstance().getEmbeddedLanguages(doc, offset);

        // Look specifically within the
        if (languages.size() > 0) {
            Children ch = getChildren();
            if ( ch instanceof ElementChildren ) {
                Node[] children = ch.getNodes();
                for (Language language : languages) {
                    // Inefficient linear search because the children may not be
                    // ordered according to the source
                    for (int i = 0; i < children.length; i++) {
                        ElementNode c = (ElementNode) children[i];
                        if (c.getDescription() instanceof ElementScanningTask.MimetypeRootNode) {
                            ElementScanningTask.MimetypeRootNode mr = (ElementScanningTask.MimetypeRootNode)c.getDescription();
                            if (mr.language == language) {
                                return c.getNodeForOffset(offset);
                            }
                        }
                    }
                }
            }
        }

        // No match in embedded languages - do normal offset search
        return getNodeForOffset(offset);
    }
    
    public ElementNode getNodeForOffset(int offset) {
        if (getDescription().getPosition() > offset) {
            return null;
        }

        // Inefficient linear search because the children may not be
        // ordered according to the source
        Children ch = getChildren();
        if ( ch instanceof ElementChildren ) {
            Node[] children = ch.getNodes();
            for (int i = 0; i < children.length; i++) {
                ElementNode c = (ElementNode) children[i];
                long start = c.getDescription().getPosition();
                if (start <= offset) {
                    long end = c.getDescription().getEndPosition();
                    if (end >= offset) {
                        return c.getNodeForOffset(offset);
                    }
                }
            }
        }

        return this;
    }

    public void updateRecursively( StructureItem newDescription ) {
           List<Node> nodesToExpand = new LinkedList<Node>();
           List<Node> nodesToExpandRec = new LinkedList<Node>();
           updateRecursively(newDescription, nodesToExpand, nodesToExpandRec);
           ui.performExpansion(nodesToExpand, nodesToExpandRec);
    }

    private void updateRecursively( StructureItem newDescription, List<Node> nodesToExpand, List<Node> nodesToExpandRec ) {
        Children ch = getChildren();

        //If a node that was a LEAF now has children the child type has to be changed from Children.LEAF
        //to ElementChildren to be able to hold the new child data
        if(!(ch instanceof ElementChildren) && newDescription.getNestedItems() != null && 
                newDescription.getNestedItems().size()>0) {
            ch=new ElementChildren(ui, fileObject);
            setChildren(ch);
        }
        
        if ( ch instanceof ElementChildren ) {           
           HashSet<StructureItem> oldSubs = new HashSet<StructureItem>( description.getNestedItems() );

           
           // Create a hashtable which maps StructureItem to node.
           // We will then identify the nodes by the description. The trick is 
           // that the new and old description are equal and have the same hashcode
           Node[] nodes = ch.getNodes( true );           
           HashMap<StructureItem,ElementNode> oldD2node = new HashMap<StructureItem,ElementNode>();           
           for (Node node : nodes) {
               oldD2node.put(((ElementNode)node).description, (ElementNode)node);
           }
           
           // Now refresh keys
           ((ElementChildren)ch).resetKeys((List<StructureItem>)newDescription.getNestedItems(), ui.getFilters());

           
           // Reread nodes
           nodes = ch.getNodes( true );

           boolean alreadyExpanded = false;
           
           for( StructureItem newSub : newDescription.getNestedItems() ) {
                ElementNode node = oldD2node.get(newSub);
                if ( node != null ) { // filtered out
                    if ( !oldSubs.contains(newSub)) {
                       nodesToExpand.add(node);
                    }
                    node.updateRecursively( newSub, nodesToExpand, nodesToExpandRec ); // update the node recursively
                } else { // a new node
                    if (! alreadyExpanded) {
                        alreadyExpanded = true;
                        if (ui.isExpandedByDefault(this)) {
                            nodesToExpand.add(this);
                        }
                    }
                    for (Node newNode : nodes) {
                        if (newNode instanceof ElementNode  &&  ((ElementNode) newNode).getDescription() == newSub) {
                            nodesToExpandRec.add(newNode);
                            break;
                        }
                    }
                }
           }
        }
                        
        StructureItem oldDescription = description; // Remember old description        
        description = newDescription; // set new descrioption to the new node
        String oldHtml = oldDescription.getHtml(new NavigatorFormatter());
        String descHtml = description.getHtml(new NavigatorFormatter());
        if ( oldHtml != null && !oldHtml.equals(descHtml)) {
            // Different headers => we need to fire displayname change
            fireDisplayNameChange(oldHtml, descHtml);
        }
        if( oldDescription.getModifiers() != null &&  !oldDescription.getModifiers().equals(newDescription.getModifiers())) {
            fireIconChange();
            fireOpenedIconChange();
        }
    }
    
    public StructureItem getDescription() {
        return description;
    }
    
    public FileObject getFileObject() {
        return fileObject;
    }
    
    private static final class ElementChildren extends Children.Keys<StructureItem> {
        private ClassMemberPanelUI ui;
        private FileObject fileObject;
        private StructureItem   parent;
        
        @Override
        protected void addNotify() {
            super.addNotify();
            if (parent != null) {
                resetKeys((List<StructureItem>)parent.getNestedItems(), ui.getFilters());
            }
        }
        
        public ElementChildren(ClassMemberPanelUI ui, FileObject fileObject) {
            this.ui = ui;
            this.fileObject = fileObject;
        }
        
        public ElementChildren(StructureItem parent, ClassMemberPanelUI ui, FileObject fileObject) {
            this.parent = parent;
            this.ui = ui;
            this.fileObject = fileObject;
        }
        
        protected Node[] createNodes(StructureItem key) {
            return new Node[] {new  ElementNode(key, ui, fileObject)};
        }
        
        void resetKeys( List<StructureItem> descriptions, ClassMemberFilters filters ) {            
            setKeys( filters.filter(descriptions) );
        }
    }
                       
    /** Stores all interesting data about given element.
     */    
    static class Description {
        
        public static final Comparator<StructureItem> ALPHA_COMPARATOR =
            new DescriptionComparator(true);
        public static final Comparator<StructureItem> POSITION_COMPARATOR = 
            new DescriptionComparator(false);    
        
        ClassMemberPanelUI ui;
                
        //FileObject fileObject; // For the root description
        
        String name;
        ElementHandle elementHandle;
        ElementKind kind;
        Set<Modifier> modifiers;        
        List<Description> subs; 
        String htmlHeader;
        long pos;
        
        Description( ClassMemberPanelUI ui ) {
            this.ui = ui;
        }
                                
        @Override
        public boolean equals(Object o) {
                        
            if ( o == null ) {
                //System.out.println("- f nul");
                return false;
            }
            
            if ( !(o instanceof Description)) {
                // System.out.println("- not a desc");
                return false;
            }
            
            Description d = (Description)o;
            
            if ( kind != d.kind ) {
                // System.out.println("- kind");
                return false;
            }
            
            // Findbugs warns about this field being uninitialized on the following line!
            if ( !name.equals(d.name) ) {
                // System.out.println("- name");
                return false;
            }

//            if ( !this.elementHandle.signatureEquals(d.elementHandle) ) {
//                return false;
//            }
            
            /*
            if ( !modifiers.equals(d.modifiers)) {
                // E.println("- modifiers");
                return false;
            }
            */
            
            // System.out.println("Equals called");            
            return true;
        }
        
        
        @Override
        public int hashCode() {
            int hash = 7;

            hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 29 * hash + (this.kind != null ? this.kind.hashCode() : 0);
            // hash = 29 * hash + (this.modifiers != null ? this.modifiers.hashCode() : 0);
            return hash;
        }

        private static class DescriptionComparator implements Comparator<StructureItem> {
            
            boolean alpha;
            
            DescriptionComparator( boolean alpha ) {
                this.alpha = alpha;
            }
            
            public int compare(StructureItem d1, StructureItem d2) {
                if ( alpha ) {
                    if ( k2i(d1.getKind()) != k2i(d2.getKind()) ) {
                        return k2i(d1.getKind()) - k2i(d2.getKind());
                    } 
                    
                    return d1.getSortText().compareTo(d2.getSortText());
                }
                else {
                    return d1.getPosition() == d2.getPosition() ? 0 : d1.getPosition() < d2.getPosition() ? -1 : 1;
                }
            }
            
            int k2i( ElementKind kind ) {
                switch( kind ) {
                    case CONSTRUCTOR:
                        return 1;
                    case METHOD:
                    case DB:
                        return 2;
                    case FIELD:
                        return 3;
                    case CLASS:
                    case INTERFACE:
//                    case ENUM:
//                    case ANNOTATION_TYPE:                        
//                        return 4;
                        
                        // TODO - what about other types?
                    default:
                        return 100;
                }
            }
        }
        
    }
        
    private static class WaitNode extends AbstractNode {
        
        private Image waitIcon = ImageUtilities.loadImage("org/netbeans/modules/csl/navigation/resources/wait.gif"); // NOI18N
        private String displayName;
        
        WaitNode( ) {
            super( Children.LEAF );
            displayName = NbBundle.getMessage(ElementNode.class, "LBL_WaitNode");
        }
        
        @Override
        public Image getIcon(int type) {
             return waitIcon;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @java.lang.Override
        public java.lang.String getDisplayName() {
            return displayName;
        }
    }
    
    private static class NavigatorFormatter extends GsfHtmlFormatter {
        @Override
        public void name(ElementKind kind, boolean start) {
            // No special formatting for names
        }
    }
}
