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
package org.netbeans.modules.java.source.ui;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementOpen;

/**
 *
 * @author jlahoda
 */
public abstract class ElementOpenAccessor {
    
    private static ElementOpenAccessor instance;

    public synchronized static ElementOpenAccessor getInstance() {
        return instance;
    }

    public synchronized static void setInstance(ElementOpenAccessor instance) {
        ElementOpenAccessor.instance = instance;
    }
    
    public abstract Object[] getOpenInfo(final ClasspathInfo cpInfo, final ElementHandle<? extends Element> el, AtomicBoolean cancel);

    static {
        try {
            Class.forName(ElementOpen.class.getName(), true, ElementOpen.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            //ignore
        }
    }
}
