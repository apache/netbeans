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

<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"
        media-type="text/html" 
        doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN"
        doctype-system="DTD/xhtml1-strict.dtd"
        cdata-section-elements="script style"
        indent="yes"
        encoding="ISO-8859-1"/>
    
    <xsl:template match="/repository-tests">
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <title>Repository tests from <xsl:value-of select="@date"/></title> 
                <style>
                    *{font-family: tahoma; font-size: 12px; color: #333333;}
                    a:link, a:hover {color: #9B002C; text-decoration:none}
                    a:active, a:visited {color: #852641;}			
                    .red {color: #EE0000; }
                    .green {color: #22EE22; }
                </style>			
            </head>
            <body>
                <h3>Tests from <xsl:value-of select="@date"/></h3>
                <table> 
                    <xsl:apply-templates select="test"/>
                </table>
                <h3>Memory Working Set</h3>
                <table> 
                    <xsl:apply-templates select="test-mws"/>
                </table>
            </body>
        </html>        
    </xsl:template>
    
    <xsl:template match="test">
        <tr><td align="right" valign="top" nowrap=""><xsl:value-of select="@name"/>:</td><xsl:apply-templates select="result"/></tr>
        <tr><td align="right" valign="top">logs:</td><td><xsl:apply-templates select="log"/></td></tr>
    </xsl:template>
    
    <xsl:template match="test-mws">
        <tr><td nowrap="">
            <xsl:value-of select="project/text()"/> on <xsl:value-of select="memory/text()"/>
            <xsl:choose>
                <xsl:when test="repository/text()='true'"> with</xsl:when>
                <xsl:when test="repository/text()='false'"> w/o</xsl:when>
            </xsl:choose>:
        </td>
        <xsl:choose>
            <xsl:when test="result">
                <xsl:apply-templates select="result"/>
                <td>(<xsl:value-of select="round(parsetime div 1000)"/> s)</td>
            </xsl:when>
            <xsl:otherwise>
                <td colspan="2" class="red">crushed</td>
            </xsl:otherwise>
        </xsl:choose>
        <td><xsl:apply-templates select="log"/></td>
        </tr>
    </xsl:template>
    
    <xsl:template match="log">
        <a>
            <xsl:attribute name="href"><xsl:value-of select="text()"/></xsl:attribute>
            <xsl:value-of select="@name"/>
        </a><br/>
    </xsl:template>
    
    <xsl:template match="result">
        <td>
            <xsl:choose>
                <xsl:when test="text()='passed'">
                    <xsl:attribute name="class">green</xsl:attribute>
                    passed
                </xsl:when>
                <xsl:when test="text()='failed'">
                    <xsl:attribute name="class">red</xsl:attribute>
                    failed
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="class">red</xsl:attribute>
                    N/A
                </xsl:otherwise>		
            </xsl:choose>
        </td>
    </xsl:template>
    
    <xsl:template match="param">
        <tr><td align="right" valign="top"><xsl:value-of select="@name"/>:</td><td><xsl:value-of select="@value"/></td></tr>
    </xsl:template>
</xsl:stylesheet>
