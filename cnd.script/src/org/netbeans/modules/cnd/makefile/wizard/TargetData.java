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

package  org.netbeans.modules.cnd.makefile.wizard;

import java.util.ArrayList;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;

/**
 * A TargetData record. This class should contain ALL iformation necessary to
 * create a specific Makefile target.
 */

final public class TargetData {

    /** Which type of target this is */
    private int targetType;

    /** The name of the target */
    private String name;

    /** The directory for object files and other transients */
    private String outputDirectory;

    /** The unique target key */
    private int key;

    /** The list of source files for this target */
    private String[] sourcesList;

    /** The list of include directories for this target */
    private String[] includesList;

    /** What system libraries to link with */
    private StdLibFlags stdLibFlags;

    /** The list of user libraries for this target */
    private String[] userLibsList;

    /** The target called in a recursive make command */
    private String targetName;

    /** The dependencies of the target in a recursive make command */
    private String dependsOn;

    /** The subdirectory a recursive make command is run in */
    private String subdirectory;

    /** The command line flags for a recursive make command */
    private String makeFlags;

    /** The actions for a custom Makefile target */
    private ArrayList actions;

    /** This target is compilable (if set ) */
    private boolean compilable;

    /** This target has one of the complex targetTypes */
    private boolean complex;

    /** This target contains C++ source files */
    private boolean haveCppFiles;

    /** This target contains C source files */
    private boolean haveCFiles;

    /** This target contains Fortran source files */
    private boolean haveFortranFiles;

    /** This target contains X-Designer source files */
    private boolean haveXdFiles;

    /** This target contains Assembly source files */
    private boolean haveAssemblyFiles;

    /** Single target corresponds to EXECUTABLE_MAKEFILE_TYPE */
    public final static int SIMPLE_EXECUTABLE =
				MakefileData.EXECUTABLE_MAKEFILE_TYPE;

    /** Single target corresponds to ARCHIVE_MAKEFILE_TYPE */
    public final static int SIMPLE_ARCHIVE =
				MakefileData.ARCHIVE_MAKEFILE_TYPE;

    /** Single target corresponds to SHAREDLIB_MAKEFILE_TYPE */
    public final static int SIMPLE_SHAREDLIB =
				MakefileData.SHAREDLIB_MAKEFILE_TYPE;

    /** Complex target for an executable */
    public final static int COMPLEX_EXECUTABLE =
				MakefileData.COMPLEX_MAKEFILE_TYPE + 1;

    /** Complex target for an archive */
    public final static int COMPLEX_ARCHIVE =
				MakefileData.COMPLEX_MAKEFILE_TYPE + 2;

    /** Complex target for a shared library */
    public final static int COMPLEX_SHAREDLIB =
				MakefileData.COMPLEX_MAKEFILE_TYPE + 3;

    /** Complex target for calling a recursive make */
    public final static int COMPLEX_MAKE_TARGET =
				MakefileData.COMPLEX_MAKEFILE_TYPE + 4;

    /** Custom target */
    public final static int COMPLEX_CUSTOM_TARGET =
				MakefileData.COMPLEX_MAKEFILE_TYPE + 5;


    /** Create a target data record of a single target */
    public TargetData(int targetType, String name, String outputDirectory, int key) {
    
	this.targetType = targetType;
	this.name = name;
	this.outputDirectory = outputDirectory;
	this.key = key;
	sourcesList = null;
	includesList = null;
	stdLibFlags = new StdLibFlags();
	userLibsList = null;
	targetName = null;
	dependsOn = null;
	subdirectory = null;
	makeFlags = null;
	actions = null;
	compilable = false;
	haveCppFiles = false;
	haveCFiles = false;
	haveFortranFiles = false;
	haveXdFiles = false;
	haveAssemblyFiles = false;

	if (targetType == COMPLEX_MAKE_TARGET || targetType == COMPLEX_CUSTOM_TARGET) {
	    compilable = false;
	} else {
	    compilable = true;
	}

	if (targetType == SIMPLE_EXECUTABLE || targetType == SIMPLE_ARCHIVE ||
			targetType == SIMPLE_SHAREDLIB) {
	    complex = false;
	} else {
	    complex = true;
	}
    }


