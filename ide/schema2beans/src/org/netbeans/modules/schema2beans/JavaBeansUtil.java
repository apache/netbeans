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

package org.netbeans.modules.schema2beans;

import java.io.*;
import java.beans.*;
import java.lang.reflect.*;
import java.util.*;
import org.w3c.dom.*;

/**
 * @author Cliff Draper
 *
 * This is a utility class with several static methods for working
 * with generic java beans.  There are methods here for writing a java
 * bean out {link writeBeanProperty}, for reading a java bean
 * from XML {link readBean}, and for copying one java bean to
 * another {link copyBean}.
 */
public class JavaBeansUtil {
    private JavaBeansUtil() {}
    
    /**
     * This is the normal top level method for taking a Java Bean and
     * dumping it as XML.
     * @param obj the java bean to write out
     * @param out the place to write it to
     * @param propertyName the top level property
     */
    public static void writeBeanProperty(Object obj, Writer out, String propertyName) throws IOException, java.beans.IntrospectionException {
        BeanWriter beanOut = new XmlBeanWriter(out);
        writeBeanProperty(obj, beanOut, propertyName);
    }

    /**
     * If you want to write the java bean out in a different way from
     * the standard XML writer, use this method.
     */
    public static void writeBeanProperty(Object obj, BeanWriter out, String propertyName) throws IOException, java.beans.IntrospectionException {
        writeBeanProperty(obj, out, new HashMap(), propertyName);
    }

    /**
     * BeanWriter is the generic way of writing a java bean out.
     */
    public static interface BeanWriter {
        public void beginPropertyName(String propertyName) throws IOException;
        public void endPropertyName(String propertyName) throws IOException;
        public void writeLeafObject(Object obj) throws IOException;
        public void beginInnerNode() throws IOException;
        public void endInnerNode() throws IOException;
    }

    /**
     * The methods in this class will do proper XML indention for you.
     */
    public abstract static class IndentingBeanWriter implements BeanWriter {
        protected String indentBy;
        protected String indent;
        protected int indentLevel = 0;
        protected List indentions;

        public IndentingBeanWriter() {
            this("", "\t");
        }
        
        public IndentingBeanWriter(String indentBy) {
            this("", indentBy);
        }
        
        public IndentingBeanWriter(String indent, String indentBy) {
            this.indent = indent;
            this.indentBy = indentBy;
            this.indentions = new ArrayList();
            this.indentions.add(indent);  // indentLevel 0
        }
        
        public void beginInnerNode() throws IOException {
            ++indentLevel;
            int indentionsSize = indentions.size();
            if (indentionsSize <= indentLevel) {
                indent = (String) indentions.get(indentionsSize-1);
                do {
                    indent += indentBy;
                    indentions.add(indent);
                    ++indentionsSize;
                } while (indentionsSize <= indentLevel);
            } else {
                indent = (String) indentions.get(indentLevel);
            }
		}

        public void endInnerNode() throws IOException {
            --indentLevel;
            indent = (String) indentions.get(indentLevel);
		}
    }

    public static class XmlBeanWriter extends IndentingBeanWriter implements BeanWriter {
        protected Writer out;

        public XmlBeanWriter(Writer out) {
            super();
            this.out = out;
        }
        
        public XmlBeanWriter(Writer out, String indentBy) {
            super(indentBy);
            this.out = out;
        }
        
        public XmlBeanWriter(Writer out, String indent, String indentBy) {
            super(indent, indentBy);
            this.out = out;
        }
        
        public void beginPropertyName(String propertyName) throws IOException {
            out.write(indent);
            out.write("<"+propertyName+">");
		}

        public void endPropertyName(String propertyName) throws IOException {
            out.write("</"+propertyName+">\n");
		}

        public void writeLeafObject(Object obj) throws IOException {
            XMLUtil.printXML(out, obj.toString(), false);
		}

        public void beginInnerNode() throws IOException {
            super.beginInnerNode();
            out.write("\n");
		}

        public void endInnerNode() throws IOException {
            super.endInnerNode();
            out.write(indent);
		}
    }
    
    public static class HtmlBeanWriter extends IndentingBeanWriter implements BeanWriter {
        protected Writer out;

        public HtmlBeanWriter(Writer out) {
            super();
            this.out = out;
        }
        
        public HtmlBeanWriter(Writer out, String indentBy) {
            super(indentBy);
            this.out = out;
        }
        
        public HtmlBeanWriter(Writer out, String indent, String indentBy) {
            super(indent, indentBy);
            this.out = out;
        }
        
        public void beginPropertyName(String propertyName) throws IOException {
            out.write(indent);
            out.write("<tr>");
            out.write("<th>"+propertyName+"</th>");
		}

        public void endPropertyName(String propertyName) throws IOException {
            out.write("</tr>");
            out.write("\n");
		}

        public void writeLeafObject(Object obj) throws IOException {
            // leaf - write a table cell
            out.write("<td>");
            XMLUtil.printXML(out, obj.toString(), false);
            out.write("</td>");
		}

        public void beginInnerNode() throws IOException {
            super.beginInnerNode();
            // inner node - write a nested table
            out.write("<td><table width=\"100%\" border=\"1\">");
            out.write("\n");
		}

