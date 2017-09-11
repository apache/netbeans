<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.

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

Contributor(s):
-->
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>
    <xsl:template match="/">
        <html>
            <style>
                body { background:#ffffee; }
                table { font-size:10.0pt; }
                h2 { font-size:11.5pt; color:#EE6D06; }
                th { background:#aaaadd; font-weight:bold; padding:0pt 10pt 0pt 10pt; }
                td { padding:0pt 10pt 0pt 10pt; }
                tr.start { background:#b5fdb7; }
                tr.start_measured { background:#b5fdb7; font-weight:bold; }
                tr.paint { background:#ffd0d0; }
                tr.paint_measured { background:#ffb4b4; font-weight:bold; }
                tr.user_action { background:#b5ffb0; }
                tr.user_action_measured { background:#9cff95; font-weight:bold; }
                tr.app_message { background:#eeeedd; }
                tr.app_message_ignored { background:#eeeedd;color:#909090; }
                tr.app_message_measured { background:#fffd6d; font-weight:bold; }
                tr.config_message { background:#ffffcc; }
                tr.trace_message { background:#d9d9ff; }
                tr.unknown {}
            </style>
            <head><title>Actions/Events list</title></head>
            <body><xsl:apply-templates select="action-tracking"/></body>
        </html>
    </xsl:template>
    
    <xsl:template match="action-tracking">
        <xsl:apply-templates select="event-list"/>
    </xsl:template>
    
    <xsl:template match="event-list">
        <h2><xsl:value-of select="@name"/></h2>
        <table border="1" cellspacing="1" cellpadding="0" width="100%">
            <tr>
                <th width="10%" align="right">Time since start</th>
                <th width="60%" align="left">Name</th>
                <th width="10%" align="left">Type</th>
                <th width="10%" align="center">Time</th>
            </tr>
            <xsl:apply-templates select="event"/>
        </table>
    </xsl:template>

    <xsl:template match="event">
        <xsl:variable name="cName">
            <xsl:value-of select="@type"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test='@measured'>
                <tr class='{$cName}_measured'>
                    <td align="right"><xsl:value-of select="@diff"/></td>
                    <td align="left"><xsl:value-of select="@name"/></td>
                    <td align="left"><xsl:value-of select="@type"/></td>
                    <td align="center"><xsl:value-of select="@time"/></td>
                </tr>
            </xsl:when>
            <xsl:when test='contains(@name,"IGNORED")'>
                <tr class='{$cName}_ignored'>
                    <td align="right"><xsl:value-of select="@diff"/></td>
                    <td align="left"><xsl:value-of select="@name"/></td>
                    <td align="left"><xsl:value-of select="@type"/></td>
                    <td align="center"><xsl:value-of select="@time"/></td>
                </tr>
            </xsl:when>
            <xsl:otherwise>
                <tr class='{$cName}'>
                    <td align="right"><xsl:value-of select="@diff"/></td>
                    <td align="left"><xsl:value-of select="@name"/></td>
                    <td align="left"><xsl:value-of select="@type"/></td>
                    <td align="center"><xsl:value-of select="@time"/></td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
