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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.modules.java.hints.spi.support.FixFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class SuppressWarningsFixer implements ErrorRule<Void> {
    
    /** Creates a new instance of SuppressWarningsFixer */
    public SuppressWarningsFixer() {
    }
    
    private static final Map<String, String> KEY2SUPRESS_KEY;
    
    static {
        Map<String, String> map = new HashMap<String, String>();
        
        String uncheckedKey = "unchecked";
        
        map.put("compiler.warn.prob.found.req", uncheckedKey); // NOI18N
        map.put("compiler.warn.unchecked.cast.to.type", uncheckedKey);  // NOI18N
        map.put("compiler.warn.unchecked.assign", uncheckedKey);  // NOI18N
        map.put("compiler.warn.unchecked.assign.to.var", uncheckedKey);  // NOI18N
        map.put("compiler.warn.unchecked.call.mbr.of.raw.type", uncheckedKey);  // NOI18N
        map.put("compiler.warn.unchecked.meth.invocation.applied", uncheckedKey);  // NOI18N
        map.put("compiler.warn.unchecked.generic.array.creation", uncheckedKey);  // NOI18N
        
        String fallThroughKey = "fallthrough"; // NOI18N
        
        map.put("compiler.warn.possible.fall-through.into.case", fallThroughKey);  // NOI18N
        
        String deprecationKey = "deprecation";  // NOI18N
        
        map.put("compiler.warn.has.been.deprecated", deprecationKey);  // NOI18N
        
        KEY2SUPRESS_KEY = Collections.unmodifiableMap(map); 
    }
    
    public Set<String> getCodes() {
        return KEY2SUPRESS_KEY.keySet();
    }

    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey,
                         int offset, TreePath treePath,
                         Data<Void> data) {
        String suppressKey = KEY2SUPRESS_KEY.get(diagnosticKey);
	
        if (suppressKey != null) {
            return FixFactory.createSuppressWarnings(compilationInfo, treePath, suppressKey);
        }
        
        return Collections.<Fix>emptyList();
    }

    public void cancel() {
    }

    public String getId() {
        return "SuppressWarningsFixer";  // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SuppressWarningsFixer.class, "LBL_Suppress_Waning");  // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(SuppressWarningsFixer.class, "LBL_Suppress_Waning");  // NOI18N
    }

}
