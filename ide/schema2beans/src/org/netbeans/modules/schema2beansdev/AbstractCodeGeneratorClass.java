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

public abstract class AbstractCodeGeneratorClass {
    static class Property {
        String	dtdName;
        private String namespace;
        String 	name;
        String 	classType;
        int	type;
        int	elementInstance;
        int    	groupInstance;
        int	level;
        boolean	isBean;
        boolean	ored;
        boolean isUnion = false;
        String constName;
        private boolean nillable;
        private boolean _isAttribute;
        private AttrProp attrProp;
        private Property attributeOwner = null;
        private String defaultValue;
        private boolean directChild;
        private GraphNode graphNode;
        private GraphLink graphLink;
        private List extraData;
        private boolean canBeEmpty = false;
        private String propertyInterface = null;
        private transient List mutuallyExclusiveProperties;
        private BeanBuilder.BeanElement beanElement;
        //private String orGroupName;
	
        //  List of attributes for this property (might be empty)
        AttrProp[]	attributes;
	
        Property(String propName, String dtdName, String namespace,
                 GraphNode graphNode,
                 GraphLink graphLink, String classType,
                 int eInst, int gInst, int level, int type, boolean ored,
                 AttrProp[] attrs, String constName, String defaultValue,
                 boolean directChild, List extraData, boolean isUnion) {
            //
            //	If the group instance is array, the element instance
            //	becomes an array.
            //
            if (gInst == Common.TYPE_0_N)
                eInst = gInst;
            else
                if (gInst == Common.TYPE_1_N) {
                    if (eInst == Common.TYPE_0_1)
                        eInst = Common.TYPE_0_N;
                    else
                        eInst = Common.TYPE_1_N;
                }
	    
            this.name = propName;
            this.isUnion = isUnion;
            this.dtdName = dtdName;
            this.namespace = namespace;
            this.graphNode = graphNode;
            this.graphLink = graphLink;
            this.classType = classType;
            this.type = type;
            this.elementInstance = eInst;
            this.groupInstance = gInst;
            this.level = level;
            this.attributes = attrs;
            this.ored = ored;
            this.constName = constName;
            this.isBean = Common.isBean(type);
            if (isScalar()) {
                this.classType = Common.wrapperClass(type);
            }
            this.defaultValue = defaultValue;
            this.directChild = directChild;
            this.extraData = extraData;
            //System.out.println("New property: name="+name+" type="+type);
        }

        public boolean isIndexed() {
            if (elementInstance == Common.TYPE_1_N) {
                return true;
            } else if (elementInstance == Common.TYPE_0_N) {
                return true;
            }
            return false;
        }

        public void setIndexed(boolean value) {
            if (isIndexed() == value)
                return;
            if (value)
                elementInstance = Common.TYPE_0_N;
            else
                elementInstance = Common.TYPE_0_1;
        }

        public boolean isNillable() {
            return nillable;
        }

        public void setNillable(boolean value) {
            nillable = value;
        }

        public boolean isUnion() {
            return isUnion;
        }

        public void setUnion(boolean value) {
            isUnion = value;
        }

        public boolean isScalar() {
            return Common.isScalar(type);
        }

        public String getScalarType() {
            if (!isScalar())
                return null;
            return Common.scalarType(type);
        }

        /**
         * The java type used for this bean.  @see getSignatureType()
         */
        public String getType() {
            if (isScalar())
                return getScalarType();
            else
                return classType;
        }

        public void setAttribute(boolean value) {
            _isAttribute = value;
        }

        public boolean isAttribute() {
            return _isAttribute;
        }

        public void setAttrProp(AttrProp ap) {
            attrProp = ap;
            setAttribute(ap != null);
        }

        public AttrProp getAttrProp() {
            return attrProp;
        }

        public void setAttributeOwner(Property owner) {
            attributeOwner = owner;
        }

        public Property getAttributeOwner() {
            return attributeOwner;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public boolean isDirectChild() {
            return directChild;
        }

        public int getGroupInstance() {
            return groupInstance;
        }

        /**
         * The canBeEmpty property is true only if this node can be EMPTY
         * or not there.  This only occurs for when a DTD says that an
         * element is EMPTY and that maps into a boolean.
         */
        public void setCanBeEmpty(boolean value) {
            canBeEmpty = value;
        }

        public boolean getCanBeEmpty() {
            return canBeEmpty;
        }

        /**
         * This may return null.
         */
        public GraphNode getGraphNode() {
            return graphNode;
        }

        /**
         * This GraphLink is the one that defined this property's name.
         * It's siblings are the siblings in the schema.
         * This may return null.
         * getGraphLink().element == getGraphNode()
         */
        public GraphLink getGraphLink() {
            return graphLink;
        }

        public void setPropertyInterface(String iface) {
            propertyInterface = iface;
        }

        /**
         * The property interface is nonnull if the getters/setters
         * are suppose to use this interface.
         */
        public String getPropertyInterface() {
            return propertyInterface;
        }

        /**
         * This is the java type name as seen in the signature.  Usually, this
         * is the same as getTypeFullClassName(), but can
         * differ if -useInterfaces was set.
         */
        public String getSignatureType(String packageName) {
            if (getPropertyInterface() == null)
                return getTypeFullClassName(packageName);
            else
                return getPropertyInterface();
        }

        public String getTypeFullClassName(String packageName) {
            return getTypeFullClassName(packageName, getType());
        }
        
        public String getTypeFullClassName(String packageName, String typeName) {
            if (isBean && getGraphNode().isCreated() && packageName != null)
                return packageName+"."+typeName;
            return typeName;
        }

        public String getDTDName() {
            return dtdName;
        }

        public String getNamespace() {
            return namespace;
        }

        public BeanBuilder.BeanElement getBeanElement() {
            return beanElement;
        }

        public void setBeanElement(BeanBuilder.BeanElement be) {
            beanElement = be;
        }

        public String instanceOf() {
            String result = java.beans.Introspector.decapitalize(name);
            while (JavaUtil.reservedWord(result))
                result = "a_"+result;
            return result;
        }

        public String beanIntrospectorName() {
            return java.beans.Introspector.decapitalize(name);
        }

        public String getEventName() {/*
            if (isAttribute() && getAttributeOwner() != null &&
                getAttributeOwner() != this) {
                return getAttributeOwner().constName+"@"+getAttrProp().getDtdName();
                } else */
                return name;
        }

        /**
         * @param withIndexedParameter whether or not this getter
         * is going to be called with an index as a parameter.
         * @return the method to be called to do a read.
         */
        public String getReadMethod(boolean withIndexParameter) {
            if (!withIndexParameter && isIndexed())
                return "get"+name;
            else if (isScalar() && Common.isBoolean(type))
                return "is"+name;
            else
                return "get"+name;
        }

        public String getWriteMethod() {
            return "set"+name;
        }

        public String getAddMethod() {
            return "add"+name;
        }

        public String getRemoveMethod() {
            return "remove"+name;
        }

        public String getSizeMethod() {
            return "size"+name;
        }

        public String getScalarIsSet() {
            return "_isSet_"+name;
        }

        public List getMutuallyExclusiveProperties() {
            if (mutuallyExclusiveProperties == null && graphLink != null) {
                // Get the mutually exclusive GraphLink's and extract
                // our properties out of them.
                List mutuallyExclusiveGraphLinks = new ArrayList();
                graphLink.getMutuallyExclusiveLinks(mutuallyExclusiveGraphLinks);
                mutuallyExclusiveProperties = new ArrayList(mutuallyExclusiveGraphLinks.size());
                for (Iterator it = mutuallyExclusiveGraphLinks.iterator(); it.hasNext(); ) {
                    GraphLink l = (GraphLink) it.next();
                    if (l.getObject() != null)
                        mutuallyExclusiveProperties.add(l.getObject());
                }
            }
            return mutuallyExclusiveProperties;
        }

        /*
        public String getOrGroupName() {
            if (orGroupName == null) {
                orGroupName = name;
                for (Iterator it = getMutuallyExclusiveProperties().iterator(); it.hasNext(); ) {
                    Property p = (Property) it.next();
                    orGroupName += p.name;
                }
                for (Iterator it = getMutuallyExclusiveProperties().iterator(); it.hasNext(); ) {
                    Property p = (Property) it.next();
                    p.orGroupName = orGroupName;
                }
            }
            return orGroupName;
        }
        */

        public boolean canBeNull() {
            boolean canBeNull = false;
            boolean isPrimitiveType = JavaUtil.isPrimitiveType(getType());
            if (ored) {
                canBeNull = !isPrimitiveType;
            } else {
                switch (elementInstance & Common.MASK_INSTANCE) {
                case Common.TYPE_0_1:
                case Common.TYPE_1_N:
                case Common.TYPE_0_N:
                    canBeNull = !isPrimitiveType;
                    break;
                }
            }
            // What about group instance?
            return canBeNull;
        }
        
        public Object searchExtraData(Class type) {
            for (Iterator it = extraData.iterator(); it.hasNext(); ) {
                Object o = it.next();
                //System.out.println("searchExtraData: o="+o);
                if (type.isAssignableFrom(o.getClass()))
                    return o;
            }
            return null;
        }

        public Iterator extraDataIterator() {
            return extraData.iterator();
        }
    }

    class PropertyVisitor {
        protected Property curProp;
        protected int propNum;
        protected int propCount;

        public PropertyVisitor() {
        }

        public void generate() throws IOException {
            propCount = attrList.size();
            preGenerate();
            for (propNum = 0; propNum < propCount; ++propNum) {
                curProp = (Property) attrList.get(propNum);
                if (skip())
                    continue;
                preGenerateProp();
                generateProp();
                postGenerateProp();
            }
            postGenerate();
        }

