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

package org.netbeans.installer.utils.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.system.shortcut.FileShortcut;
import org.netbeans.installer.utils.system.shortcut.InternetShortcut;
import org.netbeans.installer.utils.system.shortcut.Shortcut;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.system.shortcut.LocationType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Dmitry Lipin
 */
public class MacOsNativeUtils extends UnixNativeUtils {
    
    private static final String FILE_LOCALHOST = "file://localhost"; //NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    
    // constructor //////////////////////////////////////////////////////////////////
    MacOsNativeUtils() {
        loadLibrary(LIBRARY_PATH_MACOSX);
        initializeForbiddenFiles(FORBIDDEN_DELETING_FILES_MACOSX);
    }

    @Override
    protected Platform getPlatform() {        
        return System.getProperty("os.arch").contains("ppc") ? 
            Platform.MACOSX_PPC : 
            Platform.MACOSX_X86;
    }
    
    // NativeUtils implementation/override //////////////////////////////////////////
    @Override
    public File getDefaultApplicationsLocation() {
        File applications = new File("/Applications");
        
        if (applications.exists() &&
                applications.isDirectory() &&
                FileUtils.canWrite(applications)) {
            return applications;
        } else {
            return SystemUtils.getUserHomeDirectory();
        }
    }
    
    private String getShortcutFilename(Shortcut shortcut) {
        String fileName = shortcut.getFileName();
        
        if (fileName == null) {
            if(shortcut instanceof FileShortcut) {
                fileName = ((FileShortcut) shortcut).getTarget().getName();
                
                if (fileName!=null && fileName.endsWith(APP_SUFFIX)) {
                    fileName = fileName.substring(0,fileName.lastIndexOf(APP_SUFFIX));
                }
            } else if (shortcut instanceof InternetShortcut ) {
                fileName = ((InternetShortcut )shortcut).getURL().getFile();
            }
        }
        return fileName;
    }
    @Override
    public File getShortcutLocation(Shortcut shortcut, LocationType locationType) throws NativeException {
        if(shortcut.getPath()!=null) {
            return new File(shortcut.getPath());
        }

        String fileName = getShortcutFilename(shortcut);
        File file = null;
        switch (locationType) {
            case CURRENT_USER_DESKTOP:
                file = new File(SystemUtils.getUserHomeDirectory(), "Desktop/" + fileName);
                break;
            case ALL_USERS_DESKTOP:
                file = new File(SystemUtils.getUserHomeDirectory(), "Desktop/" + fileName);
                break;
            case CURRENT_USER_START_MENU:
                file = getDockPropertiesFile();
                break;
            case ALL_USERS_START_MENU:
                file = getDockPropertiesFile();
                break;
            case CUSTOM:
                file = new File(shortcut.getRelativePath(), fileName);
                break;
        }
        shortcut.setPath(file.getAbsolutePath());
        return file;
    }
    
