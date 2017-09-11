<?xml version="1.0" encoding="UTF-8"?>
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
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:atom="http://www.w3.org/2005/Atom" xmlns:xalan="http://xml.apache.org/xslt" xmlns:xhtml="http://www.w3.org/1999/xhtml">
    <xsl:import href="apichanges.xsl" />
    <xsl:output method="xml" indent="yes" xalan:indent-amount="4"/>
    <xsl:param name="date"/>
    <xsl:param name="url-prefix" select="''"/>

    <xsl:template match="/" >
        <atom:feed>
            <atom:id>urn:netbeans-org:apichanges</atom:id>
            <atom:title>NetBeans API Changes</atom:title>
            <atom:author><atom:name>netbeans.org</atom:name></atom:author>
            <atom:link rel="alternate" type="text/html" href="apichanges.html"/>
            <atom:link rel="self" type="application/xml+atom" href="http://deadlock.netbeans.org/job/nbms-and-javadoc/javadoc/apichanges.atom"/>
            <atom:updated><xsl:value-of select="$date"/></atom:updated>
            <xsl:apply-templates select="//change">
                <xsl:sort data-type="number" order="descending" select="date/@year"/>
                <xsl:sort data-type="number" order="descending" select="date/@month"/>
                <xsl:sort data-type="number" order="descending" select="date/@day"/>
            </xsl:apply-templates>
        </atom:feed>
    </xsl:template>

    <xsl:template match="change">
        <atom:entry>
            <xsl:if test="@id"><atom:id>urn:netbeans-org:apichanges:<xsl:value-of select="@id"/></atom:id></xsl:if>
            <atom:title type="xhtml"><xhtml:div>[<xsl:value-of select="translate(substring-before(@url,'/'), '-', '.')"/>] <xsl:apply-templates select="summary/node()" mode="xhtmlify"/></xhtml:div></atom:title>
            <!-- XXX is the relative URL legal? -->
            <atom:link rel="alternate" type="text/html"><xsl:attribute name="href"><xsl:value-of select="$url-prefix"/><xsl:value-of select="@url"/>#<xsl:value-of select="@id"/></xsl:attribute></atom:link>
            <xsl:if test="date"><atom:updated><xsl:value-of select="date/@year"/>-<xsl:if test="string-length(date/@month) = 1">0</xsl:if><xsl:value-of select="date/@month"/>-<xsl:if test="string-length(date/@day) = 1">0</xsl:if><xsl:value-of select="date/@day"/>T00:00:00Z</atom:updated></xsl:if>
            <xsl:if test="author"><atom:author><atom:name><xsl:value-of select="author/@login"/></atom:name><atom:email><xsl:value-of select="author/@login"/>@netbeans.org</atom:email></atom:author></xsl:if>
            <atom:summary type="xhtml"><xhtml:div><xsl:apply-templates select="description" mode="xhtmlify"/></xhtml:div></atom:summary>
        </atom:entry>
    </xsl:template>

    <xsl:template match="*" mode="xhtmlify" priority="2">
      <xsl:element name="{local-name(.)}" namespace="http://www.w3.org/1999/xhtml">
        <xsl:apply-templates select="@*|node()"/>
      </xsl:element>
    </xsl:template>
    <xsl:template match="@*|node()" mode="xhtmlify" priority="1">
      <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
