/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.jackpot.spi;

import org.netbeans.modules.java.hints.providers.spi.HintDescriptionFactory;
import org.netbeans.spi.java.hints.HintContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.hints.providers.spi.HintDescription.Worker;
import org.netbeans.modules.java.hints.providers.spi.Trigger.PatternDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.openide.util.Lookup;

/**XXX: big hack?
 *
 * @author lahvac
 */
public abstract class PatternConvertor {

    protected abstract @CheckForNull Iterable<? extends HintDescription> parseString(@NonNull String code);

    public static @CheckForNull Iterable<? extends HintDescription> create(@NonNull String code) {
        Collection<String> patterns = new ArrayList<String>();
        
        //XXX:
        if (code.contains(";;")) {
            PatternConvertor c = Lookup.getDefault().lookup(PatternConvertor.class);

            if (c != null) {
                return c.parseString(code);
            }

            for (String s : code.split(";;")) {
                s = s.trim();
                if (s.isEmpty()) {
                    continue;
                }
                
                patterns.add(s);
            }
        } else {
            patterns.add(code);
        }

        Collection<HintDescription> result = new ArrayList<HintDescription>(patterns.size());

        for (String pattern : patterns) {
            PatternDescription pd = PatternDescription.create(pattern, Collections.<String, String>emptyMap());

            HintDescription desc = HintDescriptionFactory.create()
    //                                                     .setDisplayName("Pattern Matches")
                                                         .setTrigger(pd)
                                                         .setWorker(new WorkerImpl())
                                                         .produce();
            
            result.add(desc);
        }

        return result;
    }

    private static final class WorkerImpl implements Worker {

        public Collection<? extends ErrorDescription> createErrors(HintContext ctx) {
            ErrorDescription ed = ErrorDescriptionFactory.forTree(ctx, ctx.getPath(), "Found pattern occurrence");

            return Collections.singleton(ed);
        }
        
    }
}
