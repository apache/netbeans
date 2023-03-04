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
