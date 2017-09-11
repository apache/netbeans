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
package org.netbeans.modules.nativeexecution.api.util;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.SwingUtilities;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.OS;

public final class MacroExpanderFactory {

    private static final Map<String, MacroExpander> expanderCache = new ConcurrentHashMap<>();

    public static enum ExpanderStyle {

        SUNSTUDIO_STYLE,
        DEFAULT_STYLE
    }

    private MacroExpanderFactory() {
    }

    public static MacroExpander getExpander(ExecutionEnvironment execEnv) {
        return getExpander(execEnv, ExpanderStyle.DEFAULT_STYLE, true);
    }

    public static MacroExpander getExpander(ExecutionEnvironment execEnv, boolean connectIfNeed) {
        return getExpander(execEnv, ExpanderStyle.DEFAULT_STYLE, connectIfNeed);
    }

    public static MacroExpander getExpander(
            ExecutionEnvironment execEnv, ExpanderStyle style) {        
        return getExpander(execEnv, style, true);
    }

    public static MacroExpander getExpander(
            ExecutionEnvironment execEnv, ExpanderStyle style, boolean connectIfNeed) {

        if (connectIfNeed && !HostInfoUtils.isHostInfoAvailable(execEnv)) {
            if (SwingUtilities.isEventDispatchThread()) {
                // otherwuse we'll hang forever in the attempt to connect
                throw new IllegalThreadStateException("Should never be called from AWT thread"); // NOI18N
            }
        }

        String key = ExecutionEnvironmentFactory.toUniqueID(execEnv) + '_' + style;
        MacroExpander result = expanderCache.get(key);
        
        if (result != null) {
            return result;
        }

        HostInfo hostInfo = null;        
        try {
            if (connectIfNeed || HostInfoUtils.isHostInfoAvailable(execEnv)) {
                hostInfo = HostInfoUtils.getHostInfo(execEnv);
            }            
        } catch (IOException ex) {
            // ideally, the method should throw IOException, 
            // but it's to dangerous to change signature right now
            ex.printStackTrace(System.err);
        } catch (CancellationException ex) {
            // ideally, the method should throw CancellationException, 
            // but it's to dangerous to change signature right now
        }

        result = new MacroExpanderImpl(hostInfo, style);
        if (hostInfo != null) {
            MacroExpander existing = expanderCache.putIfAbsent(key, result);
            if (existing != null) {
                result = existing;
            }
        }
        
        return result;
    }

    public interface MacroExpander {

        public String expandPredefinedMacros(String string) throws ParseException;

        public String expandMacros(
                String string,
                Map<String, String> envVariables) throws ParseException;
    }

    private static class MacroExpanderImpl implements MacroExpander {

        private static final int[][] ttable = new int[][]{
            {0, 0, 0, 1, 0, 0},
            {2, 3, 3, 10, 4, 3},
            {2, 2, 5, 6, 5, 5},
            {7, 7, 8, 8, 8, 9},
            {7, 3, 3, 3, 8, 8}
        };
        protected final Map<String, String> predefinedMacros =
                Collections.synchronizedMap(new HashMap<String, String>());
        protected final HostInfo hostInfo;

        public MacroExpanderImpl(final HostInfo hostInfo, final ExpanderStyle style) {
            this.hostInfo = hostInfo;
            setupPredefined(style);
        }

        private int getCharClass(char c) {
            if (c == '_' || (c >= 'A' && c <= 'Z') || c >= 'a' && c <= 'z') {
                return 0;
            }

            if (c >= '0' && c <= '9') {
                return 1;
            }

            if (c == '$') {
                return 3;
            }

            if (c == '{') {
                return 4;
            }

            if (c == '}') {
                return 5;
            }

            return 2;
        }

        private String valueOf(String macro, Map<String, String> map) {
            String result = map.get(macro);
            return result == null ? "${" + macro + "}" : result; // NOI18N
        }

