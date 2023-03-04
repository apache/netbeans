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

package org.netbeans.modules.j2ee.metadata.model.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.*;

/**
 *
 * @author Andrei Badea
 */
public class PersistentObjectList<T extends PersistentObject> {

    private static final Logger LOGGER = Logger.getLogger(PersistentObjectManager.class.getName());

    private final Map<ElementHandle<TypeElement>, List<T>> type2Objects = new HashMap<ElementHandle<TypeElement>, List<T>>();

    public void add(List<T> objects) {
        for (T newObject : objects) {
            List<T> list = type2Objects.get(newObject.getTypeElementHandle());
            if (list == null) {
                list = new ArrayList<T>();
                type2Objects.put(newObject.getTypeElementHandle(), list);
            }
            list.add(newObject);
        }
    }

    public boolean put(ElementHandle<TypeElement> typeHandle, List<T> objects) {
        List<T> list = new ArrayList<T>();
        for (T object : objects) {
            ElementHandle<TypeElement> sourceHandle = object.getTypeElementHandle();
            if (sourceHandle.equals(typeHandle)) {
                list.add(object);
            } else {
                LOGGER.log(Level.WARNING, "setObjects: ignoring object with incorrect ElementHandle {0} (expected {1})", new Object[] { sourceHandle, typeHandle }); // NOI18N
            }
        }
        if (list.size() > 0) {
            type2Objects.put(typeHandle, list);
            return true;
        } else {
            List<T> oldList = type2Objects.remove(typeHandle);
            return oldList != null;
        }
    }

    public List<T> remove(ElementHandle<TypeElement> typeHandle) {
        return type2Objects.remove(typeHandle);
    }

    public void clear() {
        type2Objects.clear();
    }

    public List<T> get() {
        List<T> result = new ArrayList<T>(type2Objects.size() * 2);
        for (List<T> list : type2Objects.values()) {
            result.addAll(list);
        }
        return Collections.unmodifiableList(result);
    }

    public List<T> get(ElementHandle<TypeElement> typeHandle) {
        List<T> list = type2Objects.get(typeHandle);
        return list != null ? Collections.unmodifiableList(list) : null;
    }
}
