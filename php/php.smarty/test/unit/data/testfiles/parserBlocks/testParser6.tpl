{include file="header.tpl"}
	<div id="righter_inner">
		<img class="zarazka" src="images/layout/zarazka.gif"><span class="header1">{$title}</span><br><br>
		Sending password.<br><br>
		<form method="POST" action="heslo.php">
		    <input type="text" name="email" class="textfield_longer">
		    <input type="submit" name="submit" id="forgot_password" value=" " class="forgot_password_out" onmouseover="setClass('forgot_password','forgot_password_over');" onmouseout="setClass('forgot_password','forgot_password_out');">
		</form>
	</div>
{include file="footer.tpl"}