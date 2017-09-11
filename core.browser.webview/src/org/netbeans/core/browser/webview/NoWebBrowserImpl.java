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
