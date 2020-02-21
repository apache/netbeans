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
package org.netbeans.modules.cnd.api.toolchain;

import java.util.List;
import java.util.Map;

/**
 *
 */
public final class ToolchainManager {
    public interface ToolchainDescriptor {

        String getFileName();

        String getName();

        String getDisplayName();

        String[] getFamily();

        String[] getPlatforms();

        String getUpdateCenterUrl();

        String getUpdateCenterDisplayName();

        String getUpgradeUrl();

        String getModuleID();

        boolean isAbstract();

        boolean isAutoDetected();

        String[] getAliases();

        String getSubstitute();

        String getDriveLetterPrefix();

        List<BaseFolder> getBaseFolders();

        List<BaseFolder> getCommandFolders();

        String getQmakeSpec();

        CompilerDescriptor getC();

        CompilerDescriptor getCpp();

        CompilerDescriptor getFortran();

        CompilerDescriptor getAssembler();

        ScannerDescriptor getScanner();

        LinkerDescriptor getLinker();

        MakeDescriptor getMake();

        Map<String, List<String>> getDefaultLocations();

        DebuggerDescriptor getDebugger();

        String getMakefileWriter();

        QMakeDescriptor getQMake();

        CMakeDescriptor getCMake();
    }

    public interface BaseFolder {
        
        String getFolderKey();

        String getFolderPattern();

        String getFolderSuffix();

        String getFolderPathPattern();

        String getRelativePath();
    }

    public interface ToolDescriptor {

        String[] getNames();

        String getVersionFlags();

        String getVersionPattern();

        String getFingerPrintFlags();

        String getFingerPrintPattern();

        boolean skipSearch();

        AlternativePath[] getAlternativePath();
    }

    public interface AlternativePath {
        public enum PathKind {
            PATH,
            TOOL_FAMILY,
            TOOL_NAME
        }
        String getPath();
        PathKind getKind();
    }

    public interface CompilerDescriptor extends ToolDescriptor {

        String getPathPattern();

        String getExistFolder();

        String getIncludeFlags();

        String getUserIncludeFlag();

        String getUserFileFlag();

        String getIncludeParser();

        String getRemoveIncludePathPrefix();

        String getRemoveIncludeOutputPrefix();

        String getImportantFlags();

        String getMacroFlags();

        String getMacroParser();

        List<PredefinedMacro> getPredefinedMacros();

        String getUserMacroFlag();

        String[] getDevelopmentModeFlags();

        String[] getWarningLevelFlags();

        String[] getArchitectureFlags();

        String getStripFlag();

        String[] getMultithreadingFlags();

        String[] getStandardFlags();

        String[] getLanguageExtensionFlags();

        String[] getCppStandardFlags();

        String[] getCStandardFlags();

        String[] getLibraryFlags();

        String getOutputObjectFileFlags();

        String getDependencyGenerationFlags();

        String getPrecompiledHeaderFlags();

        String getPrecompiledHeaderSuffix();

        boolean getPrecompiledHeaderSuffixAppend();
    }

    public interface PredefinedMacro {
        String getMacro();

        boolean isHidden();

        String getFlags();
    }

    public interface MakeDescriptor extends ToolDescriptor {

        String getDependencySupportCode();
    }

    public interface DebuggerDescriptor extends ToolDescriptor {
        String getID();
    }

    public interface QMakeDescriptor extends ToolDescriptor {
    }

    public interface CMakeDescriptor extends ToolDescriptor {
    }

    public interface LinkerDescriptor {

        String getLibraryPrefix();

        String getLibrarySearchFlag();

        String getDynamicLibrarySearchFlag();

        String getLibraryFlag();

        String getPICFlag();

        String getStaticLibraryFlag();

        String getDynamicLibraryFlag();

        String getDynamicLibraryBasicFlag();

        String getOutputFileFlag();

        String getPreferredCompiler();

        String getStripFlag();
    }

    public interface ScannerDescriptor {

        String getID();

	List<ScannerPattern> getPatterns();

        String getChangeDirectoryPattern();

        String getEnterDirectoryPattern();

        String getLeaveDirectoryPattern();

        String getMakeAllInDirectoryPattern();

        List<String> getStackHeaderPattern();

        List<String> getStackNextPattern();

	List<String> getFilterOutPatterns();
    }

    public interface ScannerPattern {

        String getPattern();

        String getSeverity();

        String getLanguage();
    }
}
