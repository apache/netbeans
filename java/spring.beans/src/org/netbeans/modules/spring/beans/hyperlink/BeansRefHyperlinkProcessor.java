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
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.spring.beans.utils.StringUtils;
import org.netbeans.modules.spring.util.SpringBeansUIs;
import org.netbeans.modules.spring.util.SpringBeansUIs.GoToBeanAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class BeansRefHyperlinkProcessor extends HyperlinkProcessor {

    private boolean globalSearch;

    public BeansRefHyperlinkProcessor(boolean globalSearch) {
        this.globalSearch = globalSearch;
    }

    public void process(HyperlinkEnv env) {
        final FileObject fileObject = env.getFileObject();
        if (fileObject == null) {
            return;
        }
        SpringConfigModel model = SpringConfigModel.forFileObject(fileObject);
        if (model == null) {
            return;
        }
        final String beanName = getBeanName(env);
        final GoToBeanAction[] action = {null};
        try {
            model.runReadAction(new Action<SpringBeans>() {

                public void run(SpringBeans beans) {
                    SpringBean bean;
                    if (globalSearch) {
                        bean = beans.findBean(beanName);
                    } else {
                        bean = beans.getFileBeans(fileObject).findBeanByID(beanName);
                    }

                    if (bean == null) {
                        return;
                    }
                    action[0] = SpringBeansUIs.createGoToBeanAction(bean);
                }
            });
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        if (action[0] != null) {
            action[0].invoke();
        }
    }

    /**
     * Method to retrieve the bean name being clicked by the user. Calculates the 
     * start and end position and returns the substring.
     * @param env - Hyperlink environment
     * @return - Name of the bean under the mouse pointer
     */
    private String getBeanName(HyperlinkEnv env) {
        int beanOffsets[] = getBeanOffsets(env);
        return env.getValueString().substring(beanOffsets[0], beanOffsets[1]);
    }

    /**
     * Method to get the offsets of bean name under mouse pointer
     * @param env - Hyperlink environment
     * @return - Start and End positions of the bean in the token string.
     */
    private int[] getBeanOffsets(HyperlinkEnv env) {
        String valueString = env.getValueString();
        int tokStartIdx = env.getTokenStartOffset() + 1;
        int relOffset = env.getOffset() - tokStartIdx;
        int startPos = Math.max(0, StringUtils.lastIndexOfAnyDelimiter(valueString, 0, relOffset, SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS) + 1);
        int endPos = StringUtils.indexOfAnyDelimiter(valueString, relOffset, SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS);
        if(endPos == -1) {
            endPos = valueString.length();
        }

        return new int[]{startPos, endPos};
    }

    /**
     * Overriden method to return the span of the hyperlink.
     * @param env - Hyperlink environment
     * @return - Start and End positions of the bean in the token string.
     */
    @Override
    public int[] getSpan(HyperlinkEnv env) {
        int beanOffsets[] = getBeanOffsets(env);
        if(!StringUtils.hasText(env.getValueString())) {
            return new int[] {-1, -1};
        }
        
        int tokStartIdx = env.getTokenStartOffset() + 1;
        return new int[]{tokStartIdx + beanOffsets[0], tokStartIdx + beanOffsets[1]};
    }
}
