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
package org.netbeans.modules.java.navigation;


import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.java.navigation.ElementNode.Description;
import org.netbeans.modules.java.navigation.actions.OpenAction;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Union2;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.InstanceContent.Convertor;

/** Node representing an Element
 *
 * @author Petr Hrebejk
 */
public class ElementNode extends AbstractNode implements Iterable<ElementNode> {


    private static final String ACTION_FOLDER = "Navigator/Actions/Members/text/x-java";  //NOI18N
    private static Node WAIT_NODE;

    private OpenAction openAction;
    private Description description;
           
    /** Creates a new instance of TreeNode */
    public ElementNode( Description description ) {
        super(
            description.subs == null ? Children.LEAF: new ElementChilren(description.subs, description.ui.getFilters()),
            prepareLookup(description));
        this.description = description;
        setDisplayName( description.name ); 
    }
    
    @Override
    public Image getIcon(int type) {
        final Icon icon = description.icon.get();
        return icon == null ? super.getIcon(type) : ImageUtilities.icon2Image(icon);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
                   
    @Override
    public java.lang.String getDisplayName() {
        if (description.name != null) {
            return description.name;
        }
        if (description.fileObject != null) {
            return description.fileObject.getNameExt();
        }
        return null;
    }
            
    @Override
    public String getHtmlDisplayName() {
        return description.htmlHeader;
    }
    
    @Override
    public Action[] getActions( boolean context ) {
        if ( context || description.name == null ) {
            return description.ui.getActions();
        } else {
            final Action panelActions[] = description.ui.getActions();
            final List<? extends Action> standardActions;
            final List<? extends Action> additionalActions;
            if (description.kind == ElementKind.OTHER) {
                standardActions = Collections.singletonList(getOpenAction());
                additionalActions = Collections.<Action>emptyList();
            } else {
                standardActions = Arrays.asList(new Action[] {
                    getOpenAction(),
                    RefactoringActionsFactory.whereUsedAction(),
                    RefactoringActionsFactory.popupSubmenuAction()
                });
                additionalActions = Utilities.actionsForPath(ACTION_FOLDER);
            }
            final int standardActionsSize = standardActions.isEmpty() ? 0 : standardActions.size() + 1;
            final int additionalActionSize = additionalActions.isEmpty() ? 0 : additionalActions.size() + 1;
            final List<Action> actions = new ArrayList<>(standardActionsSize + additionalActionSize + panelActions.length);
            if (standardActionsSize > 0) {
                actions.addAll(standardActions);
                actions.add(null);
            }
            if (additionalActionSize > 0) {
                actions.addAll(additionalActions);
                actions.add(null);
            }
            actions.addAll(Arrays.asList(panelActions));
            return actions.toArray(new Action[0]);
        }
    }
    
    @Override
    public Action getPreferredAction() {
        return getOpenAction();
    }

    @Override
    public boolean canCopy() {
        return false;
    }

    @Override
    public boolean canCut() {
        return false;
    }

    @Override
    public boolean canDestroy() {
        return false;
    }

    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        return null;
    }

    @Override
    public Transferable drag() throws IOException {
        return null;
    }

    @Override
    protected void createPasteTypes(Transferable t, List<PasteType> s) {
        // Do nothing
    }
        
    private synchronized Action getOpenAction() {
        if (openAction == null) {
            openAction = OpenAction.create(description.openable);
        }
        return openAction;
    }
    
    static synchronized Node getWaitNode() {
        if ( WAIT_NODE == null ) {
            WAIT_NODE = new WaitNode();
        }
        return WAIT_NODE;
    }
    
    public void refreshRecursively() {
        Children ch = getChildren();
        if ( ch instanceof ElementChilren ) {
            boolean scrollOnExpand = description.ui.getScrollOnExpand();
            description.ui.setScrollOnExpand( false );
           ((ElementChilren)ch).resetKeys(description.subs, description.ui.getFilters());
           for( Node sub : ch.getNodes() ) {
               description.ui.expandNode(sub);
               ((ElementNode)sub).refreshRecursively();
           }
           description.ui.setScrollOnExpand( scrollOnExpand );
        }        
    }
    
