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
package org.netbeans.modules.web.javascript.debugger.breakpoints;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Entlicher
 */
public class EventsBreakpoint extends AbstractBreakpoint {
    
    @NbBundle.Messages({
        "LBL_Event_Category_Animation=Animation",
        "LBL_Event_Category_Control=Control",
        "LBL_Event_Category_Clipboard=Clipboard",
        "LBL_Event_Category_DOM_Mutation=DOM Mutation",
        "LBL_Event_Category_Device=Device",
        "LBL_Event_Category_Keyboard=Keyboard",
        "LBL_Event_Category_Load=Load",
        "LBL_Event_Category_Mouse=Mouse",
        "LBL_Event_Category_Timer=Timer",
        "LBL_Event_Category_Touch=Touch"
    })
    private static final String[][] EVENTS = new String[][] {
        { Bundle.LBL_Event_Category_Animation(), "requestAnimationFrame", "cancelAnimationFrame", "animationFrameFired" },
        { Bundle.LBL_Event_Category_Clipboard(), "copy", "cut", "paste", "beforecopy", "beforecut", "beforepaste" },
        { Bundle.LBL_Event_Category_Control(), "resize", "scroll", "zoom", "focus", "blur", "select", "change", "submit", "reset" },
        { Bundle.LBL_Event_Category_DOM_Mutation(), "DOMActivate", "DOMFocusIn", "DOMFocusOut", "DOMAttrModified", "DOMCharacterDataModified", "DOMNodeInserted", "DOMNodeInsertedIntoDocument", "DOMNodeRemoved", "DOMNodeRemovedFromDocument", "DOMSubtreeModified", "DOMContentLoaded" },
        { Bundle.LBL_Event_Category_Device(), "deviceorientation", "devicemotion" },
        { Bundle.LBL_Event_Category_Keyboard(), "keydown", "keyup", "keypress", "textInput" },
        { Bundle.LBL_Event_Category_Load(), "load", "unload", "abort", "error" },
        { Bundle.LBL_Event_Category_Mouse(), "click", "dblclick", "mousedown", "mouseup", "mouseover", "mousemove", "mouseout", "mousewheel" },
        { Bundle.LBL_Event_Category_Timer(), "setTimer", "clearTimer", "timerFired" },
        { Bundle.LBL_Event_Category_Touch(), "touchstart", "touchmove", "touchend", "touchcancel" },
    };
    /** Tells which events are instrumentation. */
    private static final boolean[] INSTRUMENTATION = new boolean[] {
        true, false, false, false, false, false, false, false, true, false
    };
    
    public static final String PROP_EVENTS = "events";     // NOI18N
    
    private static final Set<String> categories;
    private static final Map<String, Set<String>> eventsByCategories;
    private static final Map<String, String> categoryOf;
    private static final Set<String> instrumentationEvents;
    
    /** The actual breakpoint events. */
    private final Set<String> events = new HashSet<String>();
    
    static {
        int n = EVENTS.length;
        assert INSTRUMENTATION.length == n;
        Map<String, Set<String>> ebcm = new HashMap<String, Set<String>>(n);
        Map<String, String> catOf = new HashMap<String, String>();
        Set<String> instrEv = new HashSet<String>();
        for (int i = 0; i < n; i++) {
            Set<String> evts = new LinkedHashSet<String>(EVENTS[i].length - 1);
            boolean isInstrumentation = INSTRUMENTATION[i];
            for (int j = 1; j < EVENTS[i].length; j++) {
                String event = EVENTS[i][j];
                evts.add(event);
                catOf.put(event, EVENTS[i][0]);
                if (isInstrumentation) {
                    instrEv.add(event);
                }
            }
            ebcm.put(EVENTS[i][0], evts);
        }
        eventsByCategories = Collections.unmodifiableMap(ebcm);
        SortedSet<String> categoriesSet = new TreeSet<String>(ebcm.keySet());
        categories = Collections.unmodifiableSortedSet(categoriesSet);
        categoryOf = Collections.unmodifiableMap(catOf);
        instrumentationEvents = Collections.unmodifiableSet(instrEv);
    }
    
    public static Set<String> getAllEventCategories() {
        return categories;
    }
    
    public static Set<String> getAllEvents(String category) {
        return eventsByCategories.get(category);
    }
    
    public static String getCategoryOf(String event) {
        return categoryOf.get(event);
    }
    
    public EventsBreakpoint() {
    }
    
    public Set<String> getEvents() {
        synchronized (events) {
            return Collections.unmodifiableSet(new HashSet<String>(events));
        }
    }
    
    public boolean isInstrumentationEvent(String event) {
        return instrumentationEvents.contains(event);
    }
    
    public void addEvent(String event) {
        boolean added;
        synchronized (events) {
            added = events.add(event);
        }
        if (added) {
            firePropertyChange(PROP_EVENTS, null, event);
        }
    }
    
    public void removeEvent(String event) {
        boolean removed;
        synchronized (events) {
            removed = events.remove(event);
        }
        if (removed) {
            firePropertyChange(PROP_EVENTS, event, null);
        }
    }
    
    public boolean hasEvent(String event) {
        synchronized (events) {
            return events.contains(event);
        }
    }
    
}
