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
package org.netbeans.modules.java.editor.codegen.ui;


import java.awt.Image;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.swing.Action;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/** Node representing an Element
 *
 * @author Petr Hrebejk, Jan Lahoda, Dusan Balek
 */
public class ElementNode extends AbstractNode {
    
    private Description description;
    private boolean singleSelection;       
    
    /** Creates a new instance of TreeNode */
    public ElementNode(Description description) {
        this( description, false );
    }
    
    private ElementNode(Description description, boolean sortChildren) {
        super(description.subs == null ? Children.LEAF: new ElementChilren(description.subs, sortChildren), Lookups.singleton(description));
        this.description = description;
        description.node = this;
        setDisplayName(description.name); 
    }
    
    public void setSingleSelection( boolean singleSelection ) {
        this.singleSelection = singleSelection;
    }
    
    @Override
    public Image getIcon(int type) {
        if (description.elementHandle == null)
            return super.getIcon(type);
        return ImageUtilities.icon2Image(ElementIcons.getElementIcon(description.elementHandle.getKind(), description.modifiers));
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
        return description.htmlHeader;
    }

    private static final Action[] EMPTY_ACTIONS = new Action[0];
    
    @Override
    public Action[] getActions(boolean context) {
        return EMPTY_ACTIONS;
    }

    public void assureSingleSelection() {
        Node pn = getParentNode();
        if (pn == null && singleSelection ) {
            description.deepSetSelected( false );
        }
        else if ( pn != null ) {
            Description d = pn.getLookup().lookup(Description.class);
            if ( d != null ) {
                d.node.assureSingleSelection();
            }
        }
    }
    
    private static final class ElementChilren extends Children.Keys<Description> {
            
        public ElementChilren(List<Description> descriptions, boolean sortChildren) {
            if( sortChildren ) {
                descriptions = new ArrayList<>(descriptions);
                descriptions.sort(Description.ALPHA_COMPARATOR );
            }
            setKeys(descriptions);            
        }
        
        protected Node[] createNodes(Description key) {
            return new Node[] {new ElementNode(key, true)};
        }
    }
    
    private static String[] c = new String[] {"&", "<", ">", "\n", "\""}; // NOI18N
    private static String[] tags = new String[] {"&amp;", "&lt;", "&gt;", "<br>", "&quot;"}; // NOI18N
    
    private static String translateToHTML(String input) {
        for (int cntr = 0; cntr < c.length; cntr++) {
            input = input.replace(c[cntr], tags[cntr]);
        }
        
        return input;
    }
                       
    /** Stores all interesting data about given element.
     */    
    public static class Description {
        
        public static final Comparator<Description> ALPHA_COMPARATOR = new DescriptionComparator();
        
        private ElementNode node;
        
        private String name;
        private ElementHandle<? extends Element> elementHandle;
        private Set<Modifier> modifiers;        
        private List<Description> subs; 
        private String htmlHeader;
        private boolean isSelected;
        private boolean isSelectable;
        
        public static Description create(List<Description> subs) {
            return new Description("<root>", null, null, subs, null, false, false); // NOI18N
        }
        
        public static Description create(CompilationInfo info, Element element, List<Description> subs, boolean isSelectable, boolean isSelected ) {
            boolean deprecated = info.getElements().isDeprecated(element);
            String htmlHeader = null;
            switch (element.getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    htmlHeader = createHtmlHeader(deprecated, (TypeElement)element);
                    break;
                case ENUM_CONSTANT:
                case FIELD:
                    htmlHeader = createHtmlHeader(deprecated, (VariableElement)element);
                    break;
                case CONSTRUCTOR:
                case METHOD:
                    htmlHeader = createHtmlHeader(deprecated, (ExecutableElement)element);
                    break;                    
            }
            return new Description(element.getSimpleName().toString(), 
                                   ElementHandle.create(element), 
                                   element.getModifiers(), 
                                   subs, 
                                   htmlHeader,
                                   isSelectable,
                                   isSelected);
        }

