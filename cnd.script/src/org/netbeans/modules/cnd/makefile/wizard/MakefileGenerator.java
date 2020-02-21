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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import org.netbeans.modules.cnd.makefile.utils.FortranParser;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *  Generate a Makefile from a MakefileData. This could have been gathered from
 *  the MakefileWizard or from a file (the latter isn't currently planned but
 *  is likely to happen in the future).
 */
public class MakefileGenerator {

    /** The date for the Makefile */
    private MakefileData md;
    /** The File of the Makefile */
    private File fmakefile;
    /** Write the Makefile from here */
    private BufferedWriter out;
    /** Make variable helper tool */
    private MakeVarName var;
    /** Tells if things are OK */
    private boolean status;
    /** One of the COMPLEX_* types */
    private boolean doComplex;
    /** Compiling C */
    private boolean doC;
    /** Compiling C++ */
    private boolean doCpp;
    /** Compiling Fortran */
    private boolean doFortran;
    /** Building X-Designer file */
    private boolean doXd;
    /** Have assembly files */
    private boolean doAssembly;
    /** Reused buffer */
    private StringBuffer buf;
    /** For localized strings */
    private ResourceBundle bundle;
    private final static int OK = 0;		// return from validation method
    private final static int BAD = 1;	// return from validation method

    /* LINE1 is the first line of a generated Makefile */
    private static final String LINE1 =
            new String("## -*- Makefile -*-");		// NOI18N

    /* BLANKCOMMENT is the second line of a generated Makefile */
    private static final String BLANKCOMMENT = new String("##");	// NOI18N

    /* LINE_PREFIX is the start of the 3rd line of a generated Makefile */
    private static final String LINE_PREFIX = new String("## ");	// NOI18N
    /** Define the suffix list used in make. Only used for Fortran builds */
    private static final String SUFFIX_LIST =
            new String(".o .c .c~ .cc .cc~ .y .y~ " + // NOI18N
            ".l .l~ .s .s~ .sh .sh~ .S .S~ .ln .h .h~ .f .f~ .F .F~ " + // NOI18N
            ".p .p~ .r .r~ .f90 .f90~ .ftn .ftn~ .cps .cps~ .C .C~ " + // NOI18N
            ".Y .Y~ .L .L~ .java .java~ .class");   // NOI18N

    /** The constructor MUST be passed the MakefileData */
    public MakefileGenerator(MakefileData md) {
        this.md = md;
        bundle = null;
        out = null;
        doComplex = false;
        doCpp = false;
        doC = false;
        doFortran = false;
        doXd = false;
        doAssembly = false;

        buf = new StringBuffer(1024);
        var = new MakeVarName();
    }

    /**
     *  Generate the Makefile. This is one of 2 public method of this class. Its
     *  called to generate a Makefile. The data used to generate the Makefile
     *  comes from the MakefileData passed to the constructor.
     *
     *  @return	Returns true if the MakefileData was valid and the Makefile was
     *		sucessfully created. Returns false if no Makefile could be
     *		generated. When false is returned, the MakefileWizard should
     *		stay up so the user can correct the data and create a Makefile.
     *		If the wizard was removed the user would need to reenter all of
     *		the Makefile's data.
     */
    public boolean generate() {

        status = false;
        if (initialize()) {
            try {
                prolog();
                compilerSetup();
                generateTargetDirectories();
                defaultTarget();

                List<TargetData> tlist = md.getTargetList();
                for (int i = 0; i < tlist.size(); i++) {
                    generateTarget(tlist.get(i));
                }

                finale();
            } catch (MakefileGenerationException ex) {
                return false;	    // keep wizard up
            } catch (IOException ex) {
                /*
                 * This exception means an error occurred writing output to the
                 * Makefile. By the time code in this if clause is executed the
                 * Makefile has been opened for writing so we know the error is
                 * a write error, possibly a full disk.
                 */
                if (CndPathUtilities.IfdefDiagnostics) {
                    System.out.println("Failure in Makefile Generation"); // NOI18N
                }
                fmakefile.delete();		    // remove partial Makefile
                String msg = getString("MSG_MakefileOutputError");	// NOI18N
                String title = getString("TITLE_MakefileOutputError");	// NOI18N
                NotifyDescriptor nd = new NotifyDescriptor(msg,
                        title, NotifyDescriptor.OK_CANCEL_OPTION,
                        NotifyDescriptor.ERROR_MESSAGE,
                        new Object[]{NotifyDescriptor.OK_OPTION,
                            NotifyDescriptor.CANCEL_OPTION},
                        NotifyDescriptor.OK_OPTION);
                Object ret = DialogDisplayer.getDefault().notify(nd);
                if (ret.equals(NotifyDescriptor.OK_OPTION)) {
                    return false;		// keep MakefileWizard up
                } else {
                    return true;		// Remove MakefileWizard
                }
            }
        }

        return status;
    }

    /**
     *  Do the initializatios necessary before creating the Makefile. This
     *  includes:
     *
     *	    - Validate the existance of the build directory or create it
     *	    - Validate the existance of the Makefile's directory or creat it
     *	    - Verify the Makefile doesn't exist, or if it exists, that its one
     *	      created by the Makefile wizard (which can be overwritten)
     *	    - Validate each target for completeness
     */
    private boolean initialize() {
        NotifyDescriptor nd;
        String msg;
        String title;

        if (bundle == null) {
            bundle = NbBundle.getBundle(MakefileGenerator.class);
        }
        title = getString("DLG_ErrorDialogTitle");			// NOI18N

        checkTargetFlags();

        // Do some validation
        if (validateOrCreateCwd() != OK ||
                validateOrCreateMakefileDir() != OK) {
            return false;
        }

        /*
         * Check if the Makefile exists. Don't overwrite a non-generated
         * Makefile since thats probably not what the user intended. Go ahead
         * and overwrite if its one we've written. Also inform the user if we
         * can't overwrite the Makefile.
         */
        if (fmakefile.exists() && fmakefile.length() > 0) {
            if (fmakefile.canRead()) {
                msg = null;
                if (!generatedMakefile(fmakefile)) {
                    msg = MessageFormat.format(
                            getString("MSG_NonGeneratedMakefile"), // NOI18N
                            new Object[]{md.getMakefileName()});
                }
            } else {
                msg = MessageFormat.format(
                        getString("MSG_CannotReadMakefile"), // NOI18N
                        new Object[]{md.getMakefileName()});
            }

            if (msg != null) {
                nd = new NotifyDescriptor(msg,
                        title, NotifyDescriptor.DEFAULT_OPTION,
                        NotifyDescriptor.ERROR_MESSAGE,
                        new Object[]{NotifyDescriptor.OK_OPTION},
                        NotifyDescriptor.OK_OPTION);
                DialogDisplayer.getDefault().notify(nd);
                return false;
            }
        }

        /*
         * Verify that the requested Makefile actualy performs something and
         * that all targets are complete. If it isn't complete, inform the user
         * but don't generate the Makefile. Let the user fix the specification
         * first.
         */
        if (targetIsIncomplete()) {
            msg = MessageFormat.format(
                    getString("MSG_TargetIsIncomplete"), // NOI18N
                    new Object[]{md.getMakefileName()});
            nd = new NotifyDescriptor(msg,
                    title, NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object[]{NotifyDescriptor.OK_OPTION},
                    NotifyDescriptor.OK_OPTION);
            DialogDisplayer.getDefault().notify(nd);
            return false;
        }

        /*
         * The directory exists so we try and create the file by opening an
         * output stream. Set the OutputStreamWriter if it succeeds or post
         * an error dialog for failure.
         */
        try {
            out = new BufferedWriter(new FileWriter(fmakefile));
        } catch (IOException e) {
            msg = MessageFormat.format(
                    getString("MSG_CannotCreateMakefile"), // NOI18N
                    new Object[]{md.getMakefileName()});

            nd = new NotifyDescriptor(msg,
                    title, NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object[]{NotifyDescriptor.OK_OPTION},
                    NotifyDescriptor.OK_OPTION);
            DialogDisplayer.getDefault().notify(nd);
            return false;
        }

        return true;
    }

