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

package org.netbeans.core.multiview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;


/** 
 * Model handling maintainance of descriptions, creation of elements and selection 
 * of active items.
 * @author Milos Kleint
 */

class MultiViewModel {
    
    private MultiViewDescription currentEditor;
    private Map<MultiViewDescription, MultiViewElement> nestedElements; //key=description, value null or multiviewelement
    private Map<MultiViewElement, MultiViewElementCallback> nestedCallbacks; //key=element, value null or the MultiViewElementCallback that it's used by this element.
    private Map<MultiViewDescription,MultiViewPerspective> nestedPerspectives; //key=description, value perspective
//    private Map nestedPerspectiveComponents; //key=description, value mull or perspectiveComponent
    private MultiViewDescription[] descriptions;
    private ButtonGroup group;
    private ButtonGroup groupSplit;
    private Collection<MultiViewElement> shownElements;
    private ArrayList<ElementSelectionListener> listeners;
    private ActionRequestObserverFactory observerFactory;
    
    MultiViewModel(MultiViewDescription[] descs, MultiViewDescription defaultDescr, 
                   MultiViewModel.ActionRequestObserverFactory factory) {
        this(descs, defaultDescr, factory, Collections.<MultiViewDescription, MultiViewElement>emptyMap());
    }
    
    /**
     * constructor used at deserialization...
     */
    MultiViewModel(MultiViewDescription[] descs, MultiViewDescription defaultDescr, 
                   MultiViewModel.ActionRequestObserverFactory factory, Map<MultiViewDescription, MultiViewElement> existingElements) {
        observerFactory = factory;
        nestedElements = new HashMap<MultiViewDescription, MultiViewElement>();
//        nestedPerspectiveComponents = new HashMap();
        nestedPerspectives = new HashMap<MultiViewDescription,MultiViewPerspective>();
        nestedCallbacks = new HashMap<MultiViewElement, MultiViewElementCallback>();
        shownElements = new HashSet<MultiViewElement>(descs.length + 3);
        descriptions = descs;
        for (int i = 0; i < descriptions.length; i++) {
            MultiViewElement element = existingElements.get(descriptions[i]);
            nestedElements.put(descriptions[i], element);
            nestedPerspectives.put(descriptions[i], Accessor.DEFAULT.createPerspective(descriptions[i]));
            if (element != null) {
                // set the observer..
                MultiViewElementCallback call = factory.createElementCallback(descriptions[i]);
                nestedCallbacks.put(element, call);
                element.setMultiViewCallback(call);
//                nestedPerspectiveComponents.put(descriptions[i], Accessor.DEFAULT.createPersComponent(element));
            }
        }
        currentEditor = (defaultDescr == null || !nestedElements.containsKey(defaultDescr) ? descriptions[0] : defaultDescr);
        group = new BtnGroup();
	groupSplit = new BtnGroup();
    }
    
    
    void setActiveDescription(MultiViewDescription description) {
        if (currentEditor == description) return;
        MultiViewDescription old = currentEditor;
        currentEditor = description;
        fireSelectionChanged(old, description);
    }
    
    MultiViewDescription getActiveDescription() {
        return currentEditor;
    }
    
    MultiViewElement getActiveElement() {
        return getActiveElement(true);
    }
    
    MultiViewElement getActiveElement(boolean createIfNotCreatedYet) {
        return getElementForDescription(currentEditor, createIfNotCreatedYet);
    }
    
    /**
     * returns all elements that were so far created/instantiated.
     */
    synchronized Collection getCreatedElements() {
       Collection<MultiViewElement> col = new ArrayList<MultiViewElement>(nestedElements.size());
       for (Map.Entry<MultiViewDescription, MultiViewElement> entry : nestedElements.entrySet()) {
           if (entry.getValue() != null) {
               col.add(entry.getValue());
           }
           
       }
       return col;
    }
    
    synchronized Map<MultiViewDescription, MultiViewElement> getCreatedElementsMap() {
        return new HashMap<MultiViewDescription, MultiViewElement>(nestedElements);
    }
    
    /**
     * keeps track of already shown elements, so that componentOpened() is not called multiple Times on a single element.
     *
     */ 
    boolean wasShownBefore(MultiViewElement element) {
        return shownElements.contains(element);
    }
    /**
     * mars the compoment as shown before, meaning componentOpened() was called on it.
     */
    void markAsShown(MultiViewElement element) {
        shownElements.add(element);
    }

