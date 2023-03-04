/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
