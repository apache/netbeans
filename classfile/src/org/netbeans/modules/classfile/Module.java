/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.classfile;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The JDK 9 Module attribute.
 * @since 1.52
 * @author Tomas Zezula
 */
public final class Module {

    private final String name;
    private final int flags;
    private final String version;
    private final List<RequiresEntry> requires;
    private final List<ExportsEntry> exports;
    private final List<OpensEntry> opens;
    private final List<ClassName> uses;
    private final List<ProvidesEntry> provides;

    Module(final DataInputStream in, final ConstantPool cp) throws IOException {
        final CPEntry entry = cp.get(in.readUnsignedShort());
        if (entry.getTag() != ConstantPool.CONSTANT_Module) {
            throw new LegacyClassFile("Java 9 older than b148.");   //NOI18N
        }
        name = ((CPModuleInfo)entry).getName();
        flags = in.readUnsignedShort();
        int index = in.readUnsignedShort();
        version = index == 0 ?
                null :
                ((CPUTF8Info)cp.get(index)).getName();
        final int reqCnt = in.readUnsignedShort();
        final RequiresEntry[] req = new RequiresEntry[reqCnt];
        for (int i=0; i<reqCnt; i++) {
            req[i] = new RequiresEntry(in, cp);
        }
        requires = Collections.unmodifiableList(Arrays.asList(req));
        final int expCnt = in.readUnsignedShort();
        final ExportsEntry[] exp = new ExportsEntry[expCnt];
        for (int i=0; i<expCnt; i++) {
            exp[i] = new ExportsEntry(in, cp);
        }
        exports = Collections.unmodifiableList(Arrays.asList(exp));
        final int opnCnt = in.readUnsignedShort();
        final OpensEntry[] opn = new OpensEntry[opnCnt];
        for (int i=0; i<opnCnt; i++) {
            opn[i] = new OpensEntry(in, cp);
        }
        opens = Collections.unmodifiableList(Arrays.asList(opn));
        final int usesCnt = in.readUnsignedShort();
        final ClassName[] uss = new ClassName[usesCnt];
        for (int i=0; i<usesCnt; i++) {
            uss[i] = ((CPClassInfo)cp.get(in.readUnsignedShort())).getClassName();
        }
        uses = Collections.unmodifiableList(Arrays.asList(uss));
        final int provCnt = in.readUnsignedShort();
        final ProvidesEntry[] prov = new ProvidesEntry[provCnt];
        for (int i=0; i< provCnt; i++) {
            prov[i] = new ProvidesEntry(in, cp);
        }
        provides = Collections.unmodifiableList(Arrays.asList(prov));
    }

    /**
     * Returns module name.
     * @return the module name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns module flags.
     * @return the module flags
     */
    public int getFlags() {
        return flags;
    }

    /**
     * Returns module version.
     * @return the module version or null if not specified
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the required modules.
     * @return the list of {@link RequiresEntry}
     */
    public List<RequiresEntry> getRequiresEntries() {
        return requires;
    }

    /**
     * Returns the exported packages.
     * @return the list of {@link ExportsEntry}
     */
    public List<ExportsEntry> getExportsEntries() {
        return exports;
    }

    /**
     * Returns the opened packages.
     * @return the list of {@link OpensEntry}
     */
    public List<OpensEntry> getOpensEntries() {
        return opens;
    }

    /**
     * Returns the used services.
     * @return the list of services used by this module
     */
    public List<ClassName> getUses() {
        return uses;
    }

    /**
     * Returns the provided services.
     * @return the list of {@link ProvidesEntry}
     */
    public List<ProvidesEntry> getProvidesEntries() {
        return provides;
    }

    /**
     * Required module
     */
    public static final class RequiresEntry {

        private final String module;
        private final int flags;
        private final String version;

        RequiresEntry(final DataInputStream in, final ConstantPool cp) throws IOException {
            module = ((CPModuleInfo)cp.get(in.readUnsignedShort())).getName();
            flags = in.readUnsignedShort();
            int index = in.readUnsignedShort();
            version = index == 0 ?
                    null :
                    ((CPUTF8Info)cp.get(index)).getName();
        }

        /**
         * Returns the module name.
         * @return the module name
         */
        public String getModule() {
            return module;
        }

        /**
         * Returns require modifiers.
         * @return flags
         */
        public int getFlags() {
            return flags;
        }

        /**
         * Returns the version of the required module.
         * @return version of required module or null if not specified
         */
        public String getVersion() {
            return version;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("requires");
            if ((flags & Access.TRANSITIVE) != 0) {
                sb.append(" transitive");           //NOI18N
            }
            if ((flags & Access.STATIC_PHASE) != 0) {
                sb.append(" static");           //NOI18N
            }
            if ((flags & Access.SYNTHETIC) != 0) {
                sb.append(" synthetic");        //NOI18N
            }
            if ((flags & Access.MANDATED) != 0) {
                sb.append(" mandated");         //NOI18N
            }
            sb.append(' ')
                    .append(module);
            if (version != null) {
                sb.append("@")
                    .append(version);
            }
            return sb.toString();
        }
    }

