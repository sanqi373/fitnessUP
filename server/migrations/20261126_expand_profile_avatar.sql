-- Existing installations stored only avatar URLs in this column.
ALTER TABLE `user` MODIFY COLUMN avatar MEDIUMTEXT;
