
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.mixeddev.java;

import java.util.Collection;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.services.CsmSymbolResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeProject;
import static org.netbeans.modules.cnd.mixeddev.MixedDevUtils.findCppSymbol;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaEntityInfo;
import org.netbeans.modules.cnd.mixeddev.java.model.JavaMethodInfo;

/**
 *
 */
public class JavaToCppHyperlinkProvider extends AbstractJavaToCppHyperlinkProvider {

    public JavaToCppHyperlinkProvider() {
        // Default no-arg constructor
    }

    @Override
    protected String[] getCppNames(Document doc, int offset) {
        String cppNames[] = null;
        for (NativeNameProvider provider : NativeNameProviders.values()) {
            String nativeNames[] = provider.getNativeNames(doc, offset);
            if (nativeNames != null) {
                cppNames = nativeNames;
                break;
            }
        }      
        return cppNames;
    }

    @Override
    protected boolean navigate(Document doc, int offset) {
        String cppNames[] = getCppNames(doc, offset);
        if (cppNames != null) {
            CsmUtilities.openSource(tryGetDefinition(findCppSymbol(cppNames)));
            return true; // anyway it has to be java to c++ hyperlink
        }
        return false;
    }
    
    private static interface NativeNameProvider {
        
        String[] getNativeNames(Document doc, int offset);
        
    }
    
    private enum NativeNameProviders implements NativeNameProvider {
        JNI {

            @Override
            public String[] getNativeNames(Document doc, int offset) {
                String cppNames[] = null;
                JavaEntityInfo entity = JNISupport.getJNIMethod(doc, offset, true);
                if (entity instanceof JavaMethodInfo) {
                    cppNames = JNISupport.getCppMethodSignatures((JavaMethodInfo) entity);
                }
                return cppNames;
            }
            
        },
        
        JNA {
            
            @Override
            public String[] getNativeNames(Document doc, int offset) {
                String cppName = null;
                JavaEntityInfo entity = JNASupport.getJNAEntity(doc, offset, true);
                if (entity instanceof JavaMethodInfo) {
                    cppName = JNASupport.getCppMethodSignature((JavaMethodInfo) entity);
                }
                return cppName != null ? new String[]{cppName} : null;
            }            
            
        }
    }
    
    private static CsmOffsetable tryGetDefinition(CsmOffsetable candidate) {
        if (CsmKindUtilities.isFunctionDeclaration(candidate)) {
            CsmFunction function = (CsmFunction) candidate;
            CsmFunctionDefinition definition = function.getDefinition();
            return definition != null ? definition : candidate;
        }
        return candidate;
    }    
}
