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

package org.netbeans.modules.debugger.ui;

import java.awt.Point;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.Watch.Pin;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.EditorPin;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;


/**
 *
 * @author Jan Jancura
 */
@DebuggerServiceRegistration(types = Properties.Reader.class)
public class WatchesReader implements Properties.Reader {
    
    private static final Logger LOG = Logger.getLogger(WatchesReader.class.getName());

    private static final String PIN = "pin";                    // NOI18N
    private static final String PIN_URL = "url";                // NOI18N
    private static final String PIN_LINE = "line";              // NOI18N
    private static final String PIN_LOCATION_X = "locationX";   // NOI18N
    private static final String PIN_LOCATION_Y = "locationY";   // NOI18N
    private static final String PIN_COMMENT = "comment";        // NOI18N
    private static final String PIN_VP_ID = "valueProviderId";  // NOI18N
    
    public static PinReaderAccess PIN_READER_ACCESS;

    public String [] getSupportedClassNames () {
        return new String[] {
            Watch.class.getName (),
            EditorPin.class.getName()
        };
    }
    public Object read (String typeID, Properties properties) {
        if (typeID.equals (Watch.class.getName ())) {
            String expression = properties.getString (Watch.PROP_EXPRESSION, null);
            Pin pin = (Pin) properties.getObject(PIN, null);
            Watch watch;
            if (pin != null) {
                watch = DebuggerManager.getDebuggerManager().createPinnedWatch(expression, pin);
            } else {
                watch = DebuggerManager.getDebuggerManager().createWatch(expression);
            }
            watch.setEnabled(properties.getBoolean(Watch.PROP_ENABLED, true));
            return watch;
        }
        if (typeID.equals(EditorPin.class.getName())) {
            String urlStr = properties.getString(PIN_URL, null);
            URL url;
            try {
                url = new URL(urlStr);
            } catch (MalformedURLException ex) {
                LOG.log(Level.CONFIG, "urlStr = "+urlStr, ex);
                return null;
            }
            FileObject fo = URLMapper.findFileObject(url);
            if (fo == null) {
                return null;    // The file's gone.
            }
            int line = properties.getInt(PIN_LINE, 0);
            Point location = new Point(properties.getInt(PIN_LOCATION_X, 0),
                                       properties.getInt(PIN_LOCATION_Y, 0));
            EditorPin pin = new EditorPin(fo, line, location);
            String comment = properties.getString(PIN_COMMENT, null);
            if (comment != null) {
                pin.setComment(comment);
            }
            PIN_READER_ACCESS.setVpId(pin, properties.getString(PIN_VP_ID, null));
            return pin;
        }
        return null;
    }
    
    public void write (Object object, Properties properties) {
        if (object instanceof Watch) {
            Watch w = (Watch) object;
            properties.setString (
                Watch.PROP_EXPRESSION, 
                w.getExpression ()
            );
            properties.setBoolean(Watch.PROP_ENABLED, w.isEnabled());
            Watch.Pin pin = w.getPin();
            properties.setObject(PIN, pin);
        } else if (object instanceof EditorPin) {
            EditorPin pin = (EditorPin) object;
            properties.setString(PIN_URL, pin.getFile().toURL().toExternalForm());
            properties.setInt(PIN_LINE, pin.getLine());
            properties.setInt(PIN_LOCATION_X, pin.getLocation().x);
            properties.setInt(PIN_LOCATION_Y, pin.getLocation().y);
            String comment = pin.getComment();
            properties.setString(PIN_COMMENT, comment);
            properties.setString(PIN_VP_ID, PIN_READER_ACCESS.getVpId(pin));
        }
    }
    
    public static interface PinReaderAccess {

        String getVpId(EditorPin pin);

        void setVpId(EditorPin pin, String vpId);

    }

}
