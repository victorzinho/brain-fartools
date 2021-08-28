import Highcharts from 'highcharts';
import HighchartsMore from 'highcharts/highcharts-more';
import Histogram from 'highcharts/modules/histogram-bellcurve';
import Summary from 'summary';

Histogram(Highcharts);
HighchartsMore(Highcharts);

const files = [];
let millisSeries = [];
let velocitySeries = [];
let histogramMillisDiff = [];
let histogramMillisDiffNormalized = [];
let histogramVelocityDiff = [];
let histogramVelocityDiffNormalized = [];

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

function toMillis (midi, note) {
  const bpm = midi.header.tempos.length > 0 ? midi.header.tempos[0].bpm : 60;
  return Math.round(60000 / (bpm * midi.header.ppq) * note.durationTicks);
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

export function analyze (midi, filename, aggregateData = true) {
  adjustDurationsWithPedal(midi);

  if (!aggregateData) {
    millisSeries = [];
    histogramMillisDiff = [];
    histogramMillisDiffNormalized = [];
    velocitySeries = [];
    histogramVelocityDiff = [];
    histogramVelocityDiffNormalized = [];
  }

  const notes = midi.tracks.flatMap(track => track.notes);
  const currentMillisSeries = [];
  const currentVelocitySeries = [];
  for (let i = 1; i < notes.length; i++) {
    let minDiffMillis = 1e7;
    const currentMillis = toMillis(midi, notes[i]);
    const previousMillis = toMillis(midi, notes[i - 1]);
    const currentVelocity = notes[i].velocity;
    const previousVelocity = notes[i - 1].velocity;
    const diffVelocity = previousVelocity - currentVelocity;

    const durationRelations = [0.25, 0.33, 0.5, 1, 2, 3, 4, 6, 8, 12, 16];
    for (const durationRelation of durationRelations) {
      const diffMillis = previousMillis * durationRelation - currentMillis;
      if (Math.abs(diffMillis) < Math.abs(minDiffMillis)) {
        minDiffMillis = diffMillis;
      }
    }

    // who doesn't love some magic numbers? :)
    if (Math.abs(currentMillis) < 1500) {
      currentMillisSeries.push(currentMillis);
      histogramMillisDiff.push(minDiffMillis);
      histogramMillisDiffNormalized.push(minDiffMillis / Math.max(currentMillis, previousMillis));
    }

    currentVelocitySeries.push(currentVelocity);
    histogramVelocityDiff.push(diffVelocity);
    histogramVelocityDiffNormalized.push(diffVelocity / Math.max(currentVelocity, previousVelocity));
  }
  millisSeries.push(currentMillisSeries);
  velocitySeries.push(currentVelocitySeries);
  files.push(filename);

  boxplot('output_boxplot1', millisSeries, 'duration', 'ms');
  histogram('output_histogram1', millisSeries.flat(), 'duration', 'ms');
  histogram('output_histogram2', histogramMillisDiff, 'duration diff', 'ms');
  histogram('output_histogram3', histogramMillisDiffNormalized, 'duration diff normalized', '%');
  boxplot('output_boxplot2', velocitySeries, 'velocity', 'ms');
  histogram('output_histogram4', velocitySeries.flat(), 'velocity', 'pepinaso [0-1]');
  histogram('output_histogram5', histogramVelocityDiff, 'velocity diff', 'pepinaso [0-1]');
  histogram('output_histogram6', histogramVelocityDiffNormalized, 'velocity diff normalized', '%');
}
