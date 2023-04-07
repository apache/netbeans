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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.multiview.MultiViewModel.ActionRequestObserverFactory;
import org.netbeans.core.multiview.MultiViewModel.ElementSelectionListener;
import org.netbeans.core.spi.multiview.CloseOperationHandler;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.openide.awt.UndoRedo;
import org.openide.text.CloneableEditorSupport.Pane;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.WeakListeners;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/** Special subclass of TopComponent which shows and handles set of
 * MultiViewElements, shows them in switchable toggle buttons style, along
 * with toolbars of actions associated with individual view elements.
 *
 *
 * @author Dafe Simonek, Milos Kleint
 */
public final class MultiViewPeer implements PropertyChangeListener {
    static final String MULTIVIEW_ID = "MultiView-"; //NOI18N

    private static final String TOOLBAR_VISIBLE_PROP = /* org.netbeans.api.editor.settings.SimpleValueNames.TOOLBAR_VISIBLE_PROP */ "toolbarVisible"; // NOI18N
    private static final Preferences editorSettingsPreferences; 
    static {
        Preferences p = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        String n;
        if (p == null && (n = System.getProperty("test.multiview.toolbar.settings")) != null) { // NOI18N
            p = NbPreferences.root().node(n);
        }
        editorSettingsPreferences = p;
    }

    private Lookup.Provider context;
    private String mimeType;
    
    MultiViewModel model;
    TabsComponent tabs;
    SelectionListener selListener;
    CloseOperationHandler closeHandler;
    transient MultiViewTopComponentLookup lookup;
    TopComponent peer;
    private ActionRequestObserverFactory factory;
    private MultiViewActionMap delegatingMap;
    private boolean activated = false;
    private final PreferenceChangeListener editorSettingsListener = new PreferenceChangeListenerImpl();
    private final PropertyChangeListener propListener;
    private DelegateUndoRedo delegateUndoRedo;
    private int splitOrientation = -1;
    private int initialSplitOrientation = -1;
    private MultiViewDescription initialSplitDescription;
    
    MultiViewPeer(TopComponent pr, ActionRequestObserverFactory fact) {
        selListener = new SelectionListener();
        peer = pr;
        factory = fact;
        delegateUndoRedo = new DelegateUndoRedo();
        propListener = WeakListeners.propertyChange(this, null);
    }
    
    void copyMimeContext(MultiViewPeer other) {
        this.context = other.context;
        this.mimeType = other.mimeType;
    }
    
    /** @param context context needs to be also serializable */
    public void setMimeLookup(String mimeType, Lookup.Provider context) {
        this.context = context;
        this.mimeType = mimeType;
        
        List<MultiViewDescription> arr = new ArrayList<MultiViewDescription>();
        final Lookup lkp = MimeLookup.getLookup(mimeType);
        for (ContextAwareDescription d : lkp.lookupAll(ContextAwareDescription.class)) {
            d = d.createContextAwareDescription(context.getLookup(), false);
            arr.add(d);
            d = d.createContextAwareDescription(context.getLookup(), true);
            arr.add(d);
        }
        if (arr.isEmpty()) {
            arr.add(new EmptyViewDescription(mimeType));
        }
        if (model != null) {
            model.removeElementSelectionListener(selListener);
        }
        model = new MultiViewModel(arr.toArray(new MultiViewDescription[0]), arr.get(0), factory);
        model.addElementSelectionListener(selListener);
        tabs.setModel(model);
        CloseOperationHandler h = lkp.lookup(CloseOperationHandler.class);
        if (h == null) {
            h = SpiAccessor.DEFAULT.createDefaultCloseHandler();
        }
        closeHandler = h;
    }
    
    
    public void setMultiViewDescriptions(MultiViewDescription[] descriptions, MultiViewDescription defaultDesc) {
        assert context == null;
        _setMultiViewDescriptions(descriptions, defaultDesc);
    }
    
    private void _setMultiViewDescriptions(MultiViewDescription[] descriptions, MultiViewDescription defaultDesc) {
        Map<MultiViewDescription, MultiViewElement> createdElements = Collections.emptyMap();
        if (model != null) {
            model.removeElementSelectionListener(selListener);
            createdElements = model.getCreatedElementsMap();
        }
        model = new MultiViewModel(descriptions, defaultDesc, factory, createdElements);
        model.addElementSelectionListener(selListener);
        tabs.setModel(model);
    }
    
    public void setCloseOperationHandler(CloseOperationHandler handler) {
        assert context == null;
        closeHandler = handler;
    }
    
