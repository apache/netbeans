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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItem.Language;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.NativeProjectProvider;
import org.netbeans.modules.cnd.makeproject.NativeProjectProvider.MacroConverter;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.netbeans.modules.dlight.libs.common.PerformanceLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;
import org.openide.util.Lookup;

public class Item implements NativeFileItem, PropertyChangeListener {
    
    public static abstract class  ItemFactory {
        private static final ItemFactory DEFAULT = new Default();

        private static ItemFactory defaultFactory;

        protected ItemFactory() {
        }

        public static ItemFactory getDefault() {
            if (defaultFactory != null) {
                return defaultFactory;
            }
            defaultFactory = Lookup.getDefault().lookup(ItemFactory.class);
            return defaultFactory == null ? DEFAULT : defaultFactory;
        }

        public abstract Item createInBaseDir(FileObject baseDirFileObject, String path);
        public abstract Item createInFileSystem(FileSystem fileSystem, String path);
        public abstract Item createDetachedViewItem(FileSystem fileSystem, String path);

        private static final class Default extends ItemFactory {

            @Override
            public Item createInBaseDir(FileObject baseDirFileObject, String path) {
                return new Item(baseDirFileObject, path);
            }

            @Override
            public Item createInFileSystem(FileSystem fileSystem, String path) {
                return new Item(fileSystem, path);
            }

            @Override
            public Item createDetachedViewItem(FileSystem fileSystem, String path) {
                CndUtils.assertNonUiThread();
                Item out = new Item(fileSystem, path);
                return out;
            }
        }    
    }
    
    protected static final Logger LOG = Logger.getLogger("makeproject.folder"); // NOI18N

    private final CharSequence path;
    protected Folder folder;
    protected FileObject canonicalFileObject = null;
    private WeakReference<FileObject> fileObjectCache = new WeakReference<>(null);
    protected final FileSystem fileSystem;
    private final CharSequence normalizedPath;

    protected Item(FileObject baseDirFileObject, String path) {
        try {
            this.fileSystem = baseDirFileObject.getFileSystem();
        } catch (FileStateInvalidException ex) {
            throw new IllegalStateException(ex);
        }
        String absPath = CndPathUtilities.toAbsolutePath(baseDirFileObject, path);
        this.normalizedPath = FilePathCache.getManager().getString(CharSequences.create(FileSystemProvider.normalizeAbsolutePath(absPath, fileSystem)));
        this.path = FilePathCache.getManager().getString(CharSequences.create(CndPathUtilities.normalizeSlashes(path)));
    }

    // XXX:fullRemote deprecate and remove!
    protected Item(FileSystem fileSystem, String path) {
        CndUtils.assertNotNull(path, "Path should not be null"); //NOI18N
        this.path = FilePathCache.getManager().getString(CharSequences.create(path));
        this.fileSystem = fileSystem; //CndFileUtils.getLocalFileSystem();
        this.normalizedPath = null;
        folder = null;
    }

    protected void rename(String newname, boolean nameWithoutExtension) {
        if (newname == null || newname.length() == 0 || getFolder() == null) {
            return;
        }
        if (CharSequenceUtils.contentEquals(path, newname)) {
            return;
        }

        // Rename name in path
        int indexName = CharSequenceUtils.lastIndexOf(path,'/');
        if (indexName < 0) {
            indexName = 0;
        } else {
            indexName++;
        }

        int indexDot = CharSequenceUtils.lastIndexOf(path,'.');
        if (indexDot < indexName || !nameWithoutExtension) {
            indexDot = -1;
        }

        String oldname;
        if (indexDot >= 0) {
            oldname = path.toString().substring(indexName, indexDot);
        } else {
            oldname = path.toString().substring(indexName);
        }
        if (oldname.equals(newname)) {
            return;
        }

        String newPath = ""; // NOI18N
        if (indexName > 0) {
            newPath = path.toString().substring(0, indexName);
        }
        newPath += newname;
        if (indexDot >= 0) {
            newPath += path.toString().substring(indexDot);
        }
        // Remove old item and insert new with new name
        renameTo(newPath);
    }

    protected void renameTo(String newPath) {
        Folder f = getFolder();
        String oldPath;
        if (normalizedPath != null) {
            oldPath = normalizedPath.toString();
        } else {
            oldPath = CndFileUtils.normalizeAbsolutePath(fileSystem, getAbsPath());
        }
        Item item = f.addItem(ItemFactory.getDefault().createInFileSystem(fileSystem, newPath));
        if (item != null && item.getFolder() != null) {
            if (item.getFolder().isProjectFiles()) {
                copyItemConfigurations(this, item);
            }
            f.removeItem(this);
            f.renameItemAction(oldPath, item);
        }
    }

