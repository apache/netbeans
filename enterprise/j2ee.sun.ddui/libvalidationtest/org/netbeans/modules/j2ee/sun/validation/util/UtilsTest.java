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

package org.netbeans.modules.j2ee.sun.validation.util;

import junit.framework.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */

public class UtilsTest extends TestCase{
    /* A class implementation comment can go here. */

    private  Utils utils = null;

    public UtilsTest(String name){
        super(name);
        utils = new Utils();
    }


    public static void main(String args[]){
        junit.textui.TestRunner.run(suite());
    }


    public void testGetIndexedName(){
        String element = "element";                                     //NOI18N
        int index = 5;
        assertTrue("element(5)".equals(                                 //NOI18N
                utils.getIndexedName(element,index)));
        assertTrue(null == utils.getIndexedName(null,index));
        
    }


    //test for : 
    //      public static Object getElement(String elementName, Object object)
    //      public static Object[] getElements(String elementName,
    //          Object object)
    //      public static Object  getElement(String elementName,
    //          int index, Object object)
    //      public static Object  getElement(String elementName,
    //          int index, Object object)
    //      public static Object getElement(String elementName, Object object,
    //          String prefix)
    public void testGetElement(){
        Object object = new CustomObject();
        assertTrue("foo".equals(                                        //NOI18N
                (String)utils.getElement("foo", object)));              //NOI18N
        assertTrue(null == utils.getElement("", object));               //NOI18N
        assertTrue(null == utils.getElement(null, object));
        assertTrue(null == utils.getElement("foo", null));              //NOI18N
        assertTrue(null == utils.getElement(null, null));               //NOI18N
        
        Integer integer = (Integer) utils.getElement("integer", object);//NOI18N
        assertTrue(5 == integer.intValue());
        String[] colours = 
            (String[]) utils.getElements("primaryColours",  object);    //NOI18N
        assertTrue("green".equals(colours[1]));                         //NOI18N
        
        String colour = 
            (String) utils.getElement("primaryColours", 2, object);     //NOI18N
        assertTrue("blue".equals(colour));                              //NOI18N
        assertTrue(null == utils.getElement("", 2, object));            //NOI18N
        assertTrue(null == utils.getElement(null, 2, object));
        assertTrue(null == utils.getElement("primaryColours", 2, null));//NOI18N
        assertTrue(null == utils.getElement("primaryColours", -1,       //NOI18N
                object));
        assertTrue(null == utils.getElement(null, -3, null));
        
        assertTrue(null == utils.getElement("", object, ""));           //NOI18N
        assertTrue(null == utils.getElement("primaryColours",           //NOI18N
                null, "size"));                                         //NOI18N
        integer = (Integer) utils.getElement(
                "primaryColours", object, "");                          //NOI18N
        assertTrue(3 == integer.intValue());
        integer = (Integer) utils.getElement(
                "primaryColours", object, null);                        //NOI18N
        assertTrue(3 == integer.intValue());
        integer = (Integer) utils.getElement(
                "primaryColours", object, "size");                      //NOI18N
        assertTrue(4 == integer.intValue());
    }


    public void testMethodNameFromDtdName(){
       String name = "ejb-name";                                        //NOI18N
       String prefix = "set";                                           //NOI18N
       assertTrue("setEjbName".equals(                                  //NOI18N
            utils.methodNameFromDtdName(name, prefix)));
    }


   public void testMethodNameFromBeanName(){
       String beanName = "name";                                        //NOI18N
       String prefix = "get";                                           //NOI18N
       assertTrue("getName".equals(                                     //NOI18N
            utils.methodNameFromBeanName(beanName, prefix)));
       
       prefix = null;
       assertTrue("name".equals(                                        //NOI18N
            utils.methodNameFromBeanName(beanName, prefix)));

       prefix = "";                                                     //NOI18N    
       assertTrue("name".equals(                                        //NOI18N
            utils.methodNameFromBeanName(beanName, prefix)));
       
       beanName = "";                                                   //NOI18N
       prefix = "set";                                                  //NOI18N
       assertTrue("set".equals(                                         //NOI18N
            utils.methodNameFromBeanName(beanName, prefix)));
       
       beanName = null;
       prefix = "set";                                                  //NOI18N
       assertTrue(null == utils.methodNameFromBeanName(beanName, prefix));
   }


