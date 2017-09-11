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

package org.netbeans.modules.project.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;


/**
 * Class for persisting icons
 * @author Milan Kubec, mkleint
 */
public class ExtIcon {

    Icon icon;

    public ExtIcon() {
    }

    public ExtIcon(byte[] content) {
        ObjectInputStream objin = null;
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(content);
            objin = new ObjectInputStream(in);
            Object obj = objin.readObject();
            if (obj instanceof Icon) {
                setIcon((Icon)obj);
            }
        } catch (Exception ex) {
            setIcon(ImageUtilities.loadImageIcon("org/openide/resources/actions/empty.gif", false)); //NOI18N
        } finally {
            try {
                if (objin != null) {
                    objin.close();
                }
            } catch (IOException ex) {
            }
        }
    }

    public void setIcon(Icon icn) {
        icon = icn;
    }

    public Icon getIcon() {
        return icon;
    }

    public byte[] getBytes() throws IOException {
        if (getIcon() == null) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(out);

        //#138000
        Icon icn = getIcon();
        if (! (icn instanceof Serializable)) {
            icn = new ImageIcon(ImageUtilities.icon2Image(icn));
        }
        objOut.writeObject(icn);
        objOut.close();
        return out.toByteArray();
    }
}
