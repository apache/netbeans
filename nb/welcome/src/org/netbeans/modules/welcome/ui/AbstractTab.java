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

package org.netbeans.modules.welcome.ui;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.Constants;

/**
 * Base class for inner tabs in the Welcome Page
 * 
 * @author S. Aubrecht
 */
abstract class AbstractTab extends JPanel implements Constants {

    private boolean initialized = false;
    
    public AbstractTab( String title ) {
        super( new BorderLayout() );
        setName( title );
        setOpaque(false);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if( !initialized ) {
            add( new ContentHeader(getName()), BorderLayout.NORTH );
            add( buildContent(), BorderLayout.CENTER );
            add( new BottomBar(), BorderLayout.SOUTH );
            initialized = true;
        }
    }

    protected abstract JComponent buildContent();
}
