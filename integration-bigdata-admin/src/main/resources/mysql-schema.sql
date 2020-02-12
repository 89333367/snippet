CREATE TABLE IF NOT EXISTS `shiro_permission`  (
  `id` int(11) NOT NULL COMMENT '主键',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '许可名称',
  `uri` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NULL DEFAULT NULL COMMENT '资源URI',
  `pid` int(11) NULL DEFAULT NULL COMMENT '父id',
  `seq` int(11) NULL DEFAULT NULL COMMENT '顺序',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

CREATE TABLE IF NOT EXISTS `shiro_role`  (
  `id` int(11) NOT NULL COMMENT '主键',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '角色名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

CREATE TABLE IF NOT EXISTS `shiro_role_permission`  (
  `role_id` int(11) NOT NULL COMMENT '角色id',
  `permission_id` int(11) NOT NULL COMMENT '权限id',
  PRIMARY KEY (`role_id`, `permission_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '角色与权限关联表' ROW_FORMAT = Dynamic;

CREATE TABLE IF NOT EXISTS `shiro_subject`  (
  `id` int(11) NOT NULL COMMENT '主键',
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '邮箱',
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '用户名',
  `disabled` int(1) NOT NULL DEFAULT 1 COMMENT '0.未禁用1.禁用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '主体表' ROW_FORMAT = Dynamic;

CREATE TABLE IF NOT EXISTS `shiro_subject_role`  (
  `subject_id` int(11) NOT NULL COMMENT '主体id',
  `role_id` int(11) NOT NULL COMMENT '角色id',
  PRIMARY KEY (`subject_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_unicode_ci COMMENT = '主体与角色关联表' ROW_FORMAT = Dynamic;