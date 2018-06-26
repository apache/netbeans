/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.admin;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.glassfish.tooling.admin.response.MessagePart;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.logging.Logger;

/**
 * Command runner that executes get property command.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class RunnerRestGetProperty extends RunnerRest {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER
            = new Logger(RunnerRestGetProperty.class);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Result object - contains list of JDBC resources names.*/
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    ResultMap<String, String> result;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of administration command executor using
     * REST interface.
     * <p/>
     * @param server  GlassFish server entity object.
     * @param command GlassFish server administration command entity.
     */
    public RunnerRestGetProperty(final GlassFishServer server,
            final Command command) {
        super(server, command, "/command/", null);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Abstract Methods                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create <code>ResultMap</code> object corresponding
     * to server get property command execution value to be returned.
     */
    @Override
    protected ResultMap<String, String> createResult() {
        return result = new ResultMap<String, String>();
    }

    @Override
    protected boolean processResponse() {
        final String METHOD = "processResponse";
        List<MessagePart> childMessages = report.getTopMessagePart().getChildren();
        if ((childMessages == null) || childMessages.isEmpty()) {
            return false;
        }
        result.value = new HashMap<String, String>(childMessages.size());
        
        for (MessagePart msg : childMessages) {
            String message = msg.getMessage();
            int equalsIndex = message.indexOf('=');
            if (equalsIndex >= 0) {
                String keyPart = message.substring(0, equalsIndex);
                String valuePart = message.substring(equalsIndex + 1);
                try {
                    // Around Sept. 2008... 3.x servers were double encoding their
                    // responces.  It appears that has stopped
                    // (See http://netbeans.org/bugzilla/show_bug.cgi?id=195015)
                    // The open question is, "When did 3.x stop doing the double
                    // encode?" since we don't know... this strategy will work
                    // for us
                    //   Belt and suspenders, like
                    result.value.put(keyPart, valuePart);       // raw form
                    result.value.put(keyPart, URLDecoder.decode(valuePart,
                            "UTF-8"));                          // single decode
                    result.value.put(keyPart, URLDecoder.decode(result.value.
                            get(keyPart), "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    LOGGER.log(Level.INFO, METHOD,
                            "unsupportedEncoding", result.value.get(keyPart));
                } catch (IllegalArgumentException iae) {
                    LOGGER.log(Level.INFO, METHOD, "illegalArgument",
                            new Object[] {valuePart, result.value.get(keyPart)});
                }
            } else {
                LOGGER.log(Level.INFO, METHOD, "emptyString", message);
                result.value.put(message, "");
            }
        }
        return true;
    }
    
    @Override
    protected void handleSend(HttpURLConnection hconn) throws IOException {
         OutputStreamWriter wr = new OutputStreamWriter(hconn.getOutputStream());
         CommandGetProperty gpCommand = (CommandGetProperty) command;
         StringBuilder data = new StringBuilder();
         data.append("pattern=").append(gpCommand.propertyPattern);
         wr.write(data.toString());
         wr.flush();
         wr.close();
    }

}
