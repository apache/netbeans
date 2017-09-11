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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
