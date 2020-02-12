<#include "../macro/head.ftl"/>
<!DOCTYPE html>
<html>
<head>
    <@head title="新能源 | 行程数据"/>
</head>
<body class="hold-transition sidebar-mini skin-black">
<div class="wrapper">
    <#include "../macro/navbar.ftl"/>

    <#include "../macro/menu.ftl"/>

    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">
            <h1>
                行程数据
                <small>新能源</small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> 大数据</a></li>
                <li><a href="#">行程数据</a></li>
                <li class="active">新能源</li>
            </ol>
        </section>

        <!-- Main content -->
        <section class="content">
            <div class="row">
                <div class="col-xs-12">
                    <div class="box box-solid">
                        <div class="box-header">
                            <i class="fa fa-search"></i>

                            <h3 class="box-title">查询条件</h3>
                            <!-- tools box -->
                            <div class="pull-right box-tools">
                                <button type="button" class="btn btn-danger btn-sm" data-widget="collapse"><i
                                            class="fa fa-minus"></i>
                                </button>
                            </div>
                            <!-- /. tools -->
                        </div>
                        <!-- /.box-header -->
                        <div class="box-body no-padding">
                            <div style="width: 100%"></div>
                        </div>
                        <!-- /.box-body -->
                        <div class="box-footer">
                            <form id="searchForm" class="form-horizontal"
                                  action="${request.contextPath}/hbase/ne/journey" method="get">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="did" class="col-md-2 col-lg-1 control-label">设备编号</label>
                                        <div class="col-md-10 col-lg-11">
                                            <input class="form-control" id="did" name="did" placeholder="设备编号" required
                                                   value="${did}">
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="startTime" class="col-md-2 col-lg-1 control-label">时间</label>
                                        <div class="col-md-10 col-lg-11">
                                            <div class="input-group date" id="time">
                                                <input name="time" class="form-control" value="${time}"
                                                       placeholder="行程时间" required/>
                                                <span class="input-group-addon">
                                                <span class="glyphicon glyphicon-calendar"></span>
                                            </span>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="did" class="col-md-2 col-lg-1 control-label">重新生成</label>
                                        <div class="col-md-10 col-lg-11">
                                            <select class="form-control" id="refresh" name="refresh">
                                                <option value="true" <#if refresh = 'true'>selected</#if>>是</option>
                                                <option value="false" <#if refresh = 'false'>selected</#if>>否</option>
                                            </select>
                                        </div>
                                    </div>
                                </div>
                                <!-- /.box-body -->

                                <div class="box-footer">
                                    <button type="submit" class="btn btn-info pull-right">查询</button>

                                    <button id="clearForm" type="button" class="btn btn-default">清空条件</button>
                                </div>
                                <!-- /.box-footer -->
                            </form>
                        </div>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col-xs-12" style="display: none;">
                    <div class="box">
                        <div class="box-body">
                            <div id="resultTable" class="table table-striped"></div>
                        </div>
                        <!-- /.box-body -->
                    </div>
                    <!-- /.box -->
                </div>
            </div>
        </section>
        <!-- /.content -->
    </div>
    <!-- /.content-wrapper -->

    <#include "../macro/footer.ftl"/>
</div>
<#include "../macro/js.ftl"/>

<script>
    $(function () {
        UML.tablePlus({
            el: 'resultTable',
            columns: [
                {data: 'no', title: '行程段编号'}
                , {data: 'startTime', title: '行程开始时间'}
                , {data: 'endTime', title: '行程结束时间'}
                , {data: 'pointCount', title: '行程段有效定位点数目'}
                , {
                    data: 'action', title: '操作', render: function (value, row) {
                        return '<a href="${request.contextPath}/hbase/ne/journey_info?did=' + $('#did').val() + '&time=' + $("#time").find("input").val() + '&no=' + row.no + '" target="_blank">轨迹回放</a>';
                    }
                }
            ],
            onBefore: function (opts) {
                opts.queryParams = $('#searchForm').serializeObject();
            },
            onSuccess: function (opts, result) {
                if (result.rows.length > 0) {
                    $('#' + opts.el).parent().parent().parent().show();
                }
            },
            url: $('#searchForm').attr('action'),
            frozenRight: $.isMobile == true ? 0 : 1,
            export: true,
            info: 'right'
        });
    });

    $('#time').datetimepicker({
        locale: 'zh-cn'
        , format: 'YYYY-MM-DD'
        , showTodayButton: true
        , showClear: true
        , showClose: true
    });
</script>
</body>
</html>