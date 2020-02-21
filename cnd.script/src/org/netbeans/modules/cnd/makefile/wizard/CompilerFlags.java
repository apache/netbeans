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

/**
 *  CompilerFlags contains the flags a user is allowed to set from the IPE.
 */
public final class CompilerFlags {

    /** Simple Development case: Debug the simple case */
    private boolean simpleDebug;
    /** Simple Development case: Optimize the simple case */
    private boolean simpleOptimize;
    /** Debug Development case: true means just debug, false means debug+opt */
    private boolean develDebug;

    ///** Use the -g compiler flag for compiling */
    //private boolean develSBrowser;
    /** Where are compile options coming from? */
    private OptionSource optionSource;
    /** Final Development case: Compile with optimization turned on */
    private boolean finalOptimize;
    /** Final Development case: Strip the output */
    private boolean finalStrip;
    /** Compile 64 bit application */
    private boolean comp64bit;
    /** Save the CFLAGS information */
    private String cFlagsSun;
    private String cFlagsGNU;
    /** Save the CCFLAGS information */
    private String ccFlagsSun;
    private String ccFlagsGNU;
    /** Save the F90FLAGS information */
    private String f90Flags;
    private String indent;

    /** List of options we've processed. Used to keep duplicates out */
    /** Initialize with everything turned off */
    public CompilerFlags() {
        simpleDebug = false;
        simpleOptimize = false;

        //develDebug = false;
        develDebug = true;
        //develSBrowser = false;

        finalOptimize = false;
        finalStrip = false;
        optionSource = OptionSource.DEVELOPMENT;

        comp64bit = false;
    }

    /** Getter for debug option */
    public boolean isSimpleDebug() {
        return simpleDebug;
    }

    /** Setter for debug option */
    public void setSimpleDebug(boolean simpleDebug) {
        this.simpleDebug = simpleDebug;
    }

    /** Getter for optimize option */
    public boolean isSimpleOptimize() {
        return simpleOptimize;
    }

    /** Setter for optimize option */
    public void setSimpleOptimize(boolean simpleOptimize) {
        this.simpleOptimize = simpleOptimize;
    }

    /** Getter for Debug (Code) Developement debug-only flag */
    public boolean isDevelDebug() {
        return develDebug;
    }

    /** Setter for Debug (Code) Development debug-only flag */
    public void setDevelDebug(boolean develDebug) {
        this.develDebug = develDebug;
    }


    ///** Getter for SourceBrowser */
    //public boolean isDevelSBrowser() {
    //	return develSBrowser;
    //}
    ///** Setter for SourceBrowser */
    //public void setDevelSBrowser(boolean develSBrowser) {
    //	this.develSBrowser = develSBrowser;
    //}
    /** What kind of build are we doing? This affects where/what options are used */
    public OptionSource getOptionSource() {
        return optionSource;
    }

    /** Set final development flag */
    public void setOptionSource(OptionSource optionSource) {
        this.optionSource = optionSource;
    }

    /** Getter for optimize option */
    public boolean isFinalOptimize() {
        return finalOptimize;
    }

    /** Setter for optimize option */
    public void setFinalOptimize(boolean finalOptimize) {
        this.finalOptimize = finalOptimize;
    }

    /** Getter for strip option */
    public boolean isFinalStrip() {
        return finalStrip;
    }

    /** Setter for strip option */
    public void setFinalStrip(boolean finalStrip) {
        this.finalStrip = finalStrip;
    }

    /** Getter for 64 bit application flags */
    public boolean is64Bit() {
        return comp64bit;
    }

    /** Setter for 64 bit application flags */
    public void set64Bit(boolean comp64bit) {
        this.comp64bit = comp64bit;
    }

    /** Get CFLAGS Sun or GNU */
    public String getCFlags(int toolset) {
        if (toolset == MakefileData.SUN_TOOLSET_TYPE) {
            return getCFlagsSun();
        } else if (toolset == MakefileData.GNU_TOOLSET_TYPE) {
            return getCFlagsGNU();
        } else {
            // FIXUP - error
        }
        return null;
    }

    /** Get CFLAGS Sun*/
    public String getCFlagsSun() {
        if (cFlagsSun == null) {
            cFlagsSun = "$(BASICOPTS) -xCC";	// NOI18N
        }

        return cFlagsSun;
    }

    /** Get CFLAGS GNU*/
    public String getCFlagsGNU() {
        if (cFlagsGNU == null) {
            cFlagsGNU = "$(BASICOPTS)";	// NOI18N
        }

        return cFlagsGNU;
    }

    /** Set CFLAGS Sun*/
    public void setCFlags(int toolset, String cFlags) {
        if (toolset == MakefileData.SUN_TOOLSET_TYPE) {
            setCFlagsSun(cFlags);
        } else if (toolset == MakefileData.GNU_TOOLSET_TYPE) {
            setCFlagsGNU(cFlags);
        } else {
            // FIXUP - error
        }
    }

    /** Set CFLAGS Sun*/
    public void setCFlagsSun(String cFlags) {
        this.cFlagsSun = cFlags;
    }

    /** Set CFLAGS GNU*/
    public void setCFlagsGNU(String cFlags) {
        this.cFlagsGNU = cFlags;
    }

