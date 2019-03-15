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
package org.netbeans.modules.java.source.run;

import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.netbeans.api.java.source.support.SingleSourceRunActionSupport;
import org.openide.loaders.DataObject;
import org.netbeans.modules.java.JavaNode;

/**
 *
 * @author Sarvesh Kesharwani
 */
public class SingleSourceFileActionProvider {
    
    private static final SingleSourceRunActionSupport runSupport;
    
    public static String[] supportedActions = {
        ActionProvider.COMMAND_RUN_SINGLE,
        
        //ActionProvider.COMMAND_DEBUG_SINGLE
    };
    
    public static boolean isActionSupported (DataObject dObj, String command) {
        if (dObj.getNodeDelegate() instanceof JavaNode) {
            for (String supportedAction : supportedActions) {
                if (supportedAction.equalsIgnoreCase(command)) 
                    return true;
            }
        }
        return false;
    }
    
    static {
        runSupport = new SingleSourceRunActionSupport();
    }
    
    public static void invokeAction(String command, FileObject fObj) {
        if (runSupport.isActionSupported(command)) {
            runSupport.invokeAction(command, fObj);
        }
    }
    
}
