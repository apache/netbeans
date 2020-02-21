/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.makefile.wizard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.openide.util.NbBundle;

/**
 * A MakefileData record. This file should contain ALL iformation necessary to
 * create a Makefile.
 */
final public class MakefileData {
    // Makefile Type Values

    public final static int EXECUTABLE_MAKEFILE_TYPE = 1;
    public final static int ARCHIVE_MAKEFILE_TYPE = 2;
    public final static int SHAREDLIB_MAKEFILE_TYPE = 3;
    public final static int COMPLEX_MAKEFILE_TYPE = 4;

    // Makefile Compiler Collection Values
    public final static int SUN_TOOLSET_TYPE = 0;
    public final static int GNU_TOOLSET_TYPE = 1;
    public final static int SUNGNU_TOOLSET_TYPE = 2;

    // Makefile OS Values
    public final static int SOLARIS_OS_TYPE = 0;
    public final static int LINUX_OS_TYPE = 1;
    public final static int UNIX_OS_TYPE = 2;
    public final static int WINDOWS_OS_TYPE = 3;
    public final static int MACOSX_OS_TYPE = 4;

    // Conformance Level C SUN
    private int conformLevelCSun = 1; // Default
    public final static String[] conformLevelsCSun = {
        "-Xc", // ISO C (-Xc) // NOI18N
        "", // ISO C plus K&R C (-Xa) // NOI18N
        "-Xs", // K&R C (-Xs) // NOI18N
    };

    // Conformance Level Cpp SUN
    private int conformLevelCppSun = 1; // Default
    public final static String[] conformLevelsCppSun = {
        "-compat=4", // 4.2 compatible // NOI18N
        "", // Standard // NOI18N
    };

    // Conformance Level C GNU
    private int conformLevelCGNU = 1; // Default
    public final static String[] conformLevelsCGNU = {
        "-ansi", // Ansi // NOI18N
        "", // Ansi plus GNU extentions // NOI18N
    };

    // Conformance Level Cpp GNU
    private int conformLevelCppGNU = 1; // Default
    public final static String[] conformLevelsCppGNU = {
        "-ansi", // Ansi // NOI18N
        "", // Ansi plus GNU extentions // NOI18N
    };
    /** The type of Makefile this data could make (complex, single executbale, ...)*/
    private int makefileType;
    /** The compiler collection this Makefile should support (Sun, GNU, ...)*/
    private int toolset;
    /** The OS this Makefile should support (Solaris, Linux, both, ...)*/
    private int makefileOS;
    /** The directory of the Makefile */
    private String baseDirectory;
    /** The Makefile name */
    private String makefileName;
    /** The Makefile dir name */
    private String makefileDirName;
    /** The targetList contains information about each target */
    private ArrayList<TargetData> targetList;
    /** The compile time flags for this Makefile */
    private CompilerFlags compilerFlags;
    /** Look up i18n strings here */
    private ResourceBundle bundle;
    /** Save the C compiler path */
    private String cCompilerSun;
    private String cCompilerGNU;
    /** Save the C++ compiler path */
    private String cppCompilerSun;
    private String cppCompilerGNU;
    /** Save the Fortran compiler path */
    private String fCompilerSun;
    private String fCompilerGNU;
    /** Save the Fortran compiler type setting */
    private int ftype;
    /** Do we have Fortran 95 files and are we using either MODULE or USE statements? */
    private boolean moduleEnabled;
    /** Save the path to X-Designer */
    private String xdCompiler;
    /** Save the assembler path */
    private String asmPath;
    /** Default C++ Compatability mode */
    public final static int COMPAT_DEFAULT = 0;
    /** Ansi C++ Compatability mode */
    public final static int COMPAT_ANSI = 1;
    /** C++ 4.2 Compatability mode */
    public final static int COMPAT_4_2 = 2;
    /** Expand the base directory */
    public static final boolean EXPAND = true;

    /**
     *  Default constructor for class Makefile. Initialize data to default.
     */
    public MakefileData() {


        makefileType = MakefileData.COMPLEX_MAKEFILE_TYPE;
        toolset = MakefileData.SUNGNU_TOOLSET_TYPE;
        makefileOS = MakefileData.UNIX_OS_TYPE;
        baseDirectory = new String(System.getProperty("user.dir"));	// NOI18N
        makefileName = getString("DFLT_MakefileName");			// NOI18N
        targetList = new ArrayList<TargetData>();
        compilerFlags = new CompilerFlags();
        moduleEnabled = false;
    }

