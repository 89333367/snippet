<#include "../macro/head.ftl"/>
<!DOCTYPE html>
<html>
<head>
    <@head title="非道路 | 统计包数"/>
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
                统计包数
                <small>非道路</small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> 大数据</a></li>
                <li><a href="#">统计包数</a></li>
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
                                  action="${request.contextPath}/hbase/farm/packageCount" method="get">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="did" class="col-md-2 col-lg-1 control-label">设备编号</label>
                                        <div class="col-md-10 col-lg-11">
                                            <input class="form-control" id="did" name="did" placeholder="设备编号" required
                                                   value="${did}">
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
                                        <label for="actualPackageNum"
                                               class="col-md-2 col-lg-1 control-label">应发包数</label>
                                        <div class="col-md-10 col-lg-11">
                                            <input class="form-control" id="actualPackageNum" name="actualPackageNum"
                                                   placeholder="实际应发包数" type="number" min="1"
                                                   value="${actualPackageNum}">
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
                <div class="col-xs-12">
                    <div class="box">
                        <div class="box-body">
                            <div id="chart"></div>
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
        var PackageNum = $('#actualPackageNum').val()
        var bar = UML.echartPlus.create({
            el: 'chart',
            chartType: 'bar',
            url: $('#searchForm').attr('action'),
            onBefore: function (opts) {
                opts.queryParams = $('#searchForm').serializeObject();
            },
            onSuccess: function (data, config, chart) {
                if (data.rows && data.rows.length > 0) {
                    var xAxis = [];
                    var seriesData = [];
                    $.each(data.rows, function (index, item) {
                        xAxis.push(item.packageTime);
                        seriesData.push(item.packageNum);
                    });
                    chart.setOption({
                        xAxis: {
                            name: '时间(小时)',
                            data: xAxis
                        },
                        yAxis: {
                            name: '接收数量'
                        },
                        series: [{
                            label: {
                                show: true,
                                position: 'top',
                                formatter: function (params) {
                                    if ($.isNumeric(PackageNum)) {
                                        return params.value + '\n(' + Math.floor(params
                                            .value / PackageNum * 100) + '%)'
                                    }
                                    return params.value
                                }
                            },
                            data: seriesData
                        }],
                    })
                }
            }
        });
    });
    $('#startTime').datetimepicker();
    $('#endTime').datetimepicker();
</script>
</body>
</html>
