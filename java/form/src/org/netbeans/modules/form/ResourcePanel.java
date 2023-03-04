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

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

/**
 * An interface representing a panel usable as part of a custom property editor
 * allowing to define (store) the property value as a resource (ResourceValue).
 * @see ResourceWrapperEditor
 * 
 * @author Tomas Pavek
 */
public interface ResourcePanel {
    JComponent getComponent();
    void init(String key, boolean enable);
    void setValue(Object value, String stringValue, String cpResourceName);
    ResourceValue getResource();
    void addChangeListener(ChangeListener l);
    void removeChangeListener(ChangeListener l);
}
