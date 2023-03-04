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
package org.netbeans.modules.websvc.design.javamodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.jws.WebParam.Mode;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.SharedClassObject;

/**
 *
 * @author mkuchtiak
 */
public class ServiceModelTest extends NbTestCase {

    //private static ServiceModelTest DEFAULT_LOOKUP = null;
    private static final String NAME = "AddNumbers";
    private static final String SERVICE_NAME = "AddNumbersService";
    private static final String PORT_NAME = "AddNumbersPort";
    private static final int NUMBER_OF_METHODS = 3;
    private static final String[] OP_NAMES = {"add", "echo-operation", "send"};
    private static final String[] OP_RETURN_NAMES = {"sum", "return", null};
    private static final String[] OP_RETURN_TYPES = {"int", "java.lang.String", null};
    private static final boolean[] OP_ONE_WAY = {false, false, true};
    private static final String[][] PARAM_NAMES = {
        {"x", "y"},
        {},
        {"message"}};
    private static final String[][] PARAM_TYPES = {
        {"int", "int"},
        {},
        {"java.lang.String"}};
    private static final Object[][] PARAM_MODES = {
        {Mode.IN, Mode.IN},
        {},
        {Mode.IN}};
    private static final Object[][] FAULTS = {
        {},
        {"FooFault", "Exception"},
        {}
    };
    private static final Object[][] FAULT_TYPES = {
        {},
        {"add.foo.FooException", "java.lang.Exception"},
        {}
    };
    private static final String NAME_1 = "AddNumbers";
    private static final String SERVICE_NAME_1 = "AddNumbers";
    private static final String PORT_NAME_1 = "AddNumbersPort";
    private static final String TARGET_NAMESPACE_1 = "http://www.netbeans.org/tests/AddNumbersTest";
    private static final int NUMBER_OF_METHODS_1 = 4;
    private static final String[] OP_NAMES_1 = {"add", "echo-operation", "send", "hello"};
    private static final String[] OP_RETURN_TYPES_1 = {"int", "java.lang.String", null, "java.lang.String"};
    private static final boolean[] OP_ONE_WAY_1 = {false, false, true, false};
    private static final String[][] PARAM_NAMES_1 = {
        {"x", "y"},
        {},
        {"message", "arg1"},
        {"arg0"}};
    private static final String[][] PARAM_TYPES_1 = {
        {"int", "int"},
        {},
        {"java.lang.String", "java.lang.String"},
        {"add.foo.Foo"}};
    private static final Object[][] PARAM_MODES_1 = {
        {Mode.IN, Mode.IN},
        {},
        {Mode.IN, Mode.IN},
        {Mode.IN}};
    private static final String[][] EXPECTED_EVENTS = {
        {"propertyChanged", "serviceName", "AddNumbersService", "AddNumbers"},
        {"propertyChanged", "targetNamespace", "http://add/", "http://www.netbeans.org/tests/AddNumbersTest"},
        {"operationChanged", "send"},
        {"operationAdded", "hello"}};
    private List<String[]> events;
    private FileObject dataDir;

    public ServiceModelTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        // workaround for JavaSource class
        System.setProperty("netbeans.user", getWorkDir().getAbsolutePath());
        dataDir = FileUtil.toFileObject(getDataDir());

        ClassPathProvider cpp = new ClassPathProvider() {

            public ClassPath findClassPath(FileObject file, String type) {
                if (ClassPath.SOURCE.equals(type)) {
                    return ClassPathSupport.createClassPath(new FileObject[]{dataDir});
                }
                if (ClassPath.COMPILE.equals(type)) {
                    return ClassPathSupport.createClassPath(new FileObject[0]);
                }
                //if (type == ClassPath.BOOT)
                //    return createClassPath(System.getProperty("sun.boot.class.path"));
                return null;
            }
        };
        
