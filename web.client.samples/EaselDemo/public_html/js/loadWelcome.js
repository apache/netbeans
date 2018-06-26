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

document.write('<scr'+'ipt type="text/javascript" src="//ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.js" ></scr'+'ipt>');
var lang;

function getWelcome() {
    lang = getLanguage();
    if (!lang || !phrases[lang]) {
        lang = 'nl';
    }
    document.getElementById('welcome').innerHTML = phrases[lang];
    
}

 function getLanguage() {

         if (navigator.language) {
             lang = navigator.language;
         } else if (navigator.userLanguage) {
             lang = navigator.userLanguage;
         }

         if (lang && lang.length > 2) {
             lang = lang.substring(0, 2);
         }

         return lang;
     }

var phrases = { /* translation table for page */
    en: ["<h1>Welcome!</h1><p>NetBeans Project Easel is about combining state of the art HTML5/CSS3/JavaScript client development with Java/REST web services. Enabling the development and customization of flexabilble and performant industry standard client-side interfaces for multiple devices.</p><p><a href='about.html' class='btn btn-primary btn-large'>Learn more &raquo;</a></p>"],
    nl: ["<h1>Welkom!</h1><p>NetBeans Project Schildersezel gaat over een combinatie van state of the art HTML5/CSS3/JavaScript client ontwikkeling met Java / REST webservices. Het inschakelen van de ontwikkeling en aanpassing van flexabilble en performante industrie standaard client-side interfaces voor meerdere apparaten.</p><p><a href='about.html' class='btn btn-primary btn-large'> meer informatie &raquo;</a></p>"]
};