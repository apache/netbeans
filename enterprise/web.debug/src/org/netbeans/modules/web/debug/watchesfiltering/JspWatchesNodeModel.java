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

package org.netbeans.modules.web.debug.watchesfiltering;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.openide.util.RequestProcessor;

/**
 * Node model for JSP EL watches.
 *
 * @author Maros Sandor
 */
public class JspWatchesNodeModel implements NodeModel {

    private static final String ICON_BASE ="org/netbeans/modules/debugger/resources/watchesView/Watch";

    private final Collection<ModelListener> modelListeners = new HashSet<ModelListener>();
    private final Map<JspElWatch, String> shortDescriptionMap = new HashMap<JspElWatch, String>();
    private RequestProcessor evaluationRP;

    public JspWatchesNodeModel(ContextProvider lookupProvider) {
        evaluationRP = lookupProvider.lookupFirst(null, RequestProcessor.class);
    }

    public String getDisplayName(Object node) throws UnknownTypeException {
        if (!(node instanceof JspElWatch)) throw new UnknownTypeException(node);
        JspElWatch watch = (JspElWatch) node;
        return watch.getExpression();
    }

    public String getIconBase(Object node) throws UnknownTypeException {
        if (!(node instanceof JspElWatch)) throw new UnknownTypeException(node);
        return ICON_BASE;
    }

    public String getShortDescription(Object node) throws UnknownTypeException {
        if (!(node instanceof JspElWatch)) throw new UnknownTypeException(node);
        final JspElWatch watch = (JspElWatch) node;
        
        synchronized (shortDescriptionMap) {
            String shortDescription = shortDescriptionMap.remove(watch);
            if (shortDescription != null) {
                return shortDescription;
            }
        }
        // Called from AWT - we need to postpone the work...
        evaluationRP.post(new Runnable() {
            public void run() {
                String shortDescription = getShortDescriptionSynch(watch);
                if (shortDescription != null && !"".equals(shortDescription)) {
                    synchronized (shortDescriptionMap) {
                        shortDescriptionMap.put(watch, shortDescription);
                    }
                    fireModelChange(new ModelEvent.NodeChanged(JspWatchesNodeModel.this,
                        watch, ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK));
                }
            }
        });
        return "";
    }

    private static String getShortDescriptionSynch(JspElWatch watch) {
        String t = watch.getType ();
        String e = watch.getExceptionDescription ();
        if (e != null) {
            return watch.getExpression() + " = >" + e + "<";
        }
        if (t == null) {
            return watch.getExpression() + " = " + watch.getValue();
        } else {
            try {
                return watch.getExpression() + " = (" + watch.getType () + ") " + watch.getToStringValue();
            } catch (InvalidExpressionException ex) {
                return ex.getLocalizedMessage ();
            }
        }
    }

    public void addModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.add(l);
        }
    }

    public void removeModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.remove(l);
        }
    }

    protected void fireModelChange(ModelEvent me) {
        ModelListener[] listeners;
        synchronized (modelListeners) {
            listeners = modelListeners.toArray(new ModelListener[]{});
        }
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].modelChanged(me);
        }
    }

}
