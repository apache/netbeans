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

package org.netbeans.modules.java.completion;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.swing.text.Document;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.support.ReferencesCount;
import org.netbeans.api.lexer.Language;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.Source;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.EntityCatalog;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Dusan Balek, Jan Lahoda
 */
public class CompletionTestBase extends CompletionTestBaseBase {
    
    public CompletionTestBase(String testName) {
        super(testName, "org/netbeans/modules/java/completion/JavaCompletionTaskTest");
    }
    
    protected void performTest(String source, int caretPos, String textToInsert, String goldenFileName) throws Exception {
        performTest(source, caretPos, textToInsert, goldenFileName, "1.5");
    }
    
    protected void performTest(String source, int caretPos, String textToInsert, String goldenFileName, String sourceLevel) throws Exception {
        this.sourceLevel.set(sourceLevel);
        File testSource = new File(getWorkDir(), "test/Test.java");
        testSource.getParentFile().mkdirs();
        copyToWorkDir(new File(getDataDir(), "org/netbeans/modules/java/completion/data/" + source + ".java"), testSource);
        FileObject testSourceFO = FileUtil.toFileObject(testSource);
        assertNotNull(testSourceFO);
        DataObject testSourceDO = DataObject.find(testSourceFO);
        assertNotNull(testSourceDO);
        EditorCookie ec = (EditorCookie) testSourceDO.getCookie(EditorCookie.class);
        assertNotNull(ec);
        final Document doc = ec.openDocument();
        assertNotNull(doc);
        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");
        int textToInsertLength = textToInsert != null ? textToInsert.length() : 0;
        if (textToInsertLength > 0) {
            doc.insertString(caretPos, textToInsert, null);
        }
        Source s = Source.create(doc);
        JavaCompletionTask<CI> task = JavaCompletionTask.create(caretPos + textToInsertLength, new CIFactory(), EnumSet.noneOf(JavaCompletionTask.Options.class), null);
        ParserManager.parse(Collections.singletonList(s), task);
        List<CI> items = task.getResults();
        Collections.sort(items);
        
        File output = new File(getWorkDir(), getName() + ".out");
        try (Writer out = new FileWriter(output)) {
            for (Object item : items) {
                String itemString = item.toString();
                if (!(org.openide.util.Utilities.isMac() && itemString.equals("apple") //ignoring 'apple' package
                        || itemString.equals("jdk")        //ignoring 'jdk' package introduced by jdk1.7.0_40
                        || itemString.equals("netscape")   //ignoring 'netscape' package present in some JDK builds
                        || itemString.equals("nbjavac")   //ignoring 'nbjavac' package from nbjavac
                        || itemString.equals("oracle"))) { //ignoring 'oracle' package present in some JDK builds
                    out.write(itemString);
                    out.write("\n");
                }
            }
        }
        
        
        File goldenFile = getGoldenFile(goldenFileName);
        File diffFile = new File(getWorkDir(), getName() + ".diff");        
        assertFile(output, goldenFile, diffFile);
        
        LifecycleManager.getDefault().saveAll();
    }

    private static class CIFactory implements JavaCompletionTask.ModuleItemFactory<CI>, JavaCompletionTask.RecordPatternItemFactory<CI> {

        private static final int SMART_TYPE = 1000;
        @Override
        public CI createKeywordItem(String kwd, String postfix, int substitutionOffset, boolean smartType) {
            return new CI(kwd, smartType ? 670 - SMART_TYPE : 670, kwd);
        }

        @Override
        public CI createModuleItem(String moduleName, int substitutionOffset) {
            return new CI(moduleName, 950, moduleName); //NOI18N
        }

        @Override
        public CI createPackageItem(String pkgFQN, int substitutionOffset, boolean inPackageStatement) {
            int idx = pkgFQN.lastIndexOf('.');
            String simpleName = idx < 0 ? pkgFQN : pkgFQN.substring(idx + 1);
            return new CI(simpleName, 900, simpleName + "#" + pkgFQN); //NOI18N
        }

