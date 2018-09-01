{capture}
    {if}
        {assign var=var value=value}
        {if true}
            {include file="anyfile.tpl"}
        {else}
            Nic se neincluduje.
        {/if}
    {/if}