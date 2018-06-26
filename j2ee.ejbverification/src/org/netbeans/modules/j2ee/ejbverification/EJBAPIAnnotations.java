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

package org.netbeans.modules.j2ee.ejbverification;

/**
 * This class defines constants that represent various annotation type names
 * defined in EJB specification.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public interface EJBAPIAnnotations {
    String ASYNCHRONOUS = "javax.ejb.Asynchronous"; //NOI18N

    String REMOTE = "javax.ejb.Remote"; //NOI18N
    String LOCAL = "javax.ejb.Local"; //NOI18N

    String STATELESS = "javax.ejb.Stateless"; // NOI18N

    String STATEFUL = "javax.ejb.Stateful"; // NOI18N
    String INIT = "javax.ejb.Init"; // NOI18N
    String REMOVE = "javax.ejb.Remove"; // NOI18N

    String MESSAGE_DRIVEN = "javax.ejb.MessageDriven"; // NOI18N
    String ACTIVATION_CONFIG_PROPERTY = "javax.ejb.ActivationConfigProperty"; // NOI18N

    String REMOTE_HOME = "javax.ejb.RemoteHome"; //NOI18N
    String LOCAL_HOME = "javax.ejb.LocalHome"; //NOI18N

    String TRANSACTION_MANAGEMENT = "javax.ejb.TransactionManagement"; //NOI18N

    //value attribute in annotations with single attribute
    String VALUE = "value"; //NOI18N

    String WEB_SERVICE = "javax.jws.WebService"; //NOI18N
    // TODO: Add other ones here including enum types
    String LOCAL_BEAN = "javax.ejb.LocalBean";

    String POST_CONSTRUCT = "javax.annotation.PostConstruct";
    String AROUND_INVOKE = "javax.interceptor.AroundInvoke";

    String SCHEDULE = "javax.ejb.Schedule"; //NOI18N
    // @Schedule parameter for persistent timer
    String PERSISTENT = "persistent"; //NOI18N

    String SESSION_SYNCHRONIZATION = "javax.ejb.SessionSynchronization"; //NOI18N
}
