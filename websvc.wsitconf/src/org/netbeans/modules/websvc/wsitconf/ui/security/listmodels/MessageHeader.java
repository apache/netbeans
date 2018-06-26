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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitconf.ui.security.listmodels;

import org.openide.util.NbBundle;

public class MessageHeader extends TargetElement {

    public static final String ADDRESSING_TO=NbBundle.getMessage(MessageHeader.class, "COMBO_Addr_To");  //NOI18N
    public static final String ADDRESSING_FROM=NbBundle.getMessage(MessageHeader.class, "COMBO_Addr_From");  //NOI18N
    public static final String ADDRESSING_FAULTTO=NbBundle.getMessage(MessageHeader.class, "COMBO_Addr_FaultTo");  //NOI18N
    public static final String ADDRESSING_REPLYTO=NbBundle.getMessage(MessageHeader.class, "COMBO_Addr_ReplyTo");  //NOI18N
    public static final String ADDRESSING_MESSAGEID=NbBundle.getMessage(MessageHeader.class, "COMBO_Addr_MessageId");  //NOI18N
    public static final String ADDRESSING_RELATESTO=NbBundle.getMessage(MessageHeader.class, "COMBO_Addr_RelatesTo");  //NOI18N
    public static final String ADDRESSING_ACTION=NbBundle.getMessage(MessageHeader.class, "COMBO_Addr_Action");  //NOI18N
    public static final String RM_ACKREQUESTED=NbBundle.getMessage(MessageHeader.class, "COMBO_RM_AckRequested");  //NOI18N
    public static final String RM_SEQUENCEACK=NbBundle.getMessage(MessageHeader.class, "COMBO_RM_SequenceAck");  //NOI18N
    public static final String RM_SEQUENCE=NbBundle.getMessage(MessageHeader.class, "COMBO_RM_Sequence");  //NOI18N
    public static final String RM_CREATESEQUENCE=NbBundle.getMessage(MessageHeader.class, "COMBO_RM_CreateSequence");  //NOI18N

    public static final String[] ADDRESSING_HEADERS  = new String[] { 
        ADDRESSING_TO, 
        ADDRESSING_FROM, 
        ADDRESSING_FAULTTO, 
        ADDRESSING_REPLYTO, 
        ADDRESSING_MESSAGEID,
        ADDRESSING_RELATESTO,
        ADDRESSING_ACTION
    };

    public static final String[] RM_HEADERS  = new String[] { 
        RM_ACKREQUESTED,
        RM_SEQUENCEACK,
        RM_SEQUENCE,
        RM_CREATESEQUENCE
    };
    
    public MessageHeader(String header) {
        super(header);
    }
 
}
