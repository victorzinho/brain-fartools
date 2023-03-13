import Highcharts from 'highcharts';
import HighchartsMore from 'highcharts/highcharts-more';
import Histogram from 'highcharts/modules/histogram-bellcurve';

function mergeWithDefaultConfig (...overrides) {
  return Object.assign({
    title: {
      text: ''
    },
    credits: {
      enabled: false
    },
    xAxis: {
      title: {
        enabled: false
      }
    },
    yAxis: {
      title: {
        enabled: false
      },
      labels: {
        enabled: false
      }
    }
  }, ...overrides);
}

Highcharts.SVGRenderer.prototype.symbols.verticalLine = function (x, y, w, h, d) {
  return ['M', x + w / 2, y - 1.25 * h, 'L', x + w / 2, y + 1.75 * h];
};
if (Highcharts.VMLRenderer) {
  Highcharts.VMLRenderer.prototype.symbols.cross = Highcharts.SVGRenderer.prototype.symbols.cross;
}

const DARK_ORANGE = '#e77e00';

const ChartConfig = {
  bar: config => mergeWithDefaultConfig({
    chart: {
      type: 'scatter'
    },
    yAxis: {
      max: 0,
      min: 0,
      tickInterval: 1,
      title: {
        enabled: false
      }
    },
    plotOptions: {
      series: {
        animation: false
      },
      scatter: {
        marker: {
          symbol: 'verticalLine',
          lineColor: DARK_ORANGE,
          lineWidth: 2
        },
        tooltip: {
          headerFormat: '',
          pointFormat: '{point.x}'
        }
      }
    }
  }, config),
  line: config => mergeWithDefaultConfig({
    tooltip: {
      enabled: false
    },
    plotOptions: {
      series: {
        color: DARK_ORANGE
      }
    }
  }, config)
};

Histogram(Highcharts);
HighchartsMore(Highcharts);

export { Highcharts, ChartConfig };
