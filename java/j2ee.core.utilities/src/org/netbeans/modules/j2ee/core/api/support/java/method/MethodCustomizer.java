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

package org.netbeans.modules.j2ee.core.api.support.java.method;

import java.util.Collection;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.core.support.java.method.MethodCustomizerPanel;
import org.netbeans.modules.j2ee.core.support.java.method.ValidatingPropertyChangeListener;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Martin Adamek
 */
public final class MethodCustomizer {
    
    private final MethodCustomizerPanel panel;
    private final String title;
    private final String prefix;
    private final Collection<MethodModel> existingMethods;
    
    // factory should be used to create instances
    protected MethodCustomizer(String title, MethodModel methodModel, ClasspathInfo cpInfo, boolean hasLocal, boolean hasRemote,
            boolean selectLocal, boolean selectRemote, boolean hasReturnType, String  ejbql, boolean hasFinderCardinality,
            boolean hasExceptions, boolean hasInterfaces, String prefix, Collection<MethodModel> existingMethods) {
        this(title, methodModel, cpInfo, hasLocal, hasRemote, selectLocal, selectRemote, hasReturnType, ejbql, hasFinderCardinality,
            hasExceptions, hasInterfaces, false, prefix, existingMethods);
    }

    protected MethodCustomizer(String title, MethodModel methodModel, ClasspathInfo cpInfo, boolean hasLocal, boolean hasRemote, 
            boolean selectLocal, boolean selectRemote, boolean hasReturnType, String  ejbql, boolean hasFinderCardinality, 
            boolean hasExceptions, boolean hasInterfaces, boolean allowsNoInterface, String prefix, Collection<MethodModel> existingMethods) {
        this.panel = MethodCustomizerPanel.create(methodModel, cpInfo, hasLocal, hasRemote, selectLocal, selectRemote,
                hasReturnType, ejbql, hasFinderCardinality, hasExceptions, hasInterfaces, allowsNoInterface);
        
        // A11Y 
        this.panel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodCustomizerPanel.class, "ACSD_AddMethod")); // NOI18N
               
        this.title = title;
        this.prefix = prefix;
        this.existingMethods = existingMethods;
    }
    
    /**
     * Opens modal window for method customization.
     * 
     * @return true if OK button on customizer was pressed and changes should be written to source files,
     * false if Cancel button on customizer was pressed or if customizer was cancelled in any other way and
     * nothing should be written to source files.
     */
    public boolean customizeMethod() {
        DialogDescriptor notifyDescriptor = new DialogDescriptor(
                panel, title, true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null
                );
        panel.addPropertyChangeListener(new ValidatingPropertyChangeListener(panel, notifyDescriptor, existingMethods, prefix));
        return DialogDisplayer.getDefault().notify(notifyDescriptor) == NotifyDescriptor.OK_OPTION;
    }
    
    public MethodModel getMethodModel() {
        return MethodModel.create(
                panel.getMethodName(),
                panel.getReturnType(),
                panel.getMethodBody(),
                panel.getParameters(),
                panel.getExceptions(),
                panel.getModifiers()
                );
    }
    
    public boolean finderReturnIsSingle() {
        return panel.finderReturnIsSingle();
    }
    
    public boolean publishToLocal() {
        return panel.hasLocal();
    }
    
    public boolean publishToRemote() {
        return panel.hasRemote();
    }
    
    public String getEjbQL() {
        return panel.getEjbql();
        
    }
    
}
