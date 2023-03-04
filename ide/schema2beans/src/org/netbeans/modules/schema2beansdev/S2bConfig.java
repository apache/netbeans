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
/**
 *	This generated bean class S2bConfig
 *	matches the schema element 's2bConfig'.
 *
 *
 *	This class matches the root element of the XML Schema,
 *	and is the root of the bean graph.
 *
 * 	s2bConfig <s2bConfig> : S2bConfig
 * 		schemaType <schemaType> : java.lang.String
 * 		traceParse <traceParse> : boolean[0,1]
 * 		traceGen <traceGen> : boolean[0,1]
 * 		traceMisc <traceMisc> : boolean[0,1]
 * 		traceDot <traceDot> : boolean[0,1]
 * 		filename <filename> : java.io.File[0,1] 	[Switch]
 * 		fileIn <fileIn> : java.io.InputStream[0,1]
 * 		docRoot <docRoot> : java.lang.String[0,1] 	[Switch]
 * 		rootDir <rootDir> : java.io.File 	[Switch]
 * 		packagePath <packagePath> : java.lang.String[0,1] 	[Switch]
 * 		indent <indent> : java.lang.String
 * 		indentAmount <indentAmount> : int[0,1] 	[Switch]
 * 		mddFile <mddFile> : java.io.File[0,1] 	[Switch]
 * 		mddIn <mddIn> : java.io.InputStream[0,1]
 * 		metaDD <metaDD> : org.netbeans.modules.schema2beansdev.metadd.MetaDD[0,1]
 * 		doGeneration <doGeneration> : boolean
 * 		scalarException <scalarException> : boolean 	[Switch]
 * 		dumpToString <dumpToString> : boolean[0,1] 	[Switch]
 * 		vetoable <vetoable> : boolean[0,1] 	[Switch]
 * 		standalone <standalone> : boolean[0,1] 	[Switch]
 * 		auto <auto> : boolean[0,1] 	[Switch]
 * 		messageOut <messageOut> : java.io.PrintStream[0,1]
 * 		outputStreamProvider <outputStreamProvider> : org.netbeans.modules.schema2beansdev.GenBeans.OutputStreamProvider[0,1]
 * 		throwErrors <throwErrors> : boolean[0,1] 	[Switch]
 * 		generateXMLIO <generateXMLIO> : boolean
 * 		generateValidate <generateValidate> : boolean[0,1] 	[Switch]
 * 		generatePropertyEvents <generatePropertyEvents> : boolean[0,1] 	[Switch]
 * 		generateStoreEvents <generateStoreEvents> : boolean[0,1]
 * 		generateTransactions <generateTransactions> : boolean[0,1] 	[Switch]
 * 		attributesAsProperties <attributesAsProperties> : boolean[0,1] 	[Switch]
 * 		generateDelegator <generateDelegator> : boolean[0,1] 	[Switch]
 * 		delegateDir <delegateDir> : java.io.File[0,1] 	[Switch]
 * 		delegatePackage <delegatePackage> : java.lang.String[0,1] 	[Switch]
 * 		generateCommonInterface <generateCommonInterface> : java.lang.String[0,1] 	[Switch]
 * 		defaultsAccessable <defaultsAccessable> : boolean[0,1] 	[Switch]
 * 		useInterfaces <useInterfaces> : boolean[0,1] 	[Switch]
 * 		generateInterfaces <generateInterfaces> : boolean[0,1] 	[Switch]
 * 		keepElementPositions <keepElementPositions> : boolean[0,1] 	[Switch]
 * 		removeUnreferencedNodes <removeUnreferencedNodes> : boolean[0,1] 	[Switch]
 * 		inputURI <inputURI> : java.lang.String[0,1]
 * 		indexedPropertyType <indexedPropertyType> : java.lang.String 	[Switch]
 * 		doCompile <doCompile> : boolean[0,1] 	[Switch]
 * 		generateSwitches <generateSwitches> : boolean[0,1] 	[Switch]
 * 		dumpBeanTree <dumpBeanTree> : java.io.File[0,1] 	[Switch]
 * 		generateDotGraph <generateDotGraph> : java.io.File[0,1] 	[Switch]
 * 		processComments <processComments> : boolean[0,1] 	[Switch]
 * 		processDocType <processDocType> : boolean[0,1] 	[Switch]
 * 		checkUpToDate <checkUpToDate> : boolean[0,1] 	[Switch]
 * 		generateParentRefs <generateParentRefs> : boolean[0,1]
 * 		generateHasChanged <generateHasChanged> : boolean[0,1] 	[Switch]
 * 		newestSourceTime <newestSourceTime> : long[0,1]
 * 		writeBeanGraphFile <writeBeanGraphFile> : java.io.File[0,1] 	[Switch]
 * 		readBeanGraphFiles <readBeanGraphFiles> : java.io.File[0,n] 	[Switch]
 * 		readBeanGraphs <readBeanGraphs> : org.netbeans.modules.schema2beansdev.beangraph.BeanGraph[0,n]
 * 		minFeatures <minFeatures> : boolean[0,1] 	[Switch]
 * 		forME <forME> : boolean[0,1] 	[Switch]
 * 		java5 <java5> : boolean[0,1] 	[Switch]
 * 		generateTagsFile <generateTagsFile> : boolean[0,1] 	[Switch]
 * 		codeGeneratorFactory <codeGeneratorFactory> : org.netbeans.modules.schema2beansdev.CodeGeneratorFactory[0,1]
 * 		generateTimeStamp <generateTimeStamp> : boolean 	[Switch]
 * 		quiet <quiet> : boolean 	[Switch]
 * 		writeConfig <writeConfig> : java.io.File[0,1] 	[Switch]
 * 		readConfig <readConfig> : java.io.File[0,n] 	[Switch]
 * 		makeDefaults <makeDefaults> : boolean 	[Switch]
 * 		setDefaults <setDefaults> : boolean[0,1] 	[Switch]
 * 		trimNonStrings <trimNonStrings> : boolean[0,1] 	[Switch]
 * 		useRuntime <useRuntime> : boolean[0,1] 	[Switch]
 * 		extendBaseBean <extendBaseBean> : boolean[0,1] 	[Switch]
 * 		finder <finder> : java.lang.String[0,n] 	[Switch]
 * 		target <target> : java.lang.String[0,1] 	[Switch]
 * 		staxProduceXMLEventReader <staxProduceXMLEventReader> : boolean[0,1] 	[Switch]
 * 		staxUseXMLEventReader <staxUseXMLEventReader> : boolean[0,1] 	[Switch]
 * 		optionalScalars <optionalScalars> : boolean[0,1] 	[Switch]
 * 		defaultElementType <defaultElementType> : java.lang.String[0,1] 	[Switch]
 * 		respectExtension <respectExtension> : boolean[0,1] 	[Switch]
 * 		logSuspicious <logSuspicious> : boolean[0,1] 	[Switch]
 *
 * @Generated
 */

package org.netbeans.modules.schema2beansdev;

public class S2bConfig {
	public static final String SCHEMATYPE = "SchemaType";	// NOI18N
	public static final String TRACEPARSE = "TraceParse";	// NOI18N
	public static final String TRACEGEN = "TraceGen";	// NOI18N
	public static final String TRACEMISC = "TraceMisc";	// NOI18N
	public static final String TRACEDOT = "TraceDot";	// NOI18N
	public static final String FILENAME = "Filename";	// NOI18N
	public static final String FILEIN = "FileIn";	// NOI18N
	public static final String DOCROOT = "DocRoot";	// NOI18N
	public static final String ROOTDIR = "RootDir";	// NOI18N
	public static final String PACKAGEPATH = "PackagePath";	// NOI18N
	public static final String INDENT = "Indent";	// NOI18N
	public static final String INDENTAMOUNT = "IndentAmount";	// NOI18N
	public static final String MDDFILE = "MddFile";	// NOI18N
	public static final String MDDIN = "MddIn";	// NOI18N
	public static final String METADD = "MetaDD";	// NOI18N
	public static final String DOGENERATION = "DoGeneration";	// NOI18N
	public static final String SCALAREXCEPTION = "ScalarException";	// NOI18N
	public static final String DUMPTOSTRING = "DumpToString";	// NOI18N
	public static final String VETOABLE = "Vetoable";	// NOI18N
	public static final String STANDALONE = "Standalone";	// NOI18N
	public static final String AUTO = "Auto";	// NOI18N
	public static final String MESSAGEOUT = "MessageOut";	// NOI18N
	public static final String OUTPUTSTREAMPROVIDER = "OutputStreamProvider";	// NOI18N
	public static final String THROWERRORS = "ThrowErrors";	// NOI18N
	public static final String GENERATEXMLIO = "GenerateXMLIO";	// NOI18N
	public static final String GENERATEVALIDATE = "GenerateValidate";	// NOI18N
	public static final String GENERATEPROPERTYEVENTS = "GeneratePropertyEvents";	// NOI18N
	public static final String GENERATESTOREEVENTS = "GenerateStoreEvents";	// NOI18N
	public static final String GENERATETRANSACTIONS = "GenerateTransactions";	// NOI18N
	public static final String ATTRIBUTESASPROPERTIES = "AttributesAsProperties";	// NOI18N
	public static final String GENERATEDELEGATOR = "GenerateDelegator";	// NOI18N
	public static final String DELEGATEDIR = "DelegateDir";	// NOI18N
	public static final String DELEGATEPACKAGE = "DelegatePackage";	// NOI18N
	public static final String GENERATECOMMONINTERFACE = "GenerateCommonInterface";	// NOI18N
	public static final String DEFAULTSACCESSABLE = "DefaultsAccessable";	// NOI18N
	public static final String USEINTERFACES = "UseInterfaces";	// NOI18N
	public static final String GENERATEINTERFACES = "GenerateInterfaces";	// NOI18N
	public static final String KEEPELEMENTPOSITIONS = "KeepElementPositions";	// NOI18N
	public static final String REMOVEUNREFERENCEDNODES = "RemoveUnreferencedNodes";	// NOI18N
	public static final String INPUTURI = "InputURI";	// NOI18N
	public static final String INDEXEDPROPERTYTYPE = "IndexedPropertyType";	// NOI18N
	public static final String DOCOMPILE = "DoCompile";	// NOI18N
	public static final String GENERATESWITCHES = "GenerateSwitches";	// NOI18N
	public static final String DUMPBEANTREE = "DumpBeanTree";	// NOI18N
	public static final String GENERATEDOTGRAPH = "GenerateDotGraph";	// NOI18N
	public static final String PROCESSCOMMENTS = "ProcessComments";	// NOI18N
	public static final String PROCESSDOCTYPE = "ProcessDocType";	// NOI18N
	public static final String CHECKUPTODATE = "CheckUpToDate";	// NOI18N
	public static final String GENERATEPARENTREFS = "GenerateParentRefs";	// NOI18N
	public static final String GENERATEHASCHANGED = "GenerateHasChanged";	// NOI18N
	public static final String NEWESTSOURCETIME = "NewestSourceTime";	// NOI18N
	public static final String WRITEBEANGRAPHFILE = "WriteBeanGraphFile";	// NOI18N
	public static final String READBEANGRAPHFILES = "ReadBeanGraphFiles";	// NOI18N
	public static final String READBEANGRAPHS = "ReadBeanGraphs";	// NOI18N
	public static final String MINFEATURES = "MinFeatures";	// NOI18N
	public static final String FORME = "ForME";	// NOI18N
	public static final String JAVA5 = "Java5";	// NOI18N
	public static final String GENERATETAGSFILE = "GenerateTagsFile";	// NOI18N
	public static final String CODEGENERATORFACTORY = "CodeGeneratorFactory";	// NOI18N
	public static final String GENERATETIMESTAMP = "GenerateTimeStamp";	// NOI18N
	public static final String QUIET = "Quiet";	// NOI18N
	public static final String WRITECONFIG = "WriteConfig";	// NOI18N
	public static final String READCONFIG = "ReadConfig";	// NOI18N
	public static final String MAKEDEFAULTS = "MakeDefaults";	// NOI18N
	public static final String SETDEFAULTS = "SetDefaults";	// NOI18N
	public static final String TRIMNONSTRINGS = "TrimNonStrings";	// NOI18N
	public static final String USERUNTIME = "UseRuntime";	// NOI18N
	public static final String EXTENDBASEBEAN = "ExtendBaseBean";	// NOI18N
	public static final String FINDER = "Finder";	// NOI18N
	public static final String TARGET = "Target";	// NOI18N
	public static final String STAXPRODUCEXMLEVENTREADER = "StaxProduceXMLEventReader";	// NOI18N
	public static final String STAXUSEXMLEVENTREADER = "StaxUseXMLEventReader";	// NOI18N
	public static final String OPTIONALSCALARS = "OptionalScalars";	// NOI18N
	public static final String DEFAULTELEMENTTYPE = "DefaultElementType";	// NOI18N
	public static final String RESPECTEXTENSION = "RespectExtension";	// NOI18N
	public static final String LOGSUSPICIOUS = "LogSuspicious";	// NOI18N

	private java.lang.String _SchemaType = "xmlschema";
	private boolean _TraceParse;
	private boolean _isSet_TraceParse = false;
	private boolean _TraceGen;
	private boolean _isSet_TraceGen = false;
	private boolean _TraceMisc;
	private boolean _isSet_TraceMisc = false;
	private boolean _TraceDot;
	private boolean _isSet_TraceDot = false;
	private java.io.File _Filename;
	private java.io.InputStream _FileIn;
	private java.lang.String _DocRoot;
	private java.io.File _RootDir = new java.io.File(".");
	private java.lang.String _PackagePath;
	private java.lang.String _Indent = "\t";
	private int _IndentAmount;
	private boolean _isSet_IndentAmount = false;
	private java.io.File _MddFile;
	private java.io.InputStream _MddIn;
	private org.netbeans.modules.schema2beansdev.metadd.MetaDD _MetaDD;
	private boolean _DoGeneration = true;
	private boolean _isSet_DoGeneration = true;
	private boolean _ScalarException = true;
	private boolean _isSet_ScalarException = true;
	private boolean _DumpToString;
	private boolean _isSet_DumpToString = false;
	private boolean _Vetoable;
	private boolean _isSet_Vetoable = false;
	private boolean _Standalone;
	private boolean _isSet_Standalone = false;
	private boolean _Auto;
	private boolean _isSet_Auto = false;
	private java.io.PrintStream _MessageOut;
	private org.netbeans.modules.schema2beansdev.GenBeans.OutputStreamProvider _OutputStreamProvider;
	private boolean _ThrowErrors;
	private boolean _isSet_ThrowErrors = false;
	private boolean _GenerateXMLIO = true;
	private boolean _isSet_GenerateXMLIO = true;
	private boolean _GenerateValidate;
	private boolean _isSet_GenerateValidate = false;
	private boolean _GeneratePropertyEvents;
	private boolean _isSet_GeneratePropertyEvents = false;
	private boolean _GenerateStoreEvents;
	private boolean _isSet_GenerateStoreEvents = false;
	private boolean _GenerateTransactions;
	private boolean _isSet_GenerateTransactions = false;
	private boolean _AttributesAsProperties;
	private boolean _isSet_AttributesAsProperties = false;
	private boolean _GenerateDelegator;
	private boolean _isSet_GenerateDelegator = false;
	private java.io.File _DelegateDir;
	private java.lang.String _DelegatePackage;
	private java.lang.String _GenerateCommonInterface;
	private boolean _DefaultsAccessable;
	private boolean _isSet_DefaultsAccessable = false;
	private boolean _UseInterfaces;
	private boolean _isSet_UseInterfaces = false;
	private boolean _GenerateInterfaces;
	private boolean _isSet_GenerateInterfaces = false;
	private boolean _KeepElementPositions;
	private boolean _isSet_KeepElementPositions = false;
	private boolean _RemoveUnreferencedNodes;
	private boolean _isSet_RemoveUnreferencedNodes = false;
	private java.lang.String _InputURI;
	private java.lang.String _IndexedPropertyType = "java.util.ArrayList";
	private boolean _DoCompile;
	private boolean _isSet_DoCompile = false;
	private boolean _GenerateSwitches;
	private boolean _isSet_GenerateSwitches = false;
	private java.io.File _DumpBeanTree;
	private java.io.File _GenerateDotGraph;
	private boolean _ProcessComments;
	private boolean _isSet_ProcessComments = false;
	private boolean _ProcessDocType;
	private boolean _isSet_ProcessDocType = false;
	private boolean _CheckUpToDate;
	private boolean _isSet_CheckUpToDate = false;
	private boolean _GenerateParentRefs;
	private boolean _isSet_GenerateParentRefs = false;
	private boolean _GenerateHasChanged;
	private boolean _isSet_GenerateHasChanged = false;
	private long _NewestSourceTime;
	private boolean _isSet_NewestSourceTime = false;
	private java.io.File _WriteBeanGraphFile;
	private java.util.List _ReadBeanGraphFiles = new java.util.ArrayList();	// List<java.io.File>
	private java.util.List _ReadBeanGraphs = new java.util.ArrayList();	// List<org.netbeans.modules.schema2beansdev.beangraph.BeanGraph>
	private boolean _MinFeatures;
	private boolean _isSet_MinFeatures = false;
	private boolean _ForME;
	private boolean _isSet_ForME = false;
	private boolean _Java5;
	private boolean _isSet_Java5 = false;
	private boolean _GenerateTagsFile;
	private boolean _isSet_GenerateTagsFile = false;
	private org.netbeans.modules.schema2beansdev.CodeGeneratorFactory _CodeGeneratorFactory;
	private boolean _GenerateTimeStamp = true;
	private boolean _isSet_GenerateTimeStamp = true;
	private boolean _Quiet;
	private boolean _isSet_Quiet = false;
	private java.io.File _WriteConfig;
	private java.util.List _ReadConfig = new java.util.ArrayList();	// List<java.io.File>
	private boolean _MakeDefaults = true;
	private boolean _isSet_MakeDefaults = true;
	private boolean _SetDefaults;
	private boolean _isSet_SetDefaults = false;
	private boolean _TrimNonStrings;
	private boolean _isSet_TrimNonStrings = false;
	private boolean _UseRuntime;
	private boolean _isSet_UseRuntime = false;
	private boolean _ExtendBaseBean;
	private boolean _isSet_ExtendBaseBean = false;
	private java.util.List _Finder = new java.util.ArrayList();	// List<java.lang.String>
	private java.lang.String _Target;
	private boolean _StaxProduceXMLEventReader;
	private boolean _isSet_StaxProduceXMLEventReader = false;
	private boolean _StaxUseXMLEventReader;
	private boolean _isSet_StaxUseXMLEventReader = false;
	private boolean _OptionalScalars;
	private boolean _isSet_OptionalScalars = false;
	private java.lang.String _DefaultElementType;
	private boolean _RespectExtension;
	private boolean _isSet_RespectExtension = false;
	private boolean _LogSuspicious;
	private boolean _isSet_LogSuspicious = false;
	private java.lang.String schemaLocation;
	private static final java.util.logging.Logger _logger = java.util.logging.Logger.getLogger("org.netbeans.modules.schema2beansdev.S2bConfig");

	/**
	 * Normal starting point constructor.
	 */
	public S2bConfig() {
	}

	/**
	 * Required parameters constructor
	 */
	public S2bConfig(java.lang.String schemaType, java.io.File rootDir, java.lang.String indent, boolean doGeneration, boolean scalarException, boolean generateXMLIO, java.lang.String indexedPropertyType, boolean generateTimeStamp, boolean quiet, boolean makeDefaults) {
		_SchemaType = schemaType;
		_RootDir = rootDir;
		_Indent = indent;
		_DoGeneration = doGeneration;
		_isSet_DoGeneration = true;
		_ScalarException = scalarException;
		_isSet_ScalarException = true;
		_GenerateXMLIO = generateXMLIO;
		_isSet_GenerateXMLIO = true;
		_IndexedPropertyType = indexedPropertyType;
		_GenerateTimeStamp = generateTimeStamp;
		_isSet_GenerateTimeStamp = true;
		_Quiet = quiet;
		_isSet_Quiet = true;
		_MakeDefaults = makeDefaults;
		_isSet_MakeDefaults = true;
	}

	/**
	 * Deep copy
	 */
	public S2bConfig(org.netbeans.modules.schema2beansdev.S2bConfig source) {
		this(source, false);
	}

