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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.spi.toolchain;


import java.util.ArrayList;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolchainDescriptor;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetManagerImpl;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolchainManagerImpl;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.openide.util.NbBundle;

/**
 *
 */
public final class ToolchainScriptGenerator {
    private static final boolean TRACE = true;
    private final StringBuilder buf = new StringBuilder();
    private boolean isAutoDetected;
    
    private ToolchainScriptGenerator(){
    }

    public static String generateScript(String path, HostInfo host){
        ToolchainScriptGenerator generator = new ToolchainScriptGenerator();
        int platform;
        String platformName;
        switch(host.getOSFamily()) {
            case LINUX:
                platform = PlatformTypes.PLATFORM_LINUX;
                platformName = "PLATFORM_LINUX"; //NOI18N
                break;
            case MACOSX:
                platform = PlatformTypes.PLATFORM_MACOSX;
                platformName = "PLATFORM_MACOSX"; //NOI18N
                break;
            case SUNOS:
                if (host.getCpuFamily() == HostInfo.CpuFamily.SPARC) {
                    platform = PlatformTypes.PLATFORM_SOLARIS_SPARC;
                    platformName = "PLATFORM_SOLARIS_SPARC"; //NOI18N
                } else {
                    platform = PlatformTypes.PLATFORM_SOLARIS_INTEL;
                    platformName = "PLATFORM_SOLARIS_INTEL"; //NOI18N
                }
                break;
            case WINDOWS:
                platform = PlatformTypes.PLATFORM_WINDOWS;
                platformName = "PLATFORM_WINDOWS"; //NOI18N
                break;
            case UNKNOWN:
            case FREEBSD:
            default:
                platform = PlatformTypes.PLATFORM_NONE;
                platformName = "PLATFORM_NONE"; //NOI18N
                break;
        }
        generator.prefix(path, platform, platformName);
        generator.scanPaths(platform);
        return generator.buf.toString();
    }