        public void preGenerate() throws IOException {}

        public boolean skip() {
            return false;
        }

        public void preGenerateProp() throws IOException {}

        public void generateProp() throws IOException {}

        public void postGenerateProp() throws IOException {}

        public void postGenerate() throws IOException {}
    }

    class NamePropertyVisitor extends PropertyVisitor {
        protected String nameVar;

        public NamePropertyVisitor(String nameVar) {
            this.nameVar = nameVar;
        }

        public void preGenerate() throws IOException {
            jw.writeEol(nameVar+" = "+nameVar+".intern()");
        }

        public void preGenerateProp() throws IOException {
            if (curProp.name.equals(curProp.dtdName))
                jw.beginIf(nameVar+" == "+curProp.constName);
            else
                jw.beginIf(nameVar+" == "+curProp.constName+" || "+nameVar+" == \""+curProp.dtdName+"\"");
        }

        public void postGenerateProp() throws IOException {
            jw.endElse();
        }

        public void postGenerate() throws IOException {
            genInvalidName(nameVar);
        }
    }

    protected void genInvalidName(String var) throws IOException {
        jw.writeEol("throw new IllegalArgumentException(",
                    var, "+\" is not a valid property name for ",
                    className+"\")");
    }
    
    protected GenBeans.Config 	config;
    protected String  		packageName;
    protected String		className;
    protected String		fullClassName;

    protected BeanBuilder.BeanElement 	beanElement;
    protected BeanBuilder.BeanElement   rootBeanElement;

    private String defaultNamespace = null;

    // the graph of meta-elements
    protected MetaDD			mdd;
    
    // the meta-element for this class
    protected MetaElement		metaElement;
    
    // the set of types we've generated create methods for.
    protected Map generatedNewMethods = new HashMap();	// Map<String, String>

    //	The list of all the attribute of this bean class
    protected ArrayList			attrList;
    
    protected PrefixGuesser prefixGuesser;
    
    //	Current building stream
    protected int		HEADER_SECTION 	= 0;
    protected int		DECL_SECTION	= 1;
    protected int		CONSTRUCTOR_SECTION	= 2;
    protected int		INITIALIZE_SECTION	= 3;
    protected int		ACCESS_SECTION	= 4;
    protected int		NEW_METHOD_SECTION = 5;
    protected int		BODY_SECTION	= 6;
    protected int		EQUALS_SECTION	= 7;
    protected int		HASHCODE_SECTION	= 8;
    protected int		TRAILER_SECTION	= 9;
    protected int		MAXVALUE	= 10;

    JavaWriter jw;

    private Map<String, Boolean> mutableTypes = new HashMap<>();
    private Map<String, Boolean> needToCallClone = new HashMap<>();

    protected boolean isMutableType(Property prop) {
        String type = prop.getType();
        Boolean mutable = mutableTypes.get(type);
        if (mutable == null) {
            if (prop.isBean)
                mutable = Boolean.TRUE;
            else if (prop.isScalar() || JavaUtil.isImmutable(type))
                mutable = Boolean.FALSE;
            else
                mutable = Boolean.TRUE;
            mutableTypes.put(type, mutable);
        }
        return mutable;
    }

    protected boolean isCloneCallNeededOnType(Property prop) {
        String type = prop.getType();
        Boolean callClone = needToCallClone.get(type);
        if (callClone == null) {
            if (prop.isBean || !isMutableType(prop))
                callClone = Boolean.FALSE;
            else
                callClone = JavaUtil.isCloneable(type) ? Boolean.TRUE : Boolean.FALSE;
            needToCallClone.put(type, callClone);
        }
        return callClone;
    }

    protected boolean genCopyWillCopy(Property a) {
        String type = a.getType();
        boolean mutable = isMutableType(a);
        boolean needToCallClone = isCloneCallNeededOnType(a);
        if ((mutable || needToCallClone)
            && !a.isBean && !JavaUtil.isInstantiable(type)) {
            mutable = false;
            needToCallClone = false;
        }
        if (type == "java.io.File") {
            return true;
        } else if (needToCallClone) {
            return true;
        } else if (mutable) {
            return true;
        } else {
            return false;
        }
    }

    protected void genCopy(String src, Property a, String justData) throws IOException {
        String type = a.getType().intern();
        String fullClassType = getTypeFullClassName(a);
        boolean mutable = isMutableType(a);
        boolean needToCallClone = isCloneCallNeededOnType(a);
        if ((mutable || needToCallClone)
            && !a.isBean && !JavaUtil.isInstantiable(type)) {
            mutable = false;
            needToCallClone = false;
        }
        if (type == "java.io.File") {
            jw.write("(", src, " == null) ? null : ");
            gen("new ");
            gen(fullClassType);
            PO();
            jw.write(src);
            jw.write(".getAbsolutePath()");
            PC();
        } else if (needToCallClone) {
            jw.write("(", src, " == null) ? null : ");
            PO();
            gen(fullClassType);
            PC();
            gen(src);
            gen(".clone()");
        } else if (mutable) {
            jw.write("(", src, " == null) ? null : ");
            String castSourceTo = null;
            if (a.isBean && hasDeepCopyConstructor()) {
                castSourceTo = a.getPropertyInterface();
                if (a.getPropertyInterface() != null && !type.equals(a.getPropertyInterface()))
                    jw.write("(", type, ") ");
                jw.write(genNewMethod(a.getPropertyInterface(), type));
            } else {
                gen("new ");
                gen(fullClassType);
            }
            
            PO();
            if (castSourceTo != null)
                jw.write("(", castSourceTo, ") ");
            jw.write(src);
            if (a.isBean) {
                if (config.isGenerateParentRefs()) {
                    jw.write(", this");
                }
                jw.write(", ", justData);
            }
            PC();
        } else {
            jw.write(src);
        }
    }

    protected boolean isTypeQName(String type) {
        type = type.intern();
        if (type == "javax.xml.namespace.QName")
            return true;
        if (type == "org.netbeans.modules.schema2beans.QName")
            return true;
        return false;
    }

    //
    //	Initial values for tabulations. Only header is 0, all other
    //	parts of the class start at one tabulation
    //
    
    protected static final int	   	PUBLIC 		= 0;
    protected static final int		PRIVATE 	= 1;
    protected static final int		VOID 		= 2;
    protected static final int		CLASS 		= 3;
    protected static final int		IMPORT 		= 4;
    protected static final int		PACKAGE    	= 5;
    protected static final int		STRING 		= 6;
    protected static final int		BOOLEAN		= 7;
    protected static final int		STATIC		= 8;
    protected static final int		FINAL		= 9;
    protected static final int		INT			= 10;
    protected static final int		PROTECTED	= 11;
    
    protected static final String arrKeywords[] = {
        "public",
        "private",
        "void",
        "class",
        "import",
        "package",
        "String",
        "boolean",
        "static",
        "final",
        "int",
        "protected"
    };

    protected void init(BeanBuilder.BeanElement be, GenBeans.Config config) {
        this.config = config;
        beanElement = be;
        packageName = null;
        className = be.getClassType();
        fullClassName = className;
        attrList = new ArrayList();
        jw = new JavaWriter();
        HEADER_SECTION = jw.HEADER_SECTION;
        DECL_SECTION = jw.DECL_SECTION;
        CONSTRUCTOR_SECTION = jw.CONSTRUCTOR_SECTION;
        INITIALIZE_SECTION = jw.insertSectionAfter(jw.CONSTRUCTOR_SECTION);
        ACCESS_SECTION = jw.insertSectionAfter(INITIALIZE_SECTION);
        NEW_METHOD_SECTION = jw.insertSectionAfter(ACCESS_SECTION);
        BODY_SECTION = jw.BODY_SECTION;
        EQUALS_SECTION = jw.insertSectionAfter(BODY_SECTION);
        HASHCODE_SECTION = jw.insertSectionAfter(EQUALS_SECTION);
        resetGenBuffers();
    }

    protected void resetGenBuffers() {
        jw.reset();
        jw.select(HEADER_SECTION);
    }

    protected void printGenBuffers(OutputStream out) throws IOException {
        jw.writeTo(out);
    }
    
    public void setPackageName(String n) {
        if (n == null || "".equals(n)) {
            packageName = null;
            fullClassName = className;
        } else {
            packageName = n.replace('/', '.');
            fullClassName = packageName + "." + className;
        }
    }

    public void setDefaultNamespace(String ns) {
        defaultNamespace = ns;
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }
    
    /**
     * @param a is the Property in question
     * @param typeName is the name of the type of @param a, could include "[]"
     * @return Only if @param a is a bean will we add the packageName
     * to it's front.
     */
    protected String getTypeFullClassName(Property a) {
        return a.getTypeFullClassName(packageName);
    }
    
    protected String getTypeFullClassName(Property a, String typeName) {
        return a.getTypeFullClassName(packageName, typeName);
    }

    public void setIndent(String indent) {
        jw.setIndent(indent);
    }

    public void setRootBeanElement(BeanBuilder.BeanElement element) {
        rootBeanElement = element;
    }

    private transient String cachedRootClassName = null;
    protected String getRootClassName() {
        if (cachedRootClassName == null) {
            String rootClassName = rootBeanElement.getClassType();
            if (packageName == null)
                cachedRootClassName = rootClassName;
            else
                cachedRootClassName = packageName + "." + rootClassName;
        }
        return cachedRootClassName;
    }

