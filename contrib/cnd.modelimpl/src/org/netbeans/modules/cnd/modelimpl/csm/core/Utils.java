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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import static org.netbeans.modules.cnd.api.model.CsmClassifier.SIZEOF_UNKNOWN;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import static org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind.*;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFile.FileType;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.support.CsmFileLanguageProvider;
import org.netbeans.modules.cnd.api.model.services.CsmInheritanceUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.content.project.FileContainer;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelutil.ClassifiersAntiLoop;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.lookup.Lookups;


/**
 * Misc. (static) utility functions
 */
public class Utils {
    
    public static final Logger LOG = Logger.getLogger("org.netbeans.modules.cnd.modelimpl"); // NOI18N
    private static final int LOG_LEVEL = Integer.getInteger("org.netbeans.modules.cnd.modelimpl.level", -1).intValue(); // NOI18N
    
    static {
        // command line param has priority for logging
        // do not change it
        if (LOG_LEVEL == -1) {
            // command line param has priority for logging
            if (TraceFlags.DEBUG) {
                LOG.setLevel(Level.ALL);
            } else {
                LOG.setLevel(Level.SEVERE);
            }
        }
    }
    
    private static final int SIZEOF_SIMPLE_POINTER = 4;
    
    private static final int SIZEOF_SIMPLE_REFERENCE = 4;     
    
    private static final int SIZEOF_SIMPLE_ENUM = 4;    
    
    private static final Map<String, Integer> builtInSizes = new HashMap<>();
    
    static {
        builtInSizes.put("char", 1);     // NOI18N
        builtInSizes.put("short", 2);    // NOI18N
        builtInSizes.put("int", 4);      // NOI18N
        builtInSizes.put("unsigned", 4); // NOI18N
        builtInSizes.put("float", 4);    // NOI18N
        builtInSizes.put("double", 8);   // NOI18N
    }        
    
    private static final ThreadLocal<ClassifiersAntiLoop> threadLocalClassifiersAntiloop = new ThreadLocal<ClassifiersAntiLoop>() {

        @Override
        protected ClassifiersAntiLoop initialValue() {
            return new ClassifiersAntiLoop();        
        }
        
    };   

    /**
     * Constructor is private to prevent instantiation.
     */
    private Utils() {
    }

    private final static class LangProviders {
        private final static Collection<? extends CsmFileLanguageProvider> langProviders = Lookups.forPath(CsmFileLanguageProvider.REGISTRATION_PATH).lookupAll(CsmFileLanguageProvider.class);
    }
    
    public static String getLanguage(CsmFile.FileType fileType, String path) {
        String lang;
        if (!LangProviders.langProviders.isEmpty()) {
            for (org.netbeans.modules.cnd.api.model.support.CsmFileLanguageProvider provider : LangProviders.langProviders) {
                lang = provider.getLanguage(fileType, path);
                if (lang != null) {
                    return lang;
                }
            }
        }
        if (fileType == FileType.SOURCE_CPP_FILE) {
            lang = APTLanguageSupport.GNU_CPP;
        } else if (fileType == FileType.SOURCE_C_FILE) {
            lang = APTLanguageSupport.GNU_C;
        } else if (fileType == FileType.SOURCE_FORTRAN_FILE) {
            lang = APTLanguageSupport.FORTRAN;
        } else {
            lang = APTLanguageSupport.GNU_CPP;
            if (path.length() > 2 && path.endsWith(".c")) { // NOI18N
                lang = APTLanguageSupport.GNU_C;
            }
        }
        return lang;
    }

    public static String getLanguageFlavor(NativeFileItem.LanguageFlavor flavor) {
        if (null != flavor) {
            switch (flavor) {
                case CPP17:
                    return APTLanguageSupport.FLAVOR_CPP17;
                case CPP14:
                    return APTLanguageSupport.FLAVOR_CPP14;
                case CPP11:
                    return APTLanguageSupport.FLAVOR_CPP11;
                case CPP98:
                    return APTLanguageSupport.FLAVOR_CPP98;
                default:
                    break;
            }
        }
        return APTLanguageSupport.FLAVOR_UNKNOWN;
    }
    
