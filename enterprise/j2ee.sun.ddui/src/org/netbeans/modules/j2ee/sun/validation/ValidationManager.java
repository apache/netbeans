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

package org.netbeans.modules.j2ee.sun.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.netbeans.modules.j2ee.sun.validation.constraints.CardinalConstraint;
import org.netbeans.modules.j2ee.sun.validation.constraints.Constraint;
import org.netbeans.modules.j2ee.sun.validation.constraints.data.Argument;
import org.netbeans.modules.j2ee.sun.validation.constraints.data.Arguments;
import org.netbeans.modules.j2ee.sun.validation.constraints.data.CheckInfo;
import org.netbeans.modules.j2ee.sun.validation.constraints.data.Constraints;
import org.netbeans.modules.j2ee.sun.validation.data.Check;
import org.netbeans.modules.j2ee.sun.validation.data.Element;
import org.netbeans.modules.j2ee.sun.validation.data.Validation;
import org.netbeans.modules.j2ee.sun.validation.data.Parameter;
import org.netbeans.modules.j2ee.sun.validation.data.Parameters;
import org.netbeans.modules.j2ee.sun.validation.util.BundleReader;
import org.netbeans.modules.j2ee.sun.validation.util.ObjectFactory;
import org.netbeans.modules.j2ee.sun.validation.util.Utils;


/**
 * {@link ValidationManager} is an object that provides Validation functionality,
 * through its <code>validate( )</code> function.
 * <code>{@link ValidationManagerFactory}</code> creates 
 * <code>{@link ValidationManager}</code> based on the Validation File.
 * Validation File specifies Validation rules i.e.
 * which Constraints to apply to which elements. Objects are validated by 
 * applying specified Constraints to its elements. Validation file is xml file
 * based on  <code>validation.dtd</code>. If no Validation File is specified,
 * <code>ValidationManagerFactory</code> returns default 
 * <code>ValidationManager</code>. Default <code>ValidationManager</code> is 
 * based on default Validation File. Default Validation File defines Constraints
 * for 8.0 SJSAS DTDs.
 * <p>
 * Validations are performed, recursively on the given Object.
 * Two types of Validations are perfomed, Structural validations and Specified
 * validations. Structural validations are expressed through 
 * <code>{@link CardinalConstraint}</code>. <code>CardinalConstraint</code> is 
 * an implicit Constraint. Its always applied to each of the element; you dont
 * need to specify it explicitly. Whereas , other Constaints need to be
 * explicitly specified for each element you wanted to apply it to.
 * Constraints to be applied are specified through Validation File.
 * You can also define, your own custom <code>Constraint</code>s and apply them 
 * to any elements. When you define your own <code>Constraint</code>s, you 
 * need to provide information about them to the framework. This information is
 * provided through Constraints File. Constraints File is xml file, based on
 * <code>constraints.dtd</code>. Constraints File to use is specified through
 * system property <code>constraints.file</code>. You can override 
 * <code>Constraint</code>s provided by framework , by providing your own.
 *
 * @see Constraint
 * @see CardinalConstraint
 * @see Validatee
 * @see ValidationManagerFactory
 *
 * @author  Rajeshwar Patil
 * @version %I%, %G%
 */
public class ValidationManager {
    /*
    ValidationManager is an object that constructs Validator objects for all
    the Validatees. ValidationManager maintains a map of xpaths to Validators.
    It constructs this by reading Validation File. Validation File specifies
    Constraints to be applied to the elements.
    Every Validatee has a corresponding Validator object and this object
    knows how to validate its Validatee. Validator maintains a list of
    Constraints that needs to be applied for each of the element of its
    Validatee. Constraint objects are built using files, Constraints File 
    and Validation File.
    */

    /**
     * A prefix used to construct the getter method name of the bean.
     */
    private static String GET_PREFIX = "get";                           //NOI18N


    /**
     * A prefix used to construct the setter method name of the bean.
     */
    private static String SET_PREFIX = "set";                           //NOI18N


    /**
     * A prefix used to construct the size method name of the bean.
     */
    private static String SIZE_PREFIX = "size";                         //NOI18N
    

