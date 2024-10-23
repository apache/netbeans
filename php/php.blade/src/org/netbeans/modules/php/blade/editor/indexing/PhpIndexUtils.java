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
package org.netbeans.modules.php.blade.editor.indexing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.blade.editor.EditorStringUtils;
import static org.netbeans.modules.php.blade.editor.EditorStringUtils.NAMESPACE_SEPARATOR;
import org.netbeans.modules.php.blade.editor.cache.QueryCache;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.index.PHPIndexer;
import org.netbeans.modules.php.editor.index.Signature;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * TODO needs simplification & refactor
 *
 * @author bhaidu
 */
public final class PhpIndexUtils {

    public final static String ESCAPED_NAMESPACE_SEPARATOR = "\\\\"; // NOI18N
    private final static QueryCache<String, Collection<PhpIndexResult>> cache = new QueryCache();
    private final static QueryCache<String, Collection<PhpIndexFunctionResult>> functionCache = new QueryCache();

    public static final int MIN_NAMESPACE_PREFIX_LENGTH = 3;
    private static final Map<Integer, PhpIndexUtils> QUERY_SUPPORT_INSTANCES = new WeakHashMap<>();

    private PhpIndexUtils() {

    }

    /**
     * class query without namespace
     *
     * @param fo
     * @param prefix
     * @return
     */
    public static Collection<PhpIndexResult> queryClass(FileObject fo, String prefix) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);

        QueryCache<String, Collection<PhpIndexResult>> selfCache = getCache(fo, prefix);
        if (selfCache != null && selfCache.containsKey(prefix)) {
            return selfCache.get(prefix).get();
        }
        Collection<PhpIndexResult> results = new ArrayList<>();
        String queryPrefix = prefix.toLowerCase();
        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(PHPIndexer.FIELD_CLASS,
                    queryPrefix, QuerySupport.Kind.PREFIX, new String[]{
                        PHPIndexer.FIELD_CLASS,});
            for (IndexResult indexResult : indexResults) {
                FileObject indexFile = indexResult.getFile();

                String[] values = indexResult.getValues(PHPIndexer.FIELD_CLASS);
                for (String value : values) {
                    Signature sig = Signature.get(value);
                    String fullName = sig.string(1);
                    String namespace = sig.string(4);

                    if (fullName.length() > 0 && fullName.startsWith(prefix)) {
                        results.add(new PhpIndexResult(fullName, namespace,
                                indexFile,
                                PhpIndexResult.Type.CLASS, new OffsetRange(0, 1)));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (selfCache != null && !results.isEmpty()) {
            selfCache.put(prefix, results);
        }
        return results;
    }

    public static Collection<PhpIndexResult> queryNamespaceClassesName(String prefix,
            String namespace, FileObject fo) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);
        Collection<PhpIndexResult> results = new ArrayList<>();
        String queryPrefix = prefix.toLowerCase() + ".*" + namespace.replace(NAMESPACE_SEPARATOR, ESCAPED_NAMESPACE_SEPARATOR) + ";.*"; // NOI18N

        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(
                    PHPIndexer.FIELD_CLASS, queryPrefix, QuerySupport.Kind.REGEXP, new String[]{
                        PHPIndexer.FIELD_CLASS, PHPIndexer.FIELD_FIELD});
            for (IndexResult indexResult : indexResults) {
                FileObject indexFile = indexResult.getFile();

                String[] values = indexResult.getValues(PHPIndexer.FIELD_CLASS);
                //should use this approach on methods
                //String[] fields = indexResult.getValues(PHPIndexer.FIELD_FIELD);
                for (String value : values) {
                    Signature sig = Signature.get(value);
                    String fullName = sig.string(1);
                    String classNamespace = sig.string(4);
                    if (fullName.length() > 0
                            && classNamespace.length() > 0
                            && classNamespace.startsWith(namespace)) {
                        results.add(new PhpIndexResult(fullName,
                                classNamespace + NAMESPACE_SEPARATOR + fullName, indexFile, PhpIndexResult.Type.CLASS, new OffsetRange(0, 1)));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return results;
    }

    public static Collection<PhpIndexResult> queryExactNamespaceClasses(String identifier,
            String namespace, FileObject fo) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);
        Collection<PhpIndexResult> results = new ArrayList<>();

        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(
                    PHPIndexer.FIELD_TOP_LEVEL, namespace.toLowerCase(), QuerySupport.Kind.EXACT,
                    new String[]{
                        PHPIndexer.FIELD_NAMESPACE
                    });
            for (IndexResult indexResult : indexResults) {
                FileObject indexFile = indexResult.getFile();

                if (!indexFile.getName().equals(identifier)) {
                    continue;
                }

                String namespaceValue = indexResult.getValue(PHPIndexer.FIELD_NAMESPACE);

                if (namespaceValue == null) {
                    continue;
                }

                results.add(new PhpIndexResult(namespace + NAMESPACE_SEPARATOR + identifier, indexFile, PhpIndexResult.Type.CLASS, new OffsetRange(0, 1)));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return results;
    }

    public static Collection<PhpIndexResult> queryComponentClass(String identifier,
            String namespace, FileObject fo) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);
        Collection<PhpIndexResult> results = new ArrayList<>();

        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(
                    PHPIndexer.FIELD_TOP_LEVEL, namespace.toLowerCase(), QuerySupport.Kind.PREFIX,
                    new String[]{
                        PHPIndexer.FIELD_NAMESPACE
                    });
            for (IndexResult indexResult : indexResults) {
                String namespaceValue = indexResult.getValue(PHPIndexer.FIELD_NAMESPACE);
                if (namespaceValue == null) {
                    continue;
                }
                Signature sig = Signature.get(namespaceValue);
                String name = sig.string(1);
                String domainName = sig.string(2);
                FileObject indexFile = indexResult.getFile();
                if (indexFile.getName().equals(identifier)) {
                    results.add(new PhpIndexResult(domainName + NAMESPACE_SEPARATOR + name + NAMESPACE_SEPARATOR + indexFile.getName(),
                            indexFile, PhpIndexResult.Type.CLASS, new OffsetRange(0, 1)));
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return results;
    }

    public static Collection<PhpIndexResult> queryExactClass(FileObject fo, String identifier) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);
        QueryCache<String, Collection<PhpIndexResult>> selfCache = getCache(fo, identifier);
        if (selfCache != null && selfCache.containsKey(identifier)) {
            return selfCache.get(identifier).get();
        }
        Collection<PhpIndexResult> results = new ArrayList<>();
        String queryPrefix = identifier.toLowerCase();
        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(PHPIndexer.FIELD_CLASS,
                    queryPrefix, QuerySupport.Kind.PREFIX, new String[]{PHPIndexer.FIELD_CLASS});
            for (IndexResult indexResult : indexResults) {
                FileObject indexFile = indexResult.getFile();

                String[] values = indexResult.getValues(PHPIndexer.FIELD_CLASS);
                for (String value : values) {
                    Signature sig = Signature.get(value);
                    String name = sig.string(1);
                    if (name.length() > 0 && name.equals(identifier)) {
                        results.add(new PhpIndexResult(name, indexFile, PhpIndexResult.Type.CLASS, new OffsetRange(0, 1)));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (selfCache != null && !results.isEmpty()) {
            selfCache.put(identifier, results);
        }
        return results;
    }

    public static Collection<PhpIndexFunctionResult> queryFunctions(FileObject fo, String prefix) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);
        Collection<PhpIndexFunctionResult> results = new ArrayList<>();
        String queryPrefix = prefix.toLowerCase();
        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(PHPIndexer.FIELD_BASE, queryPrefix, QuerySupport.Kind.PREFIX, new String[]{PHPIndexer.FIELD_BASE});
            for (IndexResult indexResult : indexResults) {
                FileObject indexFile = indexResult.getFile();
                //internal php index

                String[] values = indexResult.getValues(PHPIndexer.FIELD_BASE);
                for (String value : values) {
                    Signature sig = Signature.get(value);
                    String name = sig.string(1);

                    if (name.length() > 0 && name.startsWith(prefix)) {
                        Integer offset = sig.integer(2);
                        String params = sig.string(3);
                        results.add(new PhpIndexFunctionResult(
                                name, indexFile,
                                PhpIndexResult.Type.FUNCTION,
                                new OffsetRange(offset, offset + name.length()),
                                null,
                                parseParameters(params)
                        ));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return results;
    }

    public static Collection<PhpIndexFunctionResult> queryExactFunctions(FileObject fo, String prefix) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);
        Collection<PhpIndexFunctionResult> results = new ArrayList<>();
        QueryCache<String, Collection<PhpIndexFunctionResult>> selfCache = getFunctionCache(fo, prefix);
        if (selfCache != null && selfCache.containsKey(prefix)) {
            return selfCache.get(prefix).get();
        }
        String queryPrefix = prefix.toLowerCase();
        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(PHPIndexer.FIELD_BASE, queryPrefix, QuerySupport.Kind.PREFIX, new String[]{PHPIndexer.FIELD_BASE});
            for (IndexResult indexResult : indexResults) {
                FileObject indexFile = indexResult.getFile();
                //internal php index

                String[] values = indexResult.getValues(PHPIndexer.FIELD_BASE);
                for (String value : values) {
                    Signature sig = Signature.get(value);
                    String name = sig.string(1);

                    if (name.length() > 0 && name.equals(prefix)) {
                        Integer offset = sig.integer(2);
                        String params = sig.string(3);
                        results.add(new PhpIndexFunctionResult(name,
                                indexFile, PhpIndexResult.Type.FUNCTION,
                                new OffsetRange(offset, offset + name.length()),
                                null,
                                parseParameters(params)
                        ));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (selfCache != null && !results.isEmpty()) {
            selfCache.put(prefix, results);
        }
        return results;
    }

    //should query the class before?
    public static Collection<PhpIndexFunctionResult> queryExactClassMethods(FileObject fo,
            String method, String className, String queryNamespace) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);
        Collection<PhpIndexFunctionResult> results = new ArrayList<>();
        //for the moment a quick hack
        //maybe send the classNamePath directly?
        String regexQuery = className.toLowerCase();
        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(PHPIndexer.FIELD_CLASS, regexQuery,
                    QuerySupport.Kind.PREFIX, new String[]{PHPIndexer.FIELD_CLASS, PHPIndexer.FIELD_METHOD});
            for (IndexResult indexResult : indexResults) {
                FileObject indexFile = indexResult.getFile();
                //internal php index
                String[] classValues = indexResult.getValues(PHPIndexer.FIELD_CLASS);

                Signature classSignature = null;
                String classNamespace = null;

                for (String classValue : classValues) {
                    Signature sig = Signature.get(classValue);
                    String name = sig.string(1);
                    String namespace = sig.string(4);
                    if (name.length() > 0 && name.equals(className)) {
                        if (queryNamespace != null && !namespace.equals(queryNamespace)) {
                            continue;
                        }
                        classSignature = sig;

                        if (namespace.length() > 0) {
                            classNamespace = namespace + NAMESPACE_SEPARATOR + className;
                        }
                    }
                }

                if (classSignature == null) {
                    continue;
                }

                String[] values = indexResult.getValues(PHPIndexer.FIELD_METHOD);
                for (String value : values) {
                    Signature sig = Signature.get(value);
                    String name = sig.string(1);

                    if (name.length() > 0 && name.equals(method)) {
                        Integer offset = sig.integer(2);
                        String params = sig.string(3);
                        results.add(new PhpIndexFunctionResult(name,
                                indexFile, PhpIndexResult.Type.FUNCTION,
                                new OffsetRange(offset, offset + name.length()),
                                classNamespace,
                                parseParameters(params)
                        ));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return results;
    }

    /**
     *
     *
     * @param fo
     * @param method
     * @param className
     * @param queryNamespace
     * @return
     */
    public static Collection<PhpIndexFunctionResult> queryClassMethods(FileObject fo,
            String method, String className, String queryNamespace) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);
        Collection<PhpIndexFunctionResult> results = new ArrayList<>();
        String queryNamespacePath = queryNamespace;
        if (queryNamespace != null && queryNamespace.length() > MIN_NAMESPACE_PREFIX_LENGTH) {
            queryNamespacePath =  EditorStringUtils.trimNamespace(queryNamespace);
        }
        //should query the class befoe
        //for the moment a quick hack
        //maybe send the classNamePath directly?
        String regexQuery = className.toLowerCase();
        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(PHPIndexer.FIELD_CLASS, regexQuery,
                    QuerySupport.Kind.PREFIX, new String[]{PHPIndexer.FIELD_CLASS, PHPIndexer.FIELD_METHOD});
            for (IndexResult indexResult : indexResults) {
                FileObject indexFile = indexResult.getFile();
                String[] classValues = indexResult.getValues(PHPIndexer.FIELD_CLASS);

                Signature classSignature = null;
                String classNamespace = null;

                for (String classValue : classValues) {
                    Signature sig = Signature.get(classValue);
                    String name = sig.string(1);
                    if (name.length() > 0 && name.equals(className)) {
                        classSignature = sig;
                        String namespace = sig.string(4);

                        if (queryNamespacePath != null && !namespace.equals(queryNamespacePath)) {
                            classSignature = null;
                            continue;
                        }

                        if (namespace.length() > 0) {
                            classNamespace = namespace + EditorStringUtils.NAMESPACE_SEPARATOR + className;
                        }
                    }
                }

                if (classSignature == null) {
                    continue;
                }

                String[] values = indexResult.getValues(PHPIndexer.FIELD_METHOD);
                for (String value : values) {
                    Signature sig = Signature.get(value);
                    String name = sig.string(1);

                    if (name.length() > 0 && name.startsWith(method)) {
                        Integer offset = sig.integer(2);
                        String params = sig.string(3);
                        results.add(new PhpIndexFunctionResult(name,
                                indexFile, PhpIndexResult.Type.FUNCTION,
                                new OffsetRange(offset, offset + name.length()),
                                classNamespace,
                                parseParameters(params)
                        ));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return results;
    }

    public static Collection<PhpIndexResult> queryConstants(FileObject fo, String prefix) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);
        Collection<PhpIndexResult> results = new ArrayList<>();
        String queryPrefix = prefix.toLowerCase();
        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(PHPIndexer.FIELD_CONST, queryPrefix, QuerySupport.Kind.PREFIX, new String[]{PHPIndexer.FIELD_CONST});
            for (IndexResult indexResult : indexResults) {
                FileObject indexFile = indexResult.getFile();
                //internal php index

                String[] values = indexResult.getValues(PHPIndexer.FIELD_CONST);
                for (String value : values) {
                    Signature sig = Signature.get(value);
                    String name = sig.string(1);

                    if (name.length() > 0 && name.startsWith(prefix)) {
                        Integer offset = sig.integer(2);
                        results.add(new PhpIndexResult(name, indexFile, PhpIndexResult.Type.CONSTANT, new OffsetRange(offset, offset + name.length())));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return results;
    }

    public static Collection<PhpIndexResult> queryNamespace(FileObject fo, String prefix) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);
        Collection<PhpIndexResult> results = new ArrayList<>();
        Collection<String> namespaces = new ArrayList<>();
        //subfolders with lowercase ; rootFolder
        //third signature namespace
        //the first el is the folder
        String originalPrefix = prefix;

        if (prefix.endsWith(ESCAPED_NAMESPACE_SEPARATOR)) {
            return results;
        }

        String[] queryItems = prefix.split(ESCAPED_NAMESPACE_SEPARATOR);

        if (queryItems.length == 0) {
            return results;
        }

        String queryPrefix = prefix.toLowerCase();

        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(
                    PHPIndexer.FIELD_TOP_LEVEL, queryPrefix + NAMESPACE_SEPARATOR, QuerySupport.Kind.PREFIX, new String[]{
                        PHPIndexer.FIELD_NAMESPACE, PHPIndexer.FIELD_TOP_LEVEL});
            for (IndexResult indexResult : indexResults) {
                FileObject indexFile = indexResult.getFile();
                String topFieldValue = indexResult.getValue(PHPIndexer.FIELD_TOP_LEVEL);
                //internal php index
                if (topFieldValue.startsWith(prefix.toLowerCase())) {
                    String firstValue = indexResult.getValue(PHPIndexer.FIELD_NAMESPACE);
                    if (firstValue == null || firstValue.isEmpty()) {
                        continue;
                    }
                    Signature sig = Signature.get(firstValue);

                    String name = sig.string(1);
                    String namespace = sig.string(2);

                    String fullNamespace = ""; // NOI18N

                    if (!namespace.isEmpty()) {
                        fullNamespace = namespace + NAMESPACE_SEPARATOR;
                    }

                    fullNamespace += name;

                    //just one namespace is enough
                    if (fullNamespace.startsWith(originalPrefix) && !namespaces.contains(fullNamespace)) {
                        namespaces.add(fullNamespace);
                        results.add(new PhpIndexResult(fullNamespace, indexFile, PhpIndexResult.Type.NAMESPACE, new OffsetRange(0, 1)));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return results;
    }

    public static Collection<PhpIndexResult> queryNamespaces(FileObject fo, String namespace,
            QuerySupport.Kind queryType) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);
        Collection<PhpIndexResult> results = new ArrayList<>();
        String queryPrefix = namespace.toLowerCase();

        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(
                    PHPIndexer.FIELD_TOP_LEVEL, queryPrefix, queryType,
                    new String[]{
                        PHPIndexer.FIELD_NAMESPACE,
                        PHPIndexer.FIELD_TOP_LEVEL
                    });
            for (IndexResult indexResult : indexResults) {
                FileObject indexFile = indexResult.getFile();
                String namespaceValue = indexResult.getValue(PHPIndexer.FIELD_NAMESPACE);
                //no namespace found
                if (namespaceValue == null) {
                    continue;
                }
                results.add(new PhpIndexResult(namespaceValue, indexFile, PhpIndexResult.Type.NAMESPACE, new OffsetRange(0, 1)));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return results;
    }

    /**
     * a optimized hack solution assuming that the name of the class is the same
     * with the file
     *
     * @param fo
     * @param namespace
     * @return
     */
    public static Collection<PhpIndexResult> queryAllNamespaceClasses(FileObject fo, String namespace) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);
        Collection<PhpIndexResult> results = new ArrayList<>();
        String queryPrefix = namespace.toLowerCase();

        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(
                    PHPIndexer.FIELD_TOP_LEVEL, queryPrefix, QuerySupport.Kind.EXACT,
                    new String[]{
                        PHPIndexer.FIELD_NAMESPACE,
                        PHPIndexer.FIELD_TOP_LEVEL
                    });
            for (IndexResult indexResult : indexResults) {
                FileObject indexFile = indexResult.getFile();
                String namespaceValue = indexResult.getValue(PHPIndexer.FIELD_NAMESPACE);
                //no namespace found
                if (namespaceValue == null) {
                    continue;
                }
                results.add(new PhpIndexResult(indexFile.getName(), indexFile, PhpIndexResult.Type.CLASS, new OffsetRange(0, 1)));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return results;
    }

    public static Collection<PhpIndexResult> queryClassConstants(FileObject fo, String prefix, String ownerClass) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);
        Collection<PhpIndexResult> results = new ArrayList<>();
        String queryPrefix = prefix.toLowerCase();
        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(PHPIndexer.FIELD_CLASS_CONST, queryPrefix, QuerySupport.Kind.PREFIX, new String[]{
                PHPIndexer.FIELD_CLASS_CONST, PHPIndexer.FIELD_CLASS});
            for (IndexResult indexResult : indexResults) {
                FileObject indexFile = indexResult.getFile();
                //internal php index

                String classOwnerName = indexResult.getValue(PHPIndexer.FIELD_CLASS);
                if (classOwnerName == null || !classOwnerName.startsWith(ownerClass.toLowerCase())) {
                    continue;
                }
                String[] values = indexResult.getValues(PHPIndexer.FIELD_CLASS_CONST);
                for (String value : values) {
                    Signature sig = Signature.get(value);
                    String name = sig.string(1);

                    if (name.length() > 0 && name.startsWith(prefix)) {
                        Integer offset = sig.integer(2);
                        results.add(new PhpIndexResult(name, indexFile, PhpIndexResult.Type.CONSTANT, new OffsetRange(offset, offset + name.length())));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return results;
    }

    /**
     * @notused TO BE continued
     *
     * @param fo
     * @param prefix
     * @param ownerClass
     * @return
     */
    public static Collection<PhpIndexResult> queryClassProperties(FileObject fo, String prefix, String ownerClass) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);
        Collection<PhpIndexResult> results = new ArrayList<>();
        String queryPrefix = prefix.toLowerCase();
        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(PHPIndexer.FIELD_FIELD, queryPrefix, QuerySupport.Kind.PREFIX, new String[]{
                PHPIndexer.FIELD_FIELD, PHPIndexer.FIELD_CLASS});
            for (IndexResult indexResult : indexResults) {
                FileObject indexFile = indexResult.getFile();
                //internal php index

                String classOwnerName = indexResult.getValue(PHPIndexer.FIELD_CLASS);
                if (classOwnerName == null || !classOwnerName.startsWith(ownerClass.toLowerCase() + ";")) {
                    continue;
                }
                String[] values = indexResult.getValues(PHPIndexer.FIELD_FIELD);
                for (String value : values) {
                    Signature sig = Signature.get(value);
                    String name = sig.string(1);

                    if (name.length() > 0 && name.equals(prefix)) {
                        Integer offset = sig.integer(2);
                        results.add(new PhpIndexResult(name, indexFile, PhpIndexResult.Type.CONSTANT, new OffsetRange(offset, offset + name.length())));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return results;
    }

    public static Collection<PhpIndexResult> queryExactClassConstants(FileObject fo, String prefix, String ownerClass) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);
        Collection<PhpIndexResult> results = new ArrayList<>();
        String queryPrefix = prefix.toLowerCase();
        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(PHPIndexer.FIELD_CLASS_CONST, queryPrefix, QuerySupport.Kind.PREFIX, new String[]{
                PHPIndexer.FIELD_CLASS_CONST, PHPIndexer.FIELD_CLASS});
            for (IndexResult indexResult : indexResults) {
                FileObject indexFile = indexResult.getFile();
                //internal php index

                String classOwnerName = indexResult.getValue(PHPIndexer.FIELD_CLASS);
                if (!classOwnerName.startsWith(ownerClass.toLowerCase() + ";")) {
                    continue;
                }
                String[] values = indexResult.getValues(PHPIndexer.FIELD_CLASS_CONST);
                for (String value : values) {
                    Signature sig = Signature.get(value);
                    String name = sig.string(1);

                    if (name.length() > 0 && name.equals(prefix)) {
                        Integer offset = sig.integer(2);
                        results.add(new PhpIndexResult(name, indexFile, PhpIndexResult.Type.CONSTANT, new OffsetRange(offset, offset + name.length())));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return results;
    }

    public static Collection<PhpIndexResult> queryExactConstants(FileObject fo, String prefix) {
        QuerySupport phpindex = QuerySupportFactory.get(fo);
        Collection<PhpIndexResult> results = new ArrayList<>();
        String queryPrefix = prefix.toLowerCase();
        try {
            Collection<? extends IndexResult> indexResults = phpindex.query(PHPIndexer.FIELD_CONST, queryPrefix, QuerySupport.Kind.PREFIX, new String[]{PHPIndexer.FIELD_CONST});
            for (IndexResult indexResult : indexResults) {
                FileObject indexFile = indexResult.getFile();
                //internal php index

                String[] values = indexResult.getValues(PHPIndexer.FIELD_CONST);
                for (String value : values) {
                    Signature sig = Signature.get(value);
                    String name = sig.string(1);

                    if (name.length() > 0 && name.equals(prefix)) {
                        Integer offset = sig.integer(2);
                        results.add(new PhpIndexResult(name, indexFile, PhpIndexResult.Type.FUNCTION, new OffsetRange(offset, offset + name.length())));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return results;
    }

    static List<String> parseParameters(final String signature) {
        List<String> retval = new ArrayList<>();
        if (signature != null && signature.length() > 0) {
            final String regexp = String.format("\\%s", ","); //NOI18N

            for (String sign : signature.split(regexp)) {
                try {
                    final String param = parseOneParameter(sign);
                    if (param != null) {
                        retval.add(param);
                    }
                } catch (NumberFormatException originalException) {
                    final String message = String.format("%s [for signature: %s]", originalException.getMessage(), signature); //NOI18N
                    final NumberFormatException formatException = new NumberFormatException(message);
                    formatException.initCause(originalException);
                    throw formatException;
                }
            }
        }
        return retval;
    }

    /**
     * 
     *
     * @param sig
     * @return
     */
    static String parseOneParameter(String sig) {
        String retval = null;
        final String regexp = String.format("\\%s", ":"); //NOI18N
        String[] parts = sig.split(regexp);
        if (parts.length > 0) {
            String paramName = parts[0];
            retval = paramName;
        }
        return retval;
    }

    protected static QueryCache<String, Collection<PhpIndexResult>> getCache(FileObject fo, String prefix) {
        QueryCache<String, Collection<PhpIndexResult>> selfCache = new QueryCache<>();
        Project projectOwner = ProjectConvertors.getNonConvertorOwner(fo);
        if (projectOwner == null) {
            return null;
        }
        int pathHash = projectOwner.getProjectDirectory().toString().hashCode();
        if (PhpIndexUtils.QUERY_SUPPORT_INSTANCES.containsKey(pathHash)) {
            PhpIndexUtils indexUtils = QUERY_SUPPORT_INSTANCES.get(pathHash);
            selfCache = indexUtils.getQueryCache();

        } else {
            QUERY_SUPPORT_INSTANCES.put(pathHash, new PhpIndexUtils());
        }
        return selfCache;
    }

    protected static QueryCache<String, Collection<PhpIndexFunctionResult>> getFunctionCache(FileObject fo, String prefix) {
        QueryCache<String, Collection<PhpIndexFunctionResult>> selfCache = new QueryCache<>();
        Project projectOwner = ProjectConvertors.getNonConvertorOwner(fo);
        if (projectOwner == null) {
            return null;
        }
        int pathHash = projectOwner.getProjectDirectory().toString().hashCode();
        if (PhpIndexUtils.QUERY_SUPPORT_INSTANCES.containsKey(pathHash)) {
            PhpIndexUtils indexUtils = QUERY_SUPPORT_INSTANCES.get(pathHash);
            selfCache = indexUtils.getFunctionQueryCache();

        } else {
            QUERY_SUPPORT_INSTANCES.put(pathHash, new PhpIndexUtils());
        }
        return selfCache;
    }

    public QueryCache<String, Collection<PhpIndexResult>> getQueryCache() {
        return cache;
    }

    public QueryCache<String, Collection<PhpIndexFunctionResult>> getFunctionQueryCache() {
        return functionCache;
    }
}
