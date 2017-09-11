/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual.breakpoints;

import com.sun.jdi.ObjectReference;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
 *
 * @author martin
 */
public class FXComponentBreakpointImpl extends BaseComponentBreakpointImpl {
    public FXComponentBreakpointImpl(ComponentBreakpoint cb, JPDADebugger debugger) {
        super(cb, debugger);
    }

    @Override
    protected void initServiceBreakpoints() {
        //MethodBreakpoint mb = MethodBreakpoint.create("", "");
        ObjectReference component = cb.getComponent().getComponent(debugger);
        Variable variableComponent = ((JPDADebuggerImpl) debugger).getVariable(component);
        //mb.setInstanceFilters(debugger, new ObjectVariable[] { (ObjectVariable) variableComponent });
        
        int type = cb.getType();
        if (((type & AWTComponentBreakpoint.TYPE_ADD) != 0) || ((type & AWTComponentBreakpoint.TYPE_REMOVE) != 0)) {
            MethodBreakpoint mb = MethodBreakpoint.create("javafx.scene.Node", "setParent");
            mb.setMethodSignature("(Ljavafx/scene/Parent)V");
            if (!cb.isEnabled()) {
                mb.disable();
            }
            addMethodBreakpoint(mb, (ObjectVariable)variableComponent);
        }
        if (((type & AWTComponentBreakpoint.TYPE_SHOW) != 0) || ((type & AWTComponentBreakpoint.TYPE_HIDE) != 0)) {
            MethodBreakpoint mb = MethodBreakpoint.create("javafx.scene.Node", "setVisible");
            mb.setMethodSignature("(Z)V");
            if (!cb.isEnabled()) {
                mb.disable();
            }
            addMethodBreakpoint(mb, (ObjectVariable) variableComponent);
        }
//        if (((type & AWTComponentBreakpoint.TYPE_REPAINT) != 0)) {
//            MethodBreakpoint mbShow = MethodBreakpoint.create("java.awt.Component", "repaint");
//            // void repaint(long tm, int x, int y, int width, int height)
//            mbShow.setMethodSignature("(JIIII)V");
//            mbShow.setHidden(true);
//            mbShow.setInstanceFilters(debugger, new ObjectVariable[] { (ObjectVariable) variableComponent });
//            mbShow.addJPDABreakpointListener(new JPDABreakpointListener() {
//                @Override
//                public void breakpointReached(JPDABreakpointEvent event) {
//
//                }
//            });
//            DebuggerManager.getDebuggerManager().addBreakpoint(mbShow);
//            serviceBreakpoints.add(mbShow);
//        }
    }
}