    /** Emit compiler flags used in the Makefile */
    private void compilerSetup() throws IOException {

        if (out != null &&
                (doC || doXd || doCpp || doFortran || doAssembly)) {
            String line = getString("MFG_CompilerFlags");		// NOI18N
            out.write(line, 0, line.length());

            /*
             * The first section compiler path variables. These are only emitted
             * if the user specifies a pathname in the Select Compiler Path
             * panel (which implies it must be a Complex Makefile). This is
             * not true for X-Designer since the default make.rules file doesn't know
             * about XDesigner.
             */
            if (doC || doXd || doAssembly) {
                buf.delete(0, buf.length());
                appendToolsetDepMacro("CC", md.getCCompilerSun(), md.getCCompilerGNU()); // NOI18N
                out.write(buf.toString(), 0, buf.length());
            }
            if (doCpp || doXd) {
                buf.delete(0, buf.length());
                //appendToolsetDepMacro("CCC", md.getCppCompilerSun(), "CXX", md.getCppCompilerGNU());
                appendMacro("CCC", md.getCppCompiler(md.getToolset())); // NOI18N
                appendMacro("CXX", md.getCppCompiler(md.getToolset())); // NOI18N
                out.write(buf.toString(), 0, buf.length());
            }
            if (doFortran) {
                buf.delete(0, buf.length());
                //appendToolsetDepMacro("F90C", md.getFCompilerSun(), "FC", md.getFCompilerGNU());
                //appendToolsetDepMacro("FCDRIVER", "$(COMPILE.f90)", "$(COMPILE.f)");
                //appendToolsetDepMacro("FLDRIVER", "$(LINK.f90)", "$(LINK.f)");
                appendToolsetDepMacro("FC", md.getFCompilerSun(), md.getFCompilerGNU()); // NOI18N
                out.write(buf.toString(), 0, buf.length());
            }
            if (doAssembly) {
                buf.replace(0, buf.length(), "AS=");			// NOI18N
                buf.append(md.getAsmPath());
                buf.append('\n');
                out.write(buf.toString(), 0, buf.length());
            }

            /*
             * Now set some Makefile variables. Some of these (like CFLAGS and
             * CCFLAGS) will have target specific counterparts. But since the
             * default compiler rules use these we also set them.
             */
            CompilerFlags copts = md.getCompilerFlags();
            if (doC || doCpp || doFortran) {
                buf.delete(0, buf.length());
                appendToolsetDepMacro("BASICOPTS", copts.getBasicOptionsSun(), copts.getBasicOptionsGNU()); // NOI18N
                out.write(buf.toString(), 0, buf.length());
            }
            if (doC) {
                buf.delete(0, buf.length());
                String flagsSun = copts.getCFlagsSun();// + md.getConformFlagCSun();
                String flagsGNU = copts.getCFlagsGNU();// + md.getConformFlagCGNU();
                appendToolsetDepMacro("CFLAGS", flagsSun, flagsGNU); // NOI18N
                out.write(buf.toString(), 0, buf.length());
            }

            if (doCpp) {
                buf.delete(0, buf.length());
                //String flagsSun = copts.getCcFlagsSun();
                //String flagsGNU = copts.getCcFlagsGNU();
                String flags = copts.getCcFlags(md.getToolset());
                //appendToolsetDepMacro("CCFLAGS", flagsSun, "CXXFLAGS", flagsGNU);
                appendMacro("CCFLAGS", flags); // NOI18N
                appendMacro("CXXFLAGS", flags); // NOI18N
                out.write(buf.toString(), 0, buf.length());

                buf.delete(0, buf.length());
                appendToolsetDepMacro("CCADMIN", "CCadmin -clean", "");// NOI18N
                out.write(buf.toString(), 0, buf.length());
            }

            if (doXd) {
                /*
                 * X-Designer compiles are done from the X-Designer generated Makefile.
                 * To force the X-Designer Makefile to use the same CFLAGS and CCFLAGS
                 * we are using we create it with several X resources set to the
                 * desired CFLAGS/CCFLAGS.
                 */
                buf.replace(0, buf.length(), "XDESIGNER=xdesigner");		// NOI18N
                buf.append(" -xrm \"xdesigner.cDebugFlags: $(CFLAGS)\" ");	// NOI18N
                buf.append("-xrm \"xdesigner.cppDebugFlags: $(CCFLAGS)\"\n");// NOI18N
                buf.append("XDROOT=");				// NOI18N
                buf.append(System.getProperty("netbeans.home"));    // NOI18N
                buf.append("/platform/$(OSVARIANT)/prod");  // NOI18N
                buf.append("\nXDFLAGS = $(MAKEFLAGS) "); // NOI18N
                buf.append("XDROOT=$(XDROOT) CC=$(CC) CCC=$(CCC) ");	// NOI18N
                if (md.getConformLevelCppSun() != 0) {	    // FIXUP ???
                    buf.append("ABICCFLAGS=-features=no%conststrings");	// NOI18N
                }
                out.write(buf.toString(), 0, buf.length());
                out.newLine();
            }

            if (doFortran) {
                buf.delete(0, buf.length());
                //appendToolsetDepMacro("F90FLAGS", copts.getF90Flags(), "FFLAGS", copts.getF90Flags());
                appendMacro("FFLAGS", copts.getF90Flags()); // NOI18N
                out.write(buf.toString(), 0, buf.length());
            }

            out.newLine();
            out.newLine();
        }
    }

    /**
     *  The first target in a Makefile is the default target and is the one
     *  used if no explicit targets are specified. In our generated Makefiles,
     *  this target is called "all" and depends on all of the targets named in
     *  the Makfile.
     */
    private void defaultTarget() throws IOException {

        if (out != null) {
            List<TargetData> tlist = md.getTargetList();

            buf.replace(0, buf.length(), "all:");			// NOI18N
            for (int i = 0; i < tlist.size(); i++) {
                TargetData t = tlist.get(i);
                buf.append(" ");					// NOI18N
                String dir = getOutputDirectory(t);
                if (t.isCompilable() &&
                        dir.length() > 0 && !dir.equals(".")) {		// NOI18N
                    var.setTargetName(t.getName());
                    buf.append(var.makeRef("TARGETDIR_"));		// NOI18N
                    buf.append("/"); // always unix separator // NOI18N
                } else {
                    if (t.getName().compareTo("all") == 0) {		// NOI18N
		        /* Target 'all' is already in the list - nothing to do */
                        buf.delete(0, buf.length());
                        return;
                    }
                }
                buf.append(t.getName());
            }
            out.write(buf.toString(), 0, buf.length());

            out.newLine();
            out.newLine();
        }
    }

    /** Emit the prologue portion of the Makefile */
    private void prolog() throws IOException {

        if (out != null) {
            out.write(LINE1, 0, LINE1.length());
            out.newLine();
            out.write(BLANKCOMMENT, 0, BLANKCOMMENT.length());
            out.newLine();

            buf.replace(0, buf.length(), getString("MFG_User"));	// NOI18N
            buf.append(getUserName());
            buf.append('\n');
            out.write(buf.toString(), 0, buf.length());

            buf.replace(0, buf.length(), getString("MFG_Time"));	// NOI18N
            buf.append(getTimestamp());
            buf.append('\n');
            out.write(buf.toString(), 0, buf.length());

            buf.replace(0, buf.length(), getString("MFG_CreateMsg"));	// NOI18N
            out.write(buf.toString(), 0, buf.length());

            out.write(BLANKCOMMENT, 0, BLANKCOMMENT.length());
            out.newLine();

            buf.replace(0, buf.length(), getString("MFG_DoNotEditMsg"));// NOI18N
            out.write(buf.toString(), 0, buf.length());

            out.write(BLANKCOMMENT, 0, BLANKCOMMENT.length());
            out.newLine();

            if (doFortran) {
                buf.replace(0, buf.length(), getString("MFG_SuffixList"));    // NOI18N
                buf.append(".SUFFIXES\n");	// NOI18N
                buf.append(".SUFFIXES: ");  // NOI18N
                buf.append(SUFFIX_LIST);
                out.newLine();
                out.write(buf.toString(), 0, buf.length());
                out.newLine();
            }

            /*
            out.newLine();
            buf.replace(0, buf.length(), "PLATFORM :sh= uname -p | sed s/i386/x86/\n");	// NOI18N
            buf.append("OSVARIANT=$(PLATFORM)-SunOS\n");	// NOI18N
            out.newLine();
            out.write(buf.toString(), 0, buf.length());
             */
            // Emit TOOLSET macro only if used
            if (md.getToolset() == MakefileData.SUNGNU_TOOLSET_TYPE) {
                buf = new StringBuffer(1024);
                buf.append("# Type of makefile: SUN, GNU, ...\n");	// NOI18N
                buf.append("TOOLSET\t= $(shell echo \"GNU\")\n");	// NOI18N
                buf.append("TOOLSET:sh\t= echo `echo \"SUN\"`\n");	// NOI18N
                out.newLine();
                out.write(buf.toString(), 0, buf.length());
            }
            List<TargetData> tlist = md.getTargetList();
            boolean usingGenericPlatform = false;
            boolean usingSharedLib = false;
            for (int i = 0; i < tlist.size(); i++) {
                TargetData t = tlist.get(i);
                if (t.getOutputDirectory().equals(getString("OutputDirectoryPlatform"))) {
                    usingGenericPlatform = true;
                }
                if (t.getTargetType() == TargetData.SIMPLE_SHAREDLIB || t.getTargetType() == TargetData.COMPLEX_SHAREDLIB) {
                    usingSharedLib = true;
                }
            }
            // Emit OS macro only if used
            if (usingGenericPlatform || (usingSharedLib && md.getMakefileOS() == MakefileData.UNIX_OS_TYPE)) {
                // OS
                buf = new StringBuffer(1024);
                buf.append("# OS: SunOS, Linux, ...`\n");	// NOI18N
                int toolset = md.getToolset();
                if (toolset == MakefileData.GNU_TOOLSET_TYPE || toolset == MakefileData.SUNGNU_TOOLSET_TYPE) {
                    buf.append("OS\t\t= $(shell /bin/uname)\n");	// NOI18N
                }
                if (toolset == MakefileData.SUN_TOOLSET_TYPE || toolset == MakefileData.SUNGNU_TOOLSET_TYPE) {
                    buf.append("OS:sh\t\t= echo `/bin/uname`\n");	// NOI18N
                }
                out.newLine();
                out.write(buf.toString(), 0, buf.length());
            }
            // Emit PROCESSOR and PLATFORM macros only if used
            if (usingGenericPlatform) {
                // Processor
                buf = new StringBuffer(1024);
                buf.append("# Processor type: sparc, i686, ...\n");	// NOI18N
                int toolset = md.getToolset();
                if (toolset == MakefileData.GNU_TOOLSET_TYPE || toolset == MakefileData.SUNGNU_TOOLSET_TYPE) {
                    buf.append("PROCESSOR\t= $(shell /bin/uname -p)\n");	// NOI18N
                }
                if (toolset == MakefileData.SUN_TOOLSET_TYPE || toolset == MakefileData.SUNGNU_TOOLSET_TYPE) {
                    buf.append("PROCESSOR:sh\t= echo `/bin/uname -p``\n");	// NOI18N
                }
                out.newLine();
                out.write(buf.toString(), 0, buf.length());

                // Platform
                buf = new StringBuffer(1024);
                buf.append("# Platform\n");	// NOI18N
                buf.append("PLATFORM\t= $(OS)-$(PROCESSOR)\n");	// NOI18N
                out.newLine();
                out.write(buf.toString(), 0, buf.length());
            }

            out.newLine();
            out.newLine();
        }
    }

