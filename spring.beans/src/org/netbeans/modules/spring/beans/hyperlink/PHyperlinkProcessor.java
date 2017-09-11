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
