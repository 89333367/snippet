(function tablePuls(window) {
    // resize event
    (function ($, window, undefined) {
        var elems = $([]),
            jq_resize = $.resize = $.extend($.resize, {}),
            timeout_id,
            str_setTimeout = 'setTimeout',
            str_resize = 'resize',
            str_data = str_resize + '-special-event',
            str_delay = 'delay',
            str_throttle = 'throttleWindow';
        jq_resize[str_delay] = 500;
        jq_resize[str_throttle] = true;
        $.event.special[str_resize] = {
            setup: function () {
                if (!jq_resize[str_throttle] && this[str_setTimeout]) {
                    return false;
                }
                var elem = $(this);
                elems = elems.add(elem);
                $.data(this, str_data, {
                    w: elem.width(),
                    h: elem.height()
                });
                if (elems.length === 1) {
                    loopy();
                }
            },
            teardown: function () {
                if (!jq_resize[str_throttle] && this[str_setTimeout]) {
                    return false;
                }
                var elem = $(this);
                elems = elems.not(elem);
                elem.removeData(str_data);
                if (!elems.length) {
                    clearTimeout(timeout_id);
                }
            },
            add: function (handleObj) {
                if (!jq_resize[str_throttle] && this[str_setTimeout]) {
                    return false;
                }
                var old_handler;

                function new_handler(e, w, h) {
                    var elem = $(this),
                        data = $.data(this, str_data);
                    data.w = w !== undefined ? w : elem.width();
                    data.h = h !== undefined ? h : elem.height();
                    old_handler.apply(this, arguments);
                }
                if ($.isFunction(handleObj)) {
                    old_handler = handleObj;
                    return new_handler;
                } else {
                    old_handler = handleObj.handler;
                    handleObj.handler = new_handler;
                }
            }
        };

        function loopy() {
            timeout_id = window[str_setTimeout](function () {
                elems.each(function () {
                    var elem = $(this),
                        width = elem.width(),
                        height = elem.height(),
                        data = $.data(this, str_data);
                    if (width !== data.w || height !== data.h) {
                        elem.trigger(str_resize, [data.w = width, data.h = height]);
                    }
                });
                loopy();
            }, jq_resize[str_delay]);
        }
    })($, window);

    window.UML = window.UML || {}

    window.UML.tablePlus = function (options) {

        // 表格数据
        var tableData = {
            titles: null, // 所有title
            diffTitle: null,
            headleft: options.columns || [],
            headMid: [],
            headright: options.columnsLast || [],
            headArr: [], // 表头显示数据
            titleIntervalItem: [], // 保持表头title在表格视窗内显示
            titleIntervalIndex: [], // 保持表头title在表格视窗内显示
            rows: [], // 表格数据
            excelRows: [], // 表格数据
            excelhead: [], // 表格数据
            page: 0,
            total: 0,
            resultTotal: 0
        }
        var tableLeft = {
            headArr: [], // 表头显示数据
            rows: [], // 表格数据
            widths: []
        }
        var tableRight = {
            headArr: [], // 表头显示数据
            rows: [], // 表格数据
            widths: []
        }
        /*
        执行列顺序
        columns:[{
            data:"key",
            title:"title",
            render: function (data) {
                return data 对指定key的数据进行加工
                },
        }]
        columnsLast 同理指定后方列
        */
        // 表格参数
        var config = {
            el: "", // 表格ID
            type: 'POST',
            url: "", // 后台查询数据的请求地址
            queryParams: {}, // 请求查询参数
            queryRowKey: {},
            dataType: "json", // ajax请求返回类型
            columns: undefined, // 前排显示列
            columnsLast: undefined, // 后排显示列
            showLoading: true, // 是否显示loading 标记
            isLoad: false, // 当前是否加载中
            isInit: false, // 初始化
            isTotalEnd: false, // 数据接收完毕
            info: true, // 显示表格数据
            height: 400, // 表格高度
            thresholdHeight: 20, // 滑动加载阈值
            showColumns: [], // 隐藏数组内部包含列
            showColumnsFlag: false,
            frozenLeft: undefined,
            frozenRight: undefined,
            headLeftWidth: 0,
            isDown: false,
            isAllDown: false,
            exportTitle: '无名称',
            export: false, // 是否开放导出功能
            tableClicks: {
                // 单元格点击事件
            },
            refreshModel: false, //刷新模式
            onBefore: function (config, tableData) {
                // 用于在查询前更改查询参数的方法
                // config代表table内部的参数对象
            },
            onSuccess: function (config, result, tableData) {
                // ajax输入返回后立即执行
                // result代表查询成功后回台返回的json对象
                // 用于查询成功后，改变查询参数的方法，一般大数据中，查询成功后，需要改变下一页查询的第一行数据值

            },
            onAfter: function (config, result) {
                // 表格业务处理完毕后执行
                // result代表查询成功后回台返回的json对象,config代表表单配置
            },
            onClickRow: function (index, row) {
                // index为当前行的索引，row代表行的数据对象；
            },
            onClickTable: function (config, table) {
                // config代表table内部的参数对象，table代表table数据
            },
        }
        $.extend(config, options)


        // 判断obj差异
        function difference(object, base) {
            function changes(object, base) {
                return _.transform(object, function (result, value, key) {
                    if (!_.isEqual(value, base[key])) {
                        result[key] = (_.isObject(value) && _.isObject(base[key])) ? changes(value, base[key]) :
                            value;
                    }
                });
            }

            return changes(object, base);
        }


        // init style

        var initStyle = function () {
            var styleStr = "*{box-sizing:border-box}.pull-left{float: left !important;}.pull-right {float: right !important;}.clearfix:after{content:'.'; display:block; height:0; visibility:hidden; clear:both; }.clearfix { *zoom:1; }.uml-export-excel{margin-left:10px}.uml-btn-default{padding:2px 8px;font-size:14px;line-height:1.42857143;user-select:none;touch-action:manipulation;cursor: pointer;background-image: none;display:inline-block;margin-bottom:0;font-weight:400;text-align:center;white-space:nowrap;vertical-align: middle;border:1px solid transparent;border-radius:3px;border-color:#ddd;color:#444;background-color: #f4f4f4;}.uml-table-box thead tr th{position:relative}.text-right{text-align:right}.uml-count-table{line-height:26px}.uml-table-tool>div{display:inline-block;}.uml-table-tool{margin:10px}.table-hover>tbody>tr.hover{background-color:#f5f5f5}.table{margin-bottom:0}.uml-table-box{position:relative;width:100%;background-color:#fff;color:#666}.uml-table-box>div{position:relative;background-color:#fff}.uml-table-box table{position:relative;border-spacing:0}.uml-table-box th{background-color:#fff;text-align:left;background-color:#f9f9f9}.uml-table-box tbody tr td,.uml-table-box tbody tr th,.uml-table-box thead tr td,.uml-table-box thead tr th{border:1px solid #ddd}.table>tbody>tr>td, .table>tbody>tr>th, .table>tfoot>tr>td, .table>tfoot>tr>th, .table>thead>tr>td, .table>thead>tr>th{padding:4px 6px;}.uml-table-cell span{position:relative}.uml-table-cell{height:20px;line-height:16px;padding:2px 6px;position:relative;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;box-sizing:border-box}.uml-table-header{overflow:hidden;z-index:99}.uml-table-body{border-bottom: 1px solid #ddd;overflow:auto}.uml-table-body thead{visibility:hidden}.uml-table-box .uml-table-fixed{position:absolute;top:0;z-index:101;overflow:hidden}.uml-table-box .uml-table-fixed>div{position:relative}.uml-table-box .uml-table-fixed>div:first-of-type{z-index:110}.uml-table-box .uml-table-left{left:0}.uml-table-box .uml-table-right{right:0}.table-bordered{border:1px solid #ddd}"
            window.tablePlusStyle = true
            $('head').append('<style>' + styleStr + '</style>')
        }


        // DOM 模板 初始化
        var tableHeight = config.height == "auto" ? "" : config.height;
        var vHead = '<div class="uml-table-header"><table class="table table-hover table-bordered" cellspacing="0" cellpadding="0" border="0"><thead></thead></table></div>';
        var vBody = '<div class="uml-table-body" style="height:' + tableHeight + 'px"><table class="table table-hover table-bordered" cellspacing="0" cellpadding="0" border="0"><thead><tr></tr></thead><tbody></tbody></table></div>';
        var vLeft = '<div class="uml-table-fixed uml-table-left"><div class="uml-table-left-head"><table class="table table-hover table-bordered" cellspacing="0" cellpadding="0" border="0"><thead><tr></tr></thead></table></div><div class="uml-table-left-body"><table class="table table-hover table-bordered" cellspacing="0" cellpadding="0" border="0"><tbody></tbody></table></div>';
        var vRight = '<div class="uml-table-fixed uml-table-right"><div class="uml-table-right-head"><table class="table table-hover table-bordered" cellspacing="0" cellpadding="0" border="0"><thead><tr></tr></thead></table></div><div class="uml-table-right-body"><table class="table table-hover table-bordered" cellspacing="0" cellpadding="0" border="0"><tbody></tbody></table></div>';
        var vExportBtn = '<div class="uml-export-excel btn-export-excel pull-right"><button class="uml-btn-default">导出当前页数据</button><div>'
        var vExportAllBtn = '<div class="uml-export-excel btn-export-allexcel pull-right"><button class="uml-btn-default">导出全部数据</button><div>'
        var vCountTable = '<div class="uml-count-table">已加载 <span>' + tableData.total + '</span> 条记录，共 <span>' + tableData.page + '</span> 页<div>'
        var vBox = '<div class="uml-table-box">' + vHead + vBody + '</div>';

        $('#' + config.el).append(vBox)

        var Dom = {
            table: $('#' + options.el),
            tableBox: $('#' + options.el + ' .uml-table-box'),
            tableHead: $('#' + options.el).find('.uml-table-header'),
            tableHeadThead: $('#' + options.el).find('.uml-table-header thead'),
            tableBody: $('#' + options.el).find('.uml-table-body'),
            tableBodyTHead: $('#' + options.el).find('.uml-table-body thead'),
            tableBodyTbody: $('#' + options.el).find('.uml-table-body tbody'),
        }

        /*
        1. updateHead
        2. cloneHead
        3. updateBody
        4. setHeadSize
        5. initLock
        6. updateLock
        */

        // 更新表头结构
        var updateHead = function () {

            // 清除头部结构
            Dom.tableBodyTHead.find('tr').empty()
            //  生成内容thead dom
            var tmpHead = "";


            tableData.excelhead = []

            $.each(tableData.headArr, function (index, item) {
                tmpHead += '<th tid=' + item.data + '><div class="uml-table-cell"><span>' + (item.title || item.data || "") + '</span></div></th>'
                tableData.excelhead.push((item.title || item.data))
            });
            // tableData.excelhead = tableData.excelhead.join('') + '/n'
            Dom.tableBodyTHead.find('tr').append(tmpHead)
            cloneHead()
        }
        // 克隆表结构
        var cloneHead = function () {

            Dom.tableHeadThead.empty().append(Dom.tableBodyTHead.find('tr').clone())
        }
        // 设计表头宽度保持统一
        var setHeadSize = function (tmpLeft, tmpRight) {

            var boxHeight = Dom.tableBox.outerHeight()
            var boxWidth = Dom.tableBox.outerWidth()
            var headerHeight = Dom.tableBodyTHead.outerHeight()
            var tableHeight = Dom.tableBox.find('.uml-table-body table').outerHeight()
            // 控制表头右侧滚动栏间隔
            if (boxHeight + headerHeight < tableHeight && $(document).outerWidth() > 768) {
                Dom.tableBox.find('.uml-table-header').css('margin-right', '17px')
            } else {
                Dom.tableBox.find('.uml-table-header').css('margin-right', '0')
            }

            // 初始化固定列数据
            if (!config.isInit) {
                initLock()
                config.isInit = true
            }

            // 同步表格单元格宽度
            // 存储超长表格width数据，用于判断阈值
            var currentWidth = 0
            tableData.titleIntervalItem = []
            tableData.titleIntervalIndex = []
            var thLenght = Dom.tableBodyTHead.find('th div').length
            var leftStyle = ""
            var rightStyle = ""
            var leftWidths = []
            var rightWidths = []
            $.each(Dom.tableBodyTHead.find('th div'), function (index, item) {

                var width = $(item).width()
                var outerWidth = $(item).outerWidth()
                var parentWidth = $(item).parent().outerWidth()

                // 同步表格单元格宽度
                $(Dom.tableHeadThead.find('div')[index]).width(width)

                if (config.frozenLeft && config.frozenLeft > index) {
                    $(Dom.tableLeft.find('.uml-table-left-head thead div')[index]).width(width)
                    leftStyle += ".uml-table-left-body tbody td:nth-of-type(" + (index + 1) + ") div{width:" + outerWidth + "px}"
                    leftWidths.push(width)
                }
                if (config.frozenRight && config.frozenRight >= (thLenght - index)) {
                    $(Dom.tableRight.find('.uml-table-right-head thead div')[config.frozenRight - (thLenght - index)]).width(width)
                    rightStyle += ".uml-table-right-body tbody td:nth-of-type(" + (config.frozenRight - (thLenght - index) + 1) + ") div{width:" + outerWidth + "px}"
                    rightWidths.push(width)
                }

                if (parentWidth > boxWidth) {
                    tableData.titleIntervalItem.push(currentWidth + '|' + (currentWidth + parentWidth))
                    tableData.titleIntervalIndex.push(index)
                }
                currentWidth += parentWidth
            })
            if (tableLeft.widths.length == 0 || _.difference(tableLeft.widths, leftWidths).length > 0) {
                tableLeft.widths = leftWidths
                $('.' + config.el + '-lock-left').empty().append(leftStyle)
            }
            if (tableRight.widths.length == 0 || _.difference(tableRight.widths, rightWidths).length > 0) {
                tableRight.widths = rightWidths
                $('.' + config.el + '-lock-right').empty().append(rightStyle)
            }
            // 修改固定表头偏移，保持表格的完整性
            if (headerHeight * 1 > 0) {

                $('#' + config.el).find('.uml-table-body').css('top', -Dom.tableBodyTHead.outerHeight())
            }


            updateLock(tmpLeft, tmpRight)

            Dom.tableBox.height(Dom.tableBox.find('.uml-table-body').height())


        }

        // 初始化固定列
        var initLock = function (bottom) {

            if (config.frozenLeft) {
                tableLeft.headArr = tableData.headArr.slice(0, config.frozenLeft)
            }
            if (config.frozenRight) {
                tableRight.headArr = tableData.headArr.slice(-config.frozenRight)
            }
            if (tableLeft.headArr.length > 0 && !Dom.tableLeft) {

                Dom.tableBox.append(vLeft)
                Dom.tableLeft = $('#' + options.el).find('.uml-table-left')
                Dom.tableLeft.find('thead tr').append(Dom.tableHeadThead.find('th').slice(0, tableLeft.headArr.length).clone())
                $('head').append('<style class=' + config.el + '-lock-left>')
            }
            if (tableRight.headArr.length > 0 && !Dom.tableRight) {

                Dom.tableBox.append(vRight)
                Dom.tableRight = $('#' + options.el).find('.uml-table-right')
                Dom.tableRight.find('thead tr').append(Dom.tableHeadThead.find('th').slice(-tableRight.headArr.length).clone())
                $('head').append('<style class=' + config.el + '-lock-right>')
            }
        }

        // 更新固定列数据
        var updateLock = function (tmpLeft, tmpRight) {
            var boxWidth = Dom.tableBox.find('.uml-table-body').outerWidth()
            var boxHeight = Dom.tableBox.find('.uml-table-body').outerHeight()
            var bodyWidth = Dom.tableBox.find('.uml-table-body table').outerWidth()
            var bodyHeight = Dom.tableBox.find('.uml-table-body table').outerHeight()

            if (Dom.tableLeft) {

                Dom.tableLeft.css('width', $('#' + options.el).find('.uml-table-left thead').outerWidth() + 1)
                Dom.tableLeft.find('tbody').append(tmpLeft)
                if (boxWidth < bodyWidth) {
                    Dom.tableLeft.css('bottom', '17px')
                } else {
                    Dom.tableLeft.css('bottom', '0')
                }
                config.headLeftWidth = Dom.tableLeft.outerWidth()

            }
            if (Dom.tableRight) {
                Dom.tableRight.css('width', $('#' + options.el).find('.uml-table-right thead').outerWidth() + 1)
                Dom.tableRight.find('tbody').append(tmpRight)

                if (boxWidth < bodyWidth) {
                    Dom.tableRight.css('bottom', '17px')
                } else {
                    Dom.tableRight.css('bottom', '0')
                }
                if (boxHeight < bodyHeight) {
                    Dom.tableRight.css('right', '17px')
                } else {
                    Dom.tableRight.css('right', '0')
                }

            }
            if (boxHeight > bodyHeight && !config.isTotalEnd && !config.isLoad && config.height !== 'auto') {
                render()
            }
        }
        // 更新表格数据
        var updateBody = function (data) {
            tableData.rows = tableData.rows.concat(data.rows)
            var tmpTr = ''
            var tmpLeft = ''
            var tmpRight = ''

            $.each(data.rows, function (cindex, citem) {
                var excelRow = []
                tmpTr += '<tr index="' + ((1 * tableData.total) + (cindex * 1)) + '">'
                tmpLeft += '<tr index="' + ((1 * tableData.total) + (cindex * 1)) + '">'
                tmpRight += '<tr index="' + ((1 * tableData.total) + (cindex * 1)) + '">'
                var headLength = Dom.tableHeadThead.find('th').length
                $.each(Dom.tableHeadThead.find('th'), function (tindex, titem) {
                    var initData = citem[$(titem).attr('tid')]
                    var tmpData = citem[$(titem).attr('tid')]

                    excelRow.push((initData || (initData == 0 ? '0' : '')))
                    if (tableData.headArr[tindex].render) {
                        tmpData = tableData.headArr[tindex].render(initData, data.rows[cindex])
                    }

                    var tmpStr = '<td tid="' + $(titem).attr('tid') + '" value="' + initData + '"><div class="uml-table-cell">' + (tmpData || (initData == 0 ? '0' : '')) + '</div></td>'
                    tmpTr += tmpStr
                    if (tindex < config.frozenLeft) {
                        tmpLeft += tmpStr
                    }
                    if (tindex >= headLength - config.frozenRight) {
                        tmpRight += tmpStr
                    }
                })
                tmpTr += '</tr>'
                tmpLeft += '</tr>'
                tmpRight += '</tr>'
                // excelRow.push('\n')
                tableData.excelRows.push(excelRow)
            })

            /* 刷新模式下先清空数据 */
            if (config.refreshModel) {
                Dom.tableBox.find('tbody').empty()
            }
            Dom.tableBodyTbody.append(tmpTr)
            tableData.page++; //记录查询了多少页
            tableData.total += data.rows.length; //记录一共加载了多少数据
            if (tableData.resultTotal == tableData.total) {
                config.isTotalEnd = true
            }
            setHeadSize(tmpLeft, tmpRight)
            if (config.info) {
                if (Dom.table.find('.uml-table-tool').length > 0) {
                    Dom.table.find('.uml-table-tool .uml-count-table span:eq(0)').text(tableData.total)
                    Dom.table.find('.uml-table-tool .uml-count-table span:eq(1)').text(tableData.page)
                }
            }
        }

        var render = function (type) {

            config.isLoad = true
            config.onBefore.call(null, config, tableData); //回调用户的onBefore方法
            if (config.showLoading && Pace) {
                Pace.restart(); //显示加载进度
            }

            var queryParams = _.assign({}, config.queryParams, config.queryRowKey);
            if (config.isTotalEnd) {
                return
            }
            if (type == "refresh") {
                config.refreshModel = true
            } else {
                config.refreshModel = false
            }
            // 拉取数据
            $.ajax({
                type: config.type,
                url: config.url,
                dataType: config.dataType,
                data: queryParams,
                success: function (data) {

                    config.onSuccess.call(null, config, data, tableData); //回调用户的onSuccess方法
                    if (data && data.rows && data.rows.length > 0) {

                        tableData.resultTotal = data.total

                        // 表格已有数据
                        if (tableData.titles) {
                            tableData.diffTitle = difference(data.titles, tableData.titles)
                        } else {

                            tableData.titles = {}
                            // 当前表格无数据
                            // 合并格式化 titlesArr
                            // 如果showColumns参数存在，过滤只显示该data的值

                            if (config.showColumnsFlag) {
                                tableData.headleft = tableData.headleft.filter(function (item) {
                                    return config.showColumns.indexOf(item.data) > -1
                                })
                                tableData.headright = tableData.headright.filter(function (item) {
                                    return config.showColumns.indexOf(item.data) > -1
                                })
                            }

                            $.each(tableData.headleft, function (index, item) {

                                tableData.titles[item.data] = item.title || item.data
                                if (data.titles[item.data]) {
                                    item.title = item.title || data.titles[item.data] || item.data
                                    tableData.titles[item.data] = data.titles[item.data]
                                    delete data.titles[item.data];
                                }
                            })
                            $.each(tableData.headright, function (index, item) {
                                tableData.titles[item.data] = item.title || item.data
                                if (data.titles[item.data]) {
                                    item.title = item.title || data.titles[item.data] || item.data
                                    tableData.titles[item.data] = data.titles[item.data]
                                    delete data.titles[item.data];
                                }
                            })

                            _.forIn(data.titles, function (value, data) {
                                if (config.showColumnsFlag) {
                                    if (config.showColumns.indexOf(data) > -1) {
                                        tableData.headMid.push({
                                            data: data,
                                            title: value
                                        })
                                    }
                                } else {
                                    tableData.headMid.push({
                                        data: data,
                                        title: value
                                    })
                                }

                            });

                            // 根据 showColumns 格式化headArr
                            tableData.headArr = tableData.headleft.concat(tableData.headMid, tableData.headright)
                            tableData.titles = _.assign(tableData.titles, data.titles)

                            updateHead()
                        }

                        // 表格已有数据且title有差异
                        if (!$.isEmptyObject(tableData.diffTitle)) {
                            tableData.titles = _.assign(tableData.titles, tableData.diffTitle)
                            _.forIn(tableData.diffTitle, function (value, data) {
                                if (config.showColumnsFlag) {
                                    if (config.showColumns.indexOf(data) > -1) {
                                        tableData.headMid.push({
                                            data: data,
                                            title: value
                                        })
                                    }
                                } else {
                                    tableData.headMid.push({
                                        data: data,
                                        title: value
                                    })
                                }

                            });
                            tableData.headArr = tableData.headleft.concat(tableData.headMid, tableData.headright)
                            if (config.showColumns && config.showColumns.length > 0) {
                                tableData.headArr = tableData.headArr.filter(function (item, index, arr) {

                                    return config.showColumns.indexOf(item.data) > -1
                                })
                            }
                            updateHead()
                        }
                        tableData.diffTitle = null

                        updateBody(data)
                    } else if (data && data.rows && data.rows.length == 0) {
                        // 返回数据为空
                        config.isTotalEnd = true

                    } else {
                        // 服务器错误
                        config.isTotalEnd = true
                        if (config.onError) {
                            config.onError.call(null, data)
                        }

                    }

                    config.isLoad = false; //标识数据加载完毕
                    config.onAfter.call(null, config, data) //回调用户的 onAfter 方法
                },
                error: function (res) {
                    config.isTotalEnd = true
                    if (config.onError) {
                        config.onError.call(null, res)
                    }
                }
            });
        }

        config.refresh = function () {
            tableData.rows = []
            tableData.page = 0
            tableData.total = 0
            tableData.resultTotal = 0

            tableLeft.rows = []

            tableRight.rows = []

            config.isInit = false
            config.isTotalEnd = false
            config.isLoad = false
            config.tableClicks = {}

            Dom.table.find('.uml-table-box').off('click')
            Dom.table.find('.uml-table-box').off('mousemove')
            Dom.table.find('.uml-table-body').off('scroll')


            render('refresh')

            bindEvent()
        }

        var bindEvent = function () {

            // 监听点击事件
            Dom.table.find('.uml-table-box').on('click', function (e) {
                // 触发单元格点击事件
                config.onClickTable.call(null, config, tableData)
                // 触发行点击事件
                var index = $(e.target).parentsUntil('tbody', 'tr').attr('index')
                var tid = $(e.target).parentsUntil('tbody', 'td').attr('tid')
                var value = $(e.target).parentsUntil('tbody', 'td').attr('value')
                var row = tableData.rows[index]
                config.onClickRow.call(null, index, row)

                // 触发单元格点击事件
                if (config.tableClicks[tid]) {

                    config.tableClicks[tid].call(null, value, row)
                }
            });

            // 收集单元格点击事件

            var clickList = [].concat(config.columns || [], config.columnsLast || [])
            clickList.forEach(function (item) {
                if (item.click) {
                    config.tableClicks[item.data] = item.click
                }
            });

            // 窗口尺寸变化更改表格尺寸

            Dom.table.on('resize', _.debounce(function () {
                if (config.isInit) {
                    setHeadSize()
                }
            }, 100))

            // 同步hover事件

            Dom.table.find('.uml-table-box').on('mousemove', function (e) {

                var index = $(e.target).parentsUntil('tbody', 'tr').attr('index')

                $(Dom.tableBodyTbody.find('tr')[index]).addClass('hover').siblings().removeClass('hover')
                if (Dom.tableLeft) {
                    $(Dom.tableLeft.find('tbody tr')[index]).addClass('hover').siblings().removeClass('hover')
                }
                if (Dom.tableRight) {
                    $(Dom.tableRight.find('tbody tr')[index]).addClass('hover').siblings().removeClass('hover')
                }
            })
            Dom.table.find('.uml-table-box tbody').on('mouseleave', function (e) {
                Dom.tableBox.find('tbody tr').removeClass('hover')
            })

            // 监听滚动事件加载数据
            Dom.table.find('.uml-table-body').on('scroll', function () {

                var scrollLeft = $(this).scrollLeft()
                var scrollTop = $(this).scrollTop()
                // 将数据过长列的title固定显示在容器内
                $.each(tableData.titleIntervalItem, function (index, item) {
                    var tmp = item.split('|')
                    var tmpLeft = tmp[0] * 1
                    var tmpRight = tmp[1] * 1

                    if (tmpLeft - config.headLeftWidth < scrollLeft && scrollLeft < tmpRight) {

                        $('.uml-table-header th:eq(' + (tableData.titleIntervalIndex[index]) + ') span').css({
                            "left": scrollLeft - tmpLeft + config.headLeftWidth,
                        })
                    } else {
                        $('.uml-table-header th:eq(' + (tableData.titleIntervalIndex[index]) + ')').css({
                            "left": 0
                        })
                    }
                })
                // 同步表头滚动
                Dom.table.find('.uml-table-header table').css('left', -scrollLeft + 'px')
                // 同步冻结列滚动
                if (Dom.tableLeft) {
                    Dom.tableLeft.find('.uml-table-left-body').css('top', -scrollTop + "px")
                }
                if (Dom.tableRight) {
                    Dom.tableRight.find('.uml-table-right-body').css('top', -scrollTop + "px")
                }
                // 行数没有铺满表格自动加载
                if (!config.isTotalEnd && config.height !== 'auto' && !config.isLoad && ($(this).scrollTop() + config.height + config.thresholdHeight) > $(this).find('table').outerHeight()) {
                    render()
                }
            });
        }
        /* csv export */
        function Trim(str, is_global) {
            var result;
            result = str.replace(/(^\s+)|(\s+$)/g, "");
            result = result.replace(/\|/g, "");
            if (is_global.toLowerCase() == "g") {
                result = result.replace(/\s/g, "");
            }
            return result;
        }

        function toLargerCSV(data) {

            var str = arrayToCsv(data)
            var blob = new Blob([str], {
                type: "text/plain;charset=utf-8"
            });
            //解决中文乱码问题
            blob = new Blob([String.fromCharCode(0xFEFF), blob], {
                type: blob.type
            });
            object_url = window.URL.createObjectURL(blob);
            var link = document.createElement("a");
            link.href = object_url;
            link.download = config.exportTitle + ".csv";
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            config.isDown = false
        }

        function arrayToCsv(data) {
            var columnDelimiter = ',';
            var lineDelimiter = '\n';
            return data.reduce(function (csv, row) {
                var rowContent = Array.isArray(row) ? row.reduce(function (rowTemp, col) {
                    var ret = rowTemp ? rowTemp + columnDelimiter : rowTemp;
                    col = col || " "
                    if (col) {
                        var formatedCol = col.toString().replace(new RegExp(lineDelimiter, 'g'), ' ');
                        formatedCol = col.toString().replace(new RegExp('"', 'g'), ' ');
                        ret += /,/.test(formatedCol) ? "\"".concat(formatedCol, "\"") : formatedCol;
                        ret += '\t'
                    }

                    return ret;
                }, '') : row;
                return (csv ? csv + lineDelimiter : '') + rowContent;
            }, '');
        }

        function formatQuery(obj) {
            var str = "?"
            _.forIn(obj, function (value, key) {
                str += key + '=' + value + '&'
            });
            return str.slice(0, -1)
        }

        function exportExcel() {
            if (!config.isDown) {
                config.isDown = true
                var data = [tableData.excelhead]
                data = data.concat(tableData.excelRows)
                toLargerCSV(data)
            }
        }

        function exportAllExcel() {

            if (!config.isAllDown) {
                config.isAllDown = true
                config.queryParams.exportTitle = config.exportTitle
                window.location.href = config.exportUrl + formatQuery(config.queryParams)
                config.isAllDown = false
            }
        }
        /* csv export end */
        function init() {
            if (config.showColumns && config.showColumns.length > 0) {
                config.showColumnsFlag = true
            }

            // init style
            $('#' + config.el).css('position', 'relative')
            if (!window.tablePlusStyle) {
                initStyle()
            }

            // init tool
            if (config.export || config.info || config.exportUrl) {
                Dom.table.append($('<div class="uml-table-tool clearfix"></div>'));
                Dom.tool = $('#' + config.el + ' .uml-table-tool')
            }
            if (config.exportUrl) {
                Dom.tool.append(vExportAllBtn)
                Dom.tool.find('.btn-export-allexcel').on('click', function () {
                    exportAllExcel()
                })
            }
            if (config.export) {
                Dom.tool.append(vExportBtn)
                Dom.tool.find('.btn-export-excel').on('click', function () {
                    exportExcel()
                })
            }
            if (config.info) {
                if (config.info == 'right') {
                    Dom.tool.append(vCountTable)
                    Dom.tool.find('.uml-count-table').addClass('pull-right')

                } else {
                    Dom.tool.prepend(vCountTable)
                }
            }

            // render data
            render()

            // 绑定监听事件
            bindEvent()

            // 绑定el class
            Dom.tableBox.find('table').addClass($('#' + config.el).attr('class'))
        }

        init()
    }

})(window)