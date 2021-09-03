import Highcharts from 'highcharts';
import HighchartsMore from 'highcharts/highcharts-more';
import Histogram from 'highcharts/modules/histogram-bellcurve';
import Summary from 'summary';
import { MidiStats } from './midi_stats';

Histogram(Highcharts);
HighchartsMore(Highcharts);

const files = [];

function getPedalRanges (midi) {
  const pedalRanges = [];
  let currentRange = [];
  const controlChanges = midi.tracks
    .flatMap(track => track.controlChanges[64]);
  for (let i = 0; i < controlChanges.length; i++) {
    while (controlChanges[i].value !== (currentRange.length === 0 ? 1 : 0) && i < controlChanges.length - 1) i++;
    currentRange.push(controlChanges[i].ticks);
    if (currentRange.length === 2) {
      pedalRanges.push(currentRange);
      currentRange = [];
    }
  }
  return pedalRanges;
}

function adjustDurationsWithPedal (midi) {
  const pedalRanges = getPedalRanges(midi);
  midi.tracks
    .flatMap(track => track.notes)
    .forEach(note => {
      const endTicks = note.ticks + note.durationTicks;
      const range = pedalRanges.find(range => range[0] < endTicks && endTicks < range[1]);
      if (range) {
        note.durationTicks = range[1] - note.ticks;
      }
    });
}

function histogram (renderTo, data, title, xAxis) {
  Highcharts.chart(renderTo, {
    title: {
      text: title
    },
    xAxis: [{
      title: { text: xAxis },
      visible: false,
      alignTicks: true
    }, {
      title: { text: xAxis },
      alignTicks: true
    }],
    yAxis: [{
      title: { text: 'count' },
      visible: false
    }, {
      title: { text: 'count' },
      alignTicks: true
    }],
    plotOptions: {
      histogram: {
        binsNumber: 75
      }
    },
    series: [{
      type: 'histogram',
      xAxis: 1,
      yAxis: 1,
      baseSeries: 1
    }, {
      data: data,
      xAxis: 1,
      yAxis: 1,
      visible: false
    }]
  });
}

function boxplot (renderTo, series, title, yAxis) {
  const data = series.map(s => {
    const summary = Summary(s);
    return [summary.min(), summary.quartile(0.25), summary.median(), summary.quartile(0.75), summary.max()];
  });

  Highcharts.chart(renderTo, {
    chart: {
      type: 'boxplot'
    },
    title: {
      text: title
    },
    xAxis: {
      categories: files,
      title: { text: 'file' }
    },
    yAxis: {
      title: { text: yAxis }
    },
    series: [{
      name: 'Observations',
      data: data
    }]
  });
}

const stats = new MidiStats(true,
  0, 400,
  0, 1500,
  [0.25, 0.33, 0.5, 1, 2, 3, 4, 6, 8, 12, 16]);

export function analyze (midi, filename) {
  adjustDurationsWithPedal(midi);

  stats.newMidi(midi);
  files.push(filename);
  midi.tracks.flatMap(track => track.notes).forEach(stats.note.bind(stats));

  boxplot('output_boxplot1', stats.getAttackValues(), 'attack diff', 'ms');
  histogram('output_histogram1_1', stats.getAttackValues().flat(), 'attack diff', 'ms');
  histogram('output_histogram1_2', stats.getAttackDelta().flat(), 'attack diff delta', 'ms]');
  histogram('output_histogram1_3', stats.getAttackDeltaNormalized().flat(), 'attack diff delta normalized', '%');
  boxplot('output_boxplot2', stats.getVelocityValues(), 'velocity', 'ms');
  histogram('output_histogram2_1', stats.getVelocityValues().flat(), 'velocity', 'pepinaso [0-1]');
  histogram('output_histogram2_2', stats.getVelocityDelta().flat(), 'velocity delta', 'pepinaso [0-1]');
  histogram('output_histogram2_3', stats.getVelocityDeltaNormalized().flat(), 'velocity delta normalized', '%');
  boxplot('output_boxplot3', stats.getDurationValues(), 'duration', 'ms');
  histogram('output_histogram3_1', stats.getDurationValues().flat(), 'duration', 'ms');
  histogram('output_histogram3_2', stats.getDurationDelta().flat(), 'duration delta', 'ms');
  histogram('output_histogram3_3', stats.getDurationDeltaNormalized().flat(), 'duration delta normalized', '%');
}
