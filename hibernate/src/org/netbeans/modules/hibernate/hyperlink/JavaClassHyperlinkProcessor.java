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
package org.netbeans.modules.hibernate.hyperlink;

import org.netbeans.modules.hibernate.editor.HibernateEditorUtil;
import org.netbeans.modules.hibernate.mapping.HibernateMappingXmlConstants;
import org.w3c.dom.Node;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM), Dongmei Cao
 */
public class JavaClassHyperlinkProcessor extends HyperlinkProcessor {

    public JavaClassHyperlinkProcessor() {
    }

    @Override
    public void process(HyperlinkEnv env) {
        String className = env.getValueString();
        Node n = env.getDocumentContext().getDocRoot().getNode().getAttributes().
                getNamedItem(HibernateMappingXmlConstants.PACKAGE_ATTRIB);//NOI18N
        String pack = n == null ? null : n.getNodeValue();
        if(pack!=null &&  pack.length()>0){
            if(!className.contains(".")){
                className = pack + "." +className;
            }
        }
        HibernateEditorUtil.findAndOpenJavaClass(className, env.getDocument());
    }
}
