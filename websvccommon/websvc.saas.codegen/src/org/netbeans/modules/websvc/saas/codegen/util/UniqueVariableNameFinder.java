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
