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

package org.netbeans.modules.spring.beans.hyperlink;

import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.java.JavaUtils;
import org.netbeans.modules.spring.java.Public;
import org.netbeans.modules.spring.java.Static;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Hyperlink Processor for factory-method attribute of a bean
 * 
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class FactoryMethodHyperlinkProcessor extends HyperlinkProcessor {

    @Override
    public void process(HyperlinkEnv env) {
        Map<String, String> beanAttributes = env.getBeanAttributes();
        SpringBean mergedBean = SpringXMLConfigEditorUtils.getMergedBean(beanAttributes, env.getFileObject());
        if(mergedBean == null) {
            return;
        }
        Static staticFlag = Static.YES;
        final String[] className = { mergedBean.getClassName() };
        
        // if factory-bean has been defined, resolve it and get it's class name
        if(mergedBean.getFactoryBean() != null) { 
            final String factoryBeanName = mergedBean.getFactoryBean();
            FileObject fo = env.getFileObject();
            if(fo == null) {
                return;
            }
            SpringConfigModel model = SpringConfigModel.forFileObject(fo);
            if (model != null) {
                try {
                    model.runReadAction(new Action<SpringBeans>() {
                        @Override
                        public void run(SpringBeans beans) {
                            SpringBean bean = beans.findBean(factoryBeanName);
                            if (bean == null) {
                                className[0] = null;
                                return;
                            }
                            className[0] = bean.getClassName();
                        }
                    });
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                    className[0] = null;
                }
            } else {
                className[0] = null;
            }

            staticFlag = Static.NO;
        }
        
        if (className[0] != null) {
            String methodName = mergedBean.getFactoryMethod();
            JavaUtils.openMethodInEditor(env.getFileObject(), className[0], methodName, -1,
                    Public.DONT_CARE, staticFlag);
        }
    }
}
