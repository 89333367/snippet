<#include "../macro/head.ftl"/>
<!DOCTYPE html>
<html>
<head>
    <@head title="新能源 | 最后数据导出"/>
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
                最后数据导出
                <small>新能源</small>
            </h1>
            <ol class="breadcrumb">
                <li><a href="#"><i class="fa fa-dashboard"></i> 大数据</a></li>
                <li><a href="#">最后数据导出</a></li>
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

                            <h3 class="box-title">
                                查询条件
                            </h3>
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
                                  action="${request.contextPath}/redis/ne/batch/realtime" method="post"
                                  enctype="multipart/form-data">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="fileInput" class="col-md-2 col-lg-1 control-label">模板下载</label>
                                        <div class="col-md-10 col-lg-11">
                                            <a href="${request.contextPath}/excelTemplate/didTemplate" target="_blank">设备编号模板下载</a>
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="fileInput" class="col-md-2 col-lg-1 control-label">设备信息</label>
                                        <div class="col-md-10 col-lg-11">
                                            <input class="form-control" id="fileInput" onclick="$('#file').click();"
                                                   required placeholder="上传设备信息">
                                            <input type="file" name="file" id="file"
                                                   accept="application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                                   onchange="$('#fileInput').val($('#file').val());"
                                                   style="display: none;">
                                        </div>
                                    </div>

                                    <div class="form-group">
                                        <label for="columns" class="col-md-2 col-lg-1 control-label">导出某列</label>
                                        <div class="col-md-10 col-lg-11">
                                            <input class="form-control" id="columns" name="columns"
                                                   placeholder="选择过滤列"
                                                   value="${columns}">
                                        </div>
                                    </div>
                                </div>
                                <!-- /.box-body -->

                                <div class="box-footer">
                                    <button type="submit" class="btn btn-info pull-right">导出</button>

                                    <button id="clearForm" type="button" class="btn btn-default">清空条件</button>
                                </div>
                                <!-- /.box-footer -->
                            </form>
                        </div>
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
    UML.selectPuls({el: 'columns', url: '${request.contextPath}/config/queryProtocol'});

    $(function () {
        <#if status??>
        <#if status == '10000'>
        Swal.fire({
            position: 'top-end',
            type: 'success',
            title: '${msg!}',
            showConfirmButton: false
        });
        <#else>
        Swal.fire({
            position: 'top-end',
            type: 'error',
            title: '${msg!}',
            showConfirmButton: false
        });
        </#if>
        </#if>
    });
</script>
</body>
</html>