        @Override
        public final String expandPredefinedMacros(
                final String string) throws ParseException {
            return expandMacros(string, predefinedMacros);
        }

        @Override
        public final String expandMacros(
                final String string,
                final Map<String, String> map) throws ParseException {

            if (string == null || string.length() == 0) {
                return string;
            }

            StringBuilder res = new StringBuilder();
            StringBuilder buf = new StringBuilder();

            int state = 0, pos = 0, mpos = -1;
            char[] chars = (string + (char) 0).toCharArray();
            char c;

            while (pos < chars.length) {
                c = chars[pos];

                switch (ttable[state][getCharClass(c)]) {
                    case 0:
                        if (c != 0) {
                            res.append(c);
                        }
                        break;
                    case 1:
                        mpos = pos;
                        buf.setLength(0);
                        state = 1;
                        break;
                    case 2:
                        buf.append(c);
                        state = 2;
                        break;
                    case 3:
                        res.append(string.substring(mpos, pos + (c == 0 ? 0 : 1)));
                        buf.setLength(0);
                        state = 0;
                        break;
                    case 4:
                        state = 4;
                        break;
                    case 5:
                        res.append(valueOf(buf.toString().trim(), map));
                        pos--;
                        buf.setLength(0);
                        state = 0;
                        break;
                    case 6:
                        res.append(valueOf(buf.toString().trim(), map));
                        mpos = pos;
                        buf.setLength(0);
                        state = 1;
                        break;
                    case 7:
                        buf.append(c);
                        state = 3;
                        break;
                    case 8:
                        throw new ParseException("Bad substitution", pos); // NOI18N
                    case 9:
                        res.append(valueOf(buf.toString().trim(), map));
                        buf.setLength(0);
                        state = 0;
                        break;
                    case 10:
                        res.append(string.substring(mpos, pos));
                        pos--;
                        buf.setLength(0);
                        state = 0;
                        break;
                }
                pos++;
            }

            return res.toString();
        }

        protected final void setupPredefined(ExpanderStyle style) {
            if (hostInfo == null) {
                return;
            }
            String soext;
            String osname;
            switch (hostInfo.getOSFamily()) {
                case WINDOWS:
                    soext = "dll"; // NOI18N
                    osname = "Windows"; // NOI18N
                    break;
                case MACOSX:
                    soext = "dylib"; // NOI18N
                    osname = "MacOSX"; // NOI18N
                    break;
                case SUNOS:
                    soext = "so"; // NOI18N
                    osname = "SunOS"; // NOI18N
                    break;
                case LINUX:
                    soext = "so"; // NOI18N
                    osname = "Linux"; // NOI18N
                    break;
                case FREEBSD:
                    soext = "so"; // NOI18N
                    osname = "FreeBSD"; // NOI18N
                    break;
                default:
                    osname = hostInfo.getOSFamily().name();
                    soext = "so"; // NOI18N
            }

            OS os = hostInfo.getOS();

            predefinedMacros.put("hostname", hostInfo.getHostname().toLowerCase()); // NOI18N
            predefinedMacros.put("soext", soext); // NOI18N
            predefinedMacros.put("osname", osname); // NOI18N
            predefinedMacros.put("isa", os.getBitness().toString()); // NOI18N
            predefinedMacros.put("_isa", os.getBitness() == HostInfo.Bitness._64 ? "_64" : ""); // NOI18N
            String platform = hostInfo.getCpuFamily().name().toLowerCase();

            if (style == ExpanderStyle.SUNSTUDIO_STYLE) {
                if ("x86".equals(platform)) { // NOI18N
                    platform = "intel"; // NOI18N
                }

                if (hostInfo.getOSFamily() == HostInfo.OSFamily.SUNOS) { // NOI18N
                    platform += "-S2"; // NOI18N
                } else {
                    platform += "-" + osname; // NOI18N
                }
            }

            predefinedMacros.put("platform", platform); // NOI18N
        }
    }
}
