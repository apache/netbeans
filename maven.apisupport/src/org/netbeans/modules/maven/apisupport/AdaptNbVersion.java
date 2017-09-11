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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.apisupport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

/**
 *  will try to convert the maven version number to a Netbeans friendly version number.
 * @author Milos Kleint
 *
 */
public class AdaptNbVersion {
    
    public static final String TYPE_SPECIFICATION = "spec"; //NOI18N
    public static final String TYPE_IMPLEMENTATION = "impl"; //NOI18N
    
    private static final String SNAPSHOT = "SNAPSHOT"; //NOI18N
    
    public static String adaptVersion(String version, Object type) {
        StringTokenizer tok = new StringTokenizer(version,"."); //NOI18N
        if (SNAPSHOT.equals(version) && TYPE_IMPLEMENTATION.equals(type)) {
            return "0.0.0." + generateSnapshotValue(); //NOI18N
        }
        StringBuffer toReturn = new StringBuffer();
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            if (TYPE_IMPLEMENTATION.equals(type)) {
                int snapshotIndex = token.indexOf(SNAPSHOT);
                if (snapshotIndex > 0) {
                    String repl = token.substring(0, snapshotIndex) + generateSnapshotValue();
                    if (token.length() > snapshotIndex + SNAPSHOT.length()) {
                        repl = token.substring(snapshotIndex + SNAPSHOT.length());
                    }
                    token = repl;
                }
            }
            if (TYPE_SPECIFICATION.equals(type)) {
                // strip the trailing -RC1, -BETA5, -SNAPSHOT
                if (token.indexOf('-') > 0) {
                    token = token.substring(0, token.indexOf('-')); //NOI18N
                }
                else if (token.indexOf('_') > 0) { //NOI18N
                    token = token.substring(0, token.indexOf('_')); //NOI18N
                }
                try {
                    Integer intValue = Integer.valueOf(token);
                    token = intValue.toString();
                } catch (NumberFormatException exc) {
                    // ignore, will just not be added to the
                    token = ""; //NOI18N
                }
            }
            if (token.length() > 0) {
                if (toReturn.length() != 0) {
                    toReturn.append("."); //NOI18N
                }
                toReturn.append(token);
            }
            
        }
        if (toReturn.length() == 0) {
            toReturn.append("0.0.0"); //NOI18N
        }
        return toReturn.toString();
    }
    
    private static String generateSnapshotValue() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date()); //NOI18N
    }
    
}
