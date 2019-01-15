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

package org.netbeans.modules.php.editor.csl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.completion.PHPCompletionItem;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.QualifiedNameKind;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.project.api.PhpProjectUtils;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Radek Matous, Jan Lahoda
 */
public class PHPTypeSearcher implements IndexSearcher {
    //TODO: no supported: came cases, regular expressions in queries (needs improve PHPIndex methods)
    @Override
    public Set<? extends Descriptor> getSymbols(Project project, String textForQuery, Kind originalkind, Helper helper) {
        // XXX: use PHP specific path ids
        EnumSet<Kind> regexpKinds = EnumSet.of(Kind.CAMEL_CASE, Kind.CASE_INSENSITIVE_CAMEL_CASE,  Kind.CASE_INSENSITIVE_REGEXP);
        EnumSet<Kind> insensitiveKinds = EnumSet.of(Kind.CASE_INSENSITIVE_CAMEL_CASE,  Kind.CASE_INSENSITIVE_REGEXP, Kind.CASE_INSENSITIVE_PREFIX);
        // PHP isn't Java so we may need to overrule the chosen kind
        // in case the query looks like it may be a camel-case/wildcard pattern
        // fix for #167687
        Kind kind = originalkind;
        if ((kind == Kind.CASE_INSENSITIVE_PREFIX || kind == Kind.PREFIX) && isCamelCasePattern(textForQuery)) {
            kind = Kind.CAMEL_CASE;
        }
        final Collection<FileObject> findRoots = QuerySupport.findRoots(project,
                Collections.singleton(PhpSourcePath.SOURCE_CP),
                Arrays.asList(PhpSourcePath.BOOT_CP, PhpSourcePath.PROJECT_BOOT_CP),
                Collections.<String>emptySet());
        final Index index = ElementQueryFactory.createIndexQuery(QuerySupportFactory.get(findRoots));

        Set<PHPTypeDescriptor> result = new HashSet<>();
        if (index != null && textForQuery.trim().length() > 0) {
            final boolean isVariable = textForQuery.startsWith("$"); //NOI18N
            String query = prepareIdxQuery(textForQuery, regexpKinds, kind);
            final Kind useKind = kind.equals(Kind.EXACT) ? Kind.EXACT : Kind.CASE_INSENSITIVE_PREFIX;
            if (!kind.equals(Kind.EXACT)) {
                query = query.toLowerCase();
            }
            final NameKind prefix = NameKind.create(query, useKind);
            if (!isVariable) {
                for (PhpElement indexedElement : index.getTopLevelElements(prefix)) {
                    if (!CodeUtils.isSyntheticTypeName(indexedElement.getName())) {
                        result.add(new PHPTypeDescriptor(indexedElement, helper));
                    }
                }
                for (PhpElement indexedElement : index.getMethods(prefix)) {
                    result.add(new PHPTypeDescriptor(indexedElement, helper));
                }
            }
            for (PhpElement indexedElement : index.getTopLevelVariables(prefix)) {
                result.add(new PHPTypeDescriptor(indexedElement, helper));
            }
            for (PhpElement indexedElement : index.getFields(prefix)) {
                result.add(new PHPTypeDescriptor(indexedElement, helper));
            }
            for (PhpElement indexedElement : index.getTypeConstants(prefix)) {
                result.add(new PHPTypeDescriptor(indexedElement, helper));
            }
        }
        if (regexpKinds.contains(kind)) {
            //handles wildcards and camelCases
            Set<PHPTypeDescriptor> originalResult = result;
            result = new HashSet<>();
            Pattern pattern = queryToPattern(textForQuery, insensitiveKinds.contains(originalkind));
            for (PHPTypeDescriptor typeDescriptor : originalResult) {
                String typeName = typeDescriptor.getElement().getName();
                if (pattern.matcher(typeName).matches()) {
                    result.add(typeDescriptor);
                }
            }
        }
        return result;
    }

