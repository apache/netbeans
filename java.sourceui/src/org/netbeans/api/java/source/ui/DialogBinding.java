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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.api.java.source.ui;

import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.JavaSource;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Dusan Balek, Jan Lahoda
 * @since 1.1
 * @deprecated Use {@link org.netbeans.api.editor.DialogBinding} instead.
 */
@Deprecated
public final class DialogBinding {

    private DialogBinding() {}
    
    /**
     * Bind given component and given file together.
     * @param fileObject to bind
     * @param offset position at which content of the component will be virtually placed
     * @param length how many characters replace from the original file
     * @param component component to bind
     * @return {@link JavaSource} or null
     * @throws IllegalArgumentException if fileObject is null
     * @since 1.1
     * @deprecated Use {@link org.netbeans.api.editor.DialogBinding#bindComponentToFile(FileObject,int,int,JTextComponent)} instead.
     */
    @Deprecated
    public static JavaSource bindComponentToFile(FileObject fileObject, int offset, int length, JTextComponent component) throws IllegalArgumentException {
        org.netbeans.api.editor.DialogBinding.bindComponentToFile(fileObject, offset, length, component);
        return null;
    }
}
