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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import java.util.List;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class CascadeTypeImpl implements CascadeType {

    // XXX CascadeType and this impl should be replaced with something simpler

    private final EmptyType all, persist, merge, remove, refresh;

    public CascadeTypeImpl() {
        all = null;
        persist = null;
        merge = null;
        remove = null;
        refresh = null;
    }

    public CascadeTypeImpl(List<AnnotationValue> cascadeValues) {
        EmptyType tempAll = null, tempPersist = null, tempMerge = null, tempRemove = null, tempRefresh = null;
        for (AnnotationValue value : cascadeValues) {
            Name valueName = ((VariableElement)value.getValue()).getSimpleName();
            if (valueName.contentEquals("ALL")) { // NOI18N
                tempAll = new EmptyTypeImpl();
            } else if (valueName.contentEquals("PERSIST")) { // NOI18N
                tempPersist = new EmptyTypeImpl();
            } else if (valueName.contentEquals("MERGE")) { // NOI18N
                tempMerge = new EmptyTypeImpl();
            } else if (valueName.contentEquals("REMOVE")) { // NOI18N
                tempRemove = new EmptyTypeImpl();
            } else if (valueName.contentEquals("REFRESH")) { // NOI18N
                tempRefresh = new EmptyTypeImpl();
            }
        }
        all = tempAll;
        persist = tempPersist;
        merge = tempMerge;
        remove = tempRemove;
        refresh = tempRefresh;
    }

    public void setCascadeAll(EmptyType value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmptyType getCascadeAll() {
        return all;
    }

    public EmptyType newEmptyType() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setCascadePersist(EmptyType value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmptyType getCascadePersist() {
        return persist;
    }

    public void setCascadeMerge(EmptyType value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmptyType getCascadeMerge() {
        return merge;
    }

    public void setCascadeRemove(EmptyType value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmptyType getCascadeRemove() {
        return remove;
    }

    public void setCascadeRefresh(EmptyType value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public EmptyType getCascadeRefresh() {
        return refresh;
    }
}
