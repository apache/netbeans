<script type="text/javascript">/*<![CDATA[*/
    onlineplus.apo.kassenart = '{$custArt}';
    onlineplus.apo.verordnung = {$verordnungId};
    {if isset($previousVoId)}
        onlineplus.apo.previousVoId = {$previousVoId};
    {else}
        onlineplus.apo.previousVoId = null;
    {/if}

    {if isset($nextVoId)}
        onlineplus.apo.nextVoId = {$nextVoId};
    {else}
        onlineplus.apo.nextVoId = null;
    {/if}
/*]]>*/
</script>