    /**
     *  Create a new Makefile using values from the previously created one. This
     *  ensures we have better defaults than we would if we used compiled in
     *  defaults.
     */
    public MakefileData(MakefileData old) {

        bundle = NbBundle.getBundle(MakefileWizardPanel.class);

        makefileType = old.getMakefileType();
        toolset = old.getToolset();
        makefileOS = old.getMakefileOS();
        baseDirectory = old.getBaseDirectory();
        makefileName = old.getMakefileName();
        makefileDirName = old.getMakefileDirName();
        targetList = new ArrayList<TargetData>();
        compilerFlags = new CompilerFlags();
    }

    /**
     *  Compute the full Makefile path from the name and base.
     */
    public String getMakefilePath() {
        String name = CndPathUtilities.expandPath(makefileName);
        String path;

        if (name.charAt(0) == File.separatorChar) {
            path = makefileName;
        } else {
            File file = new File(getBaseDirectory(EXPAND), name);
            try {
                path = file.getCanonicalPath();
            } catch (IOException ex) {
                path = file.getAbsolutePath();
            }
        }

        return path;
    }

    /**
     * Compute default output directory
     */
    public String defaultOutputDirectory() {
        String toolsetName = "toolset"; // NOI18N
        String OS = "os"; // NOI18N
        String dir = null;

        if (getToolset() == SUN_TOOLSET_TYPE) {
            toolsetName = "Sun"; // NOI18N
        } else if (getToolset() == GNU_TOOLSET_TYPE) {
            toolsetName = "GNU"; // NOI18N
        }

        if (getMakefileOS() == SOLARIS_OS_TYPE) {
            OS = "Solaris"; // NOI18N
        } else if (getMakefileOS() == LINUX_OS_TYPE) {
            OS = "Linux"; // NOI18N
        } else if (getMakefileOS() == WINDOWS_OS_TYPE) {
            OS = "Windows"; // NOI18N
        } else if (getMakefileOS() == MACOSX_OS_TYPE) {
            OS = "MacOSX"; // NOI18N
        }

        dir = toolsetName + "-" + System.getProperty("os.arch") + "-" + OS; // NOI18N
        return dir;
    }

    /** Get the type of Makefile being created */
    public int getMakefileType() {
        return makefileType;
    }

    /** Set the type of Makefile to be created */
    public void setMakefileType(int makefileType) {
        this.makefileType = makefileType;
    }

    /** Get compiler conformance flag C SUN */
    public String getConformFlagCSun() {
        return conformLevelsCSun[conformLevelCSun];
    }

    /** Get the type of compiler conformance level C SUN */
    public int getConformLevelCSun() {
        return conformLevelCSun;
    }

    /** Set the type of compiler conformance level C SUN */
    public void setConformLevelCSun(int level) {
        this.conformLevelCSun = level;
    }

    /** Get compiler conformance flag Cpp SUN */
    public String getConformFlagCppSun() {
        return conformLevelsCppSun[conformLevelCppSun];
    }

    /** Get the type of compiler conformance level Cpp SUN */
    public int getConformLevelCppSun() {
        return conformLevelCppSun;
    }

    /** Set the type of compiler conformance level Cpp SUN */
    public void setConformLevelCppSun(int level) {
        this.conformLevelCppSun = level;
    }

    /** Get compiler conformance flag C GNU */
    public String getConformFlagCGNU() {
        return conformLevelsCGNU[conformLevelCGNU];
    }

    /** Get the type of compiler conformance level C GNU */
    public int getConformLevelCGNU() {
        return conformLevelCGNU;
    }

    /** Set the type of compiler conformance level C GNU */
    public void setConformLevelCGNU(int level) {
        this.conformLevelCGNU = level;
    }

    /** Get compiler conformance flag Cpp GNU */
    public String getConformFlagCppGNU() {
        return conformLevelsCppGNU[conformLevelCppGNU];
    }

    /** Get the type of compiler conformance level Cpp GNU */
    public int getConformLevelCppGNU() {
        return conformLevelCppGNU;
    }

    /** Set the type of compiler conformance level Cpp GNU */
    public void setConformLevelCppGNU(int level) {
        this.conformLevelCppGNU = level;
    }

    /** Get the type of compiler collection */
    public int getToolset() {
        return toolset;
    }

    /** Set the type of compiler collection */
    public void setToolset(int toolset) {
        this.toolset = toolset;
    }

    /** Get the OS the makefile should be created for */
    public int getMakefileOS() {
        return makefileOS;
    }

    /** Set the OS the makefile should be created for */
    public void setMakefileOS(int makefileOS) {
        this.makefileOS = makefileOS;
    }

    /** Get the current directory */
    public String getBaseDirectory() {
        return baseDirectory;
    }

    /**
     *  Get the base directory.
     *
     *  @param expand Expand ~[user] and $vars
     */
    public String getBaseDirectory(boolean expand) {
        // Ignore expand...
        return getBaseDirectory();
    }

