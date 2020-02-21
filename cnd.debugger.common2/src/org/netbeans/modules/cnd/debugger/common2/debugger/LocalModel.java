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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Action;
import javax.swing.JToggleButton;

import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.actions.SystemAction;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.cnd.debugger.common2.debugger.actions.MaxObjectAction;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineCapability;
import org.netbeans.modules.cnd.debugger.common2.debugger.api.EngineDescriptor;
import org.netbeans.spi.viewmodel.*;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 * Registered in
 *	META-INF/debugger/netbeans-DbxDebuggerEngine/LocalsView/
 *	org.netbeans.spi.viewmodel.TreeModel
 *	org.netbeans.spi.viewmodel.NodeModel
 *	org.netbeans.spi.viewmodel.TreeExpansionModel
 *	org.netbeans.spi.viewmodel.NodeActionsProvider
 */

public final class LocalModel extends VariableModel
    implements NodeActionsProvider {
    
    private static final WarningMessage NO_CODEMODEL_WARNING = new WarningMessage("CTL_WatchesModel_Warning_Watch_Hint");   // NOI18N
    private Preferences preferences = NbPreferences.forModule(VariablesViewButtons.class).node(VariablesViewButtons.PREFERENCES_NAME);
    private VariablesPreferenceChangeListener prefListener = new VariablesPreferenceChangeListener();

    public LocalModel(ContextProvider ctx) {
	super(ctx);
        preferences.addPreferenceChangeListener(prefListener);

	VariablesViewButtons.createShowAutosButton().addActionListener(new ActionListener() {
            @Override
	    public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JToggleButton) {
		    JToggleButton b = (JToggleButton) e.getSource();
		    debugger.setShowAutos(b.isSelected());
		}
	    }
	});
    }

    // interface VariableModel
    @Override
    protected boolean isLocal() {
	return true;
    }

    // interface TreeModel
    @Override
    public Object[] getChildren(Object parent, int from, int to) 
			throws UnknownTypeException {
	Object[] children;

	if (parent == ROOT) {
            if (VariablesViewButtons.isShowAutos()) {
                children = debugger.getAutos();
                if(children != null && children.length > 0 && children[0] == null){
                    Object[] newChildren = {NO_CODEMODEL_WARNING};
                    children = newChildren;
                }
            } else {
                children = debugger.getLocals();
            }
	} else if (parent instanceof Variable) {
	    Variable v = (Variable) parent;
	    children = v.getChildren();
            
            if (v.hasMore()) {
                Object[] newChildren = new Object[children.length+1];
                System.arraycopy(children, 0, newChildren, 0, children.length);
                newChildren[newChildren.length-1] = new ShowMoreMessage(v);
                children = newChildren;
            }
	} else {
	    throw new UnknownTypeException (parent);
	}

	return children;
    }

    // interface TreeModel
    @Override
    public int getChildrenCount(Object parent) 
			throws UnknownTypeException {
	int count;
	if (parent == ROOT) {
            if (VariablesViewButtons.isShowAutos()) {
		count = debugger.getAutosCount();
            } else {
		count = debugger.getLocalsCount();
            }
	} else if (parent instanceof Variable) {
	    Variable v = (Variable) parent;
	    count = v.getNumChild();
	} else {
	    throw new UnknownTypeException (parent);
	}
	return count;
    }

    @Override
    public String getDisplayName(NodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof WarningMessage) {
            return  ((WarningMessage) node).getMessage();
        } else {
            return super.getDisplayName(original, node);
        }
    }

    @Override
    public String getIconBaseWithExtension(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof WarningMessage) {
            return null;
        } else{
            return super.getIconBaseWithExtension(original, node);
        }
    }

    @Override
    public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
        if (node instanceof WarningMessage){
            return "";
        } else {
            return super.getValueAt(node, columnID);
        }
    }
    
    

    // interface TreeModel etc
    @Override
    public void addModelListener(ModelListener l) {
	if (super.addModelListenerHelp(l)) {
	    debugger.setShowAutos(VariablesViewButtons.isShowAutos());
	    debugger.registerLocalModel(this);
	}
    }

    // interface TreeModel etc
    @Override
    public void removeModelListener(ModelListener l) {
	if (super.removeModelListenerHelp(l)) {
	    debugger.setShowAutos(false);
	    debugger.registerLocalModel(null);
	}
    }


    // interface NodeActionsProvider
    @Override
    public Action[] getActions (Object node) throws UnknownTypeException {
	EngineDescriptor desp = debugger.getNDI().getEngineDescriptor();
	boolean canDoMaxObject = desp.hasCapability(EngineCapability.MAX_OBJECT);
	boolean canDoDy = desp.hasCapability(EngineCapability.DYNAMIC_TYPE);
	boolean canDoIn = desp.hasCapability(EngineCapability.INHERITED_MEMBERS);
	boolean canDoSt = desp.hasCapability(EngineCapability.STATIC_MEMBERS);
	boolean canDoPP = desp.hasCapability(EngineCapability.PRETTY_PRINT);

	if (node == ROOT) {
	    return new Action[] {
		WatchModel.NEW_WATCH_ACTION,
                WatchModel.SHOW_PINNED_WATCHES_ACTION,                
		new WatchModel.DeleteAllAction(),
		null,
		canDoIn ? Action_INHERITED_MEMBERS : null,
		canDoDy ? Action_DYNAMIC_TYPE : null,
		canDoSt ? Action_STATIC_MEMBERS : null,
		canDoPP ? Action_PRETTY_PRINT : null,
		null,
		canDoMaxObject ? SystemAction.get(MaxObjectAction.class) : null,
		null,
	    };

	} else if (node instanceof Variable) {
	    Variable v = (Variable) node;
	    return v.getActions(false);

	} else {
	    throw new UnknownTypeException(node);
	}
    }

    // interface NodeActionsProvider
    @Override
    public void performDefaultAction (Object node) throws UnknownTypeException {
	// This gets called redundantly, see issue 48891.
	if (node == ROOT) {
	    return;
	} else if (node instanceof Variable) {
	    Variable v = (Variable) node;
	} else if (node instanceof ShowMoreMessage) {
            ((ShowMoreMessage) node).getMore();
        } else {
	    throw new UnknownTypeException(node);
	}
    }

    private class VariablesPreferenceChangeListener implements PreferenceChangeListener {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt.getKey();
            if (VariablesViewButtons.SHOW_AUTOS.equals(key)) {
                refresh();
            }
        }

        private void refresh() {
            try {
                LocalModel.this.treeChanged();
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                Exceptions.printStackTrace(t);
            }
        }

    }
    
        
    /**
     * An item displayed when Autos list can not be evaluated
     * because Code Assistance is switched off or unavailable.
     */
    private static class WarningMessage {
        private String key;

        WarningMessage(String keyStr) {
            key = keyStr;
        }
        
        public String getMessage() {
            return Catalog.get(key);
        }
    }
}
