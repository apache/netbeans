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

import java.util.concurrent.*;
import org.netbeans.modules.glassfish.tooling.GlassFishIdeException;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;

/**
 * Command that sets property (properties) on the server.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
@RunnerHttpClass(runner=RunnerHttpSetProperty.class)
@RunnerRestClass(runner=RunnerRestSetProperty.class)
public class CommandSetProperty extends Command {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Command string for create-cluster command. */
    private static final String COMMAND = "set";

    /** Error message prefix for administration command execution exception .*/
    private static final String ERROR_MESSAGE_PREFIX
            = "Could not set value ";

    /** Error message middle part for administration command execution
     *  exception .*/
    private static final String ERROR_MESSAGE_MIDDLE
            = " of property ";

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Build error message from command property name and it's value.
     * <p/>
     * @param command Command used to build error message.
     * @return Error message for administration command execution exception.
     */
    private static String errorMessage(final CommandSetProperty command) {
        int valueLen = command.value != null
                ? command.value.length() : 0;
        int propertyLen = command.property != null
                ? command.property.length() : 0;
        StringBuilder sb = new StringBuilder(ERROR_MESSAGE_PREFIX.length()
                + ERROR_MESSAGE_MIDDLE.length() + valueLen + propertyLen);
        sb.append(ERROR_MESSAGE_PREFIX);
        sb.append(valueLen > 0 ? command.value : "");
        sb.append(ERROR_MESSAGE_MIDDLE);
        sb.append(propertyLen > 0 ? command.property : "");
        return sb.toString();
    }

    /**
     * Put property to server.
     * <p/>
     * @param server  GlassFish server entity.
     * @param command Command to set property value.
     * @return GlassFish command result containing <code>String</code> with
     *         result message.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString setProperty(
            final GlassFishServer server, final CommandSetProperty command)
            throws GlassFishIdeException {
        Future<ResultString> future =
                ServerAdmin.<ResultString>exec(server, command);
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException
                | CancellationException ee) {
            throw new GlassFishIdeException(errorMessage(command), ee);
        }
    }

    /**
     * Put property to server.
     * <p/>
     * @param server  GlassFish server entity.
     * @param command Command to set property value.
     * @param timeout         Administration command execution timeout [ms].
     * @return GlassFish command result containing <code>String</code> with
     *         result message.
     * @throws GlassFishIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString setProperty(
            final GlassFishServer server, final CommandSetProperty command,
            final long timeout) throws GlassFishIdeException {
        Future<ResultString> future =
                ServerAdmin.<ResultString>exec(server, command);
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException
                | CancellationException ee) {
            throw new GlassFishIdeException(errorMessage(command), ee);
        } catch (TimeoutException te) {
            throw new GlassFishIdeException(errorMessage(command)
                    + " in " + timeout + "ms", te);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////
    
    /** Name of the property to set. */
    final String property;
    
    /** Value of the property to set. */
    final String value;
    
    /** Format for the query string. */
    final String format;
    
    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an instance of GlassFish server set property command entity.
     * <p/>
     * @param property Name of the property to set.
     * @param value    Value of the property to set.
     * @param format   Format for the query string.
     */
    public CommandSetProperty(final String property, final String value,
            final String format) {
        super(COMMAND);
        this.property = property;
        this.value = value;
        this.format = format;
    }
    
    /**
     * Constructs an instance of GlassFish server set property command entity.
     * <p/>
     * @param property Name of the property to set.
     * @param value    Value of the property to set.
     */
    public CommandSetProperty(final String property, final String value) {
        super(COMMAND);
        this.property = property;
        this.value = value;
        this.format = "DEFAULT={0}={1}";
    }

    ////////////////////////////////////////////////////////////////////////////
    // Getters                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get name of the property to set.
     * <p/>
     * @return Name of the property to set.
     */
    public String getProperty() {
        return property;
    }

    /**
     * Get value of the property to set.
     * <p/>
     * @return Value of the property to set.
     */
    public String getValue() {
        return value;
    }

}