    void setDeserializedMultiViewDescriptions(int splitOrientation, MultiViewDescription[] descriptions,
       MultiViewDescription defaultDesc, MultiViewDescription defaultDescSplit, Map<MultiViewDescription, MultiViewElement> existingElements) {
        if (model != null) {
            model.removeElementSelectionListener(selListener);
        }
	// if Design view was active before closing, set default to Source view
	if( splitOrientation != -1 )
            defaultDescSplit = defaultDescSplit.getDisplayName().startsWith("&Design") ? descriptions[1] : defaultDescSplit; //NOI18N
        model = new MultiViewModel(descriptions, defaultDesc, factory, existingElements);
        model.addElementSelectionListener(selListener);
	tabs.setModel(model);
	this.initialSplitOrientation = splitOrientation;
        this.initialSplitDescription = defaultDescSplit;
    }
    
    /**
     * for use in tests only!!!!!
     */
    MultiViewModel getModel() {
        return model;
    }
    
    
    void initComponents() {
        initActionMap();
        peer.setLayout(new BorderLayout());
        tabs = new TabsComponent(isToolbarVisible());
        peer.add(tabs);
        ActionMap map = peer.getActionMap();
        Action act = new AccessTogglesAction();
        map.put("NextViewAction", new GetRightEditorAction()); //NOI18N
        map.put("PreviousViewAction", new GetLeftEditorAction()); //NOI18N
        map.put("accesstoggles", act); //NOI18N
        InputMap input = peer.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke stroke = KeyStroke.getKeyStroke("control F10"); //NOI18N
        input.put(stroke, "accesstoggles"); //NOI18N
//        stroke = (KeyStroke)new GetLeftEditorAction().getValue(Action.ACCELERATOR_KEY);
//        input.put(stroke, "getLeftEditor");
        input = peer.getInputMap(JComponent.WHEN_FOCUSED);
        input.put(stroke, "accesstoggles"); //NOI18N
        
        peer.putClientProperty("MultiViewBorderHack.topOffset", new Integer(tabs.getPreferredSize().height - 1));
    }

    private void assignLookup(MultiViewElement el, MultiViewTopComponentLookup lkp) {
        Lookup elementLookup = el.getLookup();
        assert null != elementLookup : "Null lookup from " + el;
        lkp.setElementLookup(elementLookup);
    }
    private void assignLookup(MultiViewElement el) {
        assignLookup(el, (MultiViewTopComponentLookup)peer.getLookup());
    }
    
    final void assignLookup(MultiViewTopComponentLookup lkp) {
        if (lkp.isInitialized()) {
            return;
        }
        final MultiViewElement el = getModel().getActiveElement();
        if (el != null) {
            assignLookup(el, lkp);
        }
    }
    
  // It is necessary so the old actions (clone and close from org.openide.actions package) remain working.
    // cannot use the
    private void initActionMap() {
        delegatingMap = new MultiViewActionMap(peer, new ActionMap ());
        if(peer instanceof TopComponent.Cloneable) {
            delegatingMap.put("cloneWindow", new javax.swing.AbstractAction() { // NOI18N
                public void actionPerformed(ActionEvent evt) {
                    TopComponent cloned = ((TopComponent.Cloneable)
                        peer).cloneComponent();
                    cloned.open();
                    cloned.requestActive();
                }
            });
        }
	if(peer instanceof MultiViewTopComponent || peer instanceof MultiViewCloneableTopComponent) {
            delegatingMap.put("splitWindowHorizantally", new javax.swing.AbstractAction() { // NOI18N
                @Override
                public void actionPerformed(ActionEvent evt) {
		    TopComponent split;
		    if(peer instanceof Splitable) {
			split = ((Splitable) peer).splitComponent(JSplitPane.HORIZONTAL_SPLIT, -1);
                        split.open();
                        split.requestActive();
		    }
                }
            });
            delegatingMap.put("splitWindowVertically", new javax.swing.AbstractAction() { // NOI18N
                @Override
                public void actionPerformed(ActionEvent evt) {
		    TopComponent split;
		    if(peer instanceof Splitable) {
			split = ((Splitable) peer).splitComponent(JSplitPane.VERTICAL_SPLIT, -1);
                        split.open();
                        split.requestActive();
		    }
                }
            });
            delegatingMap.put("clearSplit", new javax.swing.AbstractAction() { // NOI18N
		@Override
                public void actionPerformed(ActionEvent evt) {
		    TopComponent original;
		    if(peer instanceof Splitable) {
			original = ((Splitable) peer).clearSplit(-1);
                        original.open();
                        original.requestActive();
		    }
                }
            });
        }
        delegatingMap.put("closeWindow", new javax.swing.AbstractAction() { // NOI18N
           public void actionPerformed(ActionEvent evt) {
               peer.close();
           }
        });
        peer.setActionMap(delegatingMap);
    }        
    
