

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create TestDomain</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">TestDomain List</g:link></span>
        </div>
        <div class="body">
            <h1>Create TestDomain</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${testDomainInstance}">
            <div class="errors">
                <g:renderErrors bean="${testDomainInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="age">Age:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:testDomainInstance,field:'age','errors')}">
                                    <input type="text" id="age" name="age" value="${fieldValue(bean:testDomainInstance,field:'age')}" />
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name">Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:testDomainInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:testDomainInstance,field:'name')}"/>
                                </td>
                            </tr> 
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><input class="save" type="submit" value="Create" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
