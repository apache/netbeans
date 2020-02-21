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