    @NonNull
    public Stream<ElementNode> stream() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator(), 0),
                false);
    }

    @Override
    public Iterator<ElementNode> iterator() {
        return new Iterator<ElementNode>() {
            private final Deque<ElementNode> todo = new ArrayDeque<>();
            {
                todo.push(ElementNode.this);
            }

            @Override
            public boolean hasNext() {
                return !todo.isEmpty();
            }

            @Override
            public ElementNode next() {
                if (todo.isEmpty()) {
                    throw new NoSuchElementException();
                }
                final ElementNode n = todo.pop();
                final Node[] clds = n.getChildren().getNodes();
                for (int i=clds.length-1; i>=0; i--) {
                    if (clds[i] instanceof ElementNode) {
                        todo.push((ElementNode)clds[i]);
                    }
                }
                return n;
            }
        };
    }

    public void updateRecursively( Description newDescription ) {
        Children ch = getChildren();
        if ( ch instanceof ElementChilren ) {           
           HashSet<Description> oldSubs = new HashSet<Description>( description.subs );

           
           // Create a hashtable which maps Description to node.
           // We will then identify the nodes by the description. The trick is 
           // that the new and old description are equal and have the same hashcode
           Node[] nodes = ch.getNodes( true );           
           HashMap<Description,ElementNode> oldD2node = new HashMap<Description,ElementNode>();           
           for (Node node : nodes) {
               oldD2node.put(((ElementNode)node).description, (ElementNode)node);
           }
           
           // Now refresh keys
           ((ElementChilren)ch).resetKeys(newDescription.subs, newDescription.ui.getFilters());

           
           // Reread nodes
           nodes = ch.getNodes( true );
           
           for( Description newSub : newDescription.subs ) {
                ElementNode node = oldD2node.get(newSub);
                if ( node != null ) { // filtered out
                    if ( !oldSubs.contains(newSub) && node.getChildren() != Children.LEAF) {                                           
                        description.ui.expandNode(node); // Make sure new nodes get expanded
                    }     
                    node.updateRecursively( newSub ); // update the node recursively
                }
           }
        }
                        
        Description oldDescription = description; // Remember old description        
        description = newDescription; // set new descrioption to the new node
        if ( oldDescription.htmlHeader != null && !oldDescription.htmlHeader.equals(description.htmlHeader)) {
            // Different headers => we need to fire displayname change
            fireDisplayNameChange(oldDescription.htmlHeader, description.htmlHeader);
        }
        if( oldDescription.modifiers != null &&  !oldDescription.modifiers.equals(newDescription.modifiers)) {
            fireIconChange();
            fireOpenedIconChange();
        }
    }
    
    public Description getDescription() {
        return description;
    }

    @NonNull
    private static Lookup prepareLookup(@NonNull final Description d) {
        final InstanceContent ic = new InstanceContent();
        ic.add(d, ConvertDescription2FileObject);
        ic.add(d, ConvertDescription2DataObject);
        if (d.handle != null) {
            ic.add(d, ConvertDescription2TreePathHandle);
        }
        return new AbstractLookup(ic);
    }

    private static final Convertor<Description, TreePathHandle> ConvertDescription2TreePathHandle = new InstanceContent.Convertor<Description, TreePathHandle>() {
        @Override public TreePathHandle convert(Description obj) {
            return obj.getTreePathHandle();
        }
        @Override public Class<? extends TreePathHandle> type(Description obj) {
            return TreePathHandle.class;
        }
        @Override public String id(Description obj) {
            return "IL[" + obj.toString();
        }
        @Override public String displayName(Description obj) {
            return id(obj);
        }
    };

    private static final Convertor<Description, FileObject> ConvertDescription2FileObject = new InstanceContent.Convertor<Description, FileObject>() {
        public FileObject convert(Description d) {
            return d.getFileObject();
        }
        public Class<? extends FileObject> type(Description obj) {
            return FileObject.class;
        }
        public String id(Description obj) {
            return "IL[" + obj.toString();
        }
        public String displayName(Description obj) {
            return id(obj);
        }
    };

    private static final Convertor<Description, DataObject> ConvertDescription2DataObject = new InstanceContent.Convertor<Description, DataObject>(){
        public DataObject convert(Description d) {
            try {
                final FileObject fo = d.getFileObject();
                return fo == null ? null : DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                return null;
            }
        }
        public Class<? extends DataObject> type(Description obj) {
            return DataObject.class;
        }
        public String id(Description obj) {
            return "IL[" + obj.toString();
        }
        public String displayName(Description obj) {
            return id(obj);
        }
    };

    private static final class ElementChilren extends Children.Keys<Description> {
            
        public ElementChilren(Collection<Description> descriptions, ClassMemberFilters filters ) {
            resetKeys( descriptions, filters );            
        }
        
        protected Node[] createNodes(Description key) {
            return new Node[] {new  ElementNode(key)};
        }
        
        void resetKeys( Collection<Description> descriptions, ClassMemberFilters filters ) {            
            setKeys( filters.filter(descriptions) );
        }
        
        
                        
    }
                       
    /** Stores all interesting data about given element.
     */    
    static final class Description {
        
        public static final Comparator<Description> ALPHA_COMPARATOR =
            new DescriptionComparator(true);
        public static final Comparator<Description> POSITION_COMPARATOR = 
            new DescriptionComparator(false);    

        private final Union2<ElementHandle<?>,TreePathHandle> handle;
        final ClassMemberPanelUI ui;
        final String name;
        final ElementKind kind;
        final int posInKind;
        final Supplier<Icon> icon;
        final Openable openable;
        final Set<Modifier> modifiers;
        final long pos;
        final ClasspathInfo cpInfo;
        final boolean isInherited;
        final boolean isTopLevel;

        FileObject fileObject; // For the root description
        Collection<Description> subs;
        String htmlHeader;

        private Description(ClassMemberPanelUI ui) {
            this.ui = ui;
            this.name = null;
            this.handle = null;
            this.kind = null;
            this.posInKind = 0;
            this.isInherited = false;
            this.isTopLevel = false;
            this.icon = () -> null;
            this.openable = () -> {};
            this.cpInfo = null;
            this.pos = -1;
            this.modifiers = Collections.emptySet();
        }

        private Description(
                @NonNull final ClassMemberPanelUI ui,
                @NonNull final String name,
                @NullAllowed final Union2<ElementHandle<?>,TreePathHandle> handle,
                @NonNull final ElementKind kind,
                final int posInKind,
                @NonNull final ClasspathInfo cpInfo,
                @NonNull final Set<Modifier> modifiers,
                final long pos,
                final boolean inherited,
                final boolean topLevel,
                @NonNull Supplier<Icon> icon,
                @NonNull Openable openable) {
            Parameters.notNull("ui", ui);   //NOI18N
            Parameters.notNull("name", name);   //NOI18N
            Parameters.notNull("kind", kind);   //NOI18N
            Parameters.notNull("icon", icon);   //NOI18N
            Parameters.notNull("openable", openable);  //NOI18N
            this.ui = ui;
            this.name = name;
            this.handle = handle;
            this.kind = kind;
            this.posInKind = posInKind;
            this.cpInfo = cpInfo;
            this.modifiers = modifiers;
            this.pos = pos;
            this.icon = icon;
            this.isInherited = inherited;
            this.isTopLevel = topLevel;
            this.openable = openable;
        }

        private Description(@NonNull ClassMemberPanelUI ui,
                    @NonNull final String name,
                    @NonNull final ElementHandle<? extends Element> elementHandle,
                    final int posInKind,
                    @NonNull final ClasspathInfo cpInfo,
                    @NonNull final Set<Modifier> modifiers,
                    final long pos,
                    final boolean inherited,
                    final boolean topLevel) {
            Parameters.notNull("ui", ui);   //NOI18N
            Parameters.notNull("name", name);   //NOI18N
            Parameters.notNull("elementHandle", elementHandle); //NOI18N
            this.ui = ui;
            this.name = name;
            this.handle = Union2.<ElementHandle<?>,TreePathHandle>createFirst(elementHandle);
            this.kind = elementHandle.getKind();
            this.posInKind = posInKind;
            this.cpInfo = cpInfo;
            this.modifiers = modifiers;
            this.pos = pos;
            this.isInherited = inherited;
            this.isTopLevel = topLevel;
            this.icon = () -> ElementIcons.getElementIcon(this.kind, this.modifiers);
            this.openable = () -> {OpenAction.openable(elementHandle, getFileObject(), name).open();};
        }

        @CheckForNull
        ElementHandle<?> getElementHandle() {
            if (handle == null) {
                return null;
            }
            return handle.hasFirst() ?
                handle.first() :
                handle.second().getElementHandle();
        }

        @CheckForNull
        TreePathHandle getTreePathHandle() {
            if (handle == null) {
                return null;
            }
            return handle.hasSecond() ?
                handle.second() :
                TreePathHandle.from(handle.first(), cpInfo);
        }

        public FileObject getFileObject() {
            if (isInherited) {
                assert getElementHandle() != null : handle;
                return SourceUtils.getFile(getElementHandle(), cpInfo);
            } else {
                return ui.getFileObject();
            }
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
            
            if (this.name != d.name && (this.name == null || !this.name.equals(d.name))) {
                // System.out.println("- name");
                return false;
            }

            if (this.handle != d.handle && (this.handle == null || !this.handle.equals(d.handle))) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            int hash = 7;

            hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 29 * hash + (this.kind != null ? this.kind.hashCode() : 0);
            // hash = 29 * hash + (this.modifiers != null ? this.modifiers.hashCode() : 0);
            return hash;
        }

        @NonNull
        static Description root(ClassMemberPanelUI ui) {
            return new Description(ui);
        }

        @NonNull
        static Description element(
                @NonNull final ClassMemberPanelUI ui,
                @NonNull final String name,
                @NonNull final ElementHandle<? extends Element> elementHandle,
                @NonNull final ClasspathInfo cpInfo,
                @NonNull final Set<Modifier> modifiers,
                final long pos,
                boolean inherited,
                boolean topLevel) {
            return new Description(ui, name, elementHandle, 0, cpInfo, modifiers, pos, inherited, topLevel);
        }

        @NonNull
        static Description directive(
                @NonNull final ClassMemberPanelUI ui,
                @NonNull final String name,
                @NonNull final TreePathHandle treePathHandle,
                @NonNull final ModuleElement.DirectiveKind kind,
                @NonNull final ClasspathInfo cpInfo,
                final long pos,
                @NonNull final Openable openable) {
            return new Description(
                    ui,
                    name,
                    Union2.<ElementHandle<?>,TreePathHandle>createSecond(treePathHandle),
                    ElementKind.OTHER,
                    directivePosInKind(kind),
                    cpInfo,
                    EnumSet.of(Modifier.PUBLIC),
                    pos,
                    false,
                    false,
                    ()->ElementIcons.getModuleDirectiveIcon(kind),
                    openable);
        }

        @NonNull
        static Description directive(
                @NonNull final ClassMemberPanelUI ui,
                @NonNull final String name,
                @NonNull final ModuleElement.DirectiveKind kind,
                @NonNull final ClasspathInfo cpInfo,
                @NonNull final Openable openable) {
            return new Description(
                    ui,
                    name,
                    null,
                    ElementKind.OTHER,
                    directivePosInKind(kind),
                    cpInfo,
                    EnumSet.of(Modifier.PUBLIC),
                    -1,
                    false,
                    false,
                    ()->ElementIcons.getModuleDirectiveIcon(kind),
                    openable);
        }

        private static int directivePosInKind(ModuleElement.DirectiveKind kind) {
            switch (kind) {
                case EXPORTS:
                    return 0;
                case OPENS:
                    return 1;
                case REQUIRES:
                    return 2;
                case USES:
                    return 3;
                case PROVIDES:
                    return 4;
                default:
                    return 100;
            }
        }

        private static class DescriptionComparator implements Comparator<Description> {
            
            boolean alpha;
            
            DescriptionComparator( boolean alpha ) {
                this.alpha = alpha;
            }
            
            public int compare(Description d1, Description d2) {
                
                if ( alpha ) {
                    return alphaCompare( d1, d2 );
                }
                else {
                    if( d1.isInherited && !d2.isInherited )
                        return 1;
                    if( !d1.isInherited && d2.isInherited )
                        return -1;
                    if( d1.isInherited && d2.isInherited ) {
                        return alphaCompare( d1, d2 );
                    }
                    return d1.pos == d2.pos ? 0 : d1.pos < d2.pos ? -1 : 1;
                }
            }

            int alphaCompare( Description d1, Description d2 ) {
                if ( k2i(d1.kind) != k2i(d2.kind) ) {
                    return k2i(d1.kind) - k2i(d2.kind);
                }
                if (d1.posInKind != d2.posInKind) {
                    return d1.posInKind - d2.posInKind;
                }
                int compareToName = d1.name.compareTo(d2.name);
                if (compareToName == 0) {
                    // htmlHeader might stay uninitialized in ElementScanningTask
                    String htmlHeader1 = d1.htmlHeader != null ? d1.htmlHeader : ""; // NOI18N
                    String htmlHeader2 = d2.htmlHeader != null ? d2.htmlHeader : ""; // NOI18N
                    return htmlHeader1.compareTo(htmlHeader2);
                } else {
                    return compareToName;
                }
            }

            int k2i( ElementKind kind ) {
                switch( kind ) {
                    case CONSTRUCTOR:
                        return 1;
                    case METHOD:
                        return 2;
                    case FIELD:
                        return 3;
                    case CLASS:
                    case INTERFACE:
                    case RECORD:
                    case ENUM:
                    case ANNOTATION_TYPE:
                    case MODULE:
                        return 4;
                    default:
                        return 100;
                }
            }
            
        }
        
    }
        
    private static class WaitNode extends AbstractNode {
        
        private Image waitIcon = ImageUtilities.loadImage("org/netbeans/modules/java/navigation/resources/wait.gif"); // NOI18N
        
        WaitNode( ) {
            super( Children.LEAF );
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
            return NbBundle.getMessage(ElementNode.class, "LBL_WaitNode"); // NOI18N
        }
        
    }
    
    
}
