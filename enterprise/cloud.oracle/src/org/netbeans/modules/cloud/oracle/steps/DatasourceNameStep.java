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
package org.netbeans.modules.cloud.oracle.steps;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cloud.oracle.assets.AbstractStep;
import org.netbeans.modules.cloud.oracle.assets.Steps.Values;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "DatasourceName=Datasource Name",
})
public class DatasourceNameStep extends AbstractStep<String> {
    private String selected = null;

    public DatasourceNameStep() {
    }
    
    @Override
    public void prepare(ProgressHandle h, Values values) {
    }

    @Override
    public NotifyDescriptor createInput() {
        return new NotifyDescriptor.InputLine(selected == null ? "" : "DEFAULT", Bundle.DatasourceName()); //NOI18N
    }

    @Override
    public void setValue(String selected) {
        this.selected = selected;
    }

    @Override
    public String getValue() {
        return selected;
    }

    @Override
    public boolean onlyOneChoice() {
        return false;
    }
    
}
