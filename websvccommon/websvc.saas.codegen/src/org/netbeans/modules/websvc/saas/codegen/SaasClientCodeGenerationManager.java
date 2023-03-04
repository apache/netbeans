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
package org.netbeans.modules.websvc.saas.codegen;

import java.io.IOException;
import java.util.Collection;
import javax.swing.text.Document;
import org.netbeans.modules.websvc.saas.codegen.spi.SaasClientCodeGenerationProvider;
import org.netbeans.modules.websvc.saas.model.SaasMethod;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Manager to lookup all Code Generation Providers.
 *
 * @author nam
 */
public abstract class SaasClientCodeGenerationManager {

    public static Collection<? extends SaasClientCodeGenerationProvider> providers = null;

    public static boolean canAccept(SaasMethod m, Document doc) {
        return lookup(m, doc) != null;
    }

    public static SaasClientCodeGenerationProvider lookup(SaasMethod m, Document doc) {
        if (providers == null) {
            providers = Lookup.getDefault().lookupAll(SaasClientCodeGenerationProvider.class);
        }
        SaasClientCodeGenerationProvider theProvider = null;
        for (SaasClientCodeGenerationProvider provider : providers) {
            if (provider.canAccept(m, doc)) {
                if (theProvider == null) {
                    theProvider = provider;
                } else if (provider.getPrecedence() > theProvider.getPrecedence()) {
                    theProvider = provider;
                }
            }
        }
        
        if (theProvider != null) {
            try {
                theProvider.init(m, doc);
                return theProvider;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }
}
