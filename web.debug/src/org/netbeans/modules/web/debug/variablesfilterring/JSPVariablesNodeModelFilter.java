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

package org.netbeans.modules.web.debug.variablesfilterring;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.web.debug.variablesfilterring.JSPVariablesFilter.AttributeMap;
import org.netbeans.modules.web.debug.variablesfilterring.JSPVariablesFilter.AttributeMap.UnknownOwnerNameException;
import org.netbeans.modules.web.debug.variablesfilterring.JSPVariablesFilter.ImplicitLocals;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Libor Kotouc
 */
public class JSPVariablesNodeModelFilter implements ExtendedNodeModelFilter {
    
    private static final int TO_STRING_LENGTH_LIMIT = 10000;
    
    private RequestProcessor evaluationRP;
    private final Map<ObjectVariable, String> shortDescriptionMap = new IdentityHashMap<ObjectVariable, String>();
    private final Collection modelListeners = new HashSet();

    /** Creates a new instance of JSPVariablesNodeModelFilter */
    public JSPVariablesNodeModelFilter(ContextProvider lookupProvider) {
        evaluationRP = lookupProvider.lookupFirst(null, RequestProcessor.class);
    }

    /**
     * Returns filterred display name for given node. You should not 
     * throw UnknownTypeException directly from this method!
     *
     * @throws  ComputingException if the display name resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getDisplayName (...)</code> method call only!
     * @return  display name for given node
     */
    public String getDisplayName (NodeModel original, Object node) 
    throws UnknownTypeException 
    {
        
        String dn = "";
        if (node instanceof ImplicitLocals)
            dn =  NbBundle.getMessage(JSPVariablesFilter.class, "LBL_IMPLICIT_LOCALS");
        else if (node instanceof AttributeMap) {
            String resIcon = "";
            String ownerName = ((AttributeMap)node).getOwnerName();
            if (ownerName.equals("request"))
                resIcon = "LBL_REQUEST_ATTRIBUTES";
            else if (ownerName.equals("session"))
                resIcon = "LBL_SESSION_ATTRIBUTES";
            else if (ownerName.equals("application"))
                resIcon = "LBL_APPLICATION_ATTRIBUTES";
            
            dn = NbBundle.getMessage(JSPVariablesFilter.class, resIcon);
        }
        else if (node instanceof AttributeMap.Attribute)
            dn = ((AttributeMap.Attribute)node).getName();
        else
            dn = original.getDisplayName(node);
        
        return dn;
    }
    
    /**
     * Returns filterred icon for given node. You should not throw 
     * UnknownTypeException directly from this method!
     *
     * @throws  ComputingException if the icon resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getIconBase (...)</code> method call only!
     * @return  icon for given node
     */
    public String getIconBase (NodeModel original, Object node)
    throws UnknownTypeException 
    {
        throw new IllegalStateException(
                "getIconBaseWithExtension should be always called instead");
    }
    
    public String getIconBaseWithExtension(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        String ib = "";
        if (node instanceof ImplicitLocals)
            ib = NbBundle.getMessage(JSPVariablesFilter.class, "RES_IMPLICIT_LOCALS_GROUP");
        else if (node instanceof AttributeMap)
            ib = NbBundle.getMessage(JSPVariablesFilter.class, "RES_ATTRIBUTES_GROUP");
        else if (node instanceof AttributeMap.Attribute)
            ib = NbBundle.getMessage(JSPVariablesFilter.class, "RES_ATTRIBUTE_VALUE");
        else
            ib = original.getIconBaseWithExtension(node);
                
        return ib;
    }
    
