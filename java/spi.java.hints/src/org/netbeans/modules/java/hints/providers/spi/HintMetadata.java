/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.java.hints.providers.spi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
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
        private final Collection<String> suppressWarnings = new ArrayList<>();
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
                    setEnabled(false);
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
            Set<Options> result = EnumSet.noneOf(Options.class);

            for (Hint.Options opt : options) {
                result.add(valueOf(opt.name()));
            }

            return result;
        }
    }
}