        @Override
        public CI createTypeItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars, boolean addSimpleName, boolean smartType, boolean autoImportEnclosingType) {
            String simpleName = elem.getSimpleName().toString();
            String fqn = elem.getQualifiedName().toString();
            int weight = 50;
            if (fqn.startsWith("java.lang") || fqn.startsWith("java.util")) { // NOI18N
                weight -= 10;
            } else if (fqn.startsWith("org.omg") || fqn.startsWith("org.apache")) { // NOI18N
                weight += 10;
            } else if (fqn.startsWith("com.sun") || fqn.startsWith("com.ibm") || fqn.startsWith("com.apple")) { // NOI18N
                weight += 20;
            } else if (fqn.startsWith("sun") || fqn.startsWith("sunw") || fqn.startsWith("netscape")) { // NOI18N
                weight += 30;
            }
            return new CI(simpleName, smartType ? 800 - SMART_TYPE : 800, referencesCount != null ? simpleName + '#' + weight + '#' + info.getElementUtilities().getElementName(elem.getEnclosingElement(), true) : simpleName);
        }

        @Override
        public CI createTypeItem(ElementHandle<TypeElement> handle, EnumSet<ElementKind> kinds, int substitutionOffset, ReferencesCount referencesCount, Source source, boolean insideNew, boolean addTypeVars, boolean afterExtends) {
            String fqn = handle.getQualifiedName();
            int idx = fqn.lastIndexOf('.');
            String simpleName = idx > -1 ? fqn.substring(idx + 1) : fqn;
            int weight = 50;
            if (fqn.startsWith("java.lang") || fqn.startsWith("java.util")) { // NOI18N
                weight -= 10;
            } else if (fqn.startsWith("org.omg") || fqn.startsWith("org.apache")) { // NOI18N
                weight += 10;
            } else if (fqn.startsWith("com.sun") || fqn.startsWith("com.ibm") || fqn.startsWith("com.apple")) { // NOI18N
                weight += 20;
            } else if (fqn.startsWith("sun") || fqn.startsWith("sunw") || fqn.startsWith("netscape")) { // NOI18N
                weight += 30;
            }
            return new CI(fqn, 700, simpleName + '#' + weight + '#' + (idx > -1 ? fqn.substring(0, idx) : "")); //NOI18N
        }

        @Override
        public CI createArrayItem(CompilationInfo info, ArrayType type, int substitutionOffset, ReferencesCount referencesCount, Elements elements) {
            int dim = 0;
            TypeMirror tm = type;
            while(tm.getKind() == TypeKind.ARRAY) {
                tm = ((ArrayType)tm).getComponentType();
                dim++;
            }
            if (tm.getKind().isPrimitive()) {
                String kwd = tm.toString();
                StringBuilder sb = new StringBuilder(kwd);
                for(int i = 0; i < dim; i++) {
                    sb.append("[]"); //NOI18N
                }
                return new CI(sb.toString(), 670 - SMART_TYPE, kwd);
            }
            if (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ERROR) {
                DeclaredType dt = (DeclaredType)tm;
                TypeElement elem = (TypeElement)dt.asElement();
                String simpleName = elem.getSimpleName().toString();
                String fqn = elem.getQualifiedName().toString();
                int weight = 50;
                if (fqn.startsWith("java.lang") || fqn.startsWith("java.util")) { // NOI18N
                    weight -= 10;
                } else if (fqn.startsWith("org.omg") || fqn.startsWith("org.apache")) { // NOI18N
                    weight += 10;
                } else if (fqn.startsWith("com.sun") || fqn.startsWith("com.ibm") || fqn.startsWith("com.apple")) { // NOI18N
                    weight += 20;
                } else if (fqn.startsWith("sun") || fqn.startsWith("sunw") || fqn.startsWith("netscape")) { // NOI18N
                    weight += 30;
                }
                return new CI(simpleName, 800 - SMART_TYPE, referencesCount != null ? simpleName + '#' + weight + '#' + info.getElementUtilities().getElementName(elem.getEnclosingElement(), true) : simpleName);
            }
            throw new IllegalArgumentException("array element kind=" + tm.getKind());
        }