    /**
     * mars the compoment as currently hidden, meaning componentClosed() was called on it.
     */
    
    void markAsHidden(MultiViewElement element) {
        shownElements.remove(element);
    }
    
    /**
     * returns a list of current MultiViewDescriptions.
     */
    MultiViewDescription[] getDescriptions() {
        return descriptions;
    }
    
    MultiViewPerspective[] getPerspectives() {
        MultiViewPerspective[] toReturn = new MultiViewPerspective[descriptions.length];
        for (int i = 0; i < descriptions.length; i++) {
            toReturn[i] = nestedPerspectives.get(descriptions[i]);
        }
        return toReturn;
    }
    
    MultiViewPerspective getSelectedPerspective() {
        return nestedPerspectives.get(getActiveDescription());
    }
    
//    MultiViewPerspectiveComponent getMVComponentForDescription(MultiViewDescription desc) {
//        return (MultiViewPerspectiveComponent)nestedPerspectiveComponents.get(desc);
//    }
    
    /**
     * The button group where the togglebuttons for the descriptions are put into.
     */
    ButtonGroup getButtonGroup() {
        return group;
    }
    
    /**
     * The button group where the togglebuttons for the split descriptions are put into.
     */
    ButtonGroup getButtonGroupSplit() {
        return groupSplit;
    }
    
    MultiViewElement getElementForDescription(MultiViewDescription description) {
        return getElementForDescription(description, true);
    }

    /**
     * used primarily at deserialization time.
     */
     synchronized MultiViewElement getElementForDescription(MultiViewDescription description, boolean create) {
        MultiViewElement element = nestedElements.get(description);
        if (element == null && create) {
            element = description.createElement();
            MultiViewElementCallback call = observerFactory.createElementCallback(description);
            nestedCallbacks.put(element, call);
            element.setMultiViewCallback(call);
            nestedElements.put(description, element);
 //           nestedPerspectiveComponents.put(description, Accessor.DEFAULT.createPersComponent(element));
        }
        return element;
    }
     
     synchronized MultiViewElementCallback getCallbackForElement(MultiViewElement elem) {
         return nestedCallbacks.get(elem);
     }
    
    
    void addElementSelectionListener(ElementSelectionListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ElementSelectionListener>();
        }
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    void removeElementSelectionListener(ElementSelectionListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ElementSelectionListener>();
        }
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    private void fireSelectionChanged(MultiViewDescription oldOne, MultiViewDescription newOne) {
        if (listeners != null) {
            synchronized (listeners) {
                for (ElementSelectionListener list : listeners) {
                    list.selectionChanged(oldOne, newOne);
                }
            }
        }
    }
    
    void fireActivateCurrent() {
        if (listeners != null) {
            synchronized (listeners) {
                for (ElementSelectionListener list : listeners) {
                    list.selectionActivatedByButton();
                }
            }
        }
    }

    @Override
    public String toString() {
        return "current=" + currentEditor; // NOI18N
    }

    private boolean freezeButtons = false;
    void setFreezeTabButtons( boolean freeze ) {
        this.freezeButtons = freeze;
    }
    
    boolean canSplit() {
        for( MultiViewDescription mvd : getDescriptions() ) {
            if( !(mvd instanceof ContextAwareDescription) )
                return false;
        }
        return true;
    }

    /**
     * listener for changes in model's selected element.
     */
    static interface ElementSelectionListener  {
        
        public void selectionChanged(MultiViewDescription oldOne, MultiViewDescription newOne);
        
        public void selectionActivatedByButton();
    }
    
    /**
     * Interface for creating the observers that are passed to newly created elements.
     */
    static interface ActionRequestObserverFactory {
        MultiViewElementCallback createElementCallback(MultiViewDescription desc);
    }
    
    /**
     * handles selection of active element among the tgglebuttons.. more straightforward then adding listeners.
     */
    
    private class BtnGroup extends ButtonGroup {
        
        @Override
        public void setSelected(ButtonModel m, boolean b) {
            super.setSelected(m, b);
            if (getSelection() instanceof TabsComponent.TabsButtonModel && !freezeButtons) {
                TabsComponent.TabsButtonModel mod = (TabsComponent.TabsButtonModel)m;
                MultiViewDescription desc = mod.getButtonsDescription();
                setActiveDescription(desc);
            }
        }
        
    }
    
    
}