    void peerComponentClosed() {
        Iterator<MultiViewElement> it = model.getCreatedElements().iterator();
        while (it.hasNext()) {
            MultiViewElement el = it.next();
            model.markAsHidden(el);
            el.componentClosed();
        }
        tabs.peerComponentClosed();
    }
    
    void peerComponentShowing() {
        MultiViewElement el = model.getActiveElement();
        el.componentShowing();
        delegatingMap.setDelegateMap(el.getVisualRepresentation().getActionMap());
        assignLookup(el);
        JComponent jc = el.getToolbarRepresentation();
        assert jc != null : "MultiViewElement " + el.getClass() + " returns null as toolbar component."; //NOI18N
        jc.setOpaque(false);
	boolean isSplitDescription = false;
	MultiViewDescription desc = model.getActiveDescription();
	if(desc instanceof ContextAwareDescription) {
	    isSplitDescription = ((ContextAwareDescription)desc).isSplitDescription();
	}
        tabs.setInnerToolBar(jc, isSplitDescription);
        tabs.setToolbarBarVisible(isToolbarVisible());
        if (editorSettingsPreferences != null) {
            editorSettingsPreferences.addPreferenceChangeListener(editorSettingsListener);
        }
        if( initialSplitOrientation != -1 ) {
            splitOrientation = initialSplitOrientation;
            tabs.peerSplitComponent(splitOrientation, MultiViewPeer.this, getModel().getActiveDescription(), initialSplitDescription, -1);
            initialSplitDescription = null;
            initialSplitOrientation = -1;
        }
    }
    
    void peerComponentHidden() {
        model.getActiveElement().componentHidden();
        if (editorSettingsPreferences != null) {
            editorSettingsPreferences.removePreferenceChangeListener(editorSettingsListener);
        }
    }
    
    void peerComponentDeactivated() {
        activated = false;
        model.getActiveElement().componentDeactivated();
    }
    
    boolean isActivated() {
        return activated;
    }
    
    void peerComponentActivated() {
        activated = true;
        model.getActiveElement().componentActivated();
    }
    
    void peerComponentOpened() {
        showCurrentElement(true);
        tabs.setToolbarBarVisible(isToolbarVisible());
        addPropertyChangeListeners();
    }
    
    private void addPropertyChangeListeners() {
        if( null != model ) {
            for (MultiViewDescription mvd : model.getDescriptions()) {
                if( mvd instanceof ContextAwareDescription && ((ContextAwareDescription)mvd).isSplitDescription() )
                    continue; //#240371 - don't update name from spit elements
                
                MultiViewElement el = model.getElementForDescription( mvd, false );
                if (el == null) {
                    continue;
                }
                if (el.getVisualRepresentation() instanceof Pane) {
                    Pane pane = (Pane)el.getVisualRepresentation();
                    final CloneableTopComponent tc = pane.getComponent();
                    if (!Arrays.asList(tc.getPropertyChangeListeners()).contains(propListener)) {
                        tc.addPropertyChangeListener(propListener);
                    }
                }
            }
        }
    }

    void peerSplitComponent(int orientation, int splitPosition) {
	splitOrientation = orientation;
	tabs.peerSplitComponent(orientation, this, null, null, splitPosition);
    }

    void peerClearSplit( int elementToActivate ) {
	tabs.peerClearSplit( elementToActivate );
	showCurrentElement();
	model.fireActivateCurrent();
	splitOrientation = -1;
    }

    int getSplitOrientation() {
	return splitOrientation;
    }
    
    boolean requestFocusInWindow() {
        // somehow this may be called when model is null
        if (model == null) {
            return false;
        }
        return model.getActiveElement().getVisualRepresentation().requestFocusInWindow();
    }
    
    void requestFocus() {
        // somehow this may be called when model is null
        if (model != null) {
            model.getActiveElement().getVisualRepresentation().requestFocus();
        }
    }
    
    /**
     * hides the old element when switching elements.
     */
    void hideElement(MultiViewDescription desc) {
	if (desc != null && splitOrientation != -1) {
	    MultiViewDescription topDesc = tabs.getTopComponentDescription();
	    MultiViewDescription bottomDesc = tabs.getBottomComponentDescription();
	    if (tabs.isHiddenTriggeredByMultiViewButton()
		    && (!topDesc.getDisplayName().equals(desc.getDisplayName())
		    || !bottomDesc.getDisplayName().equals(desc.getDisplayName()))) {
		MultiViewElement el = model.getElementForDescription(desc);
		el.componentHidden();
	    }
	    return;
	}
        if (desc != null) {
            MultiViewElement el = model.getElementForDescription(desc);
            el.componentHidden();
        }
    }

    
    void showCurrentElement() {
        showCurrentElement(false);
    }
    
