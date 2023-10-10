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

package org.netbeans.modules.maven.grammar;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.jdom2.Element;
import org.netbeans.modules.maven.grammar.spi.AbstractSchemaBasedGrammar;
import org.netbeans.modules.xml.api.model.GrammarEnvironment;
import org.netbeans.modules.xml.api.model.GrammarResult;
import org.netbeans.modules.xml.api.model.HintContext;
import org.openide.ErrorManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * xml completion grammar based on xsd, additionally allowing more to be added.
 * @author Milos Kleint 
 */
public class MavenAssemblyGrammar extends AbstractSchemaBasedGrammar {
    
    
    public MavenAssemblyGrammar(GrammarEnvironment env) {
        super(env);
    }
    
    @Override
    protected InputStream getSchemaStream() {
        return getClass().getResourceAsStream("/org/netbeans/modules/maven/grammar/assembly-1.0.0.xsd"); //NOI18N
    }
    
    @Override
    protected Enumeration<GrammarResult> getDynamicValueCompletion(String path, HintContext virtualTextCtx, Element element) {
        if ("/assembly/dependencySets/dependencySet/includes/include".equals(path) || //NOI18N
            "/assembly/dependencySets/dependencySet/excludes/exclude".equals(path)) { //NOI18N
            //TODO could be nice to filter out the dependencies that are already being used..
            List<GrammarResult> toRet = new ArrayList<GrammarResult>();
            MavenProject project = getMavenProject();
            if (project != null) {
                Node previous;
                // HACK.. if currentPrefix is zero length, the context is th element, otherwise it's the content inside
                if (virtualTextCtx.getCurrentPrefix().length() == 0) {
                     previous = virtualTextCtx.getParentNode().getParentNode(); 
                } else {
                    previous = virtualTextCtx.getParentNode().getParentNode().getParentNode();
                }
                previous = previous.getPreviousSibling();
                String scope = null;
                while (previous != null) {
                    if (previous instanceof org.w3c.dom.Element) {
                        org.w3c.dom.Element el = (org.w3c.dom.Element)previous;
                        NodeList lst = el.getChildNodes();
                        if (lst.getLength() > 0) {
                            if ("scope".equals(el.getNodeName())) { //NOI18N
                                scope = lst.item(0).getNodeValue();
                                break;
                            }
                        }
                    }
                    previous = previous.getPreviousSibling();
                }
                if (scope == null) {
                    scope = "runtime"; //NOI18N
                }
                scope = scope.trim();
                Iterator<Dependency> it;
                if ("runtime".equals(scope)) { //NOI18N
                    it = project.getRuntimeDependencies().iterator();
                } else if ("test".equals(scope)) { //NOI18N
                    it = project.getTestDependencies().iterator();
                } else if ("compile".equals(scope)) { //NOI18N
                    it = project.getCompileDependencies().iterator();
                } else {
                    ErrorManager.getDefault().log(ErrorManager.WARNING, "How to process includes/excludes for scope '" + scope + "'? Fallback to 'runtime'."); //NOI18N
                    it = project.getRuntimeDependencies().iterator();
                }
                while (it.hasNext()) {
                    Dependency elem = it.next();
                    String str = elem.getGroupId() + ":" + elem.getArtifactId(); //NOI18N
                    if (str.startsWith(virtualTextCtx.getCurrentPrefix())) {
                        toRet.add(new MyTextElement(str, virtualTextCtx.getCurrentPrefix()));
                    }
                }
            }
            return Collections.enumeration(toRet);
        }
        return null;
    }
    
    
}
