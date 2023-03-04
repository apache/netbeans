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
package org.netbeans.modules.css.visual.spi;

import java.util.Collection;
import javax.swing.JComponent;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Representation of the CssStyles window named panel.
 * 
 * Instance to be registered in global lookup.
 *
 * @author marekfukala
 */
public interface CssStylesPanelProvider {
    
    /**
     * The implementation decides whether it wants to include its UI for the given file.
     * 
     * @param file context file
     * @return true if the panel should be included in the CSS Styles window UI,
     * false otherwise.
     */
    public boolean providesContentFor(FileObject file);
    
    /**
     * Gets an unique system id for the panel. 
     * 
     * Not presented in UI.
     */
    public String getPanelID();

    /**
     * Gets a display name which is show in the toolbar.
     */
    public String getPanelDisplayName();
    
    /**
     * Gets the content component.
     * 
     * Called just once per IDE session when the panel content is about to be 
     * shown in the UI for the first time.
     * 
     * The implementor should listen on the lookup content and respond according upon changes.
     * An instance of {@link FileObject} is updated in the lookup as the edited file changes.
     * 
     * @param lookup instance of {@link Lookup} with some context object. 
     */
    public JComponent getContent(Lookup lookup);
    
    /**
     * Content of the obtained lookup will be added to the Css Styles window TC's lookup.
     */
    public Lookup getLookup();
    
    /**
     * Called after the component is made active (visible).
     */
    public void activated();
    
    /**
     * Called after the component is made inactive (invisible).
     */
    public void deactivated();
    
}
