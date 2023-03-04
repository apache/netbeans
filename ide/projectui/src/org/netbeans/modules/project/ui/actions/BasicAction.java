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

package org.netbeans.modules.project.ui.actions;

import javax.swing.AbstractAction;
import javax.swing.Icon;

/** Basic action. Serves as a base class for all projects specific
 * actions.
 *
 * @author Pet Hrebejk
 */
abstract class BasicAction extends AbstractAction {

    protected BasicAction() {}

    protected BasicAction( String displayName, Icon icon ) {
        if ( displayName != null ) {
            setDisplayName( displayName );
        }
        if ( icon != null ) {
            setSmallIcon( icon );
        }
    }
    
    protected final void setDisplayName( String name ) {
        putValue( NAME, name );
    }
    
    protected final void setSmallIcon( Icon icon ) {
        if ( icon != null ) {
            putValue( SMALL_ICON, icon );
        }
    }
        
}