    public String getPath() {
        return path.toString();
    }

    @Override
    public String getAbsolutePath() {
        return getNormalizedPath();
    }

    public String getSortName() {
        //return sortName;
        return getName();
    }

    @Override
    public String getName() {
        return CndPathUtilities.getBaseName(path.toString());
    }

    public String getPath(boolean norm) {
        String pat = "./"; // UNIX path  // NOI18N
        if (norm && getPath().startsWith(pat)) {
            return getPath().substring(2);
        } else {
            return getPath();
        }
    }

    public String getAbsPath() {
        String retPath = null;
        if (CndPathUtilities.isPathAbsolute(fileSystem, getPath())) {// UNIX path
            retPath = getPath();
        } else if (getFolder() != null) {
            retPath = getFolder().getConfigurationDescriptor().getBaseDir() + '/' + getPath(); // UNIX path
        }
        return retPath;
    }

    public void setFolder(Folder folder) {
        if (folder == null && canonicalFileObject == null) {
            // store file in field. method getFile() will works after removing item
            ensureFileNotNull();
        }
        this.folder = folder;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("name")) { // NOI18N
            // File has been renamed.
            String newName = (String) evt.getNewValue();
            boolean nameWithoutExtension = true;
            rename(newName, nameWithoutExtension);
        } else if (evt.getPropertyName().equals("valid")) { // NOI18N
            if (!((Boolean) evt.getNewValue())) {
                // File has been deleted.
                // Refresh folder. See also IZ 87557 and IZ 94935
                Folder containingFolder = getFolder();
                if (containingFolder != null) {
                    containingFolder.refresh(this);
                }
            }
        } else if (evt.getPropertyName().equals("primaryFile")) { // NOI18N
            // File has been moved.
            if (getFolder() != null) {
                FileObject fo = (FileObject) evt.getNewValue();
                String newPath = fo.getPath();
                if (!CndPathUtilities.isPathAbsolute(fileSystem, getPath())) {
                    newPath = CndPathUtilities.toRelativePath(getFolder().getConfigurationDescriptor().getBaseDirFileObject(), newPath);
                }
                newPath = CndPathUtilities.normalizeSlashes(newPath);
                renameTo(newPath);
            }
        }
    }

    public Folder getFolder() {
        return folder;
    }

    public FSPath getFSPath() {
        return new FSPath(fileSystem, getNormalizedPath());
    }

    public String getNormalizedPath() {
        synchronized (this) {
            if (normalizedPath != null) {
                return normalizedPath.toString();
            }
        }
        String absPath = getAbsPath();
        return FileSystemProvider.normalizeAbsolutePath(absPath, fileSystem);
    }
    
    public String getCanonicalPath() {
        final FileObject canonicalFile = getCanonicalFile();
        if (canonicalFile != null) {
            return canonicalFile.getPath();
        }
        return getNormalizedPath();
    }

    protected void ensureFileNotNull() {
        if (canonicalFileObject == null) {
            try {
                canonicalFileObject = CndFileUtils.getCanonicalFileObject(getFileObject());
            } catch (IOException ioe) {
                canonicalFileObject = getFSPath().getFileObject();
            }
        }
        if (canonicalFileObject == null) {
            LOG.log(Level.SEVERE, "Can not resolve file {0}", getAbsPath());
        }
    }

    public FileObject getCanonicalFile() {
        ensureFileNotNull();
        return canonicalFileObject;
    }

    public String getId() {
        // ID of other objects shouldn't be like to path
        return getPath();
    }

    public ItemConfiguration getItemConfiguration(Configuration configuration) {
        if (configuration != null) {
            return (ItemConfiguration) configuration.getAuxObject(getId());
        }
        return null;
    }

    public ItemConfiguration[] getItemConfigurations() {
        ItemConfiguration[] itemConfigurations;
        MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor();
        if (makeConfigurationDescriptor == null) {
            return new ItemConfiguration[0];
        }
        Configuration[] configurations = makeConfigurationDescriptor.getConfs().toArray();
        itemConfigurations = new ItemConfiguration[configurations.length];
        for (int i = 0; i < configurations.length; i++) {
            itemConfigurations[i] = getItemConfiguration(configurations[i]);
        }
        return itemConfigurations;
    }

    public void copyConfigurations(Item src) {
        if (src.getFolder() == null) {
            return;
        }

        MakeConfigurationDescriptor makeConfigurationDescriptor = src.getFolder().getConfigurationDescriptor();
        if (makeConfigurationDescriptor == null) {
            return;
        }

        for (Configuration conf : makeConfigurationDescriptor.getConfs().toArray()) {
            ItemConfiguration srcItemConfiguration = src.getItemConfiguration(conf);
            ItemConfiguration dstItemConfiguration = getItemConfiguration(conf);
            if (srcItemConfiguration != null && dstItemConfiguration != null) {
                dstItemConfiguration.assignValues(srcItemConfiguration);
            }
        }
    }

    /**
     * Copies configuration from <code>src</code> item to <code>dst</code> item.
     * Both items must be assigned to folders to correctly operate with
     * their configurations. Otherwise NPEs will be thrown.
     *
     * @param src  item to copy configuration from
     * @param dst  item to copy configuration to
     */
    private static void copyItemConfigurations(Item src, Item dst) {
        MakeConfigurationDescriptor makeConfigurationDescriptor = src.getMakeConfigurationDescriptor();
        if (makeConfigurationDescriptor != null) {
            for (Configuration conf : makeConfigurationDescriptor.getConfs().toArray()) {
                ItemConfiguration newConf = new ItemConfiguration(conf, dst);
                newConf.assignValues(src.getItemConfiguration(conf));
                conf.addAuxObject(newConf);
            }
        }
    }

    @Override
    public FileObject getFileObject() {
        FileObject fo = getFileObjectImpl();
        if (fo == null) {
            String p = (normalizedPath != null) ? normalizedPath.toString() : getAbsPath();
            return InvalidFileObjectSupport.getInvalidFileObject(fileSystem, p);
        }
        return fo;
    }

    /** 
     * Returns file object for this item.
     * If not found, returns a honest null, no dummies (InvalidFileObjectSupport.getInvalidFileObject)
     */
    protected FileObject getFileObjectImpl() {
        PerformanceLogger.PerformaceAction performanceEvent = PerformanceLogger.getLogger().start(Folder.GET_ITEM_FILE_OBJECT_PERFORMANCE_EVENT, this);
        FileObject fileObject = fileObjectCache.get();
        if (fileObject == null || !fileObject.isValid()) {
            try {
                performanceEvent.setTimeOut(Folder.FS_TIME_OUT);
                if (normalizedPath != null) {
                    fileObject = fileSystem.findResource(normalizedPath.toString());
                } else {
                    Folder f = getFolder();
                    if (f == null) {
                        // don't know file system, fall back to the default one
                        // but do not cache file object
                        String p = getPath();
                        if (CndPathUtilities.isPathAbsolute(fileSystem, p)) {// UNIX path
                            p = FileSystemProvider.normalizeAbsolutePath(p, fileSystem);                        
                            fileObject = fileSystem.findResource(p);
                        }
                    } else {                    
                        MakeConfigurationDescriptor cfgDescr = f.getConfigurationDescriptor();
                        FileObject baseDirFO = cfgDescr.getBaseDirFileObject();
                        fileObject = RemoteFileUtil.getFileObject(baseDirFO, getPath());
                    }
                }
            } finally {
                performanceEvent.log(fileObject);
            }
            fileObjectCache = new WeakReference<>(fileObject);
        }
        return fileObject;
    }

    public void onOpen() {
    }
    
    protected void onClose() {
    }
    
    public String getMIMEType() {
        FileObject fobj = getFileObjectImpl();
        String mimeType;
        if (fobj == null || ! fobj.isValid()) {
            mimeType = MIMESupport.getKnownSourceFileMIMETypeByExtension(getName());
        } else {
            mimeType = MIMESupport.getSourceFileMIMEType(fobj);
        }
        return mimeType;
    }
    
    public PredefinedToolKind getDefaultTool() {
        PredefinedToolKind tool;
        String mimeType = getMIMEType();
        if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
//            DataObject dataObject = getDataObject();
//            FileObject fo = dataObject == null ? null : dataObject.getPrimaryFile();
//            // Do not use C for .pc files
//            if (fo != null && "pc".equals(fo.getExt())) { //NOI18N
//                tool = PredefinedToolKind.CustomTool;
//            } else {
            tool = PredefinedToolKind.CCompiler;
//            }
        } else if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
            tool = PredefinedToolKind.CustomTool;
        } else if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
            tool = PredefinedToolKind.CCCompiler;
        } else if (MIMENames.FORTRAN_MIME_TYPE.equals(mimeType)) {
            tool = PredefinedToolKind.FortranCompiler;
        } else if (MIMENames.ASM_MIME_TYPE.equals(mimeType)) {
            FileObject fobj = getFileObjectImpl();
            // Do not use assembler for .il files
            if (fobj != null && "il".equals(fobj.getExt())) { //NOI18N
                tool = PredefinedToolKind.CustomTool;
            } else {
                tool = PredefinedToolKind.Assembler;
            }
        } else {
            tool = PredefinedToolKind.CustomTool;
        }
        return tool;
    }

    private MakeConfigurationDescriptor getMakeConfigurationDescriptor() {
        if (getFolder() == null) {
            return null;
        }
        return getFolder().getConfigurationDescriptor();
    }

    private MakeConfiguration getMakeConfiguration() {
        MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor();
        if (makeConfigurationDescriptor == null) {
            return null;
        }
        return makeConfigurationDescriptor.getActiveConfiguration();
    }

    @Override
    public NativeProject getNativeProject() {
        Folder curFolder = getFolder();
        if (curFolder != null) {
            Project project = curFolder.getProject();
            if (project != null) {
                return project.getLookup().lookup(NativeProject.class);
            }
        }
        return null;
    }

    @Override
    public List<IncludePath> getSystemIncludePaths() {
        List<IncludePath> vec = new ArrayList<>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration);//ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) { // FIXUP: sometimes itemConfiguration is null (should not happen)
            return vec;
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return vec;
        }
        AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // Get include paths from compiler
            if (compiler != null && compiler.getPath() != null && compiler.getPath().length() > 0) {
                FileSystem fs = FileSystemProvider.getFileSystem(compiler.getExecutionEnvironment());
                if (makeConfiguration.isMakefileConfiguration()) {
                    vec.addAll(IncludePath.toIncludePathList(fs, compiler.getSystemIncludeDirectories(getImportantFlags())));
                } else {
                    String importantFlags = NativeProjectProvider.SPI_ACCESSOR.getImportantFlags(compilerConfiguration, compiler, makeConfiguration);
                    vec.addAll(IncludePath.toIncludePathList(fs, compiler.getSystemIncludeDirectories(importantFlags)));
                }
            }
        }
        return NativeProjectProvider.SPI_ACCESSOR.expandIncludePaths(vec, compilerConfiguration, compiler, makeConfiguration);
    }

    /**
     * List pre-included system headers.
     * 
     * @return list <FSPath> of pre-included system headers.
     */
    @Override
    public List<FSPath> getSystemIncludeHeaders() {
        List<FSPath> vec = new ArrayList<>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration);//ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) { // FIXUP: sometimes itemConfiguration is null (should not happen)
            return vec;
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return vec;
        }
        AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // Get include paths from compiler
            if (compiler != null && compiler.getPath() != null && compiler.getPath().length() > 0) {
                FileSystem fs = FileSystemProvider.getFileSystem(compiler.getExecutionEnvironment());
                if (makeConfiguration.isMakefileConfiguration()) {
                    vec.addAll(CndFileUtils.toFSPathList(fs, compiler.getSystemIncludeHeaders(getImportantFlags())));
                } else {
                    String importantFlags = NativeProjectProvider.SPI_ACCESSOR.getImportantFlags(compilerConfiguration, compiler, makeConfiguration);
                    vec.addAll(CndFileUtils.toFSPathList(fs, compiler.getSystemIncludeHeaders(importantFlags)));
                }
            }
        }
        return vec;
    }

    @Override
    public List<IncludePath> getUserIncludePaths() {
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration);//ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) { // FIXUP: sometimes itemConfiguration is null (should not happen)
            return Collections.<IncludePath>emptyList();
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return Collections.<IncludePath>emptyList();
        }
        AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // Get include paths from project/file
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration) compilerConfiguration;
            List<List<String>> list = new ArrayList<>();
            for(BasicCompilerConfiguration master : cccCompilerConfiguration.getMasters(true)) {
                list.add(((CCCCompilerConfiguration)master).getIncludeDirectories().getValue());
                if (!((CCCCompilerConfiguration)master).getInheritIncludes().getValue()) {
                    break;
                }
            }
            List<String> vec2 = new ArrayList<>();
            for(int i = list.size() - 1; i >= 0; i--) {
                vec2.addAll(list.get(i));
            }
            ExecutionEnvironment env = compiler.getExecutionEnvironment();            
            MacroConverter macroConverter = null;
            // Convert all paths to absolute paths
            FileSystem compilerFS = FileSystemProvider.getFileSystem(env);
            FileSystem projectFS = fileSystem;
            List<IncludePath> result = new ArrayList<>();            
            for (String p : vec2) {
                boolean compilerContext = false;
                if (p.contains("$")) { // NOI18N
                    // macro based path
                    if (macroConverter == null) {
                        macroConverter = new MacroConverter(env);
                    }
                    p = macroConverter.expand(p);
                    compilerContext = true;
                }
                if (p.startsWith("///")) { //NOI18N
                    // It is absolute path onbuild host
                    compilerContext = true;
                }
                if (compilerContext && CndPathUtilities.isPathAbsolute(compilerFS, p)) {
                    result.add(IncludePath.toIncludePath(compilerFS, p));
                    continue;
                }
                if (CndPathUtilities.isPathAbsolute(projectFS, p)) {
                    result.add(IncludePath.toIncludePath(projectFS, p));
                } else {
                    String absPath = CndPathUtilities.toAbsolutePath(getFolder().getConfigurationDescriptor().getBaseDirFileObject(), p);
                    result.add(IncludePath.toIncludePath(projectFS, absPath));
                }
            }
            List<IncludePath> vec3 = new ArrayList<>();
            vec3 = NativeProjectProvider.SPI_ACCESSOR.getItemUserIncludePaths(vec3, cccCompilerConfiguration, compiler, makeConfiguration);
            result.addAll(vec3);
            return NativeProjectProvider.SPI_ACCESSOR.expandIncludePaths(result, cccCompilerConfiguration, compiler, makeConfiguration);
        }
        return Collections.<IncludePath>emptyList();
    }

    @Override
    public List<FSPath> getIncludeFiles() {
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration);//ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) { // FIXUP: sometimes itemConfiguration is null (should not happen)
            return Collections.<FSPath>emptyList();
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return Collections.<FSPath>emptyList();
        }
        AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            // Get include paths from project/file
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration) compilerConfiguration;
            List<List<String>> list = new ArrayList<>();
            for(BasicCompilerConfiguration master : cccCompilerConfiguration.getMasters(true)) {
                list.add(((CCCCompilerConfiguration)master).getIncludeFiles().getValue());
                if (!((CCCCompilerConfiguration)master).getInheritFiles().getValue()) {
                    break;
                }
            }
            List<String> vec2 = new ArrayList<>();
            for(int i = list.size() - 1; i >= 0; i--) {
                vec2.addAll(list.get(i));
            }
            ExecutionEnvironment env = compiler.getExecutionEnvironment();            
            FileSystem compilerFS = FileSystemProvider.getFileSystem(env);
            FileSystem projectFS = fileSystem;
            MacroConverter macroConverter = null;
            List<FSPath> result = new ArrayList<>();
            for (String p : vec2) {
                boolean compilerContext = false;
                if (p.contains("$")) { // NOI18N
                    // macro based path
                    if (macroConverter == null) {
                        macroConverter = new MacroConverter(env);
                    }
                    p = macroConverter.expand(p);
                    compilerContext = true;
                }
                if (p.startsWith("///")) { //NOI18N
                    // It is absolute path onbuild host
                    compilerContext = true;
                }
                if (compilerContext && CndPathUtilities.isPathAbsolute(compilerFS, p)) {
                    result.add(new FSPath(compilerFS, p));
                    continue;
                }
                if (CndPathUtilities.isPathAbsolute(projectFS, p)) {
                    result.add(new FSPath(projectFS, p));
                } else {
                    String absPath = CndPathUtilities.toAbsolutePath(getFolder().getConfigurationDescriptor().getBaseDirFileObject(), p);
                    result.add(new FSPath(projectFS, absPath));
                }
            }
            return result;
        }
        return Collections.<FSPath>emptyList();
    }

    private List<String> getCompilerPreprocessorSymbols() {
        List<String> vec = new ArrayList<>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration);
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) {
            return vec;
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return vec;
        }
        AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            if (compiler != null && compiler.getPath() != null && compiler.getPath().length() > 0) {
                vec.addAll(compiler.getSystemPreprocessorSymbols());
            }
        }
        return vec;
    }
    
    @Override
    public List<String> getSystemMacroDefinitions() {
        List<String> vec = new ArrayList<>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) // FIXUP: itemConfiguration should never be null
        {
            return vec;
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return vec;
        }
        AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            if (compiler != null && compiler.getPath() != null && compiler.getPath().length() > 0) {
                if (makeConfiguration.isMakefileConfiguration()) {
                     vec.addAll(compiler.getSystemPreprocessorSymbols(getImportantFlags()));
                } else {
                    String importantFlags = NativeProjectProvider.SPI_ACCESSOR.getImportantFlags(compilerConfiguration, compiler, makeConfiguration);
                    vec.addAll(compiler.getSystemPreprocessorSymbols(importantFlags));
                }
            }
        }
        List<String> undefinedMacros = getUndefinedMacros();
        if (undefinedMacros.size() > 0) {
            List<String> out = new ArrayList<>();
            for(String macro : vec) {
                boolean remove = true;
                for(String undef : undefinedMacros) {
                    if (macro.equals(undef) ||
                        macro.startsWith(undef+"=")) { //NOI18N
                        remove = false;
                        break;
                    }
                }
                if (remove) {
                    out.add(macro);
                }
            }
            vec = out;
        }
        return vec;
    }

    @Override
    public List<String> getUserMacroDefinitions() {
        List<String> vec = new ArrayList<>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) { // FIXUP: itemConfiguration should never be null
            return vec;
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return vec;
        }
        AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            Map<String, String> res = new LinkedHashMap<>();
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration) compilerConfiguration;
            if (cccCompilerConfiguration.getInheritPreprocessor().getValue()) {
                for(BasicCompilerConfiguration master : cccCompilerConfiguration.getMasters(false)) {
                    addToMap(res, ((CCCCompilerConfiguration)master).getPreprocessorConfiguration().getValue(), false);
                    if (!((CCCCompilerConfiguration)master).getInheritPreprocessor().getValue()) {
                        break;
                    }

                }
            }
            addToMap(res, cccCompilerConfiguration.getPreprocessorConfiguration().getValue(), true);
            addToList(res, vec);
            vec = NativeProjectProvider.SPI_ACCESSOR.getItemUserMacros(vec, cccCompilerConfiguration, compiler, makeConfiguration);
        }
        return vec;
    }
    
    public List<String> getUndefinedMacros() {
        List<String> vec = new ArrayList<>();
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) { // FIXUP: itemConfiguration should never be null
            return vec;
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return vec;
        }
        AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
        if (compilerConfiguration instanceof CCCCompilerConfiguration) {
            CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration) compilerConfiguration;
            for(BasicCompilerConfiguration master : cccCompilerConfiguration.getMasters(true)) {
                vec.addAll(((CCCCompilerConfiguration)master).getUndefinedPreprocessorConfiguration().getValue());
                if (!((CCCCompilerConfiguration)master).getInheritUndefinedPreprocessor().getValue()) {
                    break;
                }
            }
        }
        return vec;
    }
    
    public String getImportantFlags() {
        String res = getImportantFlagsImpl();
        if (res.isEmpty()) {
            // important flags were lost or user set language standard
            // try to restore right important flag by standard
            switch (getLanguageFlavor()) {
                case C89:   return "-std=c89"; //NOI18N
                case C99:   return "-std=c99"; //NOI18N
                case C11:   return "-std=c11"; //NOI18N
                case C17:   return "-std=c17"; //NOI18N
                case C23:   return "-std=c2x"; //NOI18N
                case CPP98: return "-std=c++98"; //NOI18N
                case CPP11: return "-std=c++11"; //NOI18N
                case CPP14: return "-std=c++14"; //NOI18N
                case CPP17: return "-std=c++17"; //NOI18N
                case CPP20: return "-std=c++20"; //NOI18N
                case CPP23: return "-std=c++23"; //NOI18N
            }
        } 
        return res;
    }
    
    private String getImportantFlagsImpl() {
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        ItemConfiguration itemConfiguration = getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        if (itemConfiguration == null || !itemConfiguration.isCompilerToolConfiguration()) { // FIXUP: itemConfiguration should never be null
            return "";
        }
        CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return "";
        }
        if (makeConfiguration.isMakefileConfiguration()) {
            BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
            if (compilerConfiguration instanceof CCCCompilerConfiguration) {
                CCCCompilerConfiguration cccCompilerConfiguration = (CCCCompilerConfiguration) compilerConfiguration;
                return cccCompilerConfiguration.getImportantFlags().getValue();
            }
        } else {
            AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
            BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
            if (compilerConfiguration instanceof CCCCompilerConfiguration) {
                // Get include paths from compiler
                if (compiler != null && compiler.getPath() != null && compiler.getPath().length() > 0) {
                    final String importantFlags = NativeProjectProvider.SPI_ACCESSOR.getImportantFlags(compilerConfiguration, compiler, makeConfiguration);
                    if (importantFlags != null) {
                        return importantFlags;
                    }
                }
            }
        }
        
        return "";
    }
    
    private void addToMap(Map<String, String> res, List<String> list, boolean override) {
        list.forEach((macro) -> {
            int i = macro.indexOf('=');
            String key;
            String value;
            if ( i > 0){
                key = macro.substring(0,i).trim();
                value = macro.substring(i+1).trim();
            } else {
                key = macro;
                value = null;
            }
            if (!res.containsKey(key) || override) {
                res.put(key, value);
            }
        });
    }
    
    private void addToList(Map<String, String> res, List<String> list) {
        res.entrySet().forEach((e) -> {
            if (e.getValue() == null) {
                list.add(e.getKey());
            } else {
                list.add(e.getKey()+"="+e.getValue()); //NOI18N
            }
        });
    }

    public boolean hasHeaderOrSourceExtension(boolean cFiles, boolean ccFiles) {
        // Method return true for source files also.
        String mimeType = getMIMEType();
        return MIMENames.HEADER_MIME_TYPE.equals(mimeType)
                || (ccFiles && MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType))
                || (cFiles && MIMENames.C_MIME_TYPE.equals(mimeType));
    }

    /**
     * NativeFileItem interface
     **/
    @Override
    public Language getLanguage() {
        PredefinedToolKind tool;
        Language language;
        ItemConfiguration itemConfiguration = null;
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            itemConfiguration = getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(getPath()));
        }
        if (itemConfiguration != null) {
            tool = itemConfiguration.getTool();
        } else {
            tool = getDefaultTool();
        }

        if (tool == PredefinedToolKind.CCompiler) {
            language = NativeFileItem.Language.C;
        } else if (tool == PredefinedToolKind.CCCompiler) {
            language = NativeFileItem.Language.CPP;
        } else if (tool == PredefinedToolKind.FortranCompiler) {
            language = NativeFileItem.Language.FORTRAN;
        } else if (hasHeaderOrSourceExtension(true, true)) {
            language = NativeFileItem.Language.C_HEADER;
        } else {
            language = NativeFileItem.Language.OTHER;
        }

        return language;
    }

    /**
     * NativeFileItem interface
     **/
    @Override
    public LanguageFlavor getLanguageFlavor() {
        LanguageFlavor flavor = LanguageFlavor.UNKNOWN;
        ItemConfiguration itemConfiguration = null;
        MakeConfiguration makeConfiguration = getMakeConfiguration();
        if (makeConfiguration != null) {
            itemConfiguration = getItemConfiguration(makeConfiguration);
        }
        if (itemConfiguration != null && itemConfiguration.isCompilerToolConfiguration()) {
            flavor = itemConfiguration.getLanguageFlavor();
            if (flavor == LanguageFlavor.UNKNOWN || flavor == LanguageFlavor.DEFAULT) {
                CompilerSet compilerSet = makeConfiguration.getCompilerSet().getCompilerSet();
                if (compilerSet != null) {
                    Tool tool = compilerSet.getTool(itemConfiguration.getTool());
                    if (tool instanceof AbstractCompiler) {
                        AbstractCompiler compiler = (AbstractCompiler) tool;
                        if (itemConfiguration.isCompilerToolConfiguration()) {
                            BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                            if (compilerConfiguration != null) {
                                LanguageFlavor aFlavor = NativeProjectProvider.SPI_ACCESSOR.getLanguageFlavor(compilerConfiguration, compiler, makeConfiguration);
                                if (aFlavor != LanguageFlavor.UNKNOWN) {
                                    flavor = aFlavor;
                                }
                            }
                        }
                    }
                }
            }
             if (flavor == LanguageFlavor.UNKNOWN) {
                if (itemConfiguration.getTool() == PredefinedToolKind.CCompiler) {
                    switch (itemConfiguration.getCCompilerConfiguration().getInheritedCStandard()) {
                        case CCompilerConfiguration.STANDARD_C89:
                            return LanguageFlavor.C89;
                        case CCompilerConfiguration.STANDARD_C99:
                            return LanguageFlavor.C99;
                        case CCompilerConfiguration.STANDARD_C11:
                            return LanguageFlavor.C11;
                        case CCompilerConfiguration.STANDARD_C17:
                            return LanguageFlavor.C17;
                        case CCompilerConfiguration.STANDARD_C23:
                            return LanguageFlavor.C23;
                        case CCompilerConfiguration.STANDARD_DEFAULT:
                            for(String macro : getCompilerPreprocessorSymbols()) {
                                if (macro.startsWith("__STDC_VERSION__")) {  //NOI18N
                                    if (macro.endsWith("201710L")) {
                                        return LanguageFlavor.C17;
                                    } else if (macro.endsWith("201112L")) {
                                        return LanguageFlavor.C11;
                                    } else if (macro.endsWith("199901L")) {
                                        return LanguageFlavor.C99;
                                    } else if (macro.endsWith("199409L")) {
                                        return LanguageFlavor.C89;
                                    }
                                } else if (macro.startsWith("_STDC_C99=")) { //NOI18N
                                    return LanguageFlavor.C99;
                                } else if (macro.startsWith("_STDC_C11=")) { //NOI18N
                                    return LanguageFlavor.C11;
                                }
                            }
                            return LanguageFlavor.C89;
                    }
                } else if (itemConfiguration.getTool() == PredefinedToolKind.CCCompiler) {
                    switch (itemConfiguration.getCCCompilerConfiguration().getInheritedCppStandard()) {
                        case CCCompilerConfiguration.STANDARD_CPP98:
                            return LanguageFlavor.CPP98;
                        case CCCompilerConfiguration.STANDARD_CPP11:
                            return LanguageFlavor.CPP11;
                        case CCCompilerConfiguration.STANDARD_CPP14:
                            return LanguageFlavor.CPP14;
                        case CCCompilerConfiguration.STANDARD_CPP17:
                            return LanguageFlavor.CPP17;
                        case CCCompilerConfiguration.STANDARD_CPP20:
                            return LanguageFlavor.CPP20;
                        case CCCompilerConfiguration.STANDARD_CPP23:
                            return LanguageFlavor.CPP23;
                        case CCCompilerConfiguration.STANDARD_DEFAULT:
                            for(String macro : getCompilerPreprocessorSymbols()) {
                                if (macro.startsWith("__cplusplus=")) { //NOI18N
                                    if (macro.endsWith("202302L")) {
                                        return LanguageFlavor.CPP23;
                                    } else if (macro.endsWith("202002L")) {
                                        return LanguageFlavor.CPP20;
                                    } else if (macro.endsWith("201703L")) {
                                        return LanguageFlavor.CPP17;
                                    } else if (macro.endsWith("201402L")) {
                                        return LanguageFlavor.CPP14;
                                    } else if (macro.endsWith("201103L")) {
                                        return LanguageFlavor.CPP11;
                                    } else if (macro.endsWith("199711L")) {
                                        return LanguageFlavor.CPP98;
                                    }
                                }
                            }
                            return LanguageFlavor.DEFAULT;
                    }
                }
            }            
        }
        
        if (flavor == LanguageFlavor.UNKNOWN) {
            if (makeConfiguration != null) {
                CCCompilerConfiguration ccCompilerConfiguration = makeConfiguration.getCCCompilerConfiguration();
                if(ccCompilerConfiguration != null) {
                    switch (ccCompilerConfiguration.getInheritedCppStandard()) {
                        case CCCompilerConfiguration.STANDARD_CPP98:
                            return LanguageFlavor.CPP98;
                        case CCCompilerConfiguration.STANDARD_CPP11:
                            return LanguageFlavor.CPP11;
                        case CCCompilerConfiguration.STANDARD_CPP14:
                            return LanguageFlavor.CPP14;
                        case CCCompilerConfiguration.STANDARD_CPP17:
                            return LanguageFlavor.CPP17;
                        case CCCompilerConfiguration.STANDARD_CPP20:
                            return LanguageFlavor.CPP20;
                        case CCCompilerConfiguration.STANDARD_CPP23:
                            return LanguageFlavor.CPP23;
                    }
                }
            }
        }
        
        return flavor;
    }

    /**
     * NativeFileItem interface
     **/
    @Override
    public boolean isExcluded() {
        ItemConfiguration itemConfiguration = getItemConfiguration(getMakeConfiguration());
        if (itemConfiguration != null) {
            boolean value = itemConfiguration.getExcluded().getValue();
            if (value) {
                if (getMakeConfiguration().getCodeAssistanceConfiguration().includeInCA(this)) {
                    return false;
                }
            } else {
                if (getMakeConfiguration().getCodeAssistanceConfiguration().excludeInCA(this)) {
                    return true;
                }
            }
            return value;
        }
        return true;
    }

    @Override
    public String toString() {
        return path.toString();
    }
    public boolean hasImportantAttributes() {
        for (ItemConfiguration conf : getItemConfigurations()) {
            if (conf != null && !conf.isDefaultConfiguration() ) {
                return true;
            }
        }
        return false;
    }

    protected void onAddedToFolder(Folder folder) {
    }
}
