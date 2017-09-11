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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.Icon;
import org.openide.awt.StatusDisplayer;

public class ActionButton extends LinkButton {

    private Action action;
    private String urlString;
    private boolean visited = false;

    public ActionButton( Action a, String urlString, boolean showBorder, String usageTrackingId ) {
        this( a, urlString, Utils.getLinkColor(), showBorder, usageTrackingId );
    }

    public ActionButton( Action a, String urlString, Color foreground, boolean showBorder, String usageTrackingId ) {
        super( a.getValue( Action.NAME ).toString(), foreground, showBorder, usageTrackingId );
        this.action = a;
        this.urlString = urlString;
        Object icon = a.getValue( Action.SMALL_ICON );
        if( null != icon && icon instanceof Icon )
            setIcon( (Icon)icon );
        Object tooltip = a.getValue( Action.SHORT_DESCRIPTION );
        if( null != tooltip )
            setToolTipText( tooltip.toString() );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        logUsage();
        if( null != action ) {
            action.actionPerformed( e );
        }
        if( null != urlString )
            visited = true;
    }

    @Override
    protected void onMouseExited(MouseEvent e) {
        super.onMouseExited( e );
        if( null != urlString ) {
            StatusDisplayer.getDefault().setStatusText( "" ); //NOI18N
        }
    }

    @Override
    protected void onMouseEntered(MouseEvent e) {
        super.onMouseEntered( e );
        if( null != urlString ) {
            StatusDisplayer.getDefault().setStatusText( urlString );
        }
    }

    @Override
    protected boolean isVisited() {
        return visited;
    }

    private static final long serialVersionUID = 1L; 
}