    public static CsmOffsetable createOffsetable(CsmFile file, int startOffset, int endOffset) {
        return new Offsetable(file, startOffset, endOffset);
    }
    
    public static CharSequence getQualifiedName(CharSequence name, CsmNamespace parent) {
        if (parent != null && !parent.isGlobal()) {
            return CharSequenceUtils.concatenate(parent.getQualifiedName(), "::", name); // NOI18N
        }
        return name;
    }
      
    public static CharSequence[] splitQualifiedName(String qualified) {
        List<CharSequence> v = new ArrayList<>();
        for (StringTokenizer t = new StringTokenizer(qualified, ": \t\n\r\f", false); t.hasMoreTokens(); ) {// NOI18N 
            v.add(NameCache.getManager().getString(t.nextToken()));
        }
        return v.toArray(new CharSequence[v.size()]);
    }   
    
    public static void disposeAll(Collection<? extends CsmObject> coll) {
        for (CsmObject elem : coll) {
            if( elem  instanceof Disposable ) {
                Disposable decl = (Disposable) elem;
                if (TraceFlags.TRACE_DISPOSE) {
                    System.err.println("disposing with UID " + ((CsmIdentifiable)elem).getUID()); // NOI18N
                }
                decl.dispose();
            } else {
                if (TraceFlags.TRACE_DISPOSE) {
                    System.err.println("non disposable with UID " + ((CsmIdentifiable)elem).getUID()); // NOI18N
                }
            }            
        }
    }

    public static void setSelfUID(CsmObject decl) {
        if (decl instanceof OffsetableIdentifiableBase) {
            ((OffsetableIdentifiableBase)decl).setSelfUID();
        } else {
            throw new IllegalArgumentException("unexpected object:" + decl);//NOI18N
        }
    }

    public static char getCsmIncludeKindKey() {
        // Returned string should be differed from getCsmDeclarationKindkey()
        return 'I'; // NOI18N
    }

    public static char getCsmInheritanceKindKey(CsmInheritance obj) {
        switch (obj.getVisibility()) {
            case PRIVATE:
                return 'h'; // NOI18N
            case PROTECTED:
                return 'y'; // NOI18N
            case PUBLIC:
                return 'H'; // NOI18N
            case NONE:
            default:
                return 'Y'; // NOI18N
        }
        // Returned string should be differed from getCsmDeclarationKindkey()
    }

    public static char getCsmInheritanceKindKey(CsmVisibility obj) {
        switch (obj) {
            case PRIVATE:
                return 'h'; // NOI18N
            case PROTECTED:
                return 'y'; // NOI18N
            case PUBLIC:
                return 'H'; // NOI18N
            case NONE:
            default:
                return 'Y'; // NOI18N
        }
        // Returned string should be differed from getCsmDeclarationKindkey()
    }

    public static CsmVisibility getCsmVisibility(char c) {
        switch (c) {
            case 'h':
                return CsmVisibility.PRIVATE;
            case 'y':
                return CsmVisibility.PROTECTED;
            case 'H':
                return CsmVisibility.PUBLIC;
            case 'Y':
            default:
                return CsmVisibility.NONE;
        }
    }

    public static char getCsmParamListKindKey() {
        // Returned string should be differed from getCsmDeclarationKindkey()
        return 'P'; // NOI18N
    }

    public static char getCsmInstantiationKindKey() {
        // Returned string should be differed from getCsmDeclarationKindkey() and getCsmParamListKindKey()
        return 'i'; // NOI18N
    }
    
