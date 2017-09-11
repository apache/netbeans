/* 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

function assertDisplay(id, value, msg) {
    var e = document.getElementById(id);
    assertEquals(value, e.style.display, msg)
}

assertEquals(3, tck.steps(false).length, "There are three step directives");
assertEquals('init', tck.steps(false)[0], "First step id");
assertEquals('info', tck.steps(false)[1], "Second step id");
assertEquals('summary', tck.steps(false)[2], "3rd step id");

assertEquals(3, tck.steps(true).length, "There are three localized data-step headers");
assertEquals('Initial Page', tck.steps(true)[0], "First step has own display name");
assertEquals('info', tck.steps(true)[1], "Second display name is taken from id string");
assertEquals('summary', tck.steps(true)[2], "3rd display name fallbacks to id attribute");

assertEquals('init', tck.current(), "Current step is 1st one");
assertDisplay('s0', '', "Display characteristics of 1st panel not mangled");
assertDisplay('s1', 'none', "Invisible s1");
assertDisplay('s2', 'none', "Invisible s2");

tck.next();
assertEquals('info', tck.current(), "Moved to 2nd panel");
assertDisplay('s0', 'none', "Invisible s0");
assertDisplay('s1', '', "Display characteristics of 2nd panel not mangled");
assertDisplay('s2', 'none', "Invisible s2");

