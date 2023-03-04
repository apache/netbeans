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
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.rm.AckRequestInterval;
import org.netbeans.modules.websvc.wsitmodelext.rm.AllowDuplicates;
import org.netbeans.modules.websvc.wsitmodelext.rm.CloseTimeout;
import org.netbeans.modules.websvc.wsitmodelext.rm.DeliveryAssurance;
import org.netbeans.modules.websvc.wsitmodelext.rm.FlowControl;
import org.netbeans.modules.websvc.wsitmodelext.rm.InOrder;
import org.netbeans.modules.websvc.wsitmodelext.rm.InactivityTimeout;
import org.netbeans.modules.websvc.wsitmodelext.rm.MaxReceiveBufferSize;
import org.netbeans.modules.websvc.wsitmodelext.rm.Ordered;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMAssertion;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMMS13QName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMMSQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMSunClientQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMSunQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.ResendInterval;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;

/**
 *
 * @author Martin Grebac
 */
public class RMModelHelper {
    
    public static final String DEFAULT_INACT_TIMEOUT = "600000";         //NOI18N
    public static final String DEFAULT_MAXRCVBUFFERSIZE = "32";    //NOI18N
    public static final String DEFAULT_ACKINTERVAL = "200";         //NOI18N

    private static HashMap<ConfigVersion, RMModelHelper> instances = 
            new HashMap<ConfigVersion, RMModelHelper>();

    private ConfigVersion configVersion = ConfigVersion.getDefault();
    
    /**
     * Creates a new instance of RMModelHelper
     */
    private RMModelHelper(ConfigVersion configVersion) {
        this.configVersion = configVersion;
    }

    public static final synchronized RMModelHelper getInstance(ConfigVersion configVersion) {
        RMModelHelper instance = instances.get(configVersion);
        if (instance == null) {
            instance = new RMModelHelper(configVersion);
            instances.put(configVersion, instance);
        }
        return instance;
    }
    
    private RMAssertion getRMAssertion(Policy p) {
        return (RMAssertion) PolicyModelHelper.getTopLevelElement(p, RMAssertion.class,false);
    }

    private RMAssertion getRMAssertion(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        return getRMAssertion(p);
    }
    
    // checks if RM is enabled in the config wsdl on specified binding
    public boolean isRMEnabled(WSDLComponent c) {
        if (c instanceof Operation) {
            Operation o = (Operation)c;
            Binding b = (Binding)o.getParent();    
            return isRMEnabledB(b);
        }
        if (c instanceof Binding) {
            return isRMEnabledB((Binding)c);
        }
        return false;
    }
    
    // checks if RM is enabled in the config wsdl on specified binding
    private boolean isRMEnabledB(Binding b) {
        return (getRMAssertion(b) != null);
    }
        
    // enables RM in the config wsdl on specified binding
    public void enableRM(Binding b, boolean enable) {
        if (enable) {
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
            All a = pmh.createPolicy(b, true);
            pmh.createElement(a, RMQName.RMASSERTION.getQName(configVersion), RMAssertion.class, false);
        } else {
            RMAssertion rm = getRMAssertion(b);
            if (rm != null) {
                PolicyModelHelper.removeElement(rm.getParent(), RMAssertion.class, false);
            }
            enableOrdered(b, false);
            enableFlowControl(b, false);
            enableAllowDuplicates(b, false);
            setSequenceBinding(b, null);
            setInactivityTimeout(b, null);
            setMaxReceiveBufferSize(b, null);
            PolicyModelHelper.cleanPolicies(b);
        }
    }

    public String getInactivityTimeout(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        RMAssertion rm = getRMAssertion(p);
        return getInactivityTimeout(rm);
    }    
    