    /** Create a target data record from another target */
    public TargetData(TargetData old) {
	int i;

	targetType = old.targetType;
	name = new String(old.name);
	outputDirectory = new String(old.outputDirectory);
	key = old.key;
	sourcesList = null;
	targetName = old.targetName;
	dependsOn = old.dependsOn;
	subdirectory = old.subdirectory;
	makeFlags = old.makeFlags;
	actions = old.actions;
	compilable = old.compilable;
	complex = old.complex;
	haveCppFiles = false;
	haveCFiles = false;
	haveFortranFiles = false;
	haveXdFiles = false;
	haveAssemblyFiles = false;

	if (includesList != null) {
	    includesList = new String[old.includesList.length];
	    for (i = 0; i < includesList.length; i++) {
		includesList[i] = old.includesList[i];
	    }
	}
	if (stdLibFlags != null) {
	    stdLibFlags = new StdLibFlags(old.stdLibFlags);
	}
	if (userLibsList != null) {
	    userLibsList = new String[old.userLibsList.length];
	    for (i = 0; i < userLibsList.length; i++) {
		userLibsList[i] = old.userLibsList[i];
	    }
	}
    }


    /** Get the target type */
    public int getTargetType() {
	return targetType;
    }


    /** Getter for target name */
    public String getName() {
	return name;
    }


    /** Setter for target name */
    public void setName(String name) {
	this.name = name;
    }


    /** Getter for target key */
    public int getKey() {
	return key;
    }

    
    /** Getter for the source list */
    public String[] getSourcesList() {
	return sourcesList;
    }

    
    /** Set the source list */
    public void setSourcesList(String[] sourcesList) {
	this.sourcesList = sourcesList;

	haveCppFiles = false;
	haveCFiles = false;
	haveFortranFiles = false;
	haveXdFiles = false;
	haveAssemblyFiles = false;
	for (int i = 0; sourcesList != null && i < sourcesList.length; i++) {
	    if (!haveCppFiles && isCppFile(sourcesList[i])) {
		haveCppFiles = true;
	    }
	    if (!haveCFiles && isCFile(sourcesList[i])) {
		haveCFiles = true;
	    }
	    if (!haveFortranFiles && isFortranFile(sourcesList[i])) {
		haveFortranFiles = true;
	    }
	    if (!haveXdFiles && isXdFile(sourcesList[i])) {
		haveXdFiles = true;
	    }
	    if (!haveAssemblyFiles && isAssemblyFile(sourcesList[i])) {
		haveAssemblyFiles = true;
	    }
	}
    }

    
    /** Let users know if this target has sufficient data */
    public boolean isComplete() {

	if (targetType == COMPLEX_MAKE_TARGET &&
		    (targetName != null || dependsOn != null ||
		     subdirectory != null || makeFlags != null)) {
	    return true;
	} else if (targetType == COMPLEX_CUSTOM_TARGET && actions != null &&
		    actions.size() > 0) {
	    return true;
	} else {
	    return sourcesList != null && sourcesList.length > 0;
	}
    }


    /** Getter for the includes list */
    public String[] getIncludesList() {
	return includesList;
    }


    /** Set the includes list */
    public void setIncludesList(String[] includesList) {
	this.includesList = includesList;
    }


    /** Getter for the user libraries list */
    public String[] getUserLibsList() {
	return userLibsList;
    }


    /** Set the user libraries list */
    public void setUserLibsList(String[] userLibsList) {
	this.userLibsList = userLibsList;
    }


    /** Getter for outputDirectory */
    public String getOutputDirectory() {
	return outputDirectory;
    }


    /** Setter for outputDirectory */
    public void setOutputDirectory(String outputDirectory) {
	String cwd =
		MakefileWizard.getMakefileWizard().getMakefileData().getBaseDirectory();

	if (outputDirectory.startsWith(cwd) && outputDirectory.length() > cwd.length()) {
	    this.outputDirectory = outputDirectory.substring(cwd.length() + 1);
	} else {
	    this.outputDirectory = outputDirectory;
	}
    }


    /** Getter for StdLibFlags */
    public StdLibFlags getStdLibFlags() {
	return stdLibFlags;
    }

    
    /** Getter for targetName */
    public String getTargetName() {
	return targetName;
    }


    /** Setter for targetName */
    public void setTargetName(String targetName) {
	this.targetName = targetName;
    }

    
    /** Getter for dependsOn */
    public String getDependsOn() {
	return dependsOn;
    }


    /** Setter for dependsOn */
    public void setDependsOn(String dependsOn) {
	this.dependsOn = dependsOn;
    }

    
    /** Getter for subdirectory */
    public String getSubdirectory() {
	return subdirectory;
    }


    /** Setter for subdirectory */
    public void setSubdirectory(String subdirectory) {
	this.subdirectory = subdirectory;
    }

    
    /** Getter for makeFlags */
    public String getMakeFlags() {
	return makeFlags;
    }


    /** Setter for makeFlags */
    public void setMakeFlags(String makeFlags) {
	this.makeFlags = makeFlags;
    }


    /** Getter for action */
    public ArrayList getActions() {
	if (actions == null) {
	    actions = new ArrayList();
	}
	return actions;
    }


    /** Setter for actions */
    public void setActions(ArrayList actions) {
	this.actions = actions;
    }


