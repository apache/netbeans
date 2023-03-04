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

package org.netbeans.modules.schema2beansdev;

import java.util.*;
import java.io.*;
import java.math.BigDecimal;

import org.netbeans.modules.schema2beansdev.gen.JavaUtil;
import org.netbeans.modules.schema2beansdev.gen.WriteIfDifferentOutputStream;
import org.netbeans.modules.schema2beans.Common;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.netbeans.modules.schema2beans.Schema2BeansNestedException;
import org.netbeans.modules.schema2beans.DDLogFlags;
import org.netbeans.modules.schema2beans.Version;
import org.netbeans.modules.schema2beansdev.metadd.MetaDD;

//******************************************************************************
// BEGIN_NOI18N
//******************************************************************************

/**
 * This class provides the generation entry point.
 */
public class GenBeans {
    public static final String COMMON_BEAN = "CommonBean";
    /**
     * The OutputStreamProvider interface is a way of abstracting
     * how we get an OutputStream given a filename.  If GenBeans is being
     * run as part of the IDE, OutputStream's come from different places
     * than just opening up a regular File.  This is intended for the
     * writing of the generated files.
     * The caller of getStream will eventually run .close on the OutputStream
     */
    public static interface OutputStreamProvider {
        public OutputStream getStream(String dir, String name,
                                      String extension)
            throws java.io.IOException;

        /**
         * Returns true if the file in question is more older than the
         * given time.  @see java.io.File.lastModified
         */
        public boolean isOlderThan(String dir, String name, String extension,
                                   long time) throws java.io.IOException;
    }
    
    public static class DefaultOutputStreamProvider implements OutputStreamProvider {
        private Config config;
        private List generatedFiles;
        
        public DefaultOutputStreamProvider(Config config) {
            this.config = config;
            this.generatedFiles = new LinkedList();
        }

        private String getFilename(String dir, String name, String extension) {
            return dir + "/" + name + "." + extension;	// NOI18N
        }
        
        public OutputStream getStream(String dir, String name,
                                      String extension)
            throws java.io.IOException {
            String filename = getFilename(dir, name, extension);
            if (!config.isQuiet())
                config.messageOut.println(Common.getMessage("MSG_GeneratingClass", filename));	// NOI18N
            generatedFiles.add(filename);
            //return new FileOutputStream(filename);
            return new WriteIfDifferentOutputStream(filename);
        }

        public boolean isOlderThan(String dir, String name, String extension,
                                   long time) throws java.io.IOException {
            String filename = getFilename(dir, name, extension);
            File f = new File(filename);
            //System.out.println("filename="+filename+" lm="+f.lastModified());
            return f.lastModified() < time;
        }

        public List getGeneratedFiles() {
            return generatedFiles;
        }
    }

    public static class Config extends S2bConfig {
        // What kind of schema is it coming in
        public static final int XML_SCHEMA = 0;
        public static final int DTD = 1;
        /*
        int schemaType;

        private boolean		traceParse = false;
        private boolean		traceGen = false;
        private boolean		traceMisc = false;
        private boolean		traceDot = false;
	
        // filename is the name of the schema (eg, DTD) input file
        String 		filename = null;
        // fileIn is an InputStream version of filename (the source schema).
        // If fileIn is set, then filename is ignored.
        InputStream 	fileIn;
        String 		docroot;
        String 		rootDir = ".";
        String 		packagePath;
        String		indent = "\t";
        String 		mddFile;
        // If mddIn is set, then the mdd file is read from there and
        // we don't write our own.
        InputStream 	mddIn;
        private MetaDD mdd;
        boolean doGeneration = true;
        boolean		scalarException;
        boolean		dumpToString;
        boolean		vetoable;	// enable veto events
        boolean		standalone;
        // auto is set when it is assumed that there is no user sitting
        // in front of System.in
        boolean     	auto;
        // outputStreamProvider, be default, is null, which means that
        // we use standard java.io.* calls to get the OutputStream
        OutputStreamProvider outputStreamProvider;
        boolean     throwErrors;

        // Whether or not to generate classes that do XML I/O.
        boolean generateXMLIO;

        // Whether or not to generate code to do validation
        boolean generateValidate;

        // Whether or not to generate property events
        boolean generatePropertyEvents;

        // Whether or not to generate code to be able to store events
        boolean generateStoreEvents;

        boolean generateTransactions;

        boolean attributesAsProperties;

        //
        boolean generateDelegator = false;

        String generateCommonInterface = null;

        boolean defaultsAccessable = false;

        private boolean useInterfaces = false;

        // Generate an interface for the bean info accessors
        private boolean generateInterfaces = false;

        private boolean keepElementPositions = false;

        private boolean removeUnreferencedNodes = false;

        // If we're passed in a simple InputStream, then the inputURI will
        // help us find relative URI's if anything gets included for imported.
        private String inputURI;

        // This is the name of the class to use for indexed properties.
        // It must implement java.util.List
        // Use null to mean use arrays.
        private String indexedPropertyType;

        private boolean doCompile = false;

        private String dumpBeanTree = null;

        private String generateDotGraph = null;

        private boolean processComments = false;

        private boolean processDocType = false;

        private boolean checkUpToDate = false;

        private boolean generateParentRefs = false;

        private boolean generateHasChanged = false;

        // Of all our source files, newestSourceTime represents the most
        // recently modified one.
        private long newestSourceTime = 0;

        private String writeBeanGraphFilename;

        private List readBeanGraphFilenames = new ArrayList();
        private List readBeanGraphs = new ArrayList();

        private boolean minFeatures = false;

        private boolean forME = false;

        private boolean generateTagsFile = false;
        private CodeGeneratorFactory codeGeneratorFactory;
        */
        // What should we generate
        public static final int OUTPUT_TRADITIONAL_BASEBEAN = 0;
        public static final int OUTPUT_JAVABEANS = 1;
	
