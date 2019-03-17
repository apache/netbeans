/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

var BUNDLED_SERVERS_GROUP_NAME = "Servidores&nbsp;embutidos";
                                              
var NETBEANS_DOWNLOAD_BUNDLES_MSG = "Distribui\u00e7\u00f5es para baixar do NetBeans IDE";
var NETBEANS_DOWNLOAD_BUNDLES_COMMUNITY_MSG = "NetBeans IDE Download Bundles in community contributed languages";
var NETBEANS_PACKS_MSG = "Tecnologias&nbsp;suportadas\n";

var JDK_DOWNLOAD_LINK = "http://java.sun.com/javase/downloads/index.jsp";
var NBJDK_DOWNLOAD_LINK  = "http://www.oracle.com/technetwork/java/javase/downloads/jdk-netbeans-jsp-142931.html";


var JAVA_COM_LINK = "http://java.com";

// var JDK_NOTE_ALL = "O Java 6 e vers\u00f5es posteriores s\u00e3o necess\u00e1rios para instalar e executar pacotes do NetBeans PHP e C/C++. Voc\u00ea pode fazer download da <a href=\"{0}\">vers\u00e3o mais recente do Java em java.com</a>.<br><br>O JDK 6 e vers\u00f5es posteriores s\u00e3o necess\u00e1rios para instalar e executar o Java SE, o Java EE e todos os pacotes do NetBeans. Voc\u00ea pode fazer download do <a href=\"{1}\">JDK stand-alone</a> ou da vers\u00e3o mais recente do <a href=\"{2}\">JDK com o pacote do NetBeans IDE Java SE</a>.<br><br>";
var JDK_NOTE_ALL      = "HTML/JS, PHP and C/C++ NetBeans bundles include Java Runtime Environment and do not require a separate Java installation.<br><br>JDK 7 and later versions are required for installing and running the Java SE, Java EE and All NetBeans Bundles. You can download <a href=\"{1}\">standalone JDK</a> or download the latest <a href=\"{2}\">JDK with NetBeans IDE Java SE bundle</a>.<br><br>";
//var JDK_NOTE_MACOSX   = "JDK 5.0 is required for installing and running the NetBeans IDE.";
var JDK_NOTE_MACOSX = "";

var FREE_SIZE_MESSAGE = "{0}&nbsp;MB&nbsp;livre(s)";
var NOT_AVAILABLE_SIZE = "--";

var NOTE_PREFIX = "Nota:  ";
var NOTE_AND_SEP = "{0} e {1}";
var NOTE_COMMA_SEP = "{0}, {1}";

var SINGLE_NOT_AVAILABLE_ZIP = "{0} n\u00e3o est\u00e1 dispon\u00edvel em {1}";
var SINGLE_NOT_AVAILABLE_BUNDLE = "{0} n\u00e3o est\u00e1 dispon\u00edvel para {1}";
var MULTIPLE_NOT_AVAILABLE_ZIP = "{0} n\u00e3o est\u00e3o dispon\u00edveis em {1}";
var MULTIPLE_NOT_AVAILABLE_BUNDLE = "{0} n\u00e3o est\u00e3o dispon\u00edveis para {1}";

var ZIP_FILES_LIST_NAME             = "Arquivos Zip";
var MODULE_CLUSTERS_FILES_LIST_NAME = "Clusters de M\u00f3dulo";

var NOTE_SOLARIS = "Nota: Java ME est\u00e1 dispon\u00edvel apenas para Windows, Linux e Mac OS X.";
var NOTE_MACOSX = "Nota: JavaME est\u00e1 dispon\u00edvel apenas para Windows, Linux e Mac OS X."; 
var NOTE_ZIP = "Nota: GlassFish e Apache Tomcat est\u00e3o dispon\u00edveis apenas nos instaladores para plataformas espec\u00edficas.";
var NOTE_ALL = "Nota: Tecnologias em cinza n\u00e3o s\u00e3o suportadas para esta plataforma.";

