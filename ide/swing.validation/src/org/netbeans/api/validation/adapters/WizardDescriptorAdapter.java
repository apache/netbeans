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
package org.netbeans.api.validation.adapters;

import org.netbeans.validation.api.Problem;
import org.netbeans.validation.api.ui.ValidationUI;
import org.openide.WizardDescriptor;

/**
 *
 * @author Tim Boudreau
 */
public class WizardDescriptorAdapter implements ValidationUI {
    private final WizardDescriptor wiz;
    private final Type type;

    /**
     * 
     * @since 1.22
     */
    public enum Type {
        VALID, MESSAGE, VALID_AND_MESSAGE
    }
    public WizardDescriptorAdapter(WizardDescriptor d) {
        this(d, Type.VALID_AND_MESSAGE);
    }
    
    /**
     * allow to decide what ui elements should be affected, button enablement or error message or both.
     * @since 1.22
     */ 
    public WizardDescriptorAdapter(WizardDescriptor d, Type type) {
        this.wiz = d;
        this.type = type;
    }

    @Override
    public void clearProblem() {
        if (type != Type.VALID) {
            wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        }
        if (type != Type.MESSAGE) {
            wiz.setValid(true);
        }
    }

    @Override
    public void showProblem(Problem p) {
        switch (p.severity()) {
            case INFO :
                if (type != Type.VALID) {
                    wiz.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, p.getMessage());
                }
                if (type != Type.MESSAGE) {
                    wiz.setValid(true);
                }
                break;
            case WARNING :
                if (type != Type.VALID) {
                    wiz.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, p.getMessage());
                }
                if (type != Type.MESSAGE) {
                    wiz.setValid(true);
                }
                break;
            case FATAL :
                if (type != Type.VALID) {
                    wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, p.getMessage());
                }
                if (type != Type.MESSAGE) {
                    wiz.setValid(false);
                }
                break;
        }
    }
}
