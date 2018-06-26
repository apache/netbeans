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

