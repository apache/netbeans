/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
/**
 * Delegate for S2bConfig
 * 
 * @Generated
 */

package org.netbeans.modules.s2banttask;

public class S2bConfigDelegator extends org.apache.tools.ant.Task {
	protected org.netbeans.modules.schema2beansdev.S2bConfig _S2bConfig;

	public S2bConfigDelegator(org.netbeans.modules.schema2beansdev.S2bConfig delegator) {
		_S2bConfig = delegator;
	}

	public S2bConfigDelegator() {
		_S2bConfig = new org.netbeans.modules.schema2beansdev.S2bConfig();
	}

	public S2bConfigDelegator(java.lang.String schemaType, java.io.File rootDir, java.lang.String indent, boolean doGeneration, boolean scalarException, boolean generateXMLIO, java.lang.String indexedPropertyType, boolean generateTimeStamp, boolean quiet, boolean makeDefaults) {
		_S2bConfig = new org.netbeans.modules.schema2beansdev.S2bConfig(schemaType,rootDir,indent,doGeneration,scalarException,generateXMLIO,indexedPropertyType,generateTimeStamp,quiet,makeDefaults);
	}

	public S2bConfigDelegator(org.netbeans.modules.schema2beansdev.S2bConfig source, boolean justData) {
		_S2bConfig = new org.netbeans.modules.schema2beansdev.S2bConfig(source,justData);
	}

	public String _getSchemaLocation() {
		return _S2bConfig._getSchemaLocation();
	}

	public void _setSchemaLocation(String location) {
		_S2bConfig._setSchemaLocation(location);
	}

	public int addFinder(java.lang.String value) {
		return _S2bConfig.addFinder(value);
	}

	public int addReadBeanGraphFiles(java.io.File value) {
		return _S2bConfig.addReadBeanGraphFiles(value);
	}

	public int addReadBeanGraphs(org.netbeans.modules.schema2beansdev.beangraph.BeanGraph value) {
		return _S2bConfig.addReadBeanGraphs(value);
	}

	public int addReadConfig(java.io.File value) {
		return _S2bConfig.addReadConfig(value);
	}

	public void changePropertyByName(String name, Object value) {
		_S2bConfig.changePropertyByName(name,value);
	}

	public java.lang.Object[] childBeans(boolean recursive) {
		return _S2bConfig.childBeans(recursive);
	}

	public void childBeans(boolean recursive, java.util.List beans) {
		_S2bConfig.childBeans(recursive,beans);
	}

	public boolean equals(Object o) {
		return _S2bConfig.equals(o);
	}

	public boolean equals(org.netbeans.modules.schema2beansdev.S2bConfig inst) {
		return _S2bConfig.equals(inst);
	}

	public java.util.List fetchFinderList() {
		return _S2bConfig.fetchFinderList();
	}

	public Object fetchPropertyByName(String name) {
		return _S2bConfig.fetchPropertyByName(name);
	}

	public java.util.List fetchReadBeanGraphFilesList() {
		return _S2bConfig.fetchReadBeanGraphFilesList();
	}

	public java.util.List fetchReadBeanGraphsList() {
		return _S2bConfig.fetchReadBeanGraphsList();
	}

	public java.util.List fetchReadConfigList() {
		return _S2bConfig.fetchReadConfigList();
	}

	public org.netbeans.modules.schema2beansdev.CodeGeneratorFactory getCodeGeneratorFactory() {
		return _S2bConfig.getCodeGeneratorFactory();
	}

	public java.lang.String getDefaultElementType() {
		return _S2bConfig.getDefaultElementType();
	}

	public java.io.File getDelegateDir() {
		return _S2bConfig.getDelegateDir();
	}

	public java.lang.String getDelegatePackage() {
		return _S2bConfig.getDelegatePackage();
	}

	public java.lang.String getDocRoot() {
		return _S2bConfig.getDocRoot();
	}

	public java.io.File getDumpBeanTree() {
		return _S2bConfig.getDumpBeanTree();
	}

	public java.io.InputStream getFileIn() {
		return _S2bConfig.getFileIn();
	}

	public java.io.File getFilename() {
		return _S2bConfig.getFilename();
	}

	public java.lang.String[] getFinder() {
		return _S2bConfig.getFinder();
	}

