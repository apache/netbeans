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
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cloud.oracle.assets.AbstractStep;
import org.netbeans.modules.cloud.oracle.assets.Steps;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Petrovic
 */
@NbBundle.Messages({
    "ItemCreationDecission=Choose whether to create new or to add existing resource",
})
public class ItemCreationDecisionStep extends AbstractStep<String> {

    public static final String CREATE_NEW_OPTION = "Create new";
    private static final String ADD_EXISTING_OPTION = "Add existing";
    
    private List<NotifyDescriptor.QuickPick.Item> showedOptions;
    private String selected;
    private String itemType;
    
    public ItemCreationDecisionStep() {
    }
    
    public ItemCreationDecisionStep(String itemType) {
        this.itemType = itemType;
    }

    @Override
    public void prepare(ProgressHandle h, Steps.Values values) {
        showedOptions = new ArrayList<>();
        showedOptions.add(new NotifyDescriptor.QuickPick.Item(ADD_EXISTING_OPTION, ""));
        if (itemType == null) {
            itemType = values.getValueForStep(ItemTypeStep.class);
        }
        
        if (creationEnabled(itemType)) {
            showedOptions.add(new NotifyDescriptor.QuickPick.Item(CREATE_NEW_OPTION, ""));
        }
    }

    public String getItemType() {
        return itemType;
    }

    private static boolean creationEnabled(String itemType) {
        return "ContainerRepository".equals(itemType);
    }
    
    @Override
    public NotifyDescriptor createInput() {
        return new NotifyDescriptor.QuickPick(Bundle.ItemCreationDecission(), Bundle.ItemCreationDecission(), showedOptions, false);    
    }

    @Override
    public boolean onlyOneChoice() {
        return showedOptions.size() == 1;
    }

    @Override
    public void setValue(String selected) {
        if (!CREATE_NEW_OPTION.equals(selected) && !ADD_EXISTING_OPTION.equals(selected))
            throw new IllegalArgumentException("Invalid argument: " + selected);
        
        this.selected = selected;
    }

    @Override
    public String getValue() {
        return selected;
    }
    
}