	/**
	 * Deep copy
	 * @param justData just copy the XML relevant data
	 */
	public S2bConfig(org.netbeans.modules.schema2beansdev.S2bConfig source, boolean justData) {
		_SchemaType = source._SchemaType;
		_TraceParse = source._TraceParse;
		_isSet_TraceParse = source._isSet_TraceParse;
		_TraceGen = source._TraceGen;
		_isSet_TraceGen = source._isSet_TraceGen;
		_TraceMisc = source._TraceMisc;
		_isSet_TraceMisc = source._isSet_TraceMisc;
		_TraceDot = source._TraceDot;
		_isSet_TraceDot = source._isSet_TraceDot;
		_Filename = (source._Filename == null) ? null : new java.io.File(source._Filename.getAbsolutePath());
		_FileIn = source._FileIn;
		_DocRoot = source._DocRoot;
		_RootDir = (source._RootDir == null) ? null : new java.io.File(source._RootDir.getAbsolutePath());
		_PackagePath = source._PackagePath;
		_Indent = source._Indent;
		_IndentAmount = source._IndentAmount;
		_isSet_IndentAmount = source._isSet_IndentAmount;
		_MddFile = (source._MddFile == null) ? null : new java.io.File(source._MddFile.getAbsolutePath());
		_MddIn = source._MddIn;
		_MetaDD = (source._MetaDD == null) ? null : new org.netbeans.modules.schema2beansdev.metadd.MetaDD(source._MetaDD);
		_DoGeneration = source._DoGeneration;
		_isSet_DoGeneration = source._isSet_DoGeneration;
		_ScalarException = source._ScalarException;
		_isSet_ScalarException = source._isSet_ScalarException;
		_DumpToString = source._DumpToString;
		_isSet_DumpToString = source._isSet_DumpToString;
		_Vetoable = source._Vetoable;
		_isSet_Vetoable = source._isSet_Vetoable;
		_Standalone = source._Standalone;
		_isSet_Standalone = source._isSet_Standalone;
		_Auto = source._Auto;
		_isSet_Auto = source._isSet_Auto;
		_MessageOut = (source._MessageOut == null) ? null : new java.io.PrintStream(source._MessageOut);
		_OutputStreamProvider = source._OutputStreamProvider;
		_ThrowErrors = source._ThrowErrors;
		_isSet_ThrowErrors = source._isSet_ThrowErrors;
		_GenerateXMLIO = source._GenerateXMLIO;
		_isSet_GenerateXMLIO = source._isSet_GenerateXMLIO;
		_GenerateValidate = source._GenerateValidate;
		_isSet_GenerateValidate = source._isSet_GenerateValidate;
		_GeneratePropertyEvents = source._GeneratePropertyEvents;
		_isSet_GeneratePropertyEvents = source._isSet_GeneratePropertyEvents;
		_GenerateStoreEvents = source._GenerateStoreEvents;
		_isSet_GenerateStoreEvents = source._isSet_GenerateStoreEvents;
		_GenerateTransactions = source._GenerateTransactions;
		_isSet_GenerateTransactions = source._isSet_GenerateTransactions;
		_AttributesAsProperties = source._AttributesAsProperties;
		_isSet_AttributesAsProperties = source._isSet_AttributesAsProperties;
		_GenerateDelegator = source._GenerateDelegator;
		_isSet_GenerateDelegator = source._isSet_GenerateDelegator;
		_DelegateDir = (source._DelegateDir == null) ? null : new java.io.File(source._DelegateDir.getAbsolutePath());
		_DelegatePackage = source._DelegatePackage;
		_GenerateCommonInterface = source._GenerateCommonInterface;
		_DefaultsAccessable = source._DefaultsAccessable;
		_isSet_DefaultsAccessable = source._isSet_DefaultsAccessable;
		_UseInterfaces = source._UseInterfaces;
		_isSet_UseInterfaces = source._isSet_UseInterfaces;
		_GenerateInterfaces = source._GenerateInterfaces;
		_isSet_GenerateInterfaces = source._isSet_GenerateInterfaces;
		_KeepElementPositions = source._KeepElementPositions;
		_isSet_KeepElementPositions = source._isSet_KeepElementPositions;
		_RemoveUnreferencedNodes = source._RemoveUnreferencedNodes;
		_isSet_RemoveUnreferencedNodes = source._isSet_RemoveUnreferencedNodes;
		_InputURI = source._InputURI;
		_IndexedPropertyType = source._IndexedPropertyType;
		_DoCompile = source._DoCompile;
		_isSet_DoCompile = source._isSet_DoCompile;
		_GenerateSwitches = source._GenerateSwitches;
		_isSet_GenerateSwitches = source._isSet_GenerateSwitches;
		_DumpBeanTree = (source._DumpBeanTree == null) ? null : new java.io.File(source._DumpBeanTree.getAbsolutePath());
		_GenerateDotGraph = (source._GenerateDotGraph == null) ? null : new java.io.File(source._GenerateDotGraph.getAbsolutePath());
		_ProcessComments = source._ProcessComments;
		_isSet_ProcessComments = source._isSet_ProcessComments;
		_ProcessDocType = source._ProcessDocType;
		_isSet_ProcessDocType = source._isSet_ProcessDocType;
		_CheckUpToDate = source._CheckUpToDate;
		_isSet_CheckUpToDate = source._isSet_CheckUpToDate;
		_GenerateParentRefs = source._GenerateParentRefs;
		_isSet_GenerateParentRefs = source._isSet_GenerateParentRefs;
		_GenerateHasChanged = source._GenerateHasChanged;
		_isSet_GenerateHasChanged = source._isSet_GenerateHasChanged;
		_NewestSourceTime = source._NewestSourceTime;
		_isSet_NewestSourceTime = source._isSet_NewestSourceTime;
		_WriteBeanGraphFile = (source._WriteBeanGraphFile == null) ? null : new java.io.File(source._WriteBeanGraphFile.getAbsolutePath());
		for (java.util.Iterator it = source._ReadBeanGraphFiles.iterator(); 
			it.hasNext(); ) {
			java.io.File srcElement = (java.io.File)it.next();
			_ReadBeanGraphFiles.add((srcElement == null) ? null : new java.io.File(srcElement.getAbsolutePath()));
		}
		for (java.util.Iterator it = source._ReadBeanGraphs.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.schema2beansdev.beangraph.BeanGraph srcElement = (org.netbeans.modules.schema2beansdev.beangraph.BeanGraph)it.next();
			_ReadBeanGraphs.add((srcElement == null) ? null : new org.netbeans.modules.schema2beansdev.beangraph.BeanGraph(srcElement));
		}
		_MinFeatures = source._MinFeatures;
		_isSet_MinFeatures = source._isSet_MinFeatures;
		_ForME = source._ForME;
		_isSet_ForME = source._isSet_ForME;
		_Java5 = source._Java5;
		_isSet_Java5 = source._isSet_Java5;
		_GenerateTagsFile = source._GenerateTagsFile;
		_isSet_GenerateTagsFile = source._isSet_GenerateTagsFile;
		_CodeGeneratorFactory = source._CodeGeneratorFactory;
		_GenerateTimeStamp = source._GenerateTimeStamp;
		_isSet_GenerateTimeStamp = source._isSet_GenerateTimeStamp;
		_Quiet = source._Quiet;
		_isSet_Quiet = source._isSet_Quiet;
		_WriteConfig = (source._WriteConfig == null) ? null : new java.io.File(source._WriteConfig.getAbsolutePath());
		for (java.util.Iterator it = source._ReadConfig.iterator(); 
			it.hasNext(); ) {
			java.io.File srcElement = (java.io.File)it.next();
			_ReadConfig.add((srcElement == null) ? null : new java.io.File(srcElement.getAbsolutePath()));
		}
		_MakeDefaults = source._MakeDefaults;
		_isSet_MakeDefaults = source._isSet_MakeDefaults;
		_SetDefaults = source._SetDefaults;
		_isSet_SetDefaults = source._isSet_SetDefaults;
		_TrimNonStrings = source._TrimNonStrings;
		_isSet_TrimNonStrings = source._isSet_TrimNonStrings;
		_UseRuntime = source._UseRuntime;
		_isSet_UseRuntime = source._isSet_UseRuntime;
		_ExtendBaseBean = source._ExtendBaseBean;
		_isSet_ExtendBaseBean = source._isSet_ExtendBaseBean;
		for (java.util.Iterator it = source._Finder.iterator(); 
			it.hasNext(); ) {
			java.lang.String srcElement = (java.lang.String)it.next();
			_Finder.add(srcElement);
		}
		_Target = source._Target;
		_StaxProduceXMLEventReader = source._StaxProduceXMLEventReader;
		_isSet_StaxProduceXMLEventReader = source._isSet_StaxProduceXMLEventReader;
		_StaxUseXMLEventReader = source._StaxUseXMLEventReader;
		_isSet_StaxUseXMLEventReader = source._isSet_StaxUseXMLEventReader;
		_OptionalScalars = source._OptionalScalars;
		_isSet_OptionalScalars = source._isSet_OptionalScalars;
		_DefaultElementType = source._DefaultElementType;
		_RespectExtension = source._RespectExtension;
		_isSet_RespectExtension = source._isSet_RespectExtension;
		_LogSuspicious = source._LogSuspicious;
		_isSet_LogSuspicious = source._isSet_LogSuspicious;
		schemaLocation = source.schemaLocation;
	}

	// This attribute is mandatory
	public void setSchemaType(java.lang.String value) {
		_SchemaType = value;
	}

	public java.lang.String getSchemaType() {
		return _SchemaType;
	}

	// This attribute is optional
	public void setTraceParse(boolean value) {
		_TraceParse = value;
		_isSet_TraceParse = true;
	}

	public boolean isTraceParse() {
		return _TraceParse;
	}

	// This attribute is optional
	public void setTraceGen(boolean value) {
		_TraceGen = value;
		_isSet_TraceGen = true;
	}

	public boolean isTraceGen() {
		return _TraceGen;
	}

	// This attribute is optional
	public void setTraceMisc(boolean value) {
		_TraceMisc = value;
		_isSet_TraceMisc = true;
	}

	public boolean isTraceMisc() {
		return _TraceMisc;
	}

	// This attribute is optional
	public void setTraceDot(boolean value) {
		_TraceDot = value;
		_isSet_TraceDot = true;
	}

	public boolean isTraceDot() {
		return _TraceDot;
	}

	// This attribute is optional
	public void setFilename(java.io.File value) {
		_Filename = value;
	}

	public java.io.File getFilename() {
		return _Filename;
	}

	// This attribute is optional
	public void setFileIn(java.io.InputStream value) {
		_FileIn = value;
	}

	public java.io.InputStream getFileIn() {
		return _FileIn;
	}

	// This attribute is optional
	public void setDocRoot(java.lang.String value) {
		_DocRoot = value;
	}

	public java.lang.String getDocRoot() {
		return _DocRoot;
	}

	// This attribute is mandatory
	public void setRootDir(java.io.File value) {
		_RootDir = value;
	}

	public java.io.File getRootDir() {
		return _RootDir;
	}

	// This attribute is optional
	public void setPackagePath(java.lang.String value) {
		_PackagePath = value;
	}

	public java.lang.String getPackagePath() {
		return _PackagePath;
	}

	// This attribute is mandatory
	public void setIndent(java.lang.String value) {
		_Indent = value;
	}

	public java.lang.String getIndent() {
		return _Indent;
	}

	// This attribute is optional
	public void setIndentAmount(int value) {
		_IndentAmount = value;
		_isSet_IndentAmount = true;
	}

	public int getIndentAmount() {
		return _IndentAmount;
	}

	// This attribute is optional
	public void setMddFile(java.io.File value) {
		_MddFile = value;
	}

	public java.io.File getMddFile() {
		return _MddFile;
	}

	// This attribute is optional
	public void setMddIn(java.io.InputStream value) {
		_MddIn = value;
	}

	public java.io.InputStream getMddIn() {
		return _MddIn;
	}

	// This attribute is optional
	public void setMetaDD(org.netbeans.modules.schema2beansdev.metadd.MetaDD value) {
		_MetaDD = value;
	}

	public org.netbeans.modules.schema2beansdev.metadd.MetaDD getMetaDD() {
		return _MetaDD;
	}

	// This attribute is mandatory
	public void setDoGeneration(boolean value) {
		_DoGeneration = value;
		_isSet_DoGeneration = true;
	}

	public boolean isDoGeneration() {
		return _DoGeneration;
	}

	// This attribute is mandatory
	public void setScalarException(boolean value) {
		_ScalarException = value;
		_isSet_ScalarException = true;
	}

	public boolean isScalarException() {
		return _ScalarException;
	}

	// This attribute is optional
	public void setDumpToString(boolean value) {
		_DumpToString = value;
		_isSet_DumpToString = true;
	}

	public boolean isDumpToString() {
		return _DumpToString;
	}

	// This attribute is optional
	public void setVetoable(boolean value) {
		_Vetoable = value;
		_isSet_Vetoable = true;
	}

	public boolean isVetoable() {
		return _Vetoable;
	}

	// This attribute is optional
	public void setStandalone(boolean value) {
		_Standalone = value;
		_isSet_Standalone = true;
	}

	public boolean isStandalone() {
		return _Standalone;
	}

	// This attribute is optional
	public void setAuto(boolean value) {
		_Auto = value;
		_isSet_Auto = true;
	}

	public boolean isAuto() {
		return _Auto;
	}

	// This attribute is optional
	public void setMessageOut(java.io.PrintStream value) {
		_MessageOut = value;
	}

	public java.io.PrintStream getMessageOut() {
		return _MessageOut;
	}

	// This attribute is optional
	public void setOutputStreamProvider(org.netbeans.modules.schema2beansdev.GenBeans.OutputStreamProvider value) {
		_OutputStreamProvider = value;
	}

	public org.netbeans.modules.schema2beansdev.GenBeans.OutputStreamProvider getOutputStreamProvider() {
		return _OutputStreamProvider;
	}

	// This attribute is optional
	public void setThrowErrors(boolean value) {
		_ThrowErrors = value;
		_isSet_ThrowErrors = true;
	}

	public boolean isThrowErrors() {
		return _ThrowErrors;
	}

	// This attribute is mandatory
	public void setGenerateXMLIO(boolean value) {
		_GenerateXMLIO = value;
		_isSet_GenerateXMLIO = true;
	}

	public boolean isGenerateXMLIO() {
		return _GenerateXMLIO;
	}

	// This attribute is optional
	public void setGenerateValidate(boolean value) {
		_GenerateValidate = value;
		_isSet_GenerateValidate = true;
	}

	public boolean isGenerateValidate() {
		return _GenerateValidate;
	}

	// This attribute is optional
	public void setGeneratePropertyEvents(boolean value) {
		_GeneratePropertyEvents = value;
		_isSet_GeneratePropertyEvents = true;
	}

	public boolean isGeneratePropertyEvents() {
		return _GeneratePropertyEvents;
	}

	// This attribute is optional
	public void setGenerateStoreEvents(boolean value) {
		_GenerateStoreEvents = value;
		_isSet_GenerateStoreEvents = true;
	}

	public boolean isGenerateStoreEvents() {
		return _GenerateStoreEvents;
	}

	// This attribute is optional
	public void setGenerateTransactions(boolean value) {
		_GenerateTransactions = value;
		_isSet_GenerateTransactions = true;
	}

	public boolean isGenerateTransactions() {
		return _GenerateTransactions;
	}

	// This attribute is optional
	public void setAttributesAsProperties(boolean value) {
		_AttributesAsProperties = value;
		_isSet_AttributesAsProperties = true;
	}

	public boolean isAttributesAsProperties() {
		return _AttributesAsProperties;
	}

	// This attribute is optional
	public void setGenerateDelegator(boolean value) {
		_GenerateDelegator = value;
		_isSet_GenerateDelegator = true;
	}

	public boolean isGenerateDelegator() {
		return _GenerateDelegator;
	}

	// This attribute is optional
	public void setDelegateDir(java.io.File value) {
		_DelegateDir = value;
	}

	public java.io.File getDelegateDir() {
		return _DelegateDir;
	}

	// This attribute is optional
	public void setDelegatePackage(java.lang.String value) {
		_DelegatePackage = value;
	}

	public java.lang.String getDelegatePackage() {
		return _DelegatePackage;
	}

	// This attribute is optional
	public void setGenerateCommonInterface(java.lang.String value) {
		_GenerateCommonInterface = value;
	}

	public java.lang.String getGenerateCommonInterface() {
		return _GenerateCommonInterface;
	}

	// This attribute is optional
	public void setDefaultsAccessable(boolean value) {
		_DefaultsAccessable = value;
		_isSet_DefaultsAccessable = true;
	}

	public boolean isDefaultsAccessable() {
		return _DefaultsAccessable;
	}

	// This attribute is optional
	public void setUseInterfaces(boolean value) {
		_UseInterfaces = value;
		_isSet_UseInterfaces = true;
	}

	public boolean isUseInterfaces() {
		return _UseInterfaces;
	}

	// This attribute is optional
	public void setGenerateInterfaces(boolean value) {
		_GenerateInterfaces = value;
		_isSet_GenerateInterfaces = true;
	}

	public boolean isGenerateInterfaces() {
		return _GenerateInterfaces;
	}

	// This attribute is optional
	public void setKeepElementPositions(boolean value) {
		_KeepElementPositions = value;
		_isSet_KeepElementPositions = true;
	}

	public boolean isKeepElementPositions() {
		return _KeepElementPositions;
	}

	// This attribute is optional
	public void setRemoveUnreferencedNodes(boolean value) {
		_RemoveUnreferencedNodes = value;
		_isSet_RemoveUnreferencedNodes = true;
	}

	public boolean isRemoveUnreferencedNodes() {
		return _RemoveUnreferencedNodes;
	}

	// This attribute is optional
	public void setInputURI(java.lang.String value) {
		_InputURI = value;
	}

	public java.lang.String getInputURI() {
		return _InputURI;
	}

	// This attribute is mandatory
	public void setIndexedPropertyType(java.lang.String value) {
		_IndexedPropertyType = value;
	}

	public java.lang.String getIndexedPropertyType() {
		return _IndexedPropertyType;
	}

	// This attribute is optional
	public void setDoCompile(boolean value) {
		_DoCompile = value;
		_isSet_DoCompile = true;
	}

	public boolean isDoCompile() {
		return _DoCompile;
	}

	// This attribute is optional
	public void setGenerateSwitches(boolean value) {
		_GenerateSwitches = value;
		_isSet_GenerateSwitches = true;
	}

	public boolean isGenerateSwitches() {
		return _GenerateSwitches;
	}

	// This attribute is optional
	public void setDumpBeanTree(java.io.File value) {
		_DumpBeanTree = value;
	}

	public java.io.File getDumpBeanTree() {
		return _DumpBeanTree;
	}

	// This attribute is optional
	public void setGenerateDotGraph(java.io.File value) {
		_GenerateDotGraph = value;
	}

	public java.io.File getGenerateDotGraph() {
		return _GenerateDotGraph;
	}

	// This attribute is optional
	public void setProcessComments(boolean value) {
		_ProcessComments = value;
		_isSet_ProcessComments = true;
	}

	public boolean isProcessComments() {
		return _ProcessComments;
	}

	// This attribute is optional
	public void setProcessDocType(boolean value) {
		_ProcessDocType = value;
		_isSet_ProcessDocType = true;
	}

	public boolean isProcessDocType() {
		return _ProcessDocType;
	}

	// This attribute is optional
	public void setCheckUpToDate(boolean value) {
		_CheckUpToDate = value;
		_isSet_CheckUpToDate = true;
	}

	public boolean isCheckUpToDate() {
		return _CheckUpToDate;
	}

	// This attribute is optional
	public void setGenerateParentRefs(boolean value) {
		_GenerateParentRefs = value;
		_isSet_GenerateParentRefs = true;
	}

	public boolean isGenerateParentRefs() {
		return _GenerateParentRefs;
	}

	// This attribute is optional
	public void setGenerateHasChanged(boolean value) {
		_GenerateHasChanged = value;
		_isSet_GenerateHasChanged = true;
	}

	public boolean isGenerateHasChanged() {
		return _GenerateHasChanged;
	}

	// This attribute is optional
	public void setNewestSourceTime(long value) {
		_NewestSourceTime = value;
		_isSet_NewestSourceTime = true;
	}

	public long getNewestSourceTime() {
		return _NewestSourceTime;
	}

	// This attribute is optional
	public void setWriteBeanGraphFile(java.io.File value) {
		_WriteBeanGraphFile = value;
	}

	public java.io.File getWriteBeanGraphFile() {
		return _WriteBeanGraphFile;
	}

