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

package org.apache.jasper.compiler;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagAttributeInfo;
import jakarta.servlet.jsp.tagext.TagData;
import jakarta.servlet.jsp.tagext.TagFileInfo;
import jakarta.servlet.jsp.tagext.TagInfo;
import jakarta.servlet.jsp.tagext.TagLibraryInfo;
import jakarta.servlet.jsp.tagext.TagVariableInfo;
import jakarta.servlet.jsp.tagext.VariableInfo;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.Options;
import org.openide.util.Utilities;


/**
 *
 * @author Petr Jiricka
 */
public class GetParseData {

    private static final Logger LOGGER = Logger.getLogger(GetParseData.class.getName());
    
    private final JspCompilationContext ctxt;

    private final Options options;
    private final CompilerHacks compHacks;
    private int errorReportingMode;

    private org.netbeans.modules.web.jsps.parserapi.Node.Nodes nbNodes;
    private org.netbeans.modules.web.jsps.parserapi.PageInfo nbPageInfo;
    private Throwable parseException;
    
  
    /**
     * Creates a new instance of ExtractPageData
     *
     * @param ctxt
     * @param errorReportingMode
     */
    public GetParseData(JspCompilationContext ctxt, int errorReportingMode) {
        this.ctxt = ctxt;
        this.errorReportingMode = errorReportingMode;
        options = ctxt.getOptions();
        compHacks = new CompilerHacks(ctxt);
    }
    
    public org.netbeans.modules.web.jsps.parserapi.Node.Nodes getNbNodes() {
        return nbNodes;
    }

    public org.netbeans.modules.web.jsps.parserapi.PageInfo getNbPageInfo() {
        return nbPageInfo;
    }
    
    public Throwable getParseException() {
        return parseException;
    }
    
    public void setParseException(Throwable t) {
        this.parseException = t;
    }