    /**
     * shows the new element after switching elements.
     */
    
    private void showCurrentElement(boolean calledFromComponentOpened) {
        MultiViewElement el = model.getActiveElement();
        MultiViewDescription desc = model.getActiveDescription();

	// TODO display name is not a good unique id..
	// also consider a usecase where multiple elements point to a single visual component.
	//. eg. property sheet uses same component and only changes model.
	// in this case we probably should not remove and add the component from awt hierarchy
	boolean isSplitDescription = false;
	if(desc instanceof ContextAwareDescription) {
	    isSplitDescription = ((ContextAwareDescription)desc).isSplitDescription();
	}
        tabs.switchToCard(el, desc.getDisplayName(), isSplitDescription);
        if( null == peer.getIcon() ) {
            Image icon = desc.getIcon();
            if( null == icon ) {
                //#204072
                MultiViewDescription[] descriptions = model.getDescriptions();
                if( null != descriptions && descriptions.length > 0 )
                    icon = descriptions[0].getIcon();
            }
            peer.setIcon(icon);
        }
        // the first time the component is shown, we need to call componentOpened() on it to be in synch with current
        // TopComponent behaviour?
        if (peer.isOpened() || calledFromComponentOpened) {
            if (!model.wasShownBefore(el)) {
                el.componentOpened();
                model.markAsShown(el);
            }
        }
        if (!calledFromComponentOpened) {
            //#68199
            // replace isOpened() with isVisible() because some multiview don't have to be directly referenced form the
            // winsys codebase
            if (peer.isVisible()) {
                el.componentShowing();
            }
        // should we really set the stuff only when not called from componentOpened()? maybe it's better to call it twice sometimes.
            // if we don't call it here for opened but not showing component, then the map, lookup and nodes will not be initialized properly.
            // is it a problem?
            delegatingMap.setDelegateMap(el.getVisualRepresentation().getActionMap());
            assignLookup(el);
            
            if (peer.isVisible()) {
                tabs.setInnerToolBar(el.getToolbarRepresentation(), isSplitDescription);
                tabs.setToolbarBarVisible(isToolbarVisible());
            }
            
        }
    }
    
    
    
    /**
     * merge action for the topcomponent and the enclosed MultiViewElement..
     * 
     */
    Action[] peerGetActions(Action[] superActs) {
        //TEMP don't delegate to element's actions..
        Action[] acts = model.getActiveElement().getActions();
        
        // copy super actions as we will possibly null it in cycle later
        Action[] superActions = new Action[superActs.length];
        System.arraycopy(superActs, 0, superActions, 0, superActs.length);
        
        for (int i = 0; i < acts.length; i++) {
            Action act = acts[i];
            for (int j = 0 ; j < superActions.length; j++) {
                Action superact = superActions[j];
                if (act == null && superact == null){ // just to same some time.
                    break;
                }
                if (superact != null && act != null && superact.getClass().equals(act.getClass())) {
                    // these are the default topcomponent actions.. we need to replace them
                    // in order to have the correct context.
                    acts[i] = superActions[j];
                    // to keep superact.getClass().equals(act.getClass()) from filtering out
                    // different instances of the same action, null out the superActions
                    // array as you go.  
                    superActions[j] = null;
                    break;
                }                
            }
        }
        return acts;
    }
    
    MultiViewHandlerDelegate getMultiViewHandlerDelegate() {
        // TODO have one handler only or create a new one each time?
        return new MVTCHandler();
    }
    
    /**
     * Delegates the value to the element descriptions.
     */
    int getPersistenceType() {
        
        // should also take the opened/created elements into account.
        // no need to serialize the tc when the element that want to be serialized, was not 
        // even opened?!? but maybe handle this during the serialization proceess, avoid creating
        // the element when serializing.
        int type = TopComponent.PERSISTENCE_NEVER;
        if( null != model ) {
            MultiViewDescription[] descs = model.getDescriptions();
            for (int i = 0; i < descs.length; i++) {
                if (context == null && !(descs[i] instanceof Serializable)) {
                    Logger.getLogger(MultiViewTopComponent.class.getName()).warning(
                            "The MultiviewDescription instance " + descs[i].getClass() + " is not serializable. Cannot persist TopComponent.");
                    type = TopComponent.PERSISTENCE_NEVER;
                    break;
                }
                if (descs[i].getPersistenceType() == TopComponent.PERSISTENCE_ALWAYS) {
                    type = descs[i].getPersistenceType();
                    // cannot ge any better than that.
                }
                if (descs[i].getPersistenceType() == TopComponent.PERSISTENCE_ONLY_OPENED &&
                     type != TopComponent.PERSISTENCE_ALWAYS) {
                    type = descs[i].getPersistenceType();
                    // go on searching..
                }

            }
        }
        return type;
    }  
    
