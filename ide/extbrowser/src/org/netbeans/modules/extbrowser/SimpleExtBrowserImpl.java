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

package org.netbeans.modules.extbrowser;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.execution.NbProcessDescriptor;

/** Class that implements browsing.
 *  It starts new process whenever it is asked to display URL.
 */
public class SimpleExtBrowserImpl extends ExtBrowserImpl {

    public SimpleExtBrowserImpl(ExtWebBrowser extBrowserFactory) {
        super();
        this.extBrowserFactory = extBrowserFactory;
        if (ExtWebBrowser.getEM().isLoggable(Level.FINE)) {
            ExtWebBrowser.getEM().log(Level.FINE, "SimpleExtBrowserImpl created from factory: " + extBrowserFactory);    // NOI18N
        }
    }

    /** Given URL is displayed.
      *  Configured process is started to satisfy this request.
      */
    @Override
    protected void loadURLInBrowserInternal(URL url) {
        assert !EventQueue.isDispatchThread();
        if (url == null) {
            return;
        }

        NbProcessDescriptor np = extBrowserFactory.getBrowserExecutable();
        try {
            url = URLUtil.createExternalURL(url, false);
            URI uri = url.toURI();

            if (np != null) {
                np.exec(new SimpleExtBrowser.BrowserFormat((uri == null)? "": uri.toASCIIString())); // NOI18N
            }
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            logInfo(ex);
            BrowserUtils.notifyMissingBrowser(np.getProcessName());
        }
    }

    private static void logInfo(Exception ex) {
        Logger logger = Logger.getLogger(SimpleExtBrowserImpl.class.getName());
        logger.log(Level.INFO, null, ex);
    }
}
