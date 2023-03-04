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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;

import java.io.StringReader;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;


/**
 * @author   Jan Jancura
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/WatchesView", types=ExtendedNodeModelFilter.class, position=300)
public class WatchesNodeModelFilter extends VariablesNodeModel implements ExtendedNodeModelFilter {

    public static final String WATCH =
        "org/netbeans/modules/debugger/resources/watchesView/watch_16.png";


    public WatchesNodeModelFilter (ContextProvider lookupProvider) {
        super (lookupProvider);
    }
    
    public static boolean isEmptyWatch(Object node) {
        return "EmptyWatch".equals(node.getClass().getSimpleName());
    }
    
    public String getDisplayName (NodeModel model, Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return NbBundle.getBundle (WatchesNodeModelFilter.class).
                getString ("CTL_WatchesModel_Column_Name_Name");
        if (o instanceof JPDAWatch) {
            if (isEmptyWatch(o)) {
                return "<_html><font color=\"#808080\">&lt;" + // [TODO] <_html> tag used as workaround, see TreeModelNode.setName()
                    NbBundle.getBundle (WatchesNodeModelFilter.class).getString("CTL_WatchesModel_Empty_Watch_Hint") +
                    "&gt;</font></html>";
            }
            return ((JPDAWatch) o).getExpression ();
        }
        try {
            return model.getDisplayName(o);
        } catch (UnknownTypeException utex) {
            return super.getDisplayName (o);
        }
    }

    public String getShortDescription(NodeModel original, Object node) throws UnknownTypeException {
        return getShortDescription(node);
    }

    protected String getShortDescriptionSynch (Object o) {
        if (o instanceof JPDAWatch) {
            if (isEmptyWatch(o)) {
                return NbBundle.getMessage(WatchesNodeModelFilter.class, "TTP_NewWatch");
            }
            JPDAWatch w = (JPDAWatch) o;
            boolean evaluated;
            evaluated = VariablesTreeModelFilter.isEvaluated(o);
            if (!evaluated) {
                return w.getExpression ();
            }
            String e = w.getExceptionDescription ();
            if (e != null)
                return w.getExpression () + " = >" + e + "<";
            String t = w.getType ();
            if (t == null)
                return w.getExpression () + " = " + w.getValue ();
            else
                try {
                    return w.getExpression () + " = (" + w.getType () + ") " + 
                        w.getToStringValue ();
                } catch (InvalidExpressionException ex) {
                    return ex.getLocalizedMessage ();
                }
        }
        return super.getShortDescriptionSynch(o);
    }
    
    @Override
    protected void testKnown(Object o) throws UnknownTypeException {
        if (o instanceof JPDAWatch) return ;
        super.testKnown(o);
    }
    
    public boolean canRename(ExtendedNodeModel model, Object node) throws UnknownTypeException {
        return model.canRename(node) || isEmptyWatch(node);
    }

    public boolean canCopy(ExtendedNodeModel model, Object node) throws UnknownTypeException {
        return model.canCopy(node) && !isEmptyWatch(node);
    }

    public boolean canCut(ExtendedNodeModel model, Object node) throws UnknownTypeException {
        return model.canCut(node) && !isEmptyWatch(node);
    }

    public Transferable clipboardCopy(ExtendedNodeModel model, Object node) throws IOException,
                                                          UnknownTypeException {
        return model.clipboardCopy(node);
    }

    public Transferable clipboardCut(ExtendedNodeModel model, Object node) throws IOException,
                                                         UnknownTypeException {
        return model.clipboardCut(node);
    }

    public PasteType[] getPasteTypes(ExtendedNodeModel model, final Object node, final Transferable t) throws UnknownTypeException {
        return model.getPasteTypes(node, t);
    }

    public void setName(ExtendedNodeModel model, Object node, String name) throws UnknownTypeException {
        ((JPDAWatch) node).setExpression(name);
    }

    public String getIconBase(NodeModel original, Object node) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported.");
    }

    public String getIconBaseWithExtension(ExtendedNodeModel model, Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) {
            return WATCH;
        }
        if (node instanceof JPDAWatch) {
            if (isEmptyWatch(node)) {
                return null;
            }
            return WATCH;
        }
        try {
            return model.getIconBaseWithExtension(node);
        } catch (UnknownTypeException utex) {
            return super.getIconBaseWithExtension(node);
        }
    }

}
