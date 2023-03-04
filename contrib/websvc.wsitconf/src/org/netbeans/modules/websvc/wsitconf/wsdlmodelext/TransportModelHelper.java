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

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.spi.ProjectSpecificTransport;
import org.netbeans.modules.websvc.wsitconf.spi.WsitProvider;
import org.netbeans.modules.websvc.wsitconf.util.ServerUtils;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.mtom.MtomQName;
import org.netbeans.modules.websvc.wsitmodelext.mtom.OptimizedMimeSerialization;
import org.netbeans.modules.websvc.wsitmodelext.transport.AutomaticallySelectFastInfoset;
import org.netbeans.modules.websvc.wsitmodelext.transport.AutomaticallySelectOptimalTransport;
import org.netbeans.modules.websvc.wsitmodelext.transport.FIQName;
import org.netbeans.modules.websvc.wsitmodelext.transport.OptimizedFastInfosetSerialization;
import org.netbeans.modules.websvc.wsitmodelext.transport.OptimizedTCPTransport;
import org.netbeans.modules.websvc.wsitmodelext.transport.TCPQName;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.xam.Model;

/**
 *
 * @author Martin Grebac
 */
public class TransportModelHelper {
            
    /**
     * Creates a new instance of TransportModelHelper
     */
    private TransportModelHelper() { }
    
    private static OptimizedMimeSerialization getOptimizedMimeSerialization(Policy p) {
        return PolicyModelHelper.getTopLevelElement(p, OptimizedMimeSerialization.class,false);        
    }
    
