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

package org.netbeans.modules.project.uiapi;

import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.spi.project.ui.support.FileActionPerformer;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.util.ContextAwareAction;

/**
 * Factory to be implemented by the ui implementation
 * @author Petr Hrebejk
 */
public interface ActionsFactory {

    // Actions releated directly to project UI

    public Action setAsMainProjectAction();

    public Action customizeProjectAction();

    public Action openSubprojectsAction();

    public Action closeProjectAction();

    public Action newFileAction();
    
    public Action deleteProjectAction();
    
    public Action copyProjectAction();
    
    public Action moveProjectAction();
    
    public Action newProjectAction();
            
    // Actions sensitive to project selection
    
    public ContextAwareAction projectCommandAction( String command, String namePattern, Icon icon );
    
    public Action projectSensitiveAction( ProjectActionPerformer performer, String name, Icon icon );
    
    // Actions selection to main project selection
    
    public Action mainProjectCommandAction( String command, String name, Icon icon  );
        
    public Action mainProjectSensitiveAction( ProjectActionPerformer performer, String name, Icon icon );
    
    // Actions sensitive to file
    
    public Action fileCommandAction( String command, String name, Icon icon );
    
    public Action fileSensitiveAction( FileActionPerformer performer, String name, Icon icon);

    public Action renameProjectAction();

    Action setProjectConfigurationAction();
    
}
