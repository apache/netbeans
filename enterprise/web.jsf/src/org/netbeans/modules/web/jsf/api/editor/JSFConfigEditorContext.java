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

package org.netbeans.modules.web.jsf.api.editor;


import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 *
 * @author Petr Pisl
 */
public interface JSFConfigEditorContext {

    /**
     * The method provides the faces configuration file, for which the editor is opened.
     * @return faces configuration file
     */
    public FileObject getFacesConfigFile();

    /**
     * Provide UndoRedo manager for the editor.
     * @return
     */
    public UndoRedo getUndoRedo();

    /**
     * This method should be called by from the implementation of
     * MultiViewElement.setMultiViewCallback. The editor needs to know, which TopComponent
     * is now displayed.
     * @param topComponent which is displayed
     */
    public void setMultiViewTopComponent(TopComponent topComponent);
}
