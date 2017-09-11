/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.parser.model;

import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.html.editor.lib.api.HelpItem;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagType;

/**
 *
 * @author marekfukala
 */
public class UnknownHtmlTag implements HtmlTag {

    private String elementName;

    UnknownHtmlTag(String elementName) {
        this.elementName = elementName;
    }

    @Override
    public String getName() {
        return elementName;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
        hash = 61 * hash + (this.getTagClass() != null ? this.getTagClass().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof HtmlTag)) {
            return false;
        }
        HtmlTag other = (HtmlTag) obj;

        if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
            return false;
        }
        if (this.getTagClass() != other.getTagClass()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("ElementName2HtmlTagAdapter{name=%s, type=%s}", getName(), getTagClass());//NOI18N
    }

    @Override
    public synchronized Collection<HtmlTagAttribute> getAttributes() {
        return Collections.emptyList();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean hasOptionalOpenTag() {
        return false;
    }

    @Override
    public boolean hasOptionalEndTag() {
        return false;
    }

    @Override
    public synchronized HtmlTagAttribute getAttribute(String name) {
        return null;
    }

    @Override
    public HtmlTagType getTagClass() {
        return HtmlTagType.UNKNOWN;
    }

    @Override
    public synchronized Collection<HtmlTag> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public HelpItem getHelp() {
        return null;
    }
}
