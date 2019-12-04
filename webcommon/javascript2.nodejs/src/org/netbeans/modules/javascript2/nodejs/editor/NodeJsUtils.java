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
package org.netbeans.modules.javascript2.nodejs.editor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Petr pisl
 */
public class NodeJsUtils {

    public static final String REQUIRE_METHOD_NAME = "require"; // NOI18N
    public static final String ON_METHOD_NAME = "on"; // NOI18N
    public static final String NODE_MODULES_NAME = "node_modules"; // NOI18N
    public static final String PACKAGE_NAME = "package"; //NOI18N
    public static final String INDEX_NAME = "index"; //NOI18N
    public static final String FAKE_OBJECT_NAME_PREFIX = "nm$_"; //NOI18N
    public static final String NODEJS_NAME = "NodeJS"; //NOI18N
    
    public static final String JS_EXT = "js"; //NOI18N
    public static final String JSON_EXT = "json"; //NOI18N
    public static final String NODE_EXT = "node"; //NOI18N
    
    private static final String MAIN_FIELD = "main"; //NOI18N
    private static final String SLASH = "/"; //NOI18N
    
    public static final String EXPORTS = "exports"; //NOI18N
    public static final String MODULE = "module"; //NOI18N
    public static final String PROTOTYPE = "prototype"; //NOI18N

    private static final String NODEJS_ICON_PATH = "org/netbeans/modules/javascript2/nodejs/resources/nodeJs16.png"; //NOI18N
    private static ImageIcon NODEJS_ICON = null;
    
    public static ImageIcon getNodeJsIcon () {
        if (NODEJS_ICON == null) {
            NODEJS_ICON = new ImageIcon(ImageUtilities.loadImage(NODEJS_ICON_PATH)); //NOI18N
        }
        return NODEJS_ICON;
    }
    public static FileObject findModuleFile(FileObject fromModule, String modulePath) {
        if (modulePath == null || modulePath.isEmpty()) {
            // do nothing in such case
            return null;
        }
        char firstChar = modulePath.charAt(0);
        FileObject resultFO = null;
        // we should now recognize, whether the identifier is a core module
        // if (coreModule) return null;
        if (firstChar == '/') {
            File file = new File(modulePath);
            if (file.exists()) {
                resultFO = FileUtil.toFileObject(file);
            } 
            if (resultFO != null && !resultFO.isFolder()) {
                return resultFO;
            }
            return null;
        }
        if (firstChar == '.') {
            resultFO = findModuleAsFile(fromModule, modulePath);
            if (resultFO == null) {
                resultFO = findModuleAsFolder(fromModule, modulePath);
            }
            if (resultFO != null) {
                // we don't want to show .node files (binary files)
                return NODE_EXT.equals(resultFO.getExt()) ? null : resultFO;
            }
        }

        resultFO = findNodeModule(fromModule, modulePath);
        if (resultFO != null) {
            // we don't want to show .node files (binary files)
            return NODE_EXT.equals(resultFO.getExt()) ? null : resultFO;
        }
        return null;
    }

    private static FileObject findModuleAsFile(final FileObject fromModule, final String module) {
        FileObject parentFO = fromModule.isFolder() ? fromModule : fromModule.getParent();
        if (parentFO != null) {
            FileObject resultFO = parentFO.getFileObject(module);
            if (resultFO != null && !resultFO.isFolder()) {
                return resultFO;
            }
            resultFO = parentFO.getFileObject(module + '.' + JS_EXT);
            if (resultFO != null && !resultFO.isFolder()) {
                return resultFO;
            }
            resultFO = parentFO.getFileObject(module + '.' + JSON_EXT);
            if (resultFO != null && !resultFO.isFolder()) {
                return resultFO;
            }
            resultFO = parentFO.getFileObject(module + '.' + NODE_EXT);
            if (resultFO != null && !resultFO.isFolder()) {
                return resultFO;
            }
        }
        return null;
    }

