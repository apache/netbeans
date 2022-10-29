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

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.SequenceSTR;
import org.netbeans.modules.websvc.wsitmodelext.rm.SequenceTransportSecurity;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.openide.util.NbBundle;
 
public enum RMSequenceBinding {
    SECURED_TRANSPORT {
        public QName getQName() {
            return RMQName.SEQUENCETRANSPORTSECURITY.getQName(ConfigVersion.CONFIG_1_3);
        }
        public Class getAssertionClass() {
            return SequenceTransportSecurity.class;
        }
    },
    SECURED_TOKEN {
        public QName getQName() {
            return RMQName.SEQUENCESTR.getQName(ConfigVersion.CONFIG_1_3);
        }
        public Class getAssertionClass() {
            return SequenceSTR.class;
        }
    };
    
    public static final RMSequenceBinding getDefault() {
        return SECURED_TOKEN;
    }

    public static final RMSequenceBinding getValue(ConfigVersion cfgVersion, Binding b) {
        if (!ConfigVersion.CONFIG_1_0.equals(cfgVersion)) {
            if (RMModelHelper.getInstance(cfgVersion).isSequenceBinding(b, SECURED_TRANSPORT)) {
                return SECURED_TRANSPORT;
            }
            if (RMModelHelper.getInstance(cfgVersion).isSequenceBinding(b, SECURED_TOKEN)) {
                return SECURED_TOKEN;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return NbBundle.getMessage(RMSequenceBinding.class, this.name());
    }
    
    public void set(ConfigVersion cfgVersion, Binding binding) {
        RMModelHelper.getInstance(cfgVersion).setSequenceBinding(binding, this);
    }
        
    public abstract QName getQName();
    
    public abstract Class getAssertionClass();
    
}
