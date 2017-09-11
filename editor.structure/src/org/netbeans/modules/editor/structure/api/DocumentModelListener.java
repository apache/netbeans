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


package org.netbeans.modules.editor.structure.api;

import java.util.EventListener;


/**
 * An implementation of EventListener allowing to listen o changes of the DocumentModel.
 * This listner is very similar to the {@link DocumentElementListener} but in contrast with it
 * it allows to listen on the entire model, not only on a particullar element.
 *<br>
 * Allows to listen on following changes:
 * <ul>
 * <li>A new element has been added into the model
 * <li>An element has been removed from the model
 * <li>Content of an element has been changed
 * <li>Attributes of an element has changed
 * </ul>
 *
 * @author Marek Fukala
 * @version 1.0
 *
 * @see DocumentElement
 * @see DocumentElementEvent
 * @see DocumentElementListener
 *
 */
public interface DocumentModelListener extends EventListener {
    
    /** fired when a new element has been added into the model. */
    public void documentElementAdded(DocumentElement de);
    
    /** fired when an existing element has been removed from the model. */
    public void documentElementRemoved(DocumentElement de);
    
    /** fired when an element's text content has been changed. */
    public void documentElementChanged(DocumentElement de);
    
    /** fired when attributes of an element have been changed (removed/added/value changed) */
    public void documentElementAttributesChanged(DocumentElement de);
    
}
