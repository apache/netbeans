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

package org.netbeans.modules.xml.xam;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An abstract implementation of the Component. It implies modification capabilities.
 *
 * @author rico
 * @author Vidhya Narayanan
 * @author Nam Nguyen
 * @author Chris Webster
 */
public abstract class AbstractComponent<C extends Component<C>> implements Component<C> {
    private C parent;
    private List<C> children = null;
    private AbstractModel model;
    
    public AbstractComponent(AbstractModel model) {
        this.model = model;
    }
    
    protected abstract void appendChildQuietly(C component, List<C> children);
    
    protected abstract void insertAtIndexQuietly(C newComponent, List<C> children, int index);
    
    protected abstract void removeChildQuietly(C component, List<C> children);

    protected abstract void populateChildren(List<C> children);
    
    public final void removePropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if(model == null) return;
        model.removePropertyChangeListener(pcl);
    }
    
    public final void addPropertyChangeListener(java.beans.PropertyChangeListener pcl) {
        if(model == null) return;
        model.addPropertyChangeListener(new DelegateListener(pcl));
    }
    
    // Convenient method because WeakListener implmentation uses event source to unregister.
    public void removeComponentListener(ComponentListener cl) {
        if (getModel() != null) {
            getModel().removeComponentListener(cl);
        }
    }

    @Override
    public synchronized C getParent() {
        return parent;
    }
    
    protected synchronized void setParent(C component) {
        parent = component;
    }
    
    protected synchronized void setModel(AbstractModel aModel) {
        model = aModel;
        if (isChildrenInitialized()) {
            for (C component : getChildren()) {
                ((AbstractComponent)component).setModel(aModel);
            }
        }
    }
    
    private void _appendChildQuietly(C component, List<C> children) {
        if (component.getModel() == null) {
            throw new IllegalStateException("Cannot add a removed component, " +
                    "should use a fresh or a copy component."); // NOI18N
        }
        appendChildQuietly(component, children);
        ((AbstractComponent)component).setModel(getModel());
        ((AbstractComponent)component).setParent(this);
    }

    private void _insertAtIndexQuietly(C component, List<C> children, int index) {
        if (component.getModel() == null) {
            throw new IllegalStateException("Cannot add a removed component, " +
                    "should use a fresh or a copy component."); // NOI18N
        }
        insertAtIndexQuietly(component, children, index);
        ((AbstractComponent)component).setModel(getModel());
        ((AbstractComponent)component).setParent(this);
    }

    private void _removeChildQuietly(C component, List<C> children) {
        removeChildQuietly(component, children);
        ((AbstractComponent)component).setModel(null);
        ((AbstractComponent)component).setParent(null);
    }
    
    /**
     * @return the contained elements, this is the  model element
     * representations of the DOM children. The returned list is unmodifiable.
     */
    @Override
    public List<C> getChildren() {
        List<C> result = new ArrayList<C>(_getChildren());
        return Collections.unmodifiableList(result);
    }

    /**
     * This method guarantee that children are populated.
     * It's preferable to use it instead of getChildren() if children themselves
     * aren't necessary. This method works much faster then using getChildren()
     * in case of big amount of children because of absence of copying children
     * to a separate list.
     *
     * @since 1.6.1
     */
    public void checkChildrenPopulated() {
        _getChildren();
    }

    /**
     * Sometimes it's necessary to know amount of children but the children 
     * themselves aren't necessary. This method works much faster then using
     * getChildren().size() in case of big amount of children because of
     * absence of copying children to a separate list.
     *
     * @return number of children
     * @since 1.6.1
     */
    public int getChildrenCount() {
        return _getChildren().size();
    }

    /**
     * This method provides the ability to detect whether calling getChildren()
     * will trigger population of children. This can be used for meta models
     * to determine whether cleanup below a set of children is necessary. 
     */
    protected final boolean isChildrenInitialized() {
	return children != null;
    }
    
    private synchronized List<C> _getChildren() {
        if (!isChildrenInitialized()) {
            children = new ArrayList<C>();
            populateChildren(children);
            for (C child : children) {
                ((AbstractComponent)child).setParent(this);
            }
        }
        return children;
    }
    
    /**
     * @return the contained elements, this is the  model
     * element representations of the DOM children.
     *
     * @param type Interested children type to
     *	return.
     */
    @Override
    public synchronized <T extends C>List<T> getChildren(Class<T> type) {
        List<T> result = new ArrayList<T>(_getChildren().size());
        for (C child : _getChildren()) {
            if (type.isAssignableFrom(child.getClass())) {
                result.add(type.cast(child));
            }
        }
        return Collections.unmodifiableList(result);
    }
    
    /**
     * @return the contained  elements, this is the  model
     * element representations of the DOM children.
     *
     * @param typeList Collection that accepts the interested types and filters
     *	the return list of Children.
     */
    @Override
    public List<C> getChildren(Collection<Class<? extends C>> typeList) {
        List<C> comps = new ArrayList<C>();
        // createChildren is not necessary because this method delegates
        // to another getChildren which ensures initialization
        for(Class<? extends C> type : typeList) {
            comps.addAll(getChildren(type));
        }
        return Collections.unmodifiableList(comps);
    }

    @Override
    public synchronized AbstractModel getModel() {
        return model;
    }
    
    /**
     * This method ensures that a transaction is currently in progress and
     * that the current thread is able to write the model.
     */
    protected void verifyWrite() {
        getModel().validateWrite();
    }
    
    protected void firePropertyChange(String propName, Object oldValue, Object newValue) {
        PropertyChangeEvent event =
                new PropertyChangeEvent(this,propName,oldValue,newValue);
        getModel().firePropertyChangeEvent(event);
    }
    
    protected void fireValueChanged() {
        getModel().fireComponentChangedEvent(new ComponentEvent(this,
                ComponentEvent.EventType.VALUE_CHANGED));
    }
    
    protected void fireChildRemoved() {
        getModel().fireComponentChangedEvent(new ComponentEvent(this,
                ComponentEvent.EventType.CHILD_REMOVED));
    }
    
    protected void fireChildAdded() {
        getModel().fireComponentChangedEvent(new ComponentEvent(this,
                ComponentEvent.EventType.CHILD_ADDED));
    }
    
    protected <T extends C> T getChild(Class<T> type) {
        List<T> result = getChildren(type);
        T value = null;
        if (!result.isEmpty()) {
            value = result.get(0);
        }
        return value;
    }
    
    /**
     * Adds a  element before all other children whose types are in the typeList Collection.
     */
    protected synchronized void addBefore(String propertyName, C component,
            Collection<Class<? extends C>> typeList){
        verifyWrite();
        checkNullOrDuplicateChild(component);
        addChild(propertyName, component, typeList, true);
        firePropertyChange(propertyName, null, component);
        fireChildAdded();
    }
    
    /**
     * Adds a  element after all other children whose types are in the typeList Collection.
     */
    protected synchronized void addAfter(String propertyName, C component,
            Collection<Class<? extends C>> typeList){
        verifyWrite();
        checkNullOrDuplicateChild(component);
        addChild(propertyName, component, typeList, false);
        firePropertyChange(propertyName, null, component);
        fireChildAdded();
    }
    
    /**
     * Adds the New Element in the DOM model.
     *
     * @param component The  element that needs to be set
     * @param typeList The collection list that contains the class names
     *		of  types of children
     * @param before boolean to indicate to add before/after the typelist
     */
    private void addChild(String propertyName, C component,
            Collection<Class<? extends C>> typeList, boolean before) {
        assert(component != null);
        
        if (typeList == null) {
            throw new IllegalArgumentException("typeList == null"); //NOI18N
        }
        
        List<? extends C> childnodes = getChildren();
        if (typeList.isEmpty() || childnodes.isEmpty()) {
            _appendChildQuietly(component, _getChildren());
        } else {
            int lastIndex = before ? childnodes.size() : -1;
            for (Class<? extends C> type : typeList) {
                for (C child : childnodes) {
                    if (type.isAssignableFrom(child.getClass())) {
                        int i = childnodes.indexOf(child);
                        if (!before) {
                            if (i > lastIndex) lastIndex = i;
                        } else {
                            if (i < lastIndex) lastIndex = i;
                        }
                    }
                }
            }
            if (!before) {
                lastIndex++;
                for (int i=lastIndex ; i<childnodes.size() ; i++) {
                    if (childnodes.get(i).getClass().equals(component.getClass())) {
                        lastIndex++;
                    } else {
                        break;
                    }
                }
            }
            _insertAtIndexQuietly(component, _getChildren(), lastIndex);
        }
    }

    protected void checkNullOrDuplicateChild(C child) {
        if (child == null) {
            throw new IllegalArgumentException("child == null"); //NOI18N
        }
        if (_getChildren().contains(child)) {
            throw new IllegalArgumentException("child already in children list"); //NOI18N
        }
    }
    
    protected synchronized void appendChild(String propertyName, C child) {
        verifyWrite();
        checkNullOrDuplicateChild(child);
        _appendChildQuietly(child, _getChildren());
        firePropertyChange(propertyName, null, child);
        fireChildAdded();
    }
    
    /**
     * Inserts a Component child at the specified index relative to
     * the provided type. This method is expected to be used only in
     * sequence.
     * @param propertyName to fire event on
     * @param component to insert
     * @param index relative to first instance of type, index = firstpos
     * @param type which index should be relative to
     */
    protected synchronized void insertAtIndex(String propertyName,
            C component, int index,
            Class<? extends C> type) {
        verifyWrite();
        checkNullOrDuplicateChild(component);
        if (type != null) {
            int trueIndex = 0;
            for (C child: getChildren()) {
                if (type.isAssignableFrom(child.getClass())) {
                    break;
                }
                trueIndex++;
            }
            index += trueIndex;
        }
        _insertAtIndexQuietly(component, _getChildren(), index);
        firePropertyChange(propertyName, null, component);
        fireChildAdded();
    }
    
    public synchronized void insertAtIndex(String propertyName, C component, int index) {
        insertAtIndex(propertyName, component, index, null);
    }    
    
    public synchronized void removeChild(String propertyName, C component) {
        verifyWrite();
        if (component == null) {
            throw new IllegalArgumentException("component == null"); //NOI18N
        }
        if (! _getChildren().contains(component)) {
            throw new IllegalArgumentException(
                    "component to be deleted is not a child"); //NOI18N
        }
        _removeChildQuietly(component, _getChildren());
        firePropertyChange(propertyName, component, null);
        fireChildRemoved();
    }
    
    /**
     * When a child element is set using this method:
     * (1) All children that are of the same or derived type as classType are removed.
     * (2) newEl is added as a child after any children that are of the same
     * type as any of the types listed in typeList
     * @param classType Class of the Component that is being added as a child
     * @param propertyName Property name used for firing events
     * @param newComponent Component that is being added as a child
     * @param typeList Collection of java.lang.Class-es. newEl will be added as
     * a child after any children whose types belong to any listed in this. An
     * empty collection will append the child
     */
    protected void setChild(Class<? extends C> classType, String propertyName,
            C newComponent, Collection<Class<? extends C>> typeList){
        //
        setChildAfter(classType, propertyName, newComponent, typeList);
    }
    
    protected void setChildAfter(Class<? extends C> classType, 
            String propertyName, C newComponent,
            Collection<Class<? extends C>> typeList){
        //
        setChild(classType, propertyName, newComponent, typeList, false);
    }
    
    protected void setChildBefore(Class<? extends C> classType, 
            String propertyName, C newComponent,
            Collection<Class<? extends C>> typeList){
        //
        setChild(classType, propertyName, newComponent, typeList, true);
    }
    
    protected synchronized void setChild(Class<? extends C> classType, 
            String propertyName, C newComponent,
            Collection<Class<? extends C>> typeList, boolean before){
        //
        //remove all children of type classType
        verifyWrite();
        List<? extends C> childComponents = getChildren(classType);
        if (childComponents.contains(newComponent)) {
            return; // no change
        }
        C old = childComponents.isEmpty() ?
            null : childComponents.get(childComponents.size()-1);
        for (C child : childComponents) {
            _removeChildQuietly(child, _getChildren());
            fireChildRemoved();
        }
        if (newComponent != null) {
            addChild(propertyName, newComponent, typeList, before);
            fireChildAdded();
        }
        
        firePropertyChange(propertyName, old, newComponent);
    }
    
    private class DelegateListener implements PropertyChangeListener {
        private final PropertyChangeListener delegate;
        
        public DelegateListener(PropertyChangeListener pcl) {
            delegate = pcl;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() == AbstractComponent.this) {
                delegate.propertyChange(evt);
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            return delegate == obj;
        }
        
        @Override
        public int hashCode() {
            return delegate.hashCode();
        }
    }

    
    /**
     * Default implementation, subclass need to override if needed.
     */
    @Override
    public boolean canPaste(Component child) {
        return true;
    }
}

