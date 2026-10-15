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
package org.netbeans.modules.groovy.editor.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.groovy.editor.api.elements.common.MethodElement.MethodParameter;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedClass;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedElement;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedField;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedMethod;
import org.netbeans.modules.groovy.editor.utils.GroovyUtils;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 * 
 * @author Tor Norbye
 * @author Martin Adamek
 */
public final class GroovyIndex {

    private static final Logger LOG = Logger.getLogger(GroovyIndex.class.getName());
    private static final GroovyIndex EMPTY = new GroovyIndex(null);
    private static final String CLUSTER_URL = "cluster:"; // NOI18N
    private static String clusterUrl = null;
    private static String cachedPrefix = null;
    private static boolean cachedInsensitive = true;
    private static Pattern cachedCamelCasePattern = null;
    
    private final QuerySupport querySupport;


    private GroovyIndex(QuerySupport querySupport) {
        this.querySupport = querySupport;
    }

    /**
     * Get the index. Use wisely multiple calls are expensive.
     */
    public static GroovyIndex get(Collection<FileObject> roots) {
        try {
            return new GroovyIndex(QuerySupport.forRoots(
                    GroovyIndexer.Factory.NAME,
                    GroovyIndexer.Factory.VERSION,
                    roots.toArray(new FileObject[0])));
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
            return EMPTY;
        }
    }

    /**
     * Returns all {@link IndexedClass}es that are located in the given package.
     *
     * @param packageName package name for which we want to get {@link IndexedClass}es
     * @return all {@link IndexedClass}es that are located in the given package
     */
    public Set<IndexedClass> getClassesFromPackage(String packageName) {
        Set<IndexedClass> result = new HashSet<>();

        for (IndexedClass indexedClass : getAllClasses()) {
            String pkgName = GroovyUtils.getPackageName(indexedClass.getFqn());
            if (packageName.equals(pkgName)) {
                result.add(indexedClass);
            }
        }
        return result;
    }

    /**
     * Returns all available {@link IndexedClass}es.
     *
     * @return all available {@link IndexedClass}es
     */
    public Set<IndexedClass> getAllClasses() {
        return getClasses(".*", QuerySupport.Kind.REGEXP);
    }

    /**
     * Return the full set of classes that match the given name.
     *
     * @param name The name of the class - possibly a fqn like file.Stat, or just a class
     *   name like Stat, or just a prefix like St.
     * @param kind Whether we want the exact name, or whether we're searching by a prefix.
     * @param includeAll If true, return multiple IndexedClasses for the same logical
     *   class, one for each declaration point.
     */
    public Set<IndexedClass> getClasses(String name, final QuerySupport.Kind kind) {
        String classFqn = null;

        if (name != null) {
            // CamelCase check is here because of issue #212878
            if (name.endsWith(".") && (QuerySupport.Kind.CAMEL_CASE != kind)) {
                // User has typed something like "Test." and wants completion on
                // for something like Test.Unit
                classFqn = name.substring(0, name.length() - 1);
                name = "";
            }
        }

        final Set<IndexResult> result = new HashSet<>();
        String field;

        switch (kind) {
        case EXACT:
            field = GroovyIndexer.FQN_NAME;
            break;
        case PREFIX:
        case CAMEL_CASE:
        case REGEXP:
            field = GroovyIndexer.CLASS_NAME;
            break;
        case CASE_INSENSITIVE_PREFIX:
        case CASE_INSENSITIVE_CAMEL_CASE:
        case CASE_INSENSITIVE_REGEXP:
            field = GroovyIndexer.CASE_INSENSITIVE_CLASS_NAME;
            break;
        default:
            throw new UnsupportedOperationException(kind.toString());
        }

        search(field, name, kind, result);

        final Set<IndexedClass> classes = new HashSet<>();

        for (IndexResult map : result) {
            String simpleName = map.getValue(GroovyIndexer.CLASS_NAME);

            if (simpleName == null) {
                // It's probably a module
                // XXX I need to handle this... for now punt
                continue;
            }

            // Lucene returns some inexact matches, TODO investigate why this is necessary
            if ((kind == QuerySupport.Kind.PREFIX) && !simpleName.startsWith(name)) {
                continue;
            } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !simpleName.regionMatches(true, 0, name, 0, name.length())) {
                continue;
            }

            if (classFqn != null) {
                String in = map.getValue(GroovyIndexer.IN);
                if (kind == QuerySupport.Kind.CASE_INSENSITIVE_REGEXP) {
                    if (!classFqn.equalsIgnoreCase(map.getValue(GroovyIndexer.IN))) {
                        continue;
                    }
                } else if (kind == QuerySupport.Kind.CAMEL_CASE && !matchCamelCase(classFqn, in, false)) {    
                    continue;
                } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_CAMEL_CASE && !matchCamelCase(classFqn, in, true)) {    
                    continue;
                } else {
                    if (!classFqn.equals(map.getValue(GroovyIndexer.IN))) {
                        continue;
                    }
                }
            }

