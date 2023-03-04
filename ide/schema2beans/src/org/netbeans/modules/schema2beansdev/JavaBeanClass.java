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

import org.netbeans.modules.schema2beans.*;
import org.netbeans.modules.schema2beansdev.metadd.*;
import org.netbeans.modules.schema2beansdev.gen.*;

public class JavaBeanClass extends AbstractCodeGeneratorClass implements CodeGeneratorClass {
    // No element type number can be this one.
    protected final int elementTypeSetnull = -1;
    private Map/*QName, String*/ declaredQNames = new HashMap();
    private boolean loggerDeclared = false;

    JavaBeanClass(BeanBuilder.BeanElement be, GenBeans.Config config) {
        init(be, config);
    }

    /**
     *	Generate the java code in the out stream, using the optional
     *	metaDD bean graph.
     */
    public void generate(OutputStream out, MetaDD mdd) throws IOException {
        this.mdd = mdd;
        metaElement = getMetaElement(beanElement);
	
        if (metaElement != null && metaElement.isSkipGeneration()) {
            config.messageOut.println(Common.getMessage("MSG_SkippingGeneration"));
            return;
        }

        findAttributeOwners();
        
        genAllParts();

        select(DECL_SECTION);
        cr();

        printGenBuffers(out);
    }

    protected void genAllParts() throws IOException {
        genHeader(HEADER_SECTION);
        genPackage(HEADER_SECTION);
        genImports(HEADER_SECTION);
        genClassName(HEADER_SECTION);

        genConstructor();
        genAccessors();
        if (!config.isMinFeatures())
            genDeepCopy();

        if (config.isGenerateXMLIO() || config.isDumpToString()) {
            genXMLIO();
        }
        if (config.isStaxProduceXMLEventReader())
            genFetchXMLEventReader();
        
        if (config.isGenerateValidate()) {
            genValidate();
        }
        if (config.isGeneratePropertyEvents()) {
            genPropertyEvents();
        }
        if (config.isGenerateStoreEvents()) {
            genStoreEvents();
        }
        if (config.isVetoable()) {
            genVetoable();
        }
        if (config.isGenerateTransactions()) {
            genTransactions();
        }
        if (config.isGenerateHasChanged()) {
            genHasChanged();
        }
        if (config.isGenerateSwitches()) {
            generateSwitches();
        }
        if (!config.isMinFeatures())
            genPropertiesByName();
        if (config.isKeepElementPositions()) {
            genElementPositions();
        }
        if (beanElement.isRoot && config.isProcessDocType()) {
            genProcessDocType(config.isJava5());
        }

        if (!config.isMinFeatures()) {
            genName();

            genChildBeans();
        }
        genEqualsHashCode();
        if (config.isDumpToString()) {
            genToString();
        }
        if (config.isExtendBaseBean())
            genExtendBaseBean();

        genTrailer(TRAILER_SECTION);
        genFinishClass(TRAILER_SECTION);

        try {
            select(TRAILER_SECTION);
            printSchema();
        } catch (IOException ioe) {
            //	Do not generate the schema comment
        }
    }

    protected void genMadeChange() throws IOException {
        if (config.isGenerateHasChanged()) {
            jw.writeEol("_setChanged(true)");
        }
    }

    /**
     * Whenever we create a constructor, call this method first.
     */
    protected void genExtendBaseBeanConstructor() throws IOException {
        if (config.isExtendBaseBean()) {
            jw.writeEol("super(null, baseBeanRuntimeVersion)");
            //jw.writeEol("System.out.println(\"Created class \"+getClass())");
        }
    }

    public void genHeader(int out) throws IOException {
        select(out);
        gencr("/**");
        gencr(" *	This generated bean class " + className);
        gencr(" *	matches the schema element '" + beanElement.node.getName()+"'.");
        if (!beanElement.isRoot) {
            jw.writecr(" *  The root bean class is "+rootBeanElement.getClassType());
        }
        gencr(" *");
        printComment(" *	");
        if (config.isGenerateTimeStamp())
            gencr(" *	Generated on " + new Date());
        if (beanElement.isRoot) {
            gencr(" *");
            gencr(" *	This class matches the root element of the "+(config.getSchemaTypeNum() == GenBeans.Config.DTD ? "DTD" : "XML Schema")+",");
            gencr(" *	and is the root of the bean graph.");
            gencr(" *");
	    
            dumpBeanTree(jw, " * "+jw.getIndent(), jw.getIndent());
            gencr(" *");
        }
        gencr(" * @"+Common.GENERATED_TAG);
        gencr(" */"); cr();
    }

    public void genPackage(int out) {
        select(out);
        if (packageName != null) {
            gen(PACKAGE, packageName);
            eol();
            cr();
        }
    }

    public void genImports(int out) {
        select(out);
    }

    public void genClassName(int out) throws IOException {
        String name = null;
        String impName = null;
	
        select(out);
        jw.write("public ");
        // disable this for now: things are setup to new this class.
        if (false && beanElement.isAbstract)
            jw.write("abstract ");
        jw.write("class ", className);
        //gen(PUBLIC, CLASS, className);
	
        if (mdd != null) {
            if (metaElement != null) {
                name = metaElement.getExtends();
                impName = metaElement.getImplements();
            }
            if (name == null) {
                name = mdd.getExtends();
            }
            if (impName == null) {
                impName = mdd.getImplements();
            }
        }
	
        if (name != null) {
            gen(" extends ");
            gencr(name);
        }
        if (impName != null) {
            gen(" implements ", impName);
        }

        sp();
        begin();
    }

