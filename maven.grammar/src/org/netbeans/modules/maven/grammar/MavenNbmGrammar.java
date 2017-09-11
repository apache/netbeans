/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
import org.jdom.Element;
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
