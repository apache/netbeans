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
package org.netbeans.modules.payara.tooling.admin;

import java.util.concurrent.*;
import org.netbeans.modules.payara.tooling.PayaraIdeException;
import org.netbeans.modules.payara.tooling.data.PayaraServer;

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
     * @param server  Payara server entity.
     * @param command Command to set property value.
     * @return Payara command result containing <code>String</code> with
     *         result message.
     * @throws PayaraIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString setProperty(
            final PayaraServer server, final CommandSetProperty command)
            throws PayaraIdeException {
        Future<ResultString> future =
                ServerAdmin.<ResultString>exec(server, command);
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException
                | CancellationException ee) {
            throw new PayaraIdeException(errorMessage(command), ee);
        }
    }

    /**
     * Put property to server.
     * <p/>
     * @param server  Payara server entity.
     * @param command Command to set property value.
     * @param timeout         Administration command execution timeout [ms].
     * @return Payara command result containing <code>String</code> with
     *         result message.
     * @throws PayaraIdeException When error occurred during administration
     *         command execution.
     */
    public static ResultString setProperty(
            final PayaraServer server, final CommandSetProperty command,
            final long timeout) throws PayaraIdeException {
        Future<ResultString> future =
                ServerAdmin.<ResultString>exec(server, command);
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException
                | CancellationException ee) {
            throw new PayaraIdeException(errorMessage(command), ee);
        } catch (TimeoutException te) {
            throw new PayaraIdeException(errorMessage(command)
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
     * Constructs an instance of Payara server set property command entity.
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
     * Constructs an instance of Payara server set property command entity.
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
