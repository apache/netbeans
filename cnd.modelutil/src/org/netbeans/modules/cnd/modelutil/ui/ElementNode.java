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
package org.netbeans.modules.cnd.modelutil.ui;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmImageLoader;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/** Node representing an Element
 *
 */
public class ElementNode extends AbstractNode {

    private Description description;
    private boolean singleSelection;

    /** Creates a new instance of TreeNode */
    public ElementNode(Description description) {
        this(description, false);
    }

    public ElementNode(Description description, boolean sortChildren) {
        super(description.subs == null ? Children.LEAF : new ElementChilren(description.subs, sortChildren), Lookups.singleton(description));
        this.description = description;
        description.node = ElementNode.this;
        setDisplayName(description.name);
    }

    public void setSingleSelection(boolean singleSelection) {
        this.singleSelection = singleSelection;
    }

    @Override
    public Image getIcon(int type) {
        if (description.iconElementHandle == null) {
            return super.getIcon(type);
        }
        if (CsmKindUtilities.isFile(description.iconElementHandle)) {
            FileObject fo = ((CsmFile) description.iconElementHandle).getFileObject();
            if (fo != null && fo.isValid()) {
                try {
                    DataObject dob = DataObject.find(fo);
                    Node node = dob.getNodeDelegate();
                    if (node != null) {
                        return node.getIcon(type);
                    }
                } catch (DataObjectNotFoundException ex) {
                }
            }
            return super.getIcon(type);
        }
        return ImageUtilities.icon2Image(CsmImageLoader.getIcon(description.iconElementHandle));
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public java.lang.String getDisplayName() {
        return description.name;
    }

    @Override
    public String getHtmlDisplayName() {
        return description.getDisplayName();
    }
    private static final Action[] EMPTY_ACTIONS = new Action[0];

    @Override
    public Action[] getActions(boolean context) {
        return EMPTY_ACTIONS;
    }

    public void assureSingleSelection() {
        Node pn = getParentNode();
        if (pn == null && singleSelection) {
            description.deepSetSelected(false);
        } else if (pn != null) {
            Description d = pn.getLookup().lookup(Description.class);
            if (d != null) {
                d.node.assureSingleSelection();
            }
        }
    }

    public static Node getWaitNode(String displayName) {
        return new WaitNode(displayName);
    }

    private static final class ElementChilren extends Children.Keys<Description> {
        private final boolean sortChildren;

        public ElementChilren(List<Description> descriptions, boolean sortChildren) {
            this.sortChildren = sortChildren;
            if (sortChildren && descriptions.size() > 1) {
                Collections.sort(descriptions, Description.ALPHA_COMPARATOR);
            }

            setKeys(descriptions);
        }

        @Override
        protected Node[] createNodes(Description key) {
            return new Node[]{new ElementNode(key, sortChildren)};
        }
    }

    /** Stores all interesting data about given element.
     */
    public static final class Description {

        public static final Comparator<Description> ALPHA_COMPARATOR = new DescriptionComparator();
        private ElementNode node;
        private final String name;
        private final CsmDeclaration elementHandle;
        private final CsmObject iconElementHandle;
        private final Set<CsmVisibility> modifiers;
        private final List<Description> subs;
        private final String htmlHeader;
        private boolean isSelected;
        private final boolean isSelectable;

        public static Description create(List<Description> subs) {
            return new Description("<root>", null, null, subs, null, false, false); // NOI18N
        }

        public static Description create(CsmObject element, List<Description> subs, boolean isSelectable, boolean isSelected) {
            String name = "";
            if (CsmKindUtilities.isNamedElement(element)) {
                name = ((CsmNamedElement)element).getName().toString();
            }
            String htmlHeader = name;
            if (CsmKindUtilities.isVariable(element)) {
                CsmVariable field = (CsmVariable) element;
                htmlHeader = field.getName().toString() + " : " + field.getType().getText(); //NOI18N
            } else if (CsmKindUtilities.isFunction(element)) {
                CsmFunction method = (CsmFunction) element;
                if (CsmKindUtilities.isConstructor(method)) {
                    htmlHeader = method.getSignature().toString();
                } else {
                    htmlHeader = method.getSignature() + " : " + method.getReturnType().getText(); //NOI18N
                }
            } else if (CsmKindUtilities.isNamespace(element)) {
                CsmNamespace ns = (CsmNamespace)element;
                if (!ns.isGlobal()) {
                    htmlHeader = ns.getQualifiedName().toString();
                }
            }
//            switch (element.getKind()) {
//                case ANNOTATION_TYPE:
//                case CLASS:
//                case ENUM:
//                case INTERFACE:
//                    htmlHeader = createHtmlHeader((TypeElement) element);
//                    break;
//                case ENUM_CONSTANT:
//                case FIELD:
//                    htmlHeader = createHtmlHeader((VariableElement) element);
//                    break;
//                case CONSTRUCTOR:
//                case METHOD:
//                    htmlHeader = createHtmlHeader((ExecutableElement) element);
//                    break;
//            }
            return new Description(name,
                    element,
                    Collections.<CsmVisibility>emptySet(),
                    subs,
                    htmlHeader,
                    isSelectable,
                    isSelected);
        }

        private Description(String name, CsmObject elementHandle,
                Set<CsmVisibility> modifiers, List<Description> subs, String htmlHeader,
                boolean isSelectable, boolean isSelected) {
            this.name = name;
            this.iconElementHandle = elementHandle;
            this.elementHandle = CsmKindUtilities.isDeclaration(elementHandle) ? (CsmDeclaration)elementHandle : null;
            this.modifiers = modifiers;
            this.subs = subs;
            this.htmlHeader = htmlHeader;
            this.isSelectable = isSelectable;
            this.isSelected = isSelected;
        }

        public boolean isSelectable() {
            return isSelectable;
        }

        public boolean hasSelectableSubs() {
            if (null == subs) {
                return false;
            }
            for (Description d : getSubs()) {
                if (d.isSelectable()) {
                    return true;
                }
            }
            return false;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public List<Description> getSubs() {
            return subs;
        }

        public void setSelected(boolean selected) {

            if (selected == true && node != null) {
                node.assureSingleSelection();
            }

            this.isSelected = selected;
            if (node != null) {       // notity the node                
                node.fireDisplayNameChange(null, null);
            }
        }

        public void deepSetSelected(boolean value) {

            if (isSelectable() && value != isSelected()) {
                setSelected(value);
            }

            if (subs != null) {
                for (Description s : subs) {
                    s.deepSetSelected(value);
                }
            }
        }

        public CsmDeclaration getElementHandle() {
            return elementHandle;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Description)) {
                return false;
            }
            Description d = (Description) o;
            if (!this.name.equals(d.name)) {
                return false;
            }
            if (this.elementHandle != d.elementHandle) {
                if (this.elementHandle == null || d.elementHandle == null) {
                    return false;
                }
                if (this.elementHandle.getKind() != d.elementHandle.getKind()) {
                    return false;
                }
                if (!this.elementHandle.equals(d.elementHandle)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 29 * hash + (this.elementHandle != null ? this.elementHandle.getKind().hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return this.name + " " + this.isSelected; // NOI18N
        }

        public String getName() {
            return name;
        }
        
        public String getDisplayName() {
            return htmlHeader;
        }

        public static Description deepCopy(Description d) {

            List<Description> subsCopy;

            if (d.subs == null) {
                subsCopy = null;
            } else {
                subsCopy = new ArrayList<Description>(d.subs.size());
                for (Description s : d.subs) {
                    subsCopy.add(deepCopy(s));
                }
            }

            return new Description(d.name, d.elementHandle, d.modifiers, subsCopy,
                    d.htmlHeader, d.isSelectable, d.isSelected);

        }

//        private static String createHtmlHeader(ExecutableElement e) {
//            StringBuilder sb = new StringBuilder();
//            if (e.getKind() == ElementKind.CONSTRUCTOR) {
//                sb.append(e.getEnclosingElement().getSimpleName());
//            } else {
//                sb.append(e.getSimpleName());
//            }
//            sb.append("("); // NOI18N
//            for (Iterator<? extends VariableElement> it = e.getParameters().iterator(); it.hasNext();) {
//                VariableElement param = it.next();
//                if (!it.hasNext() && e.isVarArgs() && param.asType().getKind() == TypeKind.ARRAY) {
//                    sb.append(print(((ArrayType) param.asType()).getComponentType()));
//                    sb.append("...");
//                } else {
//                    sb.append(print(param.asType()));
//                }
//                sb.append(" "); // NOI18N
//                sb.append(param.getSimpleName());
//                if (it.hasNext()) {
//                    sb.append(", "); // NOI18N
//                }
//            }
//            sb.append(")"); // NOI18N
//            if (e.getKind() != ElementKind.CONSTRUCTOR) {
//                TypeMirror rt = e.getReturnType();
//                if (rt.getKind() != TypeKind.VOID) {
//                    sb.append(" : "); // NOI18N
//                    sb.append(print(e.getReturnType()));
//                }
//            }
//            return sb.toString();
//        }
//
//        private static String createHtmlHeader(VariableElement e) {
//            StringBuilder sb = new StringBuilder();
//            sb.append(e.getSimpleName());
//            if (e.getKind() != ElementKind.ENUM_CONSTANT) {
//                sb.append(" : "); // NOI18N
//                sb.append(print(e.asType()));
//            }
//            return sb.toString();
//        }
//
//        private static String createHtmlHeader(TypeElement e) {
//            StringBuilder sb = new StringBuilder();
//            sb.append(e.getSimpleName());
//            List<? extends TypeParameterElement> typeParams = e.getTypeParameters();
//            if (typeParams != null && !typeParams.isEmpty()) {
//                sb.append("<"); // NOI18N
//                for (Iterator<? extends TypeParameterElement> it = typeParams.iterator(); it.hasNext();) {
//                    TypeParameterElement tp = it.next();
//                    sb.append(tp.getSimpleName());
//                    try {
//                        List<? extends TypeMirror> bounds = tp.getBounds();
//                        if (!bounds.isEmpty()) {
//                            sb.append(printBounds(bounds));
//                        }
//                    } catch (NullPointerException npe) {
//                    }
//                    if (it.hasNext()) {
//                        sb.append(", "); // NOI18N
//                    }
//                }
//                sb.append(">"); // NOI18N
//            }
//            return sb.toString();
//        }

//        private static String printBounds(List<? extends TypeMirror> bounds) {
//            if (bounds.size() == 1 && "java.lang.Object".equals(bounds.get(0).toString())) // NOI18N
//            {
//                return "";
//            }
//            StringBuilder sb = new StringBuilder();
//            sb.append(" extends "); // NOI18N
//            for (Iterator<? extends TypeMirror> it = bounds.iterator(); it.hasNext();) {
//                TypeMirror bound = it.next();
//                sb.append(print(bound));
//                if (it.hasNext()) {
//                    sb.append(" & "); // NOI18N
//                }
//            }
//            return sb.toString();
//        }
//
//        private static String print(TypeMirror tm) {
//            StringBuilder sb;
//            switch (tm.getKind()) {
//                case DECLARED:
//                    DeclaredType dt = (DeclaredType) tm;
//                    sb = new StringBuilder(dt.asElement().getSimpleName().toString());
//                    List<? extends TypeMirror> typeArgs = dt.getTypeArguments();
//                    if (!typeArgs.isEmpty()) {
//                        sb.append("<"); // NOI18N
//                        for (Iterator<? extends TypeMirror> it = typeArgs.iterator(); it.hasNext();) {
//                            TypeMirror ta = it.next();
//                            sb.append(print(ta));
//                            if (it.hasNext()) {
//                                sb.append(", "); // NOI18N
//                            }
//                        }
//                        sb.append(">"); // NOI18N
//                    }
//                    return sb.toString();
//                case TYPEVAR:
//                    TypeVariable tv = (TypeVariable) tm;
//                    sb = new StringBuilder(tv.asElement().getSimpleName().toString());
//                    return sb.toString();
//                case ARRAY:
//                    ArrayType at = (ArrayType) tm;
//                    sb = new StringBuilder(print(at.getComponentType()));
//                    sb.append("[]"); // NOI18N
//                    return sb.toString();
//                case WILDCARD:
//                    WildcardType wt = (WildcardType) tm;
//                    sb = new StringBuilder("?"); // NOI18N
//                    if (wt.getExtendsBound() != null) {
//                        sb.append(" extends "); // NOI18N
//                        sb.append(print(wt.getExtendsBound()));
//                    }
//                    if (wt.getSuperBound() != null) {
//                        sb.append(" super "); // NOI18N
//                        sb.append(print(wt.getSuperBound()));
//                    }
//                    return sb.toString();
//                default:
//                    return tm.toString();
//            }
//        }
        private static class DescriptionComparator implements Comparator<Description> {

            @Override
            public int compare(Description d1, Description d2) {

                if (k2i(d1.elementHandle.getKind()) != k2i(d2.elementHandle.getKind())) {
                    return k2i(d1.elementHandle.getKind()) - k2i(d2.elementHandle.getKind());
                }

                return d1.name.compareTo(d2.name);
            }

            int k2i(CsmDeclaration.Kind kind) {
                switch (kind) {
                    case FUNCTION:
                    case FUNCTION_FRIEND:
                        return 1;
                    case FUNCTION_DEFINITION:
                    case FUNCTION_FRIEND_DEFINITION:
                    case FUNCTION_LAMBDA:
                        return 2;
                    case VARIABLE:
                    case VARIABLE_DEFINITION:
                        return 3;
                    case CLASS:
                        return 4;
                    case STRUCT:
                        return 5;
                    case UNION:
                        return 6;
                    case ENUM:
                        return 7;
                    default:
                        return 100;
                }
            }
        }
    }

    private static class WaitNode extends AbstractNode {

        private final Image waitIcon = ImageUtilities.loadImage("org/netbeans/modules/cnd/modelutil/resources/wait.gif"); // NOI18N
        private final String displayName;

        WaitNode(String displayName) {
            super(Children.LEAF);
            this.displayName = displayName;// NOI18N
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

        @Override
        public String getHtmlDisplayName() {
            return displayName;
        }
    }
}