    /** Code in this method copied over and adapted from Compiler.generateJava() 
     **/
    public void parse() {
        Node.Nodes pageNodes = null;
        PageInfo pageInfo = null;
        String xmlView = null;
        try {
            //String smapStr = null;

            //        long t1=System.currentTimeMillis();

            // Setup page info area
            Compiler comp = compHacks.getCompiler();
            pageInfo = comp.getPageInfo();
            ErrorDispatcher errDispatcher = comp.getErrorDispatcher();


            // pageInfo = new PageInfo(new BeanRepository(ctxt.getClassLoader(),
            //   errDispatcher));

            JspConfig jspConfig = options.getJspConfig();
            JspProperty jspProperty = jspConfig.findJspProperty(ctxt.getJspFile());

            /*
             * If the current uri is matched by a pattern specified in
             * a jsp-property-group in web.xml, initialize pageInfo with
             * those properties.
             */
            pageInfo.setELIgnored(JspUtil.booleanValue(jspProperty.isELIgnored()));
            pageInfo.setScriptingInvalid(JspUtil.booleanValue(jspProperty.isScriptingInvalid()));
            if (jspProperty.getIncludePrelude() != null) {
                pageInfo.setIncludePrelude(jspProperty.getIncludePrelude());
            }
            if (jspProperty.getIncludeCoda() != null) {
                pageInfo.setIncludeCoda(jspProperty.getIncludeCoda());
            }
            //        String javaFileName = ctxt.getServletJavaFileName();

            // Setup the ServletWriter
            //        String javaEncoding = ctxt.getOptions().getJavaEncoding();
            //	OutputStreamWriter osw = null; 
            //	try {
            //	    osw = new OutputStreamWriter(new FileOutputStream(javaFileName),
            //					 javaEncoding);
            //	} catch (UnsupportedEncodingException ex) {
            //            errDispatcher.jspError("jsp.error.needAlternateJavaEncoding", javaEncoding);
            //	}

            //	ServletWriter writer = new ServletWriter(new PrintWriter(osw));
            //        ctxt.setWriter(writer);

            // Reset the temporary variable counter for the generator.
            JspUtil.resetTemporaryVariableName();

            // Parse the file
            ParserController parserCtl = new ParserController(ctxt, comp);
            pageNodes = parserCtl.parse(ctxt.getJspFile());

            //	if (ctxt.isPrototypeMode()) {
            //	    // generate prototype .java file for the tag file
            //	    Generator.generate(writer, this, pageNodes);
            //            writer.close();
            //	    return null;
            //	}

            // Generate FunctionMapper (used for validation of EL expressions and
            // code generation)
            // pageInfo.setFunctionMapper(new FunctionMapperImpl(this));

            // Validate and process attributes
            // Validator.validate(comp, pageNodes);
            xmlView = NbValidator.validate(comp, pageNodes);

            //        long t2=System.currentTimeMillis();
            // Dump out the page (for debugging)
            // Dumper.dump(pageNodes);

            // Collect page info
            Collector.collect(comp, pageNodes);

            // Compile (if necessary) and load the tag files referenced in
            // this compilation unit.

            // PENDING - we may need to process tag files somehow
            //	TagFileProcessor tfp = new TagFileProcessor();
            //	tfp.loadTagFiles(comp, pageNodes);          
            
            // Try to obtain tagInfo if the page is a tag file
            if (ctxt.isTagFile()) {
                // find out the dir for fake tag library
                String tagDir = ctxt.getJspFile();
                int lastIndex = tagDir.lastIndexOf('/');
                int lastDotIndex = tagDir.lastIndexOf('.');
                String tagName;
                String tagExt = "";

                if (lastDotIndex > 0) {
                    tagName = tagDir.substring(lastIndex + 1, lastDotIndex);   
                    tagExt = tagDir.substring(lastDotIndex + 1);
                }
                else {
                    tagName = tagDir.substring(lastIndex + 1);
                } 
                tagDir = tagDir.substring(0, tagDir.lastIndexOf('/') + 1);
                
                // we need to compile the tag file to a virtual tag library to obtain its tag info
                ImplicitTagLibraryInfo tagLibrary = new ImplicitTagLibraryInfo(ctxt, parserCtl, "virtual", tagDir, errDispatcher);  //NOI18N
                if (tagLibrary.getTagFile(tagName) == null) {
                    // ImplicitTagLibraryInfo is not designed to work for another than .tag, .tagx files. (issue #195745)
                    // In that case there is an artifical creation of TagInfo which is set to JspCompilationContext.
                    String className = "org.apache.jsp.tag.web." + tagName + "_" + tagExt; //NOI18N
                    TagAttributeInfo tai = new TagAttributeInfo("message", false, "java.lang.String", true); //NOI18N
                    TagInfo ti = new TagInfo(
                            tagName, className, "scriptless", //NOI18N
                            "", tagLibrary, null, new TagAttributeInfo[]{tai}); //NOI18N
                    LOGGER.log(Level.FINE, "Manually created TagInfo: name={0}, tagClassName={1}", new Object[]{tagName, className});
                    ctxt.setTagInfo(ti);
                } else {
                    ctxt.setTagInfo(tagLibrary.getTagFile(tagName).getTagInfo());
                }
            }
            //        long t3=System.currentTimeMillis();

            // Determine which custom tag needs to declare which scripting vars
            ScriptingVariabler.set(pageNodes, errDispatcher);

            // Optimizations by Tag Plugins
            TagPluginManager tagPluginManager = options.getTagPluginManager();
            tagPluginManager.apply(pageNodes, errDispatcher, pageInfo);

            // Generate static funciton mapper codes.
            ELFunctionMapper.map(comp, pageNodes);

            // generate servlet .java file
            //	Generator.generate(writer, comp, pageNodes);
            //        writer.close();
            // The writer is only used during the compile, dereference
            // it in the JspCompilationContext when done to allow it
            // to be GC'd and save memory.
            //        ctxt.setWriter(null);

            //        long t4=System.currentTimeMillis();
            //        if( t4-t1 > 500 ) {
            //            log.debug("Generated "+ javaFileName + " total=" +
            //                      (t4-t1) + " generate=" + ( t4-t3 ) + " validate=" + ( t2-t1 ));
            //        }

                    //JSR45 Support - note this needs to be checked by a JSR45 guru
            //        if (! options.isSmapSuppressed()) {
            //            String smapStr = SmapUtil.generateSmap(ctxt, pageNodes);
            //        }

            // If any proto type .java and .class files was generated,
            // the prototype .java may have been replaced by the current
            // compilation (if the tag file is self referencing), but the
            // .class file need to be removed, to make sure that javac would
            // generate .class again from the new .java file just generated.

            // PENDING - we may need to process tag files somehow
            //	tfp.removeProtoTypeFiles(ctxt.getClassFileName());
        } catch (IOException | JasperException | RuntimeException t) {
            parseException = t;
        } finally {
            // convert the nodes
            try {
                if (pageNodes != null) {
                    nbNodes = convertNodes(pageNodes);
                }
            } catch (JasperException e) {
                if (parseException == null) {
                    parseException = e;
                }
            }
            // convert the pageInfo
            try {
                if (pageInfo != null) {
                    // xmlView may be null
                    nbPageInfo = convertPageInfo(pageInfo, xmlView, ctxt);
                }
            } catch (JspException e) {
                if (parseException == null) {
                    parseException = e;
                }
            }
        }
//	return smapStr;
    }
    
