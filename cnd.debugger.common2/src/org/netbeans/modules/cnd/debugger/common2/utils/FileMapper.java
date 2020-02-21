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

package org.netbeans.modules.cnd.debugger.common2.utils;

import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.openide.util.Utilities;

/**
 * Utility for mapping file pathnames from one domain to another.
 * 
 * The 'engine' domain is how things like compilers and debuggers represent
 * files. These could be remote engines using a unix notation or cygwin tools
 * which also use unix notation.
 * 
 * The 'world' domain is the local systems filesystem like Windows accessing
 * remote files via samba or some form of PC-NFS.
 *
 * NOTE! FileMapper doesn't do remote pathmapping. That is done by 
 * NativeDebugger.remoteToLocal() and localToRemote().
 */

public abstract class FileMapper {
    public enum Type {NULL, CYGWIN, MSYS}

    public static FileMapper getDefault() {
	if (Utilities.isWindows())
	    return CygwinFileMapper.INSTANCE;
	else
	    return NullFileMapper.INSTANCE;
    }

    public static FileMapper getByType(Type kind) {
	switch (kind) {
	    case NULL:	return NullFileMapper.INSTANCE;
	    case CYGWIN:return CygwinFileMapper.INSTANCE;
            case MSYS:  return MSysFileMapper.INSTANCE;
	}
        return NullFileMapper.INSTANCE;
    }

    public abstract String worldToEngine(String path);
    public abstract String engineToWorld(String path);

    /**
     * Disallow direct construction.
     */
    private FileMapper() {
    }

    private static class NullFileMapper extends FileMapper {
        private static NullFileMapper INSTANCE = new NullFileMapper();

        @Override
	public String worldToEngine(String path) {
	    return path;
	} 

        @Override
	public String engineToWorld(String path) {
	    return path;
	}
    }

    /**
     * World is of the form
     *	C:\Documents and Settings\USER\Projects
     *  C:\cygwin\bin
     * Engine is of the form
     *  /cygdrive/c/Documents and Settings/USER/Projects
     *  /bin
     */

    private static class CygwinFileMapper extends FileMapper {
        private static CygwinFileMapper INSTANCE = new CygwinFileMapper();
        
        @Override
        public String engineToWorld(String path) {
            if (path == null) {
                return null;
            }
            String res = WindowsSupport.getInstance().convertFromCygwinPath(path);
            if (res != null) {
                return res;
            } else {
                return path;
            }
        }

        @Override
        public String worldToEngine(String path) {
            if (path == null) {
                return null;
            }
            String res = WindowsSupport.getInstance().convertToCygwinPath(path);
            if (res != null) {
                return res;
            } else {
                return path;
            }
        }

    }

    private static class CygwinFileMapper_old extends FileMapper {

	private static final String prefix = "/cygdrive/"; // NOI18N
	private static final int prefixLen = prefix.length();

	/**
	 * c:\x\y\z -> /cygdrive/c/x/y/z
	 */
        @Override
	public String worldToEngine(String path) {
	    String newPath = path;
	    if (path.charAt(1) == ':') {
		char disk = path.charAt(0);
		String rest = path.substring(2);
		newPath = prefix + disk + separatorToUnix(rest);
	    }
	    System.out.println("CygwinFileMapper.worldToEngine():\n" + // NOI18N
		"\tFrom: " + path + "\n" + // NOI18N
		"\t  To: " + newPath + "\n"); // NOI18N
	    return newPath;
	} 

	/**
	 * /cygdrive/c/x/y/z -> c:\x\y\z
	 */

        @Override
	public String engineToWorld(String path) {
	    String newPath = path;
	    if (path.startsWith(prefix)) {
		char disk = path.charAt(10);
		newPath = "" + disk + ":" + path.substring(prefixLen+1); // NOI18N
		newPath = separatorToWindows(newPath);
	    }
	    System.out.println("CygwinFileMapper.engineToWorld():\n" + // NOI18N
		"\tFrom: " + path + "\n" + // NOI18N
		"\t  To: " + newPath + "\n"); // NOI18N
	    return newPath;
	}

	private static String separatorToWindows(String s) {
	    StringBuilder sb = new StringBuilder();
	    for (int sx = 0; sx < s.length(); sx++) {
		char c = s.charAt(sx);
		switch (c) {
		    case '/':
			sb.append('\\');
			break;
		    default:
			sb.append(c);
			break;
		}
	    }
	    return sb.toString();
	}

	private static String separatorToUnix(String s) {
	    StringBuilder sb = new StringBuilder();
	    for (int sx = 0; sx < s.length(); sx++) {
		char c = s.charAt(sx);
		switch (c) {
		    case '\\':
			sb.append('/');
			break;
		    default:
			sb.append(c);
			break;
		}
	    }
	    return sb.toString();
	}
    }

    private static class MSysFileMapper extends FileMapper {
        private static MSysFileMapper INSTANCE = new MSysFileMapper();
        
        @Override
        public String engineToWorld(String path) {
            if (path == null) {
                return null;
            }
            String res = WindowsSupport.getInstance().convertFromMSysPath(path);
            if (res != null) {
                return res;
            } else {
                return path;
            }
        }

        @Override
        public String worldToEngine(String path) {
            // See IZ 205418, MinGW gdb does not understand this path form
            return path;
        }
    }
}
