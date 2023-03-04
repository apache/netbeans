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

package org.netbeans.modules.j2ee.ddloaders.web.test;

import java.io.File;
import java.io.IOException;

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.AssertionFailedErrorException;

import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.netbeans.modules.j2ee.ddloaders.web.test.util.Helper;
import org.netbeans.modules.j2ee.ddloaders.web.test.util.StepIterator;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.DDBeanTableModel;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;

import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import org.openide.util.Exceptions;

/**
 *
 * @author Milan Kuchtiak
 */
public class DDEditorTest extends NbTestCase {

    private static final String CAR_VOLVO = "Volvo";
    private static final String CAR_AUDI = "Audi";

    private DDDataObject dObj;
    private static final String CONTEXT_PARAM_CYLINDERS = "\n  <context-param>\n    <param-name>cylinders</param-name>\n    <param-value>6</param-value>\n  </context-param>";

    public DDEditorTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        return new NbTestSuite(DDEditorTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    public void testReplaceParamValueFromDDAPI() throws IOException {
        initDataObject();
        FileObject fo = dObj.getPrimaryFile();
        WebApp webApp = DDProvider.getDefault().getDDRoot(fo);
        webApp.getContextParam()[0].setParamValue(CAR_VOLVO);
        webApp.write(fo);
        compareGoldenFile("ReplaceParamValue.pass");
        openInDesignView(dObj);
        Helper.waitForDispatchThread();
        assertEquals("Context Params Table wasn't changed: ", CAR_VOLVO, (String) getDDBeanModel().getValueAt(0, 1));
    }

    public void testAddParamValueInDesignView() throws IOException {
        initDataObject();
        openInDesignView(dObj);
        Helper.waitForDispatchThread();
        final DDBeanTableModel model = getDDBeanModel();
        final int n = model.getRowCount() + 1;
        model.addRow(new Object[]{"color","Blue",""});
        dObj.modelUpdatedFromUI();
        new StepIterator() {
            int sizeContextParam;

            public boolean step() throws Exception {
                sizeContextParam = dObj.getWebApp().sizeContextParam();
                return sizeContextParam == n;
            }

            @Override
            public void finalCheck() {
                assertEquals("Context Param wasn't added to the model", n, sizeContextParam);
            }
        };

        // test the model

        openInXmlView(dObj);

        XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport) dObj.getCookie(EditorCookie.class);
        final Document document = editor.getDocument();
        new StepIterator() {
            public boolean step() throws Exception {
                return document.getText(0, document.getLength()).indexOf("<param-value>Blue</param-value>") >= 0;
            }

            @Override
            public void finalCheck() {
                final Exception error = getError();
                if (error != null) {
                    throw new AssertionFailedErrorException("Failed to read the document: ", error);
                }
                assertEquals("Cannot find new context param element in XML view (editor document)", true, isSuccess());
            }
        };

        new StepIterator() {
            private SaveCookie saveCookie;
            private int steps;

            public boolean step() throws Exception {
                steps++;
                saveCookie = (SaveCookie) dObj.getCookie(SaveCookie.class);
                return saveCookie != null;
            }

            @Override
            public void finalCheck() {
                // for debugging random failures
                if (saveCookie == null){
                    log("Data object was not modified. Steps taken: " + steps + ", DataCache contents: " + dObj.getDataCache().getStringData());
                }
                // check if save cookie was created
                assertNotNull("Data Object Not Modified", saveCookie);
            }
        }.saveCookie.save();
    }

    public void testAddParamValueInXmlView() throws IOException {
        initDataObject();
        openInXmlView(dObj);
        Helper.waitForDispatchThread();
        XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport)dObj.getCookie(EditorCookie.class);
        final Document document = editor.getDocument();
        Helper.waitForDispatchThread();

        // wait to see the changes in XML view
        new StepIterator() {
            private int index;

            public boolean step() throws Exception {
                //test the editor document
                String text = document.getText(0,document.getLength());
                index = text.lastIndexOf("</context-param>");
                return index >= 0;
            }

            @Override
            public void finalCheck() {
                assertEquals("Cannot find new context param element in XML view (editor document)", true, index > 0);
                try {
                    document.insertString(index + 16, CONTEXT_PARAM_CYLINDERS, null);
                } catch (BadLocationException ex) {
                    throw new AssertionFailedErrorException("Failed to read the document: ", ex);
                }
            }
        };

