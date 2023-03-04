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

package org.netbeans.modules.openide.explorer;

import java.util.Map;
import java.util.WeakHashMap;
import org.openide.explorer.propertysheet.PropertyPanel;

/**
 * Provides access to non-public methods on PropertyPanel
 * 
 * @author Martin Entlicher
 */
public class PropertyPanelBridge {

    private static final Map<PropertyPanel, Accessor> accessors = new WeakHashMap<PropertyPanel, Accessor>();

    public static void register(PropertyPanel panel, Accessor accessor) {
        synchronized (accessors) {
            accessors.put(panel, accessor);
        }
    }

    public static boolean commit(PropertyPanel panel) {
        Accessor a;
        synchronized (accessors) {
            a = accessors.get(panel);
        }
        if (a == null) {
            return false;
        } else {
            return a.commit();
        }
    }

    public interface Accessor {

        boolean commit();

    }
}