    public void genConstructor() throws IOException {
        int size = attrList.size();
        if (!config.isMinFeatures()) {
            jw.select(DECL_SECTION);
            for (int i = 0; i < size; i++) {
                Property a = (Property)attrList.get(i);
                jw.write("public static final String ", a.constName,
                         " = \"", a.name);
                jw.writeEolNoI18N("\"");
            }
            jw.cr();
        }
        
        jw.select(CONSTRUCTOR_SECTION);
        jw.bigComment("Normal starting point constructor.");
        jw.beginConstructor(className);
        if (config.isExtendBaseBean()) {
            jw.select(DECL_SECTION);
            jw.write("private static final org.netbeans.modules.schema2beans.Version baseBeanRuntimeVersion = new org.netbeans.modules.schema2beans.Version(" +
                     Version.MAJVER, ", ");
            jw.write(Version.MINVER + ", ");
            jw.writeEol(Version.PTCVER + ")");
            jw.select(CONSTRUCTOR_SECTION);
            jw.writeEol("this(null, baseBeanRuntimeVersion)");
            jw.end();
            jw.cr();
            jw.bigComment("This constructor is here for BaseBean compatibility.");
            jw.beginConstructor(className, "java.util.Vector comps, org.netbeans.modules.schema2beans.Version baseBeanRuntimeVersion");
        }
        genExtendBaseBeanConstructor();

        if (config.isMakeDefaults() || config.isSetDefaults()) {
            for (int i = 0; i < size; i++) {
                Property a = (Property)attrList.get(i);
                boolean indexed = a.isIndexed();
                boolean isScalar = a.isScalar();
                if (indexed || isScalar || a.ored)
                    continue;
                if (a.getDefaultValue() != null)
                    continue;
                String type = a.getType();
                String attr = "_"+a.name;
                if (a.elementInstance == Common.TYPE_1) {
                    // There has to be at least 1 of them.
                    List exceps = JavaUtil.exceptionsFromParsingText(type, false);
                    if (!exceps.isEmpty()) {
                        jw.beginTry();
                    }
                    gen(attr, " = ");
                    genNewDefault(a, true);
                    eol();
                    if (a.isBean && config.isGenerateParentRefs()) {
                        jw.writeEol(attr, "._setParent(this)");
                    }
                    if (!exceps.isEmpty()) {
                        end();
                        genRethrowExceptions(exceps);
                    }
                }
            }
        }
        jw.end();
        jw.cr();

        // Create a constructor that has all of the required parameters.
        List requiredParameters = new LinkedList();
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            // Is the property optional?
            if (!a.ored && (a.elementInstance == Common.TYPE_1 || a.elementInstance == Common.TYPE_1_N))
                requiredParameters.add(a);
        }
        if (!config.isMinFeatures() && requiredParameters.size() > 0) {
            String parameters = null;
            for (Iterator it = requiredParameters.iterator(); it.hasNext(); ) {
                Property a = (Property) it.next();
                if (parameters != null)
                    parameters += ", ";
                else
                    parameters = "";
                String type = a.getType();
                String baseType = type;
                if (a.isIndexed())
                    type = baseType + "[]";
                String fullClassType = getTypeFullClassName(a, type);
                parameters += fullClassType + " " + a.instanceOf();
            }
            jw.bigComment("Required parameters constructor");
            jw.beginConstructor(className, parameters);
            genExtendBaseBeanConstructor();
            for (Iterator it = requiredParameters.iterator(); it.hasNext(); ) {
                Property a = (Property) it.next();
                boolean indexed = a.isIndexed();
                String type = a.getType();
                String baseType = type;
                if (indexed)
                    type = baseType + "[]";
                String attr = "_"+a.name;
                SchemaRep.WhiteSpace ws = (SchemaRep.WhiteSpace) a.searchExtraData(SchemaRep.WhiteSpace.class);
                if (!indexed) {
                    if (ws != null)
                        genWhiteSpaceRestriction(ws, a.instanceOf(), baseType);
                    jw.write(attr, " = ");
                    jw.writeEol(a.instanceOf());
                    if (a.isBean && config.isGenerateParentRefs()) {
                        jw.beginIf(attr+" != null");
                        jw.writeEol(attr, "._setParent(this)");
                        jw.end();
                    } else if (config.isOptionalScalars() && a.isScalar()) {
                        jw.writeEol(a.getScalarIsSet(), " = true");
                    }
                } else {
                    jw.beginIf(a.instanceOf() + "!= null");
                    if ("java.util.ArrayList".equals(config.getIndexedPropertyType())) {
                        jw.write("((", config.getIndexedPropertyType(),
                                 ") ", attr);
                        jw.writeEol(").ensureCapacity(",
                                    a.instanceOf(), ".length)");
                    }
                    jw.beginFor("int i = 0", "i < "+a.instanceOf()+".length",
                                "++i");
                    if (ws != null)
                        genWhiteSpaceRestriction(ws, a.instanceOf()+"[i]",
                                                 baseType);
                    if (a.isBean && config.isGenerateParentRefs()) {
                        jw.beginIf(a.instanceOf()+"[i] != null");
                        jw.writeEol(a.instanceOf(), "[i]._setParent(this)");
                        jw.end();
                    }
                    jw.write(attr, ".add(",
                             JavaUtil.toObject(a.instanceOf()+"[i]", baseType,
                                               config.isForME(), config.isJava5()));
                    jw.writeEol(")");
                    jw.end();
                    jw.end();
                }
            }            
            jw.end();
            jw.cr();
        }
    }

    public void genAccessors() throws IOException {
        int size = attrList.size();
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            boolean indexed = a.isIndexed();
            boolean isArrayStyle = (config.getIndexedPropertyType() == null);
            boolean	isWrapper = false;
            boolean isScalar = a.isScalar();
            String 	scalarType = a.getScalarType();
            String attr = "_"+a.name;
            String propertyName = a.beanIntrospectorName();
            MetaElement	me = getMetaElement(a);

            String type = a.getType();
            String baseType = type;
            if (indexed)
                type = baseType + "[]";
            Signatures sigs = getSignatures(a);
            Signature sig;

            //System.out.println("a.name="+a.name+" a.type="+a.type+" a.dtdName="+a.dtdName+" isWrapper="+isWrapper+" a.classType="+a.classType+" me="+me);
            select(DECL_SECTION);
            if (indexed) {
                if (isArrayStyle) {
                    gen(PRIVATE, baseType+"[]", attr);
                    gen(" = new ", baseType, "[0]");
                    eol();
                } else {
                    String paramType = (config.jdkTarget >= 150
                        ? "<"+JavaUtil.toObjectType(baseType)+">"
                        : "");
                    gen(PRIVATE, "java.util.List" + paramType, attr);
                    gen(" = new "+config.getIndexedPropertyType()+paramType+"();");
                    tabIn();
                    if (config.jdkTarget < 150)
                        comment("List<"+baseType+">");
                    else
                        cr();
                }
            } else {
                gen(PRIVATE, type, attr);
                if (a.getDefaultValue() != null && (a.elementInstance == Common.TYPE_1 || a.elementInstance == Common.TYPE_1_N)) {
                    gen(" = ");
                    if (!JavaUtil.checkValueToType(type, a.getDefaultValue())) {
                        config.messageOut.println(Common.getMessage("MSG_NotAGoodValue", a.getDefaultValue(), type));
                    }
                    gen(JavaUtil.instanceFrom(type, a.getDefaultValue()));
                    eol();
                    if (config.isOptionalScalars() && a.isScalar()) {
                        gen(PRIVATE, "boolean", a.getScalarIsSet());
                        gen(" = true");
                        eol();
                    }
                } else {
                    eol();
                    if (config.isOptionalScalars() && a.isScalar()) {
                        gen(PRIVATE, "boolean", a.getScalarIsSet());
                        gen(" = false");
                        eol();
                    }
                }
            }

            select(ACCESS_SECTION);
            comment("This attribute is ", Common.instanceToString(a.elementInstance));
            // Generate setter
            if (config.jdkTarget >= 150) {
                boolean first = true;
                for (Iterator extraDataIt = a.extraDataIterator(); extraDataIt.hasNext(); ) {
                    Object extraData = extraDataIt.next();
                    if (extraData instanceof HasAnnotation) {
                        HasAnnotation annotator = (HasAnnotation) extraData;
                        if (first)
                            first = false;
                        else
                            jw.write(' ');
                        jw.write('@');
                        jw.write(annotator.genAnnotation());
                    }
                }
                if (!first)
                    jw.cr();
            }
            gen(sigs.findSignature(SETTER));
            sp();
            begin();
            if (a.getPropertyInterface() != null) {
                jw.writeEol(type+" value = ("+type+") valueInterface");
            }
            if (indexed) {
                gen("if (value == null)");
                cr();
                tabIn();
                if (baseType.equals("byte[]"))
                    jw.writeEol("value = new byte[0][0]");
                else
                    jw.writeEol("value = new ", baseType, "[0]");
            }
            if (!indexed) {
                SchemaRep.WhiteSpace ws = (SchemaRep.WhiteSpace) a.searchExtraData(SchemaRep.WhiteSpace.class);
                if (ws != null)
                    genWhiteSpaceRestriction(ws, "value", baseType);
            }
            if (config.isGeneratePropertyEvents() || config.isVetoable()) {
                if (indexed) {
                    jw.beginIf("value.length == "+a.getSizeMethod()+"()");
                    jw.writeEol("boolean same = true");
                    jw.beginFor("int i = 0", "i < value.length", "++i");
                    jw.beginIf("!("+JavaUtil.genEquals(type, "value[i]",
                                                  a.getReadMethod(true) + "(i)")+")");
                    jw.writeEol("same = false");
                    jw.writeEol("break");
                    jw.end();
                    jw.end();
                    jw.beginIf("same");
                } else {
                    jw.beginIf(JavaUtil.genEquals(type, "value", attr));
                }
                jw.comment("No change.");
                jw.writeEol("return");
                jw.end();
                if (indexed)
                    jw.end();

                if (a.isBean) {
                    if (config.isGeneratePropertyEvents())
                        comment("Make the foreign beans take on our property change event listeners.");
                    if (config.isGenerateParentRefs())
                        comment("Maintain the parent reference.");
                    String iterVar = "value";
                    if (indexed) {
                        jw.beginFor("int i = 0", "i < value.length", "++i");
                        iterVar = "value[i]";
                    }
                    jw.beginIf(iterVar+" != null");
                    if (config.isGeneratePropertyEvents())
                        geneol(iterVar+"._setPropertyChangeSupport(eventListeners)");
                    if (config.isVetoable())
                        geneol(iterVar+"._setVetoableChangeSupport(vetos)");
                    if (config.isGenerateParentRefs())
                        jw.writeEol(iterVar+"._setParent(this)");
                    jw.end();
                    if (indexed)
                        jw.end();
                }

                jw.writeEol("java.beans.PropertyChangeEvent event = null");
                gen("if (");
                jw.setFirst(" || ");
                if (config.isGeneratePropertyEvents()) {
                    jw.writeNext("eventListeners != null");
                }
                if (config.isGenerateStoreEvents()) {
                    jw.writeNext("storeEvents");
                }
                if (config.isVetoable()) {
                    jw.writeNext("vetos != null");
                }
                gen(") ");
                begin();
                if (indexed) {
                    jw.comment("See if only 1 thing changed.");
                    jw.writeEol("int addIndex = -1");
                    jw.writeEol("int removeIndex = -1");
                    jw.writeEol("int oldSize = size", a.name, "()");
                    jw.writeEol("int newSize = value.length");
                    jw.beginIf("oldSize + 1 == newSize || oldSize == newSize + 1");
                    jw.writeEol("boolean checkAddOrRemoveOne = true");
                    jw.writeEol("int oldIndex = 0, newIndex = 0");
                    jw.beginFor("",
                                "oldIndex < oldSize && newIndex < newSize",
                                "++newIndex, ++oldIndex");
                    //jw.writeEol("System.out.println(\"oldIndex=\"+oldIndex+\" newIndex=\"+newIndex)");
                    //jw.writeEol("System.out.println(\"addIndex=\"+addIndex+\" removeIndex=\"+removeIndex)");
                    jw.beginIf(JavaUtil.genEquals(baseType, "value[newIndex]",
                                                  a.getReadMethod(true)+"(oldIndex)"));
                    jw.comment("Same, so just continue.");
                    jw.endElseBeginIf("addIndex != -1 || removeIndex != -1");
                    jw.comment("More than 1 difference detected.");
                    jw.writeEol("addIndex = removeIndex = -1");
                    jw.writeEol("checkAddOrRemoveOne = false");
                    jw.writeEol("break");
                    jw.endElseBeginIf("oldIndex + 1 < oldSize && ("+JavaUtil.genEquals(baseType, "value[newIndex]", a.getReadMethod(true)+"(oldIndex+1)")+")");
                    jw.writeEol("removeIndex = oldIndex");
                    jw.writeEol("++oldIndex");
                    jw.endElseBeginIf("newIndex + 1 < newSize && ("+JavaUtil.genEquals(baseType, "value[newIndex+1]", a.getReadMethod(true)+"(oldIndex)")+")");
                    jw.writeEol("addIndex = newIndex");
                    jw.writeEol("++newIndex");
                    jw.endElseBegin();
                    jw.comment("More than 1 difference.");
                    jw.writeEol("addIndex = removeIndex = -1");
                    jw.writeEol("checkAddOrRemoveOne = false");
                    jw.writeEol("break");
                    jw.end();
                    jw.end();	// for
                    // Only do this check if we fell off the end (oldIndex == newIndex)
                    jw.beginIf("checkAddOrRemoveOne && addIndex == -1 && removeIndex == -1");
                    jw.beginIf("oldSize + 1 == newSize");
                    jw.comment("Added last one");
                    jw.writeEol("addIndex = oldSize");
                    jw.endElseBeginIf("oldSize == newSize + 1");
                    jw.comment("Removed last one");
                    jw.writeEol("removeIndex = newSize");
                    jw.end();
                    jw.end();
                    jw.end();

                    //jw.writeEol("System.out.println(\"addIndex=\"+addIndex+\" removeIndex=\"+removeIndex)");
                    jw.beginIf("addIndex >= 0");
                    jw.write("event = ");
                    genNewEvent(a, "addIndex", "null", "value[addIndex]", baseType);
                    jw.eol();
                    if (!isArrayStyle && !config.isVetoable() && !config.isKeepElementPositions() && !config.isGenerateStoreEvents()) {
                        SchemaRep.WhiteSpace ws = (SchemaRep.WhiteSpace) a.searchExtraData(SchemaRep.WhiteSpace.class);
                        if (ws != null)
                            genWhiteSpaceRestriction(ws, "value[addIndex]", baseType);
                        jw.writeEol(attr+".add(addIndex, value[addIndex])");
                        genMadeChange();
                        jw.writeEol("eventListeners.firePropertyChange(event)");
                        jw.writeEol("return");
                    }
                    jw.endElseBeginIf("removeIndex >= 0");
                    jw.write("event = ");
                    genNewEvent(a, "removeIndex", a.getReadMethod(true)+"(removeIndex)", "null", baseType);
                    jw.eol();
                    if (!isArrayStyle && !config.isVetoable() && !config.isKeepElementPositions() && !config.isGenerateStoreEvents()) {
                        jw.writeEol(attr+".remove(removeIndex)");
                        genMadeChange();
                        jw.writeEol("eventListeners.firePropertyChange(event)");
                        jw.writeEol("return");
                    }
                    jw.endElseBegin();
                }
                jw.write("event = ");
                genNewEvent(a, indexed ? "-1" : "",
                            a.getReadMethod(false)+"()",
                            "value", type);
                jw.eol();
                end();
                if (indexed) {
                    jw.end();
                }
            }
            if (config.isVetoable()) {
                gencr("if (vetos != null)");
                tabIn();
                geneol("vetos.fireVetoableChange(event)");
            }
            if (indexed) {
                if (config.isKeepElementPositions()) {
                    jw.comment("Figure out where this type belongs.");
                    jw.writeEol("int elementCount = fetchChildCount()");
                    jw.writeEol("int destPos = 0");
                    jw.writeEol("int srcPos = 0");
                    // Remove all of the old entries
                    jw.beginFor("", "destPos < elementCount && srcPos < value.length",
                                "++destPos");
                    jw.beginIf("elementTypesByPosition[destPos] == "+i);
                    jw.comment("replace it");
                    jw.writeEol("elementsByPosition[destPos] = value[srcPos++]");
                    jw.end();
                    jw.end();
                    jw.comment("Handle when the replacement array is smaller.");
                    jw.beginWhile("destPos < elementCount");
                    jw.beginIf("elementTypesByPosition[destPos] == "+i);
                    jw.writeEol("deleteElement(destPos)");
                    jw.writeEol("--elementCount");
                    jw.endElseBegin();
                    jw.writeEol("++destPos");
                    jw.end();
                    jw.end();
                    jw.comment("Handle when replacement array is larger.");
                    jw.beginFor("", "srcPos < value.length", "++srcPos");
                    jw.writeEol("insertElementByPosition(destPos++, ",
                                JavaUtil.toObject("value[srcPos]", baseType,
                                                  config.isForME(), config.isJava5()),
                                ", "+i+")");
                    jw.end();
                }
                if (isArrayStyle) {
                    jw.writeEol(attr, " = value");
                } else {
                    jw.writeEol(attr, ".clear()");
                    if ("java.util.ArrayList".equals(config.getIndexedPropertyType())) {
                        jw.writeEol("((", config.getIndexedPropertyType(),
                                    ") "+attr, ").ensureCapacity(value.length)");
                    }
                    jw.beginFor("int i = 0", "i < value.length", "++i");
                    SchemaRep.WhiteSpace ws = (SchemaRep.WhiteSpace) a.searchExtraData(SchemaRep.WhiteSpace.class);
                    if (ws != null)
                        genWhiteSpaceRestriction(ws, "value[i]", baseType);
                    jw.write(attr, ".add(");
                    String objectValue = JavaUtil.toObject("value[i]", baseType,
                                                           config.isForME(), config.isJava5());
                    jw.writeEol(objectValue, ")");
                    jw.end();
                }
            } else {
                geneol(attr+" = value");
                if (config.isOptionalScalars() && a.isScalar()) {
                    jw.writeEol(a.getScalarIsSet(), " = true");
                }
                if (config.isKeepElementPositions()) {
                    jw.comment("Figure out where this type belongs.");
                    jw.writeEol("int elementCount = fetchChildCount()");
                    jw.writeEol("int pos = findFirstOfElementType("+i+")");
                    if (!isScalar) {
                        jw.beginIf("value != null");
                    }
                    jw.beginIf("pos >= elementCount");
                    jw.comment("It's the last one to be added");
                    jw.writeEol("expandElementsByPosition(elementCount+1)");
                    jw.writeEol("elementTypesByPosition[pos] = "+i);
                    jw.end();
                    jw.writeEol("elementsByPosition[pos] = ",
                                JavaUtil.toObject("value", type,
                                                  config.isForME(), config.isJava5()));
                    if (!isScalar) {
                        jw.endElseBegin();
                        jw.beginIf("pos < elementCount");
                        jw.writeEol("deleteElement(pos)");
                        jw.end();
                        jw.end();
                    }
                }
            }
            genMadeChange();
            if (config.isGeneratePropertyEvents()) {
                if (config.isGenerateStoreEvents()) {
                    gencr("if (storeEvents)");
                    tabIn();
                    geneol("storedEvents.add(event)");
                    gen("else ");
                }
                gencr("if (event != null)");
                tabIn();
                geneol("eventListeners.firePropertyChange(event)");
            }

            genResetMutuallyExclusive(a, true);
            end();
            cr();

            if (indexed) {
                gen(sigs.findSignature(SETTERINDEXED));
                sp();
                begin();
                if (a.getPropertyInterface() != null) {
                    jw.writeEol(baseType+" value = ("+baseType+") valueInterface");
                }
                if (config.isGeneratePropertyEvents()) {
                    jw.beginIf(JavaUtil.genEquals(type, "value",
                                                  a.getReadMethod(true)+"(index)",
                                                  true));
                    jw.comment("No change.");
                    jw.writeEol("return");
                    jw.end();
                }
                if (a.isBean && config.isGenerateParentRefs()) {
                    jw.beginIf("value != null");
                    jw.writeEol("value._setParent(this)");
                    jw.end();
                }
                if (config.isGeneratePropertyEvents()) {
                    if (a.isBean) {
                        jw.beginIf("value != null");
                        comment("Make the foreign beans take on our property change event listeners.");
                        geneol("value._setPropertyChangeSupport(eventListeners)");
                        if (config.isVetoable())
                            geneol("value._setVetoableChangeSupport(vetos)");
                        jw.end();
                    }

                    gen("if (");
                    if (config.isGenerateStoreEvents()) {
                        gen("storeEvents || ");
                    }
                    gen("eventListeners != null) ");
                    begin();
                    jw.write("java.beans.PropertyChangeEvent event = ");
                    genNewEvent(a, "index", a.getReadMethod(true)+"(index)",
                                "value", baseType);
                    jw.eol();
                    if (config.isVetoable()) {
                        gencr("if (vetos != null)");
                        tabIn();
                        geneol("vetos.fireVetoableChange(event)");
                    }
                    if (config.isGenerateStoreEvents()) {
                        gencr("if (storeEvents)");
                        tabIn();
                        geneol("storedEvents.add(event)");
                        gencr("else");
                        tabIn();
                    }
                    geneol("eventListeners.firePropertyChange(event)");
                    end();
                }
                if (isArrayStyle) {
                    jw.writeEol(attr, "[index] = value");
                } else {
                    if (!a.isDirectChild()) {
                        // Check to see if we need to grow before we set.
                        jw.beginFor("int size = "+attr+".size()",
                                    "index >= size", "++size");
                        jw.writeEol(attr, ".add(null)");
                        jw.end();
                    }
                    jw.write(attr, ".set(index, ");
                    jw.write(JavaUtil.toObject("value", baseType, config.isForME(), config.isJava5()));
                    jw.writeEol(")");
                }
                if (config.isKeepElementPositions()) {
                    jw.writeEol("int pos = findElementType("+i+", index)");
                    jw.writeEol("elementsByPosition[pos] = ",
                                JavaUtil.toObject("value", baseType,
                                                  config.isForME(), config.isJava5()));
                }
                genMadeChange();
                end();
                cr();
            }

            // Generate getter
            gen(sigs.findSignature(GETTER));
            sp();
            begin();
            if (indexed && !isArrayStyle) {
                if (baseType.equals("byte[]"))
                    jw.writeEol(type, " arr = new byte[", attr, ".size()][]");
                else
                    jw.writeEol(type, " arr = new ", baseType+"["+attr, ".size()]");
                if (isScalar) {
                    jw.beginFor("int i = 0", "i < arr.length", "++i");
                    jw.write("arr[i] = ");
                    jw.write(JavaUtil.fromObject(baseType, attr+".get(i)"));
                    jw.eol();
                    jw.end();
                    jw.write("return arr");
                } else {
                    gen("return ("+type+") "+attr+".toArray(arr)");
                }
            } else
                gen("return "+attr);
            eol();
            end();
            cr();

            if (indexed) {
                if (!config.isMinFeatures() && !isArrayStyle) {
                    gen(sigs.findSignature(GETTERLIST));
                    sp();
                    begin();
                    gen("return "+attr);
                    eol();
                    end();
                    cr();
                }

                gen(sigs.findSignature(GETTERINDEXED));
                sp();
                begin();
                jw.write("return ");
                if (isArrayStyle) {
                    jw.writeEol(attr, "[index]");
                } else {
                    jw.writeEol(JavaUtil.fromObject(baseType, attr+".get(index)"));
                }
                end();
                cr();

                comment("Return the number of "+propertyName);
                gen(sigs.findSignature(SIZE));
                sp();
                begin();
                if (isArrayStyle) {
                    jw.writeEol("return ", attr, ".length");
                } else {
                    jw.writeEol("return ", attr, ".size()");
                }
                end();
                cr();

                if (!isArrayStyle) {
                    gen(sigs.findSignature(ADD));
                    sp();
                    begin();
                    if (a.getPropertyInterface() != null) {
                        jw.writeEol(baseType+" value = ("+baseType+") valueInterface");
                    }
                    if (a.isBean && config.isGenerateParentRefs()) {
                        jw.beginIf("value != null");
                        jw.writeEol("value._setParent(this)");
                        jw.end();
                    }
                    if (a.isBean && config.isGeneratePropertyEvents()) {
                        jw.beginIf("value != null");
                        comment("Make the foreign beans take on our property change event listeners.");
                        geneol("value._setPropertyChangeSupport(eventListeners)");
                        if (config.isVetoable())
                            geneol("value._setVetoableChangeSupport(vetos)");
                        jw.end();
                    }
                    jw.writeEol(attr, ".add(",
                                JavaUtil.toObject("value", baseType,
                                                  config.isForME(), config.isJava5()),
                                ")");
                    if (config.isKeepElementPositions()) {
                        GraphLink gl = a.getGraphLink();
                        //jw.comment("gl="+gl);
                        //jw.comment("gl.lastInGroup="+gl.getLastInGroup());
                        //jw.comment("gl.lastInGroup.object="+gl.getLastInGroup().getObject());
                        int lastPropNum;
                        if (gl == null) {
                            lastPropNum = i;
                        } else {
                            Property lastProp = (Property) gl.getLastSibling().getObject();
                            if (lastProp == a) {
                                lastPropNum = i;
                            } else {
                                for (lastPropNum = 0; lastPropNum < size; ++lastPropNum)
                                    if (attrList.get(lastPropNum) == lastProp)
                                        break;
                                if (lastPropNum == size) {
                                    jw.comment("Did not find lastPropNum");
                                    lastPropNum = i;
                                }
                            }
                        }
                        jw.writeEol("int pos = findLastOfElementType("+lastPropNum+")+1");
                        jw.writeEol("insertElementByPosition(pos, ",
                                    JavaUtil.toObject("value", baseType,
                                                      config.isForME(), config.isJava5()),
                                    ", "+i+")");
                    }
                    if (config.isGeneratePropertyEvents()) {
                        gen("if (");
                        if (config.isGenerateStoreEvents()) {
                            gen("storeEvents || ");
                        }
                        gen("eventListeners != null) ");
                        begin();
                        jw.write("java.beans.PropertyChangeEvent event = ");
                        genNewEvent(a, attr+".size()-1", "null",
                                    "value", baseType);
                        jw.eol();
                        if (config.isVetoable()) {
                            gencr("if (vetos != null)");
                            tabIn();
                            geneol("vetos.fireVetoableChange(event)");
                        }
                        if (config.isGenerateStoreEvents()) {
                            gencr("if (storeEvents)");
                            tabIn();
                            geneol("storedEvents.add(event)");
                            gencr("else");
                            tabIn();
                        }
                        geneol("eventListeners.firePropertyChange(event)");
                        end();
                    }
                    jw.writeEol("int positionOfNewItem = "+attr+".size()-1");
                    if (isMutuallyExclusive(a)) {
                        jw.beginIf("positionOfNewItem == 0");
                        genResetMutuallyExclusive(a, false);
                        jw.end();
                    }
                    genMadeChange();
                    geneol("return positionOfNewItem");
                    end();
                    cr();

                    jw.bigComment("Search from the end looking for @param value, and then remove it.");
                    gen(sigs.findSignature(REMOVE));
                    sp();
                    begin();
                    if (a.getPropertyInterface() != null) {
                        jw.writeEol(baseType+" value = ("+baseType+") valueInterface");
                    }
                    jw.writeEol("int pos = ", attr, ".indexOf(",
                                JavaUtil.toObject("value", baseType,
                                                  config.isForME(), config.isJava5())+")");
                    gen("if (pos >= 0) ");
                    begin();
                    geneol(attr+".remove(pos)");
                    if (config.isKeepElementPositions()) {
                        jw.writeEol("int elementPos = findElementType("+i+", pos)");
                        jw.writeEol("deleteElement(elementPos)");
                    }
                    if (config.isGeneratePropertyEvents()) {
                        gen("if (");
                        if (config.isGenerateStoreEvents()) {
                            gen("storeEvents || ");
                        }
                        gen("eventListeners != null) ");
                        begin();
                        gen("java.beans.PropertyChangeEvent event = ");
                        genNewEvent(a, "pos", "value", "null", baseType);
                        jw.eol();
                        if (config.isVetoable()) {
                            gencr("if (vetos != null)");
                            tabIn();
                            geneol("vetos.fireVetoableChange(event)");
                        }
                        if (config.isGenerateStoreEvents()) {
                            gencr("if (storeEvents)");
                            tabIn();
                            geneol("storedEvents.add(event)");
                            gencr("else");
                            tabIn();
                        }
                        geneol("eventListeners.firePropertyChange(event)");
                        end();
                    }
                    end();
                    genMadeChange();
                    geneol("return pos");
                    end();
                    cr();
                }

                MetaProperty mp = getMetaProperty(a.name);
                GraphNode graphNode = a.getGraphNode();
                SchemaRep.Key key = null;
                if (graphNode != null)
                    key = (SchemaRep.Key)
                        graphNode.searchExtraData(SchemaRep.Key.class);
                if (a.isBean &&
                    ((key != null) || (mp != null && mp.isKey()))) {
                    //System.out.println("mp for "+a.name);
                    //System.out.println(mp.dumpBeanNode());
                    config.messageOut.println("Found key: "+key);
                    SchemaRep.Selector selector = (SchemaRep.Selector)
                        key.findSubElement(SchemaRep.Selector.class);
                    SchemaRep.Field field = (SchemaRep.Field)
                        key.findSubElement(SchemaRep.Field.class);
                    genFinder(a, graphNode, attr, key.getElementName(),
                              baseType, a.name,
                              selector.getXPath(), field.getXPath(), false);
                }
            }  // end if (indexed)
            genDefaultsAccessable(a);

            if (a.isBean && !a.getBeanElement().isAbstract) {
                genNewMethod(a.getPropertyInterface(), baseType,
                             getTypeFullClassName(a));
            }
        }
        if (config.isKeepElementPositions()) {
            select(DECL_SECTION);
            jw.writeEol("private java.lang.Object[] elementsByPosition = new java.lang.Object[0]");  // we might be storing String, int's, and beans in there.
            jw.writeEol("private int[] elementTypesByPosition = new int[0]");
        }
        if (beanElement.isRoot && !config.isMinFeatures()) {
            select(DECL_SECTION);
            jw.write("private java.lang.String schemaLocation");
            if (mdd.getSchemaLocation() != null) {
                jw.write(" = ");
                jw.write(JavaUtil.instanceFrom("java.lang.String",
                                               mdd.getSchemaLocation()));
            }
            jw.eol();
            select(ACCESS_SECTION);
            jw.beginMethod("_setSchemaLocation", "String location", null, "void", jw.PUBLIC);
            jw.writeEol("schemaLocation = location");
            genMadeChange();
            jw.end();
            jw.cr();
            
            jw.beginMethod("_getSchemaLocation", "", null, "String", jw.PUBLIC);
            jw.writeEol("return schemaLocation");
            jw.endMethod();
        }
        if (config.isGenerateParentRefs()) {
            select(DECL_SECTION);
            String parentType = parentBeanType();
            jw.writeEol("private ", parentType, " parent");
            select(BODY_SECTION);
            jw.beginMethod("_setParent", parentType+" parent");
            jw.writeEol("this.parent = parent");
            jw.endMethod();

            jw.beginMethod("_getXPathExpr", "", null, "String", jw.PUBLIC);
            jw.beginIf("parent == null");
            jw.writeEol("return \"/", beanElement.node.getName(), "\"");
            jw.endElseBegin();
            jw.writeEol("String parentXPathExpr = parent._getXPathExpr()");
            jw.writeEol("String myExpr = parent.nameChild(this, false, false, true)");
            jw.writeEol("return parentXPathExpr + \"/\" + myExpr");
            jw.end();
            jw.endMethod();

            jw.beginMethod("_getXPathExpr", "Object childObj", null, "String", jw.PUBLIC);
            jw.writeEol("String childName = nameChild(childObj, false, false, true)");
            jw.beginIf("childName == null");
            jw.writeEol("throw new IllegalArgumentException(\"childObj (\"+childObj.toString()+\") is not a child of this bean ("+className+").\")");
            jw.end();
            jw.writeEol("return _getXPathExpr() + \"/\" + childName");
            jw.endMethod();
        }

        // Look for finders
        for (Iterator it = beanElement.getGraphNode().extraDataIterator(); it.hasNext(); ) {
            Object data = it.next();
            if (data instanceof BeanBuilder.Finder) {
                BeanBuilder.Finder finder = (BeanBuilder.Finder) data;
                //System.out.println("Found finder: "+finder);
                genFinder(beanElement.getGraphNode(), finder);
            }
        }
    }

    protected void genFinder(GraphNode startingGraphNode,
                             BeanBuilder.Finder finder) throws IOException {
        genFinder(startingGraphNode,
                  finder.getFindExpr(), finder.getByExpr(),
                  finder.isListFindExpr());
    }
    
    protected void genFinder(GraphNode startingGraphNode,
                             String selectorXPath, String fieldXPath,
                             boolean isListFindExpr) throws IOException {
        genFinder(null, startingGraphNode, null, null, null, null,
                  selectorXPath, fieldXPath, isListFindExpr);
    }
    
    protected void genFinder(Property a, GraphNode startingGraphNode,
                             String attr, String keyName, String returnType,
                             String nameToFind,
                             String selectorXPath, String fieldXPath,
                             boolean isListFindExpr) throws IOException {
        String dtdName = null;
        String keyVar = null;
        String keyType = null;
        String keyTypeInterface = null;
        List xpathChain = null;

        GraphNode graphNode = startingGraphNode;
        Property selectorProp = null;
        if (graphNode != null && graphNode.getGraphLink() != null) {
            xpathChain = new LinkedList();
            GraphLink gl = null;
            for (Iterator it = graphNode.getGraphLink().xPathIterator(selectorXPath);
                 it.hasNext(); ) {
                gl = (GraphLink) it.next();
                //System.out.println("selector gl="+gl);
                if (gl == null)
                    break;
                if (gl.getObject() != null)
                    xpathChain.add(gl);
            }
            if (gl != null) {
                GraphLink nextStartingLink;
                if (gl.element != null) {
                    nextStartingLink = gl.element.getGraphLink();
                } else {
                    nextStartingLink = gl;
                }
                selectorProp = (Property) gl.getObject();
                if (!fieldXPath.equals(".")) {
                    //System.out.println("nextStartingLink="+nextStartingLink);
                    for (Iterator it = nextStartingLink.xPathIterator(fieldXPath);
                         it.hasNext(); ) {
                        gl = (GraphLink) it.next();
                        //System.out.println("field gl="+gl);
                        if (gl == null) {
                            break;
                        }
                        if (gl.getObject() != null)
                            xpathChain.add(gl);
                    }
                }
                if (gl != null) {
                    //
                    // Found the final one.
                    //
                    //System.out.println("gl="+gl+" gl.name="+gl.name);
                    Property finalProp = (Property) gl.getObject();
                    if (finalProp != null) {
                        dtdName = finalProp.dtdName;
                        keyVar = finalProp.instanceOf()+"Key";
                        keyType = finalProp.getType();
                        keyTypeInterface = finalProp.getPropertyInterface();
                    } else {
                        config.messageOut.println("Warning: finalProp=null while generating finder.");
                    }
                }
            }
            if (gl == null)
                xpathChain = null;
        }
        //System.out.println("dtdName="+dtdName);

        if (dtdName == null) {
            dtdName = fieldXPath;
            keyVar = Common.convertNameInstance(dtdName);
            keyType = "String";
            jw.comment("Did not figure out proper expression to find the key.  This method may not work.");
            jw.comment("selectorXPath="+selectorXPath);
            if (selectorProp == null) {
                jw.comment("  Failed to find selector.");
            } else {
                jw.comment("  found selector property name="+selectorProp.dtdName);
            }
            jw.comment("fieldXPath="+fieldXPath);
            xpathChain = null;
        }

        if (selectorProp != null) {
            if (nameToFind == null)
                nameToFind = Common.convertName(selectorProp.dtdName);
            if (returnType == null) {
                if (selectorProp.getPropertyInterface() == null)
                    returnType = selectorProp.getType();
                else
                    returnType = selectorProp.getPropertyInterface();
            }
        }
        if (keyName == null)
            keyName = fieldXPath;
        if (returnType == null)
            returnType = "String";
        if (isListFindExpr) {
            returnType = "java.util.List/*<"+returnType+">*/";
            jw.bigComment("Search for the key.\n@return all elements found that match.");
        } else {
            jw.bigComment("Search for the key.\n@return  null if the key is not found.");
        }
        String methodName;
        if (isListFindExpr)
            methodName = "findAll"+nameToFind;
        else
            methodName = "find"+nameToFind;
        if (!keyName.equals("."))
            methodName = methodName+"By"+Common.convertName(keyName);
        String arguments;
        if (keyTypeInterface == null)
            arguments = keyType+" "+keyVar;
        else
            arguments = keyTypeInterface+" "+keyVar+"Interface";
        jw.beginMethod(methodName,
                       arguments, null, returnType,
                       jw.PUBLIC | jw.BEANINFO);
        if (keyTypeInterface != null)
            jw.writeEol(keyVar, " = ("+keyType, ") ", keyVar+"Interface");
        if (isListFindExpr)
            jw.writeEol("java.util.List _result = new java.util.ArrayList()");
        String lastVar, topVar;
        if (attr != null) {
            beginAttrIterator(attr, a, "_el");
            jw.beginIf("_el == null");
            jw.writeEol("continue");
            jw.end();
            lastVar = "_el";
            topVar = "_el";
        } else {
            lastVar = "this";
            topVar = "null";
        }
                    
        if (xpathChain != null) {
            GraphLink gl = null;
            int foundIndices = 0;
            for (Iterator it = xpathChain.iterator();
                 it.hasNext(); ) {
                gl = (GraphLink) it.next();
                Property prop = (Property) gl.getObject();
                String var = prop.instanceOf()+foundIndices;
                if (attr == null && selectorProp == prop)
                    topVar = var;
                if (prop.isIndexed()) {
                    ++foundIndices;
                    jw.beginFor("int "+var+"Index = 0",
                                var+"Index < "+lastVar+".size"+prop.name+"()",
                                var+"Index++");
                }
                jw.write(prop.getType()+" "+var);
                jw.write(" = (", prop.getType(), ") ");
                jw.write(lastVar, ".");
                jw.write(prop.getReadMethod(prop.isIndexed()));
                if (prop.isIndexed())
                    jw.write("("+var+"Index)");
                else
                    jw.write("()");
                jw.eol();
                jw.beginIf(var+" == null");
                if (!it.hasNext()) {
                    jw.beginIf(keyVar, " == null");
                    if (isListFindExpr)
                        jw.writeEol("_result.add(", topVar, ")");
                    else
                        jw.writeEol("return ", topVar);
                    jw.end();
                }
                jw.writeEol("continue");
                jw.end();
                lastVar = var;
            }
            jw.beginIf(lastVar+".equals("+keyVar+")");
            if (isListFindExpr)
                jw.writeEol("_result.add(", topVar, ")");
            else
                jw.writeEol("return ", topVar);
            jw.end();
            for (; foundIndices > 0; --foundIndices)
                jw.end();
        }

        if (attr != null)
            jw.end(); // endAttrIterator
        if (isListFindExpr)
            jw.writeEol("return _result");
        else
            jw.writeEol("return ", JavaUtil.nullValueForType(returnType));
        jw.endMethod();
    }

    public void genXMLIO() throws IOException {
        select(BODY_SECTION);
        String beanName = beanElement.node.getName();
        if (beanElement.isRoot) {
            if (!config.isStandalone()) {
                jw.beginMethod("write", "org.openide.filesystems.FileObject fo",
                               "java.io.IOException", "void", jw.PUBLIC | jw.IO);
                jw.writeEol("org.openide.filesystems.FileLock lock = fo.lock()");
                jw.beginTry();
                jw.writeEol("java.io.OutputStream out = fo.getOutputStream(lock)");
                jw.writeEol("write(out)");
                jw.writeEol("out.close()");
                jw.endFinallyBegin();
                jw.writeEol("lock.releaseLock()");
                jw.end();
                jw.endMethod();

                jw.beginMethod("write", "final org.openide.filesystems.FileObject dir, final String filename",
                               "java.io.IOException", "void", jw.PUBLIC | jw.IO);
                jw.writeEol("org.openide.filesystems.FileSystem fs = dir.getFileSystem()");                
                jw.write("fs.runAtomicAction(new org.openide.filesystems.FileSystem.AtomicAction()\n");
                jw.begin();
                jw.write("public void run() throws java.io.IOException {\n");
                jw.writeEol("org.openide.filesystems.FileObject file = dir.getFileObject(filename)");
                jw.beginIf("file == null");
                jw.writeEol("file = dir.createData(filename)");
                jw.end();
                jw.writeEol("write(file)");
                jw.end();
                jw.end();
                jw.writeEol(")");
                jw.endMethod();
            }

            jw.beginMethod("write", "java.io.File f",
                           "java.io.IOException", "void", jw.PUBLIC | jw.IO);
            jw.writeEol("java.io.OutputStream out = new java.io.FileOutputStream(f)");
            jw.beginTry();
            jw.writeEol("write(out)");
            jw.endFinallyBegin();
            jw.writeEol("out.close()");
            jw.end();
            jw.endMethod();

            jw.beginMethod("write", "java.io.OutputStream out",
                           "java.io.IOException", "void", jw.PUBLIC | jw.IO);
            geneol("write(out, null)");
            jw.endMethod();

            jw.beginMethod("write", "java.io.OutputStream out, String encoding",
                           "java.io.IOException", "void", jw.PUBLIC | jw.IO);
            geneol("java.io.Writer w");
            jw.beginIf("encoding == null");
            gen("encoding = \"UTF-8\"");
            eolNoI18N();
            end();
            gen("w = new java.io.BufferedWriter(new java.io.OutputStreamWriter(out, encoding))");
            eol();
            geneol("write(w, encoding)");
            geneol("w.flush()");
            jw.endMethod();

            jw.bigComment("Print this Java Bean to @param out including an XML header.\n@param encoding is the encoding style that @param out was opened with.");
            jw.beginMethod("write", "java.io.Writer out, String encoding",
                           "java.io.IOException", "void", jw.PUBLIC | jw.IO);
            gen("out.write(\"<?xml version='1.0'\")");
            eolNoI18N();
            gen("if (encoding != null)");
            cr();
            tabIn();
            gen("out.write(\" encoding='\"+encoding+\"'\")");
            eolNoI18N();
            gen("out.write(\" ?>\\n\")");
            eolNoI18N();
            if (config.isProcessDocType()) {
                jw.beginIf("docType != null");
                jw.writeEol("out.write(docType.toString())");
                jw.writeEol("out.write(\"\\n\")");
                jw.end();
            }
            jw.write("writeNode(out, \"", beanName, "\", \"\")");
            eolNoI18N();
            jw.endMethod();
        }

        jw.beginMethod("writeNode", "java.io.Writer out",
                       "java.io.IOException", "void", jw.PUBLIC | jw.IO);
        jw.writeEol("String myName");
        if (config.isGenerateParentRefs())
            jw.beginIf("parent == null");
        jw.writeEol("myName = \"", beanName, "\"");
        if (config.isGenerateParentRefs()) {
            jw.endElseBegin();
            jw.writeEol("myName = parent.nameChild(this, false, true)");
            jw.beginIf("myName == null");
            jw.writeEol("myName = \"", beanName, "\"");
            jw.end();
            jw.end();
        }
        jw.write("writeNode(out, myName, \"\")");
        eolNoI18N();
        jw.endMethod();

        jw.beginMethod("writeNode",
                       "java.io.Writer out, String nodeName, String indent",
                       "java.io.IOException", "void", jw.PUBLIC | jw.IO);
        jw.write("writeNode(out, nodeName, null, indent, new java.util.HashMap");
        if (config.jdkTarget >= 150)
            jw.write("<String, String>");
        jw.writeEol("())");
        jw.endMethod();

        jw.bigComment("It's not recommended to call this method directly.");
        String writeNodeArgs = "java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map";
        if (config.jdkTarget >= 150)
            writeNodeArgs += "<String, String>";
        writeNodeArgs += " namespaceMap";
        jw.beginMethod("writeNode", writeNodeArgs,
                       "java.io.IOException", "void", jw.PUBLIC | jw.IO | jw.UNSUPPORTED);
        int size = attrList.size();
        Map namespaceToPrefixTable = new HashMap();
        geneol("out.write(indent)");
        outWrite("<");
        jw.beginIf("namespace != null");
        jw.writeEol("out.write((String)namespaceMap.get(namespace))");
        outWrite(":");
        jw.end();
        geneol("out.write(nodeName)");
        if (beanElement.isRoot && getDefaultNamespace() != null) {
            jw.writeEolNoI18N("out.write(\" xmlns='\")");
            jw.writeEolNoI18N("out.write("+JavaUtil.instanceFrom("java.lang.String", getDefaultNamespace())+")");
            jw.writeEolNoI18N("out.write(\"'\")");
        }
        if (beanElement.isRoot) {
            jw.beginIf("schemaLocation != null");
            jw.writeEol("namespaceMap.put(\"http://www.w3.org/2001/XMLSchema-instance\", \"xsi\")");
            namespaceToPrefixTable.put("http://www.w3.org/2001/XMLSchema-instance",
                                       "xsi");
            jw.writeEol("out.write(\" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='\")");
            jw.writeEol("out.write(schemaLocation)");
            jw.writeEolNoI18N("out.write(\"'\")");
            jw.end();
        }

        // Deal with namespaces
        boolean firstNS = true;
        for (int i = 0; i < size; i++) {
            Property a = (Property) attrList.get(i);
            boolean indexed = a.isIndexed();
            String attr = "_" + a.name;
            String type = a.getType().intern();
            String baseType = type;
            if (indexed)
                type = (baseType + "[]").intern();
            if (isTypeQName(baseType)) {
                if (firstNS) {
                    firstNS = false;
                    jw.comment("Work out any namespaces.");
                    jw.writeEol("boolean firstNSAddition = true");
                }
                if (indexed) {
                    beginAttrIterator(attr, a, "element");
                    attr = "element";
                }
                jw.beginIf(attr+" != null && "+
                           attr+".getNamespaceURI() != null && !\"\".equals(",
                           attr, ".getNamespaceURI())");
                jw.writeEol("String prefix = (String) namespaceMap.get(", attr, ".getNamespaceURI())");
                jw.beginIf("prefix == null || \"\".equals(prefix)");
                jw.writeEol("prefix = ", attr, ".getPrefix()");
                jw.beginIf("prefix == null || \"\".equals(prefix)");
                jw.writeEol("prefix = \"", a.dtdName, "_ns__\"");
                jw.end();
                jw.comment("Need to make sure it's a unique prefix too.");
                jw.writeEol("boolean changed");
                jw.write("do ");
                jw.begin();
                jw.writeEol("changed = false");
                jw.beginFor("java.util.Iterator valueIt = namespaceMap.values().iterator()",
                            "valueIt.hasNext()", "");
                jw.writeEol("String otherPrefix = (String) valueIt.next()");
                jw.beginIf("prefix.equals(otherPrefix)");
                jw.writeEol("prefix += \"_\"");
                jw.writeEol("changed = true");
                jw.end();
                jw.end();
                jw.end(false);
                jw.writeEol(" while (changed)");
                jw.beginIf("firstNSAddition");
                jw.writeEol("firstNSAddition = false");
                jw.comment("Copy on write");
                jw.writeEol("namespaceMap = new java.util.HashMap(namespaceMap)");
                jw.end();
                jw.writeEol("namespaceMap.put(", attr,
                            ".getNamespaceURI(), prefix)");
                jw.writeEol("out.write(\" xmlns:\")");
                jw.writeEol("out.write(prefix)");
                jw.writeEol("out.write(\"='\")");
                jw.writeEol("out.write(", attr, ".getNamespaceURI())");
                jw.writeEol("out.write(\"'\")");
                jw.end();
                jw.end();
                if (indexed)
                    jw.end();
            } else if (a.getNamespace() != null && !a.getNamespace().equals(getDefaultNamespace()) && !a.getNamespace().equals("http://www.w3.org/XML/1998/namespace")) {
                if (namespaceToPrefixTable.containsKey(a.getNamespace()))
                    continue;
                if (firstNS) {
                    firstNS = false;
                    jw.comment("Work out any namespaces.");
                    jw.writeEol("boolean firstNSAddition = true");
                }
                String prefix;
                prefix = SchemaRep.prefixOf(a.dtdName);
                if (prefix == null) {
                    prefix = prefixGuesser.guessPrefixFromURI(a.getNamespace());
                    //config.messageOut.println("Guessing prefix for "+a.getNamespace()+" is "+prefix);
                }
                jw.beginIf("namespaceMap.get(",
                           JavaUtil.instanceFrom("String",
                                                 a.getNamespace()),
                           ") == null");
                jw.beginIf("firstNSAddition");
                jw.writeEol("firstNSAddition = false");
                jw.comment("Copy on write");
                jw.write("namespaceMap = new java.util.HashMap");
                if (config.jdkTarget >= 150)
                    jw.write("<String, String>");
                jw.writeEol("(namespaceMap)");
                jw.end();
                jw.writeEol("namespaceMap.put(",
                            JavaUtil.instanceFrom("String",
                                                  a.getNamespace()),
                            ", \"", prefix+"\")");
                outWrite(" xmlns:"+prefix+"='");
                outWrite(a.getNamespace());
                outWrite("'");
                jw.end();
                namespaceToPrefixTable.put(a.getNamespace(), prefix);
            }
        }
        List directAttributes = new LinkedList();       // List<Property>
        List nonDirectAttributes = new LinkedList();	// List<Property>
        boolean hasSubElements = false;
        if (size > 0) {
            // Go over the attributes
            for (int i = 0; i < size; i++) {
                Property a = (Property) attrList.get(i);
                if (!a.isAttribute()) {
                    hasSubElements = true;
                    continue;
                }
                if (!a.isDirectChild()) {
                    nonDirectAttributes.add(a);
                    continue;
                }
                directAttributes.add(a);
            }
        }
        jw.writeEol("writeNodeAttributes(out, nodeName, namespace, indent, namespaceMap)");

        if (hasSubElements) {
            geneol("out.write(\">\\n\")");
        } else {
        }                
            
        jw.writeEol("writeNodeChildren(out, nodeName, namespace, indent, namespaceMap)");
        if (hasSubElements) {
            geneol("out.write(indent)");
            outWrite("</");
            jw.beginIf("namespace != null");
            jw.writeEol("out.write((String)namespaceMap.get(namespace))");
            outWrite(":");
            jw.end();
            jw.writeEol("out.write(nodeName)");
            outWrite(">\n");
        } else {
            geneol("out.write(\"/>\\n\")");
        }
        jw.endMethod();  // writeNode
        
        jw.beginMethod("writeNodeAttributes", writeNodeArgs,
                       "java.io.IOException", "void", jw.PROTECTED | jw.IO | jw.UNSUPPORTED);
        for (Iterator it = directAttributes.iterator(); it.hasNext(); ) {
            Property prop = (Property) it.next();
            genWriteAttr(prop);
        }
        if (config.isRespectExtension() && beanElement.getExtension() != null) {
            comment("extension is: "+beanElement.getExtension());
            jw.writeEol("super.writeNodeAttributes(out, nodeName, namespace, indent, namespaceMap)");
        }
        jw.endMethod();
        
        /*
        String writeNodeArgs = "java.io.Writer out, String nodeName, String namespace, String indent, java.util.Map";
        if (config.jdkTarget >= 150)
            writeNodeArgs += "<String, String>";
        writeNodeArgs += " namespaceMap";
         */
        jw.beginMethod("writeNodeChildren", writeNodeArgs,
                       "java.io.IOException", "void", jw.PROTECTED | jw.IO | jw.UNSUPPORTED);
        if (config.isRespectExtension() && beanElement.getExtension() != null) {
            jw.writeEol("super.writeNodeChildren(out, nodeName, namespace, indent, namespaceMap)");
        }
        if (hasSubElements) {
            geneol("String nextIndent = indent + \""+jw.getIndent()+"\"");
            if (config.isKeepElementPositions()) {
                jw.beginFor("int position = 0, count = fetchChildCount()",
                            "position < count", "++position");
                jw.writeEol("java.lang.Object child = elementsByPosition[position]");
                jw.writeEol("int elementType = elementTypesByPosition[position]");
                jw.write("switch (elementType) ");
                jw.begin();
            }
            boolean firstUseOfIndex = true;
            for (int i = 0; i < size; i++) {
                Property a = (Property)attrList.get(i);
                if (a.isAttribute())
                    continue;
                boolean indexed = a.isIndexed();
                String attr = "_" + a.name;
                String baseAttr = attr;
	    
                boolean isScalar = a.isScalar();
                boolean isNamespaceSignificant = (a.getNamespace() != null && !a.getNamespace().equals(getDefaultNamespace()) && !a.getNamespace().equals("http://www.w3.org/XML/1998/namespace"));

                String type = a.getType().intern();
                String baseType = type;
                if (indexed)
                    type = (baseType + "[]").intern();

                if (config.isKeepElementPositions()) {
                    jw.writecr("case "+i+":");
                    jw.indentRight();
                    jw.writeEol(baseType+" a"+a.name+" = "+JavaUtil.fromObject(baseType, "child"));
                    attr = "a"+a.name;
                } else {
                    if (indexed) {
                        if (!a.isBean && a.attributes != null && a.attributes.length > 0) {
                            if (firstUseOfIndex) {
                                firstUseOfIndex = false;
                                jw.writeEol("int index = 0");
                            } else {
                                jw.writeEol("index = 0");
                            }
                        }
                        beginAttrIterator(attr, a, "element");
                        attr = "element";
                    }
                    if (!isScalar) {
                        jw.beginIf(attr, " != null");
                    } else if (!indexed && config.isOptionalScalars() && isScalar) {
                        jw.beginIf(a.getScalarIsSet());
                    }
                }
                if (a.isBean) {
                    jw.write(attr, ".writeNode(out, \"", a.dtdName);
                    jw.write("\", ");
                    if (isNamespaceSignificant)
                        jw.write(JavaUtil.instanceFrom("String",
                                                       a.getNamespace()));
                    else
                        jw.write("null");
                    jw.writeEol(", nextIndent, namespaceMap)");
                } else if (a.type == Common.TYPE_COMMENT) {
                    jw.writeEol("out.write(nextIndent)");
                    jw.writeEol("out.write(\"<!--\")");
                    jw.writeEol("out.write(", attr, ")");
                    jw.writeEol("out.write(\"-->\\n\")");
                } else if ("org.w3c.dom.Element".equals(type)) {
                    jw.writeEol("out.write(nextIndent)");
                    if (config.isUseRuntime()) {
                        jw.writeEol("org.netbeans.modules.schema2beans.XMLUtil.DOMWriter domWriter = new org.netbeans.modules.schema2beans.XMLUtil.DOMWriter()");
                        jw.writeEol("domWriter.setWriter(out)");
                        jw.writeEol("domWriter.write(", attr, ")");
                    } else {
                        jw.writeEol("out.write(", attr, ".toString())");
                    }
                    outWrite("\n");
                } else {
                    boolean needEndTag = true;
                    if (baseType == "boolean" && a.getCanBeEmpty()) {
                        jw.beginIf(attr);
                        needEndTag = false;
                    }
                    if (!a.dtdName.equals("#PCDATA")) {
                        jw.writeEol("out.write(nextIndent)");
                        if (isNamespaceSignificant) {
                            outWrite("<");
                            jw.writeEol("out.write((String)namespaceMap.get(",
                                        JavaUtil.instanceFrom("String",
                                                              a.getNamespace()),
                                        "))");
                            jw.write("out.write(\":", a.dtdName, "\")");
                        } else {
                            jw.write("out.write(\"<", a.dtdName, "\")");
                        }
                        eolNoI18N();
                        if (isTypeQName(baseType)) {
                            jw.writeEol("String nsPrefix", attr, " = null");
                            jw.beginIf(attr+".getNamespaceURI() != null && !\"\".equals(",
                                        attr, ".getNamespaceURI())");
                            jw.write("nsPrefix", attr, " = (String) namespaceMap.get(");
                            jw.writeEol(attr, ".getNamespaceURI())");
                            jw.end();
                        }
                        for (int attrNum = 0; attrNum < a.attributes.length; ++attrNum) {
                            AttrProp myAttrProp = a.attributes[attrNum];
                            //jw.comment("myAttrProp="+myAttrProp);
                            for (Iterator it = nonDirectAttributes.iterator(); it.hasNext(); ) {
                                Property attrProperty = (Property) it.next();
                                AttrProp attrProp = attrProperty.getAttrProp();
                                if (myAttrProp == attrProp) {
                                    String varName;
                                    if (attrProperty.isIndexed()) {
                                        jw.beginIf("index < size"+attrProperty.name+"()");
                                        varName = attrProperty.getReadMethod(true)+"(index)";
                                    } else
                                        varName = "_"+attrProperty.name;
                                    genWriteAttr(attrProperty, varName);
                                    if (attrProperty.isIndexed()) {
                                        jw.end();
                                    }
                                }
                            }
                        }
                        if (needEndTag) {
                            gen("out.write(\">\")");
                            eolNoI18N();
                        }
                    }
                    if (!needEndTag) {
                        // no end tag, then no contents
                    } else {
                        genWriteType(a, attr, false);
                    }
                    if (!needEndTag) {
                        gen("out.write(\"/>\\n\")");
                        eolNoI18N();
                        end();
                    } else if (!a.dtdName.equals("#PCDATA")) {
                        if (isNamespaceSignificant) {
                            outWrite("</");
                            jw.writeEol("out.write((String)namespaceMap.get(",
                                        JavaUtil.instanceFrom("String",
                                                              a.getNamespace()),
                                        "))");
                            jw.write("out.write(\":", a.dtdName, ">\\n\")");
                        } else {
                            jw.write("out.write(\"</"+a.dtdName, ">\\n\")");
                        }
                        eolNoI18N();
                    }
                }
                if (config.isKeepElementPositions()) {
                    jw.writeEol("break");
                    jw.indentLeft();
                } else {
                    if (a.isNillable()) {
                        if (!isScalar) {
                            jw.endElseBegin();
                            jw.writeEol("out.write(nextIndent)");
                            jw.writeEol("out.write(\"<", a.dtdName, " xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:nil='true'/>\\n\")");
                        }
                    }
                    if (!isScalar || (!indexed && config.isOptionalScalars() && isScalar))
                        end();
                    if (indexed) {
                        if (!a.isBean && a.attributes != null && a.attributes.length > 0) {
                            jw.writeEol("++index");
                        }
                        end();
                    }
                }
            }
            if (config.isKeepElementPositions()) {
                jw.end();
                jw.end();
            }
        }
        jw.endMethod(); // writeNodeChildren

        // Generate for reading
        if (beanElement.isRoot) {
            String exceps = "javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException";
            if (config.isVetoable())
                exceps += ", java.beans.PropertyVetoException";

            if (!config.isStandalone()) {
                jw.beginMethod("read", "org.openide.filesystems.FileObject fo",
                               exceps, className, jw.PUBLIC | jw.STATIC | jw.IO);
                jw.writeEol("java.io.InputStream in = fo.getInputStream()");
                jw.beginTry();
                jw.writeEol("return read(in)");
                jw.endFinallyBegin();
                jw.writeEol("in.close()");
                jw.end();
                jw.endMethod();
            }

            jw.beginMethod("read", "java.io.File f",
                           exceps, className, jw.PUBLIC | jw.STATIC | jw.IO);
            jw.writeEol("java.io.InputStream in = new java.io.FileInputStream(f)");
            jw.beginTry();
            jw.writeEol("return read(in)");
            jw.endFinallyBegin();
            jw.writeEol("in.close()");
            jw.end();
            jw.endMethod();

            jw.beginMethod("read", "java.io.InputStream in", exceps, className,
                           jw.PUBLIC | jw.STATIC | jw.IO);
            geneol("return read(new org.xml.sax.InputSource(in), false, null, null)");
            end();
            cr();

            jw.bigComment("Warning: in readNoEntityResolver character and entity references will\nnot be read from any DTD in the XML source.\nHowever, this way is faster since no DTDs are looked up\n(possibly skipping network access) or parsed.");
            jw.beginMethod("readNoEntityResolver", "java.io.InputStream in",
                           exceps, className,
                           jw.PUBLIC | jw.STATIC | jw.IO);
            gencr("return read(new org.xml.sax.InputSource(in), false,");
            tabIn();
            gen("new org.xml.sax.EntityResolver() ");
            begin();
            gen("public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) ");
            begin();
            geneol("java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(new byte[0])");
            geneol("return new org.xml.sax.InputSource(bin)");
            end();
            end();
            tabIn();
            geneol(", null)");
            end();
            cr();

            jw.beginMethod("read",
                           "org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh",
                           exceps, className,
                           jw.PUBLIC | jw.STATIC | jw.IO);
            geneol("javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance()");
            geneol("dbf.setValidating(validate)");
            geneol("dbf.setNamespaceAware(true)");
            geneol("javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder()");
            gen("if (er != null)");
            tabIn();
            geneol("db.setEntityResolver(er)");
            gen("if (eh != null)");
            tabIn();
            geneol("db.setErrorHandler(eh)");
            geneol("org.w3c.dom.Document doc = db.parse(in)");
            geneol("return read(doc)");
            end();
            cr();

            exceps = null;
            if (config.isVetoable())
                exceps = "java.beans.PropertyVetoException";
            jw.beginMethod("read", "org.w3c.dom.Document document",
                           exceps, className, jw.PUBLIC | jw.STATIC | jw.IO);
            jw.writeEol(className, " a", className, " = new "+className+"()");
            jw.writeEol("a", className, ".readFromDocument(document)");
            jw.writeEol("return a", className);
            jw.endMethod();
            
            jw.beginMethod("readFromDocument", "org.w3c.dom.Document document",
                           exceps, "void", jw.PROTECTED);
            if (config.isProcessDocType()) {
                String fullDocTypeName;
                if (packageName == null)
                    fullDocTypeName = className+".DocType";
                else
                    fullDocTypeName = packageName+"."+className+".DocType";
                jw.writeEol("org.w3c.dom.NodeList children = document.getChildNodes()");
                jw.writeEol("int length = children.getLength()");
                jw.beginFor("int i = 0", "i < length", "++i");
                jw.beginIf("children.item(i) instanceof org.w3c.dom.DocumentType");
                jw.writeEol("docType = new "+fullDocTypeName+"((org.w3c.dom.DocumentType)children.item(i))");
                jw.writeEol("break");
                jw.end();
                jw.end();
            }
            geneol("readNode(document.getDocumentElement())");
            end();
            cr();
        }

        if (beanElement.isRoot) {
            jw.write("protected static class ReadState ");
            jw.begin();
            jw.writeEol("int lastElementType");
            jw.writeEol("int elementPosition");
            jw.end();
            jw.cr();
        }

        jw.beginMethod("readNode", "org.w3c.dom.Node node",
                       config.isVetoable() ? "java.beans.PropertyVetoException" : null,
                       "void", jw.PUBLIC | jw.IO);
        jw.write("readNode(node, new java.util.HashMap");
        if (config.jdkTarget >= 150)
            jw.write("<String, String>");
        jw.writeEol("())");
        jw.endMethod();

        String namespacePrefixesFormalParameter;
        if (config.jdkTarget >= 150)
            namespacePrefixesFormalParameter = "java.util.Map<String, String> namespacePrefixes";
        else
            namespacePrefixesFormalParameter = "java.util.Map namespacePrefixes";
        
        String readNodeArgs = "org.w3c.dom.Node node, "+namespacePrefixesFormalParameter;
        jw.beginMethod("readNode", readNodeArgs,
                       config.isVetoable() ? "java.beans.PropertyVetoException" : null,
                       "void", jw.PUBLIC | jw.IO);
        // Go over attributes
        beginAttrProcessing("node");
        jw.writeEol("boolean firstNamespaceDef = true");
        genUpdateNamespaces("namespacePrefixes", "firstNamespaceDef");
        
        int attrCount = 0;
        if (beanElement.isRoot) {
            ++attrCount;
            jw.writeEol("String xsiPrefix = \"xsi\"");
            jw.beginFor("java.util.Iterator it = namespacePrefixes.entrySet().iterator()",
                        "it.hasNext()", "");
            jw.writeEol("java.util.Map.Entry entry = (java.util.Map.Entry) it.next()");
            jw.writeEol("String prefix = (String) entry.getKey()");
            jw.writeEol("String ns = (String) entry.getValue()");
            jw.beginIf("\"http://www.w3.org/2001/XMLSchema-instance\".equals(ns)");
            jw.writeEol("xsiPrefix = prefix");
            jw.writeEol("break");
            jw.end();
            jw.end();
            genReadAttr("schemaLocation", "String",
                        "\"+xsiPrefix+\":schemaLocation",
                        "node", null, false, null, false, null);
        }
        jw.writeEol("readNodeAttributes(node, namespacePrefixes, attrs)");
        jw.end();
        jw.writeEol("readNodeChildren(node, namespacePrefixes)");
        jw.endMethod();
        
        jw.beginMethod("readNodeAttributes", readNodeArgs+", org.w3c.dom.NamedNodeMap attrs",
                       config.isVetoable() ? "java.beans.PropertyVetoException" : null,
                       "void", jw.PROTECTED | jw.IO);
        if (config.isRespectExtension() && beanElement.getExtension() != null)
            jw.writeEol("super.readNodeAttributes(node, namespacePrefixes, attrs)");
        jw.writeEol("org.w3c.dom.Attr attr");
        jw.writeEol("java.lang.String attrValue");
        boolean hasNillableElement = false;
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            if (a.isNillable())
                hasNillableElement = true;
            if (!a.isAttribute())
                continue;
            if (!a.isDirectChild())
                continue;
            ++attrCount;
            genReadAttr(a, "node");
        }
        jw.endMethod();
        
        jw.beginMethod("readNodeChildren", readNodeArgs,
                       config.isVetoable() ? "java.beans.PropertyVetoException" : null,
                       "void", jw.PROTECTED | jw.IO);
        if (hasSubElements) {
            // FIXME: Should deal with the situation where we get 2 "foo" elements
            //        and the schema only mentioned 1.
            Map dtdNames = new HashMap();
            boolean hasDuplicateDtdNames = config.isKeepElementPositions();
            boolean hasQNameType = false;
            boolean hasNonElementTypes = false;
            for (int i = 0; i < size; i++) {
                Property a = (Property)attrList.get(i);
                if (isTypeQName(a.getType()))
                    hasQNameType = true;
                if (a.type == Common.TYPE_COMMENT || a.dtdName.equals("#PCDATA")) {
                    hasNonElementTypes = true;
                    continue;
                }
                if (a.isAttribute())
                    continue;
                if (dtdNames.containsKey(a.dtdName)) {
                    //jw.comment(a.dtdName+" has duplicates");
                    hasDuplicateDtdNames = true;
                }
                dtdNames.put(a.dtdName, a);
            }
            geneol("org.w3c.dom.NodeList children = node.getChildNodes()");
            boolean hasReadState = false;
            if (hasDuplicateDtdNames || config.isKeepElementPositions()) {
                hasReadState = true;
                jw.writeEol(getRootClassName(), ".ReadState readState = new ",
                            getRootClassName(),".ReadState()");
            }
            if (hasDuplicateDtdNames)
                jw.writeEol("readState.lastElementType = -1");
            if (config.isKeepElementPositions()) {
                jw.writeEol("readState.elementPosition = 0");
                jw.writeEol("elementsByPosition = new java.lang.Object[children.getLength()]");
                jw.writeEol("elementTypesByPosition = new int[children.getLength()]");
            }
            jw.beginFor("int i = 0, size = children.getLength()",
                        "i < size", "++i");
            geneol("org.w3c.dom.Node childNode = children.item(i)");
            if (!hasNonElementTypes &&
                !(config.isRespectExtension() && beanElement.getExtension() != null)) {
                jw.beginIf("!(childNode instanceof org.w3c.dom.Element)");
                jw.writeEol("continue");
                jw.end();
            }
            geneol("String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern())");
            geneol("String childNodeValue = \"\"");
            jw.beginIf("childNode.getFirstChild() != null");
            geneol("childNodeValue = childNode.getFirstChild().getNodeValue()");
            jw.end();
            jw.write("boolean recognized = readNodeChild(childNode, childNodeName, childNodeValue, namespacePrefixes");
            if (hasReadState)
                jw.write(", readState");
            jw.writeEol(")");
            jw.beginIf("!recognized");
            if (!config.isLogSuspicious()) {
                comment("Found extra unrecognized childNode");
            } else {
                jw.beginIf("childNode instanceof org.w3c.dom.Element");
                declareLogger();
                jw.writeEol("_logger.info(\"Found extra unrecognized childNode '\"+childNodeName+\"'\")");
                jw.end();
            }
            jw.end();
            jw.end();
            if (config.isKeepElementPositions()) {
                jw.beginFor("", "readState.elementPosition < elementTypesByPosition.length",
                            "++readState.elementPosition");
                jw.writeEol("elementTypesByPosition[readState.elementPosition] = "+elementTypeSetnull);
                jw.end();
            }
            jw.endMethod();

            jw.beginMethod("readNodeChild", "org.w3c.dom.Node childNode, String childNodeName, String childNodeValue, "+namespacePrefixesFormalParameter+(hasReadState ? ", "+getRootClassName()+".ReadState readState" : ""),
                           config.isVetoable() ? "java.beans.PropertyVetoException" : null,
                           "boolean", jw.PROTECTED | jw.IO);
           jw.comment("assert childNodeName == childNodeName.intern()");
           if (hasNillableElement || hasQNameType ||
                nonDirectAttributes.size() > 0)
                declareAttrsForRead("childNode");
            boolean first = true;
            Property anyProp = null; // Is this an any property?
            for (int i = 0; i < size; i++) {
                Property a = (Property)attrList.get(i);
                if (a.isAttribute())
                    continue;
                boolean   	indexed = a.isIndexed();
                String attr = "_" + a.name;
                String baseAttr = attr;
                boolean isScalar = a.isScalar();

                String type = a.getType().intern();
                String baseType = type;
                if (indexed)
                    type = (baseType + "[]").intern();

                if (baseType == "org.w3c.dom.Element") {
                    anyProp = a;
                    continue;
                }
                if (first) first = false; else gen("else ");
                jw.write("if (");
                if (hasDuplicateDtdNames && dtdNames.get(a.dtdName) != a) {
                    int alwaysBeforeElementType;
                    if (indexed) {
                        // Keep reading in elements into this slot, until
                        // there's another element that says to go on.
                        alwaysBeforeElementType = i + 1;
                    } else {
                        // Force it to goto the next slot.
                        alwaysBeforeElementType = i;
                    }
                    jw.write("readState.lastElementType < "+alwaysBeforeElementType,
                             " && ");
                }
                if (a.dtdName.equals("#PCDATA"))
                    jw.write("childNode instanceof org.w3c.dom.CharacterData");
                else if (a.type == Common.TYPE_COMMENT)
                    jw.write("childNode instanceof org.w3c.dom.Comment");
                else
                    jw.write("\""+a.dtdName+"\".equals(childNodeName)");
                jw.write(") ");
                begin();
                String var;
                if (a.isNillable()) {
                    jw.writeEol("org.w3c.dom.Attr nilAttr = (org.w3c.dom.Attr) attrs.getNamedItem(\"xsi:nil\")");
                    jw.beginIf("nilAttr == null || !\"true\".equals(nilAttr.getValue())");
                }
                if (indexed) {
                    var = "a"+a.name;
                    if (a.isBean) {
                        jw.write(baseType, " ");
                    } else {
                        geneol(baseType+" "+var);
                    }
                } else {
                    var = attr;
                }
                if (a.isBean) {
                    jw.write(var, " = ");
                    genNewDefault(a, true);
                    jw.eol();
                    if (config.isGeneratePropertyEvents()) {
                        geneol(var+"._setPropertyChangeSupport(eventListeners)");
                        if (config.isVetoable())
                            geneol(var+"._setVetoableChangeSupport(vetos)");
                    }
                    if (config.isGenerateParentRefs())
                        jw.writeEol(var+"._setParent(this)");
                }
                boolean generatedSet = true;
                if (a.isBean)
                    geneol(var+".readNode(childNode, namespacePrefixes)");
                else if (a.dtdName.equals("#PCDATA")) {
                    geneol(var+" = ((org.w3c.dom.CharacterData)childNode).getData()");
                } else if (a.type == Common.TYPE_COMMENT) {
                    geneol(var+" = ((org.w3c.dom.CharacterData)childNode).getData()");
                } else {
                    if (config.isTrimNonStrings() &&
                        baseType != "String" && baseType != "java.lang.String") {
                        jw.writeEol("childNodeValue = childNodeValue.trim()");
                    }
                    List exceps = JavaUtil.exceptionsFromParsingText(baseType);
                    if (!exceps.isEmpty()) {
                        jw.beginTry();
                    }
                    if (baseType == "boolean" || baseType == "java.lang.Boolean") {
                        gencr("if (childNode.getFirstChild() == null)");
                        tabIn();
                        if (baseType == "boolean")
                            geneol(var+" = true");
                        else
                            geneol(var+" = Boolean.TRUE");
                        gencr("else");
                        tabIn();
                    }
                    generatedSet = genReadType(baseType, var, "childNodeValue",
                                               false, null,
                                               isScalar,
                                               (SchemaRep.EncodingStyle) a.searchExtraData(SchemaRep.EncodingStyle.class));
                        SchemaRep.WhiteSpace ws = (SchemaRep.WhiteSpace) a.searchExtraData(SchemaRep.WhiteSpace.class);
                        if (ws != null)
                            genWhiteSpaceRestriction(ws, var, baseType);
                        //}
                    if (!indexed && config.isOptionalScalars() && isScalar) {
                        jw.writeEol(a.getScalarIsSet(), " = true");
                    }
                    int directAttrCount = 0;
                    for (int attrNum = 0; attrNum < a.attributes.length; ++attrNum) {
                        AttrProp myAttrProp = a.attributes[attrNum];
                        //jw.comment("myAttrProp="+myAttrProp);
                        for (Iterator it = nonDirectAttributes.iterator(); it.hasNext(); ) {
                            Property attrProperty = (Property) it.next();
                            AttrProp attrProp = attrProperty.getAttrProp();
                            if (myAttrProp == attrProp) {
                                ++directAttrCount;
                                genReadAttr(attrProperty, "childNode");
                            }
                        }
                    }
                
                    if (!exceps.isEmpty()) {
                        end();
                        genRethrowExceptions(exceps);
                    }
                }
                if (indexed && generatedSet)
                    jw.writeEol(attr, ".add(",
                                JavaUtil.toObject(var, baseType,
                                                  config.isForME(), config.isJava5()),
                                ")");
                if (a.isNillable()) {
                    jw.endElseBegin();
                    if (indexed)
                        jw.writeEol(attr, ".add(null)");
                    else
                        jw.writeEol(var, " = ", JavaUtil.nullValueForType(baseType));
                    jw.end();
                }
                if (hasDuplicateDtdNames)
                    jw.writeEol("readState.lastElementType = "+i);
                if (config.isKeepElementPositions()) {
                    jw.writeEol("elementsByPosition[readState.elementPosition] = "+JavaUtil.toObject(var, type, config.isForME(), config.isJava5()));
                    jw.writeEol("elementTypesByPosition[readState.elementPosition++] = "+i);
                }
                end();
            }
            if (anyProp != null) {
                String attr = "_" + anyProp.name;
                boolean	indexed = anyProp.isIndexed();
                if (first) first = false; else gen("else ");
                jw.beginIf("childNode instanceof org.w3c.dom.Element");
                if (indexed)
                    jw.writeEol(attr, ".add((org.w3c.dom.Element)childNode)");
                else
                    jw.writeEol(attr, " = (org.w3c.dom.Element) childNode");
                jw.end();
            }
            if (!first) {
                gen("else ");
                begin();
                if (config.isRespectExtension() && beanElement.getExtension() != null) {
                    jw.write("return super.readNodeChild(childNode, childNodeName, childNodeValue, namespacePrefixes");
                    if (hasReadState)
                        jw.write(", readState");
                    jw.writeEol(")");
                } else
                    jw.writeEol("return false");
                /*
                jw.writeEol("System.out.println(\"Found extra unrecognized childNode: \"+childNodeName)");
                jw.writeEol("System.out.println(\"namespaceURI=\"+childNode.getNamespaceURI())");
                jw.writeEol("System.out.println(\"prefix=\"+childNode.getPrefix())");
                jw.writeEol("System.out.println(\"localName=\"+childNode.getLocalName())");
                jw.writeEol("System.out.println(\"nodeName=\"+childNode.getNodeName())");
                */
                end();
            }
            jw.writeEol("return true");
        } else {
            if (config.isRespectExtension() && beanElement.getExtension() != null) {
                jw.writeEol("super.readNodeChildren(node, namespacePrefixes)");
            }
        }
        jw.endMethod();
        
        if (beanElement.isRoot) {
            if (!config.isUseRuntime())
                genPrintXML();
            genSpecialTypes();
        }
    }
    
    protected void genFetchXMLEventReader() throws IOException {
        jw.select(BODY_SECTION);
        String commonBean = commonBeanType();
        String beanName = beanElement.node.getName();
        if (beanElement.isRoot) {
            String beanQNameName = declareQName(beanElement.getNamespace(), beanName, null);

            jw.beginMethod("fetchXMLEventReader", "", null, "javax.xml.stream.XMLEventReader");
            jw.writeEol("return new ", getRootClassName()+".XMLEventStateManager(this, ", beanQNameName, ")");
            jw.endMethod();
            
            jw.write("static class XMLEventStateManager implements javax.xml.stream.XMLEventReader");
            jw.begin();
            jw.writeEol("private java.util.Stack<"+commonBean+"> beans = new java.util.Stack<"+commonBean+">()");
            jw.writeEol("private java.util.Stack<javax.xml.namespace.QName> nodeNames = new java.util.Stack<javax.xml.namespace.QName>()");
            jw.writeEol("private java.util.Stack<Integer> eventNumbers = new java.util.Stack<Integer>()");
            jw.writeEol("private java.util.Stack<Integer> arrayIndices = new java.util.Stack<Integer>()");
            jw.writeEol("private java.util.Stack<Integer> elementNumbers = new java.util.Stack<Integer>()");
            jw.writeEol("private "+commonBean+" currentBean");
            jw.writeEol("private javax.xml.namespace.QName currentNodeName");
            jw.writeEol("private int currentEventNumber");
            jw.writeEol("private int currentArrayIndex");
            jw.writeEol("private int currentElementNumber");
            jw.writeEol("private javax.xml.stream.events.XMLEvent next");
            jw.cr();
            jw.write("public XMLEventStateManager("+commonBean+" bean, javax.xml.namespace.QName nodeName)");
            jw.begin();
            jw.writeEol("currentBean = bean");
            jw.writeEol("this.currentNodeName = nodeName");
            jw.writeEol("currentEventNumber = 0");
            jw.writeEol("currentArrayIndex = 0");
            jw.writeEol("currentElementNumber = 0");
            jw.end();
            jw.cr();
            jw.write("public int getEventNumber()");
            jw.begin();
            jw.writeEol("return currentEventNumber");
            jw.end();
            jw.cr();
            jw.write("public void nextEventNumber()");
            jw.begin();
            jw.writeEol("++currentEventNumber");
            jw.writeEol("currentArrayIndex = 0");
            jw.writeEol("currentElementNumber = 0");
            jw.end();
            jw.cr();
            jw.write("public int getArrayIndex()");
            jw.begin();
            jw.writeEol("return currentArrayIndex");
            jw.end();
            jw.cr();
            jw.write("public void nextArrayIndex()");
            jw.begin();
            jw.writeEol("++currentArrayIndex");
            jw.writeEol("currentElementNumber = 0");
            jw.end();
            jw.cr();
            jw.write("public int getElementNumber()");
            jw.begin();
            jw.writeEol("return currentElementNumber");
            jw.end();
            jw.cr();
            jw.write("public void nextElementNumber()");
            jw.begin();
            jw.writeEol("++currentElementNumber");
            jw.end();
            jw.cr();
            jw.write("public javax.xml.namespace.QName getNodeName()");
            jw.begin();
            jw.writeEol("return currentNodeName");
            jw.end();
            jw.cr();
            jw.write("public void enterChildBean("+commonBean+" bean, javax.xml.namespace.QName nodeName)");
            jw.begin();
            jw.writeEol("beans.push(currentBean)");
            jw.writeEol("eventNumbers.push(currentEventNumber)");
            jw.writeEol("arrayIndices.push(currentArrayIndex)");
            jw.writeEol("elementNumbers.push(currentElementNumber)");
            jw.writeEol("nodeNames.push(currentNodeName)");
            jw.writeEol("currentBean = bean");
            jw.writeEol("currentEventNumber = 0");
            jw.writeEol("currentArrayIndex = 0");
            jw.writeEol("currentElementNumber = 0");
            jw.writeEol("currentNodeName = nodeName");
            jw.end();
            jw.cr();
            jw.write("public void doneWithBean()");
            jw.begin();
            jw.write("if (beans.isEmpty())");
            jw.begin();
            jw.writeEol("currentBean = null");
            jw.writeEol("currentEventNumber = -1");
            jw.end();
            jw.write("else");
            jw.begin();
            jw.writeEol("currentBean = beans.pop()");
            jw.writeEol("currentEventNumber = eventNumbers.pop()");
            jw.writeEol("currentArrayIndex = arrayIndices.pop()");
            jw.writeEol("currentElementNumber = elementNumbers.pop()");
            jw.writeEol("currentNodeName = nodeNames.pop()");
            jw.end();
            jw.end();
            jw.cr();
            jw.write("public Object getProperty(String name) throws IllegalArgumentException");
            jw.begin();
            jw.writeEol("throw new IllegalArgumentException(\"Got no properties\")");
            jw.end();
            jw.cr();
            jw.write("public void remove()");
            jw.begin();
            jw.writeEol("throw new UnsupportedOperationException()");
            jw.end();
            jw.cr();
            jw.write("public javax.xml.stream.events.XMLEvent peek() throws javax.xml.stream.XMLStreamException");
            jw.begin();
            jw.beginIf("next != null");
            jw.writeEol("return next");
            jw.end();
            jw.writeEol("setNextEvent()");
            jw.writeEol("return next");
            jw.end();
            jw.cr();
            jw.write("public javax.xml.stream.events.XMLEvent nextTag() throws javax.xml.stream.XMLStreamException");
            jw.begin();
            jw.writeEol("throw new IllegalArgumentException()");
            jw.end();
            jw.cr();
            jw.write("public javax.xml.stream.events.XMLEvent nextEvent() throws javax.xml.stream.XMLStreamException");
            jw.begin();
            jw.writeEol("javax.xml.stream.events.XMLEvent result = null");
            jw.write("if (next != null)");
            jw.begin();
            jw.writeEol("result = next");
            jw.writeEol("next = null");
            jw.writeEol("return result");
            jw.end();
            jw.writeEol("setNextEvent()");
            jw.beginIf("next == null");
            jw.writeEol("throw new java.util.NoSuchElementException()");
            jw.end();
            jw.writeEol("result = next");
            jw.writeEol("next = null");
            jw.writeEol("return result");
            jw.end();
            jw.cr();
            jw.write("private void setNextEvent() throws javax.xml.stream.XMLStreamException");
            jw.begin();
            jw.write("while (next == null && currentEventNumber >= 0)");
            jw.begin();
            jw.writeEol("next = currentBean.getXMLEvent(this)");
            jw.end();
            jw.end();
            jw.cr();
            jw.write("public Object next()");
            jw.begin();
            jw.beginTry();
            jw.writeEol("return nextEvent()");
            jw.write("} catch (javax.xml.stream.XMLStreamException e)");
            jw.begin();
            jw.writeEol("throw new RuntimeException(e)");
            jw.end();
            jw.end();
            jw.cr();
            jw.write("public boolean hasNext()");
            jw.begin();
            jw.beginIf("next != null");
            jw.writeEol("return true");
            jw.end();
            jw.beginTry();
            jw.writeEol("setNextEvent()");
            jw.endCatch("javax.xml.stream.XMLStreamException e");
            jw.writeEol("throw new RuntimeException(e)");
            jw.end();
            jw.writeEol("return (next != null)");
            jw.end();
            jw.cr();
            jw.write("public String getElementText() throws javax.xml.stream.XMLStreamException");
            jw.begin();
            jw.writeEol("throw new IllegalArgumentException()");
            jw.end();
            jw.cr();
            jw.write("public void close() throws javax.xml.stream.XMLStreamException");
            jw.begin();
            jw.writeEol("currentEventNumber = -1");
            jw.writeEol("beans.clear()");
            jw.writeEol("eventNumbers.clear()");
            jw.writeEol("arrayIndices.clear()");
            jw.writeEol("nodeNames.clear()");
            jw.end();
            jw.end();

            jw.write("static abstract class BaseXMLEvent implements javax.xml.stream.events.XMLEvent ");
            jw.begin();
            jw.write("public void writeAsEncodedUnicode(java.io.Writer writer) throws javax.xml.stream.XMLStreamException ");
            jw.begin();
            jw.writeEol("throw new UnsupportedOperationException()");
            jw.end();
            jw.cr();
            jw.write("public boolean isStartElement() ");
            jw.begin();
            jw.writeEol("return false");
            jw.end();
            jw.cr();
            jw.write("public boolean isStartDocument() ");
            jw.begin();
            jw.writeEol("return false");
            jw.end();
            jw.cr();
            jw.write("public javax.xml.stream.events.Characters asCharacters() ");
            jw.begin();
            jw.writeEol("return (javax.xml.stream.events.Characters) this");
            jw.end();
            jw.cr();
            jw.write("public javax.xml.stream.events.EndElement asEndElement() ");
            jw.begin();
            jw.writeEol("return (javax.xml.stream.events.EndElement) this");
            jw.end();
            jw.cr();
            jw.write("public javax.xml.stream.events.StartElement asStartElement() ");
            jw.begin();
            jw.writeEol("return (javax.xml.stream.events.StartElement) this");
            jw.end();
            jw.cr();
            jw.write("public javax.xml.stream.Location getLocation() ");
            jw.begin();
            jw.writeEol("throw new UnsupportedOperationException()");
            jw.end();
            jw.cr();
            jw.write("public javax.xml.namespace.QName getSchemaType() ");
            jw.begin();
            jw.writeEol("return null");
            jw.end();
            jw.cr();
            jw.write("public boolean isAttribute() ");
            jw.begin();
            jw.writeEol("return false");
            jw.end();
            jw.cr();
            jw.write("public boolean isCharacters() ");
            jw.begin();
            jw.writeEol("return false");
            jw.end();
            jw.cr();
            jw.write("public boolean isEndDocument() ");
            jw.begin();
            jw.writeEol("return false");
            jw.end();
            jw.cr();
            jw.write("public boolean isEndElement() ");
            jw.begin();
            jw.writeEol("return false");
            jw.end();
            jw.cr();
            jw.write("public boolean isEntityReference() ");
            jw.begin();
            jw.writeEol("return false");
            jw.end();
            jw.cr();
            jw.write("public boolean isNamespace() ");
            jw.begin();
            jw.writeEol("return false");
            jw.end();
            jw.cr();
            jw.write("public boolean isProcessingInstruction() ");
            jw.begin();
            jw.writeEol("return false");
            jw.end();
            jw.cr();
            jw.end();
            jw.cr();
            jw.write("static class ThisStartDocument extends BaseXMLEvent implements javax.xml.stream.events.StartDocument ");
            jw.begin();
            jw.write("public int getEventType() ");
            jw.begin();
            jw.writeEol("return javax.xml.stream.XMLStreamConstants.START_DOCUMENT");
            jw.end();
            jw.cr();
            jw.write("public boolean isStartDocument() ");
            jw.begin();
            jw.writeEol("return true");
            jw.end();
            jw.cr();
            jw.write("public boolean standaloneSet() ");
            jw.begin();
            jw.writeEol("return false");
            jw.end();
            jw.cr();
            jw.write("public boolean isStandalone() ");
            jw.begin();
            jw.writeEol("return false");
            jw.end();
            jw.cr();
            jw.write("public String getVersion() ");
            jw.begin();
            jw.writeEol("return \"1.0\"");
            jw.end();
            jw.cr();
            jw.write("public String getSystemId() ");
            jw.begin();
            jw.writeEol("return null");
            jw.end();
            jw.cr();
            jw.write("public String getCharacterEncodingScheme() ");
            jw.begin();
            jw.writeEol("return \"UTF-8\"");
            jw.end();
            jw.cr();
            jw.write("public boolean encodingSet() ");
            jw.begin();
            jw.writeEol("return true");
            jw.end();
            jw.end();
            jw.cr();
            jw.write("static class ThisAttribute extends BaseXMLEvent implements javax.xml.stream.events.Attribute ");
            jw.begin();
            jw.writeEol("private javax.xml.namespace.QName name");
            jw.writeEol("private String value");
            jw.writeEol("private String dtdType");
            jw.writeEol("private javax.xml.namespace.QName schemaType");
            jw.cr();
            jw.write("public ThisAttribute(javax.xml.namespace.QName name, String value, String dtdType, javax.xml.namespace.QName schemaType) ");
            jw.begin();
            jw.writeEol("this.name = name");
            jw.writeEol("this.value = value");
            jw.writeEol("this.dtdType = dtdType");
            jw.writeEol("this.schemaType = schemaType");
            jw.end();
            jw.cr();
            jw.write("public int getEventType() ");
            jw.begin();
            jw.writeEol("return javax.xml.stream.XMLStreamConstants.ATTRIBUTE");
            jw.end();
            jw.cr();
            jw.write("public boolean isSpecified() ");
            jw.begin();
            jw.writeEol("return true");
            jw.end();
            jw.cr();
            jw.write("public String getValue() ");
            jw.begin();
            jw.writeEol("return value");
            jw.end();
            jw.cr();
            jw.write("public javax.xml.namespace.QName getName() ");
            jw.begin();
            jw.writeEol("return name");
            jw.end();
            jw.cr();
            jw.write("public String getDTDType() ");
            jw.begin();
            jw.writeEol("return dtdType");
            jw.end();
            jw.cr();
            jw.write("public javax.xml.namespace.QName getSchemaType() ");
            jw.begin();
            jw.writeEol("return schemaType");
            jw.end();
            jw.end();
            jw.cr();
            jw.write("static class ThisNamespace extends ThisAttribute implements javax.xml.stream.events.Namespace ");
            jw.begin();
            jw.writeEol("private String prefix");
            jw.cr();
            jw.write("public ThisNamespace(String prefix, String uri) ");
            jw.begin();
            jw.writeEol("super(new javax.xml.namespace.QName(\"http://FIXME/xmlns\", prefix, \"xmlns\"), uri, null, null)");
            jw.writeEol("this.prefix = prefix");
            jw.end();
            jw.cr();
            jw.write("public boolean isDefaultNamespaceDeclaration() ");
            jw.begin();
            jw.writeEol("return \"\".equals(prefix)");
            jw.end();
            jw.cr();
            jw.write("public String getPrefix() ");
            jw.begin();
            jw.writeEol("return prefix");
            jw.end();
            jw.cr();
            jw.write("public String getNamespaceURI() ");
            jw.begin();
            jw.writeEol("return getValue()");
            jw.end();
            jw.end();
            jw.cr();
            jw.write("static class ThisStartElement extends BaseXMLEvent implements javax.xml.stream.events.StartElement ");
            jw.begin();
            jw.writeEol("private javax.xml.namespace.QName name");
            jw.writeEol("private java.util.Map<javax.xml.namespace.QName, ThisAttribute> attributes = new java.util.HashMap<javax.xml.namespace.QName, ThisAttribute>()");
            jw.writeEol("private java.util.Map<String, ThisNamespace> namespaces = new java.util.HashMap<String, ThisNamespace>()");
            jw.cr();
            jw.write("public ThisStartElement(javax.xml.namespace.QName name) ");
            jw.begin();
            jw.writeEol("this.name = name");
            jw.end();
            jw.cr();
            jw.write("public void addAttribute(javax.xml.namespace.QName attrName, String attrValue, String dtdType, javax.xml.namespace.QName schemaType) ");
            jw.begin();
            jw.writeEol("attributes.put(attrName, new ThisAttribute(attrName, attrValue, dtdType, schemaType))");
            jw.end();
            jw.cr();
            jw.write("public void addNamespace(String prefix, String uri) ");
            jw.begin();
            jw.writeEol("namespaces.put(prefix, new ThisNamespace(prefix, uri))");
            jw.end();
            jw.cr();
            jw.write("public int getEventType() ");
            jw.begin();
            jw.writeEol("return javax.xml.stream.XMLStreamConstants.START_ELEMENT");
            jw.end();
            jw.cr();
            jw.write("public boolean isStartElement() ");
            jw.begin();
            jw.writeEol("return true");
            jw.end();
            jw.cr();
            jw.write("public String getNamespaceURI(String prefix) ");
            jw.begin();
            jw.writeEol("return namespaces.get(prefix).getNamespaceURI()");
            jw.end();
            jw.cr();
            jw.write("public javax.xml.stream.events.Attribute getAttributeByName(javax.xml.namespace.QName name) ");
            jw.begin();
            jw.writeEol("return attributes.get(name)");
            jw.end();
            jw.cr();
            jw.write("public java.util.Iterator getNamespaces() ");
            jw.begin();
            jw.writeEol("return namespaces.values().iterator()");
            jw.end();
            jw.cr();
            jw.write("public javax.xml.namespace.NamespaceContext getNamespaceContext() ");
            jw.begin();
            jw.writeEol("throw new UnsupportedOperationException()");
            jw.end();
            jw.cr();
            jw.write("public javax.xml.namespace.QName getName() ");
            jw.begin();
            jw.writeEol("return name");
            jw.end();
            jw.cr();
            jw.write("public java.util.Iterator getAttributes() ");
            jw.begin();
            jw.writeEol("return attributes.values().iterator()");
            jw.end();
            jw.write("public String toString() ");
            jw.begin();
            jw.writeEol("return \"StartElement: \"+name.toString()");
            jw.end();
            jw.end();
            jw.cr();
            jw.write("static class ThisEndElement extends BaseXMLEvent implements javax.xml.stream.events.EndElement ");
            jw.begin();
            jw.writeEol("private javax.xml.namespace.QName name");
            jw.cr();
            jw.write("public ThisEndElement(javax.xml.namespace.QName name) ");
            jw.begin();
            jw.writeEol("this.name = name");
            jw.end();
            jw.cr();
            jw.write("public int getEventType() ");
            jw.begin();
            jw.writeEol("return javax.xml.stream.XMLStreamConstants.END_ELEMENT");
            jw.end();
            jw.cr();
            jw.write("public boolean isEndElement() ");
            jw.begin();
            jw.writeEol("return true");
            jw.end();
            jw.cr();
            jw.write("public java.util.Iterator getNamespaces() ");
            jw.begin();
            jw.writeEol("throw new UnsupportedOperationException()");
            jw.end();
            jw.cr();
            jw.write("public javax.xml.namespace.QName getName() ");
            jw.begin();
            jw.writeEol("return name");
            jw.end();
            jw.write("public String toString() ");
            jw.begin();
            jw.writeEol("return \"EndElement: \"+name.toString()");
            jw.end();
            jw.end();
            jw.cr();
            jw.write("static class ThisCharacters extends BaseXMLEvent implements javax.xml.stream.events.Characters ");
            jw.begin();
            jw.writeEol("private String data");
            jw.cr();
            jw.write("public ThisCharacters(String data) ");
            jw.begin();
            jw.writeEol("this.data = data");
            jw.end();
            jw.cr();
            jw.write("public int getEventType() ");
            jw.begin();
            jw.writeEol("return javax.xml.stream.XMLStreamConstants.CHARACTERS");
            jw.end();
            jw.cr();
            jw.write("public boolean isCharacters() ");
            jw.begin();
            jw.writeEol("return true");
            jw.end();
            jw.cr();
            jw.write("public boolean isWhiteSpace() ");
            jw.begin();
            jw.write("for (int i = 0; i < data.length(); ++i) ");
            jw.begin();
            jw.writeEol("char c = data.charAt(i)");
            jw.writeEol("if (!Character.isWhitespace(c))");
            jw.writeEol("return false");
            jw.end();
            jw.writeEol("return true");
            jw.end();
            jw.cr();
            jw.write("public boolean isIgnorableWhiteSpace() ");
            jw.begin();
            jw.writeEol("return false");
            jw.end();
            jw.cr();
            jw.write("public boolean isCData() ");
            jw.begin();
            jw.writeEol("return false");
            jw.end();
            jw.cr();
            jw.write("public String getData() ");
            jw.begin();
            jw.writeEol("return data");
            jw.end();
            jw.write("public String toString() ");
            jw.begin();
            jw.writeEol("return \"Characters: \"+data");
            jw.end();
            jw.end();
            jw.cr();
            jw.write("static class ThisEndDocument extends BaseXMLEvent implements javax.xml.stream.events.EndDocument ");
            jw.begin();
            jw.write("public int getEventType() ");
            jw.begin();
            jw.writeEol("return javax.xml.stream.XMLStreamConstants.END_DOCUMENT");
            jw.end();
            jw.cr();
            jw.write("public boolean isEndDocument() ");
            jw.begin();
            jw.writeEol("return true");
            jw.end();
            jw.end();
            jw.cr();
        }
        jw.beginMethod("getXMLEvent", getRootClassName()+".XMLEventStateManager state", null, "javax.xml.stream.events.XMLEvent");
        jw.write("switch (state.getEventNumber()) ");
        jw.begin();
        int caseNum = 0;
        if (beanElement.isRoot) {
            jw.writecr("case "+caseNum++, ":");
            jw.indentRight();
            jw.writeEol("state.nextEventNumber()");
            jw.writeEol("return new ThisStartDocument()");
            jw.indentLeft();
        }
        
        jw.writecr("case "+caseNum++, ":");
        jw.begin();
        jw.writeEol("state.nextEventNumber()");
        jw.writeEol(getRootClassName()+".ThisStartElement startElement = new ", 
                getRootClassName(),".ThisStartElement(state.getNodeName())");
        int size = attrList.size();
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            if (!a.isAttribute())
                continue;
            if (!a.isDirectChild())
                continue;
            String qNameName = declareQName(a.getNamespace(), a.dtdName, null);
            String typeQNameName;
            //System.out.println("graphNode null for "+a.getType());
            SchemaRep schemaRep = new SchemaRep();
            String xmlSchemaType = schemaRep.javaType2XMLSchemaTypeComplex(a.getType());
            //System.out.println("xmlSchemaType="+xmlSchemaType);
            typeQNameName = declareQName(schemaRep.getNamespaceURI(schemaRep.prefixOf(xmlSchemaType)),
                    schemaRep.removePrefix(xmlSchemaType), null);

            String varName = "_"+a.name;
            if (!a.isScalar()) {
                jw.beginIf(varName+" != null");
            } else if (config.isOptionalScalars() && a.isScalar()) {
                jw.beginIf(a.getScalarIsSet());
            }
            jw.write("startElement.addAttribute(", qNameName, ", ");
            jw.write(JavaUtil.typeToString(a.getType(), varName));
            jw.writeEol(", \"CDATA\", ", typeQNameName, ")");
            if (!a.isScalar() || (config.isOptionalScalars() && a.isScalar()))
                jw.end();
        }

        if (beanElement.isRoot) {
            jw.beginIf("schemaLocation != null");
            String slQNameName = declareQName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", "xsi");
            String typeQNameName = declareQName("http://www.w3.org/2001/XMLSchema", "string", "xsd");
            jw.write("startElement.addAttribute(", slQNameName, ", ");
            jw.writeEol("schemaLocation, \"CDATA\", ", typeQNameName, ")");
            jw.end();
        }
        jw.writeEol("return startElement");
        jw.end();

        int numberOfNonAttributeProperties = countNumberOfNonAttributeProperties();
        comment("Number of non attribute properties: "+numberOfNonAttributeProperties);
        int childStartCaseNum = caseNum;
        if (caseNum < childStartCaseNum + numberOfNonAttributeProperties) {
            while (caseNum < childStartCaseNum + numberOfNonAttributeProperties) {
                jw.writecr("case "+caseNum++, ":");
            }
            jw.indentRight();
            // numberOfNonAttributeProperties
            jw.writeEol("javax.xml.stream.events.XMLEvent event = getXMLEventChildren(state, state.getEventNumber() - "+(childStartCaseNum)+")");
            jw.writeEol("return event");
            jw.indentLeft();
       }
        
        jw.writecr("case "+caseNum++, ":");
        jw.indentRight();
        jw.writeEol("state.nextEventNumber()");
        jw.writeEol("return new ", getRootClassName(), ".ThisEndElement(state.getNodeName())");
        jw.indentLeft();

        if (beanElement.isRoot) {
            jw.writecr("case "+caseNum++, ":");
            jw.indentRight();
            jw.writeEol("state.nextEventNumber()");
            jw.writeEol("return new ThisEndDocument()");
            jw.indentLeft();
        }
        jw.writecr("default:");
        jw.indentRight();
        jw.writeEol("state.doneWithBean()");
        jw.writeEol("break");
        jw.indentLeft();
        jw.end();
        jw.writeEol("return null");
        jw.endMethod();
        
        jw.beginMethod("getXMLEventChildren", getRootClassName()+".XMLEventStateManager state, int eventNumber", null, "javax.xml.stream.events.XMLEvent", jw.PROTECTED);
        caseNum = 0;
        jw.write("switch (eventNumber) ");
        jw.begin();
        
        if (config.isRespectExtension() && beanElement.getExtension() != null) {
            jw.writecr("default:");
            jw.indentRight();
            jw.writeEol("javax.xml.stream.events.XMLEvent event = super.getXMLEventChildren(state, eventNumber)");
            jw.beginIf("true");  // workaround "unreachable statement" error from javac which can happen if default is the only thing in the switch
            jw.writeEol("return event");
            jw.end();
            jw.indentLeft();
            caseNum = countNumberOfNonAttributePropertiesRecurse(beanElement.getExtension());
        }
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            if (a.isAttribute())
                continue;
            boolean indexed = a.isIndexed();
            String varName = "_"+a.name;
            
            String qNameName = declareQName(a.getNamespace(), a.dtdName, null);
            jw.writecr("case "+caseNum++, ":");
            jw.begin();
            if (indexed) {
                varName = "element";
                jw.writeEol(a.getTypeFullClassName(packageName), " element = null");
                if (!a.isBean) {
                    jw.beginIf("state.getElementNumber() == 0");
                }
                jw.beginWhile("state.getArrayIndex() < size"+a.name+"()");
                jw.writeEol("element = ", a.getReadMethod(true), "(state.getArrayIndex())");
                jw.beginIf("element != null");
                jw.writeEol("break");
                jw.end();
                jw.writeEol("state.nextArrayIndex()");
                jw.end();
                if (!a.isBean) {
                    jw.endElseBegin();
                    jw.writeEol("element = ", a.getReadMethod(true), "(state.getArrayIndex())");
                    jw.writeEol("assert element != null");
                    jw.end();
                }
            } else {
            }
            if (a.isBean) {
                jw.beginIf(varName+" != null");
                if (indexed)
                    jw.writeEol("state.nextArrayIndex()");
                else
                    jw.writeEol("state.nextEventNumber()");
                jw.write("state.enterChildBean(", varName, ", ");
                jw.writeEol(qNameName, ")");
                jw.writeEol("return ", varName, ".getXMLEvent(state)");
                jw.endElseBegin();
                jw.writeEol("state.nextEventNumber()");
                jw.end();
                jw.writeEol("break");
                jw.end();
            } else {
                if (!a.isScalar()) {
                    jw.beginIf(varName+" != null");
                } else if (config.isOptionalScalars() && a.isScalar()) {
                    jw.beginIf(a.getScalarIsSet());
                }
                jw.writeEol("state.nextElementNumber()");
                jw.write("switch (state.getElementNumber()) ");
                jw.begin();
                jw.writecr("case 1:");
                jw.indentRight();
                jw.write(getRootClassName(), ".ThisStartElement startElement = new ", getRootClassName());
                jw.writeEol(".ThisStartElement(", qNameName, ")");
                for (int attrNum = 0; attrNum < a.attributes.length; ++attrNum) {
                    AttrProp attr = a.attributes[attrNum];
                    //jw.comment("attr "+attr);
                    Property prop = findProperty(attr);

                    String qNameNameAttr = declareQName(prop.getNamespace(), prop.dtdName, null);
                    String typeQNameNameAttr;
                    //System.out.println("graphNode null for "+a.getType());
                    SchemaRep schemaRep = new SchemaRep();
                    String xmlSchemaType = schemaRep.javaType2XMLSchemaTypeComplex(a.getType());
                    //System.out.println("xmlSchemaType="+xmlSchemaType);
                    typeQNameNameAttr = declareQName(schemaRep.getNamespaceURI(schemaRep.prefixOf(xmlSchemaType)),
                            schemaRep.removePrefix(xmlSchemaType), null);

                    String varNameAttr = "_"+prop.name;
                    if (!a.isScalar()) {
                        jw.beginIf(varNameAttr+" != null");
                    } else if (config.isOptionalScalars() && a.isScalar()) {
                        jw.beginIf(a.getScalarIsSet());
                    }
                    jw.write("startElement.addAttribute("+qNameNameAttr+", ");
                    jw.write(JavaUtil.typeToString(a.getType(), varNameAttr));
                    jw.writeEol(", \"CDATA\", ", typeQNameNameAttr, ")");
                    if (!a.isScalar() || (config.isOptionalScalars() && a.isScalar()))
                        jw.end();
                }
                jw.writeEol("return startElement");
                jw.indentLeft();
                jw.writecr("case 2:");
                jw.indentRight();
                jw.write("return new ", getRootClassName(), ".ThisCharacters(");
                if (isTypeQName(a.getType())) {
                    // see line 3118 or so
                    jw.write(a.getType());
                } else if ("byte[]".equals(a.getType())) {
                    // Is this hexBinary or base64Binary?
                    SchemaRep.EncodingStyle style = (SchemaRep.EncodingStyle) a.searchExtraData(SchemaRep.EncodingStyle.class);
                    if (style instanceof SchemaRep.HexBinary) {
                    } else if (style instanceof SchemaRep.Base64Binary) {
                        jw.write(getRootClassName(), ".encodeBase64BinaryString(");
                        jw.writeEol(varName, "))");
                    } else {
                        config.messageOut.println("Unknown encoding style for "+a.getType()+" for property "+a.name);
                    }
                } else {
                    jw.write(JavaUtil.typeToString(a.getType(), varName));
                    jw.writeEol(")");
                }
                jw.indentLeft();
                jw.writecr("case 3:");
                jw.indentRight();
                jw.write("return new ", getRootClassName(), ".ThisEndElement(", qNameName);
                jw.writeEol(")");
                jw.indentLeft();
                jw.writecr("default:");
                jw.indentRight();
                if (indexed) {
                    jw.writeEol("state.nextArrayIndex()");
                    jw.writeEol("return getXMLEvent(state)");
                } else {
                    jw.writeEol("break");
                }
                jw.indentLeft();
                jw.end();
                if (!a.isScalar() || (config.isOptionalScalars() && a.isScalar())) {
                    if (indexed) {
                        jw.endElse();
                        jw.cr();
                        jw.indentOneLevel();
                    } else {
                        jw.end();
                    }
                }
                jw.writeEol("state.nextEventNumber()");
                jw.writeEol("break");
                jw.end();
            }
            /*
            if (indexed) {
                jw.writeEol("state.nextEventNumber()");
                jw.writeEol("return getXMLEvent(state)");
                jw.end();
            }
             */
        }

        jw.end();
        jw.writeEol("return null");
        jw.endMethod();

    }
    
    /**
     * Declare the QNAME as a constant.
     * @return the name of the identifier choosen.
     */
    protected String declareQName(String namespaceUri, String localPart, String prefix) throws IOException {
        if ("http://www.w3.org/XML/1998/namespace".equals(namespaceUri)) {
            if (prefix == null)
                prefix = "xml";
            if (localPart.startsWith("xml:"))
                localPart = localPart.substring(4);
        }
        QName qname = new QName(namespaceUri, localPart, prefix);
        if (declaredQNames.containsKey(qname))
            return (String) declaredQNames.get(qname);
        jw.pushSelect(DECL_SECTION);
        if (declaredQNames.size() == 0)
            jw.cr();
        String qNameName = "QNAME_"+Common.constName(localPart);
        while (declaredQNames.containsKey(qNameName))
            qNameName = qNameName + "2";
        declaredQNames.put(qname, qNameName);
        //System.out.println("Declaring "+qNameName+" "+qname);
        jw.write("public static final javax.xml.namespace.QName ");
        jw.write(qNameName);
        jw.write(" = new javax.xml.namespace.QName(");
        if (namespaceUri == null)
            jw.write("null");
        else
            jw.write(JavaUtil.instanceFrom("String", namespaceUri));
        jw.write(", \"");
        jw.write(localPart);
        jw.write("\"");
        if (prefix != null)
            jw.write(", ", JavaUtil.instanceFrom("String", prefix));
        jw.writeEolNoI18N(")");
        jw.popSelect();
        return qNameName;
    }

    protected void declareLogger() throws IOException {
        if (loggerDeclared)
            return;
        jw.pushSelect(DECL_SECTION);
        jw.writeEol("private static final java.util.logging.Logger _logger = java.util.logging.Logger.getLogger(\""+fullClassName+"\")");
        jw.popSelect();
        loggerDeclared = true;
    }

    protected void genUpdateNamespaces(String namespaceMapName,
                                       String firstNamespaceVarName) throws IOException {
        jw.beginFor("int attrNum = 0", "attrNum < attrs.getLength()",
                    "++attrNum");
        jw.writeEol("attr = (org.w3c.dom.Attr) attrs.item(attrNum)");
        jw.writeEol("String attrName = attr.getName()");
        //jw.writeEol("System.out.println(\"attrName=\"+attrName)
        jw.beginIf("attrName.startsWith(\"xmlns:\")");
        if (firstNamespaceVarName != null) {
            jw.beginIf(firstNamespaceVarName);
            jw.writeEol(firstNamespaceVarName, " = false");
            jw.comment("Dup prefix map, so as to not write over previous values, and to make it easy to clear out our entries.");
            jw.write(namespaceMapName, " = new java.util.HashMap");
            if (config.jdkTarget >= 150)
                jw.write("<String, String>");
            jw.writeEol("(", namespaceMapName, ")");
            jw.end();
        }
        jw.writeEol("String attrNSPrefix = attrName.substring(6)");
        jw.writeEol(namespaceMapName, ".put(attrNSPrefix, attr.getValue())");
        jw.end();
        jw.end();
    }

    protected void genWriteAttr(Property a) throws IOException {
        genWriteAttr(a, "_"+a.name);
    }
    
    protected void genWriteAttr(Property a, String varName) throws IOException {
        boolean isScalar = a.isScalar();
        String dtdName = a.dtdName;
        String namespace = a.getNamespace();

        comment(dtdName+" is an attribute with namespace "+namespace);
        if (!isScalar) {
            gen("if ("+varName+" != null) ");
            begin();
        }
        if (namespace != null && !namespace.equals(getDefaultNamespace()) && !namespace.equals("http://www.w3.org/XML/1998/namespace")) {
            dtdName = SchemaRep.removePrefix(dtdName);
            outWrite(" ");
            jw.writeEol("out.write((String)namespaceMap.get(",
                     JavaUtil.instanceFrom("String", namespace),
                     "))");
            jw.writeEol("out.write(\":", dtdName, "='\")");
        } else {
            jw.writeEol("out.write(\" ", dtdName, "='\")");
        }
        genWriteType(a, varName, true);
        gen("out.write(\"'\")");
        eolNoI18N();
        if (!isScalar) {
            end();
        }
    }

    protected void genWriteType(Property a, String varName,
                                boolean isAttribute) throws IOException {
        String type = a.getType();
        boolean isScalar = a.isScalar();

        if (isScalar || JavaUtil.canProduceNoXMLMetaChars(type)) {
            jw.write("out.write("+JavaUtil.typeToString(type, varName));
            jw.writeEol(")");
        } else if (isTypeQName(type)) {
            jw.beginIf(varName+".getNamespaceURI() != null && !\"\".equals(",
                       varName, ".getNamespaceURI())");
            jw.writeEol("out.write((String) namespaceMap.get(", varName,
                        ".getNamespaceURI()))");
            jw.writeEol("out.write(\":\")");
            jw.end();
            if (config.isUseRuntime())
                jw.write("org.netbeans.modules.schema2beans.XMLUtil");
            else
                jw.write(getRootClassName());
            jw.write(".writeXML(out, ");
            jw.write(varName, ".getLocalPart(), "+isAttribute);
            jw.writeEol(")");
        } else if ("byte[]".equals(type)) {
            // Is this hexBinary or base64Binary?
            SchemaRep.EncodingStyle style = (SchemaRep.EncodingStyle) a.searchExtraData(SchemaRep.EncodingStyle.class);
            if (style instanceof SchemaRep.HexBinary) {
                jw.beginFor("int byteIndex = 0", "byteIndex < "+varName+".length",
                            "++byteIndex");
                jw.writeEol("int belement = (int) ", varName, "[byteIndex]");
                jw.beginIf("belement < 0");
                jw.writeEol("belement += 256");
                jw.end();
                jw.beginIf("belement < 16");
                jw.writeEol("out.write(\"0\")");
                jw.end();
                jw.writeEol("out.write(Integer.toHexString(belement).toUpperCase())");
                jw.end();
            } else if (style instanceof SchemaRep.Base64Binary) {
                jw.write("out.write(");
                jw.write(getRootClassName());
                jw.write(".encodeBase64BinaryString(");
                jw.write(varName);
                jw.write(")");
                jw.writeEol(")");
            } else {
                config.messageOut.println("Unknown encoding style for "+type+" for property "+a.name);
            }
        } else {
            if (config.isUseRuntime())
                jw.write("org.netbeans.modules.schema2beans.XMLUtil");
            else
                jw.write(getRootClassName());
            jw.write(".writeXML(out, ");
            if ("java.util.Calendar".equals(type)) {
                jw.write(getRootClassName());
                jw.write(".calendarToString(");
                jw.write(varName);
                jw.write(")");
            } else {
                jw.write(JavaUtil.typeToString(type, varName));
            }
            jw.write(", "+isAttribute);
            jw.writeEol(")");
        }
    }

    protected void beginAttrProcessing(String nodeName) throws IOException {
        jw.beginIf(nodeName, ".hasAttributes()");
        declareAttrsForRead(nodeName);
    }

    protected void genReadAttr(Property a, String nodeName) throws IOException {
        genReadAttr("_" + a.name, a.getType().intern(), a.dtdName, nodeName,
                    (SchemaRep.WhiteSpace) a.searchExtraData(SchemaRep.WhiteSpace.class),
                    a.isIndexed(), a.getAddMethod(), a.isScalar(),
                    (SchemaRep.EncodingStyle) a.searchExtraData(SchemaRep.EncodingStyle.class));
    }

    protected void genReadAttr(String attr, String type, String dtdName,
                               String nodeName,
                               SchemaRep.WhiteSpace ws,
                               boolean isIndexed,
                               String addMethod, boolean isScalar,
                               SchemaRep.EncodingStyle style) throws IOException {
        String baseType = type;
        jw.writeEol("attr = (org.w3c.dom.Attr) attrs.getNamedItem(\"",
                    dtdName, "\")");
        List exceps = JavaUtil.exceptionsFromParsingText(baseType);
        String var = "attrValue";
        jw.beginIf("attr != null");
        jw.writeEol("attrValue = attr.getValue()");
        if (ws != null) {
            genWhiteSpaceRestriction(ws, var, "java.lang.String");
        }
        if (isIndexed) {
            jw.endElseBegin();
            jw.writeEol("attrValue = null");
            jw.end();
        }
        if (!exceps.isEmpty()) {
            jw.beginTry();
        }
        genReadType(baseType, attr, var, isIndexed, addMethod, isScalar, style);
        if (!exceps.isEmpty()) {
            jw.end();
            genRethrowExceptions(exceps);
        }
        if (!isIndexed)
            jw.end();
    }

    /**
     * @return whether or not a setter was generated.
     */
    protected boolean genReadType(String type, String var, String expr,
                               boolean isIndexed, String addMethod,
                               boolean isScalar, SchemaRep.EncodingStyle style) throws IOException {
        type = type.intern();
        if (isIndexed) {
            jw.writeEol(type, " processedValueFor", var);
            if (genReadType(type, "processedValueFor"+var, expr, false, null,
                            isScalar, style)) {
                jw.writeEol(addMethod, "(processedValueFor", var, ")");
            }
        } else if (isTypeQName(type)) {
            jw.writeEol("int colonPos = ", expr, ".indexOf(':')");
            //jw.writeEol("System.out.println(\"colonPos=\"+colonPos+\" childNodeValue=\"+childNodeValue)");
            jw.beginIf("colonPos < 0");
            jw.write(var, " = new ", type);
            jw.writeEol("(", expr, ")");
            jw.endElseBegin();
            jw.writeEol("java.util.Map nsPrefixes = new java.util.HashMap(namespacePrefixes)");
            genUpdateNamespaces("nsPrefixes", null);
            jw.writeEol("String prefix = ", expr, ".substring(0, colonPos)");
            jw.writeEol("String ns = (String) nsPrefixes.get(prefix)");
            //jw.writeEol("System.out.println(\"prefix=\"+prefix+\" ns=\"+ns)");
            jw.write("String localPart = ", expr);
            jw.writeEol(".substring(colonPos+1, ", expr, ".length())");
            jw.writeEol(var, " = new ", type, "(ns, localPart, prefix)");
            jw.end();
        } else if (type == "byte[]") {
            // Is this hexBinary or base64Binary?
            if (style instanceof SchemaRep.HexBinary) {
                // example: 07FB
                jw.writeEol(var, " = new byte[(childNodeValue.length()+1) / 2]");
                jw.beginFor("int byteIndex = 0", "byteIndex < "+var+".length",
                            "++byteIndex");
                jw.writeEol("String octet = childNodeValue.substring(byteIndex * 2, byteIndex * 2 + 2)");
                jw.writeEol(var, "[byteIndex] = Integer.valueOf(octet, 16).byteValue()");
                jw.end();
            } else if (style instanceof SchemaRep.Base64Binary) {
                jw.write(var, " = ");
                jw.write(getRootClassName());
                jw.writeEol(".decodeBase64BinaryString(childNodeValue)");
            } else {
                config.messageOut.println("Unknown encoding style for "+type+" for "+var);
            }
        } else if ((!isScalar && !JavaUtil.isInstantiable(type)
                    && type != "java.util.Calendar") ||
                   type == "org.netbeans.modules.schema2beansdev.beangraph.BeanGraph" ||
                   type == "org.netbeans.modules.schema2beansdev.metadd.MetaDD" ||
                   type == "java.io.PrintStream") {
            config.messageOut.println("Warning: Don't know how to create a "+type);
            jw.comment("Don't know how to create a "+type);
            return false;
        } else {
            if (type == "java.util.Calendar") {
                jw.write(var, " = ");
                jw.write(getRootClassName());
                jw.writeEol(".stringToCalendar(", expr, ")");
            } else {
                jw.writecr(JavaUtil.genParseText(type, expr, var,
                                                 config.isForME()));
            }
        }
        return true;
    }

    protected void declareAttrsForRead(String nodeName) throws IOException {
        jw.writeEol("org.w3c.dom.NamedNodeMap attrs = ", nodeName, ".getAttributes()");
        jw.writeEol("org.w3c.dom.Attr attr");
        jw.writeEol("java.lang.String attrValue");
    }

    public void genPrintXML() throws IOException {
        jw.bigComment("Takes some text to be printed into an XML stream and escapes any\ncharacters that might make it invalid XML (like '<').");
        jw.beginMethod("writeXML", "java.io.Writer out, String msg", "java.io.IOException", "void", jw.PUBLIC | jw.STATIC | jw.IO);
        geneol("writeXML(out, msg, true)");
        end();
        cr();
        jw.beginMethod("writeXML", "java.io.Writer out, String msg, boolean attribute",  "java.io.IOException", "void", jw.PUBLIC | jw.STATIC | jw.IO);
        gencr("if (msg == null)");
        tabIn();
        geneol("return");
        geneol("int msgLength = msg.length()");
        jw.beginFor("int i = 0", "i < msgLength", "++i");
        geneol("char c = msg.charAt(i)");
        geneol("writeXML(out, c, attribute)");
        end();
        end();
        cr();
        jw.beginMethod("writeXML", "java.io.Writer out, char msg, boolean attribute", "java.io.IOException", "void", jw.PUBLIC | jw.STATIC | jw.IO);
        gencr("if (msg == '&')");
        tabIn();
        geneol("out.write(\"&amp;\")");
        gencr("else if (msg == '<')");
        tabIn();
        geneol("out.write(\"&lt;\")");
        gencr("else if (msg == '>')");
        tabIn();
        geneol("out.write(\"&gt;\")");
        gen("else ");
        jw.beginIf("attribute");
        gencr("if (msg == '\"')");
        tabIn();
        geneol("out.write(\"&quot;\")");
        gencr("else if (msg == '\\'')");
        tabIn();
        geneol("out.write(\"&apos;\")");
        gencr("else if (msg == '\\n')");
        tabIn();
        geneol("out.write(\"&#xA;\")");
        gencr("else if (msg == '\\t')");
        tabIn();
        geneol("out.write(\"&#x9;\")");
        gencr("else");
        tabIn();
        geneol("out.write(msg)");
        jw.end();
        gencr("else");
        tabIn();
        geneol("out.write(msg)");
        jw.endMethod();
    }

    protected void outWrite(String text) throws IOException {
        jw.writeEol("out.write(",
                    JavaUtil.instanceFrom("String", text),
                    ")");
    }

    /**
     * Generate stuff for special types as needed.
     */
    public void genSpecialTypes() throws IOException {
        if (rootBeanElement.isUsedType("java.util.Calendar")) {
            JavaBeansUtil.genReadType(jw, "java.util.Calendar");
            JavaBeansUtil.genWriteType(jw, "java.util.Calendar");
            jw.cr();
        }
        if (rootBeanElement.isUsedType("byte[]")) {
            JavaBeansUtil.genReadType(jw, "base64Binary");
            JavaBeansUtil.genWriteType(jw, "base64Binary");
            jw.cr();
        }
    }

    public void genValidate() throws IOException {
        select(BODY_SECTION);
        if (beanElement.isRoot && !config.isUseRuntime()) {
            String commonBeanType = commonBeanType();
            jw.write("public static class ValidateException extends Exception ");
            jw.begin();
            jw.write("private ", commonBeanType);
            jw.writeEol(" failedBean");
            jw.writeEol("private String failedPropertyName");
            jw.writeEol("private FailureType failureType");
            jw.write("public ValidateException(String msg, String failedPropertyName, ", commonBeanType, " failedBean) ");
            jw.begin();
            jw.writeEol("super(msg)");
            jw.writeEol("this.failedBean = failedBean");
            jw.writeEol("this.failedPropertyName = failedPropertyName");
            jw.end();
            jw.write("public ValidateException(String msg, FailureType ft, String failedPropertyName, ", commonBeanType, " failedBean) ");
            jw.begin();
            jw.writeEol("super(msg)");
            jw.writeEol("this.failureType = ft");
            jw.writeEol("this.failedBean = failedBean");
            jw.writeEol("this.failedPropertyName = failedPropertyName");
            jw.end();
            jw.writecr("public String getFailedPropertyName() {return failedPropertyName;}");
            jw.writecr("public FailureType getFailureType() {return failureType;}");
            jw.write("public ", commonBeanType);
            jw.writecr(" getFailedBean() {return failedBean;}");
            jw.write("public static class FailureType ");
            jw.begin();
            jw.writeEol("private final String name");
            jw.writecr("private FailureType(String name) {this.name = name;}");
            jw.writecr("public String toString() { return name;}");
            jw.writeEol("public static final FailureType NULL_VALUE = new FailureType(\"NULL_VALUE\")");
            jw.writeEol("public static final FailureType DATA_RESTRICTION = new FailureType(\"DATA_RESTRICTION\")");
            jw.writeEol("public static final FailureType ENUM_RESTRICTION = new FailureType(\"ENUM_RESTRICTION\")");
            jw.writeEol("public static final FailureType ALL_RESTRICTIONS = new FailureType(\"ALL_RESTRICTIONS\")");
            jw.writeEol("public static final FailureType MUTUALLY_EXCLUSIVE = new FailureType(\"MUTUALLY_EXCLUSIVE\")");
            jw.end();
            jw.end();
            jw.cr();
        }
        jw.beginMethod("validate", "", (config.isUseRuntime() ? "org.netbeans.modules.schema2beans" : getRootClassName())+".ValidateException", "void", jw.PUBLIC);
        genValidateProperties();
        if (config.isRespectExtension() && beanElement.getExtension() != null) {
            jw.writeEol("super.validate()");
        }
        jw.endMethod();
    }

    protected void genValidateFail(String detail, String name,
                                   boolean quoteDetail,
                                   ValidateException.FailureType ft,
                                   JavaWriter out) throws IOException {
        out.write("throw new ");
        String validateException;
        if (config.isUseRuntime())
            validateException = "org.netbeans.modules.schema2beans.ValidateException";
        else
            validateException = getRootClassName()+".ValidateException";
        out.write(validateException);
        out.write("(");
        if (quoteDetail)
            out.write('"');
        out.write(detail);
        if (quoteDetail)
            out.write('"');
        out.write(", ", validateException+".FailureType.",
                 ft.toString());
        out.writeEolNoI18N(", \""+name+"\", this)");
    }

    public void genPropertyEvents() throws IOException {
        select(DECL_SECTION);
        gen(PRIVATE, "java.beans.PropertyChangeSupport", "eventListeners");
        eol();
        select(BODY_SECTION);
        jw.beginMethod("addPropertyChangeListener",
                       "java.beans.PropertyChangeListener listener", null,
                       "void", jw.PUBLIC | jw.BEANINFO);
        jw.beginIf("eventListeners == null");
        geneol("eventListeners = new java.beans.PropertyChangeSupport(this)");
        end();
        geneol("eventListeners.addPropertyChangeListener(listener)");
        genCallMethodOnBeans(".addPropertyChangeListener(listener)");
        jw.endMethod();

        jw.beginMethod("removePropertyChangeListener",
                       "java.beans.PropertyChangeListener listener", null,
                       "void", jw.PUBLIC | jw.BEANINFO);
        genCallMethodOnBeans(".removePropertyChangeListener(listener)");
        jw.beginIf("eventListeners == null");
        geneol("return");
        end();
        geneol("eventListeners.removePropertyChangeListener(listener)");
        jw.beginIf("!eventListeners.hasListeners(null)");
        geneol("eventListeners = null");
        jw.end();
        jw.endMethod();

        jw.beginMethod("_setPropertyChangeSupport",
                       "java.beans.PropertyChangeSupport listeners", null,
                       "void", jw.PUBLIC);
        jw.writeEol("eventListeners = listeners");
        genCallMethodOnBeans("._setPropertyChangeSupport(listeners)");
        jw.endMethod();
    }

    protected void genCallMethodOnBeans(String methodParams) throws IOException {
        int size = attrList.size();
        for (int i = 0; i < size; i++) {
            Property a = (Property) attrList.get(i);
            if (!a.isBean)
                continue;
            boolean indexed = a.isIndexed();
            String attr = "_" + a.name;
            String type = a.getType().intern();
            String baseType = type;
            if (indexed) {
                beginAttrIterator(attr, a, "element");
                attr = "element";
            }
            jw.beginIf(attr, " != null");
            jw.writeEol(attr, methodParams);
            jw.end();
            if (indexed)
                jw.end();
        }
    }

    public void genStoreEvents() throws IOException {
        select(DECL_SECTION);
        gen(PROTECTED, "boolean", "storeEvents = false");
        eol();
        gen(PRIVATE, "java.util.List", "storedEvents = new java.util.LinkedList()");
        eol();
        select(BODY_SECTION);
        jw.beginMethod("fireStoredEvents");
        jw.beginIf("eventListeners == null");
        geneol("storedEvents.clear()");
        geneol("return");
        end();
        /*
        comment("Compress the events");
        geneol("java.util.Map uniqueEvents = new java.util.HashMap(storedEvents.size()*4)");
        gen("for (java.util.Iterator it = storedEvents.iterator(); it.hasNext(); ) ");
        begin();
        geneol("java.beans.PropertyChangeEvent event = (java.beans.PropertyChangeEvent) it.next()");
        comment("The last event by the property name is stored in uniqueEvents");
        gencr("if (uniqueEvents.containsKey(event.getPropertyName())");
        tabIn();
        geneol("uniqueEvents.put(event.getPropertyName(), event)");
        end();
        */
        jw.beginFor("java.util.Iterator it = storedEvents.iterator()",
                    "it.hasNext()", "");
        geneol("java.beans.PropertyChangeEvent event = (java.beans.PropertyChangeEvent) it.next()");
        /*gencr("if (uniqueEvents.get(event.getPropertyName()) == event)");
          tabIn();*/
        geneol("eventListeners.firePropertyChange(event)");
        end();
        geneol("storedEvents.clear()");
        end();
        cr();
    }

    public void genVetoable() throws IOException {
        select(DECL_SECTION);
        gen(PRIVATE, "java.beans.VetoableChangeSupport", "vetos");
        eol();
        select(BODY_SECTION);
        jw.beginMethod("addVetoableChangeListener",
                       "java.beans.VetoableChangeListener listener", null,
                       "void", jw.PUBLIC | jw.BEANINFO);
        jw.beginIf("vetos == null");
        geneol("vetos = new java.beans.VetoableChangeSupport(this)");
        end();
        geneol("vetos.addVetoableChangeListener(listener)");
        end();
        cr();
        jw.beginMethod("removePropertyChangeListener",
                       "java.beans.VetoableChangeListener listener", null,
                       "void", jw.PUBLIC | jw.BEANINFO);
        jw.beginIf("vetos == null");
        geneol("return");
        end();
        geneol("vetos.removeVetoableChangeListener(listener)");
        jw.beginIf("!vetos.hasListeners(null)");
        geneol("vetos = null");
        end();
        end();
        cr();
        jw.beginMethod("_setVetoableChangeSupport",
                       "java.beans.VetoableChangeSupport vs", null,
                       "void", jw.PACKAGE_LEVEL);
        geneol("vetos = vs");
        end();
        cr();
    }

    public void genElementPositions() throws IOException {
        select(BODY_SECTION);
        jw.beginMethod("fetchChildByPosition", "int position", null,
                       "java.lang.Object", jw.PUBLIC);
        jw.beginIf("elementTypesByPosition[position] == "+elementTypeSetnull);
        jw.writeEol("throw new IndexOutOfBoundsException(\"position \"+position+\" out of bounds\")");
        jw.end();
        jw.writeEol("return elementsByPosition[position]");
        jw.endMethod();
        jw.beginMethod("fetchChildCount", "", null, "int", jw.PUBLIC);
        jw.writeEol("int position = elementTypesByPosition.length");
        jw.beginFor("", "position > 0 && elementTypesByPosition[position-1] == "+elementTypeSetnull, "--position");
        jw.writeEol("");
        jw.end();
        jw.writeEol("return position");
        jw.endMethod();
        jw.beginMethod("expandElementsByPosition", "int size", null, "void",
                       jw.PROTECTED);
        jw.beginIf("elementTypesByPosition.length >= size");
        jw.writeEol("return");
        jw.end();
        jw.writeEol("int newSize = elementTypesByPosition.length+8");
        jw.writeEol("java.lang.Object[] newElementsByPosition = new java.lang.Object[newSize]");
        jw.writeEol("int[] newElementTypesByPosition = new int[newSize]");
        jw.writeEol("System.arraycopy(elementsByPosition, 0, newElementsByPosition, 0, elementTypesByPosition.length)");
        jw.writeEol("System.arraycopy(elementTypesByPosition, 0, newElementTypesByPosition, 0, elementTypesByPosition.length)");
        jw.beginFor("int i = elementTypesByPosition.length",
                    "i < newSize", "++i");
        jw.writeEol("newElementTypesByPosition[i] = "+elementTypeSetnull);
        jw.end();
        jw.writeEol("elementTypesByPosition = newElementTypesByPosition");
        jw.writeEol("elementsByPosition = newElementsByPosition");
        jw.endMethod();

        jw.beginMethod("insertElementByPosition",
                       "int position, Object element, int elementType",
                       null, "void",
                       jw.PROTECTED);
        jw.writeEol("int childCount = fetchChildCount()");
        jw.writeEol("expandElementsByPosition(childCount+1)");
        jw.writeEol("int i = childCount - 1");
        jw.beginFor("", "i >= position", "--i");
        jw.writeEol("elementsByPosition[i + 1] = elementsByPosition[i]");
        jw.writeEol("elementTypesByPosition[i + 1] = elementTypesByPosition[i]");
        jw.end();
        jw.writeEol("elementTypesByPosition[position] = elementType");
        jw.writeEol("elementsByPosition[position] = element");
        jw.comment("assert childCount == fetchChildCount() + 1;");
        jw.endMethod();

        jw.beginMethod("deleteElement", "int position", null, "void",
                       jw.PROTECTED);
        jw.writeEol("int i = position+1");
        jw.writeEol("int size = elementTypesByPosition.length");
        jw.beginFor("", "i < size && elementTypesByPosition[i] != "+elementTypeSetnull, "++i");
        jw.writeEol("elementsByPosition[i - 1] = elementsByPosition[i]");
        jw.writeEol("elementTypesByPosition[i - 1] = elementTypesByPosition[i]");
        //jw.writeEol("System.out.println(\"elementTypesByPosition[\"+(i-1)+\"]=\"+elementTypesByPosition[i-1])");
        jw.end();
        jw.writeEol("elementTypesByPosition[i-1] = "+elementTypeSetnull);
        jw.writeEol("elementsByPosition[i - 1] = null");
        jw.comment("assert size == fetchChildCount() - 1;");
        jw.endMethod();

        jw.beginMethod("findFirstOfElementType", "int elementType", null,
                       "int", jw.PROTECTED);
        jw.writeEol("int maxPos = elementTypesByPosition.length");
        jw.writeEol("int pos = 0");
        jw.beginFor("", "pos < maxPos && elementTypesByPosition[pos] < elementType",
                    "++pos");
        jw.end();
        jw.writeEol("return pos");
        jw.endMethod();

        jw.beginMethod("findElementType", "int elementType, int index", null,
                       "int", jw.PROTECTED);
        jw.writeEol("int maxPos = elementTypesByPosition.length");
        jw.writeEol("int pos = 0");
        jw.beginFor("", "pos < maxPos", "++pos");
        jw.beginIf("elementTypesByPosition[pos] == elementType");
        jw.beginIf("index <= 0");
        jw.writeEol("return pos");
        jw.end();
        jw.writeEol("--index");
        jw.end();
        jw.end();
        jw.writeEol("return pos");
        jw.endMethod();

        jw.beginMethod("findLastOfElementType", "int elementType", null, "int",
                       jw.PROTECTED);
        jw.writeEol("int childCount = elementTypesByPosition.length");
        jw.writeEol("int pos = childCount - 1");
        // We can't do a binary search here, since the element types are
        // not always sorted.
        jw.beginFor("", "pos >=0", "--pos");
        jw.beginIf("elementTypesByPosition[pos] == "+elementTypeSetnull);
        jw.writeEol("--childCount");
        jw.writeEol("continue");
        jw.end();
        jw.beginIf("elementTypesByPosition[pos] <= elementType");
        jw.writeEol("return pos");
        jw.end();
        jw.end();
        //jw.beginIf("childCount < 0");
        //jw.writeEol("return 0");
        //jw.end();
        jw.writeEol("return childCount");
        jw.endMethod();
    }

    protected void genProcessDocType(boolean java5) throws IOException {
        String fullDocTypeName;
        fullDocTypeName = fullClassName+".DocType";
        select(DECL_SECTION);
        jw.writeEol("private "+fullDocTypeName+" docType = null");
        select(ACCESS_SECTION);
        jw.beginMethod("fetchDocType", "", null, fullDocTypeName, jw.PUBLIC);
        jw.writeEol("return docType");
        jw.endMethod();
        jw.beginMethod("changeDocType", fullDocTypeName+" dt", null, "void", jw.PUBLIC);
        jw.writeEol("docType = dt");
        jw.endMethod();
        jw.beginMethod("changeDocType", "String publicId, String systemId", null, "void", jw.PUBLIC);
        jw.writeEol("docType = new ", fullDocTypeName, "(publicId, systemId)");
        jw.endMethod();
        select(BODY_SECTION);
        jw.write("public static class DocType ");
        jw.begin();
        jw.writeEol("private org.w3c.dom.NamedNodeMap entities");
        jw.writeEol("private String internalSubset");
        jw.writeEol("private String name");
        jw.writeEol("private org.w3c.dom.NamedNodeMap notations");
        jw.writeEol("private String publicId");
        jw.writeEol("private String systemId");
        jw.cr();
        jw.write("public DocType("+fullDocTypeName+" docType) ");
        jw.begin();
        jw.writeEol("entities = docType.getEntities()");
        jw.writeEol("internalSubset = docType.getInternalSubset()");
        jw.writeEol("name = docType.getName()");
        jw.writeEol("notations = docType.getNotations()");
        jw.writeEol("publicId = docType.getPublicId()");
        jw.writeEol("systemId = docType.getSystemId()");
        jw.endMethod();
        jw.write("public DocType(org.w3c.dom.DocumentType docType) ");
        jw.begin();
        jw.writeEol("entities = docType.getEntities()");
        jw.writeEol("internalSubset = docType.getInternalSubset()");
        jw.writeEol("name = docType.getName()");
        jw.writeEol("notations = docType.getNotations()");
        jw.writeEol("publicId = docType.getPublicId()");
        jw.writeEol("systemId = docType.getSystemId()");
        jw.endMethod();
        jw.write("public DocType(String publicId, String systemId) ");
        jw.begin();
        jw.writeEol("this(\"",
                    beanElement.node.getName(), "\", publicId, systemId)");
        jw.end();
        jw.cr();
        jw.write("public DocType(String name, String publicId, String systemId) ");
        jw.begin();
        jw.writeEol("this.name = name");
        jw.writeEol("this.publicId = publicId");
        jw.writeEol("this.systemId = systemId");
        jw.endMethod();
        jw.write("public org.w3c.dom.NamedNodeMap getEntities() ");
        jw.begin();
        jw.writeEol("return entities");
        jw.endMethod();
        jw.write("public String getInternalSubset() ");
        jw.begin();
        jw.writeEol("return internalSubset");
        jw.endMethod();
        jw.write("public String getName() ");
        jw.begin();
        jw.writeEol("return name");
        jw.endMethod();
        jw.write("public org.w3c.dom.NamedNodeMap getNotations() ");
        jw.begin();
        jw.writeEol("return notations");
        jw.endMethod();
        jw.write("public String getPublicId() ");
        jw.begin();
        jw.writeEol("return publicId");
        jw.endMethod();
        jw.write("public String getSystemId() ");
        jw.begin();
        jw.writeEol("return systemId");
        jw.endMethod();
        jw.write("public String toString() ");
        jw.begin();
        if (java5) {
            jw.writeEol("java.lang.StringBuilder result = new java.lang.StringBuilder(\"<!DOCTYPE \")");
        } else {
            jw.writeEol("java.lang.StringBuffer result = new java.lang.StringBuffer(\"<!DOCTYPE \")");
        }
        jw.writeEol("result.append(name)");
        jw.beginIf("publicId != null");
        jw.writeEol("result.append(\" PUBLIC \\\"\")");
        jw.writeEol("result.append(publicId)");  // should be printXML
        jw.writeEol("result.append(\"\\\"\")");
        jw.beginIf("systemId == null");
        jw.writeEol("systemId = \"SYSTEM\"");
        jw.end();
        jw.end();
        jw.beginIf("systemId != null");
        jw.writeEol("result.append(\" \\\"\")");
        jw.writeEol("result.append(systemId)");
        jw.writeEol("result.append(\"\\\"\")");
        jw.end();
        jw.beginIf("entities != null");
        jw.writeEol("int length = entities.getLength()");
        jw.beginIf("length > 0");
        jw.writeEol("result.append(\" [\")");
        jw.beginFor("int i = 0", "i < length", "++i");
        jw.writeEol("org.w3c.dom.Node node = entities.item(i)");
        jw.writeEol("result.append(\"<\"+node.getNodeName()+\">\")");
        jw.writeEol("result.append(node.getNodeValue())");
        jw.writeEol("result.append(\"</\"+node.getNodeName()+\">\")");
        jw.end();
        jw.writeEol("result.append(\"]\")");
        jw.end();
        jw.end();
        jw.writeEol("result.append(\">\")");
        jw.writeEol("return result.toString()");
        jw.end();

        jw.endMethod();
    }

    public void genTransactions() throws IOException {
        select(BODY_SECTION);
        jw.beginMethod("beginTransaction");
        //geneol("inTransaction = true");
        geneol("storeEvents = true");
        comment("Tell each child bean to also beginTransaction");
        genCallMethodOnSubBeans("beginTransaction()");
        end();
        cr();

        jw.beginMethod("commit");
        geneol("storeEvents = false");
        comment("Tell each child bean to also commit");
        genCallMethodOnSubBeans("commit()");
        geneol("fireStoredEvents()");
        end();
        cr();

        jw.beginMethod("rollback");
        comment("Go over all of the events and put them back");
        geneol("java.beans.PropertyChangeEvent[] events = new java.beans.PropertyChangeEvent[storedEvents.size()]");
        jw.beginFor("int i = events.length-1", "i >= 0", "--i");
        geneol("java.beans.PropertyChangeEvent event = events[i]");
        //jw.beginIf("event.getOldValue() == null");
        geneol("changePropertyByName(event.getPropertyName(), event.getOldValue())");
        end();
        // we might be able to speed things up by setting eventListeners = null
        geneol("storeEvents = false");
        geneol("storedEvents.clear()");
        comment("Tell each child bean to also rollback");
        genCallMethodOnSubBeans("rollback()");
        end();
        cr();
    }

    protected void genCallMethodOnSubBeans(String methodName) throws IOException {
        int size = attrList.size();
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            boolean   	indexed = a.isIndexed();
            String attr = "_" + a.name;
            String type = a.getType();
            if (a.isBean) {
                String value = attr;
                if (indexed) {
                    beginAttrIterator(attr, a, "element");
                    value = "element";
                }
                gencr("if ("+value+" != null)");
                tabIn();
                gen(value);
                gen(".");
                geneol(methodName);
                if (indexed)
                    end();
            }
        }
    }

    public void genPropertiesByName() throws IOException {
        jw.beginMethod("changePropertyByName", "String name, Object value",
                       null, "void", jw.PUBLIC);
        geneol("if (name == null) return");
        geneol("name = name.intern()");
        int size = attrList.size();
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            boolean   	indexed = a.isIndexed();
            boolean isScalar = a.isScalar();
            String attr = "_" + a.name;
            String type = a.getType();
            if (i > 0)
                gen("else ");
            gencr("if (\""+a.beanIntrospectorName()+"\".equals(name))");
            tabIn();
            if (indexed) {
                gen("add"+a.name);
                geneol("("+JavaUtil.fromObject(type, "value")+")");
                gen("else ");
                gencr("if (\""+a.beanIntrospectorName()+"[]\".equals(name))");
                tabIn();
                gen(a.getWriteMethod()+"(");
                geneol("("+type+"[]) value)");
            } else {
                gen(a.getWriteMethod()+"(");
                gen(JavaUtil.fromObject(type, "value"));
                geneol(")");
            }
        }
        if (size > 0) {
            gencr("else");
            tabIn();
            if (config.isRespectExtension() && beanElement.getExtension() != null) {
                jw.writeEol("super.changePropertyByName(name, value)");
            } else {
                genInvalidName("name");
            }
        }
        genMadeChange();
        end();
        cr();

        jw.beginMethod("fetchPropertyByName", "String name", null, "Object", jw.PUBLIC);
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            boolean   	indexed = a.isIndexed();
            String type = a.getType();
            gen("if (\""+a.beanIntrospectorName());
            if (indexed)
                gen("[]");
            gencr("\".equals(name))");
            tabIn();
            jw.write("return ");
            if (indexed)
                jw.writeEol(a.getReadMethod(false)+"()");
            else
                jw.writeEol(JavaUtil.toObject(a.getReadMethod(false)+"()", type,
                                              config.isForME(), config.isJava5()));
        }
        if (config.isRespectExtension() && beanElement.getExtension() != null) {
            jw.writeEol("return super.fetchPropertyByName(name)");
        } else {
            genInvalidName("name");
        }
        jw.endMethod();
    }

    public void genEqualsHashCode() throws IOException {
        select(EQUALS_SECTION);
        jw.beginMethod("equals", "Object o", null, "boolean", jw.PUBLIC);
        jw.write("return o instanceof ", fullClassName);
        jw.writeEol(" && equals((", fullClassName, ") o)");
        jw.endMethod();
        
        jw.beginMethod("equals", fullClassName+" inst", null, "boolean", jw.PUBLIC);
        jw.beginIf("inst == this");
        jw.writeEol("return true");
        jw.end();
        jw.beginIf("inst == null");
        jw.writeEol("return false");
        jw.end();
        
        select(HASHCODE_SECTION);
        jw.beginMethod("hashCode", "", null, "int", jw.PUBLIC);
        geneol("int result = 17");

        boolean isArrayStyle = (config.getIndexedPropertyType() == null);
        int size = attrList.size();
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            boolean   	indexed = a.isIndexed();
            boolean	isWrapper = false;
            MetaElement	me = getMetaElement(a);
            boolean isScalar = a.isScalar();

            String type = a.getType().intern();
            String baseType = type;
            if (indexed)
                type = (baseType + "[]").intern();

            String attr = "_" + a.name;
            String baseAttr = attr;
            String otherAttr = "inst."+attr;
            select(EQUALS_SECTION);
            if (indexed) {
                gen("if (size"+a.name+"() != inst.size"+a.name+"())");
                cr();
                tabIn();
                gen("return false");
                eol();
                comment("Compare every element.");
                String fullType = getTypeFullClassName(a);
                if (isArrayStyle) {
                    jw.beginFor("int pos = 0, size = size"+a.name+"()",
                                "pos < size", "++pos");
                    attr = attr+"[pos]";
                    otherAttr = otherAttr+"[pos]";
                } else {
                    jw.beginFor("java.util.Iterator it = "+attr+".iterator(), it2 = inst."+attr+".iterator()",
                                "it.hasNext() && it2.hasNext()", "");
                    gen(fullType);
                    gen(" element = ");
                    geneol(JavaUtil.fromObject(fullType, "it.next()"));
                    gen(fullType);
                    gen(" element2 = ");
                    geneol(JavaUtil.fromObject(fullType, "it2.next()"));
                    attr = "element";
                    otherAttr = "element2";
                }
            }
            if (!indexed && config.isOptionalScalars() && a.isScalar()) {
                jw.beginIf(a.getScalarIsSet()+" != inst."+a.getScalarIsSet());
                jw.writeEol("return false");
                jw.end();
                jw.beginIf(a.getScalarIsSet());
            }
            jw.beginIf("!("+JavaUtil.genEquals(baseType, attr, otherAttr)+")");
            jw.writeEol("return false");
            jw.end();
            if (!indexed && config.isOptionalScalars() && a.isScalar()) {
                jw.end();
            }
            if (indexed) {
                end();
            }

            select(HASHCODE_SECTION);
            gen("result = 37*result + (");
            if (isScalar) {
                if (!indexed && config.isOptionalScalars() && a.isScalar())
                    gen(a.getScalarIsSet(), " ? 0 : (");
                gen(JavaUtil.exprToInt(type, baseAttr));
                if (!indexed && config.isOptionalScalars() && a.isScalar())
                    gen(")");
            } else
                gen(baseAttr+" == null ? 0 : "+baseAttr+".hashCode()");
            gen(")");
            eol();
        }
        select(EQUALS_SECTION);
        if (config.isRespectExtension() && beanElement.getExtension() != null) {
            jw.writeEol("return super.equals(inst)");
        } else {
            jw.writeEol("return true");
        }
        jw.endMethod();

        select(HASHCODE_SECTION);
        if (config.isRespectExtension() && beanElement.getExtension() != null) {
            jw.writeEol("result = 37*result + super.hashCode()");
        }
        jw.writeEol("return result");
        jw.endMethod();
    }

    void genDeepCopy() throws IOException {
        select(CONSTRUCTOR_SECTION);
        jw.bigComment("Deep copy");
        jw.beginConstructor(className, fullClassName+" source");
        jw.writeEol("this(source, false)");
        jw.end();
        jw.cr();
        jw.bigComment("Deep copy\n@param justData just copy the XML relevant data");
        jw.beginConstructor(className, fullClassName+" source, boolean justData");
        if (config.isGenerateParentRefs()) {
            jw.writeEol("this(source, null, justData)");
            jw.end();
            jw.cr();
            jw.bigComment("Deep copy");
            jw.beginConstructor(className, fullClassName+" source, "+parentBeanType()+" parent, boolean justData");
        }
        if (config.isRespectExtension() && beanElement.getExtension() != null) {
            if (config.isGenerateParentRefs()) {
                jw.writeEol("super(source, parent, justData)");
            } else {
                jw.writeEol("super(source, justData)");
            }
        }
        genExtendBaseBeanConstructor();
        //jw.writeEol("System.out.println(\"Deep copy for "+className+"\")");
        if (config.isGenerateParentRefs()) {
            jw.writeEol("this.parent = parent");
        }
        for (int i = 0, size = attrList.size(); i < size; i++) {
            Property 	a = (Property) attrList.get(i);
            boolean   	indexed = a.isIndexed();
            boolean isScalar = a.isScalar();
            String attr = "_" + a.name;
            String type = a.getType().intern();
            String fullClassType = getTypeFullClassName(a);
            boolean isArrayStyle = (config.getIndexedPropertyType() == null);

            String nextElement = "it.next()";
            if (indexed) {
                if (isArrayStyle) {
                    jw.writeEol(attr, " = new ", fullClassType, "[source."+attr+".length]");
                    jw.beginFor("int index = 0",
                                "index < source."+attr+".length", "++index");
                    nextElement = "source."+attr+"[index]";
                } else {
                    jw.beginFor("java.util.Iterator it = source."+attr+".iterator()",
                                "it.hasNext()", "");
                }
            }
            //System.out.println("type="+type+" needToCallClone="+needToCallClone+" mutable="+mutable);
            String getter;
            if (indexed) {
                getter = "srcElement";
                if (isScalar) {
                    if (config.jdkTarget < 150) {
                        // It's already in object format, it would be silly to
                        // convert back and forth.
                        jw.write("Object");
                    } else {
                        jw.write(JavaUtil.toObjectType(fullClassType));
                    }
                    jw.write(" ", getter, " = ");
                    if (config.jdkTarget >= 150)
                        jw.write("("+JavaUtil.toObjectType(fullClassType)+") ");
                    jw.writeEol(nextElement);
                } else {
                    jw.writeEol(fullClassType+" ", getter," = ",
                                isArrayStyle ? nextElement : JavaUtil.fromObject(fullClassType, nextElement));
                }
            } else
                getter = "source." + attr;

            if (type == "byte[]") {
                jw.writeEol("byte[] destElement"+attr, " = new byte[", getter, ".length]");
                jw.writeEol("System.arraycopy(", getter+", 0, destElement"+attr+", 0, ", getter, ".length)");
            }
            gen(attr);
            if (indexed) {
                if (isArrayStyle) {
                    jw.write("[index] = ");
                } else {
                    gen(".add(");
                }
            } else {
                gen(" = ");
            }
            if (type == "byte[]") {
                jw.write("destElement", attr);
            } else {
                genCopy(getter, a, "justData");
            }
            if (indexed && !isArrayStyle) {
                jw.write(")");
            }
            jw.eol();
            if (!indexed && config.isOptionalScalars() && a.isScalar()) {
                jw.writeEol(a.getScalarIsSet(), " = source.", a.getScalarIsSet());
            }
            if (indexed) {
                jw.end();
            }
        }
        if (beanElement.isRoot && config.isProcessDocType()) {
            String fullDocTypeName;
            if (packageName == null)
                fullDocTypeName = className+".DocType";
            else
                fullDocTypeName = packageName+"."+className+".DocType";
            jw.beginIf("source.docType != null");
            jw.writeEol("docType = new "+fullDocTypeName+"(source.docType)");
            jw.end();
        }
        if (beanElement.isRoot) {
            jw.writeEol("schemaLocation = source.schemaLocation");
        }
        boolean firstJustData = true;
        if (config.isGenerateStoreEvents()) {
            if (firstJustData) {
                firstJustData = false;
                jw.beginIf("!justData");
            }
            geneol("storeEvents = source.storeEvents");
        }
        if (config.isVetoable()) {
            if (firstJustData) {
                firstJustData = false;
                jw.beginIf("!justData");
            }
            geneol("vetos = source.vetos");
        }
        if (config.isGeneratePropertyEvents()) {
            if (firstJustData) {
                firstJustData = false;
                jw.beginIf("!justData");
            }
            jw.beginIf("source.eventListeners != null");
            jw.writeEol("eventListeners = new java.beans.PropertyChangeSupport(this)");
            jw.writeEol("java.beans.PropertyChangeListener[] theListeners = source.eventListeners.getPropertyChangeListeners()");
            jw.beginFor("int i = 0", "i < theListeners.length", "++i");
            jw.writeEol("eventListeners.addPropertyChangeListener(theListeners[i])");
            jw.end();
            jw.end();
        }
        if (!firstJustData)
            jw.end();
        end();
        cr();
    }

    public void genHasChanged() throws IOException {
        if (beanElement.isRoot) {
            select(DECL_SECTION);
            jw.writeEol("private boolean changed");
        }
        select(BODY_SECTION);
        jw.bigComment("Change the result of _hasChanged().\nThis is done automatically when a value in this bean graph gets changed.");
        jw.beginMethod("_setChanged", "boolean changed", null, "void", jw.PUBLIC);
        if (beanElement.isRoot) {
            jw.writeEol("this.changed = changed");
        } else {
            jw.beginIf("parent != null");
            jw.writeEol("parent._setChanged(changed)");
            jw.end();
        }
        jw.endMethod();
        if (beanElement.isRoot) {
            jw.bigComment("@return true if something has changed in this bean graph.");
            jw.beginMethod("_hasChanged", "", null, "boolean", jw.PUBLIC);
            jw.writeEol("return changed");
            jw.endMethod();
        }
    }
    
    public void genChildBeans() throws IOException {
        select(BODY_SECTION);
        jw.bigComment("Return an array of all of the properties that are beans and are set.");
        String cb = config.isExtendBaseBean() ? "org.netbeans.modules.schema2beans.BaseBean" : commonBeanType();
        jw.beginMethod("childBeans", "boolean recursive", null, cb+"[]", jw.PUBLIC);
        jw.write("java.util.List");
        if (config.jdkTarget >= 150)
            jw.write("<", cb, ">");
        jw.write(" children = new java.util.LinkedList");
        if (config.jdkTarget >= 150)
            jw.write("<", cb, ">");
        jw.writeEol("()");
        jw.writeEol("childBeans(recursive, children)");
        jw.writeEol(cb+"[] result = new "+cb+"[children.size()]");
        jw.writeEol("return ("+cb+"[]) children.toArray(result)");
        jw.endMethod();
        jw.bigComment("Put all child beans into the beans list.");
        String childBeansArgs = "boolean recursive, java.util.List";
        if (config.jdkTarget >= 150)
            childBeansArgs += "<"+cb+">";
        childBeansArgs += " beans";
        jw.beginMethod("childBeans", childBeansArgs, null, "void", jw.PUBLIC);
        for (int i = 0, size = attrList.size(); i < size; i++) {
            Property 	a = (Property) attrList.get(i);
            if (!a.isBean)
                continue;
            boolean   	indexed = a.isIndexed();
            String type = a.getType().intern();
            String attr = "_" + a.name;
            if (indexed) {
                beginAttrIterator(attr, a, "element");
                attr = "element";
            }
            jw.beginIf(attr+" != null");
            jw.beginIf("recursive");
            jw.writeEol(attr+".childBeans(true, beans)");
            jw.end();
            jw.writeEol("beans.add("+attr+")");
            jw.end();
            if (indexed) {
                jw.end();
            }
        }
        if (config.isRespectExtension() && beanElement.getExtension() != null) {
            jw.writeEol("super.childBeans(recursive, beans)");
        }
        jw.endMethod();
    }

    public void genName() throws IOException {
        select(BODY_SECTION);
        jw.beginMethod("nameSelf", "", null, "String", jw.PUBLIC);
        if (config.isGenerateParentRefs()) {
            jw.beginIf("parent != null");
            jw.writeEol("String parentName = parent.nameSelf()");
            jw.writeEol("String myName = parent.nameChild(this, false, false)");
            jw.writeEol("return parentName + \"/\" + myName");
            jw.end();
        }
        String beanName = Common.convertName(beanElement.node.getName());
        if (beanElement.isRoot)
            jw.writeEol("return \"/"+beanName+"\"");
        else
            jw.writeEol("return \""+beanName+"\"");
        jw.endMethod();

        jw.beginMethod("nameChild", "Object childObj", null, "String", jw.PUBLIC);
        jw.writeEol("return nameChild(childObj, false, false)");
        jw.endMethod();
        
        jw.bigComment("@param childObj  The child object to search for\n@param returnSchemaName  Whether or not the schema name should be returned or the property name\n@return null if not found");
        jw.beginMethod("nameChild", "Object childObj, boolean returnConstName, boolean returnSchemaName", null, "String", jw.PUBLIC);
        jw.writeEol("return nameChild(childObj, returnConstName, returnSchemaName, false)");
        jw.endMethod();

        jw.bigComment("@param childObj  The child object to search for\n@param returnSchemaName  Whether or not the schema name should be returned or the property name\n@return null if not found");
        jw.beginMethod("nameChild", "Object childObj, boolean returnConstName, boolean returnSchemaName, boolean returnXPathName", null, "String", jw.PUBLIC);
        // Sort by type
        Map typeMap = new HashMap();	// Map<String, List<Property>>
        for (int i = 0, size = attrList.size(); i < size; i++) {
            Property prop = (Property) attrList.get(i);
            String type = JavaUtil.toObjectType(prop.getType());
            if (type.equals("String"))
                type = "java.lang.String";  // normalize
            List lst = (List) typeMap.get(type);
            if (lst == null) {
                lst = new LinkedList();
                typeMap.put(type, lst);
            }
            lst.add(prop);
        }
        for (Iterator types = typeMap.keySet().iterator(); types.hasNext(); ) {
            String type = (String) types.next();
            boolean isString = type != null && type.equals("java.lang.String"); // NOI18N
            jw.beginIf("childObj instanceof "+type);
            jw.writeEol(type, " child = (", type, ") childObj");
            boolean firstUseOfIndex = true;
            for (Iterator props = ((List)typeMap.get(type)).iterator(); props.hasNext(); ) {
                Property prop = (Property) props.next();
                String attr = "_" + prop.name;
                String childExpr = "child";
                if (JavaUtil.isPrimitiveType(prop.getType()))
                    childExpr = JavaUtil.fromObject(prop.getType(), "child");
                if (prop.isIndexed()) {
                    if (firstUseOfIndex) {
                        firstUseOfIndex = false;
                        jw.writeEol("int index = 0");
                    } else {
                        jw.writeEol("index = 0");
                    }
                    beginAttrIterator(attr, prop, "element");
                    if (isString) {
                        jw.beginIf(childExpr+".equals(element)");
                    } else {
                        jw.beginIf(childExpr+" == element");
                    }
                    jw.beginIf("returnConstName");
                    jw.writeEol("return ", prop.constName);
                    if (prop.type != Common.TYPE_COMMENT) {
                        jw.endElseBeginIf("returnSchemaName");
                        jw.writeEol("return \"", prop.dtdName, "\"");
                        jw.endElseBeginIf("returnXPathName");
                        jw.write("return \"");
                        if (prop.isAttribute())
                            jw.write("@");
                        jw.writeEol(prop.dtdName,
                                    "[position()=\"+index+\"]\"");
                    }
                    jw.endElseBegin();
                    jw.writeEol("return \""+prop.getEventName()+".\"+Integer.toHexString(index)");
                    jw.end();
                    jw.end();
                    jw.writeEol("++index");
                    jw.end();
                } else {
                    if (isString) {
                        jw.beginIf(childExpr+".equals("+attr+")");
                    } else {
                        jw.beginIf(childExpr+" == "+attr);
                    }
                    jw.beginIf("returnConstName");
                    jw.writeEol("return ", prop.constName);
                    if (prop.type != Common.TYPE_COMMENT) {
                        jw.endElseBeginIf("returnSchemaName");
                        jw.writeEol("return \"", prop.dtdName, "\"");
                        jw.endElseBeginIf("returnXPathName");
                        jw.write("return \"");
                        if (prop.isAttribute())
                            jw.write("@");
                        jw.writeEol(prop.dtdName, "\"");
                    }
                    jw.endElseBegin();
                    jw.writeEol("return \""+prop.getEventName()+"\"");
                    jw.end();
                    jw.end();
                }
            }
            jw.end();
        }
        if (config.isRespectExtension() && beanElement.getExtension() != null) {
            jw.writeEol("return super.nameChild(childObj, returnConstName, returnSchemaName, returnXPathName)");
        } else {
            jw.writeEol("return null");
        }
        jw.endMethod();
    }

    public void genToString() throws IOException {
        jw.beginMethod("toString", "", null, "String", jw.PUBLIC);
        jw.writeEol("java.io.StringWriter sw = new java.io.StringWriter()");
        jw.beginTry();
        jw.writeEol("writeNode(sw)");
        jw.endCatch("java.io.IOException e");
        jw.comment("How can we actually get an IOException on a StringWriter?");
        jw.writeEol("throw new RuntimeException(e)");
        jw.end();
        jw.writeEol("return sw.toString()");
        jw.endMethod();
    }

    public void genExtendBaseBean() throws IOException {
        int size = attrList.size();
        jw.beginMethod("dump", "StringBuffer str, String indent", null,
                       "void", jw.PUBLIC);
        jw.writeEol("str.append(toString())");
        jw.endMethod();

        jw.pushSelect(jw.DECL_SECTION);
        String[] propByNameKeys = new String[size+1];
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            propByNameKeys[i] = a.name;
        }
        propByNameKeys[size] = "";
        jw.writeEol("private java.util.Map propByName = new java.util.HashMap("+(JavaUtil.getOptimialHashMapSize(propByNameKeys)), ", 1.0f)");
        jw.popSelect();
        jw.beginMethod("beanProp", "String name", null, "org.netbeans.modules.schema2beans.BeanProp", jw.PUBLIC);
        jw.writeEol("if (name == null) return null");
        jw.writeEol("org.netbeans.modules.schema2beans.BeanProp prop = (org.netbeans.modules.schema2beans.BeanProp) propByName.get(name)");
        jw.beginIf("prop == null");
        jw.writeEol("name = name.intern()");
        jw.writeEol("boolean indexed");
        jw.writeEol("int options");
        jw.writeEol("String constName");
        jw.writeEol("String schemaName");
        jw.writeEol("java.lang.reflect.Method writer = null");
        jw.writeEol("java.lang.reflect.Method arrayWriter = null");
        jw.writeEol("java.lang.reflect.Method reader = null");
        jw.writeEol("java.lang.reflect.Method arrayReader = null");
        jw.writeEol("java.lang.reflect.Method adder = null");
        jw.writeEol("java.lang.reflect.Method remover = null");
        boolean defaultKey = true;
        if (metaElement != null) {
            MetaProperty[] mp = metaElement.getMetaProperty();
            for (int i=0; i<mp.length; i++) {
                if (mp[i].isKey()) {
                    //	At least one key is defined - default to false
                    defaultKey = false;
                    break;
                }
            }
        }

        jw.beginTry();
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            boolean   	indexed = a.isIndexed();
            String type = a.getType();
            MetaProperty mp = getMetaProperty(a.name);
            boolean keyedElement = defaultKey;
            boolean genVetoable = config.isVetoable();
            if (mp != null) {
                keyedElement = (mp.isKey())?true:defaultKey;
                genVetoable = (mp.isVetoable())?true:genVetoable;
            }
            MetaElement me = getMetaElement(a);
            if (me != null) {
                genVetoable = (me.isVetoable())?true:genVetoable;
            }
            if (a.isBean)
                genVetoable = false;
            String signatureType = a.getSignatureType(packageName);

            /*
            if (a.getEventName().equals(a.beanIntrospectorName()))
                jw.beginIf("name == \""+a.getEventName()+"\" || name == \""+a.constName+"\"");
            else
                jw.beginIf("name == \""+a.getEventName()+"\" || name == \""+a.beanIntrospectorName()+"\" || name == \""+a.constName+"\"");
            */
            jw.beginIf("name == "+a.constName+"");
            jw.writeEol("indexed = "+indexed);
            jw.writeEol("constName = ", a.constName, "");
            jw.writeEol("schemaName = \"", a.dtdName, "\"");
            jw.write("options = ");
            if (a.ored)
                jw.write("org.netbeans.modules.schema2beans.Common.SEQUENCE_OR | ");
            if (genVetoable)
                jw.write("org.netbeans.modules.schema2beans.Common.TYPE_VETOABLE |");
            if (a.type == Common.TYPE_BOOLEAN && !a.getCanBeEmpty())
                gen("org.netbeans.modules.schema2beans.Common.TYPE_SHOULD_NOT_BE_EMPTY | ");
            if (keyedElement)
                gen("org.netbeans.modules.schema2beans.Common.TYPE_KEY | ");
            jw.writeEol("org.netbeans.modules.schema2beans.Common.",
                        Common.instanceToCommonString(a.elementInstance),
                        "|org.netbeans.modules.schema2beans.Common.",
                        Common.typeToString(a.type));
            if (indexed) {
                jw.writeEol("reader = getClass().getMethod(\"",
                            a.getReadMethod(true), "\", new Class[] {Integer.TYPE})");
                jw.writeEol("arrayReader = getClass().getMethod(\"",
                            a.getReadMethod(false), "\", new Class[] {})");
                jw.writeEol("writer = getClass().getMethod(\"",
                            a.getWriteMethod(), "\", new Class[] {Integer.TYPE, ",
                            signatureType+".class})");
                jw.writeEol("arrayWriter = getClass().getMethod(\"",
                            a.getWriteMethod(), "\", new Class[] {",
                            signatureType+"[].class})");
                jw.writeEol("adder = getClass().getMethod(\"",
                            a.getAddMethod(), "\", new Class[] {",
                            signatureType+".class})");
                jw.writeEol("remover = getClass().getMethod(\"",
                            a.getRemoveMethod(), "\", new Class[] {",
                            signatureType+".class})");
            } else {
                jw.writeEol("writer = getClass().getMethod(\"",
                            a.getWriteMethod(), "\", new Class[] {",
                            signatureType+".class})");
                jw.writeEol("reader = getClass().getMethod(\"",
                            a.getReadMethod(false), "\", new Class[] {})");
            }
            jw.end(false);
            jw.write(" else ");
        }
        jw.begin();
        jw.comment("Check if name is a schema name.");
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            // Only check those names we haven't looked at before.
            if (a.dtdName.equals(a.name))
                continue;
            jw.beginIf("name == \""+a.dtdName+"\"");
            jw.writeEol("prop = beanProp(", a.constName, ")");
            jw.writeEol("propByName.put(name, prop)");
            jw.writeEol("return prop");
            jw.end();
        }
        genInvalidName("name");
        jw.end();
        jw.endCatch("java.lang.NoSuchMethodException e");
        jw.writeEol("throw new RuntimeException(e)");
        jw.end();
        
        jw.write("prop = new org.netbeans.modules.schema2beans.ReflectiveBeanProp(this, schemaName, ");
        jw.write("constName, options, getClass(), ");
        jw.writeEol(""+beanElement.isRoot, ", writer, arrayWriter, reader, arrayReader, adder, remover)");
        jw.writeEol("propByName.put(name, prop)");
        jw.end();	// if
        
        jw.writeEol("return prop");
        jw.endMethod();

        jw.beginMethod("beanProp", "", null, "org.netbeans.modules.schema2beans.BeanProp", jw.PUBLIC);
        jw.beginIf("parent == null");
        jw.writeEol("org.netbeans.modules.schema2beans.BeanProp prop = (org.netbeans.modules.schema2beans.BeanProp) propByName.get(\"\")");
        jw.beginIf("prop == null");
        jw.write("prop = new org.netbeans.modules.schema2beans.ReflectiveBeanProp(this, \"",
                 beanElement.node.getName(), "\", \"", className);
        jw.writeEol("\", org.netbeans.modules.schema2beans.Common.TYPE_1 | org.netbeans.modules.schema2beans.Common.TYPE_BEAN, ",
                    className,
                    ".class, isRoot(), null, null, null, null, null, null)");
        jw.writeEol("propByName.put(\"\", prop)");
        jw.end();	// if
        jw.writeEol("return prop");
        jw.end();
        jw.writeEol("String myConstName = parent.nameChild(this, true, false)");
        jw.writeEol("return parent.beanProp(myConstName)");
        jw.endMethod();

        jw.beginMethod("beanProp", "int order", null, "org.netbeans.modules.schema2beans.BeanProp", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("prepareBeanPropList()");
        jw.writeEol("return (org.netbeans.modules.schema2beans.BeanProp) beanPropList.get(order)");
        jw.endMethod();
        
        jw.beginMethod("parent", "", null, "org.netbeans.modules.schema2beans.BaseBean", jw.PUBLIC);
        jw.writeEol("return (org.netbeans.modules.schema2beans.BaseBean) parent");
        jw.endMethod();

        jw.beginMethod("_getParent", "", null, "org.netbeans.modules.schema2beans.Bean", jw.PUBLIC);
        jw.writeEol("return parent");
        jw.endMethod();

        jw.beginMethod("newInstance", "String name", null, "org.netbeans.modules.schema2beans.BaseBean", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("dtdName", "", null, "String", jw.PUBLIC);
        jw.beginIf("parent == null");
        if (!beanElement.isRoot())
            jw.comment("Not necessarily the right schema name, but make a good guess.");
        jw.writeEol("return \"", beanElement.node.getName(), "\"");
        jw.end();
        jw.writeEol("return parent.nameChild(this, false, true)");
        jw.endMethod();

        jw.beginMethod("comments", "", null, "org.w3c.dom.Comment[]", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("addComment", "String comment", null, "org.w3c.dom.Comment", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("removeComment", "org.w3c.dom.Comment comment", null, "void", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("createProperty",
                       "String dtdName, String beanName, Class type", null, "void", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("createProperty",
                       "String dtdName, String beanName, int option, Class type", null, "void", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("createRoot",
                       "String dtdName, String beanName, int option, Class type", null, "void", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("knownValues",
                       "String name", null, "Object[]", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("addKnownValue",
                       "String name, Object value", null, "void", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("createAttribute",
                       "String dtdName, String name, int type, String[] values, String defValue", null, "void", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("createAttribute",
                       "String propName, String dtdName, String name, int type, String[] values, String defValue", null, "void", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("setAttributeValue",
                       "String propName, String name, String value", null, "void", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("setAttributeValue",
                       "String name, String value", null, "void", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("getAttributeValue",
                       "String name", null, "String", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("getAttributeValue",
                       "String propName, String name", null, "String", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("setAttributeValue",
                       "String propName, int index, String name, String value", null, "void", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("getAttributeValue",
                       "String propName, int index, String name", null, "String", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("getAttributeNames",
                       "String propName", null, "String[]", jw.PUBLIC);
        jw.writeEol("return new String[] {}");
        jw.endMethod();

        jw.beginMethod("getAttributeNames",
                       "", null, "String[]", jw.PUBLIC);
        jw.writeEol("return new String[] {}");
        jw.endMethod();

        jw.beginMethod("listAttributes",
                       "String propName", null, "org.netbeans.modules.schema2beans.BaseAttribute[]", jw.PUBLIC);
        jw.writeEol("return new org.netbeans.modules.schema2beans.BaseAttribute[] {}");
        jw.endMethod();

        jw.beginMethod("listAttributes",
                       "", null, "org.netbeans.modules.schema2beans.BaseAttribute[]", jw.PUBLIC);
        jw.writeEol("return new org.netbeans.modules.schema2beans.BaseAttribute[] {}");
        jw.endMethod();

        jw.beginMethod("findAttributeValue",
                       "String attrName, String value", null, "String[]", jw.PUBLIC);
        jw.writeEol("return new String[] {}");
        jw.endMethod();

        jw.beginMethod("findPropertyValue",
                       "String propName, Object value", null, "String[]", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("findValue",
                       "Object value", null, "String[]", jw.PUBLIC|jw.UNSUPPORTED);
        jw.writeEol("throw new UnsupportedOperationException(\"Not implemented\")");
        jw.endMethod();

        jw.beginMethod("buildPathName", "StringBuffer str", null, "void", jw.PROTECTED);
        jw.writeEol("str.append(nameSelf())");
        jw.endMethod();

        jw.beginMethod("graphManager", "", null, "org.netbeans.modules.schema2beans.GraphManager", jw.PUBLIC);
        jw.beginIf("graphManager == null");
        jw.beginIf("parent == null");
        //jw.writeEol("System.out.println(\"Creating GraphManager for \"+getClass()+\" this=\"+this)");
        //jw.comment("new Exception().printStackTrace()");
        jw.writeEol("graphManager = new org.netbeans.modules.schema2beans.GraphManager(this)");
        jw.endElseBegin();
        jw.writeEol("graphManager = parent.graphManager()");
        jw.end();
        jw.end();
        jw.writeEol("return graphManager");
        jw.endMethod();

        jw.beginMethod("clone", "", null, "Object", jw.PUBLIC | jw.BEANINFO);
        jw.writeEol("return new ", className, "(this, null, false)");
        jw.endMethod();

        jw.beginMethod("cloneData", "", null, "Object", jw.PUBLIC | jw.BEANINFO);
        jw.writeEol("return new ", className, "(this, null, true)");
        jw.endMethod();

        jw.pushSelect(DECL_SECTION);
        jw.write("private java.util.List beanPropList = null");
        jw.eol(false);
        jw.write("\t");
        jw.comment("List<org.netbeans.modules.schema2beans.BeanProp>");
        jw.popSelect();
        jw.beginMethod("prepareBeanPropList", "", null, "void", jw.PRIVATE);
        jw.beginIf("beanPropList == null");
        jw.writeEol("beanPropList = new java.util.ArrayList("+size+")");
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            jw.writeEol("beanPropList.add(beanProp(", a.constName, "))");
        }
        jw.end();
        jw.endMethod();

        jw.beginMethod("beanPropsIterator", "", null, "java.util.Iterator",
                       jw.PROTECTED);
        jw.writeEol("prepareBeanPropList()");
        jw.writeEol("return beanPropList.iterator()");
        jw.endMethod();

        jw.beginMethod("beanProps", "", null, "org.netbeans.modules.schema2beans.BeanProp[]", jw.PUBLIC);
        jw.writeEol("prepareBeanPropList()");
        jw.writeEol("org.netbeans.modules.schema2beans.BeanProp[] ret = new org.netbeans.modules.schema2beans.BeanProp["+size+"]");
        jw.writeEol("ret = (org.netbeans.modules.schema2beans.BeanProp[]) beanPropList.toArray(ret)");
        jw.writeEol("return ret");
        jw.endMethod();

        jw.beginMethod("setValue", "String name, Object value", null, "void", jw.PUBLIC);
        new NamePropertyVisitor("name") {
                public void generateProp() throws IOException {
                    if (curProp.isIndexed()) {
                        jw.write(curProp.getWriteMethod(), "(");
                        jw.writeEol("(", curProp.getType(), "[]) value)");
                    } else {
                        jw.write(curProp.getWriteMethod(), "(");
                        jw.write(JavaUtil.fromObject(curProp.getType(), "value"));
                        jw.writeEol(")");
                    }
                }
            }.generate();
        jw.endMethod();

        jw.beginMethod("setValue", "String name, int index, Object value", null, "void", jw.PUBLIC);
        new NamePropertyVisitor("name") {
                public void generateProp() throws IOException {
                    if (curProp.isIndexed()) {
                        jw.write(curProp.getWriteMethod(), "(index, ");
                        jw.write(JavaUtil.fromObject(curProp.getType(), "value"));
                        jw.writeEol(")");
                    } else {
                        jw.writeEol("throw new IllegalArgumentException(name+\" is not an indexed property for ",
                                    className, "\")");
                    }
                }
            }.generate();
        jw.endMethod();

        jw.beginMethod("getValue", "String name", null, "Object", jw.PUBLIC);
        new NamePropertyVisitor("name") {
                public void generateProp() throws IOException {
                    jw.write("return ");
                    if (curProp.isIndexed())
                        jw.writeEol(curProp.getReadMethod(false)+"()");
                    else
                        jw.writeEol(JavaUtil.toObject(curProp.getReadMethod(false)+"()",
                                                      curProp.getType(),
                                                      config.isForME(),
                                                      config.isJava5()));
                }
            }.generate();
        jw.endMethod();

        jw.beginMethod("getValue", "String name, int index", null, "Object", jw.PUBLIC);
        new NamePropertyVisitor("name") {
                public boolean skip() {
                    return !curProp.isIndexed();
                }
                
                public void generateProp() throws IOException {
                    jw.write("return ");
                    jw.writeEol(JavaUtil.toObject(curProp.getReadMethod(true)+"(index)",
                                                  curProp.getType(),
                                                  config.isForME(),
                                                  config.isJava5()));
                }

                public void postGenerate() throws IOException {}
            }.generate();
        new NamePropertyVisitor("name") {
                public void preGenerate() throws IOException {}
                
                public boolean skip() {
                    return curProp.isIndexed();
                }
                
                public void generateProp() throws IOException {
                    jw.beginIf("index > 0");
                    jw.writeEol("throw new IllegalArgumentException(\"index > 0\")");
                    jw.end();
                    jw.write("return ");
                    jw.writeEol(JavaUtil.toObject(curProp.getReadMethod(false)+"()",
                                                  curProp.getType(),
                                                  config.isForME(),
                                                  config.isJava5()));
                }
            }.generate();
        jw.endMethod();

        jw.beginMethod("mergeUpdate",
                       "org.netbeans.modules.schema2beans.BaseBean sourceBean",
                       null, "void", jw.PUBLIC);
        //jw.writeEol("System.out.println(\"mergeUpdate for \"+getClass())");
        jw.writeEol(fullClassName, " source = (", fullClassName, ") sourceBean");
        boolean isArrayStyle = (config.getIndexedPropertyType() == null);
        for (int i = 0; i < size; i++) {
            Property 	a = (Property) attrList.get(i);
            boolean   	indexed = a.isIndexed();
            String type = a.getType().intern();
            boolean willCopy = genCopyWillCopy(a);
            String getter = "source."+a.getReadMethod(false)+"()";
            String signatureType = a.getSignatureType(packageName);
            String baseFullClassName = a.getTypeFullClassName(packageName);
            String fullClassName;
            if (indexed) {
                fullClassName = baseFullClassName + "[]";
            } else
                fullClassName = baseFullClassName;
            jw.begin();
            jw.write(fullClassName, " srcProperty = ");
            if (!signatureType.equals(baseFullClassName))
                jw.write("(", fullClassName, ") ");
            jw.writeEol(getter);
            String srcProperty = "srcProperty";
            if (a.isBean) {
                if (indexed) {
                    jw.writeEol("int destSize = ", a.getSizeMethod(), "()");
                    jw.beginIf("destSize == srcProperty.length");
                    srcProperty = "srcProperty[i]";
                    jw.beginFor("int i = 0", "i < srcProperty.length", "++i");
                    genMergeUpdateBean(a, srcProperty, indexed, signatureType,
                                       baseFullClassName, isArrayStyle, true);
                    jw.endElseBegin();
                    jw.writeEol(fullClassName, " destArray = new ",
                                baseFullClassName,
                                "[srcProperty.length]");
                    jw.beginFor("int i = 0", "i < srcProperty.length", "++i");
                    genMergeUpdateBean(a, srcProperty, indexed, signatureType,
                                       baseFullClassName, isArrayStyle, false);
                    jw.end();
                    jw.writeEol(a.getWriteMethod(), "(destArray)");
                    jw.end();
                } else {
                    genMergeUpdateBean(a, srcProperty, indexed, signatureType,
                                       baseFullClassName, isArrayStyle, false);
                }

            } else if (indexed && (willCopy || isArrayStyle)) {
                jw.writeEol(fullClassName, " destArray = new ",
                            baseFullClassName,
                            "[srcProperty.length]");
                jw.beginFor("int i = 0", "i < srcProperty.length", "++i");
                srcProperty = "srcProperty[i]";
                jw.write("destArray[i] = ");
                genCopy(srcProperty, a, "false");
                jw.eol();
                jw.end();
                jw.writeEol(a.getWriteMethod(), "(destArray)");
            } else {
                jw.write(a.getWriteMethod(), "(");
                genCopy(srcProperty, a, "false");
                jw.writeEol(")");
            }
            jw.end();
        }
        jw.endMethod();

        if (beanElement.isRoot()) {
            jw.beginMethod("isRoot", "", null, "boolean", jw.PUBLIC);
            jw.writeEol("return true");
            jw.endMethod();

            jw.beginMethod("createGraph",
                           "java.io.InputStream in, boolean validate",
                           "java.io.IOException, javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException",
                           className, jw.PUBLIC | jw.STATIC | jw.IO);
            //jw.beginTry();
            jw.writeEol("return read(new org.xml.sax.InputSource(in), validate, null, null)");
            /*
            jw.endCatch("javax.xml.parsers.ParserConfigurationException e");
            jw.writeEol("throw new java.lang.RuntimeException(e)");
            jw.endCatch("org.xml.sax.SAXException e");
            jw.writeEol("throw new java.lang.RuntimeException(e)");
            jw.endCatch("java.io.IOException e");
            jw.writeEol("throw new java.lang.RuntimeException(e)");
            jw.end();
            */
            jw.endMethod();

            jw.beginMethod("createGraph", "java.io.InputStream in",
                           "java.io.IOException, javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException",
                           className, jw.PUBLIC | jw.STATIC | jw.IO);
            jw.writeEol("return createGraph(in, false)");
            jw.endMethod();

            jw.beginMethod("createGraph", "", null, className, jw.PUBLIC | jw.STATIC | jw.IO);
            jw.writeEol("return new ", className, "()");
            jw.endMethod();

            jw.beginMethod("createGraph", "org.w3c.dom.Document document", null, className, jw.PUBLIC | jw.STATIC);
            jw.writeEol("return read(document)");
            jw.endMethod();

            jw.pushSelect(CONSTRUCTOR_SECTION);
            jw.beginConstructor(className, "org.w3c.dom.Node doc, int currentlyUnusedOptions");
            jw.writeEol("this()");
            jw.writeEol("readFromDocument((org.w3c.dom.Document) doc)");
            jw.end();
            jw.cr();
            jw.popSelect();
        } else {
            jw.bigComment("@deprecated");
            jw.beginMethod("write", "java.io.Writer out, String encoding",
                           "java.io.IOException", "void", jw.PUBLIC | jw.IO);
            jw.writeEol("writeNode(out)");
            jw.endMethod();

            jw.bigComment("@deprecated");
            jw.beginMethod("write", "java.io.OutputStream out",
                           "java.io.IOException", "void", jw.PUBLIC | jw.IO);
            if (config.isJava5()) {
                jw.writeEol("java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(out, java.nio.charset.Charset.defaultCharset()))");
            } else {
                jw.writeEol("java.io.PrintWriter pw = new java.io.PrintWriter(out)");
            }
            jw.writeEol("writeNode(pw)");
            jw.writeEol("pw.flush()");
            jw.endMethod();
        }
    }

    private void genMergeUpdateBean(Property a, String srcProperty,
                                    boolean indexed, String signatureType,
                                    String baseFullClassName,
                                    boolean isArrayStyle,
                                    boolean mergeEach) throws IOException {
        //jw.writeEol("System.out.println(\"mergeEach="+mergeEach+"\")");
        String attr = "_" + a.name;
        jw.writeEol(baseFullClassName, " dest");
        if (!indexed)
            jw.writeEol("boolean needToSet = false");
        jw.beginIf(srcProperty+" == null");
        jw.writeEol("dest = null");
        if (!indexed)
            jw.writeEol("needToSet = true");
        jw.endElseBegin();
        if (indexed) {
            jw.beginIf("i < destSize");
        }
        jw.write("dest = ");
        if (!signatureType.equals(baseFullClassName))
            jw.write("(", baseFullClassName, ") ");
        jw.write(a.getReadMethod(indexed), "(");
        if (indexed)
            jw.write("i");
        jw.writeEol(")");
        if (indexed) {
            if (!mergeEach) {
                jw.beginIf("!"+srcProperty+".equals(dest)");
                jw.comment("It's different, so have it just dup the source one.");
                jw.writeEol("dest = null");
                jw.end();
            }
            jw.endElseBegin();
            jw.writeEol("dest = null");
            jw.end();
        }
        //jw.writeEol("System.out.println(\"dest=\"+dest)");
        jw.beginIf("dest == null");
        jw.comment("Use a temp variable, and store it after we've merged everything into it, so as to make it only 1 change event.");
        jw.writeEol("dest = new ", baseFullClassName, "(", srcProperty+", this, false)");
        if (!indexed) {
            jw.writeEol("needToSet = true");
            jw.endElseBegin();
            jw.writeEol("dest.mergeUpdate(", srcProperty, ")");
            jw.end();
        } else {
            if (mergeEach) {
                jw.endElseBegin();
                jw.writeEol("dest.mergeUpdate(", srcProperty, ")");
                jw.end();
            }
        }
        jw.end();
        if (indexed) {
            if (mergeEach) {
                jw.comment("Merge events were generated by the above dest.mergeUpdate, so just set it directly now.");
                if (isArrayStyle) {
                    jw.writeEol(attr, "[index] = value");
                } else {
                    jw.writeEol(attr, ".set(i, dest)");
                }
            } else {
                jw.writeEol("destArray[i] = dest");
            }
            jw.end();
        } else {
            jw.beginIf("needToSet");
            jw.write(a.getWriteMethod(), "(");
            jw.writeEol("dest)");
            jw.end();
        }
    }

    public void genTrailer(int out) {
        select(out);
        if (metaElement != null && metaElement.getUserCode() != null) {
            String userCode = metaElement.getUserCode();
            cr(); gencr(userCode);
        }
    }

    public void genFinishClass(int out) {
        select(out);
        end();
        cr();
    }

    public void setInvalidPropertyNames(Map invalidNames) {
        invalidNames.put("Class", null);
        if (config.isExtendBaseBean()) {
            invalidNames.put("Property", null);
            invalidNames.put("AttributeNames", null);
        }
    }

    protected void beginAttrIterator(String attr, Property a,
                                     String elementName) throws IOException {
        String fullType = getTypeFullClassName(a);
        boolean isArrayStyle = (config.getIndexedPropertyType() == null);
        if (isArrayStyle) {
            jw.beginFor("int elementIndex = 0", "elementIndex < "+attr+".length",
                        "++elementIndex");
        } else {
            jw.beginFor("java.util.Iterator it = "+attr+".iterator()",
                        "it.hasNext()", "");
        }
        jw.write(fullType);
        jw.write(" ", elementName, " = ");
        if (isArrayStyle) {
            jw.writeEol(attr, "[elementIndex]");
        } else {
            jw.writeEol(JavaUtil.fromObject(fullType, "it.next()"));
        }
    }

    protected void genNewEvent(Property a, String index,
                               String oldValue, String newValue,
                               String type) throws IOException {
        jw.write("new java.beans.PropertyChangeEvent(this, nameSelf()+\"/");
        jw.write(a.getEventName());
        if (index.equals(""))
            jw.write("\"");
        else if (index.equals("-1"))
            jw.write(".", index, "\"");
        else
            jw.write(".\"+Integer.toHexString(", index, ")");
        jw.write(", ");
        if ("null".equals(oldValue))
            jw.write("null");
        else
            jw.write(JavaUtil.toObject(oldValue, type, config.isForME(), config.isJava5()));
        jw.write(", ");
        if ("null".equals(newValue))
            jw.write("null");
        else
            jw.write(JavaUtil.toObject(newValue, type, config.isForME(), config.isJava5()));
        jw.write(")");
    }

    protected boolean hasDeepCopyConstructor() {
        return true;
    }
}
