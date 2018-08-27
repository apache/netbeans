<?php
if ($menu != false) {
    ?>
<span class="sumbenu_btns"> //wrong, should be as inside if
        <?php
        foreach ($menu as $k => $v) {
            ?>
    <a href="/jugglesk_v2/jugglesk/web/<?php $v['links']?>" ><?php $v['text']
?></a> //it's inside foreach, so wrong this one as well
            <?php
        }
        ?>
</span>
    <?php
}
?>