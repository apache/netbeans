<div id="page_background">
	<div id="outer_container">
		<div id="outer_header"></div>
		<div id="outer_body">
			<div id="header">Potvrdit akci</div>
			<div id="inner_container">
				<table cellspacing="0" cellpadding="0" width="472">
					<tr><td id="inner_header"></td></tr>
					<tr><td id="inner_body">
						<div class="confirmation_text">
							{if isset($conf_data.text)}
								{eval var=$conf_data.text}
							{else}
								{eval var=$form.text}
							{/if}
						</div>
					</td></tr>
					<tr><td id="inner_footer"></td></tr>
				</table>
			</div>
		</div>
		<div id="outer_footer">
			<div id="buttons">
				{if isset($conf_data.action)}
					<form action="{$conf_data.action}" method="{$conf_data.method}">
				{else}
					{if $form.action == "GET_URL"}
						{if $smarty.get.url == ""}
							<form action="index.php" method="{$form.method}">
						{else}
							<form action="{$smarty.get.url}" method="{$form.method}">
						{/if}
					{else}
						<form action="{$form.action}" method="{$form.method}">
					{/if}
				{/if}
				{if ($form.type == 1) || ($form.type == 3)}
					<input id="ok" type="submit" name="ok" value=" " class="butt_ok_out" onmouseout="SetClass('ok', 'butt_ok_out')" onmouseover="SetClass('ok', 'butt_ok_over')">
				{elseif ($form.type == 2)}
					{if isset($conf_data.no) && isset($conf_data.yes)}
						<input id="ano" type="submit" name="{$conf_data.yes}" value=" " class="butt_ano_out" onmouseout="SetClass('ano', 'butt_ano_out')" onmouseover="SetClass('ano', 'butt_ano_over')">
						<input id="ne" type="submit" name="{$conf_data.no}" value=" " class="butt_ne_out" onmouseout="SetClass('ne', 'butt_ne_out')" onmouseover="SetClass('ne', 'butt_ne_over')">
					{else}
						<input id="ano" type="submit" name="{$form.yes}" value=" " class="butt_ano_out" onmouseout="SetClass('ano', 'butt_ano_out')" onmouseover="SetClass('ano', 'butt_ano_over')">
						<input id="ne" type="submit" name="{$form.no}" value=" " class="butt_ne_out" onmouseout="SetClass('ne', 'butt_ne_out')" onmouseover="SetClass('ne', 'butt_ne_over')">
					{/if}
				{/if}
					{if isset($conf_data.hiddens)}
						{foreach from=$conf_data.hiddens item=item}
							<input type="hidden" name="{eval var=$item.name}" value="{eval var=$item.value}">
						{/foreach}
					{else}
						{foreach from=$form.hiddens item=item}
							<input type="hidden" name="{eval var=$item.name}" value="{eval var=$item.value}">
						{/foreach}
					{/if}
				</form>
			</div>
		</div>
	</div>
	<div id="bottom_space"></div>
</div>