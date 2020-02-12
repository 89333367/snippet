<aside class="main-sidebar">
    <!-- sidebar: style can be found in sidebar.less -->
    <section class="sidebar">
        <!-- Sidebar user panel -->
        <div class="user-panel">
            <div class="pull-left image">
                <img src="https://cdn.bootcss.com/admin-lte/2.4.15/img/user2-160x160.jpg" class="img-circle"
                     alt="User Image">
            </div>
            <div class="pull-left info">
                <p>
                    <@shiro.principal property="name"/>
                </p>
                <a href="#"><i class="fa fa-circle text-success"></i> Online</a>
            </div>
        </div>
        <!-- search form -->
        <form action="#" method="get" class="sidebar-form">
            <div class="input-group">
                <input type="text" name="q" class="form-control" placeholder="Search...">
                <span class="input-group-btn">
                <button type="submit" name="search" id="search-btn" class="btn btn-flat"><i class="fa fa-search"></i>
                </button>
              </span>
            </div>
        </form>
        <!-- /.search form -->
        <!-- sidebar menu: : style can be found in sidebar.less -->
        <ul id="menu" class="sidebar-menu" data-widget="tree">
            <li>
                <a href="${request.contextPath}/widgets" target="_blank">
                    <i class="fa fa-th"></i> <span>Widgets</span>
                    <span class="pull-right-container">
              <small class="label pull-right bg-green">new</small>
            </span>
                </a>
            </li>

            <li class="header">业务支撑</li>
            <li class="treeview">
                <a href="#">
                    <i class="fa fa-cubes"></i> <span>新能源业务</span>
                    <span class="pull-right-container">
              <i class="fa fa-angle-left pull-right"></i>
            </span>
                </a>
                <ul class="treeview-menu">
                    <li><a href="${request.contextPath}/hbase/ne/gateway"><i class="fa fa-folder-open"></i>
                            网关日志</a></li>
                    <li><a href="${request.contextPath}/hbase/ne/hex"><i class="fa fa-calendar-minus-o"></i>
                            原始报文</a></li>
                    <li><a href="${request.contextPath}/hbase/ne/command"><i class="fa fa-info-circle"></i>
                            指令数据</a></li>
                    <li><a href="${request.contextPath}/redis/ne/online"><i class="fa fa-podcast"></i>
                            在线状态</a></li>
                    <li><a href="${request.contextPath}/hbase/ne/packageCount"><i class="fa fa-pie-chart"></i>
                            统计包数</a></li>
                    <li><a href="${request.contextPath}/hbase/ne/intervalCount"><i class="fa fa-bar-chart"></i>
                            统计间隔</a></li>
                    <li><a href="${request.contextPath}/hbase/ne/errorHex"><i class="fa fa-calendar-times-o"></i>
                            错误报文</a></li>
                    <li><a href="${request.contextPath}/hbase/ne/repeat"><i class="fa fa-repeat"></i>
                            重复数据</a></li>

                    <li><a href="${request.contextPath}/hbase/ne/can"><i class="fa fa-copyright"></i>
                            Can数据</a></li>
                    <li><a href="${request.contextPath}/hbase/ne/alarm"><i class="fa fa-exclamation"></i>
                            报警数据</a></li>
                    <li><a href="${request.contextPath}/hbase/ne/journey"><i class="fa fa-car"></i>
                            行程数据</a></li>
                    <li><a href="${request.contextPath}/redis/ne/alarm"><i class="fa fa-exclamation-triangle"></i>
                            当日报警</a></li>
                    <li><a href="${request.contextPath}/redis/ne/route"><i class="fa fa-line-chart"></i>
                            当日行程</a></li>
                    <li><a href="${request.contextPath}/redis/ne/realtime"><i class="fa fa-sign-out"></i>
                            最后信息</a></li>
                    <li><a href="${request.contextPath}/redis/ne/batch/realtime"><i class="fa fa-print"></i>
                            批量导出</a></li>
                    <li><a href="${request.contextPath}/redis/ne/command"><i class="fa fa-mouse-pointer"></i>
                            指令信息</a></li>
                    <li><a href="${request.contextPath}/redis/ne/fence"><i class="fa fa-retweet"></i>
                            围栏信息</a></li>
                </ul>
            </li>
            <li class="treeview">
                <a href="#">
                    <i class="fa fa-sitemap"></i> <span>非道路业务</span>
                    <span class="pull-right-container">
              <i class="fa fa-angle-left pull-right"></i>
            </span>
                </a>
                <ul class="treeview-menu">
                    <li><a href="${request.contextPath}/hbase/farm/gateway"><i class="fa fa-folder-open"></i>
                            网关日志</a></li>
                    <li><a href="${request.contextPath}/hbase/farm/hex"><i class="fa fa-calendar-minus-o"></i>
                            原始报文</a></li>
                    <li><a href="${request.contextPath}/hbase/farm/command"><i class="fa fa-info-circle"></i>
                            指令数据</a></li>
                    <li><a href="${request.contextPath}/redis/farm/online"><i class="fa fa-podcast"></i>
                            在线状态</a></li>
                    <li><a href="${request.contextPath}/hbase/farm/packageCount"><i class="fa fa-pie-chart"></i>
                            统计包数</a></li>
                    <li><a href="${request.contextPath}/hbase/farm/intervalCount"><i class="fa fa-bar-chart"></i>
                            统计间隔</a></li>
                    <li><a href="${request.contextPath}/hbase/farm/errorHex"><i class="fa fa-calendar-times-o"></i>
                            错误报文</a></li>
                    <li><a href="${request.contextPath}/hbase/farm/repeat"><i class="fa fa-repeat"></i>
                            重复数据</a></li>

                    <li><a href="${request.contextPath}/hbase/farm/can"><i class="fa fa-copyright"></i>
                            Can数据</a></li>
                    <li><a href="${request.contextPath}/hbase/farm/work"><i class="fa fa-line-chart"></i>
                            工作数据</a></li>
                    <li><a href="${request.contextPath}/hbase/farm/split"><i class="fa fa-area-chart"></i>
                            分段数据</a></li>
                    <li><a href="${request.contextPath}/redis/farm/realtime"><i class="fa fa-sign-out"></i>
                            最后信息</a></li>
                    <li><a href="${request.contextPath}/redis/farm/fence"><i class="fa fa-retweet"></i>
                            围栏信息</a></li>

                    <li><a href="${request.contextPath}/hbase/farm/canCount"><i class="fa fa-area-chart"></i>
                            时段统计</a></li>
                </ul>
            </li>

            <li class="header">大数据概览</li>
            <li class="treeview">
                <a href="#">
                    <i class="fa fa-dashboard"></i> <span>平台监控</span>
                    <span class="pull-right-container">
              <i class="fa fa-angle-left pull-right"></i>
            </span>
                </a>
                <ul class="treeview-menu">
                    <li><a href="${request.contextPath}/dashboard/dashboard"><i class="fa fa-refresh"></i>
                            业务状态</a></li>
                    <li><a href="${request.contextPath}/hbase/interface"><i class="fa fa-list"></i>
                            接口日志</a></li>
                    <li><a href="${request.contextPath}/hbase/device/log"><i class="fa fa-list"></i>
                            设备日志</a></li>
                    <li><a href="${request.contextPath}/hbase/ql"><i class="fa fa-database"></i>
                            Hbase查询</a></li>
                </ul>
            </li>

            <li class="header">系统管理</li>
            <li class="treeview">
                <a href="#">
                    <i class="fa fa-gear"></i> <span>权限管理</span>
                    <span class="pull-right-container">
              <i class="fa fa-angle-left pull-right"></i>
            </span>
                </a>
                <ul class="treeview-menu">
                    <li><a href="${request.contextPath}/shiro/user/list"><i class="fa fa-address-card"></i>
                            用户管理</a></li>
                </ul>
            </li>

        </ul>
    </section>
    <!-- /.sidebar -->
</aside>