        PrintStream 	messageOut;
        int jdkTarget = 131;   // JDK version that we're targeting times 100

        public Config() {
            setOutputStreamProvider(new DefaultOutputStreamProvider(this));
            setSchemaTypeNum(DTD);
            setMessageOut(System.out);
            // Make the default type be boolean
            setDefaultElementType("{http://www.w3.org/2001/XMLSchema}boolean");
            /*
            attributesAsProperties = false;
            indexedPropertyType = "java.util.ArrayList";
            inputURI = null;
            mddFile = null;
            scalarException = true;
            dumpToString = false;
            vetoable = false;
            standalone = false;
            auto = false;
            mddIn = null;*/
        }

        public Config(OutputStreamProvider out) {
            setOutputStreamProvider(out);
            setSchemaTypeNum(DTD);
            setMessageOut(System.out);
        }

        public void readConfigs() throws java.io.IOException {
            long newestSourceTime = getNewestSourceTime();
            File[] readConfigs = getReadConfig();
            for (int i = 0; i < readConfigs.length; ++i) {
                try {
                    File configFile = readConfigs[i];
                    org.xml.sax.InputSource in = new org.xml.sax.InputSource(new FileInputStream(configFile));
                    javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
                    javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
                    org.w3c.dom.Document doc = db.parse(in);
                    readNode(doc.getDocumentElement());
                } catch (javax.xml.parsers.ParserConfigurationException e) {
                    throw new RuntimeException(e);
                } catch (org.xml.sax.SAXException e) {
                    throw new RuntimeException(e);
                }
            }
            setNewestSourceTime(newestSourceTime);
        }

        protected int unknownArgument(String[] args, String arg, int argNum) {
            if ("-t".equals(arg)) {
                arg = args[++argNum];
                if (arg.equalsIgnoreCase("parse"))
                    setTraceParse(true);
                else if (arg.equalsIgnoreCase("gen"))
                    setTraceGen(true);
                else if (arg.equalsIgnoreCase("dot"))
                    setTraceDot(true);
                else {
                    if (!arg.equalsIgnoreCase("all"))
                        --argNum;  // unknown value, trace all, and rethink that option
                    setTraceParse(true);
                    setTraceGen(true);
                    setTraceMisc(true);
                    setTraceDot(true);
                }
            } else if ("-version".equals(arg)) {
                messageOut.println("schema2beans - " + Version.getVersion());
                System.exit(0);
            } else if ("-xmlschema".equals(arg))
                setSchemaTypeNum(XML_SCHEMA);
            else if ("-dtd".equals(arg))
                setSchemaTypeNum(DTD);
            else if ("-premium".equals(arg))
                buyPremium();
            else if ("-strict".equals(arg))
                useStrict();
            else if ("-basebean".equals(arg)) {
                setOutputType(OUTPUT_TRADITIONAL_BASEBEAN);
            } else if ("-javabeans".equals(arg))
                setOutputType(OUTPUT_JAVABEANS);
            else if ("-commoninterface".equals(arg))
                setGenerateCommonInterface(COMMON_BEAN);
            else if ("-nocommoninterface".equals(arg))
                setGenerateCommonInterface(null);
            else {
                messageOut.println("Unknown argument: "+arg);
                messageOut.println("Use -help.");
                System.exit(1);
            }
            return argNum;
        }

        public void showHelp(java.io.PrintStream out) {
            super.showHelp(out);
            out.println(" -version\tPrint version info");
            out.println(" -xmlschema\tXML Schema input mode");
            out.println(" -dtd\tDTD input mode (default)");
            out.println(" -javaBeans\tGenerate pure JavaBeans that do not need any runtime library support (no BaseBean).");
            out.println(" -baseBean\tForce use of BaseBean.  Runtime required.");
            out.println(" -commonInterface\tGenerate a common interface between all beans.");
            out.println(" -premium The \"Premium\" Package.  Turn on what ought to be the default switches (but can't be the default due to backwards compatibility).");
            out.println(" -strict The \"Strict\" Package.  For those who are more concerned with correctness than backwards compatibility.  Turn on what ought to be the default switches (but can't be the default due to backwards compatibility).  Very similar to -premium.");
            out.println(" -no*\tAny switch that does not take an argument has a -no variant that will turn it off.");
            out.println("\nThe bean classes are generated in the directory rootDir/packagePath, where packagePath is built using the package name specified. If the package name is not specified, the doc root element value is used as the default package name.  Use the empty string to get no (default) package.");
            out.println("\nexamples: java GenBeans -f ejb.dtd");
            out.println("          java GenBeans -f webapp.dtd -d webapp -p myproject.webapp -r /myPath/src");
            out.println("          java GenBeans -f webapp.xsd -xmlschema -r /myPath/src -premium");
            out.println("\nMost of the parameters are optional. Only the file name is mandatory.");
            out.println("With only the file name specified, the generator uses the current directory, and uses the schema docroot value as the package name.");
        }
        
