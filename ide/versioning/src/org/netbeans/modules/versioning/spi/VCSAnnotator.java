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
package org.netbeans.modules.versioning.spi;

import javax.swing.*;
import java.awt.Image;

/**
 * Annotator provides these services based on files' versioning status:
 * <ul>
 * <li>coloring for labels (file and folder names, editor tabs, etc.)
 * <li>badging (modification of node icons)
 * <li>provides set of Actions
 * </ul>
 * 
 * @author Maros Sandor
 */
public abstract class VCSAnnotator {

    /**
     * Protected constructor, does nothing.   
     */
    protected VCSAnnotator() {
    }
    
    /**
     * Specifies destination of returned actions. Destination MainMenu means actions will be user to construct main
     * application menu, PopupMenu means actions will be used to construct popup menus on projects, files and folders.
     * 
     * @see #getActions
     */
    public enum ActionDestination { MainMenu, PopupMenu }; 

    /**
     * Allows a versioning system to decorate given name with HTML markup. This can be used to highlight file status. 
     * 
     * @param name text to decorate
     * @param context a context this name represents
     * @return decorated name or the same name left undecorated
     */
    public String annotateName(String name, VCSContext context) {
        return name;
    }

    /**
     * Allows a versioning system to decorate given icon (badging). This can be used to highlight file status. 
     * 
     * @param icon an icon to decorate
     * @param context a context this icon represents
     * @return decorated icon or the same icon left undecorated
     */
    public Image annotateIcon(Image icon, VCSContext context) {
        return icon;
    }

    /**
     * Returns set of actions to offer to the user use on a given context.
     * 
     * @param context context on which returned actions should operate
     * @param destination where this actions will be used
     * @return Action[] array of actions to display for the given context, use null for separators
     */
    public Action[] getActions(VCSContext context, ActionDestination destination) {
        return new Action[0];
    }
}
