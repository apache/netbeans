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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
