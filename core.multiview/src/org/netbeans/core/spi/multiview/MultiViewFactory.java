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

package org.netbeans.core.spi.multiview;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.multiview.ContextAwareDescription;
import org.netbeans.core.multiview.MultiViewCloneableTopComponent;
import org.netbeans.core.multiview.MultiViewTopComponent;
import org.netbeans.core.multiview.SourceCheckDescription;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/** Factory class for creating top components handling multi views.
 *
 * @author  Dafe Simonek, Milos Kleint
 */
public final class MultiViewFactory {
    
    /**
     * A utility singleton instance of MultiViewElement that does nothing.
     */
    
    public final static MultiViewElement BLANK_ELEMENT = new Blank();
    /**
     * a utility noop action instance to be used when no special handling is
     * required in createUnsafeCloseState() method.
     */
    public final static Action NOOP_CLOSE_ACTION = new NoopAction();
    
    

    /** Factory class, no instances. */
    private MultiViewFactory () {
    }

    /** Creates and returns new instance of top component with
     * multi views.
     * PLEASE NOTE: a non-cloneable TopComponent is not able to embed editors aka subclasses of CloneableEditor correctly.
     * Use createCloneableMultiView() method in such a case.
     * <p>
     * Please see {@link MultiViews#createMultiView} for loosely coupled variant
     * of this method which may be more suitable for modular environment.
     * 
     * @param descriptions array of descriptions of tabs in the multiview.
     * @param defaultDesc the initial selection, one of the descriptions array values.
     */
    public static TopComponent createMultiView (MultiViewDescription[] descriptions, MultiViewDescription defaultDesc) {
        return createMultiView(descriptions, defaultDesc, createDefaultCloseOpHandler());
    }

    /** Creates and returns new instance of top component with
     * multi views.
     * PLEASE NOTE: a non-cloneable TopComponent is not able to embed editors aka subclasses of CloneableEditor correctly.
     * Use createCloneableMultiView() method in such a case.
     * <p>
     * Please see {@link MultiViews#createMultiView} for loosely coupled variant
     * of this method which may be more suitable for modular environment.
     * 
     * @param descriptions  array of descriptions of tabs in the multiview.
     * @param defaultDesc  the initial selection, one of the descriptions array values.
     * @param closeHandler handles closing of the multiview component, useful when any of the embedded elements can be in modified state and closing would cause a dataloss..
     */
    public static TopComponent createMultiView (MultiViewDescription[] descriptions, MultiViewDescription defaultDesc,
                                                CloseOperationHandler closeHandler) {
        if (descriptions == null) return null;
        if (closeHandler == null) closeHandler = createDefaultCloseOpHandler();
        MultiViewTopComponent tc = new MultiViewTopComponent();
        tc.setMultiViewDescriptions(descriptions, defaultDesc);
        tc.setCloseOperationHandler(closeHandler);
        return tc;
    }
    
   /** Creates and returns new instance of cloneable top component with
     * multi views.
     * <p>
     * Please see {@link MultiViews#createCloneableMultiView} for loosely coupled variant
     * of this method which may be more suitable for modular environment.
     * 
     * 
     * @param descriptions  array of descriptions of tabs in the multiview.
     * @param defaultDesc  the initial selection, one of the descriptions array values.
    */
    public static CloneableTopComponent createCloneableMultiView (MultiViewDescription[] descriptions, MultiViewDescription defaultDesc) {
        return createCloneableMultiView(descriptions, defaultDesc, createDefaultCloseOpHandler());
    }

    /** Creates and returns new instance of cloneable top component with
     * multi views.
     * <p>
     * Please see {@link MultiViews#createCloneableMultiView} for loosely coupled variant
     * of this method which may be more suitable for modular environment.
     * 
     * @param descriptions  array of descriptions of tabs in the multiview.
     * @param defaultDesc  the initial selection, one of the descriptions array values.
     * @param closeHandler handles closing of the multiview component, useful when any of the embedded elements can be in modified state and closing would cause a dataloss..
     */
    public static CloneableTopComponent createCloneableMultiView (MultiViewDescription[] descriptions, MultiViewDescription defaultDesc,
                                                CloseOperationHandler closeHandler) {
        if (descriptions == null) return null;
        if (closeHandler == null) closeHandler = createDefaultCloseOpHandler();
        MultiViewCloneableTopComponent tc = new MultiViewCloneableTopComponent();
        tc.setMultiViewDescriptions(descriptions, defaultDesc);
        tc.setCloseOperationHandler(closeHandler);
        return tc;
    }    
    
