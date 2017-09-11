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
     * <br/>
     * The processor may call {@link CodeTemplateInsertRequest#getMasterParameters()}
     * to find the master parameters.
     * <br/>
     * On each parameter {@link CodeTemplateParameter#setValue(String)}
     * can be called. The value will be propagated to all slave parameters
     * automatically.
     */
    void updateDefaultValues();

    /**
     * Notification that a master parameter's value has been modified
     * by the user and the processor may need to react to it.
     * <br/>
     * This notification is only done after the code template was physically
     * inserted into the document i.e. {@link CodeTemplateInsertRequest#isInserted()}
     * returns true.
     * 
     * <br/>
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
     *  <br/>
     *  <code>true</code> is passed if the parameter value was modified
     *  by user's typing. Some processors may want such immediate reaction.
     *  <br/>
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
     * <br/>
     * The processor can free possible resources related to the insert
     * request processing.
     *
     * @see CodeTemplateInsertRequest#isReleased()
     */
    void release();

}