    /**
     * A file that is used specify infromation for the constraints provided
     * by this framework. <code>Constraint</code> objects are built using the
     * information in this file.
     */
    private static final String defaultConstraintsFile = 
        "org/netbeans/modules/j2ee/sun/validation/constraints/" +       //NOI18N
            "constraints.xml";                                          //NOI18N

    
    /**
     * A Cardinal Constraint class name.
     */
    private static final String CARDINAL_CONSTRAINT_CLASS =
        "org.netbeans.modules.j2ee.sun.validation.constraints." +       //NOI18N
            "CardinalConstraint";                                       //NOI18N


    /**
     * A file that is used to specify <code>Constraints<code> to be 
     * applied to various elements. xpaths are used to specify elements.
     * While constructing this object, clients of this framework can specify
     * their own file by providing the file name to the appropriate
     * constructor. If client do not, then the default file( specified by 
     * <code>defaultValidationFile</code> ) is  used.
     */
    private String validationFile = null;

    
    /**
     * A default Validation file that specify <code>Constraints<code> to be
     * applied to various elements. xpaths are used to specify elements.
     * This file is used by framework if client do not specify one.
     * This file contains default validation rules.
     */
    private String defaultValidationFile = 
           "org/netbeans/modules/j2ee/sun/validation/validation.xml";   //NOI18N
    
    
    /**
     * A map that stores for each <code>Validatee</code> object, the xpath and
     * the corresponding <code>Validator</code> object.
     */
    private HashMap xpathToValidator = null;


    /**
     * A root bean representing validation.xml
     */
    private Validation validation = null;


    /**
     * A root bean representing constaints.xml
     */
    private Constraints constraints = null;

    
    /**
     * An object providing utilities
     */
    private Utils utils = null;

    
    /** Creates a new instance of <code>ValidationManager</code> */
    public ValidationManager() {
        utils = new Utils();
    }


    /** Creates a new instance of <code>ValidationManager</code> */
    public ValidationManager(String validationFile) {
        if(null != validationFile){
            this.validationFile = validationFile;
        }
        
        utils = new Utils();
    }

    
    /**
     * Validates the given <code>Object</code>.
     * Validatee Implementation for the given <code>object</code> must be
     * provided. Validatee Implementation of an object is a 
     * <code>Validatee</code> wrapper around it. Validatee Implementation
     * of an object is specified to framework through an Implementation File.
     * Implementation File is a <code>.properties</code> file, with name-value
     * pair entries in it. An entry in Implementation File specifies the object
     * name and the corresponding Validatee Implementation.
     * Implementation File to use, is specified to framework through system
     * property <code>impl.file</code>
     *
     * @param object the object to be validated
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty
     * if there are no failures.
     */
    public Collection validate(Object object){
        //This method get called in case of Objects that are not Validatee.
        //This method, essentially, gets the Object's Validatee Implementation 
        //and uses it instead.

        Collection collection = new ArrayList();

        if(object != null){
            boolean validateeImplFound = false;
            String validateeImpl = null;

            String implFile = 
                System.getProperty("impl.file");                            //NOI18N
            ///System.out.println(implFile);

            //User specified impl file overrides the default imple file
            //default impl file -- Constants.IMPL_FILE
            if(implFile != null){
                validateeImpl = getValidateeImplementation(object, implFile);
            }

            //Using default impl file; user specified not available
            if(validateeImpl == null){
                validateeImpl = 
                    getValidateeImplementation(object, Constants.IMPL_FILE);
            }

            //switch the BundleReader back to read from bundle file
            BundleReader.setBundle(Constants.BUNDLE_FILE);

            if(validateeImpl != null){
                Validatee validatee =
                    (Validatee)ObjectFactory.newInstance(validateeImpl, object);
                if(validatee != null){
                    collection = validate(validatee);
                }
            } else {
                Class classObject = utils.getClass(object);
                String className = classObject.getName();

                String format = BundleReader.getValue(
                    "MSG_given_object_is_not_validatee");               //NOI18N
                Object[] arguments = new Object[]{className};
                System.out.println(MessageFormat.format(format, arguments));
            }            

        }
        return collection;
   }   


