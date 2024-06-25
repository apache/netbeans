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
 * @author honza
 */
@NbBundle.Messages({
    "Password=Enter password for Database user {0}",
})
public class PasswordStep extends AbstractStep<String> {
    
    private boolean ask;
    private String password;
    private final String username;

    public PasswordStep(String password, String username) {
        this.password = password;
        this.username = username;
    }

    @Override
    public void prepare(ProgressHandle h, Values values) {
        ask = password == null || password.isEmpty();
    }

    @Override
    public NotifyDescriptor createInput() {
        return new NotifyDescriptor.PasswordLine("DEFAULT", Bundle.Password(username)); //NOI18N
    }

    @Override
    public boolean onlyOneChoice() {
        return !ask;
    }

    @Override
    public void setValue(String password) {
        this.password = password;
    }

    @Override
    public String getValue() {
        return password;
    }
    
}
