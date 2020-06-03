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

package org.netbeans.modules.cnd.modelui.trace;

import java.util.Collection;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.repository.support.RepositoryTestUtils;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;

/**
 *
 */
public class TestRepositoryDistributionAction extends TestProjectActionBase {

    @Override
    protected void performAction(Collection<CsmProject> projects) {
        if (projects != null) {
            for(CsmProject project : projects) {
                System.err.println("Project "+project);
                RepositoryTestUtils.debugDistribution();
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_TestRepositoryDistribution"); // NOI18N
    }

    public static Action getInstance() {
        return SharedClassObject.findObject(TestRepositoryDistributionAction.class, true);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
