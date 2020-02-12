<#include "../macro/head.ftl"/>
<!DOCTYPE html>
<html>
<head>
    <@head title="大数据 | Hbase"/>
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
                Hbase
                <small>大数据</small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> 大数据</a></li>
                <li><a href="#">Hbase</a></li>
                <li class="active">大数据</li>
            </ol>
        </section>

        <!-- Main content -->
        <section class="content">
            <div class="row">
                <div class="col-xs-12">
                    <div class="box box-solid collapsed-box">
                        <div class="box-header">
                            <i class="fa fa-search"></i>

                            <h3 class="box-title">查询语句书写方式</h3>
                            <!-- tools box -->
                            <div class="pull-right box-tools">
                                <button type="button" class="btn btn-danger btn-sm" data-widget="collapse"><i
                                            class="fa fa-plus"></i>
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
                            查询语句
                            <pre>
查询必须填写表名和列簇名称(表名#列簇)，必须添加 limit ，否则查询所有数据太慢

查询所有列信息：
select * from can_ne#can limit 10

查询某些列信息：
select 3014,2205,did from can_ne#can limit 10

如果知道起始位置：
select * from can_ne#can where startRowKey = '00004baa3388ab01e3d153347e7fc163_20190101000000' limit 10

如果知道结束位置：
select * from can_ne#can where stopRowKey = '00004baa3388ab01e3d153347e7fc163_20191231000000' limit 10

如果知道起始与结束位置：
select * from can_ne#can where startRowKey = '00004baa3388ab01e3d153347e7fc163_20190101000000' and stopRowKey = '00004baa3388ab01e3d153347e7fc163_20191231000000' limit 10

只查询rowKey：
select rowKey from can_ne#can where startRowKey = '00004baa3388ab01e3d153347e7fc163_20190101000000' and stopRowKey = '00004baa3388ab01e3d153347e7fc163_20191231000000' limit 10

如果知道rowKey，只查一条：
select * from can_ne#can where startRowKey = '00004baa3388ab01e3d153347e7fc163_20191125065602' and stopRowKey='00004baa3388ab01e3d153347e7fc163_20191125065602'

如果不写order by，那么默认升序
降序查询需要注意，startRowKey的值必须比stopRowKey的值大，并且需要写 order by rowKey desc，例如：
select 2205,did,TIME,3014 from can_ne#can  where startRowKey = '00004baa3388ab01e3d153347e7fc163_20191231000000'  and stopRowKey = '00004baa3388ab01e3d153347e7fc163_20190101000000' order by rowKey desc limit 10

查询列不为空的写法：
select * from gateway#log where 2909 != '' limit 10

正则用法，以REG#开头，后面是正则表达式：
select * from command#command where rowKey = 'REG#test123456789_.*_3' order by rowKey desc limit 1

查询中，可以使用 and , or , like , not like , = , != , > , >= , < , <=
注意：查询结果中，包含startRowKey，也包含stopRowKey；如果列明有特殊字符，需要使用 ` 符号包裹
</pre>
                            统计语句
                            <pre>
select count(*) from can_ne#can where startRowKey = '00004baa3388ab01e3d153347e7fc163_20191203163532' and stopRowKey = '00004baa3388ab01e3d153347e7fc163_20191203183652'
</pre>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-xs-12">
                    <div class="box box-solid">
                        <div class="box-header">
                            <i class="fa fa-search"></i>

                            <h3 class="box-title">查询语句</h3>
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
                                  action="${request.contextPath}/hbase/ql" method="get">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="ql" class="col-md-2 col-lg-1 control-label">查询语句</label>
                                        <div class="col-md-10 col-lg-11">
                                            <textarea class="form-control" id="ql" name="ql" required>${ql!}</textarea>
                                        </div>
                                    </div>
                                </div>
                                <!-- /.box-body -->

                                <div class="box-footer">
                                    <button type="submit" class="btn btn-info pull-right">查询</button>
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
            onBefore: function (opts) {
                opts.queryParams = $('#searchForm').serializeObject();
            },
            onSuccess: function (opts, result) {
                if (result.rows.length > 0 && result.rows[result.rows.length - 1].rowKey) {
                    opts.queryRowKey.offsetRowKey = result.rows[result.rows.length - 1].rowKey;
                }
            },
            url: $('#searchForm').attr('action'),
            info: 'right'
        });
    });
</script>
</body>
</html>