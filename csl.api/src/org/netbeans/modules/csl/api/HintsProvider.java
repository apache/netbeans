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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.csl.api;

import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.Rule.AstRule;
import org.netbeans.modules.csl.api.Rule.ErrorRule;
import org.netbeans.modules.csl.api.Rule.SelectionRule;
import org.netbeans.modules.csl.api.Rule.UserConfigurableRule;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.spi.options.OptionsPanelController;


/**
 * Interface implemented by plugins that wish to provide quickfixes and hints.
 *
 * @author Tor Norbye
 */
public interface HintsProvider {

    /**
     * Compute hints applicable to the given compilation info and add to the given result list.
     */
    void computeHints(@NonNull HintsManager manager, @NonNull RuleContext context, @NonNull List<Hint> hints);
    
    /**
     * Compute any suggestions applicable to the given caret offset, and add to
     * the given suggestion list.
     */
    void computeSuggestions(@NonNull HintsManager manager, @NonNull RuleContext context, @NonNull List<Hint> suggestions, int caretOffset);

    /**
     * Compute any suggestions applicable to the given caret offset, and add to
     * the given suggestion list.
     */
    void computeSelectionHints(@NonNull HintsManager manager, @NonNull RuleContext context, @NonNull List<Hint> suggestions, int start, int end);
    
    /** 
     * Process the errors for the given compilation info, and add errors and
     * warning descriptions into the provided hint list. Return any errors
     * that were not added as error descriptions (e.g. had no applicable error rule)
     */
    void computeErrors(@NonNull HintsManager manager, @NonNull RuleContext context, @NonNull List<Hint> hints, @NonNull List<Error> unhandled);

    /**
     * Cancel in-progress processing of hints.
     */
    void cancel();
    
    /**
     * <p>Optional builtin Rules. Typically you don't use this; you register your rules in your filesystem
     * layer in the gsf-hints/mimetype1/mimetype2 folder, for example gsf-hints/text/x-ruby/.
     * Error hints should go in the "errors" folder, selection hints should go in the "selection" folder,
     * and all other hints should go in the "hints" folder (but note that you can create localized folders
     * and organize them under hints; these categories are shown in the hints options panel. 
     * Hints returned from this method will be placed in the "general" folder.
     * </p>
     * <p>
     * This method is primarily intended for rules that should be added dynamically, for example for
     * Rules that have a many different flavors yet a single implementation class (such as
     * JavaScript's StrictWarning rule which wraps a number of builtin parser warnings.)
     * 
     * @return A list of rules that are builtin, or null or an empty list when there are no builtins
     */
    @CheckForNull
    List<Rule> getBuiltinRules();
    
    /**
     * Create a RuleContext object specific to this HintsProvider. This lets implementations of
     * this interface created subclasses of the RuleContext that can be passed around to all
     * the executed rules.
     * @return A new instance of a RuleContext object
     */
    @NonNull
    RuleContext createRuleContext();

    /**
     * A HintsManager is implemented by the infrastructure and passed to you for many of
     * the HintsProvider callbacks. You can use this to look up hints, get an options panel,
     * etc.
     */
    public abstract class HintsManager {

        public static final HintsManager getManagerForMimeType (String mimeType) {
            Language language = LanguageRegistry.getInstance ().getLanguageByMimeType (mimeType);
            if (language != null) {
                // Force initialization if necessary
                if (language.getHintsProvider () != null) {
                    return language.getHintsManager ();
                }
            }
            return null;
        }

        public abstract boolean isEnabled(UserConfigurableRule rule);
        
        @NonNull
        public abstract Map<?,List<? extends ErrorRule>> getErrors();

        @NonNull
        public abstract Map<?,List<? extends AstRule>> getHints();

        @NonNull
        public abstract List<? extends SelectionRule> getSelectionHints();

        @NonNull
        public abstract Map<?,List<? extends AstRule>> getHints(boolean onLine, RuleContext context);

        // Is this redundant -- e.g. the getHints(boolean,CompilationInfo) method is used for this?
        @NonNull
        public abstract Map<?,List<? extends AstRule>> getSuggestions();

        /** Force the hints in the given context to be recomputed and the display updated */
        public abstract void refreshHints(@NonNull RuleContext context);
        
        /** Return an options controller suitable for editing hints preferences */
        public abstract OptionsPanelController getOptionsController();

        /** Return the Preferences associated with the given user configurable rule. */
        public abstract Preferences getPreferences(UserConfigurableRule rule);
    }
}
