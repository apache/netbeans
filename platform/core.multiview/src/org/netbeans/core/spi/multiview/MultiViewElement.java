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

package org.netbeans.core.spi.multiview;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;

/** View element for multi view, provides the UI components to the multiview component.
 * Gets notified by the enclosing component about the changes in the lifecycle.
 *
 * @author  Dafe Simonek, Milos Kleint
 */
public interface MultiViewElement {

    /** Returns Swing visual representation of this multi view element. Should be relatively fast
     * and always return the same component.
     */
    JComponent getVisualRepresentation ();
    
    /**
     * Returns the visual component with the multi view element's toolbar.Should be relatively fast as it's called
     * everytime the current perspective is switched.
     */
    JComponent getToolbarRepresentation ();
    
    /**
     * Gets the actions which will appear in the popup menu of this component.
     * <p>Subclasses are encouraged to use add the default TopComponent actions to
     * the array of their own. These are accessible by calling MultiViewElementCallback.createDefaultActions()
     *<pre>
     * <code>
     *          public Action[] getActions() {
     *             Action[] retValue;
     *             // the multiviewObserver was passed to the element in setMultiViewCallback() method.
     *             if (multiViewObserver != null) {
     *                 retValue = multiViewObserver.createDefaultActions();
     *                 // add you own custom actions here..
     *             } else {
     *                 // fallback..
     *                 retValue = super.getActions();
     *             }
     *             return retValue;
     *         }
     *   </code>
     *</pre>
     * @return array of actions for this component
     */
    Action[] getActions();

    /**
     * Lookup for the MultiViewElement. Will become part of the TopComponent's lookup.
     * @return the lookup to use when the MultiViewElement is active.
     */
    Lookup getLookup();
    
    
    /** Called only when enclosing multi view top component was closed before and
     * now is opened again for the first time. The intent is to
     * provide subclasses information about multi view TopComponent's life cycle.
     * Subclasses will usually perform initializing tasks here.
     */
    void componentOpened();
    
    /** Called only when multi view top component was closed. The intent is 
     * to provide subclasses information about TopComponent's life cycle.
     * Subclasses will usually perform cleaning tasks here.
     */
    void componentClosed();

    /** Called when this MultiViewElement is about to be shown. 
     * That can happen when switching the current perspective/view or when the topcomonent itself is shown for the first time.
     */
    void componentShowing();
    
    /** Called when this MultiViewElement was hidden. This happens when other view replaces this one as the selected view or
     * when the whole topcomponent gets hidden (eg. when user selects anothe topcomponent in the current mode).
     */
    void componentHidden();
    
    /** Called when this multi view element is activated.
    * This happens when the parent window of this component gets focus
    * (and this component is the preferred one in it), <em>or</em> when
    * this component is selected in its window (and its window was already focused).
    */
    void componentActivated ();

    /** Called when this multi view element is deactivated.
    * This happens when the parent window of this component loses focus
    * (and this component is the preferred one in the parent),
    * <em>or</em> when this component loses preference in the parent window
    * (and the parent window is focussed).
    */
    void componentDeactivated ();
    
    /**
     * UndoRedo support, 
     * Get the undo/redo support for this element.
     *
     * @return undoable edit for this component, null if not implemented.
     */
    UndoRedo getUndoRedo();
    

    /**
     * Use the passed in callback instance for manipulating the enclosing multiview component, keep the instance around
     * during lifecycle of the component if you want to automatically switch to this component etc.
     * The enclosing window enviroment attaches the callback right after creating 
     * the element from the description.
     * Same applies for deserialization of MultiViewTopComponent, thus MultiViewElement 
     * implementors shall not attempt to serialize the passed instance.
     */ 
    void setMultiViewCallback (MultiViewElementCallback callback);
    
    /**
     * Element decides if it can be safely closed. The element shall just return a value 
     * (created by MultiViewFactory.createCloseState() factory method),
     * not open any UI, that is a semantical difference from TopComponent.canClose().
     * The CloseOperationHandler is the centralized place to show dialogs to the user.
     * In cases when the element is consistent, just return CloseOperationState.STATE_OK.
     */
    CloseOperationState canCloseElement();
    
   
    /** Registers a {@link MultiViewElement}, so it is available in 
     * {@link MimeLookup} and can be found and located by 
     * {@link MultiViews#createCloneableMultiView}
     * and
     * {@link MultiViews#createMultiView}
     * factory methods. 
     * <p>
     * The element class may have default constructor or a constructor that 
     * takes {@link Lookup} as an argument. Similarly for a factory method.
     */
    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Registration {
        /** Mime type this registration is associated with.
         * 
         * @return array of mime types like "text/java", "text", etc.
         */
        public String[] mimeType();
        
        /** Gets persistence type of multi view element, the TopComponent will decide
         * on it's own persistenceType based on the sum of all it's elements.
         * {@link org.openide.windows.TopComponent#PERSISTENCE_ALWAYS} has higher priority than {@link org.openide.windows.TopComponent#PERSISTENCE_ONLY_OPENED}
         * and {@link org.openide.windows.TopComponent#PERSISTENCE_NEVER} has lowest priority.
         * The {@link org.openide.windows.TopComponent} will be stored only if at least one element requesting persistence
         * was made visible.
         */
        public int persistenceType();

        /** 
         * Gets localized display name of multi view element. Will be placed on the Element's toggle button.
         *@return localized display name
         */
        public String displayName();

        /** 
         * Icon for the MultiViewDescription's multiview component. Will be shown as TopComponent's icon
         * when this element is selected. If the icon is not provided, then the
         * multiview component will use the icon provided by the first {@link MultiViewDescription}.
         * 
         * @return the icon of multi view element 
         */
        public String iconBase() default "";

        /**
         * A Description's contribution 
         * to unique {@link org.openide.windows.TopComponent}'s Id returned by <code>getID</code>. Returned value is used as starting
         * value for creating unique {@link org.openide.windows.TopComponent} ID for whole enclosing multi view
         * component.
         * Value should be preferably unique, but need not be.
         */
        public String preferredID();

        /** Position of this element amoung the list of other elements.
         * @return integer value
         */
        public int position() default Integer.MAX_VALUE;
    }
}
