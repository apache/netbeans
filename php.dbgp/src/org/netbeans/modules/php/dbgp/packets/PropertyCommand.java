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

/**
 * @author ads
 *
 */
public abstract class PropertyCommand extends DbgpCommand {
    private static final String NAME_ARG = "-n "; // NOI18N
    private static final String MAX_SIZE_ARG = "-m "; // NOI18N
    private static final String CONTEXT_ARG = "-c "; // NOI18N
    private static final String DEPTH_ARG = "-d "; // NOI18N
    private static final String PAGE_ARG = "-p "; // NOI18N
    private int myPage;
    private int myContext;
    private int myDepth;
    private String myName;
    private int mySize;

    PropertyCommand(String command, String transactionId) {
        super(command, transactionId);
        myDepth = -1;
        myContext = -1;
        mySize = -1;
        myPage = -1;
    }

    public void setStackDepth(int depth) {
        myDepth = depth;
    }

    public void setContext(int id) {
        myContext = id;
    }

    public void setName(String name) {
        myName = name;
    }

    public void setMaxDataSize(int size) {
        mySize = size;
    }

    public void setDataPage(int page) {
        myPage = page;
    }

    protected String getName() {
        return myName;
    }

    protected int getContext() {
        return myContext;
    }

    @Override
    protected String getArguments() {
        StringBuilder builder = new StringBuilder(NAME_ARG);
        builder.append(myName);
        setDepth(builder);
        setContext(builder);
        setMaxSize(builder);
        setPage(builder);
        return builder.toString();
    }

    private void setPage(StringBuilder builder) {
        if (myPage == -1) {
            return;
        }
        builder.append(BrkpntSetCommand.SPACE);
        builder.append(PAGE_ARG);
        builder.append(myPage);
    }

    private void setMaxSize(StringBuilder builder) {
        if (mySize == -1) {
            return;
        }
        builder.append(BrkpntSetCommand.SPACE);
        builder.append(MAX_SIZE_ARG);
        builder.append(mySize);
    }

    private void setContext(StringBuilder builder) {
        if (myContext == -1) {
            return;
        }
        builder.append(BrkpntSetCommand.SPACE);
        builder.append(CONTEXT_ARG);
        builder.append(myContext);
    }

    private void setDepth(StringBuilder builder) {
        if (myDepth == -1) {
            return;
        }
        builder.append(BrkpntSetCommand.SPACE);
        builder.append(DEPTH_ARG);
        builder.append(myDepth);
    }

}