    /**
     * Exported package.
     */
    public static final class ExportsEntry {
        private final String pkg;
        private final int flags;
        private final List<String> to;

        ExportsEntry(final DataInputStream in, final ConstantPool cp) throws IOException {
            pkg = ((CPPackageInfo) cp.get(in.readUnsignedShort())).getName();
            flags = in.readUnsignedShort();
            final int toCnt = in.readUnsignedShort();
            final String[] t = new String[toCnt];
            for (int i=0; i< toCnt; i++) {
                t[i] = ((CPModuleInfo) cp.get(in.readUnsignedShort())).getName();
            }
            to = Collections.unmodifiableList(Arrays.asList(t));
        }

        /**
         * Name of exported package.
         * @return the package name
         */
        public String getPackage() {
            return pkg;
        }

        /**
         * Returns export modifiers.
         * @return flags
         */
        public int getFlags() {
            return flags;
        }

        /**
         * Returns a list of modules to which the package is exported.
         * @return module list.
         */
        public List<String> getExportsTo() {
            return to;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("exports");
            if ((flags & Access.SYNTHETIC) != 0) {
                sb.append(" synthetic");        //NOI18N
            }
            if ((flags & Access.MANDATED) != 0) {
                sb.append(" mandated");         //NOI18N
            }
            sb.append(' ')
                .append(pkg);
            if (!to.isEmpty()) {
                sb.append(" to ");                //NOI18N
                boolean first = true;
                for (String m : to) {
                    if (!first) {
                        sb.append(", ");        //NOI18N
                    } else {
                        first = false;
                    }
                    sb.append(m);
                }
            }
            return sb.toString();
        }
    }

    /**
     * Opened package.
     */
    public static final class OpensEntry {
        private final String pkg;
        private final int flags;
        private final List<String> to;

        OpensEntry(final DataInputStream in, final ConstantPool cp) throws IOException {
            pkg = ((CPPackageInfo) cp.get(in.readUnsignedShort())).getName();
            flags = in.readUnsignedShort();
            final int toCnt = in.readUnsignedShort();
            final String[] t = new String[toCnt];
            for (int i=0; i< toCnt; i++) {
                t[i] = ((CPModuleInfo) cp.get(in.readUnsignedShort())).getName();
            }
            to = Collections.unmodifiableList(Arrays.asList(t));
        }

        /**
         * Name of opened package.
         * @return the package name
         */
        public String getPackage() {
            return pkg;
        }

        /**
         * Returns open modifiers.
         * @return flags
         */
        public int getFlags() {
            return flags;
        }

        /**
         * Returns a list of modules to which the package is opened.
         * @return module list.
         */
        public List<String> getOpensTo() {
            return to;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("opens");
            if ((flags & Access.SYNTHETIC) != 0) {
                sb.append(" synthetic");        //NOI18N
            }
            if ((flags & Access.MANDATED) != 0) {
                sb.append(" mandated");         //NOI18N
            }
            sb.append(' ')
                .append(pkg);
            if (!to.isEmpty()) {
                sb.append(" to ");                //NOI18N
                boolean first = true;
                for (String m : to) {
                    if (!first) {
                        sb.append(", ");        //NOI18N
                    } else {
                        first = false;
                    }
                    sb.append(m);
                }
            }
            return sb.toString();
        }
    }

    /**
     * Provided service.
     */
    public static final class ProvidesEntry {
        private final ClassName service;
        private final List<ClassName> impls;

        ProvidesEntry(final DataInputStream in, final ConstantPool cp) throws IOException {
            service = ((CPClassInfo)cp.get(in.readUnsignedShort())).getClassName();
            final int cnt = in.readUnsignedShort();
            final ClassName[] ims = new ClassName[cnt];
            for (int i=0; i< cnt; i++) {
                ims[i] = ((CPClassInfo)cp.get(in.readUnsignedShort())).getClassName();
            }
            impls = Collections.unmodifiableList(Arrays.asList(ims));
        }

        /**
         * Service type.
         * @return the service type
         */
        public ClassName getService() {
            return service;
        }

        /**
         * Service implementation.
         * @return the class implementing the service
         */
        public List<ClassName> getImplementations() {
            return impls;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder()
                .append("provides ")            //NOI18N
                .append(service);
            if (!impls.isEmpty()) {
                sb.append(" with ");            //NOI18N
                boolean first = true;
                for (ClassName m : impls) {
                    if (!first) {
                        sb.append(", ");        //NOI18N
                    } else {
                        first = false;
                    }
                    sb.append(m.getExternalName());
                }
            }
            return sb.toString();
        }
    }
}