    public void testEleminateHypen(){
        String str = "hello";                                           //NOI18N
        assertTrue(null ==  utils.eleminateHypen(null));
        assertTrue("".equals(utils.eleminateHypen("")));                //NOI18N
        assertTrue("hello".equals(utils.eleminateHypen(str)));          //NOI18N
        str = "-hello";                                                 //NOI18N
        assertTrue("hello".equals(utils.eleminateHypen(str)));          //NOI18N
        str = "hello-";                                                 //NOI18N
        assertTrue("hello".equals(utils.eleminateHypen(str)));          //NOI18N
        str = "hello-world";                                            //NOI18N
        assertTrue("helloWorld".equals(utils.eleminateHypen(str)));     //NOI18N
        str = "hello-whole-world";                                      //NOI18N    
        assertTrue("helloWholeWorld".equals(utils.eleminateHypen(str)));//NOI18N
        str = "-hellO-WhOle-wOrld-";                                    //NOI18N
        assertTrue("hellOWhOleWOrld".equals(utils.eleminateHypen(str)));//NOI18N
    }
    
    //test for : 
    //      public static Method getMethod(String type, String methodName)
    //      public static Method getMethod(Class classObject, String methodName)
    //      public static Object invoke(Object object, Method method)
    public void testGetMethod_One(){
        String str = "hello";                                           //NOI18N
        Method method = utils.getMethod("java.lang.String", "length");  //NOI18N
        int length = ((Integer)utils.invoke(str, method)).intValue();
        assertTrue(5 == length);
        
        method = utils.getMethod(String.class, "length");               //NOI18N
        length = ((Integer)utils.invoke(str, method)).intValue();
        assertTrue(5 == length);
    }

    
    //test for : 
    //      public static Method getMethod(String type, String methodName,
    //          Class[] argumentClass)
    //      public static Method getMethod(Class classObject, String methodName,
    //          Class[] argumentClass){
    //      public static Object invoke(Object object, Method method, 
    //          Object[] arguments)    
    public void testGetMethod(){
        String str = "hello";                                        //NOI18N
        Class[] argumentClass = new Class[] {char.class, char.class};
        Method method = 
            utils.getMethod("java.lang.String", "replace",              //NOI18N
                argumentClass);
        Character oldChar = new Character('h');
        Character newChar = new Character('H');
        Object[] parameters = new Object[] { oldChar, newChar };
        assertTrue("Hello".equals(utils.invoke(str, method,             //NOI18N
                parameters)));
        
        method = utils.getMethod(String.class, "replace",               //NOI18N
                argumentClass);
        assertTrue("Hello".equals(utils.invoke(str, method,             //NOI18N
                parameters)));
    }

        
    //test for : 
    //      public static Class getClass(String type)
    //      public static Class getClass(Object object)
    public void testGetClass(){
        assertTrue(String.class == utils.getClass("java.lang.String")); //NOI18N
        Integer integer = new Integer(1234);
        assertTrue(Integer.class == utils.getClass(integer));
    }


