/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.source.parsing;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.junit.NbTestCase;


/**
 *
 * @author Tomas Zezula
 */
public class ProxyFileManagerTest extends NbTestCase {
    
    public ProxyFileManagerTest(@NonNull final String name) {
        super(name);
    }

    public void testRepeatableIterator() {
        final List<String> options = Arrays.asList(new String[] {
            "--patch-module",       //NOI18N
            "a=apatch.jar",         //NOI18N
            "--patch-module",       //NOI18N
            "b=bpatch.jar",         //NOI18N
        });
        final Map<String,List<String>> expected = new HashMap<>();
        expected.put("a",Collections.singletonList("apatch.jar"));  //NOI18N
        expected.put("b",Collections.singletonList("bpatch.jar"));  //NOI18N
        final PatchProcessor[] processors = new PatchProcessor[2];
        for (int i=0; i< processors.length; i++) {
            processors[i] = new PatchProcessor();
        }
        final ProxyFileManager.RepeatableIterator<String> it =
                ProxyFileManager.RepeatableIterator.create(options.iterator());
        for (int i=0; i< processors.length; i++) {
            final Map<String, List<String>> r = processors[i].apply(it);
            assertEquals(expected, r);
            it.reset();
        }
    }
    
    
    private static final class PatchProcessor implements Function<Iterator<String>,Map<String,List<String>>> {

        @Override
        public Map<String, List<String>> apply(Iterator<String> t) {
            final Map<String,List<String>> res = new HashMap<>();
            boolean inPatch = false;
            final Pattern p = Pattern.compile("(\\S+)=(\\S+)"); //NOI18N
            while (t.hasNext()) {
                String current = t.next();
                if (inPatch) {
                    final Matcher m = p.matcher(current);
                    if (m.matches() && m.groupCount() == 2) {
                        String module = m.group(1);
                        String[] path = m.group(2).split(":");  //NOI18N
                        res.put(module, Arrays.asList(path));
                    }
                }
                inPatch = "--patch-module".equals(current); //NOI18N
            }
            return res;
        }
        
    }
}