        public void endInnerNode() throws IOException {
            super.endInnerNode();
            out.write(indent);
            out.write("</table></td>");
		}
    }
    
    public static void writeBeanProperty(Object obj, BeanWriter out, Map skipChildren, String propertyName) throws IOException, java.beans.IntrospectionException {
        if (obj == null)
            return;
        out.beginPropertyName(propertyName);
        if (!isJavaBeanType(obj.getClass())) {
            // leaf
            out.writeLeafObject(obj);
        } else {
            // inner node
            out.beginInnerNode();
            writeBean(obj, out, skipChildren);
            out.endInnerNode();
        }
        out.endPropertyName(propertyName);
    }

    /**
     * This method is similar to writeBeanProperty except that the
     * outer property name is not known and not printed.
     */
    public static void writeBean(Object obj, Writer out) throws IOException, java.beans.IntrospectionException {
        BeanWriter beanOut = new XmlBeanWriter(out);
        writeBean(obj, beanOut);
    }
    
    public static void writeBean(Object obj, BeanWriter out) throws IOException, java.beans.IntrospectionException {
        writeBean(obj, out, new HashMap());
    }
    
    public static void writeBean(Object obj, BeanWriter out, Map skipChildren) throws IOException, java.beans.IntrospectionException {
        //System.out.println("obj="+obj);
        if (obj == null)
            return;
        // Make sure that we don't get into an infinite loop
        if (skipChildren.containsKey(obj))
            return;
        skipChildren.put(obj, null);

        Class objCls = obj.getClass();
        /*
        if (obj instanceof Collection) {
            System.out.println("Hit Collection");
            return;
        }
        if (objCls.isArray()) {
            System.out.println("Hit Array");
            return;
        }
        */

        BeanInfo bi = Introspector.getBeanInfo(objCls);
        PropertyDescriptor[] pds = bi.getPropertyDescriptors();
        for (int i = 0 ; i < pds.length; ++i) {
            PropertyDescriptor pd = pds[i];
            Method reader = pd.getReadMethod();
            if (reader == null)
                continue;
            Class propertyType = pd.getPropertyType();
            String propertyName = pd.getName();
            //System.out.println("pd.getName="+propertyName+" pd.getPropertyType="+pd.getPropertyType());
            //System.out.println("reader="+reader);
            Class declaringClass = reader.getDeclaringClass();
            //System.out.println("reader.declaring="+declaringClass);
            // Need to handle indexed properties
            if (declaringClass.equals(Object.class))
                continue;
            if (propertyType == null)
                continue;
            Object childObj = null;
            try {
                childObj = reader.invoke(obj, null);
            } catch (java.lang.reflect.InvocationTargetException e) {
                e.printStackTrace();
            } catch (java.lang.IllegalAccessException e) {
                e.printStackTrace();
            }
            if (childObj != null) {
                if (childObj instanceof Collection) {
                    //System.out.println("childObj is Collection");
                    Iterator it = ((Collection)childObj).iterator();
                    while (it.hasNext()) {
                        Object childElement = it.next();
                        writeBeanProperty(childElement, out, skipChildren,
                                          propertyName);
                    }
                } else if (childObj.getClass().isArray()) {
                    //System.out.println("childObj is Array");
                    int size = Array.getLength(childObj);
                    for (int j = 0; j < size; ++j) {
                        Object childElement = Array.get(childObj, j);
                        writeBeanProperty(childElement, out, skipChildren,
                                          propertyName);
                    }
                } else {
                    writeBeanProperty(childObj, out, skipChildren,
                                      propertyName);
                }
            }
        }
        skipChildren.remove(obj);
    }

