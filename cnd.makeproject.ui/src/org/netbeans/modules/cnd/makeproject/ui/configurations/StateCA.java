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

package org.netbeans.modules.cnd.makeproject.ui.configurations;

import org.netbeans.modules.cnd.api.project.CodeAssistance;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public enum StateCA {
    ParsedSource, ExtraParsedSource, ParsedOrphanHeader, IncludedHeader, ExcludedSource, ExcludedHeader, NotYetParsed;

    public static StateCA getState(Configuration configuration, Item item, ItemConfiguration itemConfiguration) {
        boolean source = itemConfiguration.isCompilerToolConfiguration();
        CodeAssistance.State caState = getCodeAssistanceState(item);
        switch (caState) {
            case ParsedSource:
                return itemConfiguration.getExcluded().getValue() ? ExtraParsedSource : ParsedSource;                    
            case ParsedOrphanHeader:
                return ParsedOrphanHeader;
            case ParsedIncludedHeader:
                return IncludedHeader;
            case NotParsed:
                // check if NativeFileItem would be parsed
                if (!item.isExcluded()) {
                    return NotYetParsed;
                } 
                return source ? ExcludedSource : ExcludedHeader;
            default:
                throw new IllegalStateException("unexpected CodeAssistance.State " + caState); // NOI18N
        }
    }
    
    private static CodeAssistance.State getCodeAssistanceState(Item item) {
        CodeAssistance CAProvider = Lookup.getDefault().lookup(CodeAssistance.class);
        if (CAProvider != null) {
            return CAProvider.getCodeAssistanceState(item);
        }
        return CodeAssistance.State.NotParsed;
    }

    @Override
    public String toString() {
        return NbBundle.getMessage(StateCA.class, "CodeAssistanceItem_" + name()); //NOI18N
    }
}
