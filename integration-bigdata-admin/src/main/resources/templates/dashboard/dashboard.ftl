<#include "../macro/head.ftl"/>
<!DOCTYPE html>
<html>
<head>
    <@head title="大数据 | Dashboard"/>
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
                Dashboard
                <small>Control panel</small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> Home</a></li>
                <li class="active">Dashboard</li>
            </ol>
        </section>

        <!-- Main content -->
        <section class="content">
            <div class="row">
                <div class="col-xs-6 col-sm-6 col-md-4 col-lg-3">
                    <!-- small box -->
                    <div class="small-box bg-aqua">
                        <div class="inner">
                            <h3 id="ne_session_online">-</h3>
                            <p id="ne_session_online_desc">新能源 在线设备</p>
                        </div>
                        <div class="icon">
                            <i class="fa fa-heartbeat"></i>
                        </div>
                    </div>
                </div>
                <!-- ./col -->
                <div class="col-xs-6 col-sm-6 col-md-4 col-lg-3">
                    <!-- small box -->
                    <div class="small-box bg-red">
                        <div class="inner">
                            <h3 id="ne_session_total">-</h3>
                            <p>新能源 总设备数量</p>
                        </div>
                        <div class="icon">
                            <i class="fa fa-cubes"></i>
                        </div>
                    </div>
                </div>
                <!-- ./col -->

                <div class="col-xs-6 col-sm-6 col-md-4 col-lg-3">
                    <!-- small box -->
                    <div class="small-box bg-olive-active">
                        <div class="inner">
                            <h3 id="farm_session_online">-</h3>
                            <p id="farm_session_online_desc">农机 在线设备</p>
                        </div>
                        <div class="icon">
                            <i class="fa fa-heartbeat"></i>
                        </div>
                    </div>
                </div>
                <!-- ./col -->
                <div class="col-xs-6 col-sm-6 col-md-4 col-lg-3">
                    <!-- small box -->
                    <div class="small-box bg-maroon-active">
                        <div class="inner">
                            <h3 id="farm_session_total">-</h3>
                            <p>农机 总设备数量</p>
                        </div>
                        <div class="icon">
                            <i class="fa fa-cubes"></i>
                        </div>
                    </div>
                </div>
                <!-- ./col -->

                <div class="col-xs-6 col-sm-6 col-md-4 col-lg-3">
                    <!-- small box -->
                    <div class="small-box bg-green">
                        <div class="inner">
                            <h3 id="ne_alarm_today_size">-</h3>
                            <p>新能源 当日报警数</p>
                        </div>
                        <div class="icon">
                            <i class="fa fa-bell"></i>
                        </div>
                    </div>
                </div>
                <!-- ./col -->

                <!-- Small boxes (Stat box) -->
                <div class="col-xs-6 col-sm-6 col-md-4 col-lg-3">
                    <!-- small box -->
                    <div class="small-box bg-aqua">
                        <div class="inner">
                            <h3 id="interface_log_size">-</h3>
                            <p>接口日志未入库缓存</p>
                        </div>
                        <div class="icon">
                            <i class="fa fa-pinterest-p"></i>
                        </div>
                    </div>
                </div>
                <!-- ./col -->


                <div class="col-xs-6 col-sm-6 col-md-4 col-lg-3">
                    <!-- small box -->
                    <div class="small-box bg-blue">
                        <div class="inner">
                            <h3 id="interface_token_size">-</h3>
                            <p>密钥数量</p>
                        </div>
                        <div class="icon">
                            <i class="fa fa-key"></i>
                        </div>
                    </div>
                </div>
                <!-- ./col -->
                <div class="col-xs-6 col-sm-6 col-md-4 col-lg-3">
                    <!-- small box -->
                    <div class="small-box bg-maroon">
                        <div class="inner">
                            <h3 id="redis_cluster_ping">-</h3>
                            <p>Redis集群状态</p>
                        </div>
                        <div class="icon">
                            <i class="fa fa-connectdevelop"></i>
                        </div>
                    </div>
                </div>
                <!-- ./col -->
            </div>

            <div class="row">
                <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12" style="display: none;">
                    <div class="box box-info">
                        <div class="box-header with-border">
                            <h3 class="box-title">Spark 任务执行状态</h3>
                        </div>
                        <div class="box-body">
                            <div id="spark_application_status"
                                 class="table table-striped"></div>
                        </div>
                        <!-- /.box-body -->
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12" style="display: none;">
                    <div class="box box-info">
                        <div class="box-header with-border">
                            <h3 class="box-title">Azkaban 任务执行状态</h3>
                        </div>
                        <div class="box-body">
                            <div id="azkaban_executor"
                                 class="table table-striped"></div>
                        </div>
                        <!-- /.box-body -->
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12 col-sm-12 col-md-6 col-lg-4" style="display: none;">
                    <div class="box box-success">
                        <div class="box-header with-border">
                            <h3 class="box-title">新能源 Last Data</h3>
                        </div>
                        <div class="box-body">
                            <div id="ne_last_data" class="table table-striped"></div>
                        </div>
                        <!-- /.box-body -->
                    </div>
                </div>

                <div class="col-xs-12 col-sm-12 col-md-6 col-lg-4" style="display: none;">
                    <div class="box box-success">
                        <div class="box-header with-border">
                            <h3 class="box-title">新能源 Last Alarm</h3>
                        </div>
                        <div class="box-body">
                            <div id="ne_last_alarm"
                                 class="table table-striped"></div>
                        </div>
                        <!-- /.box-body -->
                    </div>
                </div>

                <div class="col-xs-12 col-sm-12 col-md-6 col-lg-4" style="display: none;">
                    <div class="box box-success">
                        <div class="box-header with-border">
                            <h3 class="box-title">新能源 Now Run</h3>
                        </div>
                        <div class="box-body">
                            <div id="ne_now_run"
                                 class="table table-striped"></div>
                        </div>
                        <!-- /.box-body -->
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12 col-sm-12 col-md-6 col-lg-4" style="display: none;">
                    <div class="box box-success">
                        <div class="box-header with-border">
                            <h3 class="box-title">新能源 Now Charging</h3>
                        </div>
                        <div class="box-body">
                            <div id="ne_now_charging"
                                 class="table table-striped"></div>
                        </div>
                        <!-- /.box-body -->
                    </div>
                </div>

                <div class="col-xs-12 col-sm-12 col-md-6 col-lg-4" style="display: none;">
                    <div class="box box-success">
                        <div class="box-header with-border">
                            <h3 class="box-title">新能源 Last Command</h3>
                        </div>
                        <div class="box-body">
                            <div id="ne_last_command"
                                 class="table table-striped"></div>
                        </div>
                        <!-- /.box-body -->
                    </div>
                </div>

                <div class="col-xs-12 col-sm-12 col-md-6 col-lg-4" style="display: none;">
                    <div class="box box-info">
                        <div class="box-header with-border">
                            <h3 class="box-title">农机 Last Data</h3>
                        </div>
                        <div class="box-body">
                            <div id="farm_last_data"
                                 class="table table-striped"></div>
                        </div>
                        <!-- /.box-body -->
                    </div>
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
            el: 'spark_application_status'
            , columns: [
                {data: 'name', title: '任务名称'}
                , {data: 'startTime', title: '任务开始时间'}
                , {
                    data: 'processingTime', title: '每批次执行时间', render: function (value, row) {
                        var red = false;
                        if (value) {
                            var processingTime = value.split(' ');
                            var processingTimeUnit = processingTime[2];
                            var processingTimeNumber = Number(processingTime[1]);

                            var windowPeriod = row.windowPeriod.split(' ');
                            var windowPeriodUnit = windowPeriod[1];
                            var windowPeriodNumber = Number(windowPeriod[0]);

                            if (processingTimeUnit == windowPeriodUnit) {//相同单位
                                if (processingTimeNumber >= windowPeriodNumber) {
                                    red = true;
                                }
                            } else {//不同单位
                                var unitArr = ['ms', 'second', 'seconds', 'minute', 'minutes', 'hour', 'hours', 'day', 'days'];
                                if (unitArr.indexOf(processingTimeUnit) > unitArr.indexOf(windowPeriodUnit)) {
                                    red = true;
                                }
                            }
                        }
                        if (red) {
                            return '<span class="bg-red-active">' + value + '</span>';
                        }
                        return value;
                    }
                }
                , {data: 'windowPeriod', title: '执行周期'}
                , {data: 'schedulingDelay', title: '当前延迟'}
                , {data: 'totalDelay', title: '总延迟'}
                , {
                    data: 'attemptNumber', title: '尝试次数', render: function (value, row) {
                        if (value && value != '1') {
                            return '<span class="bg-red-active">' + value + '</span>';
                        }
                        return value;
                    }
                }
                , {
                    data: 'failed', title: '任务失败', render: function (value, row) {
                        if (value && value != '0') {
                            return '<span class="bg-red-active">' + value + '</span>';
                        }
                        return value;
                    }
                }
                , {
                    data: 'dead', title: '进程失败', render: function (value, row) {
                        if (value && value != '0') {
                            return '<span class="bg-red-active">' + value + '</span>';
                        }
                        return value;
                    }
                }
                , {data: 'input', title: '每秒数据量'}
                , {data: 'cpu', title: '分配的CPU'}
                , {data: 'memory', title: '分配的内存'}
                , {data: 'containers', title: '分配的容器'}
                , {data: 'id', title: '任务ID'}
                , {
                    data: 'url', title: 'UI', render: function (value, row) {
                        return '<a href="' + value + '" target="_blank">详细</a>';
                    }
                }
            ]
            , url: '${request.contextPath}/spark/app/status'
            , showLoading: false
            , height: 'auto'
            , info: 'right'
            , onAfter: function (opts) {
                window.setTimeout(opts.refresh, 1000 * 1);
            }
            , onSuccess: function (opts, result) {
                if (result.rows.length < 1) {
                    $('#' + opts.el).parent().parent().parent().hide();
                } else {
                    $('#' + opts.el).parent().parent().parent().show();
                }
            }
            , frozenLeft: $.isMobile == true ? 0 : 1
            , frozenRight: $.isMobile == true ? 0 : 1
        });

        UML.tablePlus({
            el: 'azkaban_executor',
            columns: [
                {
                    data: 'c1',
                    title: 'Execution Id'
                },
                {data: 'c2', title: 'Executor Id'},
                {
                    data: 'c3',
                    title: 'Flow'
                },
                {data: 'c4', title: 'Project'},
                {
                    data: 'c5',
                    title: 'User'
                },
                {data: 'c6', title: 'Proxy'},
                {data: 'c7', title: '开始时间'},
                {
                    data: 'c9',
                    title: 'Elapsed'
                },
                {
                    data: 'url', title: '操作', render: function (value, row) {
                        return '<a href="' + value + '" target="_blank">详细</a>';
                    }
                }
            ],
            url: '${request.contextPath}/azkaban/executor',
            showLoading: false,
            height: 'auto',
            info: 'right',
            onAfter: function (opts) {
                window.setTimeout(opts.refresh, 1000 * 1);
            },
            onSuccess: function (opts, result) {
                if (result.rows.length < 1) {
                    $('#' + opts.el).parent().parent().parent().hide();
                } else {
                    $('#' + opts.el).parent().parent().parent().show();
                }
            },
            frozenRight: $.isMobile == true ? 0 : 1
        });

        UML.tablePlus({
            el: 'ne_last_data'
            , columns: [
                {
                    data: 'did', title: '设备编号', render: function (value, row) {
                        return '<a target="_blank" href="${request.contextPath}/redis/ne/realtime?did=' + value + '">' + value + '</a>';
                    }
                }
                , {data: 'TIME', title: '网关接收时间'}
            ]
            , url: '${request.contextPath}/redis/ne/session/last'
            , showLoading: false
            , height: 'auto'
            , info: false
            , onAfter: function (opts) {
                window.setTimeout(opts.refresh, 1000 * 10);
            }
            , onSuccess: function (opts, result) {
                if (result.rows.length < 1) {
                    $('#' + opts.el).parent().parent().parent().hide();
                } else {
                    $('#' + opts.el).parent().parent().parent().show();
                }
            }
        });
        UML.tablePlus({
            el: 'ne_last_alarm'
            , columns: [
                {
                    data: 'did', title: '设备编号', render: function (value, row) {
                        return '<a target="_blank" href="${request.contextPath}/hbase/ne/can?did=' + value + '">' + value + '</a>';
                    }
                }
                , {data: 'TIME', title: '报警时间'}
            ]
            , url: '${request.contextPath}/redis/ne/alarm/last'
            , showLoading: false
            , height: 'auto'
            , info: false
            , onAfter: function (opts) {
                window.setTimeout(opts.refresh, 1000 * 60);
            }
            , onSuccess: function (opts, result) {
                if (result.rows.length < 1) {
                    $('#' + opts.el).parent().parent().parent().hide();
                } else {
                    $('#' + opts.el).parent().parent().parent().show();
                }
            }
        });
        UML.tablePlus({
            el: 'ne_now_run'
            , columns: [
                {
                    data: 'did', title: '设备编号', render: function (value, row) {
                        return '<a target="_blank" href="${request.contextPath}/redis/ne/realtime?did=' + value + '">' + value + '</a>';
                    }
                }
                , {data: 'TIME', title: '运行时间'}
            ]
            , url: '${request.contextPath}/redis/ne/now/run'
            , showLoading: false
            , height: 'auto'
            , info: false
            , onAfter: function (opts) {
                window.setTimeout(opts.refresh, 1000 * 60);
            }
            , onSuccess: function (opts, result) {
                if (result.rows.length < 1) {
                    $('#' + opts.el).parent().parent().parent().hide();
                } else {
                    $('#' + opts.el).parent().parent().parent().show();
                }
            }
        });
        UML.tablePlus({
            el: 'ne_now_charging'
            , columns: [
                {
                    data: 'did', title: '设备编号', render: function (value, row) {
                        return '<a target="_blank" href="${request.contextPath}/redis/ne/realtime?did=' + value + '">' + value + '</a>';
                    }
                }
                , {data: 'TIME', title: '充电时间'}
            ]
            , url: '${request.contextPath}/redis/ne/now/charging'
            , showLoading: false
            , height: 'auto'
            , info: false
            , onAfter: function (opts) {
                window.setTimeout(opts.refresh, 1000 * 60);
            }
            , onSuccess: function (opts, result) {
                if (result.rows.length < 1) {
                    $('#' + opts.el).parent().parent().parent().hide();
                } else {
                    $('#' + opts.el).parent().parent().parent().show();
                }
            }
        });
        UML.tablePlus({
            el: 'ne_last_command'
            , columns: [
                {
                    data: 'key', title: '唯一标识', render: function (value, row) {
                        var did = value.substring(value.indexOf('ne:command:') + 11, value.lastIndexOf(':'));
                        return '<a target="_blank" href="${request.contextPath}/hbase/ne/command?did=' + did + '">' + value + '</a>';
                    }
                }
                , {data: 'value', title: '指令内容'}
            ]
            , url: '${request.contextPath}/redis/ne/command/last'
            , showLoading: false
            , height: 320
            , info: false
            , onSuccess: function (opts, result) {
                if (result.rows.length < 1) {
                    $('#' + opts.el).parent().parent().parent().hide();
                } else {
                    $('#' + opts.el).parent().parent().parent().show();
                }
            }
            , onAfter: function (opts) {
                window.setTimeout(opts.refresh, 1000 * 5);
            }
        });
        UML.tablePlus({
            el: 'farm_last_data'
            , columns: [
                {
                    data: 'did', title: '设备编号', render: function (value, row) {
                        return '<a target="_blank" href="${request.contextPath}/redis/farm/realtime?did=' + value + '">' + value + '</a>';
                    }
                }
                , {data: 'TIME', title: '网关接收时间'}
            ]
            , url: '${request.contextPath}/redis/farm/session/last'
            , showLoading: false
            , height: 'auto'
            , info: false
            , onSuccess: function (opts, result) {
                if (result.rows.length < 1) {
                    $('#' + opts.el).parent().parent().parent().hide();
                } else {
                    $('#' + opts.el).parent().parent().parent().show();
                }
            }
            , onAfter: function (opts) {
                window.setTimeout(opts.refresh, 1000 * 10);
            }
        });
    });

    window.onload = function () {
        $.post('${request.contextPath}/redis/ne/session/total', {}, function (result) {
            $('#ne_session_total').html(result);
        }, 'text');

        $.post('${request.contextPath}/redis/farm/session/total', {}, function (result) {
            $('#farm_session_total').html(result);
        }, 'text');

        $.post('${request.contextPath}/redis/interface/token/size', {}, function (result) {
            $('#interface_token_size').html(result);
        }, 'text');

        var redisClusterPingFun = function () {
            $.post('${request.contextPath}/redis/cluster/ping', {}, function (result) {
                $('#redis_cluster_ping').html(result);

                window.setTimeout(redisClusterPingFun, 1000);
            }, 'text');
        };
        redisClusterPingFun();

        var interfaceLogSizeFun = function () {
            $.post('${request.contextPath}/redis/interface/log/size', {}, function (result) {
                $('#interface_log_size').html(result);

                window.setTimeout(interfaceLogSizeFun, 1000 * 60);
            }, 'text');
        };
        interfaceLogSizeFun();

        var second = 30;
        var neSessionOnlineFun = function () {
            $.post('${request.contextPath}/redis/ne/session/online', {second: second}, function (result) {
                $('#ne_session_online').html(result);

                window.setTimeout(neSessionOnlineFun, 1000 * second);
            }, 'text');
        };
        neSessionOnlineFun();

        var farmSessionOnlineFun = function () {
            $.post('${request.contextPath}/redis/farm/session/online', {second: second}, function (result) {
                $('#farm_session_online').html(result);

                window.setTimeout(farmSessionOnlineFun, 1000 * second);
            }, 'text');
        };
        farmSessionOnlineFun();

        var neAlarmTodaySizeFun = function () {
            $.post('${request.contextPath}/redis/ne/alarm/today/size', {}, function (result) {
                $('#ne_alarm_today_size').html(result);

                window.setTimeout(neAlarmTodaySizeFun, 1000 * 60 * 5);
            }, 'text');
        };
        neAlarmTodaySizeFun();
    };
</script>
</body>
</html>