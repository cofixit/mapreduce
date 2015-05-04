$(function() {
    getJobs();
    setInterval(getJobs, 5000);
});

function getJobs() {
    $.getJSON('/listJobs', function(data) {
        $('table tr.data').remove();
        $.each(data.jobs, function(i, v) {
            $row = $('<tr></tr>')
                .addClass('data')
                .attr('id', 'job_' + v.id)
                .append($('<td></td>').html(v.id))
                .append($('<td></td>').html(v.status))
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
            switch (v.status) {
                case 'running':
                    $row.addClass('warning');
                    break;
                case 'failed':
                    $row.addClass('danger');
                    break;
                case 'done':
                    $row.addClass('success');
                    break;
            }
            $('table').append($row);
        });
    });

}