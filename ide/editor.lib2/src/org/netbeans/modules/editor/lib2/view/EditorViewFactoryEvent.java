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

package org.netbeans.modules.editor.lib2.view;

import java.util.EventObject;
import java.util.List;

/**
 * Event fired by editor view factories upon change.
 *
 * @author Miloslav Metelka
 */
public final class EditorViewFactoryEvent extends EventObject {
    
    private final List<EditorViewFactoryChange> changes;
    
    EditorViewFactoryEvent(EditorViewFactory factory, List<EditorViewFactoryChange> changes) {
        super(factory);
        this.changes = changes;
    }

    /**
     * @return changes that occurred in the view factory.
     */
    public List<EditorViewFactoryChange> getChanges() {
        return changes;
    }

    @Override
    public String toString() {
        return super.toString() + "\nChanges: " + getChanges(); // NOI18N
    }

    
}