var MORE_LANGUAGES    = "Mais Linguagens...";
var COMMUNITY_MESSAGE = "Download do NetBeans em outros idiomas (contribui\u00e7\u00f5es da comunidade)";
var COMMUNITY_CONTRIBUTED_SEP    = " -- Contribu\u00eddo pela Comunidade -- " ;
var SUN_SUPPORTED_SEP            = " -- Suportado pela Oracle -- " ;

var NETBEANS_DOWNLOAD_PAGE_TITLE       = "Download o NetBeans IDE {0}";
var NETBEANS_DOWNLOAD_PAGE_DESCRIPTION = "Download o NetBeans IDE {0}";


var NETBEANS_DOWNLOAD_HEADER	= "Download o NetBeans IDE {0}";
var DEVELOPMENT_TITLE		= "Desenvolvimento";
var ARCHIVE_TITLE		= "Arquivo";
var PYTHON_TITLE                = "Python&nbsp;EA";
var EMAIL_LABEL			= "Endere\u00e7o&nbsp;de&nbsp;email&nbsp;(opcional):&nbsp;";
var SUBSCRIBE_LABEL		= "Inscrever-se&nbsp;na&nbsp;newsletter:";
var MONTHLY_LABEL		= "Mensal&nbsp;&nbsp;&nbsp;&nbsp;";
var WEEKLY_LABEL		= "Semanal";
var CONTACT_LABEL		= "Permito&nbsp;me&nbsp;contatar&nbsp;neste&nbsp;email";
var LANGUAGE_LABEL		= "Idioma do IDE:";
var PLATFORM_LABEL		= "Plataforma:";
var PLUGIN_MANAGER_LABEL	= "Voc\u00ea pode adicionar ou remover pacotes depois utilizando o Gerenciador de Plugins do IDE (Ferramentas | Plugins).<br><br>";
var JAVATOOLSBUNDLE_LABEL	= "Voc\u00ea tamb\u00e9m pode baixar o NetBeans IDE como parte de <a href=\"http://java.sun.com/javaee/downloads/index.jsp\">Java EE 5 Ferramentas embutidas</a> ou <a href=\"http://download.netbeans.org/netbeans/6.1/mysql_bundle/\">MySQL GlassFish Embutidos</a>.";
var SOURCE_BINARY_LABEL		= "O c\u00f3digo-fonte e os bin\u00e1rios do NetBeans sem os servidores embutidos tamb\u00e9m est\u00e3o dispon\u00edveis em <a id=\"zip_link\" href=\"{0}\">formato de arquivo zip</a>.";
var BUILD_WIKI_LABEL		= "Veja tamb\u00e9m <a href=\"http://wiki.netbeans.org/wiki/view/WorkingWithNetBeansSources\">instru\u00e7\u00f5es de como construir o IDE a partir do c\u00f3digo-fonte</a> ou <a href=\"{0}\">instru\u00e7\u00f5es de instala\u00e7\u00e3o</a>.";
var PLATFORM_DEV_LABEL       = "Voc\u00ea pode come\u00e7ar a desenvolver aplica\u00e7\u00f5es baseadas na Plataforma NetBeans utilizando a IDE NetBeans para Java SE. Conhe\u00e7a mais sobre a <a href=\"{0}\">Plataforma NetBeans</a>.";
var PLATFORM_INFO_LINK       = "http://platform.netbeans.org/platform-get.html";

var LICENSE_NOTES_LINK = "http://www.netbeans.org/about/legal/product-licences.html";

var ARCHIVE_BUILDS_LINK = "http://www.netbeans.info/downloads/dev.php";
var DEVELOPMENT_BUILDS_LINK = "http://bits.netbeans.org/download/trunk/nightly/latest/";
var PYTHON_LINK             = "http://download.netbeans.org/netbeans/6.5/python/ea/";

// TRANSLATE NOTE: change download*.gif to download*_<locale>.gif
var DOWNLOAD_BUTTON_NORMAL    = "download.gif";
var DOWNLOAD_BUTTON_DISABLED  = "download_d.gif";
var DOWNLOAD_BUTTON_HIGHLIGHT = "download_h.gif";

// DO NOT TRANSLATE
var START_PAGE = "start.html";
var ZIP_PAGE   = "zip.html";