        private Description(String name, ElementHandle<? extends Element> elementHandle,
                Set<Modifier> modifiers, List<Description> subs, String htmlHeader,
                boolean isSelectable, boolean isSelected ) {
            this.name = name;
            this.elementHandle = elementHandle;
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
            if( null == subs )
                return false;
            for( Description d : getSubs() ) {
                if( d.isSelectable() )
                    return true;
            }
            return false;
        }
        
        public boolean hasSelection(boolean includeSelf) {
            if (includeSelf && isSelected()) {
                return true;
            }
            List<Description> descs = getSubs();
            if (descs == null) {
                return false;
            }
            for (Description d : descs) {
                if (d.hasSelection(true)) {
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
        
        public void setSelected( boolean selected ) {
            
            if ( selected == true && node != null) {
                node.assureSingleSelection();
            }
            
            this.isSelected = selected;
            if ( node != null ) {       // notity the node                
                node.fireDisplayNameChange(null, null);
                if (node.getParentNode() instanceof ElementNode)
                    ((ElementNode)node.getParentNode()).fireDisplayNameChange(null, null);                
            }
        }
        
        public void deepSetSelected( boolean value ) {
         
            if ( isSelectable() && value != isSelected() ) {
                setSelected(value);
            }
            
            if ( subs != null ) {
                for( Description s : subs ) {
                    s.deepSetSelected(value);
                }
            }
        }
        
        public ElementHandle<? extends Element> getElementHandle() {
            return elementHandle;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Description))
                return false;
            Description d = (Description)o;
            if (!this.name.equals(d.name))
                return false;
            if (this.elementHandle != d.elementHandle) {
                if (this.elementHandle == null || d.elementHandle == null)
                    return false;
                if (this.elementHandle.getKind() != d.elementHandle.getKind())
                    return false;
                if (!this.elementHandle.signatureEquals(d.elementHandle))
                    return false;
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
        
        public String getName() {
            return name;
        }
        
        public static Description deepCopy( Description d ) {
         
            List<Description> subsCopy;
                    
            if ( d.subs == null ) {
                subsCopy = null;
            }
            else {            
                subsCopy = new ArrayList<Description>( d.subs.size() );
                for( Description s : d.subs ) {
                    subsCopy.add( deepCopy(s) );
                }
            }
            
            return new Description( d.name, d.elementHandle, d.modifiers, subsCopy, 
                                    d.htmlHeader, d.isSelectable, d.isSelected );
            
        }
        
        
        
        private static String createHtmlHeader(boolean deprecated, ExecutableElement e) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            if (deprecated) sb.append("<s>");
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                sb.append(e.getEnclosingElement().getSimpleName());
            } else {
                sb.append(e.getSimpleName());
            }
            if (deprecated) sb.append("</s>");
            sb.append("("); // NOI18N
            for(Iterator<? extends VariableElement> it = e.getParameters().iterator(); it.hasNext(); ) {
                VariableElement param = it.next();
                if (!it.hasNext() && e.isVarArgs() && param.asType().getKind() == TypeKind.ARRAY) {
                    sb.append(translateToHTML(print(((ArrayType) param.asType()).getComponentType())));
                    sb.append("...");
                } else {
                    sb.append(translateToHTML(print(param.asType())));
                }
                sb.append(" "); // NOI18N
                sb.append(param.getSimpleName());
                if (it.hasNext()) {
                    sb.append(", "); // NOI18N
                }
            }
            sb.append(")"); // NOI18N
            if ( e.getKind() != ElementKind.CONSTRUCTOR ) {
                TypeMirror rt = e.getReturnType();
                if ( rt.getKind() != TypeKind.VOID ) {
                    sb.append(" : "); // NOI18N
                    sb.append(translateToHTML(print(e.getReturnType())));
                }
            }
            return sb.toString();
        }
        
        private static String createHtmlHeader(boolean deprecated, VariableElement e) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            if (deprecated) sb.append("<s>");
            sb.append(e.getSimpleName());
            if (deprecated) sb.append("</s>");
            if ( e.getKind() != ElementKind.ENUM_CONSTANT ) {
                sb.append( " : " ); // NOI18N
                sb.append(translateToHTML(print(e.asType())));
            }
            return sb.toString();
        }
        
