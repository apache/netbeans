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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cloud.oracle.assets.AbstractStep;
import org.netbeans.modules.cloud.oracle.assets.Steps;
import org.netbeans.modules.cloud.oracle.assets.Steps.Values;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * This step allows the user to select which type of resource will be added.
 * 
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "SelectResourceType=Select Resource Type"
})
public class ItemTypeStep extends AbstractStep<String> {

    private static final Map<String, String> TYPES = new HashMap() {
        {
            put("Databases", Bundle.Databases()); //NOI18N
            put("Bucket", Bundle.Bucket()); //NOI18N 
            put("Vault", Bundle.Vault()); //NOI18N
        }
    };
    private String selected;

    @Override
    public void prepare(ProgressHandle h, Values values) {
    }

    @Override
    public NotifyDescriptor createInput() {
        List<NotifyDescriptor.QuickPick.Item> items = new ArrayList<>(TYPES.size());
        for (Map.Entry<String, String> itemType : TYPES.entrySet()) {
            items.add(new NotifyDescriptor.QuickPick.Item(itemType.getKey(), itemType.getValue()));
        }
        return new NotifyDescriptor.QuickPick(Bundle.SelectResourceType(), Bundle.SelectResourceType(), items, false);
    }

    @Override
    public boolean onlyOneChoice() {
        return false;
    }

    @Override
    public void setValue(String selected) {
        this.selected = selected;
    }

    @Override
    public String getValue() {
        return selected;
    }
    
}