    String preferredID() {
        StringBuffer retValue = new StringBuffer(MULTIVIEW_ID);
        assert model != null : "Multiview Model not set, error on deserialization of client code. " + //NOI18N
                "Please add comment to issue #121119 at netbeans.org and attach the ~/.netbeans/var/log/messages.log file"; //NOI18N
        MultiViewDescription[] descs = model.getDescriptions();
        for (int i = 0; i < descs.length; i++) {
            retValue.append(descs[i].preferredID());
            retValue.append("|"); //NOI18N
        }
        return retValue.toString();
    }
    
    
    
    /** Serialize this top component.
    * Subclasses wishing to store state must call the super method, then write to the stream.
    * @param out the stream to serialize to
    */
    void peerWriteExternal (ObjectOutput out) throws IOException {
        boolean fromMime;
        if (context != null) {
            out.writeObject(mimeType);
            out.writeObject(context);
            fromMime = true;
        } else {
            if (closeHandler != null) {
                if (closeHandler instanceof Serializable) {
                    out.writeObject(closeHandler);
                } else {
                    //TODO some warning to the SPI programmer
                    Logger.getAnonymousLogger().info(
                           "The CloseOperationHandler isn not serializable. MultiView component id=" + preferredID());
                }
            }
            fromMime = false;
        }
        MultiViewDescription[] descs = model.getDescriptions();
	MultiViewDescription curr = tabs.getTopComponentDescription();
	MultiViewDescription currSplit = tabs.getBottomComponentDescription();
        int currIndex = 0;
        int currIndexSplit = 0;

        for (int i = 0; i < descs.length; i++) {
            if( descs[i] instanceof RuntimeMultiViewDescription ) {
                continue; //don't store multiview elements added at runtime
            }
            if (!fromMime) {
                out.writeObject(descs[i]);
            } else {
                out.writeObject(descs[i].preferredID());
            }
            if (descs[i].getPersistenceType() != TopComponent.PERSISTENCE_NEVER) {
                // only those requeTopsted and previously created elements are serialized.
                MultiViewElement elem = model.getElementForDescription(descs[i], false);
                if (elem instanceof Serializable) {
                    out.writeObject(elem);
                }
            }
            if (descs[i] == curr) {
                currIndex = i;
            }
	    if (descs[i] == currSplit) {
                currIndexSplit = i;
            }
        }
        out.writeObject(new Integer(currIndex));
        out.writeObject(new Integer(currIndexSplit));
	out.writeObject(new Integer(splitOrientation));
        String htmlDisplayName = peer.getHtmlDisplayName();
        if( null != htmlDisplayName )
            out.writeObject(htmlDisplayName);
        
    }

