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

package org.netbeans.modules.xml.xam;

import java.util.EventListener;

/**
 * A component listener provides a coarse-grained event stream based on 
 * values or children of the source. This is not intended to replace
 * property change events and only serves as a way to differentiate between
 * children and non children related events. 
 *
 * @author Rico Cruz
 * @author Nam Nguyen
 * @author Chris Webster
 */
public interface ComponentListener extends EventListener {
    /**
     * Invoked if a value other than children is changed.
     * @param evt component event
     */
    void valueChanged(ComponentEvent evt);
    /**
     * Invoked if a child has been added.
     * @param evt component event
     */
    void childrenAdded(ComponentEvent evt);
    /**
     * Invoked if a child has been removed. 
     * @param evt component event
     */
    void childrenDeleted(ComponentEvent evt);
}
