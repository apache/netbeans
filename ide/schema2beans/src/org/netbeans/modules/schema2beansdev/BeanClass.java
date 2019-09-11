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

package org.netbeans.modules.schema2beansdev;

import java.util.*;
import java.io.*;

import org.netbeans.modules.schema2beans.*;
import org.netbeans.modules.schema2beansdev.metadd.*;
import org.netbeans.modules.schema2beansdev.gen.*;

//******************************************************************************
// BEGIN_NOI18N
//******************************************************************************

/**
 *	This class generates the bean classes code. There is one instance of
 *	this class per bean to generate.
 *
 *	The BeanBuilder creates one instance of the BeanClass class for every
 *	bean class to generate, and for each of these instances,
 *	the BeanBuilder calls the addPropertyMethod() in order to populate the
 *	list of properties contained by the bean. Then it calls the process()
 *	method, which makes the class to generate its java output into
 *	the output stream specified in the process parameter.
 *
 *	The BeanBuilder might give an optional MetaDD bean graph which contains
 *	extra information that didn't exist in the dtd. The content of this graph
 *	is used to generate extra information about the properties of the bean
 *	(for example the default values, well known values, wrapper class)
 */
public class BeanClass extends AbstractCodeGeneratorClass implements CodeGeneratorClass {
    private boolean		isVetoable;
    private boolean		genVetoListeners;
    private boolean		genGenericVetoListeners;
    
    BeanClass(BeanBuilder.BeanElement be, GenBeans.Config config) {
        init(be, config);
        addExtraMethods();
    }
    
    void setBeanName(String n) {
        this.className = n;
    }
    
    private void genVetoBegin() {
        gencr("try"); begin();
    }
    
    private void genVetoEnd() {
        end();
        gencr("catch(BaseProperty.VetoException ve)");
        begin();
        gen("throw ve.getPropertyVetoException()"); eol();
        end();
    }
    
    
    static final String VCL_FULL_CLASS_NAME = "java.beans.VetoableChangeListener";
    static final String VCL = "VetoableChangeListener";
    static final String PCL_FULL_CLASS_NAME = "java.beans.PropertyChangeListener";
    static final String PCL = "PropertyChangeListener";
    
    /**
     *	Generate the different parts of the class
     */
    void genHeader(int out) throws IOException {
	select(out);
	gencr("/**");
	gencr(" *	This generated bean class " + this.className +
	" matches the schema element '" + this.beanElement.node.getName()+"'.");
    if (!beanElement.isRoot) {
        jw.writecr(" *  The root bean class is "+rootBeanElement.getClassType());
    }
	gencr(" *");
    printComment(" *	");
    if (config.isGenerateTimeStamp())
        gencr(" *	Generated on " + new Date());
	if (this.beanElement.isRoot) {
	    gencr(" *");
	    gencr(" *	This class matches the root element of the "+(config.getSchemaTypeNum() == GenBeans.Config.DTD ? "DTD" : "XML Schema")+",");
	    gencr(" *	and is the root of the following bean graph:");
	    gencr(" *");

        dumpBeanTree(jw, " *"+jw.getIndent(), jw.getIndent());
	    gencr(" *");
	}
        gencr(" * @"+Common.GENERATED_TAG);
        gencr(" */"); cr();
    }
    
    void genPackage(int out) {
        select(out);
        if (this.packageName != null) {
            gen(PACKAGE, this.packageName);
            eol();
            cr();
        }
    }
    
    void genImports(int out) {
	select(out);
	gen(IMPORT, "org.w3c.dom.*"); eol();
	gen(IMPORT, "org.netbeans.modules.schema2beans.*"); eol();
	gen(IMPORT, "java.beans.*"); eol();
	gen(IMPORT, "java.util.*"); eol();
	//if (!this.config.isStandalone()) {
	//   gen(IMPORT, "org.openide.util.NbBundle"); eol();
	//}
	if (this.beanElement.isRoot) {
	    gen(IMPORT, "java.io.*"); eol();
	}
	
	String[] imps = null;
	if (this.mdd != null) {
	    if (this.metaElement != null)
		imps = this.metaElement.getImport();
	    
	    if (imps == null || imps.length==0)
		imps = this.mdd.getImport();
	}
	if (imps != null) {
	    for (int i=0; i<imps.length; i++) {
		String imp = imps[i];
		imp = imp.trim();
		if (imp.startsWith("import"))
		    gen(imp);
		else
		    gen(IMPORT, " ", imp);
		if (!imp.endsWith(";"))
		    eol();
		else
		    cr();
	    }
	}
	
    }
    
    void genClassName(int out) {
	String name = null;
	String impName = null;
	
	select(out);
	gen(PUBLIC, CLASS, this.className);
	
	if (this.mdd != null) {
	    if (this.metaElement != null) {
		name = this.metaElement.getExtends();
		impName = this.metaElement.getImplements();
	    }
	    if (name == null) {
		name = this.mdd.getExtends();
	    }
	    if (impName == null) {
		impName = this.mdd.getImplements();
	    }
	}
	
	gen(" extends ");
	if (name != null)
	    gencr(name);
	else
	    gencr("org.netbeans.modules.schema2beans.BaseBean");
	
	if (impName != null) {
	    gentab(1); gencr(" implements ", impName);
	}
	
	begin();
    }
    
    
    void genConstructor(int out) throws IOException {
        select(out);
        String thrownExceptions = null;
        if (this.beanElement.isRoot && shouldThrowException()) {
            thrownExceptions = "org.netbeans.modules.schema2beans.Schema2BeansException";
        }
        jw.beginConstructor(className, "", thrownExceptions, jw.PUBLIC);
	gen("this(");
	if (this.beanElement.isRoot)
	    gen("null, ");
	gen("Common.USE_DEFAULT_VALUES)");
	eol();
	end(); cr();
	
	if (this.beanElement.isRoot) {
        jw.beginConstructor(className, "org.w3c.dom.Node doc, int options",
                            thrownExceptions, jw.PUBLIC);
        //
        // This call should never pass anything but NO_DEFAULT_VALUES, since
        // initFromNode will get the options that are specified.
        //
        jw.writeEol("this(Common.NO_DEFAULT_VALUES)");
        if (!shouldThrowException()) {
            gen("try ");
            begin();
        }
	    gen("initFromNode(doc, options)"); eol();
        if (!shouldThrowException()) {
            end();
            gen("catch (Schema2BeansException e) ");
            begin();
            gen("throw new RuntimeException(e)");
            eol();
            end();
        }
	    end();
	    // Make it so that initFromNode can be called from other
	    // methods in this class (like ones used for deserializing
	    // from a DOM tree).
	    gen(PROTECTED, VOID, "initFromNode(org.w3c.dom.Node doc, int options) throws Schema2BeansException"); cr();
	    begin();
	    gencr("if (doc == null)");
	    begin();
	    gen("doc = GraphManager.createRootElementNode(\"",
	    this.beanElement.node.getName(), "\")");
        eolNoI18N();
	    gencr("if (doc == null)");
	    
	    if (this.config.isStandalone()) {
            tabIn();
            gencrNoI18N("throw new Schema2BeansException(\"Cannot create DOM root\");");
	    } else {
            tabIn();
            gencr("throw new Schema2BeansException(Common.getMessage(");
            tabIn(); tabIn();
            gencr("\"CantCreateDOMRoot_msg\", \""+beanElement.node.getName()+"\"));");
	    }
	    end();
	    
	    gen("Node n = GraphManager.getElementNode(\"");
	    gen(this.beanElement.node.getName(), "\", doc)");
	    eolNoI18N();
	    gencr("if (n == null)");
	    if (this.config.isStandalone()) {
            tabIn();
            gen("throw new Schema2BeansException(\"Doc root not in the DOM graph\")");
            eolNoI18N();
	    } else {
            tabIn();
            gencr("throw new Schema2BeansException(Common.getMessage(");
            tabIn(); tabIn();
            gen("\"DocRootNotInDOMGraph_msg\", \""+beanElement.node.getName()+"\", doc.getFirstChild().getNodeName()))");
	    }
        eol();
        
	    cr();
	    gen("this.graphManager.setXmlDocument(doc)"); eol(); cr();
	    comment("Entry point of the createBeans() recursive calls");
	    gen("this.createBean(n, this.graphManager())"); eol();
	    gen("this.initialize(options)"); eol();
	    end();
	}
	
        if (this.beanElement.isExtended()) {
            gen(PROTECTED, this.className+"(Vector comparators, Version runtimeVersion)");
            begin();
            cr();
            jw.writeEol("super(comparators, runtimeVersion)");
            end();
        }
	gen(PUBLIC, this.className+"(int options)");
    /*
    if (this.beanElement.isRoot && shouldThrowException()) {
        gen(" throws Schema2BeansException ");
    }
    */
    cr();
	begin();
	//gen("super(", this.className, ".comparators");
	jw.writeEol("super(comparators, runtimeVersion)");
	if (this.beanElement.isRoot) {
	    gen("initOptions(options)"); eol();
	    end();
	    
	    gen(PROTECTED, VOID, "initOptions(int options)"); cr();
	    begin();
	    comment("The graph manager is allocated in the bean root");
	    gen("this.graphManager = new GraphManager(this)"); eol();
	    gen("this.createRoot(\"", this.beanElement.node.getName(), "\", \"");
	    gen(this.className, "\",");
        noI18N();  tabIn();
	    gen("Common.TYPE_1 | Common.TYPE_BEAN, ");
	    gen(this.className, ".class)"); eol(); cr();
	}
    }