	public java.lang.String getFinder(int index) {
		return _S2bConfig.getFinder(index);
	}

	public java.lang.String getGenerateCommonInterface() {
		return _S2bConfig.getGenerateCommonInterface();
	}

	public java.io.File getGenerateDotGraph() {
		return _S2bConfig.getGenerateDotGraph();
	}

	public java.lang.String getIndent() {
		return _S2bConfig.getIndent();
	}

	public int getIndentAmount() {
		return _S2bConfig.getIndentAmount();
	}

	public java.lang.String getIndexedPropertyType() {
		return _S2bConfig.getIndexedPropertyType();
	}

	public java.lang.String getInputURI() {
		return _S2bConfig.getInputURI();
	}

	public java.io.File getMddFile() {
		return _S2bConfig.getMddFile();
	}

	public java.io.InputStream getMddIn() {
		return _S2bConfig.getMddIn();
	}

	public java.io.PrintStream getMessageOut() {
		return _S2bConfig.getMessageOut();
	}

	public org.netbeans.modules.schema2beansdev.metadd.MetaDD getMetaDD() {
		return _S2bConfig.getMetaDD();
	}

	public long getNewestSourceTime() {
		return _S2bConfig.getNewestSourceTime();
	}

	public org.netbeans.modules.schema2beansdev.GenBeans.OutputStreamProvider getOutputStreamProvider() {
		return _S2bConfig.getOutputStreamProvider();
	}

	public java.lang.String getPackagePath() {
		return _S2bConfig.getPackagePath();
	}

	public java.io.File[] getReadBeanGraphFiles() {
		return _S2bConfig.getReadBeanGraphFiles();
	}

	public java.io.File getReadBeanGraphFiles(int index) {
		return _S2bConfig.getReadBeanGraphFiles(index);
	}

	public org.netbeans.modules.schema2beansdev.beangraph.BeanGraph[] getReadBeanGraphs() {
		return _S2bConfig.getReadBeanGraphs();
	}

	public org.netbeans.modules.schema2beansdev.beangraph.BeanGraph getReadBeanGraphs(int index) {
		return _S2bConfig.getReadBeanGraphs(index);
	}

	public java.io.File[] getReadConfig() {
		return _S2bConfig.getReadConfig();
	}

	public java.io.File getReadConfig(int index) {
		return _S2bConfig.getReadConfig(index);
	}

	public java.io.File getRootDir() {
		return _S2bConfig.getRootDir();
	}

	public java.lang.String getSchemaType() {
		return _S2bConfig.getSchemaType();
	}

	public java.lang.String getTarget() {
		return _S2bConfig.getTarget();
	}

	public java.io.File getWriteBeanGraphFile() {
		return _S2bConfig.getWriteBeanGraphFile();
	}

	public java.io.File getWriteConfig() {
		return _S2bConfig.getWriteConfig();
	}

	public int hashCode() {
		return _S2bConfig.hashCode();
	}

	public boolean isAttributesAsProperties() {
		return _S2bConfig.isAttributesAsProperties();
	}

	public boolean isAuto() {
		return _S2bConfig.isAuto();
	}

	public boolean isCheckUpToDate() {
		return _S2bConfig.isCheckUpToDate();
	}

	public boolean isDefaultsAccessable() {
		return _S2bConfig.isDefaultsAccessable();
	}

	public boolean isDoCompile() {
		return _S2bConfig.isDoCompile();
	}

	public boolean isDoGeneration() {
		return _S2bConfig.isDoGeneration();
	}

	public boolean isDumpToString() {
		return _S2bConfig.isDumpToString();
	}

	public boolean isExtendBaseBean() {
		return _S2bConfig.isExtendBaseBean();
	}

	public boolean isForME() {
		return _S2bConfig.isForME();
	}

	public boolean isGenerateDelegator() {
		return _S2bConfig.isGenerateDelegator();
	}

	public boolean isGenerateHasChanged() {
		return _S2bConfig.isGenerateHasChanged();
	}

	public boolean isGenerateInterfaces() {
		return _S2bConfig.isGenerateInterfaces();
	}

	public boolean isGenerateParentRefs() {
		return _S2bConfig.isGenerateParentRefs();
	}

