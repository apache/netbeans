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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.providers.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.CustomizerProvider;
import org.netbeans.spi.java.hints.Hint;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class HintMetadata {

    public final String id;
    public final String displayName;
    public final String description;
    public final String category;
    public final boolean enabled;
    public final Hint.Kind kind;
    public final Severity severity;
    public final Collection<? extends String> suppressWarnings;
    public final CustomizerProvider customizer;
    public final boolean showInTaskList = false;
    public final Set<Options> options;
    public final SourceVersion sourceVersion;

    HintMetadata(String id, String displayName, String description, String category, boolean enabled, Hint.Kind kind, Severity severity, Collection<? extends String> suppressWarnings, CustomizerProvider customizer, Set<Options> options, SourceVersion sourceVersion) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
        this.enabled = enabled;
        this.kind = kind;
        this.severity = severity;
        this.suppressWarnings = suppressWarnings;
        this.customizer = customizer;
        this.options = options;
        this.sourceVersion = sourceVersion;
    }

    @Override
    public String toString() {
        return this.displayName;
    }

    private static String lookup(ResourceBundle bundle, String key, String def) {
        try {
            return bundle != null ? bundle.getString(key) : def;
        } catch (MissingResourceException mre) {
            Logger.getLogger(HintMetadata.class.getName()).log(Level.FINE, null, mre);
            return def;
        }
    }

    public static final class Builder {
        private final String id;
        private String displayName;
        private String description;
        private String category;
        private boolean enabled;
        private Hint.Kind kind;
        private Severity severity;
        private final Collection<String> suppressWarnings = new ArrayList<String>();
        private CustomizerProvider customizer;
        private final Set<Options> options = EnumSet.noneOf(Options.class);
        private SourceVersion sourceVersion;

        private Builder(String id) {
            this.id = id;
            this.displayName = "";
            this.description = "";
            this.category = "";
            this.enabled = true;
            this.kind = Hint.Kind.INSPECTION;
            this.severity = Severity.VERIFIER;
        }

        public static Builder create(String id) {
            return new Builder(id);
        }

        public Builder setDescription(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
            return this;
        }

        public Builder setBundle(ResourceBundle bundle) {
            return setBundle(bundle, null, null);
        }

        public Builder setBundle(ResourceBundle bundle, String fallbackDisplayName, String fallbackDescription) {
            if (fallbackDisplayName == null) fallbackDisplayName = "No Display Name";
            if (fallbackDescription == null) fallbackDescription = "No Description";
            
            this.displayName = lookup(bundle, "DN_" + id.replace('$', '.'), fallbackDisplayName);
            this.description = lookup(bundle, "DESC_" + id.replace('$', '.'), fallbackDescription);
            return this;
        }

        public Builder setBundle(String bundleForFQN) {
            ResourceBundle bundle;

            try {
                int lastDot = bundleForFQN.lastIndexOf('.');

                assert lastDot >= 0;

                bundle = NbBundle.getBundle(bundleForFQN.substring(0, lastDot + 1) + "Bundle");
            } catch (MissingResourceException mre) {
                Logger.getLogger(HintMetadata.class.getName()).log(Level.FINE, null, mre);
                bundle = null;
            }
            return setBundle(bundle);
        }

        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }

        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder setKind(Hint.Kind kind) {
            this.kind = kind;
            return this;
        }

        public Builder setSeverity(Severity severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder setSourceVersion(String version) {
            if (version == null || version.isEmpty()) {
                this.sourceVersion = SourceVersion.RELEASE_3;
            } else {
                if (version.startsWith("1.")) {
                    version = version.substring(2);
                }
                try {
                    this.sourceVersion = SourceVersion.valueOf("RELEASE_" + version);
                } catch (IllegalArgumentException ex) {
                    this.sourceVersion = SourceVersion.RELEASE_3;
                }
            }
            return this;
        }


        public Builder addSuppressWarnings(String... keys) {
            this.suppressWarnings.addAll(Arrays.asList(keys));
            return this;
        }

        public Builder setCustomizerProvider(CustomizerProvider customizer) {
            this.customizer = customizer;
            return this;
        }

        public Builder addOptions(Options... options) {
            this.options.addAll(Arrays.asList(options));
            return this;
        }

        public HintMetadata build() {
            return new HintMetadata(id, displayName, description, category, enabled, kind, severity, suppressWarnings, customizer, options, sourceVersion);
        }

    }

    public enum Options {
        NON_GUI,
        QUERY,
        NO_BATCH,
        HEAVY;

        public static Set<Options> fromHintOptions(Hint.Options... options) {
            Set<Options> result = new HashSet<Options>();

            for (Hint.Options opt : options) {
                result.add(valueOf(opt.name()));
            }

            return result;
        }
    }
}
