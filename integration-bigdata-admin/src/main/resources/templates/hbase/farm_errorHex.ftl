<#include "../macro/head.ftl"/>
<!DOCTYPE html>
<html>
<head>
    <@head title="非道路 | 错误报文"/>
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
                错误报文
                <small>非道路</small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> 大数据</a></li>
                <li><a href="#">错误报文</a></li>
                <li class="active">非道路</li>
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
                                  action="${request.contextPath}/hbase/farm/errorHex" method="get">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="did" class="col-md-2 col-lg-1 control-label">设备编号</label>
                                        <div class="col-md-10 col-lg-11">
                                            <input class="form-control" id="did" name="did" placeholder="设备编号"
                                                   value="${did}">
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="did" class="col-md-2 col-lg-1 control-label">IP地址</label>
                                        <div class="col-md-10 col-lg-11">
                                            <input class="form-control" id="ip" name="ip" placeholder="IP地址"
                                                   value="${ip}">
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="type" class="col-md-2 col-lg-1 control-label">错误类型</label>
                                        <div class="col-md-10 col-lg-11">
                                            <select class="form-control" id="type" name="type">
                                                <option value="">选择错误类型</option>
                                                <option value="1" <#if type = '1'>selected</#if>>GPS时间错误</option>
                                                <option value="2" <#if type = '2'>selected</#if>>设备号解析出错</option>
                                                <option value="3" <#if type = '3'>selected</#if>>其他</option>
                                            </select>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="startTime" class="col-md-2 col-lg-1 control-label">开始时间</label>
                                        <div class="col-md-10 col-lg-11">
                                            <div class="input-group date" id="startTime">
                                                <input name="startTime" class="form-control" value="${startTime}"
                                                       placeholder="开始时间" required/>
                                                <span class="input-group-addon">
                                                <span class="glyphicon glyphicon-calendar"></span>
                                            </span>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="endTime" class="col-md-2 col-lg-1 control-label">结束时间</label>
                                        <div class="col-md-10 col-lg-11">
                                            <div class="input-group date" id="endTime">
                                                <input name="endTime" class="form-control" value="${endTime}"
                                                       placeholder="结束时间" required/>
                                                <span class="input-group-addon">
                                                <span class="glyphicon glyphicon-calendar"></span>
                                            </span>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="reverseScan" class="col-md-2 col-lg-1 control-label">查询顺序</label>
                                        <div class="col-md-10 col-lg-11">
                                            <select class="form-control" id="reverseScan" name="reverseScan">
                                                <option value="true" <#if reverseScan = true>selected</#if>>逆序查询
                                                </option>
                                                <option value="false" <#if reverseScan = false>selected</#if>>正序查询
                                                </option>
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
            columns: [{data: 'ip'}, {
                data: 'errType'
            }, {data: 'hex'}],
            onBefore: function (opts) {
                opts.queryParams = $('#searchForm').serializeObject();
            },
            onSuccess: function (opts, result) {
                if (result.rows.length > 0 && result.rows[result.rows.length - 1].rowKey) {
                    $('#' + opts.el).parent().parent().parent().show();
                    opts.queryRowKey.offsetRowKey = result.rows[result.rows.length - 1].rowKey;
                }
            },
            url: $('#searchForm').attr('action'),
            exportUrl: $('#searchForm').attr('action') + '/export',
            exportTitle: '非道路错误报文',
            frozenLeft: $.isMobile == true ? 0 : 1,
            export: true,
            info: 'right'
        });
    });

    $('#startTime').datetimepicker();
    $('#endTime').datetimepicker();
</script>
</body>
</html>