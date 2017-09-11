/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.webkit.debugging.spi;

import org.json.simple.JSONObject;

/**
 * WebKit command. Class automatically assigns unique ID to each command.
 */
public final class Command {
    
    private static int uniqueCommandID = 0;
    
    public static final String COMMAND_ID = "id";
    public static final String COMMAND_METHOD = "method";
    public static final String COMMAND_PARAMS = "params";
    public static final String COMMAND_RESULT = "result";
    
    private JSONObject command;

    public Command(String method) {
        this(method, null);
    }

    @SuppressWarnings("unchecked")    
    public Command(String method, JSONObject params) {
        command = new JSONObject();
        command.put(COMMAND_ID, createUniqueID());
        command.put(COMMAND_METHOD, method);
        if (params != null && params.size() > 0) {
            command.put(COMMAND_PARAMS, params);
        }
    }
    
    private static synchronized int createUniqueID() {
        return uniqueCommandID++;
    }
    
    public int getID() {
        Object o = command.get(COMMAND_ID);
        if (o == null) {
            return -1;
        }
        assert o instanceof Number;
        return ((Number)o).intValue();
    }

    public JSONObject getCommand() {
        return command;
    }

    @Override
    public String toString() {
        return command.toJSONString();
    }

}
