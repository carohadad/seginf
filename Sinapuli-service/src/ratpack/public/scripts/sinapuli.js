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

});

