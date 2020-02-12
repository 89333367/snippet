<#include "../macro/head.ftl"/>
<!DOCTYPE html>
<html>
<head>
    <@head title="非道路 | 时段统计"/>
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
                时段统计
                <small>非道路</small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> 大数据</a></li>
                <li><a href="#">时段统计</a></li>
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
                                  action="${request.contextPath}/hbase/farm/canCount" method="get">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="did" class="col-md-2 col-lg-1 control-label">设备编号</label>
                                        <div class="col-md-10 col-lg-11">
                                            <input class="form-control" id="did" name="did" placeholder="设备编号" required
                                                   value="${did}">
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="startTime" class="col-md-2 col-lg-1 control-label">统计日期</label>
                                        <div class="col-md-10 col-lg-11">
                                            <div class="input-group date" id="day">
                                                <input name="day" class="form-control"
                                                       value="${day!}"
                                                       placeholder="统计日期" required/>
                                                <span class="input-group-addon">
                                                <span class="glyphicon glyphicon-calendar"></span>
                                            </span>
                                            </div>
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
                            <div id="chart" style="height: 500px; width: 100%;"></div>
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
        let myChart = echarts.init(document.getElementById('chart'));
        let titles = [];
        let datas = [];
        for (let i = 1; i < 24; i++) {
            titles.push(i);
            datas.push(0);
        }
        let option = {
            tooltip: {
                trigger: 'axis',
                position: function (pt) {
                    return [pt[0], '10%'];
                }
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: titles
            },
            yAxis: {
                type: 'value',
                boundaryGap: [0, '100%']
            },
            series: [
                {
                    name: '数量',
                    type: 'line',
                    smooth: true,
                    symbol: 'none',
                    sampling: 'average',
                    itemStyle: {
                        color: 'rgb(255, 70, 131)'
                    },
                    areaStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                            offset: 0,
                            color: 'rgb(255, 158, 68)'
                        }, {
                            offset: 1,
                            color: 'rgb(255, 70, 131)'
                        }])
                    },
                    data: datas
                }
            ]
        };
        myChart.setOption(option);

        $.post($('#searchForm').attr('action'), $('#searchForm').serializeObject(), function (result) {
            option.xAxis.data = result.titles;
            option.series[0].data = result.datas;
            myChart.setOption(option);
        });
    });
    $('#day').datetimepicker({format: 'YYYY-MM-DD'});
</script>
</body>
</html>