    /**
     *  Emit the variable definitions for the target (output) directories. This
     *  needs to be done before the targets are defined because target n can
     *  reference the target directory in target n+1. If the target directory
     *  isn't defined before target n then the reference has no value.
     */
    private void generateTargetDirectories() throws IOException {
        List<TargetData> tlist = md.getTargetList();

        if (doC || doXd || doCpp || doFortran || doAssembly) {
            String com = getString("MFG_TargetDirectories");		// NOI18N
            out.write(com, 0, com.length());
            for (int i = 0; i < tlist.size(); i++) {
                TargetData t = tlist.get(i);

                if (t.isCompilable()) {
                    String tdir = getOutputDirectory(t);
                    String cwd = md.getBaseDirectory(MakefileData.EXPAND);

                    /*
                     * We always emit a TARGETDIR_* = line. Sometimes we need
                     * to add a target to create this directory. We do that
                     * whenever its not "." or the current directory (which is
                     * the build directory). If we need the create target we
                     * set a boolean here and create it later.
                     */
                    buf.replace(0, buf.length(), "TARGETDIR_");		// NOI18N
                    buf.append(t.getName());
                    buf.append("=");					// NOI18N
                    String targetDir = getOutputDirectory(t);
                    if (targetDir.length() == 0 || targetDir.equals(cwd) ||
                            (targetDir.length() == 1 &&
                            targetDir.charAt(0) == '.')) {
                        buf.append(new String(".\n"));			// NOI18N
                    } else {
                        buf.append(targetDir);
                        buf.append('\n');
                    }
                    out.write(buf.toString(), 0, buf.length());
                }
            }
            out.newLine();
            out.newLine();
        }
    }

    /** Emit code for a target */
    private void generateTarget(TargetData t)
            throws IOException, MakefileGenerationException {

        if (out != null) {
            var.setTargetName(t.getName());

            buf.replace(0, buf.length(), getString("MFG_Target"));	// NOI18N
            buf.append(t.getName());
            buf.append('\n');
            out.write(buf.toString(), 0, buf.length());

            if (t.isCompilable()) {
                // A compilable target
                targetSetup(t);
                linkOrArchive(t);
                explicitRules(t);
            } else {
                makeTarget(t);
            }
        }
    }

    /**
     *  Do whatever needs doing to finish the Makefile. This includes:
     *
     *  <UL>
     *    <LI>Adding targets to create the target directories for each target
     *	  <LI>Creating a clean target which cleans up after each target
     *	  <LI>Optional Solaris dependency information (Makefile will not run
     *	      on non Solaris platfors with this information)
     *  </UL>
     */
    private void finale() throws IOException {

        if (out != null) {
            List<TargetData> tlist = md.getTargetList();
            String cwd = md.getBaseDirectory(MakefileData.EXPAND);
            Set<TargetData> mkhash = new HashSet<TargetData>(tlist.size(), 1.0F);
            String dir;
            StringBuilder cb = new StringBuilder(1024);
            boolean wasClean = false;

            for (int i = 0; i < tlist.size(); i++) {
                TargetData t = tlist.get(i);
                var.setTargetName(t.getName());

                // Build the HashSet used in creating TARGETDIR_<tname> dirs
                dir = getOutputDirectory(t);
                if (t.isCompilable() && dir.length() > 0 &&
                        !dir.equals(".") && !dir.equals(cwd)) {	// NOI18N
                    mkhash.add(t);
                }

                // Gather the lines to be be $(RM)'ed by the clean target
                if (t.getTargetType() != TargetData.COMPLEX_MAKE_TARGET &&
                        t.getTargetType() != TargetData.COMPLEX_CUSTOM_TARGET) {
                    cb.append(" \\\n\t\t");				// NOI18N
                    cb.append(var.makeRef("TARGETDIR_"));		// NOI18N
                    cb.append('/');
                    cb.append(t.getName());
                    String[] srcs = t.getSourcesList();
                    for (int j = 0; j < srcs.length; j++) {
                        if (!t.isHdrFile(srcs[j]) && !t.isXdFile(srcs[j])) {
                            String newobj = objectOf(srcs[j]);
                            if (newobj != null) {
                                cb.append(" \\\n\t\t");			// NOI18N
                                cb.append(var.makeRef("TARGETDIR_"));	// NOI18N
                                cb.append('/');
                                cb.append(newobj);
                            }
                        }
                    }
                } else {
                    if (t.getName().compareTo("clean") == 0) {		// NOI18N
		        /* Target 'clean' is already in the list */
                        wasClean = true;
                    }

                }
            }

            boolean doClean = false;
            if (cb.length() > 0) {
                doClean = true;
                String com = getString("MFG_CleanTarget");		// NOI18N
                out.write(com, 0, com.length());
                buf.replace(0, buf.length(), "clean:\n\trm -f");	// NOI18N
                out.write(buf.toString(), 0, buf.length());
                out.write(cb.toString(), 0, cb.length());
                out.write('\n');
            }

            if (mkhash.size() > 0) {
                if (doClean && doCpp) {
                    buf.replace(0, buf.length(),
                            "\t$(CCADMIN)\n");		// NOI18N
                    out.write(buf.toString(), 0, buf.length());
                }
                cb.delete(0, cb.length());

                if (doClean && doFortran) {
                    buf.replace(0, buf.length(), "\trm -f *.mod\n");	// NOI18N
                    out.write(buf.toString(), 0, buf.length());
                }

                buf.replace(0, buf.length(),
                        getString("MFG_CreateTargetDir"));	// NOI18N
                Iterator<TargetData> iter = mkhash.iterator();

                while (iter.hasNext()) {
                    TargetData t = iter.next();
                    var.setTargetName(t.getName());

                    // Create a rmdir command the directory
                    cb.append("\trm -f -r ");				// NOI18N
                    cb.append(var.makeRef("TARGETDIR_")); // NOI18N
                    cb.append('\n');

                    // Create $(TARGETDIR) if its not "." or cwd
                    buf.append(var.makeRef("TARGETDIR_")); // NOI18N
                    buf.append(":\n\tmkdir -p ");			// NOI18N
                    buf.append(var.makeRef("TARGETDIR_")); // NOI18N
                    buf.append("\n");					// NOI18N
                }

                // Emit the rmdir commands
                if (doClean) {
                    cb.append("\n\n");					// NOI18N
                    out.write(cb.toString(), 0, cb.length());
                }
                buf.append("\n\n");					// NOI18N
                out.write(buf.toString(), 0, buf.length());
            }
            if ((doClean == false) && (wasClean == false)) {
                /* Add empty target clean: (for consistency) */
                String com = getString("MFG_CleanTarget");		// NOI18N
                out.write(com, 0, com.length());
                buf.replace(0, buf.length(), "clean:\n\n");		// NOI18N
                out.write(buf.toString(), 0, buf.length());
            }

            /* Emit the KEEP_STATE stuff */
            buf.replace(0, buf.length(), getString("MFG_KeepState"));	// NOI18N
            buf.append(".KEEP_STATE:\n");		    		// NOI18N
            buf.append(".KEEP_STATE_FILE:.make.state." + md.defaultOutputDirectory() + "\n"); // NOI18N
            buf.append("\n");		    		// NOI18N
            out.write(buf.toString(), 0, buf.length());

            status = true;
            out.close();
            out = null;
        }
    }

    /**
     *  All of the compilable target types need some similar setup code. This
     *  includes variables for the target directory, cpp flags, and object
     *  variables.
     */
    private void targetSetup(TargetData t) throws IOException, MakefileGenerationException {
        String[] srcs;

        if (out != null) {
            String tname = t.getName();

            // Generate target flags (like CFLAGS_<target-name>) for each lang
            String flags = generateTargetFlags(t);
            if (flags.length() > 0) {
                out.write(flags, 0, flags.length());
            }

            // Generate the includes list in CPPFLAGS_<target-name>
            String[] includesList = t.getIncludesList();
            if (includesList != null && includesList.length > 0) {
                String incs = doList(
                        var.makeName("CPPFLAGS_"), "-I", includesList);	// NOI18N
                out.write(incs, 0, incs.length());
            }

            /*
             * Now emit the OBJS_<target-name> variable. Ignore .xd files (they
             * will be handled later).
             */
            StringBuilder lbuf = new StringBuilder(80);
            lbuf.append(" \\\n\t");					// NOI18N
            lbuf.append(var.makeRef("TARGETDIR_"));			// NOI18N
            lbuf.append('/');
            String tdir = lbuf.toString();

            buf.replace(0, buf.length(), var.makeName("OBJS_"));	// NOI18N
            buf.append(" = ");						// NOI18N

            if (doFortran && md.isModuleEnabled()) {
                srcs = getOrderedSources(t);
            } else {
                srcs = t.getSourcesList();
            }
            for (int i = 0; i < srcs.length; i++) {
                if (!t.isHdrFile(srcs[i]) && !t.isXdFile(srcs[i])) {
                    String newobj = objectOf(srcs[i]);
                    if (newobj != null) {
                        buf.append(tdir);
                        buf.append(newobj);
                    }
                }
            }
            buf.append('\n');
            out.write(buf.toString(), 0, buf.length());


            // Now set up the library options
            if (t.isLinked() && (t.isComplex() || t.containsXdFiles())) {
                boolean needLDLIBS = false;

                String syslibs = getSysLibs(t);
                if (syslibs.length() > 0) {
                    out.write(syslibs, 0, syslibs.length());
                    needLDLIBS = true;
                }

                String userlibs = getUserLibs(t);
                if (userlibs.length() > 0) {
                    out.write(userlibs, 0, userlibs.length());
                    needLDLIBS = true;
                }
                String deplibs = getDependLibs(t);
                if (deplibs.length() > 0) {
                    out.write(deplibs, 0, deplibs.length());
                }
                if (needLDLIBS) {
                    buf.replace(0, buf.length(),
                            var.makeName("LDLIBS_"));		// NOI18N
                    buf.append(" = ");					// NOI18N
                    buf.append(var.makeRef("USERLIBS_"));		// NOI18N
                    //buf.append(' ');
                    //buf.append(var.makeRef("SYSLIBS_"));		// NOI18N
                    buf.append('\n');
                    out.write(buf.toString(), 0, buf.length());
                }
            }

            out.newLine();
            out.newLine();

            // Do the X-Designer variables
            if (t.containsXdFiles()) {
                String vtargs = generateXdTargetVars(t);
                out.write(vtargs, 0, vtargs.length());
                out.newLine();
            }
        }
    }

