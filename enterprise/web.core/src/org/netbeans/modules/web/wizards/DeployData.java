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
package org.netbeans.modules.web.wizards;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;

/**
 * Generic methods for evaluating the input into the wizards.
 *
 * @author Ana von Klopp
 */
abstract class DeployData {

    private static final Logger LOG = Logger.getLogger(DeployData.class.getName());
    WebApp webApp = null;
    String className = null;
    boolean makeEntry = true;
    FileObject ddObject = null;

    // This is the web app file object
    void setWebApp(FileObject fo) {
        LOG.finer("setWebApp()");

        ddObject = fo;
        if (fo == null) {
            webApp = null;
            return;
        }

        try {
            webApp = DDProvider.getDefault().getDDRoot(fo);
            LOG.finer("webApp = " + webApp);
        } catch (IOException ioex) {
            LOG.log(Level.FINE, "Couldn't get the web app!", ioex);
        } catch (Exception ex) {
            LOG.log(Level.FINE, "Couldn't get the web app!", ex);
        }
    }

    String getClassName() {
        if (className == null) {
            return "";
        }
        return className;
    }

    void setClassName(String name) {
        this.className = name;
    }

    boolean makeEntry() {
        return makeEntry;
    }

    void setMakeEntry(boolean makeEntry) {
        this.makeEntry = makeEntry;
    }

    void writeChanges() throws IOException {
        LOG.finer("writeChanges()"); //NOI18N
        if (webApp == null) {
            return;
        }
        LOG.finer("now writing..."); //NOI18N
        webApp.write(ddObject);
    }

    abstract boolean isValid();
    // This must invoke write changes at the end 

    abstract void createDDEntries();

    abstract String getErrorMessage();

    public static FileObject getWebAppFor(FileObject folder) {
        if (folder == null) {
            return null;
        }
        WebModule webModule = WebModule.getWebModule(folder);
        if (webModule == null) {
            return null;
        }
        return webModule.getDeploymentDescriptor();
    }

    public boolean hasDD() {
        return webApp != null;
    }
}

