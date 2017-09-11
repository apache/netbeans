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

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.AbsentInformationException;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * @author   Jan Jancura
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/CallStackView", types=NodeModel.class)
public class CallStackNodeModel implements NodeModel {

    public static final String CALL_STACK =
        "org/netbeans/modules/debugger/resources/callStackView/NonCurrentFrame";
    public static final String CURRENT_CALL_STACK =
        "org/netbeans/modules/debugger/resources/callStackView/CurrentFrame";

    private JPDADebugger debugger;
    private Session session;
    private Vector listeners = new Vector ();
    private final RequestProcessor rp;
    private final Map<CallStackFrame, String> frameDescriptionsByFrame = new WeakHashMap<CallStackFrame, String>();
    private final Map<CallStackFrame, String> frameTooltipByFrame = new WeakHashMap<CallStackFrame, String>();
    
    
    public CallStackNodeModel (ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        session = lookupProvider.lookupFirst(null, Session.class);
        rp = lookupProvider.lookupFirst(null, RequestProcessor.class);
        new Listener (this, debugger);
    }
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getBundle (CallStackNodeModel.class).getString
                ("CTL_CallstackModel_Column_Name_Name");
        } else
        if (o instanceof CallStackFrame) {
            CallStackFrame sf = (CallStackFrame) o;
            boolean isCurrent;
            try {
                isCurrent = (Boolean) sf.getClass().getMethod("isCurrent").invoke(sf);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                isCurrent = false;
            }
            // Do not call JDI in AWT
            //CallStackFrame ccsf = debugger.getCurrentCallStackFrame ();
            String frameDescr;
            synchronized (frameDescriptionsByFrame) {
                frameDescr = frameDescriptionsByFrame.get(sf);
                if (frameDescr == null) {
                    loadFrameDescription(sf);
                    return BoldVariablesTableModelFilter.toHTML(
                            NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Frame_Loading"),
                            false,
                            false,
                            Color.LIGHT_GRAY);
                }
            }
            if (isCurrent) {
                return BoldVariablesTableModelFilter.toHTML (
                    frameDescr,
                    true,
                    false,
                    null
                );
            } else {
                return frameDescr;
            }
        } else if ("No current thread" == o) {
            return NbBundle.getMessage(CallStackNodeModel.class, "NoCurrentThread");
        } else if ("Thread is running" == o) {
            return NbBundle.getMessage(CallStackNodeModel.class, "ThreadIsRunning");
        } else
        throw new UnknownTypeException (o);
    }
    
    private void loadFrameDescription(final CallStackFrame sf) {
        rp.post(new Runnable() {
            @Override
            public void run() {
                String frameDescr = getCSFName(session, sf, false);
                synchronized (frameDescriptionsByFrame) {
                    frameDescriptionsByFrame.put(sf, frameDescr);
                }
                fireDisplayNameChanged(sf);
            }
        });
    }

    private void loadFrameTooltip(final CallStackFrame sf) {
        rp.post(new Runnable() {
            @Override
            public void run() {
                String frameDescr = getCSFName(session, sf, true);
                synchronized (frameTooltipByFrame) {
                    frameTooltipByFrame.put(sf, frameDescr);
                }
                fireTooltipChanged(sf);
            }
        });
    }

    private void fireDisplayNameChanged (Object node) {
        Vector v = (Vector) listeners.clone ();
        int k = v.size ();
        if (k == 0) return ;
        ModelEvent event = new ModelEvent.NodeChanged(this, node,
                ModelEvent.NodeChanged.DISPLAY_NAME_MASK);
        for (int i = 0; i < k; i++) {
            ((ModelListener) v.get (i)).modelChanged (event);
        }
    }

    private void fireTooltipChanged (Object node) {
        Vector v = (Vector) listeners.clone ();
        int k = v.size ();
        if (k == 0) return ;
        ModelEvent event = new ModelEvent.NodeChanged(this, node,
                ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK);
        for (int i = 0; i < k; i++) {
            ((ModelListener) v.get (i)).modelChanged (event);
        }
    }

    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getBundle (CallStackNodeModel.class).getString
                ("CTL_CallstackModel_Column_Name_Desc");
        } else
        if (o instanceof CallStackFrame) {
            CallStackFrame sf = (CallStackFrame) o;
            //return getCSFName (session, sf, true);
            // Do not call JDI in AWT
            String frameDescr;
            synchronized (frameTooltipByFrame) {
                frameDescr = frameTooltipByFrame.get(sf);
                if (frameDescr == null) {
                    loadFrameTooltip(sf);
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Frame_Loading");
                }
            }
            return frameDescr;
        } else if ("No current thread" == o) {
            return NbBundle.getMessage(CallStackNodeModel.class, "NoCurrentThread");
        } else if ("Thread is running" == o) {
            return NbBundle.getMessage(CallStackNodeModel.class, "ThreadIsRunning");
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object node) throws UnknownTypeException {
        if (node instanceof String) return null;
        if (node instanceof CallStackFrame) {
            CallStackFrame ccsf = debugger.getCurrentCallStackFrame ();
            if ( (ccsf != null) && 
                 (ccsf.equals (node)) 
            ) return CURRENT_CALL_STACK;
            return CALL_STACK;
        }
        throw new UnknownTypeException (node);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }
    
    private void fireNodeChanged(Object node) {
        Vector v = (Vector) listeners.clone ();
        int k = v.size ();
        if (k == 0) return ;
        ModelEvent.NodeChanged nodeChangedEvent = new ModelEvent.NodeChanged(this, node);
        for (int i = 0; i < k; i++) {
            ((ModelListener) v.get (i)).modelChanged (nodeChangedEvent);
        }
    }
    
    public static String getCSFName (
        Session s, 
        CallStackFrame sf,
        boolean l
    ) {
        String language = s.getCurrentLanguage();//sf.getDefaultStratum ();
        int ln = sf.getLineNumber (language);
        String fileName = null;
        boolean isJava = "Java".equals (language) || !sf.getAvailableStrata().contains(language);
        if (!isJava) {
            try {
                if (l) {
                    fileName = sf.getSourcePath(language);
                    if ("JS".equals(language) && (fileName.startsWith("jdk/nashorn/internal/scripts/") ||
                                                  fileName.startsWith("jdk\\nashorn\\internal\\scripts\\"))) {
                        fileName = sf.getSourceName(language);
                    }
                } else {
                    fileName = sf.getSourceName(language);
                }
                int dot = fileName.lastIndexOf('.');
                if (dot > 0) {
                    fileName = fileName.substring(0, dot + 1);
                }
                fileName += sf.getMethodName();
            } catch (AbsentInformationException e) {
                isJava = true;
            }
        }
        if (isJava) {
            fileName = l ? sf.getClassName () :
                           BreakpointsNodeModel.getShort (sf.getClassName ());
            fileName += "." + sf.getMethodName ();
        }
        if (ln < 0)
            return fileName;
        return fileName + ":" + ln;
    }

    public static String getCSFToolTipText(Session session, CallStackFrame stackFrame) {
        StringBuffer buf = new StringBuffer();
        buf.append("<html>"); // NOI18N
        String csfName = getCSFName(session, stackFrame, true);
        buf.append(NbBundle.getMessage(CallStackNodeModel.class, "CTL_CallStackFrame", csfName)); // NOI18N
        This thisVariable = stackFrame.getThisVariable();
        if (thisVariable != null && thisVariable.getClassType() != null) {
            String thisName = thisVariable.getClassType().getName();
            if (thisName != null && ! thisName.equals(stackFrame.getClassName())) {
                buf.append("<br>"); // NOI18N
                buf.append(NbBundle.getMessage(CallStackNodeModel.class, "CTL_RunType", thisName)); // NOI18N
            }
        }
        buf.append("</html>"); // NOI18N
        return buf.toString();
    }
    
    // innerclasses ............................................................
    
    /**
     * Listens on DebuggerManager on PROP_CURRENT_ENGINE, and on 
     * currentTreeModel.
     */
    private static class Listener implements PropertyChangeListener {
        
        private WeakReference ref;
        private JPDADebugger debugger;
        
        private Listener (
            CallStackNodeModel rm,
            JPDADebugger debugger
        ) {
            ref = new WeakReference (rm);
            this.debugger = debugger;
            debugger.addPropertyChangeListener (
                debugger.PROP_CURRENT_CALL_STACK_FRAME,
                this
            );
        }
        
        private CallStackNodeModel getModel () {
            CallStackNodeModel rm = (CallStackNodeModel) ref.get ();
            if (rm == null) {
                debugger.removePropertyChangeListener (
                    debugger.PROP_CURRENT_CALL_STACK_FRAME,
                    this
                );
            }
            return rm;
        }
        
        public void propertyChange (PropertyChangeEvent e) {
            CallStackNodeModel rm = getModel ();
            if (rm == null) return;
            rm.fireNodeChanged(e.getOldValue());
            rm.fireNodeChanged(e.getNewValue());
        }
    }
}
