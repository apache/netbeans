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

// GENERATED CODE FOR Sun ONE Studio class.
//
package org.netbeans.modules.dbschema.migration.archiver.deserializer;

import java.lang.reflect.*;

import org.xml.sax.*;

public  class BaseSpecificXMLDeserializer extends BaseXMLDeserializer
    implements SpecificXMLDeserializer
{
    // Fields

    public static final String WRONG_TAG = "Saw tag {1} when {2} was expected.";
    protected   java.lang.Integer State;
    protected   java.lang.Class ParameterClass;
    protected   java.lang.Class ParameterSetMethod;

//@olsen+MBO: used unsynchronized HashMap and ArrayListStack
    ArrayListStack StateStack;
    ArrayListStack ObjectStack;
    protected   java.util.HashMap ActiveAliasHash;
/*
    protected   java.util.Stack StateStack;
    protected   java.util.Stack ObjectStack;
    protected   java.util.Hashtable ActiveAliasHash;
    protected   java.util.Hashtable ClassHash; // @olsen+MBO: NOT USED!
*/

    protected   java.util.Vector ParameterArray;
    public      java.util.Vector ParameterTypeArray;
    protected   XMLDeserializer MasterDeserializer;
    private     ClassLoader classLoader;  //@lars


    // Constructors

    //@lars: added classloader-constructor
    public BaseSpecificXMLDeserializer (ClassLoader cl)
    {
        super();
//@olsen+MBO: used unsynchronized HashMap and ArrayListStack
        this.ObjectStack    = new ArrayListStack();
        this.StateStack     = new ArrayListStack();
        this.classLoader    = (cl != null  ?  cl  :  getClass ().getClassLoader ());
/*
        this.ObjectStack    = new java.util.Stack();
        this.StateStack     = new java.util.Stack();
*/
        this.setMasterDeserializer(this);
    } /*Constructor-End*/

    public  BaseSpecificXMLDeserializer()
    {
        this (null);
    } /*Constructor-End*/

    // Methods

    public   void setMasterDeserializer(XMLDeserializer master)
    {
        this.MasterDeserializer = master;
    } /*Method-End*/

    public   void unexpectedTag(String actual,String expected, boolean endTagExpected) throws org.xml.sax.SAXException
    {

        if (endTagExpected)
        {
            //String endTag = new String("/" + expected);
            //expected = endTag;
            expected = "/" + expected;

        }

        String message = ("Saw tag " + actual +  " when " + expected + " was expected.");

        SAXException tagError = new SAXException(message);

        throw tagError;
    } /*Method-End*/

    public   void validateTag(String actual, String expected, boolean endTagExpected) throws org.xml.sax.SAXException
    {
        if ( !actual.equals(expected) )
        {
            this.unexpectedTag(actual, expected, endTagExpected);
        }
    } /*Method-End*/

    public   void popState()
    {
        this.State = (Integer)(this.StateStack.pop());
    } /*Method-End*/

    public   void pushState(int newState)
    {
        // put the old state on the top of the stack

        this.StateStack.push( this.State );

        // and now set the state to the new state
        this.State = new Integer(newState);
    } /*Method-End*/

    public   void addActiveAlias(String name, String alias)
    {
        if (this.ActiveAliasHash == null)
        {
            //this.ActiveAliasHash = new java.util.Hashtable(20, 0.75F);
            this.ActiveAliasHash = new java.util.HashMap(20);
        }

//@olsen+MBO: removed redundant code
/*
        if (this.ActiveAliasHash.containsKey(alias))
        {
            this.ActiveAliasHash.remove(alias);
        }
*/
        this.ActiveAliasHash.put(alias, name);
    } /*Method-End*/

    public  String  lookupAlias(String name)
    {
        //
        //   this method searches the alias hashtable
        //   if it exists and returns the name of the alias
        //   otherwise it simply returns the name that was
        //   passed in
        //
        String retName = null;

        if (this.ActiveAliasHash != null)
        {
            retName = (String)(this.ActiveAliasHash.get(name));

        }
        if (retName == null)
        {
            retName = name;
        }

        return retName;
    } /*Method-End*/

    public  Class  findClass(String name) throws java.lang.ClassNotFoundException
    {
        Class lReturnClass;

//@lars: added classloader
//        lReturnClass = Class.forName (name);
        
        
        name=  org.netbeans.modules.dbschema.migration.archiver.MapClassName.getRealClassName(name);
        lReturnClass = java.lang.Class.forName(name, true /*initialize the class*/, this.classLoader);

        return lReturnClass;
    } /*Method-End*/

    public  Object  popObject()
    {
        return this.ObjectStack.pop();
    } /*Method-End*/

    public   void pushObject(Object obj)
    {
        this.ObjectStack.push(obj);
    } /*Method-End*/

    public  String  unescapeName(String name)
    {
        // this method is going to strip the _ and - from
        // the beginning of the name

//@olsen+MBO: minimized number of objects and operations
        if (name.startsWith("_-")) {
            return name.substring(2);
        }

        int idx = name.indexOf('-');
        if (idx >= 0) {
            StringBuffer buf = new StringBuffer(name);
            buf.setCharAt(idx, '_');
            return buf.toString();
        }

        return name;
/*
        StringBuffer lStr = new StringBuffer(name);

        if ( (lStr.charAt(0) == '_') &&
             (lStr.charAt(1) == '-') )
        {
            lStr.delete(0,2);
        }
        else
        {
            boolean lFound = false;
            int lLocation;
            // search for dash
            loop:
            for (lLocation = 0; lLocation < lStr.length(); lLocation++)
            {
                if (lStr.charAt(lLocation) == '-')
                {
                    lFound = true;
                     break loop;
                }// end if
            }// end for

            // if we find an dash replace it with a underscore
            if (lFound)
            {
                lStr.replace(lLocation, lLocation + 1, "_");

            }// end if


        }// end if
        return lStr.toString();
*/
    } /*Method-End*/

    public  boolean  useExistingAttribute(org.xml.sax.AttributeList atts, String attrname, Object existing) throws org.xml.sax.SAXException
    {
        boolean retBool = false;

        String useDirective = atts.getValue("USE");

        if (useDirective != null &&
            useDirective.equals("EXISTING"))
        {
//@olsen+MBO: ever stepped in?

            java.lang.Object lCurrentObj = this.topObject();
            Field lField = null;
            try
            {
                lField = lCurrentObj.getClass().getDeclaredField(attrname);
                existing = lField.get(lCurrentObj);
            }
            catch (IllegalArgumentException e1)
            {
                // add the illegal arg exception to the exception stack
                // and then mask it under a SAXexception and raise the
                // SAXException

                //String message = new String("Illegal Argument used " + lCurrentObj.getClass().getName());
                String message = ("Illegal Argument used " + lCurrentObj.getClass().getName());
                SAXException useError = new SAXException(message);
                throw useError;
            }
            catch (IllegalAccessException e2)
             {
                 // add the illegal access exception to the exception stack
                 // and then mask it under a SAXexception and raise the
                 // SAXException
                 //String message = new String("Illegal Access of field " + lField);
                 String message = ("Illegal Access of field " + lField);
                 SAXException useError = new SAXException(message);
                 throw useError;
             }
            catch (NoSuchFieldException e3)
             {
                 // add the no such field exception to the exception stack
                 // and then mask it under a SAXexception and raise the
                 // SAXException

                 //String message = new String("No such field " + attrname);
                 String message = ("No such field " + attrname);
                 SAXException useError = new SAXException(message);
                 throw useError;
             }

            retBool = true;
        }
        else if (useDirective != null)
        {
            //String message = new String("Invalid value USE for attribute " + useDirective);
            String message = ("Invalid value USE for attribute " + useDirective);

            SAXException useError = new SAXException(message);
            throw useError;

        }// end if

        return retBool;
    } /*Method-End*/

    public  java.lang.Object  topObject() throws org.xml.sax.SAXException
    {
        if (this.ObjectStack.size() == 0)
        {
            //String message = new String("Object Stack Empty");
            String message = ("Object Stack Empty");

            SAXException stackError = new SAXException(message);

            throw stackError;

        }

        return this.ObjectStack.peek();
    } /*Method-End*/

    public   void freeResources()
    {
        super.freeResources();
        this.ObjectStack.clear();
        //ParameterArray.clear();
        //ParameterTypeArray.clear();
        StateStack.clear();
        if (ActiveAliasHash != null)
            ActiveAliasHash.clear();
    } /*Method-End*/

    public void DumpStatus()
    {
        // This method is a debug method to dump status information about this object
        super.DumpStatus();

        System.out.println("Dump Status from class BaseSpecificXMLSerializer");
        System.out.println("Current state " + this.State);
        System.out.println("State stack " + this.StateStack);
        System.out.println("Object Stack " + this.ObjectStack);
        System.out.println("Dump Status from class BasespecificXMLSerializer - END");

    }

}  // end of class
