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

package org.netbeans.modules.java.source.usages;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Convertors;
import org.netbeans.modules.parsing.lucene.support.Queries;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public class DocumentUtil {


    static final String FIELD_PACKAGE_NAME = "packageName";     //NOI18N
    static final String FIELD_SIMPLE_NAME = "simpleName";       //NOI18N
    static final String FIELD_CASE_INSENSITIVE_NAME = "ciName"; //NOI18N
    static final String FIELD_IDENTS = "ids";                           //NOI18N
    static final String FIELD_FEATURE_IDENTS = "fids";                  //NOI18N
    static final String FIELD_CASE_INSENSITIVE_FEATURE_IDENTS = "cifids"; //NOI18N
    private static final String FIELD_BINARY_NAME = "binaryName";         //NOI18N
    private static final String FIELD_SOURCE = "source";                //NOI18N
    private static final String FIELD_REFERENCES = "references";        //NOI18N

    private static final char NO = '-';                                 //NOI18N
    private static final char YES = '+';                                //NOI18N
    private static final char WILDCARD_QUERY_WILDCARD = '?';            //NOI18N
    private static final char REGEX_QUERY_WILDCARD = '.';               //NOI18N

    private static final char EK_CLASS = 'C';                           //NOI18N
    private static final char EK_LOCAL_CLASS = 'c';                     //NOI18N
    private static final char EK_RECORD = 'R';                          //NOI18N
    private static final char EK_LOCAL_RECORD = 'r';                    //NOI18N
    private static final char EK_INTERFACE = 'I';                       //NOI18N
    private static final char EK_LOCAL_INTERFACE = 'i';                 //NOI18N
    private static final char EK_ENUM = 'E';                            //NOI18N
    private static final char EK_LOCAL_ENUM = 'e';                      //NOI18N
    private static final char EK_ANNOTATION = 'A';                      //NOI18N
    private static final char EK_LOCAL_ANNOTATION = 'a';                //NOI18N
    private static final char EK_MODULE = 'M';                          //NOI18N
    private static final int SIZE = ClassIndexImpl.UsageType.values().length;

    private DocumentUtil () {
    }

    public static Analyzer createAnalyzer() {
        final PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new KeywordAnalyzer());
        analyzer.addAnalyzer(DocumentUtil.FIELD_IDENTS, new WhitespaceAnalyzer());
        analyzer.addAnalyzer(DocumentUtil.FIELD_FEATURE_IDENTS, new WhitespaceAnalyzer());
        analyzer.addAnalyzer(DocumentUtil.FIELD_CASE_INSENSITIVE_FEATURE_IDENTS, new DocumentUtil.LCWhitespaceAnalyzer());
        return analyzer;
    }

    //Convertor factories
    @NonNull
    public static Convertor<Document,FileObject> fileObjectConvertor (
            @NonNull final ClassIndex.ResourceType resourceType,
            @NonNull final FileObject... roots) {
        assert resourceType != null;
        assert roots != null;
        return new FileObjectConvertor (resourceType, roots);
    }

    @NonNull
    public static Convertor<Document, ElementHandle<TypeElement>> typeElementConvertor() {
        List<ElementKind> eleKindList = new ArrayList<>();
        ElementKind[] otherElekinds = {ElementKind.ENUM, ElementKind.INTERFACE, ElementKind.ANNOTATION_TYPE, ElementKind.RECORD};
        eleKindList.addAll(Arrays.asList(otherElekinds));
        return new ElementHandleConvertor<>(
                ElementKind.CLASS, eleKindList.toArray(new ElementKind[0]));

    }
    
    @NonNull
    public static Convertor<Document,ElementHandle<ModuleElement>> moduleElementConvertor() {
        return new ElementHandleConvertor<>(ElementKind.MODULE);
    }

    public static Convertor<Document,String> binaryNameConvertor () {
        return new BinaryNameConvertor ();
    }

    public static Convertor<Document,String> sourceNameConvertor () {
        return new SourceNameConvertor();
    }

    static Convertor<Pair<Pair<BinaryName,String>,Object[]>,Document> documentConvertor() {
        return new DocumentConvertor();
    }

    static Convertor<Pair<String,String>,Query> queryClassWithEncConvertor(final boolean fileBased) {
        return new QueryClassesWithEncConvertor(fileBased);
    }

    static Convertor<Pair<String,String>,Query> queryClassConvertor() {
        return new QueryClassConvertor();
    }

    //Document field getters
    static String getBinaryName (final Document doc) {
        return getBinaryName(doc, null);
    }

    static String getBinaryName (final Document doc, final ElementKind[] kind) {
        assert doc != null;
        final Field pkgField = doc.getField(FIELD_PACKAGE_NAME);
        final Field snField = doc.getField (FIELD_BINARY_NAME);
        if (snField == null) {
            return null;
        }
        final String tmp = snField.stringValue();
        final String snName = tmp.substring(0,tmp.length()-1);
        if (snName.length() == 0) {
            return null;
        }
        if (kind != null) {
            assert kind.length == 1;
            kind[0] = decodeKind (tmp.charAt(tmp.length()-1));
        }
        if (pkgField == null) {
            return snName;
        }
        String pkg = pkgField.stringValue();
        if (pkg.length() == 0) {
            return snName;
        }
        return  pkg + BinaryName.PKG_SEPARATOR + snName;   //NO I18N
    }

    public static String getSimpleBinaryName (final Document doc) {
        assert doc != null;
        Fieldable field = doc.getFieldable(FIELD_BINARY_NAME);
        if (field == null) {
            return null;
        } else {
            final String binName = field.stringValue();
            return binName.substring(0, binName.length()-1);
        }
    }
    
    public static String getSimpleName(final Document doc) {
        final Fieldable field = doc.getFieldable(FIELD_SIMPLE_NAME);
        return field == null ?
                null :
                field.stringValue();
    }

    public static boolean isLocal(@NonNull final Document doc) {
        Fieldable fld = doc.getFieldable(FIELD_BINARY_NAME);
        if (fld == null) {
            return false;
        } else {
            final String binName = fld.stringValue();
            switch (binName.charAt(binName.length()-1)) {
                case EK_LOCAL_ANNOTATION:
                case EK_LOCAL_CLASS:
                case EK_LOCAL_ENUM:
                case EK_LOCAL_INTERFACE:
                    return true;
                default:
                    return false;
            }
        }
    }

    static String getPackageName (final Document doc) {
        assert doc != null;
        Field field = doc.getField(FIELD_PACKAGE_NAME);
        return field == null ? null : field.stringValue();
    }


    //Term and query factories
    static Query binaryNameQuery (final String resourceName) {
        final BooleanQuery query = new BooleanQuery ();
        int index = resourceName.lastIndexOf(BinaryName.PKG_SEPARATOR);  // NOI18N
        String pkgName, sName;
        if (index < 0) {
            pkgName = "";   // NOI18N
            sName = resourceName;
        }
        else {
            pkgName = resourceName.substring(0,index);
            sName = resourceName.substring(index+1);
        }
        sName = sName + WILDCARD_QUERY_WILDCARD;   //Type of type element (Enum, Class, Interface, Annotation)
        query.add (new TermQuery (new Term (FIELD_PACKAGE_NAME, pkgName)),BooleanClause.Occur.MUST);
        query.add (new WildcardQuery (new Term (FIELD_BINARY_NAME, sName)),BooleanClause.Occur.MUST);
        return query;
    }

    static Term referencesTerm (
        String resourceName,
        final Set<? extends ClassIndexImpl.UsageType> usageType,
        final boolean javaRegEx) {
        assert resourceName  != null;
        if (!resourceName.isEmpty()) {
            final char wildcard = javaRegEx ? REGEX_QUERY_WILDCARD : WILDCARD_QUERY_WILDCARD;
            final char[] yes = javaRegEx ? new char[] {'\\',YES} : new char[] {YES};
            if (usageType != null) {
                resourceName = encodeUsage (resourceName, usageType, wildcard, yes).toString();
            } else {
                final StringBuilder sb = new StringBuilder (resourceName);
                for (int i = 0; i< SIZE; i++) {
                    sb.append(wildcard);
                }
                resourceName = sb.toString();
            }
        }
        return new Term (FIELD_REFERENCES, resourceName);
    }

    //Factories for lucene document
    private static Document createDocument (final BinaryName name,
            List<String> references,
            String featureIdents,
            String idents,
            String source) {
        assert references != null;
        
        final String pkgName = name.getPackage();         //Package
        final String fileName = name.getClassNameKind();  //name with no package and appended type character
        assert fileName.length() > 1 : "BinaryName with type char: " + name +
                                       ", Package: " + pkgName +
                                       ", FileName with type char: " + fileName;
        String  simpleName = name.getSimpleName();
        final String caseInsensitiveName = simpleName.toLowerCase();         //XXX: I18N, Locale
        Document doc = new Document ();
        Field field = new Field (FIELD_BINARY_NAME,fileName,Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
        doc.add (field);
        field = new Field (FIELD_PACKAGE_NAME,pkgName,Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
        doc.add (field);
        field = new Field (FIELD_SIMPLE_NAME,simpleName, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
        doc.add (field);
        field = new Field (FIELD_CASE_INSENSITIVE_NAME, caseInsensitiveName, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS);
        doc.add (field);
        for (String reference : references) {
            field = new Field (FIELD_REFERENCES,reference,Field.Store.NO,Field.Index.NOT_ANALYZED_NO_NORMS);
            doc.add(field);
        }
        if (featureIdents != null) {
            field = new Field(FIELD_FEATURE_IDENTS, featureIdents, Field.Store.NO, Field.Index.ANALYZED_NO_NORMS);
            doc.add(field);
            field = new Field(FIELD_CASE_INSENSITIVE_FEATURE_IDENTS, featureIdents, Field.Store.NO, Field.Index.ANALYZED_NO_NORMS);
            doc.add(field);
        }
        if (idents != null) {
            field = new Field(FIELD_IDENTS, idents, Field.Store.NO, Field.Index.ANALYZED_NO_NORMS);
            doc.add(field);
        }
        if (source != null) {
            field = new Field (FIELD_SOURCE,source,Field.Store.YES,Field.Index.NOT_ANALYZED_NO_NORMS);
            doc.add(field);
        }
        return doc;
    }

    // Functions for encoding and decoding of UsageType
    static String encodeUsage (final String className, final Set<ClassIndexImpl.UsageType> usageTypes) {
        return encodeUsage (className, usageTypes, NO, new char[] {YES}).toString();
    }

    private static StringBuilder encodeUsage (
        final String className,
        final Set<? extends ClassIndexImpl.UsageType> usageTypes,
        char fill,
        char[] yes) {
        assert className != null;
        assert usageTypes != null;
        StringBuilder builder = new StringBuilder ();
        builder.append(className);
        for (ClassIndexImpl.UsageType ut : ClassIndexImpl.UsageType.values()) {
            if (usageTypes.contains(ut)) {
                builder.append(yes);
            } else {
                builder.append(fill);
            }
        }
        return builder;
    }

    static String decodeUsage (final String rawUsage, final Set<ClassIndexImpl.UsageType> usageTypes) {
        assert rawUsage != null;
        assert usageTypes != null;
        assert usageTypes.isEmpty();
        final int rawUsageLen = rawUsage.length();
        assert rawUsageLen>SIZE;
        final int index = rawUsageLen - SIZE;
        final String className = rawUsage.substring(0,index);
        final String map = rawUsage.substring (index);
        final ClassIndexImpl.UsageType[] values = ClassIndexImpl.UsageType.values();
        for (int i=0; i< values.length; i++) {
            if (map.charAt(i) == YES) {
                usageTypes.add (values[i]);
            }
        }
        return className;
    }

    @NonNull
    static ElementKind decodeKind (char kind) {
        switch (kind) {
            case EK_CLASS:
            case EK_LOCAL_CLASS:
                return ElementKind.CLASS;
            case EK_INTERFACE:
            case EK_LOCAL_INTERFACE:
                return ElementKind.INTERFACE;
            case EK_ENUM:
            case EK_LOCAL_ENUM:
                return ElementKind.ENUM;
            case EK_ANNOTATION:
            case EK_LOCAL_ANNOTATION:
                return ElementKind.ANNOTATION_TYPE;
            case EK_MODULE:
                return ElementKind.MODULE;
            case EK_RECORD:
            case EK_LOCAL_RECORD:
                return ElementKind.valueOf("RECORD");
            default:
                throw new IllegalArgumentException ();
        }
    }

    static char encodeKind (ElementKind kind) {
        return encodeKind(kind, false);
    }

    static char encodeKind (
            @NonNull final ElementKind kind,
            final boolean isLocal) {
        switch (kind) {
            case CLASS:
                return isLocal ? EK_LOCAL_CLASS : EK_CLASS;
            case INTERFACE:
                return isLocal ? EK_LOCAL_INTERFACE : EK_INTERFACE;
            case ENUM:
                return isLocal ? EK_LOCAL_ENUM : EK_ENUM;
            case ANNOTATION_TYPE:
                return isLocal ? EK_LOCAL_ANNOTATION : EK_ANNOTATION;
            case MODULE:
                return EK_MODULE;
            default:
                if (kind.name().equals("RECORD")) {
                    return isLocal ? EK_LOCAL_RECORD : EK_RECORD;
                }
                throw new IllegalArgumentException ();
        }
    }


    public static FieldSelector declaredTypesFieldSelector (
            final boolean includeSource,
            final boolean includeSimpleName) {
        return includeSource ?
            includeSimpleName ?
                Queries.createFieldSelector(FIELD_PACKAGE_NAME, FIELD_BINARY_NAME, FIELD_SIMPLE_NAME, FIELD_SOURCE) :
                Queries.createFieldSelector(FIELD_PACKAGE_NAME, FIELD_BINARY_NAME, FIELD_SOURCE) :
            includeSimpleName ?
                Queries.createFieldSelector(FIELD_PACKAGE_NAME, FIELD_BINARY_NAME, FIELD_SIMPLE_NAME) :
                Queries.createFieldSelector(FIELD_PACKAGE_NAME, FIELD_BINARY_NAME);
    }

    static FieldSelector sourceNameFieldSelector () {
        return Queries.createFieldSelector(FIELD_SOURCE);
    }

    static Queries.QueryKind translateQueryKind(final ClassIndex.NameKind kind) {
        switch (kind) {
            case SIMPLE_NAME: return Queries.QueryKind.EXACT;
            case PREFIX: return Queries.QueryKind.PREFIX;
            case CASE_INSENSITIVE_PREFIX: return Queries.QueryKind.CASE_INSENSITIVE_PREFIX;
            case CAMEL_CASE: return Queries.QueryKind.CAMEL_CASE;
            case CAMEL_CASE_INSENSITIVE: return Queries.QueryKind.CASE_INSENSITIVE_CAMEL_CASE;
            case REGEXP: return Queries.QueryKind.REGEXP;
            case CASE_INSENSITIVE_REGEXP: return Queries.QueryKind.CASE_INSENSITIVE_REGEXP;
            default: throw new IllegalArgumentException();
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Analyzers Implementation">
    // in Lucene 3.5, WhitespaceTokenizer became final class; isTokenChar was copied.
    private static class LCWhitespaceTokenizer extends CharTokenizer {
        LCWhitespaceTokenizer (final Reader r) {
            super (r);
        }

        @Override
        protected boolean isTokenChar(int c) {
            return !Character.isWhitespace(c);
        }

        protected char normalize(char c) {
            return Character.toLowerCase(c);
        }
    }

    static final class LCWhitespaceAnalyzer extends Analyzer {
        public TokenStream tokenStream(String fieldName, Reader reader) {
            return new LCWhitespaceTokenizer(reader);
        }
    }

    //</editor-fold>


    // <editor-fold defaultstate="collapsed" desc="Result Convertors Implementation">
    private static class FileObjectConvertor implements Convertor<Document,FileObject> {

        private final Convertor<String,String> nameFactory;
        private final Convertor<FileObject,Boolean> filter;
        private final FileObject[] roots;
        private final ElementKind[] kindHolder = new ElementKind[1];

        private FileObjectConvertor (
                @NonNull final ClassIndex.ResourceType type,
                @NonNull final FileObject... roots) {
            this.nameFactory = createNameFactory(type);
            this.filter = createFilter(type);
            this.roots = roots;
        }

        @Override
        public FileObject convert (final Document doc) {
            final String binaryName = getBinaryName(doc, kindHolder);            
            return binaryName == null ?
                    null :
                    kindHolder[0] == ElementKind.MODULE ?
                        resolveFile(FileObjects.MODULE_INFO) :
                        convertType(binaryName);
        }

        private FileObject convertType(@NonNull final String value) {
            FileObject result = resolveFile (value);
            if (result != null) {
                return result;
            }
            final ClassIndexManager cim = ClassIndexManager.getDefault();
            for (FileObject root : roots ) {
                try {
                    ClassIndexImpl impl = cim.getUsagesQuery(root.toURL(), true);
                    if (impl != null) {
                        String sourceName = impl.getSourceName(value);
                        if (sourceName != null) {
                            result = root.getFileObject(sourceName);
                            if (result != null) {
                                return result;
                            }
                        }
                    }
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                } catch (InterruptedException ie) {
                    //Safe to ingnore
                }
            }
            return null;
        }

        private FileObject resolveFile (String classBinaryName) {
            assert classBinaryName != null;
            classBinaryName = classBinaryName.replace('.', '/');    //NOI18N
            int index = classBinaryName.lastIndexOf('/');           //NOI18N
            for (FileObject root : roots) {
                FileObject folder;
                String name;
                if (index<0) {
                    folder = root;
                    name = classBinaryName;
                } else {
                    assert index > 0 : classBinaryName;
                    assert index < classBinaryName.length() - 1 : classBinaryName;
                    folder = root.getFileObject(classBinaryName.substring(0,index));
                    name = classBinaryName.substring(index+1);
                }
                if (folder != null) {                    
                    name = nameFactory.convert(name);
                    for (FileObject child : folder.getChildren()) {
                        if (name.equals(child.getName()) && filter.convert(child)) {
                            return child;
                        }
                    }
                }
            }
            return null;
        }

        @NonNull
        private static Convertor<FileObject,Boolean> createFilter(@NonNull final ClassIndex.ResourceType type) {
            switch (type) {
                case SOURCE:
                    return new Convertor<FileObject,Boolean>() {
                        @Override
                        public Boolean convert(FileObject p) {
                            return FileObjects.JAVA.equalsIgnoreCase(p.getExt());
                        }
                    };
                case BINARY:
                    return new Convertor<FileObject,Boolean>() {
                        @Override
                        public Boolean convert(FileObject p) {
                            return FileObjects.CLASS.equalsIgnoreCase(p.getExt());
                        }
                    };
                default:
                    throw new IllegalArgumentException(String.valueOf(type));
            }
        }

        @NonNull
        private static Convertor<String,String> createNameFactory(@NonNull final ClassIndex.ResourceType type) {
            switch (type) {
                case SOURCE:
                    return new Convertor<String,String>() {
                        @Override
                        public String convert(String p) {
                            final int index = p.indexOf('$');                              //NOI18N
                            if (index > 0) {
                                p = p.substring(0, index);
                            }
                            return p;
                        }
                    };
                case BINARY:
                    return Convertors.<String>identity();
                default:
                    throw new IllegalArgumentException(String.valueOf(type));
            }
        }
    }

    private static class ElementHandleConvertor<T extends Element> implements Convertor<Document,ElementHandle<T>> {

        private final ElementKind[] kindHolder = new ElementKind[1];
        private final Set<ElementKind> supportedElements;
        
        ElementHandleConvertor(
                @NonNull final ElementKind supportedElement,
                @NonNull final ElementKind... rest) {
            this.supportedElements = EnumSet.of(supportedElement, rest);
        }

        @Override
        public ElementHandle<T> convert (final Document doc) {
            final String binaryName = getBinaryName(doc, kindHolder);
            return binaryName == null || !supportedElements.contains(kindHolder[0]) ?
                    null :
                    convert(kindHolder[0], binaryName);
        }

        private ElementHandle<T> convert(ElementKind kind, String value) {
            return ElementHandleAccessor.getInstance().create(kind, value);
        }
    }

    private static class BinaryNameConvertor implements Convertor<Document,String> {

        @Override
        public String convert (final Document doc) {
            return getBinaryName(doc, null);
        }
    }

    private static class SourceNameConvertor implements Convertor<Document,String> {

        @Override
        public String convert(Document doc) {
            Field field = doc.getField(FIELD_SOURCE);
            return field == null ? null : field.stringValue();
        }
    }

    private static class DocumentConvertor implements Convertor<Pair<Pair<BinaryName,String>,Object[]>,Document> {
        @Override
        public Document convert(Pair<Pair<BinaryName, String>, Object[]> entry) {
            final Pair<BinaryName,String> pair = entry.first();
            final BinaryName cn = pair.first();
            final String srcName = pair.second();
            final Object[] data = entry.second();
            final List<String> cr = (List<String>) data[0];
            final String fids = (String) data[1];
            final String ids = (String) data[2];
            return DocumentUtil.createDocument(cn,cr,fids,ids,srcName);
        }
    }

    private static class QueryClassesWithEncConvertor implements Convertor<Pair<String,String>,Query> {

        private final boolean fileBased;

        private QueryClassesWithEncConvertor(final boolean fileBased) {
            this.fileBased = fileBased;
        }

        @Override
        public Query convert(Pair<String, String> p) {
            final String resourceName = p.first();
            final String sourceName = p.second();
            return fileBased ? createClassesInFileQuery(resourceName,sourceName) : createClassWithEnclosedQuery(resourceName, sourceName);
        }

        private static Query createClassWithEnclosedQuery (final String resourceName, final String sourceName) {
            final BooleanQuery query = createFQNQuery(resourceName);
            if (sourceName != null) {
                query.add (new TermQuery(new Term (DocumentUtil.FIELD_SOURCE,sourceName)), Occur.MUST);
            }
            return query;
        }

        private static Query createClassesInFileQuery (final String resourceName, final String sourceName) {
            if (sourceName != null) {
                final BooleanQuery result = new BooleanQuery();
                result.add(createFQNQuery(FileObjects.convertFolder2Package(FileObjects.stripExtension(sourceName))), Occur.SHOULD);
                result.add(new TermQuery(new Term (DocumentUtil.FIELD_SOURCE,sourceName)),Occur.SHOULD);
                return result;
            } else {
                final BooleanQuery result = new BooleanQuery();
                result.add(createFQNQuery(resourceName), Occur.SHOULD);
                result.add(new TermQuery(new Term (
                        DocumentUtil.FIELD_SOURCE,
                        new StringBuilder(FileObjects.convertPackage2Folder(resourceName)).append('.').append(FileObjects.JAVA).toString())),
                        Occur.SHOULD);
                return result;
            }
        }

        private static BooleanQuery createFQNQuery(final String resourceName) {
            String pkgName;
            String sName;
            int index = resourceName.lastIndexOf(BinaryName.PKG_SEPARATOR);
            if (index < 0) {
                pkgName = "";       //NOI18N
                sName = resourceName;
            } else {
                pkgName = resourceName.substring(0, index);
                sName = resourceName.substring(index+1);
            }
            final BooleanQuery snQuery = new BooleanQuery();
            snQuery.add (new WildcardQuery (new Term (DocumentUtil.FIELD_BINARY_NAME, sName + DocumentUtil.WILDCARD_QUERY_WILDCARD)),Occur.SHOULD);
            snQuery.add (new PrefixQuery (new Term (DocumentUtil.FIELD_BINARY_NAME, sName + '$')),Occur.SHOULD);   //NOI18N
            if (pkgName.length() == 0) {
                return snQuery;
            }
            final BooleanQuery fqnQuery = new BooleanQuery();
            fqnQuery.add(new TermQuery(new Term(DocumentUtil.FIELD_PACKAGE_NAME,pkgName)), Occur.MUST);
            fqnQuery.add(snQuery, Occur.MUST);
            return fqnQuery;
        }

    }

    private static class QueryClassConvertor implements Convertor<Pair<String,String>,Query> {
        @Override
        public Query convert(Pair<String, String> p) {
            return binaryNameSourceNamePairQuery(p);
        }

        private static Query binaryNameSourceNamePairQuery (final Pair<String,String> binaryNameSourceNamePair) {
            assert binaryNameSourceNamePair != null;
            final String binaryName = binaryNameSourceNamePair.first();
            final String sourceName = binaryNameSourceNamePair.second();
            assert binaryName != null || sourceName != null;
            Query query = null;
            if (binaryName != null) {
                query = binaryNameQuery(binaryName);
            }
            if (sourceName != null) {
                if (query == null) {
                   query = new TermQuery(new Term (FIELD_SOURCE,sourceName));
                } else {
                    assert query instanceof BooleanQuery : "The DocumentUtil.binaryNameQuery was incompatibly changed!";        //NOI18N
                    final BooleanQuery bq = (BooleanQuery) query;
                    bq.add(new TermQuery(new Term (FIELD_SOURCE,sourceName)), BooleanClause.Occur.MUST);
                }
            }
            assert query != null;
            return query;
        }
    }

    //</editor-fold>
}
