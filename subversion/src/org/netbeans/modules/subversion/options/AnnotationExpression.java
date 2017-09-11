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
package org.netbeans.modules.subversion.options;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class AnnotationExpression {
    
    private String urlExp;
    private String annotationExp;        
    private Pattern urlPattern;        
    
    public AnnotationExpression(String urlExp, String annotationExp) {
        this.urlExp = urlExp;
        this.annotationExp = annotationExp;
        this.urlPattern = Pattern.compile(urlExp);       
    }         
    public String getUrlExp() {
        return urlExp;        
    }
    public String getAnnotationExp() {
        return annotationExp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AnnotationExpression other = (AnnotationExpression) obj;
        if (this.urlExp != other.urlExp && (this.urlExp == null || !this.urlExp.equals(other.urlExp))) {
            return false;
        }
        if (this.annotationExp != other.annotationExp && (this.annotationExp == null || !this.annotationExp.equals(other.annotationExp))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
    
    public Pattern getUrlPatern() {
        return urlPattern;
    }
    void setUrlExp(String urlExp) {
        this.urlExp = urlExp;        
    }    
    void setAnnotationExp(String annotationExp) {
        this.annotationExp = annotationExp;
    }            
    
    public String getCopyName(String url) {
        Matcher m = getUrlPatern().matcher(url);
        if (m.matches()) {
            String ae = getAnnotationExp();

            StringBuffer copyName = new StringBuffer();
            StringBuffer groupStr = new StringBuffer();                    
            boolean inGroup = false;

            for (int i = 0; i < ae.length(); i++) {
                char c = ae.charAt(i);
                if(c == '\\') {
                    inGroup = true;                                                                      
                    continue;
                } else if(inGroup) {
                    if(Character.isDigit(c)) {                                
                        groupStr.append(c);                                                                                                                                            
                    } else {
                        if(groupStr.length() > 0) {
                            try {
                                int group = Integer.valueOf(groupStr.toString()).intValue();    
                                copyName.append(m.group(group));
                            } catch (Exception e) {
                                copyName.append('\\');
                                copyName.append(groupStr);
                            }
                            groupStr = new StringBuffer();                    
                        } else {
                            copyName.append('\\');
                            copyName.append(c);
                        }                                
                        inGroup = false;
                    }                                                                
                    continue;                            
                }
                copyName.append(c);
            }
            if(groupStr.length() > 0) {
                try {
                    int group = Integer.valueOf(groupStr.toString()).intValue();
                    copyName.append(m.group(group));
                } catch (Exception e) {
                    copyName.append('\\');
                    copyName.append(groupStr);
                }                                            
            }
            return copyName.toString();     
        }
        return null;
    }
    
    
}
