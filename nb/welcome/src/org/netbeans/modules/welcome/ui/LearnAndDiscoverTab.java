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

package org.netbeans.modules.welcome.ui;

import java.awt.GridLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.ContentSection;

/**
 * 'Learn & Discover' tab of the Start Page
 * 
 * @author S. Aubrecht
 */
class LearnAndDiscoverTab extends AbstractTab {

    public LearnAndDiscoverTab() {
        super(BundleSupport.getLabel( "LearnAndDiscoverTab" )); //NOI18N
    }

    @Override
    protected JComponent buildContent() {
        JPanel panel = new JPanel(new GridLayout(1,0));
        panel.setOpaque(false);
        panel.add(new ContentSection( new GetStarted(), false ));

        panel.add( new ContentSection( BundleSupport.getLabel("SectionDemosAndTutorials"), new Tutorials(), false )); //NOI18N

        return panel;
    }
}
