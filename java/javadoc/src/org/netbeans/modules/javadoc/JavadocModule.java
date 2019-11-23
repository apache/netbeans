/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.javadoc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import org.openide.modules.ModuleInstall;
import org.openide.windows.TopComponent;

/**
 * Class for initializing Javadoc module on IDE startup.
 * @author Petr Hrebejk
 */
public final class JavadocModule extends ModuleInstall {

    private static Collection<TopComponent> floatingTopComponents;

    public synchronized static void registerTopComponent(TopComponent tc) {
        if (floatingTopComponents == null)
            floatingTopComponents = new LinkedList<>();
        floatingTopComponents.add(tc);
    }
    
    public synchronized static void unregisterTopComponent(TopComponent tc) {
        if (floatingTopComponents == null)
            return;
        floatingTopComponents.remove(tc);
    }
    
    public void uninstalled() {
        Collection c;
        synchronized (JavadocModule.class) {
            if (floatingTopComponents != null) {
                c = new ArrayList(floatingTopComponents);
            } else {
                c = Collections.EMPTY_SET;
            }
        }
        for (Iterator it = c.iterator(); it.hasNext();) {
            TopComponent tc = (TopComponent)it.next();
            tc.close();
        }
    }
}
