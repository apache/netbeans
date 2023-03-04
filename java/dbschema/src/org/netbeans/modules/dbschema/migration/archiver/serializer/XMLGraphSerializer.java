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

package org.netbeans.modules.dbschema.migration.archiver.serializer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.OutputStream;

/**
 *
 * @author  Mark Munro
 * @version %I%
 */
public class XMLGraphSerializer extends Object {

    // Fields

    private BufferedWriter outStream;
    private int indentLevel;
    private boolean indent = true;
    private HashMap ObjectMap;

    private static String indentChar        = "  ";
    private static String startTag          = "<";
    private static String endTagNL          = ">\n";
    private static String endTag            = ">";
    private static String endEmptyTagNL     = "/>\n";
    private static String endEmptyTag       = "/>";
    private static String objectTag         = "OBJECT";
    private static String arrayTag          = "ARRAY";
    private static String rowTag            = "ROW";
    private static String classAttrib       = "CLASS";
    private static String IDAttrib          = "ID";
    private static String refAttrib         = "REFERENCE";
    private static String rowAttrib         = "ROW";
    private static String rowClassAttrib    = "ROWCLASS";
    private static String sizeAttrib        = "SIZE";
    private static String primitiveArray    = "PRIMITIVE";
    private static String startCDATA        = "<![CDATA[";
    private static String endCDATA          = "]]>";

    //MBO added
    private static final String encoding = "UTF-8";

    /** Creates new XMLGraphSerialzer */
    private XMLGraphSerializer() 
    {
        this.ObjectMap = new HashMap();
    }
    
    // MBO remove constructor
    /*
    public XMLGraphSerializer(File outputFile) 
    {
        this();
        try 
        {
            this.outStream = new BufferedWriter( new FileWriter(outputFile));
        }
        catch (IOException lError)
        {
            lError.printStackTrace();
        }
    }
    public XMLGraphSerializer(String outputFile) 
    {
        this();
        try 
        {
            this.outStream = new BufferedWriter( new FileWriter(outputFile));
        }
        catch (IOException lError)
        {
            lError.printStackTrace();
        }
    }
    */

   public XMLGraphSerializer(OutputStream outStream) 
    {
        this();
        //MBO
        //this.outStream = new BufferedWriter( new OutputStreamWriter(outStream));
        try
        {
            this.outStream = new BufferedWriter( new OutputStreamWriter(outStream, encoding));
        }
        catch(java.io.UnsupportedEncodingException ex)
        {
            throw new java.lang.RuntimeException("Problems creating OutputStreamWriter: " + ex);
        }
    }

    // Support / utility methods

    private String getObjectName(Object obj)
    {

        // The following methodf generates a uniqie name for the object.
        // This name is used to reference the object within the XML document.
        // Use the class name and hash code that way we produce a name that is
        // at least partically readable.

        // Issue 80307: the hash code alone is not enough to guarantee an unique
        // name, since two non-equal objects can have the same hash code.

        StringBuffer lNameBuffer = new StringBuffer();
        lNameBuffer.append(obj.getClass().getName());
        lNameBuffer.append(obj.hashCode());

        int fixedLength = lNameBuffer.length();
        int index = 0;

        for (;;)
        {
            lNameBuffer.setLength(fixedLength);
            if (index > 0)
            {
                lNameBuffer.append('#');
                lNameBuffer.append(index);
            }

            String name = lNameBuffer.toString();
            Object mapObj = ObjectMap.get(name);

            if (mapObj == null || mapObj == obj)
            {
                return name;
            }
            else
            {
                index++;
            }
        }
    }

    private void writeLevel(String value) throws IOException
    {
        if ( indent )
        {
            for (int i = 0; i < this.indentLevel; i++)
            {
                outStream.write(indentChar);
            }
        }
        
        outStream.write(value);
    }
    private void writeLevel(char[] value) throws IOException
    {
        if ( indent )
        {
            for (int i = 0; i < this.indentLevel; i++)
            {
                outStream.write(indentChar);
            }
        }
        
        outStream.write(value);
    }

    private void writeCDATA(String value) throws IOException
    {
//        if ( indent )
//        {
//            for (int i = 0; i < this.indentLevel; i++)
//            {
//                outStream.write(indentChar);
//            }
//        }
	outStream.write(startCDATA);
	outStream.write(value);
	outStream.write(endCDATA);
    }