    //test for :
    //      public static Object createObject(Constructor constructor,
    //          Object[] arguments)
    //      public static Constructor getConstructor(String type,
    //          Class[] argumentClass)
    //       public static Constructor getConstructor(Class classObject,
    //          Class[] argumentClass)
    public void testCreatObject_Two(){
        Class[] argumentTypes = new Class[] {String.class};
        Constructor constructor = 
            utils.getConstructor("java.lang.Integer", argumentTypes);   //NOI18N
        Object[] argumentValues = new Object[] {"1234"};                //NOI18N
        Object object = utils.createObject(constructor, argumentValues);
        Class objectClass = object.getClass();
        String objectName = objectClass.getName();
        assertTrue("java.lang.Integer".equals(objectName));             //NOI18N
        object = (Integer)object;
        assertTrue("1234".equals(object.toString()));                   //NOI18N
        
        Class[] argTypes = new Class[] {int.class};
        Constructor cons = utils.getConstructor(object.getClass(), argTypes);
        Integer parameter = new Integer(4567);
        Object[] arguments = new Object[] {parameter};
        Object obj = utils.createObject(cons, arguments);
        assertTrue("java.lang.Integer".equals(objectName));             //NOI18N
        obj = (Integer)obj;
        assertTrue("4567".equals(obj.toString()));                      //NOI18N
    }


    //test for : public static Object createObject(Class classObject)
    public void testCreateObject_One(){
        Object object = utils.createObject("java.lang.String");         //NOI18N
        String str = "hello";                                           //NOI18N
        Class objectClass = str.getClass();
        String name = objectClass.getName();
        assertTrue("java.lang.String".equals(name));                    //NOI18N
    }


    //test for :  public static Object createObject(String type)
    public void testCreateObject(){
        Object object = utils.createObject("java.lang.String");         //NOI18N
        Class objectClass = object.getClass();
        String name = objectClass.getName();
        assertTrue("java.lang.String".equals(name));                    //NOI18N
    }


    public void testGetUpperCaseFirstLetter() {
        assertTrue("Hello".equals(                                      //NOI18N
                utils.upperCaseFirstLetter("hello")));                  //NOI18N
        assertTrue("Hello".equals(                                      //NOI18N
                utils.upperCaseFirstLetter("Hello")));                  //NOI18N
        assertTrue("H".equals(                                          //NOI18N
                utils.upperCaseFirstLetter("h")));                      //NOI18N
        assertTrue(null == utils.upperCaseFirstLetter(null));           //NOI18N
        assertTrue("".equals(                                           //NOI18N
                utils.upperCaseFirstLetter("")));                       //NOI18N
    }


    public void testGetParentName() {
        assertTrue("/root/parent".equals(                               //NOI18N
                utils.getParentName("/root/parent/child", '/')));       //NOI18N
        assertTrue(null == utils.getParentName("child", '/'));          //NOI18N
        assertTrue(null == utils.getParentName("/root", '/'));          //NOI18N
        assertTrue(null == utils.getParentName(null, '/'));
    }


    public void testGetName() {
        assertTrue("child".equals(
                utils.getName("/root/parent/child", '/')));             //NOI18N
        assertTrue("child".equals(utils.getName("child", '/')));        //NOI18N
        assertTrue("root".equals(utils.getName("/root", '/')));         //NOI18N
        assertTrue(null == utils.getName(null, '/'));
    }
    
    
    /**
     * Define suite of all the Tests to run.
     */
    public static Test suite(){
        TestSuite suite = new TestSuite(UtilsTest.class);
        return suite;
    }


    /**
     * Initialize; allocate any resources needed to perform Tests.
     */
    protected void setUp() {
    }
    
    /**
     * Free all the resources initilized/allocated to perform Tests.
     */
    protected void tearDown() {
    }
    
    private void nyi() {
        ///fail("Not yet implemented");                                 //NOI18N
    }
    
    class CustomObject extends Object
    {
        String name = "foo";                                            //NOI18N
        Integer integer = new Integer(5);
        String[] primaryColours = 
                new String[] {"red", "green", "blue", "yellow"};        //NOI18N

        public String getFoo(){
          return name;
        }
        
        public int getInteger(){
            return integer.intValue();
        }
        
        public String[] getPrimaryColours(){
            return primaryColours;
        }
        
        public String getPrimaryColours(int index){
            return primaryColours[index];
        }
        
        public Integer sizePrimaryColours(){
            return new Integer(4);
        }
        
        public Integer primaryColours(){
            return new Integer(3);
        }
    }
}
