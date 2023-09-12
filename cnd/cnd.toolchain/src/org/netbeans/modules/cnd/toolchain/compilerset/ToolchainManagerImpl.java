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

package org.netbeans.modules.cnd.toolchain.compilerset;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.AlternativePath;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.BaseFolder;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CMakeDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.DebuggerDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.LinkerDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.MakeDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.PredefinedMacro;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.QMakeDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ScannerDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ScannerPattern;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolchainDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.xml.XMLUtil;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 */
@SuppressWarnings({"PackageVisibleInnerClass","PackageVisibleField"})
public final class ToolchainManagerImpl {

    private static final Logger LOG = Logger.getLogger(ToolchainManagerImpl.class.getName());
    public static final boolean TRACE = Boolean.getBoolean("cnd.toolchain.personality.trace"); // NOI18N
    private static final boolean CREATE_SHADOW = Boolean.getBoolean("cnd.toolchain.personality.create_shadow"); // NOI18N
    public static final String SHADOW_KEY = "toolchain_shadow"; // NOI18N
    public static final String ROOT_FOLDER = "CND"; // NOI18N
    public static final String SHADOW_FOLDER = "ToolChain"; // NOI18N
    public static final String TOOLCHAIN_FOLDER = "ToolChains"; // NOI18N
    public static final String TOOL_FOLDER = "Tool"; // NOI18N
    private static final ToolchainManagerImpl manager = new ToolchainManagerImpl();
    private final List<ToolchainDescriptor> descriptors = new ArrayList<ToolchainDescriptor>();

    public static ToolchainManagerImpl getImpl() {
        return manager;
    }

    private ToolchainManagerImpl() {
        if (NbPreferences.forModule(ToolchainManagerImpl.class).getBoolean(SHADOW_KEY, false)) {
            initToolchainManager();
        } else {
            initToolchainManager2();
        }
    }

