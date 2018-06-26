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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.dbgp;

import java.util.List;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.openide.util.Pair;

/**
 * @author Radek Matous
 */
public class DebuggerOptions {
    private static final DebuggerOptions GLOBAL_INSTANCE = new DefaultGlobal();
    int port = -1;
    int maxData = -2;
    int maxChildren = -1;
    int maxStructureDepth = -1;
    Boolean debugForFirstPageOnly;
    String projectEncoding;
    List<Pair<String, String>> pathMapping;
    Pair<String, Integer> debugProxy;

    public static DebuggerOptions getGlobalInstance() {
        return GLOBAL_INSTANCE;
    }

    public List<Pair<String, String>> getPathMapping() {
        return pathMapping;
    }

    /**
     *
     * @return debug proxy <host, port> or <code>null</code> if not used
     */
    public Pair<String, Integer> getDebugProxy() {
        return debugProxy;
    }

    public int getPort() {
        return (port != -1) ? port :  getGlobalInstance().getPort();
    }

    public int getMaxData() {
        return (maxData != -2) ? maxData :  getGlobalInstance().getMaxData();
    }

    public int getMaxChildren() {
        return (maxChildren != -1) ? maxChildren : getGlobalInstance().getMaxChildren();
    }

    public int getMaxStructuresDepth() {
        return (maxStructureDepth != -1) ? maxStructureDepth : getGlobalInstance().getMaxStructuresDepth();
    }

    public boolean isDebugForFirstPageOnly() {
        return (debugForFirstPageOnly != null) ? debugForFirstPageOnly : getGlobalInstance().isDebugForFirstPageOnly();
    }

    public boolean showRequestedUrls() {
        return getGlobalInstance().showRequestedUrls();
    }

    public boolean showDebuggerConsole() {
        return getGlobalInstance().showDebuggerConsole();
    }
    public boolean isDebuggerStoppedAtTheFirstLine() {
        return getGlobalInstance().isDebuggerStoppedAtTheFirstLine();
    }

    public String getPhpInterpreter() {
        return getGlobalInstance().getPhpInterpreter();
    }

    public String getProjectEncoding() {
        return projectEncoding != null ? projectEncoding : "UTF-8"; //NOI18N
    }

    private static class DefaultGlobal extends DebuggerOptions {
        public DefaultGlobal() {
        }

        @Override
        public int getPort() {
            return PhpOptions.getInstance().getDebuggerPort();
        }

        @Override
        public int getMaxData() {
            return PhpOptions.getInstance().getDebuggerMaxDataLength();
        }

        @Override
        public int getMaxChildren() {
            return PhpOptions.getInstance().getDebuggerMaxChildren();
        }

        @Override
        public int getMaxStructuresDepth() {
            return PhpOptions.getInstance().getDebuggerMaxStructuresDepth();
        }

        @Override
        public boolean isDebugForFirstPageOnly() {
            return false;
        }

        @Override
        public boolean showRequestedUrls() {
            return PhpOptions.getInstance().isDebuggerShowRequestedUrls();
        }

        @Override
        public boolean showDebuggerConsole() {
            return PhpOptions.getInstance().isDebuggerShowDebuggerConsole();
        }

        @Override
        public boolean isDebuggerStoppedAtTheFirstLine() {
            return PhpOptions.getInstance().isDebuggerStoppedAtTheFirstLine();
        }

        @Override
        public String getPhpInterpreter() {
            return PhpOptions.getInstance().getPhpInterpreter();
        }
    }
}