    /**
     *  Generate some special make variables for each X-Designer file in a target.
     *
     *  @param t    The TargetData
     */
    private String generateXdTargetVars(TargetData t) {

        buf.replace(0, buf.length(), getString("MFG_XDesignerTargetVars"));	// NOI18N

        String[] srcs = t.getSourcesList();
        for (int i = 0; i < srcs.length; i++) {
            if (t.isXdFile(srcs[i])) {
                xdTargetFile(t, srcs[i]);
            }
        }
        return buf.toString();
    }

    /** Emit X-Designer target variables for this file */
    private void xdTargetFile(TargetData t, String file) {
        String vname = getXdTargetName(t, file);

        buf.append("XDMAKEFILE_");					// NOI18N
        buf.append(vname);
        buf.append(" = ");						// NOI18N
        buf.append(vname);
        buf.append("_Makefile\nXD_OBJS_FILE_");			// NOI18N
        buf.append(vname);
        buf.append(" = xd_objs_");					// NOI18N
        buf.append(vname);
        buf.append("\nXD_OBJS_");					// NOI18N
        buf.append(vname);
        buf.append(" = `/bin/cat -s ");					// NOI18N
        buf.append(getOutputDirectory(t));
        buf.append("/xd_objs_");					// NOI18N
        buf.append(vname);
        buf.append(" | \\\n\tawk 'BEGIN { RS=\" \"} {if (NF > 0) { print \"");	// NOI18N
        buf.append(var.makeRef("TARGETDIR_"));	// NOI18N
        buf.append("/\" $$0 }}'`\nXD_LINK_ARGS_FILE_");    // NOI18N
        buf.append(vname);
        buf.append(" = xd_link_args_");				// NOI18N
        buf.append(vname);
        buf.append("\nXD_LINK_ARGS_");				// NOI18N
        buf.append(vname);
        buf.append(" = `/bin/cat -s ");					// NOI18N
        buf.append(getOutputDirectory(t));
        buf.append("/xd_link_args_");					// NOI18N
        buf.append(vname);
        buf.append("; exit 0`\nXDFLAGS_");				// NOI18N
        buf.append(vname);
        buf.append(" = -f $(XDMAKEFILE_");				// NOI18N
        buf.append(vname);
        buf.append(") $(XDFLAGS)\n\n");				// NOI18N
    }

    /**
     *  When generating names related to X-Designer we need to specify a name which
     *  is unique to both the target and X-Designer file. We do this by concatenating
     *  some text + target + xd-file-name. For readability, we change any '.'
     *  characters to '_'.
     */
    private String getXdTargetName(TargetData t, String file) {
        StringBuffer name = new StringBuffer(256);

        name.append(t.getName());
        name.append('_');
        for (int i = 0; i < file.length(); i++) {
            if (file.charAt(i) == '.' || file.charAt(i) == '/') {
                name.append('_');
            } else {
                name.append(file.charAt(i));
            }
        }
        return name.toString();
    }

    /** Emit the link information */
    private void linkOrArchive(TargetData t) throws IOException {
        int type = t.getTargetType();

        if (out != null &&
                type != TargetData.COMPLEX_MAKE_TARGET &&
                type != TargetData.COMPLEX_CUSTOM_TARGET) {
            out.write(getString("MFG_LinkOrArchive"));			// NOI18N

            buf.replace(0, buf.length(), var.makeRef("TARGETDIR_"));	// NOI18N
            buf.append('/');
            buf.append(t.getName());
            buf.append(": ");						// NOI18N
            buf.append(var.makeRef("TARGETDIR_"));			// NOI18N
            buf.append(' ');
            buf.append(var.makeRef("OBJS_"));				// NOI18N
            if (type == TargetData.COMPLEX_EXECUTABLE ||
                    type == TargetData.COMPLEX_SHAREDLIB) {
                buf.append(' ');
                buf.append(var.makeRef("DEPLIBS_"));			// NOI18N
            }
            if (t.containsXdFiles()) {
                buf.append(xdLinkDependencies(t));
            }
            buf.append('\n');
            buf.append('\t');

            switch (type) {
                case TargetData.SIMPLE_EXECUTABLE:
                case TargetData.COMPLEX_EXECUTABLE:
                    // don't care if its SIMPLE_ or COMPLEX_ in this call...
                    buf.append(linkLine(t, TargetData.SIMPLE_EXECUTABLE));
                    break;

                case TargetData.SIMPLE_ARCHIVE:
                case TargetData.COMPLEX_ARCHIVE:
                    if (t.containsCppFiles()) {
                        //buf.append("$(CCC) -xar -o $@ ");			// NOI18N
                        StringBuffer bufx = new StringBuffer(1024);
                        appendToolsetDepMacro(bufx, "ARCPP", "$(CCC) -xar -o $@ ", "$(AR) $(ARFLAGS) $@ "); // NOI18N
                        out.write(bufx.toString(), 0, bufx.length());
                        buf.append("$(ARCPP) ");			// NOI18N
                    } else {
                        buf.append("$(AR) $(ARFLAGS) $@ ");			// NOI18N
                    }
                    buf.append(var.makeRef("OBJS_"));			// NOI18N
                    buf.append('\n');
                    break;

                case TargetData.SIMPLE_SHAREDLIB:
                case TargetData.COMPLEX_SHAREDLIB:
                    // don't care if its SIMPLE_ or COMPLEX_ in this call...
                    buf.append(linkLine(t, type));
                    break;
            }

            out.write(buf.toString(), 0, buf.length());
            out.newLine();
            out.newLine();
        }
    }

    private void explicitRules(TargetData t) throws IOException {

        if (out != null) {
            int i;

            String msg = getString("MFG_CompileRuleComment");		// NOI18N
            out.write(msg, 0, msg.length());

            // Create a compile line for each source file
            String[] srcs = t.getSourcesList();
            for (i = 0; i < srcs.length; i++) {
                compileLine(t, srcs[i]);
            }

            out.newLine();
            out.newLine();

            if (t.containsXdFiles()) {
                msg = getString("MFG_XDesignerCompileRule");			// NOI18N
                out.write(msg, 0, msg.length());
                buf.delete(0, buf.length());
                for (i = 0; i < srcs.length; i++) {
                    if (t.isXdFile(srcs[i])) {
                        buf.append(xdCompile(t, srcs[i]));
                    }
                }
                out.write(buf.toString(), 0, buf.length());

                out.newLine();
                out.newLine();
            }
        }

    }

    /** Emit the target lines and actions to compile the X-Designer file */
    private String xdCompile(TargetData t, String file) {
        String vname = getXdTargetName(t, file);

        String l1 = MessageFormat.format(
                "$(TARGETDIR_{0})/$(XDMAKEFILE_{1}): $(TARGETDIR_{0}) {2}\n\t$(XDESIGNER) -m $(TARGETDIR_{0})/$(XDMAKEFILE_{1}) {2}\n\n", // NOI18N
                new Object[]{t.getName(), vname, file});
        String l2 = MessageFormat.format(
                "$(TARGETDIR_{0})/$(XD_OBJS_FILE_{1}): $(TARGETDIR_{0}) $(TARGETDIR_{0})/$(XDMAKEFILE_{1}) {2}\n\t$(XDESIGNER) -G $(TARGETDIR_{0}) {2}\n", // NOI18N
                new Object[]{t.getName(), vname, file});
        String l3 = MessageFormat.format(
                "\tcd $(TARGETDIR_{0}); $(MAKE) $(XDFLAGS_{1}) info-objects > $(XD_OBJS_FILE_{1})\n\tcd $(TARGETDIR_{0}); $(MAKE) $(XDFLAGS_{1}) info-link > $(XD_LINK_ARGS_FILE_{1})\n\tcd $(TARGETDIR_{0}); $(MAKE) $(XDFLAGS_{1}) all-objects\ntest::\n\n", // NOI18N
                new Object[]{t.getName(), vname, file});
        return new StringBuilder(1024).append(l1).append(l2).append(l3).toString();
    }

