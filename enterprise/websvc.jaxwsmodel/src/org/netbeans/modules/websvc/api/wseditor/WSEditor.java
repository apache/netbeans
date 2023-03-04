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

/*
 * WSEditor.java
 *
 * Created on March 9, 2006, 2:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.api.wseditor;

import javax.swing.JComponent;
import org.openide.nodes.Node;

/**
 *
 * @author Roderico Cruz
 * @author (modified by) ads
 */
public interface WSEditor {
    /**
     * Return the main panel of the editor
     */
    JComponent createWSEditorComponent(Node node) throws InvalidDataException;   

    /**
     * The title text that will be displayed in the tab corresponding
     * to the editor.
     */
    String getTitle();
    
    /**
     * This is called when the OK button is selected 
     */
    void save(Node node);
    
    /**
     * This is called when the Cancel button is selected
     */
    void cancel(Node node);
    
    /**
     *  Provides a description text that will be displayed at the top of the editor
     */
    String getDescription();
}
