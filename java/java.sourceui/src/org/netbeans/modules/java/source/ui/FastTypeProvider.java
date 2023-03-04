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
package org.netbeans.modules.java.source.ui;

import java.awt.Toolkit;
import java.lang.ref.Reference;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.element.ElementKind;
import javax.swing.Icon;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.ui.Icons;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Fast & dirty type provider, which fakes Java types from filenames.
 * The provider is used just after project loads, and before the source roots
 * are indexed by a proper Java indexer.
 *
 * <p/>
 * The TypeProvider uses {@link OpenProjectFastIndex} as data source.
 *
 * @author sdedic
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.jumpto.type.TypeProvider.class)
public final class FastTypeProvider implements TypeProvider {
    private static final Logger LOG = Logger.getLogger(FastTypeProvider.class.getName());

    /**
     * Cancel flag, set by the infrastructure.
     */
    private AtomicBoolean cancel = new AtomicBoolean();

    /**
     * Fast index instance
     */
    private OpenProjectFastIndex fastIndex;

    /**
     * Cached class icon for results; we do not use other icons :)
     */
    private Icon classIcon;

    public FastTypeProvider() {
        this(OpenProjectFastIndex.getDefault());
    }

    // used from unit tests
    FastTypeProvider(OpenProjectFastIndex fastIndex) {
        this.fastIndex = fastIndex;
    }

    private Icon getClassIcon() {
        if (classIcon == null) {
            classIcon = Icons.getElementIcon (ElementKind.CLASS, null);
        }
        return classIcon;
    }

    @Override
    public void cancel() {
        cancel.set(true);
    }

    @Override
    public void cleanup() {
        // no cleanup needed
    }

    @Override
    @SuppressWarnings("fallthrough")
    public void computeTypeNames(Context context, Result result) {
        StringBuilder pattern = new StringBuilder();
        boolean sensitive = true;

        String quotedText = Pattern.quote(context.getText());

        switch (context.getSearchType()) {
            case CASE_INSENSITIVE_EXACT_NAME:
                sensitive = false;
            case CAMEL_CASE:
                pattern.append(Queries.createCamelCaseRegExp(context.getText(), null, null, sensitive));
                break;
            case CASE_INSENSITIVE_CAMEL_CASE:
                sensitive = false;
                pattern.append(Queries.createCamelCaseRegExp(context.getText(), null, null, sensitive));
                break;
            case EXACT_NAME:
                pattern.append("^").append(quotedText).append("$"); // NOI18N
                break;
            case CASE_INSENSITIVE_PREFIX:
                sensitive = false;
            case PREFIX:
                pattern.append("^").append(quotedText); // NOI18N
                break;
            case CASE_INSENSITIVE_REGEXP:
                sensitive = false;
            case REGEXP:
                pattern.append(
                        NameMatcherFactory.wildcardsToRegexp(
                            JavaTypeProvider.removeNonJavaChars(context.getText()),
                            false
                        )
                );
                break;
        }
        Pattern searchPattern = Pattern.compile(
                pattern.toString(),
                Pattern.MULTILINE +
                    (sensitive ? 0 : Pattern.CASE_INSENSITIVE));

        for (Map.Entry<FileObject, OpenProjectFastIndex.NameIndex> one : fastIndex.copyIndexes().entrySet()) {
            FileObject root = one.getKey();
            Project p = FileOwnerQuery.getOwner(root);

            if (context.getProject() != null && !context.getProject().equals(p)) {
                continue;
            }
            OpenProjectFastIndex.NameIndex fileIndex = one.getValue();

            Matcher m = searchPattern.matcher(fileIndex.files());
            while (m.find()) {
                if (cancel.get()) {
                    LOG.fine("Search canceled");
                    return;
                }
                if (m.start() == m.end()) {
                    continue;
                }
                CharSequence f = fileIndex.getFilename(m.start(), m.end());
                CharSequence pkg = fileIndex.findPath(m.start());
                SimpleDescriptor desc = new SimpleDescriptor(p, root, f, pkg);
                result.addResult(desc);
            }
        }
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(FastTypeProvider.class, "LBL_FastJavaIndex"); // NOI18N
    }

    @Override
    public String name() {
        return "fastJavaIndex";  // NOI18N
    }

    private class SimpleDescriptor extends TypeDescriptor {
        public static final String JAVA_EXTENSION = ".java"; // NOI18N
        private FileObject      root;
        private String          simpleName;
        private String          pkgName;
        private Project         project;

        public SimpleDescriptor(Project project, FileObject root, CharSequence simpleName, CharSequence pkgName) {
            this.root = root;
            this.simpleName = simpleName.toString();
            this.pkgName = pkgName.toString();
            this.project = project;
        }

        @Override
        public String getContextName() {
            return NbBundle.getMessage(FastTypeProvider.class, "FMT_TypeContextName",
                pkgName == null ? NbBundle.getMessage(FastTypeProvider.class, "LBL_DefaultPackage") : pkgName);
        }

        @Override
        @CheckForNull
        public FileObject getFileObject() {
            String s = simpleName;

            if (pkgName != null && !"".equals(pkgName)) {
                StringBuilder sb = new StringBuilder();
                s = sb.append(pkgName).append('.').append(simpleName).toString().replace(".", "/"); // NOI18N
            }
            return root.getFileObject(s + JAVA_EXTENSION);
        }

        @Override
        public Icon getIcon() {
            return getClassIcon();
        }

        @Override
        public int getOffset() {
            return -1;
        }

        @Override
        public String getOuterName() {
            return null;
        }

        @Override
        public Icon getProjectIcon() {
            return fastIndex.getProjectIcon(project);
        }

        @Override
        public String getProjectName() {
            return fastIndex.getProjectName(project);
        }

        @Override
        public String getSimpleName() {
            return simpleName.toString();
        }

        @Override
        public String getTypeName() {
            return simpleName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(simpleName).append(" (");
            if (pkgName == null || "".equals(pkgName)) {
                sb.append("Default Package");
            } else {
                sb.append(pkgName);
            }
            sb.append(")");
            return sb.toString();
        }

        @Override
        public int hashCode() {
            int hc = 17;
            hc = hc * 31 + (root == null ? 0 : root.hashCode());
            hc = hc * 31 + (pkgName == null ? 0 : pkgName.hashCode());
            hc = hc * 31 + (simpleName == null ? 0 : simpleName.hashCode());
            return hc;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof SimpleDescriptor)) {
                return false;
            }
            final SimpleDescriptor other = (SimpleDescriptor) obj;
            return root == null ? other.root == null : root.equals(other.root) &&
                pkgName == null ? other.pkgName == null : pkgName.equals(other.pkgName) &&
                simpleName == null ? other.simpleName == null : simpleName.equals(other.simpleName);
        }

        @Override
        public void open() {
            boolean success = false;
            try {
                final FileObject fo = getFileObject();
                if (fo != null) {
                    final DataObject d = DataObject.find(fo);
                    final EditCookie cake = d.getCookie(EditCookie.class);
                    if (cake != null) {
                        cake.edit();
                        success = true;
                    }
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
            if (!success) {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

}