    @Override
    public Set<? extends Descriptor> getTypes(Project project, String textForQuery, Kind originalkind, Helper helper) {
        // XXX: use PHP specific path ids
        EnumSet<Kind> regexpKinds = EnumSet.of(Kind.CAMEL_CASE, Kind.CASE_INSENSITIVE_CAMEL_CASE,  Kind.CASE_INSENSITIVE_REGEXP);
        EnumSet<Kind> insensitiveKinds = EnumSet.of(Kind.CASE_INSENSITIVE_CAMEL_CASE,  Kind.CASE_INSENSITIVE_REGEXP, Kind.CASE_INSENSITIVE_PREFIX);
        // PHP isn't Java so we may need to overrule the chosen kind
        // in case the query looks like it may be a camel-case/wildcard pattern
        // fix for #167687
        Kind kind = originalkind;
        if ((kind == Kind.CASE_INSENSITIVE_PREFIX || kind == Kind.PREFIX) && isCamelCasePattern(textForQuery)) {
            kind = Kind.CAMEL_CASE;
        }

        final Collection<FileObject> findRoots = QuerySupport.findRoots(project,
                Collections.singleton(PhpSourcePath.SOURCE_CP),
                Arrays.asList(PhpSourcePath.BOOT_CP, PhpSourcePath.PROJECT_BOOT_CP),
                Collections.<String>emptySet());
        final Index index = ElementQueryFactory.createIndexQuery(QuerySupportFactory.get(findRoots));

        Set<PHPTypeDescriptor> result = new HashSet<>();
        QualifiedName queryName = QualifiedName.create(textForQuery);
        QualifiedNameKind qnk = queryName.getKind();
        if (index != null) {
            String query = qnk.isUnqualified() ? prepareIdxQuery(textForQuery, regexpKinds, kind).toLowerCase() : textForQuery;
            NameKind nameKind = NameKind.prefix(QualifiedName.create(query));
            for (PhpElement indexedElement : index.getTypes(nameKind)) {
                if (!CodeUtils.isSyntheticTypeName(indexedElement.getName())) {
                    result.add(new PHPTypeDescriptor(indexedElement, helper));
                }
            }
        }
        if (qnk.isUnqualified() && regexpKinds.contains(kind)) {
            //handles wildcards and camelCases
            Set<PHPTypeDescriptor> originalResult = result;
            result = new HashSet<>();
            if (isCaseInsensitiveExactMatch(kind, textForQuery)) {
                for (PHPTypeDescriptor typeDescriptor : originalResult) {
                    String typeName = typeDescriptor.getElement().getName();
                    if (textForQuery.equalsIgnoreCase(typeName)) {
                        result.add(typeDescriptor);
                    }
                }
            } else {
                Pattern pattern = queryToPattern(textForQuery, insensitiveKinds.contains(originalkind));
                for (PHPTypeDescriptor typeDescriptor : originalResult) {
                    String typeName = typeDescriptor.getElement().getName();
                    if (pattern.matcher(typeName).matches()) {
                        result.add(typeDescriptor);
                    }
                }
            }
        }
        return result;
    }

    private static boolean isCaseInsensitiveExactMatch(Kind kind, String textForQuery) {
        return kind == Kind.CASE_INSENSITIVE_REGEXP && !textForQuery.endsWith("*"); //NOI18N
    }

    private static class PHPTypeDescriptor extends Descriptor {
        private final PhpElement element;
        private final PhpElement enclosingClass;
        private String projectName;
        private Icon projectIcon;
        private final Helper helper;
        private FileObject projectDirectory;

        public PHPTypeDescriptor(PhpElement element, Helper helper) {
            this(element, null, helper);
        }

        public PHPTypeDescriptor(PhpElement element, PhpElement enclosingClass, Helper helper) {
            this.element = element;
            this.enclosingClass = enclosingClass;
            this.helper = helper;
        }

        @Override
        public Icon getIcon() {
            if (projectName == null) {
                initProjectInfo();
            }
            if (element instanceof InterfaceElement) {
                return PHPCompletionItem.getInterfaceIcon();
            }
            return helper.getIcon(element);
        }

        @Override
        public String getTypeName() {
            return element.getName();
        }

        @Override
        public String getProjectName() {
            if (projectName == null) {
                initProjectInfo();
            }
            return projectName;
        }

        private void initProjectInfo() {
            FileObject fo = element.getFileObject();
            if (fo != null) {
                Project p = ProjectConvertors.getNonConvertorOwner(fo);
                if (p != null) {
                    if (PhpProjectUtils.isPhpProject(p)) {
                        projectDirectory = p.getProjectDirectory();
                    }
                    ProjectInformation pi = ProjectUtils.getInformation(p);
                    projectName = pi.getDisplayName();
                    projectIcon = pi.getIcon();
                }
            }

            if (projectName == null) {
                projectName = "";
            }
        }

        @Override
        public Icon getProjectIcon() {
            if (projectName == null) {
                initProjectInfo();
            }
            return projectIcon;
        }

        @Override
        public FileObject getFileObject() {
            return element.getFileObject();
        }