    /** Generate the code for COMPLEX_MAKE_TARGETS and COMPLEX_CUSTOM_TARGETS */
    private void makeTarget(TargetData t) throws IOException {

        buf.replace(0, buf.length(), t.getName());
        /* Why this???
        if (t.getName().equals("clean")) {				// NOI18N
        buf.append(":: ");						// NOI18N
        } else {
        buf.append(": ");						// NOI18N
        }
         */
        buf.append(": ");						// NOI18N
        String dependsOn = t.getDependsOn();
        if (dependsOn != null && dependsOn.length() > 0) {
            buf.append(dependsOn);
        }
        buf.append('\n');

        if (t.getTargetType() == TargetData.COMPLEX_MAKE_TARGET) {
            String dir = t.getSubdirectory();
            if (dir != null && dir.length() > 0) {
                buf.append("\tcd ");					// NOI18N
                buf.append(dir);
                buf.append("; $(MAKE) ");				// NOI18N
            } else {
                buf.append("\t$(MAKE) ");				// NOI18N
            }
            String mflags = t.getMakeFlags();
            if (mflags != null && mflags.length() > 0) {
                buf.append(mflags);
                buf.append(' ');
            }

            String target = t.getTargetName();
            if (target != null && target.length() > 0) {
                buf.append(target);
            }
            buf.append('\n');
        } else {
            ArrayList actions = t.getActions();
            for (int i = 0; i < actions.size(); i++) {
                String line = actions.get(i).toString().trim();
                if (line.length() == 0) {
                    break;		    // stop processing after 1st target
                }
                buf.append('\t');
                buf.append(line);
                buf.append('\n');
            }
        }
        buf.append("\n\n");						// NOI18N
        out.write(buf.toString(), 0, buf.length());
    }

    /**
     *  Generate a compile line for a source file.
     *
     *  @param t    The TargetData
     *  @param src  The source file to be compiled
     */
    private void compileLine(TargetData t, String src) throws IOException {
        String compiler;
        String flagBase;

        // First, see what language we are generating
        if (t.isCppFile(src)) {
            compiler = "$(COMPILE.cc)";					// NOI18N
            flagBase = "CCFLAGS_";					// NOI18N
        } else if (t.isCFile(src)) {
            compiler = "$(COMPILE.c)";					// NOI18N
            flagBase = "CFLAGS_";					// NOI18N
        } else if (t.isFortranFile(src)) {
            compiler = "$(COMPILE.f)";				// NOI18N
            flagBase = "FFLAGS_";					// NOI18N
        } else if (t.isHdrFile(src)) {
            return;			    // don't try and compile .h files
        } else if (t.isXdFile(src)) {
            return;			    // do X-Designer files in separate section
        } else if (t.isAssemblyFile(src)) {
            compiler = "$(AS)";						// NOI18N
            flagBase = "ASFLAGS_";					// NOI18N
        } else {
            if (CndPathUtilities.IfdefDiagnostics) {
                System.out.println("Generator: Can't determine language of " + src); // NOI18N
            }
            return;
        }

        String newobj = objectOf(src);
        if (newobj == null) {
            return;
        }

        // Now generate the target line
        buf.replace(0, buf.length(), var.makeRef("TARGETDIR_"));	// NOI18N
        buf.append('/');
        buf.append(newobj);
        buf.append(": ");						// NOI18N
        buf.append(var.makeRef("TARGETDIR_"));				// NOI18N
        buf.append(' ');
        buf.append(src);
        buf.append("\n\t");						// NOI18N
        buf.append(compiler);
        buf.append(' ');
        buf.append(var.makeRef(flagBase));
        buf.append(' ');
        buf.append(var.makeRef("CPPFLAGS_"));				// NOI18N
        buf.append(" -o $@ ");						// NOI18N
        buf.append(src);
        buf.append("\n\n");	// NOI18N

        out.write(buf.toString(), 0, buf.length());
    }

    /** Reorder the sources list based on Fortran module dependencies */
    private String[] getOrderedSources(TargetData t) throws MakefileGenerationException {
        String[] old = t.getSourcesList();
        List<String> neu = new ArrayList<String>();
        List<FortranFile> flist = new LinkedList<FortranFile>();
        String options = md.getCompilerFlags().getF90Flags();
        Map<String, String> moduleList = new HashMap<String, String>();
        boolean firstTime = true;
        int nidx = 0;

        // First pass: Go through all the sources and put all non-fortran sources in the
        // neu list. The fortran source files go in flist to be reordered and added later.
        for (int i = 0; i < old.length; i++) {
            String file = old[i];

            if (isFortranFile(file)) {

                if (file.charAt(0) != '/') {
                    file = md.getBaseDirectory() + File.separator + file;
                }
                FortranParser parser = new FortranParser(file, options, true, true);
                ArrayList list = parser.parser();

                if (list == null) {
                    if (firstTime) {
                        firstTime = false;
                        String title = getString("DLG_ErrorDialogTitle");	// NOI18N
                        String msg = getString("MSG_ModuleDependencyError");	// NOI18N
                        NotifyDescriptor nd = new NotifyDescriptor(msg,
                                title, NotifyDescriptor.DEFAULT_OPTION,
                                NotifyDescriptor.ERROR_MESSAGE,
                                new Object[]{NotifyDescriptor.OK_OPTION},
                                NotifyDescriptor.OK_OPTION);

                        // Post the Error Window
                        DialogDisplayer.getDefault().notify(nd);
                    }
                    return old;
                } else if (list.size() == 0) {
                    // Fortran files which do not define/use modules
                    neu.add(file);
                } else {
                    // Fortran files which DO define and/or use modules
                    flist.add(new FortranFile(file, list));

                    for (int j = 0; j < list.size(); j++) {
                        String entry = list.get(j).toString();

                        // Add all module definitions to the module list. Verify its not a
                        // duplicate definition (which would be an error).
                        if (entry.charAt(0) == 'M') {
                            Object o = moduleList.get(entry.substring(1));
                            if (o != null) {
                                if (firstTime) {
                                    firstTime = false;
                                    String title, msg;

                                    title = getString("DLG_ErrorDialogTitle");    // NOI18N
                                    msg = MessageFormat.format(
                                            getString("MSG_DuplicateModuleError"), // NOI18N
                                            new Object[]{
                                                entry.substring(1), // module name
                                                t.getName(), // target name
                                                file, // 2nd file name
                                                o.toString() // 1st file path
                                            });
                                    NotifyDescriptor nd = new NotifyDescriptor(msg,
                                            title, NotifyDescriptor.DEFAULT_OPTION,
                                            NotifyDescriptor.ERROR_MESSAGE,
                                            new Object[]{NotifyDescriptor.OK_OPTION},
                                            NotifyDescriptor.OK_OPTION);

                                    // Post the Error Window
                                    DialogDisplayer.getDefault().notify(nd);
                                    throw new MakefileGenerationException();
                                }
                            } else {
                                moduleList.put(entry.substring(1), file);
                            }
                        }
                    }
                }
            } else {
                // non-Fortran sources go to the head of the list
                neu.add(file);
            }
        }

        // Second stage: Visit each entry in flist and create the dependsOn list
        Iterator iter = flist.iterator();
        while (iter.hasNext()) {
            FortranFile file = (FortranFile) iter.next();
            ArrayList list = file.getRawList();

            for (int j = 0; j < list.size(); j++) {
                String entry = list.get(j).toString();

                if (entry.charAt(0) == 'U') {
                    String dep = moduleList.get(entry.substring(1));

                    if (dep != null && !dep.equals(file.getName())) {
                        file.addDependsOn(dep);
                    }
                }
            }
        }


        /*
         * Third stage: Order the files based on the dependency graph.
         */
        while (flist.size() > 0) {
            boolean foundOne = false;

            iter = flist.iterator();
            while (iter.hasNext()) {
                FortranFile file = (FortranFile) iter.next();

                if (file.getDependsOn() == null) {
                    neu.add(file.getName());
                    iter.remove();
                    remove(flist.iterator(), file.getName());
                    foundOne = true;
                    break;
                }
            }

            if (foundOne == false) {
                // We only have circular dependencies left

                if (firstTime) {
                    firstTime = false;
                    String title = getString("DLG_ErrorDialogTitle");		// NOI18N
                    String msg = getString("MSG_CircularDependency");	// NOI18N
                    NotifyDescriptor nd = new NotifyDescriptor(msg,
                            title, NotifyDescriptor.DEFAULT_OPTION,
                            NotifyDescriptor.ERROR_MESSAGE,
                            new Object[]{NotifyDescriptor.OK_OPTION},
                            NotifyDescriptor.OK_OPTION);

                    // Post the Error Window
                    DialogDisplayer.getDefault().notify(nd);
                }

                // Arbitrarily remove a dependsOn and see if this breaks the logjam
                iter = flist.iterator();
                FortranFile file = (FortranFile) iter.next();
                remove(flist.iterator(), null);
            }
        }

        return neu.toArray(new String[neu.size()]);
    }

    /** Remove references to this file from all dependsOn lists */
    private void remove(Iterator iter, String name) {

        while (iter.hasNext()) {
            FortranFile file = (FortranFile) iter.next();

            ArrayList deps = file.getDependsOn();
            if (deps != null) {
                for (int i = 0; i < deps.size(); i++) {
                    if (name == null || name.equals(deps.get(i))) {
                        deps.remove(i);
                        if (deps.size() == 0) {
                            file.setDependsOn(null);
                        }
                        break;
                    }
                }
            }
        }
    }

    /** Check to see if file is a fortran file (just looking at the extension) */
    private boolean isFortranFile(String file) {
        return MIMENames.FORTRAN_MIME_TYPE.equals(MIMESupport.getKnownSourceFileMIMETypeByExtension(file));
    }

    /** A class holding module definition and use information for a Fortran file */
    private static class FortranFile {

        /** The file name of a Fortran 90 file */
        private String name;
        /** The raw module/use information returned by FortranParser.parser() */
        private ArrayList rawList;
        /** The (possibly null) list of modules this file uses (depends on) */
        private ArrayList<String> dependsOn;