    /**
     * Validates the given <code>Validatee</code>.
     * 
     * @param validatee the object to be validated
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty
     * if there are no failures.
     */
    public Collection validate(Validatee validatee){
        //This method applies Cardinal constraint and Custom constraints defined
        //for each of the elements of the validatee. It recurses for elements 
        //that are objects.
        ArrayList failures = new ArrayList();
        if(validatee != null){
            String xpath = validatee.getXPath();

            ArrayList elementNames = validatee.getElementNames();
            ArrayList elementDtdNames = validatee.getElementDtdNames();
            int noOfElements = elementNames.size();
            String elementName = null;
            String elementDtdName = null;
            int count = 0;

            for(int i=0; i<noOfElements; i++){
                elementName = (String)elementNames.get(i);
                elementDtdName = (String)elementDtdNames.get(i);

                //apply Cardinal Constraint
                failures.addAll(validateCardinalConstraint(validatee, elementName, 
                        elementDtdName));

                //apply Other Constraints
                boolean isBean = validatee.isBeanElement(elementName);
                if(isBean){
                    //Recurse if an Object
                    failures.addAll(recurse(elementName, validatee));
                } else {
                    Validator validator = getValidator(xpath);
                    if(null != validator){
                        failures.addAll(validator.validate(elementName,
                                elementDtdName, validatee));
                    } else {
                        ///String format = BundleReader.getValue(
                        ///    "MSG_No_definition_for");                //NOI18N
                        ///Object[] arguments = 
                        ///    new Object[]{"Validator", xpath};        //NOI18N
                        ///System.out.println(
                        ///    MessageFormat.format(format, arguments));
                    }
                }
           }
       }
       return failures;
    }


    /**
     * Validates the given Element.
     * 
     * @param property the Element to be validated
     * @param absoluteDtdName the complete dtd name of <code>property</code>
     * @param fieldName the name of the GUI field, associated 
     * with <code>property</code>
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty
     * if there are no failures.
     */
    public Collection validateIndividualProperty(String property,
            String absoluteDtdName, String fieldName){
        ArrayList failures = new ArrayList();

        String xpath = utils.getParentName(absoluteDtdName,
                Constants.XPATH_DELIMITER_CHAR);
        Validator validator = getValidator(xpath);
        if(null != validator){
            failures.addAll(validator.validateIndividualProperty(
                property, absoluteDtdName, fieldName));
        } else {
            ///String format = BundleReader.getValue(
            ///    "MSG_No_definition_for");                            //NOI18N
            ///Object[] arguments = 
            ///    new Object[]{"Validator", xpath};                    //NOI18N
            ///System.out.println(
            ///    MessageFormat.format(format, arguments));
        }

        return failures;
    }


    /**
     * Recurses by calling the appropriate methods.
     * This method is called when the given element to be validated is
     * an object itself(<code>Validatee</code>).
     * 
     * @param elementName the given element to be validated
     * @param validatee the given <code>Validatee</code> object; the
     * element of which needs to be validated
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty
     * if there are no failures.
     */
    private Collection recurse(String elementName, Validatee validatee){
        ArrayList failures = new ArrayList();
        boolean isIndexed = validatee.isIndexed(elementName);

        if(isIndexed){
            failures.addAll(validateBeans(elementName, validatee));
        } else {
            failures.addAll(validateBean(elementName, validatee));
        }
        return failures;
    }


