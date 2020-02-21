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

package org.netbeans.modules.cnd.completion.csm;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmVariable;

/**
 * completion resolver for the file
 * file should be passed somehow in constructor
 * using of resolver:
 *  resolver = createResolver(file);
 * if reusing => resolver.refresh();
 *  if (resolver.resolve(...)) {
 *   result = resolver.getResult();
 *  }
 *
 */
public interface CompletionResolver {
    // flags indicating what we plan to resolve using this resolver
    public static final int RESOLVE_NONE                   = 0;

    public static final int RESOLVE_CONTEXT                = 1 << 0;

    public static final int RESOLVE_CLASSES                = 1 << 1;

    public static final int RESOLVE_GLOB_VARIABLES         = 1 << 2;

    public static final int RESOLVE_GLOB_FUNCTIONS         = 1 << 3;

    public static final int RESOLVE_CLASS_FIELDS           = 1 << 4;

    public static final int RESOLVE_CLASS_METHODS          = 1 << 5;

    public static final int RESOLVE_LOCAL_VARIABLES        = 1 << 6;

    public static final int RESOLVE_LOCAL_CLASSES          = 1 << 7;

    public static final int RESOLVE_FILE_LOCAL_VARIABLES   = 1 << 8;

    public static final int RESOLVE_LIB_CLASSES            = 1 << 9;

    public static final int RESOLVE_LIB_VARIABLES          = 1 << 10;

    public static final int RESOLVE_LIB_FUNCTIONS          = 1 << 11;

    public static final int RESOLVE_LIB_ENUMERATORS        = 1 << 12;

    public static final int RESOLVE_GLOB_ENUMERATORS       = 1 << 13;

    public static final int RESOLVE_FILE_LOCAL_MACROS      = 1 << 14;

    public static final int RESOLVE_FILE_PRJ_MACROS        = 1 << 15;

    public static final int RESOLVE_FILE_LIB_MACROS        = 1 << 16;

    public static final int RESOLVE_GLOB_MACROS            = 1 << 17;

    public static final int RESOLVE_LIB_MACROS             = 1 << 18;

    public static final int RESOLVE_GLOB_NAMESPACES        = 1 << 19;

    public static final int RESOLVE_LIB_NAMESPACES         = 1 << 20;

    public static final int RESOLVE_CLASS_ENUMERATORS      = 1 << 21;

    public static final int RESOLVE_CLASS_NESTED_CLASSIFIERS= 1 << 22;

    public static final int RESOLVE_FILE_LOCAL_FUNCTIONS   = 1 << 23;

    public static final int RESOLVE_CONTEXT_CLASSES        = 1 << 24; // as alternative to RESOLVE_CLASSES

    public static final int RESOLVE_TEMPLATE_PARAMETERS    = 1 << 25;

    public static final int RESOLVE_MACROS                 = RESOLVE_FILE_LOCAL_MACROS | RESOLVE_FILE_PRJ_MACROS | RESOLVE_FILE_LIB_MACROS |
                                                                RESOLVE_GLOB_MACROS | RESOLVE_LIB_MACROS;

    public static final int RESOLVE_FUNCTIONS              = RESOLVE_GLOB_FUNCTIONS | RESOLVE_LIB_FUNCTIONS | RESOLVE_CLASS_METHODS |
                                                                RESOLVE_FILE_LOCAL_FUNCTIONS;

    public static final int RESOLVE_VARIABLES              = RESOLVE_GLOB_VARIABLES | RESOLVE_LIB_VARIABLES | RESOLVE_CLASS_FIELDS | RESOLVE_FILE_LOCAL_VARIABLES;
    
    public static final int FILE_LOCAL_ELEMENTS            = RESOLVE_FILE_LOCAL_FUNCTIONS | RESOLVE_FILE_LOCAL_MACROS | RESOLVE_FILE_LOCAL_VARIABLES;
    
    public static final int RESOLVE_LIB_ELEMENTS           = RESOLVE_LIB_CLASSES | RESOLVE_LIB_ENUMERATORS |
            RESOLVE_LIB_FUNCTIONS | RESOLVE_LIB_MACROS | RESOLVE_LIB_NAMESPACES | RESOLVE_LIB_VARIABLES | RESOLVE_FILE_LIB_MACROS;
    /**
     * specify what to resolve by this resolver
     */
    public void setResolveTypes(int resolveTypes);

    public QueryScope setResolveScope(QueryScope queryScope);
    /**
     * init resolver before using
     * or reinit
     */
    public boolean refresh();

    /**
     * resolve code completion on specified position
     * items should start with specified prefix
     * or must exactly correspond to input prefix string
     */
    public boolean resolve(int offset, String strPrefix, boolean match);

    /**
     * get result of resolving
     */
    public Result getResult();
    
    public static enum QueryScope {
        LOCAL_QUERY,
        SMART_QUERY,
        GLOBAL_QUERY,
    };
    
    public interface Result {
        public Collection<CsmVariable> getLocalVariables();

        public Collection<CsmField> getClassFields();

        public Collection<CsmEnumerator> getClassEnumerators();

        public Collection<CsmMethod> getClassMethods();

        public Collection<CsmClassifier> getProjectClassesifiersEnums();

        public Collection<CsmVariable> getFileLocalVars();

        public Collection<CsmEnumerator> getFileLocalEnumerators();

        public Collection<CsmMacro> getFileLocalMacros();

        public Collection<CsmFunction> getFileLocalFunctions();

        public Collection<CsmMacro> getInFileIncludedProjectMacros();

        public Collection<CsmVariable> getGlobalVariables();

        public Collection<CsmEnumerator> getGlobalEnumerators();

        public Collection<CsmMacro> getGlobalProjectMacros();

        public Collection<CsmFunction> getGlobalProjectFunctions();

        public Collection<CsmNamespace> getGlobalProjectNamespaces();

        public Collection<CsmNamespaceAlias> getProjectNamespaceAliases();

        public Collection<CsmClassifier> getLibClassifiersEnums();

        public Collection<CsmMacro> getInFileIncludedLibMacros();

        public Collection<CsmMacro> getLibMacros();

        public Collection<CsmVariable> getLibVariables();

        public Collection<CsmEnumerator> getLibEnumerators();

        public Collection<CsmFunction> getLibFunctions();

        public Collection<CsmNamespace> getLibNamespaces();
        
        public Collection<CsmNamespaceAlias> getLibNamespaceAliases();

        public Collection<CsmTemplateParameter> getTemplateparameters();

        public Collection<? extends CsmObject> addResulItemsToCol(Collection<? extends CsmObject> orig);
        
        public int size();
        
        public CsmContext getContext();
    }
}
