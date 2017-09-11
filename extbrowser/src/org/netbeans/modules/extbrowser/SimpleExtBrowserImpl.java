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