    /**
     * Validates the given element of given <code>Validatee</code>
     * for Cardinality.This method is called for each and every 
     * element of the Validatee. You does not need to define Cardinal
     * cosntraints in Validation File. Cardinal constraint is implicity
     * applied to each and every element.
     * 
     * @param elementName the given element to be validated
     * @param elementDtdName the dtd name of the given element
     * @param validatee the given <code>Validatee</code> object; the
     * element which needs to be validated
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty
     * if there are no failures.
     */
    private Collection validateCardinalConstraint(Validatee validatee,
            String elementName, String elementDtdName){
        ArrayList failures = new ArrayList();
        int cardinal = validatee.getElementCardinal(elementName);
        CardinalConstraint constraint = getCardinalConstraint(cardinal);

        switch(cardinal){
            case Constants.MANDATORY_ARRAY :
            case Constants.OPTIONAL_ARRAY: {
                Object[] elements = 
                    (Object [])validatee.getElements(elementName);
                String name = validatee.getIndexedXPath() +
                    Constants.XPATH_DELIMITER + elementDtdName;
                ///String name = validatee.getXPath() +
                    ///Constants.XPATH_DELIMITER + elementDtdName;
                failures.addAll(constraint.match(elements, name));
                break;
            }
            case Constants.OPTIONAL_ELEMENT : {
                Object element = validatee.getElement(elementName);
                break;
            }
            case Constants.MANDATORY_ELEMENT :
            default : {
                Object element = validatee.getElement(elementName);
                String name = validatee.getIndexedXPath() +
                    Constants.XPATH_DELIMITER + elementDtdName;
                ///String name = validatee.getXPath() +
                    ///Constants.XPATH_DELIMITER + elementDtdName;
                failures.addAll(constraint.match(element, name));
            }
            break;
        }
        return failures;
    }


    /**
     * Returns the <code>Validator</code> for the given xpath.
     * Returns <code>null</code> if there is no <code>Validator</code>
     * object for the given xpath.
     * <code>ValidationManager</code> constructs the <code>Validator</code>
     * objects for xpaths using Validation File and Constraints File.
     * 
     * @param xpath the given xpath.
     *
     * @return <code>Validator</code> the Corresponding validator object,if any.
     */
    private Validator getValidator(String xpath){
        Validator validator = null;
        if(null == xpathToValidator){
            constructXpathToValidator();
        }
        if(null != xpathToValidator) {
            validator = (Validator)xpathToValidator.get(xpath);
        }
        return validator;
    }


    /**
     * Constructs the xpath to <code>Validator</code> map.
     * Uses Validation File and Constraints File to build the map.
     */
    private void constructXpathToValidator(){
        //read validation file and construct xPathToValidator Map

        if(null == validation){
            constructValidation();
        }

        if(null == validation){
            String format = 
                BundleReader.getValue("Warning_Not_available");         //NOI18N
            Object[] arguments = new Object[]{"Validation Data"};       //NOI18N
            System.out.println(MessageFormat.format(format, arguments));
            return;
        }


        xpathToValidator = new HashMap();
        int noOfElements = validation.sizeElement();
        Element element = null;
        Validator validator = null;
	String elementName = null;
	String beanName = null;	 
        Check check = null;
        String checkName = null;

        for(int i=0; i<noOfElements; i++) {
            element = validation.getElement(i);
            elementName = utils.getName(element.getName(),
                Constants.XPATH_DELIMITER_CHAR);
            beanName = utils.getParentName(element.getName(),
                Constants.XPATH_DELIMITER_CHAR);
            validator = (Validator) xpathToValidator.get(beanName);
            ///String format = 
            ///    BundleReader.getValue("Name_Value_Pair_Format");     //NOI18N
            ///Object[] arguments;
            if(null == validator) {
                validator = new Validator();
                xpathToValidator.put(beanName,  validator);
                ///format =
                ///    BundleReader.getValue("Name_Value_Pair_Format"); //NOI18N
                ///arguments = 
                ///    new Object[]{"Bean Name", beanName};             //NOI18N
                ///System.out.println(MessageFormat.format(format, arguments));
                ///arguments = new Object[]{"Validator", validator};    //NOI18N
                ///System.out.println(MessageFormat.format(format, arguments));
            }

            int noOfChecks = element.sizeCheck();
            Constraint constraint = null;
            for(int j=0; j<noOfChecks; j++){
                check = element.getCheck(j);
                constraint = getConstraint(check);
                if(null != constraint){
                    validator.addElementConstraint(elementName, constraint);
                    ///arguments = 
                    ///    new Object[]{"Element Name", elementName};      //NOI18N
                    ///System.out.println(MessageFormat.format(format, arguments));         
                    ///arguments = new Object[]{"Check", check.getName()}; //NOI18N
                    ///System.out.println(MessageFormat.format(format, arguments));
                    ///arguments = new Object[]{"Constraint", constraint}; //NOI18N
                    ///System.out.println(MessageFormat.format(format, arguments));
                    ///constraint.print();
                }
            }
        }
    }


