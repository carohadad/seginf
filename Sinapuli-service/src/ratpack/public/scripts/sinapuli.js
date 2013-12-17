$(function() {
  var USER_FORMAT = 'd/m/Y H:i';

  var dateTimeFields = $('input.datetime')
    .each(function(index, elt) {
      var d = new Date(elt.value);
      $(elt).data('isodate', elt.value);
      elt.value = d.dateFormat(USER_FORMAT);
    }).datetimepicker({
      format: USER_FORMAT,
      inline: false,
      lang: 'en',
      minDate: 0, 
      minTime: false,
      onChangeDateTime: function(dp, $input) {
        $input.data('isodate', dp.toISOString());
      }
    }).attr('type', 'text');

  dateTimeFields.parents('form').on('submit', function(evt) {
    dateTimeFields.each(function(index, elt) {
      elt.value = $(elt).data('isodate');
    });
    return true;
  });

  $('form[data-confirm]').on('submit', function(evt) {
    var resp = window.prompt($(this).data('confirm') + ' (s/S/y/Y/n/N)').toLowerCase();
    if (resp != 's' && resp != 'y') {
      evt.preventDefault();
      evt.stopPropagation();
      return false;
    } else {
      return true;
    }
  });
});

