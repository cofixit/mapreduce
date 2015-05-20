$(function() {
    getJobs();
    setInterval(getJobs, 5000);
    $('form').submit(function(e) {
        e.preventDefault();
        $.get(
            '/addJob',
            {
                width: $('#add_width').val(),
                height: $('#add_height').val(),
                frames: $('#add_frames').val(),
                maxIterations: $('#add_maxIterations').val(),
                firstScale: $('#add_firstScale').val(),
                firstTranslateX: $('#add_firstTranslateX').val(),
                firstTranslateY: $('#add_firstTranslateY').val(),
                lastScale: $('#add_lastScale').val(),
                lastTranslateX: $('#add_lastTranslateX').val(),
                lastTranslateY: $('#add_lastTranslateY').val()
            },
            function(data) {
                getJobs();
            }
        );
    });
    $('#videocontainer a').click(function() {
        $('#videocontainer').hide();
    });
});

function openVideo(id, width, height) {
    $video = $('<video></video>')
        .attr({width: width,
            height: height,
            controls: 'true'})
        .removeClass('hide')
        .html($('<source>')
            .attr('src', '/result/job' + id + '.mp4'));
    $('#videocontainer video').remove();
    $('#videocontainer').append($video).show();
}

function getJobs() {
    $.getJSON('/listJobs', function(data) {
        $('table tr.data').remove();
        $.each(data.jobs, function(i, v) {
            var $row = $('<tr></tr>');
            var $status = $('<td></td>');
            var $label = $('<span></span>')
                .addClass('label')
                .html(v.status);
            switch (v.status) {
                case 'waiting':
                    $label.addClass('label-info');
                    $status.html($label);
                    break;
                case 'running':
                    $label.addClass('label-warning');
                    $status.html($label);
                    break;
                case 'failed':
                    $label.addClass('label-danger');
                    $status.html($label);
                    break;
                case 'done':
                    var $link = $('<a></a>')
                        .attr({href: 'javascript:openVideo(' + v.id + ',' + v.width + ', ' + v.height + ')'})
                        .addClass('openVideo label label-success')
                        .html(v.status);
                    $status.html($link);
                    break;
            }

            $row.addClass('data')
                .attr('id', 'job_' + v.id)
                .append($('<td></td>').html(v.id))
                .append($status)
                .append($('<td></td>').html(v.created))
                .append($('<td></td>').html(v.started))
                .append($('<td></td>').html(v.finished))
                .append($('<td></td>').html(v.width))
                .append($('<td></td>').html(v.height))
                .append($('<td></td>').html(v.frames))
                .append($('<td></td>').html(v.maxIterations))
                .append($('<td></td>').html(v.firstScale))
                .append($('<td></td>').html(v.firstTranslateX))
                .append($('<td></td>').html(v.firstTranslateY))
                .append($('<td></td>').html(v.lastScale))
                .append($('<td></td>').html(v.lastTranslateX))
                .append($('<td></td>').html(v.lastTranslateY))
            $('.addJobModel').after($row);
        });
    });

}