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

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary;

import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 *
 * @author Martin Grebac
 */
public enum ProprietarySecurityPolicyAttribute implements Attribute {
        VISIBILITY("visibility"),                   //NOI18N
        ITERATIONSFORPDK("iterationsForPDK"),       //NOI18N
        WSTVERSION("wstVersion"),                   //NOI18N
        SHARETOKEN("shareToken"),                   //NOI18N
        TIMESTAMPTIMEOUT("timestampTimeout"),       //NOI18N
        MAXCLOCKSKEW("maxClockSkew"),               //NOI18N
        TIMESTAMPFRESHNESS("timestampFreshnessLimit"),  //NOI18N
//        MAXNONCEAGE("maxNonceAge"),                   //NOI18N
        REVOCATION("revocationEnabled"),                //NOI18N
        LOGINMODULE("loginModule"),                     //NOI18N
        SERVICEPRINCIPAL("servicePrincipal"),           //NOI18N
        CREDENTIALDELEGATION("credentialDelegation"),   //NOI18N
        DEFAULT("default"),                         //NOI18N
        ENCRYPTISSUEDKEY("encryptIssuedKey"),       //NOI18N
        ENCRYPTISSUEDTOKEN("encryptIssuedToken"),   //NOI18N
        ENDPOINT("endpoint"),                       //NOI18N
        METADATA("metadata"),                       //NOI18N
        WSDLLOCATION("wsdlLocation"),               //NOI18N
        SERVICENAME("serviceName"),                 //NOI18N
        PORTNAME("portName"),                       //NOI18N
        NAMESPACE("namespace"),                     //NOI18N
        TIMEOUT("timeout"),                         //NOI18N
        REQUIRECANCELSCT("requireCancelSCT"),       //NOI18N
        RENEWEXPIREDSCT("renewExpiredSCT"),         //NOI18N
        LOCATION("location"),                       //NOI18N
        ALIASSELECTOR("aliasSelector"),             //NOI18N
        CERTSELECTOR("certSelector"),               //NOI18N
        ALIAS("alias"),                     //NOI18N
        STSALIAS("stsalias"),               //NOI18N
        PEERALIAS("peeralias"),             //NOI18N
        TYPE("type"),                       //NOI18N
        KEYPASS("keypass"),                 //NOI18N
        STOREPASS("storepass"),             //NOI18N
        NAME("name"),                       //NOI18N
        CLASSNAME("classname");             //NOI18N
    
    private String name;
    private Class type;
    private Class subtype;
    
    /**
     * Creates a new instance of ProprietarySecurityPolicyAttribute
     */
    ProprietarySecurityPolicyAttribute(String name) {
        this(name, String.class);
    }
    ProprietarySecurityPolicyAttribute(String name, Class type) {
        this(name, type, null);
    }
    ProprietarySecurityPolicyAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    @Override
    public String toString() { return name; }

    public Class getType() {
        return type;
    }

    public String getName() { return name; }

    public Class getMemberType() { return subtype; }
}