    /** Deserialize this top component.
    * Subclasses wishing to store state must call the super method, then read from the stream.
    * @param in the stream to deserialize from
    */
    void peerReadExternal (ObjectInput in) throws IOException, ClassNotFoundException {
        ArrayList<MultiViewDescription> descList = new ArrayList<MultiViewDescription>();
        HashMap<MultiViewDescription, MultiViewElement> map = new HashMap<MultiViewDescription, MultiViewElement>();
        int current = 0;
        int currentSplit = 0;
        int splitOrient = -1;
        CloseOperationHandler close = null;
        try {
            int counting = 0;
	    int intCounting = 0;
            MultiViewDescription lastDescription = null;
            while (true) {
                Object obj = in.readObject();
                if ((obj instanceof String) && counting++ == 0) {
                    Lookup.Provider lp = (Lookup.Provider)in.readObject();
                    setMimeLookup((String)obj, lp);
                    descList.addAll(Arrays.asList(model.getDescriptions()));
                    continue;
                }
                if (obj instanceof MultiViewDescription) {
                    lastDescription = (MultiViewDescription)obj;
                    descList.add(lastDescription);
                }
                else if (obj instanceof String) {
                    boolean match = false;
                    for (MultiViewDescription md : descList) {
                        if (md.preferredID().equals(obj)) {
                            lastDescription = md;
                            match = true;
                            break;
                        }
                    }
                    if( !match ) {
                        throw new IOException( "Cannot find multiview description for id \"" + obj
                                + "\". Maybe some module(s) is not installed or activated." );
                    }
                }
                else if (obj instanceof MultiViewElement) {
                    assert lastDescription != null;
                    map.put(lastDescription, (MultiViewElement)obj);
                    lastDescription = null;
                }
                else if (obj instanceof Integer)  {
                    Integer integ = (Integer)obj;
		    if(intCounting == 0) {
			intCounting++;
			current = integ.intValue();
		    } else if (intCounting == 1) {
			intCounting++;
			currentSplit = integ.intValue();
		    } else if (intCounting == 2) {
			splitOrient = integ.intValue();
			break;
		    }
                } 
                if (obj instanceof CloseOperationHandler) {
                    close = (CloseOperationHandler)obj;
                }
            }

            try {
                Object htmlDisplayName = in.readObject();
                if( htmlDisplayName instanceof String ) {
                    peer.setHtmlDisplayName( (String)htmlDisplayName );
                }
            } catch( OptionalDataException odE ) {
                if( odE.eof ) {
                    //end of file, HTML description field is not present
                } else {
                    throw odE;
                }
            }
        } catch (IOException exc) {
                //#121119 try preventing model corruption when deserialization of client code fails.
            if (context == null) {
                if (close == null) {
                    //TODO some warning to the SPI programmer
                    close = SpiAccessor.DEFAULT.createDefaultCloseHandler();
                }
                setCloseOperationHandler(close);
            }
            if (descList.size() > 0) {
                MultiViewDescription[] descs = new MultiViewDescription[descList.size()];
                descs = descList.toArray(descs);
                //the integer with current element was not read yet, fallback to zero.
                MultiViewDescription currDesc = descs[0];
                MultiViewDescription currDescSplit = descs[1];

                //when error, ignore any deserialized elements..
                map.clear();
                setDeserializedMultiViewDescriptions(1, descs, currDesc, currDescSplit, map);
            }
            
            throw exc;
        }
        if (context == null) {
            if (close == null) {
                    //TODO some warning to the SPI programmer
                close = SpiAccessor.DEFAULT.createDefaultCloseHandler();
            }
            setCloseOperationHandler(close);
        }
        // now that we've read everything, we should set it correctly.
        MultiViewDescription[] descs = new MultiViewDescription[descList.size()];
        descs = descList.toArray(descs);
        MultiViewDescription currDesc = descs[current];
        MultiViewDescription currDescSplit = descs[currentSplit];
        setDeserializedMultiViewDescriptions(splitOrient, descs, currDesc, currDescSplit, map);
    }    

    private Action[] getDefaultTCActions() {
        //TODO for each suppoerted peer have one entry..
        if (peer instanceof MultiViewTopComponent) {
            return ((MultiViewTopComponent)peer).getDefaultTCActions();
        }
        return new Action[0];
    }
    

    
    JEditorPane getEditorPane() {
        if (model != null) {
            MultiViewElement el = model.getActiveElement();
            if (el != null && el.getVisualRepresentation() instanceof Pane) {
                Pane pane = (Pane)el.getVisualRepresentation();
                return pane.getEditorPane();
            }
        }
        return null;
    }
    
    HelpCtx getHelpCtx() {
        return model.getActiveDescription().getHelpCtx();
    }
    
    /**
     * Get the undo/redo support for this component.
     * The default implementation returns a dummy support that cannot
     * undo anything.
     *
     * @return undoable edit for this component
     */
    UndoRedo peerGetUndoRedo() {
        return delegateUndoRedo;
    }    
    
    private UndoRedo privateGetUndoRedo() {
        return model.getActiveElement().getUndoRedo() != null ? model.getActiveElement().getUndoRedo() : UndoRedo.NONE;
    }
    
    /**
     * This method is called when this <code>TopComponent</code> is about to close.
     * Delegates to CloseOperationHandler.
     */
    boolean canClose() {
        Collection<MultiViewElement> col = model.getCreatedElements();
        Iterator<MultiViewElement> it = col.iterator();
        Collection<CloseOperationState> badOnes = new ArrayList<>();
        while (it.hasNext()) {
           MultiViewElement el = it.next();
           CloseOperationState state = el.canCloseElement();
           if (!state.canClose()) {
               badOnes.add(state);
           }
        }
        if (badOnes.size() > 0) {
            CloseOperationState[] states = new CloseOperationState[badOnes.size()];
            states = badOnes.toArray(states);
            boolean res = closeHandler.resolveCloseOperation(states);
            if( res && SpiAccessor.DEFAULT.shouldCheckCanCloseAgain(closeHandler) ) {
                //#236369 - check if everything saved ok
                col = model.getCreatedElements();
                it = col.iterator();
                while (it.hasNext()) {
                   MultiViewElement el = it.next();
                   CloseOperationState state = el.canCloseElement();
                   if (!state.canClose()) {
                       res = false;
                       break;
                   }
                }
            }
            return res;
        }
        return true;
    }
    