    /**
     * Let the caller know if the target can be converted to the requested type.
     */
    public boolean isConvertable(int newType) {
	return
	(targetType == SIMPLE_EXECUTABLE   && newType == COMPLEX_EXECUTABLE) ||
	(targetType == SIMPLE_ARCHIVE      && newType == COMPLEX_ARCHIVE) ||
	(targetType == SIMPLE_SHAREDLIB    && newType == COMPLEX_SHAREDLIB) ||
	(targetType == COMPLEX_EXECUTABLE  && newType == SIMPLE_EXECUTABLE) ||
	(targetType == COMPLEX_ARCHIVE     && newType == SIMPLE_ARCHIVE) ||
	(targetType == COMPLEX_SHAREDLIB   && newType == SIMPLE_SHAREDLIB);
    }


    /** Convert the SIMPLE_* target to the associated COMPLEX_* type */
    public void convert() {
	CompilerFlags copts = MakefileWizard.getMakefileWizard().
				getMakefileData().getCompilerFlags();

	switch (targetType) {
	case SIMPLE_EXECUTABLE:
	    targetType = COMPLEX_EXECUTABLE;
	    break;

	case SIMPLE_ARCHIVE:
	    targetType = COMPLEX_ARCHIVE;
	    break;

	case SIMPLE_SHAREDLIB:
	    targetType = COMPLEX_SHAREDLIB;
	    break;
	}

	// Now set the compiler flags
	if (copts.isSimpleDebug()) {
	    copts.setOptionSource(OptionSource.DEVELOPMENT);
	    copts.setDevelDebug(true);
	} else if (copts.isSimpleOptimize()) {
	    copts.setOptionSource(OptionSource.FINAL);
	    copts.setFinalOptimize(true);
	}

	// Leave the rest of the informatino as-is. If we convert back its
	// not lost.

	/*
	if (UsageTracking.enabled) {
	    UsageTracking.sendAction("Convert MFW Target", null);	// NOI18N
	}
	*/
    }


    /** Convert the COMPLEX_* target to the associated SIMPLE_* type */
    public void convert(int newType) {
	CompilerFlags copts = MakefileWizard.getMakefileWizard().
				getMakefileData().getCompilerFlags();

	targetType = newType;

	// Now set the compiler flags
	if (copts.getOptionSource() == OptionSource.FINAL) {
	    copts.setSimpleOptimize(copts.isFinalOptimize());
	} else {
	    copts.setSimpleDebug(true);
	    copts.setSimpleOptimize(!copts.isDevelDebug());
	}

	// Leave the rest of the informatino as-is. If we convert back its
	// not lost.

	/*
	if (UsageTracking.enabled) {
	    UsageTracking.sendAction("Convert MFW Target", null);	// NOI18N
	}
	*/
    }


    /** Helper function for checking if target is executable */
    public boolean isExecutable() {
	return targetType == SIMPLE_EXECUTABLE ||
	    targetType == COMPLEX_EXECUTABLE;
    }


    /** Helper function for checking if target is an archive */
    public boolean isArchive() {
	return targetType == SIMPLE_ARCHIVE || targetType == COMPLEX_ARCHIVE;
    }


    /** Helper function for checking if target is a shared library */
    public boolean isSharedLib() {
	return targetType == SIMPLE_SHAREDLIB ||
			    targetType == COMPLEX_SHAREDLIB;
    }


    /** Helper function for checking if target is a recursive make target */
    public boolean isMakeTarget() {
	return targetType == COMPLEX_MAKE_TARGET;
    }


    /** Helper function for checking if target is a custom make target */
    public boolean isCustomTarget() {
	return targetType == COMPLEX_CUSTOM_TARGET;
    }


    /** Check if a file is a C++ file */
    public boolean isCppFile(String file) {
        return MIMENames.CPLUSPLUS_MIME_TYPE.equals(MIMESupport.getKnownSourceFileMIMETypeByExtension(file));
    }


    /** Check if a file is a C file */
    public boolean isCFile(String file) {
        return MIMENames.C_MIME_TYPE.equals(MIMESupport.getKnownSourceFileMIMETypeByExtension(file));
    }


    /** Check if a file is a C/C++ header file */
    public boolean isHdrFile(String file) {
        return MIMENames.HEADER_MIME_TYPE.equals(MIMESupport.getKnownSourceFileMIMETypeByExtension(file));
    }


    /** Check if a file is a Fortran file */
    public boolean isFortranFile(String file) {
        return MIMENames.FORTRAN_MIME_TYPE.equals(MIMESupport.getKnownSourceFileMIMETypeByExtension(file));
    }

    /** Check if a file is a X-Designer file */
    public boolean isXdFile(String file) {
	return file.endsWith(".xd");					// NOI18N
    }


