/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.jsui.frames.models;

import org.netbeans.modules.debugger.jpda.js.frames.JSStackFrame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.modules.debugger.jpda.js.JSUtils;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.DebuggingView;
import org.netbeans.spi.viewmodel.AsynchronousModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/JS/DebuggingView",
                             types={ TreeModelFilter.class })
public class DebuggingJSTreeModel implements TreeModelFilter {
    
    private static final String FILTER1 = "java.lang.invoke.LambdaForm";
    private static final String FILTER2 = "jdk.nashorn.";

    @Override
    public Object getRoot(TreeModel original) {
        return original.getRoot();
    }

    @Override
    public Object[] getChildren(TreeModel original, Object parent, int from, int to) throws UnknownTypeException {
        Object[] children = original.getChildren(parent, from, to);
        if (parent instanceof DebuggingView.DVThread) {
            Object[] jsChildren = createChildrenWithJSStack(children);
            if (jsChildren != null) {
                children = filterChildren(jsChildren);
            }
        }
        return children;
    }
    
    static Object[] createChildrenWithJSStack(Object[] children) {
        boolean isJSStack = false;
        for (int i = 0; i < children.length; i++) {
            Object ch = children[i];
            if (ch instanceof CallStackFrame) {
                CallStackFrame csf = (CallStackFrame) ch;
                if (csf.getClassName().startsWith(JSUtils.NASHORN_SCRIPT)) {
                    if (!isJSStack) {
                        Object[] children2 = new Object[children.length];
                        System.arraycopy(children, 0, children2, 0, children.length);
                        children = children2;
                    }
                    children[i] = JSStackFrame.get(csf);
                    isJSStack = true;
                }
            }
        }
        if (isJSStack) {
            return children;
        } else {
            return null;
        }
    }
    
    static Object[] filterChildren(Object[] children) {
        List<Object> newChildren = new ArrayList<>(children.length);
        newChildren.addAll(Arrays.asList(children));
        for (int i = 0; i < newChildren.size(); i++) {
            Object ch = newChildren.get(i);
            if (ch instanceof CallStackFrame) {
                String className = ((CallStackFrame) ch).getClassName();
                if (className.startsWith(FILTER1) ||
                    className.startsWith(FILTER2)) {
                    
                    newChildren.remove(i);
                    i--;
                }
            }
        }
        return newChildren.toArray();
    }

    @Override
    public int getChildrenCount(TreeModel original, Object node) throws UnknownTypeException {
        return original.getChildrenCount(node);
    }

    @Override
    public boolean isLeaf(TreeModel original, Object node) throws UnknownTypeException {
        if (node instanceof JSStackFrame) {
            return true;
        } else {
            return original.isLeaf(node);
        }
    }

    @Override
    public void addModelListener(ModelListener l) {
    }

    @Override
    public void removeModelListener(ModelListener l) {
    }
    
}
