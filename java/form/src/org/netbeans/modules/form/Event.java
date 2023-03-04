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

package org.netbeans.modules.form;

import java.util.*;
import java.beans.EventSetDescriptor;
import java.lang.reflect.Method;

/**
 * Represents one event of a component. Holds handlers attached to the event.
 *
 * @author Tomas Pavek
 */

public class Event {

    private static String[] NO_HANDLERS = {};

    private RADComponent component;
    private EventSetDescriptor eventSetDescriptor;
    private Method listenerMethod;
    private boolean inCEDL; // CEDL - common event dispatching listener
    private List<String> eventHandlers;

    Event(RADComponent component,
          EventSetDescriptor eventSetDescriptor,
          Method listenerMethod)
    {
        this.component = component;
        this.eventSetDescriptor = eventSetDescriptor;
        this.listenerMethod = listenerMethod;
    }

    // --------

    public String getName() {
        return listenerMethod.getName();
    }

    public String getId() {
        return FormEvents.getEventIdName(listenerMethod);
    }

    public final RADComponent getComponent() {
        return component;
    }

    public final EventSetDescriptor getEventSetDescriptor() {
        return eventSetDescriptor;
    }

    public final Method getListenerMethod() {
        return listenerMethod;
    }

    public final String getEventParameterType() {
        Class[] paramTypes = listenerMethod != null ? listenerMethod.getParameterTypes() : null;
        if (paramTypes != null && paramTypes.length == 1) {
            return paramTypes[0].getCanonicalName();
        }
        return null;
    }

    public boolean hasEventHandlers() {
        return eventHandlers != null && eventHandlers.size() > 0;
    }

    public boolean hasEventHandler(String handler) {
        return eventHandlers != null ? eventHandlers.contains(handler) : false;
    }

    public String[] getEventHandlers() {
        if (eventHandlers == null || eventHandlers.isEmpty())
            return NO_HANDLERS;

        String[] handlerNames = new String[eventHandlers.size()];
        eventHandlers.toArray(handlerNames);
        return handlerNames;
    }

    // CEDL - common event dispatching listener
    public final boolean isInCEDL() {
        return inCEDL;
    }

    // --------

    void setInCEDL(boolean isIn) {
        inCEDL = isIn;
    }

    boolean addEventHandler(String handlerName) {
        if (eventHandlers == null)
            eventHandlers = new ArrayList<String>(1);
        else if (eventHandlers.contains(handlerName))
            return false;

        eventHandlers.add(handlerName);
        return true;
    }

    boolean removeEventHandler(String handlerName) {
        return eventHandlers != null && eventHandlers.remove(handlerName);
    }

    boolean renameEventHandler(String oldHandlerName, String newHandlerName) {
        if (eventHandlers == null)
            return false;
        int index = eventHandlers.indexOf(oldHandlerName);
        if (index < 0 || eventHandlers.contains(newHandlerName))
            return false;

        eventHandlers.set(index, newHandlerName);
        return true;
    }

    List getEventHandlerList() {
        return eventHandlers;
    }
}
