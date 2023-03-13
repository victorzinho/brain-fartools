import UI from '../UI';
import MidiIO from '../MidiIO';

import { Highcharts } from '../charts';
// import Highcharts from 'highcharts';
// import HighchartsMore from 'highcharts/highcharts-more';
// import Histogram from 'highcharts/modules/histogram-bellcurve';
import Summary from 'summary';
import { MidiStats } from './MidiStats';

import './midi_analysis.css';

// Histogram(Highcharts);
// HighchartsMore(Highcharts);

const INFO = {
  title: 'Are we really human?',
  html: `
<p>The idea behind this one is to analyze the deviations between MIDI produced by some software and one played by a human.</p>
<p>The analyzed parameters are the inter-onset interval (how much do we like to move notes in time), the velocity 
(how much we like to move dynamics) and duration (how much we like articulation).</p>
<p>Conclusions? Not really. The plots (even the delta normalized ones) produce an aggregation of everything that was
 played, so local deviations (like some tenuto or rubato somewhere) might disappear in the overall aggregation. Trills
 destroy the analysis quite a bit :)</p>
 <p>Still, fun to watch.</p>
`
};

const SAMPLE_MIDI_BASE_64 = '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';
const SAMPLE_MIDI = readSampleMidi();

function readSampleMidi () {
  const raw = atob(SAMPLE_MIDI_BASE_64);
  const bytes = new Uint8Array(new ArrayBuffer(raw.length));
  for (let i = 0; i < raw.length; i++) {
    bytes[i] = raw.charCodeAt(i);
  }
  return MidiIO.read(bytes.buffer);
}

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

function boxplot (files, renderTo, series, title, yAxis) {
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

export function analyze (midi, filename, files, stats) {
  adjustDurationsWithPedal(midi);

  stats.newMidi(midi);
  files.push(filename);
  midi.tracks.flatMap(track => track.notes).forEach(stats.note.bind(stats));

  boxplot(files, 'output_boxplot1', stats.getAttackValues(), 'IOI (Inter-onset interval)', 'ms');
  histogram('output_histogram1_1', stats.getAttackValues().flat(), 'IOI', 'ms');
  histogram('output_histogram1_2', stats.getAttackDelta().flat(), 'IOI Δ', 'ms');
  histogram('output_histogram1_3', stats.getAttackDeltaNormalized().flat(), 'IOI Δ normalized', '%');
  boxplot(files, 'output_boxplot2', stats.getVelocityValues(), 'velocity', 'pepinaso [0-1]');
  histogram('output_histogram2_1', stats.getVelocityValues().flat(), 'velocity', 'pepinaso [0-1]');
  histogram('output_histogram2_2', stats.getVelocityDelta().flat(), 'velocity Δ', 'pepinaso [0-1]');
  histogram('output_histogram2_3', stats.getVelocityDeltaNormalized().flat(), 'velocity Δ normalized', '%');
  boxplot(files, 'output_boxplot3', stats.getDurationValues(), 'duration', 'ms');
  histogram('output_histogram3_1', stats.getDurationValues().flat(), 'duration', 'ms');
  histogram('output_histogram3_2', stats.getDurationDelta().flat(), 'duration Δ', 'ms');
  histogram('output_histogram3_3', stats.getDurationDeltaNormalized().flat(), 'duration Δ normalized', '%');
}

function readBytes (fileReader, filename, files, stats) {
  const arrayBuffer = new Uint8Array(fileReader.result).buffer;
  const midi = MidiIO.read(arrayBuffer);
  analyze(midi, filename, files, stats);
}

function newStats () {
  return new MidiStats(true,
    0, 400,
    0, 1500,
    [0.25, 0.33, 0.5, 1, 2, 3, 4, 6, 8, 12, 16]);
}

/**
 * Analyzes the deviations in inter-onset interval, velocity and duration of MIDI files.
 *
 * @param parent The DOM element to put all the controls (canvas, sliders to control params, etc.).
 */
function create (parent) {
  let loadedFiles = [];
  let stats = newStats();
  let firstLoad;

  const container = UI.Div(parent).withClassName('midiAnalysisContainer');
  const fileInput = UI.FileInput(container).withClassName('midiAnalysisFileInput').withEventListener('change', event => {
    const files = event.target.files;
    if (FileReader && files && files.length) { // eslint-disable-line no-undef
      const reader = new FileReader(); // eslint-disable-line no-undef
      if (firstLoad) {
        loadedFiles = [];
        stats = newStats();
        firstLoad = false;
      }
      reader.onload = () => readBytes(reader, files[0].name, loadedFiles, stats);
      reader.readAsArrayBuffer(files[0]);
    }
  });
  const resetButton = UI.Div(container).withClassName('shinyButton').withText('Reset');
  UI.Div(container).withAttribute('id', 'output_boxplot1').withClassName('midiAnalysisBoxplot');
  UI.Div(container).withAttribute('id', 'output_histogram1_1').withClassName('midiAnalysisHistogram');
  UI.Div(container).withAttribute('id', 'output_histogram1_2').withClassName('midiAnalysisHistogram');
  UI.Div(container).withAttribute('id', 'output_histogram1_3').withClassName('midiAnalysisHistogram');
  UI.Div(container).withAttribute('id', 'output_boxplot2').withClassName('midiAnalysisBoxplot');
  UI.Div(container).withAttribute('id', 'output_histogram2_1').withClassName('midiAnalysisHistogram');
  UI.Div(container).withAttribute('id', 'output_histogram2_2').withClassName('midiAnalysisHistogram');
  UI.Div(container).withAttribute('id', 'output_histogram2_3').withClassName('midiAnalysisHistogram');
  UI.Div(container).withAttribute('id', 'output_boxplot3').withClassName('midiAnalysisBoxplot');
  UI.Div(container).withAttribute('id', 'output_histogram3_1').withClassName('midiAnalysisHistogram');
  UI.Div(container).withAttribute('id', 'output_histogram3_2').withClassName('midiAnalysisHistogram');
  UI.Div(container).withAttribute('id', 'output_histogram3_3').withClassName('midiAnalysisHistogram');

  function reset () {
    loadedFiles = [];
    stats = newStats();
    analyze(SAMPLE_MIDI, 'sample', loadedFiles, stats);
    firstLoad = true;
    fileInput.value = null;
  }

  reset();
  resetButton.withEventListener('click', reset);
}

export default { create, INFO };
