/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.core.browser.webview.ext;

import org.netbeans.core.browser.api.*;
import java.awt.AWTEvent;
import org.w3c.dom.Node;

/**
 * Browser event implementation.
 * 
 * @author S. Aubrecht
 */
class WebBrowserEventImpl extends WebBrowserEvent {

    private final int type;
    private final WebBrowser browser;
    private final Node node;
    private final AWTEvent event;
    private boolean cancelled = false;
    private final String url;

    public WebBrowserEventImpl( int type, WebBrowser browser, String url ) {
        this.type = type;
        this.browser = browser;
        this.url = url;
        this.event = null;
        this.node = null;
    }

    public WebBrowserEventImpl( int type, WebBrowser browser, AWTEvent event, Node node ) {
        this.type = type;
        this.browser = browser;
        this.event = event;
        this.node = node;
        this.url = null;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public WebBrowser getWebBrowser() {
        return browser;
    }

    @Override
    public String getURL() {
        return null != url ? url : browser.getURL();
    }

    @Override
    public AWTEvent getAWTEvent() {
        return event;
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    boolean isCancelled() {
        return cancelled;
    }
}