    /**
     * Constructs <code>Validation</code> object, representing the root of
     * xml defining <code>Constraints</code> to be applied to different
     * elements.
     * Clients of this framework can specify their own file.
     * If client do not, then the default file( specified by 
     * <code>defaultValidationFile</code> ) is  used.
     */
    private void constructValidation() { 
        //Create an InpurtStream object

        URL url = null;
        InputStream inputStream = null;

        if(validationFile != null){
            inputStream = getInputStream(validationFile);
            if(inputStream == null){
                String format = 
                    BundleReader.getValue("MSG_using_the_default_file");//NOI18N
                Object[] arguments = new Object[]{defaultValidationFile};
                System.out.println(MessageFormat.format(format, arguments));

                inputStream = getDafaultStream();
            }
        } else {
            inputStream = getDafaultStream();
        }
        
        if(inputStream != null){
            //Create graph
            try {
                validation = Validation.createGraph(inputStream);
            } catch(Exception e) {
                System.out.println(e.getMessage());
                validation = null;
            }
        }
    }


    /**
     * Constructs <code>Constraint</code> object, for the given 
     * <code>check</code>. <code>check</code> object represents the 
     * information of the constraint declared in Constraints File.
     * <code>Constraints</code> to be applied to different elements are
     * declared in Validation File. This method uses Constraints File to
     * construct Constraint object from the given <code>check</code> object.
     *
     * @param check the given <code>Check</code> object
     *
     * @return <code>Constraint</code> the corresponding <code>Constraint</code>
     * object. Returns <code>null</code> in case no information for this 
     * constaint is specified in Constraints File or if Constraints File is not
     * found/defined.
     */
    private Constraint getConstraint(Check check){
        Constraint constraint = null;
        if(null == constraints) {
            constructConstraints();
        }
        CheckInfo checkInfo = null;
        if(null != constraints){
            String checkName = check.getName();
            checkInfo = getCheckInfo(checkName, constraints);
            if(null != checkInfo){
                constraint = buildConstraint(check, checkInfo);
            } else {
                String format = 
                    BundleReader.getValue("MSG_No_definition_for");     //NOI18N
                Object[] arguments = 
                    new Object[]{"CheckInfo", checkName};               //NOI18N
                System.out.println(MessageFormat.format(format, arguments));
            }
        } else { 
            String format = 
                BundleReader.getValue("MSG_Not_defined");               //NOI18N
            Object[] arguments = new Object[]{"Constraints"};           //NOI18N
            System.out.println(MessageFormat.format(format, arguments));
        }
        return constraint;
    }


    /**
     * Constructs <code>Constraints</code> object, representing the root of
     * Constraints File. Constraints File provide information about the 
     * <code>Constraint</code>s i.e. Constraint class name, Constraint 
     * constructor arguments & thier types.
     */
    private void constructConstraints() { 
        Constraints customConstraints = null;
        Constraints defaultConstraints = null;
        
        String constraintsFile =                     
            System.getProperty("constraints.file");                     //NOI18N
        ///System.out.println(consFile);
        if(constraintsFile != null){
            customConstraints = getConstraints(constraintsFile);
        }
        
        if(defaultConstraintsFile != null){
            defaultConstraints = getConstraints(defaultConstraintsFile);
        }
        
        if(customConstraints != null){
            if(defaultConstraints != null){
                int count = defaultConstraints.sizeCheckInfo();
                CheckInfo checkInfo = null;
                CheckInfo checkInfoClone = null;
                for(int i=0; i<count; i++){
                    checkInfo = defaultConstraints.getCheckInfo(i);
                    ///String str = checkInfo.dumpBeanNode();
                    checkInfoClone = (CheckInfo) checkInfo.clone();
                    ///System.out.println(checkInfoClone.dumpBeanNode());
                    customConstraints.addCheckInfo(checkInfoClone);
                }
                constraints =  customConstraints;
            } else {
                constraints =  customConstraints;                
            }
        } else {
            constraints =  defaultConstraints;
        }
    }


