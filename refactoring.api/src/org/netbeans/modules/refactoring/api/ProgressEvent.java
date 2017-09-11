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
package org.netbeans.modules.refactoring.api;

import java.util.EventObject;
import org.netbeans.api.annotations.common.NonNull;
import org.openide.util.Parameters;

/** Progress event object.
 *
 * @author Martin Matula
 */
public final class ProgressEvent extends EventObject {
    /** Start event id */
    public static final int START = 1;
    /** Step event id */
    public static final int STEP = 2;
    /** Stop event id */
    public static final int STOP = 4;

    // event id
    private final int eventId;
    // type of opreation that is being processed (source-specific number)
    private final int operationType;
    // number of steps of the operation being processed
    private final int count;

    /** Creates ProgressEvent instance.
     * @param source Source of the event.
     * @param eventId ID of the event.
     */
    public ProgressEvent(@NonNull Object source, int eventId) {
        this(source, eventId, 0, 0);
    }

    /** Creates ProgressEvent instance.
     * @param source Source of the event.
     * @param eventId ID of the event.
     * @param operationType Source-specific number identifying source operation that
     * is being processed.
     * @param count Number of steps that the processed opration consists of.
     */
    public ProgressEvent(@NonNull Object source, int eventId, int operationType, int count) {
        super(source);
        Parameters.notNull("source", source); // NOI18N
        this.eventId = eventId;
        this.operationType = operationType;
        this.count = count;
    }

    /** Returns ID of the event.
     * @return ID of the event.
     */
    public int getEventId() {
        return eventId;
    }

    /** Returns operation type.
     * @return Source-specific number identifying operation being processed. Needs to
     * be valid for START events, can be 0 for STEP and STOP events.
     */
    public int getOperationType() {
        return operationType;
    }

    /** Returns step count.
     * @return Number of step that the operation being processed consists of. Needs to
     * be valid for START events, can be 0 for STEP and STOP events. If it is not 0
     * for STEP events, it is a new progress.
     */
    public int getCount() {
        return count;
    }
}
