/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
    private final static String pathSeparator = System.getProperty( "file.separator", "/" ); //NOI18N

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