    /**
     * Construct a new cls and fill in it's contents from in.
     *
     * @param cls the Class to construct from the XML.
     * @param in the source of the XML.
     */
	public static Object readBean(Class cls, java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException, java.beans.IntrospectionException, java.lang.NoSuchMethodException, java.lang.InstantiationException, java.lang.IllegalAccessException, java.lang.reflect.InvocationTargetException {
        Constructor construct = cls.getConstructor(new Class[0]);
        Object newValue = construct.newInstance(new Object[0]);
        readBean(newValue, in);
        return newValue;
    }

    /**
     * Fill in the contents of obj from the XML that's in in.
     *
     * @param obj the object to fill in from the XML.
     * @param in the source of the XML.
     */
	public static void readBean(Object obj, java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException, java.beans.IntrospectionException {
        readBean(obj, new org.xml.sax.InputSource(in), false, null, null);
	}

	/**
     * Warning: in readNoEntityResolver character and entity references will
     * not be read from any DTD in the XML source.
     * However, this way is faster since no DTDs are looked up
     * (possibly skipping network access) or parsed.
     */
	public static void readBeanNoEntityResolver(Object obj, java.io.InputStream in) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException, java.beans.IntrospectionException {
        readBean(obj, new org.xml.sax.InputSource(in), false,
                 new org.xml.sax.EntityResolver() {
                         public org.xml.sax.InputSource resolveEntity(String publicId, String systemId) {
                             java.io.ByteArrayInputStream bin = new java.io.ByteArrayInputStream(new byte[0]);
                             return new org.xml.sax.InputSource(bin);
                         }
                     }
                 , null);
	}

	public static void readBean(Object obj, org.xml.sax.InputSource in, boolean validate, org.xml.sax.EntityResolver er, org.xml.sax.ErrorHandler eh) throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, java.io.IOException, java.beans.IntrospectionException {
		javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
		dbf.setValidating(validate);
		javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
		if (er != null)	db.setEntityResolver(er);
		if (eh != null)	db.setErrorHandler(eh);
		org.w3c.dom.Document doc = db.parse(in);
        readBean(obj, doc);
    }

    /**
     * Construct a new cls and fill in it's contents from document.
     *
     * @param cls the Class to construct from the XML.
     * @param document the source of the XML.
     */
    public static Object readBean(Class cls, org.w3c.dom.Document document) throws java.beans.IntrospectionException, java.lang.NoSuchMethodException, java.lang.InstantiationException, java.lang.IllegalAccessException, java.lang.reflect.InvocationTargetException {
        Constructor construct = cls.getConstructor(new Class[0]);
        Object newValue = construct.newInstance(new Object[0]);
        readBean(newValue, document);
        return newValue;
    }
    
    /**
     * Fill in the contents of obj from the XML that's in document.
     *
     * @param obj the object to fill in from the XML.
     * @param document the source of the XML.
     */
    public static void readBean(Object obj, org.w3c.dom.Document document) throws java.beans.IntrospectionException {
        readBean(obj, document.getDocumentElement());
    }
    
    public static void readBean(Object obj, Node node) throws java.beans.IntrospectionException {
        if (obj == null)
            return;
        int errorCount = 0;
        Class objCls = obj.getClass();
        BeanInfo bi = Introspector.getBeanInfo(objCls);
        PropertyDescriptor[] pds = bi.getPropertyDescriptors();
        Map propertyWriters = new HashMap();	// <String, Method>
        Map propertyTypes = new HashMap();	// <String, Class>
        for (int i = 0 ; i < pds.length; ++i) {
            PropertyDescriptor pd = pds[i];
            Method writer = pd.getWriteMethod();
            if (writer == null)
                continue;
            Class propertyType = pd.getPropertyType();
            String propertyName = pd.getName();
            Class declaringClass = writer.getDeclaringClass();
            //System.out.println("pd.getName="+propertyName+" pd.getPropertyType="+pd.getPropertyType()+" declaringClass="+declaringClass);
            if (declaringClass == null || declaringClass.equals(Object.class))
                continue;
            if (propertyType == null)
                continue;
            propertyWriters.put(propertyName, writer);
            propertyTypes.put(propertyName, propertyType);
        }

        Map propertiesNewValues = new HashMap();
		if (node.hasAttributes()) {
			org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
            for (int i = 0; i < attrs.getLength(); ++i) {
                Attr attr = (Attr) attrs.item(i);
                String attrName = attr.getName();
                if (!propertyWriters.containsKey(attrName)) {
                    attrName = Common.convertName(attrName);
                    if (!propertyWriters.containsKey(attrName)) {
                        attrName = Introspector.decapitalize(attrName);
                        if (!propertyWriters.containsKey(attrName)) {
                            ++errorCount;
                            System.out.println("Found attribute and did not find property in Java Bean: "+attr.getName());
                            continue;
                        }
                    }
                }
                Object newValue = convertValue((Class)propertyTypes.get(attrName),
                                               attr.getValue());
                propertiesNewValues.put(attrName, newValue);
            }
        }
		org.w3c.dom.NodeList children = node.getChildNodes();
		for (int i = 0, size = children.getLength(); i < size; ++i) {
			org.w3c.dom.Node childNode = children.item(i);
            if (!(childNode instanceof Element))
                continue;
			String childNodeName = (childNode.getLocalName() == null ? childNode.getNodeName().intern() : childNode.getLocalName().intern());
            //System.out.println("Found child named "+childNodeName);
            if (!propertyWriters.containsKey(childNodeName)) {
                childNodeName = Common.convertName(childNodeName);
                if (!propertyWriters.containsKey(childNodeName)) {
                    childNodeName = Introspector.decapitalize(childNodeName);
                    if (!propertyWriters.containsKey(childNodeName)) {
                        ++errorCount;
                        System.out.println("Found element and did not find property in Java Bean: "+childNode.getNodeName());
                        continue;
                    }
                }
            }
            Class propertyType = (Class) propertyTypes.get(childNodeName);
            Object newValue = null;
            if (isJavaBeanType(propertyType)) {
                Class propertyTypeOnce = propertyType;
                if (propertyType.isArray())
                    propertyTypeOnce = propertyType.getComponentType();
                try {
                    //System.out.println("It's a Java Bean type");
                    Constructor construct = propertyTypeOnce.getConstructor(new Class[0]);
                    newValue = construct.newInstance(new Object[0]);
                    readBean(newValue, childNode);
                } catch (java.lang.NoSuchMethodException e) {
                    e.printStackTrace();
                    ++errorCount;
                } catch (java.lang.InstantiationException e) {
                    e.printStackTrace();
                    ++errorCount;
                } catch (java.lang.IllegalAccessException e) {
                    e.printStackTrace();
                    ++errorCount;
                } catch (java.lang.reflect.InvocationTargetException e) {
                    e.printStackTrace();
                    ++errorCount;
                }
            } else {
                String nodeValue;
                if (childNode.getFirstChild() == null)
                    nodeValue = "";
                else
                    nodeValue = childNode.getFirstChild().getNodeValue();
                Class typeOfNewValue = propertyType;
                if (propertyType.isArray())
                    typeOfNewValue = propertyType.getComponentType();
                newValue = convertValue(typeOfNewValue, nodeValue);
            }
            //System.out.println("newValue="+newValue+" newValue.class="+newValue.getClass()+" propertyType="+propertyType.getName());
            if (propertyType.isArray()) {
                List values = (List) propertiesNewValues.get(childNodeName);
                if (values == null) {
                    values = new ArrayList();
                    propertiesNewValues.put(childNodeName, values);
                }
                values.add(newValue);
            } else {
                propertiesNewValues.put(childNodeName, newValue);
            }
        }

        for (Iterator it = propertiesNewValues.keySet().iterator();
             it.hasNext(); ) {
            String propertyName = (String) it.next();
            Class propertyType = (Class) propertyTypes.get(propertyName);
            Method writer = (Method) propertyWriters.get(propertyName);

            //System.out.println("propertyName="+propertyName+" writer="+writer);
            Object newValue;
            if (propertyType.isArray()) {
                // convert our List into an array
                List values = (List) propertiesNewValues.get(propertyName);
                newValue = Array.newInstance(propertyType.getComponentType(),
                                             values.size());
                //newValue = values.toArray((Object[])newValue);
                for (int i = 0; i < values.size(); ++i) {
                    //System.out.println("i="+i+" values.get(i)="+values.get(i)+" values.get(i).class="+values.get(i).getClass());
                    Array.set(newValue, i, values.get(i));
                }
            } else {
                newValue = propertiesNewValues.get(propertyName);
            }

            //System.out.println("newValue="+newValue+" newValue.class="+newValue.getClass());
            try {
                writer.invoke(obj, new Object[] {newValue});
            } catch (java.lang.reflect.InvocationTargetException e) {
                e.printStackTrace();
                ++errorCount;
            } catch (java.lang.IllegalAccessException e) {
                e.printStackTrace();
                ++errorCount;
            }
        }
    }


    /**
     * Find matching properties between these 2 beans and copy over the
     * contents.
     */
    public static void copyBean(Object src, Object dest) throws java.beans.IntrospectionException {
        copyBean(src, dest, Collections.emptyMap());
    }
    
    /**
     * Find matching properties between these 2 beans and copy over the
     * contents.
     *
     * @param nameMapping maps property names from the src object to the dest object.
     */
    public static void copyBean(Object src, Object dest, Map nameMapping) throws java.beans.IntrospectionException {
        if (src == null)
            return;
        Class srcCls = src.getClass();
        BeanInfo bi = Introspector.getBeanInfo(srcCls);
        PropertyDescriptor[] pds = bi.getPropertyDescriptors();
        Map propertyReaders = new HashMap();	// <String, Method>
        Map propertyTypes = new HashMap();	// <String, Class>
        for (int i = 0 ; i < pds.length; ++i) {
            PropertyDescriptor pd = pds[i];
            Method reader = pd.getReadMethod();
            if (reader == null)
                continue;
            Class propertyType = pd.getPropertyType();
            String propertyName = pd.getName();
            Class declaringClass = reader.getDeclaringClass();
            //System.out.println("pd.getName="+propertyName+" pd.getPropertyType="+pd.getPropertyType()+" declaringClass="+declaringClass);
            if (declaringClass == null || declaringClass.equals(Object.class))
                continue;
            if (propertyType == null)
                continue;
            if (nameMapping.containsKey(propertyName))
                propertyName = (String) nameMapping.get(propertyName);
            propertyReaders.put(propertyName, reader);
            propertyTypes.put(propertyName, propertyType);
        }

        Class destCls = dest.getClass();
        bi = Introspector.getBeanInfo(destCls);
        pds = bi.getPropertyDescriptors();
        for (int i = 0 ; i < pds.length; ++i) {
            PropertyDescriptor pd = pds[i];
            Method writer = pd.getWriteMethod();
            if (writer == null)
                continue;
            Class propertyType = pd.getPropertyType();
            String propertyName = pd.getName();
            Class declaringClass = writer.getDeclaringClass();
            if (declaringClass == null || declaringClass.equals(Object.class))
                continue;
            if (propertyType == null)
                continue;
            if (propertyReaders.containsKey(propertyName)) {
                // Found a common property
                //System.out.println("copying propertyName="+propertyName);
                try {
                    Method reader = (Method) propertyReaders.get(propertyName);
                    Object srcValue = reader.invoke(src, null);
                    //System.out.println("srcValue="+srcValue);
                    if (isJavaBeanType(propertyType)) {
                        Class propertyTypeOnce = propertyType;
                        int size = 1;
                        Object destValue = null;
                        if (propertyType.isArray()) {
                            propertyTypeOnce = propertyType.getComponentType();
                            size = Array.getLength(srcValue);
                            destValue = Array.newInstance(propertyTypeOnce, size);
                        }
                        for (int index = 0; index < size; ++index) {
                            Constructor construct = propertyTypeOnce.getConstructor(new Class[0]);
                            Object srcValueOnce;
                            Object destValueOnce = construct.newInstance(new Object[0]);
                            if (propertyType.isArray()) {
                                Array.set(destValue, index, destValueOnce);
                                srcValueOnce = Array.get(srcValue, index);
                            } else {
                                destValue = destValueOnce;
                                srcValueOnce = srcValue;
                            }
                            copyBean(srcValueOnce, destValueOnce, nameMapping);
                            //System.out.println("destValueOnce.class="+destValueOnce.getClass());
                            //System.out.println("destValueOnce="+destValueOnce);
                        }
                        writer.invoke(dest, new Object[] {destValue});
                    } else if (propertyType.isAssignableFrom((Class)propertyTypes.get(propertyName))) {
                        writer.invoke(dest, new Object[] {srcValue});
                    } else {
                        //System.out.println("Wrong type!");
                        continue;
                    }
                } catch (java.lang.IllegalAccessException e) {
                    e.printStackTrace();
                } catch (java.lang.reflect.InvocationTargetException e) {
                    e.printStackTrace();
                } catch (java.lang.NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (java.lang.InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Determine if type is a java bean or not.  This is a tough
     * decision.  This method is called to figure out at what types to
     * stop recursing in the bean graph.  Any type that is not simple
     * enough is called a java bean.
     */
    public static boolean isJavaBeanType(Class type) {
        if (Collection.class.isAssignableFrom(type))
            return false;
        if (type.isArray()) {
            return isJavaBeanType(type.getComponentType());
        }
        String typeName = type.getName().intern();
        if (typeName == "java.lang.String"||
            typeName == "java.lang.Integer"|| typeName == "int" ||
            typeName == "java.lang.Character"|| typeName == "char" ||
            typeName == "java.lang.Long"|| typeName == "long" ||
            typeName == "java.lang.Float"|| typeName == "float" ||
            typeName == "java.lang.Double"|| typeName == "double" ||
            typeName == "java.lang.Boolean"|| typeName == "boolean" ||
            typeName == "java.lang.Short"|| typeName == "short" ||
            typeName == "java.lang.Byte"|| typeName == "byte" ||
            typeName == "java.math.BigDecimal"||
            typeName == "java.math.BigInteger"||
            typeName == "java.lang.Object" ||
            typeName == "java.util.Calendar" || typeName == "java.util.Date" ||
            typeName == "java.util.GregorianCalendar" ||
            typeName == "javax.xml.namespace.QName" ||
            typeName == "java.net.URL" || typeName == "java.net.URI")
            return false;
        return true;
    }

    /**
     * Convert a textual representation of type stored in value into a
     * java object.  If we don't know the type, then look for a
     * constructor that takes a single String as a parameter.
     */
    public static Object convertValue(Class type, String value) {
        String typeName = type.getName().intern();
        if (typeName == "java.lang.String")
            return value;
        if (typeName == "java.lang.Boolean" || typeName == "boolean")
            return Boolean.valueOf(value);
        if (typeName == "java.lang.Integer" || typeName == "int")
            return Integer.valueOf(value);
        if (typeName == "java.lang.Long" || typeName == "long")
            return Long.valueOf(value);
        if (typeName == "java.lang.Float" || typeName == "float")
            return Float.valueOf(value);
        if (typeName == "java.lang.Double" || typeName == "double")
            return Double.valueOf(value);
        if (typeName == "java.lang.Byte" || typeName == "byte")
            return Byte.valueOf(value);
        if (typeName == "java.lang.Short" || typeName == "short")
            return Short.valueOf(value);
        if (typeName == "java.lang.Character" || typeName == "char")
            return Character.valueOf(value.charAt(0));
        if (typeName == "java.net.URL") {
            try {
                return new java.net.URL(value);
            } catch (java.net.MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        if (typeName == "java.net.URI") {
            try {
                return new java.net.URI(value);
            } catch (java.net.URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        if (typeName == "java.math.BigDecimal")
            return new java.math.BigDecimal(value);
        if (typeName == "java.math.BigInteger")
            return new java.math.BigInteger(value);
        if (typeName == "java.util.Calendar") {
            // For XML Schema, we're trying to parse ISO8601 date time format
            // CCYY-MM-DDThh:mm:ss+TZ:TZ
            // 1999-05-31T13:20:00-05:00
            // For -5 time zone, should use
            // TimeZone.getAvailableIDs(-5*60*60*1000)
            TimeZone tz = TimeZone.getDefault();
            Calendar cal = Calendar.getInstance(tz);
            Date date = null;
            String[] possibleFormats = {"yyyy-MM-dd'T'HH:mm:ss.S", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd"};	// NOI18N
            java.text.ParsePosition pos = null;
            for (int formatNum = 0; formatNum < possibleFormats.length; ++formatNum) {
                pos = new java.text.ParsePosition(0);
                java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(possibleFormats[formatNum]);
                formatter.setCalendar(cal);
                date = formatter.parse(value, pos);
                if (date != null) {
                    break;
                }
            }
            if (date == null) {
                throw new java.lang.RuntimeException(Common.getMessage("MSG_BadParse", value));
            }
            cal.setTime(date);
            return cal;
        }
        
        Constructor c = null;
        
        try {
            Class[] cc = new Class[] {java.lang.String.class};
            c = type.getDeclaredConstructor(cc);
            Object[] p = new Object[] {value};
            return c.newInstance(p);
        } catch (NoSuchMethodException me) {
            throw new RuntimeException(me);
        } catch (java.lang.InstantiationException e) {
            throw new RuntimeException(e);
        } catch (java.lang.IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Used for generating Java code for reading complicated types.
     * @param typeName  the java type to generate the method about, can also
     *                  be "base64Binary".
     */
    public static void genReadType(Writer out, String typeName) throws IOException {
        typeName = typeName.intern();
        if (typeName == "java.util.Calendar") {
            out.write("public static java.util.Calendar stringToCalendar(String value) throws java.text.ParseException {\n");
            out.write("java.util.TimeZone tz = java.util.TimeZone.getDefault();\n");
            out.write("java.util.Calendar cal = java.util.Calendar.getInstance(tz);\n");
            out.write("java.util.Date date = null;\n");
            out.write("String[] possibleFormats = {\"yyyy-MM-dd'T'HH:mm:ss.S\", \"yyyy-MM-dd'T'HH:mm:ss\", \"yyyy-MM-dd\"};	// NOI18N\n");
            out.write("java.text.ParsePosition pos = null;\n");
            out.write("for (int formatNum = 0; formatNum < possibleFormats.length; ++formatNum) {\n");
			out.write("pos = new java.text.ParsePosition(0);\n");
            out.write("java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(possibleFormats[formatNum]);\n");
            out.write("formatter.setCalendar(cal);\n");
            out.write("date = formatter.parse(value, pos);\n");
            out.write("if (date != null) {\n");
            out.write("break;\n");
            out.write("}\n");
            out.write("}\n");
            out.write("if (date == null) {\n");
            out.write("throw new java.text.ParseException(\"Bad time/date parse of \"+value, pos.getErrorIndex());\n");
            out.write("}\n");
            out.write("int len = value.length();\n");
            out.write("if (pos.getIndex() < len) {\n");
            out.write("if (value.charAt(pos.getIndex()) == 'Z') {\n");
            out.write("// The Timezone is UTC\n");
            out.write("tz = java.util.TimeZone.getTimeZone(\"GMT\");\n");
            out.write("cal.setTimeZone(tz);\n");
            out.write("} else {\n");
            out.write("tz = java.util.TimeZone.getTimeZone(\"GMT\"+value.substring(pos.getIndex(), len));\n");
            out.write("cal.setTimeZone(tz);\n");
            out.write("}\n");
            out.write("}\n");
            out.write("cal.setTime(date);\n");
            out.write("return cal;\n");
            out.write("}\n");
        } else if (typeName == "base64Binary") {
            out.write("public static byte[] decodeBase64BinaryString(String text) {\n");
            out.write("final int decodeBase64Table[] = {62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};\n");
            out.write("StringBuffer cleanedEncoding = new StringBuffer();\n");
            out.write("int len = text.length();\n");
            out.write("// Get rid of extraneous characters (like whitespace).\n");
            out.write("for (int i = 0; i < len; ++i) {\n");
            out.write("if (text.charAt(i) > 0x20) {\n");
            out.write("cleanedEncoding.append(text.charAt(i));\n");
            out.write("}\n");
            out.write("}\n");
            out.write("char[] encodedText = cleanedEncoding.toString().toCharArray();\n");
            out.write("len = encodedText.length;\n");
            out.write("if (len == 0) {\n");
            out.write("return new byte[0];\n");
            out.write("}\n");
            out.write("int howManyBlocks = len / 4;\n");
            out.write("int partialLen = 3;\n");
            out.write("if (encodedText[len-1] == '=') {\n");
            out.write("partialLen -= 1;\n");
            out.write("if (encodedText[len-2] == '=') {\n");
            out.write("partialLen -= 1;\n");
            out.write("}\n");
            out.write("}\n");
            out.write("int resultLen = partialLen + (howManyBlocks - 1) * 3;\n");
            out.write("byte[] result = new byte[resultLen];\n");
            out.write("int resultIndex = 0;\n");
            out.write("int encodedTextIndex = 0;\n");
            out.write("for (int blockNum = 0; blockNum < howManyBlocks; ++blockNum) {\n");
            out.write("int a = decodeBase64Table[encodedText[encodedTextIndex++] - '+'];\n");
            out.write("int b = decodeBase64Table[encodedText[encodedTextIndex++] - '+'];\n");
            out.write("int c = decodeBase64Table[encodedText[encodedTextIndex++] - '+'];\n");
            out.write("int d = decodeBase64Table[encodedText[encodedTextIndex++] - '+'];\n");
            
            out.write("result[resultIndex++] = (byte) ( (b >> 4) | (a << 2) );\n");
            out.write("if (resultIndex < resultLen) {\n");
            out.write("result[resultIndex++] = (byte) ( ((b & 0xf) << 4) | (c >> 2) );\n");
            out.write("}\n");
            out.write("if (resultIndex < resultLen) {\n");
            out.write("result[resultIndex++] = (byte) ( ((c & 0x3) << 6) | d);\n");
            out.write("}\n");
            out.write("}\n");
            out.write("return result;\n");
            out.write("}\n");
        }
    }

    /**
     * Used for generating Java code for writing complicated types.
     * @param typeName  the java type to generate the method about, can also
     *                  be "base64Binary".
     */
    public static void genWriteType(Writer out, String typeName) throws IOException {
        typeName = typeName.intern();
        if (typeName == "java.util.Calendar") {
            out.write("public static String calendarToString(java.util.Calendar cal) {\n");
            out.write("java.util.Date date = cal.getTime();\n");
            out.write("java.text.SimpleDateFormat formatter;\n");
            out.write("if (cal.get(java.util.Calendar.HOUR) == 0 && cal.get(java.util.Calendar.MINUTE) == 0 && cal.get(java.util.Calendar.SECOND) == 0) {\n");
            out.write("formatter = new java.text.SimpleDateFormat(\"yyyy-MM-dd\");	// NOI18N\n");
            out.write("} else if (cal.get(java.util.Calendar.MILLISECOND) == 0) {\n");
            out.write("formatter = new java.text.SimpleDateFormat(\"yyyy-MM-dd'T'HH:mm:ss\");	// NOI18N\n");
            out.write("} else {\n");
            out.write("formatter = new java.text.SimpleDateFormat(\"yyyy-MM-dd'T'HH:mm:ss.S\");	// NOI18N\n");
            out.write("}\n");
            out.write("String result = formatter.format(date);\n");
            out.write("if (java.util.TimeZone.getDefault().hasSameRules(cal.getTimeZone())) {\n");
            out.write("return result;\n");
            out.write("}\n");
            out.write("int offset = cal.getTimeZone().getOffset(0);\n");
            out.write("if (offset == 0) {\n");
            out.write("return result+\"Z\";\n");
            out.write("}\n");
            out.write("int seconds = offset / 1000;\n");
            out.write("if (seconds > 0) {\n");
            out.write("result += \"+\";\n");
            out.write("} else {\n");
            out.write("seconds = -1 * seconds;\n");
            out.write("result += \"-\";\n");
            out.write("}\n");
            out.write("int hours = seconds / 3600;\n");
            out.write("if (hours < 10) {\n");
            out.write("result += \"0\";\n");
            out.write("}\n");
            out.write("result += hours + \":\";\n");
            out.write("int minutes = (seconds / 60) % 60;\n");
            out.write("if (minutes < 10) {\n");
            out.write("result += \"0\";\n");
            out.write("}\n");
            out.write("result += minutes;\n");
            out.write("return result;\n");
            out.write("}\n");
        } else if (typeName == "base64Binary") {
            out.write("public static String encodeBase64BinaryString(byte[] instance) {\n");
            out.write("final char encodeBase64Table[] = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};\n");
            out.write("byte[] value = (byte[]) instance;\n");
            out.write("int len = value.length;\n");
            out.write("if (len == 0) {\n");
            out.write("return \"\";\n");
            out.write("}\n");
            out.write("int howManyBlocks = len / 3;\n");
            out.write("int partialLen = len % 3;\n");
            out.write("if (partialLen != 0) {\n");
            out.write("howManyBlocks += 1;\n");
            out.write("}\n");
            out.write("int resultLen = howManyBlocks * 4;\n");
            out.write("StringBuffer result = new StringBuffer(resultLen);\n");
            out.write("int valueIndex = 0;\n");
            out.write("for (int blockNum = 0; blockNum < howManyBlocks; ++blockNum) {\n");
            out.write("int a = value[valueIndex++];\n");
            out.write("int b;\n");
            out.write("int c;\n");
            out.write("if (valueIndex < len) {\n");
            out.write("b = value[valueIndex++];\n");
            out.write("} else {\n");
            out.write("b = 0;\n");
            out.write("}\n");
            out.write("if (valueIndex < len) {\n");
            out.write("c = value[valueIndex++];\n");
            out.write("} else {\n");
            out.write("c = 0;\n");
            out.write("}\n");
            out.write("if (a < 0) {\n");
            out.write("a += 256;\n");
            out.write("}\n");
            out.write("if (b < 0) {\n");
            out.write("b += 256;\n");
            out.write("}\n");
            out.write("if (c < 0) {\n");
            out.write("c += 256;\n");
            out.write("}\n");
            out.write("result.append(encodeBase64Table[a >> 2]);\n");
            out.write("result.append(encodeBase64Table[((a & 0x3) << 4) | (b >> 4)]);\n");
            out.write("result.append(encodeBase64Table[((b & 0xf) << 2) | (c >> 6)]);\n");
            out.write("result.append(encodeBase64Table[c & 0x3f]);\n");
            out.write("}\n");
            out.write("if (partialLen == 1) {\n");
            out.write("result.setCharAt(resultLen - 1, '=');\n");
            out.write("result.setCharAt(resultLen - 2, '=');\n");
            out.write("} else if (partialLen == 2) {\n");
            out.write("result.setCharAt(resultLen - 1, '=');\n");
            out.write("}\n");
            out.write("return result.toString();\n");
            out.write("}\n");
        }
    }

    ///////////////////////////////////////////////////////////////////////

    /**
     * Create a bean with dummy values.
     * If a bean property write method fails or the constructor to the bean
     * fails, then a null value is used.
     */
    public static Object dummyBean(Class cls, int arraySize) throws java.beans.IntrospectionException {

        // construct a new instance
        if (!isJavaBeanType(cls)) {
            return dummyValue(cls, arraySize);
        }
        
        Object obj = null;
        try {
            Constructor construct = cls.getConstructor(new Class[0]);
            obj = construct.newInstance(new Object[0]);
        } catch (java.lang.NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (java.lang.IllegalAccessException e) {
            e.printStackTrace();
            return null;
        } catch (java.lang.reflect.InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }

        // stuff the bean with the dummy values
        BeanInfo bi = Introspector.getBeanInfo(cls);
        PropertyDescriptor[] pds = bi.getPropertyDescriptors();

        for (int i = 0 ; i < pds.length; ++i) {
            PropertyDescriptor pd = pds[i];
            Method writer = pd.getWriteMethod();
            if (writer == null) continue;
            Class propertyType = pd.getPropertyType();
            String propertyName = pd.getName();
            Class declaringClass = writer.getDeclaringClass();
            if (declaringClass == null || declaringClass.equals(Object.class)) continue;
            if (propertyType == null) continue;

            Object newValue=null;
            Object baseValue=null;
            if (isJavaBeanType(propertyType)) {
                Class baseType = propertyType;
                if (propertyType.isArray())
                    baseType = propertyType.getComponentType();
                baseValue = dummyBean(baseType, arraySize);
                if (propertyType.isArray()) {
                    // if array of beans, set each element
                    newValue = Array.newInstance(baseType, arraySize);
                    for (int ii=0; ii<arraySize; ++ii) {
                        Array.set(newValue, ii, baseValue);
                    }
                } else {
                    newValue = baseValue;
                }
            } else {
                Class baseType = propertyType;
                if (propertyType.isArray())
                    baseType = propertyType.getComponentType();
                baseValue = dummyValue(baseType, arraySize);
                if (propertyType.isArray()) {
                    // if array, set each elements
                    newValue = Array.newInstance(baseType, arraySize);
                    for (int ii=0; ii<arraySize; ++ii) {
                        Array.set(newValue, ii, baseValue);
                    }
                } else {
                    newValue = baseValue;
                }
            }

            // set the value to property
            try {
                writer.invoke(obj, new Object[] {newValue});
            } catch (java.lang.reflect.InvocationTargetException e) {
                // Let the null value be used
                e.printStackTrace();
            } catch (java.lang.IllegalAccessException e) {
                // Let the null value be used
                e.printStackTrace();
            }
        }

        // return the dummy bean
        return obj;
    }


    /**
     * @return dummy value for a particular type.  If it's not a standard
     * type, then look for a constructor that takes a single String as
     * a parameter; if that fails, then return null.
     */
    public static Object dummyValue(Class type, int arraySize) { 
        String typeName = type.getName().intern(); 
        if (Collection.class.isAssignableFrom(type)) {
            // if collection - make an arraylist of string
            ArrayList lst = new ArrayList();
            for (int ii=0; ii < arraySize; ++ii) {
                lst.add("collection-element");
            }
            return lst;
        } else if (typeName == "java.lang.String")
            return "string";
        else if (typeName == "java.lang.Boolean" || typeName == "boolean")
            return Boolean.FALSE;
        else if (typeName == "java.lang.Integer" || typeName == "int")
            return Integer.valueOf("1");
        else if (typeName == "java.lang.Long" || typeName == "long")
            return Long.valueOf("1");
        else if (typeName == "java.lang.Float" || typeName == "float")
            return Float.valueOf("1.0");
        else if (typeName == "java.lang.Double" || typeName == "double")
            return Double.valueOf("1.0");
        else if (typeName == "java.lang.Byte" || typeName == "byte")
            return Byte.valueOf("1");
        else if (typeName == "java.lang.Short" || typeName == "short")
            return Short.valueOf("1");
        else if (typeName == "java.lang.Character" || typeName == "char")
            return Character.valueOf('C');
        else if (typeName == "java.math.BigDecimal")
            return new java.math.BigDecimal("1.0");
        else if (typeName == "java.math.BigInteger")
            return new java.math.BigInteger("1");
        else if (typeName == "java.util.Calendar") {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            return cal;
        } else {
            // all other types, try to construct using string constructor.
            Constructor c = null;
	
            try {
                Class[] cc = new Class[] {java.lang.String.class};
                c = type.getDeclaredConstructor(cc);
                Object[] p = new Object[] {"string"};
                return c.newInstance(p);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return null;
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
                return null;
            } catch (java.lang.IllegalAccessException e) {
                e.printStackTrace();
                return null;
            } catch (java.lang.reflect.InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
