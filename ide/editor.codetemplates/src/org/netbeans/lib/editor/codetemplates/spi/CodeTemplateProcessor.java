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

package org.netbeans.lib.editor.codetemplates.spi;

/**
 * Fills in default values of the code template's parameters and may react
 * to user's typing modifications of the parameters.
 * <br>
 * Each processor is associated with {@link CodeTemplateInsertRequest}
 * which was given to it during construction by {@link CodeTemplateProcessorFactory}.
 * @see CodeTemplateProcessorFactory
 *
 * @author Miloslav Metelka
 */
public interface CodeTemplateProcessor {

    /**
     * Update the values of the parameters in the parsed code template
     * before the code template gets physically inserted into the document.
     * <br>
     * The processor may call {@link CodeTemplateInsertRequest#getMasterParameters()}
     * to find the master parameters.
     * <br>
     * On each parameter {@link CodeTemplateParameter#setValue(String)}
     * can be called. The value will be propagated to all slave parameters
     * automatically.
     */
    void updateDefaultValues();

    /**
     * Notification that a master parameter's value has been modified
     * by the user and the processor may need to react to it.
     * <br>
     * This notification is only done after the code template was physically
     * inserted into the document i.e. {@link CodeTemplateInsertRequest#isInserted()}
     * returns true.
     * 
     * <br>
     * Typically the processor either does nothing or it may change other
     * parameter(s)' values. The change may occur in the same thread
     * or it may post the parameter's new value recomputation and changing
     * into another thread.
     *
     * <p>
     * The processor is only allowed to change master parameters.
     * </p>
     *
     * <p>
     * Slave parameter's changes are not notified at all.
     * </p>
     *
     * @param masterParameter master parameter that was changed.
     * @param typingChange allows to react to user's typing immediately
     *  or only react once the active parameter gets changed e.g. by <i>TAB</i>.
     *  <br>
     *  <code>true</code> is passed if the parameter value was modified
     *  by user's typing. Some processors may want such immediate reaction.
     *  <br>
     *  Others will only react when this parameter
     *  is <code>false</code> which happens when
     *  at least one typing change occurred in the current active parameter
     *  and the active parameter is being changed by <i>TAB</i>
     *  or <i>Shift-TAB</i> or <i>Enter</i>.
     */
    void parameterValueChanged(CodeTemplateParameter masterParameter, boolean typingChange);

    /**
     * Notify the processor that the insert request which it services
     * was already completed and there is no more work to do.
     * <br>
     * The processor can free possible resources related to the insert
     * request processing.
     *
     * @see CodeTemplateInsertRequest#isReleased()
     */
    void release();

}