        @Override
        public CI createTypeParameterItem(TypeParameterElement elem, int substitutionOffset) {
            String simpleName = elem.getSimpleName().toString();
            return new CI(simpleName, 700, simpleName);
        }

        @Override
        public CI createVariableItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean smartType, int assignToVarOffset) {
            String varName = elem.getSimpleName().toString();
            String typeName = type != null ? info.getTypeUtilities().getTypeName(type).toString() : null;
            ElementKind ek = elem.getKind();
            switch (ek) {
                case LOCAL_VARIABLE:
                case RESOURCE_VARIABLE:
                case PARAMETER:
                case EXCEPTION_PARAMETER:
                case BINDING_VARIABLE:
                    return new CI((typeName != null ? typeName + " " : "") + varName, smartType ? 200 - SMART_TYPE : 200, varName);
                case ENUM_CONSTANT:
                case FIELD:
                    StringBuilder sb = new StringBuilder();
                    for(Modifier mod : elem.getModifiers()) {
                       sb.append(mod.toString());
                       sb.append(' ');
                    }
                    sb.append(typeName);
                    sb.append(' ');
                    sb.append(varName);
                    return new CI(sb.toString(), smartType ? 300 - SMART_TYPE : 300, varName + "#" + (referencesCount != null ? elem.getEnclosingElement().getSimpleName().toString() + "#" : "")); //NOI18N
                default:
                    throw new IllegalArgumentException("kind=" + elem.getKind());
            }
        }

        @Override
        public CI createVariableItem(CompilationInfo info, String varName, int substitutionOffset, boolean newVarName, boolean smartType) {
            return new CI(varName, smartType ? 200 - SMART_TYPE : 200, varName);
        }

        @Override
        public CI createExecutableItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, ReferencesCount referencesCount, boolean isInherited, boolean isDeprecated, boolean inImport, boolean addSemicolon, boolean smartType, int assignToVarOffset, boolean memberRef) {
            String simpleName = elem.getKind() == ElementKind.CONSTRUCTOR ? elem.getEnclosingElement().getSimpleName().toString() : elem.getSimpleName().toString();
            StringBuilder sb = new StringBuilder();
            StringBuilder sortParams = new StringBuilder();
            int cnt = 0;
            for (Modifier mod : elem.getModifiers()) {
                sb.append(mod.toString());
                sb.append(' ');
            }
            if (elem.getKind() == ElementKind.METHOD) {
                sb.append(info.getTypeUtilities().getTypeName(type.getReturnType()));
                sb.append(' ');
            }
            sb.append(simpleName);
            sb.append('(');
            sortParams.append('(');
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                if (tm == null) {
                    break;
                }
                Set<TypeUtilities.TypeNameOptions> options = EnumSet.noneOf(TypeUtilities.TypeNameOptions.class);
                if (elem.isVarArgs() && !tIt.hasNext()) {
                    options.add(TypeUtilities.TypeNameOptions.PRINT_AS_VARARG);
                }
                String typeName = info.getTypeUtilities().getTypeName(tm, options.toArray(new TypeUtilities.TypeNameOptions[0])).toString();
                sb.append(typeName);
                sb.append(' ');
                sb.append(it.next().getSimpleName());
                sortParams.append(typeName);
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                    sortParams.append(',');
                }
                cnt++;
            }
            sb.append(')');
            sortParams.append(')');
            switch (elem.getKind()) {
                case METHOD:
                    return new CI(sb.toString(), smartType ? 500 - SMART_TYPE : 500, simpleName + "#" + (referencesCount != null ? elem.getEnclosingElement().getSimpleName().toString() + "#" : "") + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString());
                case CONSTRUCTOR:
                   return new CI(sb.toString(), smartType ? 650 - SMART_TYPE : 650, simpleName + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString());
                default:
                    throw new IllegalArgumentException("kind=" + elem.getKind());
            }
        }

        @Override
        public CI createThisOrSuperConstructorItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, String name) {
            if (elem.getKind() == ElementKind.CONSTRUCTOR) {
                StringBuilder sb = new StringBuilder();
                StringBuilder sortParams = new StringBuilder();
                int cnt = 0;
                for (Modifier mod : elem.getModifiers()) {
                    sb.append(mod.toString());
                    sb.append(' ');
                }
                if (elem.getKind() == ElementKind.METHOD) {
                    sb.append(info.getTypeUtilities().getTypeName(type.getReturnType()));
                    sb.append(' ');
                }
                sb.append(name);
                sb.append('(');
                sortParams.append('(');
                Iterator<? extends VariableElement> it = elem.getParameters().iterator();
                Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
                while(it.hasNext() && tIt.hasNext()) {
                    TypeMirror tm = tIt.next();
                    if (tm == null) {
                        break;
                    }
                    Set<TypeUtilities.TypeNameOptions> options = EnumSet.noneOf(TypeUtilities.TypeNameOptions.class);
                    if (elem.isVarArgs() && !tIt.hasNext()) {
                        options.add(TypeUtilities.TypeNameOptions.PRINT_AS_VARARG);
                    }
                    String typeName = info.getTypeUtilities().getTypeName(tm, options.toArray(new TypeUtilities.TypeNameOptions[0])).toString();
                    sb.append(typeName);
                    sb.append(' ');
                    sb.append(it.next().getSimpleName());
                    sortParams.append(typeName);
                    if (it.hasNext()) {
                        sb.append(", "); //NOI18N
                        sortParams.append(',');
                    }
                    cnt++;
                }
                sb.append(')');
                sortParams.append(')');
                return new CI(sb.toString(), 550, name + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString());
            }
            throw new IllegalArgumentException("kind=" + elem.getKind());
        }

        @Override
        public CI createOverrideMethodItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean implement) {
            switch (elem.getKind()) {
                case METHOD:
                    String simpleName = elem.getSimpleName().toString();
                    StringBuilder sb = new StringBuilder();
                    StringBuilder sortParams = new StringBuilder();
                    int cnt = 0;
                    for (Modifier mod : elem.getModifiers()) {
                        sb.append(mod.toString());
                        sb.append(' ');
                    }
                    if (elem.getKind() == ElementKind.METHOD) {
                        sb.append(info.getTypeUtilities().getTypeName(type.getReturnType()));
                        sb.append(' ');
                    }
                    sb.append(simpleName);
                    sb.append('(');
                    sortParams.append('(');
                    Iterator<? extends VariableElement> it = elem.getParameters().iterator();
                    Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
                    while(it.hasNext() && tIt.hasNext()) {
                        TypeMirror tm = tIt.next();
                        if (tm == null) {
                            break;
                        }
                        Set<TypeUtilities.TypeNameOptions> options = EnumSet.noneOf(TypeUtilities.TypeNameOptions.class);
                        if (elem.isVarArgs() && !tIt.hasNext()) {
                            options.add(TypeUtilities.TypeNameOptions.PRINT_AS_VARARG);
                        }
                        String typeName = info.getTypeUtilities().getTypeName(tm, options.toArray(new TypeUtilities.TypeNameOptions[0])).toString();
                        sb.append(typeName);
                        sb.append(' ');
                        sb.append(it.next().getSimpleName());
                        sortParams.append(typeName);
                        if (it.hasNext()) {
                            sb.append(", "); //NOI18N
                            sortParams.append(',');
                        }
                        cnt++;
                    }
                    sb.append(") - ");
                    sortParams.append(')');
                    sb.append(implement ? "implement" : "override");
                    return new CI(sb.toString(), 500, simpleName + "##" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString() + " - " + (implement ? "implement" : "override"));
                default:
                    throw new IllegalArgumentException("kind=" + elem.getKind());
            }
        }

        @Override
        public CI createGetterSetterMethodItem(CompilationInfo info, VariableElement elem, TypeMirror type, int substitutionOffset, String name, boolean setter) {
            switch (elem.getKind()) {
                case ENUM_CONSTANT:
                case FIELD:
                    CodeStyle cs = null;
                    try {
                        cs = CodeStyle.getDefault(info.getDocument());
                    } catch (IOException ex) {
                    }
                    if (cs == null) {
                        cs = CodeStyle.getDefault(info.getFileObject());
                    }
                    boolean isStatic = elem.getModifiers().contains(Modifier.STATIC);
                    String simpleName = CodeStyleUtils.removePrefixSuffix(elem.getSimpleName(),
                        isStatic ? cs.getStaticFieldNamePrefix() : cs.getFieldNamePrefix(),
                        isStatic ? cs.getStaticFieldNameSuffix() : cs.getFieldNameSuffix());
                    String paramName = CodeStyleUtils.addPrefixSuffix(
                            simpleName,
                            cs.getParameterNamePrefix(),
                            cs.getParameterNameSuffix());
                    String typeName = info.getTypeUtilities().getTypeName(type).toString();
                    StringBuilder sb = new StringBuilder();
                    StringBuilder sortParams = new StringBuilder();
                    sb.append("public "); //NOI18N
                    sb.append(setter ? "void" : typeName); //NOI18N
                    sb.append(' ');
                    sb.append(name);
                    sb.append('(');
                    sortParams.append('(');
                    if (setter) {
                        sb.append(typeName);
                        sb.append(' ');
                        sb.append(paramName);
                        sortParams.append(typeName);
                    }
                    sb.append(") - generate"); //NOI18N
                    sortParams.append(')'); //NOI18N
                    return new CI(sb.toString(), 500, name + "#" + (setter ? "01" : "00") + "#" + sortParams.toString());
                default:
                    throw new IllegalArgumentException("kind=" + elem.getKind());
            }
        }

        @Override
        public CI createDefaultConstructorItem(TypeElement elem, int substitutionOffset, boolean smartType) {
            String simpleName = elem.getSimpleName().toString();
            return new CI(simpleName + "()", smartType ? 650 - SMART_TYPE : 650, simpleName + "#0#");
        }
        
        @Override
        public CI createRecordPatternItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated, boolean insideNew, boolean addTypeVars) {
            String simpleName = elem.getSimpleName().toString();
            StringBuilder sb = new StringBuilder();
            StringBuilder sortParams = new StringBuilder();
            sb.append(simpleName);
            sb.append('(');
            sortParams.append('(');
            Iterator<? extends RecordComponentElement> it = elem.getRecordComponents().iterator();
            RecordComponentElement recordComponent;
            String name;
            int cnt = 0;
            while (it.hasNext()) {
                recordComponent = it.next();
                name = recordComponent.getAccessor().getReturnType().toString();
                sortParams.append(name);
                sb.append(name.substring(name.lastIndexOf(".") + 1));
                sb.append(" ");
                sb.append(recordComponent.getSimpleName().toString());
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                }
                cnt++;
            }
            sb.append(")");
            return new CI(sb.toString(), 650, "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString());
        }

        @Override
        public CI createParametersItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated, int activeParamIndex, String name) {
            String simpleName = name != null ? name : elem.getKind() == ElementKind.CONSTRUCTOR ? elem.getEnclosingElement().getSimpleName().toString() : elem.getSimpleName().toString();
            StringBuilder sb = new StringBuilder();
            StringBuilder sortParams = new StringBuilder();
            int cnt = 0;
            sb.append(info.getTypeUtilities().getTypeName(type.getReturnType()));
            sb.append(' ');
            sb.append(simpleName);
            sb.append('(');
            sortParams.append('(');
            Iterator<? extends VariableElement> it = elem.getParameters().iterator();
            Iterator<? extends TypeMirror> tIt = type.getParameterTypes().iterator();
            while(it.hasNext() && tIt.hasNext()) {
                TypeMirror tm = tIt.next();
                if (tm == null) {
                    break;
                }
                Set<TypeUtilities.TypeNameOptions> options = EnumSet.noneOf(TypeUtilities.TypeNameOptions.class);
                if (elem.isVarArgs() && !tIt.hasNext()) {
                    options.add(TypeUtilities.TypeNameOptions.PRINT_AS_VARARG);
                }
                String typeName = info.getTypeUtilities().getTypeName(tm, options.toArray(new TypeUtilities.TypeNameOptions[0])).toString();
                sb.append(typeName);
                sb.append(' ');
                sb.append(it.next().getSimpleName());
                sortParams.append(typeName);
                if (it.hasNext()) {
                    sb.append(", "); //NOI18N
                    sortParams.append(',');
                }
                cnt++;
            }
            sb.append(") - parameters"); //NOI18N
            sortParams.append(')');
            return new CI(sb.toString(), 100 - SMART_TYPE, "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString());
        }

        @Override
        public CI createAnnotationItem(CompilationInfo info, TypeElement elem, DeclaredType type, int substitutionOffset, ReferencesCount referencesCount, boolean isDeprecated) {
            return createTypeItem(info, elem, type, substitutionOffset, referencesCount, isDeprecated, false, false, false, true, false);
        }

        @Override
        public CI createAttributeItem(CompilationInfo info, ExecutableElement elem, ExecutableType type, int substitutionOffset, boolean isDeprecated) {
            String simpleName = elem.getSimpleName().toString();
            return new CI(simpleName, 100, simpleName);
        }

        @Override
        public CI createAttributeValueItem(CompilationInfo info, String value, String documentation, TypeElement element, int substitutionOffset, ReferencesCount referencesCount) {
            return new CI(value, -SMART_TYPE, element != null ? createTypeItem(info, element, (DeclaredType)element.asType(), substitutionOffset, referencesCount, false, false, false, false, false, false).sortText : value);
        }

        @Override
        public CI createStaticMemberItem(CompilationInfo info, DeclaredType type, Element memberElem, TypeMirror memberType, boolean multipleVersions, int substitutionOffset, boolean isDeprecated, boolean addSemicolon) {
            switch (memberElem.getKind()) {
                case METHOD:
                case ENUM_CONSTANT:
                case FIELD:
                    String memberName = memberElem.getSimpleName().toString();
                    StringBuilder sb = new StringBuilder();
                    StringBuilder sortParams = new StringBuilder();
                    String typeName = info.getTypeUtilities().getTypeName(type).toString();
                    int cnt = 0;
                    for(Modifier mod : memberElem.getModifiers()) {
                       sb.append(mod.toString());
                       sb.append(' '); // NOI18N
                    }
                    sb.append(info.getTypeUtilities().getTypeName(memberElem.getKind().isField() ? memberType : ((ExecutableType)memberType).getReturnType()));
                    sb.append(' ');
                    sb.append(typeName);
                    sb.append('.');
                    sb.append(memberName);
                    if (!memberElem.getKind().isField()) {
                        sb.append('(');
                        sortParams.append('(');
                        if (multipleVersions) {
                            sb.append("..."); //NOI18N
                            sortParams.append("..."); //NOI18N
                        } else {
                            Iterator<? extends VariableElement> it = ((ExecutableElement)memberElem).getParameters().iterator();
                            Iterator<? extends TypeMirror> tIt = ((ExecutableType)memberType).getParameterTypes().iterator();
                            while(it.hasNext() && tIt.hasNext()) {
                                TypeMirror tm = tIt.next();
                                if (tm == null) {
                                    break;
                                }
                                Set<TypeUtilities.TypeNameOptions> options = EnumSet.noneOf(TypeUtilities.TypeNameOptions.class);
                                if (((ExecutableElement)memberElem).isVarArgs() && !tIt.hasNext()) {
                                    options.add(TypeUtilities.TypeNameOptions.PRINT_AS_VARARG);
                                }
                                String paramTypeName = info.getTypeUtilities().getTypeName(tm, options.toArray(new TypeUtilities.TypeNameOptions[0])).toString();
                                sb.append(paramTypeName);
                                sortParams.append(paramTypeName);
                                sb.append(' ');
                                sb.append(it.next().getSimpleName());
                                if (it.hasNext()) {
                                    sb.append(", "); //NOI18N
                                    sortParams.append(',');
                                }
                                cnt++;
                            }
                        }
                        sb.append(')');
                        sortParams.append(')');
                    }
                    return new CI(sb.toString(), (memberElem.getKind().isField() ? 720 : 750) - SMART_TYPE, memberElem.getKind().isField() ? memberName + "#" + typeName : memberName + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString() + "#" + typeName); //NOI18N
                default:
                    throw new IllegalArgumentException("kind=" + memberElem.getKind());
            }
        }

        @Override
        public CI createStaticMemberItem(ElementHandle<TypeElement> handle, String name, int substitutionOffset, boolean addSemicolon, ReferencesCount referencesCount, Source source) {
            String fqn = handle.getQualifiedName();
            int weight = 50;
            if (fqn.startsWith("java.lang") || fqn.startsWith("java.util")) { // NOI18N
                weight -= 10;
            } else if (fqn.startsWith("org.omg") || fqn.startsWith("org.apache")) { // NOI18N
                weight += 10;
            } else if (fqn.startsWith("com.sun") || fqn.startsWith("com.ibm") || fqn.startsWith("com.apple")) { // NOI18N
                weight += 20;
            } else if (fqn.startsWith("sun") || fqn.startsWith("sunw") || fqn.startsWith("netscape")) { // NOI18N
                weight += 30;
            }
            return new CI(name, 690, name + '#' + weight + '#' + fqn);
        }

        @Override
        public CI createChainedMembersItem(CompilationInfo info, List<? extends Element> chainedElems, List<? extends TypeMirror> chainedTypes, int substitutionOffset, boolean isDeprecated, boolean addSemicolon) {
            Element lastMemeber = chainedElems.get(chainedElems.size() - 1);
            StringBuilder sb = new StringBuilder();
            StringBuilder sortBuilder = new StringBuilder();
            for(Modifier mod : lastMemeber.getModifiers()) {
               sb.append(mod.toString());
               sb.append(' '); // NOI18N
            }
            Iterator<? extends TypeMirror> typesIt = chainedTypes.iterator();
            for (Element element : chainedElems) {
                TypeMirror type = typesIt.next();
                String elementName = element.getSimpleName().toString();
                sb.append(elementName);
                sortBuilder.append(elementName);
                if (element.getKind() == ElementKind.METHOD) {
                    sb.append('(');
                    StringBuilder sortParams = new StringBuilder();
                    sortParams.append("#("); //NOI18N
                    int cnt = 0;
                    Iterator<? extends VariableElement> it = ((ExecutableElement)element).getParameters().iterator();
                    Iterator<? extends TypeMirror> tIt = ((ExecutableType)type).getParameterTypes().iterator();
                    while(it.hasNext() && tIt.hasNext()) {
                        TypeMirror tm = tIt.next();
                        if (tm == null) {
                            break;
                        }
                        Set<TypeUtilities.TypeNameOptions> options = EnumSet.noneOf(TypeUtilities.TypeNameOptions.class);
                        if (((ExecutableElement)element).isVarArgs() && !tIt.hasNext()) {
                            options.add(TypeUtilities.TypeNameOptions.PRINT_AS_VARARG);
                        }
                        String paramTypeName = info.getTypeUtilities().getTypeName(tm, options.toArray(new TypeUtilities.TypeNameOptions[0])).toString();
                        sb.append(paramTypeName);
                        sortParams.append(paramTypeName);
                        sb.append(' ');
                        sb.append(it.next().getSimpleName());
                        if (it.hasNext()) {
                            sb.append(", "); //NOI18N
                            sortParams.append(',');
                        }
                        cnt++;
                    }
                    sb.append(')');
                    sortParams.append(')');
                    sortBuilder.append(cnt < 10 ? "0" : "").append(cnt).append("#").append(sortParams); //NOI18N
                }
                if (typesIt.hasNext()) {
                    sb.append('.');
                    sortBuilder.append('#');
                }
           }
            return new CI(sb.toString(), (lastMemeber.getKind().isField() ? 710 : 740) - SMART_TYPE, sortBuilder.toString());
        }

        @Override
        public CI createInitializeAllConstructorItem(CompilationInfo info, boolean isDefault, Iterable<? extends VariableElement> fields, ExecutableElement superConstructor, TypeElement parent, int substitutionOffset) {
            String simpleName = parent.getSimpleName().toString();
            StringBuilder sb = new StringBuilder();
            StringBuilder sortParams = new StringBuilder();
            int cnt = 0;
            sb.append("public "); //NOI18N
            sb.append(simpleName);
            sb.append('(');
            sortParams.append('(');
            boolean hasParams = false;
            for (VariableElement ve : fields) {
                if (!isDefault) {
                    if (hasParams) {
                        sb.append(", "); //NOI18N
                    }
                    String typeName = info.getTypeUtilities().getTypeName(ve.asType()).toString();
                    sb.append(typeName);
                    sb.append(' ');
                    sb.append(ve.getSimpleName());
                    sortParams.append(typeName);
                    cnt++;
                    hasParams = true;
                }
            }
            if (superConstructor != null) {
                if (!isDefault) {
                    for (VariableElement ve : superConstructor.getParameters()) {
                        if (hasParams) {
                            sb.append(", "); //NOI18N
                        }
                        String typeName = info.getTypeUtilities().getTypeName(ve.asType()).toString();
                        sb.append(typeName);
                        sb.append(' ');
                        sb.append(ve.getSimpleName());
                        sortParams.append(typeName);
                        cnt++;
                        hasParams = true;
                    }
                }
            }
            sb.append(") - generate"); //NOI18N
            return new CI(sb.toString(), 400, simpleName + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString());
        }        
    }
    
    private static class CI implements Comparable<CI> {
        
        private final String text;
        private final int priority;
        private final String sortText;

        public CI(String text, int priority, String sortText) {
            this.text = text;
            this.priority = priority;
            this.sortText = sortText;
        }

        @Override
        public String toString() {
            return text;
        }

        @Override
        public int compareTo(CI o) {
            int importanceDiff = compareIntegers(this.priority, o.priority);
            if (importanceDiff != 0) {
                return importanceDiff;
            }
            int alphabeticalDiff = compareText(this.sortText, o.sortText);
            return alphabeticalDiff;
        }

        private static int compareIntegers(int x, int y) {
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }

        private static int compareText(CharSequence text1, CharSequence text2) {
            if (text1 == null) {
                text1 = ""; //NOI18N
            }
            if (text2 == null) {
                text2 = ""; //NOI18N
            }
            int len = Math.min(text1.length(), text2.length());
            for (int i = 0; i < len; i++) {
                char ch1 = text1.charAt(i);
                char ch2 = text2.charAt(i);
                if (ch1 != ch2) {
                    return ch1 - ch2;
                }
            }
            return text1.length() - text2.length();
        }
    }

    @ServiceProvider(service=EntityCatalog.class)
    public static final class TestEntityCatalogImpl extends EntityCatalog {

        @Override
        public InputSource resolveEntity(String publicID, String systemID) throws SAXException, IOException {
            switch (publicID) {
                case "-//NetBeans//DTD Editor KeyBindings settings 1.1//EN":
                    return new InputSource(TestEntityCatalogImpl.class.getResourceAsStream("/org/netbeans/modules/editor/settings/storage/keybindings/EditorKeyBindings-1_1.dtd"));
                case "-//NetBeans//DTD Editor Preferences 1.0//EN":
                    return new InputSource(TestEntityCatalogImpl.class.getResourceAsStream("/org/netbeans/modules/editor/settings/storage/preferences/EditorPreferences-1_0.dtd"));
            }
            return null;
        }

    }
}
