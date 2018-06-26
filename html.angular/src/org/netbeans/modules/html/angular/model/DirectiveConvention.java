/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.angular.model;

import org.netbeans.modules.web.common.api.LexerUtils;

/**
 * Angular directive attribute form convention.
 *
 * @author marekfukala
 */
public enum DirectiveConvention {
    
    /**
     *  data-ng-app
     */
    data_dash("data", '-'), //NOI18N
    
    /**
     *  data-ng_app
     */
    data_underscore("data", '_'), //NOI18N
    
    /**
     *  data-ng:app //is it possible?
     */
    data_colon("data", ':'), //NOI18N
    
    /**
     *  data-ng-app
     */
    x_dash("x", '-'), //NOI18N
    
    /**
     *  data-ng_app
     */
    x_underscore("x", '_'), //NOI18N
    
    /**
     *  data-ng:app //is it possible?
     */
    x_colon("x", ':'), //NOI18N
    
    //note: base_* members needs to be last - see the getConvention() logic
    
    /**
     * ng-app
     */
    base_dash(null, '-'),
    
    /**
     * ng_app
     */
    base_underscore(null, '_'),
    
    /**
     * ng:app
     */
    base_colon(null, ':');
      
    private static final String NG_PREFIX = "ng"; //NOI18N
    
    private final char delimiter;
    private final String fullPrefix;
    
    private DirectiveConvention(String prefix, char delimiter) {
        this.delimiter = delimiter;
        this.fullPrefix = prefix == null ? NG_PREFIX : prefix + '-' + NG_PREFIX; //XXX is this correct? data-ng_bind? or data_ng_bind???
    }

    String createFQN(Directive directive) {
        StringBuilder sb = new StringBuilder();
        sb.append(fullPrefix);
        sb.append(delimiter);
        sb.append(directive.getAttributeCoreName(delimiter));
        
        return sb.toString();
    }

    /**
     * Checks whether the attribute name fits to one of the AJS conventions.
     * @param attributeName
     * @return the convention or null
     */
    public static DirectiveConvention getConvention(CharSequence attributeName) {
        for(DirectiveConvention dc : values()) {
            //data-ng:bind
            if(LexerUtils.startsWith(attributeName, dc.fullPrefix, true, false)) {
                if(attributeName.length() > dc.fullPrefix.length()) {
                    char delimiterChar = attributeName.charAt(dc.fullPrefix.length());
                    if(dc.delimiter == delimiterChar) {
                        return dc;
                    }
                }
            }
        }
        return null;
    }
    
}
