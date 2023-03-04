/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