        public void setMessageOut(PrintStream messageOut) {
            this.messageOut = messageOut;
            super.setMessageOut(messageOut);
        }
	
        public void setOutputType(int type) {
            if (type == OUTPUT_JAVABEANS) {
                setCodeGeneratorFactory(JavaBeansFactory.newInstance());
                setAttributesAsProperties(true);
            } else if (type == OUTPUT_TRADITIONAL_BASEBEAN) {
                if (false) {
                    // Force extendBaseBean option
                    setOutputType(OUTPUT_JAVABEANS);
                    setExtendBaseBean(true);
                } else {
                    setCodeGeneratorFactory(null);
                    setProcessComments(false);  // we already do it anyway
                }
            } else {
                throw new IllegalArgumentException("type != OUTPUT_JAVABEANS && type != OUTPUT_TRADITIONAL_BASEBEAN");	// NOI18N
            }
        }

        public CodeGeneratorFactory getCodeGeneratorFactory() {
            if (super.getCodeGeneratorFactory() == null)
                setCodeGeneratorFactory(BaseBeansFactory.newInstance());
            return super.getCodeGeneratorFactory();
        }

        public void setTarget(String value) {
            BigDecimal num = new BigDecimal(value);
            num = num.movePointRight(2);
            jdkTarget = num.intValue();
        }

        public void setPackagePath(String pkg) {
            if (pkg.equals("."))
                pkg = "";
            else
                pkg = pkg.replace('.', '/');
            super.setPackagePath(pkg);
        }

        public boolean isTrace() {
            return isTraceParse() || isTraceGen() || isTraceMisc();
        }

        public void setTraceGen(boolean value) {
            super.setTraceGen(value);
            DDLogFlags.debug = value;
        }

        void setIfNewerSourceTime(long t) {
            //System.out.println("setIfNewerSourceTime: newestSourceTime="+newestSourceTime+" t="+t);
            if (t > getNewestSourceTime())
                setNewestSourceTime(t);
        }
        /*
        public void setOutputStreamProvider(OutputStreamProvider outputStreamProvider) {
            this.outputStreamProvider = outputStreamProvider;
        }

        public void setWriteBeanGraph(String filename) {
            writeBeanGraphFilename = filename;
        }

        public String getWriteBeanGraph() {
            return writeBeanGraphFilename;
        }

        public void addReadBeanGraphFilename(String filename) {
            readBeanGraphFilenames.add(filename);
        }

        public Iterator readBeanGraphFilenames() {
            return readBeanGraphFilenames.iterator();
            }

        public void addReadBeanGraph(org.netbeans.modules.schema2beansdev.beangraph.BeanGraph bg) {
            readBeanGraphs.add(bg);
        }
        */

        public Iterator readBeanGraphs() {
            return fetchReadBeanGraphsList().iterator();
        }

        public Iterator readBeanGraphFiles() {
            return fetchReadBeanGraphFilesList().iterator();
        }

        public int getSchemaTypeNum() {
            if ("xmlschema".equalsIgnoreCase(getSchemaType()))
                return XML_SCHEMA;
            if ("dtd".equalsIgnoreCase(getSchemaType()))
                return DTD;
            throw new IllegalStateException(Common.getMessage("MSG_IllegalSchemaName", getSchemaType()));
        }

        public void setSchemaType(int type) {
            setSchemaTypeNum(type);
        }
        
        public void setSchemaTypeNum(int type) {
            if (type == XML_SCHEMA)
                setSchemaType("xmlschema");
            else if (type == DTD)
                setSchemaType("dtd");
            else
                throw new IllegalStateException("illegal schema type: "+type);
        }

        /**
         * @deprecated  use setMddIn instead
         */
        @Deprecated
        public void setMDDIn(java.io.InputStream value) {
            setMddIn(value);
        }

        /**
         * @deprecated  use setFileIn instead
         */
        @Deprecated
        public void setDTDIn(java.io.InputStream value) {
            setFileIn(value);
        }

        public void setRootDir(String dir) {
            setRootDir(new File(dir));
        }

        public void setIndexedPropertyType(String className) {
            if ("".equals(className))
                className = null;  // use arrays
            super.setIndexedPropertyType(className);
        }

        public void setForME(boolean value) {
            super.setForME(value);
            if (value) {
                setOutputType(OUTPUT_JAVABEANS);
                setGeneratePropertyEvents(false);
                setIndexedPropertyType(null);
            }
        }

