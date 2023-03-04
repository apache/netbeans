/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
