/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
