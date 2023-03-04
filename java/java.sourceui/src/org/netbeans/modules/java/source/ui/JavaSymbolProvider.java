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

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor6;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.DocumentUtil;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.jumpto.support.NameMatcher;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.jumpto.symbol.SymbolProvider.class)
public class JavaSymbolProvider implements SymbolProvider {

    private static final Logger LOGGER = Logger.getLogger(JavaSymbolProvider.class.getName());

    private static final String CAPTURED_WILDCARD = "<captured wildcard>"; //NOI18N
    private static final String UNKNOWN = "<unknown>"; //NOI18N

    private final AtomicBoolean canceled = new AtomicBoolean();

    @Override
    public String name() {
        return "java symbols";  //NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(JavaTypeProvider.class, "MSG_JavaSymbols");
    }

    @Override
    public void computeSymbolNames(final Context context, final Result result) {
        try {
            final SearchType st = context.getSearchType();
            String textToSearch = context.getText();
            final boolean scanInProgress = SourceUtils.isScanInProgress();
            if (scanInProgress) {
                // ui message
                final String warningKind = NbBundle.getMessage(JavaSymbolProvider.class, "LBL_SymbolKind");
                final String message = NbBundle.getMessage(JavaSymbolProvider.class, "LBL_ScanInProgress_warning", warningKind);
                result.setMessage(message);
                result.pendingResult();
                final Cache cache = Cache.get(textToSearch, st);
                if (cache != null) {
                    cache.populateResult(result);
                    return;
                }
            } else {
                Cache.clear();
            }
            final Cache cache = scanInProgress ?
                Cache.create(textToSearch, st) :
                null;
            doComputeSymbols(st, textToSearch, new ResultHandler() {
                private FileObject root;
                private ProjectInformation projectInfo;
                private ClassIndexImpl ci;
                @Override
                public void setHighlightText(String text) {
                    result.setHighlightText(text);
                }

                @Override
                public void runRoot(FileObject root, ClassIndexImpl ci, Exec exec) throws IOException, InterruptedException {
                    try {
                        Project project = FileOwnerQuery.getOwner(root);

                        this.root = root;
                        this.projectInfo = project == null ?
                                null :
                                project.getLookup().lookup(ProjectInformation.class);   //Intentionally does not use ProjectUtils.getInformation() it does project icon annotation which is expensive
                        this.ci = ci;
                        exec.run();
                    } finally {
                        this.root = null;
                        this.projectInfo = null;
                        this.ci = null;
                    }
                }

                @Override
                public void handleResult(ElementHandle<TypeElement> owner, String ident, boolean caseSensitive) {
                    final AsyncJavaSymbolDescriptor d = new AsyncJavaSymbolDescriptor(
                            projectInfo,
                            root,
                            ci,
                            owner,
                            ident,
                            caseSensitive);
                    result.addResult(d);
                    if (cache != null) {
                        cache.offer(d);
                    }
                }
            }, true, canceled);
        } finally {
            clearCancel();
        }
    }