    public static CharSequence[] getAllClassifiersUniqueNames(CharSequence uniqueName) {
        CharSequence namePostfix = uniqueName.subSequence(1, uniqueName.length());
        CharSequence out[] = new CharSequence[]
                                {
                                CharSequenceUtils.concatenate(getCsmDeclarationKindkey(CsmDeclaration.Kind.CLASS), namePostfix),
                                CharSequenceUtils.concatenate(getCsmDeclarationKindkey(CsmDeclaration.Kind.STRUCT), namePostfix),
                                CharSequenceUtils.concatenate(getCsmDeclarationKindkey(CsmDeclaration.Kind.UNION), namePostfix),
                                CharSequenceUtils.concatenate(getCsmDeclarationKindkey(CsmDeclaration.Kind.ENUM), namePostfix),
                                CharSequenceUtils.concatenate(getCsmDeclarationKindkey(CsmDeclaration.Kind.TYPEDEF), namePostfix),
                                CharSequenceUtils.concatenate(getCsmDeclarationKindkey(CsmDeclaration.Kind.TYPEALIAS), namePostfix),
                                CharSequenceUtils.concatenate(getCsmDeclarationKindkey(CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION), namePostfix),
                                CharSequenceUtils.concatenate(getCsmDeclarationKindkey(CsmDeclaration.Kind.ENUM_FORWARD_DECLARATION), namePostfix)
                                };
        return out;
    }

    public static char getCsmDeclarationKindkey(CsmDeclaration.Kind kind) {
        // Returned string should be differed from getCsmIncludeKindkey()
        switch (kind) {
            case ASM:
                return 'A'; // NOI18N
            case BUILT_IN:
                return 'B'; // NOI18N
            case CLASS:
                return 'C'; // NOI18N
            case ENUM:
                return 'E'; // NOI18N
            case FUNCTION:
                return 'F'; // NOI18N
            case MACRO:
                return 'M'; // NOI18N
            case NAMESPACE_DEFINITION:
                return 'N'; // NOI18N
            case STRUCT:
                return 'S'; // NOI18N
            case TEMPLATE_DECLARATION:
                return 'T'; // NOI18N
            case UNION:
                return 'U'; // NOI18N
            case VARIABLE:
                return 'V'; // NOI18N
            case NAMESPACE_ALIAS:
                return 'a'; // NOI18N
            case ENUMERATOR:
                return 'e'; // NOI18N
            case FUNCTION_DEFINITION:
                return 'f'; // NOI18N
            case FUNCTION_LAMBDA:
                return 'l'; // NOI18N
            case FUNCTION_INSTANTIATION:
                return 'j'; // NOI18N
            case USING_DIRECTIVE:
                return 'g'; // NOI18N
            case TEMPLATE_PARAMETER:
                return 'p'; // NOI18N
            case CLASS_FRIEND_DECLARATION:
                return 'r'; // NOI18N
            case TEMPLATE_SPECIALIZATION:
                return 's'; // NOI18N
            case TYPEDEF:
                return 't'; // NOI18N
            case TYPEALIAS:
                return 'x'; // NOI18N
            case USING_DECLARATION:
                return 'u'; // NOI18N
            case VARIABLE_DEFINITION:
                return 'v'; // NOI18N
            case CLASS_FORWARD_DECLARATION:
                return 'w'; // NOI18N
            case ENUM_FORWARD_DECLARATION:
                return 'W'; // NOI18N
            case FUNCTION_FRIEND:
                return 'D'; // NOI18N
            case FUNCTION_FRIEND_DEFINITION:
                return 'd'; // NOI18N
            case FUNCTION_TYPE:
                return 'z'; // NOI18N
            default:
                throw new IllegalArgumentException("Unexpected value of CsmDeclaration.Kind:" + kind); //NOI18N
        }
    }

