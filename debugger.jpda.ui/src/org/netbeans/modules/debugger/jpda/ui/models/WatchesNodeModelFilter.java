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