    // from CloneableEditor.Pane
    public void updateName() {
        // is called before setMultiViewDescriptions() need to check for null.
        if (model != null) {
            for (MultiViewDescription mvd : model.getDescriptions()) {
                if( mvd instanceof ContextAwareDescription && ((ContextAwareDescription)mvd).isSplitDescription() )
                    continue; //#240371 - don't update name from spit elements
                
                MultiViewElement el = model.getElementForDescription(
                    mvd, MultiViewCloneableTopComponent.isSourceView(mvd)
                );
                if (el == null) {
                    continue;
                }
                if (el.getVisualRepresentation() instanceof Pane) {
                    Pane pane = (Pane)el.getVisualRepresentation();
                    pane.updateName();
                    final CloneableTopComponent tc = pane.getComponent();
                    peer.setDisplayName(tc.getDisplayName());
                    peer.setIcon(tc.getIcon());
                    if (!Arrays.asList(tc.getPropertyChangeListeners()).contains(propListener)) {
                        tc.addPropertyChangeListener(propListener);
                    }
                }
            }
        }
    }
    
    public Lookup getLookup() {
        if (lookup == null) {
            lookup = new MultiViewTopComponentLookup(delegatingMap);
        }
        return lookup;
    }
    
    
    private boolean isToolbarVisible() {
        return editorSettingsPreferences == null || editorSettingsPreferences.getBoolean(TOOLBAR_VISIBLE_PROP, true);
    }

    
    @Override
    public String toString() {
        return "[model=" + model + "]"; // NOI18N
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (
            "icon".equals(evt.getPropertyName()) || // NOI18N
            "name".equals(evt.getPropertyName()) || // NOI18N
            "displayName".equals(evt.getPropertyName()) || // NOI18N
            "htmlDisplayName".equals(evt.getPropertyName()) // NOI18N
        ) {
            updateName();
        }
    }
    /**
     * notification from the model that the selection changed.
     */
    private class SelectionListener implements ElementSelectionListener {
        
        public void selectionChanged(MultiViewDescription oldOne, MultiViewDescription newOne) {
            if (isActivated()) {
                MultiViewElement el = model.getElementForDescription(oldOne);
                el.componentDeactivated();
            }
            hideElement(oldOne);
            showCurrentElement();
            delegateUndoRedo.updateListeners(model.getElementForDescription(oldOne),
                                             model.getElementForDescription(newOne));
        }
        
        public void selectionActivatedByButton() {
            MultiViewElement elem = model.getActiveElement();
            elem.getVisualRepresentation().requestFocus();
            elem.componentActivated();
        }
        
    }
    
    private class MVTCHandler implements MultiViewHandlerDelegate {
        private MultiViewPerspective[] perspectives = null;
        
        public MultiViewPerspective[] getDescriptions() {
            return model.getPerspectives();
        }
        
        public MultiViewPerspective getSelectedDescription() {
            return model.getSelectedPerspective();
        }
        
        public void requestActive(MultiViewPerspective pers) {
            MultiViewDescription desc = Accessor.DEFAULT.extractDescription(pers);
            if (model.getActiveDescription() != desc) {
                tabs.changeActiveManually(desc);
                model.getActiveElement().componentActivated();
            }
        }
        
        public void requestVisible(MultiViewPerspective pers) {
            MultiViewDescription desc = Accessor.DEFAULT.extractDescription(pers);
            tabs.changeVisibleManually(desc);
        }
        
//        public MultiViewPerspectiveComponent getElementForDescription(MultiViewPerspective pers) {
//            MultiViewDescription desc = Accessor.DEFAULT.extractDescription(pers);
//            return model.getMVComponentForDescription(desc);
//        }
        
        @Override
        public void addMultiViewDescription(MultiViewDescription descr, int position) {
            if( -1 != splitOrientation )
                peerClearSplit(0);
            MultiViewDescription[] oldDesc = model.getDescriptions();
            if( position < 0 || position >= oldDesc.length/2 )
                position = oldDesc.length/2;
            RuntimeMultiViewDescription wrapper = new RuntimeMultiViewDescription(descr, false);
            RuntimeMultiViewDescription splitWrapper = new RuntimeMultiViewDescription(descr, true);
            MultiViewDescription[] newDesc = new MultiViewDescription[oldDesc.length+2];
            int index = 0;
            for( int i=0; i<newDesc.length/2; i++ ) {
                if( i == position ) {
                    newDesc[2*i] = wrapper;
                    newDesc[2*i+1] = splitWrapper;
                } else {
                    newDesc[2*i] = oldDesc[index++];
                    newDesc[2*i+1] = oldDesc[index++];
                }
            }
            _setMultiViewDescriptions(newDesc, null);
            tabs.changeActiveManually(wrapper);
        }
        
