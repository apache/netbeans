<?xml version="1.0" encoding="UTF-8" ?>
<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Oracle in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml"/>

    <!-- print out <api /> dependencies on all needed netbeans subprojects -->
    <xsl:template match="/" >
        <p>
            These modules are required in project.xml:
            <xsl:apply-templates select="//dependency" mode="comment" />
            <ul>
                <xsl:apply-templates select="//dependency" />
            </ul>
        </p>
    </xsl:template>

    <xsl:template match="dependency" >
        <li><api>
            <xsl:attribute name="type">import</xsl:attribute>
            <xsl:attribute name="group">java</xsl:attribute>
            <xsl:attribute name="category">
                <xsl:choose>
                    <xsl:when test="api-category" >
                        <xsl:value-of select="api-category/text()"/>
                    </xsl:when>
                    <xsl:otherwise>private</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:choose>
                    <xsl:when test="api-name" >
                        <xsl:apply-templates select="api-name/text()"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="code-name-base/text()"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <!-- Removed, since there is no guarantee Javadoc for that module is published:
            <xsl:attribute name="url">
                <xsl:text>@</xsl:text>
                <xsl:value-of select="translate(code-name-base/text(),'.','-')"/>
                <xsl:text>@/overview-summary.html</xsl:text>
            </xsl:attribute>
            -->

            <xsl:if test="compile-dependency">
                The module is needed for compilation. 
            </xsl:if>
            <xsl:if test="run-dependency">
                The module is used during runtime. 
                <xsl:if test="run-dependency/specification-version">
                    Specification version 
                    <xsl:value-of select="run-dependency/specification-version/node()" />
                    is required.
                </xsl:if>
            </xsl:if>
        </api></li>
    </xsl:template>
    
    <xsl:template match="dependency" mode="comment" >
        <xsl:comment>
        <xsl:text>&lt;li&gt;&lt;api type='import' group='java' category='</xsl:text>
        <xsl:choose>
            <xsl:when test="api-category" >
                <xsl:value-of select="api-category/text()"/>
            </xsl:when>
            <xsl:otherwise>private</xsl:otherwise>
        </xsl:choose>
        <xsl:text>' name='</xsl:text>
        <xsl:choose>
            <xsl:when test="api-name" >
                <xsl:apply-templates select="api-name/text()"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="code-name-base/text()"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>'&gt;</xsl:text>
        <!-- Removed, since there is no guarantee Javadoc for that module is published:
             url='@</xsl:text>
             <xsl:value-of select="translate(code-name-base/text(),'.','-')"/>
             <xsl:text>@/overview-summary.html'
        -->
        <xsl:if test="compile-dependency">
            The module is needed for compilation. 
        </xsl:if>
        <xsl:if test="run-dependency">
            The module is used during runtime. 
            <xsl:if test="run-dependency/specification-version">
                Specification version 
                <xsl:value-of select="run-dependency/specification-version/node()" />
                is required.
            </xsl:if>
        </xsl:if>
        <xsl:text>&lt;/api&gt;
&lt;/li&gt;            
</xsl:text>
        </xsl:comment>
    </xsl:template>
</xsl:stylesheet> 

