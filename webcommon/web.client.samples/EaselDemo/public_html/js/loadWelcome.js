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