    /** Get CcFLAGS Sun or GNU */
    public String getCcFlags(int toolset) {
        if (toolset == MakefileData.SUN_TOOLSET_TYPE) {
            return getCcFlagsSun();
        } else if (toolset == MakefileData.GNU_TOOLSET_TYPE) {
            return getCcFlagsGNU();
        } else {
            // FIXUP - error
        }
        return null;
    }

    /** Get CcFLAGS Sun*/
    public String getCcFlagsSun() {
        if (ccFlagsSun == null) {
            ccFlagsSun = "$(BASICOPTS)";	// NOI18N
        }

        return ccFlagsSun;
    }

    /** Get CcFLAGS GNU*/
    public String getCcFlagsGNU() {
        if (ccFlagsGNU == null) {
            ccFlagsGNU = "$(BASICOPTS)";	// NOI18N
        }

        return ccFlagsGNU;
    }

    /** Set CcFLAGS Sun*/
    public void setCcFlags(int toolset, String ccFlags) {
        if (toolset == MakefileData.SUN_TOOLSET_TYPE) {
            setCcFlagsSun(ccFlags);
        } else if (toolset == MakefileData.GNU_TOOLSET_TYPE) {
            setCcFlagsGNU(ccFlags);
        } else {
            // FIXUP - error
        }
    }

    /** Set CcFLAGS Sun*/
    public void setCcFlagsSun(String ccFlags) {
        this.ccFlagsSun = ccFlags;
    }

    /** Set CcFLAGS GNU*/
    public void setCcFlagsGNU(String ccFlags) {
        this.ccFlagsGNU = ccFlags;
    }

    /** Get F90FLAGS */
    public String getF90Flags() {
        if (f90Flags == null) {
            f90Flags = "$(BASICOPTS)";  // NOI18N
        }

        return f90Flags;
    }

    /** Set F90FLAGS */
    public void setF90Flags(String f90Flags) {
        this.f90Flags = f90Flags;
    }

    /** Get Basic Options Sun or GNU */
    public String getBasicOptions(int toolset) {
        if (toolset == MakefileData.SUN_TOOLSET_TYPE) {
            return getBasicOptionsSun();
        } else if (toolset == MakefileData.GNU_TOOLSET_TYPE) {
            return getBasicOptionsGNU();
        } else {
            // FIXUP - error
        }
        return null;
    }

    /** Get Basic Options Sun */
    public String getBasicOptionsSun() {
        StringBuilder basicOptions = new StringBuilder(64);

        if (optionSource == OptionSource.SIMPLE) {
            if (simpleDebug) {
                basicOptions.append("-g ");	// NOI18N
            }
            if (simpleOptimize) {
                basicOptions.append("-xO3 ");	// NOI18N
            }
        } else if (optionSource == OptionSource.DEVELOPMENT) {
            if (develDebug) {
                basicOptions.append("-g ");	// NOI18N
            } else {
                basicOptions.append("-g -xO3 ");	// NOI18N
            }
        } else {
            if (finalOptimize) {
                basicOptions.append("-xO3 ");   // NOI18N
            }
            if (finalStrip) {
                basicOptions.append("-s ");	// NOI18N
            }
        }
        if (comp64bit) {
            basicOptions.append("-xarch=generic64 ");  // NOI18N
        }

        return basicOptions.toString().trim();
    }

    /** Get Basic Options GNU */
    public String getBasicOptionsGNU() {
        StringBuilder basicOptions = new StringBuilder(64);

        if (optionSource == OptionSource.SIMPLE) {
            if (simpleDebug) {
                basicOptions.append("-g ");	// NOI18N
            }
            if (simpleOptimize) {
                basicOptions.append("-O ");	// NOI18N
            }
        } else if (optionSource == OptionSource.DEVELOPMENT) {
            if (develDebug) {
                basicOptions.append("-g ");	// NOI18N
            } else {
                basicOptions.append("-g -O ");	// NOI18N
            }
        } else {
            if (finalOptimize) {
                basicOptions.append("-O ");   // NOI18N
            }
            if (finalStrip) {
                basicOptions.append("-s ");	// NOI18N FIXUP ???
            }
        }
        if (comp64bit) {
            basicOptions.append("-m64 ");  // NOI18N FIXUP ???
        }

        return basicOptions.toString().trim();
    }

    /** Some debug methods */
    public void dump() {
        println("Dumping CompilerFlags {");				// NOI18N
        println("    Simple Debug      = ", simpleDebug);		// NOI18N
        println("    Simple Optimize   = ", simpleOptimize);		// NOI18N
        println("    OptionSource      = " + optionSource.toString());	// NOI18N
        println("    Devel Debug       = True");			// NOI18N
        println("    Devel Optimize    = ", !develDebug);		// NOI18N
        println("    Final Optimize    = ", finalOptimize);		// NOI18N
        println("    FinalStrip        = ", finalStrip);		// NOI18N
        println("    64 Bit            = ", comp64bit);			// NOI18N
        println("}");							// NOI18N
    }

    /**
     *  Allow caller to indent all data. This is usefull for indenting target
     *  dumps within MakefileData dumps.
     */
    public void dump(String in) {

        setIndent(in);
        dump();
    }

    private void println(String s) {
        System.out.println(indent + s);
    }

    private void println(String s, boolean tf) {
        System.out.println(indent + s + (tf ? "True" : "False")); // NOI18N
    }

    private void setIndent(String indent) {
        this.indent = indent;
    }
}

