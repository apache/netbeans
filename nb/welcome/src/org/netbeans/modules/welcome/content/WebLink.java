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
import org.openide.awt.StatusDisplayer;

/**
 *
 * @author S. Aubrecht
 */
public class WebLink extends LinkButton {

    private String url;

    /** Creates a new instance of WebLink */
    public WebLink( String key, boolean showBorder ) {
        this( BundleSupport.getLabel( key ), BundleSupport.getURL( key ), showBorder );
    }

    public WebLink( String label, String url, boolean showBorder ) {
        super( label, showBorder, url );
        this.url = url;

        getAccessibleContext().setAccessibleName(
                BundleSupport.getAccessibilityName( "WebLink", label ) ); //NOI18N
        getAccessibleContext().setAccessibleDescription(
                BundleSupport.getAccessibilityDescription( "WebLink", url ) ); //NOI18N

        setUsageTrackingId(url);
    }

    public WebLink( String label, String url, Color foreground, boolean showBorder ) {
        super( label, foreground, showBorder, url );
        this.url = url;

        setUsageTrackingId(url);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        logUsage();
        Utils.showURL( url );
    }
    
    @Override
    protected void onMouseExited(MouseEvent e) {
        super.onMouseExited( e );
        StatusDisplayer.getDefault().setStatusText( "" );
    }

    @Override
    protected void onMouseEntered(MouseEvent e) {
        super.onMouseEntered( e );
        StatusDisplayer.getDefault().setStatusText( url );
    }
}

