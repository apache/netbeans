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

import java.io.IOException;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.hibernate.editor.HibernateEditorUtil;
import org.netbeans.modules.hibernate.mapping.HibernateMappingXmlConstants;
import org.openide.util.Exceptions;
import org.w3c.dom.Node;

/**
 *
 * @author Dongmei Cao
 */
public class PropertyHyperlinkProcessor extends HyperlinkProcessor {

    public PropertyHyperlinkProcessor() {
    }

    @Override
    public void process(HyperlinkEnv env) {
        try {
            String className0 = HibernateEditorUtil.getClassName(env.getCurrentTag().getNode());
            if (className0 == null) {
                return;
            }
        Node n = env.getDocumentContext().getDocRoot().getNode().getAttributes().
                getNamedItem(HibernateMappingXmlConstants.PACKAGE_ATTRIB);//NOI18N
        String pack = n == null ? null : n.getNodeValue();
            if(pack!=null &&  pack.length()>0){
                if(!className0.contains(".")){
                    className0 = pack + "." +className0;
                }
            }
            final String className = className0;
            final String propName = env.getValueString();
            if( propName == null || propName.length() == 0 ) {
                return;
            }

            JavaSource js = HibernateEditorUtil.getJavaSource(env.getDocument());
            if (js == null) {
                return;
            }

            js.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController cc) throws Exception {
                    TypeElement te = HibernateEditorUtil.findClassElementByBinaryName(className, cc);
                    if (te == null) {
                        return;
                    }
                    
                    TypeMirror startType = te.asType();
                     if (startType == null) {
                        return;
                     }
                    
                    ElementUtilities eu = cc.getElementUtilities();

                    VariableElement fieldElement = HibernateEditorUtil.findFieldElementOnType(eu, startType, propName);
                    if (fieldElement != null) {
                        ElementOpen.open(cc.getClasspathInfo(), fieldElement);
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
