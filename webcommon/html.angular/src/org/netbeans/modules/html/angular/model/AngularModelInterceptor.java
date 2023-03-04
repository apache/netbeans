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
