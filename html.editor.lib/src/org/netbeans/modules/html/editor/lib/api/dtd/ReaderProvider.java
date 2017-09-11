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
package org.netbeans.modules.html.editor.lib.api.dtd;

import java.io.Reader;
import java.util.Collection;
import org.openide.filesystems.FileObject;

/**
 * DTDReaderProvider is interface used as a source of Readers used to parse DTD
 * by DTDParser. One DTDReaderProvider shall offer all Readers for a given DTD,
 * i.e. the provider for "-//W3C//DTD HTML 4.01//EN" shall also provide Readers
 * for proper "-//W3C//ENTITIES Latin1//EN/HTML", as this public entity is
 * referred from HTML 4.01 DTD and the file provided with 4.01 DTD differs
 * from the file provided with 4.0 DTD although they have the same
 * public identifier (They differ only in comments, though).
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public interface ReaderProvider {

    /* Asks for Reader providing content of DTD file identified by 
     * given identifier, and possibly by given fileName.
     * These parameters are typically obtained from invocation DTD directive
     * like &lt;!ENTITY % HTMLlat1 PUBLIC "-//W3C//ENTITIES Latin1//EN//HTML" "HTMLlat1.ent">,
     * in this case, the string -//W3C//....//HTML" is identifier
     * and "HTMLlat1.ent" is name of file in which it is probably stored
     * @param identifier the public identifier of required DTD
     * @param fileName the probable name of file with DTD data, may be
     *      <CODE>null</CODE>. It is used only as helper to identifier.
     * @return Reader from which to read out the DTD content.
     */
    public Reader getReaderForIdentifier( String identifier, String fileName );
    
    /** Asks for all the identifiers available from this ReaderProvider.
     * @returns a Collection of all identifiers for which this ReaderProvider
     * is able to provide Readers for.
     */
    public Collection<String> getIdentifiers();

    /**
     *
     * @param publicId
     * @return an internall system ID resource as FileObject for the given public ID
     */
    public FileObject getSystemId(String publicId);

    /**
     * @param public identifiers
     * @return true if the content to parse is xml content, false if sgml content
     */
    public boolean isXMLContent(String identifier);
}