    public static void doComputeSymbols(SearchType st, String textToSearch, ResultHandler handler, boolean async, AtomicBoolean canceled) {
        String prefix = null;
        final int dotIndex = textToSearch.lastIndexOf('.'); //NOI18N
        if (dotIndex > 0 && dotIndex != textToSearch.length()-1) {
            prefix = textToSearch.substring(0, dotIndex);
            textToSearch = textToSearch.substring(dotIndex+1);
        }
        final String textToHighLight = textToSearch;
        ClassIndex.NameKind _kind;
        boolean _caseSensitive;
        switch (st) {
            case PREFIX:
                _kind = ClassIndex.NameKind.PREFIX;
                _caseSensitive = true;
                break;
            case REGEXP:
                _kind = ClassIndex.NameKind.REGEXP;
                textToSearch = NameMatcherFactory.wildcardsToRegexp(
                        removeNonJavaChars(textToSearch),
                        true);
                _caseSensitive = true;
                break;
            case CAMEL_CASE:
                _kind = ClassIndex.NameKind.CAMEL_CASE;
                _caseSensitive = true;
                break;
            case CASE_INSENSITIVE_CAMEL_CASE:
                _kind = ClassIndex.NameKind.CAMEL_CASE_INSENSITIVE;
                _caseSensitive = false;
                break;
            case EXACT_NAME:
                _kind = ClassIndex.NameKind.SIMPLE_NAME;
                _caseSensitive = true;
                break;
            case CASE_INSENSITIVE_PREFIX:
                _kind = ClassIndex.NameKind.CASE_INSENSITIVE_PREFIX;
                _caseSensitive = false;
                break;
            case CASE_INSENSITIVE_EXACT_NAME:
                _kind = ClassIndex.NameKind.CASE_INSENSITIVE_REGEXP;
                _caseSensitive = false;
                break;
            case CASE_INSENSITIVE_REGEXP:
                _kind = ClassIndex.NameKind.CASE_INSENSITIVE_REGEXP;
                textToSearch = NameMatcherFactory.wildcardsToRegexp(
                        removeNonJavaChars(textToSearch),
                        true);
                _caseSensitive = false;
                break;
            default:
                throw new IllegalArgumentException();
        }
        final String ident = textToSearch;
        final ClassIndex.NameKind kind = _kind;
        final boolean caseSensitive = _caseSensitive;
        final Pair<NameMatcher,Boolean> restriction;
        if (prefix != null) {
            restriction = compileName(prefix,caseSensitive);
            handler.setHighlightText(textToHighLight);
        } else {
            restriction = null;
        }
        try {
            final ClassIndexManager manager = ClassIndexManager.getDefault();

            Collection<FileObject> roots = QuerySupport.findRoots(
                    (Project)null,
                    Collections.singleton(ClassPath.SOURCE),
                    Collections.<String>emptySet(),
                    Collections.<String>emptySet());

            final Set<URL> rootUrls = new HashSet<>();
            for(FileObject root : roots) {
                if (canceled.get()) {
                    return;
                }
                rootUrls.add(root.toURL());
            }

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Querying following roots:"); //NOI18N
                for (URL url : rootUrls) {
                    LOGGER.log(Level.FINE, "  {0}", url); //NOI18N
                }
                LOGGER.log(Level.FINE, "-------------------------"); //NOI18N
            }
            //Perform all queries in single op
            IndexManager.priorityAccess(new IndexManager.Action<Void>() {
                @Override
                public Void run() throws IOException, InterruptedException {
                    for (URL url : rootUrls) {
                        if (canceled.get()) {
                            return null;
                        }
                        final FileObject root = URLMapper.findFileObject(url);
                        if (root == null) {
                            continue;
                        }

                        final ClassIndexImpl impl = manager.getUsagesQuery(root.toURL(), true);
                        if (impl != null) {
                            handler.runRoot(root, impl, () -> {
                                final Map<ElementHandle<TypeElement>,Set<String>> r = new HashMap<>();
                                impl.getDeclaredElements(ident, kind, DocumentUtil.typeElementConvertor(),r);
                                if (!r.isEmpty()) {
                                    for (final Map.Entry<ElementHandle<TypeElement>,Set<String>> p : r.entrySet()) {
                                        final ElementHandle<TypeElement> owner = p.getKey();
                                        for (String symbol : p.getValue()) {
                                            if (matchesRestrictions(owner.getQualifiedName(), symbol, restriction, caseSensitive)) {
                                                handler.handleResult(owner, symbol, caseSensitive);
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                    return null;
                }
            });
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        } catch (InterruptedException ie) {
            //ignore
        }
    }

    public interface ResultHandler {
        public void setHighlightText(String text);
        public void runRoot(FileObject root, ClassIndexImpl ci, Exec exec) throws IOException, InterruptedException;
        public void handleResult(@NonNull ElementHandle<TypeElement> owner, @NonNull String ident, boolean caseSensitive);
        public interface Exec {
            public void run() throws IOException, InterruptedException;
        }
    }

    private static boolean matchesRestrictions(
            @NonNull final String fqn,
            @NonNull final String ident,
            @NullAllowed Pair<NameMatcher,Boolean> restriction,
            final boolean caseSensitive) {
        return matchesRestrictionsImpl(fqn, ident, restriction, caseSensitive, false);
    }

    private static boolean matchesRestrictionsImpl(
            @NonNull final String fqn,
            @NonNull final String ident,
            @NullAllowed Pair<NameMatcher,Boolean> restriction,
            final boolean caseSensitive,
            final boolean enclosing) {
        if (restriction == null) {
            return true;
        }
        final String simpleName = getSimpleName(fqn);
        return restriction.first().accept(restriction.second() ? fqn : simpleName) ||
               (!enclosing &&
                (caseSensitive ? ident.equals(simpleName) : ident.equalsIgnoreCase(simpleName)) &&
                matchesRestrictionsImpl(getOwner(fqn), ident, restriction, caseSensitive, true));
    }

    @NonNull
    private static String getSimpleName(@NonNull final String fqn) {
        final int index = fqn.lastIndexOf('.'); //NOI18N
        return index < 0 ? fqn : fqn.substring(index+1);
    }

    @NonNull
    private static String getOwner(@NonNull final String fqn) {
        final int index = fqn.lastIndexOf('.'); //NOI18N
        return index < 0 ? "" : fqn.substring(0, index);    //NOI18N
    }

    private static Pair<NameMatcher,Boolean> compileName(
            @NonNull final String prefix,
            final boolean caseSensitive) {
        final boolean fqn = prefix.indexOf('.') > 0;    //NOI18N
        final SearchType searchType = containsWildCard(prefix)?
            (caseSensitive ? SearchType.REGEXP : SearchType.CASE_INSENSITIVE_REGEXP) :
            (caseSensitive ? SearchType.PREFIX : SearchType.CASE_INSENSITIVE_PREFIX);
        return Pair.<NameMatcher,Boolean>of(
            NameMatcherFactory.createNameMatcher(prefix, searchType),
            fqn);
    }

    private static boolean containsWildCard(String text) {
        for( int i = 0; i < text.length(); i++ ) {
            if ( text.charAt( i ) == '?' || text.charAt( i ) == '*' ) { // NOI18N
                return true;
            }
        }
        return false;
    }

    @NonNull
    public static Pair<String,String> getDisplayName (
            @NonNull final Element e,
            @NonNull final Element enclosingElement) {
        assert e != null;
        String name;
        String suffix = null;
        if (e.getKind() == ElementKind.METHOD || e.getKind() == ElementKind.CONSTRUCTOR) {
            name = (e.getKind() == ElementKind.CONSTRUCTOR ?
                    enclosingElement.getSimpleName():
                    e.getSimpleName()).toString();
            final StringBuilder sb = new StringBuilder();
            sb.append('('); //NOI18N
            ExecutableElement ee = (ExecutableElement) e;
            final List<? extends VariableElement> vl = ee.getParameters();
            for (Iterator<? extends VariableElement> it = vl.iterator(); it.hasNext();) {
                final VariableElement v = it.next();
                final TypeMirror tm = v.asType();
                sb.append(getTypeName(tm, false, true));
                if (it.hasNext()) {
                    sb.append(", ");    //NOI18N
                }
            }
            sb.append(')');
            suffix = sb.toString();
        } else {
            name = e.getSimpleName().toString();
        }
        return Pair.of(name,suffix);
    }

    private static CharSequence getTypeName(TypeMirror type, boolean fqn, boolean varArg) {
	if (type == null) {
            return ""; //NOI18N
        }
        return new TypeNameVisitor(varArg).visit(type, fqn);
    }

    private static class TypeNameVisitor extends SimpleTypeVisitor6<StringBuilder,Boolean> {

        private boolean varArg;
        private boolean insideCapturedWildcard = false;

        private TypeNameVisitor(boolean varArg) {
            super(new StringBuilder());
            this.varArg = varArg;
        }

        @Override
        public StringBuilder defaultAction(TypeMirror t, Boolean p) {
            return DEFAULT_VALUE.append(t);
        }

        @Override
        public StringBuilder visitDeclared(DeclaredType t, Boolean p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement)e;
                DEFAULT_VALUE.append((p ? te.getQualifiedName() : te.getSimpleName()).toString());
                Iterator<? extends TypeMirror> it = t.getTypeArguments().iterator();
                if (it.hasNext()) {
                    DEFAULT_VALUE.append("<"); //NOI18N
                    while(it.hasNext()) {
                        visit(it.next(), p);
                        if (it.hasNext()) {
                            DEFAULT_VALUE.append(", "); //NOI18N
                        }
                    }
                    DEFAULT_VALUE.append(">"); //NOI18N
                }
                return DEFAULT_VALUE;
            } else {
                return DEFAULT_VALUE.append(UNKNOWN); //NOI18N
            }
        }

        @Override
        public StringBuilder visitArray(ArrayType t, Boolean p) {
            boolean isVarArg = varArg;
            varArg = false;
            visit(t.getComponentType(), p);
            return DEFAULT_VALUE.append(isVarArg ? "..." : "[]"); //NOI18N
        }

        @Override
        public StringBuilder visitTypeVariable(TypeVariable t, Boolean p) {
            Element e = t.asElement();
            if (e != null) {
                String name = e.getSimpleName().toString();
                if (!CAPTURED_WILDCARD.equals(name)) {
                    return DEFAULT_VALUE.append(name);
                }
            }
            DEFAULT_VALUE.append("?"); //NOI18N
            if (!insideCapturedWildcard) {
                insideCapturedWildcard = true;
                TypeMirror bound = t.getLowerBound();
                if (bound != null && bound.getKind() != TypeKind.NULL) {
                    DEFAULT_VALUE.append(" super "); //NOI18N
                    visit(bound, p);
                } else {
                    bound = t.getUpperBound();
                    if (bound != null && bound.getKind() != TypeKind.NULL) {
                        DEFAULT_VALUE.append(" extends "); //NOI18N
                        if (bound.getKind() == TypeKind.TYPEVAR) {
                            bound = ((TypeVariable)bound).getLowerBound();
                        }
                        visit(bound, p);
                    }
                }
                insideCapturedWildcard = false;
            }
            return DEFAULT_VALUE;
        }

        @Override
        public StringBuilder visitWildcard(WildcardType t, Boolean p) {
            int len = DEFAULT_VALUE.length();
            DEFAULT_VALUE.append("?"); //NOI18N
            TypeMirror bound = t.getSuperBound();
            if (bound == null) {
                bound = t.getExtendsBound();
                if (bound != null) {
                    DEFAULT_VALUE.append(" extends "); //NOI18N
                    if (bound.getKind() == TypeKind.WILDCARD) {
                        bound = ((WildcardType)bound).getSuperBound();
                    }
                    visit(bound, p);
                } else if (len == 0) {
                    bound = SourceUtils.getBound(t);
                    if (bound != null && (bound.getKind() != TypeKind.DECLARED || !((TypeElement)((DeclaredType)bound).asElement()).getQualifiedName().contentEquals("java.lang.Object"))) { //NOI18N
                        DEFAULT_VALUE.append(" extends "); //NOI18N
                        visit(bound, p);
                    }
                }
            } else {
                DEFAULT_VALUE.append(" super "); //NOI18N
                visit(bound, p);
            }
            return DEFAULT_VALUE;
        }

        @Override
        public StringBuilder visitError(ErrorType t, Boolean p) {
            Element e = t.asElement();
            if (e instanceof TypeElement) {
                TypeElement te = (TypeElement)e;
                return DEFAULT_VALUE.append((p ? te.getQualifiedName() : te.getSimpleName()).toString());
            }
            return DEFAULT_VALUE;
        }
    }

    private static String removeNonJavaChars(String text) {
       StringBuilder sb = new StringBuilder();

       for( int i = 0; i < text.length(); i++) {
           char c = text.charAt(i);
           if( Character.isJavaIdentifierPart(c) || c == '*' || c == '?') {
               sb.append(c);
           }
       }
       return sb.toString();
    }

    @Override
    public void cancel() {
        canceled.set(true);
    }

    @Override
    public void cleanup() {
        clearCancel();
        Cache.clear();
    }

    private void clearCancel() {
        canceled.set(false);
    }

    private static final class Cache {
        private static Cache instance;

        private final String text;
        private final SearchType type;
        private final Collection<SymbolDescriptor> descriptors;

        private Cache(
            @NonNull final String text,
            @NonNull final SearchType type) {
            this.text = text;
            this.type = type;
            this.descriptors = Collections.synchronizedSet(new HashSet<SymbolDescriptor>());
        }

        void populateResult(@NonNull final Result result) {
            synchronized (descriptors) {
                for (SymbolDescriptor d : descriptors) {
                    result.addResult(d);
                }
            }
        }

        void offer(@NonNull final SymbolDescriptor d) {
            descriptors.add(d);
        }

        static void clear() {
            instance = null;
        }

        @CheckForNull
        static Cache get(
            @NonNull final String text,
            @NonNull final SearchType type) {
            Cache res = instance;
            if (res != null && (!res.text.equals(text) || res.type != type)) {
                res = instance = null;
            }
            return res;
        }

        @NonNull
        static Cache create(
            @NonNull final String text,
            @NonNull final SearchType type) {
            return instance = new Cache(text, type);
        }
    }

}
