let searchFormDidName;
if (window.location.href.indexOf('/repeat') == -1 && window.location.href.indexOf('/errorHex') == -1) {
    if (window.location.href.indexOf('/ne/') > 0) {
        searchFormDidName = 'neSearchFormDid';
    } else if (window.location.href.indexOf('/farm/') > 0) {
        searchFormDidName = 'farmSearchFormDid';
    }
    if (searchFormDidName) {
        let did = $('#searchForm #did').val();
        if (did) {
            localStorage.setItem(searchFormDidName, did);
        } else {
            did = localStorage.getItem(searchFormDidName);
            if (did) {
                $('#searchForm #did').val(did);
            }
        }
    }
}

if (!$.getUrlParam('startTime')) {
    let startTime = localStorage.getItem('searchForm_startTime');
    if (startTime) {
        $('#searchForm [name=startTime]').val(startTime);
    }
}
if (!$.getUrlParam('endTime')) {
    let endTime = localStorage.getItem('searchForm_endTime');
    if (endTime) {
        $('#searchForm [name=endTime]').val(endTime);
    }
}

$('#searchForm').submit(function () {
    let startTime = $('#searchForm [name=startTime]').val();
    if (startTime) {
        localStorage.setItem('searchForm_startTime', startTime);
    }
    let endTime = $('#searchForm [name=endTime]').val();
    if (endTime) {
        localStorage.setItem('searchForm_endTime', endTime);
    }
});

$('#searchForm #clearForm').on('click', function () {
    $('#searchForm [name]').removeAttr('name');
    $('#searchForm [required]').removeAttr('required');
    if ($('#searchForm #did').length > 0 && searchFormDidName) {
        localStorage.setItem(searchFormDidName, '');
    }
    localStorage.setItem('searchForm_startTime', '');
    localStorage.setItem('searchForm_endTime', '');
    $('#searchForm').submit();
});