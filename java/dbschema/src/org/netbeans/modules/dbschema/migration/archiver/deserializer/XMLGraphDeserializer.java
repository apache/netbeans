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

package org.netbeans.modules.dbschema.migration.archiver.deserializer;

import java.util.*;

import java.lang.reflect.Field;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

import org.xml.sax.*;

public  class XMLGraphDeserializer extends BaseSpecificXMLDeserializer implements java.io.Serializable
{

    // Fields

    // The following static fields control the various states that this
    // state machine expects and handles. Some states are currently no-op's

    public static final int XGD_END                 = 99;
    public static final int XGD_NEED_ATTRIBUTE      = 1;
    public static final int XGD_NEED_END_ATTR       = 2;
    public static final int XGD_NEED_END_NULLVALUE  = 4;
    public static final int XGD_NEED_END_OBJECT     = 5;
    public static final int XGD_NEED_END_PARAM      = 10;
    public static final int XGD_NEED_END_ROW        = 3;
    public static final int XGD_NEED_END_ROW_ELEMENT = 14;
    public static final int XGD_NEED_OBJECT         = 7;
    public static final int XGD_NEED_PARAM          = 9;
    public static final int XGD_NEED_ROW            = 6;
    public static final int XGD_NEED_ROW_ELEMENT    = 13;
    public static final int XGD_NEED_ROW_TAG        = 12;
    public static final int XGD_NEED_STRING         = 11;
    public static final int XGD_NEED_VALUE          = 8;

    // The following fields maintain various states
//@olsen+MBO: used unsynchronized HashMap and ArrayListStack
    private  java.util.HashMap ObjectHash;
    private  ArrayListStack AttrNameStack;
    ArrayListStack RowTypeStack;
    private ArrayListStack RowCountStack;
/*
    private  java.util.Hashtable ObjectHash;
    private  java.util.Stack AttrNameStack;
    public java.util.Stack RowTypeStack;
    private java.util.Stack RowCountStack;
*/

    //@olsen+MBO: added field for reflection lookup
    private HashMap hashedClasses = new HashMap();


    // Constructors

    //@lars: added classloader-constructor
    public  XMLGraphDeserializer(ClassLoader cl)
    {
        super(cl);
//@olsen+MBO: used unsynchronized HashMap and ArrayListStack
        this.AttrNameStack  = new ArrayListStack();
        this.ObjectHash     = new java.util.HashMap(20);
        this.RowTypeStack   = new ArrayListStack();
        this.RowCountStack  = new ArrayListStack();
/*
        this.AttrNameStack  = new java.util.Stack();
        this.ObjectHash     = new java.util.Hashtable(20, 0.75F);
        this.RowTypeStack   = new java.util.Stack();
        this.RowCountStack  = new java.util.Stack();
*/
    } /*Method-End*/

    public  XMLGraphDeserializer()
    {
        this (null);
    }

