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
package org.netbeans.spi.editor.document;

import java.beans.PropertyChangeListener;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.document.EditorMimeTypes;

/**
 * The SPI interface for {@link EditorMimeTypes}.
 * @since 1.1
 * @author Tomas Zezula
 */
public interface EditorMimeTypesImplementation {
    /**
     * The name of the "supportedMimeTypes" property.
     */
    String PROP_SUPPORTED_MIME_TYPES = "supportedMimeTypes";    //NOI18N

    /**
     * Returns a set of the supported mime types.
     * @return the supported mime types.
     */
    Set<String> getSupportedMimeTypes();

    /**
     * Adds a {@link PropertyChangeListener}.
     * @param listener the listener to be added.
     */
    void addPropertyChangeListener(@NonNull PropertyChangeListener listener);

    /**
     * Removes a {@link PropertyChangeListener}.
     * @param listener the listener to be removed.
     */
    void removePropertyChangeListener(@NonNull PropertyChangeListener listener);
}
