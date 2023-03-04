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
package org.netbeans.modules.websvc.core;

import javax.swing.event.ChangeListener;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.netbeans.api.project.Project;

/**
 * This API displays the web service and client nodes in this project.
 * @author Ajit Bhate
 */
public interface ProjectWebServiceViewImpl {

    /** 
     * Add changeListener for given type (service or client)
     */
    void addChangeListener(ChangeListener l, ProjectWebServiceView.ViewType viewType);
    /** 
     * Remove changeListener for given type (service or client)
     */
    void removeChangeListener(ChangeListener l, ProjectWebServiceView.ViewType viewType);

    /** 
     * Create view for given type (service or client)
     */
    Node[] createView(ProjectWebServiceView.ViewType viewType);

    /** 
     * If a view for given type (service or client) is empty.
     */
    boolean isViewEmpty(ProjectWebServiceView.ViewType viewType);

    /** 
     * Notify that this view is in use.
     * Subclasses may add listeners here
     */
    void addNotify();

    /** 
     * Notify that this view is not in use.
     * Subclasses may remove listeners here.
     */
    void removeNotify();

}
