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
package org.netbeans.modules.jcef.browser;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.openide.awt.HtmlBrowser;
import org.openide.modules.OnStart;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class JCEFBrowser extends HtmlBrowser.Impl {

    CefSettings settings = new CefSettings();
    final CefBrowser browser;

    public JCEFBrowser() {
        CefApp app = CefApp.getInstance(settings);
        CefClient client = app.createClient();
        browser = client.createBrowser(PROP_URL, false, true);
    }


    @Override
    public Component getComponent() {
        return browser.getUIComponent();
    }

    @Override
    public void reloadDocument() {
        browser.reload();
    }

    @Override
    public void stopLoading() {
        browser.stopLoad();
    }

    @Override
    public void setURL(URL url) {
        browser.loadURL(url.toString());
    }

    @Override
    public URL getURL() {
        try {
            return new URI(browser.getURL()).toURL();
        } catch (URISyntaxException|MalformedURLException ex) {
            return null;
        }
    }

    @Override
    public String getStatusMessage() {
        return "";
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public boolean isForward() {
        return browser.canGoForward();
    }

    @Override
    public void forward() {
        browser.goForward();
    }

    @Override
    public boolean isBackward() {
        return browser.canGoBack();
    }

    @Override
    public void backward() {
        browser.goBack();
    }

    @Override
    public boolean isHistory() {
        return false;
    }

    @Override
    public void showHistory() {

    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {

    }

    @OnStart
    public static final class Startup implements Runnable {

        @Override
        public void run() {
            System.out.println("JCEF Browser Loaded");
        }

    }
}
