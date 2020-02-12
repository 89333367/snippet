<!-- jQuery -->
<script src="https://cdn.bootcss.com/jquery/3.4.1/jquery.min.js"></script>

<!-- pace -->
<script src="https://cdn.bootcss.com/pace/1.0.2/pace.min.js"></script>

<!-- jquery ui -->
<script src="https://cdn.bootcss.com/jqueryui/1.12.1/jquery-ui.min.js"></script>
<!-- Resolve conflict in jQuery UI tooltip with Bootstrap tooltip -->
<script>$.widget.bridge('uibutton', $.ui.button);</script>

<!-- Bootstrap -->
<script src="https://cdn.bootcss.com/twitter-bootstrap/3.4.1/js/bootstrap.min.js"></script>

<!-- FastClick -->
<script src="https://cdn.bootcss.com/fastclick/1.0.6/fastclick.min.js"></script>

<!-- select2 -->
<script src="https://cdn.bootcss.com/select2/4.0.7-rc.0/js/select2.full.min.js"></script>
<script src="https://cdn.bootcss.com/select2/4.0.7-rc.0/js/i18n/zh-CN.js"></script>

<!-- moment -->
<script src="https://cdn.bootcss.com/moment.js/2.24.0/moment-with-locales.min.js"></script>

<!-- datetimepicker -->
<script src="https://cdn.bootcss.com/bootstrap-datetimepicker/4.17.47/js/bootstrap-datetimepicker.min.js"></script>
<script>
    $.extend($.fn.datetimepicker.defaults, {
        locale: 'zh-cn'
        , format: 'YYYY-MM-DD HH:mm:ss'
        , showTodayButton: true
        , showClear: true
        , showClose: true
    });
</script>

<!-- sweetalert2 -->
<script src="https://cdn.bootcss.com/limonte-sweetalert2/8.11.8/sweetalert2.all.min.js"></script>

<!-- lodash -->
<script src="https://cdn.bootcss.com/lodash.js/4.17.15/lodash.min.js"></script>

<!-- axios -->
<script src="https://cdn.bootcss.com/axios/0.19.0-beta.1/axios.min.js"></script>

<!-- iCheck -->
<script src="https://cdn.bootcss.com/iCheck/1.0.2/icheck.min.js"></script>

<!-- echarts -->
<script src="https://cdn.bootcss.com/echarts/4.2.1-rc1/echarts.min.js"></script>

<!-- AdminLTE App -->
<script src="https://cdn.bootcss.com/admin-lte/2.4.15/js/adminlte.min.js"></script>
<!-- AdminLTE for demo purposes -->
<script src="https://cdn.bootcss.com/admin-lte/2.4.15/js/demo.js"></script>

<!-- AdminLte status -->
<script src="${request.contextPath}/js/status.js?version=2019.6.17"></script>

<!-- 自己封装的工具 -->
<script src="${request.contextPath}/js/toolkit.js?version=2019.7.4"></script>

<!-- 数据表格插件 -->
<script src="${request.contextPath}/js/tablePlus.js?version=2019.8.5"></script>

<!-- 查询条件select2插件 -->
<script src="${request.contextPath}/js/selectPlus.js?version=2019.7.4"></script>

<!-- 图表插件 -->
<script src="${request.contextPath}/js/echartPlus.js?version=2019.7.4"></script>

<!-- 业务脚本 -->
<script src="${request.contextPath}/js/business.js?version=2020.1.2"></script>