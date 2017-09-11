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
