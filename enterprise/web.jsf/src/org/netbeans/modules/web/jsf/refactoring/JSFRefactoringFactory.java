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

package org.netbeans.modules.web.jsf.refactoring;

import java.util.logging.Logger;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;

/**
 *
 * @author Petr Pisl
 */

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.RefactoringPluginFactory.class)
public class JSFRefactoringFactory implements RefactoringPluginFactory {
    
    private static final Logger LOGGER = Logger.getLogger(JSFRefactoringFactory.class.getName());
    
    /** Creates a new instance of J2EERefactoringFactory */
    public JSFRefactoringFactory() { }
    
    /** Creates and returns a new instance of the refactoring plugin or returns
     * null if the plugin is not suitable for the passed refactoring.
     * @param refactoring Refactoring, the plugin should operate on.
     * @return Instance of RefactoringPlugin or null if the plugin is not applicable to
     * the passed refactoring.
     */
    public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
        
        RefactoringPlugin plugin = null;
        
        LOGGER.fine("Create instance called: " + refactoring);                  //NOI18N
        if (refactoring instanceof RenameRefactoring) {
            LOGGER.fine("Rename refactoring");                                  //NOI18N
            plugin =  new JSFRenamePlugin((RenameRefactoring)refactoring);
        } else if (refactoring instanceof WhereUsedQuery) {
            LOGGER.fine("Where used refactoring");                              //NOI18N
            plugin = new JSFWhereUsedPlugin((WhereUsedQuery)refactoring);
        } else if (refactoring instanceof MoveRefactoring) {
            LOGGER.fine("Move refactoring");                                    //NOI18N
            plugin = new JSFMoveClassPlugin((MoveRefactoring)refactoring);
        } else if (refactoring instanceof SafeDeleteRefactoring) {
            LOGGER.fine("Safe delete refactoring");                             //NOI18N
            return new JSFSafeDeletePlugin((SafeDeleteRefactoring)refactoring);
        }
        return plugin;
    }
}
