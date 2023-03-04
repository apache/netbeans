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
package org.netbeans.core.browser.webview;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Map;
import javax.swing.JLabel;
import org.netbeans.core.browser.api.WebBrowser;
import org.netbeans.core.browser.api.WebBrowserListener;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;

/**
 * Embedded browser implementation to use when the actual browser creation failed.
 * 
 * @author S. Aubrecht
 */
class NoWebBrowserImpl extends WebBrowser {

    private final Component component;
    private String url;
    
    public NoWebBrowserImpl(String cause) {
        JLabel lbl = new JLabel(NbBundle.getMessage(NoWebBrowserImpl.class, "Err_CannotCreateBrowser", cause));
        lbl.setEnabled( false );
        lbl.setHorizontalAlignment( JLabel.CENTER );
        lbl.setVerticalAlignment( JLabel.CENTER );
        component = lbl;
    }

    public NoWebBrowserImpl(Component content) {
        this.component = content;
    }
    
    @Override
    public Component getComponent() {
        return component;
    }

    @Override
    public void reloadDocument() {
        //NOOP
    }

    @Override
    public void stopLoading() {
        //NOOP
    }

    @Override
    public void setURL( String url ) {
        this.url = url;
    }

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public String getStatusMessage() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public boolean isForward() {
        return false;
    }

    @Override
    public void forward() {
        //NOOP
    }

    @Override
    public boolean isBackward() {
        return false;
    }

    @Override
    public void backward() {
        //NOOP
    }

    @Override
    public boolean isHistory() {
        return false;
    }

    @Override
    public void showHistory() {
        //NOOP
    }

    @Override
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        //NOOP
    }

    @Override
    public void removePropertyChangeListener( PropertyChangeListener l ) {
        //NOOP
    }

    @Override
    public void setContent( String content ) {
        //NOOP
    }

    @Override
    public Document getDocument() {
        return null;
    }

    @Override
    public void dispose() {
        //NOOP
    }

    @Override
    public void addWebBrowserListener( WebBrowserListener l ) {
        //NOOP
    }

    @Override
    public void removeWebBrowserListener( WebBrowserListener l ) {
        //NOOP
    }

    @Override
    public Map<String, String> getCookie( String domain, String name, String path ) {
        return Collections.emptyMap();
    }

    @Override
    public void deleteCookie( String domain, String name, String path ) {
        //NOOP
    }

    @Override
    public void addCookie( Map<String, String> cookie ) {
        //NOOP
    }

    @Override
    public Object executeJavaScript( String script ) {
        return null;
    }

    @Override
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }
    
}