    /**
     * Returns the <code>CheckInfo</code> object for the given 
     * <code>Check</code> object. <code>Check</code> object represents
     * the <code>Constraint</code> object. <code>CheckInfo</code> hold
     * information for a particular <code>Constraint</code> , for example,
     * Constraint class name, Constraint constructor arguments & thier types.
     *
     * @param checkName the given <code>Check</code> name
     *
     * @return <code>CheckInfo</code> the <code>CheckInfo</code>
     * object for the given <code>Check</code>
     */
    private CheckInfo getCheckInfo(String checkName, 
                Constraints constraints){
        CheckInfo checkInfo = null;
        int size = constraints.sizeCheckInfo();
        for(int i=0; i<size; i++) {
            checkInfo = constraints.getCheckInfo(i);
            if(checkName.equals(checkInfo.getName())) {
                return checkInfo;
            }
        }
        return null;
    }


    /**
     * Constructs <code>Cosntraint</code> object, from the given
     * <code>Check</code> and <code>CheckInfo</code> objects.
     * <code>Check</code> object identifies a particular 
     * <code>Constraint</code>, whereas <code>CheckInfo</code> object
     * provides additional information about the <code>Constraint</code>,
     * such as Constraint class name, Constraint constructor arguments &
     * thier types.
     * For a given element <code>Check</code> object is constructed from
     * the information specified in Validation File whereas the
     * corresponding <code>CheckInfo</code> object is built from the 
     * infromation provided in Constraints File.
     *
     * @param check the <code>Check</code> object identifying the 
     * <code>Constraint</code>
     * @param checkInfo the given <code>CheckInfo</code> object providing more
     * information about the <code>Constraint</code> identified by input 
     * parameter <code>check</code>
     *
     * @return <code>Constraint</code> the <code>Constraint</code>object 
     * constructed from the input paramerters <code>check</code> and 
     * <code>checkInfo</code>
     */
    private Constraint buildConstraint(Check check, CheckInfo checkInfo){
        //Constraint objects are always created using the default constructor.
        //Fields in Constraint object are then set using using the setter 
        //methods. In order to set the fields in the Constraint object, field
        //values are fetched from the check object and the no of arguments,
        //argument names and thier types are fetched from checkInfo object.
        //Assumptions
        //      Every Constraint must have default constructor.
        //      Every Constraint that has fields must provide setter functions
        //      to set those fields.
        //      <name> field of <check> in Validation File is matched with 
        //      <name> field of <check-info> in Constraints File to find out
        //      infromation of a particulatar Constraint.
        //      Number of <parameters> defined in <check> should match the
        //      number of <arguments> of corresponding <check-info>
        //
        Constraint constraint = null;
        String classname = checkInfo.getClassname();
        Arguments arguments = checkInfo.getArguments();
        Class[] argumentTypeClass = new Class[1];
        Object[] argumentValue = new Object[1];
        
        String argumentName;
        String argumentType;

        constraint = (Constraint)utils.createObject(classname);
        if(null != arguments){
            int size = arguments.sizeArgument();
            Parameters parameters = check.getParameters();

            if((null != parameters) && (size == parameters.sizeParameter())){
                Argument argument =  null;
                Parameter parameter = null;
                for(int i=0; i<size; i++) {
                    argument = arguments.getArgument(i);
                    argumentName = argument.getName();
                    argumentType = argument.getType();
                    parameter = 
                        getParameter(parameters, argumentName);

                    if(parameter == null){
                        String format = BundleReader.getValue(
                            "Warning_no_value_specified_for");          //NOI18N
                        Object[] substitutes = 
                            new Object[]{argumentName, check.getName()};
                        System.out.println(
                            MessageFormat.format(format, substitutes));
                        continue;
                    }

                    if(null == argumentType){
                        argumentType = "java.lang.String";              //NOI18N
                    }

                    int noOfValues = 1;
                    if(argumentType.equals("java.lang.String[]")){      //NOI18N
                        Integer sz = (Integer)utils.getElement("value", //NOI18N
                            parameter, SIZE_PREFIX);
                        noOfValues = sz;
                        argumentType = "java.lang.String";              //NOI18N
                    } 
                    for(int j=0; j<noOfValues; j++) {
                        argumentValue[0] = utils.getElement("value",    //NOI18N
                            j, parameter);
                        argumentTypeClass[0] =
                            utils.getClass(argumentType);
                        String methodName =
                            utils.methodNameFromDtdName(argumentName,
                                SET_PREFIX);
                        Method method = 
                            utils.getMethod(utils.getClass(constraint),
                                methodName, argumentTypeClass);
                        utils.invoke(constraint, method, argumentValue);
                    }
                }
            } else {
                String format = BundleReader.getValue(
                    "MSG_Conflicting_Constraint_information");          //NOI18N
                Object[] substitues = new Object[]{check.getName()};    //NOI18N
                String message = MessageFormat.format(format, substitues);
                assert false : message;
            }
        }
        return constraint;
    }


