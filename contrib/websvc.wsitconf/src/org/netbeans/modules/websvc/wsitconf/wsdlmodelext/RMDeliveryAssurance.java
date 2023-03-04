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

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.wsitmodelext.rm.AtLeastOnce;
import org.netbeans.modules.websvc.wsitmodelext.rm.ExactlyOnce;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMQName;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.openide.util.NbBundle;

public enum RMDeliveryAssurance {
    AT_LEAST_ONCE {
        public boolean isSet(ConfigVersion cfgVersion, Binding binding) {
            return RMModelHelper.getInstance(cfgVersion).isDeliveryAssurance(binding, this);
        }
        public void set(ConfigVersion cfgVersion, Binding binding) {
            if (ConfigVersion.CONFIG_1_0.equals(cfgVersion)) {
                RMModelHelper.getInstance(cfgVersion).enableAllowDuplicates(binding, true);
            } else {
                RMModelHelper.getInstance(cfgVersion).setDeliveryAssurance(binding, this);
            }
        }
        public QName getQName() {
            return RMQName.ATLEASTONCE.getQName(ConfigVersion.CONFIG_1_3);
        }
        public Class getAssertionClass() {
            return AtLeastOnce.class;
        }
    }, 
    EXACTLY_ONCE {
        public boolean isSet(ConfigVersion cfgVersion, Binding binding) {
            return RMModelHelper.getInstance(cfgVersion).isDeliveryAssurance(binding, this);
        }
        public void set(ConfigVersion cfgVersion, Binding binding) {
            if (ConfigVersion.CONFIG_1_0.equals(cfgVersion)) {
                RMModelHelper.getInstance(cfgVersion).enableAllowDuplicates(binding, false);
            } else {
                RMModelHelper.getInstance(cfgVersion).setDeliveryAssurance(binding, this);
            }
        }
        public QName getQName() {
            return RMQName.EXACTLYONCE.getQName(ConfigVersion.CONFIG_1_3);
        }
        public Class getAssertionClass() {
            return ExactlyOnce.class;
        }
    },
    /*AT_MOST_ONCE*/;
    
    public static final RMDeliveryAssurance getDefault() {
        return EXACTLY_ONCE;
    }

    public static final RMDeliveryAssurance getValue(ConfigVersion cfgVersion, Binding b) {
        if (ConfigVersion.CONFIG_1_0.equals(cfgVersion)) {
            if (RMModelHelper.isAllowDuplicates(b)) {
                return AT_LEAST_ONCE;
            }
            return EXACTLY_ONCE;
        } else {
            for (RMDeliveryAssurance delivery : RMDeliveryAssurance.values()) {
                if (delivery.isSet(cfgVersion, b)) {
                    return delivery;
                }
            }
            return null;
        }
    }
    
    @Override
    public String toString() {
        return NbBundle.getMessage(RMDeliveryAssurance.class, this.name());
    }
    
    public abstract boolean isSet(ConfigVersion cfgVersion, Binding binding);

    public abstract void set(ConfigVersion cfgVersion, Binding binding);
    
    public abstract QName getQName();

    public abstract Class getAssertionClass();
}
