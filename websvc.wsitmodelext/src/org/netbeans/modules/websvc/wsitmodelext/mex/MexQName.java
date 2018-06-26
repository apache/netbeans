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
package org.netbeans.modules.websvc.wsitmodelext.mex;

import java.util.HashMap;
import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.websvc.wsitmodelext.versioning.SchemaLocationProvider;

/**
 *
 * @author Martin Grebac
 */
public enum MexQName implements SchemaLocationProvider {
    METADATAREFERENCE(createMexQName("MetadataReference")), //NOI18N
    METADATA(createMexQName("Metadata")),                   //NOI18N
    METADATASECTION(createMexQName("MetadataSection")),     //NOI18N
    IDENTIFIER(createMexQName("Identifier")),               //NOI18N
    LOCATION(createMexQName("Location")),                   //NOI18N
    DIALECT(createMexQName("Dialect"));                     //NOI18N

    public static final String MEX_NS_PREFIX = "wsx";       //NOI18N

    public static final String MEX_NS_URI = "http://schemas.xmlsoap.org/ws/2004/09/mex";    //NOI18N
    public static final String MEX_NS_URI_EXT = "http://schemas.xmlsoap.org/ws/2004/09/mex/MetadataExchange.xsd";    //NOI18N
    public static final String MEX_NS_URI_LOCAL = "nbres:/org/netbeans/modules/websvc/wsitmodelext/catalog/resources/metadata-exchange.xsd";    //NOI18N
    
    public static QName createMexQName(String localName){
        return new QName(MEX_NS_URI, localName, MEX_NS_PREFIX);
    }
    
    MexQName(QName name) {
        qName = name;
    }
    
    public QName getQName(){
        return qName;
    }
    private static Set<QName> qnames = null;
    public static Set<QName> getQNames() {
        if (qnames == null) {
            qnames = new HashSet<QName>();
            for (MexQName wq : values()) {
                qnames.add(wq.getQName());
            }
        }
        return qnames;
    }
    private final QName qName;

    public Map<String, String> getSchemaLocations(boolean local) {
        HashMap<String, String> hmap = new HashMap<String, String>();
        hmap.put(MEX_NS_URI, local ? MEX_NS_URI_LOCAL : MEX_NS_URI_EXT);
        return hmap;
    }

    public String getSchemaLocation(String namespace, boolean local) {
        if (MEX_NS_URI.equals(namespace)) {
            return local ? MEX_NS_URI_LOCAL : MEX_NS_URI_EXT;
        }
        return null;
    }

}
