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
package org.netbeans.modules.cnd.apt.impl.support.clank;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 *
 */
public class ClankFileMacroMap extends ClankMacroMap {

    private ClankSystemMacroMap sysMacros;

    public ClankFileMacroMap() {
        super(0);
    }

    public ClankFileMacroMap(ClankSystemMacroMap sysMacroMap, List<String> userMacros) {
        super(userMacros);
        this.sysMacros = sysMacroMap;
    }

    @Override
    public long getCompilationUnitCRC() {
        if (sysMacros == null) {
            return super.getCompilationUnitCRC();
        }
        return super.getCompilationUnitCRC() ^ sysMacros.getCompilationUnitCRC();
    }

    @Override
    public State getState() {
        return new FileStateImpl(this);
    }

    @Override
    public void setState(State state) {
        ((FileStateImpl)state).restoreTo(this);
    }

    Collection<String> getSystemMacroDefinitions() {
        return (sysMacros == null) ? Collections.<String>emptyList() : this.sysMacros.getMacros();
    }

    Collection<String> getUserMacroDefinitions() {
        return this.getMacros();
    }

    public static final class FileStateImpl extends StateImpl {

        private final ClankSystemMacroMap sysMacros;

        private FileStateImpl(ClankFileMacroMap macroMap) {
            super(macroMap);
            this.sysMacros = macroMap.sysMacros;
        }

        private FileStateImpl(FileStateImpl other, boolean cleaned) {
            super(other, cleaned);
            this.sysMacros = other.sysMacros;
        }
        ////////////////////////////////////////////////////////////////////////
        // persistence support

        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            super.write(output);
        }

        public FileStateImpl(RepositoryDataInput input) throws IOException {
            super(input);
            // TODO
            this.sysMacros = null;
        }

        protected void restoreTo(ClankFileMacroMap macroMap) {
            super.restoreTo(macroMap);
            if (this.sysMacros != null) {
                macroMap.sysMacros = this.sysMacros;
            }
        }

        @Override
        public State copyCleaned() {
            return super.cleaned ? this : new FileStateImpl(this, true);
        }

        @Override
        public String toString() {
            StringBuilder retValue = new StringBuilder();
            retValue.append("FileState\n"); // NOI18N
            retValue.append("Parent\n"); // NOI18N
            retValue.append(super.toString());
            retValue.append("\nSystem MacroMap\n"); // NOI18N
            if (System.getProperty("cnd.apt.macro.trace") != null) {
                retValue.append(sysMacros);
            } else if (sysMacros == null) {
                retValue.append("null"); // NOI18N
            } else {
                retValue.append(System.identityHashCode(sysMacros));
            }
            return retValue.toString();
        }
    }

    @Override
    public String toString() {
        StringBuilder retValue = new StringBuilder();
        retValue.append("Own Map:\n"); // NOI18N
        retValue.append(super.toString());
        retValue.append("System Map:\n"); // NOI18N
        retValue.append(sysMacros);
        return retValue.toString();
    }
}