    /**
     * Once all properties have been added to this Bean, call this method
     * to figure out for attribute property who it's owner is.
     * That is, make Property.getAttributeOwner() work.
     */
    protected void findAttributeOwners() {
        Property attributeOwner;
        int size = attrList.size();
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            if (a.isAttribute()) {
                attributeOwner = a;
                AttrProp attrProp = a.getAttrProp();
            propertyLoop:
                for (int propNum = 0; propNum < size; ++propNum) {
                    Property prop = (Property) attrList.get(propNum);
                    if (prop.attributes == null)
                        continue;
                    for (int attrNum = 0; attrNum < prop.attributes.length; ++attrNum) {
                        AttrProp propAttr = prop.attributes[attrNum];
                        if (propAttr == attrProp) {
                            // Found it!
                            attributeOwner = prop;
                            break propertyLoop;
                        }
                    }
                }
                a.setAttributeOwner(attributeOwner);
                a.setIndexed(attributeOwner.isIndexed());
            }
        }
    }
    
    /**
     * @return null if not found
     */
    protected Property findProperty(AttrProp attr) {
        int size = attrList.size();
        for (int j = 0; j < size; j++) {
            Property p = (Property)attrList.get(j);
            if (p.getAttrProp() == attr) {
                // Found it
                return p;
            }
        }
        return null;
    }

    /*
     * Set of helpers to generate the classe content
     */
    protected void select(int o) {
        jw.select(o);
    }
    
