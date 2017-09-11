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
