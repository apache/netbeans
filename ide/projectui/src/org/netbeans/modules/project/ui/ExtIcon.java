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