    /**
     * Utility method for MultiViewElements to create a CloseOperationState instance that
     * informs the environment that the MVElement is ok to be closed.
     */
    
    static CloseOperationState createSafeCloseState() {
        return new CloseOperationState(true, "ID_CLOSE_OK", NOOP_CLOSE_ACTION, NOOP_CLOSE_ACTION);
    }

    /**
     * Utility method for MultiViewElements to create a CloseOperationState instance 
     * that warns about possible data loss. Corrective actions can be defined.
     * <p>
     * There is a default implementation of {@link CloseOperationHandler}. It 
     * uses <code>warningId</code> of all elements with unsafe close state to
     * select the unique ones. These unique elements are then presented in a 
     * question box and user can decide to save or discard them. The user
     * friendly message in such dialog is taken from 
     * <code>proceedAction.getValue(Action.LONG_DESCRIPTION)</code>, if present.
     * 
     * @param warningId an id that identifies the problem, 
     *     the CloseOperationHandler used in the component should know about the warning's meaning and handle appropriately
     * @param proceedAction will be performed when the CloseOperationHandler decides that closing the component is ok and changes are to be saved.
     * @param discardAction will be performed when the CloseOperationHandler decides that the nonsaved data shall be discarded
     */
    
    public static CloseOperationState createUnsafeCloseState(String warningId, Action proceedAction, Action discardAction) {
        return new CloseOperationState(false, 
                        (warningId == null ? "" : warningId),
                        (proceedAction == null ? NOOP_CLOSE_ACTION : proceedAction),
                        (discardAction == null ? NOOP_CLOSE_ACTION : discardAction));
    }
    
    static CloseOperationHandler createDefaultCloseOpHandler() {
        return new DefaultCloseHandler();
    }
    
    static MultiViewDescription createMultiViewDescription(Map map) {
        return new MapMVD(map, null, false);
    }
    
    private static final class Blank implements MultiViewElement, Serializable {
        
        private JPanel panel;
        private JPanel bar;
        
        Blank() {
            panel = new JPanel();
            bar = new JPanel();
        }
        
        public void componentActivated() {
        }
        
        public void componentClosed() {
        }
        
        public void componentDeactivated() {
        }
        
        public void componentHidden() {
        }
        
        public void componentOpened() {
        }
        
        public void componentShowing() {
        }
        
        public Action[] getActions() {
            return new Action[0];
        }
        
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
        
        public JComponent getToolbarRepresentation() {
            return bar;
        }
        
        public javax.swing.JComponent getVisualRepresentation() {
            return panel;
        }
        
        public void setMultiViewCallback(MultiViewElementCallback callback) {
        }
        
        
        public org.openide.awt.UndoRedo getUndoRedo() {
            return null;
        }
        
        public CloseOperationState canCloseElement() {
            return CloseOperationState.STATE_OK;
        }
        
    }

/**
 * default simple implementation of the close handler.
 */    
    static final class DefaultCloseHandler implements CloseOperationHandler, Serializable {
         private static final long serialVersionUID =-3126744916624172427L;

         private boolean checkCanCloseAgain = false;
       
