/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.model.api.Element;
import org.netbeans.modules.css.model.api.ElementHandle;
import org.netbeans.modules.css.model.api.ElementListener;
import org.netbeans.modules.css.model.api.Media;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.ModelVisitor;
import org.netbeans.modules.css.model.api.PlainElement;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author marekfukala
 */
public abstract class ModelElement implements Element {

    private static final Logger LOG = Logger.getLogger(ModelElement.class.getName());
    
    private final List<ClassElement> CLASSELEMENTS = new ArrayList<>();
    private Collection<ElementListener> LISTENERS;
    protected final Model model;
    private Node node;
    private Element parent;
    //used in there's no document to get the indent from
    private static final String DEFAULT_INDENT = "    "; //NOI18N

    public ModelElement(Model model) {
        this.model = model;
    }

    public ModelElement(Model model, Node node) {
        this(model);
        this.node = node;
    }
    
    /**
     * Overriding classes may define their own way of identification of the element in a String form.
     * @see ElementHandleImpl
     */
    protected String getCustomElementID() {
        return null;
    }

    @Override
    public ElementHandle getElementHandle() {
        return new ElementHandleImpl(this);
    }
    
    @Override
    public Model getModel() {
        return this.model;
    }

    @Override
    public void accept(ModelVisitor modelVisitor) {
        for (int i = 0; i < getElementsCount(); i++) {
            Element child = getElementAt(i);
            if(child == null) {
                //null child is allowed for the placeholder class elements created by #addEmptyElement()
            } else {
                child.accept(modelVisitor);
            }
        }

        acceptVisitorGeneric(this, modelVisitor);
//        modelVisitor.visitElement(this);
    }