    public static CsmDeclaration.Kind getCsmDeclarationKind(char kind) {
        switch (kind) {
            case 'A': // NOI18N
                return ASM;
            case 'B': // NOI18N
                return BUILT_IN;
            case 'C': // NOI18N
                return CLASS;
            case 'E': // NOI18N
                return ENUM;
            case 'F': // NOI18N
                return FUNCTION;
            case 'M': // NOI18N
                return MACRO;
            case 'N': // NOI18N
                return NAMESPACE_DEFINITION;
            case 'S': // NOI18N
                return STRUCT;
            case 'T': // NOI18N
                return TEMPLATE_DECLARATION;
            case 'U': // NOI18N
                return UNION;
            case 'V': // NOI18N
                return VARIABLE;
            case 'a': // NOI18N
                return NAMESPACE_ALIAS;
            case 'e': // NOI18N
                return ENUMERATOR;
            case 'f': // NOI18N
                return FUNCTION_DEFINITION;
            case 'l': // NOI18N
                return FUNCTION_LAMBDA;
            case 'j': // NOI18N
                return FUNCTION_INSTANTIATION;
            case 'g': // NOI18N
                return USING_DIRECTIVE;
            case 'p': // NOI18N
                return TEMPLATE_PARAMETER;
            case 'r': // NOI18N
                return CLASS_FRIEND_DECLARATION;
            case 's': // NOI18N
                return TEMPLATE_SPECIALIZATION;
            case 't': // NOI18N
                return TYPEDEF;
            case 'x': // NOI18N
                return TYPEALIAS;
            case 'u': // NOI18N
                return USING_DECLARATION;
            case 'v': // NOI18N
                return VARIABLE_DEFINITION;
            case 'w': // NOI18N
                return CLASS_FORWARD_DECLARATION;
            case 'W': // NOI18N
                return ENUM_FORWARD_DECLARATION;
            case 'D': // NOI18N
                return FUNCTION_FRIEND;
            case 'd': // NOI18N
                return FUNCTION_FRIEND_DEFINITION;
            case 'z': // NOI18N
                return FUNCTION_TYPE;
            default:
                throw new IllegalArgumentException("Unexpected char for CsmDeclaration.Kind: " + kind); //NOI18N
        }
    }

    public static boolean canRegisterDeclaration(CsmDeclaration decl) {
        // WAS: don't put unnamed declarations
        assert decl != null;
        assert decl.getName() != null;
        if (decl.getName().length() == 0) {
            return false;
        }
        CsmScope scope = decl.getScope();
        if (scope instanceof CsmCompoundClassifier) {
            return canRegisterDeclaration((CsmCompoundClassifier) scope);
        }
        return true;
    }

    public static <T> LinkedList<T> reverse(LinkedList<T> original) {
        LinkedList<T> reverse = new LinkedList<>();
        ListIterator<T> it = original.listIterator(original.size());
        while(it.hasPrevious()){
           reverse.addLast(it.previous());
        }
        return reverse;
    }

    public static NativeFileItem getCompiledFileItem(FileImpl fileImpl) {
        NativeFileItem out = null;
        ProjectBase filePrj = fileImpl.getProjectImpl(true);
        if (filePrj != null) {
            // use start file from one of states (i.e. first)
            CharSequence fileKey = FileContainer.getFileKey(fileImpl.getAbsolutePath(), false);
            PreprocHandler.State state = filePrj.getFirstValidPreprocState(fileKey);
            if (state == null) {
                return null;
            }
            FileImpl startFile = getStartFile(state);
            out = startFile != null ? startFile.getNativeFileItem() : null;
        }
        return out;
    }

    public static FileImpl getStartFile(final PreprocHandler.State state) {
        StartEntry startEntry = APTHandlersSupport.extractStartEntry(state);
        ProjectBase startProject = getStartProject(startEntry);
        FileImpl csmFile = startProject == null ? null : startProject.getFile(startEntry.getStartFile(), false);
        return csmFile;
    }

    public static ProjectBase getStartProject(final PreprocHandler.State state) {
        return getStartProject(APTHandlersSupport.extractStartEntry(state));
    }

    public static ProjectBase getStartProject(StartEntry startEntry) {
        if (startEntry == null) {
            return null;
        }
        Key key = startEntry.getStartFileProject();
        ProjectBase prj = (ProjectBase) RepositoryUtils.get(key);
        return prj;
    }

    public static boolean isCppFile(CsmFile file) {
        return (file instanceof FileImpl) && ((FileImpl) file).isCppFile();
    }

    public static FileImpl.FileType getFileType(NativeFileItem nativeFile) {
        switch (nativeFile.getLanguage()) {
            case C:
                return FileImpl.FileType.SOURCE_C_FILE;
            case CPP:
                return FileImpl.FileType.SOURCE_CPP_FILE;
            case FORTRAN:
                return FileImpl.FileType.SOURCE_FORTRAN_FILE;
            case C_HEADER:
                return FileImpl.FileType.HEADER_FILE;
            default:
                return FileImpl.FileType.UNDEFINED_FILE;
        }
    }    
    