        /** Create one of these for each Fortran file */
        public FortranFile(String name, ArrayList rawList) {
            this.name = new String(name);
            this.rawList = rawList;
            dependsOn = null;
        }

        /** Add a use record to this file */
        public final void addDependsOn(String dep) {

            if (dependsOn == null) {
                dependsOn = new ArrayList<String>();
            }

            for (int i = 0; i < dependsOn.size(); i++) {
                if (dep.equals(dependsOn.get(i))) {
                    return;		// already in the list
                }
            }
            dependsOn.add(dep);
        }

        /** Get the dependsOn of this file */
        public final ArrayList getDependsOn() {
            return dependsOn;
        }

        /** Set the dependsOn of this file */
        public final void setDependsOn(ArrayList<String> dependsOn) {
            this.dependsOn = dependsOn;
        }

        /** Getter for the name */
        public final String getName() {
            return name;
        }

        /** Get the raw data list */
        public final ArrayList getRawList() {
            return rawList;
        }
    }

    /**
     *  Remove the suffix of a src file and replace it with 'o'. Also
     *  remove any leading directory information. The object file will
     *  be generated in the OBJS_<target> directory.
     *
     *  @param file The source filename
     *  @return The object file name
     */
    private String objectOf(String file) {
        StringBuilder obj = new StringBuilder(80);
        int start = file.lastIndexOf('/') + 1;
        int dot = file.substring(start).lastIndexOf('.');

        if (dot >= 0) {
            obj.append(file.substring(start, start + dot + 1));
            obj.append('o');
            return obj.toString();
        } else {
            return null;
        }
    }

    private void sharedLibFlags(TargetData t) throws IOException {
        StringBuffer bufx = new StringBuffer(1024);
        appendOSDepMacro(bufx, var.makeName("SHAREDLIB_FLAGS_"), "-G -norunpath -h " + t.getName() + " ", "-shared ", "-dynamiclib -install_name " + t.getName()); // NOI18N
        out.write(bufx.toString(), 0, bufx.length());
    }

    /**
     *  Build a link line for either an executable or shared library.
     *
     *  @param t    The TargetData
     *  @param type The type (Executable or SharedLib)
     */
    private String linkLine(TargetData t, int type) throws IOException {
        StringBuilder lbuf = new StringBuilder(256);
        String linker;
        String flags;

        // Set compiler and flags
        if (t.containsCppFiles()) {
            linker = "$(LINK.cc) ";					// NOI18N
            flags = "CCFLAGS_";						// NOI18N
        } else if (t.containsFortranFiles()) {
            linker = "$(LINK.f) ";					// NOI18N
            flags = "FFLAGS_";					// NOI18N
        } else {
            linker = "$(LINK.c) ";					// NOI18N
            flags = "CFLAGS_";						// NOI18N
        }

        // Now start emiting the link line
        lbuf.append(linker);
        lbuf.append(var.makeRef(flags));
        lbuf.append(' ');
        lbuf.append(var.makeRef("CPPFLAGS_"));				// NOI18N
        lbuf.append(" -o $@ ");						// NOI18N
        lbuf.append(var.makeRef("OBJS_"));				// NOI18N
        lbuf.append(' ');

        if (type == TargetData.SIMPLE_SHAREDLIB || type == TargetData.COMPLEX_SHAREDLIB) {
            // Add the Shared Library flags
            sharedLibFlags(t);
            lbuf.append("$(" + var.makeName("SHAREDLIB_FLAGS_") + ") "); // NOI18N
        }

        lbuf.append(var.makeRef("LDLIBS_"));				// NOI18N
        if (t.containsXdFiles()) {
            lbuf.append(xdLinkMagic(t));
        }
        lbuf.append('\n');

        return lbuf.toString();
    }

    /**
     *  Emit X-Designer link library magic. The mogic is needed because the X-Designer link
     *  line comes from the X-Designer generated Makefile so the Makefile we are
     *  creating doesn't know about it. To get around this, we extract the
     *  information into files and create variables which read these files
     *  during the compile.
     */
    private String xdLinkMagic(TargetData t) {
        StringBuilder lbuf = new StringBuilder(256);

        String[] srcs = t.getSourcesList();
        for (int i = 0; i < srcs.length; i++) {
            String file = srcs[i];

            if (t.isXdFile(srcs[i])) {
                String vname = getXdTargetName(t, file);

                lbuf.append(" \\\n\t\t");				// NOI18N
                lbuf.append("$(XD_OBJS_");				// NOI18N
                lbuf.append(vname);
                lbuf.append(") $(XD_LINK_ARGS_");			// NOI18N
                lbuf.append(vname);
                lbuf.append(')');
            }
        }
        return lbuf.toString();
    }

    /**
     *  Gather special Xd link depenencies
     */
    private String xdLinkDependencies(TargetData t) {
        StringBuilder lbuf = new StringBuilder(256);

        String[] srcs = t.getSourcesList();
        for (int i = 0; i < srcs.length; i++) {
            String file = srcs[i];

            if (t.isXdFile(srcs[i])) {
                String vname = getXdTargetName(t, file);

                lbuf.append(" \\\n\t\t");				// NOI18N
                lbuf.append(var.makeRef("TARGETDIR_"));			// NOI18N
                lbuf.append("/$(XD_OBJS_FILE_");			// NOI18N
                lbuf.append(vname);
                lbuf.append(") $(TARGETDIR_");				// NOI18N
                lbuf.append(t.getName());
                lbuf.append(")/$(XD_LINK_ARGS_FILE_");		// NOI18N
                lbuf.append(vname);
                lbuf.append(')');
            }
        }
        return lbuf.toString();
    }

    /**
     *  Gather the system libraries specified on the Standard Libs panel.
     *
     *  @param t    The TargetData for this target
     *  @return	    String containing all specified libraries
     */
    private String getSysLibs(TargetData t) {
        StdLibFlags flags = t.getStdLibFlags();

        buf.delete(0, buf.length());
        buf.append(flags.getSysLibFlags(md.getToolset(), md.getMakefileOS(), md.getCompilerFlags().is64Bit(), t));
        /*
        if (flags.getLinkType() == StdLibFlags.DYNAMIC_LINK_TYPE) {
        if (md.getToolset() == MakefileData.SUN_TOOLSET_TYPE) {
        //buf.append("-Bdynamic ");					// NOI18N
        ; // nothing
        }
        else {
        ; // nothing
        }
        } else if (flags.getLinkType() == StdLibFlags.STATIC_LINK_TYPE) {
        if (md.getToolset() == MakefileData.SUN_TOOLSET_TYPE) {
        buf.append("-Bstatic ");					// NOI18N
        }
        else {
        buf.append("-static ");					// NOI18N
        }
        }

        // X-Designer will supply these libraries another way
        if (flags.isMotifLibs() && !t.containsXdFiles()) {
        CompilerFlags cflags = md.getCompilerFlags();

        if (md.getMakefileOS() == MakefileData.SOLARIS_OS_TYPE) {
        if (cflags.is64Bit()) {
        buf.append("-L/usr/openwin/lib/sparcv9 ");		// NOI18N
        buf.append("-L/usr/dt/lib/sparcv9 ");		 	// NOI18N
        buf.append("-R/usr/openwin/lib/sparcv9 ");		// NOI18N
        buf.append("-R/usr/dt/lib/sparcv9 ");		 	// NOI18N
        } else {
        buf.append("-L/usr/openwin/lib -L/usr/dt/lib ");	// NOI18N
        buf.append("-R/usr/openwin/lib -R/usr/dt/lib ");	// NOI18N
        }
        }
        else if (md.getMakefileOS() == MakefileData.LINUX_OS_TYPE) {
        if (cflags.is64Bit()) {
        // ???
        buf.append("-L/usr/X11R6/lib ");	// NOI18N
        }
        else {
        buf.append("-L/usr/X11R6/lib ");	// NOI18N
        }
        }
        else {
        ; // FIXUP - error
        }
        }

        StdLib[] stdLibs;
        if (md.getMakefileOS() == MakefileData.SOLARIS_OS_TYPE) {
        stdLibs = t.getStdLibFlags().getSolarisStdLibs();
        }
        else {
        stdLibs = t.getStdLibFlags().getLinuxStdLibs();
        }

        // Unset certain libs if x-designer
        if (t.containsXdFiles()) {
        flags.motif.setUsed(false);
        flags.socketnsl.setUsed(false);
        flags.genlib.setUsed(false);
        }

        for (int i = 0; i < stdLibs.length; i++) {
        if (stdLibs[i].isUsed())
        buf.append(stdLibs[i].getCmd());
        }
         */

        if (buf.length() > 0) {
            buf.insert(0, " = ");					// NOI18N
            buf.insert(0, var.makeName("SYSLIBS_"));			// NOI18N
            buf.append('\n');
        }
        return buf.toString();
    }

    /**
     *  Put the user libraries into a usable format and return the caller.
     *
     *  @param t    The TargetData for this target
     *  @return	    String containing all user libraries
     */
    private String getUserLibs(TargetData t) {
        String[] ulibs = t.getUserLibsList();

        buf.delete(0, buf.length());
        if (ulibs != null) {
            for (int i = 0; i < ulibs.length; i++) {
                String lib = ulibs[i];
                buf.append(lib);
                buf.append(' ');
            }
        }

        if (buf.length() > 0) {
            buf.insert(0, " = ");					// NOI18N
            buf.insert(0, var.makeName("USERLIBS_"));			// NOI18N
            buf.append('\n');
        }
        return buf.toString();
    }

