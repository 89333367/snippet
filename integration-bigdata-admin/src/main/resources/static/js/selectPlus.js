/* createSelect start */

(function selectPuls($, window) {
    function getObjectFirstAttr(data) {
        for (var key in data)
            return key;
    };

    function createTag(str, id) {
        return $('<a class="btn btn-app" style="margin:10px 10px 0 0;height:2.6em;line-height:2.6em;padding:0 5px;">' + str + '<span data-id=' + id + ' style="padding:2px;" class="badge bg-red"><i class="fa fa-fw fa-close"></i></span></a>')
    };
    window.UML = window.UML || {}
    window.UML.selectPuls = function (options) {

        var options = options || {}
        if (!options.el || !options.url) {
            throw "el and url is require!";
        }
        var el = '#' + options.el
        var parendDom = $(el).parent()
        var select2Dom = $('<select></select>')
        var selectedArr = $(el).val() ? $(el).val().split(',') : [];

        // 生成 select2容器
        $(el).hide()
        parendDom.append(select2Dom)
        // 实例化 select2
        $(select2Dom).select2({
            language: 'zh-CN',
            width: '100%',
            ajax: {
                url: options.url,
                dataType: 'json',
                delay: options.delay || 800,
                data: function (params) {
                    return {
                        q: params.term, // search term
                    };
                },
                // 格式化返回值，添加id，避免select2 下拉点击无效问题
                processResults: function (data, params) {
                    var filterData = []
                    $.each(data, function (index, item) {
                        var key = getObjectFirstAttr(item)
                        item.id = key
                        if($.inArray(key, selectedArr)==-1){
                            filterData.push(item)
                        }
                    })
                    return {
                        results: filterData
                    };
                },
                cache: true
            },
            placeholder: $(el).attr('placeholder') || "",
            escapeMarkup: function (markup) {
                return markup;
            },
            minimumInputLength: 0,
            // 格式化下拉列表
            templateResult: formatRepo,
            templateSelection: formatRepoSelection
        });
        // 生成tag容器
        parendDom.append("<div class='select2-tags-box'></div>")
        var tagsBox = parendDom.find('.select2-tags-box')
        // 初始化tag容器

        $.each(selectedArr, function (index, item) {
            if (index % 3 == 0) {
                tagsBox.append(createTag((selectedArr[index + 2] || selectedArr[index + 1] || item), item))
            }

        })
        // 挂载tag关闭事件
        parendDom.on('click', 'span.badge', function () {
            var id = $(this).data('id') + ''
            var index = $(this).parent().index()
            var idIndex = $.inArray(id, selectedArr)
            if (idIndex > -1) {
                selectedArr.splice(idIndex, 3)
            }
            tagsBox.find('.btn-app').eq(index).remove()
            formatColumn()
        })
        // 选中事件
        $(select2Dom).on('select2:select', function (e) {
            var data = e.params.data
            var idIndex = $.inArray(data.id, selectedArr)
            var en = data[data.id].en || data.id + 'en'
            var cn = data[data.id].cn
            if (idIndex == -1) {
                selectedArr.push(data.id, en, cn)
                tagsBox.append(createTag((cn || en || data.id), data.id))
            }
            formatColumn()

        });
        $(select2Dom).on('select2:close', function (e) {
            $(select2Dom).val(null).trigger('change');
        });
        // 格式化columns参数
        function formatColumn() {
            $(el).val(selectedArr.join(','))
        }

        function formatRepo(repo) {

            if (repo.loading) {
                return repo.text;
            }
            var markup = '<span> ' + repo.id + ':' + (repo[repo.id].cn || repo[repo.id].en) + '</span>'
            return markup;
        }

        function formatRepoSelection() {
            return $('<span style="color:#999">' + ($(el).attr("placeholder") || "") + '</span>')
        }
    }
})($, window)