        openInDesignView(dObj);
        Helper.waitForDispatchThread();

        new StepIterator() {
            private String paramValue;

            public boolean step() throws Exception {
                // get context params table model
                DDBeanTableModel model = getDDBeanModel();
                if (model.getRowCount() == 2) {
                    paramValue = (String) model.getValueAt(1, 0);
                    return "cylinders".equals(paramValue);
                } else {
                    return false;
                }
            }

            @Override
            public void finalCheck() {
                assertEquals("Context Params Table wasn't changed: ", "cylinders", paramValue);
            }
        };

        // check if save cookie was created
        SaveCookie cookie = (SaveCookie) dObj.getCookie(SaveCookie.class);
        assertNotNull("Data Object Not Modified",cookie);
        cookie.save();
    }

    public void testReplaceParamValueFromDDAPI2() throws IOException {
        initDataObject();
        openInXmlView(dObj);
        final FileObject fo = dObj.getPrimaryFile();
        WebApp webApp = DDProvider.getDefault().getDDRoot(fo);
        webApp.getContextParam()[0].setParamValue(CAR_AUDI);
        webApp.write(fo);
        
        XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport) dObj.getCookie(EditorCookie.class);
        final Document document = editor.getDocument();

        new StepIterator() {
            public boolean step() throws Exception {
                return document.getText(0, document.getLength()).indexOf("<param-value>Audi</param-value>") >= 0;
            }
            
            @Override
            public void finalCheck() {
                final Exception error = getError();
                if (error != null) {
                    throw new AssertionFailedErrorException("Failed to read the document: ", error);
                }
                assertEquals("Cannot find new context param element in XML view (editor document)", true, isSuccess());
            }
        };
    }

    public void testCheckParamValueInDesignView2() throws IOException {
        initDataObject();
        openInDesignView(dObj);
        Helper.waitForDispatchThread();
        new StepIterator() {
            private String paramValue;

            public boolean step() throws Exception {
                paramValue = (String) getDDBeanModel().getValueAt(0, 1);
                return CAR_VOLVO.equals(paramValue);
            }
            
            @Override
            public void finalCheck() {
                assertEquals("Context Params Table wasn't changed: ", CAR_VOLVO, paramValue);
            }
        };
    }

    public void testFinalSave() throws IOException {
        initDataObject();
        SaveCookie cookie = (SaveCookie) dObj.getCookie(SaveCookie.class);
        if (cookie != null) {
            cookie.save();
        }
    }

    public DDBeanTableModel getDDBeanModel() {
        DDBeanTableModel ddBeanModel;
        try {
            ddBeanModel = Helper.getContextParamsTableModel(dObj);
        } catch (Exception ex) {
            throw new AssertionFailedErrorException("Failed to open Context Params section", ex);
        }
        assertNotNull("Table Model Not Found", ddBeanModel);
        return ddBeanModel;
    }

    private File getDDFile() {
        try {
            final FileObject originalDD = FileUtil.toFileObject(Helper.getDDFile(getDataDir()));
            FileObject result = FileUtil.copyFile(originalDD, FileUtil.toFileObject(getWorkDir()), originalDD.getName());
            return FileUtil.toFile(result);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    private void compareGoldenFile(String goldenFileName) throws IOException {
        assertFile(FileUtil.toFile(dObj.getPrimaryFile()), getGoldenFile(goldenFileName), getWorkDir());
    }

    private void initDataObject() throws DataObjectNotFoundException {
        if (dObj == null) {
            File f = getDDFile();
            FileObject fo = FileUtil.toFileObject(f);
            dObj = ((DDDataObject) DataObject.find(fo));
            assertNotNull("DD DataObject not found", dObj);
        }
    }

    private static void openInXmlView(DDDataObject dObj) {
        ((EditCookie) dObj.getCookie(EditCookie.class)).edit();
    }

    private static void openInDesignView(DDDataObject dObj) {
        try {
            dObj.showElement(dObj.getWebApp().getContextParam()[0]);
        } catch (Exception ex) {
            throw new AssertionFailedErrorException("Failed to switch to Design View",ex);
        }
    }

    /**
     * Used for running test from inside the IDE by internal execution.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

}