    /**
     *  The user libraries can be either libraries developed by the user or
     *  system libraries which are not in the Standard Libraries panel. The
     *  libraries developed by the user should be used as dependencies for the
     *  link step. This method tries to determine which user libraries should
     *  be used as dependencies. In general, we will generate a dependency for
     *  all user libraries specified as a pathname.
     *
     *  @param t    The TargetData for this target
     *  @return	    String containing all dependent libraries
     */
    private String getDependLibs(TargetData t) {
        String[] ulibs = t.getUserLibsList();

        buf.delete(0, buf.length());
        if (ulibs != null) {
            for (int i = 0; i < ulibs.length; i++) {
                String lib = ulibs[i];

                if (lib.charAt(0) != '-' && lib.charAt(0) != '/' &&
                        (lib.endsWith(".a") || lib.endsWith(".so") || lib.endsWith(".dylib") || lib.endsWith(".dll"))) {	// NOI18N
                    buf.append(lib);
                    buf.append(' ');
                }
                buf.append(' ');
            }
        }

        if (buf.length() > 0) {
            buf.insert(0, " = ");					// NOI18N
            buf.insert(0, var.makeName("DEPLIBS_"));			// NOI18N
            buf.append('\n');
        }
        return buf.toString();
    }

    /**
     *  Thie doList method is used to generate Makefile variables with a list
     *  of values. The variable name will be on its own line with each item in
     *  the list of a following line with a tab indent.
     *
     *  @param var	The Make variable name
     *  @param prefix	An optional String to prepend each item
     *  @param list	An array of Strings, where each String is a list item
     */
    private String doList(String var, String prefix, String[] list) {
        StringBuilder lbuf = new StringBuilder(256);

        if (prefix == null) {
            prefix = " \\\n\t";						// NOI18N
        } else {
            prefix = new StringBuilder(" \\\n\t").append(prefix).toString();	// NOI18N
        }

        lbuf.append(var);
        lbuf.append(" =");						// NOI18N
        for (int i = 0; i < list.length; i++) {
            lbuf.append(prefix);
            lbuf.append(list[i]);
        }
        lbuf.append('\n');
        return lbuf.toString();
    }

    /** Generate target-specific flag variables for each language needing one */
    private String generateTargetFlags(TargetData t) {
        StringBuilder flagsSun = new StringBuilder(80);

        /*
         * See if we have shared per-target flags (these are flags which are
         * needed on a per-target basis for each language used in this
         * Makefile).
         */
        if (t.getTargetType() == TargetData.COMPLEX_SHAREDLIB ||
                t.getTargetType() == TargetData.SIMPLE_SHAREDLIB) {
            flagsSun.append("-Kpic ");					// NOI18N
        }

        buf.delete(0, buf.length());
        if (flagsSun.length() > 0) {
            int toolset = md.getToolset();

            // Check for CFLAGS_<target-name> first
            if (t.containsCFiles()) {
                appendToolsetDepMacro(var.makeName("CFLAGS_"), flagsSun.toString(), ""); // NOI18N
            }

            // Check for CCFLAGS_<target-name> first
            if (t.containsCppFiles()) {
                appendToolsetDepMacro(var.makeName("CCFLAGS_"), flagsSun.toString(), ""); // NOI18N
            }

            // Check for FFLAGS_<target-name> first
            if (t.containsFortranFiles()) {
                appendToolsetDepMacro(var.makeName("FFLAGS_"), flagsSun.toString(), ""); // NOI18N
            }
        }
        return buf.toString();
    }

    /**
     *  Given a list of source files, replace the suffix with ".o" and return
     *  a list of object files.
     *
     *  @param srcs	A String[] of source files
     *  @return		A String[] of object files
     */
    private String[] getObjectList(String[] srcs) {
        String[] objs = new String[srcs.length];

        for (int i = 0; i < srcs.length; i++) {
            int dot = srcs[i].lastIndexOf('.');
            if (dot >= 0) {
                StringBuilder obj =
                        new StringBuilder(srcs[i].substring(0, dot + 1));
                obj.append('o');
                objs[i] = new String(obj);
            }
        }
        return objs;
    }

