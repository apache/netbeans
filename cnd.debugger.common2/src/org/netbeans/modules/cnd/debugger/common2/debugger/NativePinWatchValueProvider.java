/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.debugger.common2.debugger;

import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.cnd.debugger.common2.values.VariableValue;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.AbstractExpandToolTipAction;
import org.netbeans.spi.debugger.ui.PinWatchUISupport;

/**
 *
 */

public class NativePinWatchValueProvider implements PinWatchUISupport.ValueProvider, PinWatchUISupport.ValueProvider.ValueChangeListener {
    public static final String ID = "NativePinWatchValueProvider";    // NOI18N
    
    private final Map<Watch, ValueChangeListener> valueListeners = new HashMap();
    private final NativeDebugger debugger;

    public NativePinWatchValueProvider(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, NativeDebugger.class);
        NativeDebuggerManager.get().registerPinnedWatchesUpdater(debugger, this);
    }
    
    protected final NativeDebugger getDebugger() {
        return debugger;
    }
    
    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getValue(Watch watch) {
        for (WatchVariable watchVar : debugger.getWatches()) {
            //if (watchVar.getNativeWatch().watch().getExpression().equals(watch.getExpression())) {
            //see bz#270230, the watches should be equal (otherwise update will not come anyway)
            if (watchVar.getNativeWatch().watch().equals(watch)) {
                Variable v = ((Variable) watchVar);
                VariableValue value = new VariableValue(v.getAsText(), v.getDelta());
                return value.toString();
            }
        }
        
        return "";    // NOI18N
    }

    @Override
    public String getEditableValue(Watch watch) {
        return null;
    }
    
    private boolean isExpandable(Watch watch) {
        for (WatchVariable watchVar : debugger.getWatches()) {
            if (watchVar.getNativeWatch().watch().getExpression().equals(watch.getExpression())) {
                Variable v = ((Variable) watchVar);                
                return !v.isLeaf;
            }
        }
        return false;
    }

    @Override
    public Action[] getHeadActions(Watch watch) {
        //return head action IF it is expandabe
        if (!isExpandable(watch)) {
            return new Action[0];
        }
        return new Action[]{new ExpandAction(debugger, watch)};
    }
    
    @Override
    public void setChangeListener(Watch watch, ValueChangeListener chl) {
        synchronized (valueListeners) {
            valueListeners.put(watch, chl);
        }
    }

    @Override
    public void unsetChangeListener(Watch watch) {
        synchronized (valueListeners) {
            valueListeners.remove(watch);
        }
    }
    
    @Override
    public void valueChanged(Watch watch) {
        ValueChangeListener listener = valueListeners.get(watch);
        if (listener != null) {
            listener.valueChanged(watch);
        }
    }
    
    protected static class ExpandAction extends AbstractExpandToolTipAction {

        private final NativeDebugger debugger;
        private final Watch watch;

        public ExpandAction(NativeDebugger debugger, Watch watch) {
            this.debugger = debugger;
            this.watch = watch;
        }

        @Override
        protected void openTooltipView() {
            debugger.evaluateInOutline(watch.getExpression());
        }
    }

}
