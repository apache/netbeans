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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.php.dbgp.packets;

import java.util.Locale;

/**
 * @author ads
 *
 */
public class FeatureGetCommand extends DbgpCommand {
    public enum Feature {
        LANGUAGE_SUPPORTS_THREADS,
        LANGUAGE_NAME,
        LANGUAGE_VERSION,
        ENCODING,
        PROTOCOL_VERSION,
        SUPPORTS_ASYNC,
        DATA_ENCODING,
        BREAKPOINT_LANGUAGES,
        BREAKPOINT_TYPES,
        MULTIPLE_SESSIONS,
        MAX_CHILDREN,
        MAX_DATA,
        MAX_DEPTH,
        SUPPORTS_POSTMORTEM,
        SHOW_HIDDEN,
        NOTIFY_OK,
        /*
         * additional commands that could be supported
         */
        BREAK, // at the time of writing ( protocol version 2.0.0 ) this command is NOT supported
        EVAL, // at the time of writing ( protocol version 2.0.0 ) this command is supported
        EXPR, // at the time of writing ( protocol version 2.0.0 ) this command is NOT supported
        EXEC; // at the time of writing ( protocol version 2.0.0 ) this command is NOT supported

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.US);
        }

        public Feature forString(String str) {
            Feature[] features = Feature.values();
            for (Feature feature : features) {
                if (str.equals(feature.toString())) {
                    return feature;
                }
            }
            return null;
        }

    }
    static final String FEATURE_GET = "feature_get"; // NOI18N
    private static final String NAME_ARG = "-n "; // NOI18N
    private String myName;

    public FeatureGetCommand(String transactionId) {
        this(FEATURE_GET, transactionId);
    }

    protected FeatureGetCommand(String command, String transactionId) {
        super(command, transactionId);
    }

    public void setFeature(Feature feature) {
        myName = feature.toString();
    }

    public void setFeature(String name) {
        myName = name;
    }

    @Override
    protected String getArguments() {
        return NAME_ARG + myName;
    }

    @Override
    public boolean wantAcknowledgment() {
        return true;
    }

}
