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
package org.netbeans.modules.javascript2.knockout.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.spi.ModelElementFactory;
import org.netbeans.modules.javascript2.model.spi.ModelInterceptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hejl
 */
@ModelInterceptor.Registration(priority=200)
public class KnockoutModelInterceptor implements ModelInterceptor {

    private static final Logger LOGGER = Logger.getLogger(KnockoutModelInterceptor.class.getName());

    // for unit testing
    static boolean disabled = false;

    @NbBundle.Messages("label_knockout=Knockout")
    @Override
    public Collection<JsObject> interceptGlobal(ModelElementFactory factory, FileObject fo) {
        if (disabled) {
            return Collections.emptySet();
        }

        InputStream is = getClass().getClassLoader().getResourceAsStream(
                "org/netbeans/modules/javascript2/knockout/model/resources/knockout-3.2.0.model"); // NOI18N
        try {
            return Collections.singleton(factory.loadGlobalObject(is, Bundle.label_knockout(),
                    new URL("http://knockoutjs.com/documentation/introduction.html")));
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
