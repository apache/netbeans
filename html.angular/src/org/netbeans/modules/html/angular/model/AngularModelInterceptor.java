/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.angular.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.html.angular.AngularDoc;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.javascript2.model.spi.ModelInterceptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Roman Svitanic
 */
@NbBundle.Messages({
    "LBL_Angular=AngularJS",
    "LBL_DocumentationDownload=Loading AngularJS Documentation",})
@ModelInterceptor.Registration(priority = 300)
public class AngularModelInterceptor implements ModelInterceptor {

    private static final Logger LOGGER = Logger.getLogger(AngularModelInterceptor.class.getName());

    // for unit testing
    @SuppressWarnings("PackageVisibleField")
    static boolean disabled = false;

    @Override
    public Collection<JsObject> interceptGlobal(ModelElementFactory factory, FileObject fo) {
        if (disabled) {
            return Collections.emptySet();
        }
        InputStream is = getClass().getClassLoader().getResourceAsStream(
                "org/netbeans/modules/html/angular/model/resources/angular-1.3.6.model"); // NOI18N
        try {
            final JsObject angularObject = factory.loadGlobalObject(
                    is, Bundle.LBL_Angular(), new URL("https://docs.angularjs.org/api/ng")); // NOI18N

            RequestProcessor.getDefault().execute(new Runnable() {

                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    ProgressHandle progress = ProgressHandle.createHandle(Bundle.LBL_DocumentationDownload());
                    progress.start(angularObject.getProperty("angular").getProperties().entrySet().size());
                    int loaded = 0;
                    try {
                        for (Entry<String, ? extends JsObject> prop : angularObject.getProperty("angular").getProperties().entrySet()) { // NOI18N
                            AngularDoc.FunctionDocUrl fdoc = new AngularDoc.FunctionDocUrl(prop.getValue().getFullyQualifiedName());
                            String docContent = AngularDoc.getDefault().getFunctionDocumentation(fdoc);
                            if (docContent != null) {
                                    prop.getValue().setDocumentation(Documentation.create(docContent, new URL(fdoc.getDocumentationUrl())));
                            }
                            progress.progress(++loaded);
                        }
                    } catch (MalformedURLException ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                    }
                    progress.finish();
                    long end = System.currentTimeMillis();
                    LOGGER.log(Level.FINE, "Loading of AngularJS documentation took: {0} ms.", (end - start)); //NOI18N
                }
            });
            return Collections.<JsObject>singleton(angularObject);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return Collections.emptySet();
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
    }

}