    private static org.netbeans.modules.web.jsps.parserapi.Node.Nodes convertNodes(Node.Nodes nodes) throws JasperException {
        org.netbeans.modules.web.jsps.parserapi.Node.Nodes nbNodes =
	    NodeConverterVisitor.convertNodes(nodes);
        return nbNodes;
    }    
    
    private static org.netbeans.modules.web.jsps.parserapi.PageInfo convertPageInfo(PageInfo pageInfo, String xmlView, JspCompilationContext ctxt) throws JspException {
        IdentityHashMap<Object,Object> convertedObjects = new IdentityHashMap<>();
        PageInfoImpl nbPageInfo = 
            new PageInfoImpl(
                getTaglibsMapReflect(pageInfo, ctxt).entrySet().stream().collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> convert(convertedObjects, e.getValue())
                )),
                getJSPPrefixMapperReflect(pageInfo), 
                getXMLPrefixMapperReflect(pageInfo), 
                ((CompilerHacks.HackPageInfo)pageInfo).getApproxXmlPrefixMapper(), 
                pageInfo.getImports(),
                pageInfo.getDependants(),
                pageInfo.getIncludePrelude(),
                pageInfo.getIncludeCoda(),
                getPluginDclsReflect(pageInfo),
                getPrefixesReflect(pageInfo)
            );
        nbPageInfo.setLanguage(            pageInfo.getLanguage());
        nbPageInfo.setExtends(             pageInfo.getExtends());
        nbPageInfo.setContentType(         pageInfo.getContentType());
        nbPageInfo.setSession(             pageInfo.getSession());
        nbPageInfo.setBufferValue(         pageInfo.getBufferValue());
        nbPageInfo.setAutoFlush(           pageInfo.getAutoFlush());
        nbPageInfo.setIsThreadSafe(        pageInfo.getIsThreadSafe());
        nbPageInfo.setIsErrorPage(         pageInfo.getIsErrorPage());
        nbPageInfo.setErrorPage(           pageInfo.getErrorPage());
        nbPageInfo.setScriptless(          pageInfo.isScriptless());
        nbPageInfo.setScriptingInvalid(    pageInfo.isScriptingInvalid());
        nbPageInfo.setELIgnored(           pageInfo.isELIgnored());
        nbPageInfo.setOmitXmlDecl(         pageInfo.getOmitXmlDecl());
        nbPageInfo.setIsJspPrefixHijacked( pageInfo.isJspPrefixHijacked());
        nbPageInfo.setDoctypeName(         pageInfo.getDoctypeName());
        nbPageInfo.setDoctypeSystem(       pageInfo.getDoctypeSystem());
        nbPageInfo.setDoctypePublic(       pageInfo.getDoctypePublic());
        nbPageInfo.setHasJspRoot(          pageInfo.hasJspRoot());
        nbPageInfo.setBeans(createBeanData(pageInfo.getBeanRepository()));
        // the xml view
        nbPageInfo.setXMLView(xmlView);
      
        nbPageInfo.setTagFile(ctxt.isTagFile());
        nbPageInfo.setTagInfo(convert(convertedObjects, ctxt.getTagInfo()));

        return nbPageInfo;
    }

    private static org.netbeans.modules.web.jsps.parserapi.TagInfo convert(IdentityHashMap<Object,Object> convertedObjects, TagInfo source) {
        if(source == null) {
            return null;
        }

        if(convertedObjects.containsKey(source)) {
            return (org.netbeans.modules.web.jsps.parserapi.TagInfo) convertedObjects.get(source);
        }

        org.netbeans.modules.web.jsps.parserapi.TagInfo result = new org.netbeans.modules.web.jsps.parserapi.TagInfo();
        convertedObjects.put(source, result);

        TagData td = new TagData(new Object[][]{});
        result.setDisplayName(source.getDisplayName());
        result.setTagClassName(source.getTagClassName());
        result.setTagName(source.getTagName());
        result.setBodyContent(source.getBodyContent());
        result.setInfoString(source.getInfoString());
        result.setTagLibrary(convert(convertedObjects, source.getTagLibrary()));
        result.getAttributes().addAll(stream(source.getAttributes()).map(s -> convert(convertedObjects, s)).toList());
        result.getVariables().addAll(stream(source.getTagVariableInfos()).map(s -> convert(convertedObjects, s)).toList());
        result.getRuntimeVariables().addAll(stream(source.getVariableInfo(td)).map(s -> convert(convertedObjects, s)).toList());

        return result;
    }

    private static org.netbeans.modules.web.jsps.parserapi.TagLibraryInfo convert(IdentityHashMap<Object,Object> convertedObjects, TagLibraryInfo source) {
        if(source == null) {
            return null;
        }

        if(convertedObjects.containsKey(source)) {
            return (org.netbeans.modules.web.jsps.parserapi.TagLibraryInfo) convertedObjects.get(source);
        }

        org.netbeans.modules.web.jsps.parserapi.TagLibraryInfo result = new org.netbeans.modules.web.jsps.parserapi.TagLibraryInfo();
        convertedObjects.put(source, result);

        result.setShortName(source.getShortName());
        result.setReliableURN(source.getReliableURN());
        result.setInfoString(source.getInfoString());
        result.setURI(source.getURI());
        result.setPrefixString(source.getPrefixString());
        result.setRequiredVersion(source.getRequiredVersion());
        result.setTlibversion(getFieldByReflection("tlibversion", source));
        result.getTags().addAll(stream(source.getTags()).map(s -> convert(convertedObjects, s)).toList());
        result.getTagFiles().addAll(stream(source.getTagFiles()).map(s -> convert(convertedObjects, s)).toList());

        return result;
    }

    private static org.netbeans.modules.web.jsps.parserapi.TagAttributeInfo convert(IdentityHashMap<Object,Object> convertedObjects, TagAttributeInfo source) {
        if(source == null) {
            return null;
        }

        if (convertedObjects.containsKey(source)) {
            return (org.netbeans.modules.web.jsps.parserapi.TagAttributeInfo) convertedObjects.get(source);
        }

        org.netbeans.modules.web.jsps.parserapi.TagAttributeInfo result = new org.netbeans.modules.web.jsps.parserapi.TagAttributeInfo();
        convertedObjects.put(source, result);

        result.setName(source.getName());
        result.setRequired(source.isRequired());
        result.setTypeName(source.getTypeName());
        result.setCanBeRequestTime(source.canBeRequestTime());
        result.setFragment(source.isFragment());

        return result;
    }

    private static org.netbeans.modules.web.jsps.parserapi.TagVariableInfo convert(IdentityHashMap<Object,Object> convertedObjects, TagVariableInfo source) {
        if(source == null) {
            return null;
        }

        if (convertedObjects.containsKey(source)) {
            return (org.netbeans.modules.web.jsps.parserapi.TagVariableInfo) convertedObjects.get(source);
        }

        org.netbeans.modules.web.jsps.parserapi.TagVariableInfo result = new org.netbeans.modules.web.jsps.parserapi.TagVariableInfo();
        convertedObjects.put(source, result);

        result.setNameGiven(source.getNameGiven());
        result.setNameFromAttribute(source.getNameFromAttribute());
        result.setClassName(source.getClassName());
        result.setDeclare(source.getDeclare());
        result.setScope(source.getScope());

        return result;
    }

    private static org.netbeans.modules.web.jsps.parserapi.VariableInfo convert(IdentityHashMap<Object,Object> convertedObjects, VariableInfo source) {
        if(source == null) {
            return null;
        }

        if (convertedObjects.containsKey(source)) {
            return (org.netbeans.modules.web.jsps.parserapi.VariableInfo) convertedObjects.get(source);
        }

        org.netbeans.modules.web.jsps.parserapi.VariableInfo result = new org.netbeans.modules.web.jsps.parserapi.VariableInfo();
        convertedObjects.put(source, result);

        result.setVarName(source.getVarName());
        result.setClassName(source.getClassName());
        result.setDeclare(source.getDeclare());

        return result;
    }

    private static org.netbeans.modules.web.jsps.parserapi.TagFileInfo convert(IdentityHashMap<Object, Object> convertedObjects, TagFileInfo source) {
        if(source == null) {
            return null;
        }

        if (convertedObjects.containsKey(source)) {
            return (org.netbeans.modules.web.jsps.parserapi.TagFileInfo) convertedObjects.get(source);
        }

        org.netbeans.modules.web.jsps.parserapi.TagFileInfo result = new org.netbeans.modules.web.jsps.parserapi.TagFileInfo();
        convertedObjects.put(source, result);

        result.setName(source.getName());
        result.setPath(source.getPath());
        result.setTagInfo(convert(convertedObjects, source.getTagInfo()));

        return result;
    }

    private static <T> Stream<T> stream(T[] t) {
        if(t == null) {
            return Stream.empty();
        } else {
            return Arrays.stream(t);
        }
    }

    private static String getFieldByReflection(String fieldName, TagLibraryInfo info) {
        try {
            java.lang.reflect.Field f = TagLibraryInfo.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            return (String) f.get(info);
        }
        catch (NoSuchFieldException | IllegalAccessException | ClassCastException e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }
        return null;
    }

    private static org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData[] createBeanData(BeanRepository rep) {
        try {
            initBeanRepository();
            Map beanTypes = (HashMap) beanTypesF.get(rep);
            int size = beanTypes.size();
            org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData[] bd =
                new org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData[size];
            Iterator it = beanTypes.keySet().iterator();
            int index = 0;
            while (it.hasNext()) {
                String id = (String) it.next();
                String type = (String) beanTypes.get(id);

                bd[index] = new BeanDataImpl(id, type);
                ++index;
            }
            return bd;
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.INFO, null, e);
            throw new RuntimeException();
        }
    }
    
    private static class PageInfoImpl extends org.netbeans.modules.web.jsps.parserapi.PageInfo {
        
        public PageInfoImpl(/*BeanRepository beanRepository*/
                Map<String, org.netbeans.modules.web.jsps.parserapi.TagLibraryInfo> taglibsMap,
                Map<String, String> jspPrefixMapper,
                Map<String, LinkedList<String>> xmlPrefixMapper,
                Map<String, String> approxXmlPrefixMapper,
                List<String> imports,
                List<String> dependants,
                List<String> includePrelude,
                List<String> includeCoda,
                List<String> pluginDcls,
                Set<String> prefixes
            ) {
            super(taglibsMap, jspPrefixMapper, xmlPrefixMapper, approxXmlPrefixMapper, imports, dependants, includePrelude,
                includeCoda, pluginDcls, prefixes);
        }
        
        private String xmlView;
        
        public void setXMLView(String xmlView) {
            this.xmlView = xmlView;
        }
        
        public String getXMLView() {
            return xmlView;
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(super.toString());
            sb.append(" ------- XML View (constructed from the original data structure) -----\n"); // NOI18N
            if (xmlView == null) {
                sb.append("no XML view\n"); // NOI18N
            } else {
                sb.append(getXMLView());
            }
            return sb.toString();
        }
    }
    
    private static class BeanDataImpl implements org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData {

        private final String id;
        private final String className;

        BeanDataImpl(String id, String className) {
            this.id = id;
            this.className = className;
        }

        /** Identifier of the bean in the page (variable name). */
        @Override
        public String getId() {
            return id;
        }

        /** Returns the class name for this bean. */
        @Override
        public String getClassName() {
            return className;
        }
    }
                
    // ------ getting BeanRepository data by reflection
    private static Field beanTypesF;
    
    private static void initBeanRepository() {
        if (beanTypesF == null) {
            try {
                beanTypesF = BeanRepository.class.getDeclaredField("beanTypes");
                beanTypesF.setAccessible(true);
            } catch (NoSuchFieldException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
    }

    
    // ------ getting BeanRepository data by reflection
    private static Field pluginDclsF, prefixesF, taglibsMapF, jspPrefixMapperF, xmlPrefixMapperF;
    
    private static void initPageInfoFields() {
        if (pluginDclsF == null) {
            try {
                pluginDclsF = PageInfo.class.getDeclaredField("pluginDcls");
                pluginDclsF.setAccessible(true);
                prefixesF = PageInfo.class.getDeclaredField("prefixes");
                prefixesF.setAccessible(true);
                taglibsMapF = PageInfo.class.getDeclaredField("taglibsMap");
                taglibsMapF.setAccessible(true);
                jspPrefixMapperF = PageInfo.class.getDeclaredField("jspPrefixMapper");
                jspPrefixMapperF.setAccessible(true);
                xmlPrefixMapperF = PageInfo.class.getDeclaredField("xmlPrefixMapper");
                xmlPrefixMapperF.setAccessible(true);
            } catch (NoSuchFieldException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static List<String> getPluginDclsReflect(PageInfo pageInfo) {
        initPageInfoFields();
        try {
            return (List<String>)pluginDclsF.get(pageInfo);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.INFO, null, e);
            throw new RuntimeException();
        }
    }
    
    @SuppressWarnings("unchecked")
    private static HashSet<String> getPrefixesReflect(PageInfo pageInfo) {
        initPageInfoFields();
        try {
            return (HashSet)prefixesF.get(pageInfo);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.INFO, null, e);
            throw new RuntimeException();
        }
    }

    private static class TagFileInfoCacheRecord {
        final long time;
        final TagFileInfo tagFileInfo;
        
        public TagFileInfoCacheRecord(long time, TagFileInfo info){
            tagFileInfo = info;
            this.time = time;
        }
    }
    
    /** The cache for the tag infos from tagfiles. There are stored TagInfoCacheRecord as value and 
     * the path for a tagfile as key. The informations are cached for the whole application. 
     * The cache is changed, when a jsp page is parsed and a tag file was changed. 
     */
    
    private static final Map<URL, TagFileInfoCacheRecord> tagFileInfoCache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private static Map<String, TagLibraryInfo> getTaglibsMapReflect(PageInfo pageInfo, JspCompilationContext ctxt) {
        initPageInfoFields();
        try {
            Map taglibs = (Map)taglibsMapF.get(pageInfo);
            Iterator iter = taglibs.values().iterator();
            TagLibraryInfo libInfo;
            // Caching information about tag files from implicit libraries
            while (iter.hasNext()){
                libInfo = (TagLibraryInfo)iter.next();
                try {
                    if (libInfo instanceof ImplicitTagLibraryInfo){
                        //We need the access for the files
                        Field tagFileMapF = ImplicitTagLibraryInfo.class.getDeclaredField("tagFileMap");
                        tagFileMapF.setAccessible(true);
                        Map tagFileMap = (HashMap)tagFileMapF.get(libInfo);
                        TagFileInfo[] tagFiles = new TagFileInfo[tagFileMap.size()];
                        int index = 0;
                        //Check every file in tag library
                        Iterator iterator = tagFileMap.keySet().iterator();
                        while (iterator.hasNext()){
                            //Find the path for the file
                            String name = (String) iterator.next();
                            String filePath = (String)tagFileMap.get(name);
                            
                            URL path =  ctxt.getResource(filePath);
                            File file = Utilities.toFile(new URI( path.toExternalForm() ));
                            // Is there the file in the cache?
                            if (tagFileInfoCache.containsKey(path)){
                                TagFileInfoCacheRecord r = (TagFileInfoCacheRecord)tagFileInfoCache.get(path);
                                // Is there a change in the tagfile?
                                if (r.time < file.lastModified()){
                                    tagFileInfoCache.put(path, new TagFileInfoCacheRecord (file.lastModified(), libInfo.getTagFile(name)));
                                }
                            }
                            else {
                                tagFileInfoCache.put(path, new TagFileInfoCacheRecord (file.lastModified(), libInfo.getTagFile(name)));
                            }
                            //Obtain information from the cache
                            tagFiles[index] = tagFileInfoCache.get(path).tagFileInfo;
                            index++;
                        }
                        Field tagInfosF = ImplicitTagLibraryInfo.class.getSuperclass().getDeclaredField("tagFiles");
                        tagInfosF.setAccessible(true);    
                        tagInfosF.set(libInfo, tagFiles);
                    }
                } catch (NoSuchFieldException | MalformedURLException | URISyntaxException e) {
                    LOGGER.log(Level.INFO, null, e);
                }
            }
            return taglibs;
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.INFO, null, e);
            throw new RuntimeException();
        }
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, String> getJSPPrefixMapperReflect(PageInfo pageInfo) {
        initPageInfoFields();
        try {
            return (Map<String,String>) jspPrefixMapperF.get(pageInfo);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.INFO, null, e);
            throw new RuntimeException();
        }
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, LinkedList<String>> getXMLPrefixMapperReflect(PageInfo pageInfo) {
        initPageInfoFields();
        try {
            return (Map<String, LinkedList<String>>) xmlPrefixMapperF.get(pageInfo);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.INFO, null, e);
            throw new RuntimeException();
        }
    }
}