        /**
         * Turn on a bunch of the great switches that arn't on by default.
         */
        public void buyPremium() {
            setSetDefaults(true);
            setStandalone(true);
            setDumpToString(true);
            setThrowErrors(true);
            setGenerateXMLIO(true);
            setGenerateValidate(true);
            setAttributesAsProperties(true);
            setGenerateInterfaces(true);
            setOptionalScalars(true);
            setRespectExtension(true);
            setLogSuspicious(true);
            setGenerateCommonInterface(COMMON_BEAN);
            setOutputType(OUTPUT_JAVABEANS);
        }

        /**
         * For those who are more concerned with correctness than
         * backwards compatibility.
         */
        public void useStrict() {
            setSetDefaults(true);
            setStandalone(true);
            setDumpToString(true);
            setThrowErrors(true);
            setGenerateXMLIO(true);
            setGenerateValidate(true);
            setAttributesAsProperties(true);
            setRespectExtension(true);
            setOutputType(OUTPUT_JAVABEANS);
        }

        public void setMinFeatures(boolean value) {
            super.setMinFeatures(value);
            if (value) {
                setOutputType(OUTPUT_JAVABEANS);
                setGenerateXMLIO(false);
                setGenerateValidate(false);
                setGenerateInterfaces(false);
                setGenerateCommonInterface(null);
                setGeneratePropertyEvents(false);
                setGenerateStoreEvents(false);
                setGenerateTransactions(false);
                setDefaultsAccessable(false);
                setKeepElementPositions(false);
                setRemoveUnreferencedNodes(true);
                setProcessComments(false);
                setProcessDocType(false);
                setGenerateParentRefs(false);
                setGenerateHasChanged(false);
            }
        }

        public void setExtendBaseBean(boolean value) {
            super.setExtendBaseBean(value);
            if (value) {
                setUseRuntime(true);
                setGenerateParentRefs(true);
                setGenerateValidate(true);
                setGeneratePropertyEvents(true);
                setProcessComments(true);
                setProcessDocType(true);
            }
        }
    }

