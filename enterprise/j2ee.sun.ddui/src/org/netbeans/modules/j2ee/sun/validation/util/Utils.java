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

package org.netbeans.modules.j2ee.sun.validation.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;


/**
 * Utils is an utility class. Provides various utility methods.
 *
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class Utils {

    private static final String GET_PREFIX = "get";                     //NOI18N


    /** Creates a new instance of Utils */
    public Utils() {
    }


    /**
    * Gets the name from the absolute name/path
    * 
    * @param absoluteName the complete path name
    * @param delimiter the separator character used
    * in the <code>absoluteName</code>
    * @return the name
    */
    public String getName(String absoluteName, int delimiter) {
        if(null == absoluteName){
            return absoluteName;
        }
        int index = absoluteName.lastIndexOf(delimiter);
        if( index != -1) {
            index = index + 1;
            return absoluteName.substring(index);
        } else {
            return absoluteName;
        }
    }


    /**
    * Gets the parent of the given absolute name
    * 
    * @param absoluteName the complete path name
    * @param delimiter the separator character used
    * in the <code>absoluteName</code>
    * @return the parent of the given absolute name;
    * returns <code>null</code> if the given name does not have parent.
    */
    public String getParentName(String absoluteName, int  delimiter) {
        if(null == absoluteName){
            return absoluteName;
        }
        int endIndex = absoluteName.lastIndexOf(delimiter);
        if(endIndex != -1){
            if(0 == endIndex){
                return null;
            } else {
                return absoluteName.substring(0, endIndex);
            }
        } else {
            return null;
        }
    }


    /**
    * Converts the first letter of the given string to Uppercase.
    * 
    * @param string the input string
    * @return the string with the Uppercase first letter
    */
    public String upperCaseFirstLetter(String string)
    {
        if(string == null || string.length() <= 0){
            return string;
        }
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }


    /**
    * Creates an instance of the given type.
    * Uses constructor with no arguments to instantiate the type object.
    * 
    * @param type the type name
    * @return the <code>Object</code> of the given <code>type</code>
    */
    public Object createObject(String type) {
        Object object = null;
        try {
          Class classObject = Class.forName(type);
          object = classObject.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
          System.out.println(e);
        }
        return object;
    }


    /**
    * Creates an instance of the type; given the <code>Class</code>
    * object of the type. Uses constructor with no arguments to
    * instantiate the type object.
    * 
    * @param class the Class object of the type to create instance of.
    * @return the <code>Object</code> of the given <code>type</code>
    */
    public Object createObject(Class classObject) {
        Object object = null;
        try {
          object = classObject.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
          System.out.println(e);
        }
        return object;
    }    


    /**
    * Creates an instance of the type; given the <code>Constructor</code>
    * object of the type and the array of argument values.
    * Uses constructor, represented by the input <code>Constructor</code>
    * object.
    * 
    * @param constructor the <code>Constructor</code> object of the type
    * @param arguments an array of arugments to the constructor
    * @return the <code>Object</code> of the given <code>type</code>
    */
    public Object createObject(Constructor constructor, 
                                     Object[] arguments) {
        //System.out.println ("Constructor: " + constructor.toString());
        Object object = null;

        try {
        object = constructor.newInstance(arguments);
        //System.out.println ("Object: " + object.toString());
        return object;
        } catch (ReflectiveOperationException e) {
          System.out.println(e);
        }
        return object;
    }


    /**
     * Gets a corresponding Constructor object of a given type, 
     * based on the Class objects of the arguments.
     * Constructor object represents constructor which takes arguments
     * represented by argumentClass array.
     * 
     * @param type the type to get the Constructor of.
     * @param argumentClass an array <code>Class</code> objects of the arugments
     * @return the <code>Constructor</code> of the given <code>type</code>
     */
    public Constructor getConstructor(String type,
            Class[] argumentClass){
        Constructor constructor = null;
        Class classObject = getClass(type);

        try {
            constructor = classObject.getConstructor(argumentClass);
        } catch (NoSuchMethodException e) {
            System.out.println(e);
        }            
        return constructor;
    }

    
    /**
     * Gets a corresponding Constructor object of a given type, 
     * based on the Class objects of the arguments.
     * Constructor object represents constructor which takes arguments
     * represented by argumentClass array.
     * 
     * @param classObject the <code>Class</code> object of the type to
     * get Constructor of.
     * @param argumentClass an array of <code>Class</code> objects of
     * the arugments
     * @return the <code>Constructor</code> of the given <code>type</code>
     */
    public Constructor getConstructor(Class classObject,
        Class[] argumentClass){
        Constructor constructor = null;

        try {
            constructor = classObject.getConstructor(argumentClass);
        } catch (NoSuchMethodException e) {
            System.out.println(e);
        }            
        return constructor;
    }

    
    /**
     * Gets a <code>Class</code> object of the given <code>Object</code>.
     * 
     * @param object the given <code>Object</code>
     * @return the <code>Class</code> object of the given <code>Object</code>
     */
    public Class getClass(Object object){
        Class classObject = null;

        classObject =  object.getClass();
        ///System.out.println(classObject.toString());
        return classObject;
    }

    
    /**
     * Gets a <code>Class</code> object of the given type.
     * 
     * @param object the given type
     * @return the <code>Class</code> object of the given type
     */
    public Class getClass(String type){
        Class classObject = null;

        try {
            classObject =  Class.forName(type);
        } catch (ClassNotFoundException e) {
            System.out.println(e);
        }
        return classObject;
    }

    
    /**
     * Gets a Method object of a given type, based on method name and 
     * the Class objects of the arguments. 
     * 
     * @param type the type to fetch method of.
     * @param methodName the name of the method to fetch 
     * <code>Method</code> object of.
     * @param argumentClass an array of <code>Class</code> objects of
     * the arugments
     * @return the <code>Method</code> object of the given type
     */
    public Method getMethod(String type, String methodName,
        Class[] argumentClass){
        Method method = null;
        Class classObject = getClass(type);

        try {
            method = classObject.getMethod(methodName, argumentClass);
        } catch (NoSuchMethodException e) {
            System.out.println(e);
        }            
        return method;
    }

    
    /**
     * Gets a Method object of a given type, based on method name and 
     * the Class objects of the arguments. 
     * 
     * @param classObject the <code>Class</code> object of
     * the type to fetch method of.
     * @param methodName the name of the method to fetch 
     * <code>Method</code> object of.
     * @param argumentClass an array of <code>Class</code> objects of 
     * the arugments
     * @return the <code>Method</code> object of the given type
     */
    public Method getMethod(Class classObject, String methodName,
        Class[] argumentClass){
        Method method = null;

        try {
            method = classObject.getMethod(methodName, argumentClass);
        } catch (NoSuchMethodException e) {
            System.out.println(e);
        }            
        return method;
    }

    
    /**
     * Invokes the method, on the given object with the given arguments.
     * Invokes method, represented by the <code>Method</code> object.
     * 
     * @param object the <code>Object</code> to invoke the method of
     * @param method the method to be invoked
     * @param arguments an array of <code>Objects</code> to be used as
     * arugments to the method being invoked
     * @return an <code>Object</code>, returned by the invoked method.
     */
    public Object invoke(Object object, Method method, 
                                     Object[] arguments) {
      Object result = null;
      try {
        result = method.invoke(object, arguments);
      } catch (IllegalAccessException e) {
          System.out.println(e);
      } catch (InvocationTargetException e) {
          System.out.println(e);
      }
      return result;
    }


    /**
     * Gets the <code>Method</code> object of the given
     * type; given the method name.
     * 
     * @param type the given type, to get the method of
     * @param methodName the method name to get the 
     * <code>Method</code> object of
     * @return a <code>Method</code> object of the given type
     */
    public Method getMethod(String type, String methodName){
        Method method = null;
        Class classObject = getClass(type);

        try {
            method = classObject.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            System.out.println(e);
        }            
        return method;
    }

    
    /**
     * Gets the <code>Method</code> object of the given
     * type; given the method name.
     * 
     * @param classObject the <codeb>Class</code> object
     * of the given type, to get the method of
     * @param methodName the method name to get the
     * <code>Method</code> object of
     * @return a <code>Method</code> object of the given type
     */
    public Method getMethod(Class classObject,
            String methodName){
        Method method = null;

        try {
            method = classObject.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            System.out.println(e);
        }            
        return method;
    }


    /**
     * Invokes the method, on the given object.
     * Invokes method, represented by the Method object.
     * 
     * @param object the <codeb>Olass</code> to invoke the method of
     * @param method the method to be invoked
     * @return an <code>Object</code> returned by the invoked method
     */
    public Object invoke(Object object, Method method) {
            Object result = null;
      try {
        result = method.invoke(object);
      } catch (IllegalAccessException e) {
          System.out.println(e);
      } catch (InvocationTargetException e) {
          System.out.println(e);
      }
      return result;
    }


    /**
     * Removes any hypens ( - ) from the given string.
     * When it removes a hypen, it converts next immidiate
     * character, if any,  to an Uppercase.(schema2beans convention)
     * @param string the input string
     * @return a <code>String</code> resulted after removing the hypens
     */
    public String eleminateHypen(String string){
        if(!(string == null || string.length() <= 0)){
            int index = string.indexOf('-');
            while(index != -1){
                if(index == 0){
                    string = string.substring(1);
                } else {
                    if(index == (string.length() - 1)){
                        string = string.substring(0,string.length()-1);
                    } else {
                        string = string.substring(0,index) +
                            upperCaseFirstLetter(string.substring(index + 1));
                    }
                }
                index = string.indexOf('-');
            }
        }
        return string;
    }

    
    /** 
     * Constructs a method name from  element's bean
     * name for a given prefix.(schema2beans convention)
     *
     * @param elementName the given element name
     * @param prefix the given prefix
     * @return a method name formed from the given name and the prefix
     */
    public String methodNameFromBeanName(String elementName,
            String prefix){
        if((null == elementName) || (null == prefix) ||
                (prefix.length() <= 0 )){
            return elementName;
        }
        String methodName = upperCaseFirstLetter(elementName);
        return methodName = prefix + methodName;
    }

    
    /** 
     * Constructs a method name from  element's dtd name
     * name for a given prefix.(schema2beans convention)
     *
     * @param elementName the given element name
     * @param prefix the given prefix
     * @return a method name formed from the given name and the prefix
     */
    public String methodNameFromDtdName(String elementName,
            String prefix){
        return methodNameFromBeanName(eleminateHypen(elementName), prefix);
    }


    /** 
     * Gets an element from the given <code>Object</code>;
     * given the name of the element.
     *
     * @param elementName the name of the element to get from the given object
     * @param object the given object
     * @return the retrieved element from the given object; returns null if the
     * following cases :
     *      the given <code>object</code> is null
     *      the given <code>elementName</code> is null or zero length.
     */
    public Object getElement(String elementName, Object object){
        //Consvert the first letter of elementName to Uppercase
        //Construct method name by appending given "get" to elementName //NOI18N
        //Invoke this method on object to get the required value
        if((null ==  object) || (null == elementName) || 
            (elementName.length() <= 0)){
            return null;
        }

        String methodName = 
                methodNameFromDtdName(elementName, GET_PREFIX);        //NOI18N
        Method getMethod = null;
        getMethod = getMethod(getClass(object), methodName);
        return invoke(object, getMethod);
    }


    /** 
     * Gets the elements from the given <code>Object</code>;
     * given the name of the element.
     *
     * @param elementName the name of the elements to get from the given object
     * @param object the given object
     * @return an array of the retrieved elements from the given object; returns
     * null in the following cases :
     *      the given <code>object</code> is null
     *      the given <code>elementName</code> is null or zero length.
     */
    public Object[] getElements(String elementName, Object object){
        return (Object[]) getElement(elementName, object);
    }


    /** 
     * Gets an element at a given index, from the given <code>Object</code>; 
     * given the name of the element.
     *
     * @param elementName the name of the element to get from the given object
     * @param index the given index
     * @param object the given object
     * @return the retrieved element from the given object; returns null in the
     * following cases: 
     *      the given <code>object</code> is null
     *      the given <code>elementName</code> is null or zero length.
     *      the given <code>index</code> is less than zero.
     */
    public Object  getElement(String elementName, int index,
            Object object){
        //Consvert the first letter of elementName to Uppercase
        //Construct method name by appending given "get" to elementName //NOI18N
        //Invoke this method on object to get the required value
       if((null ==  object) || (null == elementName) || 
            (elementName.length() <= 0) || (index < 0)){
            return null;
       }

       String methodName = 
                methodNameFromDtdName(elementName, GET_PREFIX);        //NOI18N
       Class[] argumentTypes = new Class[] {int.class}; 
       Method getMethod = null;
       getMethod = getMethod(getClass(object), methodName,
                argumentTypes);

       Integer in = index;
       Object[] argumentValues = new Object[] {in};
       return invoke(object, getMethod, argumentValues);
    }


    /** 
     * Gets an element from the given <code>Object</code>;
     * given the name of the element and prefix to use to for the method name.
     *
     * @param elementName the name of the element to get from the given object
     * @param object the given object
     * @param prefix the prefix to use to formulate the name of the fetch method
     * @return Object the retrieved element from the given object; returns
     * null if the following cases :
     *      the given <code>object</code> is null
     *      the given <code>elementName</code> is null or zero length.
     */
    public Object getElement(String elementName, Object object,
        String prefix){
        //Consvert the first letter of elementName to Uppercase
        //Construct method name by appending given "perfix" to elementName
        //Invoke this method on object to get the required value
        if((null ==  object) || (null == elementName) || 
            (elementName.length() <= 0)){
            return null;
        }

        String methodName = methodNameFromDtdName(elementName, prefix);
        Class classObject = getClass(object);
        Method method = getMethod(classObject, methodName);
        return (Integer) invoke(object, method);
    }


    /** 
     * Gets an indexed name from a given name and index.
     *
     * @param name the given name
     * @param index the given index
     * @return the indexed name; returns null if the
     * given <code>name</code> is null.
     */
    public String getIndexedName(String name, int index){
        if(name != null) {
            String format = 
                BundleReader.getValue("Indexed_Name_Format");           //NOI18N
            Object[] arguments = new Object[]{name, String.valueOf(index)};
            name = MessageFormat.format(format, arguments);
        }
        return name;
    }


    /** 
     * Gets an Url object for a given relative path.
     */
    public URL getUrlObject(String relativePath){
        Class cl = getClass();
        ClassLoader classLoader = cl.getClassLoader();
        return classLoader.getResource(relativePath);
    }


    /** 
     * Gets an input stream for the given file.
     *
     * @param relavtiveFilePath the relative path name of the file.
     *
     * @return InputStream the input stream for the given file; returns null 
     * in case of failure.
     */
    public InputStream getInputStream(String relavtiveFilePath){
        InputStream inputStream = null;
        URL url = null;
        if(relavtiveFilePath != null){
            url = getUrlObject(relavtiveFilePath);
            if(url != null) {
                try {
                    inputStream = url.openStream();
                } catch (IOException exception){
                    System.out.println(exception.getMessage());
                }
            } else {
                String format = 
                    BundleReader.getValue("Error_does_not_exists");     //NOI18N
                Object[] arguments =    
                    new Object[]{"File", relavtiveFilePath};            //NOI18N
                System.out.println(MessageFormat.format(format, arguments));
            }
        }
        return inputStream;
    }

    
    /** 
     * Determines if the given file exists.
     *
     * @param relativePath the relative path name of the file.
     *
     * @return boolean true if the given file exists; else false.
     */
    public boolean fileExists(String relativePath){
        boolean returnValue = false;
        InputStream inputStream = getInputStream(relativePath);
        if(inputStream != null){
            returnValue = true;
        }
        return returnValue;
    }
    
}
