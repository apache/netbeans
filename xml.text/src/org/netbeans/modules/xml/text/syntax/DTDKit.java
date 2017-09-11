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
package org.netbeans.modules.xml.text.syntax;

import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.Syntax;
import org.netbeans.modules.xml.text.syntax.bridge.LegacySyntaxBridge;
import org.netbeans.modules.xml.text.syntax.javacc.lib.*;
import org.netbeans.modules.xml.text.syntax.javacc.*;

/**
 * Editor kit implementation for dtd content type.
 * In inherits inband encoding handling from UniKit because
 * a DTD is an external entity that must be UTF-8 encoded
 * or it must begin with "<?xml ...?>" i.e. encoding can be retrieved by 
 * inband-encoding atribute assisted autodetection.
 *
 * @author Libor Kramolis
 * @author Petr Kuzel
 * @version jj
 */
public class DTDKit extends UniKit {

    /** Serial Version UID */
    private static final long serialVersionUID =-6140259975700590155L;
    
    /**
     * Default MIME type.
     * Read http://www.ietf.org/rfc/rfc3023.txt
     */
    public static final String MIME_TYPE = "application/xml-dtd"; // NOI18N
    
    @Override
    public String getContentType() {
        return MIME_TYPE;
    }
}
