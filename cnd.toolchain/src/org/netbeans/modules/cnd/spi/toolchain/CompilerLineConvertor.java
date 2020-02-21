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

import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.extexecution.print.ConvertedLine;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ScannerDescriptor;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerFlavorImpl;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider.ErrorParser;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider.OutputListenerRegistry;
import org.netbeans.modules.cnd.spi.toolchain.ErrorParserProvider.Result;
import org.netbeans.modules.cnd.toolchain.execution.CompileErrorScanner;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.CpuFamily;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.filesystems.FileObject;
import org.openide.windows.InputOutput;


/**
 *
 */
public final class CompilerLineConvertor implements LineConvertor, ChangeListener {

    private final List<ErrorParser> parsers = new ArrayList<ErrorParser>();
    private final OutputListenerRegistry registry;
    private static final Logger LOG = Logger.getLogger(CompilerLineConvertor.class.getName());

    public CompilerLineConvertor(Project project, CompilerSet set, ExecutionEnvironment execEnv, FileObject relativeTo, InputOutput io) {
        registry = new OutputListenerRegistry(project, io);
        List<CompilerFlavor> flavors = getCompilerSet(set, execEnv);
        for(CompilerFlavor flavor : flavors) {
            try {
                ErrorParser parser = ErrorParserProvider.getDefault().getErorParser(project, flavor, execEnv, relativeTo);
                if (parser != null) {
                    parsers.add(parser);
                    parser.setOutputListenerRegistry(registry);
                }
            } catch (Throwable ex) {
                LOG.log(Level.SEVERE, "Cannot initialize error scanner for "+flavor.getToolchainDescriptor().getName(), ex);
            }
        }
        try {
            List<ErrorParser> universalErorParsers = ErrorParserProvider.getUniversalErorParsers(project, execEnv, relativeTo);
            for (ErrorParser parser : universalErorParsers) {
                parser.setOutputListenerRegistry(registry);
            }
            parsers.addAll(universalErorParsers);
        } catch (Throwable ex) {
            LOG.log(Level.SEVERE, "Cannot initialize universal error scanner", ex);
        }
    }

    @Override
    public List<ConvertedLine> convert(String line) {
        try {
            return handleLine(line);
        } catch (Throwable ex) {
            LOG.log(Level.SEVERE, "Error during handling line "+line, ex);
        }
        return null;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        CompileErrorScanner.attach(registry, registry.getProject());
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                OutputListenerProvider.getInstance().attach(registry);
            }
        });
    }

    private static final int LENGTH_TRESHOLD = 2048;

    private List<ConvertedLine> handleLine(String line) {
        if (line.length() < LENGTH_TRESHOLD) {
            // We can ignore strings which can't be compiler messages
            // (their's length is capped by max(filename) + max(error desc)).
            // See IZ#124796 for details about perf issues with very long lines.
            for (ErrorParser parser : parsers) {
               Result res = parser.handleLine(line);
               if (res != null && res.result()) {
                   return res.converted();
                }
            }
        }
        return null;
    }

    private List<CompilerFlavor> getCompilerSet(CompilerSet set, ExecutionEnvironment execEnv) {
	int platform = PlatformTypes.getDefaultPlatform();
	try {
	    HostInfo hostInfo = HostInfoUtils.getHostInfo(execEnv);
	    switch(hostInfo.getOSFamily()){
		case SUNOS:
		    if (hostInfo.getCpuFamily() == CpuFamily.SPARC){
			platform = PlatformTypes.PLATFORM_SOLARIS_SPARC;
		    } else {
			platform = PlatformTypes.PLATFORM_SOLARIS_INTEL;
		    }
		    break;
		case WINDOWS:
		    platform = PlatformTypes.PLATFORM_WINDOWS;
		    break;
		case LINUX:
		    platform = PlatformTypes.PLATFORM_LINUX;
		    break;
		case MACOSX:
		    platform = PlatformTypes.PLATFORM_MACOSX;
		    break;
		case UNKNOWN:
		case FREEBSD:
		default:
		    platform = PlatformTypes.PLATFORM_GENERIC;
		    break;
	    }
	} catch (IOException ex) {
	    //Exceptions.printStackTrace(ex);
	} catch (CancellationException ex) {
	    //Exceptions.printStackTrace(ex);
	}
	List<CompilerFlavor> flavors = new ArrayList<CompilerFlavor>();
	flavors.add(set.getCompilerFlavor());
	for(CompilerFlavor flavor : CompilerFlavorImpl.getFlavors(platform)) {
	    if (!flavors.contains(flavor)){
		boolean found = false;
		for(CompilerFlavor f : flavors) {
		    if (isScannerEquals(f, flavor)) {
			found = true;
			break;
		    }
		}
		if (!found) {
		    flavors.add(flavor);
		}
	    }
	}
        return flavors;
    }

    private boolean isScannerEquals(CompilerFlavor flavor1, CompilerFlavor flavor2) {
	ScannerDescriptor scanner1 = flavor1.getToolchainDescriptor().getScanner();
	ScannerDescriptor scanner2 = flavor2.getToolchainDescriptor().getScanner();
	if (scanner1.getPatterns().size() != scanner2.getPatterns().size()) {
	    return false;
	}
	for(int i = 0; i < scanner1.getPatterns().size(); i++){
	    if (!scanner1.getPatterns().get(i).getPattern().equals(scanner2.getPatterns().get(i).getPattern())){
		return false;
	    }
	}
	if (!isEquals(scanner1.getEnterDirectoryPattern(), scanner2.getEnterDirectoryPattern())) {
	    return false;
	}
	if (!isEquals(scanner1.getLeaveDirectoryPattern(), scanner2.getLeaveDirectoryPattern())) {
	    return false;
	}
	if (!isEquals(scanner1.getChangeDirectoryPattern(), scanner2.getChangeDirectoryPattern())) {
	    return false;
	}
	if (!isEquals(scanner1.getMakeAllInDirectoryPattern(), scanner2.getMakeAllInDirectoryPattern())) {
	    return false;
	}
        if (scanner1.getStackHeaderPattern().size() != scanner2.getStackHeaderPattern().size()) {
            return false;
        }
	for(int i = 0; i < scanner1.getStackHeaderPattern().size(); i++){
	    if (!scanner1.getStackHeaderPattern().get(i).equals(scanner2.getStackHeaderPattern().get(i))){
		return false;
	    }
	}
        if (scanner1.getStackNextPattern().size() != scanner2.getStackNextPattern().size()) {
            return false;
        }
	for(int i = 0; i < scanner1.getStackNextPattern().size(); i++){
	    if (!scanner1.getStackNextPattern().get(i).equals(scanner2.getStackNextPattern().get(i))){
		return false;
	    }
	}
	if (scanner1.getFilterOutPatterns().size() != scanner2.getFilterOutPatterns().size()) {
	    return false;
	}
	for(int i = 0; i < scanner1.getFilterOutPatterns().size(); i++){
	    if (!scanner1.getFilterOutPatterns().get(i).equals(scanner2.getFilterOutPatterns().get(i))){
		return false;
	    }
	}
	return true;
    }

    private boolean isEquals(String s1, String s2) {
	if (s1 == null) {
	    return s2 == null;
	}
	return s1.equals(s2);
    }
}
