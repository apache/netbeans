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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javahelp;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;

/**
 * The <code>CopyLinkLocationAction</code>.
 * 
 * @author Victor G. Vasilyev
 */
public class CopyLinkLocationAction extends  AbstractAction {

    private static final String LBL_CopyLinkLocationAction =
                              NbBundle.getMessage(HyperlinkEventProcessor.class,
                                                  "LBL_CopyLinkLocationAction");

    private LinkOwner linkOwner;

    public CopyLinkLocationAction(LinkOwner link) {
        super(LBL_CopyLinkLocationAction);
        // putValue(SHORT_DESCRIPTION, desc);
        // putValue(MNEMONIC_KEY, mnemonic);
        this.linkOwner = link;
    }

    public void actionPerformed(ActionEvent e) {
        textToSystemClipboard(linkOwner.getURLExternalForm());
    }

    private void textToSystemClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = linkOwner.getClipboard();
        clipboard.setContents(selection, linkOwner);
    }

    public interface LinkOwner extends ClipboardOwner {
        String getURLExternalForm();
        Clipboard getClipboard();
    }
    
}