        SourceUtilsTestUtil.compileRecursively(dataDir);
        SharedClassObject loader = JavaDataLoader.findObject(JavaDataLoader.class, true);
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[]{loader, cpp});

        events = new ArrayList<String[]>();
    }

    protected void tearDown() throws Exception {
    }

    /** Test service model for AddNumbers service
     */
    public void testServiceModel() throws IOException {
        FileObject sourceFileObject = dataDir.getFileObject("add/AddNumbers.java");
        assertNotNull(sourceFileObject);
        String res = copyFileToString(FileUtil.toFile(sourceFileObject));
        try {
            // compare model values
            ServiceModel model = ServiceModel.getServiceModel(sourceFileObject);
            assertEquals(NAME, model.getName());
            assertEquals(SERVICE_NAME, model.getServiceName());
            assertEquals(PORT_NAME, model.getPortName());
            assertEquals(NUMBER_OF_METHODS, model.getOperations().size());
            List<MethodModel> operations = model.getOperations();
            int i = 0;
            for (MethodModel op : operations) {
                assertEquals(OP_NAMES[i], op.getOperationName());
                ResultModel result = op.getResult();
                assertEquals(OP_RETURN_NAMES[i], result == null ? null : result.getName());
                assertEquals(OP_RETURN_TYPES[i], result == null ? null : result.getResultType());
                assertEquals(OP_ONE_WAY[i], op.isOneWay());
                List<ParamModel> params = op.getParams();
                int j = 0;
                for (ParamModel param : params) {
                    assertEquals(PARAM_NAMES[i][j], param.getName());
                    assertEquals(PARAM_TYPES[i][j], param.getParamType());
                    assertEquals(PARAM_MODES[i][j], param.getMode());
                    j++;
                }
                List<FaultModel> faults = op.getFaults();
                j = 0;
                for (FaultModel fault : faults) {
                    assertEquals(FAULTS[i][j], fault.getName());
                    assertEquals(FAULT_TYPES[i][j], fault.getFaultType());
                    j++;
                }
                System.out.println("SOAP REQUEST :");
                System.out.println(getFormatedDocument(op.getSoapRequest()));

                System.out.println("");
                System.out.println("------------------");
                if (!op.isOneWay()) {
                    System.out.println("SOAP RESPONSE :");
                    System.out.println(getFormatedDocument(op.getSoapResponse()));
                    System.out.println("");
                    System.out.println("------------------");
                }

                i++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Test service model for implementation class with
     * @WebService(endpointInterface="...") specified
     */
    public void testSEI() throws IOException {
        FileObject sourceFileObject = dataDir.getFileObject("hello/Hello.java");
        assertNotNull(sourceFileObject);

        try {
            // compare model values
            ServiceModel model = ServiceModel.getServiceModel(sourceFileObject);
            assertEquals("Hello", model.getName());
            assertEquals("HelloService", model.getServiceName());
            assertEquals("HelloPort", model.getPortName());
            assertEquals("hello.HelloInterface", model.getEndpointInterface());
            assertEquals(1, model.getOperations().size());
            List<MethodModel> operations = model.getOperations();
            MethodModel op = operations.get(0);

            assertEquals("hello_operation", op.getOperationName());
            ResultModel result = op.getResult();
            assertEquals("echoString", result == null ? null : result.getName());
            assertEquals("java.lang.String", result == null ? null : result.getResultType());
            assertEquals(false, op.isOneWay());
            List<ParamModel> params = op.getParams();
            assertEquals(1, params.size());

            ParamModel param = params.get(0);

            assertEquals("name", param.getName());
            assertEquals("java.lang.String", param.getParamType());
            assertEquals(Mode.IN, param.getMode());

            System.out.println("SOAP REQUEST :");
            System.out.println(getFormatedDocument(op.getSoapRequest()));

            System.out.println("");
            System.out.println("------------------");
            if (!op.isOneWay()) {
                System.out.println("SOAP RESPONSE :");
                System.out.println(getFormatedDocument(op.getSoapResponse()));
                System.out.println("");
                System.out.println("------------------");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /** Model Merge test
     */
    public void testModelMerge() {
        // testing merge
        FileObject sourceFileObject = dataDir.getFileObject("add/AddNumbers.java");
        assertNotNull(sourceFileObject);
        try {
            ServiceModel model = ServiceModel.getServiceModel(sourceFileObject);
            FileObject sourceFileObject_1 = dataDir.getFileObject("add/AddNumbers_1.java");
            assertNotNull(sourceFileObject_1);
            ServiceModel model_1 = ServiceModel.getServiceModel(sourceFileObject_1);
            model.addServiceChangeListener(new ServiceChangeListener() {

                public void propertyChanged(String propertyName, String oldValue, String newValue) {
                    System.out.println("propertyChanged " + propertyName + ":" + oldValue + ":" + newValue);
                    events.add(new String[]{"propertyChanged", propertyName, oldValue, newValue});
                }

                public void operationAdded(MethodModel method) {
                    System.out.println("operationAdded " + method.getOperationName());
                    events.add(new String[]{"operationAdded", method.getOperationName()});
                }

                public void operationRemoved(MethodModel method) {
                    System.out.println("operationRemoved " + method.getOperationName());
                    events.add(new String[]{"operationRemoved", method.getOperationName()});
                }

                public void operationChanged(MethodModel oldMethod, MethodModel newMethod) {
                    System.out.println("operationChanged " + newMethod.getOperationName());
                    events.add(new String[]{"operationChanged", newMethod.getOperationName()});
                }
            });
            model.mergeModel(model_1);
            int i = 0;
            for (String[] event : events) {
                int j = 0;
                for (String eventPart : event) {
                    assertEquals(EXPECTED_EVENTS[i][j], eventPart);
                    j++;
                }
                i++;
            }

            // compare again model values
            assertEquals(NAME_1, model.getName());
            assertEquals(SERVICE_NAME_1, model.getServiceName());
            assertEquals(PORT_NAME_1, model.getPortName());
            assertEquals(NUMBER_OF_METHODS_1, model.getOperations().size());
            List<MethodModel> operations = model.getOperations();
            i = 0;
            for (MethodModel op : operations) {
                assertEquals(OP_NAMES_1[i], op.getOperationName());
                ResultModel result = op.getResult();
                assertEquals(OP_RETURN_TYPES_1[i], result == null ? null : result.getResultType());
                assertEquals(OP_ONE_WAY_1[i], op.isOneWay());
                List<ParamModel> params = op.getParams();
                int j = 0;
                for (ParamModel param : params) {
                    assertEquals(PARAM_NAMES_1[i][j], param.getName());
                    assertEquals(PARAM_TYPES_1[i][j], param.getParamType());
                    assertEquals(PARAM_MODES_1[i][j], param.getMode());
                    j++;
                }
                i++;
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void testServiceModel2() {
        FileObject sourceFileObject = dataDir.getFileObject("add/AddNumbers.java");

        try {

            String orgFile = copyFileToString(FileUtil.toFile(sourceFileObject));
            // compare model values
            ServiceModel model = ServiceModel.getServiceModel(sourceFileObject);

            model.setServiceName("addService");
            model.setName("add");
            model.setPortName("addPort");

            String res1 = copyFileToString(FileUtil.toFile(sourceFileObject));
//            System.out.println("Changed AddNumbers.java:");
//            System.out.println(".....................................");
//            System.out.println(res1);
//            System.out.println(".....................................");
            model.setServiceName(null);
            model.setPortName(null);
            model.setName(null);

            String res2 = copyFileToString(FileUtil.toFile(sourceFileObject));
//            System.out.println("Original AddNumbers.java:");
//            System.out.println(".....................................");
//            System.out.println("res = "+res2);
//            System.out.println(".....................................");

            assertEquals(orgFile, res2);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AssertionError(ex);
        }
    }

    public void testServiceModelSetter() {
        FileObject sourceFileObject = dataDir.getFileObject("generator/ServiceSetterTestService.java");

        try {

            String org = copyFileToString(FileUtil.toFile(sourceFileObject));

            System.out.println("Original ServiceSetterTestService.java:");
            System.out.println(".....................................");
            System.out.println(org);
            System.out.println(".....................................");

            ServiceModel model = ServiceModel.getServiceModel(sourceFileObject);

            model.setServiceName("EmptyService");

            String res = copyFileToString(FileUtil.toFile(sourceFileObject));
            System.out.println("Changed ServiceSetterTestService.java:");
            System.out.println(".....................................");
            System.out.println(res);
            System.out.println(".....................................");

            model.setServiceName(null);

            String res1 = copyFileToString(FileUtil.toFile(sourceFileObject));

            assertEquals(org, res1);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AssertionError(ex);
        }
    }

    public void testMethodModelSetter() {
        FileObject sourceFileObject = dataDir.getFileObject("generator/MethodSetterTestService.java");

        try {

            ServiceModel model = ServiceModel.getServiceModel(sourceFileObject);

            String org = copyFileToString(FileUtil.toFile(sourceFileObject));
            System.out.println("Original MethodSetterTestService.java:");
            System.out.println(".....................................");
            System.out.println(org);
            System.out.println(".....................................");

            model.getOperations().get(0).setOperationName("hello");

            String res = copyFileToString(FileUtil.toFile(sourceFileObject));
            System.out.println("Changed MethodSetterTestService.java:");
            System.out.println(".....................................");
            System.out.println(res);
            System.out.println(".....................................");



            model.getOperations().get(0).setOperationName(null);

            String res1 = copyFileToString(FileUtil.toFile(sourceFileObject));
            assertEquals(org, res1);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AssertionError(ex);
        }
    }

    public void testParamModelSetter() {
        FileObject sourceFileObject = dataDir.getFileObject("generator/ParamSetterTestService.java");

        try {
            ServiceModel model = ServiceModel.getServiceModel(sourceFileObject);

            String org = copyFileToString(FileUtil.toFile(sourceFileObject));
            System.out.println("Original ParamSetterTestService.java:");
            System.out.println(".....................................");
            System.out.println(org);
            System.out.println(".....................................");

            model.getOperations().get(0).getParams().get(0).setName("name");

            String res = copyFileToString(FileUtil.toFile(sourceFileObject));
            System.out.println("Changed ParamSetterTestService.java:");
            System.out.println(".....................................");
            System.out.println(res);
            System.out.println(".....................................");

            model.getOperations().get(0).getParams().get(0).setName(null);

            String res1 = copyFileToString(FileUtil.toFile(sourceFileObject));
            assertEquals(org, res1);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AssertionError(ex);
        }
    }

    public void testParamModelSetter_1() {
        FileObject sourceFileObject = dataDir.getFileObject("generator/ParamSetterTestService_1.java");

        try {
            ServiceModel model = ServiceModel.getServiceModel(sourceFileObject);

            String org = copyFileToString(FileUtil.toFile(sourceFileObject));
            System.out.println("Original ParamSetterTestService_1.java:");
            System.out.println(".....................................");
            System.out.println(org);
            System.out.println(".....................................");

            model.getOperations().get(0).getParams().get(0).setName("name");

            String res = copyFileToString(FileUtil.toFile(sourceFileObject));
            System.out.println("Changed ParamSetterTestService.java_1:");
            System.out.println(".....................................");
            System.out.println(res);
            System.out.println(".....................................");

            model.getOperations().get(0).getParams().get(0).setName(null);

            String res1 = copyFileToString(FileUtil.toFile(sourceFileObject));
            System.out.println(res1);
        //assertEquals(org, res1);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new AssertionError(ex);
        }
    }

    /**
     * Tests removal of the last operation from the model
     *
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=122228
     */
    public void testRemoveLastOperation() throws Exception {
        System.out.println("=======================");
        System.out.println("testRemoveLastOperation");
        FileObject sourceFileObject = dataDir.getFileObject("remove/MethodRemoveTestService.java");
        assertNotNull(sourceFileObject);
        ServiceModel model = ServiceModel.getServiceModel(sourceFileObject);
        FileObject sourceFileObject_1 = dataDir.getFileObject("remove/MethodRemoveTestService_1.java");
        assertNotNull(sourceFileObject_1);
        ServiceModel model_1 = ServiceModel.getServiceModel(sourceFileObject_1);
        model.addServiceChangeListener(new ServiceChangeListener() {

            public void propertyChanged(String propertyName, String oldValue, String newValue) {
                System.out.println("propertyChanged " + propertyName + ":" + oldValue + ":" + newValue);
                events.add(new String[]{"propertyChanged", propertyName, oldValue, newValue});
            }

            public void operationAdded(MethodModel method) {
                System.out.println("operationAdded " + method.getOperationName());
                events.add(new String[]{"operationAdded", method.getOperationName()});
            }

            public void operationRemoved(MethodModel method) {
                System.out.println("operationRemoved " + method.getOperationName());
                events.add(new String[]{"operationRemoved", method.getOperationName()});
            }

            public void operationChanged(MethodModel oldMethod, MethodModel newMethod) {
                System.out.println("operationChanged " + newMethod.getOperationName());
                events.add(new String[]{"operationChanged", newMethod.getOperationName()});
            }
        });
        model.mergeModel(model_1);
        List<MethodModel> operations = model.getOperations();
        assertEquals(0, operations.size());
    }

    /**
     * Returns a string which contains the contents of a file.
     *
     * @param f the file to be read
     * @return the contents of the file(s).
     */
    public static final String copyFileToString(java.io.File f) throws IOException {
        int s = (int) f.length();
        byte[] data = new byte[s];
        int len = new FileInputStream(f).read(data);
        if (len != s) {
            throw new IOException("truncated file");
        }
        return new String(data);
    }

    private String getFormatedDocument(SOAPMessage message) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 4);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StreamResult result = new StreamResult(new StringWriter());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            message.writeTo(bos);
            String output = bos.toString();
            InputStream bis = new ByteArrayInputStream(output.getBytes());
            StreamSource source = new StreamSource(bis);

            transformer.transform(source, result);

            return result.getWriter().toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