    //	Entry point - print help and call the parser
    public static void main(String args[]) {
        GenBeans.Config config = new GenBeans.Config();
	
        if (config.parseArguments(args)) {
            config.showHelp(System.out);
            return;
        }
        /*
        if (config.getFilename() == null) {
            config.showHelp(System.out);
            return;
        }
          try {
          for (int i=0; i<args.length; i++) {
          if (args[i].equals("-f")) {
          config.setFilename(new File(args[++i]));
          help++;
          }
          else
          if (args[i].equals("-d")) {
          config.setDocRoot(args[++i]);
          if (config.getDocRoot() == null) {
          help = 0;
          break;
          }
          }
          else
          if (args[i].equals("-veto")) {
          config.setVetoable(true);
          }
          else
          if (args[i].equals("-t")) {
          ++i;
          if (args[i].equalsIgnoreCase("parse"))
          config.setTraceParse(true);
          else if (args[i].equalsIgnoreCase("gen"))
          config.setTraceGen(true);
          else if (args[i].equalsIgnoreCase("dot"))
          config.setTraceDot(true);
          else {
          if (!args[i].equalsIgnoreCase("all"))
          --i;  // unknown value, trace all, and rethink that option
          config.setTraceParse(true);
          config.setTraceGen(true);
          config.setTraceMisc(true);
          config.setTraceDot(true);
          }
          }
          else
          if (args[i].equals("-ts")) {
          config.setDumpToString(true);
          }
          else
          if (args[i].equals("-version")) {
          System.out.println("schema2beans - " + Version.getVersion());
          return;
          }
          else
          if (args[i].equals("-noe")) {
          config.setScalarException(false);
          }
          else
          if (args[i].equals("-p")) {
          config.setPackagePath(args[++i]);
          if (config.getPackagePath() == null) {
          help = 0;
          break;
          }
          }
          else
          if (args[i].equals("-r")) {
          String dir = args[++i];
          if (dir == null) {
          help = 0;
          break;
          }
          config.setRootDir(new File(dir));
          }
          else
          if (args[i].equals("-st")) {
          config.setStandalone(true);
          }
          else
          if (args[i].equals("-sp")) {
          tab = args[++i];
          }
          else
          if (args[i].equals("-mdd")) {
          String f = args[++i];
          if (f == null) {
          help = 0;
          break;
          }
          config.setMddFile(new File(f));
          } else if (args[i].equalsIgnoreCase("-xmlschema")) {
          config.setSchemaTypeNum(Config.XML_SCHEMA);
          } else if (args[i].equalsIgnoreCase("-dtd")) {
          config.setSchemaTypeNum(Config.DTD);
          } else if (args[i].equalsIgnoreCase("-throw")) {
          config.setThrowErrors(true);
          } else if (args[i].equalsIgnoreCase("-basebean")) {
          config.setOutputType(Config.OUTPUT_TRADITIONAL_BASEBEAN);
          } else if (args[i].equalsIgnoreCase("-javabeans")) {
          config.setOutputType(Config.OUTPUT_JAVABEANS);
          } else if (args[i].equalsIgnoreCase("-validate")) {
          config.setGenerateValidate(true);
          } else if (args[i].equalsIgnoreCase("-novalidate")) {
          config.setGenerateValidate(false);
          } else if (args[i].equalsIgnoreCase("-propertyevents")) {
          config.setGeneratePropertyEvents(true);
          } else if (args[i].equalsIgnoreCase("-nopropertyevents")) {
          config.setGeneratePropertyEvents(false);
          } else if (args[i].equalsIgnoreCase("-transactions")) {
          config.setGenerateTransactions(true);
          } else if (args[i].equalsIgnoreCase("-attrprop")) {
          config.setAttributesAsProperties(true);
          } else if (args[i].equalsIgnoreCase("-noattrprop")) {
          config.setAttributesAsProperties(false);
          } else if (args[i].equalsIgnoreCase("-indexedpropertytype")) {
          config.setIndexedPropertyType(args[++i]);
          } else if (args[i].equalsIgnoreCase("-delegator")) {
          config.setGenerateDelegator(true);
          } else if (args[i].equalsIgnoreCase("-premium")) {
          config.buyPremium();
          } else if (args[i].equalsIgnoreCase("-commoninterface")) {
          config.setGenerateCommonInterface(COMMON_BEAN);
          } else if (args[i].equalsIgnoreCase("-nocommoninterface")) {
          config.setGenerateCommonInterface(null);
          } else if (args[i].equalsIgnoreCase("-commoninterfacename")) {
          config.setGenerateCommonInterface(args[++i]);
          } else if (args[i].equalsIgnoreCase("-compile")) {
          config.setDoCompile(true);
          } else if (args[i].equalsIgnoreCase("-auto")) {
          config.setAuto(true);
          } else if (args[i].equalsIgnoreCase("-defaultsaccessable")) {
          config.setDefaultsAccessable(true);
          } else if (args[i].equalsIgnoreCase("-useinterfaces")) {
          config.setUseInterfaces(true);
          } else if (args[i].equalsIgnoreCase("-geninterfaces")) {
          config.setGenerateInterfaces(true);
          } else if (args[i].equalsIgnoreCase("-nogeninterfaces")) {
          config.setGenerateInterfaces(false);
          } else if (args[i].equalsIgnoreCase("-keepelementpositions")) {
          config.setKeepElementPositions(true);
          } else if (args[i].equalsIgnoreCase("-comments")) {
          config.setProcessComments(true);
          } else if (args[i].equalsIgnoreCase("-doctype")) {
          config.setProcessDocType(true);
          } else if (args[i].equalsIgnoreCase("-dumpbeantree")) {
          config.setDumpBeanTree(new File(args[++i]));
          } else if (args[i].equalsIgnoreCase("-removeunreferencednodes")) {
          config.setRemoveUnreferencedNodes(true);
          } else if (args[i].equalsIgnoreCase("-writebeangraph")) {
          config.setWriteBeanGraphFile(new File(args[++i]));
          } else if (args[i].equalsIgnoreCase("-readbeangraph")) {
          config.addReadBeanGraphFiles(new File(args[++i]));
          } else if (args[i].equalsIgnoreCase("-gendotgraph")) {
          config.setGenerateDotGraph(new File(args[++i]));
          } else if (args[i].equalsIgnoreCase("-delegatedir")) {
          config.setDelegateDir(new File(args[++i]));
          } else if (args[i].equalsIgnoreCase("-delegatepackage")) {
          config.setDelegatePackage(args[++i]);
          } else if (args[i].equalsIgnoreCase("-checkuptodate")) {
          config.setCheckUpToDate(true);
          } else if (args[i].equalsIgnoreCase("-haschanged")) {
          config.setGenerateHasChanged(true);
          } else if (args[i].equalsIgnoreCase("-generateSwitches")) {
          config.setGenerateSwitches(true);
          } else if (args[i].equalsIgnoreCase("-min")) {
          config.setMinFeatures(true);
          } else if (args[i].equalsIgnoreCase("-forme")) {
          config.setForME(true);
          } else if (args[i].equalsIgnoreCase("-tagsfile")) {
          config.setGenerateTagsFile(true);
          } else if (args[i].equalsIgnoreCase("-help") || args[i].equalsIgnoreCase("--help")) {
          help = 0;
          } else {
          System.out.println("Unknown argument: "+args[i]);
          System.out.println("Use -help.");
          System.exit(1);
          }
          }
          } catch (Exception e) {
          help = 0;
          }
	
          if (help < 1) {
          System.out.println("usage:");
          System.out.println("java "+GenBeans.class.getName()+" -f filename [-d docRoot] [-t] [-p package] [-r rootDir] \n\t[-sp number] [-mdd filename] [-noe] [-ts] [-veto] [-version]\n\t[-st] [-throw] [-dtd|-xmlschema] [-javabeans] [-validate]\n\t[-propertyevents] [-attrprop] [-delegator] [-commonInterface] [-commonInterfaceName name]\n\t[-premium] [-compile] [-defaultsAccessable] [-auto]\n\t[-useinterfaces] [-geninterfaces] [-keepelementpositions]\n\t[-dumpbeantree filename] [-removeUnreferencedNodes]\n\t[-genDotGraph filename] [-comments] [-checkUpToDate]\n\t[-writeBeanGraph filename] [-readBeanGraph filename]*\n\t[-hasChanged] [-min] [-forME] [-tagsFile]\n");
          System.out.println("where:");
          System.out.println(" -f file name of the DTD");
          System.out.println(" -d DTD root element name (for example webapp or ejb-jar)");
          System.out.println(" -p package name");
          System.out.println(" -r base root directory (root of the package path)");
          System.out.println(" -sp set the indentation to use 'number' spaces instead of \n  the default tab (\ ) value");
          System.out.println(" -mdd provides extra information that the dtd cannot provide. \n  If the file doesn't exist, a skeleton file is created and \n  no bean generation happens.");
          System.out.println(" -noe do not throw the NoSuchElement exception when a scalar property\n  has no value, return a default '0' value instead.");
          System.out.println(" -ts the toString() of the bean returns the full content\n  of the bean sub-tree instead of its simple name.");
          System.out.println(" -veto generate vetoable properties (only for non-bean properties).");
          System.out.println(" -st standalone mode - do not generate NetBeans dependencies");
          System.out.println(" -throw generate code that prefers to pass exceptions\n  through instead of converting them to RuntimeException (recommended).");
          System.out.println(" -dtd DTD input mode (default)");
          System.out.println(" -xmlschema XML Schema input mode");
          System.out.println(" -javabeans Generate pure JavaBeans that do not need\n  any runtime library support (no BaseBean).");
          System.out.println(" -validate Generate a validate method for doing validation.");
          System.out.println(" -propertyevents Generate methods for dealing with property events (always on for BaseBean type).");
          System.out.println(" -attrprop Attributes become like any other property");
          System.out.println(" -delegator Generate a delegator class for every bean generated.");
          System.out.println(" -commonInterface Generate a common interface between all beans.");
          System.out.println(" -commonInterfaceName Name the common interface.");
          System.out.println(" -premium The \"Premium\" Package.  Turn on what ought to be the default switches (but can't be the default due to backwards compatibility).");
          System.out.println(" -min Generate the minimum Java Beans.  Reduce features in favor of reduced class file size.");
          System.out.println(" -compile Compile all generated classes using javac.");
          System.out.println(" -defaultsAccessable Generate methods to be able to get at default values.");
          System.out.println(" -useInterfaces Getters and setters signatures would any defined interface on the bean.");
          System.out.println(" -genInterfaces For every bean generated, generate an interfaces for it's accessors.");
          System.out.println(" -keepElementPositions Keep track of the positions of elements (no BaseBean support).");
          System.out.println(" -dumpBeanTree filename Write out the bean tree to filename.");
          System.out.println(" -removeUnreferencedNodes Do not generate unreferenced nodes from the bean graph.");
          System.out.println(" -writeBeanGraph Write out a beangraph XML file.  Useful for connecting separate bean graphs.");
          System.out.println(" -readBeanGraph Read in and use the results of another bean graph.");
          System.out.println(" -genDotGraph filename Generate a .dot style file for use with GraphViz.");
          System.out.println(" -comments Process and keep comments (always on for BaseBean type).");
          System.out.println(" -doctype Process and keep Document Types (always on for BaseBean type).");
          System.out.println(" -hasChanged Generate code to keep track if the bean graph has changed.");
          System.out.println(" -checkUpToDate Only do generation if the source files are newer than the to be generated files.");
          System.out.println(" -forME Generate code for use on J2ME.");
          System.out.println(" -tagsFile Generate a class that has all schema element & attribute names");
          System.out.println(" -t [parse|gen|all]\ttracing.");
	    
          System.out.println("\n\nThe bean classes are generated in the directory rootDir/packagePath, where packagePath is built using the package name specified. If the package name is not specified, the doc root element value is used as the default package name.  Use the empty string to get no (default) package.");
          System.out.println("\nexamples: java GenBeans -f ejb.dtd");
          System.out.println("          java GenBeans -f webapp.dtd -d webapp -p myproject.webapp -r /myPath/src");
          System.out.println("          java GenBeans -f webapp.xsd -xmlschema -r /myPath/src -premium");
          System.out.println("\nMost of the parameters are optional. Only the file name is mandatory.");
          System.out.println("With only the file name specified, the generator uses the current directory, and uses the schema docroot value as the package name.");
          }
          else*/
        DDLogFlags.debug = config.isTrace();
	    
        try {
            doIt(config);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public static void doIt(Config config) throws java.io.IOException, Schema2BeansException {
        normalizeConfig(config);

        calculateNewestSourceTime(config);

        boolean needToWriteMetaDD = processMetaDD(config);

        //  The class that build the DTD object graph
        TreeBuilder		tree = new TreeBuilder(config);
	
        //  The file parser calling back the handler
        SchemaParser 	parser = null;
        boolean tryAgain;
        int schemaType = config.getSchemaTypeNum();
        SchemaParseException lastException = null;
        do {
            tryAgain = false;
            if (schemaType == Config.XML_SCHEMA) {
                XMLSchemaParser xmlSchemaParser = new XMLSchemaParser(config, tree);
                if (config.getInputURI() != null)
                    xmlSchemaParser.setInputURI(config.getInputURI());
                parser = xmlSchemaParser;
            } else {
                parser = new DocDefParser(config, tree);
            }
            readBeanGraphs(config);
	
            try {
                //  parse the DTD, building the object graph
                parser.process();
            } catch (SchemaParseException e) {
                if (schemaType == Config.DTD) {
                    // Retry as XML Schema
                    tryAgain = true;
                    schemaType = Config.XML_SCHEMA;
                } else {
                    if (lastException == null)
                        throw e;
                    else
                        throw lastException;
                }
                lastException = e;
            }
        } while (tryAgain);
        config.setSchemaTypeNum(schemaType);

        // Build the beans from the graph, and code generate them out to disk.
        BeanBuilder builder = new BeanBuilder(tree, config,
                                              config.getCodeGeneratorFactory());
        builder.process();

        if (needToWriteMetaDD) {
            try {
                config.messageOut.println("Writing metaDD XML file");	// NOI18N
                FileOutputStream mddOut = new FileOutputStream(config.getMddFile());
                try {
                    config.getMetaDD().write(mddOut);
                } finally {
                    mddOut.close();
                }
            } catch (IOException e) {
                config.messageOut.println("Failed to write the mdd file: " +
                                          e.getMessage()); // NOI18N
                throw e;
            }
        }

        if (config.isDoCompile() &&
            config.getOutputStreamProvider() instanceof DefaultOutputStreamProvider) {
            DefaultOutputStreamProvider out = (DefaultOutputStreamProvider) config.getOutputStreamProvider();
            String[] javacArgs = new String[out.getGeneratedFiles().size()];
            int javaFileCount = 0;
            for (Iterator it = out.getGeneratedFiles().iterator(); it.hasNext(); ) {
                javacArgs[javaFileCount] = (String) it.next();
                ++javaFileCount;
            }

            if (javaFileCount == 0) {
                if (!config.isQuiet())
                    config.messageOut.println(Common.getMessage("MSG_NothingToCompile"));
            } else {
                if (!config.isQuiet())
                    config.messageOut.println(Common.getMessage("MSG_Compiling"));
                try {
                    Class javacClass = Class.forName("com.sun.tools.javac.Main");
                    java.lang.reflect.Method compileMethod = javacClass.getDeclaredMethod("compile", new Class[] {String[].class, PrintWriter.class});
                    // com.sun.tools.javac.Main.compile(javacArgs, pw);
                    PrintWriter pw = new PrintWriter(config.messageOut, true);
                    Object result = compileMethod.invoke(null,
                                                new Object[] {javacArgs, pw});
                    pw.flush();
                    int compileExitCode = 0;
                    if (result instanceof Integer)
                        compileExitCode = ((Integer)result).intValue();
                    if (compileExitCode != 0)
                        throw new RuntimeException("Compile errors: javac had an exit code of "+compileExitCode);
                } catch (java.lang.Exception e) {
                    // Maybe it's just a missing $JRE/tools.jar from
                    // the CLASSPATH.
                    //config.messageOut.println(Common.getMessage("MSG_UnableToCompile"));
                    //config.messageOut.println(e.getClass().getName()+": "+e.getMessage());	// NOI18N
                    //e.printStackTrace();
                    if (e instanceof IOException)
                        throw (IOException) e;
                    if (e instanceof Schema2BeansException)
                        throw (Schema2BeansException) e;
                    throw new Schema2BeansNestedException(Common.getMessage("MSG_UnableToCompile"), e);
                }
            }
        }
    }

    // Do some config parameter validation/adjustment.
    protected static void normalizeConfig(Config config) throws IOException {
        if (config.getIndentAmount() > 0) {
            config.setIndent("");
            for(int i = 0; i < config.getIndentAmount(); i++)
                config.setIndent(config.getIndent()+" ");
        }
        if (config.isGenerateTransactions()) {
            config.setGeneratePropertyEvents(true);
            config.setGenerateStoreEvents(true);
        } else if (!config.isGeneratePropertyEvents() && config.isGenerateStoreEvents())
            config.setGenerateStoreEvents(false);
        if (config.isGenerateHasChanged())
            config.setGenerateParentRefs(true);

        config.readConfigs();
        if (config.getWriteConfig() != null) {
            FileOutputStream fos = new FileOutputStream(config.getWriteConfig());
            try {
               config.write(fos);
            } finally {
                fos.close();
            }
        }
    }

    private static void readBeanGraphs(Config config) throws IOException {
        try {
            for (Iterator it = config.readBeanGraphFiles(); it.hasNext(); ) {
                File filename = (File) it.next();
                InputStream in = new FileInputStream(filename);
                org.netbeans.modules.schema2beansdev.beangraph.BeanGraph bg = org.netbeans.modules.schema2beansdev.beangraph.BeanGraph.read(in);
                in.close();
                config.addReadBeanGraphs(bg);
            }
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (org.xml.sax.SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return whether or not the MetaDD should be written out at the end.
     */
    private static boolean processMetaDD(Config config) throws IOException {
        boolean needToWriteMetaDD = false;
        MetaDD mdd;
        //
        //	Either creates the metaDD file or read the existing one
        //
        if (config.getMddFile() != null || config.getMddIn() != null) {
            File file = config.getMddFile();
	    
            if (config.getMddIn() == null && !file.exists()) {
                config.setDoGeneration(false);
                if (!config.isAuto()) {
                    if (!askYesOrNo(config.messageOut, "The mdd file " + config.getMddFile() +	// NOI18N
                                    " doesn't exist. Should we create it (y/n)? "))	{ // NOI18N
                        config.messageOut.println("Generation aborted."); // NOI18N
                        return false;
                    }
                }
                needToWriteMetaDD = true;
                mdd = new MetaDD();
            } else {
                try {
                    InputStream is = config.getMddIn();
                    if (config.getMddIn() == null) {
                        is = new FileInputStream(config.getMddFile());
                        config.messageOut.println(Common.getMessage("MSG_UsingMdd",
                                                                    config.getMddFile()));
                    }
                    mdd = MetaDD.read(is);
                    if (config.getMddIn() == null) {
                        is.close();
                    }
                } catch (IOException e) {
                    if (config.isTraceParse())
                        e.printStackTrace();
                    throw new IOException(Common.
                                          getMessage("CantCreateMetaDDFile_msg", e.getMessage()));
                } catch (javax.xml.parsers.ParserConfigurationException e) {
                    if (config.isTraceParse())
                        e.printStackTrace();
                    throw new IOException(Common.
                                          getMessage("CantCreateMetaDDFile_msg", e.getMessage()));
                } catch (org.xml.sax.SAXException e) {
                    if (config.isTraceParse())
                        e.printStackTrace();
                    throw new IOException(Common.
                                          getMessage("CantCreateMetaDDFile_msg", e.getMessage()));
                }
            }
        } else {
            // Create a MetaDD to look stuff up in later on, even though
            // it wasn't read in, and we're not going to write it out.
            mdd = new MetaDD();
        }
        config.setMetaDD(mdd);
        return needToWriteMetaDD;
    }

    private static boolean askYesOrNo(PrintStream out, String prompt) throws IOException {
        out.print(prompt);
        BufferedReader rd =
			new BufferedReader(new InputStreamReader(System.in));
		    
        String str;
        str = rd.readLine();
        
        return str.equalsIgnoreCase("y");	// NOI18N
    }

    private static void calculateNewestSourceTime(Config config) {
        if (config.getFilename() != null) {
            config.setIfNewerSourceTime(config.getFilename().lastModified());
        }
        if (config.getMddFile() != null) {
            config.setIfNewerSourceTime(config.getMddFile().lastModified());
        }
        for (Iterator it = config.readBeanGraphFiles(); it.hasNext(); ) {
            File f = (File) it.next();
            config.setIfNewerSourceTime(f.lastModified());
        }

        // Need to also check the times on schema2beans.jar & schema2beansdev.jar
        config.setIfNewerSourceTime(getLastModified(org.netbeans.modules.schema2beans.BaseBean.class));
        config.setIfNewerSourceTime(getLastModified(BeanClass.class));
        config.setIfNewerSourceTime(getLastModified(GenBeans.class));
        config.setIfNewerSourceTime(getLastModified(BeanBuilder.class));
        config.setIfNewerSourceTime(getLastModified(TreeBuilder.class));
        config.setIfNewerSourceTime(getLastModified(GraphLink.class));
        config.setIfNewerSourceTime(getLastModified(GraphNode.class));
        config.setIfNewerSourceTime(getLastModified(JavaBeanClass.class));
        //System.out.println("config.getNewestSourceTime="+config.getNewestSourceTime());
    }

    private static long getLastModified(Class cls) {
        try {
            String shortName = cls.getName().substring(1+cls.getName().lastIndexOf('.'));
            java.net.URL url = cls.getResource(shortName + ".class");
            String file = url.getFile();
            //System.out.println("url="+url);
            if ("file".equals(url.getProtocol())) {
                // example: file='/home/cliffwd/cvs/dublin/nb_all/schema2beans/rt/src/org/netbeans/modules/schema2beans/GenBeans.class'
                String result = file.substring(0, file.length() - cls.getName().length() - 6);
                return new File(file).lastModified();
            } else if ("jar".equals(url.getProtocol())) {
                // example: file = 'jar:/usr/local/j2sdkee1.3.1/lib/j2ee.jar!/org/w3c/dom/Node.class'
                String jarFile = file.substring(file.indexOf(':')+1);
                jarFile = jarFile.substring(0, jarFile.indexOf('!'));
                //System.out.println("jarFile="+jarFile);
                return new File(jarFile).lastModified();
            }
            return url.openConnection().getDate();
        } catch (java.io.IOException e) {
            return 0;
        }
    }
}

//******************************************************************************
// END_NOI18N
//******************************************************************************
