module.exports = function(i) {
  return {
    id: 'view-calendar-job-example-' + i,
    title: '#' + i,
    url: '/jenkins/view/Calendar/job/Example/' + i + '/',
    icon: '<img src="build' + i + '.png">',
    start: '2018-07-' + i + 'T20:00:12',
    end: '2018-07-' + i + 'T20:02:12',
    state: 'finished'
  };
};
