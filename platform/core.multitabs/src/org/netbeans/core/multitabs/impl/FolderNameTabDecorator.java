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
package org.netbeans.core.multitabs.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.Icon;
import org.netbeans.core.multitabs.TabDecorator;
import org.netbeans.core.multitabs.prefs.SettingsImpl;
import org.netbeans.swing.tabcontrol.TabData;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * Show the name of parent folder in tab's title.
 * http://netbeans.org/bugzilla/show_bug.cgi?id=222696
 * 
 * @author S. Aubrecht
 */
@ServiceProvider(service=TabDecorator.class)
public class FolderNameTabDecorator extends TabDecorator {

    private final SettingsImpl settings = new SettingsImpl();
    private static final String pathSeparator = System.getProperty( "file.separator", "/" ); //NOI18N

    @Override
    public String getText( TabData tab ) {
        if( !settings.isShowFolderName() )
            return null;
        if( tab.getComponent() instanceof TopComponent ) {
            TopComponent tc = ( TopComponent ) tab.getComponent();
            DataObject dob = tc.getLookup().lookup( DataObject.class );
            if( null != dob ) {
                FileObject fo = dob.getPrimaryFile();
                if( fo.isData() ) {
                    FileObject folder = fo.getParent();
                    if( null != folder ) {
                        String folderName = folder.getNameExt() + pathSeparator;
                        String defaultText = tab.getText();

                        return merge( folderName, defaultText );
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Icon getIcon( TabData tab ) {
        return null;
    }

    @Override
    public Color getBackground( TabData tab, boolean selected ) {
        return null;
    }

    @Override
    public Color getForeground( TabData tab, boolean selected ) {
        return null;
    }

    @Override
    public void paintAfter( TabData tab, Graphics g, Rectangle tabRect, boolean isSelected ) {
    }

    private static String merge( String prefix, String baseText ) {
        if( null == baseText )
            baseText = "";
        StringBuilder res = new StringBuilder( prefix.length() + baseText.length() );

        if( baseText.toLowerCase().startsWith( "<html>") ) { //NOI18N
            res.append( "<html>" ); //NOI18N
            res.append( prefix );
            res.append( baseText.substring( 6 ) );
        } else {
            res.append( prefix );
            res.append( baseText );
        }

        return res.toString();
    }
}
