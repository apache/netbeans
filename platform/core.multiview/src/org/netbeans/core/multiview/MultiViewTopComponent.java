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

package org.netbeans.core.multiview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.multiview.MultiViewModel.ActionRequestObserverFactory;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.Actions;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;


/** Special subclass of TopComponent which shows and handles set of
 * MultiViewElements, shows them in switchable toggle buttons style, along
 * with toolbars and actions associated with individual view elements.
 *
 *
 * @author Dafe Simonek, Milos Kleint
 */


public final class MultiViewTopComponent 
                            extends TopComponent 
                            implements ActionRequestObserverFactory, Splitable {

    MultiViewPeer peer;
                                           
    public MultiViewTopComponent() {
        super();
        peer = new MultiViewPeer(this, this);
        // initializes the multiview component.
        peer.initComponents();
        // assocuate lookup needs to come after the init.. initComponents() initializes actionMap
        associateLookup(peer.getLookup());
        setName("");
        setFocusCycleRoot(false);
    }
    
    public <T extends Serializable & Lookup.Provider> void setMimeLookup(String mimeType, T context) {
        peer.setMimeLookup(mimeType, context);
    }
    
    public void setMultiViewDescriptions(MultiViewDescription[] descriptions, MultiViewDescription defaultDesc) {
        peer.setMultiViewDescriptions(descriptions, defaultDesc);
    }
    
    public void setCloseOperationHandler(CloseOperationHandler handler) {
        peer.setCloseOperationHandler(handler);
    }
    
    private void setDeserializedMultiViewDescriptions(MultiViewDescription[] descriptions, 
                                                      MultiViewDescription defaultDesc, Map existingElements) {
        peer.setDeserializedMultiViewDescriptions(-1, descriptions, defaultDesc, null, existingElements);
    }
    
    MultiViewModel getModel() {
        return peer.getModel();
    }
    
    
    @Override
    public Lookup getLookup() {
        peer.assignLookup((MultiViewTopComponentLookup)super.getLookup());
        return super.getLookup();
    }
    
    @Override
    protected void componentClosed() {
        super.componentClosed();
        peer.peerComponentClosed();
    }
    
    @Override
    protected void componentShowing() {
        super.componentShowing();
        peer.peerComponentShowing();
    }
    
    @Override
    protected void componentHidden() {
        super.componentHidden();
        peer.peerComponentHidden();
    }
    
    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        peer.peerComponentDeactivated();
    }
    
    @Override
    protected void componentActivated() {
        super.componentActivated();
        peer.peerComponentActivated();
    }
    
    @Override
    protected void componentOpened() {
        super.componentOpened();
        peer.peerComponentOpened();
    }
    
    
    /**
     * merge action for the topcomponent and the enclosed MultiViewElement..
     * 
     */
    @Override
    public Action[] getActions() {
        //TEMP don't delegate to element's actions..
        Action[] superActions = superActions4Tests == null ? super.getActions() : superActions4Tests;
        List<Action> acts = new ArrayList<Action>(Arrays.asList(peer.peerGetActions(superActions)));
        if( !acts.isEmpty() )
            acts.add(null);
        acts.add(new EditorsAction());
        if( canSplit() ) {
            acts.add(new SplitAction(true));
        }
        return acts.toArray(new Action[0]);
    }
    
    @Override
    public boolean canSplit() {
        return null != peer.model && peer.model.canSplit();
    }
    
    /** Just for unit tests, allows to set actions coming from
     * underlying TopComponent. See MVTCTest.testFix_132948_MoreActionsOfSameClass
     * for details. */
    
    private Action[] superActions4Tests = null;
    
    void setSuperActions (Action[] acts) {
        superActions4Tests = acts;
    }
    
    public MultiViewHandlerDelegate getMultiViewHandlerDelegate() {
        // TODO have one handler only or create a new one each time?
        return peer.getMultiViewHandlerDelegate();
    }
    
    /**
     * Delegates the value to the element descriptions.
     */
    @Override
    public int getPersistenceType() {
        return peer.getPersistenceType();
    }  
    
    @Override
    protected String preferredID() {
        return peer.preferredID();
    }
    
    
    
    /** Serialize this top component.
    * Subclasses wishing to store state must call the super method, then write to the stream.
    * @param out the stream to serialize to
    */
    @Override
    public void writeExternal (ObjectOutput out) throws IOException {
        super.writeExternal(out);
        peer.peerWriteExternal(out);
    }

    /** Deserialize this top component.
    * Subclasses wishing to store state must call the super method, then read from the stream.
    * @param in the stream to deserialize from
    */
    @Override
    public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        peer.peerReadExternal(in);
    }    
    
    
    Action[] getDefaultTCActions() {
        return super.getActions();
    }
    
    public MultiViewElementCallback createElementCallback(MultiViewDescription desc) {
        return SpiAccessor.DEFAULT.createCallback(new ActReqObserver(desc));
    }
    
    
    @Override
    public HelpCtx getHelpCtx() {
        return peer.getHelpCtx();
    }

    @Override
    public String toString() {
        return "MultiViewTopComponent[name=" + getDisplayName() + ", peer=" + peer + "]";   // NOI18N
    }

    /**
     * Get the undo/redo support for this component.
     * The default implementation returns a dummy support that cannot
     * undo anything.
     *
     * @return undoable edit for this component
     */
    @Override
    public UndoRedo getUndoRedo() {
        UndoRedo retValue;
        retValue = peer.peerGetUndoRedo();
        if (retValue == null) {
            retValue = super.getUndoRedo();
        }
        return retValue;
    }    

    /**
     * This method is called when this <code>TopComponent</code> is about to close.
     * Delegates to CloseOperationHandler.
     */
    @Override
    public boolean canClose() {
        return peer.canClose();
    }

    /**
     * delegate to the apppropriate active element's component
     */
    @Override
    @SuppressWarnings("deprecation")
    public boolean requestFocusInWindow() {
        return peer.requestFocusInWindow();
    }

    /**
     * delegate to the apppropriate active element's component
     */
    @SuppressWarnings("deprecation")
    @Override
    public void requestFocus() {
        peer.requestFocus();
    }

    @Override
    public SubComponent[] getSubComponents() {
        return getSubComponents( peer );
    }

    static SubComponent[] getSubComponents( final MultiViewPeer peer ) {
        MultiViewModel model = peer.getModel();
        MultiViewPerspective[] perspectives = model.getPerspectives();
        ArrayList<SubComponent> res = new ArrayList<SubComponent>(perspectives.length);
        for( int i=0; i<perspectives.length; i++ ) {
            final MultiViewPerspective mvp = perspectives[i];
	    MultiViewDescription descr = Accessor.DEFAULT.extractDescription(mvp);
            if( descr instanceof ContextAwareDescription ) {
                //don't show split elements unless they're really showing on the screen
                ContextAwareDescription contextDescr = ( ContextAwareDescription ) descr;
                if( contextDescr.isSplitDescription() && !peer.tabs.isShowing( descr ) ) {
                    continue;
                }
            }
	    Lookup lookup = descr == null ? peer.getLookup() :
		    model.getElementForDescription(descr, false) == null ? peer.getLookup() : model.getElementForDescription(descr, false).getLookup();
	    boolean showing = peer.tabs.isShowing(descr);
            res.add( new SubComponent( Actions.cutAmpersand(mvp.getDisplayName()), null, new ActionListener() {

                @Override
                public void actionPerformed( ActionEvent e ) {
                    peer.getMultiViewHandlerDelegate().requestActive( mvp );
                }
            }, mvp == model.getSelectedPerspective(), lookup, showing ) );
        }
        return res.toArray(new SubComponent[0] );
    }

    @Override
    public TopComponent splitComponent(int orientation, int splitPosition) {
	peer.peerSplitComponent(orientation, splitPosition);
	return this;
    }

    @Override
    public TopComponent clearSplit(int splitElementToActivate) {
	peer.peerClearSplit(splitElementToActivate);
	return this;
    }

    @Override
    public int getSplitOrientation() {
	return peer.getSplitOrientation();
    }
    
