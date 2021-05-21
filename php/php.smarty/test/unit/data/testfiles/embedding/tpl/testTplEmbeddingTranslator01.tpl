{if isset($_smarty_debug_output) and $_smarty_debug_output eq "html"}
    {$debug_output}
{else}
    <script type="text/javascript">
        // <![CDATA[
        if (self.name == '') {ldelim}
            var title = 'Console';
        {rdelim}
        else {ldelim}
            var title = 'Console_' + self.name;
        {rdelim}
        _smarty_console =
                window.open("", title.value, "width=680,height=600,resizable,scrollbars=yes");
        _smarty_console.document.write('{$debug_output|escape:'javascript'}');
        _smarty_console.document.close();
        // ]]>
    </script>
{/if}