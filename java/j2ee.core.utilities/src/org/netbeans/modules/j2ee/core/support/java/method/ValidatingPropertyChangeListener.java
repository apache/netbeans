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

package org.netbeans.modules.j2ee.core.support.java.method;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Adamek
 */
public final class ValidatingPropertyChangeListener implements PropertyChangeListener {
    
    private final MethodCustomizerPanel panel;
    private final NotifyDescriptor notifyDescriptor;
    private final NotificationLineSupport statusLine;
    private final boolean checkInterfaces;
    private final Collection<MethodModel> existingMethods;
    private final Set<String> existingMethodsNames; // just for faster validation, if such name exists, more detailed validation follows
    private final String prefix;
    
    public ValidatingPropertyChangeListener(MethodCustomizerPanel panel, NotifyDescriptor notifyDescriptor, Collection<MethodModel> existingMethods, String prefix) {
        this.panel = panel;
        this.notifyDescriptor = notifyDescriptor;
        statusLine = notifyDescriptor.createNotificationLineSupport();
        this.checkInterfaces = panel.supportsInterfacesChecking();
        this.existingMethods = existingMethods;
        this.existingMethodsNames = new HashSet<String>();
        for (MethodModel methodModel : existingMethods) {
            existingMethodsNames.add(methodModel.getName());
        }
        this.prefix = prefix;
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        validate();
    }
    
    // protected for testing
    protected boolean validate() {
        statusLine.clearMessages();
        notifyDescriptor.setValid(false);

        // method name
        String name = panel.getMethodName();
        if (!Utilities.isJavaIdentifier(name)) {
            statusLine.setErrorMessage(NbBundle.getMessage(ValidatingPropertyChangeListener.class, "ERROR_nameNonJavaIdentifier"));  // NOI18N
            return false;
        }
        // valid method name prefix
        if (prefix != null) {
            if (!name.startsWith(prefix)) {
                statusLine.setErrorMessage(NbBundle.getMessage(ValidatingPropertyChangeListener.class, "ERROR_wrongMethodPrefix", prefix));  // NOI18N
                return false;
            }
        }
        // return type
        String returnType = panel.getReturnType();
        if ("".equals(returnType)) {  // NOI18N
            statusLine.setErrorMessage(NbBundle.getMessage(ValidatingPropertyChangeListener.class, "ERROR_returnTypeInvalid"));  // NOI18N
            return false;
        }
        // interfaces
        if (checkInterfaces) {
            boolean local = panel.hasLocal();
            boolean remote = panel.hasRemote();
            boolean allowsNoInterface = panel.allowsNoInterface();
            if (!allowsNoInterface && !local && !remote) {
                statusLine.setErrorMessage(NbBundle.getMessage(ValidatingPropertyChangeListener.class, "ERROR_selectSomeInterface"));  // NOI18N
                return false;
            }
            if (local && remote) {
                statusLine.setWarningMessage(NbBundle.getMessage(ValidatingPropertyChangeListener.class, "LBL_commonImplForBothInterfaces"));  // NOI18N
            }
        }
        // existing methods
        if (existingMethodsNames.contains(name)) {
            List<MethodModel.Variable> proposedParams = panel.getParameters();
            for (MethodModel methodModel : existingMethods) {
                if (sameParams(proposedParams, methodModel.getParameters())) {
                    statusLine.setErrorMessage(NbBundle.getMessage(ValidatingPropertyChangeListener.class, "ERROR_methodExists"));  // NOI18N
                    return false;
                }
            }
        }
        // method parameters
        List<MethodModel.Variable> params = panel.getParameters();
        for (MethodModel.Variable param : params) {
            String parName = param.getName();
            int count = 0;
            for (MethodModel.Variable par : params) {
                if (parName.equals(par.getName())) {
                    count++;
                }
            }
            if (count > 1) {
                statusLine.setErrorMessage(NbBundle.getMessage(ValidatingPropertyChangeListener.class, "ERROR_duplicateParameterName", parName));  // NOI18N
                return false;
            }
        }

        notifyDescriptor.setValid(true);
        return true;
    }
    
    private boolean sameParams(List<MethodModel.Variable> proposedParams, List<MethodModel.Variable> existingParams) {
        if (existingParams.size() == proposedParams.size()) {
            for (int i = 0; i < existingParams.size(); i++) {
                String existingType = existingParams.get(i).getType();
                String proposedType = proposedParams.get(i).getType();
                int existingIndex = existingType.lastIndexOf('.');
                int proposedIndex = proposedType.lastIndexOf('.');
                // try to get right result even if comparing String and java.lang.String; compare only simple names
                if (existingIndex == -1 && proposedIndex != -1) {
                    proposedType = proposedType.substring(proposedIndex + 1);
                } else if (existingIndex != -1 && proposedIndex == -1) {
                    existingType = existingType.substring(existingIndex + 1);
                }
                if (!existingType.equals(proposedType)) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
    
}