        @Override
        public void removeMultiViewDescription(MultiViewDescription descr) {
            MultiViewDescription[] oldDesc = model.getDescriptions();
            int position = -1;
            for( int i=0; i<oldDesc.length/2; i++ ) {
                if( oldDesc[2*i] instanceof RuntimeMultiViewDescription ) {
                    RuntimeMultiViewDescription runtimeDesc = (RuntimeMultiViewDescription) oldDesc[2*i];
                    if( runtimeDesc.delegate.equals(descr) ) {
                        position = i;
                        break;
    }
                }
            }
            if( position < 0 )
                return; //trying to remove multiview description that isn't in our model
    
            if( -1 != splitOrientation )
                peerClearSplit(0);

            MultiViewDescription[] newDesc = new MultiViewDescription[oldDesc.length-2];
            int index = 0;
            for( int i=0; i<oldDesc.length/2; i++ ) {
                if( i == position ) {
                    continue;
                }
                newDesc[index++] = oldDesc[2*i];
                newDesc[index++] = oldDesc[2*i+1];
            }
            _setMultiViewDescriptions(newDesc, null);
            tabs.changeActiveManually(newDesc[0]);
            model.setActiveDescription(newDesc[0]);
            showCurrentElement();
        }
    }
    
    private static class RuntimeMultiViewDescription implements ContextAwareDescription {
        private final MultiViewDescription delegate;
        private final boolean split;
        
        public RuntimeMultiViewDescription( MultiViewDescription delegate, boolean split ) {
            this.delegate = delegate;
            this.split = split;
        }

        @Override
        public ContextAwareDescription createContextAwareDescription(Lookup context, boolean isSplitDescription) {
            return new RuntimeMultiViewDescription(delegate, isSplitDescription);
        }

        @Override
        public boolean isSplitDescription() {
            return split;
        }

        @Override
        public int getPersistenceType() {
            return delegate.getPersistenceType();
        }

        @Override
        public String getDisplayName() {
            return delegate.getDisplayName();
        }

        @Override
        public Image getIcon() {
            return delegate.getIcon();
        }

        @Override
        public HelpCtx getHelpCtx() {
            return delegate.getHelpCtx();
        }

        @Override
        public String preferredID() {
            return delegate.preferredID();
        }

        @Override
        public MultiViewElement createElement() {
            return delegate.createElement();
        }
    }

    private class AccessTogglesAction extends AbstractAction {
        
        AccessTogglesAction() {
//            putValue(Action.NAME, "AccessToggleMenu");
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control F10")); //NOI18N
        }
        
        public void actionPerformed(ActionEvent e) {
            tabs.requestFocusForSelectedButton();
            
        }
    }
    
    private class DelegateUndoRedo implements UndoRedo {
        
        private List<ChangeListener> listeners = new ArrayList<>();
        
        public boolean canUndo() {
            return privateGetUndoRedo().canUndo();
        }

        public boolean canRedo() {
            return privateGetUndoRedo().canRedo();
        }

        public void undo() throws CannotUndoException {
            privateGetUndoRedo().undo();
        }

        public void redo() throws CannotRedoException {
            privateGetUndoRedo().redo();
        }

        public void addChangeListener(ChangeListener l) {
            listeners.add(l);
            privateGetUndoRedo().addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
            privateGetUndoRedo().removeChangeListener(l);
        }

        public String getUndoPresentationName() {
            return privateGetUndoRedo().getUndoPresentationName();
        }

        public String getRedoPresentationName() {
            return privateGetUndoRedo().getRedoPresentationName();
        }
        
        private void fireElementChange() {
            Iterator<ChangeListener> it = new ArrayList<ChangeListener>(listeners).iterator();
            while (it.hasNext()) {
                ChangeListener elem = it.next();
                ChangeEvent event = new ChangeEvent(this);
                elem.stateChanged(event);
            }
            
        }
        
        void updateListeners(MultiViewElement old, MultiViewElement fresh) {
            Iterator<ChangeListener> it = listeners.iterator();
            while (it.hasNext()) {
                ChangeListener elem = it.next();
                if (old.getUndoRedo() != null) {
                    old.getUndoRedo().removeChangeListener(elem);
                }
                if (fresh.getUndoRedo() != null) {
                    fresh.getUndoRedo().addChangeListener(elem);
                }
            }
            fireElementChange();
        }
        
    }

    private class PreferenceChangeListenerImpl 
    implements PreferenceChangeListener, Runnable {
        public PreferenceChangeListenerImpl() {
        }

        public @Override void preferenceChange(PreferenceChangeEvent evt) {
            if (TOOLBAR_VISIBLE_PROP.equals(evt.getKey())) {
                EventQueue.invokeLater(this);
            }
        }
        public @Override void run() {
            tabs.setToolbarBarVisible(isToolbarVisible());
        }
    }
    
}
