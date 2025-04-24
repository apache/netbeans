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
package org.netbeans.modules.refactoring.java.ui.elements;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.modules.refactoring.java.ui.MoveMembersPanel;
import org.openide.explorer.view.CheckableNode;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.InstanceContent.Convertor;

/**
 * Node representing an Element
 *
 * @author Petr Hrebejk
 */
public class ElementNode extends AbstractNode {

    private static Node WAIT_NODE;
    private Description description;
    private final DescriptionFilter filters;

    /**
     * Creates a new instance of TreeNode
     */
    public ElementNode(final Description description, DescriptionFilter filters, ChangeListener listener) {
        super(description.subs == null ? Children.LEAF : new ElementChilren(description.subs, filters, listener),
                description.elementHandle == null ? null : prepareLookup(description, listener));
        this.filters = filters;
        this.description = description;
        setDisplayName(description.name);
    }

    @Override
    public Image getIcon(int type) {
        return description.kind == null ? super.getIcon(type) : ImageUtilities.icon2Image(ElementIcons.getElementIcon(description.kind, description.modifiers));
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
        return null;
    }

    @Override
    public String getHtmlDisplayName() {
        return description.htmlHeader;
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

    public static synchronized Node getWaitNode() {
        if (WAIT_NODE == null) {
            WAIT_NODE = new WaitNode();
        }
        return WAIT_NODE;
    }

    public void refreshRecursively() {
        Children ch = getChildren();
        if ( ch instanceof ElementChilren ) {
           ((ElementChilren)ch).resetKeys(description.subs, filters);
           for( Node sub : ch.getNodes() ) {
               ((ElementNode)sub).refreshRecursively();
            }
        }
    }

    public ElementNode getNodeForElement(ElementHandle<Element> eh) {

        if (getDescription().elementHandle != null
                && eh.signatureEquals(getDescription().elementHandle)) {
            return this;
        }

        Children ch = getChildren();
        if (ch instanceof ElementChilren) {
            for (Node sub : ch.getNodes()) {
                ElementNode result = ((ElementNode) sub).getNodeForElement(eh);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    @Override
    public Action[] getActions(boolean context) {
        // TODO add Actions
        return new Action[]{};
    }

    public void updateRecursively(Description newDescription) {
        Children ch = getChildren();
        if (ch instanceof ElementChilren) {
            HashSet<Description> oldSubs = new HashSet<Description>(description.subs);


            // Create a hashtable which maps Description to node.
            // We will then identify the nodes by the description. The trick is 
            // that the new and old description are equal and have the same hashcode
            Node[] nodes = ch.getNodes(true);
            HashMap<Description, ElementNode> oldD2node = new HashMap<Description, ElementNode>();
            for (Node node : nodes) {
                oldD2node.put(((ElementNode) node).description, (ElementNode) node);
            }

            // Now refresh keys
           ((ElementChilren)ch).resetKeys(newDescription.subs, filters);


            // Reread nodes
            nodes = ch.getNodes(true);

            for (Description newSub : newDescription.subs) {
                ElementNode node = oldD2node.get(newSub);
                if (node != null) { // filtered out
                    node.updateRecursively(newSub); // update the node recursively
                }
            }
        }

        Description oldDescription = description; // Remember old description        
        description = newDescription; // set new descrioption to the new node
        if (oldDescription.htmlHeader != null && !oldDescription.htmlHeader.equals(description.htmlHeader)) {
            // Different headers => we need to fire displayname change
            fireDisplayNameChange(oldDescription.htmlHeader, description.htmlHeader);
        }
        if (oldDescription.modifiers != null && !oldDescription.modifiers.equals(newDescription.modifiers)) {
            fireIconChange();
            fireOpenedIconChange();
        }
    }
    
    public void selectionChanged() {
        fireIconChange(); // TODO: Strange
    }

    public Description getDescription() {
        return description;
    }

    private static Lookup prepareLookup(final Description description, final ChangeListener listener) {
        InstanceContent ic = new InstanceContent();

        ic.add(description, ConvertDescription2TreePathHandle);
        ic.add(description, ConvertDescription2FileObject);
        ic.add(description, ConvertDescription2DataObject);
        ic.add(new CheckableNode() {

            /**
             * Tell the view to display a check-box for this node.
             *
             * @return
             * <code>true</code> if the check-box should be displayed,
             * <code>false</code> otherwise.
             */
            public boolean isCheckable() {
                return true;
            }

            /**
             * Provide the enabled state of the check-box.
             *
             * @return
             * <code>true</code> if the check-box should be enabled,
             * <code>false</code> otherwise.
             */
            public boolean isCheckEnabled() {
                return true;
            }

            /**
             * Provide the selected state of the check-box.
             *
             * @return
             * <code>true</code> if the check-box should be selected,
             * <code>false</code> if it should be unselected and
             * <code>null</code> if the state is unknown.
             */
            public Boolean isSelected() {
                return description.selected;
            }

            /**
             * Called by the view when the check-box gets selected/unselected
             *
             * @param selected
             * <code>true</code> if the check-box was selected,
             * <code>false</code> if the check-box was unselected.
             */
            public void setSelected(Boolean selected) {
                description.selected = selected;
                listener.stateChanged(new ChangeEvent(this));
            }
        });

        return new AbstractLookup(ic);
    }
    private static final Convertor<Description, TreePathHandle> ConvertDescription2TreePathHandle = new InstanceContent.Convertor<Description, TreePathHandle>() {

        @Override
        public TreePathHandle convert(Description obj) {
            return TreePathHandle.from(obj.elementHandle, obj.cpInfo);
        }

        @Override
        public Class<? extends TreePathHandle> type(Description obj) {
            return TreePathHandle.class;
        }

        @Override
        public String id(Description obj) {
            return "IL[" + obj.toString(); // NOI18N
        }

        @Override
        public String displayName(Description obj) {
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
            return "IL[" + obj.toString(); // NOI18N
        }

        public String displayName(Description obj) {
            return id(obj);
        }
    };
    private static final Convertor<Description, DataObject> ConvertDescription2DataObject = new InstanceContent.Convertor<Description, DataObject>() {

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
            return "IL[" + obj.toString(); // NOI18N
        }

        public String displayName(Description obj) {
            return id(obj);
        }
    };

    private static final class ElementChilren extends Children.Keys<Description> {
        private final DescriptionFilter filters;
        private final ChangeListener listener;

        public ElementChilren(Collection<Description> descriptions, DescriptionFilter filters, ChangeListener listener ) {
            this.filters = filters;
            this.listener = listener;
            resetKeys( descriptions, filters );
        }

        protected Node[] createNodes(Description key) {
            return new Node[] {new  ElementNode(key, filters, listener)};
        }

        void resetKeys( Collection<Description> descriptions, DescriptionFilter filters ) {            
            setKeys( filters.filter(descriptions) );
        }
    }

    /**
     * Stores all interesting data about given element.
     */
    public static class Description {

        public static final Comparator<Description> ALPHA_COMPARATOR =
                new DescriptionComparator(true);
        public static final Comparator<Description> POSITION_COMPARATOR =
                new DescriptionComparator(false);
        private final String name;
        private final ElementHandle<? extends Element> elementHandle;
        private final ElementKind kind;
        private Set<Modifier> modifiers;
        private Collection<Description> subs;
        private String htmlHeader;
        private long pos;
        private boolean inherited;
        private boolean constructor;
        private ClasspathInfo cpInfo;
        private Boolean selected;

        public Description() {
            this.name = null;
            this.elementHandle = null;
            this.kind = null;
            this.inherited = false;
            this.selected = Boolean.FALSE;
        }

        public Description(@NonNull String name,
                @NonNull ElementHandle<? extends Element> elementHandle,
                @NonNull ElementKind kind,
                boolean inherited) {
            assert name != null;
            assert elementHandle != null;
            assert kind != null;
            this.name = name;
            this.elementHandle = elementHandle;
            this.kind = kind;
            this.inherited = inherited;
            this.selected = Boolean.FALSE;
        }

        public FileObject getFileObject() {
            return SourceUtils.getFile(elementHandle, cpInfo);
        }

        public boolean isInherited() {
            return inherited;
        }

        public void setIsInherited(boolean isInherited) {
            this.inherited = isInherited;
        }

        public boolean isConstructor() {
            return constructor;
        }

        public void setIsConstructor(boolean isConstructor) {
            this.constructor = isConstructor;
        }

        public Collection<Description> getSubs() {
            return subs;
        }

        public void setSubs(Collection<Description> subs) {
            this.subs = subs;
        }

        public ElementHandle<? extends Element> getElementHandle() {
            return elementHandle;
        }

        public ClasspathInfo getCpInfo() {
            return cpInfo;
        }

        public void setCpInfo(ClasspathInfo cpInfo) {
            this.cpInfo = cpInfo;
        }

        public String getHtmlHeader() {
            return htmlHeader;
        }

        public void setHtmlHeader(String htmlHeader) {
            this.htmlHeader = htmlHeader;
        }

        public Set<Modifier> getModifiers() {
            return modifiers;
        }

        public void setModifiers(Set<Modifier> modifiers) {
            this.modifiers = modifiers;
        }

        public long getPos() {
            return pos;
        }

        public void setPos(long pos) {
            this.pos = pos;
        }

        public Boolean getSelected() {
            return selected;
        }

        public void setSelected(Boolean selected) {
            this.selected = selected;
        }

        public ElementKind getKind() {
            return kind;
        }

        public String getName() {
            return name;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof Description)) {
                return false;
            }
            Description d = (Description) o;
            if (kind != d.kind) {
                return false;
            }
            if (this.name != d.name && (this.name == null || !this.name.equals(d.name))) {
                return false;
            }
            if (this.elementHandle != d.elementHandle && (this.elementHandle == null || !this.elementHandle.equals(d.elementHandle))) {
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

        private static class DescriptionComparator implements Comparator<Description> {

            boolean alpha;

            DescriptionComparator(boolean alpha) {
                this.alpha = alpha;
            }

            public int compare(Description d1, Description d2) {

                if (alpha) {
                    return alphaCompare(d1, d2);
                } else {
                    if (d1.inherited && !d2.inherited) {
                        return 1;
                    }
                    if (!d1.inherited && d2.inherited) {
                        return -1;
                    }
                    if (d1.inherited && d2.inherited) {
                        return alphaCompare(d1, d2);
                    }
                    return d1.pos == d2.pos ? 0 : d1.pos < d2.pos ? -1 : 1;
                }
            }

            int alphaCompare(Description d1, Description d2) {
                if (k2i(d1.kind) != k2i(d2.kind)) {
                    return k2i(d1.kind) - k2i(d2.kind);
                }

                return d1.name.compareTo(d2.name);
            }

            int k2i(ElementKind kind) {
                switch (kind) {
                    case CONSTRUCTOR:
                        return 1;
                    case METHOD:
                        return 2;
                    case FIELD:
                        return 3;
                    case CLASS:
                    case INTERFACE:
                    case ENUM:
                    case RECORD:
                    case ANNOTATION_TYPE:
                        return 4;
                    default:
                        return 100;
                }
            }
        }
    }

    @NbBundle.Messages("LBL_WaitNode=Please Wait...")
    private static class WaitNode extends AbstractNode {

        private Image waitIcon = ImageUtilities.loadImage("org/netbeans/modules/refactoring/java/resources/wait.gif"); // NOI18N

        WaitNode() {
            super(Children.LEAF);
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
