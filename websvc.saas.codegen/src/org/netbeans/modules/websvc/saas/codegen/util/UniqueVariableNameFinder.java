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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.websvc.saas.codegen.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSParameter;
import org.netbeans.modules.websvc.saas.codegen.Constants;
import org.netbeans.modules.websvc.saas.codegen.model.ParameterInfo;

/**
 * Code for finding unique variable names
 *
 * @author ayubskhan
 */
public class UniqueVariableNameFinder {

    private Map<String, Integer> varDeclMap = new HashMap<String, Integer>();

    public UniqueVariableNameFinder() {
    }

    public void addPattern(String pattern, int count) {
        if(varDeclMap.containsKey(pattern))
            varDeclMap.remove(pattern);
        varDeclMap.put(pattern, new Integer(count));
    }

    public int getPatternCount(String pattern) {
        Integer count = varDeclMap.get(pattern);
        return count != null?count.intValue():0;
    }

    public String getVariableDecl(ParameterInfo p) {
        if (p == null) {
            return "";
        }
        return p.getType().getSimpleName() + " " +
                Util.getParameterName(p, true, true, true);
    }

    public String getVariableDecl(WSParameter p) {
        if (p == null) {
            return "";
        }
      
        return p.getTypeName() + " " + p.getName();
    }
    
    public String getVariableCount(String varName) {
        return getPatternCount(varName) > 0 ? getPatternCount(varName) + "" : "";
    }

    public void updateVariableDecl(String text, List<ParameterInfo> params) throws BadLocationException {
        for (ParameterInfo p : params) {
            updateVariableDecl(text, getVariableDecl(p));
        }
    }
    
    public void updateVariableDeclForWS(String text, List<? extends WSParameter> params) throws BadLocationException {
        for (WSParameter p : params) {
            updateVariableDecl(text, getVariableDecl(p));
        }
    }
    
    public void updateVariableDecl(String text, String pattern) throws BadLocationException {
        int count = 0;
        int ndx = -1;
        while ((ndx = text.indexOf(pattern, ndx + 1)) > -1) {
            count++;
        }
        if (count > 0) {
            addPattern(pattern, count);
        }
    }
    
    public String findNewName(String pattern, String oldName) {      
        Integer pCount = varDeclMap.get(pattern);
  
        if (pCount != null) {
            return oldName + pCount.intValue();
        }
        return oldName;
    }
    
    public List<ParameterInfo> renameParameterNames(List<ParameterInfo> params) {
        List<ParameterInfo> returnParams = new ArrayList<ParameterInfo>();
        for (ParameterInfo p : params) {
            String oldName = Util.getParameterName(p, true, true, true);
            String newName = oldName;
            if (!(Constants.HTTP_SERVLET_REQUEST_VARIABLE.equals(oldName) ||
                    Constants.HTTP_SERVLET_RESPONSE_VARIABLE.equals(oldName))) {
                newName = findNewName(getVariableDecl(p), oldName);
            }
    
            if (!newName.equals(oldName)) {
                ParameterInfo clone = clone(p, newName, p.getType());
                returnParams.add(clone);
            } else {
                returnParams.add(p);
            }
        }
        return returnParams;
    }

    private ParameterInfo clone(ParameterInfo p, String name, Class type) {
        ParameterInfo clone = new ParameterInfo(name, type);
        clone.setFixed(p.getFixed());
        clone.setStyle(p.getStyle());
        clone.setDefaultValue(p.getDefaultValue());
        clone.setIsApiKey(p.isApiKey());
        clone.setId(p.getId());
        clone.setIsRequired(p.isRequired());
        clone.setIsRepeating(p.isRepeating());
        clone.setIsSessionKey(p.isSessionKey());
        clone.setOption(p.getOption());
        return clone;
    }

    public void clearPatterns() {
        varDeclMap.clear();
    }
}
