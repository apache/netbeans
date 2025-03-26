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
package org.netbeans.core.multitabs.impl;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.CharConversionException;
import java.io.File;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.netbeans.core.multitabs.TabDecorator;
import org.netbeans.core.multitabs.prefs.SettingsImpl;
import org.netbeans.swing.popupswitcher.SwitcherTable;
import org.netbeans.swing.tabcontrol.TabData;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.xml.XMLUtil;

import static java.util.Objects.requireNonNullElse;

/**
 * Show the name of parent folder in tab's title.
 * http://netbeans.org/bugzilla/show_bug.cgi?id=222696
 * 
 * @author S. Aubrecht
 */
@ServiceProvider(service=TabDecorator.class)
public class FolderNameTabDecorator extends TabDecorator {

    private final SettingsImpl settings = new SettingsImpl();
    private final String fadeColor;
    private final boolean isTab;

    /**
     * Decorator used for tabs
     */
    public FolderNameTabDecorator() {
        fadeColor = fadeColor(
            requireNonNullElse(UIManager.getColor("nb.multitabs.foreground"), UIManager.getColor("TabbedPane.foreground")), //NOI18N
            requireNonNullElse(UIManager.getColor("nb.multitabs.background"), UIManager.getColor("TabbedPane.background")) //NOI18N
        );
        isTab = true;
    }

    /**
     * Decorator used for switcher
     */
    FolderNameTabDecorator(SwitcherTable switcher) {
        fadeColor = fadeColor(switcher.getForeground(), switcher.getBackground());
        isTab = false;
    }

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
                        String folderName = folder.getNameExt() + File.separator;
                        // don't use faded colors in tabs when colored tabs are active
                        // since it is difficult to get right with so many colors and shades involved
                        // the switcher uses a line as marker instead of bg change and can continue using fade colors
                        if (!(isTab && settings.isSameProjectSameColor())) {
                            folderName = "<font color=\"" + fadeColor + "\">" + folderName + "</font>"; //NOI18N
                        }
                        return merge(folderName, tab.getText());
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
            if (prefix.startsWith("<font")) { //NOI18N
                res.append("<html>"); //NOI18N
                res.append(prefix);
                try {
                    res.append(XMLUtil.toElementContent(baseText));
                } catch (CharConversionException ex) {
                    res.append(baseText);
                }
            } else {
                res.append(prefix);
                res.append(baseText);
            }
        }

        return res.toString();
    }

    private String fadeColor(Color f, Color b) {
        float a = isDarkLaF() ? 0.7f : 0.6f;
        return String.format("#%02x%02x%02x", //NOI18N
                 (int)(b.getRed()   + a * (f.getRed()   - b.getRed())),
                 (int)(b.getGreen() + a * (f.getGreen() - b.getGreen())),
                 (int)(b.getBlue()  + a * (f.getBlue()  - b.getBlue())));
    }

    private static boolean isDarkLaF() {
        return UIManager.getBoolean("nb.dark.theme"); //NOI18N 
    }

}
