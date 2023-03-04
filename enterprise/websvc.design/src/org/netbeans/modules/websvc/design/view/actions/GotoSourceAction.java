/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.websvc.design.view.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.modules.websvc.design.javamodel.MethodModel;
import org.netbeans.modules.websvc.design.multiview.MultiViewSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 *
 * @author Ajit Bhate
 */
public class GotoSourceAction extends AbstractAction {
    
    private MultiViewSupport support;
    private MethodModel method;
    /**
     * Creates a new instance of AddOperationAction
     * @param implementationClass fileobject of service implementation class
     */
    public GotoSourceAction(MethodModel method, FileObject implementationClass) {
        super(getName());
        putValue(SHORT_DESCRIPTION, NbBundle.getMessage(GotoSourceAction.class, "Hint_GotoSource"));
        this.method = method;
        try {
            DataObject dobj = DataObject.find(implementationClass);
            support = dobj.getCookie(MultiViewSupport.class);
        } catch (DataObjectNotFoundException ex) {
        }
        setEnabled(support!=null);
    }
    
    private static String getName() {
        return NbBundle.getMessage(GotoSourceAction.class, "LBL_GotoSource");
    }
    
    public void actionPerformed(ActionEvent arg0) {
        support.view(MultiViewSupport.View.SOURCE, method.getMethodHandle());
    }
}


