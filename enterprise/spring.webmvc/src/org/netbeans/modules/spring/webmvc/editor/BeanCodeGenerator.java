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
package org.netbeans.modules.spring.webmvc.editor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.Line.ShowOpenType;
import org.openide.text.Line.ShowVisibilityType;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author alexeybutenko
 */
public class BeanCodeGenerator implements CodeGenerator {

    private static final String BEANS_END_TAG = "</beans>";   //NOI18N
    private static final String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";    //NOI18N
    private static final String DISPATCHER_SERVLET_NAME = "dispatcher-servlet";    //NOI18N
    private static final String JAVA_MIME = "text/x-java";    //NOI18N
    private Document document;
    private FileObject fileObject;

    private BeanCodeGenerator(JTextComponent component) {
        document = component.getDocument();
        fileObject = NbEditorUtilities.getFileObject(document);
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(BeanCodeGenerator.class, "MSG_BeanCodeGenerator");
    }

    @Override
    public void invoke() {
        AddBeanPanel panel = new AddBeanPanel(document);
        AddBeanPanelVisual comp = new AddBeanPanelVisual(panel);
        boolean accept = comp.showDialog();
        if (accept) {
            createBean(panel.getId(), panel.getClassName());
        }
    }

    private void insertBean(String componentStr, FileObject fo) {
        try {
            DataObject dataObject = DataObject.find(fo);
            if (dataObject != null) {
                EditorCookie ec = dataObject.getCookie(EditorCookie.class);
                LineCookie lc = dataObject.getCookie(LineCookie.class);
                if (ec != null && lc != null) {
                    StyledDocument doc = ec.openDocument();
                    if (doc != null) {
                        int offset = doc.getText(0, doc.getLength()).indexOf(BEANS_END_TAG) - 1;
                        doc.insertString(offset, componentStr, null);
                        offset = offset + componentStr.length();
                        int lineNumber = NbDocument.findLineNumber(doc, offset);
                        if (lineNumber != -1) {
                            Line line = lc.getLineSet().getCurrent(lineNumber);
                            if (line != null) {
                                int lineOffset = NbDocument.findLineOffset(doc, lineNumber);
                                int column = offset - lineOffset;
                                line.show(ShowOpenType.OPEN, ShowVisibilityType.FOCUS, column);
                            }
                        }
                    }
                }

            }
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void createBean(String id, String className) {
        SpringScope scope = SpringScope.getSpringScope(fileObject);
        final String lineSep = System.getProperty("line.separator");    //NOI18N
        final String insertStr = lineSep + "    <bean id=\"" + id + "\" class=\"" + className + "\"/>";   //NOI18N
        if (!JAVA_MIME.equals(fileObject.getMIMEType())) {
            File file = FileUtil.toFile(fileObject);
            if (scope.getConfigFileManager().getConfigFiles().contains(file)) {
                insertBean(insertStr, fileObject);
                return;
            }
        }
        for (File file : scope.getConfigFileManager().getConfigFiles()) {
            FileObject fo = FileUtil.toFileObject(file);
            if (!isDispatcherServlet(fo)) {
                insertBean(insertStr, fo);
                break;
            }

        }
    }

    private boolean isDispatcherServlet(FileObject configFile) {
        return configFile.getName().equals(DISPATCHER_SERVLET_NAME);
    }

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            JTextComponent component = context.lookup(JTextComponent.class);
            List<CodeGenerator> generators = new ArrayList<CodeGenerator>();
            Document doc = component.getDocument();
            if (doc != null) {
                FileObject fo = NbEditorUtilities.getFileObject(doc);
                WebModule webModule = WebModule.getWebModule(fo);
                if (webModule != null && springConfigExist(webModule)) {
                    generators.add(new BeanCodeGenerator(component));
                }
            }
            return generators;
        }

        private boolean springConfigExist(WebModule webModule) {
            //Need to check spring config file in web.xml
            FileObject dd = webModule.getDeploymentDescriptor();
            if (dd == null) {
                return false;
            }
            try {

                WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
                InitParam[] parameters = webApp.getContextParam();
                for (InitParam param : parameters) {
                    if (param.getParamName().startsWith(CONTEXT_CONFIG_LOCATION)) {
                        return true;
                    }
                }
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
            }
            return false;
        }
    }
}
