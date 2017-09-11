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
