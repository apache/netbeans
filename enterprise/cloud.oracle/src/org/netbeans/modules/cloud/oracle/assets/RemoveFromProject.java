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
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Horvath
 */
@ActionID(
        category = "Tools",
        id = "org.netbeans.modules.cloud.oracle.actions.RemoveFromProject"
)
@ActionRegistration( 
        displayName = "#RemoveFromProject", 
        asynchronous = true
)

@NbBundle.Messages({
    "RemoveFromProject=Remove From Assets"
})
public class RemoveFromProject implements ActionListener {
    
    private final OCIItem context;

    public RemoveFromProject(OCIItem context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CloudAssets.getDefault().removeItem(context);
    }

    
}
