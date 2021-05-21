<h1>Logged user</h1>

{if $status == 0}
	{foreach from=$error item=foo}
		- {$foo} <br/>
	{/foreach}

	<br/><form action="login.php" method="post">
		<table align="center">
            <tr>
                <td><b>Nick:</b></td>
                <td><input type="Text" name="login"><br/></td>
            </tr>
            <tr>
                <td><b>Password:</b></td>
                <td><input type="Password" name="password"><br/></td>
            </tr>
            <tr>
                <td align="center" colspan="2"><input type="Submit" value="Login" class="tlacitko"></td>
            </tr>
		</table>
	</form>
{elseif $status == 1}
	Successfully logged in ... Welcome!
{elseif $status == 2}
	Logged out!
{elseif $status == 5}
	Not activated yet!
	{foreach from=$error item=foo}
		- {$foo} <br/>
	{/foreach}
	<br/>
	<form action="login.php" method="post">
		<input type="hidden" value="{$u}" name="uid">
		<input type="Text" name="aktiv"><br/>
		<input type="Submit" value="activate" class="tlacitko">
	</form>
{elseif $status == 6}
	Successfully activated.
{/if}