    /**
     *  Check for situations where one of the Makefile's targets is incomplete.
     *  Currently we only check compilable targets for source files. This may
     *  change in the future.
     */
    private boolean targetIsIncomplete() {
        List<TargetData> tlist = md.getTargetList();

        for (int i = 0; i < tlist.size(); i++) {
            TargetData t = tlist.get(i);

            if (t.isCompilable() && (t.getSourcesList() == null ||
                    t.getSourcesList().length == 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Traverse each target and set some of the fields which need setting before
     * code can be generated.
     */
    private void completeTargets() {
        List<TargetData> tlist = md.getTargetList();

        for (int i = 0; i < tlist.size(); i++) {
        }
    }

    /**
     *  Test the given file to see if its a Makefile we generated. We won't
     *  overwrite a Makefile we did not generate.
     */
    public static boolean generatedMakefile(File mf) {
        String[] line = new String[3];
        int i = 0;

        try {
            BufferedReader in = new BufferedReader(new FileReader(mf));
            line[i++] = in.readLine();
            line[i++] = in.readLine();
            line[i++] = in.readLine();
            in.close();
        } catch (IOException ex) {
            return false;
        }

        if (line[2] != null &&
                line[0].equals(LINE1) &&
                line[1].equals(BLANKCOMMENT) &&
                line[2].startsWith(LINE_PREFIX)) {
            return true;
        } else {
            return false;
        }
    }

    /** Validate or create the Current build directory */
    private int validateOrCreateCwd() {
        String cwd = md.getBaseDirectory(MakefileData.EXPAND);
        File fcwd = new File(cwd);


        return validateOrCreateDir(cwd, fcwd,
                getString("MSG_CwdNotDir"));			// NOI18N
    }

    /** Validate or create the directory the Makefile will be generated in */
    private int validateOrCreateMakefileDir() {
        String makefile = md.getMakefileName();

        if (makefile.charAt(0) == File.separatorChar) {
            fmakefile = new File(makefile);
        } else {
            fmakefile = new File(md.getBaseDirectory(MakefileData.EXPAND), makefile);
        }

        String parent = fmakefile.getParent();
        File fparent = fmakefile.getParentFile();

        return validateOrCreateDir(parent, fparent,
                getString("MSG_MakefileDirNotDir"));		// NOI18N
    }

    /**
     *  Validate the given directory. If it doesn't exist try and create it.
     *  Perform several validation tests and post dialogs if they fail. If the
     *  requested directory doesn't exist try and create it. Post an error if
     *  it cannot be created.
     *
     * @param dir   The directory to validate/create in String form
     * @param fdir  The directory to validate/create in File form
     * @param msg   The message to post if dir exists but is not a directory
     *
     * @return	    OK if the directory exists or was successfully created
     */
    private int validateOrCreateDir(String dir, File fdir, String msg) {
        NotifyDescriptor nd;
        String s;
        String title;

        if (fdir.exists() && !fdir.isDirectory()) {
            s = MessageFormat.format(msg, new Object[]{dir});
            title = getString("DLG_ErrorDialogTitle");			// NOI18N
            nd = new NotifyDescriptor(s,
                    title, NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object[]{NotifyDescriptor.OK_OPTION},
                    NotifyDescriptor.OK_OPTION);

            // Post the Error Window
            DialogDisplayer.getDefault().notify(nd);
            return BAD;
        }

        if (!fdir.exists()) {
            s = MessageFormat.format(
                    getString("MSG_CreateDirectory"), // NOI18N
                    new Object[]{dir});
            title = getString("DLG_QuestionDialogTitle");		// NOI18N
            nd = new NotifyDescriptor(s,
                    title, NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    null, NotifyDescriptor.YES_OPTION);

            // Post the Question Window. Try and create directory if requested
            Object ret = DialogDisplayer.getDefault().notify(nd);
            if (ret == NotifyDescriptor.NO_OPTION || !mkdirs(dir, fdir)) {
                return BAD;
            }
        }

        return OK;
    }

    /** Get a timestamp in the current locale */
    private String getTimestamp() {
        Date date = new Date();
        StringBuilder timestamp = new StringBuilder(80);

        timestamp.append(DateFormat.getDateInstance().format(date));
        timestamp.append(" ");						// NOI18N
        timestamp.append(DateFormat.getTimeInstance().format(date));

        return timestamp.toString();
    }

    /** Get the user name from the Unix environment */
    private String getUserName() {

        String user = System.getProperty("Env-USER");			// NOI18N
        if (user == null) {
            user = System.getProperty("Env-LOGNAME");			// NOI18N
            if (user == null) {
                user = System.getProperty("user.name");			// NOI18N
                if (user == null) {
                    user = getString("UNKNOWN_USER");			// NOI18N
                }
            }
        }

        return user;
    }

    /** Try and make the directory. Post an error dialog if this fails */
    private boolean mkdirs(String name, File file) {

        if (file.mkdirs() == false) {
            String msg = MessageFormat.format(
                    getString("MSG_CannotCreateDirectory"), // NOI18N
                    new Object[]{name});
            String title = getString("DLG_ErrorDialogTitle");		// NOI18N
            NotifyDescriptor nd = new NotifyDescriptor(msg,
                    title, NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object[]{NotifyDescriptor.OK_OPTION},
                    NotifyDescriptor.OK_OPTION);

            // Post the Error Window
            DialogDisplayer.getDefault().notify(nd);
            return false;
        } else {
            return true;
        }
    }

    /** Iterate through the target list and see which languages are used */
    private void checkTargetFlags() {
        List<TargetData> tlist = md.getTargetList();

        for (int i = 0; i < tlist.size(); i++) {
            TargetData t = tlist.get(i);
            if (t.isComplex()) {
                doComplex = true;
            }
            if (t.containsCppFiles()) {
                doCpp = true;
            }
            if (t.containsCFiles()) {
                doC = true;
            }
            if (t.containsFortranFiles()) {
                doFortran = true;
            }
            if (t.containsXdFiles()) {
                doXd = true;
                doC = true;	    		// since X-Designer can generate C
                doCpp = true;		    	// since X-Designer can generate C++
            }
            if (t.containsAssemblyFiles()) {
                doAssembly = true;
            }
        }
    }

    /** Helper method for getting a string from a bundle */
    protected String getString(String s) {
        return bundle.getString(s);
    }

    /**
     *  We need to emit lots of make variables of the form "$(foo_bar)", where
     *  "foo" is the related to the variable we want to creat and "bar" is
     *  related to the current target. This class is a helper class which
     *  creates these names for us. To cut down on object creation its intended
     *  to be reused with different variables and targets.
     */
    private static final class MakeVarName {

        private String targetName;		// this gets appended to name
        private StringBuffer lastName;		// save the last name created
        private StringBuffer lastRef;		// save the last ref created
        private StringBuffer lastSuffix;	// check if same as last call
        private StringBuffer buffer = new StringBuffer(80);

        public MakeVarName() {
            targetName = null;
            lastName = new StringBuffer(80);
            lastRef = new StringBuffer(80);
            lastSuffix = new StringBuffer(20);
        }

        /**
         *  Change the targetName so we can reuse this same object with another
         *  target.
         */
        public void setTargetName(String targetName) {
            this.targetName = targetName;

            lastName.delete(0, lastName.length());
            lastRef.delete(0, lastRef.length());
            lastSuffix.delete(0, lastSuffix.length());
        }

        /**
         *  Return a string with the desired name. Cache the last suffix and
         *  returned string so we don't need to recreate it if we match the
         *  last call. This should happen fairly often.
         */
        public String makeName(String suffix) {

            if (suffix.equals(lastSuffix.toString())) {
                return lastName.toString();
            } else {
                buffer.replace(0, buffer.length(), suffix);
                buffer.append(targetName);
                lastName.replace(0, lastName.length(), buffer.toString());
                return buffer.toString();
            }
        }

        /**
         *  Return a string with the desired name. This flavor allows an extra
         *  string to be appended to the name.
         */
        public String makeName(String suffix, String extra) {

            if (suffix.equals(lastSuffix.toString())) {
                return lastName.toString();
            } else {
                buffer.replace(0, buffer.length(), suffix);
                buffer.append(targetName);
                buffer.append("_");					// NOI18N
                buffer.append(extra);
                lastName.replace(0, lastName.length(), buffer.toString());
                return buffer.toString();
            }
        }

        /**
         *  Return a string with the desired variable reference. Cache the last
         *  suffix and returned string so we don't need to recreate it if we
         *  match the last call. This should happen fairly often.
         */
        public String makeRef(String suffix) {

            if (suffix.equals(lastSuffix.toString())) {
                return lastRef.toString();
            } else {
                buffer.replace(0, buffer.length(), "$(");		// NOI18N
                buffer.append(suffix);
                buffer.append(targetName);
                buffer.append(")");					// NOI18N
                lastRef.replace(0, lastRef.length(), buffer.toString());
                return buffer.toString();
            }
        }

        /**
         *  Return a string with the desired variable reference. This flavor
         *  allows an extra string to be appended to the name.
         */
        public String makeRef(String suffix, String extra) {

            if (suffix.equals(lastSuffix.toString())) {
                return lastRef.toString();
            } else {
                buffer.replace(0, buffer.length(), "$(");		// NOI18N
                buffer.append(suffix);
                buffer.append(targetName);
                buffer.append("_");					// NOI18N
                buffer.append(extra);
                buffer.append(")");					// NOI18N
                lastRef.replace(0, lastRef.length(), buffer.toString());
                return buffer.toString();
            }
        }

        /** Return the last name we created */
        public String lastName() {
            return lastName.toString();
        }

        /** Return the last variable reference we created */
        public String lastRef() {
            return lastRef.toString();
        }
    }

    private static final class MakefileGenerationException extends Exception {

        public MakefileGenerationException() {
            super();
        }

        public MakefileGenerationException(String s) {
            super(s);
        }
    }

    /**
     * Get the output directory. If it is of the form <platform>, return
     * a macro value, othervise return the value.
     */
    private String getOutputDirectory(TargetData t) {
        String outputDirectory = t.getOutputDirectory();
        if (outputDirectory.equals(getString("OutputDirectoryPlatform"))) // NOI18N
        {
            return "$(PLATFORM)"; // NOI18N
        } else {
            return outputDirectory;
        }
    }

    /**
     * Append an toolset dependent macro to the global stringbuffer buf.
     */
    private void appendToolsetDepMacro(String macro, String sunVal, String gnuVal) {
        appendToolsetDepMacro(buf, macro, sunVal, gnuVal);
    }

    /**
     * Append an toolset dependent macro to the stringbuffer sbuf.
     */
    private void appendToolsetDepMacro(StringBuffer sbuf, String macro, String sunVal, String gnuVal) {
        int toolset = md.getToolset();
        if (toolset == MakefileData.SUN_TOOLSET_TYPE) {
            sbuf.append(macro);
            sbuf.append(" = ");	// NOI18N
            sbuf.append(sunVal);
            sbuf.append('\n');	// NOI18N
        } else if (toolset == MakefileData.GNU_TOOLSET_TYPE) {
            sbuf.append(macro);
            sbuf.append(" = ");	// NOI18N
            sbuf.append(gnuVal);
            sbuf.append('\n');	// NOI18N
        } else if (toolset == MakefileData.SUNGNU_TOOLSET_TYPE) {
            sbuf.append(macro + "-SUN");			// NOI18N
            sbuf.append(" = ");					// NOI18N
            sbuf.append(sunVal);
            sbuf.append('\n');
            sbuf.append(macro + "-GNU");			// NOI18N
            sbuf.append(" = ");					// NOI18N
            sbuf.append(gnuVal);
            sbuf.append('\n');
            sbuf.append(macro + " = $(" + macro + "-$(TOOLSET))");		// NOI18N
            sbuf.append('\n');
        } else {
            // FIXUP - error
        }
    }

    /**
     * Append an toolset dependent macro to the global stringbuffer buf.
     */
    private void appendToolsetDepMacro(String sunMacro, String sunVal, String gnuMacro, String gnuVal) {
        appendToolsetDepMacro(buf, sunMacro, sunVal, gnuMacro, gnuVal);
    }

    /**
     * Append an toolset dependent macro to the stringbuffer sbuf.
     */
    private void appendToolsetDepMacro(StringBuffer sbuf, String sunMacro, String sunVal, String gnuMacro, String gnuVal) {
        int toolset = md.getToolset();
        if (toolset == MakefileData.SUN_TOOLSET_TYPE || toolset == MakefileData.SUNGNU_TOOLSET_TYPE) {
            sbuf.append(sunMacro);
            sbuf.append(" = ");	// NOI18N
            sbuf.append(sunVal);
            sbuf.append('\n');	// NOI18N
        }
        if (toolset == MakefileData.GNU_TOOLSET_TYPE || toolset == MakefileData.SUNGNU_TOOLSET_TYPE) {
            sbuf.append(gnuMacro);
            sbuf.append(" = ");	// NOI18N
            sbuf.append(gnuVal);
            sbuf.append('\n');	// NOI18N
        }
    }

    /**
     * Append a macro
     */
    private void appendMacro(String macro, String val) {
        appendMacro(buf, macro, val);
    }

    /**
     * Append a macro
     */
    private void appendMacro(StringBuffer sbuf, String macro, String val) {
        sbuf.append(macro);
        sbuf.append(" = ");	// NOI18N
        sbuf.append(val);
        sbuf.append('\n');	// NOI18N
    }

    /**
     * Append an OS dependent macro to the global stringbuffer buf.
     */
    private void appendOSDepMacro(String macro, String sunVal, String gnuVal, String macosxVal) {
        appendOSDepMacro(buf, macro, sunVal, gnuVal, macosxVal);
    }

    /**
     * Append an OS dependent macro to the stringbuffer sbuf.
     */
    private void appendOSDepMacro(StringBuffer sbuf, String macro, String sunVal, String gnuVal, String macosxVal) {
        int os = md.getMakefileOS();
        if (os == MakefileData.SOLARIS_OS_TYPE) {
            sbuf.append(macro);
            sbuf.append(" = ");	// NOI18N
            sbuf.append(sunVal);
            sbuf.append('\n');	// NOI18N
        } else if (os == MakefileData.LINUX_OS_TYPE) {
            sbuf.append(macro);
            sbuf.append(" = ");	// NOI18N
            sbuf.append(gnuVal);
            sbuf.append('\n');	// NOI18N
        } else if (os == MakefileData.MACOSX_OS_TYPE) {
            sbuf.append(macro);
            sbuf.append(" = ");	// NOI18N
            sbuf.append(macosxVal);
            sbuf.append('\n');	// NOI18N
        } else if (os == MakefileData.UNIX_OS_TYPE) {
            sbuf.append(macro + "-SunOS");			// NOI18N
            sbuf.append(" = ");					// NOI18N
            sbuf.append(sunVal);
            sbuf.append('\n');
            sbuf.append(macro + "-Linux");			// NOI18N
            sbuf.append(" = ");					// NOI18N
            sbuf.append(gnuVal);
            sbuf.append('\n');
            sbuf.append(macro + " = $(" + macro + "-$(OS))");		// NOI18N
            sbuf.append('\n');
        } else {
            // FIXUP - error
        }
    }
}
