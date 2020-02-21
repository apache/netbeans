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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public final class OutputPattern {

    public final Pattern pattern;
    public final Order order;
    public final String name;

    public OutputPattern(String name, Pattern pattern, Order order) {
        this.name = name;
        this.pattern = pattern;
        this.order = order;
    }

    public boolean match(String line) {
        Matcher m = pattern.matcher(line);
        return (m.matches() && m.groupCount() == 2);
    }

    public MatchResult process(String line) {
        Matcher m = pattern.matcher(line);
        if (m.matches() && m.groupCount() == 2) {
            try {
                int l = Integer.parseInt(m.group(order == Order.FILE_LINE ? 2 : 1));
                String file = m.group(order == Order.FILE_LINE ? 1 : 2);
                return new MatchResult(file, l);
            } catch (Exception ex) {
            }
        }
        return null;
    }

    public static class MatchResult {

        public final String filePath;
        public final int line;

        public MatchResult(String filePath, int line) {
            this.filePath = filePath;
            this.line = line;
        }
    }

    public static enum Order {

        FILE_LINE,
        LINE_FILE
    }
}
