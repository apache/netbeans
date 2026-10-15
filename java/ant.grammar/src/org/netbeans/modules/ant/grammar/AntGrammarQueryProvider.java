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

package org.netbeans.modules.ant.grammar;

import java.beans.FeatureDescriptor;
import java.util.Enumeration;
import org.netbeans.modules.xml.api.model.GrammarEnvironment;
import org.netbeans.modules.xml.api.model.GrammarQuery;
import org.netbeans.modules.xml.api.model.GrammarQueryManager;
import org.openide.filesystems.FileObject;
import org.openide.util.Enumerations;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * Provides the Ant grammar for any documents whose root elements matches a standard pattern.
 * See also ant/src/.../resources/mime-resolver.xml.
 * @author Petr Kuzel, Jesse Glick
 */
@ServiceProvider(service=GrammarQueryManager.class, path=/* XXX GrammarQueryManager.DefaultQueryManager.FOLDER*/ "Plugins/XML/GrammarQueryManagers")
public final class AntGrammarQueryProvider extends GrammarQueryManager {
    
    public Enumeration enabled(GrammarEnvironment ctx) {
        FileObject f = ctx.getFileObject();
        if (f != null && !f.getMIMEType().equals("text/x-ant+xml")) {
            return null;
        }
        Enumeration<Node> en = ctx.getDocumentChildren();
        while (en.hasMoreElements()) {
            Node next = en.nextElement();
            if (next.getNodeType() == Node.ELEMENT_NODE) {
                Element root = (Element) next;                
                if ("project".equals(root.getNodeName())) { // NOI18N
                    return Enumerations.singleton (next);
                }
            }
        }
        return null;
    }
    
    public FeatureDescriptor getDescriptor() {
        return new FeatureDescriptor();
    }
    
    public GrammarQuery getGrammar(GrammarEnvironment env) {
        // XXX pick up env.fileObject too
        return new AntGrammar();
    }
    
}
