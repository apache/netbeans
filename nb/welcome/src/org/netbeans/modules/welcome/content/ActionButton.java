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
        if(icon instanceof Icon)
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
