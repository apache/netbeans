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

package org.netbeans.modules.payara.common.wizards;

import org.netbeans.modules.payara.common.ServerDetails;
import org.netbeans.spi.server.ServerWizardProvider;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Williams
 * @author vince kraemer
 */
public class PayaraWizardProvider implements ServerWizardProvider {

    public static PayaraWizardProvider createEe6() {
        return new PayaraWizardProvider(
                NbBundle.getMessage(PayaraWizardProvider.class,
                        "STR_PF_FAMILY_NAME", new Object[]{}) // NOI18N
        );
    }

    public static PayaraWizardProvider createEe7() {
        return new PayaraWizardProvider(
                NbBundle.getMessage(PayaraWizardProvider.class,
                        "STR_PF_FAMILY_NAME", new Object[]{}) // NOI18N
        );
    }

    public static PayaraWizardProvider createEe8() {
        return new PayaraWizardProvider(
                NbBundle.getMessage(PayaraWizardProvider.class,
                        "STR_PF_FAMILY_NAME", new Object[]{}) // NOI18N
        );
    }
    
    private final String displayName;

    private PayaraWizardProvider(
            String displayName
            ) {
        this.displayName = displayName;
    }

    // ------------------------------------------------------------------------
    // ServerWizardProvider interface implementation
    // ------------------------------------------------------------------------
    @Override
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Creates an iterator for a wizard to instantiate server objects.
     * <p/>
     * @return Server wizard iterator initialized with supported Payara
     * server versions.
     */
    @Override
    public InstantiatingIterator getInstantiatingIterator() {
        return ServerDetails.getInstantiatingIterator();
    }

}