        @Override
        @Messages({
            "CTL_Save=Save",
            "CTL_Discard=&Discard"
        })
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            checkCanCloseAgain = false;
            Iterator<CloseOperationState> it;
            if (elements != null) {
                boolean canBeClosed = true;
                Map<String,CloseOperationState> badOnes = new LinkedHashMap<String, CloseOperationState>();
                for (int i = 0; i < elements.length; i++) {
                    if (!elements[i].canClose()) {
                        badOnes.put(elements[i].getCloseWarningID(), elements[i]);
                        canBeClosed = false;
                    }
                }
                if (!canBeClosed) {
                    NotifyDescriptor desc = new NotifyDescriptor.Confirmation(
                        createPanel(badOnes), NotifyDescriptor.YES_NO_CANCEL_OPTION
                    );
                    Object[] choose = { Bundle.CTL_Save(), Bundle.CTL_Discard(), NotifyDescriptor.CANCEL_OPTION };
                    desc.setOptions(choose);
                    Object retVal = DialogDisplayer.getDefault().notify(desc);
                    if (retVal == choose[0]) {
                        // do proceed.
                        it = badOnes.values().iterator();
                        while (it.hasNext()) {
                            Action act = it.next().getProceedAction();
                            if (act != null) {
                                act.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "proceed"));
                            }
                        }
                        //#236369 - check if everything saved ok later on
                        checkCanCloseAgain = true;
                    } else if (retVal == choose[1]) {
                        // do discard
                        it = badOnes.values().iterator();
                        while (it.hasNext()) {
                            Action act = it.next().getDiscardAction();
                            if (act != null) {
                                act.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "discard"));
                            }
                        }
                    } else {
                        // was cancel..
                        return false;
                    }
                }
            }
            return true;
        }

        boolean shouldCheckCanCloseAgain() {
            return checkCanCloseAgain;
        }
        
        private Object createPanel(Map<String,CloseOperationState> elems) {
            if (elems.size() == 1) {
                return findDescription(elems.values().iterator().next());
            }
            
            StringBuilder sb = new StringBuilder();
            Iterator it = elems.values().iterator();
            while (it.hasNext()) {
                CloseOperationState state = (CloseOperationState)it.next();
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(findDescription(state));
            }
            return sb;
        }

        private Object findDescription(final CloseOperationState e) {
            final Action a = e.getProceedAction();
            Object msg = a.getValue(Action.LONG_DESCRIPTION);
            if (msg == null) {
                msg = a.getValue(Action.SHORT_DESCRIPTION);
            }
            if (msg == null) {
                msg = a.getValue(Action.NAME);
            }
            if (msg == null) {
                msg = e.getCloseWarningID();
            }
            return msg;
        }
    }
    
    /**
     * just a default noon action to put into the closeoperation state.
     */
    private static final class NoopAction extends AbstractAction {
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
            // do nothing
        }
        
    }

    /** default MultiViewDescription */
    private static final class MapMVD implements
    MultiViewDescription, ContextAwareDescription , SourceCheckDescription {
        private final Map map;
        private final Lookup context;
	private boolean isSplitDescription;
        public MapMVD(Map map, Lookup context, boolean isSplitDescription) {
            this.map = map;
            this.context = context;
	    this.isSplitDescription = isSplitDescription;
        }
        
        private <T> T get(String attr, Class<T> type) {
            Object obj = map.get(attr); // NOI18N
            if (obj == null) {
                throw new NullPointerException(attr + " attribute not specified for " + map.get("class"));
            }
            if (type.isInstance(obj)) {
                return type.cast(obj);
            }
            throw new IllegalArgumentException(attr + " not of type " + type + " but " + obj + " for " + map.get("class"));
        }
        

        @Override
        public int getPersistenceType() {
            if( !map.containsKey("persistenceType") )
                return TopComponent.PERSISTENCE_NEVER; //#212993
            return get("persistenceType", Integer.class);
        }

        @Override
        public String getDisplayName() {
            return get("displayName", String.class);
        }

        @Override
        public Image getIcon() {
            if (!map.containsKey("iconBase")) {
                return null; // #206525
            }
            String base = get("iconBase", String.class); // NOI18N
            return ImageUtilities.loadImage(base, true);
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        public String preferredID() {
            return get("preferredID", String.class); // NOI18N
        }

        @Override
        public MultiViewElement createElement() {
            String name = get("class", String.class); // NOI18N
            String method = (String)map.get("method"); // NOI18N
            Exception first = null;
            try {
                ClassLoader cl = Lookup.getDefault().lookup(ClassLoader.class);
                if (cl == null) {
                    cl = Thread.currentThread().getContextClassLoader();
                }
                if (cl == null) {
                    cl = MultiViewFactory.class.getClassLoader();
                }
                Class<?> clazz = Class.forName(name, true, cl);
                if (method == null) {
                    try {
                        Constructor<?> lookupC = clazz.getConstructor(Lookup.class);
                        return (MultiViewElement)lookupC.newInstance(context);
                    } catch (Exception ex) {
                        first = ex;
                        Constructor<?> defC = clazz.getConstructor();
                        return (MultiViewElement)defC.newInstance();
                    }
                } else {
                    try {
                        Method m = clazz.getMethod(method, Lookup.class);
                        return (MultiViewElement) m.invoke(null, context);
                    } catch (NoSuchMethodException ex) {
                        first = ex;
                        Method m = clazz.getMethod(method);
                        return (MultiViewElement) m.invoke(null);
                    }
                }
            } catch (Exception ex) {
                IllegalStateException ise = new IllegalStateException("Cannot instantiate " + name, ex);
                Throwable t = ise;
                while (t.getCause() != null) {
                    t = t.getCause();
                }
                t.initCause(first);
                throw ise;
            }
        }

        @Override
	public ContextAwareDescription createContextAwareDescription(Lookup context, boolean isSplitDescription) {
	    return new MapMVD(map, context, isSplitDescription);
	}

        @Override
        public boolean isSourceView() {
            return Boolean.TRUE.equals(map.get("sourceview")); // NOI18N
        }

	@Override
	public boolean isSplitDescription() {
	    return isSplitDescription;
	}
    }
}
