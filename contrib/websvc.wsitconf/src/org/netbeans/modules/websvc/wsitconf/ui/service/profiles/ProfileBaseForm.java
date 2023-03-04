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
package org.netbeans.modules.websvc.wsitconf.ui.service.profiles;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile;
import org.netbeans.modules.websvc.wsitconf.spi.features.VersionedFeature;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 * Base Profile definition
 *
 * @author Martin Grebac
 */
public abstract class ProfileBaseForm extends JPanel {

    private ProfileBaseForm() {}

    protected boolean inSync = false;

    protected WSDLComponent comp;
    protected SecurityProfile secProfile = null;
    
    protected ConfigVersion cfgVersion = null;
                
    protected ProfileBaseForm(WSDLComponent comp, SecurityProfile secProfile) {
        super();
        this.comp = comp;
        this.secProfile = secProfile;
        if (secProfile instanceof VersionedFeature) {
            this.cfgVersion = ((VersionedFeature)secProfile).getVersion();
        } else {
            this.cfgVersion = PolicyModelHelper.getConfigVersion(comp);
        }
    }
    
    protected void setCombo(JComboBox combo, boolean second) {
        combo.setSelectedIndex(second ? 1 : 0);
    }
            
    protected void setCombo(JComboBox combo, String item) {
        if (item == null) {
            combo.setSelectedIndex(0);
        } else {
            combo.setSelectedItem(item);
        }
    }

    protected void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
        }
    }

    protected void fillKeySize(JComboBox keySizeCombo) {
        fillKeySize(keySizeCombo, false);
    }

    protected void fillKeySize(JComboBox keySizeCombo, boolean publicKey) {
        keySizeCombo.removeAllItems();
        if (publicKey) {
            keySizeCombo.addItem(ComboConstants.NONE);
            keySizeCombo.addItem(ComboConstants.ISSUED_KEYSIZE_1024);
            keySizeCombo.addItem(ComboConstants.ISSUED_KEYSIZE_2048);
            keySizeCombo.addItem(ComboConstants.ISSUED_KEYSIZE_3072);
        } else {
            keySizeCombo.addItem(ComboConstants.ISSUED_KEYSIZE_128);
            keySizeCombo.addItem(ComboConstants.ISSUED_KEYSIZE_192);
            keySizeCombo.addItem(ComboConstants.ISSUED_KEYSIZE_256);
        }
    }
    
    protected void fillWssCombo(JComboBox wssCombo) {
        wssCombo.removeAllItems();
        wssCombo.addItem(ComboConstants.WSS10);
        wssCombo.addItem(ComboConstants.WSS11);    
    }
    
    protected void fillSamlCombo(JComboBox samlVersionCombo) {
        samlVersionCombo.removeAllItems();
        samlVersionCombo.addItem(ComboConstants.SAML_V1010);
        samlVersionCombo.addItem(ComboConstants.SAML_V1011);
        samlVersionCombo.addItem(ComboConstants.SAML_V1110);
        samlVersionCombo.addItem(ComboConstants.SAML_V1111);
        samlVersionCombo.addItem(ComboConstants.SAML_V2011);
    }

    protected void fillLayoutCombo(JComboBox layoutCombo) {
        layoutCombo.removeAllItems();
        layoutCombo.addItem(ComboConstants.STRICT);
        layoutCombo.addItem(ComboConstants.LAX);
        layoutCombo.addItem(ComboConstants.LAXTSFIRST);
        layoutCombo.addItem(ComboConstants.LAXTSLAST);        
    }
    
    protected void fillAlgoSuiteCombo(JComboBox algoSuiteCombo) {
        algoSuiteCombo.removeAllItems();
        algoSuiteCombo.addItem(ComboConstants.BASIC256);
        algoSuiteCombo.addItem(ComboConstants.BASIC192);
        algoSuiteCombo.addItem(ComboConstants.BASIC128);
        algoSuiteCombo.addItem(ComboConstants.TRIPLEDES);
        algoSuiteCombo.addItem(ComboConstants.BASIC256RSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC192RSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC128RSA15);
        algoSuiteCombo.addItem(ComboConstants.TRIPLEDESRSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC256SHA256);
        algoSuiteCombo.addItem(ComboConstants.BASIC192SHA256);
        algoSuiteCombo.addItem(ComboConstants.BASIC128SHA256);
        algoSuiteCombo.addItem(ComboConstants.TRIPLEDESSHA256);
        algoSuiteCombo.addItem(ComboConstants.BASIC256SHA256RSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC192SHA256RSA15);
        algoSuiteCombo.addItem(ComboConstants.BASIC128SHA256RSA15);
        algoSuiteCombo.addItem(ComboConstants.TRIPLEDESSHA256RSA15);        
    }
    
    protected abstract void sync();
    protected abstract void enableDisable();
    protected abstract void setValue(JComponent source);
}