    private void initToolchainManager() {
        try {
            Map<Integer, CompilerVendor> vendors = new TreeMap<Integer, CompilerVendor>();
            Map<String, String> cache = new HashMap<String, String>();
            FileObject folder = FileUtil.getConfigFile(ROOT_FOLDER+"/"+SHADOW_FOLDER); // NOI18N
            int indefinedID = Integer.MAX_VALUE / 2;
            if (folder != null && folder.isFolder()) {
                FileObject[] files = folder.getChildren();
                for (FileObject file : files) {
                    Integer position = (Integer) file.getAttribute("position"); // NOI18N
                    if (position == null || vendors.containsKey(position)) {
                        position = indefinedID++;
                    }
                    String displayName = (String) file.getAttribute("displayName"); // NOI18N
                    CompilerVendor v = new CompilerVendor(file.getNameExt(), position);
                    if (read(file, files, v, new HashSet<FileObject>(), cache)) {
                        if (displayName != null && !displayName.isEmpty()) {
                            v.toolChainDisplay = displayName;
                        }
                        vendors.put(position, v);
                    }
                }
            }
            for (CompilerVendor v : vendors.values()) {
                descriptors.add(new ToolchainDescriptorImpl(v));
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
        if (CREATE_SHADOW && !NbPreferences.forModule(ToolchainManagerImpl.class).getBoolean(SHADOW_KEY, false)) {
            try {
                writeToolchains();
                NbPreferences.forModule(ToolchainManagerImpl.class).putBoolean(SHADOW_KEY, true);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    private void initToolchainManager2() {
        try {
            Map<Integer, CompilerVendor> vendors = new TreeMap<Integer, CompilerVendor>();
            Map<String, String> cache = new HashMap<String, String>();
            FileObject folder = FileUtil.getConfigFile(ROOT_FOLDER+"/"+TOOLCHAIN_FOLDER); // NOI18N
            FileObject[] files = FileUtil.getConfigFile(ROOT_FOLDER+"/"+TOOL_FOLDER).getChildren(); // NOI18N
            int indefinedID = Integer.MAX_VALUE / 2;
            if (folder != null && folder.isFolder()) {
                for (FileObject file : folder.getChildren()) {
                    Integer position = (Integer) file.getAttribute("position"); // NOI18N
                    if (position == null || vendors.containsKey(position)) {
                        position = indefinedID++;
                    }
                    String displayName = null;
                    String bundleName = (String)file.getAttribute("SystemFileSystem.localizingBundle"); // NOI18N
                    if (bundleName != null) {
                        try {
                            bundleName = Utilities.translate(bundleName);
                            ResourceBundle bundle = NbBundle.getBundle(bundleName);
                            if (bundle != null) {
                                    displayName = bundle.getString(file.getPath());
                            }
                        } catch (MissingResourceException ex) {
                            ex.printStackTrace(System.err);
                        }
                    }
                    CompilerVendor v = new CompilerVendor(file.getNameExt()+".xml", position); // NOI18N
                    if (read2(file, files, v, cache)) {
                        if (displayName != null && !displayName.isEmpty()) {
                            v.toolChainDisplay = displayName;
                        }
                        vendors.put(position, v);
                    }
                }
            }
            for (CompilerVendor v : vendors.values()) {
                descriptors.add(new ToolchainDescriptorImpl(v));
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        }
        if (CREATE_SHADOW && !NbPreferences.forModule(ToolchainManagerImpl.class).getBoolean(SHADOW_KEY, false)) {
            try {
                writeToolchains();
                NbPreferences.forModule(ToolchainManagerImpl.class).putBoolean(SHADOW_KEY, true);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }

    /**
     * available in package for testing only
     */
    public void reinitToolchainManager() {
        try {
            writeToolchains();
            NbPreferences.forModule(ToolchainManagerImpl.class).putBoolean(SHADOW_KEY, true);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        descriptors.clear();
        initToolchainManager();
    }

    public ToolchainDescriptor getToolchain(String name, int platform) {
        for (ToolchainDescriptor d : descriptors) {
            if (name.equals(d.getName()) && (ToolUtils.isPlatforSupported(platform, d)
                        || ToolUtils.isPlatforSupported(PlatformTypes.PLATFORM_NONE, d))) {
                return d;
            }
        }
        for (ToolchainDescriptor d : descriptors) {
            if (ToolUtils.isPlatforSupported(PlatformTypes.PLATFORM_NONE, d)) {
                return d;
            }
        }
        return null;
    }

    public List<ToolchainDescriptor> getAllToolchains() {
        return new ArrayList<ToolchainDescriptor>(descriptors);
    }

    public List<ToolchainDescriptor> getToolchains(int platform) {
        List<ToolchainDescriptor> res = new ArrayList<ToolchainDescriptor>();
        for (ToolchainDescriptor d : descriptors) {
            if (ToolUtils.isPlatforSupported(platform, d)) {
                res.add(d);
            }
        }
        return res;
    }

    private boolean read(FileObject file, FileObject[] files, CompilerVendor v, Set<FileObject> antiloop, Map<String, String> cache) {
        if (antiloop.contains(file)) {
            LOG.log(Level.INFO, "Recursive inclusion of file {0}", file.getPath()); // NOI18N
            return false;
        }
        antiloop.add(file);
        String originalFile = (String) file.getAttribute("originalFile"); // NOI18N
        if (originalFile != null) {
            FileObject redirect = FileUtil.getConfigFile(originalFile);
            if (redirect != null) {
                file = redirect;
            } else {
                LOG.log(Level.INFO, "Not found original file {0} in file {1}", new Object[]{originalFile, file.getPath()}); // NOI18N
            }
        }
        String baseName = (String) file.getAttribute("extends"); // NOI18N
        if (baseName != null && baseName.length() > 0) {
            boolean find = false;
            for (FileObject base : files) {
                if (baseName.equals(base.getNameExt())) {
                    if (!read(base, files, v, antiloop, cache)) {
                        LOG.log(Level.INFO, "Cannot read base file {0}", file.getPath()); // NOI18N
                        return false;
                    }
                    find = true;
                }
            }
            if (!find) {
                LOG.log(Level.INFO, "Cannot find base file {0}", baseName); // NOI18N
            }
        }
        try {
            return read(file.getInputStream(), v, cache);
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Cannot read file {0}", file.getPath()); // NOI18N
            ex.printStackTrace(System.err);
        } catch (SAXException ex) {
            LOG.log(Level.INFO, "Wrong content of file {0}", file.getPath()); // NOI18N
            ex.printStackTrace(System.err);
        } catch (ParserConfigurationException ex) {
            LOG.log(Level.INFO, "Error in xml parser on parse file {0}", file.getPath()); // NOI18N
            ex.printStackTrace(System.err);
        }
        return false;
    }

    private boolean read2(FileObject file, FileObject[] files, CompilerVendor v, Map<String, String> cache) {
        boolean res = true;
        for (FileObject fo  : file.getChildren()){
            res &= read(fo, files, v, new HashSet<FileObject>(), cache);
        }
        return res;
    }

    private boolean read(InputStream inputStream, CompilerVendor v, Map<String, String> cache) throws SAXException, ParserConfigurationException, IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        SAXParser saxParser = spf.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        SAXHandler handler = new SAXHandler(v, cache);
        xmlReader.setContentHandler(handler);
        InputSource source = new InputSource(inputStream);
        xmlReader.parse(source);
        return true;
    }

    private String unsplit(String[] array) {
        if (array == null) {
            return ""; // NOI18N
        }
        StringBuilder buf = new StringBuilder();
        for (String s : array) {
            if (buf.length() > 0) {
                buf.append(',');
            }
            buf.append(s);
        }
        return buf.toString();
    }

    /**
     * available in package for testing only
     */
    /*test*/public void writeToolchains() throws IOException {
        FileObject folder = FileUtil.getConfigFile(ROOT_FOLDER);
        FileObject shadow = folder.getFileObject(SHADOW_FOLDER);
        if (shadow == null) {
            shadow = folder.createFolder(SHADOW_FOLDER);
        }
        if (shadow != null && shadow.isFolder()) {
            for (ToolchainDescriptor descriptor : descriptors) {
                String name = descriptor.getFileName();
                FileObject file = shadow.getFileObject(name);
                if (file == null) {
                    file = shadow.createData(name);
                }
                writeDescriptor(descriptor, file);
                ToolchainDescriptorImpl impl = (ToolchainDescriptorImpl) descriptor;
                file.setAttribute("position", Integer.valueOf(impl.v.position)); // NOI18N
                file.setAttribute("extends", ""); // NOI18N
                file.setAttribute("displayName", impl.v.toolChainDisplay); // NOI18N
            }
        }
    }

    private void writeDescriptor(ToolchainDescriptor descriptor, FileObject file) throws DOMException {
        //System.err.println("Found file " + file.getNameExt()); // NOI18N
        Document doc = XMLUtil.createDocument("toolchaindefinition", "http://www.netbeans.org/ns/cnd-toolchain-definition/1", null, null); // NOI18N
        Element root = doc.getDocumentElement();
        Element element;
        element = doc.createElement("toolchain"); // NOI18N
        element.setAttribute("name", descriptor.getName()); // NOI18N
        element.setAttribute("display", descriptor.getDisplayName()); // NOI18N
        element.setAttribute("family", unsplit(descriptor.getFamily())); // NOI18N
        if (descriptor.getQmakeSpec() != null) {
            element.setAttribute("qmakespec", descriptor.getQmakeSpec()); // NOI18N
        }
        if (descriptor.isAbstract()) {
            element.setAttribute("abstract", "true"); // NOI18N
        }
        if (!descriptor.isAutoDetected()) {
            element.setAttribute("auto_detected", "false"); // NOI18N
        }
        if (descriptor.getAliases().length > 0) {
            element.setAttribute("aliases", unsplit(descriptor.getAliases())); // NOI18N
        }
        if (descriptor.getSubstitute() != null) {
            element.setAttribute("substitute", descriptor.getSubstitute()); // NOI18N
        }
        root.appendChild(element);
        if (descriptor.getUpdateCenterUrl() != null && descriptor.getModuleID() != null) {
            element = doc.createElement("download"); // NOI18N
            element.setAttribute("uc_url", descriptor.getUpdateCenterUrl()); // NOI18N
            element.setAttribute("module_id", descriptor.getModuleID()); // NOI18N
            element.setAttribute("uc_display", descriptor.getUpdateCenterDisplayName()); // NOI18N
            element.setAttribute("upgrade_url", descriptor.getUpgradeUrl()); // NOI18N
            root.appendChild(element);
        }
        element = doc.createElement("platforms"); // NOI18N
        element.setAttribute("stringvalue", unsplit(descriptor.getPlatforms())); // NOI18N
        root.appendChild(element);
        if (descriptor.getDriveLetterPrefix() != null) {
            element = doc.createElement("drive_letter_prefix"); // NOI18N
            element.setAttribute("stringvalue", descriptor.getDriveLetterPrefix()); // NOI18N
            root.appendChild(element);
        }
        if (descriptor.getMakefileWriter() != null) {
            element = doc.createElement("makefile_writer"); // NOI18N
            element.setAttribute("class", descriptor.getMakefileWriter()); // NOI18N
            root.appendChild(element);
        }
        if (descriptor.getBaseFolders() != null) {
            element = doc.createElement("base_folders"); // NOI18N
            root.appendChild(element);
            for (BaseFolder info : descriptor.getBaseFolders()) {
                Element p = doc.createElement("base_folder"); // NOI18N
                if (info.getFolderKey() != null) {
                    p.setAttribute("regestry", info.getFolderKey()); // NOI18N
                }
                if (info.getFolderPattern() != null) {
                    p.setAttribute("pattern", info.getFolderPattern()); // NOI18N
                }
                if (info.getFolderPathPattern() != null) {
                    p.setAttribute("path_patern", info.getFolderPathPattern()); // NOI18N
                }
                if (info.getFolderSuffix() != null) {
                    p.setAttribute("suffix", info.getFolderSuffix()); // NOI18N
                }
                element.appendChild(p);
            }
        }
        if (descriptor.getCommandFolders() != null) {
            element = doc.createElement("command_folders"); // NOI18N
            root.appendChild(element);
            for (BaseFolder info : descriptor.getCommandFolders()) {
                Element p = doc.createElement("command_folder"); // NOI18N
                if (info.getFolderKey() != null) {
                    p.setAttribute("regestry", info.getFolderKey()); // NOI18N
                }
                if (info.getFolderPattern() != null) {
                    p.setAttribute("pattern", info.getFolderPattern()); // NOI18N
                }
                if (info.getFolderPathPattern() != null) {
                    p.setAttribute("path_patern", info.getFolderPathPattern()); // NOI18N
                }
                if (info.getFolderSuffix() != null) {
                    p.setAttribute("suffix", info.getFolderSuffix()); // NOI18N
                }
                if (info.getRelativePath() != null) {
                    p.setAttribute("relative_path", info.getRelativePath()); // NOI18N
                }
                element.appendChild(p);
            }
        }
        if (descriptor.getDefaultLocations() != null) {
            element = doc.createElement("default_locations"); // NOI18N
            root.appendChild(element);
            for (Map.Entry<String, List<String>> e : descriptor.getDefaultLocations().entrySet()) {
                for (String st : e.getValue()) {
                    Element p = doc.createElement("platform"); // NOI18N
                    p.setAttribute("os", e.getKey()); // NOI18N
                    p.setAttribute("directory", st); // NOI18N
                    element.appendChild(p);
                }
            }
        }
        CompilerDescriptor compiler;
        compiler = descriptor.getC();
        if (compiler != null) {
            element = doc.createElement("c"); // NOI18N
            writeCompiler(doc, element, compiler);
            root.appendChild(element);
        }
        compiler = descriptor.getCpp();
        if (compiler != null) {
            element = doc.createElement("cpp"); // NOI18N
            writeCompiler(doc, element, compiler);
            root.appendChild(element);
        }
        compiler = descriptor.getFortran();
        if (compiler != null) {
            element = doc.createElement("fortran"); // NOI18N
            writeCompiler(doc, element, compiler);
            root.appendChild(element);
        }
        compiler = descriptor.getAssembler();
        if (compiler != null) {
            element = doc.createElement("assembler"); // NOI18N
            writeCompiler(doc, element, compiler);
            root.appendChild(element);
        }
        ScannerDescriptor scanner = descriptor.getScanner();
        if (scanner != null) {
            element = doc.createElement("scanner"); // NOI18N
            element.setAttribute("id", scanner.getID()); // NOI18N
            writeScanner(doc, element, scanner);
            root.appendChild(element);
        }
        LinkerDescriptor linker = descriptor.getLinker();
        if (linker != null) {
            element = doc.createElement("linker"); // NOI18N
            writeLinker(doc, element, linker);
            root.appendChild(element);
        }
        MakeDescriptor make = descriptor.getMake();
        if (make != null) {
            element = doc.createElement("make"); // NOI18N
            writeMake(doc, element, make);
            root.appendChild(element);
        }
        DebuggerDescriptor debugger = descriptor.getDebugger();
        if (debugger != null) {
            element = doc.createElement("debugger"); // NOI18N
            element.setAttribute("id", debugger.getID()); // NOI18N
            writeDebugger(doc, element, debugger);
            root.appendChild(element);
        }
        QMakeDescriptor qmake = descriptor.getQMake();
        if (qmake != null) {
            element = doc.createElement("qmake"); // NOI18N
            writeQMake(doc, element, qmake);
            root.appendChild(element);
        }
        CMakeDescriptor cmake = descriptor.getCMake();
        if (cmake != null) {
            element = doc.createElement("cmake"); // NOI18N
            writeCMake(doc, element, cmake);
            root.appendChild(element);
        }
        try {
            FileLock lock = file.lock();
            try {
                OutputStream os = file.getOutputStream(lock);
                try {
                    XMLUtil.write(doc, os, "UTF-8"); // NOI18N
                } finally {
                    os.close();
                }
            } finally {
                lock.releaseLock();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void writeCompiler(Document doc, Element element, CompilerDescriptor compiler) {
        Element e;
        e = doc.createElement("compiler"); // NOI18N
        e.setAttribute("name", unsplit(compiler.getNames())); // NOI18N
        if (compiler.skipSearch()) {
            e.setAttribute("skip", "true"); // NOI18N
        }
        element.appendChild(e);
        if (compiler.getPathPattern() != null ||
                compiler.getExistFolder() != null) {
            e = doc.createElement("recognizer"); // NOI18N
            if (compiler.getPathPattern() != null) {
                e.setAttribute("pattern", compiler.getPathPattern()); // NOI18N
            }
            if (compiler.getExistFolder() != null) {
                e.setAttribute("or_exist_folder", compiler.getExistFolder()); // NOI18N
            }
            element.appendChild(e);
        }
        if (compiler.getVersionFlags() != null ||
                compiler.getVersionPattern() != null) {
            e = doc.createElement("version"); // NOI18N
            if (compiler.getVersionFlags() != null) {
                e.setAttribute("flags", compiler.getVersionFlags()); // NOI18N
            }
            if (compiler.getVersionPattern() != null) {
                e.setAttribute("pattern", compiler.getVersionPattern()); // NOI18N
            }
            if (compiler.getFingerPrintFlags() != null) {
                e.setAttribute("fingerprint_flags", compiler.getFingerPrintFlags()); // NOI18N
            }
            if (compiler.getFingerPrintPattern() != null) {
                e.setAttribute("fingerprint_pattern", compiler.getFingerPrintPattern()); // NOI18N
            }
            element.appendChild(e);
        }
        writeAlternativePath(doc, element, compiler);
        if (compiler.getIncludeFlags() != null ||
                compiler.getIncludeParser() != null ||
                compiler.getRemoveIncludePathPrefix() != null ||
                compiler.getRemoveIncludeOutputPrefix() != null) {
            e = doc.createElement("system_include_paths"); // NOI18N
            if (compiler.getIncludeFlags() != null) {
                e.setAttribute("flags", compiler.getIncludeFlags()); // NOI18N
            }
            if (compiler.getIncludeParser() != null) {
                e.setAttribute("parser", compiler.getIncludeParser()); // NOI18N
            }
            if (compiler.getRemoveIncludePathPrefix() != null) {
                e.setAttribute("remove_in_path", compiler.getRemoveIncludePathPrefix()); // NOI18N
            }
            if (compiler.getRemoveIncludeOutputPrefix() != null) {
                e.setAttribute("remove_in_output", compiler.getRemoveIncludeOutputPrefix()); // NOI18N
            }
            element.appendChild(e);
        }
        if (compiler.getImportantFlags() != null) {
            e = doc.createElement("important_flags"); // NOI18N
            e.setAttribute("flags", compiler.getImportantFlags()); // NOI18N
            element.appendChild(e);
        }
        if (compiler.getMacroFlags() != null ||
                compiler.getMacroParser() != null ||
                compiler.getPredefinedMacros() != null) {
            e = doc.createElement("system_macros"); // NOI18N
            if (compiler.getMacroFlags() != null) {
                e.setAttribute("flags", compiler.getMacroFlags()); // NOI18N
            }
            if (compiler.getMacroParser() != null) {
                e.setAttribute("parser", compiler.getMacroParser()); // NOI18N
            }
            element.appendChild(e);
            if (compiler.getPredefinedMacros() != null) {
                for(PredefinedMacro p : compiler.getPredefinedMacros()){
                    Element ee = doc.createElement("macro"); // NOI18N
                    ee.setAttribute("stringvalue", p.getMacro()); // NOI18N
                    if (p.getFlags() != null) {
                        ee.setAttribute("flags", p.getFlags()); // NOI18N
                    }
                    if (p.isHidden()) {
                        ee.setAttribute("hide", "true"); // NOI18N
                    }
                    e.appendChild(ee);
                }
            }
        }
        if (compiler.getUserIncludeFlag() != null) {
            e = doc.createElement("user_include"); // NOI18N
            e.setAttribute("flags", compiler.getUserIncludeFlag()); // NOI18N
            element.appendChild(e);
        }
        if (compiler.getUserFileFlag() != null) {
            e = doc.createElement("user_file"); // NOI18N
            e.setAttribute("flags", compiler.getUserFileFlag()); // NOI18N
            element.appendChild(e);
        }
        if (compiler.getUserMacroFlag() != null) {
            e = doc.createElement("user_macro"); // NOI18N
            e.setAttribute("flags", compiler.getUserMacroFlag()); // NOI18N
            element.appendChild(e);
        }
        writeDevelopmentMode(doc, element, compiler);
        writeWarningLevel(doc, element, compiler);
        writeArchitecture(doc, element, compiler);
        if (compiler.getStripFlag() != null) {
            e = doc.createElement("strip"); // NOI18N
            e.setAttribute("flags", compiler.getStripFlag()); // NOI18N
            element.appendChild(e);
        }
        writeMultithreading(doc, element, compiler);
        writeStandard(doc, element, compiler);
        writeLanguageExtension(doc, element, compiler);
        writeCppStandard(doc, element, compiler);
        writeCStandard(doc, element, compiler);
        writeLibrary(doc, element, compiler);
        if (compiler.getOutputObjectFileFlags() != null) {
            e = doc.createElement("output_object_file"); // NOI18N
            e.setAttribute("flags", compiler.getOutputObjectFileFlags()); // NOI18N
            element.appendChild(e);
        }
        if (compiler.getDependencyGenerationFlags() != null) {
            e = doc.createElement("dependency_generation"); // NOI18N
            e.setAttribute("flags", compiler.getDependencyGenerationFlags()); // NOI18N
            element.appendChild(e);
        }
        if (compiler.getPrecompiledHeaderFlags() != null ||
                compiler.getPrecompiledHeaderSuffix() != null) {
            e = doc.createElement("precompiled_header"); // NOI18N
            if (compiler.getPrecompiledHeaderFlags() != null) {
                e.setAttribute("flags", compiler.getPrecompiledHeaderFlags()); // NOI18N
            }
            if (compiler.getPrecompiledHeaderSuffix() != null) {
                e.setAttribute("suffix", compiler.getPrecompiledHeaderSuffix()); // NOI18N
            }
            if (compiler.getPrecompiledHeaderSuffixAppend()) {
                e.setAttribute("append", "true"); // NOI18N
            }
            element.appendChild(e);
        }
    }

    private void writeDevelopmentMode(Document doc, Element element, CompilerDescriptor compiler) {
        String[] flags = compiler.getDevelopmentModeFlags();
        if (flags == null) {
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl) compiler).tool.developmentMode.default_selection;
        }
        Element e = doc.createElement("development_mode"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"fast_build", "debug", "performance_debug", // NOI18N
            "test_coverage", "diagnosable_release", "release", // NOI18N
            "performance_release"}; // NOI18N
        for (int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i) {
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeWarningLevel(Document doc, Element element, CompilerDescriptor compiler) {
        String[] flags = compiler.getWarningLevelFlags();
        if (flags == null) {
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl) compiler).tool.warningLevel.default_selection;
        }
        Element e = doc.createElement("warning_level"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"no_warnings", "default", "more_warnings", // NOI18N
            "warning2error"}; // NOI18N
        for (int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i) {
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeArchitecture(Document doc, Element element, CompilerDescriptor compiler) {
        String[] flags = compiler.getArchitectureFlags();
        if (flags == null) {
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl) compiler).tool.architecture.default_selection;
        }
        Element e = doc.createElement("architecture"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"default", "bits_32", "bits_64"}; // NOI18N
        for (int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i) {
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeMultithreading(Document doc, Element element, CompilerDescriptor compiler) {
        String[] flags = compiler.getMultithreadingFlags();
        if (flags == null) {
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl) compiler).tool.multithreading.default_selection;
        }
        Element e = doc.createElement("multithreading"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"none", "safe", "automatic", "open_mp"}; // NOI18N
        for (int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i) {
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeStandard(Document doc, Element element, CompilerDescriptor compiler) {
        String[] flags = compiler.getStandardFlags();
        if (flags == null) {
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl) compiler).tool.standard.default_selection;
        }
        Element e = doc.createElement("standard"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"old", "legacy", "default", "modern"}; // NOI18N
        for (int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i) {
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeLanguageExtension(Document doc, Element element, CompilerDescriptor compiler) {
        String[] flags = compiler.getLanguageExtensionFlags();
        if (flags == null) {
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl) compiler).tool.languageExtension.default_selection;
        }
        Element e = doc.createElement("language_extension"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"none", "default", "all"}; // NOI18N
        for (int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i) {
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeCppStandard(Document doc, Element element, CompilerDescriptor compiler) {
        String[] flags = compiler.getCppStandardFlags();
        if (flags == null) {
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl) compiler).tool.cppStandard.default_selection;
        }
        Element e = doc.createElement("cpp_standard"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"default", "cpp98", "cpp11", "cpp14", "cpp17", "cpp20", "cpp23"}; // NOI18N
        for (int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i) {
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeCStandard(Document doc, Element element, CompilerDescriptor compiler) {
        String[] flags = compiler.getCStandardFlags();
        if (flags == null) {
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl) compiler).tool.cStandard.default_selection;
        }
        Element e = doc.createElement("c_standard"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"default", "c89", "c99", "c11", "c17", "c23"}; // NOI18N
        for (int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i) {
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeLibrary(Document doc, Element element, CompilerDescriptor compiler) {
        String[] flags = compiler.getLibraryFlags();
        if (flags == null) {
            return;
        }
        int def = 0;
        if (compiler instanceof CompilerDescriptorImpl) {
            def = ((CompilerDescriptorImpl) compiler).tool.library.default_selection;
        }
        Element e = doc.createElement("library"); // NOI18N
        element.appendChild(e);
        Element c;
        String[] names = new String[]{"none", "runtime", "classic", // NOI18N
            "binary_standard", "conforming_standard"}; // NOI18N
        for (int i = 0; i < flags.length; i++) {
            c = doc.createElement(names[i]);
            c.setAttribute("flags", flags[i]); // NOI18N
            if (def == i) {
                c.setAttribute("default", "true"); // NOI18N
            }
            e.appendChild(c);
        }
    }

    private void writeScanner(Document doc, Element element, ScannerDescriptor scanner) {
        Element c;
        for (ScannerPattern pattern : scanner.getPatterns()) {
            c = doc.createElement("error"); // NOI18N
            c.setAttribute("pattern", pattern.getPattern()); // NOI18N
            if (pattern.getSeverity() != null) {
                c.setAttribute("severity", pattern.getSeverity()); // NOI18N
            }
            if (pattern.getLanguage() != null) {
                c.setAttribute("language", pattern.getLanguage()); // NOI18N
            }
            element.appendChild(c);
        }
        for(String pattern : scanner.getStackHeaderPattern()) {
            c = doc.createElement("stack_header"); // NOI18N
            c.setAttribute("pattern", pattern); // NOI18N
            element.appendChild(c);
        }
        for(String pattern : scanner.getStackNextPattern()) {
            c = doc.createElement("stack_next"); // NOI18N
            c.setAttribute("pattern", pattern); // NOI18N
            element.appendChild(c);
        }
        if (scanner.getEnterDirectoryPattern() != null) {
            c = doc.createElement("enter_directory"); // NOI18N
            c.setAttribute("pattern", scanner.getEnterDirectoryPattern()); // NOI18N
            element.appendChild(c);
        }
        if (scanner.getChangeDirectoryPattern() != null) {
            c = doc.createElement("change_directory"); // NOI18N
            c.setAttribute("pattern", scanner.getChangeDirectoryPattern()); // NOI18N
            element.appendChild(c);
        }
        if (scanner.getMakeAllInDirectoryPattern() != null) {
            c = doc.createElement("making_all_in_directory"); // NOI18N
            c.setAttribute("pattern", scanner.getMakeAllInDirectoryPattern()); // NOI18N
            element.appendChild(c);
        }
        if (scanner.getLeaveDirectoryPattern() != null) {
            c = doc.createElement("leave_directory"); // NOI18N
            c.setAttribute("pattern", scanner.getLeaveDirectoryPattern()); // NOI18N
            element.appendChild(c);
        }
        for (String pattern : scanner.getFilterOutPatterns()) {
            c = doc.createElement("filter_out"); // NOI18N
            c.setAttribute("pattern", pattern); // NOI18N
            element.appendChild(c);
        }
    }

    private void writeLinker(Document doc, Element element, LinkerDescriptor linker) {
        Element c;
        if (linker.getLibraryPrefix() != null) {
            c = doc.createElement("library_prefix"); // NOI18N
            c.setAttribute("stringvalue", linker.getLibraryPrefix()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getLibrarySearchFlag() != null) {
            c = doc.createElement("library_search"); // NOI18N
            c.setAttribute("flags", linker.getLibrarySearchFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getDynamicLibrarySearchFlag() != null) {
            c = doc.createElement("dynamic_library_search"); // NOI18N
            c.setAttribute("flags", linker.getDynamicLibrarySearchFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getLibraryFlag() != null) {
            c = doc.createElement("library_flag"); // NOI18N
            c.setAttribute("flags", linker.getLibraryFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getPICFlag() != null) {
            c = doc.createElement("PIC"); // NOI18N
            c.setAttribute("flags", linker.getPICFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getStaticLibraryFlag() != null) {
            c = doc.createElement("static_library"); // NOI18N
            c.setAttribute("flags", linker.getStaticLibraryFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getDynamicLibraryFlag() != null) {
            c = doc.createElement("dynamic_library"); // NOI18N
            c.setAttribute("flags", linker.getDynamicLibraryFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getDynamicLibraryBasicFlag() != null) {
            c = doc.createElement("dynamic_library_basic"); // NOI18N
            c.setAttribute("flags", linker.getDynamicLibraryBasicFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getOutputFileFlag() != null) {
            c = doc.createElement("output_file"); // NOI18N
            c.setAttribute("flags", linker.getOutputFileFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getStripFlag() != null) {
            c = doc.createElement("strip_flag"); // NOI18N
            c.setAttribute("flags", linker.getStripFlag()); // NOI18N
            element.appendChild(c);
        }
        if (linker.getPreferredCompiler() != null) {
            c = doc.createElement("preferred_compiler"); // NOI18N
            c.setAttribute("compiler", linker.getPreferredCompiler()); // NOI18N
            element.appendChild(c);
        }
    }

    private void writeMake(Document doc, Element element, MakeDescriptor make) {
        Element c;
        c = doc.createElement("tool"); // NOI18N
        c.setAttribute("name", unsplit(make.getNames())); // NOI18N
        if (make.skipSearch()) {
            c.setAttribute("skip", "true"); // NOI18N
        }
        element.appendChild(c);

        if (make.getVersionFlags() != null ||
                make.getVersionPattern() != null) {
            c = doc.createElement("version"); // NOI18N
            if (make.getVersionFlags() != null) {
                c.setAttribute("flags", make.getVersionFlags()); // NOI18N
            }
            if (make.getVersionPattern() != null) {
                c.setAttribute("pattern", make.getVersionPattern()); // NOI18N
            }
            element.appendChild(c);
        }
        writeAlternativePath(doc, element, make);
        if (make.getDependencySupportCode() != null) {
            c = doc.createElement("dependency_support"); // NOI18N
            c.setAttribute("code", make.getDependencySupportCode()); // NOI18N
            element.appendChild(c);
        }
    }

    private void writeDebugger(Document doc, Element element, DebuggerDescriptor debugger) {
        Element c;
        c = doc.createElement("tool"); // NOI18N
        c.setAttribute("name", unsplit(debugger.getNames())); // NOI18N
        if (debugger.skipSearch()) {
            c.setAttribute("skip", "true"); // NOI18N
        }
        element.appendChild(c);
        if (debugger.getVersionFlags() != null ||
                debugger.getVersionPattern() != null) {
            c = doc.createElement("version"); // NOI18N
            if (debugger.getVersionFlags() != null) {
                c.setAttribute("flags", debugger.getVersionFlags()); // NOI18N
            }
            if (debugger.getVersionPattern() != null) {
                c.setAttribute("pattern", debugger.getVersionPattern()); // NOI18N
            }
            element.appendChild(c);
        }
        writeAlternativePath(doc, element, debugger);
    }

    private void writeQMake(Document doc, Element element, QMakeDescriptor qmake) {
        Element c;
        c = doc.createElement("tool"); // NOI18N
        c.setAttribute("name", unsplit(qmake.getNames())); // NOI18N
        if (qmake.skipSearch()) {
            c.setAttribute("skip", "true"); // NOI18N
        }
        element.appendChild(c);
        if (qmake.getVersionFlags() != null ||
                qmake.getVersionPattern() != null) {
            c = doc.createElement("version"); // NOI18N
            if (qmake.getVersionFlags() != null) {
                c.setAttribute("flags", qmake.getVersionFlags()); // NOI18N
            }
            if (qmake.getVersionPattern() != null) {
                c.setAttribute("pattern", qmake.getVersionPattern()); // NOI18N
            }
            element.appendChild(c);
        }
        writeAlternativePath(doc, element, qmake);
    }

    private void writeCMake(Document doc, Element element, CMakeDescriptor cmake) {
        Element c;
        c = doc.createElement("tool"); // NOI18N
        c.setAttribute("name", unsplit(cmake.getNames())); // NOI18N
        if (cmake.skipSearch()) {
            c.setAttribute("skip", "true"); // NOI18N
        }
        element.appendChild(c);
        if (cmake.getVersionFlags() != null ||
                cmake.getVersionPattern() != null) {
            c = doc.createElement("version"); // NOI18N
            if (cmake.getVersionFlags() != null) {
                c.setAttribute("flags", cmake.getVersionFlags()); // NOI18N
            }
            if (cmake.getVersionPattern() != null) {
                c.setAttribute("pattern", cmake.getVersionPattern()); // NOI18N
            }
            element.appendChild(c);
        }
        writeAlternativePath(doc, element, cmake);
    }

    private void writeAlternativePath(Document doc, Element element, ToolDescriptor tool){
        AlternativePath[] paths = tool.getAlternativePath();
        if (paths != null) {
            Element c = doc.createElement("alternative_path"); // NOI18N
            element.appendChild(c);
            for(AlternativePath path : paths){
                Element p = doc.createElement("path"); // NOI18N
                c.appendChild(p);
                switch(path.getKind()){
                    case PATH:
                        p.setAttribute("directory", path.getPath()); // NOI18N
                        break;
                    case TOOL_FAMILY:
                        p.setAttribute("toolchain_family", path.getPath()); // NOI18N
                        break;
                    case TOOL_NAME:
                        p.setAttribute("toolchain_name", path.getPath()); // NOI18N
                        break;
                }

            }
        }
    }

    /**
     * class package-local for testing only
     */
    static final class CompilerVendor {

        final String toolChainFileName;
        final int position;
        String toolChainName;
        String toolChainDisplay;
        Map<String, List<String>> default_locations;
        String family;
        String platforms;
        String uc;
        String ucName;
        String upgrage;
        String module;
        String aliases;
        String substitute;
        boolean isAbstract;
        boolean isAutoDetected;
        String driveLetterPrefix;
        List<FolderInfo> baseFolder;
        List<FolderInfo> commandFolder;
        String qmakespec;
        String makefileWriter;
        final Compiler c = new Compiler();
        final Compiler cpp = new Compiler();
        final Compiler fortran = new Compiler();
        final Compiler assembler = new Compiler();
        Scanner scanner = new Scanner();
        final Linker linker = new Linker();
        final Make make = new Make();
        final Debugger debugger = new Debugger();
        final QMake qmake = new QMake();
        final CMake cmake = new CMake();

        private CompilerVendor(String fileName, int position) {
            toolChainFileName = fileName;
            this.position = position;
        }

        public boolean isValid() {
            return toolChainName != null && toolChainName.length() > 0 &&
                    (c.isValid() || cpp.isValid() || fortran.isValid());
        }
    }

    static final class FolderInfo {
        String folderKey;
        String folderPattern;
        String folderSuffix;
        String folderPathPattern;
        String relativePath;
    }

    /**
     * class package-local for testing only
     */
    public static class Tool {

        String name;
        String versionFlags;
        String versionPattern;
        String fingerprintFlags;
        String fingerprintPattern;
        boolean skipSearch;
        List<AlternativePath> alternativePath;
    }

    static class Alternative implements AlternativePath {
        final String path;
        final AlternativePath.PathKind kind;
        Alternative(String path, AlternativePath.PathKind kind){
            this.path = path;
            this.kind = kind;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public PathKind getKind() {
            return kind;
        }
    }

    /**
     * class package-local for testing only
     */
    static public final class Compiler extends Tool {

        String pathPattern;
        String existFolder;
        String includeFlags;
        String includeOutputParser;
        String removeIncludePathPrefix;
        String removeIncludeOutputPrefix;
        String userIncludeFlag;
        String userFileFlag;
        String importantFlags;
        String macrosFlags;
        String macrosOutputParser;
        String userMacroFlag;
        List<PredefinedMacro> predefinedMacros;
        String outputObjectFileFlags;
        String dependencyGenerationFlags;
        String precompiledHeaderFlags;
        String precompiledHeaderSuffix;
        boolean precompiledHeaderSuffixAppend;
        final DevelopmentMode developmentMode = new DevelopmentMode();
        final WarningLevel warningLevel = new WarningLevel();
        final Architecture architecture = new Architecture();
        String strip;
        final MultiThreading multithreading = new MultiThreading();
        final Standard standard = new Standard();
        final LanguageExtension languageExtension = new LanguageExtension();
        final CppStandard cppStandard = new CppStandard();
        final CStandard cStandard = new CStandard();
        final Library library = new Library();

        public boolean isValid() {
            return name != null && name.length() > 0;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class Scanner {

        String id;
        final List<ErrorPattern> patterns = new ArrayList<ErrorPattern>();
        String changeDirectoryPattern;
        String enterDirectoryPattern;
        String leaveDirectoryPattern;
        String makingAllInDirectoryPattern;
        List<String> stackHeaderPattern = new ArrayList<String>();
        List<String> stackNextPattern = new ArrayList<String>();
        final List<String> filterOut = new ArrayList<String>();
    }

    /**
     * class package-local for testing only
     */
    static final class ErrorPattern {

        String pattern;
        String severity;
        String language;
    }

    /**
     * class package-local for testing only
     */
    static final class Linker {

        String library_prefix;
        String librarySearchFlag;
        String dynamicLibrarySearchFlag;
        String libraryFlag;
        String PICFlag;
        String staticLibraryFlag;
        String dynamicLibraryFlag;
        String dynamicLibraryBasicFlag;
        String outputFileFlag;
        String stripFlag;
        String preferredCompiler;
    }

    /**
     * class package-local for testing only
     */
    static final class Make extends Tool {

        String dependencySupportCode;
    }

    /**
     * class package-local for testing only
     */
    static final class Debugger extends Tool {
        String id;
    }

    /**
     * class package-local for testing only
     */
    static final class QMake extends Tool {
    }

    /**
     * class package-local for testing only
     */
    static final class CMake extends Tool {
    }

    /**
     * class package-local for testing only
     */
    static final class DevelopmentMode {

        String fast_build;
        String debug;
        String performance_debug;
        String test_coverage;
        String diagnosable_release;
        String release;
        String performance_release;
        int default_selection = 0;

        public boolean isValid() {
            return fast_build != null && debug != null && performance_debug != null && test_coverage != null &&
                    diagnosable_release != null && release != null && performance_release != null;
        }

        public String[] values() {
            if (isValid()) {
                return new String[]{fast_build, debug, performance_debug, test_coverage,
                            diagnosable_release, release, performance_release};
            }
            return null;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class WarningLevel {

        String no_warnings;
        String default_level;
        String more_warnings;
        String warning2error;
        int default_selection = 0;

        public boolean isValid() {
            return no_warnings != null && default_level != null && more_warnings != null && warning2error != null;
        }

        public String[] values() {
            if (isValid()) {
                return new String[]{no_warnings, default_level, more_warnings, warning2error};
            }
            return null;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class Architecture {

        String default_architecture;
        String bits_32;
        String bits_64;
        int default_selection = 0;

        public boolean isValid() {
            return default_architecture != null && bits_32 != null && bits_64 != null;
        }

        public String[] values() {
            if (isValid()) {
                return new String[]{default_architecture, bits_32, bits_64};
            }
            return null;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class MultiThreading {

        String none;
        String safe;
        String automatic;
        String open_mp;
        int default_selection = 0;

        public boolean isValid() {
            return none != null && safe != null && automatic != null && open_mp != null;
        }

        public String[] values() {
            if (isValid()) {
                return new String[]{none, safe, automatic, open_mp};
            }
            return null;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class Standard {

        String old;
        String legacy;
        String default_standard;
        String modern;
        int default_selection = 0;

        public boolean isValid() {
            return old != null && legacy != null && default_standard != null && modern != null;
        }

        public String[] values() {
            if (isValid()) {
                return new String[]{old, legacy, default_standard, modern};
            }
            return null;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class CppStandard {

        String cppDefault;
        String cpp98;
        String cpp11;
        String cpp14;
        String cpp17;
        String cpp20;
        String cpp23;
        int default_selection = 0;

        public boolean isValid() {
            return cppDefault != null && cpp98 != null && cpp11 != null && cpp14 != null && cpp17 != null && cpp20 != null && cpp23 != null;
        }

        public String[] values() {
            if (isValid()) {
                return new String[]{cppDefault, cpp98, cpp11, cpp14, cpp17, cpp20, cpp23};
            } else if (cppDefault != null && cpp98 != null && cpp11 != null && cpp14 != null && cpp17 != null && cpp20 != null) {
                return new String[]{cppDefault, cpp98, cpp11, cpp14, cpp17, cpp20};
            } else if (cppDefault != null && cpp98 != null && cpp11 != null && cpp14 != null && cpp17 != null) {
                return new String[]{cppDefault, cpp98, cpp11, cpp14, cpp17};
            } else if (cppDefault != null && cpp98 != null && cpp11 != null && cpp14 != null) {
                return new String[]{cppDefault, cpp98, cpp11, cpp14};
            } else if (cppDefault != null && cpp98 != null && cpp11 != null) {
                return new String[]{cppDefault, cpp98, cpp11};
            }
            return null;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class CStandard {

        String cDefault;
        String c89;
        String c99;
        String c11;
        String c17;
        String c23;
        int default_selection = 0;

        public boolean isValid() {
            return cDefault != null && c89 != null && c99 != null && c11 != null && c17 != null && c23 != null;
        }

        public String[] values() {
            if (isValid()) {
                return new String[]{cDefault, c89, c99, c11, c17, c23};
            } else if (cDefault != null && c89 != null && c99 != null && c11 != null && c17 != null) {
                return new String[]{cDefault, c89, c99, c11, c17};
            } else if (cDefault != null && c89 != null && c99 != null && c11 != null) {
                return new String[]{cDefault, c89, c99, c11};
            } else if (cDefault != null && c89 != null && c99 != null) {
                return new String[]{cDefault, c89, c99};
            }
            return null;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class LanguageExtension {

        String none;
        String default_extension;
        String all;
        int default_selection = 0;

        public boolean isValid() {
            return none != null && default_extension != null && all != null;
        }

        public String[] values() {
            if (isValid()) {
                return new String[]{none, default_extension, all};
            }
            return null;
        }
    }

    /**
     * class package-local for testing only
     */
    static final class Library {

        String none;
        String runtime;
        String classic;
        String binary_standard;
        String conforming_standard;
        int default_selection = 0;

        public boolean isValid() {
            return none != null && runtime != null && classic != null && binary_standard != null && conforming_standard != null;
        }

        public String[] values() {
            if (isValid()) {
                return new String[]{none, runtime, classic, binary_standard, conforming_standard};
            }
            return null;
        }
    }

    private static final class SAXHandler extends DefaultHandler {

        private String path;
        private final CompilerVendor v;
        private boolean isScanerOverrided = false;
        private int version = 1;
        private final Map<String, String> cache;

        private SAXHandler(CompilerVendor v, Map<String, String> cache) {
            this.v = v;
            this.cache = cache;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (path != null) {
                if (path.equals(qName)) {
                    path = null;
                } else {
                    path = path.substring(0, path.length() - qName.length() - 1);
                }
            }
        }

        private String getValue(org.xml.sax.Attributes attributes, String key){
            String res = attributes.getValue(key);
            if (res != null) {
                String c = cache.get(res);
                if (c == null){
                    cache.put(res, res);
                } else {
                    res = c;
                }
            }
            return res;
        }

        private String getValue(String res){
            String c = cache.get(res);
            if (c == null){
                cache.put(res, res);
            } else {
                res = c;
            }
            return res;
        }

        @Override
        public void startElement(String uri, String lname, String name, org.xml.sax.Attributes attributes) throws SAXException {
            super.startElement(uri, lname, name, attributes);
            if (path == null) {
                path = name;
            } else {
                path += "." + name; // NOI18N
            }
            if (path.equals("toolchaindefinition")) { // NOI18N
                String xmlns = attributes.getValue("xmlns"); // NOI18N
                if (xmlns != null) {
                    int lastSlash = xmlns.lastIndexOf('/'); // NOI18N
                    if (lastSlash >= 0 && (lastSlash + 1 < xmlns.length())) {
                        String versionStr = xmlns.substring(lastSlash + 1);
                        if (versionStr.length() > 0) {
                            try {
                                version = Integer.parseInt(versionStr);
                            } catch (NumberFormatException ex) {
                                // skip
                                if (TRACE) {
                                    System.err.println("Incorrect version information:" + xmlns);
                                } // NOI18N
                            }
                        }
                    } else {
                        if (TRACE) {
                            System.err.println("Incorrect version information:" + xmlns);
                        } // NOI18N
                    }
                }
                return;
            } else if (path.endsWith(".toolchain")) { // NOI18N
                v.toolChainName = getValue(attributes, "name"); // NOI18N
                v.toolChainDisplay = getValue(attributes, "display"); // NOI18N
                v.family = getValue(attributes, "family"); // NOI18N
                v.qmakespec = getValue(attributes, "qmakespec"); // NOI18N
                v.isAbstract = "true".equals(getValue(attributes, "abstract"));// NOI18N
                v.isAutoDetected = !"false".equals(getValue(attributes, "auto_detected"));// NOI18N
                v.aliases = getValue(attributes, "aliases"); // NOI18N
                v.substitute = getValue(attributes, "substitute"); // NOI18N
                return;
            } else if (path.endsWith(".platforms")) { // NOI18N
                v.platforms = getValue(attributes, "stringvalue"); // NOI18N
                return;
            } else if (path.endsWith(".download")) { // NOI18N
                v.uc = getValue(attributes, "uc_url"); // NOI18N
                v.ucName = getValue(attributes, "uc_display"); // NOI18N
                v.module = getValue(attributes, "module_id"); // NOI18N
                v.upgrage = getValue(attributes, "upgrade_url"); // NOI18N
                return;
            } else if (path.endsWith(".drive_letter_prefix")) { // NOI18N
                v.driveLetterPrefix = getValue(attributes, "stringvalue"); // NOI18N
                return;
            } else if (path.endsWith(".makefile_writer")) { // NOI18N
                v.makefileWriter = getValue(attributes, "class"); // NOI18N
                return;
            } else if (path.endsWith(".base_folders.base_folder")) { // NOI18N
                if (v.baseFolder == null){
                    v.baseFolder = new ArrayList<FolderInfo>();
                }
                FolderInfo folder = new FolderInfo();
                v.baseFolder.add(folder);
                folder.folderKey = getValue(attributes, "regestry"); // NOI18N
                folder.folderPattern = getValue(attributes, "pattern"); // NOI18N
                folder.folderSuffix = getValue(attributes, "suffix"); // NOI18N
                folder.folderPathPattern = getValue(attributes, "path_patern"); // NOI18N
                return;
            } else if (path.endsWith(".command_folders.command_folder")) { // NOI18N
                if (v.commandFolder == null){
                    v.commandFolder = new ArrayList<FolderInfo>();
                }
                FolderInfo folder = new FolderInfo();
                v.commandFolder.add(folder);
                folder.folderKey = getValue(attributes, "regestry"); // NOI18N
                folder.folderPattern = getValue(attributes, "pattern"); // NOI18N
                folder.folderSuffix = getValue(attributes, "suffix"); // NOI18N
                folder.folderPathPattern = getValue(attributes, "path_patern"); // NOI18N
                folder.relativePath = getValue(attributes, "relative_path"); // NOI18N
                return;
            } else if (path.indexOf(".default_locations.") > 0) { // NOI18N
                if (path.endsWith(".platform")) { // NOI18N
                    String os_attr = getValue(attributes, "os"); // NOI18N
                    String dir_attr = getValue(attributes, "directory"); // NOI18N
                    if (os_attr != null && dir_attr != null) {
                        if (v.default_locations == null) {
                            v.default_locations = new HashMap<String, List<String>>();
                        }
                        List<String> value = v.default_locations.get(os_attr);
                        if (value == null){
                            value = new ArrayList<String>();
                        }
                        value.add(dir_attr);
                        v.default_locations.put(os_attr, value);
                    }
                }
                return;
            }
            if (path.indexOf(".linker.") > 0) { // NOI18N
                Linker l = v.linker;
                if (path.endsWith(".library_prefix")) { // NOI18N
                    l.library_prefix = getValue(attributes, "stringvalue"); // NOI18N
                    return;
                } else if (path.endsWith(".library_search")) { // NOI18N
                    l.librarySearchFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                } else if (path.endsWith(".dynamic_library_search")) { // NOI18N
                    l.dynamicLibrarySearchFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                } else if (path.endsWith(".library_flag")) { // NOI18N
                    l.libraryFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                } else if (path.endsWith(".PIC")) { // NOI18N
                    l.PICFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                } else if (path.endsWith(".static_library")) { // NOI18N
                    l.staticLibraryFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                } else if (path.endsWith(".dynamic_library")) { // NOI18N
                    l.dynamicLibraryFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                } else if (path.endsWith(".dynamic_library_basic")) { // NOI18N
                    l.dynamicLibraryBasicFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                } else if (path.endsWith(".output_file")) { // NOI18N
                    l.outputFileFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                } else if (path.endsWith(".strip_flag")) { // NOI18N
                    l.stripFlag = getValue(attributes, "flags"); // NOI18N
                    return;
                } else if (path.endsWith(".preferred_compiler")) { // NOI18N
                    l.preferredCompiler = getValue(attributes, "compiler"); // NOI18N
                    return;
                }
                return;
            }
            if (path.indexOf(".make.") > 0) { // NOI18N
                Make m = v.make;
                if (path.endsWith(".tool")) { // NOI18N
                    m.name = getValue(attributes, "name"); // NOI18N
                    m.skipSearch = "true".equals(getValue(attributes, "skip")); // NOI18N
                } else if (path.endsWith(".version")) { // NOI18N
                    m.versionFlags = getValue(attributes, "flags"); // NOI18N
                    m.versionPattern = getValue(attributes, "pattern"); // NOI18N
                } else if (path.endsWith(".alternative_path")) { // NOI18N
                    m.alternativePath = new ArrayList<AlternativePath>();
                } else if (path.endsWith(".dependency_support")) { // NOI18N
                    m.dependencySupportCode = getValue(getValue(attributes, "code").replace("\\n", "\n")); // NOI18N
                } else if (checkAlternativePath(attributes, m.alternativePath)) {
                }
                return;
            }
            if (path.endsWith(".debugger")) { // NOI18N
                v.debugger.id = getValue(attributes, "id"); // NOI18N
		return;
	    }
            if (path.indexOf(".debugger.") > 0) { // NOI18N
                Debugger d = v.debugger;
                if (path.endsWith(".tool")) { // NOI18N
                    d.name = getValue(attributes, "name"); // NOI18N
                    d.skipSearch = "true".equals(getValue(attributes, "skip")); // NOI18N
                } else if (path.endsWith(".version")) { // NOI18N
                    d.versionFlags = getValue(attributes, "flags"); // NOI18N
                    d.versionPattern = getValue(attributes, "pattern"); // NOI18N
                } else if (path.endsWith(".alternative_path")) { // NOI18N
                    d.alternativePath = new ArrayList<AlternativePath>();
                } else if (checkAlternativePath(attributes, d.alternativePath)) {
                }
                return;
            }
            if (path.indexOf(".qmake.") > 0) { // NOI18N
                QMake d = v.qmake;
                if (path.endsWith(".tool")) { // NOI18N
                    d.name = getValue(attributes, "name"); // NOI18N
                    d.skipSearch = "true".equals(getValue(attributes, "skip")); // NOI18N
                } else if (path.endsWith(".version")) { // NOI18N
                    d.versionFlags = getValue(attributes, "flags"); // NOI18N
                    d.versionPattern = getValue(attributes, "pattern"); // NOI18N
                } else if (path.endsWith(".alternative_path")) { // NOI18N
                    d.alternativePath = new ArrayList<AlternativePath>();
                } else if (checkAlternativePath(attributes, d.alternativePath)) {
                }
                return;
            }
            if (path.indexOf(".cmake.") > 0) { // NOI18N
                CMake d = v.cmake;
                if (path.endsWith(".tool")) { // NOI18N
                    d.name = getValue(attributes, "name"); // NOI18N
                    d.skipSearch = "true".equals(getValue(attributes, "skip")); // NOI18N
                } else if (path.endsWith(".version")) { // NOI18N
                    d.versionFlags = getValue(attributes, "flags"); // NOI18N
                    d.versionPattern = getValue(attributes, "pattern"); // NOI18N
                } else if (path.endsWith(".alternative_path")) { // NOI18N
                    d.alternativePath = new ArrayList<AlternativePath>();
                } else if (checkAlternativePath(attributes, d.alternativePath)) {
                }
                return;
            }
            if (path.endsWith(".scanner")) { // NOI18N
                if (!isScanerOverrided) {
                    v.scanner = new Scanner();
                    isScanerOverrided = true;
		    v.scanner.id = getValue(attributes, "id"); // NOI18N
                }
		return;
	    }
            if (path.indexOf(".scanner.") > 0) { // NOI18N
                Scanner s = v.scanner;
                if (path.endsWith(".error")) { // NOI18N
                    final String pattern = getValue(attributes, "pattern"); // NOI18N
                    if (pattern != null) {
                        ErrorPattern e = new ErrorPattern();
                        s.patterns.add(e);
                        e.severity = getValue(attributes, "severity"); // NOI18N
                        if (e.severity == null) {
                            e.severity = "error"; // NOI18N
                        }
                        e.pattern = pattern;
                        e.language = getValue(attributes, "language"); // NOI18N
                    }
                } else if (path.endsWith(".change_directory")) { // NOI18N
                    s.changeDirectoryPattern = getValue(attributes, "pattern"); // NOI18N
                } else if (path.endsWith(".enter_directory")) { // NOI18N
                    s.enterDirectoryPattern = getValue(attributes, "pattern"); // NOI18N
                } else if (path.endsWith(".leave_directory")) { // NOI18N
                    s.leaveDirectoryPattern = getValue(attributes, "pattern"); // NOI18N
                } else if (path.endsWith(".stack_header")) { // NOI18N
                    s.stackHeaderPattern.add(getValue(attributes, "pattern")); // NOI18N
                } else if (path.endsWith(".stack_next")) { // NOI18N
                    s.stackNextPattern.add(getValue(attributes, "pattern")); // NOI18N
                } else if (path.endsWith(".filter_out")) { // NOI18N
                    final String pattern = getValue(attributes, "pattern"); // NOI18N
                    if (pattern != null) {
                        s.filterOut.add(pattern);
                    }
                } else if (path.endsWith(".making_all_in_directory")) { // NOI18N
                    s.makingAllInDirectoryPattern = getValue(attributes, "pattern"); // NOI18N
                }
                return;
            }
            Compiler c;
            if (path.indexOf(".c.") > 0) { // NOI18N
                c = v.c;
            } else if (path.indexOf(".cpp.") > 0) { // NOI18N
                c = v.cpp;
            } else if (path.indexOf(".fortran.") > 0) { // NOI18N
                c = v.fortran;
            } else if (path.indexOf(".assembler.") > 0) { // NOI18N
                c = v.assembler;
            } else {
                return;
            }
            if (path.endsWith(".compiler")) { // NOI18N
                c.name = getValue(attributes, "name"); // NOI18N
                c.skipSearch = "true".equals(getValue(attributes, "skip")); // NOI18N
                return;
            } else if (path.endsWith(".recognizer")) { // NOI18N
                c.pathPattern = getValue(attributes, "pattern"); // NOI18N
                c.existFolder = getValue(attributes, "or_exist_folder"); // NOI18N
                return;
            } else if (path.endsWith(".version")) { // NOI18N
                c.versionPattern = getValue(attributes, "pattern"); // NOI18N
                c.versionFlags = getValue(attributes, "flags"); // NOI18N
                c.fingerprintFlags = getValue(attributes, "fingerprint_flags"); // NOI18N
                c.fingerprintPattern = getValue(attributes, "fingerprint_pattern"); // NOI18N
                return;
            } else if (path.endsWith(".alternative_path")) { // NOI18NBaseFolders
                c.alternativePath = new ArrayList<AlternativePath>();
                return;
            } else if (checkAlternativePath(attributes, c.alternativePath)) {
                return;
            } else if (path.endsWith(".system_macros.macro")) { // NOI18N
                if (c.predefinedMacros == null) {
                    c.predefinedMacros = new ArrayList<PredefinedMacro>();
                }
                PredefinedMacro m = new PredefinedMacroImpl(getValue(attributes, "stringvalue"), // NOI18N
                        getValue(attributes, "flags"), "true".equals(getValue(attributes, "hide"))); // NOI18N
                c.predefinedMacros.add(m);
                return;
            }
            String flags = getValue(attributes, "flags"); // NOI18N
            if (flags == null) {
                return;
            }
            boolean isDefault = "true".equals(getValue(attributes, "default")); // NOI18N
            if (path.endsWith(".system_include_paths")) { // NOI18N
                c.includeFlags = flags;
                c.includeOutputParser = getValue(attributes, "parser"); // NOI18N
                c.removeIncludePathPrefix = getValue(attributes, "remove_in_path"); // NOI18N
                c.removeIncludeOutputPrefix = getValue(attributes, "remove_in_output"); // NOI18N
            } else if (path.endsWith(".user_include")) { // NOI18N
                c.userIncludeFlag = flags;
            } else if (path.endsWith(".user_file")) { // NOI18N
                c.userFileFlag = flags;
            } else if (path.endsWith(".important_flags")) { // NOI18N
                c.importantFlags = flags;
            } else if (path.endsWith(".system_macros")) { // NOI18N
                c.macrosFlags = flags;
                c.macrosOutputParser = getValue(attributes, "parser"); // NOI18N
            } else if (path.endsWith(".user_macro")) { // NOI18N
                c.userMacroFlag = flags;
            } else if (path.indexOf(".development_mode.") > 0) { // NOI18N
                DevelopmentMode d = c.developmentMode;
                if (path.endsWith(".fast_build")) { // NOI18N
                    d.fast_build = flags;
                    if (isDefault) {
                        d.default_selection = 0;
                    }
                } else if (path.endsWith(".debug")) { // NOI18N
                    d.debug = flags;
                    if (isDefault) {
                        d.default_selection = 1;
                    }
                } else if (path.endsWith(".performance_debug")) { // NOI18N
                    d.performance_debug = flags;
                    if (isDefault) {
                        d.default_selection = 2;
                    }
                } else if (path.endsWith(".test_coverage")) { // NOI18N
                    d.test_coverage = flags;
                    if (isDefault) {
                        d.default_selection = 3;
                    }
                } else if (path.endsWith(".diagnosable_release")) { // NOI18N
                    d.diagnosable_release = flags;
                    if (isDefault) {
                        d.default_selection = 4;
                    }
                } else if (path.endsWith(".release")) { // NOI18N
                    d.release = flags;
                    if (isDefault) {
                        d.default_selection = 5;
                    }
                } else if (path.endsWith(".performance_release")) { // NOI18N
                    d.performance_release = flags;
                    if (isDefault) {
                        d.default_selection = 6;
                    }
                }
            } else if (path.indexOf(".warning_level.") > 0) { // NOI18N
                WarningLevel w = c.warningLevel;
                if (path.endsWith(".no_warnings")) { // NOI18N
                    w.no_warnings = flags;
                    if (isDefault) {
                        w.default_selection = 0;
                    }
                } else if (path.endsWith(".default")) { // NOI18N
                    w.default_level = flags;
                    if (isDefault) {
                        w.default_selection = 1;
                    }
                } else if (path.endsWith(".more_warnings")) { // NOI18N
                    w.more_warnings = flags;
                    if (isDefault) {
                        w.default_selection = 2;
                    }
                } else if (path.endsWith(".warning2error")) { // NOI18N
                    w.warning2error = flags;
                    if (isDefault) {
                        w.default_selection = 3;
                    }
                }
            } else if (path.indexOf(".architecture.") > 0) { // NOI18N
                Architecture a = c.architecture;
                if (path.endsWith(".default")) { // NOI18N
                    a.default_architecture = flags;
                    if (isDefault) {
                        a.default_selection = 0;
                    }
                } else if (path.endsWith(".bits_32")) { // NOI18N
                    a.bits_32 = flags;
                    if (isDefault) {
                        a.default_selection = 1;
                    }
                } else if (path.endsWith(".bits_64")) { // NOI18N
                    a.bits_64 = flags;
                    if (isDefault) {
                        a.default_selection = 2;
                    }
                }
            } else if (path.indexOf(".cpp_standard.") > 0) { // NOI18N
                CppStandard st = c.cppStandard;
                if (path.endsWith(".default")) { // NOI18N
                    st.cppDefault = flags;
                    if (isDefault) {
                        st.default_selection = 0;
                    }
                } else if (path.endsWith(".cpp98")) { // NOI18N
                    st.cpp98 = flags;
                    if (isDefault) {
                        st.default_selection = 1;
                    }
                } else if (path.endsWith(".cpp11")) { // NOI18N
                    st.cpp11 = flags;
                    if (isDefault) {
                        st.default_selection = 2;
                    }
                } else if (path.endsWith(".cpp14")) { // NOI18N
                    st.cpp14 = flags;
                    if (isDefault) {
                        st.default_selection = 3;
                    }
                } else if (path.endsWith(".cpp17")) { // NOI18N
                    st.cpp17 = flags;
                    if (isDefault) {
                        st.default_selection = 4;
                    }
                } else if (path.endsWith(".cpp20")) { // NOI18N
                    st.cpp20 = flags;
                    if (isDefault) {
                        st.default_selection = 5;
                    }
                } else if (path.endsWith(".cpp23")) { // NOI18N
                    st.cpp23 = flags;
                    if (isDefault) {
                        st.default_selection = 6;
                    }
                }
            } else if (path.indexOf(".c_standard.") > 0) { // NOI18N
                CStandard st = c.cStandard;
                if (path.endsWith(".default")) { // NOI18N
                    st.cDefault = flags;
                    if (isDefault) {
                        st.default_selection = 0;
                    }
                } else if (path.endsWith(".c89")) { // NOI18N
                    st.c89 = flags;
                    if (isDefault) {
                        st.default_selection = 1;
                    }
                } else if (path.endsWith(".c99")) { // NOI18N
                    st.c99 = flags;
                    if (isDefault) {
                        st.default_selection = 2;
                    }
                } else if (path.endsWith(".c11")) { // NOI18N
                    st.c11 = flags;
                    if (isDefault) {
                        st.default_selection = 3;
                    }
                } else if (path.endsWith(".c17")) { // NOI18N
                    st.c17 = flags;
                    if (isDefault) {
                        st.default_selection = 4;
                    }
                } else if (path.endsWith(".c23")) { // NOI18N
                    st.c23 = flags;
                    if (isDefault) {
                        st.default_selection = 5;
                    }
                }
            } else if (path.endsWith(".strip")) { // NOI18N
                c.strip = flags;
            } else if (path.endsWith(".output_object_file")) { // NOI18N
                c.outputObjectFileFlags = flags;
            } else if (path.endsWith(".dependency_generation")) { // NOI18N
                c.dependencyGenerationFlags = flags;
            } else if (path.endsWith(".precompiled_header")) { // NOI18N
                c.precompiledHeaderFlags = flags;
                c.precompiledHeaderSuffix = getValue(attributes, "suffix"); // NOI18N
                c.precompiledHeaderSuffixAppend = Boolean.valueOf(getValue(attributes, "append")); // NOI18N
            } else if (path.indexOf(".multithreading.") > 0) { // NOI18N
                MultiThreading m = c.multithreading;
                if (path.endsWith(".none")) { // NOI18N
                    m.none = flags;
                    if (isDefault) {
                        m.default_selection = 0;
                    }
                } else if (path.endsWith(".safe")) { // NOI18N
                    m.safe = flags;
                    if (isDefault) {
                        m.default_selection = 1;
                    }
                } else if (path.endsWith(".automatic")) { // NOI18N
                    m.automatic = flags;
                    if (isDefault) {
                        m.default_selection = 2;
                    }
                } else if (path.endsWith(".open_mp")) { // NOI18N
                    m.open_mp = flags;
                    if (isDefault) {
                        m.default_selection = 3;
                    }
                }
            } else if (path.indexOf(".standard.") > 0) { // NOI18N
                Standard s = c.standard;
                if (path.endsWith(".old")) { // NOI18N
                    s.old = flags;
                    if (isDefault) {
                        s.default_selection = 0;
                    }
                } else if (path.endsWith(".legacy")) { // NOI18N
                    s.legacy = flags;
                    if (isDefault) {
                        s.default_selection = 1;
                    }
                } else if (path.endsWith(".default")) { // NOI18N
                    s.default_standard = flags;
                    if (isDefault) {
                        s.default_selection = 2;
                    }
                } else if (path.endsWith(".modern")) { // NOI18N
                    s.modern = flags;
                    if (isDefault) {
                        s.default_selection = 3;
                    }
                }
            } else if (path.indexOf(".language_extension.") > 0) { // NOI18N
                LanguageExtension e = c.languageExtension;
                if (path.endsWith(".none")) { // NOI18N
                    e.none = flags;
                    if (isDefault) {
                        e.default_selection = 0;
                    }
                } else if (path.endsWith(".default")) { // NOI18N
                    e.default_extension = flags;
                    if (isDefault) {
                        e.default_selection = 1;
                    }
                } else if (path.endsWith(".all")) { // NOI18N
                    e.all = flags;
                    if (isDefault) {
                        e.default_selection = 2;
                    }
                }
            } else if (path.indexOf(".library.") > 0) { // NOI18N
                Library l = c.library;
                if (path.endsWith(".none")) { // NOI18N
                    l.none = flags;
                    if (isDefault) {
                        l.default_selection = 0;
                    }
                } else if (path.endsWith(".runtime")) { // NOI18N
                    l.runtime = flags;
                    if (isDefault) {
                        l.default_selection = 1;
                    }
                } else if (path.endsWith(".classic")) { // NOI18N
                    l.classic = flags;
                    if (isDefault) {
                        l.default_selection = 2;
                    }
                } else if (path.endsWith(".binary_standard")) { // NOI18N
                    l.binary_standard = flags;
                    if (isDefault) {
                        l.default_selection = 3;
                    }
                } else if (path.endsWith(".conforming_standard")) { // NOI18N
                    l.conforming_standard = flags;
                    if (isDefault) {
                        l.default_selection = 4;
                    }
                }
            }
        }

        private boolean checkAlternativePath(org.xml.sax.Attributes attributes, List<AlternativePath> alternativePath){
            if (path.endsWith(".alternative_path.path") && alternativePath != null) { // NOI18N
                String s = getValue(attributes, "directory"); // NOI18N
                if (s != null) {
                    alternativePath.add(new Alternative(s, AlternativePath.PathKind.PATH));
                    return true;
                }
                s = getValue(attributes, "toolchain_family"); // NOI18N
                if (s != null) {
                    alternativePath.add(new Alternative(s, AlternativePath.PathKind.TOOL_FAMILY));
                    return true;
                }
                s = getValue(attributes, "toolchain_name"); // NOI18N
                if (s != null) {
                    alternativePath.add(new Alternative(s, AlternativePath.PathKind.TOOL_NAME));
                    return true;
                }
                return true;
            }
            return false;
        }
    }
    /**
     * class package-local for testing only
     */
    static final class ToolchainDescriptorImpl implements ToolchainDescriptor {

        final CompilerVendor v;
        private CompilerDescriptor c;
        private CompilerDescriptor cpp;
        private CompilerDescriptor fortran;
        private CompilerDescriptor assembler;
        private LinkerDescriptor linker;
        private ScannerDescriptor scanner;
        private MakeDescriptor make;
        private DebuggerDescriptor debugger;
        private QMakeDescriptor qmake;
        private CMakeDescriptor cmake;

        private ToolchainDescriptorImpl(CompilerVendor v) {
            this.v = v;
        }

        @Override
        public String getFileName() {
            return v.toolChainFileName;
        }

        @Override
        public String getName() {
            return v.toolChainName;
        }

        @Override
        public String getDisplayName() {
            return v.toolChainDisplay;
        }

        @Override
        public String[] getFamily() {
            synchronized(v) {
                if (v.family != null && v.family.length() > 0) {
                    return v.family.split(","); // NOI18N
                }
            }
            return new String[]{};
        }

        @Override
        public String[] getPlatforms() {
            synchronized(v) {
                if (v.platforms != null && v.platforms.length() > 0) {
                    return v.platforms.split(","); // NOI18N
                }
            }
            return new String[]{};
        }

        @Override
        public String getUpdateCenterUrl() {
            return v.uc;
        }

        @Override
        public String getUpdateCenterDisplayName() {
            return v.ucName;
        }

        @Override
        public String getUpgradeUrl() {
            return v.upgrage;
        }

        @Override
        public String getModuleID() {
            return v.module;
        }

        @Override
        public boolean isAbstract() {
            return v.isAbstract;
        }

        @Override
        public boolean isAutoDetected() {
            return v.isAutoDetected;
        }

        @Override
        public String[] getAliases() {
            synchronized(v) {
                if (v.aliases != null && v.aliases.length() > 0) {
                    return v.aliases.split(","); // NOI18N
                }
            }
            return new String[]{};
        }

        @Override
        public String getSubstitute() {
            return v.substitute;
        }

        @Override
        public String getDriveLetterPrefix() {
            return v.driveLetterPrefix;
        }

        @Override
        public String getMakefileWriter() {
            return v.makefileWriter;
        }

        @Override
        public CompilerDescriptor getC() {
            synchronized(v) {
                if (c == null && v.c.isValid()) {
                    c = new CompilerDescriptorImpl(v.c);
                }
            }
            return c;
        }

        @Override
        public List<BaseFolder> getBaseFolders() {
            if (v.baseFolder == null) {
                return null;
            }
            List<BaseFolder> res = new ArrayList<BaseFolder>(v.baseFolder.size());
            for(FolderInfo info : v.baseFolder){
                res.add(new BaseFolderImpl(info));
            }
            return res;
        }

        @Override
        public List<BaseFolder> getCommandFolders() {
            if (v.commandFolder == null) {
                return null;
            }
            List<BaseFolder> res = new ArrayList<BaseFolder>(v.baseFolder.size());
            for(FolderInfo info : v.commandFolder){
                res.add(new BaseFolderImpl(info));
            }
            return res;
        }

        @Override
        public String getQmakeSpec() {
            return v.qmakespec;
        }

        @Override
        public CompilerDescriptor getCpp() {
            synchronized(v) {
                if (cpp == null && v.cpp.isValid()) {
                    cpp = new CompilerDescriptorImpl(v.cpp);
                }
            }
            return cpp;
        }

        @Override
        public CompilerDescriptor getFortran() {
            synchronized(v) {
                if (fortran == null && v.fortran.isValid()) {
                    fortran = new CompilerDescriptorImpl(v.fortran);
                }
            }
            return fortran;
        }

        @Override
        public CompilerDescriptor getAssembler() {
            synchronized(v) {
                if (assembler == null && v.assembler.isValid()) {
                    assembler = new CompilerDescriptorImpl(v.assembler);
                }
            }
            return assembler;
        }

        @Override
        public ScannerDescriptor getScanner() {
            synchronized(v) {
                if (scanner == null) {
                    scanner = new ScannerDescriptorImpl(v.scanner);
                }
            }
            return scanner;
        }

        @Override
        public LinkerDescriptor getLinker() {
            synchronized(v) {
                if (linker == null) {
                    linker = new LinkerDescriptorImpl(v.linker);
                }
            }
            return linker;
        }

        @Override
        public MakeDescriptor getMake() {
            synchronized(v) {
                if (make == null) {
                    make = new MakeDescriptorImpl(v.make);
                }
            }
            return make;
        }

        @Override
        public Map<String, List<String>> getDefaultLocations() {
            return v.default_locations;
        }

        @Override
        public DebuggerDescriptor getDebugger() {
            synchronized(v) {
                if (debugger == null) {
                    debugger = new DebuggerDescriptorImpl(v.debugger);
                }
            }
            return debugger;
        }

        @Override
        public QMakeDescriptor getQMake() {
            synchronized(v) {
                if (qmake == null) {
                    qmake = new QMakeDescriptorImpl(v.qmake);
                }
            }
            return qmake;
        }

        @Override
        public CMakeDescriptor getCMake() {
            synchronized(v) {
                if (cmake == null) {
                    cmake = new CMakeDescriptorImpl(v.cmake);
                }
            }
            return cmake;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    private static class BaseFolderImpl implements BaseFolder {
        private final FolderInfo info;

        public BaseFolderImpl(FolderInfo info) {
            this.info = info;
        }

        @Override
        public String getFolderKey() {
            return info.folderKey;
        }

        @Override
        public String getFolderPattern() {
            return info.folderPattern;
        }

        @Override
        public String getFolderSuffix() {
            return info.folderSuffix;
        }

        @Override
        public String getFolderPathPattern() {
            return info.folderPathPattern;
        }

        @Override
        public String getRelativePath() {
            return info.relativePath;
        }
    }

    private static class ToolDescriptorImpl<T extends Tool> implements ToolDescriptor {

        protected final T tool;

        public ToolDescriptorImpl(T tool) {
            this.tool = tool;
        }

        @Override
        public String[] getNames() {
            synchronized(tool) {
                if (tool.name != null && tool.name.length() > 0) {
                    return tool.name.split(","); // NOI18N
                }
            }
            return new String[]{};
        }

        @Override
        public String getVersionFlags() {
            return tool.versionFlags;
        }

        @Override
        public String getVersionPattern() {
            return tool.versionPattern;
        }

        @Override
        public String getFingerPrintFlags() {
            return tool.fingerprintFlags;
        }

        @Override
        public String getFingerPrintPattern() {
            return tool.fingerprintPattern;
        }

        @Override
        public AlternativePath[] getAlternativePath() {
            synchronized(tool) {
                if (tool.alternativePath != null) {
                    return tool.alternativePath.toArray(new AlternativePath[tool.alternativePath.size()] );
                }
            }
            return null;
        }

        @Override
        public boolean skipSearch() {
            return tool.skipSearch;
        }
    }

    private static final class CompilerDescriptorImpl
            extends ToolDescriptorImpl<Compiler> implements CompilerDescriptor {

        private CompilerDescriptorImpl(Compiler compiler) {
            super(compiler);
        }

        @Override
        public String getPathPattern() {
            return tool.pathPattern;
        }

        @Override
        public String getExistFolder() {
            return tool.existFolder;
        }

        @Override
        public String getIncludeFlags() {
            return tool.includeFlags;
        }

        @Override
        public String getIncludeParser() {
            return tool.includeOutputParser;
        }

        @Override
        public String getRemoveIncludePathPrefix() {
            return tool.removeIncludePathPrefix;
        }

        @Override
        public String getRemoveIncludeOutputPrefix() {
            return tool.removeIncludeOutputPrefix;
        }

        @Override
        public String getUserIncludeFlag() {
            return tool.userIncludeFlag;
        }

        @Override
        public String getUserFileFlag() {
            return tool.userFileFlag;
        }

        @Override
        public String getImportantFlags() {
            return tool.importantFlags;
        }

        @Override
        public String getMacroFlags() {
            return tool.macrosFlags;
        }

        @Override
        public String getMacroParser() {
            return tool.macrosOutputParser;
        }

        @Override
        public List<PredefinedMacro> getPredefinedMacros() {
            return tool.predefinedMacros;
        }

        @Override
        public String getUserMacroFlag() {
            return tool.userMacroFlag;
        }

        @Override
        public String[] getDevelopmentModeFlags() {
            return tool.developmentMode.values();
        }

        @Override
        public String[] getWarningLevelFlags() {
            return tool.warningLevel.values();
        }

        @Override
        public String[] getArchitectureFlags() {
            return tool.architecture.values();
        }

        @Override
        public String[] getCppStandardFlags() {
            return tool.cppStandard.values();
        }

        @Override
        public String[] getCStandardFlags() {
            return tool.cStandard.values();
        }

        @Override
        public String getStripFlag() {
            return tool.strip;
        }

        @Override
        public String[] getMultithreadingFlags() {
            return tool.multithreading.values();
        }

        @Override
        public String[] getStandardFlags() {
            return tool.standard.values();
        }

        @Override
        public String[] getLanguageExtensionFlags() {
            return tool.languageExtension.values();
        }

        @Override
        public String[] getLibraryFlags() {
            return tool.library.values();
        }

        @Override
        public String getOutputObjectFileFlags() {
            return tool.outputObjectFileFlags;
        }

        @Override
        public String getDependencyGenerationFlags() {
            return tool.dependencyGenerationFlags;
        }

        @Override
        public String getPrecompiledHeaderFlags() {
            return tool.precompiledHeaderFlags;
        }

        @Override
        public String getPrecompiledHeaderSuffix() {
            return tool.precompiledHeaderSuffix;
        }

        @Override
        public boolean getPrecompiledHeaderSuffixAppend() {
            return tool.precompiledHeaderSuffixAppend;
        }

        @Override
        public String toString() {
            return "Path="+getPathPattern()+" Folder="+getExistFolder()+" Version="+getVersionPattern();// NOI18N
        }
    }
    
    private static final class PredefinedMacroImpl implements PredefinedMacro {
        final String macro;
        final String flags;
        final boolean hidden;

        PredefinedMacroImpl(String macro, String flags, boolean hidden){
            this.macro = macro;
            this.flags = flags;
            this.hidden = hidden;
        }

        @Override
        public String getMacro() {
            return macro;
        }

        @Override
        public boolean isHidden() {
            return hidden;
        }

        @Override
        public String getFlags() {
            return flags;
        }

    }

    private static final class ScannerDescriptorImpl implements ScannerDescriptor {

        private final Scanner s;
        private List<ScannerPattern> patterns;
        private List<String> filterOut;

        private ScannerDescriptorImpl(Scanner s) {
            this.s = s;
        }

        @Override
	public String getID() {
	    return s.id;
	}

        @Override
	public List<ScannerPattern> getPatterns() {
            synchronized(s) {
                if (patterns == null) {
                    patterns = new ArrayList<ScannerPattern>();
                    for (ErrorPattern p : s.patterns) {
                        patterns.add(new ScannerPatternImpl(p));
                    }
                }
            }
            return patterns;
        }

        @Override
        public String getChangeDirectoryPattern() {
            return s.changeDirectoryPattern;
        }

        @Override
        public String getEnterDirectoryPattern() {
            return s.enterDirectoryPattern;
        }

        @Override
        public String getLeaveDirectoryPattern() {
            return s.leaveDirectoryPattern;
        }

        @Override
        public String getMakeAllInDirectoryPattern() {
            return s.makingAllInDirectoryPattern;
        }

        @Override
        public List<String> getStackHeaderPattern() {
            return s.stackHeaderPattern;
        }

        @Override
        public List<String> getStackNextPattern() {
            return s.stackNextPattern;
        }

        @Override
	public List<String> getFilterOutPatterns() {
            synchronized(s) {
                if (filterOut == null){
                    filterOut = new ArrayList<String>();
                    if (s.filterOut != null) {
                        filterOut.addAll(s.filterOut);
                    }
                }
            }
	    return filterOut;
	}
    }

    private static final class ScannerPatternImpl implements ScannerPattern {

        private final ErrorPattern e;

        private ScannerPatternImpl(ErrorPattern e) {
            this.e = e;
        }

        @Override
        public String getPattern() {
            return e.pattern;
        }

        @Override
        public String getSeverity() {
            return e.severity;
        }

        @Override
        public String getLanguage() {
            return e.language;
        }
    }

    private static final class LinkerDescriptorImpl implements LinkerDescriptor {

        private final Linker l;

        private LinkerDescriptorImpl(Linker l) {
            this.l = l;
        }

        @Override
        public String getLibrarySearchFlag() {
            return l.librarySearchFlag;
        }

        @Override
        public String getDynamicLibrarySearchFlag() {
            return l.dynamicLibrarySearchFlag;
        }

        @Override
        public String getLibraryFlag() {
            return l.libraryFlag;
        }

        @Override
        public String getLibraryPrefix() {
            return l.library_prefix;
        }

        @Override
        public String getPICFlag() {
            return l.PICFlag;
        }

        @Override
        public String getStaticLibraryFlag() {
            return l.staticLibraryFlag;
        }

        @Override
        public String getDynamicLibraryFlag() {
            return l.dynamicLibraryFlag;
        }

        @Override
        public String getDynamicLibraryBasicFlag() {
            return l.dynamicLibraryBasicFlag;
        }

        @Override
        public String getOutputFileFlag() {
            return l.outputFileFlag;
        }

        @Override
        public String getStripFlag() {
            return l.stripFlag;
        }

        @Override
        public String getPreferredCompiler() {
            return l.preferredCompiler;
        }
    }

    private static final class MakeDescriptorImpl
            extends ToolDescriptorImpl<Make> implements MakeDescriptor {

        private MakeDescriptorImpl(Make make) {
            super(make);
        }

        @Override
        public String getDependencySupportCode() {
            return tool.dependencySupportCode;
        }
    }

    private static final class DebuggerDescriptorImpl
            extends ToolDescriptorImpl<Debugger> implements DebuggerDescriptor {
        private final Debugger debugger;

        public DebuggerDescriptorImpl(Debugger debugger) {
            super(debugger);
            this.debugger = debugger;
        }

        @Override
        public String getID() {
            return debugger.id;
        }
    }

    private static final class QMakeDescriptorImpl
            extends ToolDescriptorImpl<QMake> implements QMakeDescriptor {

        public QMakeDescriptorImpl(QMake qmake) {
            super(qmake);
        }
    }

    private static final class CMakeDescriptorImpl
            extends ToolDescriptorImpl<CMake> implements CMakeDescriptor {

        public CMakeDescriptorImpl(CMake qmake) {
            super(qmake);
        }
    }
}
