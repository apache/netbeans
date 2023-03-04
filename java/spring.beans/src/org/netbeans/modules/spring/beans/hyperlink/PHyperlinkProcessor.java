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

import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.modules.spring.beans.editor.BeanClassFinder;
import org.netbeans.modules.spring.beans.editor.ContextUtilities;
import org.netbeans.modules.spring.beans.utils.ElementSeekerTask;
import org.netbeans.modules.spring.java.JavaUtils;
import org.netbeans.modules.spring.java.MatchType;
import org.netbeans.modules.spring.java.Property;
import org.netbeans.modules.spring.java.PropertyFinder;
import org.openide.util.NbBundle;

/**
 * Hyperlink Processor for p-namespace stuff. Delegates to beanref processor
 * and property processor for computation
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class PHyperlinkProcessor extends HyperlinkProcessor {

    private BeansRefHyperlinkProcessor beansRefHyperlinkProcessor
            = new BeansRefHyperlinkProcessor(true);

    @NbBundle.Messages("title.attribute.searching=Searching Attribute")
    @Override
    public void process(HyperlinkEnv env) {
        String attribName = env.getAttribName();
        if (env.getType().isValueHyperlink()) {
            if (attribName.endsWith("-ref")) { //NOI18N
                beansRefHyperlinkProcessor.process(env);
            }
        } else if (env.getType().isAttributeHyperlink()) {
            String propName = ContextUtilities.getLocalNameFromTag(attribName);
            if (propName.endsWith("-ref")) { //NOI18N
                propName = propName.substring(0, propName.indexOf("-ref")); //NOI18N
            }

            String className = new BeanClassFinder(env.getBeanAttributes(),
                    env.getFileObject()).findImplementationClass(false);
            if (className == null) {
                return;
            }

            JavaSource js = JavaUtils.getJavaSource(env.getFileObject());
            if (js == null) {
                return;
            }

            ClassSeekerTask classSeekerTask = new ClassSeekerTask(js, className, propName);
            classSeekerTask.runAsUserTask();
            if (!classSeekerTask.wasElementFound() && SourceUtils.isScanInProgress()) {
                ScanDialog.runWhenScanFinished(classSeekerTask, Bundle.title_attribute_searching());
            }
        }
    }

    @Override
    public int[] getSpan(HyperlinkEnv env) {
        if (env.getType().isValueHyperlink()) {
            return super.getSpan(env);
        }

        if (env.getType().isAttributeHyperlink()) {
            return new int[] {env.getTokenStartOffset(), env.getTokenEndOffset()};
        }

        return null;
    }

    private class ClassSeekerTask extends ElementSeekerTask {

        private final String className;
        private final String propName;

        public ClassSeekerTask(JavaSource javaSource, String className, String propName) {
            super(javaSource);
            this.className = className;
            this.propName = propName;
        }

        @Override
        public void run(CompilationController cc) throws Exception {
            TypeElement type = JavaUtils.findClassElementByBinaryName(className, cc);
            if (type == null) {
                return;
            }
            elementFound.set(true);
            Property[] props = new PropertyFinder(
                    type.asType(), propName, cc.getElementUtilities(), MatchType.PREFIX).findProperties();
            if (props.length > 0 && props[0].getSetter() != null) {
                ElementOpen.open(cc.getClasspathInfo(), props[0].getSetter());
            }
        }
    }
}
