/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.editor.parser.astnodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a declare statement
 * <pre>e.g.<pre> declare(ticks=1) { }
 * declare(ticks=2) { for ($x = 1; $x < 50; ++$x) {  }  }
 */
public class DeclareStatement extends Statement {

    private final ArrayList<Identifier> directiveNames = new ArrayList<>();
    private final ArrayList<Expression> directiveValues = new ArrayList<>();
    private Statement body;

    private DeclareStatement(int start, int end, Identifier[] directiveNames, Expression[] directiveValues, Statement action) {
        super(start, end);

        if (directiveNames == null || directiveValues == null || directiveNames.length != directiveValues.length) {
            throw new IllegalArgumentException();
        }
        this.directiveNames.addAll(Arrays.asList(directiveNames));
        this.directiveValues.addAll(Arrays.asList(directiveValues));
        this.body = action;
    }

    public DeclareStatement(int start, int end, List<Identifier> directiveNames, List<Expression> directiveValues, Statement action) {
        this(start, end,
                directiveNames  == null ? null : directiveNames.toArray(new Identifier[0]),
                directiveValues == null ? null : directiveValues.toArray(new Expression[0]),
                action);
    }

    /**
     * The list of directive names
     *
     * @return List of directive names
     */
    public List<Identifier> getDirectiveNames() {
        return directiveNames;
    }

    /**
     * The list of directive values
     *
     * @return List of directive values
     */
    public List<Expression> getDirectiveValues() {
        return directiveValues;
    }

    /**
     * The body of this declare statement
     *
     * @return body of this this declare statement
     */
    public Statement getBody() {
        return this.body;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
