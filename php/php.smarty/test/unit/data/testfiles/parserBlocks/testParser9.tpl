{capture name=href_informace_left}
	<a href="pokoj.php?action=edit_pokoje_info">
		<div class="href_informace_left"></div>
	</a>
{/capture}

{capture name=href_vybava_left}
	<a href="pokoj.php?action=edit_pokoje_vybava">
		<div class="href_vybava_left"></div>
	</a>
{/capture}

{capture name=href_odkazy_left}
	<a href="pokoj.php?action=edit_pokoje_odkazy">
		<div class="href_odkazy_left"></div>
	</a>
{/capture}

{capture name=href_vzkazy_left}
	<a href="pokoj.php?action=edit_pokoje_vzkazy">
		<div class="href_vzkazy_left"></div>
	</a>
{/capture}

{capture name=href_vzhled_left}.
	<a href="pokoj.php?action=edit_pokoje_vzhled">
		<div class="href_vzhled_left"></div>
	</a>
{/capture}

{capture name=href_informace_right}
	<a href="pokoj.php?action=edit_pokoje_info">
		<div class="href_informace_right"></div>
	</a>
{/capture}

{capture name=href_vybava_right}
	<a href="pokoj.php?action=edit_pokoje_vybava">
		<div class="href_vybava_right"></div>
	</a>
{/capture}

{capture name=href_odkazy_right}
	<a href="pokoj.php?action=edit_pokoje_odkazy">
		<div class="href_odkazy_right"></div>
	</a>
{/capture}

{capture name=href_vzkazy_right}
	<a href="pokoj.php?action=edit_pokoje_vzkazy">
		<div class="href_vzkazy_right"></div>
	</a>
{/capture}

{capture name=href_vzhled_right}
	<a href="pokoj.php?action=edit_pokoje_vzhled">
		<div class="href_vzhled_right"></div>
	</a>
{/capture}

{capture name=nastaveni_header}
	<div id="umisteni_nastaveni">
		{if ($smarty.request.action eq "edit_pokoje_info")}
			<img src="images/pokoj/nastaveni/header1.png" alt="">
			<div id="body_informace">
				{$smarty.capture.href_informace_right}
				{$smarty.capture.href_vybava_right}
				{$smarty.capture.href_odkazy_right}
				{$smarty.capture.href_vzkazy_right}
				{$smarty.capture.href_vzhled_right}
		{elseif ($smarty.request.action eq "edit_pokoje_vybava")}
			<img src="images/pokoj/nastaveni/header2.png" alt="">
			<div id="body_vybava">
				{$smarty.capture.href_informace_left}
				{$smarty.capture.href_vybava_right}
				{$smarty.capture.href_odkazy_right}
				{$smarty.capture.href_vzkazy_right}
				{$smarty.capture.href_vzhled_right}
		{elseif ($smarty.request.action eq "edit_pokoje_odkazy")}
			<img src="images/pokoj/nastaveni/header2.png" alt="">
			<div id="body_odkazy">
				{$smarty.capture.href_informace_left}
				{$smarty.capture.href_vybava_left}
				{$smarty.capture.href_odkazy_right}
				{$smarty.capture.href_vzkazy_right}
				{$smarty.capture.href_vzhled_right}
		{elseif ($smarty.request.action eq "edit_pokoje_vzkazy")}
			<img src="images/pokoj/nastaveni/header2.png" alt="">
			<div id="body_vzkazy">
				{$smarty.capture.href_informace_left}
				{$smarty.capture.href_vybava_left}
				{$smarty.capture.href_odkazy_left}
				{$smarty.capture.href_vzkazy_right}
				{$smarty.capture.href_vzhled_right}
		{elseif ($smarty.request.action eq "edit_pokoje_vzhled")}
			<img src="images/pokoj/nastaveni/header2.png" alt="">
			<div id="body_vzhled">
				{$smarty.capture.href_informace_left}
				{$smarty.capture.href_vybava_left}
				{$smarty.capture.href_odkazy_left}
				{$smarty.capture.href_vzkazy_left}
				{$smarty.capture.href_vzhled_right}
		{/if}
{/capture}