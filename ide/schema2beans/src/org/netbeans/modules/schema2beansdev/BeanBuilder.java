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
import org.netbeans.modules.schema2beansdev.beangraph.*;
import org.netbeans.modules.schema2beansdev.gen.*;

/**
 *  This class implements the Document Definition handler in order to build
 *  the internal tree representation of the DD DTD.
 */
public class BeanBuilder {
    private static final String UNIQUE_PREFIX = "My";
    protected GenBeans.Config config;
    protected CodeGeneratorFactory codeGenFactory;
    protected BeanElement rootElement;
    protected String genDir;
    protected String packagePath;
    protected String packageName = null;
    protected TreeParser parser;
    protected Map constNameMap = null;	// LinkedHashMap<String, String>

    protected Map illegalClassNames = new HashMap();
    
    class BeanElement {
        GraphNode	node;
        String 		beanName;
	
        int		type;
        String		classType;
        private boolean typeSetExternally = false;
	
        boolean		isRoot;
        boolean         isAbstract;
        boolean         isExtended;

        private boolean canBeEmpty = false;

        private Map usedTypes;
        private int nonAttributePropertyCount = 0;
	
        BeanElement(GraphNode node) {
            this.node = node;
        }
	
        void initialize(boolean isRoot) {
            //	Make up a bean name: xxx-yyy has to be XxxYyy
            this.beanName = Common.convertName(this.node.getName());
	    
            //
            //	As each node ends up as a property, we have to find out the
            //	type of the property. The default value is Common.TYPE_BEAN.
            //
            this.type = Common.TYPE_BEAN;
            this.isRoot = isRoot;
            
            calculateType();
        }
	
        public GraphNode getGraphNode() {
            return this.node;
        }

        /**
         * Call this to change whether or not we will actually generate
         * this as a bean.  Should we create it or not?
         * Users need to call this method instead of node.setCreated directly,
         * because some of this object's state depends on that value.
         */
        public void setNodeCreated(boolean value) {
            node.setCreated(value);
            calculateType();
        }

        protected void calculateType() {
            // If we arn't going to create it, then it must not be a bean.
            if (!node.isCreated())
                this.type = Common.TYPE_STRING;
            GraphNode[] nodes = node.getNodes();
            // If it is the root, then it must be a bean.
            if (nodes.length == 1 && !isRoot) {
                if (Common.DTD_STRING.equals(nodes[0].getName()))
                    this.type = Common.TYPE_STRING;
                else {
                    if (Common.DTD_EMPTY.equals(nodes[0].getName())) {
                        this.type = Common.TYPE_BOOLEAN;
                        this.canBeEmpty = true;
                    }
                }
            }
        }

        void setCanBeEmpty(boolean value) {
            canBeEmpty = value;
            if (canBeEmpty) {
                //type = Common.TYPE_BOOLEAN;
            }
        }
	
        /**
         * setName allows you to change the name of the generated class.
         */
        public void setName(String name) {
            this.beanName = name;
        }
		
        public String getName() {
            return this.beanName;
        }
	
        public String getDTDName() {
            return this.node.getName();
        }

        public void setDTDName(String dtdName) {
            node.setName(dtdName);
        }

        public String getNamespace() {
            return node.getNamespace();
        }
	
        public boolean isBean() {
            return Common.isBean(this.type);
        }
	
        public boolean isBoolean() {
            return Common.isBoolean(this.type);
        }
	
        public boolean isRoot() {
            return this.isRoot;
        }
	
        public String typeToString() {
            switch(this.type) {
            case Common.TYPE_STRING:
                return Common.CLASS_STRING;
            case Common.TYPE_BOOLEAN:
                return Common.CLASS_BOOLEAN;
            default:
                return this.beanName;
            }
        }
	
        public String getClassType() {
            if (classType == null)
                return typeToString();
            return classType;
        }

        public String getFullClassType() {
            String result = getClassType();
            if (packageName == null)
                return result;
            if (isBean() && node.isCreated())
                return packageName+"."+result;
            return result;
        }

        public void setClassType(String ct) {
            classType = ct;
        }

        public boolean isTypeSetExternally() {
            return typeSetExternally;
        }

        public void setTypeSetExternally(boolean value) {
            typeSetExternally = value;
        }

        public boolean isExtended() {
            return isExtended;
        }

        public void setExtended(boolean isExtended) {
            this.isExtended = isExtended;
        }
        
        public String toString() {
            return this.beanName +
                ((this.type == 
                  Common.TYPE_STRING)?" \t(String)":"\t(Bean)");  // NOI18N
        }

        public void setUsedTypes(Map usedTypes) {
            this.usedTypes = usedTypes;
        }

        public boolean isUsedType(String type) {
            return usedTypes.containsKey(type);
        }

        public boolean getCanBeEmpty() {
            return canBeEmpty;
        }

        public String getOutputFileName() {
            if (isBean())
                return getClassType();
            else
                return getName();
        }
        
        public BeanElement getExtension() {
            if (node.getExtension() == null || !node.getExtension().isCreated())
                return null;
            return (BeanElement) node.getExtension().getObject();
        }
        
        public void setNonAttributePropertyCount(int value) {
            nonAttributePropertyCount = value;
        }

        public int getNonAttributePropertyCount() {
            return nonAttributePropertyCount;
        }
    }

    static class Finder {
        private String findExpr, byExpr;
        private boolean listFindExpr;
        
        public Finder(String findExpr, String byExpr, boolean listFindExpr) {
            this.findExpr = findExpr;
            this.byExpr = byExpr;
            this.listFindExpr = listFindExpr;
        }

        public String getFindExpr() {
            return findExpr;
        }

        public String getByExpr() {
            return byExpr;
        }

        public boolean isListFindExpr() {
            return listFindExpr;
        }

        public String toString() {
            if (listFindExpr)
                return "Finder list "+findExpr+" by "+byExpr;
            else
                return "Finder "+findExpr+" by "+byExpr;
        }
    }
    