    /**
     * This method needs to be run after genAccessors is run, so that
     * all of the a.isIndexed values are correct (for attributes).
     */
    void genInitializer() throws IOException {
        select(INITIALIZE_SECTION);
        comment("Setting the default values of the properties");
        jw.beginMethod("initialize", "int options", null, "void", jw.PACKAGE_LEVEL);
        if (beanElement.isRoot) {
            if (getDefaultNamespace() != null) {
                jw.write("setDefaultNamespace(");
                jw.write(JavaUtil.instanceFrom("java.lang.String",
                                               getDefaultNamespace()));
                jw.writeEol(")");
            }
            if (mdd.getSchemaLocation() != null) {
                jw.beginIf("(options & Common.USE_DEFAULT_VALUES) == Common.USE_DEFAULT_VALUES");
                jw.write("_setSchemaLocation(");
                jw.write(JavaUtil.instanceFrom("java.lang.String",
                                               mdd.getSchemaLocation()));
                jw.writeEol(")");
                jw.end();
            }
        }

        int size = attrList.size();
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            boolean indexed = a.isIndexed();
            String type = a.getType();
            String	constName = a.constName;
            //	Generate the initializer (default & well known property values)
            String values[] = null;
            String wrapperClass = null;
	    
            MetaElement me = getMetaElement(a);
            MetaProperty mp = getMetaProperty(a);
           //	no default value for a bean element (only for final property)
            if (me != null && !a.isBean) {
                select(INITIALIZE_SECTION);
                //
                //  Generate the default values
                //
                int elts = 0;
		
                //  Get the current property default values first
                if (mp != null)
                    values = mp.getDefaultValue();
		
                if (values != null)
                    elts = values.length;
		
                //  If no default value, try to get the generic ones
                if (elts == 0)
                    values = me.getDefaultValue();
		
                if (values != null ) {
                    if (values.length > 0)
                        gencr("if ((options & Common.USE_DEFAULT_VALUES) ",
                              "== Common.USE_DEFAULT_VALUES)");
                    if (values.length > 1)
                        begin();
                    for (int j=0; j<values.length; j++) {
                        if (indexed) {
                            gen("this.addValue(", constName, ", ");
                            gen(JavaUtil.instanceFrom(JavaUtil.toObjectType(type), values[j]));
                            gen(")");
                            eol();
                        }
                        else {
                            gen("this.setValue(", constName, ", ");
                            gen(JavaUtil.instanceFrom(JavaUtil.toObjectType(type), values[j]));
                            gen(")");
                            eol();
                            break;
                        }
                    }
                    if (values.length > 1)
                        end();
                }
		
                //
                //  Generate the well-known values
                //
                elts = 0;
                values = null;
		
                //  Get the current property default values first
                if (mp != null)
                    values = mp.getKnownValue();
		
                if (values != null)
                    elts = values.length;
		
                //  If no default value, try to get the generic ones
                if (elts == 0)
                    values = me.getKnownValue();

                if (values != null) {
                    for (int j = 0; j < values.length; j++) {
                        jw.writeEol("addKnownValue("+constName+", ", JavaUtil.instanceFrom(JavaUtil.toObjectType(type), values[j]), ")");
                    }
                }
            }
        }