    /**
     * Returns filterred tooltip for given node. You should not throw 
     * UnknownTypeException directly from this method!
     *
     * @throws  ComputingException if the tooltip resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException this exception can be thrown from 
     *          <code>original.getShortDescription (...)</code> method call only!
     * @return  tooltip for given node
     */
    public String getShortDescription (NodeModel original, Object node)
    throws UnknownTypeException 
    {
        String sd = "";
        if (node instanceof ImplicitLocals)
            sd = NbBundle.getMessage(JSPVariablesFilter.class, "TLT_IMPLICIT_LOCALS");
        else if (node instanceof AttributeMap) {
            String tltAttributes;
            String ownerName = ((AttributeMap)node).getOwnerName();
            if (ownerName.equals("request"))
                tltAttributes = "TLT_REQUEST_ATTRIBUTES";
            else if (ownerName.equals("session"))
                tltAttributes = "TLT_SESSION_ATTRIBUTES";
            else if (ownerName.equals("application"))
                tltAttributes = "TLT_APPLICATION_ATTRIBUTES";
            else throw new UnknownOwnerNameException(ownerName);
            
            sd = NbBundle.getMessage(JSPVariablesFilter.class, tltAttributes);
        }
        else if (node instanceof AttributeMap.Attribute) {
            Variable attributeValue = ((AttributeMap.Attribute)node).getValue();
            String type = attributeValue.getType ();
            String stringValue = attributeValue.getValue();
            if (attributeValue instanceof ObjectVariable) {
                final ObjectVariable ov = (ObjectVariable) attributeValue;

                synchronized (shortDescriptionMap) {
                    Object shortDescription = shortDescriptionMap.remove(ov);
                    if (shortDescription != null) {
                        return "(" + type + ") " + shortDescription;
                    }
                }
                // Called from AWT - we need to postpone the work...
                evaluationRP.post(new Runnable() {
                    public void run() {
                        String shortDescription = getShortDescriptionSynch(ov);
                        if (shortDescription != null && !"".equals(shortDescription)) {
                            synchronized (shortDescriptionMap) {
                                shortDescriptionMap.put(ov, shortDescription);
                            }
                            fireModelChange(new ModelEvent.NodeChanged(JSPVariablesNodeModelFilter.this,
                                ov, ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK));
                        }
                    }
                });
                return "";
                //stringValue = ov.getToStringValue();
            }
            sd = "(" + type + ") " + stringValue;
        }
        else
            sd = original.getShortDescription(node);
                
        return sd;
    }

    private static String getLimitedToString(ObjectVariable v) throws InvalidExpressionException {
        String toString = null;
        try {
            java.lang.reflect.Method toStringMethod =
                    v.getClass().getMethod("getToStringValue",  // NOI18N
                                           new Class[] { Integer.TYPE });
            toStringMethod.setAccessible(true);
            toString = (String) toStringMethod.invoke(v, TO_STRING_LENGTH_LIMIT);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        if (toString == null) {
            toString = v.getToStringValue();
        }
        return toString;
    }

    private String getShortDescriptionSynch (ObjectVariable v) {
        try {
            return getLimitedToString(v);
        } catch (InvalidExpressionException ex) {
            return ex.getLocalizedMessage();
        }
    }

    /**
     * 
     * Unregisters given listener.
     * 
     * @param l the listener to remove
     */
    public void removeModelListener(org.netbeans.spi.viewmodel.ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.remove(l);
        }
    }

    /**
     * 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addModelListener(org.netbeans.spi.viewmodel.ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.add(l);
        }
    }

    private void fireModelChange(ModelEvent me) {
        Object[] listeners;
        synchronized (modelListeners) {
            listeners = modelListeners.toArray();
        }
        for (int i = 0; i < listeners.length; i++) {
            ((ModelListener) listeners[i]).modelChanged(me);
        }
    }

    public boolean canRename(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCopy(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCut(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return false;
    }

    public Transferable clipboardCopy(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        throw new UnsupportedOperationException("not supported");
    }

    public Transferable clipboardCut(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        throw new UnsupportedOperationException("not supported");
    }

    public PasteType[] getPasteTypes(ExtendedNodeModel original, Object node, Transferable t) throws UnknownTypeException {
        return new PasteType[0];
    }

    public void setName(ExtendedNodeModel original, Object node, String name) throws UnknownTypeException {
    }

}
