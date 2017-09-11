/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.metadata.model.jdbc;

import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.api.Nullable;
import org.netbeans.modules.db.metadata.model.api.Parameter.Direction;
import org.netbeans.modules.db.metadata.model.api.MetadataElement;
import org.netbeans.modules.db.metadata.model.api.SQLType;
import org.netbeans.modules.db.metadata.model.spi.ParameterImplementation;

/**
 *
 * @author David Van Couvering
 */
public class JDBCParameter extends ParameterImplementation {

    private static final Logger LOGGER = Logger.getLogger(JDBCParameter.class.getName());
    private final MetadataElement parent;
    private final Direction direction;
    private final int ordinalPosition;
    private final JDBCValue value;

    public JDBCParameter(MetadataElement parent, JDBCValue value, Direction direction, int ordinalPosition) {
        this.parent = parent;
        this.direction = direction;
        this.value = value;
        this.ordinalPosition = ordinalPosition;
    }

    @Override
    public String toString() {
        return "JDBCParameter[" + value + ", direction=" + getDirection() + ", position=" + getOrdinalPosition() + "]"; // NOI18N
    }

    @Override
    public MetadataElement getParent() {
        return parent;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    @Override
    public String getName() {
        return value.getName();
    }

    @Override
    public int getPrecision() {
        return value.getPrecision();
    }

    @Override
    public short getRadix() {
        return value.getRadix();
    }

    @Override
    public short getScale() {
        return value.getScale();
    }

    @Override
    public SQLType getType() {
        return value.getType();
    }

    @Override
    public String getTypeName() {
        return value.getTypeName();
    }

    @Override
    public int getLength() {
        return value.getLength();
    }

    @Override
    public Nullable getNullable() {
        return value.getNullable();
    }

    @Override
    public int getOrdinalPosition() {
        return ordinalPosition;
    }

}
