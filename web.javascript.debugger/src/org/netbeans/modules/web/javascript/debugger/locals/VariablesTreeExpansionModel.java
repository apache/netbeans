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

package org.netbeans.modules.web.javascript.debugger.locals;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.WeakSet;


/**
 * @author   Martin Entlicher
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="javascript-debuggerengine/LocalsView",
                                 types=TreeExpansionModel.class),
    @DebuggerServiceRegistration(path="javascript-debuggerengine/ResultsView",
                                 types=TreeExpansionModel.class),
    @DebuggerServiceRegistration(path="javascript-debuggerengine/WatchesView",
                                 types=TreeExpansionModel.class),
})
public class VariablesTreeExpansionModel implements TreeExpansionModel {
    
    private static final Logger LOGGER = Logger.getLogger(VariablesTreeExpansionModel.class.getName());

    private Set expandedNodes = new WeakSet();
    private Set collapsedNodes = new WeakSet();

    /**
     * Defines default state (collapsed, expanded) of given node.
     *
     * @param node a node
     * @return default state (collapsed, expanded) of given node
     */
    @Override
    public boolean isExpanded (Object node) 
    throws UnknownTypeException {
        synchronized (this) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE,"isExpanded({0}):\n"+
                                      " expandedNodes = {1}\n"+
                                      " => {2}",
                           new Object[]{node, expandedNodes, expandedNodes.contains(node)});
            }
            if (expandedNodes.contains(node)) {
                return true;
            }
            if (collapsedNodes.contains(node)) {
                return false;
            }
        }
        // Default behavior follows:
        return false;
    }
    
    /**
     * Called when given node is expanded.
     *
     * @param node a expanded node
     */
    @Override
    public void nodeExpanded (Object node) {
        synchronized (this) {
            expandedNodes.add(node);
            collapsedNodes.remove(node);
            LOGGER.log(Level.FINE,"nodeExpanded({0}):\n"+
                                  " => expandedNodes = {1}",
                       new Object[]{node, expandedNodes});
        }
    }
    
    /**
     * Called when given node is collapsed.
     *
     * @param node a collapsed node
     */
    @Override
    public void nodeCollapsed (Object node) {
        synchronized (this) {
            collapsedNodes.add(node);
            expandedNodes.remove(node);
        }
    }
}
