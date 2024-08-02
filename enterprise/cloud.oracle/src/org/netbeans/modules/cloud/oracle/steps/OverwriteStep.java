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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cloud.oracle.assets.AbstractStep;
import org.netbeans.modules.cloud.oracle.assets.Steps.Values;
import org.netbeans.modules.cloud.oracle.vault.SecretItem;
import org.netbeans.modules.cloud.oracle.vault.SecretNode;
import org.netbeans.modules.cloud.oracle.vault.VaultItem;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "SecretExists=Secrets with name {0} already exists",
    "Cancel=Cancel",
    "AddVersion=Add new versions",
    "DatasourceEmpty=Datasource name cannot be empty"
})
public class OverwriteStep extends AbstractStep<Boolean> {
    
    private Set<String> dsNames;
    private String choice;
    private Lookup lookup;
    private String dsName;
    private static final Pattern p = Pattern.compile("[A-Z]*_([a-zA-Z0-9]*)_[A-Z]*"); //NOI18N
    
    @Override
    public void prepare(ProgressHandle h, Values values) {
        dsName = values.getValueForStep(DatasourceNameStep.class);
        VaultItem vault = values.getValueForStep(VaultStep.class);
        if (dsName == null || dsName.isEmpty() || vault == null) {
            return;
        }
        List<SecretItem> secrets = SecretNode.getSecrets().apply(vault);
        this.dsNames = secrets.stream().map(s -> extractDatasourceName(s.getName())).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public NotifyDescriptor createInput() {
        if (dsName == null || dsName.isEmpty()) {
            return new NotifyDescriptor.QuickPick("", Bundle.DatasourceEmpty(), Collections.emptyList(), false);
        }
        List<NotifyDescriptor.QuickPick.Item> yesNo = new ArrayList();
        yesNo.add(new NotifyDescriptor.QuickPick.Item(Bundle.AddVersion(), ""));
        yesNo.add(new NotifyDescriptor.QuickPick.Item(Bundle.Cancel(), ""));
        return new NotifyDescriptor.QuickPick("", Bundle.SecretExists(dsName), yesNo, false);
    }

    @Override
    public void setValue(String choice) {
        this.choice = choice;
    }

    @Override
    public Boolean getValue() {
        return Bundle.AddVersion().equals(choice) || onlyOneChoice();
    }

    @Override
    public boolean onlyOneChoice() {
        return dsNames == null || !dsNames.contains(dsName);
    }
    
    protected static String extractDatasourceName(String value) {
        Matcher m = p.matcher(value);
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }
    
}