    //@olsen+MBO: added method for reflection lookup
    private Field findField(Object lCurrentObj,
                            final String lCurrentField)
                            //String lCurrentField)
    {
        Field lField = null;
        Class lClass = lCurrentObj.getClass();
        HashMap hashedFields = (HashMap)hashedClasses.get(lClass);
        if (hashedFields == null) {
            hashedFields = new HashMap();
            hashedClasses.put(lClass, hashedFields);
        } else {
            lField = (Field)hashedFields.get(lCurrentField);
        }

        for (;
             lClass != null && lField == null;
             lClass = lClass.getSuperclass())
        {
/*
            try {
                lField = lClass.getDeclaredField(lCurrentField);
                if (lField != null) {
                    hashedFields.put(lCurrentField, lField);
                    lField.setAccessible(true);
                    break;
                }
            } catch (java.lang.NoSuchFieldException ex) {
            }
*/

            final Class tmpClass = lClass;
            lField = (Field)java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        try {
              		    Field tmpField = tmpClass.getDeclaredField(lCurrentField);
               		    if (tmpField != null) {
                                tmpField.setAccessible(true);
                            }
                            return tmpField;
                        } catch (java.lang.NoSuchFieldException ex) {
                            return null;
                        }
                    }
                }
                );
                if (lField != null) {
                    hashedFields.put(lCurrentField, lField);
                    break;
                }

        }
        return lField;
    }

    public   void endDocument() throws org.xml.sax.SAXException
    {
        super.endDocument();

        this.State = new Integer(this.XGD_END);
    } /*Method-End*/

    public   void endElement(java.lang.String name) throws org.xml.sax.SAXException
    {
        // Debug information
        int lInitialState = this.State.intValue();

        try {
            if ( name.equals("ARRAY") )
                this.popRowCount();

            if (!name.equals("_.ALIAS"))
            {
                switch ( this.State.intValue() )
                {
                case XGD_NEED_ATTRIBUTE:
                    this.validateTag(name, "OBJECT", true);
                    this.popState();

                    break;
                case XGD_NEED_END_ATTR:
                    java.lang.String lFieldName = this.topAttrName();
                    this.validateTag(this.unescapeName(name), lFieldName, true);
                    java.lang.Object lObj = this.popObject();
                    this.setCurrentAttribute(lObj);
                    this.popAttrName();
                    this.popState();

                    break;
                case XGD_NEED_END_PARAM:
                    this.validateTag(name, "PARAM", true);

                    break;
                case XGD_NEED_END_ROW:
                    this.validateTag(name, "ROW", true);
                    this.popState();
                    java.lang.Object lRow = this.popObject();

                    Object lArrayRef = this.topObject();
                    if ( lArrayRef instanceof java.util.Collection )
                    {
                        java.util.Collection lArray = (java.util.Collection)(lArrayRef);
                        lArray.add(lRow);
                    }
                    else if ( lArrayRef.getClass().isArray() )
                    {
                        int lRowNo = this.currentRowCount();
                        Array.set(lArrayRef, lRowNo, lRow);
                        this.incrementRowCount();
                    }
                    break;
                case XGD_NEED_END_NULLVALUE:
                    this.validateTag(name, "NULLVALUE", true);
                    this.popState();

                    break;
                case XGD_NEED_END_OBJECT:
                    this.validateTag(name, "OBJECT", true);
                    this.popState();

                    break;

                case XGD_NEED_PARAM:
//                    System.out.println("In endElement and this.XGD_NEED_PARAM currently a no op ");
                    this.popState();
                    break;
                case XGD_NEED_ROW:
                    this.validateTag(name, "ARRAY", true);
                    this.popState();
                    this.popRowType();

                    break;

                case XGD_NEED_VALUE:
                    this.popState();

                    java.lang.String lValue = null;
                    java.lang.String lSource = this.getCharacters();

                    int lLength = lSource.length();

                    if (lLength != 0 ||
                        lLength != 1)
                    {
                        lValue = lSource;
                    }

                    switch ( this.State.intValue() )
                    {
                    case XGD_NEED_END_ROW:
                        this.validateTag(name, "ROW", true);

                        java.util.Collection lArray2 = (java.util.Collection)(this.topObject());

                        java.lang.Object lRow2 = null;
                        java.lang.Class lRowType;

                        lRowType = this.topRowType();

                        if (lRowType == java.lang.String.class)
                        {
                            //java.lang.String lNewString = new String(lValue);
                            java.lang.String lNewString = (lValue);
                            lArray2.add(lNewString);
                        }
                        else
                        {
                            lArray2.add(lRow2);
                        }// end if
                        this.popState();
                        break;
                    case XGD_NEED_END_ATTR:
                        this.validateTag(this.unescapeName(name), this.topAttrName(), true);
                        this.setCurrentAttribute(lValue);
                        this.popAttrName();
                        this.popState();
                        break;
                    case XGD_NEED_END_PARAM:
//                    System.out.println("In endElement and this.XGD_NEED_VALUE,XGD_NEED_END_PARAM currently a no op ");
                        this.popState();
                        this.pushState(this.XGD_NEED_PARAM);
                        break;
                        //AddParameter(value, ParameterClass, ParameterSetMethod);
                    }// end case;

                    break;
                case XGD_NEED_STRING:
//                    System.out.println("In endElement and this.XGD_NEED_STRING currently a no op ");
                    this.popState();
                    break;
                case XGD_NEED_END_ROW_ELEMENT:
//                    System.out.println("In endElement and this.XGD_NEED_END_ROW_ELEMENT currently a no op ");

                    this.State = new Integer(this.XGD_NEED_ROW_ELEMENT);
                    break;
                case XGD_NEED_ROW_ELEMENT:
//                    System.out.println("In endElement and this.XGD_NEED_ROW_ELEMENT currently a no op ");
                     this.State = new Integer(this.XGD_NEED_ROW_TAG);
                    break;
                case XGD_NEED_ROW_TAG:
//                    System.out.println("In endElement and this.XGD_NEED_ROW_TAG currently a no op ");
                    this.popState();

                    break;
                case XGD_NEED_OBJECT:
                // Cases we should never see
                    this.unexpectedTag(name, "OBJECT", false);

                    break;
                }// end case
            }// end if

            super.endElement(name);
        }// end try
        catch (SAXException lError)
        {
            // Dump so debug information if ew get an exception at this point
            lError.printStackTrace();
            System.out.println("Exception cause in XMLGraphDeserializer.endElement");
            System.out.println("Tag being process is " + name + " initial state was " + lInitialState);
            this.DumpStatus();
            // Now rethrow the exception
            throw lError;

        }// end catch
        catch (RuntimeException lError)
        {
            // Dump so debug information if ew get an exception at this point
            lError.printStackTrace();
            System.out.println("Exception cause in XMLGraphDeserializer.endElement");
            System.out.println("Tag being process is " + name + " initial state was " + lInitialState);
            this.DumpStatus();
            // Now rethrow the exception
            throw lError;

        }// end catch

    } /*Method-End*/

    public   void startDocument() throws org.xml.sax.SAXException
    {
        super.startDocument();

        State = new Integer(this.XGD_END);
        this.pushState(this.XGD_NEED_OBJECT);
    } /*Method-End*/

    public   void startElement(java.lang.String name, org.xml.sax.AttributeList atts) throws org.xml.sax.SAXException
    {

        // Debug information
        int lInitialState = this.State.intValue();

/*        System.out.println("startElement : Current State is " + this.State);
        System.out.println("startElement : State stack is ");
        System.out.println(this.StateStack);
        System.out.println("startElement : State stack finished ");
        System.out.println("Name is " + name);
*/
        try {

            if ( name.equals("ARRAY") )
                this.pushRowCount();

            if ( name.equals( "_.ALIAS" ) )
            {
                java.lang.String lName = atts.getValue("NAME");
                java.lang.String lAlias = atts.getValue("ALIAS");
                this.addActiveAlias(lName, lAlias);
            }
            else
            {
                switch ( this.State.intValue() )
                {
                case XGD_NEED_ATTRIBUTE:
                    this.readAttributeHeader(name, atts);
                    break;
                case XGD_NEED_ROW_TAG:
//                    System.out.println("In startElement and this.XGD_NEED_ROW_TAG currently a no op ");
                    this.State = new Integer(this.XGD_NEED_ROW_ELEMENT);
                    break;
                //    row :  XMLRow = new;
                //    row.Elements = new;
                //   row.RowTag = name;
                //    state = XGD_NEED_ROW_ELEMENT;
                //    PushObject(row);
                case XGD_NEED_ROW_ELEMENT:
//                    System.out.println("In startElement and this.XGD_NEED_ROW_ELEMENT currently a no op ");
                    this.State = new Integer(this.XGD_NEED_END_ROW_ELEMENT);
                    break;
                //    elm :  XMLRowElement = new;
                //    elm.ElementNm = name;
                //    elm.ClassNm = atts.GetValue('CLASS');
                //    XMLRow(TopObject()).Elements.Add(elm);
                //    PushObject(elm);
                //    state = XGD_NEED_END_ROW_ELEMENT;
                case XGD_NEED_ROW:
                    this.validateTag(name, "ROW", false);

                    if ( this.checkSimpleRow(name, atts) )
                    {
                        this.pushState(XGD_NEED_END_ROW);
                    }
                    else
                    {
                        this.pushState(XGD_NEED_END_ROW);
                        this.pushState(XGD_NEED_VALUE);
                    }
                    break;
                case XGD_NEED_OBJECT:
                    this.validateTag(name, "OBJECT" , false);
                    this.readObjectHeader(name, atts, false);
                    break;
                case XGD_NEED_PARAM:
//                System.out.println("In startElement and this.XGD_NEED_PARAM currently a no op ");
                //this.validateTag(name, "PARAM", false);
                    this.popState();
                    this.pushState(this.XGD_NEED_END_PARAM);
                    this.pushState(this.XGD_NEED_VALUE);
                    //className : string = atts.GetValue('CLASS');
                    //if className = NIL then
                    //    ParameterClass = NIL;
                    //else
                    //    ParameterClass = FindClass(className);
                    //end if;
                    //ParameterSetMethod = atts.GetValue('SETMETHOD');
                    //PopState();
                    //PushState(XGD_NEED_END_PARAM);
                    //PushState(XGD_NEED_VALUE);
                    break;
                case XGD_NEED_VALUE:
                    if (!name.equals("OBJECT") &&
                        !name.equals("ARRAY") &&
                        !name.equals("NULLVALUE"))
                    {
                        this.unexpectedTag(name, "OBJECT, ARRAY, NULLVALUE", false);
                    }// end if;
                    this.readValue(name, atts);

                    break;

                case XGD_NEED_END_ATTR:
                // Cases we should never see
                    this.unexpectedTag(name, this.topAttrName(), true);
                    break;
                case XGD_NEED_END_PARAM:
                    this.unexpectedTag(name, "/PARAM", false);
                    break;
                case XGD_NEED_END_ROW:

                    this.unexpectedTag(name, "/ROW", false);
                    break;
                case XGD_NEED_END_NULLVALUE:
                    this.unexpectedTag(name, "/NULLVALUE", false);
                    break;
                case XGD_NEED_END_OBJECT:
                    this.unexpectedTag(name, "/OBJECT", false);
                    break;
                }// end case
            }// end if


            super.startElement(name, atts);

        }// end try
        catch (SAXException lError)
        {
            // Dump so debug information if ew get an exception at this point
            lError.printStackTrace();
            System.out.println("Exception cause in XMLGraphDeserializer.startElement");
            System.out.println("Tag being process is " + name + " initial state was " + lInitialState);
            System.out.println("Attribute list is :");
            for ( int i = 0; i < atts.getLength(); i++)
            {
                System.out.println("Attribute " + atts.getName(i) + " Type " + atts.getType(i) + " value " + atts.getValue(i));
            }
            this.DumpStatus();
            // Now rethrow the exception
            throw lError;

        }// end catch
        catch (RuntimeException lError)
        {
            // Dump so debug information if ew get an exception at this point
            lError.printStackTrace();
            System.out.println("Exception cause in XMLGraphDeserializer.startElement");
            System.out.println("Tag being process is " + name + " initial state was " + lInitialState);
            System.out.println("Attribute list is :");
            for ( int i = 0; i < atts.getLength(); i++)
            {
                System.out.println("Attribute " + atts.getName(i) + " Type " + atts.getType(i) + " value " + atts.getValue(i));
            }
            this.DumpStatus();
            // Now rethrow the exception
            throw lError;

        }// end catch
    } /*Method-End*/

    public  java.lang.Class  findClass(java.lang.String name) throws java.lang.ClassNotFoundException
    {
        return super.findClass(this.lookupAlias(name));
    } /*Method-End*/

    public  java.lang.String  popAttrName()
    {
        return (java.lang.String)(AttrNameStack.pop());
    } /*Method-End*/

    public   void processObjectReference(java.lang.String refName) throws org.xml.sax.SAXException
    {
        //@olsen+MBO: assign directly
        java.lang.Object lObj = this.ObjectHash.get(refName);

        if (lObj == null)
        {
            //String message = new String("Object " + refName + " could not be found");
            String message = ("Object " + refName + " could not be found");

            SAXException objError = new SAXException(message);

            throw objError;
        }
        this.pushObject(lObj);
    } /*Method-End*/

    public   void pushAttrName(java.lang.String name)
    {
        this.AttrNameStack.push(name);
    } /*Method-End*/

    public   void readAttributeHeader(java.lang.String name, org.xml.sax.AttributeList atts)
    {
        //@olsen+MBO: intended to compare strings by '==' instead of equals()?
        if (name == "_.METHOD"  || name == "_.CALLBACK")
        {
            this.pushAttrName(atts.getValue("NAME"));
            this.pushAttrName(name);
            this.pushState(this.XGD_NEED_PARAM);
            this.ParameterArray.clear();
        }
        else
        {
            this.pushAttrName(this.unescapeName(name));
            this.pushState(this.XGD_NEED_END_ATTR);
            this.pushState(this.XGD_NEED_VALUE);
        }
    } /*Method-End*/

    public   void readObjectHeader(java.lang.String name, org.xml.sax.AttributeList atts, boolean refOK) throws org.xml.sax.SAXException
    {
        boolean lFirstObj = (this.ObjectStack.size() == 0);
        Object lObj = null;
        String lIDName = null;

        if (refOK)
        {
            java.lang.String lRefName = atts.getValue("REFERENCE");
            if (lRefName != null)
            {
                this.processObjectReference(lRefName);
                State = new Integer(this.XGD_NEED_END_OBJECT);
                return;
            }// end if
        }// end if;

        // MBO: need to get the object name here
        lIDName = atts.getValue("ID");

        if (lFirstObj ||
            !this.useExistingAttribute(atts, topAttrName(), lObj)
           )
        {
            java.lang.String lClassName = atts.getValue("CLASS");
            if (lClassName == null)
            {
                lObj = null;
            }
            else
            {
                java.lang.Class lClass = null;
                try
                {
                    lClass = this.findClass(lClassName);
                    // MBO: special handling for wrapper classes.
                    // We need to postpone the creation of the wrapper
                    // class instance to the point in time where the value
                    // is available, because the value field might be
                    // final. Store all the information that is available
                    // here (class object and object id) into a
                    // WrapperClassHelper and push this on the object
                    // stack. The helper will be replaced later by the
                    // wrapper class instance.
                    if (WrapperClassHelper.isWrapperClass(lClass))
                        lObj = new WrapperClassHelper(lClass, lIDName);
                    else
                        lObj = lClass.getDeclaredConstructor().newInstance();
                }
                catch (IllegalAccessException e1)
                {
                    e1.printStackTrace();
                    //java.lang.String message = new String("Illegal Access to class " + lClass.getName());
                    java.lang.String message = ("Illegal Access to class " + lClass.getName());
                    SAXException useError = new SAXException(message);
                    throw useError;
                }
                catch (InstantiationException | NoSuchMethodException | InvocationTargetException e2)
                {

                    lObj = NewInstanceHelper.newInstance(lClassName, this.topObject());
                    if ( lObj == null )
                    {
                        e2.printStackTrace();

                        //java.lang.String message = new String("Instantiation exception of class " + lClass.getName());
                        java.lang.String message = ("Instantiation exception of class " + lClass.getName());
                        SAXException useError = new SAXException(message);
                        throw useError;
                    }
                }
                catch (ClassNotFoundException e3)
                {
                    e3.printStackTrace();

                    //java.lang.String message = new String("Class " + lClass.getName() + " could not be found");
                    java.lang.String message = ("Class " + lClass.getName() + " could not be found");
                    SAXException useError = new SAXException(message);
                    throw useError;
                }
            }// end if
        }// end if


        this.pushObject(lObj);

        // handle id
        if (lIDName != null)
        {
            this.ObjectHash.put(lIDName, lObj);
        }// end if

        if (lFirstObj)
        {
            this.MasterDeserializer.setInitialObject(lObj);
        }// end if

        if (lObj == null)
        {
               State = new Integer(this.XGD_NEED_END_OBJECT);
        }
        //else if (lobj.IsA(Stringizeable) then
        //    State = XGD_NEED_STRING;
        //else if obj.IsA(UseXMLRows) then
        //    UseXMLRows(obj).BeginXMLRows(FALSE);
        //    State = XGD_NEED_ROW_TAG;
        else
        {
            State = new Integer(this.XGD_NEED_ATTRIBUTE);
        }
    } /*Method-End*/

    public  java.lang.String  topAttrName()
    {

        return (java.lang.String)(this.AttrNameStack.peek());
    } /*Method-End*/

    public   void readValue(java.lang.String name, org.xml.sax.AttributeList atts) throws org.xml.sax.SAXException
    {
        boolean lFirstObj = (this.ObjectStack.size() == 0);

        if (name.equals("OBJECT"))
        {
            this.readObjectHeader(name, atts, true);
        }
        else if (name.equals("ARRAY"))
        {
            java.lang.Object lObj = null;
            java.lang.Class lRowClassType = null;
            java.lang.Object lArray = null;

            // really need a way of working out what type
            // of array this is but this will do for now

            String lArrayType = atts.getValue("CLASS");
            int lArraySize = Integer.parseInt(atts.getValue("SIZE"));
            java.lang.String lRowTypeName = atts.getValue("ROWCLASS");

//            if ( lArrayType.equals("PRIMITIVE") )
//                lArray = new ArrayList();
//            else
            if ( !lArrayType.equals("PRIMITIVE") )
            {
                try
                {
                    Class lArrayTypeClass = this.findClass(lArrayType);
                    lArray = lArrayTypeClass.getDeclaredConstructor().newInstance();
                }
                catch (ClassNotFoundException e1)
                {
                    e1.printStackTrace();
                    //java.lang.String message = new String("Class " + lArrayType + " could not be found, " +
                    //                                      "either it has the wrong name or cannot be found in lookup table");
                    java.lang.String message = ("Class " + lArrayType + " could not be found, " +
                                                          "either it has the wrong name or cannot be found in lookup table");
                    SAXException classError = new SAXException(message);
                    throw classError;
                }
                catch (IllegalAccessException e2)
                {
                    e2.printStackTrace();
                    //java.lang.String message = new String("Illegal Access exception whilst trying to init new instance of " + lArrayType);
                    java.lang.String message = ("Illegal Access exception whilst trying to init new instance of " + lArrayType);
                    SAXException accessError = new SAXException(message);
                    throw accessError;
                }
                catch (InstantiationException | NoSuchMethodException | InvocationTargetException e3)
                {
                    e3.printStackTrace();
                    //java.lang.String message = new String("Instantiation exception whilst trying to init new instance of " + lArrayType);
                    java.lang.String message = ("Instantiation exception whilst trying to init new instance of " + lArrayType);
                    SAXException initError = new SAXException(message);
                    throw initError;
                }

            }

            if (!lFirstObj &&
                this.useExistingAttribute(atts, this.topAttrName(), lObj))
            {
//                lArray = (java.util.ArrayList)(lObj);
                lArray = lObj;
            }// end if;

            // I can't simply check the array for the row type
            // as array list does not have an default class
            // also I am assuming for the minutes that TOOL array map
            // to ArrayList's  RESOLVE later

            if (lRowTypeName != null &&
                !lRowTypeName.equals("")
               )
            {
                try
                {
                    lRowClassType = this.findClass(lRowTypeName);
                }
                catch (ClassNotFoundException e1)
                {
                    e1.printStackTrace();
                    //java.lang.String message = new String("Class " + lRowTypeName + " could not be found, " +
                    //                                      "either it has the wrong name or cannot be found in lookup table");
                    java.lang.String message = ("Class " + lRowTypeName + " could not be found, " +
                                                "either it has the wrong name or cannot be found in lookup table");
                    SAXException classError = new SAXException(message);
                    throw classError;
                }
            }
            else
            {
                lRowClassType = java.lang.Object.class;
            }// end if;

            if ( lArrayType.equals("PRIMITIVE") )
            {
                lArray = Array.newInstance(lRowClassType, lArraySize);
            }

            this.pushRowType(lRowClassType);
            this.pushObject(lArray);
            this.State = new Integer(this.XGD_NEED_ROW);
        }
        else if (name.equals("NULLVALUE"))
        {
            java.lang.Object lCurrentObj = null;
            Field lField = null;
            try
            {
                lCurrentObj = this.topObject();
                // due to the fact that this method is currently
                // designed to handle tool to java conversion
                // we must get all of the declared fields and do
                // a caseless search as tool is not case sensitive
                // if we have someway of working out the destination
                // language then we should do a check here
                //lField = lCurrentObj.getClass().getDeclaredField(this.topAttrName());

/* @olsen+MBO: perf. optimization: replace reflect:field lookup by method findField that caches Field objects
                Field[] lFields = null;
                lFields = lCurrentObj.getClass().getDeclaredFields();


                if (lFields == null ||
                    lFields.length == 0)
                {
                    java.lang.String message = new String("No fields on class " + lCurrentObj.getClass().getName());
                    java.lang.NoSuchFieldException noFieldsError = new NoSuchFieldException(message);
                    //SAXException noFieldsError = new SAXException(message);

                    throw noFieldsError;

                }

                FORLOOP:
                for (int i = 0; i < lFields.length; i++)
                {
                    if (lFields[i].getName().equalsIgnoreCase(this.topAttrName()))
                    {
                        lField = lFields[i];
                        break FORLOOP;
                    }
                }
*/
                // MBO: This code is suspicious!
                // - I could not find the place in the XMLGraphSerializer
                // that creates an element called NULLVALUE, so maybe this
                // code is never executed.
                // - It creates an instance of the type of the current
                // field, which fails if the field type is an interface! 
                // - It pushes an instance on the object statck where it is
                // not clear to me where the instance is removed. 

                //@olsen+MBO: perf. optimization
                lField = findField(lCurrentObj, this.topAttrName());

                if (lField == null)
                    // MBO: field not found. The current version of the class
                    // does not declare the field anymore => ignore. 
                    this.pushObject(new Object());
                else
                    this.pushObject(lField.getType().getDeclaredConstructor().newInstance());

                this.State = new Integer(this.XGD_NEED_END_NULLVALUE);
            }
            catch (InstantiationException | NoSuchMethodException | InvocationTargetException e1)
            {
                e1.printStackTrace();
                //java.lang.String message = new String("Could not init instance of " + lField.getType().getName());
                java.lang.String message = ("Could not init instance of " + lField.getType().getName());
                SAXException initError = new SAXException(message);
                throw initError;
            }
            catch (IllegalAccessException e2)
            {
                e2.printStackTrace();
                //java.lang.String message = new String("Illegal access of field " + this.topAttrName());
                java.lang.String message = ("Illegal access of field " + this.topAttrName());
                SAXException illegalError = new SAXException(message);
                throw illegalError;
            }
        }// end if;
    } /*Method-End*/

    public   void pushRowType(java.lang.Class type)
    {
        this.RowTypeStack.push(type);
    } /*Method-End*/

    public  java.lang.Class  popRowType()
    {
        return (java.lang.Class)(this.RowTypeStack.pop());
    } /*Method-End*/


    public   void setCurrentAttribute(java.lang.Object value) throws org.xml.sax.SAXException
    {
        java.lang.Object lCurrentObj = this.topObject();
        java.lang.String lCurrentField = this.topAttrName();

        // MBO: special handling for wrapper class instances.
        // A WrapperClassHelper instance on top of the object stack
        // indicates the creation of the wrapper class instance was
        // postponed. Now we have the value, so we can create the wrapper
        // class instance.
        if (lCurrentObj instanceof WrapperClassHelper) {
            WrapperClassHelper helper = (WrapperClassHelper)lCurrentObj;
            // Represent the value as an instance of the wrapper class.
            lCurrentObj = helper.valueOf((String)value);
            // The top of the ObjectStack is a WrapperClassHelper, which
            // needs to be replaced by the wrapper class instance itself.
            popObject();
            pushObject(lCurrentObj);
            // In case of a named object (having an ID) we need to replace
            // the WrapperClassHelper instance in the ObjectMap by the
            // wrapper class instance itself. 
            String id = helper.getId();
            if (id != null)
                this.ObjectHash.put(id, lCurrentObj);

            // The wrapper class object is created with its value, so we
            // are done with this instace => return.
            return;
        }

        Field lField = null;

        try
        {
            // due to the fact that this method is currently
            // designed to handle tool to java conversion
            // we must get all of the declared fields and do
            // a caseless search as tool is not case sensitive
            // if we have someway of working out the destination
            // language then we should do a check here
            //lField = lCurrentObj.getClass().getDeclaredField(lCurrentField);

            // also to handle a particular tool convension that is not
            // used much in java we will strip leading '_' characters of
            // field name and check both types. This is a fudge that we may wish
            // to remove

/* @olsen+MBO: perf. optimization: replace reflect:field lookup by method findField that caches Field objects
            java.lang.StringBuffer lSBuff = new StringBuffer(lCurrentField);
            java.lang.String lAltFieldName = null;
            if (lSBuff.charAt(0) == '_')
            {
                // chop the '_' character off
                lSBuff.deleteCharAt(0);
                lAltFieldName = lSBuff.toString();
            }

            // this is a tmp store of the fields
            Field[] lTFields = null;
            // this is were all of the fields including the
            // super classes fields will end up
            java.util.ArrayList lFields = new ArrayList();

            // place holder for a class reference which initially
            // points to the class of the current object
            // but later will point to the currect objects's super clases
            java.lang.Class lClass = lCurrentObj.getClass();

            //lTFields = lCurrentObj.getClass().getDeclaredFields();

            while (lClass != null)
            {
                lTFields = lClass.getDeclaredFields();
                lFields.ensureCapacity(lTFields.length);

                for (int i = 0; i < lTFields.length; i++)
                {
                    lFields.add(lTFields[i]);
                }

                lClass = lClass.getSuperclass();
            }


        //    if (lFields == null ||
        //        lFields.length == 0)
            if (lFields.isEmpty())
            {
                java.lang.String message = new String("No fields on class " + lCurrentObj.getClass().getName());
                java.lang.NoSuchFieldException noFieldsError = new NoSuchFieldException(message);
                //SAXException noFieldsError = new SAXException(message);

                throw noFieldsError;

            }

        //    for (int i = 0; i < lFields.length; i++)
            int lSize = lFields.size();
            FORLOOP:
            for (int i = 0; i < lSize; i++)
            {
                Field tField = (Field)(lFields.get(i));
                if (tField.getName().equalsIgnoreCase(lCurrentField))
                {
                    lField = tField;
                    break FORLOOP;
                }// end if

                if (lAltFieldName != null)
                {
                    if (tField.getName().equalsIgnoreCase(lAltFieldName))
                   {
                       lField = tField;
                       break FORLOOP;
                   }// end if
                }// end if
            }// end for
*/

            //@olsen+MBO: perf. optimization
            lField = findField(lCurrentObj, lCurrentField);

            if (lField == null)
                // MBO: field not found. The current version of the class 
                // does not declare the field anymore => ignore. 
                return;

//            System.out.println(lField.toString());
//            System.out.println(lField.getType().getName());
//            System.out.println("Value is :" + value + ":");

            // first thing we are going to do it set the accessibility
            // flag such that we are able to access the field just in
            // case it is private/package/protected visibility

            //@olsen+MBO: moved to findField()
            //lField.setAccessible(true);

            java.lang.Class lFieldClass = lField.getType();
            if (lFieldClass.isPrimitive() &&
                value instanceof java.lang.String)
            {
                //@olsen+MBO: use .class instead of .TYPE
                if (lFieldClass == int.class)
                    lField.setInt(lCurrentObj, java.lang.Integer.parseInt((java.lang.String)(value)));
                else if (lFieldClass == short.class)
                    lField.setShort(lCurrentObj, java.lang.Short.parseShort((java.lang.String)(value)));
                else if (lFieldClass == long.class)
                    lField.setLong(lCurrentObj, java.lang.Long.parseLong((java.lang.String)(value)));
                //@olsen+MBO: changed for Byte
                else if (lFieldClass == byte.class)
                    lField.setByte(lCurrentObj, java.lang.Byte.parseByte((java.lang.String)(value)));
                else if (lFieldClass == double.class)
                    lField.setDouble(lCurrentObj, java.lang.Double.parseDouble((java.lang.String)(value)));
                else if (lFieldClass == float.class)
                    lField.setFloat(lCurrentObj, java.lang.Float.parseFloat((java.lang.String)(value)));
                else if (lFieldClass == boolean.class)
                    lField.setBoolean(lCurrentObj, (java.lang.Boolean.valueOf((java.lang.String)(value))).booleanValue());
                else if (lFieldClass == char.class)
                    lField.setChar(lCurrentObj, ((java.lang.String)(value)).charAt(0));
            }
            else if (lFieldClass.isArray() &&
                     value instanceof ArrayList)
            {
                // OK we are actually dealing with a primitive array here
                // but we have got an ArrayList so we need to do some transformation

                Class lArrayType = lFieldClass.getComponentType();
                ArrayList lList = (ArrayList)(value);

                Object lArray = Array.newInstance(lArrayType, lList.size());
                for ( int i = 0; i < lList.size(); i++)
                {
                    Array.set(lArray, i, lList.get(i));
                }

                lField.set(lCurrentObj, lArray);
            }
//            else if (lFieldClass.isArray())
//            {
//                lField.set(lCurrentObj, value);
//            }
            else {
                lField.set(lCurrentObj, value);
            }
        }
        catch (IllegalAccessException e1)
        {
            e1.printStackTrace();
            //java.lang.String message = new String("Illegal access of field " + lCurrentField);
            java.lang.String message = ("Illegal access of field " + lCurrentField);
            SAXException accessError = new SAXException(message);

            throw accessError;
        }

        catch (SecurityException e3)
        {
            e3.printStackTrace();
            //java.lang.String message = new String("Security Exception accessing fields of" + lCurrentObj.getClass().getName());
            java.lang.String message = ("Security Exception accessing fields of" + lCurrentObj.getClass().getName());
            SAXException accessError = new SAXException(message);

            throw accessError;
        }
        catch (RuntimeException e4)
        {
            e4.printStackTrace();
            throw e4;
        }
    } /*Method-End*/

    public  java.lang.Class  topRowType()
    {
        return (java.lang.Class)(this.RowTypeStack.peek());
    } /*Method-End*/


    private boolean checkSimpleRow(java.lang.String name, org.xml.sax.AttributeList atts) throws org.xml.sax.SAXException
    {
        boolean isSimple = false;

        String lRowType = atts.getValue("ROWCLASS");
        String lRowValue = atts.getValue("VALUE");

        if ( lRowType != null &&
             !lRowType.equals("")
           )
        {
            isSimple = true;

            if ( lRowType.equals("java.lang.String") )
                this.pushObject(lRowValue);
            else if ( lRowType.equals("java.lang.StringBuffer") )
                this.pushObject(new StringBuffer(lRowValue));
            else if (lRowType.equals("int") || lRowType.equals("java.lang.Integer"))
                this.pushObject(Integer.valueOf(lRowValue));
            else if (lRowType.equals("short")  || lRowType.equals("java.lang.Short"))
                this.pushObject(Short.valueOf(lRowValue));
            else if (lRowType.equals("long")  || lRowType.equals("java.lang.Long"))
                this.pushObject(Long.valueOf(lRowValue));
            else if (lRowType.equals("float")  || lRowType.equals("java.lang.Float"))
                this.pushObject(Float.valueOf(lRowValue));
            else if (lRowType.equals("double")  || lRowType.equals("java.lang.Double"))
                this.pushObject(Double.valueOf(lRowValue));
            else if (lRowType.equals("boolean")  || lRowType.equals("java.lang.Boolean"))
                this.pushObject(Boolean.valueOf(lRowValue));
            else if (lRowType.equals("char")  || lRowType.equals("java.lang.Character"))
                this.pushObject(new Character(lRowValue.charAt(0)));
            else
            {
                System.out.println("Found and unknown type in a row");
                this.pushObject(null);
            }

        }

        return isSimple;
    }

    public void DumpStatus()
    {
        // This method is a debug method to dump status information about this object
        super.DumpStatus();

        System.out.println("Dump Status from class XMLGraphSerializer");
        System.out.println("Row Type stack " + this.RowTypeStack);
        System.out.println("Row Counter stack " + this.RowCountStack);
        System.out.println("Object hash table " + this.ObjectHash);
        System.out.println("Dump Status from class XMLGraphSerializer - END");

    }

    private void pushRowCount()
    {
        Integer lInt = new Integer(0);
        this.RowCountStack.push(lInt);
    }

    private void popRowCount()
    {
        this.RowCountStack.pop();
    }

    private int currentRowCount()
    {
        Integer lInt = (Integer)(this.RowCountStack.peek());
        return lInt.intValue();
    }

    private void incrementRowCount()
    {
        Integer lInt = (Integer)(this.RowCountStack.pop());
        int lRowCount = lInt.intValue();
        lRowCount++;
        this.RowCountStack.push(new Integer(lRowCount));
    }

    // MBO: added new class WrapperClassHelper.

    /**
     * The WrapperClassHelper stores all necessary information (the wrapper
     * class object and an ID) in case the creation of a wrapper class
     * instance is postponed to when the value is available. Use method
     * valueOf(String s) to convert a String represenation into a value of
     * the wrapper class stored in the helper.
     */
    private static class WrapperClassHelper
    {
        /**
         * Interface defining a valueOf method that converts the specified
         * string into an instance of a wrapper class.
         */
        private static interface Converter { public Object valueOf(String s); }

        /** Map of wrapper class to Converter instances. */ 
        private static final Map converters;

        static 
        {
            // Initialize wrapper class to converter map
            converters = new HashMap(8);
            converters.put(Boolean.class, 
                           new Converter() { public Object valueOf(String s) 
                               { return Boolean.valueOf(s); } });
            converters.put(Byte.class,
                           new Converter() { public Object valueOf(String s) 
                               { return Byte.valueOf(s); } });
            converters.put(Short.class,
                           new Converter() { public Object valueOf(String s) 
                               { return Short.valueOf(s); } });
            converters.put(Character.class,
                           new Converter() { public Object valueOf(String s) 
                               { return new Character(s.charAt(0)); } });
            converters.put(Integer.class,
                           new Converter() { public Object valueOf(String s) 
                               { return Integer.valueOf(s); } });
            converters.put(Long.class,
                           new Converter() { public Object valueOf(String s) 
                               { return Long.valueOf(s); } });
            converters.put(Float.class,
                           new Converter() { public Object valueOf(String s) 
                               { return Float.valueOf(s); } });
            converters.put(Double.class,
                           new Converter() { public Object valueOf(String s) 
                               { return Double.valueOf(s); } });
        }

        /** Class instance of the wrapper class. */ 
        private Class wrapperClass;

        /** Key in the ObjectHash map. */ 
        private String id;

        /**
         * Constructor.
         * @param wrapperClasses the Class instance of the wrapper class.
         * @param id the key in the the ObjectHash map.
         */
        public WrapperClassHelper(Class wrapperClass, String id)
        {
            this.wrapperClass = wrapperClass;
            this.id = id;
        }

        /** Returns the key in the ObjectHash map. */
        public String getId()
        {
            return id;
        }

        /** 
         * Returns a wrapper Class object holding the value given by the
         * specified String. This method converts the specified string into
         * an instance of the wrapper class stored in this
         * WrapperClassHelper.
         * @param s the string to be parsed.
         * @return a wrapper Class object holding the value represented by
         * the string argument.
         */
        public Object valueOf(String s)
        {
            Converter conv = (Converter) converters.get(wrapperClass);
            return conv != null ? conv.valueOf(s) : null;
        }

        /** 
         * Determines if the specified argument represents a Java wrapper
         * class.  
         * @param class the Class to be tested
         * @return true if the specified argument represents a Java wrapper 
         * class.
         */
        public static boolean isWrapperClass(Class clazz)
        {
            return converters.containsKey(clazz);
        }

    }

}  // end of class
