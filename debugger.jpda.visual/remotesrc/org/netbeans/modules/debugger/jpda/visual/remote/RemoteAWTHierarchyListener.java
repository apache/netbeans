/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.visual.remote;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.AWTEventListener;
import java.awt.event.HierarchyEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.WeakHashMap;

/**
 * Listens on changes in component hierarchy.
 * 
 * @author Martin Entlicher
 */
class RemoteAWTHierarchyListener implements AWTEventListener {
    
    private final WeakHashMap components = new WeakHashMap();

    public void eventDispatched(AWTEvent event) {
        HierarchyEvent e = (HierarchyEvent) event;
        if ((e.getChangeFlags() & HierarchyEvent.PARENT_CHANGED) != 0) {
            Component c = e.getChanged();
            Container p = c.getParent();
            if (p == null) {
                // Component was removed from the hierarchy
                components.remove(c);
            } else {
                Throwable t = new RuntimeException();
                StringWriter sw = new StringWriter();
                t.printStackTrace(new PrintWriter(sw));
                String stackTrace = sw.toString();
                String stackLine = getComponentAddStackLine(stackTrace);
                components.put(c, stackLine);
            }
        }
    }
    
    String getStackFromComponent(Component c) {
        return (String) components.get(c);
    }
    
    private static String getComponentAddStackLine(String stack) {
        int pos = 0;
        int eol;
        String lineSeparator = System.getProperty("line.separator");
        for ( ; (eol = stack.indexOf(lineSeparator, pos)) > 0; pos = eol + 1) {
            String line = stack.substring(pos, eol).trim();
            if (line.startsWith("at ")) {
                if (line.indexOf(RemoteAWTHierarchyListener.class.getName()) > 0) {
                    continue;
                }
                if (line.indexOf(" java.awt.") > 0 ||
                    line.indexOf(" javax.swing.") > 0 ||
                    line.indexOf(" com.sun.") > 0) {
                    
                    continue;
                }
                return line;
            }
        }
        return stack;
    }

}