    private boolean recordObject(Object obj)
    {

        boolean lObjectRecordedAlready = false;

        String lObjectName = this.getObjectName(obj);

        if ( !this.ObjectMap.containsKey(lObjectName) )
        {

            // This adds the object to the hash table for the first time
            this.ObjectMap.put(lObjectName, obj);
        }
        else
            lObjectRecordedAlready = true;
            
        return lObjectRecordedAlready;
    }

    private void addAttribute(String attributeName, String attributeValue, StringBuffer tag)
    {
        if (tag.length() > 0 )
            tag.append(' ');
        tag.append(attributeName);
        tag.append('=');
        tag.append('\u0022');
        tag.append(attributeValue);
        tag.append('\u0022');

    }

    private boolean needsCDATA(String value)
    {

        boolean lNeedsCDATA = false;
        char lChar;
        int lStringLength = value.length();

        for ( int i = 0; i < lStringLength; i++)
        {
            lChar = value.charAt(i);
            if ( lChar == '<' || lChar == '>' || lChar == '&' )
            {
                lNeedsCDATA = true;
                break;
            }

        }
        return lNeedsCDATA;
    }

    public void DumpStatus()
    {
        System.out.println("Dumping state information for XMLGraphSerializer");
        System.out.println("Object Map contains ");
        Iterator lIterator = this.ObjectMap.values().iterator();
        while (lIterator.hasNext())
        {
            Object lNext = lIterator.next();
            System.out.println("Object Map contains object or class " + lNext.getClass().getName());
            System.out.println("Object state is " + lNext.toString());
        }
        System.out.println("Dumping state information for XMLGraphSerializer - END");
    }
    
    // main methods

    private void putStartTag(String tag, String elements, boolean empty, boolean newLine) throws IOException
    {
        this.writeLevel(startTag);
        outStream.write(tag);
        if ( elements != null )
        {
            outStream.write(' ');
            outStream.write(elements);
        }
        if ( empty )
        {
            if ( newLine )
                outStream.write(endEmptyTagNL);
            else
                outStream.write(endEmptyTag);
        }
        else
        {
            if ( newLine )
                outStream.write(endTagNL);
            else
                outStream.write(endTag);

            if ( this.indent )
                this.indentLevel++;
        }
    }
    
    private void putEndTag(String tag, boolean doIndent) throws IOException
    {
        if ( indent )
	    this.indentLevel--;

        if ( indent && doIndent)
            this.writeLevel("</");
        else
            outStream.write("</");
        outStream.write(tag);
        outStream.write(">\n");	
    }