	public boolean isGeneratePropertyEvents() {
		return _S2bConfig.isGeneratePropertyEvents();
	}

	public boolean isGenerateStoreEvents() {
		return _S2bConfig.isGenerateStoreEvents();
	}

	public boolean isGenerateSwitches() {
		return _S2bConfig.isGenerateSwitches();
	}

	public boolean isGenerateTagsFile() {
		return _S2bConfig.isGenerateTagsFile();
	}

	public boolean isGenerateTimeStamp() {
		return _S2bConfig.isGenerateTimeStamp();
	}

	public boolean isGenerateTransactions() {
		return _S2bConfig.isGenerateTransactions();
	}

	public boolean isGenerateValidate() {
		return _S2bConfig.isGenerateValidate();
	}

	public boolean isGenerateXMLIO() {
		return _S2bConfig.isGenerateXMLIO();
	}

	public boolean isJava5() {
		return _S2bConfig.isJava5();
	}

	public boolean isKeepElementPositions() {
		return _S2bConfig.isKeepElementPositions();
	}

	public boolean isLogSuspicious() {
		return _S2bConfig.isLogSuspicious();
	}

	public boolean isMakeDefaults() {
		return _S2bConfig.isMakeDefaults();
	}

	public boolean isMinFeatures() {
		return _S2bConfig.isMinFeatures();
	}

	public boolean isOptionalScalars() {
		return _S2bConfig.isOptionalScalars();
	}

	public boolean isProcessComments() {
		return _S2bConfig.isProcessComments();
	}

	public boolean isProcessDocType() {
		return _S2bConfig.isProcessDocType();
	}

	public boolean isQuiet() {
		return _S2bConfig.isQuiet();
	}

	public boolean isRemoveUnreferencedNodes() {
		return _S2bConfig.isRemoveUnreferencedNodes();
	}

	public boolean isRespectExtension() {
		return _S2bConfig.isRespectExtension();
	}

	public boolean isScalarException() {
		return _S2bConfig.isScalarException();
	}

	public boolean isSetDefaults() {
		return _S2bConfig.isSetDefaults();
	}

	public boolean isStandalone() {
		return _S2bConfig.isStandalone();
	}

	public boolean isStaxProduceXMLEventReader() {
		return _S2bConfig.isStaxProduceXMLEventReader();
	}

	public boolean isStaxUseXMLEventReader() {
		return _S2bConfig.isStaxUseXMLEventReader();
	}

	public boolean isThrowErrors() {
		return _S2bConfig.isThrowErrors();
	}

	public boolean isTraceDot() {
		return _S2bConfig.isTraceDot();
	}

	public boolean isTraceGen() {
		return _S2bConfig.isTraceGen();
	}

	public boolean isTraceMisc() {
		return _S2bConfig.isTraceMisc();
	}

	public boolean isTraceParse() {
		return _S2bConfig.isTraceParse();
	}

	public boolean isTrimNonStrings() {
		return _S2bConfig.isTrimNonStrings();
	}

	public boolean isUseInterfaces() {
		return _S2bConfig.isUseInterfaces();
	}

	public boolean isUseRuntime() {
		return _S2bConfig.isUseRuntime();
	}

	public boolean isVetoable() {
		return _S2bConfig.isVetoable();
	}

