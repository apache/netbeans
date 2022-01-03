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

package org.netbeans.modules.cnd.api.model;

import java.util.EventListener;

/**
 * Gets notification on model events
 * for now, project opening and closing
 *
 */
public interface CsmModelListener extends EventListener {

    /** Is called upon project opening */
    void projectOpened(CsmProject project);

    /** 
     * Is called upon project closing.
     * At the moment of this call the project isn't really closed;
     * (this is more convenient to clients)
     * TODO: consider renaming to projectClosing
     */
    void projectClosed(CsmProject project);

    /**
     * Is called when model is changed
     * (except for changes made at initial scanning)
     */
    void modelChanged(CsmChangeEvent e);
}
