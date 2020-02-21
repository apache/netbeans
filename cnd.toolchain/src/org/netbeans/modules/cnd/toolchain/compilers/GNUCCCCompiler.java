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

package org.netbeans.modules.cnd.toolchain.compilers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.ToolKind;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.dwarfdump.source.Driver;
import org.netbeans.modules.cnd.spi.utils.CndNotifier;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * A common base class for GNU C and C++  compilers
 */
/*package*/abstract class GNUCCCCompiler extends CCCCompiler {

    public GNUCCCCompiler(ExecutionEnvironment env, CompilerFlavor flavor, ToolKind kind, String name, String displayName, String path) {
        super(env, flavor, kind, name, displayName, path);
    }

    protected String getCompilerStderrCommand() {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null){
            return " " + compiler.getIncludeFlags(); // NOI18N
        }
        return ""; // NOI18N
    }

    protected String getCompilerStdoutCommand() {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null){
            return " " + compiler.getMacroFlags();  // NOI18N
        }
        return ""; // NOI18N
    }

    @Override
    protected CompilerDefinitions getFreshCompilerDefinitions() {
        CompilerDefinitions res = new CompilerDefinitions();
        try {
            getSystemIncludesAndDefines(getCompilerStderrCommand(), false, res);
            getSystemIncludesAndDefines(getCompilerStdoutCommand(), true, res);
            completePredefinedMacros(res);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
            System.err.println("IOException " + ioe);
            String errormsg;
            if (getExecutionEnvironment().isLocal()) {
                errormsg = NbBundle.getMessage(getClass(), "CANTFINDCOMPILER", getPath()); // NOI18N
            }  else {
                errormsg = NbBundle.getMessage(getClass(), "CANT_FIND_REMOTE_COMPILER", getPath(), getExecutionEnvironment().getDisplayName()); // NOI18N
            }
            CndNotifier.getDefault().notifyErrorLater(errormsg);
//            if (CndUtils.isStandalone()) {
//                System.err.println(errormsg);
//            } else {
//                DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
//            }
        }
        checkModel(res, getCallable());
        return res;
    }
    
    @Override
    protected MyCallable<CompilerDefinitions> getCallable(){
        return new MyCallable<CompilerDefinitions>() {

            @Override
            public CompilerDefinitions call(String p) {
                CompilerDefinitions tmp = new CompilerDefinitions();
                try {
                    getSystemIncludesAndDefines(getCompilerStderrCommand()+" "+p, false, tmp); // NOI18N
                    getSystemIncludesAndDefines(getCompilerStdoutCommand()+" "+p, true, tmp); // NOI18N
                    completePredefinedMacros(tmp);
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
                return tmp;
            }
        };
    }

    private boolean startsWithPath(String line) {
        line = line.trim();
        if( line.startsWith("/") ) {  // NOI18N
            return true;
        } else if ( line.length()>2 && Character.isLetter(line.charAt(0)) && line.charAt(1) == ':' ) {
            return true;
        }
        return false;
    }

    protected String cutIncludePrefix(String line) {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getRemoveIncludeOutputPrefix() != null) {
            String remove = compiler.getRemoveIncludeOutputPrefix();
            String lline = line.toLowerCase();
            if (lline.startsWith(getIncludeFilePathPrefix().toLowerCase())) {
                line = line.substring(getIncludeFilePathPrefix().length());
            } else {
                String wpath = WindowsSupport.getInstance().convertToShellPath(getIncludeFilePathPrefix().toLowerCase());
                if (wpath != null && lline.startsWith(wpath)) {
                    line = line.substring(wpath.length());
                } else if (lline.startsWith(remove)) {
                    line = line.substring(remove.length());
                }
            }
        }
        return line;
    }

   @Override
   protected void parseCompilerOutput(BufferedReader reader, CompilerDefinitions pair) {

       try {
           String line;
           boolean startIncludes = false;
           while ((line = reader.readLine()) != null) {
               //System.out.println(line);
               line = line.trim();
               if (line.contains("#include <...>")) { // NOI18N
                   startIncludes = true;
                   continue;
               }
               if (startIncludes) {
                   if (line.startsWith("End of search") || ! startsWithPath(line)) { // NOI18N
                       startIncludes = false;
                       continue;
                   }
                   if (line.length() > 2 && line.charAt(1) == ':') {
                       addUnique(pair.systemIncludeDirectoriesList, normalizePath(line));
                   } else {
                       String compilePath = getPath().replace('\\', '/'); // NOI18N
                       if ((Utilities.isWindows() || compilePath.length()>1 && compilePath.charAt(1) == ':') && getExecutionEnvironment().isLocal()) {
                           int i = compilePath.indexOf("/usr/bin/"); // NOI18N
                           if (i > 0) {
                               String prefix = compilePath.substring(0,i);
                               addUnique(pair.systemIncludeDirectoriesList, normalizePath(prefix + line));
                           } else {
                               i = compilePath.indexOf("/bin/"); // NOI18N
                                if (i > 0) {
                                    String prefix = compilePath.substring(0,i);
                                    line = line.replace('\\', '/'); // NOI18N
                                    if (line.startsWith("/usr/lib/")) { // NOI18N
                                        if (new File(normalizePath(prefix + line)).exists()) {
                                            addUnique(pair.systemIncludeDirectoriesList, normalizePath(prefix + line));
                                        }
                                        addUnique(pair.systemIncludeDirectoriesList, normalizePath(prefix + line.substring(4)));
                                    } else if (line.startsWith("/mingw/")) { // NOI18N
                                        addUnique(pair.systemIncludeDirectoriesList, normalizePath(prefix + line.substring(6)));
                                    } else {
                                        addUnique(pair.systemIncludeDirectoriesList, normalizePath(prefix + line));
                                    }
                                } else {
                                    // I do not know such compiler
                                    addUnique(pair.systemIncludeDirectoriesList, applyPathPrefix(line));
                                }
                           }
                       } else {
                           if (line.endsWith(" (framework directory)")) { // NOI18N
                               line = line.substring(0, line.lastIndexOf('(')).trim() + Driver.FRAMEWORK;
                           }
                           addUnique(pair.systemIncludeDirectoriesList, normalizePath(line));
                       }
                   }
                   continue;
               }
               if (line.startsWith("#define ")) { // NOI18N
                   String[] macro = CCCCompiler.getMacro(line.substring(8).trim());
                   if (CCCCompiler.isValidMacroName(macro[0])) {
                       String token;
                       if (macro[1] != null) {
                            token = macro[0] + "=" + macro[1]; // NOI18N
                       } else {
                           token = macro[0];
                       }
                       addUnique(pair.systemPreprocessorSymbolsList, token);
                   }
               } else {
                   parseUserMacros(line, pair.systemPreprocessorSymbolsList);
               }
           }
           reader.close();
       } catch (IOException ioe) {
           ErrorManager.getDefault().notify(ErrorManager.WARNING, ioe); // FIXUP
       }
   }
}