    protected void createURLShortcut(InternetShortcut shortcut) throws NativeException {
        try {
            List<String> lines = new LinkedList<String> ();
            lines.add("[InternetShortcut]");
            lines.add("URL=" + shortcut.getURL());
            lines.add("IconFile=" + shortcut.getIconPath());
            lines.add(SystemUtils.getLineSeparator());
            FileUtils.writeStringList(new File(shortcut.getPath()),lines);
        } catch (IOException ex) {
            throw new NativeException("Can`t create URL shortcut", ex);
        }
    }
    @Override
    public File createShortcut(Shortcut shortcut, LocationType locationType) throws NativeException {
        try {
            final File shortcutFile = getShortcutLocation(shortcut, locationType);            
            switch (locationType) {
                case CURRENT_USER_DESKTOP:
                case ALL_USERS_DESKTOP:
                case CUSTOM:
                    // create a symlink for files/directories and .url for internet shortcuts
                    if (!shortcutFile.exists()) {
                        if (shortcut instanceof FileShortcut) {
                            createSymLink(shortcutFile, ((FileShortcut) shortcut).getTarget());
                        } else if (shortcut instanceof InternetShortcut) {
                            createURLShortcut((InternetShortcut) shortcut);
                        }
                    }
                    break;
                case CURRENT_USER_START_MENU:
                case ALL_USERS_START_MENU:
                    if (shortcut instanceof FileShortcut &&
                            convertDockProperties(true) == 0) {
                        //create link in the Dock
                        if (modifyDockLink((FileShortcut) shortcut, shortcutFile, true)) {
                            LogManager.log(ErrorLevel.DEBUG,
                                    "    Updating Dock");
                            convertDockProperties(false);
                            SystemUtils.executeCommand(null, UPDATE_DOCK_COMMAND);
                        }
                    }
                    break;
                default:
                    LogManager.log("... unknown shortcut type " + locationType);
                    return null;
            }
            return shortcutFile;
        } catch (IOException e) {
            throw new NativeException("Cannot create shortcut", e);
        }
    }
    @Override
    public void removeShortcut(Shortcut shortcut, LocationType locationType, boolean cleanupParents) throws NativeException {
        try {
            final File shortcutFile = getShortcutLocation(shortcut, locationType);
            switch(locationType) {
                case CURRENT_USER_DESKTOP:
                case ALL_USERS_DESKTOP:
                case CUSTOM:
                    // remove symlink from desktop
                    if(shortcutFile.exists()) {
                        FileUtils.deleteFile(shortcutFile,false);
                    }
                    break;
                case CURRENT_USER_START_MENU :
                case ALL_USERS_START_MENU :
                    if (shortcut instanceof FileShortcut &&
                            convertDockProperties(true) == 0) {
                        //remove link from the Dock
                        if (modifyDockLink((FileShortcut) shortcut, shortcutFile, false)) {
                            LogManager.log(ErrorLevel.DEBUG, "... updating Dock");
                            if (convertDockProperties(false) == 0) {
                                SystemUtils.executeCommand(null, UPDATE_DOCK_COMMAND);
                            }
                        }
                    }
                    break;
                default:
                    LogManager.log("... unknown shortcut type " + locationType);
            }
        } catch (IOException e) {
            throw new NativeException("Cannot remove shortcut", e);
        }
    }
    
    // mac os x specific ////////////////////////////////////////////////////////////
    public boolean isCheetah() {
        return (getOSVersion().startsWith("10.0"));
    }
    
    public boolean isPuma() {
        return (getOSVersion().startsWith("10.1"));
    }
    
    public boolean isJaguar() {
        return (getOSVersion().startsWith("10.2"));
    }
    
    public boolean isPanther() {
        return (getOSVersion().startsWith("10.3"));
    }
    
    public boolean isTiger() {
        return (getOSVersion().startsWith("10.4"));
    }

