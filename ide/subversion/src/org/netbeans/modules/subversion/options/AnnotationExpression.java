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