	// This attribute is an array, possibly empty
	public void setReadBeanGraphFiles(java.io.File[] value) {
		if (value == null)
			value = new java.io.File[0];
		_ReadBeanGraphFiles.clear();
		((java.util.ArrayList) _ReadBeanGraphFiles).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_ReadBeanGraphFiles.add(value[i]);
		}
	}

	public void setReadBeanGraphFiles(int index, java.io.File value) {
		_ReadBeanGraphFiles.set(index, value);
	}

	public java.io.File[] getReadBeanGraphFiles() {
		java.io.File[] arr = new java.io.File[_ReadBeanGraphFiles.size()];
		return (java.io.File[]) _ReadBeanGraphFiles.toArray(arr);
	}

	public java.util.List fetchReadBeanGraphFilesList() {
		return _ReadBeanGraphFiles;
	}

	public java.io.File getReadBeanGraphFiles(int index) {
		return (java.io.File)_ReadBeanGraphFiles.get(index);
	}

	// Return the number of readBeanGraphFiles
	public int sizeReadBeanGraphFiles() {
		return _ReadBeanGraphFiles.size();
	}

	public int addReadBeanGraphFiles(java.io.File value) {
		_ReadBeanGraphFiles.add(value);
		int positionOfNewItem = _ReadBeanGraphFiles.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeReadBeanGraphFiles(java.io.File value) {
		int pos = _ReadBeanGraphFiles.indexOf(value);
		if (pos >= 0) {
			_ReadBeanGraphFiles.remove(pos);
		}
		return pos;
	}

	// This attribute is an array, possibly empty
	public void setReadBeanGraphs(org.netbeans.modules.schema2beansdev.beangraph.BeanGraph[] value) {
		if (value == null)
			value = new org.netbeans.modules.schema2beansdev.beangraph.BeanGraph[0];
		_ReadBeanGraphs.clear();
		((java.util.ArrayList) _ReadBeanGraphs).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_ReadBeanGraphs.add(value[i]);
		}
	}

	public void setReadBeanGraphs(int index, org.netbeans.modules.schema2beansdev.beangraph.BeanGraph value) {
		_ReadBeanGraphs.set(index, value);
	}

	public org.netbeans.modules.schema2beansdev.beangraph.BeanGraph[] getReadBeanGraphs() {
		org.netbeans.modules.schema2beansdev.beangraph.BeanGraph[] arr = new org.netbeans.modules.schema2beansdev.beangraph.BeanGraph[_ReadBeanGraphs.size()];
		return (org.netbeans.modules.schema2beansdev.beangraph.BeanGraph[]) _ReadBeanGraphs.toArray(arr);
	}

	public java.util.List fetchReadBeanGraphsList() {
		return _ReadBeanGraphs;
	}

	public org.netbeans.modules.schema2beansdev.beangraph.BeanGraph getReadBeanGraphs(int index) {
		return (org.netbeans.modules.schema2beansdev.beangraph.BeanGraph)_ReadBeanGraphs.get(index);
	}

	// Return the number of readBeanGraphs
	public int sizeReadBeanGraphs() {
		return _ReadBeanGraphs.size();
	}

	public int addReadBeanGraphs(org.netbeans.modules.schema2beansdev.beangraph.BeanGraph value) {
		_ReadBeanGraphs.add(value);
		int positionOfNewItem = _ReadBeanGraphs.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeReadBeanGraphs(org.netbeans.modules.schema2beansdev.beangraph.BeanGraph value) {
		int pos = _ReadBeanGraphs.indexOf(value);
		if (pos >= 0) {
			_ReadBeanGraphs.remove(pos);
		}
		return pos;
	}

	// This attribute is optional
	public void setMinFeatures(boolean value) {
		_MinFeatures = value;
		_isSet_MinFeatures = true;
	}

	public boolean isMinFeatures() {
		return _MinFeatures;
	}

	// This attribute is optional
	public void setForME(boolean value) {
		_ForME = value;
		_isSet_ForME = true;
	}

	public boolean isForME() {
		return _ForME;
	}

	// This attribute is optional
	public void setJava5(boolean value) {
		_Java5 = value;
		_isSet_Java5 = true;
	}

	public boolean isJava5() {
		return _Java5;
	}

	// This attribute is optional
	public void setGenerateTagsFile(boolean value) {
		_GenerateTagsFile = value;
		_isSet_GenerateTagsFile = true;
	}

	public boolean isGenerateTagsFile() {
		return _GenerateTagsFile;
	}

	// This attribute is optional
	public void setCodeGeneratorFactory(org.netbeans.modules.schema2beansdev.CodeGeneratorFactory value) {
		_CodeGeneratorFactory = value;
	}

	public org.netbeans.modules.schema2beansdev.CodeGeneratorFactory getCodeGeneratorFactory() {
		return _CodeGeneratorFactory;
	}

	// This attribute is mandatory
	public void setGenerateTimeStamp(boolean value) {
		_GenerateTimeStamp = value;
		_isSet_GenerateTimeStamp = true;
	}

	public boolean isGenerateTimeStamp() {
		return _GenerateTimeStamp;
	}

	// This attribute is mandatory
	public void setQuiet(boolean value) {
		_Quiet = value;
		_isSet_Quiet = true;
	}

	public boolean isQuiet() {
		return _Quiet;
	}

	// This attribute is optional
	public void setWriteConfig(java.io.File value) {
		_WriteConfig = value;
	}

	public java.io.File getWriteConfig() {
		return _WriteConfig;
	}

	// This attribute is an array, possibly empty
	public void setReadConfig(java.io.File[] value) {
		if (value == null)
			value = new java.io.File[0];
		_ReadConfig.clear();
		((java.util.ArrayList) _ReadConfig).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_ReadConfig.add(value[i]);
		}
	}

	public void setReadConfig(int index, java.io.File value) {
		_ReadConfig.set(index, value);
	}

	public java.io.File[] getReadConfig() {
		java.io.File[] arr = new java.io.File[_ReadConfig.size()];
		return (java.io.File[]) _ReadConfig.toArray(arr);
	}

	public java.util.List fetchReadConfigList() {
		return _ReadConfig;
	}

	public java.io.File getReadConfig(int index) {
		return (java.io.File)_ReadConfig.get(index);
	}

	// Return the number of readConfig
	public int sizeReadConfig() {
		return _ReadConfig.size();
	}

	public int addReadConfig(java.io.File value) {
		_ReadConfig.add(value);
		int positionOfNewItem = _ReadConfig.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeReadConfig(java.io.File value) {
		int pos = _ReadConfig.indexOf(value);
		if (pos >= 0) {
			_ReadConfig.remove(pos);
		}
		return pos;
	}

	// This attribute is mandatory
	public void setMakeDefaults(boolean value) {
		_MakeDefaults = value;
		_isSet_MakeDefaults = true;
	}

	public boolean isMakeDefaults() {
		return _MakeDefaults;
	}

	// This attribute is optional
	public void setSetDefaults(boolean value) {
		_SetDefaults = value;
		_isSet_SetDefaults = true;
	}

	public boolean isSetDefaults() {
		return _SetDefaults;
	}

	// This attribute is optional
	public void setTrimNonStrings(boolean value) {
		_TrimNonStrings = value;
		_isSet_TrimNonStrings = true;
	}

	public boolean isTrimNonStrings() {
		return _TrimNonStrings;
	}

	// This attribute is optional
	public void setUseRuntime(boolean value) {
		_UseRuntime = value;
		_isSet_UseRuntime = true;
	}

	public boolean isUseRuntime() {
		return _UseRuntime;
	}

	// This attribute is optional
	public void setExtendBaseBean(boolean value) {
		_ExtendBaseBean = value;
		_isSet_ExtendBaseBean = true;
	}

	public boolean isExtendBaseBean() {
		return _ExtendBaseBean;
	}

	// This attribute is an array, possibly empty
	public void setFinder(java.lang.String[] value) {
		if (value == null)
			value = new java.lang.String[0];
		_Finder.clear();
		((java.util.ArrayList) _Finder).ensureCapacity(value.length);
		for (int i = 0; i < value.length; ++i) {
			_Finder.add(value[i]);
		}
	}

	public void setFinder(int index, java.lang.String value) {
		_Finder.set(index, value);
	}

	public java.lang.String[] getFinder() {
		java.lang.String[] arr = new java.lang.String[_Finder.size()];
		return (java.lang.String[]) _Finder.toArray(arr);
	}

	public java.util.List fetchFinderList() {
		return _Finder;
	}

	public java.lang.String getFinder(int index) {
		return (java.lang.String)_Finder.get(index);
	}

	// Return the number of finder
	public int sizeFinder() {
		return _Finder.size();
	}

	public int addFinder(java.lang.String value) {
		_Finder.add(value);
		int positionOfNewItem = _Finder.size()-1;
		return positionOfNewItem;
	}

	/**
	 * Search from the end looking for @param value, and then remove it.
	 */
	public int removeFinder(java.lang.String value) {
		int pos = _Finder.indexOf(value);
		if (pos >= 0) {
			_Finder.remove(pos);
		}
		return pos;
	}

	// This attribute is optional
	public void setTarget(java.lang.String value) {
		_Target = value;
	}

	public java.lang.String getTarget() {
		return _Target;
	}

	// This attribute is optional
	public void setStaxProduceXMLEventReader(boolean value) {
		_StaxProduceXMLEventReader = value;
		_isSet_StaxProduceXMLEventReader = true;
	}

	public boolean isStaxProduceXMLEventReader() {
		return _StaxProduceXMLEventReader;
	}

	// This attribute is optional
	public void setStaxUseXMLEventReader(boolean value) {
		_StaxUseXMLEventReader = value;
		_isSet_StaxUseXMLEventReader = true;
	}

	public boolean isStaxUseXMLEventReader() {
		return _StaxUseXMLEventReader;
	}

	// This attribute is optional
	public void setOptionalScalars(boolean value) {
		_OptionalScalars = value;
		_isSet_OptionalScalars = true;
	}

	public boolean isOptionalScalars() {
		return _OptionalScalars;
	}

	// This attribute is optional
	public void setDefaultElementType(java.lang.String value) {
		_DefaultElementType = value;
	}

	public java.lang.String getDefaultElementType() {
		return _DefaultElementType;
	}

	// This attribute is optional
	public void setRespectExtension(boolean value) {
		_RespectExtension = value;
		_isSet_RespectExtension = true;
	}

	public boolean isRespectExtension() {
		return _RespectExtension;
	}

	// This attribute is optional
	public void setLogSuspicious(boolean value) {
		_LogSuspicious = value;
		_isSet_LogSuspicious = true;
	}

	public boolean isLogSuspicious() {
		return _LogSuspicious;
	}

	public void _setSchemaLocation(String location) {
		schemaLocation = location;
	}

	public String _getSchemaLocation() {
		return schemaLocation;
	}

	public void write(java.io.File f) throws java.io.IOException {
		java.io.OutputStream out = new java.io.FileOutputStream(f);
		try {
			write(out);
		} finally {
			out.close();
		}
	}

	public void write(java.io.OutputStream out) throws java.io.IOException {
		write(out, null);
	}

	public void write(java.io.OutputStream out, String encoding) throws java.io.IOException {
		java.io.Writer w;
		if (encoding == null) {
			encoding = "UTF-8";	// NOI18N
		}
		w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(out, encoding));
		write(w, encoding);
		w.flush();
	}

	/**
	 * Print this Java Bean to @param out including an XML header.
	 * @param encoding is the encoding style that @param out was opened with.
	 */
	public void write(java.io.Writer out, String encoding) throws java.io.IOException {
		out.write("<?xml version='1.0'");	// NOI18N
		if (encoding != null)
			out.write(" encoding='"+encoding+"'");	// NOI18N
		out.write(" ?>\n");	// NOI18N
		writeNode(out, "s2bConfig", "");	// NOI18N
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		String myName;
		myName = "s2bConfig";
		writeNode(out, myName, "");	// NOI18N
	}

	public void writeNode(java.io.Writer out, String nodeName, String indent) throws java.io.IOException {
		writeNode(out, nodeName, null, indent, new java.util.HashMap());
	}

	/**
	 * It's not recommended to call this method directly.
	 */
	public void writeNode(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		out.write(indent);
		out.write("<");
		if (namespace != null) {
			out.write((String)namespaceMap.get(namespace));
			out.write(":");
		}
		out.write(nodeName);
		if (schemaLocation != null) {
			namespaceMap.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
			out.write(" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='");
			out.write(schemaLocation);
			out.write("'");	// NOI18N
		}
		writeNodeAttributes(out, nodeName, namespace, indent, namespaceMap);
		out.write(">\n");
		writeNodeChildren(out, nodeName, namespace, indent, namespaceMap);
		out.write(indent);
		out.write("</");
		if (namespace != null) {
			out.write((String)namespaceMap.get(namespace));
			out.write(":");
		}
		out.write(nodeName);
		out.write(">\n");
	}

	protected void writeNodeAttributes(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
	}

	protected void writeNodeChildren(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		String nextIndent = indent + "	";
		if (_SchemaType != null) {
			out.write(nextIndent);
			out.write("<schemaType");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _SchemaType, false);
			out.write("</schemaType>\n");	// NOI18N
		}
		if (_isSet_TraceParse) {
			out.write(nextIndent);
			out.write("<traceParse");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_TraceParse ? "true" : "false");
			out.write("</traceParse>\n");	// NOI18N
		}
		if (_isSet_TraceGen) {
			out.write(nextIndent);
			out.write("<traceGen");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_TraceGen ? "true" : "false");
			out.write("</traceGen>\n");	// NOI18N
		}
		if (_isSet_TraceMisc) {
			out.write(nextIndent);
			out.write("<traceMisc");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_TraceMisc ? "true" : "false");
			out.write("</traceMisc>\n");	// NOI18N
		}
		if (_isSet_TraceDot) {
			out.write(nextIndent);
			out.write("<traceDot");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_TraceDot ? "true" : "false");
			out.write("</traceDot>\n");	// NOI18N
		}
		if (_Filename != null) {
			out.write(nextIndent);
			out.write("<filename");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _Filename.toString(), false);
			out.write("</filename>\n");	// NOI18N
		}
		if (_FileIn != null) {
			out.write(nextIndent);
			out.write("<fileIn");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _FileIn.toString(), false);
			out.write("</fileIn>\n");	// NOI18N
		}
		if (_DocRoot != null) {
			out.write(nextIndent);
			out.write("<docRoot");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _DocRoot, false);
			out.write("</docRoot>\n");	// NOI18N
		}
		if (_RootDir != null) {
			out.write(nextIndent);
			out.write("<rootDir");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _RootDir.toString(), false);
			out.write("</rootDir>\n");	// NOI18N
		}
		if (_PackagePath != null) {
			out.write(nextIndent);
			out.write("<packagePath");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _PackagePath, false);
			out.write("</packagePath>\n");	// NOI18N
		}
		if (_Indent != null) {
			out.write(nextIndent);
			out.write("<indent");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _Indent, false);
			out.write("</indent>\n");	// NOI18N
		}
		if (_isSet_IndentAmount) {
			out.write(nextIndent);
			out.write("<indentAmount");	// NOI18N
			out.write(">");	// NOI18N
			out.write(""+_IndentAmount);
			out.write("</indentAmount>\n");	// NOI18N
		}
		if (_MddFile != null) {
			out.write(nextIndent);
			out.write("<mddFile");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _MddFile.toString(), false);
			out.write("</mddFile>\n");	// NOI18N
		}
		if (_MddIn != null) {
			out.write(nextIndent);
			out.write("<mddIn");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _MddIn.toString(), false);
			out.write("</mddIn>\n");	// NOI18N
		}
		if (_MetaDD != null) {
			out.write(nextIndent);
			out.write("<metaDD");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _MetaDD.toString(), false);
			out.write("</metaDD>\n");	// NOI18N
		}
		if (_isSet_DoGeneration) {
			out.write(nextIndent);
			out.write("<doGeneration");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_DoGeneration ? "true" : "false");
			out.write("</doGeneration>\n");	// NOI18N
		}
		if (_isSet_ScalarException) {
			out.write(nextIndent);
			out.write("<scalarException");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_ScalarException ? "true" : "false");
			out.write("</scalarException>\n");	// NOI18N
		}
		if (_isSet_DumpToString) {
			out.write(nextIndent);
			out.write("<dumpToString");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_DumpToString ? "true" : "false");
			out.write("</dumpToString>\n");	// NOI18N
		}
		if (_isSet_Vetoable) {
			out.write(nextIndent);
			out.write("<vetoable");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_Vetoable ? "true" : "false");
			out.write("</vetoable>\n");	// NOI18N
		}
		if (_isSet_Standalone) {
			out.write(nextIndent);
			out.write("<standalone");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_Standalone ? "true" : "false");
			out.write("</standalone>\n");	// NOI18N
		}
		if (_isSet_Auto) {
			out.write(nextIndent);
			out.write("<auto");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_Auto ? "true" : "false");
			out.write("</auto>\n");	// NOI18N
		}
		if (_MessageOut != null) {
			out.write(nextIndent);
			out.write("<messageOut");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _MessageOut.toString(), false);
			out.write("</messageOut>\n");	// NOI18N
		}
		if (_OutputStreamProvider != null) {
			out.write(nextIndent);
			out.write("<outputStreamProvider");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _OutputStreamProvider.toString(), false);
			out.write("</outputStreamProvider>\n");	// NOI18N
		}
		if (_isSet_ThrowErrors) {
			out.write(nextIndent);
			out.write("<throwErrors");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_ThrowErrors ? "true" : "false");
			out.write("</throwErrors>\n");	// NOI18N
		}
		if (_isSet_GenerateXMLIO) {
			out.write(nextIndent);
			out.write("<generateXMLIO");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_GenerateXMLIO ? "true" : "false");
			out.write("</generateXMLIO>\n");	// NOI18N
		}
		if (_isSet_GenerateValidate) {
			out.write(nextIndent);
			out.write("<generateValidate");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_GenerateValidate ? "true" : "false");
			out.write("</generateValidate>\n");	// NOI18N
		}
		if (_isSet_GeneratePropertyEvents) {
			out.write(nextIndent);
			out.write("<generatePropertyEvents");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_GeneratePropertyEvents ? "true" : "false");
			out.write("</generatePropertyEvents>\n");	// NOI18N
		}
		if (_isSet_GenerateStoreEvents) {
			out.write(nextIndent);
			out.write("<generateStoreEvents");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_GenerateStoreEvents ? "true" : "false");
			out.write("</generateStoreEvents>\n");	// NOI18N
		}
		if (_isSet_GenerateTransactions) {
			out.write(nextIndent);
			out.write("<generateTransactions");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_GenerateTransactions ? "true" : "false");
			out.write("</generateTransactions>\n");	// NOI18N
		}
		if (_isSet_AttributesAsProperties) {
			out.write(nextIndent);
			out.write("<attributesAsProperties");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_AttributesAsProperties ? "true" : "false");
			out.write("</attributesAsProperties>\n");	// NOI18N
		}
		if (_isSet_GenerateDelegator) {
			out.write(nextIndent);
			out.write("<generateDelegator");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_GenerateDelegator ? "true" : "false");
			out.write("</generateDelegator>\n");	// NOI18N
		}
		if (_DelegateDir != null) {
			out.write(nextIndent);
			out.write("<delegateDir");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _DelegateDir.toString(), false);
			out.write("</delegateDir>\n");	// NOI18N
		}
		if (_DelegatePackage != null) {
			out.write(nextIndent);
			out.write("<delegatePackage");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _DelegatePackage, false);
			out.write("</delegatePackage>\n");	// NOI18N
		}
		if (_GenerateCommonInterface != null) {
			out.write(nextIndent);
			out.write("<generateCommonInterface");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _GenerateCommonInterface, false);
			out.write("</generateCommonInterface>\n");	// NOI18N
		}
		if (_isSet_DefaultsAccessable) {
			out.write(nextIndent);
			out.write("<defaultsAccessable");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_DefaultsAccessable ? "true" : "false");
			out.write("</defaultsAccessable>\n");	// NOI18N
		}
		if (_isSet_UseInterfaces) {
			out.write(nextIndent);
			out.write("<useInterfaces");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_UseInterfaces ? "true" : "false");
			out.write("</useInterfaces>\n");	// NOI18N
		}
		if (_isSet_GenerateInterfaces) {
			out.write(nextIndent);
			out.write("<generateInterfaces");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_GenerateInterfaces ? "true" : "false");
			out.write("</generateInterfaces>\n");	// NOI18N
		}
		if (_isSet_KeepElementPositions) {
			out.write(nextIndent);
			out.write("<keepElementPositions");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_KeepElementPositions ? "true" : "false");
			out.write("</keepElementPositions>\n");	// NOI18N
		}
		if (_isSet_RemoveUnreferencedNodes) {
			out.write(nextIndent);
			out.write("<removeUnreferencedNodes");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_RemoveUnreferencedNodes ? "true" : "false");
			out.write("</removeUnreferencedNodes>\n");	// NOI18N
		}
		if (_InputURI != null) {
			out.write(nextIndent);
			out.write("<inputURI");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _InputURI, false);
			out.write("</inputURI>\n");	// NOI18N
		}
		if (_IndexedPropertyType != null) {
			out.write(nextIndent);
			out.write("<indexedPropertyType");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _IndexedPropertyType, false);
			out.write("</indexedPropertyType>\n");	// NOI18N
		}
		if (_isSet_DoCompile) {
			out.write(nextIndent);
			out.write("<doCompile");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_DoCompile ? "true" : "false");
			out.write("</doCompile>\n");	// NOI18N
		}
		if (_isSet_GenerateSwitches) {
			out.write(nextIndent);
			out.write("<generateSwitches");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_GenerateSwitches ? "true" : "false");
			out.write("</generateSwitches>\n");	// NOI18N
		}
		if (_DumpBeanTree != null) {
			out.write(nextIndent);
			out.write("<dumpBeanTree");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _DumpBeanTree.toString(), false);
			out.write("</dumpBeanTree>\n");	// NOI18N
		}
		if (_GenerateDotGraph != null) {
			out.write(nextIndent);
			out.write("<generateDotGraph");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _GenerateDotGraph.toString(), false);
			out.write("</generateDotGraph>\n");	// NOI18N
		}
		if (_isSet_ProcessComments) {
			out.write(nextIndent);
			out.write("<processComments");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_ProcessComments ? "true" : "false");
			out.write("</processComments>\n");	// NOI18N
		}
		if (_isSet_ProcessDocType) {
			out.write(nextIndent);
			out.write("<processDocType");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_ProcessDocType ? "true" : "false");
			out.write("</processDocType>\n");	// NOI18N
		}
		if (_isSet_CheckUpToDate) {
			out.write(nextIndent);
			out.write("<checkUpToDate");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_CheckUpToDate ? "true" : "false");
			out.write("</checkUpToDate>\n");	// NOI18N
		}
		if (_isSet_GenerateParentRefs) {
			out.write(nextIndent);
			out.write("<generateParentRefs");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_GenerateParentRefs ? "true" : "false");
			out.write("</generateParentRefs>\n");	// NOI18N
		}
		if (_isSet_GenerateHasChanged) {
			out.write(nextIndent);
			out.write("<generateHasChanged");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_GenerateHasChanged ? "true" : "false");
			out.write("</generateHasChanged>\n");	// NOI18N
		}
		if (_isSet_NewestSourceTime) {
			out.write(nextIndent);
			out.write("<newestSourceTime");	// NOI18N
			out.write(">");	// NOI18N
			out.write(""+_NewestSourceTime);
			out.write("</newestSourceTime>\n");	// NOI18N
		}
		if (_WriteBeanGraphFile != null) {
			out.write(nextIndent);
			out.write("<writeBeanGraphFile");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _WriteBeanGraphFile.toString(), false);
			out.write("</writeBeanGraphFile>\n");	// NOI18N
		}
		for (java.util.Iterator it = _ReadBeanGraphFiles.iterator(); 
			it.hasNext(); ) {
			java.io.File element = (java.io.File)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<readBeanGraphFiles");	// NOI18N
				out.write(">");	// NOI18N
				org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, element.toString(), false);
				out.write("</readBeanGraphFiles>\n");	// NOI18N
			}
		}
		for (java.util.Iterator it = _ReadBeanGraphs.iterator(); 
			it.hasNext(); ) {
			org.netbeans.modules.schema2beansdev.beangraph.BeanGraph element = (org.netbeans.modules.schema2beansdev.beangraph.BeanGraph)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<readBeanGraphs");	// NOI18N
				out.write(">");	// NOI18N
				org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, element.toString(), false);
				out.write("</readBeanGraphs>\n");	// NOI18N
			}
		}
		if (_isSet_MinFeatures) {
			out.write(nextIndent);
			out.write("<minFeatures");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_MinFeatures ? "true" : "false");
			out.write("</minFeatures>\n");	// NOI18N
		}
		if (_isSet_ForME) {
			out.write(nextIndent);
			out.write("<forME");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_ForME ? "true" : "false");
			out.write("</forME>\n");	// NOI18N
		}
		if (_isSet_Java5) {
			out.write(nextIndent);
			out.write("<java5");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_Java5 ? "true" : "false");
			out.write("</java5>\n");	// NOI18N
		}
		if (_isSet_GenerateTagsFile) {
			out.write(nextIndent);
			out.write("<generateTagsFile");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_GenerateTagsFile ? "true" : "false");
			out.write("</generateTagsFile>\n");	// NOI18N
		}
		if (_CodeGeneratorFactory != null) {
			out.write(nextIndent);
			out.write("<codeGeneratorFactory");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _CodeGeneratorFactory.toString(), false);
			out.write("</codeGeneratorFactory>\n");	// NOI18N
		}
		if (_isSet_GenerateTimeStamp) {
			out.write(nextIndent);
			out.write("<generateTimeStamp");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_GenerateTimeStamp ? "true" : "false");
			out.write("</generateTimeStamp>\n");	// NOI18N
		}
		if (_isSet_Quiet) {
			out.write(nextIndent);
			out.write("<quiet");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_Quiet ? "true" : "false");
			out.write("</quiet>\n");	// NOI18N
		}
		if (_WriteConfig != null) {
			out.write(nextIndent);
			out.write("<writeConfig");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _WriteConfig.toString(), false);
			out.write("</writeConfig>\n");	// NOI18N
		}
		for (java.util.Iterator it = _ReadConfig.iterator(); it.hasNext(); 
			) {
			java.io.File element = (java.io.File)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<readConfig");	// NOI18N
				out.write(">");	// NOI18N
				org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, element.toString(), false);
				out.write("</readConfig>\n");	// NOI18N
			}
		}
		if (_isSet_MakeDefaults) {
			out.write(nextIndent);
			out.write("<makeDefaults");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_MakeDefaults ? "true" : "false");
			out.write("</makeDefaults>\n");	// NOI18N
		}
		if (_isSet_SetDefaults) {
			out.write(nextIndent);
			out.write("<setDefaults");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_SetDefaults ? "true" : "false");
			out.write("</setDefaults>\n");	// NOI18N
		}
		if (_isSet_TrimNonStrings) {
			out.write(nextIndent);
			out.write("<trimNonStrings");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_TrimNonStrings ? "true" : "false");
			out.write("</trimNonStrings>\n");	// NOI18N
		}
		if (_isSet_UseRuntime) {
			out.write(nextIndent);
			out.write("<useRuntime");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_UseRuntime ? "true" : "false");
			out.write("</useRuntime>\n");	// NOI18N
		}
		if (_isSet_ExtendBaseBean) {
			out.write(nextIndent);
			out.write("<extendBaseBean");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_ExtendBaseBean ? "true" : "false");
			out.write("</extendBaseBean>\n");	// NOI18N
		}
		for (java.util.Iterator it = _Finder.iterator(); it.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			if (element != null) {
				out.write(nextIndent);
				out.write("<finder");	// NOI18N
				out.write(">");	// NOI18N
				org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, element, false);
				out.write("</finder>\n");	// NOI18N
			}
		}
		if (_Target != null) {
			out.write(nextIndent);
			out.write("<target");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _Target, false);
			out.write("</target>\n");	// NOI18N
		}
		if (_isSet_StaxProduceXMLEventReader) {
			out.write(nextIndent);
			out.write("<staxProduceXMLEventReader");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_StaxProduceXMLEventReader ? "true" : "false");
			out.write("</staxProduceXMLEventReader>\n");	// NOI18N
		}
		if (_isSet_StaxUseXMLEventReader) {
			out.write(nextIndent);
			out.write("<staxUseXMLEventReader");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_StaxUseXMLEventReader ? "true" : "false");
			out.write("</staxUseXMLEventReader>\n");	// NOI18N
		}
		if (_isSet_OptionalScalars) {
			out.write(nextIndent);
			out.write("<optionalScalars");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_OptionalScalars ? "true" : "false");
			out.write("</optionalScalars>\n");	// NOI18N
		}
		if (_DefaultElementType != null) {
			out.write(nextIndent);
			out.write("<defaultElementType");	// NOI18N
			out.write(">");	// NOI18N
			org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out, _DefaultElementType, false);
			out.write("</defaultElementType>\n");	// NOI18N
		}
		if (_isSet_RespectExtension) {
			out.write(nextIndent);
			out.write("<respectExtension");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_RespectExtension ? "true" : "false");
			out.write("</respectExtension>\n");	// NOI18N
		}
		if (_isSet_LogSuspicious) {
			out.write(nextIndent);
			out.write("<logSuspicious");	// NOI18N
			out.write(">");	// NOI18N
			out.write(_LogSuspicious ? "true" : "false");
			out.write("</logSuspicious>\n");	// NOI18N
		}
	}

	public static S2bConfig read(java.io.File f) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(f);
		try {
			return read(in);
		} finally {
			in.close();
		}
	}

	public static S2bConfig read(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false, null, null);
	}

	/**
	 * Warning: in readNoEntityResolver character and entity references will
	 * not be read from any DTD in the XML source.
	 * However, this way is faster since no DTDs are looked up
	 * (possibly skipping network access) or parsed.
	 */
	public static S2bConfig readNoEntityResolver(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return read(new org.xml.sax.InputSource(in), false,
			new org.xml.sax.EntityResolver() {
			public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
				java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(new byte[0]);
				return new org.xml.sax.InputSource(bin);
			}
		}
			, null);
	}

	public static S2bConfig read(org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		dbf.setValidating(validate);
		dbf.setNamespaceAware(true);
		javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
		if (er != null)	db.setEntityResolver(er);
		if (eh != null)	db.setErrorHandler(eh);
		org.w3c.dom.Document doc = db.parse(in);
		return read(doc);
	}

	public static S2bConfig read(org.w3c.dom.Document document) {
		S2bConfig aS2bConfig = new S2bConfig();
		aS2bConfig.readFromDocument(document);
		return aS2bConfig;
	}

	protected void readFromDocument(org.w3c.dom.Document document) {
		readNode(document.getDocumentElement());
	}

	protected static class ReadState {
		int lastElementType;
		int elementPosition;
	}

	public void readNode(org.w3c.dom.Node node) {
		readNode(node, new java.util.HashMap());
	}

	public void readNode(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
		if (node.hasAttributes()) {
			org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
			org.w3c.dom.Attr attr;
			java.lang.String attrValue;
			boolean firstNamespaceDef = true;
			for (int attrNum = 0; attrNum < attrs.getLength(); ++attrNum) {
				attr = (org.w3c.dom.Attr) attrs.item(attrNum);
				String attrName = attr.getName();
				if (attrName.startsWith("xmlns:")) {
					if (firstNamespaceDef) {
						firstNamespaceDef = false;
						// Dup prefix map, so as to not write over previous values, and to make it easy to clear out our entries.
						namespacePrefixes = new java.util.HashMap(namespacePrefixes);
					}
					String attrNSPrefix = attrName.substring(6);
					namespacePrefixes.put(attrNSPrefix, attr.getValue());
				}
			}
			String xsiPrefix = "xsi";
			for (java.util.Iterator it = namespacePrefixes.entrySet().iterator(); 
				it.hasNext(); ) {
				java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
				String prefix = (String) entry.getKey();
				String ns = (String) entry.getValue();
				if ("http://www.w3.org/2001/XMLSchema-instance".equals(ns)) {
					xsiPrefix = prefix;
					break;
				}
			}
			attr = (org.w3c.dom.Attr) attrs.getNamedItem(""+xsiPrefix+":schemaLocation");
			if (attr != null) {
				attrValue = attr.getValue();
				schemaLocation = attrValue;
			}
			readNodeAttributes(node, namespacePrefixes, attrs);
		}
		readNodeChildren(node, namespacePrefixes);
	}

	protected void readNodeAttributes(org.w3c.dom.Node node, java.util.Map namespacePrefixes, org.w3c.dom.NamedNodeMap attrs) {
		org.w3c.dom.Attr attr;
		java.lang.String attrValue;
	}

	protected void readNodeChildren(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
		org.w3c.dom.NodeList children = node.getChildNodes();
		for (int i = 0, size = children.getLength(); i < size; ++i) {
			org.w3c.dom.Node childNode = children.item(i);
			if (!(childNode instanceof org.w3c.dom.Element)) {
				continue;
			}
			String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
			String childNodeValue = "";
			if (childNode.getFirstChild() != null) {
				childNodeValue = childNode.getFirstChild().getNodeValue();
			}
			boolean recognized = readNodeChild(childNode, childNodeName, childNodeValue, namespacePrefixes);
			if (!recognized) {
				if (childNode instanceof org.w3c.dom.Element) {
					_logger.info("Found extra unrecognized childNode '"+childNodeName+"'");
				}
			}
		}
	}

	protected boolean readNodeChild(org.w3c.dom.Node childNode, String childNodeName, String childNodeValue, java.util.Map namespacePrefixes) {
		// assert childNodeName == childNodeName.intern()
		if ("schemaType".equals(childNodeName)) {
			_SchemaType = childNodeValue;
		}
		else if ("traceParse".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_TraceParse = true;
			else
				_TraceParse = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_TraceParse = true;
		}
		else if ("traceGen".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_TraceGen = true;
			else
				_TraceGen = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_TraceGen = true;
		}
		else if ("traceMisc".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_TraceMisc = true;
			else
				_TraceMisc = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_TraceMisc = true;
		}
		else if ("traceDot".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_TraceDot = true;
			else
				_TraceDot = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_TraceDot = true;
		}
		else if ("filename".equals(childNodeName)) {
			_Filename = new java.io.File(childNodeValue);
		}
		else if ("fileIn".equals(childNodeName)) {
			// Don't know how to create a java.io.InputStream
		}
		else if ("docRoot".equals(childNodeName)) {
			_DocRoot = childNodeValue;
		}
		else if ("rootDir".equals(childNodeName)) {
			_RootDir = new java.io.File(childNodeValue);
		}
		else if ("packagePath".equals(childNodeName)) {
			_PackagePath = childNodeValue;
		}
		else if ("indent".equals(childNodeName)) {
			_Indent = childNodeValue;
		}
		else if ("indentAmount".equals(childNodeName)) {
			_IndentAmount = Integer.parseInt(childNodeValue);
			_isSet_IndentAmount = true;
		}
		else if ("mddFile".equals(childNodeName)) {
			_MddFile = new java.io.File(childNodeValue);
		}
		else if ("mddIn".equals(childNodeName)) {
			// Don't know how to create a java.io.InputStream
		}
		else if ("metaDD".equals(childNodeName)) {
			// Don't know how to create a org.netbeans.modules.schema2beansdev.metadd.MetaDD
		}
		else if ("doGeneration".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_DoGeneration = true;
			else
				_DoGeneration = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_DoGeneration = true;
		}
		else if ("scalarException".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_ScalarException = true;
			else
				_ScalarException = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_ScalarException = true;
		}
		else if ("dumpToString".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_DumpToString = true;
			else
				_DumpToString = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_DumpToString = true;
		}
		else if ("vetoable".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_Vetoable = true;
			else
				_Vetoable = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_Vetoable = true;
		}
		else if ("standalone".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_Standalone = true;
			else
				_Standalone = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_Standalone = true;
		}
		else if ("auto".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_Auto = true;
			else
				_Auto = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_Auto = true;
		}
		else if ("messageOut".equals(childNodeName)) {
			// Don't know how to create a java.io.PrintStream
		}
		else if ("outputStreamProvider".equals(childNodeName)) {
			// Don't know how to create a org.netbeans.modules.schema2beansdev.GenBeans.OutputStreamProvider
		}
		else if ("throwErrors".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_ThrowErrors = true;
			else
				_ThrowErrors = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_ThrowErrors = true;
		}
		else if ("generateXMLIO".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_GenerateXMLIO = true;
			else
				_GenerateXMLIO = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_GenerateXMLIO = true;
		}
		else if ("generateValidate".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_GenerateValidate = true;
			else
				_GenerateValidate = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_GenerateValidate = true;
		}
		else if ("generatePropertyEvents".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_GeneratePropertyEvents = true;
			else
				_GeneratePropertyEvents = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_GeneratePropertyEvents = true;
		}
		else if ("generateStoreEvents".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_GenerateStoreEvents = true;
			else
				_GenerateStoreEvents = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_GenerateStoreEvents = true;
		}
		else if ("generateTransactions".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_GenerateTransactions = true;
			else
				_GenerateTransactions = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_GenerateTransactions = true;
		}
		else if ("attributesAsProperties".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_AttributesAsProperties = true;
			else
				_AttributesAsProperties = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_AttributesAsProperties = true;
		}
		else if ("generateDelegator".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_GenerateDelegator = true;
			else
				_GenerateDelegator = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_GenerateDelegator = true;
		}
		else if ("delegateDir".equals(childNodeName)) {
			_DelegateDir = new java.io.File(childNodeValue);
		}
		else if ("delegatePackage".equals(childNodeName)) {
			_DelegatePackage = childNodeValue;
		}
		else if ("generateCommonInterface".equals(childNodeName)) {
			_GenerateCommonInterface = childNodeValue;
		}
		else if ("defaultsAccessable".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_DefaultsAccessable = true;
			else
				_DefaultsAccessable = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_DefaultsAccessable = true;
		}
		else if ("useInterfaces".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_UseInterfaces = true;
			else
				_UseInterfaces = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_UseInterfaces = true;
		}
		else if ("generateInterfaces".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_GenerateInterfaces = true;
			else
				_GenerateInterfaces = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_GenerateInterfaces = true;
		}
		else if ("keepElementPositions".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_KeepElementPositions = true;
			else
				_KeepElementPositions = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_KeepElementPositions = true;
		}
		else if ("removeUnreferencedNodes".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_RemoveUnreferencedNodes = true;
			else
				_RemoveUnreferencedNodes = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_RemoveUnreferencedNodes = true;
		}
		else if ("inputURI".equals(childNodeName)) {
			_InputURI = childNodeValue;
		}
		else if ("indexedPropertyType".equals(childNodeName)) {
			_IndexedPropertyType = childNodeValue;
		}
		else if ("doCompile".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_DoCompile = true;
			else
				_DoCompile = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_DoCompile = true;
		}
		else if ("generateSwitches".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_GenerateSwitches = true;
			else
				_GenerateSwitches = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_GenerateSwitches = true;
		}
		else if ("dumpBeanTree".equals(childNodeName)) {
			_DumpBeanTree = new java.io.File(childNodeValue);
		}
		else if ("generateDotGraph".equals(childNodeName)) {
			_GenerateDotGraph = new java.io.File(childNodeValue);
		}
		else if ("processComments".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_ProcessComments = true;
			else
				_ProcessComments = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_ProcessComments = true;
		}
		else if ("processDocType".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_ProcessDocType = true;
			else
				_ProcessDocType = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_ProcessDocType = true;
		}
		else if ("checkUpToDate".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_CheckUpToDate = true;
			else
				_CheckUpToDate = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_CheckUpToDate = true;
		}
		else if ("generateParentRefs".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_GenerateParentRefs = true;
			else
				_GenerateParentRefs = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_GenerateParentRefs = true;
		}
		else if ("generateHasChanged".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_GenerateHasChanged = true;
			else
				_GenerateHasChanged = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_GenerateHasChanged = true;
		}
		else if ("newestSourceTime".equals(childNodeName)) {
			_NewestSourceTime = Long.parseLong(childNodeValue);
			_isSet_NewestSourceTime = true;
		}
		else if ("writeBeanGraphFile".equals(childNodeName)) {
			_WriteBeanGraphFile = new java.io.File(childNodeValue);
		}
		else if ("readBeanGraphFiles".equals(childNodeName)) {
			java.io.File aReadBeanGraphFiles;
			aReadBeanGraphFiles = new java.io.File(childNodeValue);
			_ReadBeanGraphFiles.add(aReadBeanGraphFiles);
		}
		else if ("readBeanGraphs".equals(childNodeName)) {
			org.netbeans.modules.schema2beansdev.beangraph.BeanGraph aReadBeanGraphs;
			// Don't know how to create a org.netbeans.modules.schema2beansdev.beangraph.BeanGraph
		}
		else if ("minFeatures".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_MinFeatures = true;
			else
				_MinFeatures = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_MinFeatures = true;
		}
		else if ("forME".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_ForME = true;
			else
				_ForME = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_ForME = true;
		}
		else if ("java5".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_Java5 = true;
			else
				_Java5 = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_Java5 = true;
		}
		else if ("generateTagsFile".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_GenerateTagsFile = true;
			else
				_GenerateTagsFile = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_GenerateTagsFile = true;
		}
		else if ("codeGeneratorFactory".equals(childNodeName)) {
			// Don't know how to create a org.netbeans.modules.schema2beansdev.CodeGeneratorFactory
		}
		else if ("generateTimeStamp".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_GenerateTimeStamp = true;
			else
				_GenerateTimeStamp = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_GenerateTimeStamp = true;
		}
		else if ("quiet".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_Quiet = true;
			else
				_Quiet = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_Quiet = true;
		}
		else if ("writeConfig".equals(childNodeName)) {
			_WriteConfig = new java.io.File(childNodeValue);
		}
		else if ("readConfig".equals(childNodeName)) {
			java.io.File aReadConfig;
			aReadConfig = new java.io.File(childNodeValue);
			_ReadConfig.add(aReadConfig);
		}
		else if ("makeDefaults".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_MakeDefaults = true;
			else
				_MakeDefaults = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_MakeDefaults = true;
		}
		else if ("setDefaults".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_SetDefaults = true;
			else
				_SetDefaults = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_SetDefaults = true;
		}
		else if ("trimNonStrings".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_TrimNonStrings = true;
			else
				_TrimNonStrings = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_TrimNonStrings = true;
		}
		else if ("useRuntime".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_UseRuntime = true;
			else
				_UseRuntime = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_UseRuntime = true;
		}
		else if ("extendBaseBean".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_ExtendBaseBean = true;
			else
				_ExtendBaseBean = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_ExtendBaseBean = true;
		}
		else if ("finder".equals(childNodeName)) {
			java.lang.String aFinder;
			aFinder = childNodeValue;
			_Finder.add(aFinder);
		}
		else if ("target".equals(childNodeName)) {
			_Target = childNodeValue;
		}
		else if ("staxProduceXMLEventReader".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_StaxProduceXMLEventReader = true;
			else
				_StaxProduceXMLEventReader = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_StaxProduceXMLEventReader = true;
		}
		else if ("staxUseXMLEventReader".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_StaxUseXMLEventReader = true;
			else
				_StaxUseXMLEventReader = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_StaxUseXMLEventReader = true;
		}
		else if ("optionalScalars".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_OptionalScalars = true;
			else
				_OptionalScalars = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_OptionalScalars = true;
		}
		else if ("defaultElementType".equals(childNodeName)) {
			_DefaultElementType = childNodeValue;
		}
		else if ("respectExtension".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_RespectExtension = true;
			else
				_RespectExtension = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_RespectExtension = true;
		}
		else if ("logSuspicious".equals(childNodeName)) {
			if (childNode.getFirstChild() == null)
				_LogSuspicious = true;
			else
				_LogSuspicious = ("true".equalsIgnoreCase(childNodeValue) || "1".equals(childNodeValue));
			_isSet_LogSuspicious = true;
		}
		else {
			return false;
		}
		return true;
	}

	/**
	 * Takes some text to be printed into an XML stream and escapes any
	 * characters that might make it invalid XML (like '<').
	 */
	public static void writeXML(java.io.Writer out, String msg) throws java.io.IOException {
		writeXML(out, msg, true);
	}

	public static void writeXML(java.io.Writer out, String msg, boolean attribute) throws java.io.IOException {
		if (msg == null)
			return;
		int msgLength = msg.length();
		for (int i = 0; i < msgLength; ++i) {
			char c = msg.charAt(i);
			writeXML(out, c, attribute);
		}
	}

	public static void writeXML(java.io.Writer out, char msg, boolean attribute) throws java.io.IOException {
		if (msg == '&')
			out.write("&amp;");
		else if (msg == '<')
			out.write("&lt;");
		else if (msg == '>')
			out.write("&gt;");
		else if (attribute) {
			if (msg == '"')
				out.write("&quot;");
			else if (msg == '\'')
				out.write("&apos;");
			else if (msg == '\n')
				out.write("&#xA;");
			else if (msg == '\t')
				out.write("&#x9;");
			else
				out.write(msg);
		}
		else
			out.write(msg);
	}

	public static class ValidateException extends Exception {
		private java.lang.Object failedBean;
		private String failedPropertyName;
		private FailureType failureType;
		public ValidateException(String msg, String failedPropertyName, java.lang.Object failedBean) {
			super(msg);
			this.failedBean = failedBean;
			this.failedPropertyName = failedPropertyName;
		}
		public ValidateException(String msg, FailureType ft, String failedPropertyName, java.lang.Object failedBean) {
			super(msg);
			this.failureType = ft;
			this.failedBean = failedBean;
			this.failedPropertyName = failedPropertyName;
		}
		public String getFailedPropertyName() {return failedPropertyName;}
		public FailureType getFailureType() {return failureType;}
		public java.lang.Object getFailedBean() {return failedBean;}
		public static class FailureType {
			private final String name;
			private FailureType(String name) {this.name = name;}
			public String toString() { return name;}
			public static final FailureType NULL_VALUE = new FailureType("NULL_VALUE");
			public static final FailureType DATA_RESTRICTION = new FailureType("DATA_RESTRICTION");
			public static final FailureType ENUM_RESTRICTION = new FailureType("ENUM_RESTRICTION");
			public static final FailureType ALL_RESTRICTIONS = new FailureType("ALL_RESTRICTIONS");
			public static final FailureType MUTUALLY_EXCLUSIVE = new FailureType("MUTUALLY_EXCLUSIVE");
		}
	}

	public void validate() throws org.netbeans.modules.schema2beansdev.S2bConfig.ValidateException {
		boolean restrictionFailure = false;
		boolean restrictionPassed = false;
		// Validating property schemaType
		if (getSchemaType() == null) {
			throw new org.netbeans.modules.schema2beansdev.S2bConfig.ValidateException("getSchemaType() == null", org.netbeans.modules.schema2beansdev.S2bConfig.ValidateException.FailureType.NULL_VALUE, "schemaType", this);	// NOI18N
		}
		// Validating property traceParse
		// Validating property traceGen
		// Validating property traceMisc
		// Validating property traceDot
		// Validating property filename
		// Validating property fileIn
		// Validating property docRoot
		// Validating property rootDir
		if (getRootDir() == null) {
			throw new org.netbeans.modules.schema2beansdev.S2bConfig.ValidateException("getRootDir() == null", org.netbeans.modules.schema2beansdev.S2bConfig.ValidateException.FailureType.NULL_VALUE, "rootDir", this);	// NOI18N
		}
		// Validating property packagePath
		// Validating property indent
		if (getIndent() == null) {
			throw new org.netbeans.modules.schema2beansdev.S2bConfig.ValidateException("getIndent() == null", org.netbeans.modules.schema2beansdev.S2bConfig.ValidateException.FailureType.NULL_VALUE, "indent", this);	// NOI18N
		}
		// Validating property indentAmount
		// Validating property mddFile
		// Validating property mddIn
		// Validating property metaDD
		// Validating property doGeneration
		// Validating property scalarException
		// Validating property dumpToString
		// Validating property vetoable
		// Validating property standalone
		// Validating property auto
		// Validating property messageOut
		// Validating property outputStreamProvider
		// Validating property throwErrors
		// Validating property generateXMLIO
		// Validating property generateValidate
		// Validating property generatePropertyEvents
		// Validating property generateStoreEvents
		// Validating property generateTransactions
		// Validating property attributesAsProperties
		// Validating property generateDelegator
		// Validating property delegateDir
		// Validating property delegatePackage
		// Validating property generateCommonInterface
		// Validating property defaultsAccessable
		// Validating property useInterfaces
		// Validating property generateInterfaces
		// Validating property keepElementPositions
		// Validating property removeUnreferencedNodes
		// Validating property inputURI
		// Validating property indexedPropertyType
		if (getIndexedPropertyType() == null) {
			throw new org.netbeans.modules.schema2beansdev.S2bConfig.ValidateException("getIndexedPropertyType() == null", org.netbeans.modules.schema2beansdev.S2bConfig.ValidateException.FailureType.NULL_VALUE, "indexedPropertyType", this);	// NOI18N
		}
		// Validating property doCompile
		// Validating property generateSwitches
		// Validating property dumpBeanTree
		// Validating property generateDotGraph
		// Validating property processComments
		// Validating property processDocType
		// Validating property checkUpToDate
		// Validating property generateParentRefs
		// Validating property generateHasChanged
		// Validating property newestSourceTime
		// Validating property writeBeanGraphFile
		// Validating property readBeanGraphFiles
		// Validating property readBeanGraphs
		// Validating property minFeatures
		// Validating property forME
		// Validating property java5
		// Validating property generateTagsFile
		// Validating property codeGeneratorFactory
		// Validating property generateTimeStamp
		// Validating property quiet
		// Validating property writeConfig
		// Validating property readConfig
		// Validating property makeDefaults
		// Validating property setDefaults
		// Validating property trimNonStrings
		// Validating property useRuntime
		// Validating property extendBaseBean
		// Validating property finder
		// Validating property target
		// Validating property staxProduceXMLEventReader
		// Validating property staxUseXMLEventReader
		// Validating property optionalScalars
		// Validating property defaultElementType
		// Validating property respectExtension
		// Validating property logSuspicious
	}

	/**
	 * @return true if error, then should display help
	 */
	public boolean parseArguments(String[] args) {
		for (int argNum = 0, size = args.length; argNum < size; ++argNum) {
			String arg = args[argNum].toLowerCase().intern();
			if ("-f".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				setFilename(new java.io.File(args[++argNum]));
				continue;
			}
			if ("-d".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				setDocRoot(args[++argNum]);
				continue;
			}
			if ("-r".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				setRootDir(new java.io.File(args[++argNum]));
				continue;
			}
			if ("-p".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				setPackagePath(args[++argNum]);
				continue;
			}
			if ("-sp".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				setIndentAmount(Integer.parseInt(args[++argNum]));
				continue;
			}
			if ("-mdd".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				setMddFile(new java.io.File(args[++argNum]));
				continue;
			}
			if ("-noe".equals(arg)) {
				setScalarException(true);
				continue;
			}
			if ("-nonoe".equals(arg)) {
				setScalarException(false);
				continue;
			}
			if ("-ts".equals(arg)) {
				setDumpToString(true);
				continue;
			}
			if ("-nots".equals(arg)) {
				setDumpToString(false);
				continue;
			}
			if ("-veto".equals(arg)) {
				setVetoable(true);
				continue;
			}
			if ("-noveto".equals(arg)) {
				setVetoable(false);
				continue;
			}
			if ("-st".equals(arg)) {
				setStandalone(true);
				continue;
			}
			if ("-nost".equals(arg)) {
				setStandalone(false);
				continue;
			}
			if ("-auto".equals(arg)) {
				setAuto(true);
				continue;
			}
			if ("-noauto".equals(arg)) {
				setAuto(false);
				continue;
			}
			if ("-throw".equals(arg)) {
				setThrowErrors(true);
				continue;
			}
			if ("-nothrow".equals(arg)) {
				setThrowErrors(false);
				continue;
			}
			if ("-validate".equals(arg)) {
				setGenerateValidate(true);
				continue;
			}
			if ("-novalidate".equals(arg)) {
				setGenerateValidate(false);
				continue;
			}
			if ("-propertyevents".equals(arg)) {
				setGeneratePropertyEvents(true);
				continue;
			}
			if ("-nopropertyevents".equals(arg)) {
				setGeneratePropertyEvents(false);
				continue;
			}
			if ("-transactions".equals(arg)) {
				setGenerateTransactions(true);
				continue;
			}
			if ("-notransactions".equals(arg)) {
				setGenerateTransactions(false);
				continue;
			}
			if ("-attrprop".equals(arg)) {
				setAttributesAsProperties(true);
				continue;
			}
			if ("-noattrprop".equals(arg)) {
				setAttributesAsProperties(false);
				continue;
			}
			if ("-delegator".equals(arg)) {
				setGenerateDelegator(true);
				continue;
			}
			if ("-nodelegator".equals(arg)) {
				setGenerateDelegator(false);
				continue;
			}
			if ("-delegatedir".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				setDelegateDir(new java.io.File(args[++argNum]));
				continue;
			}
			if ("-delegatepackage".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				setDelegatePackage(args[++argNum]);
				continue;
			}
			if ("-commoninterfacename".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				setGenerateCommonInterface(args[++argNum]);
				continue;
			}
			if ("-defaultsaccessable".equals(arg)) {
				setDefaultsAccessable(true);
				continue;
			}
			if ("-nodefaultsaccessable".equals(arg)) {
				setDefaultsAccessable(false);
				continue;
			}
			if ("-useinterfaces".equals(arg)) {
				setUseInterfaces(true);
				continue;
			}
			if ("-nouseinterfaces".equals(arg)) {
				setUseInterfaces(false);
				continue;
			}
			if ("-geninterfaces".equals(arg)) {
				setGenerateInterfaces(true);
				continue;
			}
			if ("-nogeninterfaces".equals(arg)) {
				setGenerateInterfaces(false);
				continue;
			}
			if ("-keepelementpositions".equals(arg)) {
				setKeepElementPositions(true);
				continue;
			}
			if ("-nokeepelementpositions".equals(arg)) {
				setKeepElementPositions(false);
				continue;
			}
			if ("-removeunreferencednodes".equals(arg)) {
				setRemoveUnreferencedNodes(true);
				continue;
			}
			if ("-noremoveunreferencednodes".equals(arg)) {
				setRemoveUnreferencedNodes(false);
				continue;
			}
			if ("-indexedpropertytype".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				setIndexedPropertyType(args[++argNum]);
				continue;
			}
			if ("-compile".equals(arg)) {
				setDoCompile(true);
				continue;
			}
			if ("-nocompile".equals(arg)) {
				setDoCompile(false);
				continue;
			}
			if ("-generateswitches".equals(arg)) {
				setGenerateSwitches(true);
				continue;
			}
			if ("-nogenerateswitches".equals(arg)) {
				setGenerateSwitches(false);
				continue;
			}
			if ("-dumpbeantree".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				setDumpBeanTree(new java.io.File(args[++argNum]));
				continue;
			}
			if ("-gendotgraph".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				setGenerateDotGraph(new java.io.File(args[++argNum]));
				continue;
			}
			if ("-comments".equals(arg)) {
				setProcessComments(true);
				continue;
			}
			if ("-nocomments".equals(arg)) {
				setProcessComments(false);
				continue;
			}
			if ("-doctype".equals(arg)) {
				setProcessDocType(true);
				continue;
			}
			if ("-nodoctype".equals(arg)) {
				setProcessDocType(false);
				continue;
			}
			if ("-checkuptodate".equals(arg)) {
				setCheckUpToDate(true);
				continue;
			}
			if ("-nocheckuptodate".equals(arg)) {
				setCheckUpToDate(false);
				continue;
			}
			if ("-haschanged".equals(arg)) {
				setGenerateHasChanged(true);
				continue;
			}
			if ("-nohaschanged".equals(arg)) {
				setGenerateHasChanged(false);
				continue;
			}
			if ("-writebeangraph".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				setWriteBeanGraphFile(new java.io.File(args[++argNum]));
				continue;
			}
			if ("-readbeangraph".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				addReadBeanGraphFiles(new java.io.File(args[++argNum]));
				continue;
			}
			if ("-min".equals(arg)) {
				setMinFeatures(true);
				continue;
			}
			if ("-nomin".equals(arg)) {
				setMinFeatures(false);
				continue;
			}
			if ("-forme".equals(arg)) {
				setForME(true);
				continue;
			}
			if ("-noforme".equals(arg)) {
				setForME(false);
				continue;
			}
			if ("-java5".equals(arg)) {
				setJava5(true);
				continue;
			}
			if ("-nojava5".equals(arg)) {
				setJava5(false);
				continue;
			}
			if ("-tagsfile".equals(arg)) {
				setGenerateTagsFile(true);
				continue;
			}
			if ("-notagsfile".equals(arg)) {
				setGenerateTagsFile(false);
				continue;
			}
			if ("-generatetimestamp".equals(arg)) {
				setGenerateTimeStamp(true);
				continue;
			}
			if ("-nogeneratetimestamp".equals(arg)) {
				setGenerateTimeStamp(false);
				continue;
			}
			if ("-quiet".equals(arg)) {
				setQuiet(true);
				continue;
			}
			if ("-noquiet".equals(arg)) {
				setQuiet(false);
				continue;
			}
			if ("-writeconfig".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				setWriteConfig(new java.io.File(args[++argNum]));
				continue;
			}
			if ("-readconfig".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				addReadConfig(new java.io.File(args[++argNum]));
				continue;
			}
			if ("-makedefaults".equals(arg)) {
				setMakeDefaults(true);
				continue;
			}
			if ("-nomakedefaults".equals(arg)) {
				setMakeDefaults(false);
				continue;
			}
			if ("-setdefaults".equals(arg)) {
				setSetDefaults(true);
				continue;
			}
			if ("-nosetdefaults".equals(arg)) {
				setSetDefaults(false);
				continue;
			}
			if ("-trimnonstrings".equals(arg)) {
				setTrimNonStrings(true);
				continue;
			}
			if ("-notrimnonstrings".equals(arg)) {
				setTrimNonStrings(false);
				continue;
			}
			if ("-useruntime".equals(arg)) {
				setUseRuntime(true);
				continue;
			}
			if ("-nouseruntime".equals(arg)) {
				setUseRuntime(false);
				continue;
			}
			if ("-extendbasebean".equals(arg)) {
				setExtendBaseBean(true);
				continue;
			}
			if ("-noextendbasebean".equals(arg)) {
				setExtendBaseBean(false);
				continue;
			}
			if ("-finder".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				addFinder(args[++argNum]);
				continue;
			}
			if ("-target".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				setTarget(args[++argNum]);
				continue;
			}
			if ("-staxproducexmleventreader".equals(arg)) {
				setStaxProduceXMLEventReader(true);
				continue;
			}
			if ("-nostaxproducexmleventreader".equals(arg)) {
				setStaxProduceXMLEventReader(false);
				continue;
			}
			if ("-staxusexmleventreader".equals(arg)) {
				setStaxUseXMLEventReader(true);
				continue;
			}
			if ("-nostaxusexmleventreader".equals(arg)) {
				setStaxUseXMLEventReader(false);
				continue;
			}
			if ("-optionalscalars".equals(arg)) {
				setOptionalScalars(true);
				continue;
			}
			if ("-nooptionalscalars".equals(arg)) {
				setOptionalScalars(false);
				continue;
			}
			if ("-defaultelementtype".equals(arg)) {
				if (argNum+1 >= size) {
					missingArgument(args, arg);
					continue;
				}
				setDefaultElementType(args[++argNum]);
				continue;
			}
			if ("-respectextension".equals(arg)) {
				setRespectExtension(true);
				continue;
			}
			if ("-norespectextension".equals(arg)) {
				setRespectExtension(false);
				continue;
			}
			if ("-logsuspicious".equals(arg)) {
				setLogSuspicious(true);
				continue;
			}
			if ("-nologsuspicious".equals(arg)) {
				setLogSuspicious(false);
				continue;
			}
			if (arg == "-help" || arg == "--help") {
				return true;
			}
			argNum = unknownArgument(args, arg, argNum);
		}
		return false;
	}

	protected int unknownArgument(String[] args, String arg, int argNum) {
		throw new IllegalArgumentException("Found unknown argument '"+arg+"'");
	}

	protected void missingArgument(String[] args, String arg) {
		throw new IllegalArgumentException("Not enough arguments.  Need 1 more for '"+arg+"'");
	}

	protected void missingMandatoryArgument(String arg) {
		throw new IllegalArgumentException("Missing argument '"+arg+"'");
	}

	public void showHelp(java.io.PrintStream out) {
		out.println(" [-f filename] [-d docRoot] [-r filename] [-p packagePath] [-sp indentAmount]\n [-mdd filename] [-noe] [-nonoe] [-ts] [-nots] [-veto] [-noveto]\n [-st] [-nost] [-auto] [-noauto] [-throw] [-nothrow] [-validate] [-novalidate]\n [-propertyEvents] [-nopropertyEvents] [-transactions] [-notransactions]\n [-attrProp] [-noattrProp] [-delegator] [-nodelegator] [-delegateDir filename]\n [-delegatePackage delegatePackage] [-commonInterfaceName generateCommonInterface]\n [-defaultsAccessable] [-nodefaultsAccessable] [-useInterfaces] [-nouseInterfaces]\n [-genInterfaces] [-nogenInterfaces] [-keepElementPositions] [-nokeepElementPositions]\n [-removeUnreferencedNodes] [-noremoveUnreferencedNodes] [-indexedPropertyType indexedPropertyType]\n [-compile] [-nocompile] [-generateSwitches] [-nogenerateSwitches]\n [-dumpBeanTree filename] [-genDotGraph filename] [-comments] [-nocomments]\n [-docType] [-nodocType] [-checkUpToDate] [-nocheckUpToDate] [-hasChanged] [-nohasChanged]\n [-writeBeanGraph filename] [-readBeanGraph filename] [-min] [-nomin]\n [-forME] [-noforME] [-java5] [-nojava5] [-tagsFile] [-notagsFile]\n [-generateTimeStamp] [-nogenerateTimeStamp] [-quiet] [-noquiet]\n [-writeConfig filename] [-readConfig filename] [-makeDefaults] [-nomakeDefaults]\n [-setDefaults] [-nosetDefaults] [-trimNonStrings] [-notrimNonStrings]\n [-useRuntime] [-nouseRuntime] [-extendBaseBean] [-noextendBaseBean]\n [-finder finder] [-target target] [-staxProduceXMLEventReader] [-nostaxProduceXMLEventReader]\n [-staxUseXMLEventReader] [-nostaxUseXMLEventReader] [-optionalScalars] [-nooptionalScalars]\n [-defaultElementType defaultElementType] [-respectExtension] [-norespectExtension]\n [-logSuspicious] [-nologSuspicious]\n");
		out.print(" -f\tfile name of the schema\n -d\tDTD root element name (for example webapp or ejb-jar)\n -r\tbase root directory (root of the package path)\n -p\tpackage name\n -sp\tset the indentation to use 'number' spaces instead of the default tab (\\t) value\n -mdd\tprovides extra information that the schema cannot provide. If the file doesn't exist, a skeleton file is created and no bean generation happens.\n -noe\tdo not throw the NoSuchElement exception when a scalar property has no value, return a default '0' value instead (BaseBean only).\n -ts\tthe toString() of the bean returns the full content\\n  of the bean sub-tree instead of its simple name.\n -veto\tgenerate vetoable properties (only for non-bean properties).\n -st\tstandalone mode - do not generate NetBeans dependencies\n -auto\tDon't ask the user any questions.\n -throw\tgenerate code that prefers to pass exceptions\\n  through instead of converting them to RuntimeException (recommended).\n -validate\tGenerate a validate method for doing validation.\n -propertyEvents\tGenerate methods for dealing with property events (always on for BaseBean type).\n -transactions\texperimental feature\n -attrProp\tAttributes become like any other property\n -delegator\tGenerate a delegator class for every bean generated.\n -delegateDir\tThe base directory to write every delegate into.\n -delegatePackage\tThe package to use for the delegates.\n -commonInterfaceName\tName the common interface between all beans.\n -defaultsAccessable\tGenerate methods to be able to get at default values.\n -useInterfaces\tGetters and setters signatures would use the first defined interface on the bean.\n -genInterfaces\tFor every bean generated, generate an interfaces for it's accessors.\n -keepElementPositions\tKeep track of the positions of elements (no BaseBean support).\n -removeUnreferencedNodes\tDo not generate unreferenced nodes from the bean graph.\n -indexedPropertyType\tThe name of the class to use for indexed properties.\n -compile\tCompile all generated classes using javac.\n -generateSwitches\tGenerate parseArguments()\n -dumpBeanTree\tWrite out the bean tree to filename.\n -genDotGraph\tGenerate a .dot style file for use with GraphViz (http://www.graphviz.org/).\n -comments\tProcess and keep comments (always on for BaseBean type).\n -docType\tProcess and keep Document Types (always on for BaseBean type).\"\n -checkUpToDate\tOnly do generation if the source files are newer than the to be generated files.\n -hasChanged\tKeep track of whether or not the beans have changed.\n -writeBeanGraph\tWrite out a beangraph XML file.  Useful for connecting separate bean graphs.\n -readBeanGraph\tRead in and use the results of another bean graph.\n -min\tGenerate the minimum Java Beans.  Reduce features in favor of reduced class file size.\n -forME\tGenerate code for use on J2ME.\n -java5\tGenerate code for use on Java 5 and newer.\n -tagsFile\tGenerate a class that has all schema element and attribute names\n -generateTimeStamp\tOutput a born on date into generated files.\n -quiet\tDon't be as verbose.\n -writeConfig\tWrite out Config as a file; this includes all command line switches.  Useful for seeing what switches are set, and for reloading a bunch of switches with -readConfig.\n -readConfig\tRead in Config file.  See -writeConfig.\n -makeDefaults\tMake properties that require a value have a default value even if the schema didn't say it had a default (defaults to true).\n -setDefaults\tFill in defaults.\n -trimNonStrings\tTrim non strings while reading XML.\n -useRuntime\tMake use of the schema2beans runtime (always on for BaseBean type).\n -extendBaseBean\tMake every bean extend BaseBean (always on for BaseBean type).  For those who like -javabean's better performance, but can't seem to get away from BaseBean.\n -finder\tAdd a finder method.  Format: \"on {start} find {selector} by {key}\".  Example: \"on /ejb-jar/enterprise-beans find session by ejb-name\".\n -target\tTarget JDK to generate for.\n -staxProduceXMLEventReader\tProduce a StAX XMLEventReader to read the beans as if they were XML.\n -staxUseXMLEventReader\tUse an StAX XMLEventReader for reading the beans.\n -optionalScalars\tWhether or not scalars can be optional.  Default: false.  Recommended: true.\n -defaultElementType\tWhen a type cannot be figured out, use this type.  Default: \"{http://www.w3.org/2001/XMLSchema}boolean\".\n -respectExtension\tTake advantage of when an extension is defined in the schema.\n -logSuspicious\tLog suspicious things.\n");
	}

	public void changePropertyByName(String name, Object value) {
		if (name == null) return;
		name = name.intern();
		if ("schemaType".equals(name))
			setSchemaType((java.lang.String)value);
		else if ("traceParse".equals(name))
			setTraceParse(((java.lang.Boolean)value).booleanValue());
		else if ("traceGen".equals(name))
			setTraceGen(((java.lang.Boolean)value).booleanValue());
		else if ("traceMisc".equals(name))
			setTraceMisc(((java.lang.Boolean)value).booleanValue());
		else if ("traceDot".equals(name))
			setTraceDot(((java.lang.Boolean)value).booleanValue());
		else if ("filename".equals(name))
			setFilename((java.io.File)value);
		else if ("fileIn".equals(name))
			setFileIn((java.io.InputStream)value);
		else if ("docRoot".equals(name))
			setDocRoot((java.lang.String)value);
		else if ("rootDir".equals(name))
			setRootDir((java.io.File)value);
		else if ("packagePath".equals(name))
			setPackagePath((java.lang.String)value);
		else if ("indent".equals(name))
			setIndent((java.lang.String)value);
		else if ("indentAmount".equals(name))
			setIndentAmount(((java.lang.Integer)value).intValue());
		else if ("mddFile".equals(name))
			setMddFile((java.io.File)value);
		else if ("mddIn".equals(name))
			setMddIn((java.io.InputStream)value);
		else if ("metaDD".equals(name))
			setMetaDD((org.netbeans.modules.schema2beansdev.metadd.MetaDD)value);
		else if ("doGeneration".equals(name))
			setDoGeneration(((java.lang.Boolean)value).booleanValue());
		else if ("scalarException".equals(name))
			setScalarException(((java.lang.Boolean)value).booleanValue());
		else if ("dumpToString".equals(name))
			setDumpToString(((java.lang.Boolean)value).booleanValue());
		else if ("vetoable".equals(name))
			setVetoable(((java.lang.Boolean)value).booleanValue());
		else if ("standalone".equals(name))
			setStandalone(((java.lang.Boolean)value).booleanValue());
		else if ("auto".equals(name))
			setAuto(((java.lang.Boolean)value).booleanValue());
		else if ("messageOut".equals(name))
			setMessageOut((java.io.PrintStream)value);
		else if ("outputStreamProvider".equals(name))
			setOutputStreamProvider((org.netbeans.modules.schema2beansdev.GenBeans.OutputStreamProvider)value);
		else if ("throwErrors".equals(name))
			setThrowErrors(((java.lang.Boolean)value).booleanValue());
		else if ("generateXMLIO".equals(name))
			setGenerateXMLIO(((java.lang.Boolean)value).booleanValue());
		else if ("generateValidate".equals(name))
			setGenerateValidate(((java.lang.Boolean)value).booleanValue());
		else if ("generatePropertyEvents".equals(name))
			setGeneratePropertyEvents(((java.lang.Boolean)value).booleanValue());
		else if ("generateStoreEvents".equals(name))
			setGenerateStoreEvents(((java.lang.Boolean)value).booleanValue());
		else if ("generateTransactions".equals(name))
			setGenerateTransactions(((java.lang.Boolean)value).booleanValue());
		else if ("attributesAsProperties".equals(name))
			setAttributesAsProperties(((java.lang.Boolean)value).booleanValue());
		else if ("generateDelegator".equals(name))
			setGenerateDelegator(((java.lang.Boolean)value).booleanValue());
		else if ("delegateDir".equals(name))
			setDelegateDir((java.io.File)value);
		else if ("delegatePackage".equals(name))
			setDelegatePackage((java.lang.String)value);
		else if ("generateCommonInterface".equals(name))
			setGenerateCommonInterface((java.lang.String)value);
		else if ("defaultsAccessable".equals(name))
			setDefaultsAccessable(((java.lang.Boolean)value).booleanValue());
		else if ("useInterfaces".equals(name))
			setUseInterfaces(((java.lang.Boolean)value).booleanValue());
		else if ("generateInterfaces".equals(name))
			setGenerateInterfaces(((java.lang.Boolean)value).booleanValue());
		else if ("keepElementPositions".equals(name))
			setKeepElementPositions(((java.lang.Boolean)value).booleanValue());
		else if ("removeUnreferencedNodes".equals(name))
			setRemoveUnreferencedNodes(((java.lang.Boolean)value).booleanValue());
		else if ("inputURI".equals(name))
			setInputURI((java.lang.String)value);
		else if ("indexedPropertyType".equals(name))
			setIndexedPropertyType((java.lang.String)value);
		else if ("doCompile".equals(name))
			setDoCompile(((java.lang.Boolean)value).booleanValue());
		else if ("generateSwitches".equals(name))
			setGenerateSwitches(((java.lang.Boolean)value).booleanValue());
		else if ("dumpBeanTree".equals(name))
			setDumpBeanTree((java.io.File)value);
		else if ("generateDotGraph".equals(name))
			setGenerateDotGraph((java.io.File)value);
		else if ("processComments".equals(name))
			setProcessComments(((java.lang.Boolean)value).booleanValue());
		else if ("processDocType".equals(name))
			setProcessDocType(((java.lang.Boolean)value).booleanValue());
		else if ("checkUpToDate".equals(name))
			setCheckUpToDate(((java.lang.Boolean)value).booleanValue());
		else if ("generateParentRefs".equals(name))
			setGenerateParentRefs(((java.lang.Boolean)value).booleanValue());
		else if ("generateHasChanged".equals(name))
			setGenerateHasChanged(((java.lang.Boolean)value).booleanValue());
		else if ("newestSourceTime".equals(name))
			setNewestSourceTime(((java.lang.Long)value).longValue());
		else if ("writeBeanGraphFile".equals(name))
			setWriteBeanGraphFile((java.io.File)value);
		else if ("readBeanGraphFiles".equals(name))
			addReadBeanGraphFiles((java.io.File)value);
		else if ("readBeanGraphFiles[]".equals(name))
			setReadBeanGraphFiles((java.io.File[]) value);
		else if ("readBeanGraphs".equals(name))
			addReadBeanGraphs((org.netbeans.modules.schema2beansdev.beangraph.BeanGraph)value);
		else if ("readBeanGraphs[]".equals(name))
			setReadBeanGraphs((org.netbeans.modules.schema2beansdev.beangraph.BeanGraph[]) value);
		else if ("minFeatures".equals(name))
			setMinFeatures(((java.lang.Boolean)value).booleanValue());
		else if ("forME".equals(name))
			setForME(((java.lang.Boolean)value).booleanValue());
		else if ("java5".equals(name))
			setJava5(((java.lang.Boolean)value).booleanValue());
		else if ("generateTagsFile".equals(name))
			setGenerateTagsFile(((java.lang.Boolean)value).booleanValue());
		else if ("codeGeneratorFactory".equals(name))
			setCodeGeneratorFactory((org.netbeans.modules.schema2beansdev.CodeGeneratorFactory)value);
		else if ("generateTimeStamp".equals(name))
			setGenerateTimeStamp(((java.lang.Boolean)value).booleanValue());
		else if ("quiet".equals(name))
			setQuiet(((java.lang.Boolean)value).booleanValue());
		else if ("writeConfig".equals(name))
			setWriteConfig((java.io.File)value);
		else if ("readConfig".equals(name))
			addReadConfig((java.io.File)value);
		else if ("readConfig[]".equals(name))
			setReadConfig((java.io.File[]) value);
		else if ("makeDefaults".equals(name))
			setMakeDefaults(((java.lang.Boolean)value).booleanValue());
		else if ("setDefaults".equals(name))
			setSetDefaults(((java.lang.Boolean)value).booleanValue());
		else if ("trimNonStrings".equals(name))
			setTrimNonStrings(((java.lang.Boolean)value).booleanValue());
		else if ("useRuntime".equals(name))
			setUseRuntime(((java.lang.Boolean)value).booleanValue());
		else if ("extendBaseBean".equals(name))
			setExtendBaseBean(((java.lang.Boolean)value).booleanValue());
		else if ("finder".equals(name))
			addFinder((java.lang.String)value);
		else if ("finder[]".equals(name))
			setFinder((java.lang.String[]) value);
		else if ("target".equals(name))
			setTarget((java.lang.String)value);
		else if ("staxProduceXMLEventReader".equals(name))
			setStaxProduceXMLEventReader(((java.lang.Boolean)value).booleanValue());
		else if ("staxUseXMLEventReader".equals(name))
			setStaxUseXMLEventReader(((java.lang.Boolean)value).booleanValue());
		else if ("optionalScalars".equals(name))
			setOptionalScalars(((java.lang.Boolean)value).booleanValue());
		else if ("defaultElementType".equals(name))
			setDefaultElementType((java.lang.String)value);
		else if ("respectExtension".equals(name))
			setRespectExtension(((java.lang.Boolean)value).booleanValue());
		else if ("logSuspicious".equals(name))
			setLogSuspicious(((java.lang.Boolean)value).booleanValue());
		else
			throw new IllegalArgumentException(name+" is not a valid property name for S2bConfig");
	}

	public Object fetchPropertyByName(String name) {
		if ("schemaType".equals(name))
			return getSchemaType();
		if ("traceParse".equals(name))
			return (isTraceParse() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("traceGen".equals(name))
			return (isTraceGen() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("traceMisc".equals(name))
			return (isTraceMisc() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("traceDot".equals(name))
			return (isTraceDot() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("filename".equals(name))
			return getFilename();
		if ("fileIn".equals(name))
			return getFileIn();
		if ("docRoot".equals(name))
			return getDocRoot();
		if ("rootDir".equals(name))
			return getRootDir();
		if ("packagePath".equals(name))
			return getPackagePath();
		if ("indent".equals(name))
			return getIndent();
		if ("indentAmount".equals(name))
			return new java.lang.Integer(getIndentAmount());
		if ("mddFile".equals(name))
			return getMddFile();
		if ("mddIn".equals(name))
			return getMddIn();
		if ("metaDD".equals(name))
			return getMetaDD();
		if ("doGeneration".equals(name))
			return (isDoGeneration() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("scalarException".equals(name))
			return (isScalarException() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("dumpToString".equals(name))
			return (isDumpToString() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("vetoable".equals(name))
			return (isVetoable() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("standalone".equals(name))
			return (isStandalone() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("auto".equals(name))
			return (isAuto() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("messageOut".equals(name))
			return getMessageOut();
		if ("outputStreamProvider".equals(name))
			return getOutputStreamProvider();
		if ("throwErrors".equals(name))
			return (isThrowErrors() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("generateXMLIO".equals(name))
			return (isGenerateXMLIO() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("generateValidate".equals(name))
			return (isGenerateValidate() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("generatePropertyEvents".equals(name))
			return (isGeneratePropertyEvents() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("generateStoreEvents".equals(name))
			return (isGenerateStoreEvents() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("generateTransactions".equals(name))
			return (isGenerateTransactions() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("attributesAsProperties".equals(name))
			return (isAttributesAsProperties() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("generateDelegator".equals(name))
			return (isGenerateDelegator() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("delegateDir".equals(name))
			return getDelegateDir();
		if ("delegatePackage".equals(name))
			return getDelegatePackage();
		if ("generateCommonInterface".equals(name))
			return getGenerateCommonInterface();
		if ("defaultsAccessable".equals(name))
			return (isDefaultsAccessable() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("useInterfaces".equals(name))
			return (isUseInterfaces() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("generateInterfaces".equals(name))
			return (isGenerateInterfaces() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("keepElementPositions".equals(name))
			return (isKeepElementPositions() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("removeUnreferencedNodes".equals(name))
			return (isRemoveUnreferencedNodes() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("inputURI".equals(name))
			return getInputURI();
		if ("indexedPropertyType".equals(name))
			return getIndexedPropertyType();
		if ("doCompile".equals(name))
			return (isDoCompile() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("generateSwitches".equals(name))
			return (isGenerateSwitches() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("dumpBeanTree".equals(name))
			return getDumpBeanTree();
		if ("generateDotGraph".equals(name))
			return getGenerateDotGraph();
		if ("processComments".equals(name))
			return (isProcessComments() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("processDocType".equals(name))
			return (isProcessDocType() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("checkUpToDate".equals(name))
			return (isCheckUpToDate() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("generateParentRefs".equals(name))
			return (isGenerateParentRefs() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("generateHasChanged".equals(name))
			return (isGenerateHasChanged() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("newestSourceTime".equals(name))
			return new java.lang.Long(getNewestSourceTime());
		if ("writeBeanGraphFile".equals(name))
			return getWriteBeanGraphFile();
		if ("readBeanGraphFiles[]".equals(name))
			return getReadBeanGraphFiles();
		if ("readBeanGraphs[]".equals(name))
			return getReadBeanGraphs();
		if ("minFeatures".equals(name))
			return (isMinFeatures() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("forME".equals(name))
			return (isForME() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("java5".equals(name))
			return (isJava5() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("generateTagsFile".equals(name))
			return (isGenerateTagsFile() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("codeGeneratorFactory".equals(name))
			return getCodeGeneratorFactory();
		if ("generateTimeStamp".equals(name))
			return (isGenerateTimeStamp() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("quiet".equals(name))
			return (isQuiet() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("writeConfig".equals(name))
			return getWriteConfig();
		if ("readConfig[]".equals(name))
			return getReadConfig();
		if ("makeDefaults".equals(name))
			return (isMakeDefaults() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("setDefaults".equals(name))
			return (isSetDefaults() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("trimNonStrings".equals(name))
			return (isTrimNonStrings() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("useRuntime".equals(name))
			return (isUseRuntime() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("extendBaseBean".equals(name))
			return (isExtendBaseBean() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("finder[]".equals(name))
			return getFinder();
		if ("target".equals(name))
			return getTarget();
		if ("staxProduceXMLEventReader".equals(name))
			return (isStaxProduceXMLEventReader() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("staxUseXMLEventReader".equals(name))
			return (isStaxUseXMLEventReader() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("optionalScalars".equals(name))
			return (isOptionalScalars() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("defaultElementType".equals(name))
			return getDefaultElementType();
		if ("respectExtension".equals(name))
			return (isRespectExtension() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		if ("logSuspicious".equals(name))
			return (isLogSuspicious() ? java.lang.Boolean.TRUE : java.lang.Boolean.FALSE);
		throw new IllegalArgumentException(name+" is not a valid property name for S2bConfig");
	}

	public String nameSelf() {
		return "/S2bConfig";
	}

	public String nameChild(Object childObj) {
		return nameChild(childObj, false, false);
	}

	/**
	 * @param childObj  The child object to search for
	 * @param returnSchemaName  Whether or not the schema name should be returned or the property name
	 * @return null if not found
	 */
	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName) {
		return nameChild(childObj, returnConstName, returnSchemaName, false);
	}

	/**
	 * @param childObj  The child object to search for
	 * @param returnSchemaName  Whether or not the schema name should be returned or the property name
	 * @return null if not found
	 */
	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName, boolean returnXPathName) {
		if (childObj instanceof java.io.InputStream) {
			java.io.InputStream child = (java.io.InputStream) childObj;
			if (child == _FileIn) {
				if (returnConstName) {
					return FILEIN;
				} else if (returnSchemaName) {
					return "fileIn";
				} else if (returnXPathName) {
					return "fileIn";
				} else {
					return "FileIn";
				}
			}
			if (child == _MddIn) {
				if (returnConstName) {
					return MDDIN;
				} else if (returnSchemaName) {
					return "mddIn";
				} else if (returnXPathName) {
					return "mddIn";
				} else {
					return "MddIn";
				}
			}
		}
		if (childObj instanceof org.netbeans.modules.schema2beansdev.beangraph.BeanGraph) {
			org.netbeans.modules.schema2beansdev.beangraph.BeanGraph child = (org.netbeans.modules.schema2beansdev.beangraph.BeanGraph) childObj;
			int index = 0;
			for (java.util.Iterator it = _ReadBeanGraphs.iterator(); 
				it.hasNext(); ) {
				org.netbeans.modules.schema2beansdev.beangraph.BeanGraph element = (org.netbeans.modules.schema2beansdev.beangraph.BeanGraph)it.next();
				if (child == element) {
					if (returnConstName) {
						return READBEANGRAPHS;
					} else if (returnSchemaName) {
						return "readBeanGraphs";
					} else if (returnXPathName) {
						return "readBeanGraphs[position()="+index+"]";
					} else {
						return "ReadBeanGraphs."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof java.lang.Boolean) {
			java.lang.Boolean child = (java.lang.Boolean) childObj;
			if (((java.lang.Boolean)child).booleanValue() == _TraceParse) {
				if (returnConstName) {
					return TRACEPARSE;
				} else if (returnSchemaName) {
					return "traceParse";
				} else if (returnXPathName) {
					return "traceParse";
				} else {
					return "TraceParse";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _TraceGen) {
				if (returnConstName) {
					return TRACEGEN;
				} else if (returnSchemaName) {
					return "traceGen";
				} else if (returnXPathName) {
					return "traceGen";
				} else {
					return "TraceGen";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _TraceMisc) {
				if (returnConstName) {
					return TRACEMISC;
				} else if (returnSchemaName) {
					return "traceMisc";
				} else if (returnXPathName) {
					return "traceMisc";
				} else {
					return "TraceMisc";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _TraceDot) {
				if (returnConstName) {
					return TRACEDOT;
				} else if (returnSchemaName) {
					return "traceDot";
				} else if (returnXPathName) {
					return "traceDot";
				} else {
					return "TraceDot";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _DoGeneration) {
				if (returnConstName) {
					return DOGENERATION;
				} else if (returnSchemaName) {
					return "doGeneration";
				} else if (returnXPathName) {
					return "doGeneration";
				} else {
					return "DoGeneration";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _ScalarException) {
				if (returnConstName) {
					return SCALAREXCEPTION;
				} else if (returnSchemaName) {
					return "scalarException";
				} else if (returnXPathName) {
					return "scalarException";
				} else {
					return "ScalarException";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _DumpToString) {
				if (returnConstName) {
					return DUMPTOSTRING;
				} else if (returnSchemaName) {
					return "dumpToString";
				} else if (returnXPathName) {
					return "dumpToString";
				} else {
					return "DumpToString";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _Vetoable) {
				if (returnConstName) {
					return VETOABLE;
				} else if (returnSchemaName) {
					return "vetoable";
				} else if (returnXPathName) {
					return "vetoable";
				} else {
					return "Vetoable";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _Standalone) {
				if (returnConstName) {
					return STANDALONE;
				} else if (returnSchemaName) {
					return "standalone";
				} else if (returnXPathName) {
					return "standalone";
				} else {
					return "Standalone";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _Auto) {
				if (returnConstName) {
					return AUTO;
				} else if (returnSchemaName) {
					return "auto";
				} else if (returnXPathName) {
					return "auto";
				} else {
					return "Auto";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _ThrowErrors) {
				if (returnConstName) {
					return THROWERRORS;
				} else if (returnSchemaName) {
					return "throwErrors";
				} else if (returnXPathName) {
					return "throwErrors";
				} else {
					return "ThrowErrors";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _GenerateXMLIO) {
				if (returnConstName) {
					return GENERATEXMLIO;
				} else if (returnSchemaName) {
					return "generateXMLIO";
				} else if (returnXPathName) {
					return "generateXMLIO";
				} else {
					return "GenerateXMLIO";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _GenerateValidate) {
				if (returnConstName) {
					return GENERATEVALIDATE;
				} else if (returnSchemaName) {
					return "generateValidate";
				} else if (returnXPathName) {
					return "generateValidate";
				} else {
					return "GenerateValidate";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _GeneratePropertyEvents) {
				if (returnConstName) {
					return GENERATEPROPERTYEVENTS;
				} else if (returnSchemaName) {
					return "generatePropertyEvents";
				} else if (returnXPathName) {
					return "generatePropertyEvents";
				} else {
					return "GeneratePropertyEvents";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _GenerateStoreEvents) {
				if (returnConstName) {
					return GENERATESTOREEVENTS;
				} else if (returnSchemaName) {
					return "generateStoreEvents";
				} else if (returnXPathName) {
					return "generateStoreEvents";
				} else {
					return "GenerateStoreEvents";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _GenerateTransactions) {
				if (returnConstName) {
					return GENERATETRANSACTIONS;
				} else if (returnSchemaName) {
					return "generateTransactions";
				} else if (returnXPathName) {
					return "generateTransactions";
				} else {
					return "GenerateTransactions";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _AttributesAsProperties) {
				if (returnConstName) {
					return ATTRIBUTESASPROPERTIES;
				} else if (returnSchemaName) {
					return "attributesAsProperties";
				} else if (returnXPathName) {
					return "attributesAsProperties";
				} else {
					return "AttributesAsProperties";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _GenerateDelegator) {
				if (returnConstName) {
					return GENERATEDELEGATOR;
				} else if (returnSchemaName) {
					return "generateDelegator";
				} else if (returnXPathName) {
					return "generateDelegator";
				} else {
					return "GenerateDelegator";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _DefaultsAccessable) {
				if (returnConstName) {
					return DEFAULTSACCESSABLE;
				} else if (returnSchemaName) {
					return "defaultsAccessable";
				} else if (returnXPathName) {
					return "defaultsAccessable";
				} else {
					return "DefaultsAccessable";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _UseInterfaces) {
				if (returnConstName) {
					return USEINTERFACES;
				} else if (returnSchemaName) {
					return "useInterfaces";
				} else if (returnXPathName) {
					return "useInterfaces";
				} else {
					return "UseInterfaces";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _GenerateInterfaces) {
				if (returnConstName) {
					return GENERATEINTERFACES;
				} else if (returnSchemaName) {
					return "generateInterfaces";
				} else if (returnXPathName) {
					return "generateInterfaces";
				} else {
					return "GenerateInterfaces";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _KeepElementPositions) {
				if (returnConstName) {
					return KEEPELEMENTPOSITIONS;
				} else if (returnSchemaName) {
					return "keepElementPositions";
				} else if (returnXPathName) {
					return "keepElementPositions";
				} else {
					return "KeepElementPositions";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _RemoveUnreferencedNodes) {
				if (returnConstName) {
					return REMOVEUNREFERENCEDNODES;
				} else if (returnSchemaName) {
					return "removeUnreferencedNodes";
				} else if (returnXPathName) {
					return "removeUnreferencedNodes";
				} else {
					return "RemoveUnreferencedNodes";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _DoCompile) {
				if (returnConstName) {
					return DOCOMPILE;
				} else if (returnSchemaName) {
					return "doCompile";
				} else if (returnXPathName) {
					return "doCompile";
				} else {
					return "DoCompile";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _GenerateSwitches) {
				if (returnConstName) {
					return GENERATESWITCHES;
				} else if (returnSchemaName) {
					return "generateSwitches";
				} else if (returnXPathName) {
					return "generateSwitches";
				} else {
					return "GenerateSwitches";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _ProcessComments) {
				if (returnConstName) {
					return PROCESSCOMMENTS;
				} else if (returnSchemaName) {
					return "processComments";
				} else if (returnXPathName) {
					return "processComments";
				} else {
					return "ProcessComments";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _ProcessDocType) {
				if (returnConstName) {
					return PROCESSDOCTYPE;
				} else if (returnSchemaName) {
					return "processDocType";
				} else if (returnXPathName) {
					return "processDocType";
				} else {
					return "ProcessDocType";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _CheckUpToDate) {
				if (returnConstName) {
					return CHECKUPTODATE;
				} else if (returnSchemaName) {
					return "checkUpToDate";
				} else if (returnXPathName) {
					return "checkUpToDate";
				} else {
					return "CheckUpToDate";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _GenerateParentRefs) {
				if (returnConstName) {
					return GENERATEPARENTREFS;
				} else if (returnSchemaName) {
					return "generateParentRefs";
				} else if (returnXPathName) {
					return "generateParentRefs";
				} else {
					return "GenerateParentRefs";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _GenerateHasChanged) {
				if (returnConstName) {
					return GENERATEHASCHANGED;
				} else if (returnSchemaName) {
					return "generateHasChanged";
				} else if (returnXPathName) {
					return "generateHasChanged";
				} else {
					return "GenerateHasChanged";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _MinFeatures) {
				if (returnConstName) {
					return MINFEATURES;
				} else if (returnSchemaName) {
					return "minFeatures";
				} else if (returnXPathName) {
					return "minFeatures";
				} else {
					return "MinFeatures";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _ForME) {
				if (returnConstName) {
					return FORME;
				} else if (returnSchemaName) {
					return "forME";
				} else if (returnXPathName) {
					return "forME";
				} else {
					return "ForME";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _Java5) {
				if (returnConstName) {
					return JAVA5;
				} else if (returnSchemaName) {
					return "java5";
				} else if (returnXPathName) {
					return "java5";
				} else {
					return "Java5";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _GenerateTagsFile) {
				if (returnConstName) {
					return GENERATETAGSFILE;
				} else if (returnSchemaName) {
					return "generateTagsFile";
				} else if (returnXPathName) {
					return "generateTagsFile";
				} else {
					return "GenerateTagsFile";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _GenerateTimeStamp) {
				if (returnConstName) {
					return GENERATETIMESTAMP;
				} else if (returnSchemaName) {
					return "generateTimeStamp";
				} else if (returnXPathName) {
					return "generateTimeStamp";
				} else {
					return "GenerateTimeStamp";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _Quiet) {
				if (returnConstName) {
					return QUIET;
				} else if (returnSchemaName) {
					return "quiet";
				} else if (returnXPathName) {
					return "quiet";
				} else {
					return "Quiet";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _MakeDefaults) {
				if (returnConstName) {
					return MAKEDEFAULTS;
				} else if (returnSchemaName) {
					return "makeDefaults";
				} else if (returnXPathName) {
					return "makeDefaults";
				} else {
					return "MakeDefaults";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _SetDefaults) {
				if (returnConstName) {
					return SETDEFAULTS;
				} else if (returnSchemaName) {
					return "setDefaults";
				} else if (returnXPathName) {
					return "setDefaults";
				} else {
					return "SetDefaults";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _TrimNonStrings) {
				if (returnConstName) {
					return TRIMNONSTRINGS;
				} else if (returnSchemaName) {
					return "trimNonStrings";
				} else if (returnXPathName) {
					return "trimNonStrings";
				} else {
					return "TrimNonStrings";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _UseRuntime) {
				if (returnConstName) {
					return USERUNTIME;
				} else if (returnSchemaName) {
					return "useRuntime";
				} else if (returnXPathName) {
					return "useRuntime";
				} else {
					return "UseRuntime";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _ExtendBaseBean) {
				if (returnConstName) {
					return EXTENDBASEBEAN;
				} else if (returnSchemaName) {
					return "extendBaseBean";
				} else if (returnXPathName) {
					return "extendBaseBean";
				} else {
					return "ExtendBaseBean";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _StaxProduceXMLEventReader) {
				if (returnConstName) {
					return STAXPRODUCEXMLEVENTREADER;
				} else if (returnSchemaName) {
					return "staxProduceXMLEventReader";
				} else if (returnXPathName) {
					return "staxProduceXMLEventReader";
				} else {
					return "StaxProduceXMLEventReader";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _StaxUseXMLEventReader) {
				if (returnConstName) {
					return STAXUSEXMLEVENTREADER;
				} else if (returnSchemaName) {
					return "staxUseXMLEventReader";
				} else if (returnXPathName) {
					return "staxUseXMLEventReader";
				} else {
					return "StaxUseXMLEventReader";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _OptionalScalars) {
				if (returnConstName) {
					return OPTIONALSCALARS;
				} else if (returnSchemaName) {
					return "optionalScalars";
				} else if (returnXPathName) {
					return "optionalScalars";
				} else {
					return "OptionalScalars";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _RespectExtension) {
				if (returnConstName) {
					return RESPECTEXTENSION;
				} else if (returnSchemaName) {
					return "respectExtension";
				} else if (returnXPathName) {
					return "respectExtension";
				} else {
					return "RespectExtension";
				}
			}
			if (((java.lang.Boolean)child).booleanValue() == _LogSuspicious) {
				if (returnConstName) {
					return LOGSUSPICIOUS;
				} else if (returnSchemaName) {
					return "logSuspicious";
				} else if (returnXPathName) {
					return "logSuspicious";
				} else {
					return "LogSuspicious";
				}
			}
		}
		if (childObj instanceof java.lang.Long) {
			java.lang.Long child = (java.lang.Long) childObj;
			if (((java.lang.Long)child).longValue() == _NewestSourceTime) {
				if (returnConstName) {
					return NEWESTSOURCETIME;
				} else if (returnSchemaName) {
					return "newestSourceTime";
				} else if (returnXPathName) {
					return "newestSourceTime";
				} else {
					return "NewestSourceTime";
				}
			}
		}
		if (childObj instanceof org.netbeans.modules.schema2beansdev.metadd.MetaDD) {
			org.netbeans.modules.schema2beansdev.metadd.MetaDD child = (org.netbeans.modules.schema2beansdev.metadd.MetaDD) childObj;
			if (child == _MetaDD) {
				if (returnConstName) {
					return METADD;
				} else if (returnSchemaName) {
					return "metaDD";
				} else if (returnXPathName) {
					return "metaDD";
				} else {
					return "MetaDD";
				}
			}
		}
		if (childObj instanceof java.lang.Integer) {
			java.lang.Integer child = (java.lang.Integer) childObj;
			if (((java.lang.Integer)child).intValue() == _IndentAmount) {
				if (returnConstName) {
					return INDENTAMOUNT;
				} else if (returnSchemaName) {
					return "indentAmount";
				} else if (returnXPathName) {
					return "indentAmount";
				} else {
					return "IndentAmount";
				}
			}
		}
		if (childObj instanceof java.io.File) {
			java.io.File child = (java.io.File) childObj;
			if (child == _Filename) {
				if (returnConstName) {
					return FILENAME;
				} else if (returnSchemaName) {
					return "filename";
				} else if (returnXPathName) {
					return "filename";
				} else {
					return "Filename";
				}
			}
			if (child == _RootDir) {
				if (returnConstName) {
					return ROOTDIR;
				} else if (returnSchemaName) {
					return "rootDir";
				} else if (returnXPathName) {
					return "rootDir";
				} else {
					return "RootDir";
				}
			}
			if (child == _MddFile) {
				if (returnConstName) {
					return MDDFILE;
				} else if (returnSchemaName) {
					return "mddFile";
				} else if (returnXPathName) {
					return "mddFile";
				} else {
					return "MddFile";
				}
			}
			if (child == _DelegateDir) {
				if (returnConstName) {
					return DELEGATEDIR;
				} else if (returnSchemaName) {
					return "delegateDir";
				} else if (returnXPathName) {
					return "delegateDir";
				} else {
					return "DelegateDir";
				}
			}
			if (child == _DumpBeanTree) {
				if (returnConstName) {
					return DUMPBEANTREE;
				} else if (returnSchemaName) {
					return "dumpBeanTree";
				} else if (returnXPathName) {
					return "dumpBeanTree";
				} else {
					return "DumpBeanTree";
				}
			}
			if (child == _GenerateDotGraph) {
				if (returnConstName) {
					return GENERATEDOTGRAPH;
				} else if (returnSchemaName) {
					return "generateDotGraph";
				} else if (returnXPathName) {
					return "generateDotGraph";
				} else {
					return "GenerateDotGraph";
				}
			}
			if (child == _WriteBeanGraphFile) {
				if (returnConstName) {
					return WRITEBEANGRAPHFILE;
				} else if (returnSchemaName) {
					return "writeBeanGraphFile";
				} else if (returnXPathName) {
					return "writeBeanGraphFile";
				} else {
					return "WriteBeanGraphFile";
				}
			}
			int index = 0;
			for (java.util.Iterator it = _ReadBeanGraphFiles.iterator(); 
				it.hasNext(); ) {
				java.io.File element = (java.io.File)it.next();
				if (child == element) {
					if (returnConstName) {
						return READBEANGRAPHFILES;
					} else if (returnSchemaName) {
						return "readBeanGraphFiles";
					} else if (returnXPathName) {
						return "readBeanGraphFiles[position()="+index+"]";
					} else {
						return "ReadBeanGraphFiles."+Integer.toHexString(index);
					}
				}
				++index;
			}
			if (child == _WriteConfig) {
				if (returnConstName) {
					return WRITECONFIG;
				} else if (returnSchemaName) {
					return "writeConfig";
				} else if (returnXPathName) {
					return "writeConfig";
				} else {
					return "WriteConfig";
				}
			}
			index = 0;
			for (java.util.Iterator it = _ReadConfig.iterator(); 
				it.hasNext(); ) {
				java.io.File element = (java.io.File)it.next();
				if (child == element) {
					if (returnConstName) {
						return READCONFIG;
					} else if (returnSchemaName) {
						return "readConfig";
					} else if (returnXPathName) {
						return "readConfig[position()="+index+"]";
					} else {
						return "ReadConfig."+Integer.toHexString(index);
					}
				}
				++index;
			}
		}
		if (childObj instanceof java.lang.String) {
			java.lang.String child = (java.lang.String) childObj;
			if (child.equals(_SchemaType)) {
				if (returnConstName) {
					return SCHEMATYPE;
				} else if (returnSchemaName) {
					return "schemaType";
				} else if (returnXPathName) {
					return "schemaType";
				} else {
					return "SchemaType";
				}
			}
			if (child.equals(_DocRoot)) {
				if (returnConstName) {
					return DOCROOT;
				} else if (returnSchemaName) {
					return "docRoot";
				} else if (returnXPathName) {
					return "docRoot";
				} else {
					return "DocRoot";
				}
			}
			if (child.equals(_PackagePath)) {
				if (returnConstName) {
					return PACKAGEPATH;
				} else if (returnSchemaName) {
					return "packagePath";
				} else if (returnXPathName) {
					return "packagePath";
				} else {
					return "PackagePath";
				}
			}
			if (child.equals(_Indent)) {
				if (returnConstName) {
					return INDENT;
				} else if (returnSchemaName) {
					return "indent";
				} else if (returnXPathName) {
					return "indent";
				} else {
					return "Indent";
				}
			}
			if (child.equals(_DelegatePackage)) {
				if (returnConstName) {
					return DELEGATEPACKAGE;
				} else if (returnSchemaName) {
					return "delegatePackage";
				} else if (returnXPathName) {
					return "delegatePackage";
				} else {
					return "DelegatePackage";
				}
			}
			if (child.equals(_GenerateCommonInterface)) {
				if (returnConstName) {
					return GENERATECOMMONINTERFACE;
				} else if (returnSchemaName) {
					return "generateCommonInterface";
				} else if (returnXPathName) {
					return "generateCommonInterface";
				} else {
					return "GenerateCommonInterface";
				}
			}
			if (child.equals(_InputURI)) {
				if (returnConstName) {
					return INPUTURI;
				} else if (returnSchemaName) {
					return "inputURI";
				} else if (returnXPathName) {
					return "inputURI";
				} else {
					return "InputURI";
				}
			}
			if (child.equals(_IndexedPropertyType)) {
				if (returnConstName) {
					return INDEXEDPROPERTYTYPE;
				} else if (returnSchemaName) {
					return "indexedPropertyType";
				} else if (returnXPathName) {
					return "indexedPropertyType";
				} else {
					return "IndexedPropertyType";
				}
			}
			int index = 0;
			for (java.util.Iterator it = _Finder.iterator(); it.hasNext(); 
				) {
				java.lang.String element = (java.lang.String)it.next();
				if (child.equals(element)) {
					if (returnConstName) {
						return FINDER;
					} else if (returnSchemaName) {
						return "finder";
					} else if (returnXPathName) {
						return "finder[position()="+index+"]";
					} else {
						return "Finder."+Integer.toHexString(index);
					}
				}
				++index;
			}
			if (child.equals(_Target)) {
				if (returnConstName) {
					return TARGET;
				} else if (returnSchemaName) {
					return "target";
				} else if (returnXPathName) {
					return "target";
				} else {
					return "Target";
				}
			}
			if (child.equals(_DefaultElementType)) {
				if (returnConstName) {
					return DEFAULTELEMENTTYPE;
				} else if (returnSchemaName) {
					return "defaultElementType";
				} else if (returnXPathName) {
					return "defaultElementType";
				} else {
					return "DefaultElementType";
				}
			}
		}
		if (childObj instanceof java.io.PrintStream) {
			java.io.PrintStream child = (java.io.PrintStream) childObj;
			if (child == _MessageOut) {
				if (returnConstName) {
					return MESSAGEOUT;
				} else if (returnSchemaName) {
					return "messageOut";
				} else if (returnXPathName) {
					return "messageOut";
				} else {
					return "MessageOut";
				}
			}
		}
		if (childObj instanceof org.netbeans.modules.schema2beansdev.CodeGeneratorFactory) {
			org.netbeans.modules.schema2beansdev.CodeGeneratorFactory child = (org.netbeans.modules.schema2beansdev.CodeGeneratorFactory) childObj;
			if (child == _CodeGeneratorFactory) {
				if (returnConstName) {
					return CODEGENERATORFACTORY;
				} else if (returnSchemaName) {
					return "codeGeneratorFactory";
				} else if (returnXPathName) {
					return "codeGeneratorFactory";
				} else {
					return "CodeGeneratorFactory";
				}
			}
		}
		if (childObj instanceof org.netbeans.modules.schema2beansdev.GenBeans.OutputStreamProvider) {
			org.netbeans.modules.schema2beansdev.GenBeans.OutputStreamProvider child = (org.netbeans.modules.schema2beansdev.GenBeans.OutputStreamProvider) childObj;
			if (child == _OutputStreamProvider) {
				if (returnConstName) {
					return OUTPUTSTREAMPROVIDER;
				} else if (returnSchemaName) {
					return "outputStreamProvider";
				} else if (returnXPathName) {
					return "outputStreamProvider";
				} else {
					return "OutputStreamProvider";
				}
			}
		}
		return null;
	}

	/**
	 * Return an array of all of the properties that are beans and are set.
	 */
	public java.lang.Object[] childBeans(boolean recursive) {
		java.util.List children = new java.util.LinkedList();
		childBeans(recursive, children);
		java.lang.Object[] result = new java.lang.Object[children.size()];
		return (java.lang.Object[]) children.toArray(result);
	}

	/**
	 * Put all child beans into the beans list.
	 */
	public void childBeans(boolean recursive, java.util.List beans) {
	}

	public boolean equals(Object o) {
		return o instanceof org.netbeans.modules.schema2beansdev.S2bConfig && equals((org.netbeans.modules.schema2beansdev.S2bConfig) o);
	}

	public boolean equals(org.netbeans.modules.schema2beansdev.S2bConfig inst) {
		if (inst == this) {
			return true;
		}
		if (inst == null) {
			return false;
		}
		if (!(_SchemaType == null ? inst._SchemaType == null : _SchemaType.equals(inst._SchemaType))) {
			return false;
		}
		if (_isSet_TraceParse != inst._isSet_TraceParse) {
			return false;
		}
		if (_isSet_TraceParse) {
			if (!(_TraceParse == inst._TraceParse)) {
				return false;
			}
		}
		if (_isSet_TraceGen != inst._isSet_TraceGen) {
			return false;
		}
		if (_isSet_TraceGen) {
			if (!(_TraceGen == inst._TraceGen)) {
				return false;
			}
		}
		if (_isSet_TraceMisc != inst._isSet_TraceMisc) {
			return false;
		}
		if (_isSet_TraceMisc) {
			if (!(_TraceMisc == inst._TraceMisc)) {
				return false;
			}
		}
		if (_isSet_TraceDot != inst._isSet_TraceDot) {
			return false;
		}
		if (_isSet_TraceDot) {
			if (!(_TraceDot == inst._TraceDot)) {
				return false;
			}
		}
		if (!(_Filename == null ? inst._Filename == null : _Filename.equals(inst._Filename))) {
			return false;
		}
		if (!(_FileIn == null ? inst._FileIn == null : _FileIn.equals(inst._FileIn))) {
			return false;
		}
		if (!(_DocRoot == null ? inst._DocRoot == null : _DocRoot.equals(inst._DocRoot))) {
			return false;
		}
		if (!(_RootDir == null ? inst._RootDir == null : _RootDir.equals(inst._RootDir))) {
			return false;
		}
		if (!(_PackagePath == null ? inst._PackagePath == null : _PackagePath.equals(inst._PackagePath))) {
			return false;
		}
		if (!(_Indent == null ? inst._Indent == null : _Indent.equals(inst._Indent))) {
			return false;
		}
		if (_isSet_IndentAmount != inst._isSet_IndentAmount) {
			return false;
		}
		if (_isSet_IndentAmount) {
			if (!(_IndentAmount == inst._IndentAmount)) {
				return false;
			}
		}
		if (!(_MddFile == null ? inst._MddFile == null : _MddFile.equals(inst._MddFile))) {
			return false;
		}
		if (!(_MddIn == null ? inst._MddIn == null : _MddIn.equals(inst._MddIn))) {
			return false;
		}
		if (!(_MetaDD == null ? inst._MetaDD == null : _MetaDD.equals(inst._MetaDD))) {
			return false;
		}
		if (_isSet_DoGeneration != inst._isSet_DoGeneration) {
			return false;
		}
		if (_isSet_DoGeneration) {
			if (!(_DoGeneration == inst._DoGeneration)) {
				return false;
			}
		}
		if (_isSet_ScalarException != inst._isSet_ScalarException) {
			return false;
		}
		if (_isSet_ScalarException) {
			if (!(_ScalarException == inst._ScalarException)) {
				return false;
			}
		}
		if (_isSet_DumpToString != inst._isSet_DumpToString) {
			return false;
		}
		if (_isSet_DumpToString) {
			if (!(_DumpToString == inst._DumpToString)) {
				return false;
			}
		}
		if (_isSet_Vetoable != inst._isSet_Vetoable) {
			return false;
		}
		if (_isSet_Vetoable) {
			if (!(_Vetoable == inst._Vetoable)) {
				return false;
			}
		}
		if (_isSet_Standalone != inst._isSet_Standalone) {
			return false;
		}
		if (_isSet_Standalone) {
			if (!(_Standalone == inst._Standalone)) {
				return false;
			}
		}
		if (_isSet_Auto != inst._isSet_Auto) {
			return false;
		}
		if (_isSet_Auto) {
			if (!(_Auto == inst._Auto)) {
				return false;
			}
		}
		if (!(_MessageOut == null ? inst._MessageOut == null : _MessageOut.equals(inst._MessageOut))) {
			return false;
		}
		if (!(_OutputStreamProvider == null ? inst._OutputStreamProvider == null : _OutputStreamProvider.equals(inst._OutputStreamProvider))) {
			return false;
		}
		if (_isSet_ThrowErrors != inst._isSet_ThrowErrors) {
			return false;
		}
		if (_isSet_ThrowErrors) {
			if (!(_ThrowErrors == inst._ThrowErrors)) {
				return false;
			}
		}
		if (_isSet_GenerateXMLIO != inst._isSet_GenerateXMLIO) {
			return false;
		}
		if (_isSet_GenerateXMLIO) {
			if (!(_GenerateXMLIO == inst._GenerateXMLIO)) {
				return false;
			}
		}
		if (_isSet_GenerateValidate != inst._isSet_GenerateValidate) {
			return false;
		}
		if (_isSet_GenerateValidate) {
			if (!(_GenerateValidate == inst._GenerateValidate)) {
				return false;
			}
		}
		if (_isSet_GeneratePropertyEvents != inst._isSet_GeneratePropertyEvents) {
			return false;
		}
		if (_isSet_GeneratePropertyEvents) {
			if (!(_GeneratePropertyEvents == inst._GeneratePropertyEvents)) {
				return false;
			}
		}
		if (_isSet_GenerateStoreEvents != inst._isSet_GenerateStoreEvents) {
			return false;
		}
		if (_isSet_GenerateStoreEvents) {
			if (!(_GenerateStoreEvents == inst._GenerateStoreEvents)) {
				return false;
			}
		}
		if (_isSet_GenerateTransactions != inst._isSet_GenerateTransactions) {
			return false;
		}
		if (_isSet_GenerateTransactions) {
			if (!(_GenerateTransactions == inst._GenerateTransactions)) {
				return false;
			}
		}
		if (_isSet_AttributesAsProperties != inst._isSet_AttributesAsProperties) {
			return false;
		}
		if (_isSet_AttributesAsProperties) {
			if (!(_AttributesAsProperties == inst._AttributesAsProperties)) {
				return false;
			}
		}
		if (_isSet_GenerateDelegator != inst._isSet_GenerateDelegator) {
			return false;
		}
		if (_isSet_GenerateDelegator) {
			if (!(_GenerateDelegator == inst._GenerateDelegator)) {
				return false;
			}
		}
		if (!(_DelegateDir == null ? inst._DelegateDir == null : _DelegateDir.equals(inst._DelegateDir))) {
			return false;
		}
		if (!(_DelegatePackage == null ? inst._DelegatePackage == null : _DelegatePackage.equals(inst._DelegatePackage))) {
			return false;
		}
		if (!(_GenerateCommonInterface == null ? inst._GenerateCommonInterface == null : _GenerateCommonInterface.equals(inst._GenerateCommonInterface))) {
			return false;
		}
		if (_isSet_DefaultsAccessable != inst._isSet_DefaultsAccessable) {
			return false;
		}
		if (_isSet_DefaultsAccessable) {
			if (!(_DefaultsAccessable == inst._DefaultsAccessable)) {
				return false;
			}
		}
		if (_isSet_UseInterfaces != inst._isSet_UseInterfaces) {
			return false;
		}
		if (_isSet_UseInterfaces) {
			if (!(_UseInterfaces == inst._UseInterfaces)) {
				return false;
			}
		}
		if (_isSet_GenerateInterfaces != inst._isSet_GenerateInterfaces) {
			return false;
		}
		if (_isSet_GenerateInterfaces) {
			if (!(_GenerateInterfaces == inst._GenerateInterfaces)) {
				return false;
			}
		}
		if (_isSet_KeepElementPositions != inst._isSet_KeepElementPositions) {
			return false;
		}
		if (_isSet_KeepElementPositions) {
			if (!(_KeepElementPositions == inst._KeepElementPositions)) {
				return false;
			}
		}
		if (_isSet_RemoveUnreferencedNodes != inst._isSet_RemoveUnreferencedNodes) {
			return false;
		}
		if (_isSet_RemoveUnreferencedNodes) {
			if (!(_RemoveUnreferencedNodes == inst._RemoveUnreferencedNodes)) {
				return false;
			}
		}
		if (!(_InputURI == null ? inst._InputURI == null : _InputURI.equals(inst._InputURI))) {
			return false;
		}
		if (!(_IndexedPropertyType == null ? inst._IndexedPropertyType == null : _IndexedPropertyType.equals(inst._IndexedPropertyType))) {
			return false;
		}
		if (_isSet_DoCompile != inst._isSet_DoCompile) {
			return false;
		}
		if (_isSet_DoCompile) {
			if (!(_DoCompile == inst._DoCompile)) {
				return false;
			}
		}
		if (_isSet_GenerateSwitches != inst._isSet_GenerateSwitches) {
			return false;
		}
		if (_isSet_GenerateSwitches) {
			if (!(_GenerateSwitches == inst._GenerateSwitches)) {
				return false;
			}
		}
		if (!(_DumpBeanTree == null ? inst._DumpBeanTree == null : _DumpBeanTree.equals(inst._DumpBeanTree))) {
			return false;
		}
		if (!(_GenerateDotGraph == null ? inst._GenerateDotGraph == null : _GenerateDotGraph.equals(inst._GenerateDotGraph))) {
			return false;
		}
		if (_isSet_ProcessComments != inst._isSet_ProcessComments) {
			return false;
		}
		if (_isSet_ProcessComments) {
			if (!(_ProcessComments == inst._ProcessComments)) {
				return false;
			}
		}
		if (_isSet_ProcessDocType != inst._isSet_ProcessDocType) {
			return false;
		}
		if (_isSet_ProcessDocType) {
			if (!(_ProcessDocType == inst._ProcessDocType)) {
				return false;
			}
		}
		if (_isSet_CheckUpToDate != inst._isSet_CheckUpToDate) {
			return false;
		}
		if (_isSet_CheckUpToDate) {
			if (!(_CheckUpToDate == inst._CheckUpToDate)) {
				return false;
			}
		}
		if (_isSet_GenerateParentRefs != inst._isSet_GenerateParentRefs) {
			return false;
		}
		if (_isSet_GenerateParentRefs) {
			if (!(_GenerateParentRefs == inst._GenerateParentRefs)) {
				return false;
			}
		}
		if (_isSet_GenerateHasChanged != inst._isSet_GenerateHasChanged) {
			return false;
		}
		if (_isSet_GenerateHasChanged) {
			if (!(_GenerateHasChanged == inst._GenerateHasChanged)) {
				return false;
			}
		}
		if (_isSet_NewestSourceTime != inst._isSet_NewestSourceTime) {
			return false;
		}
		if (_isSet_NewestSourceTime) {
			if (!(_NewestSourceTime == inst._NewestSourceTime)) {
				return false;
			}
		}
		if (!(_WriteBeanGraphFile == null ? inst._WriteBeanGraphFile == null : _WriteBeanGraphFile.equals(inst._WriteBeanGraphFile))) {
			return false;
		}
		if (sizeReadBeanGraphFiles() != inst.sizeReadBeanGraphFiles())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _ReadBeanGraphFiles.iterator(), it2 = inst._ReadBeanGraphFiles.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			java.io.File element = (java.io.File)it.next();
			java.io.File element2 = (java.io.File)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (sizeReadBeanGraphs() != inst.sizeReadBeanGraphs())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _ReadBeanGraphs.iterator(), it2 = inst._ReadBeanGraphs.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			org.netbeans.modules.schema2beansdev.beangraph.BeanGraph element = (org.netbeans.modules.schema2beansdev.beangraph.BeanGraph)it.next();
			org.netbeans.modules.schema2beansdev.beangraph.BeanGraph element2 = (org.netbeans.modules.schema2beansdev.beangraph.BeanGraph)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (_isSet_MinFeatures != inst._isSet_MinFeatures) {
			return false;
		}
		if (_isSet_MinFeatures) {
			if (!(_MinFeatures == inst._MinFeatures)) {
				return false;
			}
		}
		if (_isSet_ForME != inst._isSet_ForME) {
			return false;
		}
		if (_isSet_ForME) {
			if (!(_ForME == inst._ForME)) {
				return false;
			}
		}
		if (_isSet_Java5 != inst._isSet_Java5) {
			return false;
		}
		if (_isSet_Java5) {
			if (!(_Java5 == inst._Java5)) {
				return false;
			}
		}
		if (_isSet_GenerateTagsFile != inst._isSet_GenerateTagsFile) {
			return false;
		}
		if (_isSet_GenerateTagsFile) {
			if (!(_GenerateTagsFile == inst._GenerateTagsFile)) {
				return false;
			}
		}
		if (!(_CodeGeneratorFactory == null ? inst._CodeGeneratorFactory == null : _CodeGeneratorFactory.equals(inst._CodeGeneratorFactory))) {
			return false;
		}
		if (_isSet_GenerateTimeStamp != inst._isSet_GenerateTimeStamp) {
			return false;
		}
		if (_isSet_GenerateTimeStamp) {
			if (!(_GenerateTimeStamp == inst._GenerateTimeStamp)) {
				return false;
			}
		}
		if (_isSet_Quiet != inst._isSet_Quiet) {
			return false;
		}
		if (_isSet_Quiet) {
			if (!(_Quiet == inst._Quiet)) {
				return false;
			}
		}
		if (!(_WriteConfig == null ? inst._WriteConfig == null : _WriteConfig.equals(inst._WriteConfig))) {
			return false;
		}
		if (sizeReadConfig() != inst.sizeReadConfig())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _ReadConfig.iterator(), it2 = inst._ReadConfig.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			java.io.File element = (java.io.File)it.next();
			java.io.File element2 = (java.io.File)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (_isSet_MakeDefaults != inst._isSet_MakeDefaults) {
			return false;
		}
		if (_isSet_MakeDefaults) {
			if (!(_MakeDefaults == inst._MakeDefaults)) {
				return false;
			}
		}
		if (_isSet_SetDefaults != inst._isSet_SetDefaults) {
			return false;
		}
		if (_isSet_SetDefaults) {
			if (!(_SetDefaults == inst._SetDefaults)) {
				return false;
			}
		}
		if (_isSet_TrimNonStrings != inst._isSet_TrimNonStrings) {
			return false;
		}
		if (_isSet_TrimNonStrings) {
			if (!(_TrimNonStrings == inst._TrimNonStrings)) {
				return false;
			}
		}
		if (_isSet_UseRuntime != inst._isSet_UseRuntime) {
			return false;
		}
		if (_isSet_UseRuntime) {
			if (!(_UseRuntime == inst._UseRuntime)) {
				return false;
			}
		}
		if (_isSet_ExtendBaseBean != inst._isSet_ExtendBaseBean) {
			return false;
		}
		if (_isSet_ExtendBaseBean) {
			if (!(_ExtendBaseBean == inst._ExtendBaseBean)) {
				return false;
			}
		}
		if (sizeFinder() != inst.sizeFinder())
			return false;
		// Compare every element.
		for (java.util.Iterator it = _Finder.iterator(), it2 = inst._Finder.iterator(); 
			it.hasNext() && it2.hasNext(); ) {
			java.lang.String element = (java.lang.String)it.next();
			java.lang.String element2 = (java.lang.String)it2.next();
			if (!(element == null ? element2 == null : element.equals(element2))) {
				return false;
			}
		}
		if (!(_Target == null ? inst._Target == null : _Target.equals(inst._Target))) {
			return false;
		}
		if (_isSet_StaxProduceXMLEventReader != inst._isSet_StaxProduceXMLEventReader) {
			return false;
		}
		if (_isSet_StaxProduceXMLEventReader) {
			if (!(_StaxProduceXMLEventReader == inst._StaxProduceXMLEventReader)) {
				return false;
			}
		}
		if (_isSet_StaxUseXMLEventReader != inst._isSet_StaxUseXMLEventReader) {
			return false;
		}
		if (_isSet_StaxUseXMLEventReader) {
			if (!(_StaxUseXMLEventReader == inst._StaxUseXMLEventReader)) {
				return false;
			}
		}
		if (_isSet_OptionalScalars != inst._isSet_OptionalScalars) {
			return false;
		}
		if (_isSet_OptionalScalars) {
			if (!(_OptionalScalars == inst._OptionalScalars)) {
				return false;
			}
		}
		if (!(_DefaultElementType == null ? inst._DefaultElementType == null : _DefaultElementType.equals(inst._DefaultElementType))) {
			return false;
		}
		if (_isSet_RespectExtension != inst._isSet_RespectExtension) {
			return false;
		}
		if (_isSet_RespectExtension) {
			if (!(_RespectExtension == inst._RespectExtension)) {
				return false;
			}
		}
		if (_isSet_LogSuspicious != inst._isSet_LogSuspicious) {
			return false;
		}
		if (_isSet_LogSuspicious) {
			if (!(_LogSuspicious == inst._LogSuspicious)) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int result = 17;
		result = 37*result + (_SchemaType == null ? 0 : _SchemaType.hashCode());
		result = 37*result + (_isSet_TraceParse ? 0 : (_TraceParse ? 0 : 1));
		result = 37*result + (_isSet_TraceGen ? 0 : (_TraceGen ? 0 : 1));
		result = 37*result + (_isSet_TraceMisc ? 0 : (_TraceMisc ? 0 : 1));
		result = 37*result + (_isSet_TraceDot ? 0 : (_TraceDot ? 0 : 1));
		result = 37*result + (_Filename == null ? 0 : _Filename.hashCode());
		result = 37*result + (_FileIn == null ? 0 : _FileIn.hashCode());
		result = 37*result + (_DocRoot == null ? 0 : _DocRoot.hashCode());
		result = 37*result + (_RootDir == null ? 0 : _RootDir.hashCode());
		result = 37*result + (_PackagePath == null ? 0 : _PackagePath.hashCode());
		result = 37*result + (_Indent == null ? 0 : _Indent.hashCode());
		result = 37*result + (_isSet_IndentAmount ? 0 : (_IndentAmount));
		result = 37*result + (_MddFile == null ? 0 : _MddFile.hashCode());
		result = 37*result + (_MddIn == null ? 0 : _MddIn.hashCode());
		result = 37*result + (_MetaDD == null ? 0 : _MetaDD.hashCode());
		result = 37*result + (_isSet_DoGeneration ? 0 : (_DoGeneration ? 0 : 1));
		result = 37*result + (_isSet_ScalarException ? 0 : (_ScalarException ? 0 : 1));
		result = 37*result + (_isSet_DumpToString ? 0 : (_DumpToString ? 0 : 1));
		result = 37*result + (_isSet_Vetoable ? 0 : (_Vetoable ? 0 : 1));
		result = 37*result + (_isSet_Standalone ? 0 : (_Standalone ? 0 : 1));
		result = 37*result + (_isSet_Auto ? 0 : (_Auto ? 0 : 1));
		result = 37*result + (_MessageOut == null ? 0 : _MessageOut.hashCode());
		result = 37*result + (_OutputStreamProvider == null ? 0 : _OutputStreamProvider.hashCode());
		result = 37*result + (_isSet_ThrowErrors ? 0 : (_ThrowErrors ? 0 : 1));
		result = 37*result + (_isSet_GenerateXMLIO ? 0 : (_GenerateXMLIO ? 0 : 1));
		result = 37*result + (_isSet_GenerateValidate ? 0 : (_GenerateValidate ? 0 : 1));
		result = 37*result + (_isSet_GeneratePropertyEvents ? 0 : (_GeneratePropertyEvents ? 0 : 1));
		result = 37*result + (_isSet_GenerateStoreEvents ? 0 : (_GenerateStoreEvents ? 0 : 1));
		result = 37*result + (_isSet_GenerateTransactions ? 0 : (_GenerateTransactions ? 0 : 1));
		result = 37*result + (_isSet_AttributesAsProperties ? 0 : (_AttributesAsProperties ? 0 : 1));
		result = 37*result + (_isSet_GenerateDelegator ? 0 : (_GenerateDelegator ? 0 : 1));
		result = 37*result + (_DelegateDir == null ? 0 : _DelegateDir.hashCode());
		result = 37*result + (_DelegatePackage == null ? 0 : _DelegatePackage.hashCode());
		result = 37*result + (_GenerateCommonInterface == null ? 0 : _GenerateCommonInterface.hashCode());
		result = 37*result + (_isSet_DefaultsAccessable ? 0 : (_DefaultsAccessable ? 0 : 1));
		result = 37*result + (_isSet_UseInterfaces ? 0 : (_UseInterfaces ? 0 : 1));
		result = 37*result + (_isSet_GenerateInterfaces ? 0 : (_GenerateInterfaces ? 0 : 1));
		result = 37*result + (_isSet_KeepElementPositions ? 0 : (_KeepElementPositions ? 0 : 1));
		result = 37*result + (_isSet_RemoveUnreferencedNodes ? 0 : (_RemoveUnreferencedNodes ? 0 : 1));
		result = 37*result + (_InputURI == null ? 0 : _InputURI.hashCode());
		result = 37*result + (_IndexedPropertyType == null ? 0 : _IndexedPropertyType.hashCode());
		result = 37*result + (_isSet_DoCompile ? 0 : (_DoCompile ? 0 : 1));
		result = 37*result + (_isSet_GenerateSwitches ? 0 : (_GenerateSwitches ? 0 : 1));
		result = 37*result + (_DumpBeanTree == null ? 0 : _DumpBeanTree.hashCode());
		result = 37*result + (_GenerateDotGraph == null ? 0 : _GenerateDotGraph.hashCode());
		result = 37*result + (_isSet_ProcessComments ? 0 : (_ProcessComments ? 0 : 1));
		result = 37*result + (_isSet_ProcessDocType ? 0 : (_ProcessDocType ? 0 : 1));
		result = 37*result + (_isSet_CheckUpToDate ? 0 : (_CheckUpToDate ? 0 : 1));
		result = 37*result + (_isSet_GenerateParentRefs ? 0 : (_GenerateParentRefs ? 0 : 1));
		result = 37*result + (_isSet_GenerateHasChanged ? 0 : (_GenerateHasChanged ? 0 : 1));
		result = 37*result + (_isSet_NewestSourceTime ? 0 : ((int)(_NewestSourceTime^(_NewestSourceTime>>>32))));
		result = 37*result + (_WriteBeanGraphFile == null ? 0 : _WriteBeanGraphFile.hashCode());
		result = 37*result + (_ReadBeanGraphFiles == null ? 0 : _ReadBeanGraphFiles.hashCode());
		result = 37*result + (_ReadBeanGraphs == null ? 0 : _ReadBeanGraphs.hashCode());
		result = 37*result + (_isSet_MinFeatures ? 0 : (_MinFeatures ? 0 : 1));
		result = 37*result + (_isSet_ForME ? 0 : (_ForME ? 0 : 1));
		result = 37*result + (_isSet_Java5 ? 0 : (_Java5 ? 0 : 1));
		result = 37*result + (_isSet_GenerateTagsFile ? 0 : (_GenerateTagsFile ? 0 : 1));
		result = 37*result + (_CodeGeneratorFactory == null ? 0 : _CodeGeneratorFactory.hashCode());
		result = 37*result + (_isSet_GenerateTimeStamp ? 0 : (_GenerateTimeStamp ? 0 : 1));
		result = 37*result + (_isSet_Quiet ? 0 : (_Quiet ? 0 : 1));
		result = 37*result + (_WriteConfig == null ? 0 : _WriteConfig.hashCode());
		result = 37*result + (_ReadConfig == null ? 0 : _ReadConfig.hashCode());
		result = 37*result + (_isSet_MakeDefaults ? 0 : (_MakeDefaults ? 0 : 1));
		result = 37*result + (_isSet_SetDefaults ? 0 : (_SetDefaults ? 0 : 1));
		result = 37*result + (_isSet_TrimNonStrings ? 0 : (_TrimNonStrings ? 0 : 1));
		result = 37*result + (_isSet_UseRuntime ? 0 : (_UseRuntime ? 0 : 1));
		result = 37*result + (_isSet_ExtendBaseBean ? 0 : (_ExtendBaseBean ? 0 : 1));
		result = 37*result + (_Finder == null ? 0 : _Finder.hashCode());
		result = 37*result + (_Target == null ? 0 : _Target.hashCode());
		result = 37*result + (_isSet_StaxProduceXMLEventReader ? 0 : (_StaxProduceXMLEventReader ? 0 : 1));
		result = 37*result + (_isSet_StaxUseXMLEventReader ? 0 : (_StaxUseXMLEventReader ? 0 : 1));
		result = 37*result + (_isSet_OptionalScalars ? 0 : (_OptionalScalars ? 0 : 1));
		result = 37*result + (_DefaultElementType == null ? 0 : _DefaultElementType.hashCode());
		result = 37*result + (_isSet_RespectExtension ? 0 : (_RespectExtension ? 0 : 1));
		result = 37*result + (_isSet_LogSuspicious ? 0 : (_LogSuspicious ? 0 : 1));
		return result;
	}

	public String toString() {
		java.io.StringWriter sw = new java.io.StringWriter();
		try {
			writeNode(sw);
		} catch (java.io.IOException e) {
			// How can we actually get an IOException on a StringWriter?
			throw new RuntimeException(e);
		}
		return sw.toString();
	}

}

