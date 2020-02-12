if (typeof (Storage) !== 'undefined') {
    /**
     * 记录用户选择的 AdminLte 状态
     */
    $('.control-sidebar').on('click', 'input', function () {
        text = $.trim($(this).parent().text());

        /**
         * 记录切换菜单显示状态
         */
        if ('Toggle Sidebar'.indexOf(text) > -1) {
            if ($(this).is(":checked")) {
                localStorage.setItem('sidebar', 'true');
            } else {
                localStorage.removeItem('sidebar');
            }
        }
    });

    /**
     * 如果没有选择过主题，那么设置默认主题为 skin-black
     */
    if (!localStorage.getItem('skin')) {
        $('body').addClass('skin-black');
        localStorage.setItem('skin', 'skin-black');
    }

    /**
     * 如果用户选择了折叠菜单，那么刷新页面后，也要再次折叠菜单
     */
    if (localStorage.getItem('sidebar')) {
        $('[data-toggle="push-menu"]').pushMenu('toggle');//toggle左侧菜单
    }
}

/**
 * 通过url选中菜单
 */
$('#menu a[href="' + window.location.pathname + '"]').parents('li').addClass('active');

/**
 * 无权限时重新载入页面
 */
$.ajaxSetup({
    dataType: 'json'
    , complete: function (jqXHR, textStatus) {
        if (jqXHR.status == 403) {
            location.reload();
        }
    }
});