    private void xlateObject(Object obj) throws IOException, IllegalAccessException
    {

        try
        {
        
            if ( obj == null )
            {
                this.putStartTag(objectTag, null, true, true);
            
            }
            else if ( obj instanceof String ||
                      obj instanceof StringBuffer
                    )
            {
                this.xlateString(null, obj);
            }

            else
            {
                if ( this.recordObject(obj) )
                {
                    // OK this object has already been recorded so process as a reference
                    this.xlateObjectReference(obj);
                }
                else
                {
                    Class lClassType = obj.getClass();

                    StringBuffer lClassAttributes = new StringBuffer();
                    this.addAttribute(classAttrib, org.netbeans.modules.dbschema.migration.archiver.MapClassName.getClassNameToken( lClassType.getName()), lClassAttributes);
                    this.addAttribute(IDAttrib, this.getObjectName(obj), lClassAttributes);

                    this.putStartTag(objectTag, lClassAttributes.toString(), false, true);

//

                    ArrayList lFields;
                
                    HashMap lFieldsMap = new HashMap();

                    Class lClass = lClassType;
                    Field[] lTFields = null;

                    while (lClass != null)
                    {
                        lTFields = lClass.getDeclaredFields();
//                        lFields.ensureCapacity(lTFields.length);
                
                        for (int i = 0; i < lTFields.length; i++)
                        {
//                            lFields.add(lTFields[i]);    
                              if ( ! lFieldsMap.containsKey(lTFields[i].getName()) )
                                lFieldsMap.put(lTFields[i].getName(), lTFields[i]);
                        }
        
                        lClass = lClass.getSuperclass();
                    }
                    
                    lFields = new ArrayList(lFieldsMap.values());

                    for ( int i = 0; i < lFields.size(); i++)
                    {
                        Field lCurrentField = (Field)(lFields.get(i));

                        if ( !Modifier.isTransient(lCurrentField.getModifiers()) &&
                             !Modifier.isStatic(lCurrentField.getModifiers())
                           )                
                        {

                            Class lCurrentFieldType = lCurrentField.getType();
                            lCurrentField.setAccessible(true);
                            Object lRealValue = lCurrentField.get(obj);
                            String lFieldName = lCurrentField.getName();
                            if ( lRealValue != null )
                            {
                                if ( lCurrentFieldType.isPrimitive())
                                {
                                    this.xlatePrimitive(lFieldName,lRealValue);
                                }
                                else if ( lRealValue instanceof java.lang.String ||
                                          lRealValue instanceof java.lang.StringBuffer
                                        )
                                {
                                    this.xlateString(lFieldName,lRealValue);
                                }
                                else if ( lCurrentFieldType.isArray() )
                                {
                                    this.xlateArray(lFieldName, lRealValue);
                                }
                                else if ( lRealValue instanceof Collection )
                                {
                                    this.xlateCollection(lFieldName, lRealValue);
                                }
                                else
                                {
                                    this.putStartTag(lFieldName, null, false, true);
                                    this.xlateObject(lRealValue);
                                    this.putEndTag(lFieldName, true);
                                }   
                            }
                            else
                            {
                                this.putStartTag(lFieldName, null, false, true);
                                this.putStartTag(objectTag, null, true, true);
                                this.putEndTag(lFieldName, true);
                            
                            }

                        }
                    }
                    this.putEndTag(objectTag, true);
                }
            }
            
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
            this.DumpStatus();
            System.out.println("IO Exception in XLateObject current object class " + obj.getClass().getName());
            System.out.println("IO Exception in XLateObject current object is " + obj);
            this.outStream.close();
            throw e1;
            
        }
        catch (IllegalAccessException e2)
        {
            e2.printStackTrace();
            this.DumpStatus();
            System.out.println("IO Exception in XLateObject current object class " + obj.getClass().getName());
            System.out.println("IO Exception in XLateObject current object is " + obj);
            this.outStream.close();
            throw e2;
            
        }
        catch (RuntimeException e3)
        {
            e3.printStackTrace();
            this.DumpStatus();
            System.out.println("IO Exception in XLateObject current object class " + obj.getClass().getName());
            System.out.println("IO Exception in XLateObject current object is " + obj);
            this.outStream.close();
            throw e3;
        }
    }

    private void xlateObjectReference(Object obj) throws IOException
    {
        StringBuffer lReferenceAttributes = new StringBuffer();
        this.addAttribute(refAttrib, this.getObjectName(obj), lReferenceAttributes);

        this.putStartTag(objectTag, lReferenceAttributes.toString(), true, true);

    }

    private void xlatePrimitive(String name, Object obj) throws IOException
    {
        Class lType = obj.getClass();
        String lValue = obj.toString();

        this.putStartTag(name, null, false, false);

        if ( lType == java.lang.Character.TYPE )
        {
            // This is a character so check for CDATA requirements
            if ( this.needsCDATA(lValue) )
            {
                this.writeCDATA(lValue);
            }
            else
                outStream.write(lValue);
        }
        else
            outStream.write(lValue);

        this.putEndTag(name, false);
    }

    private void xlateString(String name, Object obj) throws IOException
    {

        if ( name != null )
            this.putStartTag(name, null, false, false);

        String lValue = obj.toString();
            
        if ( this.needsCDATA(lValue) )
        {
            this.writeCDATA(lValue);
        }
        else
            outStream.write(lValue);

        if ( name != null )
            this.putEndTag(name, false);
    }

    private void xlateArray(String name, Object obj) throws IOException, IllegalAccessException
    {

        StringBuffer lArrayAttributes = new StringBuffer();
        
        int lArraySize = Array.getLength(obj);

        this.addAttribute(sizeAttrib, Integer.toString(lArraySize), lArrayAttributes);
        this.addAttribute(classAttrib, primitiveArray, lArrayAttributes);
        this.addAttribute(rowClassAttrib,org.netbeans.modules.dbschema.migration.archiver.MapClassName.getClassNameToken( obj.getClass().getComponentType().getName()), lArrayAttributes);
        
//        System.out.println( "Component Type is " + obj.getClass().getComponentType().getName() );
//        System.out.println( "Class Type is " + obj.getClass().getName() );
        
        this.putStartTag(name, null, false, true);
        this.putStartTag(arrayTag, lArrayAttributes.toString(), false, true);

        for ( int i = 0; i < lArraySize; i++)
        {
        
            Object lRow = Array.get(obj, i);

            if ( lRow instanceof java.lang.String ||
                 lRow instanceof java.lang.Number ||
                 lRow instanceof java.lang.Character ||
                 lRow instanceof java.lang.Boolean
               )
            {
                this.xlateSimpleRow(i, lRow);
            }
            else
            {
                StringBuffer lRowAttributes = new StringBuffer();
                this.addAttribute(rowAttrib, Integer.toString(i), lRowAttributes);
                this.putStartTag(rowTag, lRowAttributes.toString(), false, true);
                this.xlateObject(lRow);
                this.putEndTag(rowTag, true);
            }
        }

        this.putEndTag(arrayTag, true);
        this.putEndTag(name, true);

    }

