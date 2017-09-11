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



package org.netbeans.modules.jumpto.quicksearch;

import org.netbeans.modules.jumpto.symbol.GoToSymbolAction;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jan Becicka
 */
public class GoToSymbolProvider implements SearchProvider {

    private GoToSymbolWorker worker;

    public void evaluate(SearchRequest request, SearchResponse response) {
        String text = removeNonJavaChars(request.getText());
        if(text.length() == 0) {
            return;
        }
        GoToSymbolWorker local;
        synchronized(this) {
            if (worker!=null) {
                worker.cancel();
            }
            worker = new GoToSymbolWorker(text);
            local = worker;
        }
        local.run();
        
        for (SymbolDescriptor td : local.getTypes()) {
            String displayHint = td.getFileDisplayPath();
            String htmlDisplayName = escapeLtGt(td.getSymbolName()) + " " + NbBundle.getMessage(GoToSymbolAction.class, "MSG_DeclaredIn",escapeLtGt(td.getOwnerName()));
            final String projectName = td.getProjectName();
            if (projectName != null && !projectName.isEmpty()) {
                htmlDisplayName = String.format(
                    "%s [%s]",  //NOI18N
                    htmlDisplayName,
                    projectName);
            }
            if (!response.addResult(new GoToSymbolCommand(td),
                                    htmlDisplayName,
                                    displayHint,
                                    null)) {
                break;
            }
        }
    }
     
    private static String escapeLtGt(String input) {
        String temp = input.replaceAll("<", "&lt;"); // NOI18N
        temp = temp.replaceAll(">", "&gt;"); // NOI18N
        return temp;
    }
     
    private static class GoToSymbolCommand implements Runnable {
        private SymbolDescriptor command;
        
        public GoToSymbolCommand(SymbolDescriptor command) {
            this.command = command;
        }

        public void run() {
            command.open();
        }
    }

    private static String removeNonJavaChars(String text) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
