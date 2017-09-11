/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.settings.storage.api;

/**
 * Mixin interface to detect if a value is inherited (defaulted) or not.
 * The interface is to be implemented on Preferences objects (e.g. Mime Preferences),
 * which support some sort of fallback, inheritance or default. It allows
 * clients to determine whether a preference key is defined at the level represented
 * by the Preferences object, or whether the value produced by {@link java.util.prefs.Preferences#get}
 * originates in some form of default or inherited values.
 * <p/>
 * This interface is implemented on Editor settings Preferences objects 
 * stored in MimeLookup (can be obtained by <code>MimeLookup.getLookup(mime).lookup(Preferences.class)</code>).
 *
 * @since 1.38
 * @author sdedic
 */
public interface OverridePreferences {
    /**
     * Determines whether the value is defined locally.
     * If the value comes from an inherited or default set of values,
     * the method returns {@code false}.
     * 
     * @param key key to check
     * @return true, if the value is defined locally, false if inherited.
     */
    public boolean      isOverriden(String key);
}