        @Override
        public void open() {
            FileObject fileObject = element.getFileObject();
            if (fileObject != null) {
                GsfUtilities.open(fileObject, element.getOffset(), element.getName());
            } else {
                Logger logger = Logger.getLogger(PHPTypeSearcher.class.getName());
                logger.log(Level.INFO, String.format("%s: cannot find %s", PHPTypeSearcher.class.getName(), element.getFilenameUrl())); //NOI18N
            }
        }

        @Override
        public String getContextName() {
            if (projectName == null) {
                initProjectInfo();
            }
            StringBuilder sb = new StringBuilder();
            boolean s = false;
            if (element instanceof FullyQualifiedElement) {
                FullyQualifiedElement fqnElement = (FullyQualifiedElement) element;
                if (!fqnElement.getNamespaceName().isDefaultNamespace()) {
                    if (element instanceof TypeElement) {
                        sb.append(fqnElement.getFullyQualifiedName());
                    } else {
                        sb.append(fqnElement.getNamespaceName());
                    }
                    s = true;
                }
            } else {
                if (enclosingClass != null) {
                    if ((enclosingClass instanceof FullyQualifiedElement) && (!((FullyQualifiedElement) enclosingClass).getNamespaceName().isDefaultNamespace())) {
                        sb.append(((FullyQualifiedElement) enclosingClass).getFullyQualifiedName().toString());
                    } else {
                        sb.append(enclosingClass.getName());
                    }
                    s = true;
                }
            }
            FileObject file = getFileObject();
            if (file != null) {
                if (s) {
                    sb.append(" in ");
                }
                String filePath = FileUtil.getFileDisplayName(file);
                String pathToDisplay = filePath;
                if (projectDirectory != null) {
                    String projectPath = FileUtil.getFileDisplayName(projectDirectory);
                    if (projectPath.length() > 0 && projectPath.length() < filePath.length()) {
                        pathToDisplay = getProjectName() + " ." + filePath.substring(projectPath.length()); //NOI18N
                    }
                }
                sb.append(pathToDisplay); //NOI18N
            }
            if (sb.length() > 0) {
                return sb.toString();
            }
            return null;
        }

        @Override
        public ElementHandle getElement() {
            return element;
        }

        @Override
        public int getOffset() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getSimpleName() {
            return element.getName();
        }

        @Override
        public String getOuterName() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PHPTypeDescriptor other = (PHPTypeDescriptor) obj;
            if (this.element != other.element && (this.element == null || !this.element.equals(other.element))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 19 * hash + (this.element != null ? this.element.hashCode() : 0);
            return hash;
        }
    }

    private static Pattern queryToPattern(String query, boolean caseInsensitive) {
        StringBuilder sb = new StringBuilder();
        char[] chars = query.toCharArray();
        boolean incamel = false;
        if (!query.startsWith("$")) {
            sb.append("[$]*"); //NOI18N
        }
        for (int i = 0; i < chars.length; i++) {
            if (i + 1 < chars.length && chars[i] == '.' && chars[i + 1] == '*') { //NOI18N
                sb.append(".*"); //NOI18N
                i++;
            } else if (chars[i] == '?') { //NOI18N
                sb.append('.'); //NOI18N
            } else if (chars[i] == '*') {
                sb.append(".*"); //NOI18N
            } else if (chars[i] == '.') {
                sb.append("."); //NOI18N
            } else if (Character.isUpperCase(chars[i])) {
                if (incamel) {
                    sb.append("[a-z0-9_]*"); //NOI18N
                }
                sb.append(chars[i]);
                incamel = true;
            } else if (i == 0 && chars[i] == '$') {
                sb.append('\\').append(chars[i]); //NOI18N
            } else {
                sb.append(Pattern.quote(String.valueOf(chars[i])));
            }
        }
        sb.append(".*"); //NOI18N
        String patternString = sb.toString();
        return caseInsensitive ? Pattern.compile(patternString, Pattern.CASE_INSENSITIVE) : Pattern.compile(patternString);
    }

    private String prepareIdxQuery(String textForQuery, EnumSet<Kind> regexpKinds, Kind kind) {
        String query = textForQuery.toLowerCase();
        if (regexpKinds.contains(kind)) {
            final char charAt = textForQuery.charAt(0);
            final int length = textForQuery.length();
            if (Character.isLetter(charAt) && length > 0) {
                query = query.substring(0, 1); //NOI18N
            } else if (charAt == '$' && length > 1) {
                query = query.substring(0, 1); //NOI18N
            } else {
                query = ""; //NOI18N
            }
        }
        return query;
    }

    private static boolean isCamelCasePattern(String query) {
        char[] chars = query.toCharArray();
        for (char c : chars) {
            if (c == '*' || c == '?' || Character.isUpperCase(c)) {
                return true;
            }
        }
        return false;
    }
}
