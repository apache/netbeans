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
package org.netbeans.modules.netserver.api;


/**
 * @author ads
 *
 */
public final class ProtocolDraft {
    
    public enum Draft {
        Draft75,
        Draft76,
    }

    private ProtocolDraft( Draft draft ){
        this.draft = draft;
        version = 0;
    }
    
    private ProtocolDraft( int version){
        draft = null;
        this.version = version;
    }
    
    private ProtocolDraft( ){
        draft = null;
        version = 0;
    }
    
    public static ProtocolDraft getProtocol( int number ){
        if ( number == 75 ){
            return new ProtocolDraft( Draft.Draft75 );
        }
        else if ( number == 76 ){
            return new ProtocolDraft( Draft.Draft76 );
        }
        else if ( number >=7 && number < 13){
            return new ProtocolDraft(number);
        }
        else if (number >=13 && number <=17){
            return new ProtocolDraft();
        }
        else {
            throw new IllegalArgumentException();
        }
    }
    
    public static ProtocolDraft getRFC(){
        return new ProtocolDraft();
    }
    
    @Override
    public boolean equals(Object obj) {
        if ( obj instanceof ProtocolDraft){
            ProtocolDraft protocol = (ProtocolDraft)obj;
            return draft == protocol.draft && version == protocol.version;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        if ( draft == Draft.Draft75){
            return 75;
        }
        else if ( draft == Draft.Draft76){
            return 76; 
        }
        else {
            return version;
        }
    }
    
    public Draft getDraft(){
        return draft;
    }
    
    public int getVersion(){
        return version;
    }
    
    public boolean isRFC(){
        return draft == null && version ==0;
    }
    
    private final Draft draft;
    private final int version;
}