    // checks if Mtom is enabled in the config wsdl on specified binding
    public static boolean isMtomEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            OptimizedMimeSerialization mtomAssertion = getOptimizedMimeSerialization(p);
            return (mtomAssertion != null);
        }
        return false;
    }
    
    // enables Mtom in the config wsdl on specified binding
    public static void enableMtom(Binding b, boolean enable) {
        if (enable) {
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(PolicyModelHelper.getConfigVersion(b));
            All a = pmh.createPolicy(b, false);
            pmh.createElement(a, MtomQName.OPTIMIZEDMIMESERIALIZATION.getQName(), OptimizedMimeSerialization.class, false);
        } else {
            Policy p = PolicyModelHelper.getPolicyForElement(b);
            OptimizedMimeSerialization mtom = getOptimizedMimeSerialization(p);
            if (mtom != null) {
                PolicyModelHelper.removeElement(mtom);
            }
            PolicyModelHelper.cleanPolicies(b);        
        }
    }

    private static OptimizedTCPTransport getOptimizedTCPTransport(Policy p) {
        return PolicyModelHelper.getTopLevelElement(p, OptimizedTCPTransport.class,false);
    }
    
    // checks if TCP is enabled in the config wsdl on specified binding
    public static boolean isTCPEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            OptimizedTCPTransport tcpAssertion = getOptimizedTCPTransport(p);
            if (tcpAssertion != null) {
                return tcpAssertion.isEnabled();
            }
        }
        return false;
    }

    /* doesn't set anything in the DD; is used when changing namespaces for policies
     */
    static void enableTCP(Binding b, boolean enable) {
        enableTCP(null, null, null, false, b, null, enable);
    }
    
    // enables/disables TCP in the config wsdl on specified binding
    public static void enableTCP(String name, String serviceName, String implClass, boolean isFromJava, Binding b, Project p, boolean enable) {
        PolicyModelHelper pmh = PolicyModelHelper.getInstance(PolicyModelHelper.getConfigVersion(b));
        All a = pmh.createPolicy(b, false);
        OptimizedTCPTransport tcp = 
                pmh.createElement(a, TCPQName.OPTIMIZEDTCPTRANSPORT.getQName(), 
                OptimizedTCPTransport.class, false);
        
        // make sure the listener is there (in Web project and jsr109 deployment
        if (enable) {

            Model model = b.getModel();
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }

            boolean tomcat = ServerUtils.isTomcat(p);
            if (tomcat) {
                tcp.setPort(OptimizedTCPTransport.DEFAULT_PORT_VALUE);
            }

            if (p != null) {
                WsitProvider provider = p.getLookup().lookup(WsitProvider.class);
                if (provider != null) {
                    ProjectSpecificTransport t = provider.getProjectTransportUpdater();
                    if (t != null) {
                        t.setTCPUrl(name, serviceName, implClass, tomcat);
                    }
                }
            }

            try {
                tcp.enable(enable);
            } finally {
                if (!isTransaction) {
                    WSITModelSupport.doEndTransaction(model);
                }
            }
        } else {
            removeTCP(b);
            PolicyModelHelper.cleanPolicies(b);
        }
    }
    
   private static void removeTCP(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        OptimizedTCPTransport tcp = getOptimizedTCPTransport(p);
        if (tcp != null) {
            PolicyModelHelper.removeElement(tcp);
        }
    }
       
    private static OptimizedFastInfosetSerialization getOptimizedFastInfosetSerialization(Policy p) {
        return PolicyModelHelper.getTopLevelElement(p, OptimizedFastInfosetSerialization.class,false);
    }
    
    // checks if FI is enabled in the config wsdl on specified binding
    public static boolean isFIEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            OptimizedFastInfosetSerialization fiAssertion = getOptimizedFastInfosetSerialization(p);
            if (fiAssertion != null) {
                return fiAssertion.isEnabled();
            }
        }
        return true;
    }
    
    // enables/disables FI in the config wsdl on specified binding
    public static void enableFI(Binding b, boolean enable) {
        PolicyModelHelper pmh = PolicyModelHelper.getInstance(PolicyModelHelper.getConfigVersion(b));
        All a = pmh.createPolicy(b, false);
        OptimizedFastInfosetSerialization fi = 
                pmh.createElement(a, FIQName.OPTIMIZEDFASTINFOSETSERIALIZATION.getQName(), 
                OptimizedFastInfosetSerialization.class, false);
        Model model = b.getModel();
        boolean isTransaction = model.isIntransaction();
        if (enable) {
            removeFI(b);
            PolicyModelHelper.cleanPolicies(b);
        } else {
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                fi.enable(enable);
            } finally {
                if (!isTransaction) {
                    WSITModelSupport.doEndTransaction(model);
                }
            }
        }
        if (enable) {
            PolicyModelHelper.cleanPolicies(b);
        }
    }
    
    private static void removeFI(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        OptimizedFastInfosetSerialization fi = getOptimizedFastInfosetSerialization(p);
        if (fi != null) {
            PolicyModelHelper.removeElement(fi);
        }
    }
    
    private static AutomaticallySelectFastInfoset getAutoEncoding(Policy p) {
        return (AutomaticallySelectFastInfoset) PolicyModelHelper.getTopLevelElement(p, AutomaticallySelectFastInfoset.class,false);
    }
    
    public static boolean isAutoEncodingEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            AutomaticallySelectFastInfoset ae = getAutoEncoding(p);
            return (ae != null);
        }
        return false;
    }
    
    public static void setAutoEncoding(Binding b, boolean enable) {
        if (enable) {
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(PolicyModelHelper.getConfigVersion(b));
            All a = pmh.createPolicy(b, false);
            pmh.createElement(a, FIQName.AUTOMATICALLYSELECTFASTINFOSET.getQName(), AutomaticallySelectFastInfoset.class, false);
        } else {
            Policy p = PolicyModelHelper.getPolicyForElement(b);
            AutomaticallySelectFastInfoset ae = getAutoEncoding(p);
            if (ae != null) {
                PolicyModelHelper.removeElement(ae);
            }
        }
    }
    
    private static AutomaticallySelectOptimalTransport getAutoTransport(Policy p) {
        return (AutomaticallySelectOptimalTransport) PolicyModelHelper.getTopLevelElement(p, AutomaticallySelectOptimalTransport.class,false);
    }
    
    public static  boolean isAutoTransportEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            AutomaticallySelectOptimalTransport at = getAutoTransport(p);
            return (at != null);
        }
        return false;
    }
    
    public static void enableAutoTransport(Binding b, boolean enable) {
        if (enable) {
            PolicyModelHelper pmh = PolicyModelHelper.getInstance(PolicyModelHelper.getConfigVersion(b));
            All a = pmh.createPolicy(b, false);
            pmh.createElement(a, 
                    TCPQName.AUTOMATICALLYSELECTOPTIMALTRANSPORT.getQName(), 
                    AutomaticallySelectOptimalTransport.class, false);
        } else {
            Policy p = PolicyModelHelper.getPolicyForElement(b);
            AutomaticallySelectOptimalTransport at = getAutoTransport(p);
            if (at != null) {
                PolicyModelHelper.removeElement(at);
            }
        }
    }

}
