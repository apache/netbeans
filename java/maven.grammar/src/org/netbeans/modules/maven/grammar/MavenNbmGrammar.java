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

/**
 * xml completion grammar based on xsd, additionally allowing more to be added.
 * @author Milos Kleint
 */
public class MavenNbmGrammar extends AbstractSchemaBasedGrammar {
    
    
    public MavenNbmGrammar(GrammarEnvironment env) {
        super(env);
    }
    
    @Override
    protected InputStream getSchemaStream() {
        return getClass().getResourceAsStream("/org/netbeans/modules/maven/grammar/nbm-1.0.0.xsd"); //NOI18N
    }
    

    @Override
    protected Enumeration<GrammarResult> getDynamicValueCompletion(String path, HintContext hintCtx, Element lowestParent) {
        if ("/nbm/dependencies/dependency/type".equals(path)) { //NOI18N
            return createTextValueList(new String[] {
                "spec", //NOI18N
                "impl", //NOI18N
                "loose" //NOI18N
                }, hintCtx);
        }
        if ("/nbm/moduleType".equals(path)) { //NOI18N
            return createTextValueList(new String[] {
                "normal", //NOI18N
                "autoload", //NOI18N
                "eager" //NOI18N
                }, hintCtx);
            
        }
        if ("/nbm/dependencies/dependency/id".equals(path) || //NOI18N
            "/nbm/libraries/library".equals(path)) { //NOI18N
            //TODO could be nice to filter out the dependencies that are already being used..
            List<GrammarResult> toRet = new ArrayList<GrammarResult>();
            MavenProject project = getMavenProject();
            if (project != null) {
                @SuppressWarnings("unchecked")
                Iterator<Dependency> it = project.getCompileDependencies().iterator();
                while (it.hasNext()) {
                    Dependency elem = it.next();
                    String str = elem.getGroupId() + ":" + elem.getArtifactId(); //NOI18N
                    if (str.startsWith(hintCtx.getCurrentPrefix())) {
                        toRet.add(new MyTextElement(str, hintCtx.getCurrentPrefix()));
                    }
                }
            }
            return Collections.enumeration(toRet);
        }
        return null;
    }
    
    
    
    
}