    private void prefix(String path, int platform, String platformName){
        line("#!/bin/sh"); // NOI18N
        line("PLATFORM="+platform); // NOI18N
        line("PLATFORM_NAME="+platformName); // NOI18N
        lines(NbBundle.getMessage(ToolchainScriptGenerator.class, "DetectHostInfo")); // NOI18N
        if (path != null) {
            line("PATHSLIST=\""+path+"\""); // NOI18N
            isAutoDetected= false;
        } else {
            isAutoDetected= true;
            line("echo $PLATFORM_NAME"); // NOI18N
            if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                line("PATH=$PATH;C:/WINDOWS/System32;C:/WINDOWS;C:/WINDOWS/System32/WBem"); // NOI18N
            } else if (platform != PlatformTypes.PLATFORM_NONE) {
                line("PATH=$PATH:/bin:/usr/bin:/sbin:/usr/sbin"); // NOI18N
            } else {
                line("PATH=$PATH"); // NOI18N
            }
            ArrayList<String> dirlist = new ArrayList<String>();
            CompilerSetManagerImpl.appendDefaultLocations(platform, dirlist);
            for(String s : dirlist) {
                line("PATH=$PATH:"+s); // NOI18N
            }
            line("PATHSLIST=$PATH"); // NOI18N
        }
        if (platform == PlatformTypes.PLATFORM_WINDOWS) {
            line(" IFS=;"); // NOI18N
        } else {
            line(" IFS=:"); // NOI18N
        }
        line("foundFlavors=\";\""); // NOI18N
    }

    private void scanPaths(int platform){
        line("for f in $PATHSLIST; do"); // NOI18N
        line("  line="); // NOI18N
        line("  flavor="); // NOI18N
        line("  version="); // NOI18N
        line("  flags="); // NOI18N
        line("  echo $f | egrep -e \"^/\" >/dev/null"); // NOI18N
        line("  if [ \"$?\" != \"0\" ]; then"); // NOI18N
        line("    continue  # skip relative directories"); // NOI18N
        line("  fi"); // NOI18N
        line("  echo $f | egrep -e \"^/usr/ucb\" >/dev/null"); // NOI18N
        line("  if [ \"$?\" = \"0\" ]; then"); // NOI18N
        line("    continue  # skip /usr/ucb (IZ #142780)"); // NOI18N
        line("  fi"); // NOI18N
        scanPath(platform);
        line("done"); // NOI18N
    }
    
    private void scanPath(int platform){
        platformPath(platform);
    }
    
    private void platformPath(int platform){
        for (ToolchainDescriptor d : ToolchainManagerImpl.getImpl().getToolchains(platform)) {
            if (d.isAbstract() || (isAutoDetected && !d.isAutoDetected())) {
                continue;
            }
            if (d.getModuleID() != null) {
                continue;
            }
            CompilerDescriptor c = d.getC();
            if (c == null || c.getNames().length == 0) {
                continue;
            }
            line("status="); // NOI18N
            line("while [ ! -n \"$status\" ]; do"); // NOI18N
            if (c.getPathPattern() != null) {
                // todo windows use case insensitive regexp
                line("  echo $f | egrep -e \""+c.getPathPattern()+"\" >/dev/null"); // NOI18N
                line("  status=$?"); // NOI18N
                line("  if [ ! \"$status\" = \"0\" ]; then"); // NOI18N
                if (c.getExistFolder() == null) {
                    line("    break"); // NOI18N
                } else {
                    line("    if [ ! -d \"$f/"+c.getExistFolder()+"\" ]; then"); // NOI18N
                    line("      break"); // NOI18N
                    line("    fi"); // NOI18N
                }
                line("  fi"); // NOI18N
            }
            line("  file=\"$f/"+c.getNames()[0]+"\""); // NOI18N
            line("  if [ ! -x \"$file\" ]; then"); // NOI18N
            if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                line("    file=\"$f/"+c.getNames()[0]+".exe\""); // NOI18N
                line("    if [ ! -x \"$f/"+c.getNames()[0]+".exe\" ]; then"); // NOI18N
                line("      break"); // NOI18N
                line("    fi"); // NOI18N
            } else {
                line("    break"); // NOI18N
            }
            line("  fi"); // NOI18N
            if (c.getVersionFlags() != null && c.getVersionPattern() != null) {
                line("  if [ ! \"$flags\" = \"" + c.getVersionFlags() + "\" ]; then"); // NOI18N
                line("    version=`$file "+c.getVersionFlags()+" 2>&1`"); // NOI18N
                line("    flags="+c.getVersionFlags()); // NOI18N
                line("  fi"); // NOI18N
                line("  echo ${version} | egrep -e \""+c.getVersionPattern()+"\" >/dev/null"); // NOI18N
                line("  status=$?"); // NOI18N
                line("  if [ ! \"$status\" = \"0\" ]; then"); // NOI18N
                line("    break"); // NOI18N
                line("  fi"); // NOI18N
                line("  versionstring=`echo ${version} | egrep -e \""+c.getVersionPattern()+"\" | head -1`"); // NOI18N
            } else if (c.getFingerPrintFlags() != null && c.getFingerPrintPattern() != null) {
                line("  if [ ! \"$flags\" = \"" + c.getFingerPrintFlags() + "\" ]; then"); // NOI18N
                line("    version=`$file "+c.getFingerPrintFlags()+" 2>&1`"); // NOI18N
                line("    flags="+c.getFingerPrintFlags()); // NOI18N
                line("  fi"); // NOI18N
                line("  echo ${version} | egrep -e \""+c.getFingerPrintPattern()+"\" >/dev/null"); // NOI18N
                line("  status=$?"); // NOI18N
                line("  if [ ! \"$status\" = \"0\" ]; then"); // NOI18N
                line("    break"); // NOI18N
                line("  fi"); // NOI18N
                line("  versionstring=`echo ${version} | egrep -e \""+c.getFingerPrintPattern()+"\" | head -1`"); // NOI18N
            } else if (c.getVersionFlags() != null) {
                line("  if [ ! \"$flags\" = \"" + c.getVersionFlags() + "\" ]; then"); // NOI18N
                line("    version=`$file "+c.getVersionFlags()+" 2>&1`"); // NOI18N
                line("    flags="+c.getVersionFlags()); // NOI18N
                line("  fi"); // NOI18N
                line("  versionstring=`echo ${version} | head -1`"); // NOI18N
            } else {
                line("  versionstring="); // NOI18N
            }
            //Found compiler set
            line("  line=\""+d.getName()+";$f\""); // NOI18N
            line("  flavor=\""+d.getName()+";\""); // NOI18N
            addTool("c", d.getC().getNames(), platform); // NOI18N
            if (d.getCpp() != null) {
                addTool("cpp", d.getCpp().getNames(), platform); // NOI18N
            }
            if (d.getFortran() != null) {
                addTool("fortran", d.getFortran().getNames(), platform); // NOI18N
            }
            if (d.getAssembler() != null) {
                addTool("assembler", d.getAssembler().getNames(), platform); // NOI18N
            }
            if (d.getMake() != null) {
                addTool("make", d.getMake().getNames(), platform); // NOI18N
            }
            if (d.getDebugger() != null) {
                addTool("debugger", d.getDebugger().getNames(), platform); // NOI18N
            }
            if (d.getCMake() != null) {
                addTool("cmake", d.getCMake().getNames(), platform); // NOI18N
            }
            if (d.getQMake() != null) {
                addTool("qmake", d.getQMake().getNames(), platform); // NOI18N
            }
            line("line=\"${line};version=${versionstring}\""); // NOI18N
            line("  addNewToolChain"); // NOI18N
            line("  break"); // NOI18N
            line("done"); // NOI18N
        }
    }

    private void addTool(String kind, String[] names, int platform){
        if (names != null) {
            StringBuilder list = new StringBuilder();
            for(String name : names) {
                if (list.length()>0) {
                    if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                        list.append(';'); // NOI18N
                    } else {
                        list.append(':'); // NOI18N
                    }
                }
                list.append(name);
            }
            line("findCompiler \""+list.toString()+"\" \""+kind+"\""); // NOI18N
        }
    }

    private void lines(String lines){
        StringTokenizer st = new StringTokenizer(lines,"\n"); // NOI18N
        while(st.hasMoreTokens()) {
            line(st.nextToken());
        }
    }

    private int level = 0;
    private void line(String line){
        String l = line.trim();
        if (TRACE) {
            if (l.equals("fi") || l.equals("done") || l.equals("else") || l.equals("}")){ // NOI18N
                level--;
            }
            if (level >= 0) {
                for(int i = 0; i < level; i++){
                    buf.append(' ');
                    buf.append(' ');
                }
            }
            if (l.startsWith("while ") || l.startsWith("if ") || l.startsWith("for ") || l.equals("else") || l.endsWith("{")){ // NOI18N
                level++;
            }
        }
        buf.append(l).append('\n'); // NOI18N
    }
}
