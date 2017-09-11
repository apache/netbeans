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

package threaddemo.views;

import java.util.Enumeration;
import org.netbeans.spi.looks.Look;
import org.netbeans.spi.looks.LookProvider;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;
import threaddemo.model.Phadhail;

/**
 * A look selector matching PhadhailLook.
 * @author Jesse Glick
 */
final class PhadhailLookProvider implements LookProvider {

    private static final Look PHADHAIL_LOOK = new PhadhailLook();
    private static final Look STRING_LOOK = new StringLook();
    private static final Look ELEMENT_LOOK = new ElementLook();
    
    public PhadhailLookProvider() {}
    
    public Enumeration getLooksForObject(Object representedObject) {
        if (representedObject instanceof Phadhail) {
            return Enumerations.singleton(PHADHAIL_LOOK);
        } else if (representedObject instanceof String) {
            return Enumerations.singleton(STRING_LOOK);
        } else {
            assert representedObject instanceof Element : representedObject;
            assert representedObject instanceof EventTarget : representedObject;
            return Enumerations.singleton(ELEMENT_LOOK);
        }
    }
    
    /**
     * Just shows plain text nodes - markers.
     */
    private static final class StringLook extends Look {
        public StringLook() {
            super("StringLook");
        }
        public String getDisplayName() {
            return "Simple Messages";
        }
        public String getName(Object o, Lookup l) {
            return (String)o;
        }
        public String getDisplayName(Object o, Lookup l) {
            return (String)o;
        }
        public boolean isLeaf(Object o, Lookup l) {
            return true;
        }
    }
    
}
