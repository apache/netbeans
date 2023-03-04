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

package org.myorg.systemproperties;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;




/**
 *
 * @author Administrator
 */
public class PropertiesNotifier {
    private static Set listeners = new HashSet();
    public static void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }
    public static void removeChangeListener(ChangeListener
            listener) {
        listeners.remove(listener);
    }
    public static void changed() {
        ChangeEvent ev = new ChangeEvent(PropertiesNotifier.class);
        Iterator it = listeners.iterator();
        while (it.hasNext())
            ((ChangeListener) it.next()).stateChanged(ev);
    }
}