        private static String createHtmlHeader(boolean deprecated, TypeElement e) {
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            if (deprecated) sb.append("<s>");
            sb.append(e.getSimpleName());
            if (deprecated) sb.append("</s>");
            List<? extends TypeParameterElement> typeParams = e.getTypeParameters();
            if (typeParams != null && !typeParams.isEmpty()) {
                sb.append("&lt;"); // NOI18N
                for(Iterator<? extends TypeParameterElement> it = typeParams.iterator(); it.hasNext();) {
                    TypeParameterElement tp = it.next();
                    sb.append(tp.getSimpleName());
                    try {
                        List<? extends TypeMirror> bounds = tp.getBounds();
                        if (!bounds.isEmpty()) {
                            sb.append(translateToHTML(printBounds(bounds)));
                        }
                    }
                    catch (NullPointerException npe) {
                    }                    
                    if (it.hasNext()) {
                        sb.append(", "); // NOI18N
                    }
                }
                sb.append("&gt;"); // NOI18N
            }
            return sb.toString();
        }
        
        private static String printBounds(List<? extends TypeMirror> bounds) {
            if (bounds.size() == 1 && "java.lang.Object".equals(bounds.get(0).toString())) // NOI18N
                return "";
            StringBuilder sb = new StringBuilder();
            sb.append(" extends "); // NOI18N
            for (Iterator<? extends TypeMirror> it = bounds.iterator(); it.hasNext();) {
                TypeMirror bound = it.next();
                sb.append(print(bound));
                if (it.hasNext()) {
                    sb.append(" & "); // NOI18N
                }
            }
            return sb.toString();
        }
        
        private static String print( TypeMirror tm ) {
            StringBuilder sb;
            switch (tm.getKind()) {
                case DECLARED:
                    DeclaredType dt = (DeclaredType)tm;
                    sb = new StringBuilder(dt.asElement().getSimpleName().toString());
                    List<? extends TypeMirror> typeArgs = dt.getTypeArguments();
                    if (!typeArgs.isEmpty()) {
                        sb.append("<"); // NOI18N
                        for (Iterator<? extends TypeMirror> it = typeArgs.iterator(); it.hasNext();) {
                            TypeMirror ta = it.next();
                            sb.append(print(ta));
                            if (it.hasNext()) {
                                sb.append(", "); // NOI18N
                            }
                        }
                        sb.append(">"); // NOI18N
                    }                    
                    return sb.toString();
                case TYPEVAR:
                    TypeVariable tv = (TypeVariable)tm;
                    sb = new StringBuilder(tv.asElement().getSimpleName().toString());
                    return sb.toString();
                case ARRAY:
                    ArrayType at = (ArrayType)tm;
                    sb = new StringBuilder(print(at.getComponentType()));
                    sb.append("[]"); // NOI18N
                    return sb.toString();
                case WILDCARD:
                    WildcardType wt = (WildcardType)tm;
                    sb = new StringBuilder("?"); // NOI18N
                    if (wt.getExtendsBound() != null) {
                        sb.append(" extends "); // NOI18N
                        sb.append(print(wt.getExtendsBound()));
                    }
                    if (wt.getSuperBound() != null) {
                        sb.append(" super "); // NOI18N
                        sb.append(print(wt.getSuperBound()));
                    }
                    return sb.toString();
                default:
                    return tm.toString();
            }
        }
            
        
        private static class DescriptionComparator implements Comparator<Description> {
            
            public int compare(Description d1, Description d2) {
                
                if ( k2i(d1.elementHandle.getKind()) != k2i(d2.elementHandle.getKind()) ) {
                    return k2i(d1.elementHandle.getKind()) - k2i(d2.elementHandle.getKind());
                } 

                return d1.name.compareTo(d2.name);
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
                        return 4;
                    case INTERFACE:
                        return 5;
                    case ENUM:
                        return 6;
                    case ANNOTATION_TYPE:                        
                        return 7;
                    default:
                        return 100;
                }
            }
            
        }
    }
}