    BeanBuilder(TreeParser parser, GenBeans.Config config, CodeGeneratorFactory cgf) {
        this.parser = parser;
        this.config = config;
        this.codeGenFactory = cgf;
        
        // Since java.lang is implicitly imported, we have to make sure to
        // not generate a class with that collides with java.lang.
        illegalClassNames.put("Object", null);
        illegalClassNames.put("Thread", null);
        illegalClassNames.put("Compiler", null);
        illegalClassNames.put("Class", null);
        illegalClassNames.put("ClassLoader", null);
        //illegalClassNames.put("Process", null);
        illegalClassNames.put("Package", null);
        illegalClassNames.put("String", null);
        illegalClassNames.put("Boolean", null);
        illegalClassNames.put("Integer", null);
        illegalClassNames.put("Long", null);
        illegalClassNames.put("Short", null);
        illegalClassNames.put("Double", null);
        illegalClassNames.put("Float", null);
        illegalClassNames.put("Byte", null);
        illegalClassNames.put("Character", null);
        illegalClassNames.put("int", null);
        illegalClassNames.put("char", null);
        illegalClassNames.put("byte", null);
        illegalClassNames.put("short", null);
        illegalClassNames.put("long", null);
        illegalClassNames.put("double", null);
        illegalClassNames.put("float", null);
        illegalClassNames.put("boolean", null);
        illegalClassNames.put("void", null);
    }
    
    
    /**
     *	Parse the attributes of the bean
     *
     * usedNames helps us to not generate multiple properties with the
     * same name.  The key is the name of the property and the entry
     * is a BeanElement (name = BeanElement.getName()).  Currently,
     * the storage of the BeanElement is not used, and null should be
     * valid for properties that are defined without a BeanElement.
     */
    private void buildProperties(GraphLink l, CodeGeneratorClass bc,
                                 int nestedLevel, int groupInstance,
                                 boolean ored,
                                 MetaElement e, MetaDD mdd, Map usedNames) {
        while (l != null) {
            if (config.isTraceGen())
                config.messageOut.println("buildProperties: l="+l+" l.name="+l.name+" l.element="+l.element+" l.getSibling()="+l.getSibling()+" groupInstance="+groupInstance);
            if (l.element != null) {
                BeanElement be = (BeanElement)l.element.getObject();
		
                if (be == null) {
                    // This can happen if there are no properties associated
                    // with this bean (like if the root element has no
                    // children elements).
                    config.messageOut.println("Warning: be was null");
                    continue;
                }

                //
                //  The group prop was set on the parent of the children.
                //  Set it on each property directly for building the class.
                //
                if (l.getParent() != null)
                    ored = ored || l.getParent().isSequenceOr();
                //System.out.println("ored="+ored);

                String name;
                String dtdName;
                String namespace;
                String constName;
                if (l.name != null) {
                    name = Common.convertName(l.name);
                    dtdName = l.getSchemaName();
                    namespace = l.getNamespace();
                } else {
                    name = be.getName();
                    dtdName = be.getDTDName();
                    namespace = be.getNamespace();
                }
                MetaElement propertyME = getMetaElement(mdd, dtdName);
                if (propertyME != null && propertyME.getBeanName() != null) {
                    name = propertyME.getBeanName();
                    if (config.isTraceGen())
                        config.messageOut.println("buildProperties: property in "+e.getBeanName()+" has been renamed to "+name);
                }
                constName = Common.constName(dtdName);
                if ("#PCDATA".equals(dtdName)) {
                    // text node
                    //config.messageOut.println("Hit #PCDATA");
                    name = "pcdata";
                    constName = "PCDATA";
                }
                if (usedNames.containsKey(name)) {
                    int uniqNum = 2;
                    String baseName = name;
                    while (usedNames.containsKey(name = baseName + uniqNum))
                        uniqNum++;
                    constName = constName + uniqNum;
                    if (l.name != null) {
                        //l.name = l.name + uniqNum;
                    } else {
                        be.setName(name);
                        MetaElement origE = getMetaElement(mdd, dtdName);
                        if (origE != null) {
                            MetaElement newE = new MetaElement(origE);
                            mdd.addMetaElement(newE);
                        }
                    }
                    config.messageOut.println(Common.getMessage("RenamedProperty_msg", baseName, name, e.getBeanName()));
                }
                usedNames.put(name, be);
                if (config.isTraceGen())
                    config.messageOut.println("buildProperties: name="+name+" constName="+constName+" dtdName="+dtdName+" graphlink.name="+l.name+" be.getClassType="+be.getClassType());
                AttrProp[] attrs = be.node.getAttributes();
                List extraData = new ArrayList(l.extraData);
                if (!be.node.getExtraData().isEmpty()) {
                    extraData.addAll(be.node.getExtraData());
                    //System.out.println("**** be.node="+be.node+" extraData="+extraData);
                }
                //System.out.println("extraData="+extraData);
                if (propertyME != null && propertyME.sizeKnownValue() > 0) {
                    for (int valNum = 0, size = propertyME.sizeKnownValue(); valNum < size; ++valNum) {
                        String knownValue = propertyME.getKnownValue(valNum);
                        extraData.add(new KnownValueEnumeration(knownValue));
                    }
                }
                constNameMap.put(constName, dtdName);
                int type = be.type;
                String classType = be.getClassType();
                //
                // Look to see if we need to make a primitive type into an
                // Object type, but don't do it, if the user has set the
                // type (like from mdd file).
                //
                if (!be.isTypeSetExternally() &&
                    (ored || l.isNillable())) {
                    if (JavaUtil.isPrimitiveType(classType)) {
                        classType = JavaUtil.toObjectType(classType);
                        type = Common.wrapperToType(classType);
                        if (type == Common.NONE)
                            type = Common.TYPE_STRING;
                        if (config.isTraceGen())
                            config.messageOut.println("Promoting primitive type to object type for "+name+" classType="+classType+" type="+type);
                    }
                }
                AbstractCodeGeneratorClass.Property prop = 
                    bc.addProperty(name, dtdName, namespace, l.element, l,
                                   classType, nestedLevel,
                                   l.getElementInstance(),
                                   groupInstance, type, ored,
                                   attrs, constName,
                                   l.getDefaultValue(), true, extraData, l.isUnion());
                prop.setCanBeEmpty(be.getCanBeEmpty());
                prop.setNillable(l.isNillable());
                prop.setBeanElement(be);
                l.setObject(prop);
                if (e != null) {
                    // Need to check that name is not already in there.
                    MetaProperty[] metaProperties = e.getMetaProperty();
                    boolean found = false;
                    for (int i = 0; i < metaProperties.length; ++i) {
                        if (name.equals(metaProperties[i].getBeanName())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        MetaProperty mp = new MetaProperty();
                        mp.setBeanName(name);
                        e.addMetaProperty(mp);
                    }
                }

                //
                // Take care of the case where a subnode is a leaf node
                // and it has attributes too.  In which case, that subnode's
                // attributes, become our own.
                //
                if (!Common.isBean(be.type) &&
                    config.isAttributesAsProperties()) {
                    addAttrProps(bc, attrs, name, usedNames,
                                 l.getElementInstance(), false);
                }
            }
	    
            //
            //	As we go one level deeper, the current link has
            //	the instance property of the group of children.
            //
            int childGroupInstance = Common.widestInstance(groupInstance,
                                                           l.getGroupInstance());
            buildProperties(l.getFirstChild(), bc, nestedLevel+1,
                            childGroupInstance, ored, e, mdd, usedNames);
	    
            l = l.getSibling();
        }
    }
    
    protected void addAttrProps(CodeGeneratorClass bc, AttrProp[] attrs,
                                String propertyName, Map usedNames,
                                int groupInstance,
                                boolean directChild) {
        if (attrs != null) {
            for (int i = 0; i < attrs.length; ++i) {
                addAttrProp(bc, attrs[i], propertyName, usedNames,
                            groupInstance, directChild);
            }
        }
    }
    
    protected void addAttrProp(CodeGeneratorClass bc, AttrProp attr,
                               String propertyName, Map usedNames,
                               int groupInstance,
                               boolean directChild) {
        String name;
        if (directChild)
            name = Common.convertName(attr.getName());
        else
            name = Common.convertName(propertyName+"_"+attr.getName());
        if (usedNames.containsKey(name)) {
            int uniqNum = 2;
            String baseName = name;
            while (usedNames.containsKey(name = baseName + uniqNum))
                uniqNum++;
            attr.setName(name);
            config.messageOut.println(Common.getMessage("RenamedProperty_msg", baseName, name, propertyName));
        }
        usedNames.put(name, attr);

        String javaType = attr.getJavaType();
        int type;
        if (javaType == null) {
            type = Common.TYPE_STRING;
            javaType = "java.lang.String";	// NOI18N
        } else {
            type = Common.wrapperToType(javaType);
            if (type == Common.NONE)
                type = Common.TYPE_STRING;
        }
        //System.out.println("addAttrProp: attr="+attr+" attr.javaType="+javaType);
        List extraData = attr.getExtraData();
        String namespace = attr.getNamespace();
        bc.addProperty(name, attr.getDtdName(), namespace, null, null,
                       javaType, 0,
                       attr.getInstance(),
                       groupInstance,
                       type,
                       false,
                       null,
                       Common.constName(name),
                       attr.getDefaultValue(), directChild,
                       extraData, false)
            .setAttrProp(attr);
    }

    protected void addCommentsProcessing(CodeGeneratorClass bc) {
        bc.addProperty("Comments", "comment", null, null, null,
                       "java.lang.String", 0,
                       Common.TYPE_0_N, 0, Common.TYPE_COMMENT,
                       false, null, "COMMENTS", null, true,
                       Collections.EMPTY_LIST, false);
    }

    protected static class KnownValueEnumeration implements DataEnumRestriction {
        private String knownValue;
        
        protected KnownValueEnumeration(String value) {
            knownValue = value;
        }

        public void genRestriction(Writer out, String type) throws IOException {
            out.write(JavaUtil.instanceFrom(type, knownValue));
        }
    }
    
    //	Called by GenBeans
    void process() throws IOException {
        Map generators = new LinkedHashMap();	// Map<BeanElement, CodeGeneratorClass>
        prepareBeans(generators);
        doGeneration(generators);
    }

    void prepareBeans(Map generators) throws IOException {
        GraphNode	    root = parser.getRoot();
        GraphNode[]	    list = parser.getNodes();
        String		    rootDir;
        BeanElement	    be;
        MetaDD			mdd = config.getMetaDD();
	
        if (root == null)
            throw new IllegalStateException(Common.getMessage("DTDObjectGraphIsNull_msg"));
        
        constNameMap = new LinkedHashMap();

        for (int i=0; i<list.length; i++) {
            GraphNode node = list[i];
            /*
              config.messageOut.println("list["+i+"]="+node+" alias="+node.getAlias());
              GraphLink link = node.getGraphLink();
              while (link != null) {
              config.messageOut.println("link="+link+" link.name="+link.name+" link.element="+link.element);
              link = link.sibling;
              }
            */
            be = new BeanElement(node);
            be.initialize(node==root);
            node.setObject(be);
        }

        if (config.getRootDir() == null)
            rootDir = ".";	// NOI18N
        else
            rootDir = config.getRootDir().toString();
        packagePath = config.getPackagePath();
	
        rootElement = (BeanElement)root.getObject();

        //	No package specified, use the docroot as the default value
        if (packagePath == null) {	// NOI18N
            packagePath = rootElement.getName().toLowerCase();			
        }
	
        if (rootDir.equals(""))	// NOI18N
            genDir = packagePath;
        else {
            if (!rootDir.equals("/")) {	// NOI18N
                if (packagePath == null || packagePath.equals(""))
                    genDir = rootDir;	// NOI18N
                else
                    genDir = rootDir + "/" + packagePath;	// NOI18N
            } else
                genDir = "/" + packagePath;	// NOI18N
        }
        packageName = null;
        if (packagePath != null)
            packageName = packagePath.replace('/', '.');
	
        if (config.isDoGeneration() &&
            config.getOutputStreamProvider() instanceof GenBeans.DefaultOutputStreamProvider) {
            File dir = new File(genDir);
	    
            if (dir.exists() && !dir.isDirectory()) {
                throw new Schema2BeansRuntimeException(Common.
                                                       getMessage("CantCreateDirIsFile_msg", dir));
            } else {
                if (!dir.exists()) {
                    if (dir.mkdirs()) {
                        if (!config.isQuiet())
                            config.messageOut.println(Common.getMessage("MSG_CreatedDirectory", dir));	// NOI18N
                    }
                }
            }
        }

        //
        // Bring in any previously generated types.
        //
        setSchemaType(list);

        String commonInterface = config.getGenerateCommonInterface();
        if (commonInterface != null) {
            illegalClassNames.put(commonInterface, "Common Bean Interface");
            if (packageName != null && !"".equals(packageName))
                commonInterface = packageName + "." + commonInterface;
        }
        boolean doGeneration = !config.isCheckUpToDate();
        //
        //	Build the internal representation of the bean classes
        //
        for (int i=0; i<list.length; i++) {
            MetaElement		e = null;
            StringBuffer 	str;
            GraphNode graphNode = list[i];
    
            String suggestedJavaType = graphNode.getJavaType();
            //config.messageOut.println("graphNode="+graphNode+" suggestedJavaType="+suggestedJavaType);

            be = (BeanElement)graphNode.getObject();
            if (be == null)
                continue;
	    
            // The user has provided us with some data in their own .mdd
            e = getMetaElement(mdd, be.getDTDName(), graphNode.getNamespace());
            if (e == null) {
                e = new MetaElement();
                e.setBeanName(be.getName());
                e.setDtdName(be.getDTDName());
                e.setNamespace(graphNode.getNamespace());
            
                mdd.addMetaElement(e);
            }
            if (e.isCanBeEmpty()) {
                be.setCanBeEmpty(true);
            }
            
            //config.messageOut.println("process: e="+e+" be.getName="+ ((be != null) ? be.getName() : null));
            // Figure out what java type to use.
            // Make sure that if the user specifies a bean-name that
            // we use it for the name of this bean.
            if (e.getBeanName() != null && !e.getBeanName().equals("")) {
                be.setClassType(e.getBeanName());
                be.setName(e.getBeanName());
            }
            if (e.getWrapperClass() != null) {
                suggestedJavaType = e.getWrapperClass();
                // This is a user defined wrapper class, so we are not
                // creating this guy.
                //be.setNodeCreated(false);  Do not call as the user could have set the wrapper-class for a bean.
                graphNode.setCreated(false);
                be.setTypeSetExternally(true);
            } else if (suggestedJavaType != null) {
                e.setWrapperClass(suggestedJavaType);
            } else if (be != rootElement && Common.isScalar(be.type)) {
                e.setWrapperClass(Common.wrapperClass(be.type));
                suggestedJavaType = e.getWrapperClass();
            } else if (be != rootElement && !Common.isBean(be.type)) {
                e.setWrapperClass(be.typeToString());
                suggestedJavaType = e.getWrapperClass();
            }
            //config.messageOut.println("e.getBeanName="+e.getBeanName());
            if (suggestedJavaType != null) {
                int proposedType = Common.wrapperToType(suggestedJavaType);
                if (proposedType != Common.NONE)
                    be.type = proposedType;
                be.setClassType(suggestedJavaType);
                //config.messageOut.println("Setting class type to "+suggestedJavaType);
            }
            if (graphNode.getExtendedProperty("can-be-empty") != null) {
                be.setCanBeEmpty(true);
            }
            if (be.isBean()) {
                //System.out.println("be.getClassType()="+be.getClassType()+" illegalClassNames[]="+illegalClassNames.get(be.getClassType()));
                while (illegalClassNames.containsKey(be.getClassType())) {
                    String prefix;
                    if (graphNode.getNamespace() != null) {
                        prefix = Common.convertName(graphNode.getNamespace());
                    } else {
                        prefix = UNIQUE_PREFIX;
                    }
                    be.setClassType(prefix+be.getClassType());
                    be.setName(prefix+be.getName());
                    e.setBeanName(prefix+e.getBeanName());
                    if (config.isTraceGen())
                        config.messageOut.println("Made class name change to "+be.getClassType());
                }
                // Make sure no one else wants to use this name.
                illegalClassNames.put(be.getClassType(), be);
                
                if (graphNode.getExtension() != null) {
                    GraphNode extensionNode = graphNode.getExtension();
                    /*
                    config.messageOut.println("Node with extension: "+be);
                    config.messageOut.println("extensionNode="+extensionNode);
                    config.messageOut.println("extensionNode.created="+extensionNode.isCreated());
                    config.messageOut.println("extensionNode.object="+extensionNode.getObject());
                    */
                    if (extensionNode.isCreated()) {
                        BeanElement extensionBE = (BeanElement) extensionNode.getObject();
                        extensionBE.setExtended(true);
                        String extendsName = extensionBE.getFullClassType();
                        e.setExtends(extendsName);
                        addToBeanInterfaceExtends(e, extendsName+"Interface");
                    }
                }

                if (config.isExtendBaseBean()) {
                    if (e.getExtends() == null)
                        e.setExtends("org.netbeans.modules.schema2beans.BaseBean");
                }
                if (graphNode.getExtendedProperty("extends") != null && e.getExtends() == null) {
                    e.setExtends((String) graphNode.getExtendedProperty("extends"));
                }
                if (graphNode.getExtendedProperty("implements") != null && e.getImplements() == null) {
                    e.setImplements((String) graphNode.getExtendedProperty("implements"));
                }
                if (config.isGenerateInterfaces()) {
                    String interfaceName;
                    if (graphNode.getJavaType() == null) {
                        interfaceName = be.getName()+"Interface";
                        if (packageName != null && !"".equals(packageName))
                            interfaceName = packageName + "." + interfaceName;
                    } else {
                        interfaceName = graphNode.getJavaType() + "Interface";
                    }
                    addToImplements(e, interfaceName);
                }
                if (commonInterface != null) {
                    if (graphNode.getJavaType() == null) {
                        addToImplements(e, commonInterface);
                    }
                }
                if (config.isExtendBaseBean()) {
                    addToImplements(e, "org.netbeans.modules.schema2beans.Bean");
                }
                if (config.isCheckUpToDate()) {
                    String outputFileName = be.getOutputFileName();
                    if (config.getOutputStreamProvider().isOlderThan(genDir,
                                                                     outputFileName,
                                                                     "java",
                                                                     config.getNewestSourceTime())) {
                        doGeneration = true;
                    }
                }
            }
        }

        if (!doGeneration) {
            config.messageOut.println(Common.getMessage("MSG_SkippingGenerationDueToTime"));
            return;
        }

        //
        // Build up all of the properties
        //
        Map usedTypes = new HashMap();
        for (int i = 0; i < list.length; i++) {
            MetaElement		e = null;
            be = (BeanElement)list[i].getObject();
            //config.messageOut.println("i="+i+" be="+be);
            if (be == null)
                continue;

            GraphNode graphNode = be.getGraphNode();
            if (be.isBean() || be == rootElement) {
                if (config.isTraceGen())
                    config.messageOut.println("Building properties for "+be);
                be.isAbstract = graphNode.isAbstract();
                
                e = getMetaElement(mdd, be.getDTDName(), graphNode.getNamespace());
                Map usedNames = new HashMap();

                CodeGeneratorClass bc = codeGenFactory.newCodeGeneratorClass(be, config);
                bc.setPackageName(packageName);
                bc.setIndent(config.getIndent());
                bc.setRootBeanElement(rootElement);
                bc.setDefaultNamespace(parser.getDefaultNamespace());
                bc.setInvalidPropertyNames(usedNames);
                if (config.isProcessComments()) {
                    addCommentsProcessing(bc);
                }
                if (parser instanceof HasPrefixGuesser)
                    bc.setPrefixGuesser(((HasPrefixGuesser)parser).getPrefixGuesser());
                if (config.isAttributesAsProperties())
                    addAttrProps(bc, be.node.getAttributes(), be.getName(),
                                 usedNames, Common.TYPE_1, true);
                if (be.isBean()) {
                    GraphLink l = graphNode.getGraphLink();
                    if (l != null) {
                        int groupInstance = l.getGroupInstance();
                        buildProperties(l, bc, 0, groupInstance,
                                        false,
                                        e, mdd, usedNames);
                        int nonAttributePropertyCount = 0;
                        List props = bc.getPropertyList();
                        for (Iterator it = props.iterator(); it.hasNext(); ) {
                            AbstractCodeGeneratorClass.Property prop =
                                (AbstractCodeGeneratorClass.Property) it.next();
                            if (prop.isAttribute())
                                continue;
                            ++nonAttributePropertyCount;
                        }
                        //config.messageOut.println("HIT be "+be+" count "+nonAttributePropertyCount);
                        be.setNonAttributePropertyCount(be.getNonAttributePropertyCount() + nonAttributePropertyCount);
                    }
                }

                //
                // Only if we created it do we want to keep the code generator
                // and use it in a following step.
                //
                if (graphNode.isCreated())
                    generators.put(be, bc);
            }
            usedTypes.put(be.getClassType(), be);
        }
        /*
          for (Iterator it = usedTypes.keySet().iterator(); it.hasNext(); ) {
          String type = (String) it.next();
          System.out.println("Used type: "+type);
          }
        */
        rootElement.setUsedTypes(usedTypes);

        processFinders(rootElement.node);

        if (config.getWriteBeanGraphFile() != null) {
            // The bean graph itself is not part of the bean generation,
            // which is why it's in prepareBeans().
            File beanGraphFile = config.getWriteBeanGraphFile();
            File parentDir = beanGraphFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    if (!config.isQuiet())
                        config.messageOut.println(Common.getMessage("MSG_CreatedDirectory", parentDir));	// NOI18N
                }
            }
            OutputStream out = new WriteIfDifferentOutputStream(beanGraphFile);
            BeanGraph bg = generateBeanGraph(list);
            bg.write(out);
            close(out);
        }
    }

    void doGeneration(Map generators) throws IOException {
        BeanElement	    be;
        MetaDD			mdd = config.getMetaDD();
        if (config.isDoGeneration()) {
            List generatedMethods = new LinkedList();	// List<Collection<JavaWriter.Method>>
            // And generate the files
            for (Iterator it = generators.keySet().iterator(); it.hasNext(); ) {
                be = (BeanElement) it.next();
                CodeGeneratorClass bc = (CodeGeneratorClass) generators.get(be);
                String outputFileName = be.getOutputFileName();

                MetaElement metaElement = getMetaElement(mdd, be.getDTDName(), be.node.getNamespace());
                if (metaElement.isSkipGeneration()) {
                    config.messageOut.println("Skipping generation of class " + be.beanName
                                              + " (as specified in the mdd file)");	// NOI18N
                    continue;
                }

                //
                //	The bean class has now everything it needs to
                //	generate its content (name, package, attributes, ...)
                //
                try {
                    OutputStream out;
                    out = config.getOutputStreamProvider().getStream(genDir, 
                                                                     outputFileName, "java");	// NOI18N
                    bc.generate(out, mdd);
                    close(out);
                    out = null;  // Try to encourage the GC
                    Collection beansMethods = bc.getGeneratedMethods();
                    generatedMethods.add(beansMethods);

                    if (config.isGenerateDelegator()) {
                        GraphNode graphNode = be.getGraphNode();
                        MetaElement e = getMetaElement(mdd, be.getDTDName(),
                                                       graphNode.getNamespace());
                        String delegatorClassName;
                        if (e != null && e.getDelegatorName() != null)
                            delegatorClassName = e.getDelegatorName();
                        else {
                            delegatorClassName = outputFileName+"Delegator";
                            if (e != null)
                                e.setDelegatorName(delegatorClassName);
                        }
                        String delegatorPackageName = packageName;
                        String dir = genDir;
                        if (config.getDelegateDir() != null) {
                            dir = config.getDelegateDir().getAbsolutePath();
                            if (config.getDelegatePackage() == null) {
                                if (packagePath != null && !packagePath.equals(""))
                                    dir = dir + "/" + packagePath;	// NOI18N
                            } else {
                                delegatorPackageName = config.getDelegatePackage();
                                dir = dir + "/" + delegatorPackageName.replace('.', '/');	// NOI18N
                            }
                        }
                        out = config.getOutputStreamProvider().getStream(dir, 
                                                                         delegatorClassName, "java");	// NOI18N
                        bc.generateDelegator(out, mdd, delegatorClassName, delegatorPackageName);
                        close(out);
                        out = null;  // Try to encourage the GC
                    }
                    if (config.isGenerateInterfaces()) {
                        List beanInfoMethods = new ArrayList(beansMethods.size());	// List<JavaWriter.Method>
                        for (Iterator mit = beansMethods.iterator(); mit.hasNext(); ) {
                            JavaWriter.Method method = (JavaWriter.Method) mit.next();
                            if (method.isStatic() || method.isConstructor() ||
                                !method.isPublic())
                                continue;
                            if (!method.isBeanInfo())
                                continue;
                            //System.out.println("\tFound bean info: "+method.getNameParameters());
                            beanInfoMethods.add(method);
                        }
                        String interfaceName = outputFileName+"Interface";	// NOI18N
                        GraphNode graphNode = be.getGraphNode();
                        MetaElement me = getMetaElement(mdd, be.getDTDName(),
                                                       graphNode.getNamespace());
                        generateInterface(genDir, packageName,
                                          interfaceName,
                                          beanInfoMethods,
                                          "This interface has all of the bean info accessor methods.",
                                          me.getBeanInterfaceExtends());
                    }
                } catch(IOException ioe) {
                    config.messageOut.println("Failed to generate bean class: "+outputFileName);	// NOI18N
                    TraceLogger.error(ioe);
                    throw ioe;
                } catch(IllegalStateException ise) {
                    config.messageOut.println("Failed to generate bean class "+outputFileName);	// NOI18N
                    TraceLogger.error(ise);
                    throw ise;
                }
            }

            if (config.getGenerateCommonInterface() != null
                && generatedMethods.size() > 0) {
                Map commonGeneratedMethods = new HashMap(); 	// Map<String, JavaWriter.Method>
                Iterator it = generatedMethods.iterator();
                Collection methods = (Collection) it.next();
                // Put all of the methods into our map
                for (Iterator mit = methods.iterator(); mit.hasNext(); ) {
                    JavaWriter.Method method = (JavaWriter.Method) mit.next();
                    if (method.isStatic() || method.isConstructor() ||
                        !method.isPublic() || method.isUnsupported())
                        continue;
                    commonGeneratedMethods.put(method.getNameParameters(), method);
                }
                // Now go thru the other classes, and remove any methods that we do
                // not find.
                while (it.hasNext()) {
                    //System.out.println("---- Next bean");
                    methods = (Collection) it.next();
                    Map toKeep = new HashMap();	// Map<String, JavaWriter.Method>
                    for (Iterator mit = methods.iterator(); mit.hasNext(); ) {
                        JavaWriter.Method method = (JavaWriter.Method) mit.next();
                        String nameParameters = method.getNameParameters();
                        if (commonGeneratedMethods.containsKey(nameParameters)) {
                            //System.out.println("Keeping "+nameParameters);
                            toKeep.put(nameParameters, commonGeneratedMethods.get(nameParameters));
                        }
                    }
                    commonGeneratedMethods = toKeep;
                }
                //System.out.println("Common Methods:");
                List sortedMethodNames = new ArrayList(commonGeneratedMethods.keySet());
                Collections.sort(sortedMethodNames);
                List sortedMethods = new ArrayList(sortedMethodNames.size());
                for (Iterator sortedMethodNamesIterator = sortedMethodNames.iterator();
                     sortedMethodNamesIterator.hasNext(); ) {
                    sortedMethods.add(commonGeneratedMethods.get(sortedMethodNamesIterator.next()));
                }
                generateInterface(genDir, packageName,
                                  config.getGenerateCommonInterface(),
                                  sortedMethods,
                                  "This interface is the intersection of all generated methods.",
                                  null);
            }
            if (config.getDumpBeanTree() != null) {
                Writer out = new FileWriter(config.getDumpBeanTree());
                try {
                    CodeGeneratorClass bc = (CodeGeneratorClass) generators.get(rootElement);
                    bc.dumpBeanTree(out, "", config.getIndent());
                } finally {
                    close(out);
                }
            }
            if (config.isGenerateTagsFile()) {
                String tagsClassName = "Tags";
                while (illegalClassNames.containsKey(tagsClassName)) {
                    tagsClassName = UNIQUE_PREFIX + tagsClassName;
                }
                OutputStream out = config.getOutputStreamProvider().getStream(genDir, 
                                                                              tagsClassName, "java");	// NOI18N
                generateTagsFile(out, packageName, tagsClassName);
                close(out);
            }
        }
        if (config.getGenerateDotGraph() != null) {
            Writer out = new FileWriter(config.getGenerateDotGraph());
            try {
                generateDotGraph(out, rootElement.getGraphNode());
            } finally {
                close(out);
            }
        }

        if (!config.isQuiet())
            config.messageOut.println(Common.getMessage("MSG_GenerationSummary",
                                                        rootElement.getDTDName(),
                                                        rootElement.getClassType()));
    }

    protected void processFinders(GraphNode rootGraphNode) {
        for (int i = 0, size = config.sizeFinder(); i < size; ++i) {
            String finderExpr = config.getFinder(i);
            processFinder(rootGraphNode, finderExpr);
        }
        MetaDD mdd = config.getMetaDD();
        for (Iterator it = mdd.fetchFinderList().iterator();
             it.hasNext(); ) {
            String finderExpr = (String) it.next();
            processFinder(rootGraphNode, finderExpr);
        }
    }

    protected void processFinder(GraphNode rootGraphNode, String finderExpr) {
        String rootName = rootGraphNode.getName();
        //System.out.println("finderExpr="+finderExpr);
        //
        // Parse finder expression: on _ find _ by _"
        //
        String onExpr = null;
        String findExpr = null;
        boolean isListFindExpr = false;
        String byExpr = null;
        StringTokenizer st = new StringTokenizer(finderExpr);
        while (st.hasMoreTokens()) {
            String token = st.nextToken().intern();
            if (token == "on")
                onExpr = st.nextToken();
            else if (token == "find") {
                findExpr = st.nextToken();
                isListFindExpr = false;
            } else if (token == "findall") {
                findExpr = st.nextToken();
                isListFindExpr = true;
            } else if (token == "by")
                byExpr = st.nextToken();
            else
                throw new IllegalArgumentException(Common.getMessage("MSG_BadTokenInFinder", token));
        }
        if (onExpr == null)
            throw new IllegalArgumentException(Common.getMessage("MSG_MissingOnExpression", finderExpr));

        if (onExpr.startsWith("/"+rootName)) {
            onExpr = onExpr.substring(rootName.length()+1);
            if (onExpr.startsWith("/"))
                onExpr = onExpr.substring(1);
        }
        //System.out.println("onExpr="+onExpr);
        GraphNode onNode = null;
        if (onExpr.equals("")) {
            // It's on the root
            onNode = rootGraphNode;
        } else {
            GraphLink gl = null;
            for (Iterator it = rootGraphNode.getGraphLink().xPathIterator(onExpr);
                 it.hasNext(); ) {
                gl = (GraphLink) it.next();
                if (gl == null)
                    break;
            }
            if (gl == null)
                throw new IllegalArgumentException(Common.getMessage("MSG_UnableToFindExpressionFromFinder", finderExpr));
            onNode = gl.element;
        }
        //System.out.println("onNode="+onNode);
        onNode.addExtraDataIncludeAlias(new Finder(findExpr, byExpr,
                                                   isListFindExpr));
    }

    protected void setSchemaType(GraphNode[] list) {
        Map nodeMap = new HashMap(list.length*4);
        for (int i  = 0; i < list.length; ++i) {
            nodeMap.put(list[i].getNameWithNamespace(), list[i]);
        }
        GraphNode emptyGraphNode = null;
        for (Iterator it = config.readBeanGraphs(); it.hasNext(); ) {
            org.netbeans.modules.schema2beansdev.beangraph.BeanGraph bg = (org.netbeans.modules.schema2beansdev.beangraph.BeanGraph) it.next();
            for (int i = 0; i < bg.sizeSchemaTypeMapping(); ++i) {
                org.netbeans.modules.schema2beansdev.beangraph.SchemaTypeMappingType stm = bg.getSchemaTypeMapping(i);
                String key;
                if (stm.getSchemaTypeNamespace() == null)
                    key = stm.getSchemaTypeName();
                else
                    key = "{"+stm.getSchemaTypeNamespace()+"}"+stm.getSchemaTypeName();
                if (nodeMap.containsKey(key)) {
                    GraphNode node = (GraphNode) nodeMap.get(key);
                    //System.out.println("Found match from beangraph: node="+node);
                    node.setJavaType(stm.getJavaType());
                    node.setCreated(false);
                    if (stm.isCanBeEmpty()) {
                        node.setExtendedProperty("can-be-empty", Boolean.TRUE);
                    }
                }
            }
        }
    }

    protected void generateInterface(String genDir, String packageName,
                                     String name, List methods,
                                     String comments,
                                     String extendsStatement) throws IOException {
        JavaWriter jw = new JavaWriter();
        jw.bigComment(comments+"\n\n@"+Common.GENERATED_TAG);
        jw.cr();
        if (!(packageName == null || "".equals(packageName))) {
            jw.writePackage(packageName);
            jw.cr();
        }
        jw.writeAccess(jw.PUBLIC);
        jw.write(" interface ");
        jw.write(name);
        jw.write(" ");
        if (extendsStatement != null)
            jw.write("extends ", extendsStatement, " ");
        jw.begin();
        for (Iterator methodsIterator = methods.iterator(); methodsIterator.hasNext(); ) {
            JavaWriter.Method method = (JavaWriter.Method) methodsIterator.next();
            method.writeMethod(jw);
            jw.eol();
            jw.cr();
        }
        jw.end();
        try {
            OutputStream out;
            out = config.getOutputStreamProvider().getStream(genDir, 
                                                        name, "java");	// NOI18N
            jw.writeTo(out);
            close(out);
        } catch(IOException ioe) {
            config.messageOut.println("Failed to generate interface: "+name);	// NOI18N
            TraceLogger.error(ioe);
            throw ioe;
        }
    }

    protected BeanGraph generateBeanGraph(GraphNode[] list) {
        BeanGraph bg = new BeanGraph();
        for (int i = 0; i < list.length; ++i) {
            GraphNode node = list[i];
            BeanElement be = (BeanElement)node.getObject();
            SchemaTypeMappingType stm =
                new SchemaTypeMappingType(node.getName(),
                                          be.getFullClassType());
            stm.setRoot(be.isRoot());
            stm.setBean(be.isBean());
            stm.setCanBeEmpty(be.getCanBeEmpty());
            stm.setSchemaTypeNamespace(node.getNamespace());
            bg.addSchemaTypeMapping(stm);
        }
        return bg;
    }

    /**
     * Generate a .dot file for the dot or dotty program to run with.
     * This is primarily useful for debugging the graph that the schema parser
     * created for us.  See http://www.graphviz.org
     */
    protected void generateDotGraph(Writer out, GraphNode node) throws IOException {
        out.write("digraph \""+node.getName()+"\" {\n");
        out.write("\t\""+node.getName()+"\" [shape=box]\n");
        out.write("\t\""+node.getName()+"\" -> \""+node.getGraphLink()+"\";\n");
        generateDotGraph(out, node.getGraphLink(), new HashMap());
        out.write("}\n");
    }

    protected void generateDotGraph(Writer out, List children, Map doneLinks) throws IOException {
        for (Iterator it = children.iterator(); it.hasNext(); ) {
            GraphLink l = (GraphLink) it.next();
            generateDotGraph(out, l, doneLinks);
        }
    }
    
    protected void generateDotGraph(Writer out, GraphLink l, Map doneLinks) throws IOException {
        if (l == null)
            return;
        doneLinks.put(l, null);
        out.write("\t\""+l+"\" [label=\""+dotGraphLabel(l)+"\"];\n");
        GraphNode node = l.element;
        if (node != null) {
            BeanBuilder.BeanElement be =
                (BeanBuilder.BeanElement)node.getObject();
            if (be == null)
                return;

            String type = be.getClassType();
            out.write("\t\""+node+"\" [label=\""+dotGraphLabel(node)+"\", shape=box];\n");
            out.write("\t\""+l+"\" -> \""+node+"\" [label=\"type of property\", color=darkgreen];\n");
            if ("#PCDATA".equals(l.name) && "String".equals(type)) {
                return;
            }
            AttrProp[] attrs = node.getAttributes();
            for (int i = 0; i < attrs.length; ++i) {
                String attrName = node.getName()+" attribute "+attrs[i].getName();
                out.write("\t\""+attrName+"\" [label=\""+dotGraphLabel(attrs[i])+"\", shape=egg];\n");
                out.write("\t\""+node+"\" -> \""+attrName+"\" [label=\"attribute\", color=magenta];\n");
            }

            GraphLink hasAttr = node.getGraphLink();
            if (hasAttr != null) {
                if (node.getMarked() == false ) {
                    if ((config.isTraceDot() || hasData(hasAttr)) && !doneLinks.containsKey(hasAttr)) {
                        out.write("\t\""+node+"\" -> \""+hasAttr+"\" [label=\"has attr\", color=purple];\n");
                        node.setMarked(true);
                        generateDotGraph(out, hasAttr, doneLinks);
                        node.setMarked(false);
                    }
                }
            }
        }

        List children = l.getChildren();
        for (Iterator childIt = children.iterator(); childIt.hasNext(); ) {
            GraphLink child = (GraphLink) childIt.next();
            out.write("\t\""+l+"\" -> \""+child+"\" [label=child, color=blue];\n");
        }
        generateDotGraph(out, children, doneLinks);

        /*
          GraphLink sibling = l.getSibling();
          if (sibling != null) {
          while (!config.isTraceDot() && sibling.name == null &&
          sibling.getFirstChild() == null &&
          sibling.getSibling() != null)
          sibling = sibling.getSibling();
          out.write("\t\""+l+"\" -> \""+sibling+"\" [label=sibling, color=red];\n");
          }
        */
    }

    private String dotGraphLabel(GraphLink l) {
        if (config.isTraceDot()) {
            String elementInstance = TreeBuilder.instanceToString(l.getElementInstance(), true);
            String groupInstance = TreeBuilder.instanceToString(l.getGroupInstance(), true);
            String result = "GraphLink@"+Integer.toHexString(l.hashCode());
            if (l.name == null)
                result += " (grouping)";
            else
                result += ":"+l.name;
            result += "\\n";
            if (!"".equals(elementInstance))
                result += " element: "+elementInstance;
            if (!"".equals(groupInstance))
                result += " group: "+groupInstance;
            if (l.isSequenceAnd())
                result += " ,";
            if (l.isSequenceOr())
                result += " |";
            return result;
        } else if (l.name == null)
            return "GraphLink";
        else
            return "property: "+l.name;
    }

    private String dotGraphLabel(GraphNode node) {
        String result;
        if (config.isTraceDot())
            result = "GraphNode@"+Integer.toHexString(node.hashCode())+":"+node.toString();
        else
            result = node.getName();
        if (node.getJavaType() != null)
            result += ":" + node.getJavaType();
        return result;
    }

    private String dotGraphLabel(AttrProp attr) {
        return attr.toString();
    }

    private boolean hasData(GraphLink l) {
        for (; l != null; l = l.getSibling()) {
            if (l.name != null)
                return true;
            if (l.element != null) {
                return true;
            }
            if (l.getFirstChild() != null)
                if (hasData(l.getFirstChild()))
                    return true;
        }
        return false;
    }

    protected void generateTagsFile(OutputStream out,
                                    String packageName, String className) throws IOException {
        JavaWriter jw = new JavaWriter();
        try {
            jw.bigComment("This class has all element and attribute names as constants.\n\n@"+Common.GENERATED_TAG);
            jw.cr();
            jw.writePackage(packageName);
            jw.cr();

            jw.writeClassDecl(className, null, null, jw.PUBLIC);
            jw.select(jw.DECL_SECTION);

            for (Iterator it = constNameMap.keySet().iterator(); it.hasNext(); ) {
                String constName = (String) it.next();
                String dtdName = (String) constNameMap.get(constName);
                jw.write("public final static String ", constName, " = ");
                jw.writeEol("\"", dtdName, "\"");
            }
            jw.cr();

            jw.select(jw.CONSTRUCTOR_SECTION);
            jw.comment("This class is not to be instantiated.");
            jw.beginConstructor(className, "", null, jw.PRIVATE);
            jw.end();

            jw.writeTo(out);
        } finally {
            jw.close();
        }
    }

    /**
     * Search in the MetaDD @mdd for @dtdName
     */
    private MetaElement getMetaElement(MetaDD mdd, String dtdName) {
        return getMetaElement(mdd, dtdName, null);
    }

    private MetaElement getMetaElement(MetaDD mdd, String dtdName, String namespace) {
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

    private void addToImplements(MetaElement e, String interfce) {
        String implList = e.getImplements();
        if (implList == null) {
            e.setImplements(interfce);
            return;
        }
        //
        // Check to see if we already have this one.
        //
        implList = implList.trim();
        int pos = implList.indexOf(',');
        while (pos >= 0) {
            String impl = implList.substring(0, pos);
            impl = impl.trim();
            if (impl.equals(interfce))
                return;
            implList = implList.substring(pos+1);
            implList = implList.trim();
            pos = implList.indexOf(',');
        }
        if (implList.equals(interfce))
            return;

        //
        // It's a new interface, add it on in.
        //
        e.setImplements(e.getImplements()+", "+interfce);
    }

    private void addToBeanInterfaceExtends(MetaElement e, String interfce) {
        String implList = e.getBeanInterfaceExtends();
        if (implList == null) {
            e.setBeanInterfaceExtends(interfce);
            return;
        }
        //
        // Check to see if we already have this one.
        //
        implList = implList.trim();
        int pos = implList.indexOf(',');
        while (pos >= 0) {
            String impl = implList.substring(0, pos);
            impl = impl.trim();
            if (impl.equals(interfce))
                return;
            implList = implList.substring(pos+1);
            implList = implList.trim();
            pos = implList.indexOf(',');
        }
        if (implList.equals(interfce))
            return;

        //
        // It's a new interface, add it on in.
        //
        e.setBeanInterfaceExtends(e.getBeanInterfaceExtends()+", "+interfce);
    }

    protected void close(OutputStream out) throws java.io.IOException {
        out.close();
        if (!config.isQuiet() && config.isTraceGen() &&
            out instanceof org.netbeans.modules.schema2beansdev.gen.WriteIfDifferentOutputStream) {
            org.netbeans.modules.schema2beansdev.gen.WriteIfDifferentOutputStream widos = (org.netbeans.modules.schema2beansdev.gen.WriteIfDifferentOutputStream) out;
            if (!widos.isChanged())
                config.messageOut.println(Common.getMessage("MSG_DidNotChangeFile"));
        }
    }

    protected void close(Writer out) throws java.io.IOException {
        out.close();
    }
}
