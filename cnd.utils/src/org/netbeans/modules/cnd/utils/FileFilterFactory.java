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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
