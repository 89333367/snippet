/**
 * 为Array添加inArray方法
 * @param val
 * @returns {boolean}
 */
Array.prototype.inArray = function (val) {
    for (var i = 0; i < this.length; i++) {
        if (this[i] == val) {
            return true;
        }
    }
    return false;
};

/* 表单序列化对象 */
;(function ($) {
    $.fn.serializeObject = function (options) {
        var o = {};
        var a = $(this).serializeArray();
        $.each(a, function () {
            if (o[this.name]) {
                if (!o[this.name].push) {
                    o[this.name] = [o[this.name]];
                }
                o[this.name].push(this.value || '');
            } else {
                o[this.name] = this.value || '';
            }
        });
        return o;
    };
})(jQuery);

;(function ($) {
    /*获得url参数值*/
    $.getUrlParam = function (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURIComponent(r[2]);
        return null;
    };
})(jQuery);


;(function ($) {
    $.isMobile = /iphone|ios|android|ipod/i.test(navigator.userAgent.toLowerCase());
})(jQuery);

function uuid_S4() {
    return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
}

function get_uuid() {
    return (uuid_S4() + uuid_S4() + uuid_S4() + uuid_S4() + uuid_S4() + uuid_S4() + uuid_S4() + uuid_S4());
}