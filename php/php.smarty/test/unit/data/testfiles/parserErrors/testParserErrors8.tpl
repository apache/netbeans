<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
    <head>
	<link rel="stylesheet" type="text/css" href="css/layout.css">
	<title>{$title}</title>
	<meta http-equiv="Content-Type" content="text/html;charset=utf-8">
	<meta http-equiv="Content-Language" content="CS">
        <style type="text/css">
                .a {
                    margin-left: 15px;
                    }

            </style>
    </head>
    <body>
	<div id="centering_div">

            <script type="text/javascript">

            </script>
            <script type="text/javascript">

            </script>
	    <div id="login_div">
		<form method="POST" action="login.php">
		    <table class="text" style="margin-left: 450px">
			<tr><td>
				login <input name="login" type="text" class="textfield text"/>
				heslo <input name="password" type="password" class="textfield text" />
				<input id ="loginform_submit" name="submit" type="submit" value=" " class="login_submit" onmouseout="loginform_submit.style.backgroundImage = 'url(images/layout/loginform_button_ok_out.gif)'" onmouseover="loginform_submit.style.backgroundImage = 'url(images/layout/loginform_button_ok_over.gif)';"/>
			    </td>
			    <td style="line-height: 1.2; padding-left: 10px;">
				<a href="registrace.php">zaregistrovat</a><br>
				<a href="heslo.php">zapomenuté heslo</a>
			    {else}
				login: <b>{$username}</b> &nbsp;&nbsp;&nbsp;&nbsp;<a href="login.php?logout=1">odhlasit</a>
			    {/if}
			    </td>
			</tr>
		    </table>
		</form>

	    </div>
	    <div id="header_image"></div>
	    <div id="subpage_header">
			{include file="submenu.tpl"}
			{include file="menu.tpl"}
	    </div>
	    <div id="subpage_inner">
		<div id="subpage_content">
		    <div id="subpage_lefter">
			<div id="lefter_inner">
			    <img class="zarazka" src="images/layout/zarazka.gif"><span class="header1">NOVINKY</span><br><br>
			    {foreach from=$newsRows item=item}
				<b>{$item[0]} {$item[1]}</b><br>{$item[2]}<br><br>
				    <div style="text-align: right;"><a href="pridat_novinku.php">Přidat novinku</a></div>
				{/if}
			</div>
		    </div>
		    <div id="subpage_middle"></div>
		    <div id="subpage_righter">