    private void xlateCollection(String name, Object obj) throws IOException, IllegalAccessException
    {

        StringBuffer lArrayAttributes = new StringBuffer();

        Collection lArray = (Collection)(obj);

        int lArraySize = lArray.size();

        this.addAttribute(sizeAttrib, Integer.toString(lArraySize), lArrayAttributes);
        this.addAttribute(classAttrib, lArray.getClass().getName(), lArrayAttributes);
        this.addAttribute(rowClassAttrib, "", lArrayAttributes);
        this.putStartTag(name, null, false, true);
        this.putStartTag(arrayTag, lArrayAttributes.toString(), false, true);


        Iterator lIterator = lArray.iterator();
        int lRowCount = 0;

        while (lIterator.hasNext())
        {
            Object lRow = lIterator.next();
            
            if ( lRow instanceof java.lang.String ||
                 lRow instanceof java.lang.Number ||
                 lRow instanceof java.lang.Character ||
                 lRow instanceof java.lang.Boolean
               )
            {
                this.xlateSimpleRow(lRowCount, lRow);
            }
            else
            {
                StringBuffer lRowAttributes = new StringBuffer();
                this.addAttribute(rowAttrib, Integer.toString(lRowCount), lRowAttributes);
                this.putStartTag(rowTag, lRowAttributes.toString(), false, true);
                this.xlateObject(lRow);
                this.putEndTag(rowTag, true);
            }
            lRowCount++;
        }

        this.putEndTag(arrayTag, true);
        this.putEndTag(name, true);

    }

    private void xlateSimpleRow(int rowNumber, Object obj) throws IOException
    {
    
        StringBuffer lRowAttributes = new StringBuffer();
        this.addAttribute(rowAttrib, Integer.toString(rowNumber), lRowAttributes);

        Class lObjectClass = obj.getClass();

        if ( lObjectClass.isPrimitive() )
        {
            if (lObjectClass == java.lang.Integer.TYPE)
                this.addAttribute("ROWCLASS", "int" , lRowAttributes);
            else if (lObjectClass == java.lang.Short.TYPE)
                this.addAttribute("ROWCLASS", "short" , lRowAttributes);
            else if (lObjectClass == java.lang.Long.TYPE)
                this.addAttribute("ROWCLASS", "long" , lRowAttributes);
            else if (lObjectClass == java.lang.Float.TYPE)
                this.addAttribute("ROWCLASS", "float" , lRowAttributes);
            else if (lObjectClass == java.lang.Double.TYPE)
                this.addAttribute("ROWCLASS", "double" , lRowAttributes);
            else if (lObjectClass == java.lang.Boolean.TYPE)
                this.addAttribute("ROWCLASS", "boolean" , lRowAttributes);
            else if (lObjectClass == java.lang.Character.TYPE)
                this.addAttribute("ROWCLASS", "char" , lRowAttributes);
            else
                this.addAttribute("ROWCLASS", "unknown" , lRowAttributes);

        }
        else
            this.addAttribute("ROWCLASS",  org.netbeans.modules.dbschema.migration.archiver.MapClassName.getClassNameToken( lObjectClass.getName()) , lRowAttributes);
        

        this.addAttribute("VALUE", obj.toString(), lRowAttributes);

        this.putStartTag(rowTag, lRowAttributes.toString(), true, true);
  
    }


    public void writeObject(Object obj) throws IOException
    {
        try
        {
            outStream.write("<?xml version=\"1.0\" encoding=\"" + encoding + "\" ?>\n\n");

            this.xlateObject(obj);
            this.outStream.close();
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
            this.DumpStatus();
            try
            {
                this.outStream.close();
            }
            catch (IOException lNotClosed)
            {
                // Do nothing
            }
            throw e1;
        }
        catch (IllegalAccessException e2)
        {
            e2.printStackTrace();
            this.DumpStatus();
            try
            {
                this.outStream.close();
            }
            catch (IOException lNotClosed)
            {
                // Do nothing
            }
        }

    }

}
