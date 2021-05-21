<script type="text/javascript" src='js/ajax/chat_new_message.js'></script>
{capture name=chat_read}
    <div class="chat_main">
    	<div class="chat_talk" id="abc">
            {foreach from=$chat_text item=item name=foo}
                {$item.timeins|date_format:$config.time} {$item.login} - {$item.text}<br />
            {/foreach}
    	</div>
    	<div class="chat_users">
            Users in discussion: <br /><br />
            {foreach from=$chat_users item=item name=foo}
                {$item.login}
            {/foreach}
    	</div>
    	<div class="chat_form">
            <br />
            &nbsp;<input type="Text" size="50" maxlength="200" valign="middle" id="input_new">
            <img src="../images/chat/butt_odeslat_out.jpg" alt="Odeslat" onclick="chat_new_message(1)">
    	</div>
    </div>
{/capture}

{if ($room > 0)}
    {$smarty.capture.chat_read}
{else}
    <div align="center">
    {$error}<br /><br />
    <a href="chat.php?room=1">Enter chat...</a></div><br />
{/if}