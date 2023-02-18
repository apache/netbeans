/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.httpserver;

import java.beans.*;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;

import org.openide.util.NbBundle;

/** Property editor for host property of HttpServerSettings class
*
* @author Ales Novak, Petr Jiricka
*/
public class HostPropertyEditor extends PropertyEditorSupport implements ExPropertyEditor {

    private PropertyEnv env;

    /** localized local (selected) host string*/
    private static String localhost() {
        return NbBundle.getMessage(HostPropertyEditor.class, "CTL_Local_host");
    }

    /** localized any host string*/
    private static String anyhost() {
        return NbBundle.getMessage(HostPropertyEditor.class, "CTL_Any_host");
    }

    /** @return text for the current value */
    @Override
    public String getAsText () {
        HttpServerSettings.HostProperty hp = (HttpServerSettings.HostProperty) getValue();
        if (hp == null) {
            return "";
        }
        String host = hp.getHost();
        if (host.equals(HttpServerSettings.LOCALHOST)) {
            return localhost () + hp.getGrantedAddresses ();
        }
        else {
            return anyhost ();
        }
    }

    /** @param text A text for the current value. */
    @Override
    public void setAsText (String text) {
        if (anyhost ().equals (text)) {
            setValue (new HttpServerSettings.HostProperty ("", HttpServerSettings.ANYHOST));    // NOI18N
            return;
        } else if (text != null && text.startsWith(localhost())) {
            setValue (new HttpServerSettings.HostProperty (text.substring (localhost ().length ()), HttpServerSettings.LOCALHOST));
            return;
        } else if (text != null) {
            setValue (new HttpServerSettings.HostProperty (text, HttpServerSettings.LOCALHOST));
            return;
        }
        throw new IllegalArgumentException (text);
    }

    @Override
    public boolean supportsCustomEditor () {
        return true;
    }

    @Override
    public java.awt.Component getCustomEditor () {
        return new HostPropertyCustomEditor (this, env);
    }

    @Override
    public void attachEnv(PropertyEnv env) {
        this.env = env;
    }

}
