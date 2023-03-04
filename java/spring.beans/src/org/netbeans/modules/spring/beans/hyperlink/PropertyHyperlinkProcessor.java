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
package org.netbeans.modules.spring.beans.hyperlink;

import java.util.StringTokenizer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.modules.spring.beans.editor.BeanClassFinder;
import org.netbeans.modules.spring.beans.utils.ElementSeekerTask;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.netbeans.modules.spring.java.JavaUtils;
import org.netbeans.modules.spring.java.MatchType;
import org.netbeans.modules.spring.java.Property;
import org.netbeans.modules.spring.java.PropertyFinder;
import org.openide.util.NbBundle;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class PropertyHyperlinkProcessor extends HyperlinkProcessor {

    @NbBundle.Messages("title.property.searching=Property Searching")
    @Override
    public void process(HyperlinkEnv env) {
        String className = new BeanClassFinder(env.getBeanAttributes(),
                env.getFileObject()).findImplementationClass(false);
        if (className == null) {
            return;
        }

        String propChain = getPropertyChainUptoPosition(env);
        if (propChain == null || propChain.equals("")) { //NOI18N
            return;
        }

        JavaSource js = JavaUtils.getJavaSource(env.getFileObject());
        if (js == null) {
            return;
        }

        boolean jumpToGetter = StringUtils.occurs(env.getValueString(), ".", propChain.length()); //NOI18N
        PropertySeekerTask propertySeeker = new PropertySeekerTask(js, className, propChain, jumpToGetter);
        propertySeeker.runAsUserTask();
        if (!propertySeeker.wasElementFound()) {
            ScanDialog.runWhenScanFinished(propertySeeker, Bundle.title_property_searching());
        }
    }

    private class PropertySeekerTask extends ElementSeekerTask {

        private final String className;
        private final String propChain;
        private final boolean jumpToGetter;

        public PropertySeekerTask(JavaSource javaSource, String className, String propChain, boolean jumpToGetter) {
            super(javaSource);
            this.className = className;
            this.propChain = propChain;
            this.jumpToGetter = jumpToGetter;
        }

        @Override
        public void run(CompilationController cc) throws Exception {
            cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            int dotIndex = propChain.lastIndexOf("."); //NOI18N
            if (className == null) {
                return;
            }
            TypeElement te = JavaUtils.findClassElementByBinaryName(className, cc);
            if (te == null) {
                return;
            }
            TypeMirror startType = te.asType();
            ElementUtilities eu = cc.getElementUtilities();

            // property chain
            if (dotIndex != -1) {
                String getterChain = propChain.substring(0, dotIndex);
                StringTokenizer tokenizer = new StringTokenizer(getterChain, "."); //NOI18N
                while (tokenizer.hasMoreTokens() && startType != null) {
                    String propertyName = tokenizer.nextToken();
                    Property[] props = new PropertyFinder(startType, propertyName, eu, MatchType.PREFIX).findProperties();

                    // no matching element found
                    if (props.length == 0 || props[0].getGetter() == null) {
                        startType = null;
                        break;
                    }

                    TypeMirror retType = props[0].getGetter().getReturnType();
                    if (retType.getKind() == TypeKind.DECLARED) {
                        startType = retType;
                    } else {
                        startType = null;
                    }
                }
            }

            if (startType == null) {
                return;
            }

            elementFound.set(true);
            String setterProp = propChain.substring(dotIndex + 1);
            Property[] sProps = new PropertyFinder(startType, setterProp, eu, MatchType.PREFIX).findProperties();
            if (sProps.length > 0) {
                ExecutableElement element = jumpToGetter ? sProps[0].getGetter() : sProps[0].getSetter();
                if (element != null) {
                    ElementOpen.open(cc.getClasspathInfo(), element);
                }
            }
        }
    }

    @Override
    public int[] getSpan(HyperlinkEnv env) {
        int addOffset = env.getTokenStartOffset() + 1;
        String propChain = getPropertyChainUptoPosition(env);
        if(propChain == null || propChain.equals("")) { // NOI18N
            return null;
        }

        int endPos = env.getTokenStartOffset() + propChain.length() + 1;
        int startPos = propChain.lastIndexOf("."); // NOI18N
        startPos = (startPos == -1) ? 0 : ++startPos;
        startPos += addOffset;

        return new int[] { startPos, endPos };
    }

    private String getPropertyChainUptoPosition(HyperlinkEnv env) {
        int relOffset = env.getOffset() - env.getTokenStartOffset() - 1;

        int endPos = env.getValueString().indexOf(".", relOffset); // NOI18N
        // no . after the current pos, return full string
        if(endPos == -1) {
            return env.getValueString();
        } else {
            return env.getValueString().substring(0, endPos);
        }
    }
}