    protected void gen(String s) {
        try {
            jw.write(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void gen(StringBuffer s) {
        try {
            jw.write(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void gen(String s1, String s2) { gen(s1); gen(s2); }
    
    protected void gen(String s1, String s2, String s3) {
        gen(s1); gen(s2); gen(s3);
    }

    protected void gen(int a)	{ gen(arrKeywords[a]); }
    
    protected void gen(int a, String s) { gen(a); sp(); gen(s); }
    
    protected void gen(int a, String s, String s2) { gen(a, s); sp(); gen(s2); }
    
    protected void gen(int a, int b, String s, String s2) {
        gen(a, b, s); gen(s2);
    }
    
    protected void gen(int a, int b, String s) {
        gen(a); sp(); gen(b); sp(); gen(s);
    }
    
    protected void gen(int a, int b, int c, String s) {
        gen(a); sp(); gen(b); sp(); gen(c); sp(); gen(s);
    }
    
    protected void gen(int a, int b, int c, int d, String s) {
        gen(a); sp(); gen(b); sp(); gen(c); sp(); gen(d); sp(); gen(s);
    }
    
    protected void gencr(String s) { gen(s); cr(); }
    
    protected void gencr(String s, String s2) { gen(s, s2); cr(); }
    
    protected void gencr(int a, String s) { gen(a, s); cr(); }
    
    protected void gencr(int a, String s, String s2) { gen(a, s, s2); cr(); }
    
    protected void gencr(int a, int b, String s, String s2) {
        gen(a, b, s, s2); cr();
    }
    
    protected void gencr(int a, int b, String s) { gen(a, b, s); cr(); }
    
    protected void gencr(int a, int b, int c, String s) {gen(a, b, c, s); cr();}
    
    protected void get(String s) {gen(" get"); gen(s); PO(); PC();}
    protected void geti(String s) {gen(" get"); gen(s, "(int index)");}
    protected void getidx(String s, boolean b) {if (b) geti(s); else get(s); }
    
    protected void cr() {
        try {
            jw.cr();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void geneol(String s) { gen(s); eol(); }

    protected void PO() {	gen("("); }
    
    protected void PC() {	gen(")"); }
    
    protected void begin() {
        try {
            jw.begin();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void end() { end(true); };
    
    protected void end(boolean cr) {
        try {
            jw.end(cr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void comment() { gencr("//"); }

    protected void gencrNoI18N(String s) {gen(s); noI18N();}
    protected void gencrNoI18N(String s, String s2) {gen(s); gen(s2); noI18N();}

    protected void beginNoI18N() { gencr("// BEGIN" + "_NOI18N"); }
    protected void endNoI18N() { gencr("// END" + "_NOI18N"); }
    protected void noI18N()  {
        try {
            jw.noI18N();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void comment(String s) {
        try {
            jw.comment(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void comment(String s, String s2) { comment(s + s2); }
    
    protected void eol() {
        try {
            jw.eol();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void eol(boolean cr) { gen(";"); if (cr) cr(); }
    
    protected void eolNoI18N() {
        try {
            jw.eolNoI18N();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void sp() { gen(" "); }
    
    protected void genforprop() {gencr("for (int i=0; i<this.prop.length; i++)");}
    protected void gengetprop() {
        gencr("BeanProp p = this.beanProp();");
        gencr("if (p != null)"); }
    
    protected void gengetpropbyname() {
        gencr("BeanProp p = this.beanProp(n);");
        gencr("if (p != null)"); }
    
    protected void gensig1(String s) {gen("(", s, " l)"); cr();}
    
    protected void gensig2(String s) {gen("(String n, ", s, " l)"); cr();}

    protected void tabIn() { jw.indentOneLevel(); }

    protected void gentab(int n) {for(int i=0; i<n; i++) tabIn();}
    
    protected void genSetValue(boolean isScalar, String classType) {
        gen(JavaUtil.toObject("value", classType, config.isForME(), config.isJava5()));
        gen(")");
    }

    protected void genSetValP(boolean b, String s1, String s2) {
        if(b) gen(s1); else gen(s2); gen(" value"); }
    
    /**
     *	This method is called by the BeanBuilder to register a new property
     *	on this bean class.
     */
    public Property addProperty(String name, String dtdName, String namespace,
                                GraphNode node,
                                GraphLink l, String classType, int nestedLevel,
                                int eltInstance, int groupInstance,
                                int type, boolean ored,
                                AttrProp[] propAttributes,
                                String constName, String defaultValue,
                                boolean directChild, List extraData,
                                boolean isUnion) {
        if (config.isTraceGen()) {
            config.messageOut.println("AbstractCodeGen.addProperty: name="+name+" dtdName="+dtdName+" classType="+classType+" type="+type+" eltInstance="+eltInstance+" groupInstance="+groupInstance+" isUnion="+isUnion);
            /*
            if (propAttributes != null)
                for (int i = 0; i < propAttributes.length; ++i)
                    System.out.println("   propAttributes["+i+"]="+propAttributes[i]);
            */
        }
        Property attr = new Property(name, dtdName, namespace,
                                     node, l, classType,
                                     eltInstance,
                                     groupInstance, nestedLevel, type,
                                     ored, propAttributes, constName,
                                     defaultValue, directChild, extraData, isUnion);
        attrList.add(attr);
        return attr;
    }
    
    public List/*<AbstractCodeGeneratorClass.Property>*/ getPropertyList() {
        return attrList;
    }

    /**
     *	Generate the java code in the out stream, using the optional
     *	metaDD bean graph.
     */
    public void generate(String filename, MetaDD mdd) throws IOException {
        this.mdd = mdd;
        if (className == null)
            throw new NullPointerException();
        metaElement = getMetaElement(beanElement);
	
        if (metaElement != null && metaElement.isSkipGeneration()) {
            config.messageOut.println("Skipping generation of class " + filename
                                      + " (as specified in the mdd file)");	// NOI18N
            return;
        }

        config.messageOut.println("Generating class " + filename);  // NOI18N
        OutputStream out = new FileOutputStream(filename);
        try {
            generate(out, mdd);
        } finally {
            out.close();
        }
    }

    public abstract void generate(OutputStream out, MetaDD mdd) throws IOException;

    protected MetaElement getMetaElement(BeanBuilder.BeanElement be) {
        return getMetaElement(be.getDTDName(), be.getNamespace());
    }
    
    protected MetaElement getMetaElement(Property prop) {
        BeanBuilder.BeanElement be = prop.getBeanElement();
        if (be != null)
            return getMetaElement(be);
        return getMetaElement(prop.getDTDName(), prop.getNamespace());
    }

    protected MetaElement getMetaElement(String dtdName, String namespace) {
        if (mdd == null)
            return null;
        int size = mdd.sizeMetaElement();
        for (int i=0; i<size; i++) {
            MetaElement e = mdd.getMetaElement(i);
            if (e == null)
                continue;
            if (namespace != null && !namespace.equals(e.getNamespace()))
                continue;
            if (e.getDtdName().equals(dtdName))
                return e;
        }
        return null;
    }

    protected MetaProperty getMetaProperty(Property prop) {
        return getMetaProperty(prop.name);
    }
    
    //	Return the meta property from the current meta element
    protected MetaProperty getMetaProperty(String name) {
        MetaProperty ret = null;
        //	Get the extra information we might have about this bean
        if (metaElement != null) {
            MetaProperty[] mp = this.metaElement.getMetaProperty();
            for (int i=0; i<mp.length; i++) {
                if (mp[i] != null && mp[i].getBeanName().equals(name)) {
                    ret = mp[i];
                    break;
                }
            }
        }
        return ret;
    }
    
    /**
     * Send the schema to the current output channel.
     */
    protected void printSchema() throws IOException {
        if (config.getFilename() == null) {
            return;
        }
	File f = config.getFilename();
        if (f.length() < 16384L) {
            FileInputStream fi = new FileInputStream(f);
            try {
                byte[] r = new byte[(int)f.length()];
                fi.read(r);
                cr(); gencr("/*"); gentab(2);
                gencr("The following schema file has been used for generation:");
                cr();
                gen(new String(r));
                cr();
                gencr("*/");
            } finally {
                fi.close();
            }
        }
    }

    protected void printComment(String indent) throws IOException {
        String comment = (String) beanElement.node.getExtendedProperty("comment");
        if (comment == null)
            return;
        gen(indent);
        gencr("===============================================================");
        gen(indent);
        // need to escape any special chars
        int length = comment.length();
        for (int i = 0; i < length; ++i) {
            char c = comment.charAt(i);
            if (c == '\n') {
                cr();
                gen(indent);
            } else if (c == '*' && i+1 < length && comment.charAt(i+1) == '/') {
                jw.write("* /");
                ++i;
            } else {
                jw.write(c);
            }
        }
        cr();
        gen(indent);
        gencr("===============================================================");
    }
    
    public void dumpBeanTree(java.io.Writer out, String indent, String indentBy) throws java.io.IOException {
        out.write(indent);
        out.write(java.beans.Introspector.decapitalize(Common.convertName(beanElement.node.getName())));
        out.write(" <");
        out.write(beanElement.node.getName());
        out.write(">");
        out.write(" : ");
        out.write(className);
        if (!beanElement.node.getExtraData().isEmpty()) {
            out.write(" \t");
            out.write(beanElement.node.getExtraData().toString());
        }
        out.write("\n");
        StringBuffer str = new StringBuffer();
        int lineCount = 1;
        lineCount = dumpAttributes(beanElement.getGraphNode(), str, indent+indentBy, lineCount);
        lineCount = dumpBeanTree(beanElement, str,
                 indent+indentBy, indentBy, lineCount);
        out.write(str.toString());
        if (lineCount >= MAX_DUMP_TREE_LINE_COUNT) {
            out.write(indent);
            out.write("... etc ...\n");
        }
    }
    
    protected int dumpBeanTree(BeanBuilder.BeanElement be, StringBuffer str,
            String indent, String indentBy, int lineCount) {
        lineCount = beanTree(be.getGraphNode().getGraphLink(), str,
                 indent, indentBy, lineCount);
        if (be.getExtension() != null) {
            //lineCount = dumpAttributes(be.getExtension().getGraphNode(), str, indent, lineCount);
            lineCount = dumpBeanTree(be.getExtension(), str, indent, indentBy, lineCount);
        }
        return lineCount;
    }

    protected int dumpAttributes(GraphNode elt, StringBuffer str, String indent, int lineCount) {
        AttrProp[] attrList = elt.getAttributes();
	
        for (int i = 0; i < attrList.length; i++) {
            AttrProp attr = attrList[i];
            str.append(indent + "[attr: " + attr  + "]");
            if (!attr.getExtraData().isEmpty()) {
                str.append(" \t");
                str.append(attr.getExtraData().toString());
            }
            str.append("\n");
            ++lineCount;
        }
        if (false && elt.getExtension() != null)
            lineCount = dumpAttributes(elt.getExtension(), str, indent, lineCount);
        return lineCount;
    }
    
    protected int beanTree(GraphLink l, StringBuffer str,
                            String indent, String indentBy, int lineCount) {
        if (lineCount >= MAX_DUMP_TREE_LINE_COUNT)
            return lineCount;  // Enough, already.
        for (; l != null; l = l.getSibling()) {
            //str.append("l="+l+"\n");
            //str.append("l.object="+l.getObject()+"\n");
            //str.append("l.lastInGroup="+l.getLastInGroup()+"\n");
            String nextIndent = indent + indentBy;
            if (l.element != null) {
                BeanBuilder.BeanElement be =
                    (BeanBuilder.BeanElement)l.element.getObject();
		
                if (be == null)
                    continue;
                Property prop = (Property) l.getObject();
                String type = be.getClassType();
                String name = l.name;
                if ("#PCDATA".equals(l.name) && "String".equals(type))
                    continue;
                if (prop != null) {
                    type = prop.getType();
                    if ("EMPTY".equals(prop.dtdName))
                        name = prop.dtdName;
                    else
                        name = prop.beanIntrospectorName() + " <"+prop.dtdName+">";
                }
			
                str.append(indent);
                str.append(name);
                str.append(" : ");
                str.append(type);
                str.append(TreeBuilder.instanceToString(l.getElementInstance(),
                                                        true));
                if (l.element.getMarked())
                    str.append("...");
                if (!l.extraData.isEmpty()) {
                    str.append(" \t");
                    str.append(l.extraData.toString());
                }
                if (!be.node.getExtraData().isEmpty()) {
                    str.append(" \t");
                    str.append(be.node.getExtraData().toString());
                }
                str.append("\n");
                ++lineCount;
                
                if (config.isRespectExtension() && be.getExtension() != null)
                    lineCount = dumpBeanTree(be.getExtension(),
                            str, nextIndent, indentBy, lineCount);
		    
                lineCount = this.dumpAttributes(l.element, str, nextIndent, lineCount);
		    
                if (l.element.getGraphLink() != null) {
                    if (l.element.getMarked() == false) {
                        l.element.setMarked(true);
                        lineCount =
                            beanTree(l.element.getGraphLink(), str, nextIndent +
                                 TreeBuilder.instanceToString(l.getGroupInstance(),
                                                              true) +
                                 (l.isSequenceOr() ? "| " : ""), indentBy, lineCount);
                        l.element.setMarked(false);
                    }
                }
            }

            String groupInstance = TreeBuilder.instanceToString(l.getGroupInstance(), true);
            if (!"".equals(groupInstance)) {
                str.append(indent);
                str.append("(\n");
                ++lineCount;
            }
            if (l.getFirstChild() != null) {
                lineCount = beanTree(l.getFirstChild(), str, indent + (("".equals(groupInstance)) ? "" : "  ") + (l.isSequenceOr() ? "| " : ""), indentBy, lineCount);
            }
            if (!"".equals(groupInstance)) {
                str.append(indent);
                str.append(")");
                str.append(groupInstance);
                str.append("\n");
                ++lineCount;
            }
        }
        return lineCount;
    }

    public String toString() {
        if (packageName == null)
            return "Class " + className;
        else
            return "Class " + packageName + "." + className;
    }

    protected void gen(Signature sig) throws IOException {
        sig.writeMethod(jw);
    }

    public static final int MAX_DUMP_TREE_LINE_COUNT = 512;

    protected static final Signature.TypeOfMethod SETTER = Signature.TypeOfMethod.SETTER;
    protected static final Signature.TypeOfMethod SETTERINDEXED = Signature.TypeOfMethod.SETTERINDEXED;
    protected static final Signature.TypeOfMethod GETTER = Signature.TypeOfMethod.GETTER;
    protected static final Signature.TypeOfMethod GETTERLIST = Signature.TypeOfMethod.GETTERLIST;
    protected static final Signature.TypeOfMethod GETTERINDEXED = Signature.TypeOfMethod.GETTERINDEXED;
    protected static final Signature.TypeOfMethod SIZE = Signature.TypeOfMethod.SIZE;
    protected static final Signature.TypeOfMethod ADD = Signature.TypeOfMethod.ADD;
    protected static final Signature.TypeOfMethod REMOVE = Signature.TypeOfMethod.REMOVE;
    protected static final Signature.TypeOfMethod OTHER = Signature.TypeOfMethod.OTHER;

    protected static class Signature {
        public static class TypeOfMethod {
            private final String name;
            private TypeOfMethod(String name) { this.name = name; }
            public String toString() { return "TypeOfMethod "+name; }
            public static final TypeOfMethod SETTER = new TypeOfMethod("setter");
            public static final TypeOfMethod SETTERINDEXED = new TypeOfMethod("setterindexed");
            public static final TypeOfMethod GETTER = new TypeOfMethod("getter");
            public static final TypeOfMethod GETTERLIST = new TypeOfMethod("getterlist");
            public static final TypeOfMethod GETTERINDEXED = new TypeOfMethod("getterindexed");
            public static final TypeOfMethod SIZE = new TypeOfMethod("size");
            public static final TypeOfMethod ADD = new TypeOfMethod("add");
            public static final TypeOfMethod REMOVE = new TypeOfMethod("remove");
            public static final TypeOfMethod OTHER = new TypeOfMethod("other");
        }

        private String returnType;
        private String methodName;
        private TypeOfMethod methodType;
        private List parameterTypes;
        private List parameterNames;
        private List throwTypes;
        private boolean _isStatic;

        public Signature(String returnType, String methodName, TypeOfMethod methodType) {
            this.returnType = returnType;
            this.methodName = methodName;
            this.methodType = methodType;
            this.parameterTypes = new LinkedList();
            this.parameterNames = new LinkedList();
            this.throwTypes = new LinkedList();
            this._isStatic = false;
        }

        public Signature(String returnType, String methodName) {
            this.returnType = returnType;
            this.methodName = methodName;
            this.methodType = OTHER;
            this.parameterTypes = new LinkedList();
            this.parameterNames = new LinkedList();
            this.throwTypes = new LinkedList();
            this._isStatic = false;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getReturnType() {
            return returnType;
        }

        public TypeOfMethod getMethodType() {
            return methodType;
        }

        public boolean isStatic() {
            return _isStatic;
        }

        public void setStatic() {
            _isStatic = true;
        }

        public boolean isVoidReturnType() {
            return "void".equals(returnType);
        }

        public void addParameter(String type, String name) {
            parameterTypes.add(type);
            parameterNames.add(name);
        }

        public void addThrows(String type) {
            throwTypes.add(type);
        }

        public void writeMethod(JavaWriter jw) throws IOException {
            StringBuffer parameters = new StringBuffer();
            Iterator itTypes = parameterTypes.iterator();
            Iterator itNames = parameterNames.iterator();
            boolean first = true;
            while (itTypes.hasNext() && itNames.hasNext()) {
                if (first)
                    first = false;
                else
                    parameters.append(", ");
                parameters.append((String) itTypes.next());
                parameters.append(" ");
                parameters.append((String) itNames.next());
            }
            String exceptions = null;
            Iterator itThrows = throwTypes.iterator();
            if (itThrows.hasNext()) {
                exceptions = "";
                first = true;
                do {
                    if (first)
                        first = false;
                    else
                        exceptions += ", ";
                    exceptions += ((String) itThrows.next());
                } while (itThrows.hasNext());
            }
            int options = jw.PUBLIC | jw.BEANINFO;
            if (_isStatic)
                options |= jw.STATIC;
            jw.writeMethod(methodName, parameters.toString(), exceptions,
                           returnType, options);
        }

        /**
         *  int sizeFoo()
         *  int addFoo(Foo value)
         */
        public String toString() {
            StringBuffer str = new StringBuffer();
            if (_isStatic)
                str.append("static ");
            str.append(returnType);
            str.append(" ");
            str.append(methodName);
            str.append("(");
            Iterator itTypes = parameterTypes.iterator();
            Iterator itNames = parameterNames.iterator();
            boolean first = true;
            while (itTypes.hasNext() && itNames.hasNext()) {
                if (first)
                    first = false;
                else
                    str.append(", ");
                str.append((String) itTypes.next());
                str.append(" ");
                str.append((String) itNames.next());
            }
            str.append(")");
            Iterator itThrows = throwTypes.iterator();
            if (itThrows.hasNext()) {
                str.append(" throws ");
                first = true;
                do {
                if (first)
                    first = false;
                else
                    str.append(", ");
                str.append((String) itThrows.next());
                } while (itThrows.hasNext());
            }
            return str.toString();
        }

        /**
         * Generate what's needed to call the method.
         * sizeFoo()
         * addFoo(value)
         */
        public String callMethod() {
            StringBuffer str = new StringBuffer();
            str.append(methodName);
            str.append("(");
            Iterator itTypes = parameterTypes.iterator();
            Iterator itNames = parameterNames.iterator();
            boolean first = true;
            while (itTypes.hasNext() && itNames.hasNext()) {
                if (first)
                    first = false;
                else
                    str.append(", ");
                str.append((String) itNames.next());
            }
            str.append(")");
            return str.toString();
        }
    }

    protected static class Signatures {
        Map signatureTable;
        List others;

        public Signatures() {
            signatureTable = new HashMap();
            others = new LinkedList();
        }

        public void add(Signature sig) {
            if (sig.getMethodType() == OTHER)
                others.add(sig);
            else
                signatureTable.put(sig.getMethodType(), sig);
        }

        public Signature findSignature(Signature.TypeOfMethod methodType) {
            Signature sig = null;
            if (methodType == OTHER) {
                Iterator it = others.iterator();
                if (it.hasNext())
                    sig = (Signature) it.next();
            } else
                sig = (Signature) signatureTable.get(methodType);
            if (sig == null)
                throw new IllegalStateException("Unable to find "+methodType);
            return sig;
        }

        public Iterator iterator() {
            return new SignaturesIterator();
            //return signatureTable.values().iterator();
        }

        public class SignaturesIterator implements Iterator {
            Iterator tableIterator;
            Iterator othersIterator;

            public SignaturesIterator() {
                tableIterator = signatureTable.values().iterator();
                othersIterator = others.iterator();
            }
            
            public boolean hasNext() {
                if (tableIterator.hasNext())
                    return true;
                return othersIterator.hasNext();
            }

            public Object next() {
                if (tableIterator.hasNext())
                    return tableIterator.next();
                return othersIterator.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }
    }

    protected Signatures getSignatures(Property a) {
        Signatures result = new Signatures();
        Signature sig;

        boolean indexed = a.isIndexed();
        boolean isScalar = a.isScalar();
        String type = getTypeFullClassName(a);
        String typeVariable = "value";
        //System.out.println("a.name="+a.name+" a.dtdName="+a.dtdName+" a.type="+a.getType());
        if (a.isBean && config.isUseInterfaces()) {
            MetaElement me = getMetaElement(a);
            if (me != null) {
                String firstInterface = firstInCommaSeparatedList(me.getImplements());
                if (firstInterface != null &&
                    !firstInterface.equals(config.getGenerateCommonInterface()) &&
                    !firstInterface.equals(packageName+"."+config.getGenerateCommonInterface())) {
                    type = firstInterface;
                    typeVariable = "valueInterface";
                    a.setPropertyInterface(firstInterface);
                }
            }
        }
        String baseType = type;
        if (indexed)
            type = baseType + "[]";

        // the setter
        sig = new Signature("void", "set"+a.name, SETTER);
        sig.addParameter(type, typeVariable);
        if (config.isVetoable())
            sig.addThrows("java.beans.PropertyVetoException");
        result.add(sig);
        if (indexed) {
            sig = new Signature("void", "set"+a.name, SETTERINDEXED);
            sig.addParameter("int", "index");
            sig.addParameter(baseType, typeVariable);
            if (config.isVetoable())
                sig.addThrows("java.beans.PropertyVetoException");
            result.add(sig);
        }

        // the getter
        String getterName = a.getReadMethod(false);
        result.add(new Signature(type, getterName, GETTER));
        if (indexed) {
            String returnType = "java.util.List";
            if (config.jdkTarget >= 150)
                returnType += "<"+JavaUtil.toObjectType(baseType)+">";
            result.add(new Signature(returnType, "fetch"+a.name+"List", GETTERLIST));
            sig = new Signature(baseType, a.getReadMethod(true), GETTERINDEXED);
            sig.addParameter("int", "index");
            result.add(sig);
            result.add(new Signature("int", "size"+a.name, SIZE));
            sig = new Signature("int", "add"+a.name, ADD);
            sig.addParameter(baseType, typeVariable);
            if (config.isVetoable())
                sig.addThrows("java.beans.PropertyVetoException");
            result.add(sig);
            sig = new Signature("int", "remove"+a.name, REMOVE);
            sig.addParameter(baseType, typeVariable);
            if (config.isVetoable())
                sig.addThrows("java.beans.PropertyVetoException");
            result.add(sig);
        }

        return result;
    }

    public void generateDelegator(OutputStream out, MetaDD mdd,
                                  String delegatorClassName,
                                  String delegatorPackageName) throws IOException {
        Collection generatedMethods = jw.getStoredMethods();
        resetGenBuffers();
        
        select(HEADER_SECTION);
        jw.bigComment("Delegate for "+className+"\n\n@"+Common.GENERATED_TAG);
        jw.cr();
        if (delegatorPackageName != null) {
            jw.writePackage(delegatorPackageName);
            cr();
        }
        gen(PUBLIC, CLASS, delegatorClassName);
        if (metaElement.getDelegatorExtends() != null) {
            jw.write(" extends ");
            jw.write(metaElement.getDelegatorExtends());
        }
        if (config.getGenerateCommonInterface() != null) {
            jw.write(" implements ");
            jw.write(config.getGenerateCommonInterface());
        }
        sp();
        begin();

        select(DECL_SECTION);
        String delegator = "_"+className;	// NOI18N
        gen(PROTECTED, fullClassName, delegator);
        eol();
        cr();

        generateDelegatorConstructors(delegatorClassName);

        List sortedMethods = new LinkedList(generatedMethods);
        Collections.sort(sortedMethods);
        for (Iterator it = sortedMethods.iterator(); it.hasNext(); ) {
            JavaWriter.Method method = (JavaWriter.Method) it.next();
            if (method.isPublic())
                generateDelegator(method, delegatorClassName);
        }

        select(TRAILER_SECTION);
        end();

        printGenBuffers(out);
    }

    protected void generateDelegatorConstructors(String delegatorClassName) throws IOException {
        select(CONSTRUCTOR_SECTION);
        String delegator = "_"+className;	// NOI18N
        gen(PUBLIC, delegatorClassName);
        PO();
        gen(fullClassName);
        gen(" delegator");
        PC(); sp();
        begin();
        geneol(delegator+" = delegator");
        end();
        cr();
    }

    protected void generateDelegator(JavaWriter.Method method, String delegatorClassName) throws IOException {
        if ("".equals(method.getReturnType())) {
            if (method.getParameters().indexOf(',') < 0 &&
                (method.getParameters().startsWith(className+" ") ||
                 method.getParameters().startsWith(fullClassName+" "))) {
                // We already have a constructor with that parameter
                return;
            }
            select(CONSTRUCTOR_SECTION);
            String delegator = "_"+className;	// NOI18N
            jw.write("public ");
            jw.write(delegatorClassName);
            jw.write("(");
            jw.write(method.getParameters());
            jw.write(") ");
            if (method.getExceptions() != null) {
                jw.write("throws ", method.getExceptions(), " ");
            }
            jw.begin();
            jw.write(delegator, " = new ", fullClassName);
            jw.write("(");
            method.writeParametersNoTypes(jw);
            jw.writeEol(")");
            jw.end();
            jw.cr();
            return;
        }
        select(BODY_SECTION);
        boolean returnsDelegation = false;
        if (className.equals(method.getReturnType()) ||
            fullClassName.equals(method.getReturnType())) {
            returnsDelegation = true;
        }
        jw.beginMethod(method.getName(), method.getParameters(),
                       method.getExceptions(),
                       returnsDelegation ? delegatorClassName : method.getReturnType(),
                       method.getOptions());
        if (!"void".equals(method.getReturnType())) {
            gen("return ");
        }
        if (returnsDelegation)
            jw.write("new ", delegatorClassName, "(");
        if (!method.isStatic())
            jw.write("_", className);
        else
            jw.write(fullClassName);
        jw.write(".");
        method.writeCall(jw);
        if (returnsDelegation)
            jw.write(")");
        jw.eol();
        jw.end();
        jw.cr();
    }

    /**
     * @param var The name of a variable.
     */
    protected void genWhiteSpaceRestriction(SchemaRep.WhiteSpace ws, String var,
                                            String type) throws IOException {
        if (!("String".equals(type) || "java.lang.String".equals(type)))
            return;
        if (ws.isPreserve())
            return;
        if (ws.isReplace()) {
            jw.beginIf(var+" != null");
            jw.comment("Whitespace Replace due to whitespace restriction.");
            jw.writeEol(var+" = "+var+".replace('\\n', ' ').replace('\\t', ' ')");
            jw.end();
        } else if (ws.isCollapse()) {
            jw.beginIf(var+" != null");
            jw.comment("Whitespace Collapse due to whitespace restriction.");
            jw.writeEol(var+" = "+var+".replace('\\n', ' ').replace('\\t', ' ').trim()");
            jw.beginFor("int pos = "+var+".indexOf(\"  \")", "pos >= 0",
                        "pos = "+var+".indexOf(\"  \", pos)");
            jw.writeEol(var+" = "+var+".substring(0, pos) + "+var+".substring(pos+1, "+var+".length())");
            jw.end();
            jw.end();
        }
    }

    protected void genRethrowExceptions(List exceps) {
        for (Iterator it = exceps.iterator(); it.hasNext(); ) {
            gen("catch (");
            gen((String) it.next());
            gen(" e) ");
            begin();
            geneol("throw new java.lang.RuntimeException(e)");
            end();
        }
    }

    protected void genValidateProperties() throws IOException {
        jw.writeEol("boolean restrictionFailure = false");
        jw.writeEol("boolean restrictionPassed = false");
        Set requiredPropertySet = new HashSet();
        Map requiredProperty = new HashMap();
        int size = attrList.size();
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            boolean indexed = a.isIndexed();
            String attr = a.getReadMethod(false)+"()";
            String type = a.getType().intern();
            boolean mightBeNull = a.canBeNull();
            boolean isPrimitiveType = JavaUtil.isPrimitiveType(type);
            String propertyName = a.beanIntrospectorName();
            boolean hasDataEnumRestriction = false;
            String enumArray = "enumRestriction"+a.name;
            //boolean checkOrCount = a.ored && !isPrimitiveType;
            boolean checkOrCount = a.ored;
            if (checkOrCount) {
                //jw.comment("a.groupInstance="+a.getGroupInstance());
                // If there can be zero of them, then don't check the or.
                if (a.getGroupInstance() == Common.TYPE_0_N ||
                    a.getGroupInstance() == Common.TYPE_0_1) {
                    checkOrCount = false;
                }
            }
            jw.comment("Validating property "+propertyName);
            //jw.writeEol("System.out.println(\"Validating property "+propertyName+"\")");
            if (a.isUnion())
                jw.writeEol("restrictionPassed = false");
            if (a.ored) {
            } else {
                switch (a.elementInstance & Common.MASK_INSTANCE) {
                case Common.TYPE_1:
                    if (isPrimitiveType) {
                        // There is always one of them (no null).
                        break;
                    }
                    if (a.isNillable())
                        break;
                    genValidateIf(attr+" == null", propertyName,
                                  ValidateException.FailureType.NULL_VALUE,
                                  jw);
                    break;
                case Common.TYPE_0_1:
                    break;
                case Common.TYPE_0_N:
                    // Never a problem.
                    break;
                case Common.TYPE_1_N:
                    genValidateIf("size"+a.name+"() == 0", propertyName,
                                  ValidateException.FailureType.NULL_VALUE, jw);
                    break;
                }
            }
            if (indexed && a.isAttribute())
                continue;
            // Test children
            int markedPosition = -1;
            if (indexed) {
                for (Iterator it = a.extraDataIterator(); it.hasNext(); ) {
                    Object extraData = it.next();
                    if (extraData instanceof DataListRestriction) {
                        DataListRestriction dlr = (DataListRestriction) extraData;
                        dlr.genRestriction(jw, "size"+a.name+"()", a.getReadMethod(false), type, "restrictionFailure", false);
                        jw.beginIf("restrictionFailure");
                        genValidateFail(JavaUtil.instanceFrom("java.lang.String", attr+" "+dlr.toString()),
                                        propertyName, false,
                                        ValidateException.FailureType.DATA_RESTRICTION,
                                        jw);
                        jw.end();
                    }
                }
                markedPosition = jw.getCurrentPosition();
                jw.beginFor("int _index = 0",
                            "_index < size"+a.name+"()", "++_index");
                jw.write(getTypeFullClassName(a));
                jw.write(" element = ");
                if (a.getPropertyInterface() != null) {
                    jw.write("(", getTypeFullClassName(a), ") ");
                }
                jw.writeEol(a.getReadMethod(true)+"(_index)");
                attr = "element";
            }
            if (mightBeNull) {
                if (markedPosition == -1)
                    markedPosition = jw.getCurrentPosition();
                jw.beginIf(attr+" != null");
            }

            int marked2Position = jw.getCurrentPosition();
            if (a.isBean) {
                if (a.getPropertyInterface() == null) {
                    if (!a.getGraphNode().isCreated()) {
                        jw.beginTry();
                        jw.comment("Catch and deal with any foreign validate exceptions.");
                    }
                    jw.writeEol(attr, ".validate()");
                    if (!a.getGraphNode().isCreated()) {
                        jw.endCatch("java.lang.Exception e");
                        jw.writeEol("throw new java.lang.RuntimeException(e)");
                        jw.end();
                    }
                } else
                    jw.writeEol("(("+type+")", attr, ").validate()");
            }
            genForRestrictions(a, attr);
            //System.out.println("  a.name="+a.name);
            for (Iterator it = a.extraDataIterator(); it.hasNext(); ) {
                Object extraData = it.next();
                //System.out.println("  Found extraData="+extraData);
                //jw.comment("name="+a.name+" Found extraData="+extraData);
                if (extraData instanceof DataTypeRestriction) {
                    DataTypeRestriction dtr = (DataTypeRestriction) extraData;
                    dtr.genRestriction(jw, attr, type, "restrictionFailure", false);
                    jw.beginIf("restrictionFailure");
                    genValidateFail(JavaUtil.instanceFrom("java.lang.String", attr+" "+dtr.toString()),
                                    propertyName, false,
                                    ValidateException.FailureType.DATA_RESTRICTION,
                                    jw);
                    jw.end();
                }
            }
            for (Iterator it = a.extraDataIterator(); it.hasNext(); ) {
                Object extraData = it.next();
                if (extraData instanceof DataEnumRestriction) {
                    DataEnumRestriction der = (DataEnumRestriction) extraData;
                    if (!hasDataEnumRestriction) {
                        // first one
                        hasDataEnumRestriction = true;
                        jw.write("final "+type+"[] "+enumArray+" = {");
                    } else {
                        jw.write(", ");
                    }
                    der.genRestriction(jw, type);
                }
            }
            if (hasDataEnumRestriction) {
                jw.writeEol("}");
                jw.writeEol("restrictionFailure = true");
                jw.beginFor("int _index2 = 0", "_index2 < "+enumArray+".length",
                            "++_index2");
                jw.beginIf(JavaUtil.genEquals(type, enumArray+"[_index2]",
                                              attr, false));
                jw.writeEol("restrictionFailure = false");
                jw.writeEol("break");
                jw.end();
                jw.end();
                jw.beginIf("restrictionFailure");
                genValidateFail(JavaUtil.instanceFrom("java.lang.String", attr+" enumeration test"),
                                propertyName, false,
                                ValidateException.FailureType.ENUM_RESTRICTION,
                                jw);
                jw.end();
            }

            if (marked2Position != jw.getCurrentPosition()) {
                if (mightBeNull) {
                    jw.end();
                }
                if (indexed) {
                    jw.end();
                }
            } else {
                if (markedPosition >= 0) {
                    // We didn't write anything since marked2Position, which
                    // means that we didn't need the stuff after markedPosition
                    jw.truncateAtPosition(markedPosition);
                }
                if (mightBeNull)
                    jw.indentLeft();
                if (indexed)
                    jw.indentLeft();
            }

            if (checkOrCount) {
                GraphLink gl = a.getGraphLink();
                //jw.comment("checkOrCount: gl="+gl);
                GraphLink parentgl = null;
                if (gl != null) {
                    parentgl = gl.getParent();
                }
                //if (parentgl == null || parentgl.getGroupInstance() == Common.TYPE_1) {
                List requiredPropertyTest = new ArrayList();
                jw.beginIf(testIfPropertySet(a));
                requiredPropertyTest.add(testIfPropertyNotSet(a));
                for (Iterator it = a.getMutuallyExclusiveProperties().iterator(); it.hasNext(); ) {
                    Property prop = (Property) it.next();
                    jw.beginIf(testIfPropertySet(prop));
                    requiredPropertyTest.add(testIfPropertyNotSet(prop));
                    genValidateFail("mutually exclusive properties: "+a.name+" and "+prop.name,
                                    prop.name, true,
                                    ValidateException.FailureType.MUTUALLY_EXCLUSIVE,
                                    jw);
                    jw.end();
                }
                jw.end();

                //
                // This is preparing for the check that at least 1
                // of the properties are set.
                // Sort our list of property checks and store the
                // if expression into a Set, so that we generate
                // only one if per group of properties.
                //
                Collections.sort(requiredPropertyTest);
                StringBuffer requiredExpr = new StringBuffer();
                boolean first = true;
                for (Iterator it = requiredPropertyTest.iterator(); it.hasNext(); ) {
                    if (first)
                        first = false;
                    else
                        requiredExpr.append(" && ");
                    requiredExpr.append((String) it.next());
                }
                String requiredExprString = requiredExpr.toString();
                requiredPropertySet.add(requiredExprString);
                requiredProperty.put(requiredExprString, a);
            }
        }
        for (Iterator it = requiredPropertySet.iterator(); it.hasNext(); ) {
            String required = (String) it.next();
            Property prop = (Property) requiredProperty.get(required);
            jw.beginIf(required);
            genValidateFail("required properties: "+required,
                            prop.name, true,
                            ValidateException.FailureType.NULL_VALUE,
                            jw);
            jw.end();
        }
    }

    protected void genForRestrictions(Property a, String attr)
            throws IOException {
        boolean isUnion = a.isUnion();
        String propertyName = a.beanIntrospectorName();
        String enumArray = "enumRestriction"+a.name;
        String type = a.getType().intern();
        SchemaRep.Restriction restrict = null;
        for (Iterator it = a.extraDataIterator(); it.hasNext(); ) {
            boolean hasDataEnumRestriction = false;
            Object extraData = it.next();
            if (extraData instanceof SchemaRep.Restriction)
                restrict = (SchemaRep.Restriction)extraData;
            else
                continue;
            if (isUnion) {
                // There is no type associated with the union
                // So get the type of the restriction
                String tempType = restrict.getJavaTypeName();
                if (tempType != null)
                    type = tempType.intern();
                jw.write("{\n");
            }
            boolean firstPattern = true;
            for (Iterator itr = restrict.subElementsIterator(); itr.hasNext(); ) {
                Object rType = itr.next();
                if (rType instanceof SchemaRep.Pattern) {
                    if (firstPattern) {
                        jw.write("{\n");
                        jw.writeEol("boolean patternPassed = false");
                        firstPattern = false;
                    }
                    DataTypeRestriction dtr = (DataTypeRestriction) rType;
                    dtr.genRestriction(jw, attr, type, "patternPassed", true);
                }
            }
            if (!firstPattern) {
                jw.writeEol("restrictionFailure = !patternPassed");
                jw.write("}\n");
                if (!isUnion) {
                    jw.beginIf("restrictionFailure");
                    genValidateFail(JavaUtil.instanceFrom("java.lang.String", attr),
                                    propertyName, false,
                                    ValidateException.FailureType.DATA_RESTRICTION,
                                    jw);
                    jw.end();
                }
            }
            for (Iterator itr = restrict.subElementsIterator(); itr.hasNext(); ) {
                Object rType = itr.next();
                if (rType instanceof DataListRestriction) {
                    DataListRestriction dlr = (DataListRestriction) rType;
                    dlr.genRestriction(jw, "size"+a.name+"()", a.getReadMethod(false), type, "restrictionFailure", false);
                    if (!isUnion) {
                        jw.beginIf("restrictionFailure");
                        genValidateFail(JavaUtil.instanceFrom("java.lang.String", attr+" "+dlr.toString()),
                                        propertyName, false,
                                        ValidateException.FailureType.DATA_RESTRICTION,
                                        jw);
                        jw.end();
                    }
                }
                if (rType instanceof DataTypeRestriction && !(rType instanceof SchemaRep.Pattern)) {
                    DataTypeRestriction dtr = (DataTypeRestriction) rType;
                    String typeAttr = attr;
                    if (isUnion) {
                        // for an element having a uniontype the getXXX function would return String 
                        // So for the actual validation we need to convert the string to the
                        // restriction's type so that we can compare
                        jw.beginTry();
                        typeAttr = "("+JavaUtil.genParseText(type, attr, config.isJava5())+")";
                    }
                    dtr.genRestriction(jw, typeAttr, type, "restrictionFailure", false);
                    if (isUnion) {
                        // Catch any exception during the comparison
                        // An exception will be thrown only for the conversion of the type
                        // so assume failure if an exception occurs, since comparison did not
                        // occur
                        jw.endCatch("Exception e");
                        jw.writeEol("restrictionFailure = true");
                        jw.end();
                    }
                    if (!isUnion) {
                        jw.beginIf("restrictionFailure");
                        genValidateFail(JavaUtil.instanceFrom("java.lang.String", attr+" "+dtr.toString()),
                                        propertyName, false,
                                        ValidateException.FailureType.DATA_RESTRICTION,
                                        jw);
                        jw.end();
                    }
                }
            }
            for (Iterator itr = restrict.subElementsIterator(); itr.hasNext(); ) {
                Object rType = itr.next();
                if (rType instanceof DataEnumRestriction) {
                    DataEnumRestriction der = (DataEnumRestriction) rType;
                    if (!hasDataEnumRestriction) {
                        // first one
                        hasDataEnumRestriction = true;
                        jw.write("final "+type+"[] "+enumArray+" = {");
                    } else {
                        jw.write(", ");
                    }
                    der.genRestriction(jw, type);
                }
            }
            if (hasDataEnumRestriction) {
                jw.writeEol("}");
                jw.writeEol("restrictionFailure = true");
                jw.beginFor("int _index2 = 0", "_index2 < "+enumArray+".length",
                            "++_index2");
                jw.beginIf(JavaUtil.genEquals(type, enumArray+"[_index2]",
                                              attr, false));
                jw.writeEol("restrictionFailure = false");
                jw.writeEol("break");
                jw.end();
                jw.end();
                if (!isUnion) {
                    jw.beginIf("restrictionFailure");
                    genValidateFail(JavaUtil.instanceFrom("java.lang.String", attr+" enumeration test"),
                                    propertyName, false,
                                    ValidateException.FailureType.ENUM_RESTRICTION,
                                    jw);
                    jw.end();
                }
            }
            if (isUnion) {
                // Set restrictionPassed only if it is not already set to true
                jw.beginIf("!restrictionPassed");
                jw.writeEol("restrictionPassed = !restrictionFailure");
                jw.end();
                jw.writeEol("restrictionFailure = false");
                jw.write("}\n");
            }
        }
        if (isUnion) {
            jw.beginIf("!restrictionPassed");
            genValidateFail(JavaUtil.instanceFrom("java.lang.String", attr),
                            propertyName, false,
                            ValidateException.FailureType.ALL_RESTRICTIONS,
                            jw);
            jw.end();
        }
    }

    protected void genValidateIf(String test, String name,
                                 ValidateException.FailureType ft,
                                 JavaWriter out) throws IOException {
        jw.beginIf(test);
        genValidateFail(test, name, true, ft, out);
        jw.end();
    }

    protected abstract void genValidateFail(String detail, String name,
                                            boolean quoteDetail,
                                            ValidateException.FailureType ft,
                                            JavaWriter out) throws IOException;

    public Collection getGeneratedMethods() {
        return jw.getStoredMethods();
    }

    protected void genDefaultsAccessable(Property a) throws IOException {
        String type = a.getType();
        boolean mustHaveOneInstance = (!a.ored && a.elementInstance == Common.TYPE_1);
        if (config.isDefaultsAccessable() && !a.isIndexed() &&
            (a.isScalar() || a.getDefaultValue() != null ||
             mustHaveOneInstance)) {
            jw.beginMethod("fetchDefault"+a.name, "", null, type, jw.PUBLIC);
            List exceps = JavaUtil.exceptionsFromParsingText(type, false);
            if (!exceps.isEmpty()) {
                jw.beginTry();
            }
            jw.write("return ");
            if (a.getDefaultValue() != null)
                jw.write(JavaUtil.instanceFrom(type, a.getDefaultValue()));
            else if (config.isMakeDefaults())
                jw.write(JavaUtil.genNewDefault(type));
            else
                jw.write(JavaUtil.nullValueForType(type));
            jw.eol();
            if (!exceps.isEmpty()) {
                end();
                genRethrowExceptions(exceps);
            }
            jw.end();
            jw.cr();
        }
    }

    protected void generateSwitches() throws IOException {
        select(jw.BODY_SECTION);
        jw.bigComment("@return true if error, then should display help");
        jw.beginMethod("parseArguments", "String[] args", null, "boolean");
        jw.beginFor("int argNum = 0, size = args.length", "argNum < size",
                    "++argNum");
        jw.writeEol("String arg = args[argNum].toLowerCase().intern()");
        int size = attrList.size();
        Map mandatoryProperties = new LinkedHashMap();
        StringBuffer helpParams = new StringBuffer();
        int lastHelpParamsPos = 0;
        StringBuffer helpText = new StringBuffer();
        for (int i = 0; i < size; i++) {
            Property a = (Property)attrList.get(i);
            XMLSchemaParser.SwitchData sd = (XMLSchemaParser.SwitchData) a.searchExtraData(XMLSchemaParser.SwitchData.class);
            if (sd == null)
                continue;
            if (sd.isMandatory())
                mandatoryProperties.put(sd.getName(), a);
            String sw = sd.getName();
            sw = sw.toLowerCase();
            String type = a.getType().intern();
            boolean indexed = a.isIndexed();
            String writeMethod;
            if (indexed)
                writeMethod = a.getAddMethod();
            else
                writeMethod = a.getWriteMethod();
            //jw.comment("a.name="+a.name+" sw="+sw+" sd.help="+sd.getHelp()+" mandatory="+sd.isMandatory());
            jw.beginIf(JavaUtil.instanceFrom("String", "-"+sw) + ".equals(arg)");
            boolean isBoolean = (type == "boolean" || type == "Boolean" || type == "java.lang.Boolean");
            helpParams.append(" ");
            if (!sd.isMandatory())
                helpParams.append("[");
            helpParams.append("-");
            if (isBoolean) {
                jw.writeEol(writeMethod+"(true)");
                helpParams.append(sd.getName());
            } else {
                jw.beginIf("argNum+1 >= size");
                jw.writeEol("missingArgument(args, arg)");
                jw.writeEol("continue");
                jw.end();
                jw.writeEol(writeMethod+"("+JavaUtil.genParseText(type,
                                                                         "args[++argNum]",
                                                                         config.isForME())+")");
                helpParams.append(sd.getName());
                helpParams.append(" ");
                if (type == "java.io.File")
                    helpParams.append("filename");
                else
                    helpParams.append(a.dtdName);
            }
            if (!sd.isMandatory())
                helpParams.append("]");
            jw.writeEol("continue");
            jw.end();
            helpText.append(" -");
            helpText.append(sd.getName());
            helpText.append("\t");
            if (sd.getHelp() != null) {
                helpText.append(sd.getHelp());
            } else {
                helpText.append(a.name);
            }
            helpText.append('\n');
            if (isBoolean) {
                jw.beginIf(JavaUtil.instanceFrom("String", "-no"+sw) + ".equals(arg)");
                jw.writeEol(writeMethod+"(false)");
                jw.writeEol("continue");
                jw.end();
                helpParams.append(" [-no");
                helpParams.append(sd.getName());
                helpParams.append("]");
            }
            if (helpParams.length() - lastHelpParamsPos > 63) {
                helpParams.append("\n");
                lastHelpParamsPos = helpParams.length();
            }
        }
        jw.beginIf("arg == \"-help\" || arg == \"--help\"");
        jw.writeEol("return true");
        jw.end();
        jw.writeEol("argNum = unknownArgument(args, arg, argNum)");
        jw.end();
        for (Iterator it = mandatoryProperties.keySet().iterator(); it.hasNext(); ) {
            String sw = (String) it.next();
            Property a = (Property) mandatoryProperties.get(sw);
            jw.beginIf(testIfPropertyNotSet(a));
            jw.writeEol("missingMandatoryArgument(", JavaUtil.instanceFrom("java.lang.String", "-"+sw),
                        ")");
            jw.end();
        }
        jw.writeEol("return false");
        jw.end();
        jw.cr();
        jw.beginMethod("unknownArgument",
                       "String[] args, String arg, int argNum", null, "int",
                       jw.PROTECTED);
        jw.writeEol("throw new IllegalArgumentException(\"Found unknown argument '\"+arg+\"'\")");
        jw.end();
        jw.cr();
        jw.beginMethod("missingArgument",
                       "String[] args, String arg", null, "void",
                       jw.PROTECTED);
        jw.writeEol("throw new IllegalArgumentException(\"Not enough arguments.  Need 1 more for '\"+arg+\"'\")");
        jw.end();
        jw.cr();
        jw.beginMethod("missingMandatoryArgument",
                       "String arg", null, "void",
                       jw.PROTECTED);
        jw.writeEol("throw new IllegalArgumentException(\"Missing argument '\"+arg+\"'\")");
        jw.end();
        jw.cr();
        jw.beginMethod("showHelp", "java.io.PrintStream out");
        if (helpParams.length() > 0) {
            if (lastHelpParamsPos < helpParams.length())
                helpParams.append("\n");
            jw.writeEol("out.println(",
                        JavaUtil.instanceFrom("java.lang.String",
                                              helpParams.toString()),
                        ")");
        }
        if (helpText.length() > 0) {
            jw.writeEol("out.print(",
                        JavaUtil.instanceFrom("java.lang.String",
                                              helpText.toString()),
                        ")");
        }
        jw.end();
        jw.cr();
    }

    protected boolean isMutuallyExclusive(Property a) {
        return (a.ored && !a.getMutuallyExclusiveProperties().isEmpty());
    }

    /**
     * Generate commands for resetting the mutually exclusive properties,
     * only if isMutuallyExclusive(a) returns true.
     */
    protected void genResetMutuallyExclusive(Property a, boolean genTest) throws IOException {
        if (isMutuallyExclusive(a)) {
            if (genTest) {
                if (a.isIndexed()) {
                    jw.beginIf("value != null && value.length > 0");
                } else {
                    jw.beginIf("value != "+JavaUtil.nullValueForType(a.getType()));
                }
            }
            jw.comment("It's a mutually exclusive property.");
            for (Iterator it = a.getMutuallyExclusiveProperties().iterator(); it.hasNext(); ) {
                Property prop = (Property) it.next();
                jw.writeEol(prop.getWriteMethod(), "(",
                            prop.isIndexed() ? "null" : JavaUtil.nullValueForType(prop.getType()),
                            ")");
            }
            if (genTest)
                jw.end();
        }
    }

    /**
     * All generated beans are at least this type.
     */
    protected String commonBeanType() {
        String cb = "java.lang.Object";
        if (config.getGenerateCommonInterface() != null) {
            if (packageName == null)
                cb = config.getGenerateCommonInterface();
            else
                cb = packageName + "." + config.getGenerateCommonInterface();
        }
        return cb;
    }

    protected String parentBeanType() {
        if (config.isExtendBaseBean())
            return "org.netbeans.modules.schema2beans.BaseBean";
        else
            return commonBeanType();
    }

    /**
     * Returns the first element in a command separated list
     */
    protected static String firstInCommaSeparatedList(String lst) {
        if (lst == null)
            return null;
        int pos = lst.indexOf(',');
        if (pos < 0)
            return lst;
        return lst.substring(0, pos);
    }

    protected String genNewMethod(String abstractType, String concreteType) throws IOException {
        return genNewMethod(abstractType, concreteType, concreteType);
    }

    /**
     * Generate a method that will get a new instance of some type.
     * Only 1 method will be generated per abstractType.
     * @param abstractType interface name to use as return type, if
     *                     null, then concreteType is used.
     * @param concreteType the class to get a new instance of
     * @param concreteFullTypeName the full name of the concreteType
     */
    protected String genNewMethod(String abstractType, String concreteType,
                                  String concreteFullTypeName) throws IOException {
        if (abstractType == null)
            abstractType = concreteType;
        abstractType = abstractType.trim();
        concreteType = concreteType.trim();
        String returnType = abstractType;
        boolean abstractConcreteSame = abstractType.equals(concreteType);
        if (abstractConcreteSame)
            returnType = concreteFullTypeName;
        String methodName = (String) generatedNewMethods.get(abstractType);
        if (methodName == null) {
            if (abstractType.indexOf('.') >= 0) {
                String base = JavaUtil.baseName(abstractType);
                if (generatedNewMethods.containsKey(base)) {
                    methodName = "new"+(abstractType.replace('.', '_'));
                } else {
                    methodName = "new"+base;
                }
            } else {
                methodName = "new"+abstractType;
            }
            jw.pushSelect(NEW_METHOD_SECTION);
            jw.bigComment("Create a new bean using it's default constructor.\nThis does not add it to any bean graph.");
            jw.beginMethod(methodName, "", null, returnType, jw.PUBLIC | jw.BEANINFO);
            jw.writeEol("return new ", concreteFullTypeName, "()");
            jw.endMethod();

            if (hasDeepCopyConstructor()) {
                jw.bigComment("Create a new bean, copying from another one.\nThis does not add it to any bean graph.");
                String formalParam = abstractType + " source";
                String actualParam;
                if (abstractConcreteSame)
                    actualParam = "source";
                else
                    actualParam = "("+concreteType+") source";
                if (config.isGenerateParentRefs()) {
                    formalParam += ", "+parentBeanType()+" parent";
                    actualParam += ", parent";
                }
                formalParam += ", boolean justData";
                actualParam += ", justData";
                jw.beginMethod(methodName, formalParam, null, returnType, jw.PUBLIC | jw.BEANINFO);
                jw.write("return new ", concreteFullTypeName);
                jw.writeEol("(", actualParam, ")");
                jw.endMethod();
            }
            jw.popSelect();
            generatedNewMethods.put(abstractType, methodName);
        }
        return methodName;
    }

    protected void genNewDefault(Property prop,
                                 boolean wantConcreteType) throws IOException {
        String type = prop.getType();
        if (prop.isBean) {
            String abstractType = prop.getPropertyInterface();
            String methodName = genNewMethod(abstractType, type,
                                             getTypeFullClassName(prop));
            if (wantConcreteType) {
                if (abstractType != null && !type.equals(abstractType))
                    jw.write("(", type, ") ");
            }
            jw.write(methodName, "()");
        } else {
            jw.write(JavaUtil.genNewDefault(type));
        }
    }

    protected boolean hasDeepCopyConstructor() {
        return false;
    }

    protected String testIfPropertySet(Property prop) {
        if (prop.isIndexed()) {
            return "size"+prop.name+"() > 0";
        } else {
            return prop.getReadMethod(false)+"() != "+JavaUtil.nullValueForType(prop.getType());
        }
    }

    protected String testIfPropertyNotSet(Property prop) {
        if (prop.isIndexed()) {
            return "size"+prop.name+"() == 0";
        } else {
            return prop.getReadMethod(false)+"() == "+JavaUtil.nullValueForType(prop.getType());
        }
    }

    protected int countNumberOfNonAttributeProperties() {
        int count = beanElement.getNonAttributePropertyCount();
        /*
        int count = 0;
        for (int i = 0; i < attrList.size(); i++) {
            Property prop = (Property)attrList.get(i);
            if (prop.isAttribute())
                continue;
            ++count;
        }
        */
        if (config.isRespectExtension()) {
            count += countNumberOfNonAttributePropertiesRecurse(beanElement.getExtension());
        }
        return count;
    }

    protected int countNumberOfNonAttributePropertiesRecurse(BeanBuilder.BeanElement be) {
        int count = 0;
        while (be != null) {
            //config.messageOut.println("extensionBE="+be);
            count += be.getNonAttributePropertyCount();
            be = be.getExtension();
            //config.messageOut.println("count="+count);
        }
        return count;
    }
    
    public void setPrefixGuesser(PrefixGuesser guesser) {
        if (guesser == null) {
            if (prefixGuesser == null) {
                prefixGuesser = new PrefixGuesser() {
                    public String guessPrefixFromURI(String uri) {
                        return SchemaRep.guessPrefix(uri);
                    }
                };
            }
        } else {
            prefixGuesser = guesser;
        }
    }
}
