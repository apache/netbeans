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

import java.util.HashMap;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitmodelext.tx.ATAlwaysCapability;
import org.netbeans.modules.websvc.wsitmodelext.tx.ATAssertion;
import org.netbeans.modules.websvc.wsitmodelext.tx.TxQName;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public class TxModelHelper {
    
    private static HashMap<ConfigVersion, TxModelHelper> instances =
            new HashMap<ConfigVersion, TxModelHelper>();
    
    private ConfigVersion configVersion = ConfigVersion.getDefault();

    /**
     * Creates a new instance of TxModelHelper
     */
    private TxModelHelper(ConfigVersion configVersion) {
        this.configVersion = configVersion;
    }

    public static final synchronized TxModelHelper getInstance(ConfigVersion configVersion) {
        TxModelHelper instance = instances.get(configVersion);
        if (instance == null) {
            instance = new TxModelHelper(configVersion);
            instances.put(configVersion, instance);
        }
        return instance;
    }
    
    private static ATAssertion getATAssertion(Policy p) {
        return (ATAssertion) PolicyModelHelper.getTopLevelElement(p, ATAssertion.class,false);
    }
    
    private static ATAlwaysCapability getATAlwaysAssertion(Policy p) {
        return (ATAlwaysCapability) PolicyModelHelper.getTopLevelElement(p, ATAlwaysCapability.class,false);
    }

    /** Tx Value should be one of ComboConstants.TX_*
     */
    public void setTx(BindingOperation bop, Node node, String txValue) {
        
//        String txAnnot = getTxFromAnnotation(bop, node);
//        String txConfig = getTxFromConfig(bop);
        
//        if (WSITModelSupport.isServiceFromWsdl(node) || // do not care about annotation if WS from WSDL
//            (ComboConstants.TX_NOTSUPPORTED.equals(txConfig) && (txAnnot == null)) || ( // Nothing is set
//           !ComboConstants.TX_NOTSUPPORTED.equals(txConfig))) {  // Something is set in wsit config
            setTxInConfig(bop, txValue);
//        } else {
//            setTxInAnnotation(bop, node, txValue);
//        }
    }

//    private static void setTxInAnnotation(BindingOperation bop, Node node, String txValue) {
//        Method m = JMIUtils.getMethod(node, bop.getName());
//        
//        VariableAccess va = getTxAnnotationVariable(m);
//        if (va != null) {
//            va.setName(txValue);
//        }
//        
//    }
    
    private void setTxInConfig(BindingOperation bop, String txValue) {
        WSDLModel model = bop.getModel();
        Policy p = PolicyModelHelper.getPolicyForElement(bop);
        
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {

            ATAssertion tx = getATAssertion(p);
            ATAlwaysCapability txAlways = getATAlwaysAssertion(p);
            
            // first remove what has been there already
            if (tx != null) {
                tx.getParent().removeExtensibilityElement(tx);
            }
            if (txAlways != null) {
                txAlways.getParent().removeExtensibilityElement(txAlways);
            }

            // now add what is required
            WSDLComponentFactory wcf = model.getFactory();
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
            All all = pmh.createPolicy(bop, false);
            
            if ((ComboConstants.TX_NEVER.equals(txValue)) || 
                (ComboConstants.TX_NOTSUPPORTED.equals(txValue))) {
                    PolicyModelHelper.cleanPolicies(bop);
                    return;
            }
            
            if (ComboConstants.TX_MANDATORY.equals(txValue)) {
                tx = (ATAssertion)wcf.create(all, TxQName.ATASSERTION.getQName());
                all.addExtensibilityElement(tx);
            }

            if (ComboConstants.TX_REQUIRED.equals(txValue)) {
                tx = (ATAssertion)wcf.create(all, TxQName.ATASSERTION.getQName());
                tx.setOptional(true, configVersion);
                all.addExtensibilityElement(tx);
                txAlways = (ATAlwaysCapability)wcf.create(all, TxQName.ATALWAYSCAPABILITY.getQName());
                all.addExtensibilityElement(txAlways);
            }

            if (ComboConstants.TX_REQUIRESNEW.equals(txValue)) {
                txAlways = (ATAlwaysCapability)wcf.create(all, TxQName.ATALWAYSCAPABILITY.getQName());
                all.addExtensibilityElement(txAlways);
            }

            if (ComboConstants.TX_SUPPORTED.equals(txValue)) {
                tx = (ATAssertion)wcf.create(all, TxQName.ATASSERTION.getQName());
                tx.setOptional(true, configVersion);
                all.addExtensibilityElement(tx);
            }
            
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }

    public static String getTx(BindingOperation bop, Node node) {
        String tx = getTxFromConfig(bop);
        
//        if (WSITModelSupport.isServiceFromWsdl(node) ||                         // WS from WSDL doesn't care about annotations
//            ((tx != null) && (tx != ComboConstants.TX_NOTSUPPORTED) && (tx != ComboConstants.TX_NEVER))  // if there's something in config, do not consider annotations
//            ) {
//            return tx;
//        }
//        tx = getTxFromAnnotation(bop, node);
        return tx;
    }

//    private static String getTxFromAnnotation(BindingOperation bop, Node node) {
//        JavaClass jc = JMIUtils.getJavaClassFromNode(node);
//        Method m = JMIUtils.getMethod(node, bop.getName());
//        
//        // first check if there's annotation on method
//        String value = getTxFromAnnotation(m);
//        if (value != null) {
//            return value;
//        }
//        
//        // then check the class; if set on class - applies to all methods
//        value = getTxFromAnnotation(jc);
//        if (value != null) {
//            return value;
//        }
//
//        return ComboConstants.TX_NOTSUPPORTED;
//    }

//    private static String getTxFromAnnotation(Feature f) {
//        VariableAccess va = getTxAnnotationVariable(f);
//        if (va != null) {
//            return getTxComboValue(va.getName());
//        }
//        return null;
//    }

//    private static VariableAccess getTxAnnotationVariable(Feature f) {
//        List<Annotation> annotations = f.getAnnotations();
//        for (Annotation a : annotations) {
//            String aName = a.getType().getName();
//            if ("javax.ejb.TransactionAttribute".equals(aName)) { //NOI18N
//                List<AttributeValue> attribs = a.getAttributeValues();
//                for (AttributeValue attr : attribs) {
//                    InitialValue iv = attr.getValue();
//                    if (iv instanceof VariableAccess) {
//                        return (VariableAccess) iv;
//                    }
//                }
//            }
//        }
//        return null;
//    }

//    private static String getTxComboValue(String annotationAttr) {
//        if (annotationAttr != null) {
//            if ("MANDATORY".equals(annotationAttr)) {       //NOI18N
//                return ComboConstants.TX_MANDATORY;
//            }
//            if ("REQUIRED".equals(annotationAttr)) {        //NOI18N
//                return ComboConstants.TX_REQUIRED;
//            }
//            if ("REQUIRES_NEW".equals(annotationAttr)) {    //NOI18N
//                return ComboConstants.TX_REQUIRESNEW;
//            }
//            if ("SUPPORTED".equals(annotationAttr)) {       //NOI18N
//                return ComboConstants.TX_SUPPORTED;
//            }
//            if ("NOT_SUPPORTED".equals(annotationAttr)) {   //NOI18N
//                return ComboConstants.TX_NOTSUPPORTED;
//            }
//            if ("NEVER".equals(annotationAttr)) {           //NOI18N
//                return ComboConstants.TX_NEVER;
//            }
//        }
//        return null;
//    }

//    private static String getAnnotationAttrValue(String comboStr) {
//        if (comboStr != null) {
//            if (ComboConstants.TX_MANDATORY.equals(comboStr)) {
//                return "MANDATORY";                             //NOI18N
//            }
//            if (ComboConstants.TX_REQUIRED.equals(comboStr)) {
//                return "REQUIRED";                              //NOI18N
//            }
//            if (ComboConstants.TX_REQUIRESNEW.equals(comboStr)) {
//                return "REQUIRES_NEW";                          //NOI18N
//            }
//            if (ComboConstants.TX_SUPPORTED.equals(comboStr)) {
//                return "SUPPORTED";                             //NOI18N
//            }
//            if (ComboConstants.TX_NOTSUPPORTED.equals(comboStr)) {
//                return "NOT_SUPPORTED";                         //NOI18N
//            }
//            if (ComboConstants.TX_NEVER.equals(comboStr)) {
//                return "NEVER";                                 //NOI18N
//            }
//        }
//        return null;
//    }
    
    private static String getTxFromConfig(BindingOperation bop) {
        Policy p = PolicyModelHelper.getPolicyForElement(bop);

        ATAssertion tx = getATAssertion(p);
        ATAlwaysCapability txAlways = getATAlwaysAssertion(p);
        
        if ((tx != null) && (txAlways == null)) {
            if (tx.isOptional()) {
                return ComboConstants.TX_SUPPORTED;
            }
            return ComboConstants.TX_MANDATORY;
        }
        if ((tx != null) && (txAlways != null)) {
            return ComboConstants.TX_REQUIRED;
        }
        if ((tx == null) && (txAlways != null)) {
            return ComboConstants.TX_REQUIRESNEW;
        }
        
        return ComboConstants.TX_NOTSUPPORTED;
    }

}
