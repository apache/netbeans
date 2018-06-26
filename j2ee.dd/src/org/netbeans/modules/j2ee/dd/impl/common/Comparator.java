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

package org.netbeans.modules.j2ee.dd.impl.common;

import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.BaseProperty;
import org.netbeans.modules.j2ee.dd.api.web.*;

/**
 * Customized comparator for web.xml
 *
 * @author  Milan Kuchtiak
 */
public class Comparator extends org.netbeans.modules.schema2beans.BeanComparator
{
    public BaseBean compareBean(String 		beanName,
				BaseBean 	curBean,
				BaseBean 	newBean) {
        if (curBean!=null && newBean!= null) {
            if (curBean instanceof EnclosingBean && newBean instanceof EnclosingBean) {
                if (((EnclosingBean) curBean).getOriginal() == ((EnclosingBean) newBean).getOriginal()) {
                    return curBean;
                }
            }
            if (curBean instanceof KeyBean) {
                String prop = ((KeyBean) curBean).getKeyProperty();
                Object key1 = curBean.getValue(prop);
                Object key2 = newBean.getValue(prop);
                if (key1 != null) {
                    if (key1.equals(key2)) {
                        return curBean;
                    }
                }
            } else {
                if (beanName.equals("SessionConfig")) { //NOI18N
                    return curBean;
                } else if (beanName.equals("WelcomeFileList")) { //NOI18N
                    return curBean;
                } else if (beanName.equals("LoginConfig")) { //NOI18N
                    return curBean;
                } else if (beanName.equals("FormLoginConfig")) { //NOI18N
                    return curBean;
                } else if (beanName.equals("FilterMapping")) { //NOI18N
                    return curBean;
                } else if (beanName.equals("Listener")) { //NOI18N
                    return curBean;
                } else if (beanName.equals("RunAs")) { //NOI18N
                    return curBean;
                } else if (beanName.equals("AuthConstraint")) { //NOI18N
                    return curBean;
                } else if (beanName.equals("UserDataConstraint")) { //NOI18N
                    return curBean;
                } else if (beanName.equals("JspConfig")) { //NOI18N
                    return curBean;
                } else if (beanName.equals("JspPropertyGroup")) { //NOI18N
                    return curBean;
                } else if (beanName.equals("LocaleEncodingMappingList")) { //NOI18N
                    return curBean;
                }
            }
        }
        return super.compareBean(beanName, curBean, newBean);
    }
    
    public Object compareProperty(String 	propertyName,
                                  BaseBean 	curBean,
                                  Object 	curValue,
                                  int		curIndex,
                                  BaseBean	newBean,
                                  Object 	newValue,
                                  int		newIndex) {
        return super.compareProperty(propertyName, curBean,curValue,curIndex,newBean,newValue, newIndex);
    }
}