            String attrs = map.getValue(GroovyIndexer.CLASS_ATTRS);
            boolean isClass = true;
            if (attrs != null) {
                int flags = IndexedElement.stringToFlag(attrs, 0);
                isClass = (flags & IndexedClass.MODULE) == 0;
            }

            String fqn = map.getValue(GroovyIndexer.FQN_NAME);

            classes.add(createClass(fqn, simpleName, map));
        }

        return classes;
    }

    /**
     * For the given class name finds explicitely declared constructors.
     *
     * @param className name of the class
     * @return explicitely declared constructors
     */
    public Set<IndexedMethod> getConstructors(final String className) {
        final Set<IndexResult> indexResult = new HashSet<>();
        final Set<IndexedMethod> result = new HashSet<>();

        search(GroovyIndexer.CONSTRUCTOR, className, QuerySupport.Kind.PREFIX, indexResult);

        for (IndexResult map : indexResult) {
            String[] constructors = map.getValues(GroovyIndexer.CONSTRUCTOR);

            for (String constructor : constructors) {
                String[] parts = constructor.split(";");
                if (parts.length < 4) {
                    continue;
                }
                String paramList = parts[1];
                String[] params = paramList.split(",");

                List<MethodParameter> methodParams = new ArrayList<>();
                for (String param : params) {
                    if (!"".equals(param.trim())) { // NOI18N
                        methodParams.add(new MethodParameter(param, GroovyUtils.stripPackage(param)));
                    }
                }
                int flags = 0;
                if (!parts[2].isEmpty()) {
                    flags = IndexedElement.stringToFlag(parts[2], 0);
                }
                
                IndexedMethod c = new IndexedMethod(map, className, className, "void", methodParams, "", flags);
                OffsetRange range = createOffsetRange(parts[3]);
                if (range != null) {
                    c.setOffsetRange(range);
                }
                
                result.add(c);
            }
        }

        return result;
    }

    /**
     * Return a set of methods that match the given name prefix, and are in the given
     * class and module. If no class is specified, match methods across all classes.
     * Note that inherited methods are not checked. If you want to match inherited methods
     * you must call this method on each superclass as well as the mixin modules.
     */
    @SuppressWarnings("fallthrough")
    public Set<IndexedMethod> getMethods(final String name, final String clz, QuerySupport.Kind kind) {
        final Set<IndexResult> result = new HashSet<>();

        String field = GroovyIndexer.METHOD_NAME;
        QuerySupport.Kind originalKind = kind;
        if (kind == QuerySupport.Kind.EXACT) {
            // I can't do exact searches on methods because the method
            // entries include signatures etc. So turn this into a prefix
            // search and then compare chopped off signatures with the name
            kind = QuerySupport.Kind.PREFIX;
        }

        search(field, name, kind, result);

        // TODO Prune methods to fit my scheme - later make lucene index smarter about how to prune its index search
        final Set<IndexedMethod> methods = new HashSet<>();

        for (IndexResult map : result) {
            if (clz != null) {
                String fqn = map.getValue(GroovyIndexer.FQN_NAME);

                if (!(clz.equals(fqn))) {
                    continue;
                }
            }

            String[] signatures = map.getValues(GroovyIndexer.METHOD_NAME);

            if (signatures != null) {
                for (String signature : signatures) {
                    // Skip weird methods... Think harder about this
                    if (((name == null) || (name.length() == 0)) &&
                            !Character.isLowerCase(signature.charAt(0))) {
                        continue;
                    }

                    // Lucene returns some inexact matches, TODO investigate why this is necessary
                    if ((kind == QuerySupport.Kind.PREFIX) && !signature.startsWith(name)) {
                        continue;
                    } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, name, 0, name.length())) {
                        continue;
                    } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_CAMEL_CASE && !matchCamelCase(name, signature, true)) {    
                        continue;
                    } else if (kind == QuerySupport.Kind.CAMEL_CASE && !matchCamelCase(name, signature, false)) {    
                        continue;
                    } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_REGEXP) {
                        int len = signature.length();
                        int end = signature.indexOf('(');
                        if (end == -1) {
                            end = signature.indexOf(';');
                            if (end == -1) {
                                end = len;
                            }
                        }
                        String n = end != len ? signature.substring(0, end) : signature;
                        try {
                            if (!n.matches(name)) {
                                continue;
                            }
                        } catch (PatternSyntaxException e) {
                            // Silently ignore regexp failures in the search expression
                        }
                    } else if (originalKind == QuerySupport.Kind.EXACT) {
                        // Make sure the name matches exactly
                        // We know that the prefix is correct from the first part of
                        // this if clause, by the signature may have more
                        if (((signature.length() > name.length()) &&
                                (signature.charAt(name.length()) != '(')) &&
                                (signature.charAt(name.length()) != ';')) {
                            continue;
                        }
                    }

                    // XXX THIS DOES NOT WORK WHEN THERE ARE IDENTICAL SIGNATURES!!!
                    assert map != null;
                    IndexedMethod method = createMethod(signature, map);
                    if (method != null) {
                        methods.add(method);
                    }
                }
            }
        }

        return methods;
    }
    
    // protected for test reasons.
    protected static boolean matchCamelCase(String prefix, String where, boolean insensitive) {
        if (where == null || where.length() == 0 || prefix == null || prefix.length() == 0) {
            return false;
        }
        
        synchronized (GroovyIndex.class) {
            if (!prefix.equals(cachedPrefix) || cachedInsensitive != insensitive) {
                StringBuilder sb = new StringBuilder();
                int lastIndex = 0;
                int index;
                do {
                    index = findNextUpper(prefix, lastIndex + 1);
                    String token = prefix.substring(lastIndex, index == -1 ? prefix.length() : index);
                    sb.append(token);
                    sb.append(index != -1 ? "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N         
                    lastIndex = index;
                } while (index != -1);
                cachedPrefix = prefix;
                cachedInsensitive = insensitive;
                cachedCamelCasePattern = insensitive ? Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE) : Pattern.compile(sb.toString());
            }
        }
        return cachedCamelCasePattern.matcher(where).matches();
    }
    
    private static int findNextUpper(String text, int offset) {
        for (int i = offset; i < text.length(); i++) {
            if (Character.isUpperCase(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Gets all fields for the given fully qualified name.
     *
     * @param fqName fully qualified name
     * @return all fields for the given type
     */
    public Set<IndexedField> getAllFields(final String fqName) {
        return getFields(".*", fqName, QuerySupport.Kind.REGEXP); // NOI18N
    }

    /**
     * Gets all static fields for the given fully qualified name.
     *
     * @param fqName fully qualified name
     * @return all static fields for the given type
     */
    public Set<IndexedField> getStaticFields(final String fqName) {
        Set<IndexedField> fields = getFields(".*", fqName, QuerySupport.Kind.REGEXP); // NOI18N
        Set<IndexedField> staticFields = new HashSet<>();

        for (IndexedField field : fields) {
            if (field.getModifiers().contains(Modifier.STATIC)) {
                staticFields.add(field);
            }
        }
        return staticFields;
    }

    public Set<IndexedField> getFields(final String name, final String clz, QuerySupport.Kind kind) {
        boolean inherited = clz == null;
        final Set<IndexResult> result = new HashSet<>();

        String field = GroovyIndexer.FIELD_NAME;
        QuerySupport.Kind originalKind = kind;
        if (kind == QuerySupport.Kind.EXACT) {
            // I can't do exact searches on methods because the method
            // entries include signatures etc. So turn this into a prefix
            // search and then compare chopped off signatures with the name
            kind = QuerySupport.Kind.PREFIX;
        }

        search(field, name, kind, result);

        // TODO Prune methods to fit my scheme - later make lucene index smarter about how to prune its index search
        final Set<IndexedField> fields = new HashSet<>();

        for (IndexResult map : result) {
            if (clz != null) {
                String fqn = map.getValue(GroovyIndexer.FQN_NAME);

                if (!(clz.equals(fqn))) {
                    continue;
                }
            }

            String[] signatures = map.getValues(GroovyIndexer.FIELD_NAME);

            if (signatures != null) {
                for (String signature : signatures) {
                    // Skip weird methods... Think harder about this
                    if (((name == null) || (name.length() == 0)) &&
                            !Character.isLowerCase(signature.charAt(0))) {
                        continue;
                    }

                    // Lucene returns some inexact matches, TODO investigate why this is necessary
                    if ((kind == QuerySupport.Kind.PREFIX) && !signature.startsWith(name)) {
                        continue;
                    } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, name, 0, name.length())) {
                        continue;
                    } else {
                        int len = signature.length();
                        int end = signature.indexOf(';');
                        if (end == -1) {
                            end = len;
                        }

                        String fieldName = end != len ? signature.substring(0, end) : signature;
                        if (originalKind == QuerySupport.Kind.EXACT && !name.equals(fieldName)) {
                            continue;
                        } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_REGEXP) {
                            try {
                                if (!fieldName.matches(name)) {
                                    continue;
                                }
                            } catch (PatternSyntaxException e) {
                                // Silently ignore regexp failures in the search expression
                            }
                        } else if (kind == QuerySupport.Kind.CAMEL_CASE && !matchCamelCase(name, fieldName, false)) {
                            continue;
                        } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_CAMEL_CASE && !matchCamelCase(name, fieldName, true)) {
                            continue;
                        }
                    }

                    // XXX THIS DOES NOT WORK WHEN THERE ARE IDENTICAL SIGNATURES!!!
                    assert map != null;
                    IndexedField createdField = createField(signature, map, inherited);
                    if (createdField != null) {
                        fields.add(createdField);
                    }
                }
            }
        }

        return fields;
    }
    
    /**
     * Get the set of inherited (through super classes and mixins) for the given fully qualified class name.
     * @param classFqn FQN: module1.module2.moduleN.class
     * @param prefix If kind is NameKind.PREFIX/CASE_INSENSITIVE_PREFIX, a prefix to filter methods by. Else,
     *    if kind is NameKind.EXACT_NAME filter methods by the exact name.
     * @param kind Whether the prefix field should be taken as a prefix or a whole name
     */
    public Set<IndexedMethod> getInheritedMethods(String classFqn, String prefix, QuerySupport.Kind kind) {
        Set<IndexedMethod> methods = new HashSet<>();
        Set<String> scannedClasses = new HashSet<>();
        Set<String> seenSignatures = new HashSet<>();

        if (prefix == null) {
            prefix = "";
        }

        addMethodsFromClass(prefix, kind, classFqn, methods, seenSignatures, scannedClasses);

        return methods;
    }

    /** Return whether the specific class referenced (classFqn) was found or not. This is
     * not the same as returning whether any classes were added since it may add
     * additional methods from parents (Object/Class).
     */
    private boolean addMethodsFromClass(String prefix, QuerySupport.Kind kind, String classFqn,
        Set<IndexedMethod> methods, Set<String> seenSignatures, Set<String> scannedClasses) {
        // Prevent problems with circular includes or redundant includes
        if (scannedClasses.contains(classFqn)) {
            return false;
        }

        scannedClasses.add(classFqn);

        String searchField = GroovyIndexer.FQN_NAME;

        Set<IndexResult> result = new HashSet<>();

        search(searchField, classFqn, QuerySupport.Kind.EXACT, result);

        boolean foundIt = result.size() > 0;

        // If this is a bogus class entry (no search rsults) don't continue
        if (!foundIt) {
            return foundIt;
        }

        for (IndexResult map : result) {
            assert map != null;

            String[] signatures = map.getValues(GroovyIndexer.METHOD_NAME);

            if (signatures != null) {
                for (String signature : signatures) {
                    // Skip weird methods like "[]" etc. in completion lists... TODO Think harder about this
                    if ((prefix.length() == 0) && !Character.isLowerCase(signature.charAt(0))) {
                        continue;
                    }

                    // Prevent duplicates when method is redefined
                    if (!seenSignatures.contains(signature)) {
                        if (signature.startsWith(prefix)) {
                            if (kind == QuerySupport.Kind.EXACT) {
                                // Ensure that the method is not longer than the prefix
                                if ((signature.length() > prefix.length()) &&
                                        (signature.charAt(prefix.length()) != '(') &&
                                        (signature.charAt(prefix.length()) != ';')) {
                                    continue;
                                }
                            } else {
                                // REGEXP, CAMELCASE filtering etc. not supported here
                                assert (kind == QuerySupport.Kind.PREFIX) ||
                                (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);
                            }

                            seenSignatures.add(signature);

                            IndexedMethod method = createMethod(signature, map);
                            if (method != null) {
                                methods.add(method);
                            }
                        }
                    }
                }
            }
        }

//        if (extendsClass == null) {
            // XXX GroovyObject, GroovyScript
        addMethodsFromClass(prefix, kind, "java.lang.Object", methods, seenSignatures, scannedClasses); // NOI18N

        return foundIt;
    }

    private IndexedClass createClass(String fqn, String simpleName, IndexResult map) {

        // TODO - how do I determine -which- file to associate with the file?
        // Perhaps the one that defines initialize() ?

        if (simpleName == null) {
            simpleName = map.getValue(GroovyIndexer.CLASS_NAME);
        }

        String attrs = map.getValue(GroovyIndexer.CLASS_ATTRS);

        int flags = 0;
        if (attrs != null) {
            flags = IndexedElement.stringToFlag(attrs, 0);
        }

        IndexedClass c = IndexedClass.create(simpleName, fqn, map, attrs, flags);

        String offset = map.getValue(GroovyIndexer.CLASS_OFFSET);
        if (offset != null) {
            OffsetRange range = createOffsetRange(offset);
            if (range != null) {
                c.setOffsetRange(range);
            }
        }
        return c;
    }

    private IndexedMethod createMethod(String signature, IndexResult map) {
        String clz = map.getValue(GroovyIndexer.CLASS_NAME);
        String module = map.getValue(GroovyIndexer.IN);

        if (clz == null) {
            // Module method?
            clz = module;
        } else if ((module != null) && (module.length() > 0)) {
            clz = module + "." + clz; // NOI18N
        }

        //String fqn = map.getValue(GroovyIndexer.FQN_NAME);

        String[] parts = signature.split(";");
        if (parts.length < 4) {
            return null;
        }
        String methodSignature = parts[0];
        String type = parts[1].isEmpty() ? "void" : parts[1];
        int flags = parts[2].isEmpty() ? 0 : IndexedElement.stringToFlag(parts[2], 0);
        OffsetRange range = createOffsetRange(parts[3]);        
        String attributes = parts[2];

        IndexedMethod m = new IndexedMethod(map, clz, getMethodName(methodSignature), type, getMethodParameter(methodSignature), attributes, flags);
        
        if (range != null) {
            m.setOffsetRange(range);
        }   

        return m;
    }

    private String getMethodName(String methodSignature) {
        int parenIndex = methodSignature.indexOf('(');
        if (parenIndex == -1) {
            return methodSignature;
        } else {
            return methodSignature.substring(0, parenIndex);
        }
    }

    private List<MethodParameter> getMethodParameter(String methodSignature) {
        int parenIndex = methodSignature.indexOf('('); // NOI18N
        if (parenIndex == -1) {
            return Collections.emptyList();
        }

        String argsPortion = methodSignature.substring(parenIndex + 1, methodSignature.length() - 1);
        String[] args = argsPortion.split(","); // NOI18N

        if (args == null || args.length <= 0) {
            return Collections.emptyList();
        }

        List<MethodParameter> parameters = new ArrayList<>();
        for (String paramType : args) {
            parameters.add(new MethodParameter(paramType, GroovyUtils.stripPackage(paramType)));
        }
        return parameters;
    }
    
    private IndexedField createField(String signature, IndexResult map, boolean inherited) {
        String clz = map.getValue(GroovyIndexer.CLASS_NAME);
        String module = map.getValue(GroovyIndexer.IN);

        if (clz == null) {
            // Module method?
            clz = module;
        } else if ((module != null) && (module.length() > 0)) {
            clz = module + "." + clz; // NOI18N
        }

        //String fqn = map.getValue(GroovyIndexer.FQN_NAME);

        String[] parts = signature.split(";");
        if (parts.length < 5) {
            return null;
        }
        String name = parts[0];
        String type = parts[1].isEmpty() ? "java.lang.Object" : parts[1];
        int flags = parts[2].isEmpty() ? 0 : IndexedElement.stringToFlag(parts[2], 0);
        String isProperty = parts[3];
        String attributes = flags + ";" + isProperty;
        OffsetRange range = createOffsetRange(parts[4]);
        
        IndexedField m = IndexedField.create(type, name, clz, map, attributes, flags);
        m.setInherited(inherited);
        if (range != null) {
            m.setOffsetRange(range);
        }   
        return m;
    }

    private static OffsetRange createOffsetRange(String text) {
        OffsetRange result = null;
        int offsetStartIndex = text.indexOf('[');
        int commaIndex = text.indexOf(',', offsetStartIndex + 1);
        int offsetLastIndex = text.indexOf(']', commaIndex + 1);
        if (offsetStartIndex != -1 && commaIndex != -1 && offsetLastIndex != -1) {
            int startOffset = Integer.parseInt(text.substring(offsetStartIndex + 1, commaIndex));
            int endOffset = Integer.parseInt(text.substring(commaIndex + 1, offsetLastIndex));
            result = new OffsetRange(startOffset, endOffset);
        }
        return result;
    }
    
    private boolean search(String key, String name, QuerySupport.Kind kind, Set<IndexResult> result) {
        try {
            result.addAll(querySupport.query(key, name, kind));
            return true;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return false;
        }
    }

    public static void setClusterUrl(String url) {
        clusterUrl = url;
    }

    static String getPreindexUrl(String url) {
        String s = getClusterUrl();

        if (url.startsWith(s)) {
            return CLUSTER_URL + url.substring(s.length());
        }

        return url;
    }

    static String getClusterUrl() {
        if (clusterUrl == null) {
            File f =
                InstalledFileLocator.getDefault()
                                    .locate("modules/org-netbeans-modules-groovy-editor.jar", null, false); // NOI18N

            if (f == null) {
                throw new RuntimeException("Can't find cluster");
            }

            f = new File(f.getParentFile().getParentFile().getAbsolutePath());

            try {
                f = f.getCanonicalFile();
                clusterUrl = f.toURI().toURL().toExternalForm();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        return clusterUrl;
    }
}