    private static FileObject findModuleAsFolder(final FileObject fromModule, final String module) {
        FileObject parentFO = fromModule.isFolder() ? fromModule : fromModule.getParent();
        if (parentFO == null) {
            return null;
        }
        FileObject moduleFolderFO = parentFO.getFileObject(module);
        if (moduleFolderFO != null && moduleFolderFO.isFolder()) {
            FileObject packageFO = moduleFolderFO.getFileObject(PACKAGE_NAME + '.' + JSON_EXT);
            FileObject resultFO = null;
            if (packageFO != null && !packageFO.isFolder()) {
                // need to parser package.json
                // find "main" field
                String valueOfMain = getValueOfMain(packageFO);
                if (valueOfMain != null) {
                    resultFO = findModuleAsFile (packageFO, valueOfMain);
                    if (resultFO != null) {
                        return resultFO;
                    }
                }
            }
            resultFO = parentFO.getFileObject(module + "/" + INDEX_NAME + '.' + JS_EXT);
            if (resultFO != null && !resultFO.isFolder()) {
                return resultFO;
            }
            resultFO = parentFO.getFileObject(module + "/" + INDEX_NAME + '.' + NODE_EXT);
            if (resultFO != null && !resultFO.isFolder()) {
                return resultFO;
            }
        }
        return null;
    }

    private static FileObject findNodeModule(final FileObject fromModule, final String module) {
        FileObject runtimeModule = getRuntimeModuleFile(fromModule, module);
        if (runtimeModule != null) {
            // the runtime modules has the biggest priority 
            return runtimeModule;
        }
        FileObject parentFolder = fromModule.isFolder() ? fromModule : fromModule.getParent();
        // we have to go through parent/node_modules/modulePath
        while (parentFolder != null) {
            FileObject nodeModulesFO = parentFolder.getFileObject(NODE_MODULES_NAME);
            if (nodeModulesFO != null) {
                FileObject resultFO = findModuleAsFile(nodeModulesFO, module); //NOI18N
                if (resultFO == null) {
                    resultFO = findModuleAsFolder(nodeModulesFO, module); //NOI18N
                }
                if (resultFO != null) {
                    return resultFO;
                }
            }
            parentFolder = parentFolder.getParent();
        }
        return null;
    }

    private static FileObject getRuntimeModuleFile(final FileObject fromModule, final String module) {
        FileObject runtime = NodeJsDataProvider.getDefault(fromModule).getFolderWithRuntimeSources();
        FileObject result = null;
        if (runtime != null && runtime.isFolder()) {
            result = runtime.getFileObject(module, "js");   //NOI18N
        }
        return result;
    }
    
    private static String getValueOfMain(final FileObject file) {
        String content = loadFileContent(file);
        String value = null;
        if (content != null && !content.isEmpty()) {
            JSONObject root = (JSONObject) JSONValue.parse(content);
            if (root != null) {
                Object main = root.get(MAIN_FIELD);
                if (main != null && main instanceof String) {
                    value = (String)main;
                }
            }
        }
        return value;
    }

    public static String loadFileContent(final FileObject file) {
        Reader r = null;
        try {
            DataObject dobj = DataObject.find(file);
            EditorCookie ec = dobj.getLookup().lookup(EditorCookie.class);
            if (ec == null) {
                return null;
            }
            final StyledDocument document = ec.openDocument();
            final AtomicReference<String> docContentRef = new AtomicReference<>();
            final AtomicReference<BadLocationException> bleRef = new AtomicReference<>();
            document.render(new Runnable() {

                @Override
                public void run() {
                    try {
                        docContentRef.set(document.getText(0, document.getLength()));
                    } catch (BadLocationException ex) {
                        bleRef.set(ex);
                    }
                }

            });
            if (bleRef.get() != null) {
                return null;
            }
            return docContentRef.get();
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } 
        return "";  //NOI18N
    }
    
    public static String getModuleName(String modulePath) {
        String name = modulePath;
        if(name.indexOf('/') > -1) {
            name = name.substring(name.lastIndexOf('/') + 1);
        }
        if(name.indexOf('.') > -1) {
            name = name.substring(0, name.lastIndexOf('.'));
        }
        return name;
    }
    
    public static String writeFilePathForDocWindow(final FileObject fo) {
        String path = fo.getPath();
        String[] parts = path.split(SLASH);
        StringBuilder sb = new StringBuilder();
        sb.append("<pre>"); // NOI18N
        int length = 0;
        for (String part : parts) {
            if ((length + part.length()) > 50) {
                sb.append("\n    "); // NOI18N
                length = 4;
            }
            sb.append(part).append('/');
            length += part.length() + 1;
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("</pre>"); // NOI18N
        return sb.toString();
    }
}
