/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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

    private final static String BEANS_END_TAG = "</beans>";   //NOI18N
    private final static String CONTEXT_CONFIG_LOCATION = "contextConfigLocation";    //NOI18N
    private final static String DISPATCHER_SERVLET_NAME = "dispatcher-servlet";    //NOI18N
    private final static String JAVA_MIME = "text/x-java";    //NOI18N
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
