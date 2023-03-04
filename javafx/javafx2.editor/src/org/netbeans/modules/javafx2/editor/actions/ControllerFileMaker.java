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
package org.netbeans.modules.javafx2.editor.actions;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.java.source.parsing.ClasspathInfoProvider;
import org.netbeans.modules.javafx2.editor.completion.model.FxModel;
import org.netbeans.modules.javafx2.editor.completion.model.FxXmlSymbols;
import org.netbeans.modules.javafx2.editor.completion.model.FxmlParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import static org.netbeans.modules.javafx2.editor.actions.Bundle.*;

/**
 *
 * @author sdedic
 */
class ControllerFileMaker {
    private DataObject  context;
    private DataObject  controllerFile;
    private String      controllerClassName;
    private boolean     hasControllerName;

    public ControllerFileMaker(DataObject context) {
        this.context = context;
    }

    public String getControllerClassName() {
        return controllerClassName;
    }
    
    public boolean hasControllerName() {
        return hasControllerName;
    }
    
    
    /**
     * Creates a new Controller form the template, or finds & returns an
     * existing one - if one is mentioned in the source file.
     * @return {@code null} in the case of an error
     */
    @NbBundle.Messages({
        "TITLE_makeController=Make Controller",
        "ERR_controllerClassNotValid=Controller class name is not a valid identifier",
        "ERR_controllerClassAmbiguous=Controller class name is ambiguous",
        "ERR_controllerSourceNotFound=Controller class source cannot be found",
        "ERR_controllerInLibrary=Controller class is in a library",
        "ERR_controllerSourceFailure=Could not load controller source"
    })
    DataObject getControllerFile() throws IOException {
        Source s = Source.create(context.getPrimaryFile());
        final DataObject[] result = new DataObject[1];
        
        final ClasspathInfo cpInfo = ClasspathInfo.create(context.getPrimaryFile());
        
        try {
            ParserManager.parse(Collections.singleton(s), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    final FxmlParserResult fxResult = FxmlParserResult.get(resultIterator.getParserResult());
                    if (fxResult == null) {
                        throw new IllegalStateException();
                    }
                    
                    FxModel m = fxResult.getSourceModel();
                    if (m == null || m.getRootComponent() == null) {
                        // the file is either totally broken, or contains no root component
                        // -> we cannot create the controller anyway
                        return;
                    }
                    
                    controllerClassName = m.getController();
                    
                    if (controllerClassName == null) {
                        return;
                    }
                    hasControllerName = true;
                    // try to get the type
                    ElementHandle<TypeElement> type = m.getControllerType();
                    if (type != null) {
                        // override with a resolved name
                        controllerClassName = type.getQualifiedName();
                        FileObject target = SourceUtils.getFile(type, cpInfo);
                        if (target == null) {
                            throw new ParseException(ERR_controllerSourceNotFound());
                        }
                        if (FileUtil.getArchiveFile(target) != null) {
                            throw new ParseException(ERR_controllerInLibrary());
                        }
                        // we've found the source file
                        try {
                            result[0] = DataObject.find(target);
                        } catch (IOException ex) {
                            throw new ParseException(ERR_controllerSourceFailure(), ex);
                        }
                    } else {
                        // the type may not be found, or is ambigous, or an invalid identifier. Check
                        if (!FxXmlSymbols.isQualifiedIdentifier(controllerClassName)) {
                            // error, not a valid identifier
                            throw new ParseException(ERR_controllerClassNotValid());
                        }
                        
                        // must run java compilation:
                        
                        class UT extends UserTask implements ClasspathInfoProvider {

                            @Override
                            public void run(ResultIterator resultIterator) throws Exception {
                                CompilationInfo info = CompilationInfo.get(resultIterator.getParserResult());
                                Set<String> names = fxResult.resolveClassName(info, controllerClassName);
                                // essentially null or many, otherwise the type would have been resolved by fxml parsing
                                if (names != null) {
                                    throw new ParseException(ERR_controllerClassAmbiguous());
                                }
                            }

                            @Override
                            public ClasspathInfo getClasspathInfo() {
                                return cpInfo;
                            }
                            
                        }
                        
                        ParserManager.parse("text/x-java", new UT());
                        
                        // if succeeds, the controller class is OK but not created, so we'll create it.
                    }
                }
            });
        } catch (ParseException ex) {
            Throwable cause;
            
            if (ex.getCause() == ex) {
                cause = ex;
            } else {
                cause = ex.getCause();
            }
            NotifyDescriptor nd = new NotifyDescriptor(
                    cause.getLocalizedMessage(), 
                    TITLE_makeController(), 
                    NotifyDescriptor.DEFAULT_OPTION, 
                    NotifyDescriptor.WARNING_MESSAGE,
                    new Object[] { NotifyDescriptor.OK_OPTION },
                    null
            );
            DialogDisplayer.getDefault().notifyLater(nd);
            return null;
        }
        
        if (result[0] != null) {
            return result[0];
        }
        
        ClassPath cp = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
        FileObject root = cp.findOwnerRoot(context.getPrimaryFile());
        if (root == null) {
            // ???
            throw new UnsupportedOperationException();
        }
        String cn = controllerClassName;
        String packageName;
        DataFolder targetFolder;
        String folderName;
        
        String fname;
        
        if (cn == null) {
            fname = FMT_controllerClassName(capitalize(context.getName()));
            // folder name within the classpath. Make 
            String resName = cp.getResourceName(context.getPrimaryFile().getParent());
            packageName = resName.replace("/", ".");
            targetFolder = context.getFolder();
        } else {
            int dot = cn.lastIndexOf('.');
            if (dot == -1) {
                fname = cn;
                String resName = cp.getResourceName(context.getPrimaryFile().getParent());
                packageName = resName.replace("/", ".");
                targetFolder = context.getFolder();
            } else {
                // convert the prefix into package:
                packageName = cn.substring(0, dot);
                fname = cn.substring(dot + 1);
                folderName = packageName.replace(".", "/");
                FileObject folder;
                
                folder = root.getFileObject(folderName);
                if (folder == null || folder.isData()) {
                    folder = FileUtil.createFolder(root, folderName);
                }
                targetFolder = DataFolder.findFolder(folder);
            }
        }
        if (!isValidPackageName(packageName)) {
            throw new UnsupportedOperationException("handle invalid package");
        }
        
        controllerClassName = packageName != null && !"".equals(packageName) ? packageName + "." + fname : fname;
        
        FileObject templateF = FileUtil.getConfigFile(TEMPLATE_PATH);
        DataObject template = DataObject.find(templateF);
        
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("package", packageName); //NOI18N

        // try what if the controller already exists; mainly for the case with null controller name in FXML:
        FileObject existing = targetFolder.getPrimaryFile().getFileObject(fname, "java");
        if (existing != null) {
            return DataObject.find(existing);
        }
        return template.createFromTemplate(targetFolder, fname, parameters);
    }

    private static String capitalize(String str) {
        final String spaceChar = " "; //NOI18N
        if (str.contains(spaceChar)) {
            String[] splittedName = str.trim().split(spaceChar);
            StringBuilder sb = new StringBuilder();
            for (String part : splittedName) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1));
            }
            return sb.toString();
        } else {
            return Character.toUpperCase(str.charAt(0)) + str.substring(1);
        }
    }

    private static final String TEMPLATE_PATH = "Templates/javafx/FXMLController.java"; // NO18N

    static boolean isValidPackageName(String str) {
        if (str.length() > 0 && str.charAt(0) == '.') {
            return false;
        }
        StringTokenizer tukac = new StringTokenizer(str, ".");
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if ("".equals(token)) {
                return false;
            } 
            if (!Utilities.isJavaIdentifier(token)) {
                return false;
            }
        }
        return true;
    }
    
}