    public void setInactivityTimeout(Binding b, String value) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (value != null) {
            enableRM(b, true);
        }
        RMAssertion rm = getRMAssertion(p);
        setInactivityTimeout(rm, value);
    }

    private String getInactivityTimeout(RMAssertion rm) {
        String timeout = null;
        if (rm != null) {
            if (ConfigVersion.CONFIG_1_0.equals(configVersion)) {
                List<InactivityTimeout> time = rm.getExtensibilityElements(InactivityTimeout.class);
                if ((time != null) && (time.size() > 0)) {
                    timeout = time.get(0).getMilliseconds();
                }
            } else {
                List<InactivityTimeout> time = rm.getParent().getExtensibilityElements(InactivityTimeout.class);
                if ((time != null) && (time.size() > 0)) {
                    timeout = time.get(0).getMilliseconds();
                }
            }
        }
        return timeout;
    }
    
    private void setInactivityTimeout(RMAssertion rm, String value) {
        if (rm != null) {
            Model model = rm.getModel();
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                if (ConfigVersion.CONFIG_1_0.equals(configVersion)) {
                    List<InactivityTimeout> time = rm.getExtensibilityElements(InactivityTimeout.class);
                    InactivityTimeout iTimeout = null;
                    if ((time != null) && (time.size() > 0)) { iTimeout = time.get(0); }
                    if (iTimeout == null) {
                        if (value != null) {    // if is null, then there's no element and we want to remove it -> do nothing
                            WSDLComponentFactory wcf = rm.getModel().getFactory();
                            InactivityTimeout inT = null;
                            inT = (InactivityTimeout)wcf.create(rm, RMMS13QName.INACTIVITYTIMEOUT.getQName(configVersion));
                            inT.setMilliseconds(value);
                            rm.addExtensibilityElement(inT);
                        }
                    } else {
                        if (value == null) {
                            rm.removeExtensibilityElement(iTimeout);
                        } else {
                            iTimeout.setMilliseconds(value);
                        }
                    }
                } else {
                    InactivityTimeout time = PolicyModelHelper.getTopLevelElement(rm.getParent(), InactivityTimeout.class, false);
                    if (value != null) {    // if is null, then there's no element and we want to remove it -> do nothing
                        if (time == null) {
                            WSDLComponentFactory wcf = rm.getModel().getFactory();
                            InactivityTimeout inT = null;
                            inT = (InactivityTimeout)wcf.create(rm.getParent(), RMMS13QName.INACTIVITYTIMEOUT.getQName(configVersion));
                            inT.setMilliseconds(value);
                            rm.getParent().addExtensibilityElement(inT);
                        } else {
                            time.setMilliseconds(value);
                        }
                    } else {
                        if (time != null) {
                            rm.getParent().removeExtensibilityElement(time);
                        }
                    }
                }
            } finally {
                if (!isTransaction) {
                    WSITModelSupport.doEndTransaction(model);   
                }
            }
        }
    }
    
    // enables FlowControl in the config wsdl on specified binding
    public void enableFlowControl(Binding b, boolean enable) {
        if (enable) {
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
            All a = pmh.createPolicy(b, true);
            pmh.createElement(a, RMMSQName.RMFLOWCONTROL.getQName(), FlowControl.class, false);
        } else {
            Policy p = PolicyModelHelper.getPolicyForElement(b);
            FlowControl fc = getFlowControl(p);
            if (fc != null) {
                PolicyModelHelper.removeElement(fc.getParent(), FlowControl.class, false);
            }
            PolicyModelHelper.cleanPolicies(b);        
        }
    }

    // checks if Flow Control is enabled in the config wsdl  on specified binding
    public static boolean isFlowControl(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            FlowControl fc = getFlowControl(p);
            return (fc != null);
        }
        return false;
    }

    public void enableAllowDuplicates(Binding b, boolean enable) {
        if (enable) {
            assert(ConfigVersion.CONFIG_1_0.equals(configVersion));
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
            All a = pmh.createPolicy(b, true);
            pmh.createElement(a, RMSunQName.ALLOWDUPLICATES.getQName(), AllowDuplicates.class, false);
        } else {
            Policy p = PolicyModelHelper.getPolicyForElement(b);
            AllowDuplicates ad = getAllowDuplicates(p);
            if (ad != null) {
                PolicyModelHelper.removeElement(ad.getParent(), AllowDuplicates.class, false);
            }
            PolicyModelHelper.cleanPolicies(b);        
        }
    }
    
    public static boolean isAllowDuplicates(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            AllowDuplicates dup = getAllowDuplicates(p);
            return (dup != null);
        }
        return false;
    }
    
    public boolean isSequenceBinding(Binding b, RMSequenceBinding seq) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            RMAssertion ass = (RMAssertion) getRMAssertion(PolicyModelHelper.getPolicyForElement(b));
            return (PolicyModelHelper.getTopLevelElement(ass, seq.getAssertionClass(),false) != null);
        }
        return false;
    }

    public boolean isDeliveryAssurance(Binding b, RMDeliveryAssurance assurance) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            RMAssertion ass = (RMAssertion) getRMAssertion(PolicyModelHelper.getPolicyForElement(b));
            DeliveryAssurance delivery = PolicyModelHelper.getTopLevelElement(ass, DeliveryAssurance.class, true);
            return (PolicyModelHelper.getTopLevelElement(delivery, assurance.getAssertionClass(), true) != null);
        }
        return false;
    }
    
    public void setSequenceBinding(Binding b, RMSequenceBinding newValue) {
        PolicyModelHelper pmh = PolicyModelHelper.getInstance(this.configVersion);
        RMAssertion rmAssertion = getRMAssertion(b);
        if (newValue == null) {
            if (rmAssertion != null) {
                for (RMSequenceBinding seq : RMSequenceBinding.values()) {
                    PolicyModelHelper.removeElement(rmAssertion, seq.getAssertionClass(), true);          
                }
                PolicyModelHelper.cleanPolicies(b);
            }
            return;
        }
        pmh.createElement(rmAssertion, newValue.getQName(), newValue.getAssertionClass(), true);
    }

    public void setDeliveryAssurance(Binding b, RMDeliveryAssurance newValue) {
        PolicyModelHelper pmh = PolicyModelHelper.getInstance(this.configVersion);
        RMAssertion rmAssertion = getRMAssertion(b);
        if ((newValue == null) || (RMDeliveryAssurance.getDefault().equals(newValue)) ){
            DeliveryAssurance dAssurance = PolicyModelHelper.getTopLevelElement(rmAssertion, DeliveryAssurance.class, true);
            if (dAssurance != null) {
                for (RMDeliveryAssurance seq : RMDeliveryAssurance.values()) {
                    PolicyModelHelper.removeElement(dAssurance, seq.getAssertionClass(), true);                    
                }
                if (PolicyModelHelper.isEmpty(dAssurance)) {
                    PolicyModelHelper.removeElement(rmAssertion, DeliveryAssurance.class, true);                    
                }
            }
            PolicyModelHelper.cleanPolicies(b);
            return;
        }
        DeliveryAssurance dAssurance = pmh.createElement(rmAssertion, 
                RMQName.DELIVERYASSURANCE.getQName(configVersion), DeliveryAssurance.class, true);
        pmh.createElement(dAssurance, newValue.getQName(), newValue.getAssertionClass(), true);
    }
    
    public static void setMaxReceiveBufferSize(Binding b, String value) {
        FlowControl fc = getFlowControl(b);
        setMaxReceiveBufferSize(fc, value);
    }
    
    public static String getMaxReceiveBufferSize(Binding b) {
        FlowControl fc = getFlowControl(b);
        return getMaxReceiveBufferSize(fc);
    }
    
    private static FlowControl getFlowControl(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        return getFlowControl(p);
    }

    private static FlowControl getFlowControl(Policy p) {
        return (FlowControl) PolicyModelHelper.getTopLevelElement(p, FlowControl.class, false);
    }

    private static String getMaxReceiveBufferSize(FlowControl fc) {
        String max = null;
        if (fc != null) {
            MaxReceiveBufferSize maxBuf = fc.getMaxReceiveBufferSize();
            if (maxBuf!=null) {
                max = maxBuf.getMaxReceiveBufferSize();
            }
        }
        return max;
    }

    private static void setMaxReceiveBufferSize(FlowControl fc, String value) {
        if (fc != null) {
            Model model = fc.getModel();
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                MaxReceiveBufferSize maxBufSize = fc.getMaxReceiveBufferSize();
                if (maxBufSize == null) {
                    if (value != null) {    // if is null, then there's no element and we want to remove it -> do nothing
                        WSDLComponentFactory wcf = fc.getModel().getFactory();
                        MaxReceiveBufferSize maxBuf = (MaxReceiveBufferSize)wcf.create(fc, 
                                RMMSQName.MAXRECEIVEBUFFERSIZE.getQName()
                                );
                        maxBuf.setMaxReceiveBufferSize(value);
                        fc.addExtensibilityElement(maxBuf);
                    }
                } else {
                    if (value == null) {
                        fc.removeMaxReceiveBufferSize(maxBufSize);
                    } else {
                        maxBufSize.setMaxReceiveBufferSize(value);
                    }
                }                  
            } finally {
                if (!isTransaction) {
                    WSITModelSupport.doEndTransaction(model);
                }
            }
        }
    }

    // enables Ordered delivery in the config wsdl on specified binding
    public void enableOrdered(Binding b, boolean enable) {
        if (configVersion.equals(ConfigVersion.CONFIG_1_0)) {
            if (enable) {
                PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
                All a = pmh.createPolicy(b, true);
                pmh.createElement(a, RMSunQName.ORDERED.getQName(), Ordered.class, false);
            } else {
                Policy p = PolicyModelHelper.getPolicyForElement(b);
                Ordered ord = getOrdered(p);
                if (ord != null) {
                    PolicyModelHelper.removeElement(ord.getParent(), Ordered.class, false);
                }
                PolicyModelHelper.cleanPolicies(b);        
            }
        } else {
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
            Policy p = PolicyModelHelper.getPolicyForElement(b);
            RMAssertion rm = getRMAssertion(p);
            if (enable) {
                DeliveryAssurance delAssurance = pmh.createElement(rm, 
                        RMQName.DELIVERYASSURANCE.getQName(configVersion), 
                        DeliveryAssurance.class, true);
                pmh.createElement(delAssurance, 
                        RMQName.INORDER.getQName(configVersion),
                        InOrder.class, true);
            } else {
                if (rm != null) {
                    DeliveryAssurance delAssurance = getDeliveryAssurance(b);
                    if (delAssurance != null) {
                        PolicyModelHelper.removeElement(delAssurance, InOrder.class, true);
                    }
                    if (PolicyModelHelper.isEmpty(delAssurance)) {
                        PolicyModelHelper.removeElement(rm, DeliveryAssurance.class, true);                    
                    }
                }
            }
        }
    }

    public boolean isOrderedEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            if (configVersion.equals(ConfigVersion.CONFIG_1_0)) {
                Ordered ord = getOrdered(p);
                return (ord != null);
            } else {
                DeliveryAssurance delAssurance = getDeliveryAssurance(b);
                if (delAssurance == null) return false;
                InOrder order = 
                        PolicyModelHelper.getTopLevelElement(delAssurance, InOrder.class, true);
                return (order != null);
            }
        }
        return false; 
    }

    private DeliveryAssurance getDeliveryAssurance(Binding b) {
        RMAssertion rm = getRMAssertion(b);
        if (rm != null) {
            return PolicyModelHelper.getTopLevelElement(rm, DeliveryAssurance.class, true);
        }
        return null;
    }
    
    private Ordered getOrdered(Policy p) {
        return (Ordered) PolicyModelHelper.getTopLevelElement(p, Ordered.class, false);
    }    

    private static AllowDuplicates getAllowDuplicates(Policy p) {
        return (AllowDuplicates) PolicyModelHelper.getTopLevelElement(p, AllowDuplicates.class, false);
    }    
    
    public static String getResendInterval(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        ResendInterval ri = (ResendInterval)PolicyModelHelper.getTopLevelElement(p, ResendInterval.class, false);
        if (ri != null) {
            return ri.getResendInterval();
        }
        return null;
    }
    
    public void setResendInterval(Binding b, String value) {
        WSDLModel model = b.getModel();
        PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
        All all = pmh.createPolicy(b, false);
        ResendInterval ri = pmh.createElement(all, 
                RMSunClientQName.RESENDINTERVAL.getQName(), ResendInterval.class, false);
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (ri != null) {
                if (value == null) {
                    PolicyModelHelper.removeElement(ri);
                } else {
                    ri.setResendInterval(value);
                }
            }
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }

    public static String getCloseTimeout(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        CloseTimeout ct = (CloseTimeout)PolicyModelHelper.getTopLevelElement(p, CloseTimeout.class, false);
        if (ct != null) {
            return ct.getCloseTimeout();
        }
        return null;
    }
    
    public void setCloseTimeout(Binding b, String value) {
        WSDLModel model = b.getModel();
        PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
        All all = pmh.createPolicy(b, false);
        CloseTimeout ct = pmh.createElement(all, 
                RMSunClientQName.CLOSETIMEOUT.getQName(), CloseTimeout.class, false);
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (ct != null) {
                if (value == null) {
                    PolicyModelHelper.removeElement(ct);
                } else {
                    ct.setCloseTimeout(value);
                }
            }
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }
    
    public static String getAckRequestInterval(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        AckRequestInterval ri = PolicyModelHelper.getTopLevelElement(p, AckRequestInterval.class, false);
        if (ri != null) {
            return ri.getAckRequestInterval();
        }
        return null;
    }
    
    public void setAckRequestInterval(Binding b, String value) {
        WSDLModel model = b.getModel();
        PolicyModelHelper pmh = PolicyModelHelper.getInstance(configVersion);
        All all = pmh.createPolicy(b, false);
        AckRequestInterval ri = pmh.createElement(all, 
                RMSunClientQName.ACKREQUESTINTERVAL.getQName(), AckRequestInterval.class, false);
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (ri != null) {
                if (value == null) {
                    PolicyModelHelper.removeElement(ri);
                } else {
                    ri.setAckRequestInterval(value);
                }
            }
        } finally {
            if (!isTransaction) {
                WSITModelSupport.doEndTransaction(model);
            }
        }
    }
    
}
