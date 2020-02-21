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
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import org.netbeans.modules.cnd.apt.impl.support.SupportAPIAccessor;
import org.netbeans.modules.cnd.apt.support.api.PPMacroMap;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 *
 */
public class ClankMacroMap implements PPMacroMap {

    private long startCRC;
    private List<String> macros;

//    /** Creates a new instance of ClankSystemMacroMap */
    protected ClankMacroMap(long crc) {
        startCRC = crc;
        this.macros = Collections.emptyList();
    }

    public ClankMacroMap(List<String> sysMacros) {
        startCRC = calculateCRC(sysMacros);
        this.macros = Collections.unmodifiableList(sysMacros);
    }

    //@Override
    public long getCompilationUnitCRC() {
        return startCRC;
    }

    protected Collection<String> getMacros() {
        return this.macros;
    }

    protected static long calculateCRC(List<String> sysMacros) {
        Checksum checksum = new Adler32();
        for (String s : sysMacros) {
            checksum.update(s.getBytes(SupportAPIAccessor.INTERNAL_CHARSET), 0, s.length());
        }
        return checksum.getValue();
    }

    @Override
    public State getState() {
        return new StateImpl(this);
    }

    @Override
    public void setState(State state) {
        ((StateImpl)state).restoreTo(this);
    }

    public static class StateImpl implements State {

        private final List<String> macros;
        private long startCRC;
        protected final boolean cleaned;

        protected StateImpl(ClankMacroMap macroMap) {
            this.macros = macroMap.macros;
            this.startCRC = macroMap.startCRC;
            this.cleaned = false;
        }

        protected StateImpl(StateImpl other, boolean cleaned) {
            this.macros = other.macros;
            this.startCRC = other.startCRC;
            this.cleaned = cleaned;
        }

        ////////////////////////////////////////////////////////////////////////
        // persistence support
        public void write(RepositoryDataOutput output) throws IOException {
            // TODO
            output.writeLong(this.startCRC);
        }

        protected StateImpl(RepositoryDataInput input) throws IOException {
            // TODO
            this.startCRC = input.readLong();
            this.macros = Collections.emptyList();
            this.cleaned = true;
        }

        protected void restoreTo(ClankMacroMap macroMap) {
            if (!cleaned) {
                macroMap.macros = this.macros;
            }
            macroMap.startCRC = this.startCRC;
        }

        public State copyCleaned() {
            return cleaned ? this : new StateImpl(this, true);
        }

        @Override
        public String toString() {
            return APTUtils.macros2String(macros);
        }
    }

    @Override
    public String toString() {
        return APTUtils.macros2String(macros);
    }
}