    public static boolean acceptNativeItem(NativeFileItem item) {
        if (item.getFileObject() == null || !item.getFileObject().isValid()) {
            return false;
        }
        NativeFileItem.Language language = item.getLanguage();
        return (language == NativeFileItem.Language.C ||
                language == NativeFileItem.Language.CPP ||
                language == NativeFileItem.Language.FORTRAN ||
                language == NativeFileItem.Language.C_HEADER) &&
                !item.isExcluded();
    }
    
    public static int getSizeOfType(CsmType type, CsmFile context) {
        if (!CsmBaseUtilities.isValid(type)) {
            return SIZEOF_UNKNOWN;
        }
        
        if (type.isReference()) {
            return getSizeOfReference(type.getContainingFile());
        } else if (type.isPointer()) {
            return getSizeOfPointer(type.getContainingFile());
        } else if (type.getArrayDepth() > 0) {
            // TODO: all arrays have size of classifier*2 until TypeImpl will store its array definition expression
            CsmClassifier cls = type.getClassifier();
            return getSizeOfClassifier(cls, type.getContainingFile());
        } else {
            CsmClassifier cls = type.getClassifier();
            return getSizeOfClassifier(cls, type.getContainingFile());
        }
    }      
    
    public static int getSizeOfClassifier(CsmClassifier classifier, CsmFile context) {
        if (!CsmBaseUtilities.isValid(classifier)) {
            return SIZEOF_UNKNOWN;
        }
        
        if (threadLocalClassifiersAntiloop.get().add(classifier)) {
            try {
                if (CsmKindUtilities.isClass(classifier)) {
                    int size = 0;

                    CsmClass cls = (CsmClass) classifier;
                    // TODO: what about vtable and virtual inheritance?

                    Collection<CsmInheritance> baseClasses = cls.getBaseClasses();
                    for (CsmInheritance inheritance : baseClasses) {
                        CsmClass baseClass = CsmInheritanceUtilities.getCsmClass(inheritance);
                        int baseClassSize = getSizeOfClassifier(baseClass, context);
                        if (baseClassSize == SIZEOF_UNKNOWN) {
                            return SIZEOF_UNKNOWN;
                        }
                        size += baseClassSize;
                    }

                    for (CsmMember member : cls.getMembers()) {
                        if (CsmKindUtilities.isField(member)) {
                            CsmField field = (CsmField) member;
                            if (!field.isStatic()) {
                                CsmType fieldType = field.getType();
                                int fieldSize = getSizeOfType(fieldType, context);
                                if (fieldSize == SIZEOF_UNKNOWN) {
                                    return SIZEOF_UNKNOWN;
                                }
                                size += fieldSize;
                            }
                        }
                    }

                    return Math.max(size, 1);
                } else if (CsmKindUtilities.isEnum(classifier)) {
                    return getSizeOfEnum(context);
                } else if (CsmKindUtilities.isBuiltIn(classifier)) {
                    return getSizeofBuiltIn(classifier.getName(), context);
                } else if (CsmKindUtilities.isTypedef(classifier)) {
                    return getSizeOfType(((CsmTypedef) classifier).getType(), context);
                }
            } finally {
                threadLocalClassifiersAntiloop.get().remove(classifier);
            }
        }
        
        return CsmClassifier.SIZEOF_UNKNOWN;
    }    

    private static int getSizeOfPointer(CsmFile contextFile) {
        return SIZEOF_SIMPLE_POINTER;
    }
    
    private static int getSizeOfReference(CsmFile contextFile) {
        return SIZEOF_SIMPLE_REFERENCE;
    }
    
    private static int getSizeOfEnum(CsmFile contextFile) {
        return SIZEOF_SIMPLE_ENUM;
    }    
    
    private static int getSizeofBuiltIn(CharSequence _name, CsmFile contextFile) {
        String name = _name.toString();
        Integer size = builtInSizes.get(name);
        return size != null ? size : CsmClassifier.SIZEOF_UNKNOWN;
    }
}