	public String nameChild(Object childObj) {
		return _S2bConfig.nameChild(childObj);
	}

	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName) {
		return _S2bConfig.nameChild(childObj,returnConstName,returnSchemaName);
	}

	public String nameChild(Object childObj, boolean returnConstName, boolean returnSchemaName, boolean returnXPathName) {
		return _S2bConfig.nameChild(childObj,returnConstName,returnSchemaName,returnXPathName);
	}

	public String nameSelf() {
		return _S2bConfig.nameSelf();
	}

	public boolean parseArguments(String[] args) {
		return _S2bConfig.parseArguments(args);
	}

	public static S2bConfigDelegator read(java.io.File f) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return new S2bConfigDelegator(org.netbeans.modules.schema2beansdev.S2bConfig.read(f));
	}

	public static S2bConfigDelegator read(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return new S2bConfigDelegator(org.netbeans.modules.schema2beansdev.S2bConfig.read(in));
	}

	public static S2bConfigDelegator read(org.w3c.dom.Document document) {
		return new S2bConfigDelegator(org.netbeans.modules.schema2beansdev.S2bConfig.read(document));
	}

	public static S2bConfigDelegator read(org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return new S2bConfigDelegator(org.netbeans.modules.schema2beansdev.S2bConfig.read(in,validate,er,eh));
	}

	public static S2bConfigDelegator readNoEntityResolver(java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException {
		return new S2bConfigDelegator(org.netbeans.modules.schema2beansdev.S2bConfig.readNoEntityResolver(in));
	}

	public void readNode(org.w3c.dom.Node node) {
		_S2bConfig.readNode(node);
	}

	public void readNode(org.w3c.dom.Node node, java.util.Map namespacePrefixes) {
		_S2bConfig.readNode(node,namespacePrefixes);
	}

	public int removeFinder(java.lang.String value) {
		return _S2bConfig.removeFinder(value);
	}

	public int removeReadBeanGraphFiles(java.io.File value) {
		return _S2bConfig.removeReadBeanGraphFiles(value);
	}

	public int removeReadBeanGraphs(org.netbeans.modules.schema2beansdev.beangraph.BeanGraph value) {
		return _S2bConfig.removeReadBeanGraphs(value);
	}

	public int removeReadConfig(java.io.File value) {
		return _S2bConfig.removeReadConfig(value);
	}

	public void setAttributesAsProperties(boolean value) {
		_S2bConfig.setAttributesAsProperties(value);
	}

	public void setAuto(boolean value) {
		_S2bConfig.setAuto(value);
	}

	public void setCheckUpToDate(boolean value) {
		_S2bConfig.setCheckUpToDate(value);
	}

	public void setCodeGeneratorFactory(org.netbeans.modules.schema2beansdev.CodeGeneratorFactory value) {
		_S2bConfig.setCodeGeneratorFactory(value);
	}

	public void setDefaultElementType(java.lang.String value) {
		_S2bConfig.setDefaultElementType(value);
	}

	public void setDefaultsAccessable(boolean value) {
		_S2bConfig.setDefaultsAccessable(value);
	}

	public void setDelegateDir(java.io.File value) {
		_S2bConfig.setDelegateDir(value);
	}

	public void setDelegatePackage(java.lang.String value) {
		_S2bConfig.setDelegatePackage(value);
	}

	public void setDoCompile(boolean value) {
		_S2bConfig.setDoCompile(value);
	}

	public void setDoGeneration(boolean value) {
		_S2bConfig.setDoGeneration(value);
	}

	public void setDocRoot(java.lang.String value) {
		_S2bConfig.setDocRoot(value);
	}

	public void setDumpBeanTree(java.io.File value) {
		_S2bConfig.setDumpBeanTree(value);
	}

	public void setDumpToString(boolean value) {
		_S2bConfig.setDumpToString(value);
	}

	public void setExtendBaseBean(boolean value) {
		_S2bConfig.setExtendBaseBean(value);
	}

	public void setFileIn(java.io.InputStream value) {
		_S2bConfig.setFileIn(value);
	}

	public void setFilename(java.io.File value) {
		_S2bConfig.setFilename(value);
	}

	public void setFinder(int index, java.lang.String value) {
		_S2bConfig.setFinder(index,value);
	}

	public void setFinder(java.lang.String[] value) {
		_S2bConfig.setFinder(value);
	}

	public void setForME(boolean value) {
		_S2bConfig.setForME(value);
	}

	public void setGenerateCommonInterface(java.lang.String value) {
		_S2bConfig.setGenerateCommonInterface(value);
	}

	public void setGenerateDelegator(boolean value) {
		_S2bConfig.setGenerateDelegator(value);
	}

	public void setGenerateDotGraph(java.io.File value) {
		_S2bConfig.setGenerateDotGraph(value);
	}

	public void setGenerateHasChanged(boolean value) {
		_S2bConfig.setGenerateHasChanged(value);
	}

	public void setGenerateInterfaces(boolean value) {
		_S2bConfig.setGenerateInterfaces(value);
	}

	public void setGenerateParentRefs(boolean value) {
		_S2bConfig.setGenerateParentRefs(value);
	}

	public void setGeneratePropertyEvents(boolean value) {
		_S2bConfig.setGeneratePropertyEvents(value);
	}

	public void setGenerateStoreEvents(boolean value) {
		_S2bConfig.setGenerateStoreEvents(value);
	}

	public void setGenerateSwitches(boolean value) {
		_S2bConfig.setGenerateSwitches(value);
	}

	public void setGenerateTagsFile(boolean value) {
		_S2bConfig.setGenerateTagsFile(value);
	}

	public void setGenerateTimeStamp(boolean value) {
		_S2bConfig.setGenerateTimeStamp(value);
	}

	public void setGenerateTransactions(boolean value) {
		_S2bConfig.setGenerateTransactions(value);
	}

	public void setGenerateValidate(boolean value) {
		_S2bConfig.setGenerateValidate(value);
	}

	public void setGenerateXMLIO(boolean value) {
		_S2bConfig.setGenerateXMLIO(value);
	}

	public void setIndent(java.lang.String value) {
		_S2bConfig.setIndent(value);
	}

	public void setIndentAmount(int value) {
		_S2bConfig.setIndentAmount(value);
	}

	public void setIndexedPropertyType(java.lang.String value) {
		_S2bConfig.setIndexedPropertyType(value);
	}

	public void setInputURI(java.lang.String value) {
		_S2bConfig.setInputURI(value);
	}

	public void setJava5(boolean value) {
		_S2bConfig.setJava5(value);
	}

	public void setKeepElementPositions(boolean value) {
		_S2bConfig.setKeepElementPositions(value);
	}

	public void setLogSuspicious(boolean value) {
		_S2bConfig.setLogSuspicious(value);
	}

	public void setMakeDefaults(boolean value) {
		_S2bConfig.setMakeDefaults(value);
	}

	public void setMddFile(java.io.File value) {
		_S2bConfig.setMddFile(value);
	}

	public void setMddIn(java.io.InputStream value) {
		_S2bConfig.setMddIn(value);
	}

	public void setMessageOut(java.io.PrintStream value) {
		_S2bConfig.setMessageOut(value);
	}

	public void setMetaDD(org.netbeans.modules.schema2beansdev.metadd.MetaDD value) {
		_S2bConfig.setMetaDD(value);
	}

	public void setMinFeatures(boolean value) {
		_S2bConfig.setMinFeatures(value);
	}

	public void setNewestSourceTime(long value) {
		_S2bConfig.setNewestSourceTime(value);
	}

	public void setOptionalScalars(boolean value) {
		_S2bConfig.setOptionalScalars(value);
	}

	public void setOutputStreamProvider(org.netbeans.modules.schema2beansdev.GenBeans.OutputStreamProvider value) {
		_S2bConfig.setOutputStreamProvider(value);
	}

	public void setPackagePath(java.lang.String value) {
		_S2bConfig.setPackagePath(value);
	}

	public void setProcessComments(boolean value) {
		_S2bConfig.setProcessComments(value);
	}

	public void setProcessDocType(boolean value) {
		_S2bConfig.setProcessDocType(value);
	}

	public void setQuiet(boolean value) {
		_S2bConfig.setQuiet(value);
	}

	public void setReadBeanGraphFiles(int index, java.io.File value) {
		_S2bConfig.setReadBeanGraphFiles(index,value);
	}

	public void setReadBeanGraphFiles(java.io.File[] value) {
		_S2bConfig.setReadBeanGraphFiles(value);
	}

	public void setReadBeanGraphs(int index, org.netbeans.modules.schema2beansdev.beangraph.BeanGraph value) {
		_S2bConfig.setReadBeanGraphs(index,value);
	}

	public void setReadBeanGraphs(org.netbeans.modules.schema2beansdev.beangraph.BeanGraph[] value) {
		_S2bConfig.setReadBeanGraphs(value);
	}

	public void setReadConfig(int index, java.io.File value) {
		_S2bConfig.setReadConfig(index,value);
	}

	public void setReadConfig(java.io.File[] value) {
		_S2bConfig.setReadConfig(value);
	}

	public void setRemoveUnreferencedNodes(boolean value) {
		_S2bConfig.setRemoveUnreferencedNodes(value);
	}

	public void setRespectExtension(boolean value) {
		_S2bConfig.setRespectExtension(value);
	}

	public void setRootDir(java.io.File value) {
		_S2bConfig.setRootDir(value);
	}

	public void setScalarException(boolean value) {
		_S2bConfig.setScalarException(value);
	}

	public void setSchemaType(java.lang.String value) {
		_S2bConfig.setSchemaType(value);
	}

	public void setSetDefaults(boolean value) {
		_S2bConfig.setSetDefaults(value);
	}

	public void setStandalone(boolean value) {
		_S2bConfig.setStandalone(value);
	}

	public void setStaxProduceXMLEventReader(boolean value) {
		_S2bConfig.setStaxProduceXMLEventReader(value);
	}

	public void setStaxUseXMLEventReader(boolean value) {
		_S2bConfig.setStaxUseXMLEventReader(value);
	}

	public void setTarget(java.lang.String value) {
		_S2bConfig.setTarget(value);
	}

	public void setThrowErrors(boolean value) {
		_S2bConfig.setThrowErrors(value);
	}

	public void setTraceDot(boolean value) {
		_S2bConfig.setTraceDot(value);
	}

	public void setTraceGen(boolean value) {
		_S2bConfig.setTraceGen(value);
	}

	public void setTraceMisc(boolean value) {
		_S2bConfig.setTraceMisc(value);
	}

	public void setTraceParse(boolean value) {
		_S2bConfig.setTraceParse(value);
	}

	public void setTrimNonStrings(boolean value) {
		_S2bConfig.setTrimNonStrings(value);
	}

	public void setUseInterfaces(boolean value) {
		_S2bConfig.setUseInterfaces(value);
	}

	public void setUseRuntime(boolean value) {
		_S2bConfig.setUseRuntime(value);
	}

	public void setVetoable(boolean value) {
		_S2bConfig.setVetoable(value);
	}

	public void setWriteBeanGraphFile(java.io.File value) {
		_S2bConfig.setWriteBeanGraphFile(value);
	}

	public void setWriteConfig(java.io.File value) {
		_S2bConfig.setWriteConfig(value);
	}

	public void showHelp(java.io.PrintStream out) {
		_S2bConfig.showHelp(out);
	}

	public int sizeFinder() {
		return _S2bConfig.sizeFinder();
	}

	public int sizeReadBeanGraphFiles() {
		return _S2bConfig.sizeReadBeanGraphFiles();
	}

	public int sizeReadBeanGraphs() {
		return _S2bConfig.sizeReadBeanGraphs();
	}

	public int sizeReadConfig() {
		return _S2bConfig.sizeReadConfig();
	}

	public String toString() {
		return _S2bConfig.toString();
	}

	public void validate() throws org.netbeans.modules.schema2beansdev.S2bConfig.ValidateException {
		_S2bConfig.validate();
	}

	public void write(java.io.File f) throws java.io.IOException {
		_S2bConfig.write(f);
	}

	public void write(java.io.OutputStream out) throws java.io.IOException {
		_S2bConfig.write(out);
	}

	public void write(java.io.OutputStream out, String encoding) throws java.io.IOException {
		_S2bConfig.write(out,encoding);
	}

	public void write(java.io.Writer out, String encoding) throws java.io.IOException {
		_S2bConfig.write(out,encoding);
	}

	public void writeNode(java.io.Writer out) throws java.io.IOException {
		_S2bConfig.writeNode(out);
	}

	public void writeNode(java.io.Writer out, String nodeName, String indent) throws java.io.IOException {
		_S2bConfig.writeNode(out,nodeName,indent);
	}

	public void writeNode(java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map namespaceMap) throws java.io.IOException {
		_S2bConfig.writeNode(out,nodeName,namespace,indent,namespaceMap);
	}

	public static void writeXML(java.io.Writer out, String msg) throws java.io.IOException {
		org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out,msg);
	}

	public static void writeXML(java.io.Writer out, String msg, boolean attribute) throws java.io.IOException {
		org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out,msg,attribute);
	}

	public static void writeXML(java.io.Writer out, char msg, boolean attribute) throws java.io.IOException {
		org.netbeans.modules.schema2beansdev.S2bConfig.writeXML(out,msg,attribute);
	}

}