    /** Check if a file is a Assembly file */
    public boolean isAssemblyFile(String file) {
        return MIMENames.ASM_MIME_TYPE.equals(MIMESupport.getKnownSourceFileMIMETypeByExtension(file));
    }


    /** Is this a simple or complex type? */
    public boolean isComplex() {
	return complex;
    }

    /** Does the target get linked? */
    public boolean isLinked() {

	return	    targetType == SIMPLE_EXECUTABLE ||
		    targetType == COMPLEX_EXECUTABLE ||
		    targetType == SIMPLE_SHAREDLIB ||
		    targetType == COMPLEX_SHAREDLIB;
    }


    /** True if this target is a type which contains compilable files */
    public boolean isCompilable() {
	return compilable;
    }


    /** Let caller know if this project contains C++ source files */
    public boolean containsCppFiles() {
	return haveCppFiles;
    }


    /** Let caller know if this project contains C source files */
    public boolean containsCFiles() {
	return haveCFiles;
    }


    /** Let caller know if this project contains Fortran source files */
    public boolean containsFortranFiles() {
	return haveFortranFiles;
    }


    /** Let caller know if this project contains X-Designer source files */
    public boolean containsXdFiles() {
	return haveXdFiles;
    }


    /** Let caller know if this project contains Assembly files */
    public boolean containsAssemblyFiles() {
	return haveAssemblyFiles;
    }
	

    private String indent = new String(""); // NOI18N

    /** Default dump has no indent */
    public void dump() {
	int i;

	//println("Dumping target[" + key + "]:");			// NOI18N
	println("    name            = \"" + name + "\"");		// NOI18N
	println("    type            = " + // NOI18N
	    (targetType == SIMPLE_EXECUTABLE     ? "SIMPLE_EXECUTABLE" :// NOI18N
	     targetType == SIMPLE_ARCHIVE        ? "SIMPLE_ARCHIVE" :	// NOI18N
	     targetType == SIMPLE_SHAREDLIB      ? "SIMPLE_SHAREDLIB" :	// NOI18N
	     targetType == COMPLEX_EXECUTABLE    ? "COMPLEX_EXECUTABLE":// NOI18N
	     targetType == COMPLEX_ARCHIVE       ? "COMPLEX_ARCHIVE" :	// NOI18N
	     targetType == COMPLEX_SHAREDLIB     ? "COMPLEX_SHAREDLIB" :// NOI18N
	     targetType == COMPLEX_MAKE_TARGET ? "COMPLEX_MAKE_TARGET" :// NOI18N
	     targetType == COMPLEX_CUSTOM_TARGET ?
			    "COMPLEX_CUSTOM_TARGET" : "Unknown"));	// NOI18N

	println("    key             = " + key);			// NOI18N
	println("    outputDirectory = \"" + outputDirectory + "\"");	// NOI18N
	if (sourcesList == null || sourcesList.length == 0) {
	    println("    sourcesList     = {}");			// NOI18N
	} else {
	    println("    sourcesList     = {");				// NOI18N
	    for (i = 0; i < sourcesList.length; i++) {
		println("        \"" + sourcesList[i] + "\"");		// NOI18N
	    }
	    println("    }");						// NOI18N
	}
	if (includesList == null || includesList.length == 0) {
	    println("    includesList    = {}");			// NOI18N
	} else {
	    println("    includesList    = {");				// NOI18N
	    for (i = 0; i < includesList.length; i++) {
		println("        \"" + includesList[i] + "\"");		// NOI18N
	    }
	    println("    }");						// NOI18N
	}
	stdLibFlags.dump();
	if (userLibsList == null || userLibsList.length == 0) {
	    println("    userLibsList    = {}");			// NOI18N
	} else {
	    println("    userLibsList    = {");				// NOI18N
	    for (i = 0; i < userLibsList.length; i++) {
		println("        \"" + userLibsList[i] + "\"");		// NOI18N
	    }
	    println("    }");						// NOI18N
	}
	println("    targetName      = \"" + targetName + "\"");	// NOI18N
	println("    dependsOn       = \"" + dependsOn + "\"");		// NOI18N
	println("    subdirectory    = \"" + subdirectory + "\"");	// NOI18N
	println("    makeFlags       = \"" + makeFlags + "\"");		// NOI18N
	if (actions == null || actions.size() == 0) {
	    println("    actions         = {}");			// NOI18N
	} else {
	    println("    actions         = {");    			// NOI18N
	    for (i = 0; i < actions.size(); i++) {
		println("        " + actions.get(i).toString());	// NOI18N
	    }
	    println("    }");						// NOI18N
	}
		
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

    private void setIndent(String indent) {
	this.indent = indent;
    }
}
