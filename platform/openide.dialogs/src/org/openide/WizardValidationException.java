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
package org.openide;

import javax.swing.JComponent;


/** The exception informs about fail in wizard panel validation and provides
 * a localized description what's wrong. Also can return JComponent which should
 * be focused to correct wrong values.
 *
 * @author  Jiri Rechtacek
 * @since 4.28
 */
public final class WizardValidationException extends Exception {
    private String localizedMessage;
    private JComponent source;

    /** Creates a new instance of WizardValidationException */
    private WizardValidationException() {
    }

    /**
     * Creates a new exception instance.
     * @param source component which should have focus to correct wrong values
     * @param message the detail message
     * @param localizedMessage description notifies an user what value must be corrected
     */
    public WizardValidationException(JComponent source, String message, String localizedMessage) {
        super(message);
        this.source = source;
        this.localizedMessage = localizedMessage;
    }

    /**
     *
     * @return JComponent for request focus to correct wrong values
     * or null if there is no useful component to focus it
     */
    public JComponent getSource() {
        return source;
    }

    /**
     *
     * @return description will notifies an user what value must be corrected
     */
    @Override
    public String getLocalizedMessage() {
        return (localizedMessage != null) ? localizedMessage : this.getMessage();
    }
}
