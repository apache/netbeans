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

package org.netbeans.modules.java.hints.perf;

import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.TriggerPatterns;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint.Options;
import org.openide.util.NbBundle;

/**
 *
 * @author lahvac
 */
public class InitialCapacity {

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.InitialCapacity.collections", description = "#DESC_org.netbeans.modules.java.hints.perf.InitialCapacity.collections", category="performance", enabled=false, suppressWarnings="CollectionWithoutInitialCapacity", options=Options.QUERY)
    @TriggerPatterns({
        @TriggerPattern(value="new java.util.ArrayDeque()"),
        @TriggerPattern(value="new java.util.ArrayDeque<$T$>()"),
        @TriggerPattern(value="new java.util.ArrayList()"),
        @TriggerPattern(value="new java.util.ArrayList<$T$>()"),
        @TriggerPattern(value="new java.util.BitSet()"),
        @TriggerPattern(value="new java.util.BitSet<$T$>()"),
        @TriggerPattern(value="new java.util.concurrent.ConcurrentHashMap()"),
        @TriggerPattern(value="new java.util.concurrent.ConcurrentHashMap<$T$>()"),
        @TriggerPattern(value="new java.util.HashMap()"),
        @TriggerPattern(value="new java.util.HashMap<$T$>()"),
        @TriggerPattern(value="new java.util.HashSet()"),
        @TriggerPattern(value="new java.util.HashSet<$T$>()"),
        @TriggerPattern(value="new java.util.Hashtable()"),
        @TriggerPattern(value="new java.util.Hashtable<$T$>()"),
        @TriggerPattern(value="new java.util.IdentityHashMap()"),
        @TriggerPattern(value="new java.util.IdentityHashMap<$T$>()"),
        @TriggerPattern(value="new java.util.LinkedHashMap()"),
        @TriggerPattern(value="new java.util.LinkedHashMap<$T$>()"),
        @TriggerPattern(value="new java.util.LinkedHashSet()"),
        @TriggerPattern(value="new java.util.LinkedHashSet<$T$>()"),
        @TriggerPattern(value="new java.util.Vector()"),
        @TriggerPattern(value="new java.util.Vector<$T$>()")
    })
    public static ErrorDescription collections(HintContext ctx) {
        String displayName = NbBundle.getMessage(InitialCapacity.class, "ERR_InitialCapacity_collections");
        
        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.perf.InitialCapacity.stringBuilder", description = "#DESC_org.netbeans.modules.java.hints.perf.InitialCapacity.stringBuilder", category="performance", enabled=false, suppressWarnings="StringBufferWithoutInitialCapacity", options=Options.QUERY)
    @TriggerPatterns({
        @TriggerPattern(value="new java.lang.StringBuffer()"),
        @TriggerPattern(value="new java.lang.StringBuilder()")
    })
    public static ErrorDescription stringBuilder(HintContext ctx) {
        String displayName = NbBundle.getMessage(InitialCapacity.class, "ERR_InitialCapacity_stringBuilder");

        return ErrorDescriptionFactory.forName(ctx, ctx.getPath(), displayName);
    }
}
