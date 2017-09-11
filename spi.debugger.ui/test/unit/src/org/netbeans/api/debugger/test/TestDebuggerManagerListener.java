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

package org.netbeans.api.debugger.test;

import org.netbeans.api.debugger.*;

import java.util.*;
import java.beans.PropertyChangeEvent;

/**
 * A test debugger manager listener implementation.
 *
 * @author Maros Sandor
 */
public class TestDebuggerManagerListener implements DebuggerManagerListener {

    public static class Event {

        String name;
        Object param;

        public Event(String name, Object param) {
            this.name = name;
            this.param = param;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Event)) return false;

            final Event event = (Event) o;

            if (!name.equals(event.name)) return false;
            if (param != null ? !param.equals(event.param) : event.param != null) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = name.hashCode();
            result = 29 * result + (param != null ? param.hashCode() : 0);
            return result;
        }

        public String getName() {
            return name;
        }

        public Object getParam() {
            return param;
        }
    }

    private List events = new ArrayList();

    public List getEvents() {
        List listCopy = new ArrayList(events);
        events.clear();
        return listCopy;
    }

    public void breakpointAdded(Breakpoint breakpoint) {
        events.add(new Event("breakpointAdded", breakpoint));
    }

    public void breakpointRemoved(Breakpoint breakpoint) {
        events.add(new Event("breakpointRemoved", breakpoint));
    }

    public void watchAdded(Watch watch) {
        events.add(new Event("watchAdded", watch));
    }

    public void watchRemoved(Watch watch) {
        events.add(new Event("watchRemoved", watch));
    }

    public void sessionAdded(Session session) {
        events.add(new Event("sessionAdded", session));
    }

    public void sessionRemoved(Session session) {
        events.add(new Event("sessionRemoved", session));
    }

    public void propertyChange(PropertyChangeEvent evt) {
        events.add(new Event("propertyChange", evt));
    }

    public Breakpoint[] initBreakpoints() {
        return new Breakpoint[0];
    }

    public void initWatches() {
    }

    // TODO: Include check of these call in the test suite
    public void engineAdded(DebuggerEngine engine) {
    }

    // TODO: Include check of these call in the test suite
    public void engineRemoved(DebuggerEngine engine) {
    }
}
