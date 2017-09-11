/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
 * A cache of artefacts in the target VM.
 *
 * @author martin
 */
public class VMCache {

    private static final int MAX_ENCLOSING_TYPES = 100;

    private final JPDADebuggerImpl debugger;
    private final Map<String, ReferenceType> cachedClasses = new HashMap<>();
    private final Map<CEncl, ReferenceType> enclosingTypes = new LinkedHashMap<CEncl, ReferenceType>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<CEncl, ReferenceType> eldest) {
            return size() > MAX_ENCLOSING_TYPES;
        }
    };

    public VMCache(JPDADebuggerImpl debugger) {
        this.debugger = debugger;
        debugger.addPropertyChangeListener(JPDADebugger.PROP_CLASSES_FIXED,
                                           event -> reset());
    }

    private void reset() {
        synchronized (cachedClasses) {
            cachedClasses.clear();
        }
        synchronized (enclosingTypes) {
            enclosingTypes.clear();
        }
    }

    /**
     * Get a cached version of a basic Java class, which is used often.
     * Use for Java platform classes only.
     * @param name the class name
     * @return the reference type or <code>null</code>
     */
    ReferenceType getClass(String name) {
        ReferenceType rt;
        synchronized (cachedClasses) {
            rt = cachedClasses.get(name);
            if (rt == null) {
                rt = loadClass(name);
                if (rt != null) {
                    cachedClasses.put(name, rt);
                }
            }
        }
        return rt;
    }
    
    private ReferenceType loadClass(String name) {
        VirtualMachine vm = debugger.getVirtualMachine();
        if (vm == null) {
            return null;
        }
        List<ReferenceType> stringClasses = vm.classesByName(name);
        if (stringClasses.isEmpty()) {
            return null;
        }
        return stringClasses.get(0);
    }

    /**
     * Get a cached type of name 'name' that encloses 'type'.
     * @return The cached type, or <code>null</code>.
     */
    ReferenceType getEnclosingType(ReferenceType type, String name) {
        CEncl classEnclosing = new CEncl(type, name);
        ReferenceType enclosingType;
        synchronized (enclosingTypes) {
            enclosingType = enclosingTypes.get(classEnclosing);
        }
        return enclosingType;
    }

    /**
     * Set an enclosing type of name 'name' that encloses 'type'.
     */
    void setEnclosingType(ReferenceType type, String name, ReferenceType enclosingType) {
        CEncl classEnclosing = new CEncl(type, name);
        synchronized (enclosingTypes) {
            enclosingTypes.put(classEnclosing, enclosingType);
        }
    }

    private static class CEncl {

        final ReferenceType type;
        final String enclName;

        CEncl(ReferenceType type, String enclName) {
            this.type = type;
            this.enclName = enclName;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.type, this.enclName);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CEncl other = (CEncl) obj;
            if (!Objects.equals(this.enclName, other.enclName)) {
                return false;
            }
            if (!Objects.equals(this.type, other.type)) {
                return false;
            }
            return true;
        }

    }
}
