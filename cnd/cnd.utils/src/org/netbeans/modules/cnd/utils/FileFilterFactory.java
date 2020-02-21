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

package org.netbeans.modules.cnd.utils;

import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.filters.AllBinaryFileFilter;
import org.netbeans.modules.cnd.utils.filters.AllFileFilter;
import org.netbeans.modules.cnd.utils.filters.AllLibraryFileFilter;
import org.netbeans.modules.cnd.utils.filters.AllSourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.CCSourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.CMakeFileFilter;
import org.netbeans.modules.cnd.utils.filters.CSourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.ConfigureFileFilter;
import org.netbeans.modules.cnd.utils.filters.CoreFileFilter;
import org.netbeans.modules.cnd.utils.filters.ElfDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.utils.filters.ElfExecutableFileFilter;
import org.netbeans.modules.cnd.utils.filters.ElfStaticLibraryFileFilter;
import org.netbeans.modules.cnd.utils.filters.FortranSourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.HeaderSourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.MacOSXDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.utils.filters.MacOSXExecutableFileFilter;
import org.netbeans.modules.cnd.utils.filters.MakefileFileFilter;
import org.netbeans.modules.cnd.utils.filters.PeDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.utils.filters.PeExecutableFileFilter;
import org.netbeans.modules.cnd.utils.filters.PeStaticLibraryFileFilter;
import org.netbeans.modules.cnd.utils.filters.QMakeFileFilter;
import org.netbeans.modules.cnd.utils.filters.QtFileFilter;
import org.netbeans.modules.cnd.utils.filters.ResourceFileFilter;
import org.netbeans.modules.cnd.utils.filters.SconsFileFilter;
import org.netbeans.modules.cnd.utils.filters.ShellFileFilter;
import org.netbeans.modules.cnd.utils.filters.WorkshopProjectFilter;
import org.openide.filesystems.FileSystem;

/**
 *
 */
public final class FileFilterFactory {

    public static abstract class AbstractFileAndFileObjectFilter
            extends FileFilter implements FileObjectFilter {
        public abstract String getSuffixesAsString();
    }
    private FileFilterFactory() {
    }

    @Deprecated
    public static FileFilter[] getLibraryFilters() {
        return getLibraryFilters(null);
    }

    public static FileFilter[] getLibraryFilters(FileSystem fs) {
        FileFilter[] filters = null;
        if (CndFileSystemProvider.isWindows(fs)) {
            filters = new FileFilter[]{
                        FileFilterFactory.getAllLibraryFileFilter(),
                        FileFilterFactory.getElfStaticLibraryFileFilter(),
                        FileFilterFactory.getPeDynamicLibraryFileFilter()
                    };
        } else if (CndFileSystemProvider.isMacOS(fs)) {
            filters = new FileFilter[]{
                        FileFilterFactory.getAllLibraryFileFilter(),
                        FileFilterFactory.getElfStaticLibraryFileFilter(),
                        FileFilterFactory.getMacOSXDynamicLibraryFileFilter()
                    };
        } else {
            filters = new FileFilter[]{
                        FileFilterFactory.getAllLibraryFileFilter(),
                        FileFilterFactory.getElfStaticLibraryFileFilter(),
                        FileFilterFactory.getElfDynamicLibraryFileFilter()
                    };
        }
        return filters;
    }

    @Deprecated
    public static FileFilter[] getBinaryFilters() {
        return getBinaryFilters(null);
    }
    
    public static FileFilter[] getBinaryFilters(FileSystem fs) {
        FileFilter[] filters = null;
        if (CndFileSystemProvider.isWindows(fs)) {
            filters = new FileFilter[]{
                        FileFilterFactory.getAllBinaryFileFilter(),
                        FileFilterFactory.getPeExecutableFileFilter(),
                        FileFilterFactory.getElfStaticLibraryFileFilter(),
                        FileFilterFactory.getPeDynamicLibraryFileFilter()
                    };
        } else if (CndFileSystemProvider.isMacOS(fs)) {
            filters = new FileFilter[]{
                        FileFilterFactory.getAllBinaryFileFilter(),
                        FileFilterFactory.getMacOSXExecutableFileFilter(),
                        FileFilterFactory.getElfStaticLibraryFileFilter(),
                        FileFilterFactory.getMacOSXDynamicLibraryFileFilter()
                    };
        } else {
            filters = new FileFilter[]{
                        FileFilterFactory.getAllBinaryFileFilter(),
                        FileFilterFactory.getElfExecutableFileFilter(),
                        FileFilterFactory.getElfStaticLibraryFileFilter(),
                        FileFilterFactory.getElfDynamicLibraryFileFilter()
                    };
        }
        return filters;
    }
    
    public static AbstractFileAndFileObjectFilter getAllFileFilter(){
        return AllFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getAllSourceFileFilter(){
        return AllSourceFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getAllBinaryFileFilter(){
        return AllBinaryFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getAllLibraryFileFilter(){
        return AllLibraryFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getCCSourceFileFilter(){
        return CCSourceFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getCSourceFileFilter(){
        return CSourceFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getConfigureFileFilter(){
        return ConfigureFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getCMakeFileFilter(){
        return CMakeFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getQMakeFileFilter(){
        return QMakeFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getElfDynamicLibraryFileFilter(){
        return ElfDynamicLibraryFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getElfExecutableFileFilter(){
        return ElfExecutableFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getElfStaticLibraryFileFilter(){
        return ElfStaticLibraryFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getFortranSourceFileFilter(){
        return FortranSourceFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getHeaderSourceFileFilter(){
        return HeaderSourceFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getMacOSXDynamicLibraryFileFilter(){
        return MacOSXDynamicLibraryFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getMacOSXExecutableFileFilter(){
        return MacOSXExecutableFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getMakefileFileFilter(){
        return MakefileFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getSconsFileFilter(){
        return SconsFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getPeDynamicLibraryFileFilter(){
        return PeDynamicLibraryFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getPeExecutableFileFilter(){
        return PeExecutableFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getPeStaticLibraryFileFilter(){
        return PeStaticLibraryFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getQtFileFilter(){
        return QtFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getResourceFileFilter(){
        return ResourceFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getShellFileFilter(){
        return ShellFileFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getWorkshopProjectFilter(){
        return WorkshopProjectFilter.getInstance();
    }
    public static AbstractFileAndFileObjectFilter getCoreFileFilter(){
        return CoreFileFilter.getInstance();
    }
}
