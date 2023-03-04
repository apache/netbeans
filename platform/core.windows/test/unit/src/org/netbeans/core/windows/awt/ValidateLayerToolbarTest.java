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

package org.netbeans.core.windows.awt;


import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.openide.filesystems.*;

/** Checks the consistence of Toolbar folder.
 *
 * @author Jaroslav Tulach
 */
public class ValidateLayerToolbarTest extends ValidateLayerMenuTest {

    static {
        System.setProperty("java.awt.headless", "true");
    }

    /** Creates a new instance of SFSTest */
    public ValidateLayerToolbarTest(String name) {
        super (name);
    }

    public static Test suite() {
        return NbModuleSuite.createConfiguration(ValidateLayerToolbarTest.class)
                .clusters(".*")
                .enableModules(".*")
                .gui(false)
                .suite();
    }
    
    //
    // override in subclasses
    //
    
    @Override
    protected String rootName () {
        return "Toolbars";
    }
    
    /** Allowes to skip filest that are know to be broken */
    @Override
    protected boolean skipFile (FileObject fo) {
        return false;
    }
    
    @Override
    protected boolean correctInstance (Object obj) {
        if (obj instanceof javax.swing.Action) {
            return true;
        }
        if (obj instanceof org.openide.util.actions.Presenter.Toolbar) {
            return true;
        }
        if (obj instanceof javax.swing.JToolBar.Separator) {
            return true;
        }
        if (obj instanceof org.openide.awt.ToolbarPool.Configuration) {
            // definition of configuration
            return true;
        }
        if (obj instanceof java.awt.Component) {
            // definition of configuration
            return true;
        }
        
        return false;
    }

    //
    // Inherits test from superclass
    //
    
}

