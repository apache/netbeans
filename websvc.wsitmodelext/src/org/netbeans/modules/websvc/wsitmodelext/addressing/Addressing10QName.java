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


package org.netbeans.modules.websvc.wsitmodelext.addressing;

import java.util.HashMap;
import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Martin Grebac
 */
public enum Addressing10QName {
    ENDPOINTREFERENCE(createAddressingQName("EndpointReference")),              //NOI18N
    ANONYMOUS(createAddressingQName("Anonymous")),                              //NOI18N
    ADDRESS(createAddressingQName("Address")),                                  //NOI18N
    ADDRESSINGMETADATA(createAddressingQName("Metadata")),                      //NOI18N
    REFERENCEPROPERTIES(createAddressingQName("ReferenceProperties"));          //NOI18N

    public static final String ADDRESSING10_NS_PREFIX = "wsaw";                 //NOI18N

    public static final String ADDRESSING10_NS_URI = "http://www.w3.org/2005/08/addressing";  //NOI18N
    public static final String ADDRESSING10_NS_URI_EXT = "http://www.w3.org/2006/03/addressing/ws-addr.xsd";  //NOI18N
    public static final String ADDRESSING10_NS_URI_LOCAL = "nbres:/org/netbeans/modules/websvc/wsitmodelext/catalog/resources/ws-addr.xsd";  //NOI18N
    
    public static QName createAddressingQName(String localName){
        return new QName(ADDRESSING10_NS_URI, localName, ADDRESSING10_NS_PREFIX);
    }
    
    Addressing10QName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (Addressing10QName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

    public Map<String, String> getSchemaLocations(boolean local) {
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put(ADDRESSING10_NS_URI, local ? ADDRESSING10_NS_URI_LOCAL : ADDRESSING10_NS_URI_EXT);
        return hmap;
    }

    public String getSchemaLocation(String namespace, boolean local) {
        if (ADDRESSING10_NS_URI.equals(namespace)) {
            return local ? ADDRESSING10_NS_URI_LOCAL : ADDRESSING10_NS_URI_EXT;
        }
        return null;
    }

}
