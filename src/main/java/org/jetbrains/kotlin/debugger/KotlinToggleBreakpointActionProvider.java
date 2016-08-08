/**
 * *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************
 */
package org.jetbrains.kotlin.debugger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import kotlin.Pair;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Alexander.Baratynski
 */
@ActionsProvider.Registrations({
    @ActionsProvider.Registration(path="", actions={ "toggleBreakpoint" }, activateForMIMETypes={ "text/x-kt" }),
    @ActionsProvider.Registration(path="netbeans-JPDASession", actions={ "toggleBreakpoint" }, activateForMIMETypes={ "text/x-kt" })
})
public class KotlinToggleBreakpointActionProvider extends ActionsProviderSupport 
    implements PropertyChangeListener {

    private JPDADebugger debugger;
    
    public KotlinToggleBreakpointActionProvider() {
        KotlinEditorContextBridge.getContext().
                addPropertyChangeListener(KotlinToggleBreakpointActionProvider.this);
        enableAllActions();
    }
    
    public KotlinToggleBreakpointActionProvider(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, 
                KotlinToggleBreakpointActionProvider.this);
        enableAllActions();
        KotlinEditorContextBridge.getContext().
                addPropertyChangeListener(KotlinToggleBreakpointActionProvider.this);
    }
    
    private void destroy() {
        debugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, this);
        KotlinEditorContextBridge.getContext().removePropertyChangeListener(this);
    }
    
    @Override
    public void doAction(Object o) {
        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        JPDABreakpoint breakpoint;
        
        int lineNumber = KotlinEditorContextBridge.getContext().getCurrentLineNumber();
        String urlStr = KotlinEditorContextBridge.getContext().getCurrentURL();
        
        if ("".equals(urlStr.trim())) {
            return;
        }
        
        Pair<String, String> functionNameAndClassName = KotlinDebugUtils.getFunctionNameAndContainingClass(urlStr, lineNumber);
        if (functionNameAndClassName != null) {
            breakpoint = MethodBreakpoint.create(functionNameAndClassName.getFirst(), 
                    functionNameAndClassName.getSecond());
        } else {
            breakpoint = LineBreakpoint.create(urlStr, lineNumber);
            String className = KotlinDebugUtils.getClassFqName(urlStr, lineNumber);
            if (className == null) {
                className = "";
            }
            ((LineBreakpoint) breakpoint).setPreferredClassName(className);
        }
        KotlinDebugUtils.annotate(breakpoint, urlStr, lineNumber);
//        KotlinLineBreakpoint lineBreakpoint = findBreakpoint(url, lineNumber);
//        if (lineBreakpoint != null) {
//            manager.removeBreakpoint(lineBreakpoint);
//            return;
//        }
//        
//        lineBreakpoint = KotlinLineBreakpoint.create(url, lineNumber);
//        lineBreakpoint.setPrintText("breakpoint");
        manager.addBreakpoint(breakpoint);
    }

    @Override
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String url = KotlinEditorContextBridge.getContext().getCurrentURL();
        FileObject fo;
        try {
            fo = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException ex) {
            fo = null;
        }
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT,
                (KotlinEditorContextBridge.getContext().getCurrentLineNumber() >= 0) &&
                        (fo != null && "text/x-kt".equals(fo.getMIMEType())));
        if (debugger != null && debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
            destroy();
        }
    }
    
//    private static Breakpoint findBreakpointAtLine(String url, int line) {
//        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
//        for (Breakpoint breakpoint : breakpoints) {
//            JPDABreakpoint br = (JPDABreakpoint) breakpoint;
//            
//            if (!breakpoint.getURL().equals(url)) {
//                continue;
//            }
//            if (lineBreakpoint.getLineNumber() == lineNumber) {
//                return lineBreakpoint;
//            }
//        }
//        
//        return null;
//    }
    
    private static KotlinLineBreakpoint findBreakpoint(String url, int lineNumber) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (Breakpoint breakpoint : breakpoints) {
            if (!(breakpoint instanceof KotlinLineBreakpoint)) {
                continue;
            }
            KotlinLineBreakpoint lineBreakpoint = (KotlinLineBreakpoint) breakpoint;
            if (!lineBreakpoint.getURL().equals(url)) {
                continue;
            }
            if (lineBreakpoint.getLineNumber() == lineNumber) {
                return lineBreakpoint;
            }
        }
        
        return null;
    }
    
    private void enableAllActions() {
        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, true);
        setEnabled(ActionsManager.ACTION_CONTINUE, true);
        setEnabled(ActionsManager.ACTION_EVALUATE, true);
        setEnabled(ActionsManager.ACTION_FIX, true);
        setEnabled(ActionsManager.ACTION_KILL, true);
        setEnabled(ActionsManager.ACTION_MAKE_CALLEE_CURRENT, true);
        setEnabled(ActionsManager.ACTION_NEW_WATCH, true);
        setEnabled(ActionsManager.ACTION_MAKE_CALLER_CURRENT, true);
        setEnabled(ActionsManager.ACTION_PAUSE, true);
        setEnabled(ActionsManager.ACTION_POP_TOPMOST_CALL, true);
        setEnabled(ActionsManager.ACTION_RESTART, true);
        setEnabled(ActionsManager.ACTION_RUN_INTO_METHOD, true);
        setEnabled(ActionsManager.ACTION_RUN_TO_CURSOR, true);
        setEnabled(ActionsManager.ACTION_START, true);
        setEnabled(ActionsManager.ACTION_STEP_INTO, true);
        setEnabled(ActionsManager.ACTION_STEP_OPERATION, true);
        setEnabled(ActionsManager.ACTION_STEP_OUT, true);
        setEnabled(ActionsManager.ACTION_STEP_OVER, true);
    }
    
}