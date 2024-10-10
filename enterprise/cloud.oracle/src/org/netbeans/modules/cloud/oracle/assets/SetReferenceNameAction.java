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
package org.netbeans.modules.cloud.oracle.assets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.netbeans.modules.cloud.oracle.steps.ReferenceNameStep;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.actions.SetReferenceName"
)
@ActionRegistration( 
        displayName = "#ReferenceName", 
        asynchronous = true
)

@NbBundle.Messages({
    "ReferenceName=Set a reference name",
    "ReferenceNameValidationError=Reference Name can contain only letters and numbers",
    "ReferenceNameSame=Reference names must be unique for each resource type."
})
public class SetReferenceNameAction implements ActionListener {
    
    private final OCIItem context;

    public SetReferenceNameAction(OCIItem context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
         setReferenceName();
    }
    
    public CompletableFuture<String> setReferenceName() {
        CompletableFuture future = new CompletableFuture();
        Steps.getDefault().executeMultistep(new ReferenceNameStep(context), Lookup.EMPTY)
                .thenAccept(vals -> {
                    String refName = vals.getValueForStep(ReferenceNameStep.class);
                    CloudAssets.getDefault().setReferenceName(context, refName);
                    future.complete(refName);
                });
        return future;
    }
}