    /**
     * Constructs <code>CardinalCosntraint</code> object, for the given
     * cardinal. Cardinal Constraint is implicit i.e user do not need to
     * define Cardinal Constraint for elements. Its applied implicitly to
     * all the elements. Where in case of other Constraints user must 
     * specify which Constraints to apply to which elements. User specifies
     * this through Validation File.
     *
     * @param cardinal the given cardinal; it could be one of the following :
     * MANDATORY_ELEMENT, MANDATORY_ARRAY, OPTIONAL_ELEMENT or OPTIONAL_ARRAY
     * 
     * @return <code>CardinalConstraint</code> object representing the given
     * cardinal
     */
    private CardinalConstraint getCardinalConstraint(int cardinal){
        Class[] argumentTypes = new Class[] {int.class};
        Constructor constructor =
            utils.getConstructor(CARDINAL_CONSTRAINT_CLASS, argumentTypes);

        Integer parameter = cardinal;
        Object[] argumentValues = new Object[] {parameter};

        return (CardinalConstraint) utils.createObject(constructor,
            argumentValues);
    }


    /**
     * Validates the given element of the given <code>Validatee</code>.
     * This method is called for an element that is an array(optional array
     * or madatory array) of objects.
     * 
     * @param elementName the given element to be validated
     * @param validatee the given <code>Validatee</code> object; the
     * element of which needs to be validated
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty
     * if there are no failures.
     */
    private Collection validateBeans(String elementName,
            Validatee validatee){
        //This method does an recursive call for each of the objects in an
        //array of given element.
        int noOfElements = 0;
        String sizeMethodName =  utils.methodNameFromBeanName(elementName,
            SIZE_PREFIX);
        Method sizeMethod = validatee.getMethod(sizeMethodName);
        noOfElements = ((Integer)validatee.invoke(sizeMethod));

        ArrayList failures = new ArrayList();
        Object child = null;
        for(int i=0; i<noOfElements; i++) {
            child = validatee.getElement(elementName, i);
            if(child != null) {
                failures.addAll(validate(child));
            }
        }
        return failures;
    }


    /**
     * Validates the given element of the given <code>Validatee</code>.
     * This method is called for an element that is an object.
     * 
     * @param elementName the given element to be validated
     * @param validatee the given <code>Validatee</code> object; the
     * element of which needs to be validated
     *
     * @return <code>Collection</code> the Collection of
     * <code>ConstraintFailure</code> Objects. Collection is empty
     * if there are no failures.
     */
    private Collection validateBean(String elementName,
            Validatee validatee){
        //This method does an recursive call on the object of given element.
        ArrayList failures = new ArrayList();
        Object child = null;
        child = validatee.getElement(elementName);
        if(child != null) {
            failures.addAll(validate(child));
        }
        return failures;
    }


    /**
     * Constructs <code>InputStream</code> object, for default Validation File
     * defining <code>Constraints</code> to be applied to different
     * elements. The default Validation File is represented by the private
     * attribute <code>defaultValidationFile</code> 
     *
     * @return <code>InputStream</code> an InputStream object representing
     * default Validation File. Returns null, in case of failure.
     */
    private InputStream getDafaultStream(){
        InputStream inputStream = null;
        URL url = null;
        if(defaultValidationFile != null){
            url = utils.getUrlObject(defaultValidationFile);
            if(url != null) {
                try {
                    inputStream = url.openStream();
                } catch (IOException exception){
                    System.out.println(exception.getMessage());
                }
            } else {
                assert false : (BundleReader.getValue(
                    "Error_control_should_never_reach_here"));          //NOI18N

                String format = 
                    BundleReader.getValue("Error_can_not_access_file"); //NOI18N
                Object[] arguments = new Object[]{defaultValidationFile};
                String message = MessageFormat.format(format, arguments);
                assert false : message;
            }
        } else {
            assert false : (BundleReader.getValue(
                "Error_control_should_never_reach_here"));              //NOI18N
        }
        return inputStream;
    }


