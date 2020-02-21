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
package org.netbeans.modules.dlight.sendto.output;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 */
public final class OutputPatterns {

    private static final String NODE_NAME = "outputPatterns"; // NOI18N
    private static final List<OutputPattern> patterns = new ArrayList<OutputPattern>();
    private static boolean initialized = false;

    public static List<OutputPattern> getPatterns() {
        ArrayList<OutputPattern> result;
        synchronized (patterns) {
            if (!initialized) {
                List<OutputPattern> model = loadFromProperties(NODE_NAME);
                if (model == null) {
                    try {
                        model = loadFromStream(OutputPatterns.class.getClassLoader().getResourceAsStream("org/netbeans/modules/dlight/sendto/resources/initialOutputPatterns")); // NOI18N
                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                patterns.addAll(model);
                try {
                    storeToProperties(NODE_NAME, model);
                } catch (BackingStoreException ex) {
                    Exceptions.printStackTrace(ex);
                }
                initialized = true;
            }
            result = new ArrayList<OutputPattern>(patterns);
        }
        return Collections.unmodifiableList(result);
    }

    /* package-visible */
    static List<OutputPattern> loadFromStream(final InputStream in) throws ParseException {
        final List<OutputPattern> newModel = new ArrayList<OutputPattern>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(in));
            String s;
            while ((s = br.readLine()) != null) {
                OutputPattern ptrn = parseLine(s.trim());
                if (ptrn != null) {
                    newModel.add(ptrn);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
            }
        }
        return newModel;
    }

    /* package-visible */
    static void storeToFile(FileObject afile, List<OutputPattern> model) {
        PrintWriter w = null;
        try {
            w = new PrintWriter(afile.getOutputStream());
            for (OutputPattern pattern : model) {
                w.append(pattern.name).append('=').
                        append(pattern.pattern.pattern()).
                        append('#').append(pattern.order.name()).append('\n');
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (w != null) {
                w.close();
            }
        }
    }

    private static List<OutputPattern> loadFromProperties(final String propertiesNode) {
        try {
            Preferences prefs = NbPreferences.forModule(OutputPatterns.class);
            if (!prefs.nodeExists(propertiesNode)) {
                return null;
            }
            prefs = prefs.node(propertiesNode);
            final List<OutputPattern> newModel = new ArrayList<OutputPattern>();
            for (String name : prefs.keys()) {
                OutputPattern pattern = parseLine(name + "=" + prefs.get(name, "")); // NOI18N
                if (pattern != null) {
                    newModel.add(pattern);
                }
            }
            return newModel;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private static void storeToProperties(String propertiesNode, List<OutputPattern> model) throws BackingStoreException {
        Preferences prefs = NbPreferences.forModule(OutputPatterns.class);
        prefs = prefs.node(propertiesNode);
        prefs.clear();
        for (OutputPattern pattern : model) {
            prefs.put(pattern.name, pattern.pattern.pattern() + '#' + pattern.order.name());
        }
        prefs.flush();
    }

    private static OutputPattern parseLine(String line) throws ParseException {
        if (line.isEmpty() || line.startsWith("#")) { // NOI18N
            return null;
        }
        int idx1 = line.lastIndexOf('=');
        int idx2 = line.lastIndexOf('#');
        if (idx1 < 0 || idx2 < 0) {
            throw new ParseException(line, 0);
        }
        try {
            String name = line.substring(0, idx1);
            Pattern pattern = Pattern.compile(line.substring(idx1 + 1, idx2));
            OutputPattern.Order order = OutputPattern.Order.valueOf(line.substring(idx2 + 1));
            return new OutputPattern(name, pattern, order);
        } catch (IllegalArgumentException ex) {
            throw new ParseException(line, 0);
        }
    }

    static void setPatterns(List<OutputPattern> newPatterns) {
        synchronized (patterns) {
            patterns.clear();
            patterns.addAll(newPatterns);
        }
        try {
            storeToProperties(NODE_NAME, newPatterns);
        } catch (BackingStoreException ex) {
        }
    }
}
