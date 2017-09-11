/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.welcome.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.welcome.content.BundleSupport;
import org.netbeans.modules.welcome.content.ActionButton;
import org.netbeans.modules.welcome.content.Constants;
import org.netbeans.modules.welcome.content.Utils;
import org.netbeans.modules.welcome.content.WebLink;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 * @author S. Aubrecht
 */
class Tutorials extends JPanel implements Constants {

    private int row;

    /** Creates a new instance of RecentProjects */
    public Tutorials() {
        super( new GridBagLayout() );
        setOpaque(false);
        buildContent();
    }
    
    private void buildContent() {
        String rootName = "WelcomePage/TutorialsLinks";  // NOI18N
        FileObject root = FileUtil.getConfigFile( rootName );
        if( null == root ) {
            Logger.getLogger(Tutorials.class.getName()).log(Level.INFO,
                    "Start page content not found: " + "FileObject: " + rootName ); //NOI18N
            return;
        }
        DataFolder folder = DataFolder.findFolder( root );
        if( null == folder ) {
            Logger.getLogger(Tutorials.class.getName()).log(Level.INFO,
                    "Start page content not found: " + "DataFolder: " + rootName ); //NOI18N
            return;
        }
        DataObject[] children = folder.getChildren();
        if( null == children ) {
            Logger.getLogger(Tutorials.class.getName()).log(Level.INFO,
                    "Start page content not found: " + "DataObject: " + rootName ); //NOI18N
            return;
        }

        for( int i=0; i<children.length; i++ ) {
            row = addLink( row, children[i] );
        }

        WebLink link = new WebLink(BundleSupport.getLabel("AllOnlineDocs"), BundleSupport.getURL("AllOnlineDocs"), Utils.getLinkColor(), false); //NOI18N
        link.setFont( link.getFont().deriveFont( Font.BOLD ) );
        add( link, new GridBagConstraints(0, row++, 1, 1, 0.0, 0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(20,0,0,0), 0, 0 ) );

        add( new JLabel(), new GridBagConstraints(0, row++, 1, 1, 0.0, 1.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.VERTICAL, new Insets(0,0,0,0), 0, 0 ) );
    }

    private int addLink( int row, DataObject dob ) {
        Action action = extractAction( dob );
        if( null != action ) {
            JPanel panel = new JPanel( new GridBagLayout() );
            panel.setOpaque(false);
            ActionButton lb = new ActionButton( action, Utils.getUrlString( dob ),
                    Utils.getLinkColor(), false, dob.getPrimaryFile().getPath() );
            panel.add( lb, new GridBagConstraints(1,0,1,3,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.NONE,new Insets(0,0,0,0),0,0) );
            lb.setFont( BUTTON_FONT );
            
            panel.add( new JLabel(), 
                    new GridBagConstraints(2,0,1,3,1.0,0.0,GridBagConstraints.WEST,GridBagConstraints.BOTH,new Insets(0,0,0,0),0,0) );
            
            lb.getAccessibleContext().setAccessibleName( lb.getText() );
            //TODO fix acn
            lb.getAccessibleContext().setAccessibleDescription( 
                    BundleSupport.getAccessibilityDescription( "GettingStarted", lb.getText() ) ); //NOI18N
            add( panel, new GridBagConstraints( 0,row++,1,1,1.0,0.0,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0,0,7,0), 0, 0 ) );
        }
        return row;
    }

    private Action extractAction( DataObject dob ) {
        OpenCookie oc = dob.getCookie( OpenCookie.class );
        if( null != oc )
            return new LinkAction( dob );

        InstanceCookie.Of instCookie = dob.getCookie(InstanceCookie.Of.class);
        if( null != instCookie && instCookie.instanceOf( Action.class ) ) {
            try {
                Action res = (Action) instCookie.instanceCreate();
                if( null != res ) {
                    res.putValue(Action.NAME, dob.getNodeDelegate().getDisplayName() );
                }
                return res;
            } catch( Exception e ) {
                Logger.getLogger(SampleProjectAction.class.getName()).log( Level.INFO, null, e );
            }
        }
        return null;
    }

    private static class LinkAction extends AbstractAction {
        private DataObject dob;
        public LinkAction( DataObject dob ) {
            super( dob.getNodeDelegate().getDisplayName() );
            this.dob = dob;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OpenCookie oc = dob.getCookie( OpenCookie.class );
            if( null != oc )
                oc.open();
        }
    }
}