    /**
     * Gets the element from this object with the given name.
     *
     * @return <code>Parameter</code> an object with the given name.
     * Returns null if the object with the given name is not found.
     */
    private Parameter getParameter(Parameters parameters, String name){
        int size = parameters.sizeParameter();
        Parameter returnValue = null;
        Parameter parameter = null;
        String parameterName = null;
        for(int i=0; i<size; i++){
            parameter =  (Parameter)utils.getElement("parameter", i,    //NOI18N
                parameters);
            parameterName = parameter.getName();
            if(parameterName.equals(name)){
                returnValue = parameter;
                break;
            }
        }
        return returnValue;
    }


    /**
     * Constructs <code>InputStream</code> object, for the given file.
     *
     * @return <code>InputStream</code> an InputStream object representing
     * the given file. Returns null, in case of failure.
     */
    private InputStream getInputStream(String inputFile){
        //Create an InpurtStream object

        InputStream inputStream = null;

        if(inputFile.lastIndexOf(':') == -1){
            URL url = null;

            url = utils.getUrlObject(inputFile);
            if(url != null) {
                try {
                    inputStream = url.openStream();
                } catch (IOException exception){
                    System.out.println(exception.getMessage());
                }
            } else {
                String format = BundleReader.getValue(
                    "Error_specified_file_can_not_be_used");            //NOI18N
                Object[] arguments = new Object[]{inputFile};
                System.out.println(MessageFormat.format(format, arguments));
            }
        } else {
            File file = new File(inputFile);
            if(file.exists()){
                try {
                    inputStream = new FileInputStream(file);
                } catch(FileNotFoundException exception){
                    System.out.println(exception.getMessage());
                    inputStream = null;
                }
            } else {
                String format = BundleReader.getValue(
                    "Error_specified_file_can_not_be_used");            //NOI18N
                Object[] arguments = new Object[]{inputFile};
                System.out.println(MessageFormat.format(format, arguments));
            }
        }

        return inputStream;
    }


    /**
     * Gets the Validatee Implementation name for the given object from the 
     * given <code>properties</code> file.
     *
     * @return <code>String</code> name of the Validatee Implementation for the 
     * the object. Returns null, if no Validatee Implementation is found in the
     * given <code>properties</code> file.
     */
    private String getValidateeImplementation(Object object,
        String propertiesFile){
        String returnVal = null;
        Class classObject = utils.getClass(object);
        String className = classObject.getName();

        //switch the BundleReader to read from the given impl file
        //validatee implementations are specified through this impl file
        BundleReader.setBundle(propertiesFile);
        
        String validateeImplName = BundleReader.getValue(className);
        while(!(className.equals("java.lang.Object"))){                 //NOI18N
            if(!(validateeImplName.equals(className))){
                returnVal = validateeImplName;
                break;
            } else {
                classObject = classObject.getSuperclass();
                className = classObject.getName();
                validateeImplName = BundleReader.getValue(className);
            }
        }
        return returnVal;
    }


    /**
     * Constructs <code>Constraints</code> object, representing the root of
     * given Constraints File.
     * 
     * @return <code>Constraints</code> object representing the root of the
     * given Constraints File. Returns null, in case of failure.
     */
    private Constraints getConstraints(String constraintsFile){
        URL url = null;
        InputStream inputStream = null;
        Constraints retVal = null;

        if(constraintsFile != null){
            inputStream = getInputStream(constraintsFile);
        }

        //Create graph
        if(inputStream != null){
            try {
                retVal = Constraints.createGraph(inputStream);
            } catch(Exception e) {
                System.out.println(e.getMessage());
                retVal = null;
            }
        }
        return retVal;
    }
}