    /** Set the current directory */
    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = CndPathUtilities.trimSlashes(baseDirectory);
    }

    /** Get the Makefile name */
    public String getMakefileName() {
        return makefileName;
    }

    /** Set the Makefile name */
    public void setMakefileName(String makefileName) {
        if (makefileName.length() > 0) {
            this.makefileName = makefileName;
        }
    }

    /** Get the Makefile dir name */
    public String getMakefileDirName() {
        return makefileDirName;
    }

    /** Set the Makefile dir name */
    public void setMakefileDirName(String makefileDirName) {
        if (makefileDirName.length() > 0) {
            this.makefileDirName = makefileDirName;
        }
    }

    /** Get the CompilerFlags */
    public CompilerFlags getCompilerFlags() {
        return compilerFlags;
    }

    /** Get the list of targets */
    public List<TargetData> getTargetList() {
        if (targetList == null) {
            targetList = new ArrayList<TargetData>(
                    makefileType == MakefileData.COMPLEX_MAKEFILE_TYPE
                    ? 15 : 1);
        }
        return targetList;
    }

    /** Get the current target */
    public TargetData getCurrentTarget() {
        int key = MakefileWizard.getMakefileWizard().getCurrentTargetKey();

        return getTarget(key);
    }

    /** Get the target matching key */
    public TargetData getTarget(int key) {
        TargetData target = null;

        for (int i = 0; i < targetList.size(); i++) {
            target = targetList.get(i);
            if (target.getKey() == key) {
                break;
            }
        }

        return target;
    }

    /**
     *  Tells if all targets are complete. In some cases thim method is called
     *  after a target has been completed but before the data has been
     *  transferred from the widget to the TargetData. When this happens the
     *  method is called with ignoreCurrent set to true.
     *
     *  @param ignoreCurrent Ignore the current target (its complete)
     */
    public boolean isComplete(boolean ignoreCurrent) {
        TargetData ct = getCurrentTarget();

        for (int i = 0; i < targetList.size(); i++) {
            TargetData target = targetList.get(i);

            if (target == ct && ignoreCurrent) {
                continue;
            }
            if (!target.isComplete()) {
                return false;
            }
        }

        return targetList.size() > 0;	// always return false for empty list
    }

    /** Tells if all targets are complete */
    public boolean isComplete() {
        return isComplete(false);
    }

    /** Get the C compiler */
    public String getCCompiler(int toolset) {
        if (toolset == SUN_TOOLSET_TYPE) {
            return getCCompilerSun();
        } else if (toolset == GNU_TOOLSET_TYPE) {
            return getCCompilerGNU();
        } else {
            // FIXUP - error
        }
        return null;
    }

    /** Get the C compiler Sun*/
    public String getCCompilerSun() {
        if (cCompilerSun == null) {
            cCompilerSun = "cc";	// NOI18N
        }
        return cCompilerSun;
    }

    /** Get the C compiler GNU*/
    public String getCCompilerGNU() {
        if (cCompilerGNU == null) {
            cCompilerGNU = "gcc";	// NOI18N
        }
        return cCompilerGNU;
    }

    /** Setter for the C compiler*/
    public void setCCompiler(int toolset, String cCompiler) {
        if (toolset == SUN_TOOLSET_TYPE) {
            setCCompilerSun(cCompiler);
        } else if (toolset == GNU_TOOLSET_TYPE) {
            setCCompilerGNU(cCompiler);
        } else {
            // FIXUP - error
        }
    }

    /** Setter for the C compiler Sun*/
    public void setCCompilerSun(String cCompiler) {
        this.cCompilerSun = cCompiler;
    }

    /** Setter for the C compiler GNU*/
    public void setCCompilerGNU(String cCompiler) {
        this.cCompilerGNU = cCompiler;
    }

    /** Get the C++ compiler */
    public String getCppCompiler(int toolset) {
        if (toolset == SUN_TOOLSET_TYPE) {
            return getCppCompilerSun();
        } else if (toolset == GNU_TOOLSET_TYPE) {
            return getCppCompilerGNU();
        } else {
            // FIXUP - error
        }
        return null;
    }

    /** Get the C++ compiler Sun*/
    public String getCppCompilerSun() {
        if (cppCompilerSun == null) {
            cppCompilerSun = "CC";	// NOI18N);
        }
        return cppCompilerSun;
    }

    /** Get the C++ compiler GNU*/
    public String getCppCompilerGNU() {
        if (cppCompilerGNU == null) {
            cppCompilerGNU = "g++";	// NOI18N);
        }
        return cppCompilerGNU;
    }

    /** Setter for the Cpp compiler*/
    public void setCppCompiler(int toolset, String cppCompiler) {
        if (toolset == SUN_TOOLSET_TYPE) {
            setCppCompilerSun(cppCompiler);
        } else if (toolset == GNU_TOOLSET_TYPE) {
            setCppCompilerGNU(cppCompiler);
        } else {
            // FIXUP - error
        }
    }

    /** Setter for the C++ compiler Sun*/
    public void setCppCompilerSun(String cppCompiler) {
        this.cppCompilerSun = cppCompiler;
    }

    /** Setter for the C++ compiler GNU*/
    public void setCppCompilerGNU(String cppCompiler) {
        this.cppCompilerGNU = cppCompiler;
    }

    /** Get the Fortran compiler */
    public String getFCompiler(int toolset) {
        if (toolset == SUN_TOOLSET_TYPE) {
            return getFCompilerSun();
        } else if (toolset == GNU_TOOLSET_TYPE) {
            return getFCompilerGNU();
        } else {
            // FIXUP - error
        }
        return null;
    }

    /** Get the Fortran compiler - Sun */
    public String getFCompilerSun() {
        if (fCompilerSun == null) {
            fCompilerSun = "f95";    // NOI18N
        }
        return fCompilerSun;
    }

    /** Get the Fortran compiler - GNU */
    public String getFCompilerGNU() {
        if (fCompilerGNU == null) {
            fCompilerGNU = "g77";    // NOI18N
        }
        return fCompilerGNU;
    }

    /** Setter for the F compiler */
    public void setFCompiler(int toolset, String fCompiler) {
        if (toolset == SUN_TOOLSET_TYPE) {
            setFCompilerSun(fCompiler);
        } else if (toolset == GNU_TOOLSET_TYPE) {
            setFCompilerGNU(fCompiler);
        } else {
            // FIXUP - error
        }
    }

    /** Setter for the F compiler - Sun*/
    public void setFCompilerSun(String fCompiler) {
        this.fCompilerSun = fCompiler;
    }

    /** Setter for the F compiler - GNU*/
    public void setFCompilerGNU(String fCompiler) {
        this.fCompilerGNU = fCompiler;
    }

    /** Get the Assembler name/path */
    public String getAsmPath() {
        if (asmPath == null) {
            asmPath = "as";	// NOI18N
        }
        return asmPath;
    }

    /** Setter for the Assembler */
    public void setAsmPath(String asmPath) {
        this.asmPath = asmPath;
    }

    private String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(MakefileWizardPanel.class);
        }
        return bundle.getString(s);
    }

    /** Check if we are using MODULE or USE statements in Fortran sources */
    public final boolean isModuleEnabled() {
        return moduleEnabled;
    }

    /** Setter for muduleEnabled */
    public void setModuleEnabled(boolean moduleEnabled) {
        this.moduleEnabled = moduleEnabled;
    }
    // Debugging stuff...
    private String indent;

    public void dump() {

        setIndent(""); // NOI18N
        println("\n\nDumping Makefile:");				// NOI18N
        println("    makefileType     = " + makefileType);		// NOI18N
        println("    toolset " + toolset);				// NOI18N
        println("    makefileOS     = " + makefileOS);			// NOI18N
        println("    baseDirectory = \"" + baseDirectory + "\"");	// NOI18N
        println("    makefileName     = \"" + makefileName + "\"");	// NOI18N
        println("    makefileDirName  = \"" + makefileDirName + "\"");	// NOI18N
        compilerFlags.dump(indent + "    ");				// NOI18N
        println("    targetList.size  = " + targetList.size());		// NOI18N

        for (int i = 0; i < targetList.size(); i++) {
            println("    targetList[" + i + "]    = {");		// NOI18N
            (targetList.get(i)).dump(
                    new StringBuilder(indent).append("    ").toString());// NOI18N
            println("    }");						// NOI18N
        }
    }

    public void dump(String indent) {
        setIndent(indent);
        dump();
    }

    private void println(String s) {
        System.out.println(indent + s);
    }

    private void setIndent(String indent) {
        this.indent = indent;
    }

    public boolean validateTargetName(String name, int type) {
        boolean ok = true;

        if (name == null || name.length() == 0) {
            ok = false;
        } else {
            switch (type) {
                case TargetData.COMPLEX_EXECUTABLE:
                case TargetData.COMPLEX_ARCHIVE:
                case TargetData.COMPLEX_SHAREDLIB:
                    ok = dontContainChar(name, ":#= \t()"); // NOI18N
                    break;
                case TargetData.COMPLEX_MAKE_TARGET:
                case TargetData.COMPLEX_CUSTOM_TARGET:
                    ok = dontContainChar(name, ":#= \t"); // NOI18N
                    break;
                default:
            }
        }
        return ok;
    }

    private boolean dontContainChar(String name, String chars) {
        boolean ok = true;

        if (chars == null || chars.length() == 0) {
            // nothing
        } else {
            for (int i = 0; i < chars.length(); i++) {
                if (name.indexOf(chars.charAt(i)) >= 0) {
                    ok = false;
                    break;
                }
            }
        }
        return ok;
    }
}
