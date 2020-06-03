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

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.wizards.CommonUtilities;
import org.netbeans.modules.cnd.makeproject.configurations.CppUtils;
import org.netbeans.modules.cnd.makeproject.platform.StdLibraries;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public abstract class LibraryItem implements Cloneable {
    public static final int PROJECT_ITEM = 0;
    public static final int STD_LIB_ITEM = 1;
    public static final int LIB_ITEM = 2;
    public static final int LIB_FILE_ITEM = 3;
    public static final int OPTION_ITEM = 4;

    private int type;

    private LibraryItem() {
    }

    public int getType() {
	return type;
    }

    public void setType(int type) {
	this.type = type;
    }

    // Should be overridden
    public String getDescription() {
	return "Should be overridden"; // NOI18N
    }

    // Should be overridden
    public void setValue(String value) {
    }

    // Should be overridden
    public String getPath() {
        return null;
    }

    // Should be overridden
    @Override
    public String toString() {
	return "Should be overridden"; // NOI18N
    }

    // Should be overridden
//    public String getOption() {
//	return ""; // NOI18N
//    }
    
    // Must be overridden
    public abstract String getOption(MakeConfiguration conf);

    // Should be overridden
    public boolean canEdit() {
	return false;
    }

    // Should be overridden
    @Override
    public LibraryItem clone() {
	return this;
    }

    public static class ProjectItem extends LibraryItem implements Cloneable {
	private MakeArtifact makeArtifact;
	private Project project; // Just for caching

	public ProjectItem(MakeArtifact makeArtifact) {
	    this.makeArtifact = makeArtifact;
	    setType(PROJECT_ITEM);
	}

	public MakeArtifact getMakeArtifact() {
	    return makeArtifact;
	}

	public void setMakeArtifact(MakeArtifact makeArtifact) {
	    this.makeArtifact = makeArtifact;
	}

	public Project getProject(FSPath baseDir) {
	    if (project == null) {
		String location = CndPathUtilities.toAbsolutePath(baseDir.getFileObject(), getMakeArtifact().getProjectLocation());
		try {
		    FileObject fo = RemoteFileUtil.getFileObject(baseDir.getFileObject(), location);
                    if (fo != null && fo.isValid()) {
                        fo = CndFileUtils.getCanonicalFileObject(fo);
                    }
                    if (fo != null && fo.isValid()) {
                        project = ProjectManager.getDefault().findProject(fo);
                    }
		}
		catch (Exception e) {
		    System.err.println("Cannot find subproject in '"+location+"' "+e); // FIXUP // NOI18N
		}
	    }
	    return project;
	}

        @Override
	public String getDescription() {
            String ret = NbBundle.getMessage(LibraryItem.class, "ProjectTxt", getMakeArtifact().getProjectLocation()); // NOI18N
            if (getMakeArtifact().getOutput() != null && getMakeArtifact().getOutput().length() > 0) {
                ret = ret + " (" + getMakeArtifact().getOutput() + ")"; // NOI18N
            }
            return ret;
	}

        @Override
        public String toString() {
            String ret = CndPathUtilities.getBaseName(getMakeArtifact().getProjectLocation());
            if (getMakeArtifact().getOutput() != null && getMakeArtifact().getOutput().length() > 0) {
                ret = ret + " (" + getMakeArtifact().getOutput() + ")"; // NOI18N
            }
            return ret;
        }

        @Override
        public void setValue(String value) {
            // Can't do
        }

        @Override
        public String getPath() {
            String libPath = getMakeArtifact().getOutput();
            if (!CndPathUtilities.isPathAbsolute(libPath)) {
                libPath = getMakeArtifact().getProjectLocation() + '/' + libPath; // UNIX path
            }
            return libPath;
        }

        @Override
        public String getOption(MakeConfiguration conf) {
            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            Platform platform = Platforms.getPlatform(conf.getDevelopmentHost().getBuildPlatform());
            IntConfiguration librariesRunTimeSearchPathKind = conf.getLinkerConfiguration().getLibrariesRunTimeSearchPathKind();
            String libPath = getPath();
            String libDir = CndPathUtilities.getDirName(libPath);
            String libName = CndPathUtilities.getBaseName(libPath);
            String libSearchPath;
            switch (librariesRunTimeSearchPathKind.getValue()) {
                case LinkerConfiguration.SEARCH_PATH_RELATIVE_TO_WORKING_DIR:
                    libSearchPath = libDir;
                    break;
                case LinkerConfiguration.SEARCH_PATH_RELATIVE_TO_BINARY:{
                    StringBuilder buf = new StringBuilder();
                    buf.append('$').append(CommonUtilities.ORIGIN).append('/'); // NOI18N
                    buf.append(CndPathUtilities.getRelativePath(CndPathUtilities.getDirName(conf.getAbsoluteOutputValue()), conf.getMakefileConfiguration().getAbsBuildCommandWorkingDir()));
                    buf.append('/'); // NOI18N
                    buf.append(libDir);
                    libSearchPath = CndPathUtilities.normalizeSlashes(buf.toString());
                    break;}
                case LinkerConfiguration.SEARCH_PATH_ABSOLUTE:{
                    StringBuilder buf = new StringBuilder(conf.getMakefileConfiguration().getAbsBuildCommandWorkingDir());
                    buf.append('/');
                    buf.append(libDir);
                    libSearchPath = CndPathUtilities.normalizeUnixPath(CndPathUtilities.normalizeSlashes(buf.toString()));
                    break;}
                case LinkerConfiguration.SEARCH_PATH_NONE:
                default:
                    libSearchPath = null;
                    break;
            }
            return platform.getLibraryLinkOption(libName, libDir, libPath, libSearchPath, compilerSet);
        }

        @Override
	public boolean canEdit() {
	    return false;
	}

        @Override
	public ProjectItem clone() {
	    ProjectItem clone = new ProjectItem(getMakeArtifact());
	    return clone;
	}
    }

    public static class StdLibItem extends LibraryItem implements Cloneable {
	private final String name;
	private final String displayName;
	private final String[] libs;

	public StdLibItem(String name, String displayName, String[] libs) {
            this.name = name;
            this.displayName = displayName;
            this.libs = libs;
            setType(STD_LIB_ITEM);
        }

        public static StdLibItem getStandardItem(String id) {
            return StdLibraries.getStandardLibary(id);
        }

        public String getName() {
            return name;
        }

	public String getDisplayName() {
	    return displayName;
	}

	public String[] getLibs() {
	    return libs;
	}

        @Override
	public String getDescription() {
            StringBuilder options = new StringBuilder();
            for (int i = 0; i < libs.length; i++) {
                if (options.length()>0) {
                    options.append(' '); // NOI18N
                }
                options.append(libs[i]); // NOI18N
            }
	    return NbBundle.getMessage(LibraryItem.class, "StandardLibraryTxt", getDisplayName(), options.toString()); // NOI18N
	}

        @Override
	public String toString() {
	    return getDisplayName();
	}

        @Override
	public void setValue(String value) {
	    // Can't do
	}

        @Override
        public String getOption(MakeConfiguration conf) {
            StringBuilder options = new StringBuilder();
            String flag = null;
            for (int i = 0; i < libs.length; i++) {
                if (libs[i].charAt(0) != '-') {
                    if (flag == null) {
                        CompilerSet cs = conf.getCompilerSet().getCompilerSet();
                        if (cs != null) {
                            flag = cs.getCompilerFlavor().getToolchainDescriptor().getLinker().getLibraryFlag();
                        }
                    }
                    if (flag != null) {
                        options.append(flag).append(libs[i]).append(" "); // NOI18N
                    }
                } else {
                    options.append(libs[i]).append(" "); // NOI18N
                }
            }
            return options.toString();
        }

        @Override
	public boolean canEdit() {
	    return false;
	}

        @Override
	public StdLibItem clone() {
	    StdLibItem clone = new StdLibItem(getName(), getDisplayName(), getLibs());
	    return clone;
	}
    }

    public static class LibItem extends LibraryItem implements Cloneable {
	private String libName;

	public LibItem(String libName) {
	    this.libName = libName;
	    setType(LIB_ITEM);
	}

	public String getLibName() {
	    return libName;
	}

	public void setLibName(String libName) {
	    this.libName = libName;
	}

        @Override
	public String getDescription() {
	    return NbBundle.getMessage(LibraryItem.class, "LibraryTxt", getLibName()); // NOI18N
	}

        @Override
	public String toString() {
	    return getLibName();
	}

        @Override
	public void setValue(String value) {
	    setLibName(value);
	}

        @Override
	public String getOption(MakeConfiguration conf) {
            CompilerSet cs = conf.getCompilerSet().getCompilerSet();
            if (cs != null) {
                String lib = getLibName();
                lib = CndPathUtilities.quoteIfNecessary(CppUtils.normalizeDriveLetter(cs, lib));
                return cs.getCompilerFlavor().getToolchainDescriptor().getLinker().getLibraryFlag() + lib;
            }
	    return ""; // NOI18N
	}

        @Override
	public boolean canEdit() {
	    return true;
	}

        @Override
	public LibItem clone() {
	    return new LibItem(getLibName());
	}
    }

    public static class LibFileItem extends LibraryItem implements Cloneable {
	private String path;

	public LibFileItem(String path) {
	    this.path = path;
	    setType(LIB_FILE_ITEM);
	}

        @Override
	public String getPath() {
	    return path;
	}

	public void setPath(String path) {
	    this.path = path;
	}

        @Override
	public String getDescription() {
	    return NbBundle.getMessage(LibraryItem.class, "LibraryFileTxt", getPath()); // NOI18N
	}

        @Override
	public String toString() {
	    return getPath();
	}

        @Override
	public void setValue(String value) {
	    setPath(value);
	}

        @Override
	public String getOption(MakeConfiguration conf) {
            String lpath = getPath();
            if (conf != null) {
                CompilerSet cs = conf.getCompilerSet().getCompilerSet();
                lpath = CndPathUtilities.quoteIfNecessary(CppUtils.normalizeDriveLetter(cs, lpath));
            }
	    return lpath;
	}

        @Override
	public boolean canEdit() {
	    return true;
	}

        @Override
	public LibFileItem clone() {
	    return new LibFileItem(getPath());
	}
    }

    public static class OptionItem extends LibraryItem implements Cloneable {
	private String libraryOption;

	public OptionItem(String libraryOption) {
	    this.libraryOption = libraryOption;
	    setType(OPTION_ITEM);
	}

	public String getLibraryOption() {
	    return libraryOption;
	}

	public void setLibraryOption(String libraryOption) {
	    this.libraryOption = libraryOption;
	}

        @Override
	public String getDescription() {
	    return NbBundle.getMessage(LibraryItem.class, "LibraryOptionTxt", getLibraryOption()); // NOI18N
	}

        @Override
	public String toString() {
	    return getLibraryOption();
	}

        @Override
	public void setValue(String value) {
	    setLibraryOption(value);
	}

        @Override
	public String getOption(MakeConfiguration conf) {
	    return getLibraryOption();
	}

        @Override
	public boolean canEdit() {
	    return true;
	}

        @Override
	public OptionItem clone() {
	    return new OptionItem(getLibraryOption());
	}
    }
}