//    public Lookup getLookup() {
//        return peer.getLookup(super.getLookup());
//    }
    
    /**
     * implementation of the MultiViewElement.ActionRequestObserver, manages activatation of the elements
     * and the TC itself based on requests from the elements.
     */
    class ActReqObserver implements Serializable, MultiViewElementCallbackDelegate {
        
        private static final long serialVersionUID =-3126744916624172415L;        
        private MultiViewDescription description;
        
        ActReqObserver(MultiViewDescription desc) {
            description = desc;
        }
        
        public void requestActive() {
            boolean activated = peer.isActivated();
            if (!activated) {
                MultiViewTopComponent.this.requestActive();
            }
            if (peer.model.getActiveDescription() != description) {
                peer.tabs.changeActiveManually(description);
                if (activated) {
                    peer.model.getActiveElement().componentActivated();
                }
            }
        }
        
        public void requestVisible() {
            peer.tabs.changeVisibleManually(description);
        }
        
        public Action[] createDefaultActions() {
            return MultiViewTopComponent.this.getDefaultTCActions();
        }
        
        public void updateTitle(String title) {
            MultiViewTopComponent.this.setDisplayName(title);
        }
        
        /** replace as null - should not be stored and read..*/
        public Object writeReplace() throws ObjectStreamException {
            return null;
        }

        /** Resolve as null -should not be stored and read..*/
        public Object readResolve() throws ObjectStreamException {
            return null;
        }
        
        public boolean isSelectedElement() {
            return (description.equals(peer.model.getActiveDescription()));
        }
        
        public TopComponent getTopComponent() {
            return MultiViewTopComponent.this;
        }
        
    }
    
}
