/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    
    public final static RMDeliveryAssurance getDefault() {
        return EXACTLY_ONCE;
    }

    public final static RMDeliveryAssurance getValue(ConfigVersion cfgVersion, Binding b) {
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