        boolean first = true;
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            boolean indexed = a.isIndexed();
            String type = a.getType();
            if (!indexed && a.getDefaultValue() != null && (a.elementInstance == Common.TYPE_1 || a.elementInstance == Common.TYPE_1_N)) {
                if (!JavaUtil.checkValueToType(type, a.getDefaultValue())) {
                    config.messageOut.println(Common.getMessage("MSG_NotAGoodValue", a.getDefaultValue(), type));
                }
                if (first) {
                    first = false;
                    jw.beginIf("(options & Common.USE_DEFAULT_VALUES) == Common.USE_DEFAULT_VALUES");
                }
                jw.write(a.getWriteMethod(), "(");
                jw.write(JavaUtil.instanceFrom(type, a.getDefaultValue()));
                jw.writeEol(")");
            }
        }
        if (!first)
            jw.end();
    }
    
    
    void genDeclarations(int out) {
        select(out);
        cr();
        gen("static Vector comparators = new Vector()"); eol();
        if (this.metaElement != null) {
            int size = this.metaElement.sizeComparatorClass();
	    
            if (size >0) {
                gen(STATIC);
                begin();
                for (int i=0; i<size; i++) {
                    gen(this.className, ".addComparator(new ");
                    gen(this.metaElement.getComparatorClass(i), "())");
                    eol();
                }
                end();
            }
        }
        gen("private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(" + Version.MAJVER);
        gen(", " + Version.MINVER);
        gen(", " + Version.PTCVER, ")");
        eol();
        if (this.beanElement.isRoot) {
            gen("private static final String SERIALIZATION_HELPER_CHARSET = \"UTF-8\"");
        }
        eolNoI18N();
    }
    
    /**
     */
    void genAccessors(int out) throws IOException {
	int 	size = this.attrList.size();
	boolean	defaultKey = true;
	ArrayList attrNames = new ArrayList();
	
	select(TRAILER_SECTION);
	if (this.beanElement.isRoot) {
	    //
	    // Put in some writeObject/readObject serializer/deserializer
	    // methods, so that this object can implement java.io.Serializable
	    // correctly.
	    //
	    comment("Special serializer: output XML as serialization");
	    gen(PRIVATE, VOID, "writeObject(java.io.ObjectOutputStream out) throws java.io.IOException");
	    begin();
	    
            gen("out.defaultWriteObject()"); eol();
            gen("final int MAX_SIZE = 0XFFFF"); eol();
            gen("final ByteArrayOutputStream baos = new ByteArrayOutputStream()"); eol();
            gen("try");
            begin();
	    gen("write(baos, SERIALIZATION_HELPER_CHARSET)"); eol();
            gen("final byte [] array = baos.toByteArray()"); eol();
            gen("final int numStrings = array.length / MAX_SIZE"); eol();
            gen("final int leftover = array.length % MAX_SIZE"); eol();
            gen("out.writeInt(numStrings + (0 == leftover ? 0 : 1))"); eol();
            gen("out.writeInt(MAX_SIZE)"); eol();
            gen("int offset = 0"); eol();
            gen("for (int i = 0; i < numStrings; i++)"); 
            begin();
            gen("out.writeUTF(new String(array, offset, MAX_SIZE, SERIALIZATION_HELPER_CHARSET))"); eol();
            gen("offset += MAX_SIZE"); eol();
            end();
            gen("if (leftover > 0)"); 
            begin();
            gen("final int count = array.length - offset"); eol();
            gen("out.writeUTF(new String(array, offset, count, SERIALIZATION_HELPER_CHARSET))"); eol();
            end();
            end();
            gen("catch (Schema2BeansException ex)");
            begin();
            gen("throw new Schema2BeansRuntimeException(ex)"); eol();
            end();

	    end();
	    
	    comment("Special deserializer: read XML as deserialization");
	    gen(PRIVATE, VOID, "readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException");
	    begin();
            gen("try");
            begin();
            
            gen("in.defaultReadObject()"); eol();
            gen("init(comparators, runtimeVersion)"); eol();
            comment("init(comparators, new GenBeans.Version(1, 0, 8))");
            gen("final int numStrings = in.readInt()");eol();
            gen("final int max_size = in.readInt()");eol();
            if (config.isJava5()) {
                gen("final StringBuilder sb = new StringBuilder(numStrings * max_size)"); eol();
            } else {
                gen("final StringBuffer sb = new StringBuffer(numStrings * max_size)"); eol();
            }
            gen("for (int i = 0; i < numStrings; i++)"); begin();
            gen("sb.append(in.readUTF())"); eol();
            end();
            gen("ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes(SERIALIZATION_HELPER_CHARSET))"); eol();
            gen("Document doc = GraphManager.createXmlDocument(bais, false)"); eol();
            if (config.isSetDefaults()){
                gen("initOptions(Common.USE_DEFAULT_VALUES)"); eol();
                gen("initFromNode(doc, Common.USE_DEFAULT_VALUES)"); eol();
            } else {
                gen("initOptions(Common.NO_DEFAULT_VALUES)"); eol();
                gen("initFromNode(doc, Common.NO_DEFAULT_VALUES)"); eol();
            }
            end();
            gen("catch (Schema2BeansException e)");
            begin();
            gen("throw new RuntimeException(e)"); eol();
            end(); 
            end();
            cr();

        jw.beginMethod("_setSchemaLocation", "String location", null, "void", jw.PUBLIC);
        jw.beginIf("beanProp().getAttrProp(\"xsi:schemaLocation\", true) == null");
        jw.writeEol("createAttribute(\"xmlns:xsi\", \"xmlns:xsi\", AttrProp.CDATA | AttrProp.IMPLIED, null, \"http://www.w3.org/2001/XMLSchema-instance\")");
        jw.writeEol("setAttributeValue(\"xmlns:xsi\", \"http://www.w3.org/2001/XMLSchema-instance\")");
        jw.writeEol("createAttribute(\"xsi:schemaLocation\", \"xsi:schemaLocation\", AttrProp.CDATA | AttrProp.IMPLIED, null, location)");
        jw.end();
        jw.writeEol("setAttributeValue(\"xsi:schemaLocation\", location)");
        jw.end();
        jw.cr();

        jw.beginMethod("_getSchemaLocation", "", null, "String", jw.PUBLIC);
        jw.beginIf("beanProp().getAttrProp(\"xsi:schemaLocation\", true) == null");
        jw.writeEol("createAttribute(\"xmlns:xsi\", \"xmlns:xsi\", AttrProp.CDATA | AttrProp.IMPLIED, null, \"http://www.w3.org/2001/XMLSchema-instance\")");
        jw.writeEol("setAttributeValue(\"xmlns:xsi\", \"http://www.w3.org/2001/XMLSchema-instance\")");
        jw.writeEol("createAttribute(\"xsi:schemaLocation\", \"xsi:schemaLocation\", AttrProp.CDATA | AttrProp.IMPLIED, null, null)");
        jw.end();
        jw.writeEol("return getAttributeValue(\"xsi:schemaLocation\")");
        jw.end();
        jw.cr();
	}
	
	select(TRAILER_SECTION);
	comment("Dump the content of this bean returning it as a String");
	gen(PUBLIC, VOID, "dump(StringBuffer str, String indent)");
	begin();
	gen("String s"); eol();
	gen("Object o"); eol();
	gen("org.netbeans.modules.schema2beans.BaseBean n"); eol();
	
	select(CONSTRUCTOR_SECTION);
	comment("Properties (see root bean comments for the bean graph)");

    int propertyCount = 0;
    for (int i = 0; i < size; i++) {
        Property a = (Property) attrList.get(i);
        if (!a.isAttribute()) {
            ++propertyCount;
        }
    }
    jw.writeEol("initPropertyTables("+propertyCount, ")");
	
	select(DECL_SECTION); cr();
	
	//
	//	Assume that there is no property interested in veto events.
	//	We'll adjust later on in the above properties loop.
	//
	this.genVetoListeners = false;
	
	//	Find out if any element has a key
	if (this.metaElement != null) {
	    MetaProperty[] mp = this.metaElement.getMetaProperty();
	    for (int i=0; i<mp.length; i++) {
		if (mp[i].isKey()) {
		    //	At least one key is defined - default to false
		    defaultKey = false;
		    break;
		}
	    }
	}
    
	for(int i=0; i<size; i++) {
	    boolean   	indexed = false;
	    Property 	a = (Property)this.attrList.get(i);
	    String	constName = a.constName;
	    boolean	keyedElement = defaultKey;
	    boolean	isWrapper = false;
	    MetaElement	me;
        String 	scalarType = a.getScalarType();
	    boolean 	genVetoable = this.isVetoable;
        Signatures sigs = getSignatures(a);
	    boolean isScalar = a.isScalar();
	    
	    
	    //
	    //	Information about this property defined in the mdd file
	    //	(extra information provided on top of the dtd informations)
	    //
	    MetaProperty mp = getMetaProperty(a);
	    
	    if (mp != null) {
		keyedElement = (mp.isKey())?true:defaultKey;
		genVetoable = (mp.isVetoable())?true:genVetoable;
	    }
	    
	    //	Change the class type with the Wrapper info
	    me = getMetaElement(a);
        if (me != null) {
            /*
            String tmp = me.getWrapperClass();
            if (tmp != null && !tmp.equals("") &&
                !tmp.equals("String") && !tmp.equals("java.lang.String")) {
                int	t = Common.wrapperToType(tmp);
                if (t != Common.NONE)
                    a.type = t;
                else {
                    a.classType = tmp;
                    isWrapper = true;
                }
            }
            */
            /*
            if (me.getBeanName() != null) {
                if (config.isTraceGen() && !a.name.equals(me.getBeanName()))
                    config.messageOut.println("Reseting a.name from "+a.name+" to "+me.getBeanName());
                a.name = me.getBeanName();
            }
            */
            
            genVetoable = (me.isVetoable())?true:genVetoable;
        }
        if (!isScalar && !a.isBean) {
            if (!JavaUtil.isPrimitiveType(a.classType))
                isWrapper = true;
        }
        
        //System.out.println("a.name="+a.name+" a.type="+a.type+" a.dtdName="+a.dtdName+" isWrapper="+isWrapper+" a.classType="+a.classType+" me="+me+" constName="+constName);
	    
	    if (genVetoable) {
		//	At least prop is handling veto events - we'll need to
		//	generate the listener methods.
		this.genVetoListeners = true;
	    }
	    
	    //	Only final prop can be vetoable
	    if (a.isBean)
            genVetoable = false;
	    
	    //	Generate the constants
	    select(DECL_SECTION);
        gen(STATIC, PUBLIC, FINAL, STRING, (String)constName);
        gen(" = \"");
        gen(a.name);
        gen("\"");
        eolNoI18N();
	    
	    //	Keep track of the constant name for this property
	    select(CONSTRUCTOR_SECTION);
        if (!a.isAttribute()) {
            gen("this.createProperty(\"", a.dtdName, "\", ");
            noI18N(); tabIn();
            gen(constName, ", ");
            if (a.ored)
                gen("Common.SEQUENCE_OR | ");
            
            if (genVetoable)
                gen("Common.TYPE_VETOABLE |");
            
            cr(); tabIn();
	    
            select(BODY_SECTION);
            if (a.elementInstance == Common.TYPE_1) {
                //
                //  The property is mandatory. Check it is properly set.
                //
                if (!Common.isBoolean(a.type)) {
                    select(CONSTRUCTOR_SECTION);
                    gen("Common.TYPE_1");
                }
                else {
                    //
                    //  A boolean type cannot be mandatory because the
                    //  value of the tag depends on the existance of the
                    //  empty tag in the document (a mandatory DTD empty
                    //  element doesn't make sense since it would always
                    //  be true).
                    //
                    select(CONSTRUCTOR_SECTION);
                    gen("Common.TYPE_0_1");
                }
            }
            else
                if (a.elementInstance == Common.TYPE_0_1) {
                    select(CONSTRUCTOR_SECTION);
                    gen("Common.TYPE_0_1");
                }
                else
                    if (a.elementInstance == Common.TYPE_1_N) {
                        //
                        //  Check out that the first element is set and check
                        //  that every element set is valid.
                        //
                        indexed = true;
                        select(CONSTRUCTOR_SECTION);
                        gen("Common.TYPE_1_N");
                    }
                    else
                        if (a.elementInstance == Common.TYPE_0_N) {
                            //
                            //  The property is optional. However if 
                            //  the property is set and is a bean, 
                            //  its content has to be valid.
                            //
                            indexed = true;
                            select(CONSTRUCTOR_SECTION);
                            gen("Common.TYPE_0_N");
                        }
	    }
	    select(out);
	    
	    /*
	     *	Set method
	     */
        Property attributeOwner = null;
        if (a.isAttribute()) {
            attributeOwner = a.getAttributeOwner();
            indexed = attributeOwner.isIndexed();
        }
	    comment("This attribute is ", Common.instanceToString(a.elementInstance));
        String type;
        if (isScalar)
            type = scalarType;
        else
            type = a.classType;
	    //	Set - signature
        String setParameters = "";
	    if (indexed)
            setParameters = "int index, ";
        if (a.getPropertyInterface() == null) {
            setParameters += type + " value";
        } else {
            setParameters += a.getPropertyInterface() + " valueInterface";
        }
        String thrownExceptions = null;
	    if (genVetoable) {
            thrownExceptions = "java.beans.PropertyVetoException";
        }

        jw.beginMethod(a.getWriteMethod(), setParameters, thrownExceptions,
                       "void", jw.PUBLIC | jw.BEANINFO);
        if (a.getPropertyInterface() != null) {
            jw.writeEol(type+" value = ("+type+") valueInterface");
        }
	    if (genVetoable)
            genVetoBegin();
	    
        SchemaRep.WhiteSpace ws = (SchemaRep.WhiteSpace) a.searchExtraData(SchemaRep.WhiteSpace.class);
        if (ws != null)
            genWhiteSpaceRestriction(ws, "value", type);
        if (a.isAttribute()) {
            AttrProp attrProp = a.getAttrProp();
            if (attributeOwner != a) {
                jw.comment("Make sure we've got a place to put this attribute.");
                jw.beginIf("size("+attributeOwner.constName+") == 0");
                String valueToSetTo = "\"\"";	// The empty string.
                if (Common.isBoolean(attributeOwner.type) && attributeOwner.getCanBeEmpty())
                    valueToSetTo = "java.lang.Boolean.TRUE";
                if (attributeOwner.isIndexed()) {
                    jw.writeEol("addValue("+attributeOwner.constName+", ",
                                valueToSetTo, ")");
                } else {
                    jw.writeEol("setValue("+attributeOwner.constName+", ",
                                valueToSetTo, ")");
                }
                jw.end();
                if (Common.isBoolean(attributeOwner.type) && attributeOwner.getCanBeEmpty() && attributeOwner.isIndexed()) {
                    jw.write("setValue(", attributeOwner.constName);
                    jw.write(", index");
                    jw.writeEol(", java.lang.Boolean.TRUE)");
                }
                jw.write("setAttributeValue(");
                jw.write(attributeOwner.constName+", ");
                if (attributeOwner.isIndexed())
                    gen("index, ");
                jw.write("\""+attrProp.getName()+"\"");
            } else {
                gen("setAttributeValue(");
                gen(a.constName);
            }
            gen(", ");
            if (isScalar)
                gen("\"\"+value");
            else
                gen(JavaUtil.typeToString(a.getType(), "value"));
            geneol(")");
        } else {
            gen("this.setValue(", constName, ", ");
            
            if (indexed) gen("index, ");
            
            genSetValue(isScalar, a.getType());
	    
            eol();
        }
	    
	    if (genVetoable)
            genVetoEnd();

	    if (!indexed)
            genResetMutuallyExclusive(a, true);
	    end(); cr();
	    
	    /*
	     *	Get method
	     */    
	    comment();
	    //	Signature
        String getParameters = "";
        if (indexed)
            getParameters = "int index";
        jw.beginMethod(a.getReadMethod(indexed), getParameters, null,
                       a.getPropertyInterface() == null ? type :a.getPropertyInterface(),
                       jw.PUBLIC|jw.BEANINFO);
	    
	    //	Body
        if (a.isAttribute()) {
            List exceps = JavaUtil.exceptionsFromParsingText(a.getType());
            if (!exceps.isEmpty()) {
                gen("try ");
                begin();
            }
            if (attributeOwner != a) {
                AttrProp attrProp = a.getAttrProp();
                jw.comment("If our element does not exist, then the attribute does not exist.");
                jw.beginIf("size("+attributeOwner.constName+") == 0");
                jw.writeEol("return " + JavaUtil.nullValueForType(a.getType()));
                jw.endElseBegin();
                jw.write("return ");
                jw.writeEol(JavaUtil.genParseText(a.getType(), "getAttributeValue("+attributeOwner.constName+ ((attributeOwner.isIndexed()) ? ", index" : "") +", \""+attrProp.getName()+"\")", config.isForME()));
                jw.end();
            } else {
                String nullValue = JavaUtil.nullValueForType(a.getType());
                gen("return ");
                if (!"null".equals(nullValue))
                    gen("(getAttributeValue("+a.constName+") == null) ? "+nullValue+" : ");
                gen(JavaUtil.genParseText(a.getType(), "getAttributeValue("+a.constName+")", config.isForME()));
                eol();
            }
            if (!exceps.isEmpty()) {
                end();
                genRethrowExceptions(exceps);
            }
        } else if (isScalar) {
            gen(a.classType, " ret = ");
            gen("(", a.classType, ")");
            gen("this.getValue(", constName);
            if (indexed) gen(", index");
            PC(); eol();
            gencr("if (ret == null)");
            if (this.config.isScalarException() && !Common.isBoolean(a.type)) {
                if (this.config.isStandalone()) {
                    tabIn();
                    gen("throw new NoSuchElementException(\"The element ",
                        a.name, " of type ");
                    gen(scalarType, " has no value.\")"); eol();
                } else {
                    tabIn();
                    gencr("throw new RuntimeException(Common.getMessage(");
                    tabIn(); tabIn();
                    gencr("\"NoValueForElt_msg\",");
                    tabIn(); tabIn();
                    gen("new Object[] {\"");
                    gen(constName);
                    gen("\", ");
                    gen("\"", scalarType);
                    gen("\"}));");
                    cr();
                }
            }
            else {
                tabIn();
                gen("ret = ");
                gen("(", a.classType, ")");
                gen("Common.defaultScalarValue(Common.");
                gen(Common.typeToString(a.type), ")"); eol();
            }
            gen("return "+JavaUtil.fromObject(a.getType(), "ret"));
            eol();
	    } else {
            gen("return (", a.classType, ")");
            gen("this.getValue(", constName);
            if (indexed) gen(", index"); PC();
            eol();
	    }
	    end(); cr();
	    
	    //
	    //	If we are an indexed property, we have to generate another
	    //	kind of setter and getter method for the whole array.
	    //
	    if (indexed) {
            jw.comment("Return the number of properties");
            //	Signature
            gen(sigs.findSignature(SIZE));
            sp();
            begin();
            //	Body
            if (!a.isAttribute())
                gen("return this.size(", constName, ")");
            else
                gen("return this.size(", attributeOwner.constName, ")");
            eol(); end(); cr();
            
            if (!a.isAttribute()) {
                //	Setter method
                comment("This attribute is ", Common.instanceToString(a.elementInstance));
                //	Set - signature
                if (a.getPropertyInterface() == null) {
                    setParameters = type + "[] value";
                } else {
                    setParameters = a.getPropertyInterface() +"[] value";
                }
                jw.beginMethod(a.getWriteMethod(), setParameters, thrownExceptions, "void", jw.PUBLIC|jw.BEANINFO);
		
                //	Body
                if (isScalar) {
                    gen(a.classType, "[] values = null"); eol();
                    gencr("if (value != null)");
                    begin();
                    gen("values = new ", a.classType, "[value.length]"); eol();
                    gencr("for (int i=0; i<value.length; i++)");
                    gentab(1);
                    if (!config.isForME() && Common.isBoolean(a.type))
                        gen("values[i] = (value[i] ? Boolean.TRUE : Boolean.FALSE)");
                    else
                        gen("values[i] = new ", a.classType, "(value[i])");
                    eol();
                    end();
                }
		
                if (genVetoable)
                    genVetoBegin();
		
                gen("this.setValue(", constName, ", ");
            
                if (isScalar)
                    gen("values)");
                else
                    gen("value)");
            
                eol();
                if (genVetoable)
                    genVetoEnd();

                genResetMutuallyExclusive(a, true);
                end(); cr();
		
                //	Getter method
                comment();
                //	Signature
                jw.beginMethod(a.getReadMethod(false), "", null,
                               (a.getPropertyInterface() == null ? type : a.getPropertyInterface())+"[]",
                               jw.PUBLIC|jw.BEANINFO);

                //	Body
                if (isScalar) {
                    gen(scalarType, "[] ret = null"); eol();
                    gen(a.classType, "[] values = (", a.classType);
                    gen("[])this.getValues(", constName, ")"); eol();
                    gencr("if (values != null)");
                    begin();
                    gen("ret = new ", scalarType, "[values.length]"); eol();
                    gencr("for (int i=0; i<values.length; i++)");
                    gentab(1);
                    gen("ret[i] = values[i].",
                        Common.wrapperGetMethod(a.type), "()"); eol();
                    end();
                    geneol("return ret");
                } else {
                    gen("return (", a.classType, "[])");
                    gen("this.getValues(", constName, ")");
                    eol();
                }
                end(); cr();

                /*
                 *	Remove and Add methods
                 */
                comment("Add a new element returning its index in the list");
                //	Signature
                gen(sigs.findSignature(ADD));
                sp();
                if (genVetoable) {
                    gencr("throws PropertyVetoException ");
                }
                begin();
                //	Body
                if (a.getPropertyInterface() != null) {
                    jw.writeEol(a.getType()+" value = ("+a.getType()+") valueInterface");
                }
                if (genVetoable)
                    genVetoBegin();
                jw.write("int positionOfNewItem = this.addValue(", constName, ", ");
                genSetValue(isScalar, a.getType());
                jw.eol();
                if (isMutuallyExclusive(a)) {
                    jw.beginIf("positionOfNewItem == 0");
                    genResetMutuallyExclusive(a, false);
                    jw.end();
                }
                jw.writeEol("return positionOfNewItem");
                if (genVetoable)
                    genVetoEnd();
                end(); cr();
		
                comment();
                comment("Remove an element using its reference");
                comment("Returns the index the element had in the list");
                comment();
                //	Signature
                gen(sigs.findSignature(REMOVE));
                sp();
                if (genVetoable) {
                    gencr("throws PropertyVetoException ");
                }
                begin();
                //	Body
                if (a.getPropertyInterface() != null) {
                    jw.writeEol(a.getType()+" value = ("+a.getType()+") valueInterface");
                }
                if (genVetoable)
                    genVetoBegin();
                gen("return this.removeValue(", constName, ", ");
                genSetValue(isScalar, a.getType());
                eol();
                if (genVetoable)
                    genVetoEnd();
                end(); cr();

                if (isScalar && !"int".equals(a.getType())) {
                    // Don't do "int" because we already have a method
                    // with that signature.
                    comment();
                    comment("Remove an element using its index");
                    comment();
                    //	Signature
                    jw.beginMethod(a.getRemoveMethod(), "int index",
                                   thrownExceptions, "void",
                                   jw.PUBLIC|jw.BEANINFO);
                    //	Body
                    if (genVetoable)
                        genVetoBegin();
                    gen("this.removeValue(", constName, ", index)");
                    eol();
                    if (genVetoable)
                        genVetoEnd();
                    end(); cr();
                }
            }
        }
	    
	    //
	    //-----------------
	    
	    {
            //	The metaDD might define the class name of the bean
            MetaElement e = getMetaElement(a);
            String cls = null;
            if (e != null)
                cls = e.getBeanClass();
            if (cls == null)
                cls = a.classType;
            //System.out.println("cls="+cls);

            if (!a.isAttribute()) {
                select(CONSTRUCTOR_SECTION);
                gen(" | Common.");
                if (isScalar && (a.type != Common.TYPE_BOOLEAN)) {
                    gen(Common.typeToString(Common.TYPE_STRING));
                } else {
                    gen(Common.typeToString(a.type));
                    if (a.type == Common.TYPE_BOOLEAN && !a.getCanBeEmpty()) {
                        gen(" | Common.TYPE_SHOULD_NOT_BE_EMPTY");
                    }
                }
                if (keyedElement)
                    gen(" | Common.TYPE_KEY");
                gen(", "); cr(); tabIn();
                gen( cls, ".class)"); eol();
            }
	    }
	    
	    //	dump()
        if (!a.isAttribute()) {
            select(TRAILER_SECTION);
            if (isScalar && config.isScalarException() && !Common.isBoolean(a.type)) {
                gen("if (this.getValue(");
                gen(constName);
                gen(") != null) ");
                begin();
            }
            gen("str.append(indent)"); eol();
            gen("str.append(\"",a.name);
            if (indexed) {
                gen("[\"+this.size", a.name, "()+\"]");
            }
            gen("\")" );
            eolNoI18N();
            if (indexed) {
                gen("for(int i=0; i<this.size", a.name, "(); i++)");
                cr();
                begin();
                gen("str.append(indent+\"\\t\")"); eol();
                gen("str.append(\"#\"+i+\":\")"); eol();
            }
	    
            boolean isCharArray = a.classType.equals("char[]");
            if (Common.isBoolean(a.type)) {
                gen("str.append(indent+\"\\t\")"); eolNoI18N();
                gen("str.append((");
                gen("this.is", a.name, "(");
                if (indexed)
                    gen("i");
                gen(")?\"true\":\"false\"))"); eol();
            } else if (Common.isString(a.type) || isScalar || isCharArray) {
                gen("str.append(indent+\"\\t\")"); eolNoI18N();
                gen("str.append(\"<\")"); eolNoI18N();
		    
                if (isWrapper && !isCharArray)
                    gen("o = ");
                else
                    gen("s = ");
		    
                if (isScalar || isCharArray) gen("String.valueOf(");
                gen("this.get", a.name, "(");
                if (indexed) gen("i");
                gen(")");
                if (isScalar || isCharArray) gen(")");
                eol();
		    
                if (isWrapper && !isCharArray) {
                    gen("str.append((o==null?\"null\":o.toString()");
                } else {
                    if (isScalar && !isCharArray) {
                        gen("str.append((s");
                    } else {
                        gen("str.append((s==null?\"null\":s");
                    }
                }
		    
                gen(".trim()))");
                eolNoI18N();
                gen("str.append(\">\\n\")"); eolNoI18N();
            } else if (a.isBean) {
                gen("n = (org.netbeans.modules.schema2beans.BaseBean) this.get", a.name, "(");
                if (indexed) gen("i");
                gencr(");");
                gencr("if (n != null)");
                gen("\tn.dump(str, indent + \"\\t\")"); eolNoI18N();
                gencr("else");
                gen("\tstr.append(indent+\"\\tnull\")"); eolNoI18N();
            }
            if (!a.isAttribute()) {
                gen("this.dumpAttributes(", constName, ", ");
                if (indexed)
                    gen("i");
                else
                    gen("0");
                gen(", str, indent)"); eol();
            }
	    
            if (isScalar && config.isScalarException() && !Common.isBoolean(a.type)) {
                end();
            }
	    
            if (indexed) {
                //gen("str.append(indent+\"\\t\")"); eol();
                //gen("str.append(\"---\")"); eol();
                end();
            }
            cr();
        }
	    

        genAttributes(CONSTRUCTOR_SECTION, a.attributes, constName);
        select(ACCESS_SECTION);
	    genDefaultsAccessable(a);

        if (a.isBean) {
            genNewMethod(a.getPropertyInterface(), a.getType());
        }
	}
	
	//  The root might have attributes
	if (this.beanElement.isRoot)
	    genAttributes(CONSTRUCTOR_SECTION,
	    this.beanElement.node.getAttributes(), null);
	
	select(DECL_SECTION); cr();
	
	select(TRAILER_SECTION);
	end(false);	cr();
	
    }
    
    //	Generate the createAttribute() code
    void genAttributes(int out, AttrProp[] attributes, String name) {
        select(out);
        if (attributes == null)
            return;
        for (int j=0; j<attributes.length; j++) {
            AttrProp attr = attributes[j];
            genAttribute(name, attr);
        }
    }

    void genAttribute(String name, AttrProp attr) {
        String constName = Common.constName(attr.getDtdName());
        gen("this.createAttribute(");
        if (name != null)
            gen(name, ", ");
        gen("\"", attr.getDtdName(), "\", ");
        gen("\"", attr.getName(), "\", "); cr(); gentab(4);
        gencr(attr.typeAsString(), ","); gentab(4);
        String[] values = attr.getValues();
        if (values.length > 0) {
            gencr("new String[] {");
            for (int k=0; k<values.length; k++) {
                if (k>0) gencr(",");
                gentab(5);
                gen("\"", values[k], "\"");
            }
            cr(); gentab(4); gen("}, ");
        }
        else
            gen("null, ");
	    
        String value = attr.getDefaultValue();
	    
        if (value != null)
            gen("\"", value, "\")");
        else
            gen("null)");
        eol();
    }
    
    void genBody(int out) throws IOException {
	select(out);
	
	//
	//  add/remove comparators
	//
	comment();
    jw.beginMethod("addComparator", "org.netbeans.modules.schema2beans.BeanComparator c", null, "void", jw.PUBLIC | jw.STATIC);
	gen("comparators.add(c)"); eol();
	end(); cr();
	
	comment();
    jw.beginMethod("removeComparator", "org.netbeans.modules.schema2beans.BeanComparator c", null, "void", jw.PUBLIC | jw.STATIC);
	gen("comparators.remove(c)"); eol();
	end();
	
	//
	//  Event registration methods
	//
	
	//
	// the generation of addPropertyChange are in BaseBean.
	//
	
	if (this.genGenericVetoListeners) {
	    comment();
        jw.beginMethod("add"+VCL, VCL_FULL_CLASS_NAME+" l", null, "void", jw.PUBLIC);
	    gengetprop();
	    gen("\tp.addVCListener(l)"); eol();
	    end(); cr();
	    
	    comment();
        jw.beginMethod("remove"+VCL, VCL_FULL_CLASS_NAME+" l", null, "void", jw.PUBLIC);
	    gengetprop();
	    gen("\tp.removeVCListener(l)"); eol();
	    end(); cr();
	}
	
	//
	//	Per property event registration method
	//
	
	//
	// the generation of addPropertyChange are now in BaseBean.
	//	
	if (this.genVetoListeners) {
	    comment();
        jw.beginMethod("add"+VCL, "String n, "+VCL_FULL_CLASS_NAME+" l", null, "void", jw.PUBLIC);
	    gengetpropbyname();
	    gen("\tp.addVCListener(l)"); eol();
	    end(); cr();
	    
	    comment();
        jw.beginMethod("remove"+VCL, "String n, "+VCL_FULL_CLASS_NAME+" l", null, "void", jw.PUBLIC);
	    gengetpropbyname();
	    gen("\tp.removeVCListener(l)"); eol();
	    end(); cr();
	}
	
	if (this.beanElement.isRoot) {
	    //	public void createBean(Node doc)
	    comment();
	    comment("This method returns the root of the bean graph");
	    comment("Each call creates a new bean graph from the specified DOM graph");
	    comment();

        String thrownExceptions = null;
        if (shouldThrowException()) {
            thrownExceptions = "org.netbeans.modules.schema2beans.Schema2BeansException";
        }
        jw.beginMethod("createGraph", "org.w3c.dom.Node doc", thrownExceptions, className, jw.PUBLIC | jw.STATIC);
        if (config.isSetDefaults())
            gen("return new ", this.className, "(doc, Common.USE_DEFAULT_VALUES)");
        else
            gen("return new ", this.className, "(doc, Common.NO_DEFAULT_VALUES)");
        eol();
	    end(); cr();
	    
        jw.beginMethod("createGraph", "java.io.File f",
                       (thrownExceptions == null) ? "java.io.IOException" :
                         thrownExceptions + ", " + "java.io.IOException",
                       className, jw.PUBLIC | jw.STATIC);
        jw.writeEol("java.io.InputStream in = new java.io.FileInputStream(f)");
        jw.beginTry();
        gen("return createGraph(in, false)"); eol();
        jw.endFinallyBegin();
        jw.writeEol("in.close()");
        jw.end();
	    jw.endMethod();

	    //  public void createGraph(InputStream in)
        jw.beginMethod("createGraph", "java.io.InputStream in", thrownExceptions, className, jw.PUBLIC | jw.STATIC);
        gen("return createGraph(in, false)"); eol();
	    jw.endMethod();
	    
	    //  public void createGraph(InputStream in)
        jw.beginMethod("createGraph", "java.io.InputStream in, boolean validate", thrownExceptions, className, jw.PUBLIC | jw.STATIC);
        if (!shouldThrowException()) {
            gen("try ");
            begin();
        }
	    gen("Document doc = GraphManager.createXmlDocument(in, validate)");
        eol();
        gen("return createGraph(doc)"); eol();
        if (!shouldThrowException()) {
            end();
            gen("catch (Exception t) ");
            begin();
            if (this.config.isStandalone()) {
                gencrNoI18N("throw new RuntimeException(\"DOM graph creation failed\", t);");
            } else {
                gencr("throw new RuntimeException(Common.getMessage(");
                tabIn();
                gencr("\"DOMGraphCreateFailed_msg\",");
                tabIn();
                gen("t))"); eol();
            }
            end();
        }
	    end(); cr();
	    
	    
	    //  public void createBean()
	    comment();
	    comment("This method returns the root for a new empty bean graph");
	    comment();
	    
        jw.beginMethod("createGraph", "", null, className, jw.PUBLIC | jw.STATIC);
        if (shouldThrowException()) {
            gen("try ");
            begin();
        }
	    gen("return new ", this.className, "()"); eol();
        if (shouldThrowException()) {
            end();
            gen("catch (Schema2BeansException e) ");
            begin();
            gen("throw new RuntimeException(e)"); eol();
            end();
        }
	    end(); cr();
	}
	
	MetaElement me = getMetaElement(beanElement);
	if (me != null && me.getUserCode() != null) {
	    String userCode = me.getUserCode();
	    cr(); gencr(userCode);
	}
    }

    boolean shouldThrowException() {
        return (config.isThrowErrors() || (mdd != null && mdd.isThrowExceptions()));
    }
    
    void genValidate() throws IOException {
        select(BODY_SECTION);
        jw.beginMethod("validate", "", "org.netbeans.modules.schema2beans.ValidateException", "void", jw.PUBLIC);
        if (config.isGenerateValidate()) {
            genValidateProperties();
        }
        end();
        cr();
    }

    protected void genValidateFail(String detail, String name,
                                   boolean quoteDetail,
                                   ValidateException.FailureType ft,
                                   JavaWriter out) throws IOException {
        out.write("throw new org.netbeans.modules.schema2beans.ValidateException(");
        if (quoteDetail)
            out.write('"');
        out.write(detail);
        if (quoteDetail)
            out.write('"');
        out.write(", org.netbeans.modules.schema2beans.ValidateException.FailureType.",
                 ft.toString());
        out.writeEolNoI18N(", \""+name+"\", this)");
    }

    void genTrailer(int out) {
	select(out);
	gen(PUBLIC, STRING, "dumpBeanNode()");
	begin();
	gen("StringBuffer str = new StringBuffer()"); eol();
	gen("str.append(\"", this.className, "\\n\")"); eolNoI18N();
	gen("this.dump(str, \"\\n  \")"); eolNoI18N();
	gen("return str.toString()"); eol();
	end(false);
	
	if (this.config.isDumpToString()) {
	    cr();
	    gencr(PUBLIC, STRING, "toString()");
	    begin();
	    gen("return this.dumpBeanNode()"); eol();
	    end();
	}
    }
        
    /**
     *	Generate the java code in the out stream, using the optional
     *	metaDD bean graph.
     */
    public void generate(OutputStream out, MetaDD mdd) throws IOException {
	this.mdd = mdd;
	this.metaElement = getMetaElement(beanElement);
	
	if (this.metaElement != null && this.metaElement.isSkipGeneration()) {
	    config.messageOut.println("Skipping generation of class "
	    + " (as specified in the mdd file)");	// NOI18N
	    return;
	}
	
    findAttributeOwners();

	this.isVetoable = this.config.isVetoable();
	
	//  Find out if there is any veto specified in the mdd
	if (this.mdd != null) {
	    //if (this.mdd.findPropertyValue("vetoable", Boolean.TRUE).length != 0) {
        org.netbeans.modules.schema2beansdev.metadd.CommonBean[] beans = mdd.childBeans(true);
        for (int beanPos = 0; beanPos < beans.length; ++beanPos) {
            try {
                if (((Boolean)beans[beanPos].fetchPropertyByName("vetoable")).booleanValue()) {
                    //  Override information from the mdd file
                    this.isVetoable = this.mdd.isVetoable();
                    this.genGenericVetoListeners = true;
                    break;
                }
            } catch (IllegalArgumentException e) {
                // This bean does not have that property
            }
        }
	}
	
	
	this.genHeader(HEADER_SECTION);
	this.genPackage(HEADER_SECTION);
	this.genImports(HEADER_SECTION);
	select(HEADER_SECTION);
	cr(); beginNoI18N(); cr();
	this.genClassName(HEADER_SECTION);
	
	this.genDeclarations(DECL_SECTION);
	this.genConstructor(CONSTRUCTOR_SECTION);
	
	this.genAccessors(ACCESS_SECTION);
	
	this.genBody(BODY_SECTION);
	
	this.genInitializer();

    genValidate();
    
	this.genTrailer(TRAILER_SECTION);
	
	select(CONSTRUCTOR_SECTION);
	gen("this.initialize(options)"); eol();
	end(); cr();
	
	select(INITIALIZE_SECTION);
	cr(); end(); cr();
	
	select(TRAILER_SECTION);
	end(); cr();
	endNoI18N(); cr();
	
	try {
        printSchema();
	} catch (IOException ioe) {
	    //	Do not generate the schema comment
	}
    printGenBuffers(out);
    }

    protected void addExtraMethods() {
        // Most of these are taken from the public methods of BaseBean
        jw.addToMethodStore("addComparator", "org.netbeans.modules.schema2beans.BeanComparator c", null, "void", jw.PUBLIC | jw.STATIC);
        jw.addToMethodStore("removeComparator", "org.netbeans.modules.schema2beans.BeanComparator c", null, "void", jw.PUBLIC | jw.STATIC);
        jw.addToMethodStore("getValue", "String name", null, "Object");
        jw.addToMethodStore("getValue", "String name, int index", null, "Object");
        jw.addToMethodStore("isNull", "String name", null, "boolean");
        jw.addToMethodStore("isNull", "String name, int index", null, "boolean");
        jw.addToMethodStore("getValues", "String name", null, "Object[]");
        jw.addToMethodStore("setValue", "String name, Object value", null, "void");
        jw.addToMethodStore("setValue", "String name, int index, Object value", null, "void");
        jw.addToMethodStore("setValue", "String name, Object[] value", null, "void");
        jw.addToMethodStore("addValue", "String name, Object value", null, "int");
        jw.addToMethodStore("removeValue", "String name, Object value", null, "int");
        jw.addToMethodStore("removeValue", "String name, int index", null, "void");
        jw.addToMethodStore("indexOf", "String name, Object value", null, "int");
        jw.addToMethodStore("size", "String name", null, "int");
        jw.addToMethodStore("isChoiceProperty", "String name", null, "boolean");
        jw.addToMethodStore("isChoiceProperty", "", null, "boolean");
        jw.addToMethodStore("getAttributeValue", "String name", null, "String");
        jw.addToMethodStore("getAttributeValue", "String propName, String name", null, "String");
        jw.addToMethodStore("setAttributeValue", "String propName, int index, String name, String value", null, "void");
        jw.addToMethodStore("getAttributeValue", "String propName, int index, String name", null, "String");
        jw.addToMethodStore("getAttributeNames", "String propName", null, "String[]");
        jw.addToMethodStore("getAttributeNames", "", null, "String[]");
        jw.addToMethodStore("write", "java.io.OutputStream out", "java.io.IOException, org.netbeans.modules.schema2beans.Schema2BeansRuntimeException", "void");
        jw.addToMethodStore("write", "java.io.OutputStream out, String encoding", "java.io.IOException, org.netbeans.modules.schema2beans.Schema2BeansException", "void");
        jw.addToMethodStore("write", "java.io.Writer w", "java.io.IOException, org.netbeans.modules.schema2beans.Schema2BeansException", "void");
        jw.addToMethodStore("write", "java.io.Writer w, String encoding", "java.io.IOException, org.netbeans.modules.schema2beans.Schema2BeansException", "void");
        jw.addToMethodStore("writeNoReindent", "java.io.OutputStream out", "java.io.IOException, org.netbeans.modules.schema2beans.Schema2BeansException", "void");
        jw.addToMethodStore("reindent", "", null, "void");
        jw.addToMethodStore("clone", "", null, "Object");
        jw.addToMethodStore("merge", "org.netbeans.modules.schema2beans.BaseBean bean, int mode", null, "void");
        jw.addToMethodStore("merge", "org.netbeans.modules.schema2beans.BaseBean bean", null, "void");
        jw.addToMethodStore("equals", "Object obj", null, "boolean");
        jw.addToMethodStore("parent", "", null, "org.netbeans.modules.schema2beans.BaseBean");
        jw.addToMethodStore("fullName", "", null, "String");
        jw.addToMethodStore("isRoot", "", null, "boolean");
        jw.addToMethodStore("name", "", null, "String");
        jw.addToMethodStore("dtdName", "", null, "String");
        jw.addToMethodStore("dump", "StringBuffer str, String indent", null, "void");
        jw.addToMethodStore("createGraph", "Class clazz, java.io.InputStream in", "org.netbeans.modules.schema2beans.Schema2BeansException", "org.netbeans.modules.schema2beans.BaseBean", jw.PUBLIC | jw.STATIC);
        jw.addToMethodStore("createGraph", "Class clazz, java.io.InputStream in, boolean validate", "org.netbeans.modules.schema2beans.Schema2BeansException", "org.netbeans.modules.schema2beans.BaseBean", jw.PUBLIC | jw.STATIC);
        jw.addToMethodStore("createGraph", "Class clazz, java.io.InputStream in, boolean validate, org.xml.sax.EntityResolver er", "org.netbeans.modules.schema2beans.Schema2BeansException", "org.netbeans.modules.schema2beans.BaseBean", jw.PUBLIC | jw.STATIC);
        jw.addToMethodStore("createGraph", "Class clazz, java.io.InputStream in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh", "org.netbeans.modules.schema2beans.Schema2BeansException", "org.netbeans.modules.schema2beans.BaseBean", jw.PUBLIC | jw.STATIC);
        jw.addToMethodStore("addPropertyChangeListener", "java.beans.PropertyChangeListener l", null, "void");
        jw.addToMethodStore("removePropertyChangeListener", "java.beans.PropertyChangeListener l", null, "void");
        jw.addToMethodStore("addPropertyChangeListener", "String n, java.beans.PropertyChangeListener l", null, "void");
        jw.addToMethodStore("removePropertyChangeListener", "String n, java.beans.PropertyChangeListener l", null, "void");
        jw.addToMethodStore("comments", "", null, "org.w3c.dom.Comment[]");
        jw.addToMethodStore("addComment", "String comment", null, "org.w3c.dom.Comment");
        jw.addToMethodStore("removeComment", "org.w3c.dom.Comment comment", null, "void");
        jw.addToMethodStore("childBeans", "boolean recursive", null, "org.netbeans.modules.schema2beans.BaseBean[]");
        jw.addToMethodStore("childBeans", "boolean recursive, java.util.List beans", null, "void");
        jw.addToMethodStore("setDefaultNamespace", "String namespace", null, "void");
        jw.addToMethodStore("getDefaultNamespace", "", null, "String");
        jw.addToMethodStore("toString", "", null, "String");
        //jw.addToMethodStore("copyProperties", "org.netbeans.modules.schema2beans.BaseBean destBean, java.util.Map nameMapping", null, "void");
    }

    /*
    protected Signatures getSystemSignatures() {
        Signatures result = new Signatures();
        Signature sig;
        if (config.isGenerateValidate()) {
            result.add(new Signature("void", "validate"));
        }
        sig = new Signature("void", "addComparator");
        sig.addParameter("org.netbeans.modules.schema2beans.BeanComparator", "c");
        sig.setStatic();
        result.add(sig);
        sig = new Signature("void", "removeComparator");
        sig.addParameter("org.netbeans.modules.schema2beans.BeanComparator", "c");
        sig.setStatic();
        result.add(sig);
        if (beanElement.isRoot) {
            sig = new Signature(className, "createGraph");
            sig.addParameter("org.w3c.dom.Node", "doc");
            sig.setStatic();
            result.add(sig);
            sig = new Signature(className, "createGraph");
            sig.addParameter("java.io.InputStream", "in");
            sig.setStatic();
            result.add(sig);
            sig = new Signature(className, "createGraph");
            sig.addParameter("java.io.InputStream", "in");
            sig.addParameter("boolean", "validate");
            sig.setStatic();
            result.add(sig);
            sig = new Signature(className, "createGraph");
            sig.setStatic();
            result.add(sig);
        }
        sig = new Signature("void", "write");
        sig.addParameter("java.io.OutputStream", "out");
        sig.addThrows("java.io.IOException");
        sig.addThrows("org.netbeans.modules.schema2beans.Schema2BeansRuntimeException");
        result.add(sig);
        sig = new Signature("void", "write");
        sig.addParameter("java.io.OutputStream", "out");
        sig.addParameter("String", "encoding");
        sig.addThrows("java.io.IOException");
        sig.addThrows("org.netbeans.modules.schema2beans.Schema2BeansException");
        result.add(sig);
        sig = new Signature("void", "write");
        sig.addParameter("java.io.Writer", "w");
        sig.addThrows("java.io.IOException");
        sig.addThrows("org.netbeans.modules.schema2beans.Schema2BeansException");
        result.add(sig);
        sig = new Signature("void", "write");
        sig.addParameter("java.io.Writer", "w");
        sig.addParameter("String", "encoding");
        sig.addThrows("java.io.IOException");
        sig.addThrows("org.netbeans.modules.schema2beans.Schema2BeansException");
        result.add(sig);
        sig = new Signature("void", "merge");
        sig.addParameter("org.netbeans.modules.schema2beans.BaseBean", "bean");
        result.add(sig);
        sig = new Signature("boolean", "equals", OTHER);
        sig.addParameter("Object", "obj");
        result.add(sig);
        result.add(new Signature("String", "toString"));
        return result;
    }
    */

    public void setInvalidPropertyNames(Map invalidNames) {
        invalidNames.put("Class", null);
        invalidNames.put("Property", null);
        invalidNames.put("AttributeNames", null);
    }

    protected String testIfPropertySet(Property prop) {
        if (!prop.isAttribute() && prop.isScalar() &&
            !Common.isBoolean(prop.type))
            return "getValue("+prop.constName+") != null";
        else
            return super.testIfPropertySet(prop);
    }

    protected String testIfPropertyNotSet(Property prop) {
        if (!prop.isAttribute() && prop.isScalar() &&
            !Common.isBoolean(prop.type))
            return "getValue("+prop.constName+") == null";
        else
            return super.testIfPropertyNotSet(prop);
    }
}

//******************************************************************************
// END_NOI18N
// This class does not (and will not) cantain strings that need to be localized.
//******************************************************************************
