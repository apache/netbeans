/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.modules.java.api.common.project.ui.customizer.vmo;

import org.antlr.runtime.Token;

/**
 * @author Rastislav Komara
 */
public class ParametrizedNode extends JavaVMOption<OptionValue.SimpleString> {
    private String delimiter;

    public ParametrizedNode(Token token, int splitIndex) {
        super(token);
        final String string = token.getText();
        if (string != null) {
            setName(string.substring(0, splitIndex));
            setValue(new OptionValue.SimpleString(string.substring(splitIndex)));
            delimiter = "";
            setValid(true);
        } else {
            setName("");
            setValid(false);
        }
    }

    public ParametrizedNode(Token name, String delimiter, String parameter) {
        this(name,delimiter, parameter, true);
    }
    public ParametrizedNode(Token name, String delimiter, String parameter, boolean isValid) {
        super(name);
        setName(name.getText());
        this.delimiter = delimiter;
        if (parameter != null) {
            setValue(new OptionValue.SimpleString(parameter));
        }
        setValid(isValid);
    }

    public ParametrizedNode(String name, String delimiter) {
        super(name);
        this.delimiter = delimiter;
        setValue(new OptionValue.SimpleString());
    }

    public ParametrizedNode(Token token, String name, String delimiter, String value) {
        super(token);
        setName(name);
        this.delimiter = delimiter;
        setValue(new OptionValue.SimpleString(value));
    }

    @Override
    public StringBuilder print(StringBuilder builder) {
        StringBuilder sb = super.print(builder);
        if (getValue().isPresent()) {
                sb.append(SPACE).append(HYPHEN).append(getName()).append(delimiter).append(getValue().getValue());
        }
        return sb;
    }
}
