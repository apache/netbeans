<div>
	<ul>
		<?php if ($mode == 'edit'): ?>
			<li>
				<select name="something">
					<option value="<?php echo $s ?>"><?php echo $s; ?></option>
				</select>
			</li>
		<?php endif; ?>
			<li></li>
	</ul>
</div>

<?php print_r($stuff); ?>
