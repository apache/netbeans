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

package org.netbeans.modules.spring.beans.completion;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import javax.lang.model.element.Element;
import javax.swing.Action;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ui.ElementJavadoc;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.openide.util.NbBundle;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public abstract class SpringXMLConfigCompletionDoc implements CompletionDocumentation {

    public static SpringXMLConfigCompletionDoc getAttribValueDoc(String text) {
        return new AttribValueDoc(text);
    }
    
    public static SpringXMLConfigCompletionDoc createJavaDoc(CompilationController cc, Element element) {
        return new JavaElementDoc(ElementJavadoc.create(cc, element));
    }
    
    public static SpringXMLConfigCompletionDoc createBeanRefDoc(String beanId, List<String> beanNames, 
            String beanClassName, String beanFileLoc, Action goToBeanAction) {
        return new BeanRefDoc(beanId, beanNames, beanClassName, beanFileLoc, goToBeanAction);
    }

    public URL getURL() {
        return null;
    }

    public CompletionDocumentation resolveLink(String link) {
        return null;
    }

    public Action getGotoSourceAction() {
        return null;
    }
    
    protected static class JavaElementDoc extends SpringXMLConfigCompletionDoc {

        private ElementJavadoc elementJavadoc;

        public JavaElementDoc(ElementJavadoc elementJavadoc) {
            this.elementJavadoc = elementJavadoc;
        }

        @Override
        public JavaElementDoc resolveLink(String link) {
            ElementJavadoc doc = elementJavadoc.resolveLink(link);
            return doc != null ? new JavaElementDoc(doc) : null;
        }

        @Override
        public URL getURL() {
            return elementJavadoc.getURL();
        }

        @Override
        public String getText() {
            return elementJavadoc.getText();
        }

        public Future<String> getFutureText() {
            return elementJavadoc.getTextAsync();
        }

        @Override
        public Action getGotoSourceAction() {
            return elementJavadoc.getGotoSourceAction();
        }
    }

    private static class BeanRefDoc extends SpringXMLConfigCompletionDoc {

        private String beanId;
        private List<String> beanNames;
        private String beanClassName;
        private String beanLocFile;
        private Action goToBeanAction;
        private String displayText;

        public BeanRefDoc(String beanId, List<String> beanNames, String beanClassName, String beanLocFile, Action goToBeanAction) {
            this.beanId = beanId;
            this.beanNames = beanNames;
            this.beanClassName = beanClassName;
            this.beanLocFile = beanLocFile;
            this.goToBeanAction = goToBeanAction;
        }

        public String getText() {
            if (displayText == null) {
                StringBuilder sb = new StringBuilder();
                if (beanLocFile != null) {
                    sb.append("<b>"); // NOI18N
                    sb.append(new File(beanLocFile).getName());
                    sb.append("</b>"); // NOI18N
                    sb.append("<br>"); // NOI18N
                    sb.append("<br>"); // NOI18N
                }
                String beanName = getBeanName();
                List<String> otherNames = new ArrayList<String>(beanNames);
                otherNames.remove(beanName);
                if (otherNames.size() > 0) {
                    sb.append(NbBundle.getMessage(SpringXMLConfigCompletionDoc.class, "LBL_BeanLabelWithAliases",
                            beanName, StringUtils.join(otherNames, ", "))); // NOI18N
                } else {
                    sb.append(NbBundle.getMessage(SpringXMLConfigCompletionDoc.class, "LBL_BeanLabel", beanName));
                }
                if (beanClassName != null) {
                    sb.append("<pre>"); // NOI18N
                    sb.append(beanClassName);
                    sb.append("</pre>"); // NOI18N
                }
                displayText = sb.toString();
            }
            return displayText;
        }

        private String getBeanName() {
            if (beanId != null && beanId.trim().length() > 0) {
                return beanId;
            } else {
                for (String name : beanNames) {
                    if (name != null && name.trim().length() > 0) {
                        return name;
                    }
                }
            }
            return NbBundle.getMessage(SpringXMLConfigCompletionDoc.class, "LBL_Unnamed");
        }

        @Override
        public Action getGotoSourceAction() {
            return goToBeanAction;
        }
    }
    
    private static class AttribValueDoc extends SpringXMLConfigCompletionDoc {

        private String text;

        public AttribValueDoc(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }
}
