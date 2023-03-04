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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.RSSFeedReaderPanel;
import org.netbeans.modules.welcome.content.WebLink;

/**
 *
 * @author S. Aubrecht
 */
class Blogs extends RSSFeedReaderPanel {

    public Blogs() {
        super( "Blogs", false ); // NOI18N

        add( buildBottomContent(), BorderLayout.SOUTH );
    }

    protected JComponent buildBottomContent() {
        WebLink allBlogs = new WebLink( "AllBlogs", true ); // NOI18N
        BundleSupport.setAccessibilityProperties( allBlogs, "AllBlogs" ); //NOI18N

        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque(false);
        panel.add( allBlogs, new GridBagConstraints(1,0,1,1,0.0,0.0,GridBagConstraints.SOUTHEAST,GridBagConstraints.HORIZONTAL,new Insets(5,5,0,5),0,0) );
        panel.add( new JLabel(), new GridBagConstraints(0,0,1,1,1.0,0.0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0) );

        return panel;
    }
}