    public boolean isLeopard() {
        return (getOSVersion().startsWith("10.5"));
    }
    public boolean isSnowLeopard() {
        return (getOSVersion().startsWith("10.6"));
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private String getOSVersion() {
        return System.getProperty("os.version");
    }
    
    private File upToApp(
            final File file) {
        File executable = file;
        
        while ((executable != null) && 
                !executable.getPath().endsWith(APP_SUFFIX)) {
            executable = executable.getParentFile();
        }
        
        return executable;
    }
    
    private void modifyShortcutPath(
            final FileShortcut shortcut) {
        if (shortcut.canModifyPath()) {
            File target = upToApp(shortcut.getTarget());
            
            if (target != null) {
                shortcut.setTarget(target);
            }
        }
    }
    
    private boolean modifyDockLink(
            final FileShortcut shortcut, 
            final File dockFile, 
            final boolean adding) {
        OutputStream outputStream = null;
        boolean modified  = false;
        
        try {
            if(shortcut instanceof FileShortcut) {
                modifyShortcutPath(shortcut);
                
                final DocumentBuilderFactory documentBuilderFactory =
                        DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setNamespaceAware(true);
                
                final DocumentBuilder documentBuilder =
                        documentBuilderFactory.newDocumentBuilder();
                documentBuilder.setEntityResolver(new PropertyListEntityResolver());
                LogManager.log(ErrorLevel.DEBUG, 
                        "    parsing xml file...");
                
                final Document document = documentBuilder.parse(dockFile);
                LogManager.log(ErrorLevel.DEBUG,
                        "    ...complete");
                
                LogManager.log(ErrorLevel.DEBUG,
                        "    getting root element");
                final Element root = document.getDocumentElement();
                
                LogManager.log(ErrorLevel.DEBUG,
                        "    getting root/dict element");
                Element dict = XMLUtils.getChild(root, "dict");
                
                LogManager.log(ErrorLevel.DEBUG,
                        "    getting root/dict/[key=persistent-apps] element. dict = " +
                        dict.getNodeName());
                LogManager.log(ErrorLevel.DEBUG,"Get Keys");
                
                
                List<Element> keys = XMLUtils.getChildren(dict, "key");
                LogManager.log(ErrorLevel.DEBUG,"Length = " + keys.size());
                Element persistentAppsKeyNode = null;
                int index = 0;
                while(keys.get(index)!=null) {
                    if(keys.get(index).getTextContent().equals("persistent-apps")) {
                        persistentAppsKeyNode = keys.get(index);
                        break;
                    }
                    index++;
                }
                
                if(persistentAppsKeyNode == null) {
                    LogManager.log(ErrorLevel.DEBUG,
                            "    Not found.. strange.. Create new one");
                    persistentAppsKeyNode = XMLUtils.appendChild(dict,"key","persistent-apps");
                } else {
                    LogManager.log(ErrorLevel.DEBUG,
                            "    done. KeyNode = " + persistentAppsKeyNode.getTextContent());
                }
                LogManager.log(ErrorLevel.DEBUG,
                        "    Getting next element.. expecting it to be array element");
                Element array = keys.get(index);
                Node arrayIt = array;
                
                index = 0 ;
                int MAX_SEARCH_ELEMENTS = 20 ;
                String nodeName;
                while(index < MAX_SEARCH_ELEMENTS && arrayIt!=null) {
                    nodeName = arrayIt.getNodeName();
                    if(nodeName!=null && nodeName.equals("array") &&
                            arrayIt instanceof Element) {
                        break;
                    }
                    
                    arrayIt = arrayIt.getNextSibling();
                    index++;
                }
                
                if(index==MAX_SEARCH_ELEMENTS || arrayIt==null) {
                    LogManager.log(ErrorLevel.DEBUG,
                            "    is not an array element... very strange");
                    return false;
                }
                array = (Element) arrayIt;
                
                if(array==null) {
                    LogManager.log(ErrorLevel.DEBUG,
                            "    null... very strange");
                    return false;
                }
                LogManager.log(ErrorLevel.DEBUG,
                        "    OK. Content = " + array.getNodeName());
                if(!array.getNodeName().equals("array")) {
                    LogManager.log(ErrorLevel.DEBUG,
                            "    Not an array element");
                    return false;
                }
                
                
                if(adding) {
                    LogManager.log(ErrorLevel.DEBUG,
                            "Adding shortcut with the following properties: ");
                    
                    LogManager.log(ErrorLevel.DEBUG, "    target = " + shortcut.getTargetPath());
                    LogManager.log(ErrorLevel.DEBUG, "    name = " + shortcut.getName());
                    
                    dict = XMLUtils.appendChild(array,"dict",null);
                    XMLUtils.appendChild(dict,"key","tile-data");
                    Element dictChild = XMLUtils.appendChild(dict,"dict",null);
                    XMLUtils.appendChild(dictChild,"key","file-data");
                    Element dictCC = XMLUtils.appendChild(dictChild,"dict",null);
                    XMLUtils.appendChild(dictCC,"key","_CFURLString");
                    XMLUtils.appendChild(dictCC,"string",shortcut.getTargetPath());
                    XMLUtils.appendChild(dictCC,"key","_CFURLStringType");
                    XMLUtils.appendChild(dictCC,"integer","0");
                    XMLUtils.appendChild(dictChild,"key","file-label");
                    XMLUtils.appendChild(dictChild,"string",shortcut.getName());
                    XMLUtils.appendChild(dictChild,"key","file-type");
                    XMLUtils.appendChild(dictChild,"integer","41");
                    XMLUtils.appendChild(dict,"key","tile-type");
                    XMLUtils.appendChild(dict, "string","file-tile");
                    LogManager.log(ErrorLevel.DEBUG,
                            "... adding shortcut to Dock XML finished");
                    modified = true;
                } else {
                    LogManager.log(ErrorLevel.DEBUG,
                            "Removing shortcut with the following properties: ");
                    LogManager.indent();
                    LogManager.log(ErrorLevel.DEBUG, "    target = " + shortcut.getTargetPath());
                    LogManager.log(ErrorLevel.DEBUG,
                            "name = " + shortcut.getName());
                    
                    String location = shortcut.getTargetPath();
                    List<Element> dcts = new LinkedList <Element> ();
                    
                    for(Element el1: XMLUtils.getChildren(array, "dict")) {
                        for(Element el2: XMLUtils.getChildren(el1, "dict")) {
                            for(Element el3: XMLUtils.getChildren(el2, "dict")) {
                                dcts.addAll(XMLUtils.getChildren(el3, "string"));
                            }
                        }
                    }
                    
                    
                    index = 0;
                    Node dct = null;
                    LogManager.log(ErrorLevel.DEBUG,
                            "Total dict/dict/dict/string items = " + dcts.size());
                    LogManager.log(ErrorLevel.DEBUG,
                            "        location = " + location);                   
                    
                    File locationFile = new File(location);
                    
                    while(index < dcts.size() && dcts.get(index)!=null) {
                        Node item = dcts.get(index);
                        String content = item.getTextContent();
                        LogManager.log(ErrorLevel.DEBUG, "        content = " + content);
                        
                        if (content!=null && !content.equals("")) {
                            if (content.startsWith(FILE_LOCALHOST)) {
                                content = content.substring(FILE_LOCALHOST.length());
                            }

                            File contentFile = new File(content);
                                                
                            if(locationFile.equals(contentFile)) {
                                dct = item;
                                break;
                            }
                        }
                        index++;
                    }
                    
                    if(dct!=null) {
                        LogManager.log(ErrorLevel.DEBUG,
                                "Shortcut exists in the dock.plist");
                        array.removeChild(dct.getParentNode().getParentNode().getParentNode());
                        modified = true;
                    } else {
                        LogManager.log(ErrorLevel.DEBUG,
                                "... shortcut doesn`t exist in the dock.plist");
                        modified = false;
                    }
                    LogManager.unindent();
                    LogManager.log(ErrorLevel.DEBUG,
                            "... removing shortcut from Dock XML finished");
                }
                if(modified) {
                    LogManager.log(ErrorLevel.DEBUG,
                            "    Saving XML... ");
                    XMLUtils.saveXMLDocument(document,dockFile);
                    LogManager.log(ErrorLevel.DEBUG,
                            "    Done (saving xml)");
                }
            } else {
                LogManager.log(ErrorLevel.DEBUG,
                        "Adding non-file shortcuts to the Dock is not supported");
            }
        } catch (ParserConfigurationException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        } catch (XMLException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        } catch (SAXException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        } catch (IOException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        } catch (NullPointerException e) {
            LogManager.log(ErrorLevel.WARNING,e);
            return false;
        } finally {
            if (outputStream!=null) {
                try {
                    outputStream.close();
                } catch (IOException ex) {
                    LogManager.log(ErrorLevel.WARNING,
                            "Can`t close stream for Dock properties file");
                }
            }
        }
        LogManager.log(ErrorLevel.DEBUG,
                "    Return " + modified + " from modifyDockLink");
        return modified;
        
    }
    
    private File getDockPropertiesFile() {
        return new File(SystemUtils.getUserHomeDirectory(),
                "Library/Preferences/" + DOCK_PROPERIES);//NOI18N
    }
    
    private int convertDockProperties(boolean decode) {
        File dockFile = getDockPropertiesFile();
        int returnResult = 0;
        try {
            if(!isCheetah() && !isPuma()) {
                if((!decode && (isTiger() || isLeopard() || isSnowLeopard())) || decode) {
                    // decode for all except Cheetah and Puma
                    // code only for Tiger and Leopard
                    
                    ExecutionResults result = SystemUtils.executeCommand(null,
                            new String[] { PLUTILS,PLUTILS_CONVERT,(decode)? PLUTILS_CONVERT_XML :
                                PLUTILS_CONVERT_BINARY,dockFile.getPath()});
                    returnResult = result.getErrorCode();
                }
            }
        } catch (IOException ex) {
            LogManager.log(ErrorLevel.WARNING,ex);
            returnResult = -1;
        }
        return returnResult;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private class PropertyListEntityResolver implements  EntityResolver {
        public static final String PROPERTTY_LIST_DTD_LOCAL =
                "/System/Library/DTDs/PropertyList.dtd"; //NOI18N
        public static final String PROPERTTY_LIST_DTD_REMOTE =
                "http://www.apple.com/DTDs/PropertyList-1.0.dtd"; //NOI18N
        public static final String PROPERTTY_LIST_DTD_PUBLIC_ID =
                "-//Apple Computer//DTD PLIST 1.0//EN"; //NOI18N
        
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            File propDtd = new File(PROPERTTY_LIST_DTD_LOCAL);
            
            return ((PROPERTTY_LIST_DTD_PUBLIC_ID.equals(publicId) ||
                    PROPERTTY_LIST_DTD_REMOTE.equals(systemId)) &&
                    FileUtils.exists(propDtd)) ?
                        new InputSource(new FileInputStream(propDtd)):
                        null;
            
        }
        
    }
    @Override
    protected String [] getPossibleBrowserLocations() {
        return POSSIBLE_BROWSER_LOCATIONS_MAC;
    }
    
    public static final String[] POSSIBLE_BROWSER_LOCATIONS_MAC = new String[]{
        "/usr/bin/open"
    };
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String LIBRARY_PATH_MACOSX =
            NATIVE_JNILIB_RESOURCE_SUFFIX + "macosx/macosx.dylib"; // NO18N
    
    public static final String APP_SUFFIX = 
            ".app"; // NOI18N
    
    private static final String   DOCK_PROPERIES = 
            "com.apple.dock.plist"; // NOI18N
    
    private static final String PLUTILS = 
            "plutil"; // NOI18N
    
    private static final String PLUTILS_CONVERT = 
            "-convert"; // NOI18N
    
    private static final String PLUTILS_CONVERT_XML = 
            "xml1"; // NOI18N
    
    private static final String PLUTILS_CONVERT_BINARY = 
            "binary1"; // NOI18N
    
    private static final String[] UPDATE_DOCK_COMMAND  = new String[] {
        "killall", // NOI18N
        "-HUP", // NOI18N
        "Dock", // NOI18N
    };
    
    public static final String[] FORBIDDEN_DELETING_FILES_MACOSX = {
        "/Applications", // NOI18N
        "/Developer", // NOI18N
        "/Library", // NOI18N
        "/Network", // NOI18N
        "/System", // NOI18N
        "/Users", // NOI18N
    };
}