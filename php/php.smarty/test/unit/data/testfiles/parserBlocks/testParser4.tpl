{if true}
    {assign var=var value=value}
    {setfilter}
        {$var}
    {/setfilter}
{else}
    {include file="myfile.tpl"}
{/if}

