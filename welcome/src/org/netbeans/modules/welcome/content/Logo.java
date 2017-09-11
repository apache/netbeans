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

package org.netbeans.modules.welcome.content;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.awt.StatusDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 */
public class Logo extends JPanel implements Constants, MouseListener {

    private String url;

    public static Logo createOracleLogo() {
        return new Logo( ORACLE_LOGO_IMAGE, BundleSupport.getURL( "OracleLogo" ) ); // NOI18N
    }

    public static Logo createJavaLogo() {
        return new Logo( JAVA_LOGO_IMAGE, BundleSupport.getURL( "JavaLogo" ) ); // NOI18N
    }

    public static Logo createNetBeansLogo() {
        return new Logo( NETBEANS_LOGO_IMAGE, BundleSupport.getURL( "NetBeansLogo" ) ); // NOI18N
    }

    /** Creates a new instance of RecentProjects */
    public Logo( String img, String url ) {
        super( new BorderLayout() );
        Icon image = new ImageIcon( ImageUtilities.loadImage(img, true) );
        JLabel label = new JLabel( image );
        label.setBorder( BorderFactory.createEmptyBorder() );
        label.setOpaque( false );
        label.addMouseListener( this );
        setOpaque( false );
        add( label, BorderLayout.CENTER );
        setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) );
        this.url = url;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        LogRecord rec = new LogRecord(Level.INFO, "USG_START_PAGE_LINK"); //NOI18N
        rec.setParameters(new Object[] {url} );
        rec.setLoggerName(Constants.USAGE_LOGGER.getName());
        rec.setResourceBundle(NbBundle.getBundle(BundleSupport.BUNDLE_NAME));
        rec.setResourceBundleName(BundleSupport.BUNDLE_NAME);

        Constants.USAGE_LOGGER.log(rec);
        Utils.showURL( url );
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        StatusDisplayer.getDefault().setStatusText( url );
    }

    @Override
    public void mouseExited(MouseEvent e) {
        StatusDisplayer.getDefault().setStatusText( null );
    }
}
