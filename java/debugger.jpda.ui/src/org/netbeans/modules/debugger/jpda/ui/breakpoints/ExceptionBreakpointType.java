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
package org.netbeans.modules.debugger.jpda.ui.breakpoints;

import javax.swing.JComponent;

import org.netbeans.spi.debugger.ui.BreakpointType;

import org.openide.util.NbBundle;


/**
* Implementation of breakpoint on exception.
*
* @author   Jan Jancura
*/
@BreakpointType.Registration(displayName="#CTL_Exception_event_name_type_name")
public class ExceptionBreakpointType extends BreakpointType {

    public String getCategoryDisplayName() {
        return NbBundle.getMessage (
            ExceptionBreakpointType.class,
            "CTL_Java_breakpoint_events_cathegory_name"
        );
    }
    
    public JComponent getCustomizer () {
        return new ExceptionBreakpointPanel ();
    }
    
    public String getTypeDisplayName () {
        return NbBundle.getMessage (
            ExceptionBreakpointType.class,
            "CTL_Exception_event_name_type_name"
        );
    }
    
    public boolean isDefault () {
        return false;
    }
}

