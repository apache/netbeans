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

package org.netbeans.modules.debugger.ui;

import java.beans.PropertyChangeEvent;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.spi.debugger.ui.EditorPin;


/**
 * Listens on DebuggerManager and:
 * - loads all breakpoints & watches on startup
 * - listens on all changes of breakpoints and watches (like breakoint / watch
 *     added / removed, or some property change) and saves a new values
 *
 * @author Jan Jancura
 */
public class PersistenceManager implements LazyDebuggerManagerListener {
    
    public Breakpoint[] initBreakpoints () {
        return new Breakpoint [0];
    }
    
    public void initWatches () {
        // As a side-effect, creates the watches. WatchesReader is triggered.
        Properties p = Properties.getDefault ().getProperties ("debugger");
        Watch[] watches = (Watch[]) p.getArray (
            DebuggerManager.PROP_WATCHES, 
            new Watch [0]
        );
        for (Watch watch : watches) {
            watch.addPropertyChangeListener (this);
            Watch.Pin pin = watch.getPin();
            if (pin instanceof EditorPin) {
                ((EditorPin) pin).addPropertyChangeListener(this);
            }
        }
    }
    
    public String[] getProperties () {
        return new String [] {
            DebuggerManager.PROP_WATCHES_INIT,
            DebuggerManager.PROP_WATCHES
        };
    }
    
    public void breakpointAdded (Breakpoint breakpoint) {
    }

    public void breakpointRemoved (Breakpoint breakpoint) {
    }
    
    public void watchAdded (Watch watch) {
        Properties p = Properties.getDefault ().getProperties ("debugger");
        p.setArray (
            DebuggerManager.PROP_WATCHES, 
            DebuggerManager.getDebuggerManager ().getWatches ()
        );
        watch.addPropertyChangeListener (this);
        Watch.Pin pin = watch.getPin();
        if (pin instanceof EditorPin) {
            ((EditorPin) pin).addPropertyChangeListener(this);
        }
    }
    
    public void watchRemoved (Watch watch) {
        Properties p = Properties.getDefault ().getProperties ("debugger");
        p.setArray (
            DebuggerManager.PROP_WATCHES, 
            DebuggerManager.getDebuggerManager ().getWatches ()
        );
        watch.removePropertyChangeListener(this);
        Watch.Pin pin = watch.getPin();
        if (pin instanceof EditorPin) {
            ((EditorPin) pin).removePropertyChangeListener(this);
        }
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        Object source = evt.getSource();
        if (source instanceof Watch || source instanceof EditorPin) {
            Properties.getDefault ().getProperties ("debugger").setArray (
                DebuggerManager.PROP_WATCHES,
                DebuggerManager.getDebuggerManager ().getWatches ()
            );
        }
    }
    
    public void sessionAdded (Session session) {}
    public void sessionRemoved (Session session) {}
    public void engineAdded (DebuggerEngine engine) {}
    public void engineRemoved (DebuggerEngine engine) {}
}