    public void acceptVisitorGeneric(Element element, ModelVisitor modelVisitor) {
        try {
            Class<?> elementClass = getModelClass(element);
            
            if(Rule.class.equals(elementClass)) {
                modelVisitor.visitRule((Rule)element);
            } else if(Media.class.equals(elementClass)) {
                modelVisitor.visitMedia((Media)element);
            }
            
            //The reflection approach is horribly slow
            
//            String elementClassSimpleName = elementClass.getSimpleName();
//
//            Class<?> visitorClass = modelVisitor.getClass();
//            StringBuilder sb = new StringBuilder();
//            sb.append("visit"); //NOI18N
//            sb.append(elementClassSimpleName);
//
////            System.out.println("trying to call ModelVisitor." + sb.toString() + "(" + elementClass.getSimpleName() + ")");
//            
//            Method method = visitorClass.getMethod(sb.toString(), elementClass);
//            method.setAccessible(true);
//            
//            method.invoke(modelVisitor, element);
//        } catch (NoSuchMethodException nsme) {
            //the visitor doesn't contain such method, delegate to the generic
            //ModelVisitor.visitElement(...)
            //XXX ideally the ModelVisitor interface is generated so it contain
            //all the methods
//            modelVisitor.visitElement(element);
        } catch (/* NoSuchMethodException, SecurityException,
                 InstantiationException, IllegalAccessException, IllegalArgumentException, 
                 InvocationTargetException */Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Element getParent() {
        return parent;
    }

    @Override
    public void setParent(Element e) {
        parent = e;
    }

    /**
     * @return true if this {@link ModelElement} has been build by a client code, 
     * not from the source code.
     */
    protected boolean isArtificialElement() {
        return node == null;
    }
    
    @Override
    public boolean isValid() {
        if (node == null) {
            //artificial (node-less) elements
            return true;
        }
        return NodeUtil.getChildrenRecursivelyByType(node, NodeType.error, NodeType.recovery).isEmpty();
    }

    @Override
    public int getStartOffset() {
        return node != null ? node.from() : -1;
    }

    @Override
    public int getEndOffset() {
        return node != null ? node.to() : -1;
    }

    @Override
    public synchronized void addElementListener(ElementListener listener) {
        if (LISTENERS == null) {
            LISTENERS = new ArrayList<>();
            LISTENERS.add(listener);
        }
    }

    @Override
    public synchronized void removeElementListener(ElementListener listener) {
        if (LISTENERS == null) {
            return;
        }
        LISTENERS.remove(listener);
        if (LISTENERS.isEmpty()) {
            LISTENERS = null;
        }
    }

    protected synchronized void fireElementChanged() {
        if (LISTENERS == null) {
            return;
        }

        for (ElementListener el : LISTENERS) {
            el.elementChanged(null);
        }

    }

    private void fireElementAdded(Element e) {
        //set new parent to the element 
        e.setParent(this);

        ModelElementListener.Support.fireElementAdded(e, getElementListener());
        fireElementChanged();
    }

    private void fireElementRemoved(Element e) {
        //remove the parent
        e.setParent(null);

        ModelElementListener.Support.fireElementRemoved(e, getElementListener());
        fireElementChanged();
    }

    protected abstract Class getModelClass();

    protected final void initChildrenElements() {
        for (Node child : node.children()) {
            addElement(((ElementFactoryImpl) model.getElementFactory()).createElement(model, child));
        }
    }

    protected void addEmptyElement(Class clazz) {
        CLASSELEMENTS.add(new ClassElement(clazz, null));
    }

    protected void addTextElement(CharSequence text) {
        addElement(model.getElementFactory().createPlainElement(text));
    }

    private Class getModelClass(Element element) {
        if (element instanceof ModelElement) {
            ModelElement melement = (ModelElement) element;
            Class clazz = melement.getModelClass();
            if (!clazz.isAssignableFrom(element.getClass())) {
                throw new IllegalArgumentException(String.format("Element %s declares %s as its superinterface but it is not true!", element.getClass().getSimpleName(), clazz.getSimpleName()));
            }
            return clazz;
        } else {
            return element.getClass();
        }
    }

    @Override
    public int addElement(Element e) {
        Class clazz = getModelClass(e);
        CLASSELEMENTS.add(new ClassElement(clazz, e));
        fireElementAdded(e);
        return getElementsCount() - 1; //last element index
    }

    @Override
    public Element getElementAt(int index) {
        ClassElement ce = CLASSELEMENTS.get(index);
        return ce == null ? null : ce.getElement();
    }

    @Override
    public int getElementsCount() {
        return CLASSELEMENTS.size();
    }

    @Override
    public Element setElementAt(int index, Element e) {
        Class clazz = getModelClass(e);
        ClassElement ce = new ClassElement(clazz, e);
        ClassElement old = CLASSELEMENTS.set(index, ce);
        if (old != null && old.getElement() != null) {
            //the ClassElement may contain just the placeholder so no element
            fireElementRemoved(old.getElement());
        }
        fireElementAdded(e);

        return old == null ? null : old.getElement();
    }

    @Override
    public Iterator<Element> childrenIterator() {
        List<Element> elements = new ArrayList<>();
        for (ClassElement ce : CLASSELEMENTS) {
            elements.add(ce.getElement());
        }
        return elements.iterator();
    }

    @Override
    public Element removeElement(int index) {
        ClassElement removed = CLASSELEMENTS.remove(index);
        if (removed != null) {
            Element removedElement = removed.getElement();
            fireElementRemoved(removedElement);
            return removedElement;
        }
        return null;
    }

    @Override
    public boolean removeElement(Element element) {
        int index = getElementIndex(element);
        if (index == -1) {
            return false;
        }
        return removeElement(index) != null;
    }

    @Override
    public void insertElement(int index, Element element) {
        Class clazz = getModelClass(element);
        CLASSELEMENTS.add(index, new ClassElement(clazz, element));
        fireElementAdded(element);
    }

    @Override
    public int getElementIndex(Element e) {
        //XXX: fix the linear search :-(
        for (int i = 0; i < CLASSELEMENTS.size(); i++) {
            ClassElement ce = CLASSELEMENTS.get(i);
            if (ce.getElement().equals(e)) { //identity comparison?!?!
                return i;
            }
        }
        return -1;
    }

    public int getElementIndex(Class elementClass) {
        //XXX: fix the linear search :-(
        for (int i = 0; i < CLASSELEMENTS.size(); i++) {
            ClassElement ce = CLASSELEMENTS.get(i);
            if (ce.getClazz().equals(elementClass)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getModelClass(this).getSimpleName());
        sb.append("(model=");
        sb.append(model);
        sb.append(",sihc=");
        sb.append(System.identityHashCode(this));
        sb.append(",type=");
        if(node != null) {
            sb.append("source[ofs=");
            sb.append(node.from());
            sb.append('-');
            sb.append(node.to());
            sb.append(";line=");
            try {
                sb.append(1 + LexerUtils.getLineOffset(model.getOriginalSource(), node.from()));
                sb.append('-');
                sb.append(1 + LexerUtils.getLineOffset(model.getOriginalSource(), node.to()));
            } catch (BadLocationException ex) {
                sb.append("BLE!");
            }
            sb.append(']');
        } else {
            sb.append("AI");
        }
        sb.append(')');
        
        return sb.toString();
    }

    /**
     * Adds or replaces the given element in its enclosing element
     *
     * @param element
     */
    protected void setElement(Element element) {
        setElement(element, false);
    }

    /**
     * Adds or replaces the given element in its enclosing element.
     *
     * If position for the given element is found and there's no value defined,
     * then the given element is set to that position. If there's already an
     * element at the position the given element is added just after that
     * position.
     *
     * @param element
     */
    protected int setElement(Element element, boolean addElementIfSetAlready) {
        int index = getElementIndex(getModelClass(element));
        if (index == -1) {
            //not fount, just add it
            return addElement(element);
        } else {
            //found, replace or add if exists
            Element original = getElementAt(index);
            if (original == null) {
                //set
                setElementAt(index, element);
                return index;
            } else {
                //insert after the position
                index++;
                insertElement(index, element);
                return index;
            }
        }
    }

    /**
     * Returns and element at the specified position if it is of the T type.
     * This method doesn't throw any exception if the index is out of the
     * children element bounds. It just returns null in this case.
     *
     */
    protected <T extends Element> T getElementAt(int index, Class<T> type) {
        if (index < 0 || index >= getElementsCount()) {
            //XXX why not to throw IOOBE????
            return null; 
        }
        Element e = getElementAt(index);
        if (type.isAssignableFrom(e.getClass())) {
            return type.cast(e);
        }
        return null;
    }

    protected String getIndent() {
        //init the indent
        Document doc = model.getLookup().lookup(Document.class);
        if (doc != null) {
            int indentLevel = IndentUtils.indentLevelSize(doc);
            return IndentUtils.createIndentString(null, indentLevel);
        } else {
            return DEFAULT_INDENT;
        }
    }
    private static final String EMPTY_STRING = "";
    //xxx possibly refactor such methods to some utility class 

    /**
     * tries to clear part of the PlainElement from whitespaces.
     */
    protected void wipeWhitespaces(PlainElement pe, boolean endLineInclusive) {
        String text = pe.getContent().toString();
        //remove all whitespace after last endline
        int lastEndIndex = text.lastIndexOf('\n');
        if (lastEndIndex >= 0) {
            String upToTheNL = text.substring(0, lastEndIndex);
            if (upToTheNL.trim().length() == 0) {
                //remove all the whitespaces even before the NL
                pe.setContent(endLineInclusive ? EMPTY_STRING : "\n");
            } else {
                //there are some comments which cannot be wiped
                //in this case keep the endline
                text = text.substring(0, lastEndIndex + 1);
                pe.setContent(text);
            }
        } else {
            //no endline
            //remove the text if it is only WS
            if (text.trim().length() == 0) {
                pe.setContent(EMPTY_STRING);
            }
        }
    }

//    protected void removeTokenElementsFw(int fromIndex, String... images) {
//        if(fromIndex < 0 || fromIndex >= getElementsCount()) {
//            return ;
//        }
//        int toIndex = fromIndex;
//        for(int i = fromIndex; i < getElementsCount(); i++) {
//            Element e = getElementAt(i);
//            //XXX consolidate Plain-TokenElements!!!!!!!!
//            if(e instanceof PlainElement) {
//                PlainElement pe = (PlainElement)e;
//                String peImage = pe.getContent().toString().trim();
//                for(String image : images) {
//                    if(image.equals(peImage)) {
//                        toIndex = i;
//                        break;
//                    }
//                }
//            } else {
//                break;
//            }
//        }
//        
//        for(int i = toIndex; i >= fromIndex; i--) {
//            removeElement(i);
//        }
//        
//    }
//    
//    protected void removeTokenElementsBw(int fromIndex, String... images) {
//         if(fromIndex < 0 || fromIndex >= getElementsCount()) {
//            return ;
//        }
//        int toIndex = fromIndex;
//        for(int i = fromIndex; i >= 0; i--) {
//            Element e = getElementAt(i);
//            //XXX consolidate Plain-TokenElements!!!!!!!!
//            if(e instanceof PlainElement) {
//                PlainElement pe = (PlainElement)e;
//                String peImage = pe.getContent().toString().trim();
//                for(String image : images) {
//                    if(image.equals(peImage)) {
//                        toIndex = i;
//                        break;
//                    }
//                }
//            } else {
//                break;
//            }
//        }
//        
//        for(int i = toIndex; i >= fromIndex; i--) {
//            removeElement(i);
//        }
//        
//    }
    protected abstract ModelElementListener getElementListener();

    private static class ClassElement {

        private Class clazz;
        private Element element;

        public ClassElement(Class clazz, Element element) {
            this.clazz = clazz;
            this.element = element;
        }

        public Class getClazz() {
            return clazz;
        }

        public Element getElement() {
            return element;
        }

        @Override
        public String toString() {
            return getElement().toString();
        }
    }
}
