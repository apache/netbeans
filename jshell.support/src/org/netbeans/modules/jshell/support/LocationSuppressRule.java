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
package org.netbeans.modules.jshell.support;

import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.tools.Diagnostic;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.friendapi.OverrideErrorMessage;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.util.NbBundle;

import static org.netbeans.modules.jshell.support.Bundle.*;
/**
 * Suppresses erroneous locations. The javac gives location in its error messages, which
 * displays classname. In case of JShell snippets, those class names are 'hidden' and computer generated
 * This rule suppresses the class name part of the message.
 * 
 * @author sdedic
 */
@NbBundle.Messages({
    "# {0} - classname",
    "Pattern_Location=Location:\\p{Space}*'{0}'",
    "# {0} - location description",
    "Format_Location=Location: {0}",
})
public class LocationSuppressRule implements OverrideErrorMessage {
    private static final Set ALL_CODES = new HashSet<>(Arrays.asList("*"));
    private static final Pattern LOCATION_LINE = Pattern.compile("^\\p{Space}*location:\\p{Space}*(.+)", Pattern.MULTILINE);
    @Override
    public String createMessage(CompilationInfo info, Diagnostic d, int offset, TreePath treePath, ErrorRule.Data data) {
        String msg = d.getMessage(null);
        Matcher m = LOCATION_LINE.matcher(msg);
        if (!m.find()) {
            return null;
        }
        String location = m.group(1);
        int idx = location.indexOf("$JShell$"); // NOI18N
        if (idx == -1) {
            idx = location.indexOf("$JSHELL$"); // NOI18N
            if (idx == -1) {
                return null;
            }
        }
        String[] components = location.substring(idx).split("\\."); // NOI18N
        String last = components[components.length - 1];
        if (last.startsWith("$JShell$") || last.startsWith("$JSHELL$")) { // NOI18N
            // leave out the location at all
            return msg.substring(0, m.start(0));
        } else {
            return msg.substring(0, m.start(0)) + Bundle.Format_Location(last) + 
                    msg.substring(m.end(0));
        }
    }

    @Override
    public Set getCodes() {
        return ALL_CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data data) {
        return null;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public void cancel() {
    }
    
}
