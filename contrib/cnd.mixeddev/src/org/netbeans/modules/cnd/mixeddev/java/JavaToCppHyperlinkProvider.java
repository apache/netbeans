
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
