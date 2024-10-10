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

import static org.netbeans.modules.cloud.oracle.NotificationUtils.showMessage;
import org.netbeans.modules.cloud.oracle.assets.AbstractStep;
import org.netbeans.modules.cloud.oracle.assets.CloudAssets;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Dusan Petrovic
 */
@NbBundle.Messages({
    "ReferenceName=Set a reference name",
    "ReferenceNameValidationError=Reference Name can contain only letters and numbers",
    "ReferenceNameExist=Reference names must be unique for each resource type."
})
public class ReferenceNameStep extends AbstractStep<String> {

    private String referenceName;
    private String contextPath;
    private OCIItem context;
    
    public ReferenceNameStep(OCIItem context) {
        this.context = context;
    }
    
    public ReferenceNameStep(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public NotifyDescriptor createInput() {
        NotifyDescriptor.InputLine ci = new NotifyDescriptor.InputLine(Bundle.ReferenceName(), Bundle.ReferenceName());
        if (context != null) {
            String oldRefName = CloudAssets.getDefault().getReferenceName(context);
            if (oldRefName != null) {
                ci.setInputText(oldRefName);
            }
        }
        return ci;
    }

    @Override
    public boolean onlyOneChoice() {
        return false;
    }

    @Override
    public void setValue(String value) {
        if (validateName(value)) {
            this.referenceName = value;
        }
    }

    @Override
    public String getValue() {
        return this.referenceName;
    }

    private boolean validateName(String value) {
        Parameters.notNull("Reference name", value); //NOI18N

        if (!value.matches("[a-zA-Z0-9]+")) { //NOI18N
            showMessage(Bundle.ReferenceNameValidationError());
            return false;
        } 
        if (referenceNameExist(value)) {
            showMessage(Bundle.ReferenceNameExist());
        }

        return true;
    }

    private boolean referenceNameExist(String value) {
        String itemContextPath = context == null ? contextPath : context.getKey().getPath();
        return CloudAssets.getDefault().referenceNameExist(itemContextPath, value);
    }
    
}
