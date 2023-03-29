import UI from '../UI';
import { Highcharts, ChartConfig } from '../charts';
import './time_steps.css';
import MusicPiece from '../MusicPiece';
import { NOTE_ARRAY_DURATION } from '../formats';

const INFO = {
  title: 'They don\'t have to be giant',
  html: `
<p>Ever wanted to be like Christopher Nolan? With this tool you can fully control the passage of time.</p>
<p>It allows you to generate a set of notes with exponential durations by choosing the factor (duration increase/decrease
 on each step), as well as the number of steps and the initial step (or total duration).</p>
<p>Disclaimer: I'm not taking responsibility if the browser crashes while playing with funny values.</p>
`
};

function round (value) {
  return Math.round(value * 1000.0) / 1000.0;
}

function createChart (parent) {
  const canvasContainer = UI.Div(parent).withClassName('timeStepsCanvasContainer');
  const chartDomElement = UI.Div(canvasContainer);
  const config = ChartConfig.bar({
    series: [{
      showInLegend: false,
      data: [[4, 0], [2, 0]]
    }]
  });
  return Highcharts.chart(chartDomElement.domElement, config);
}

function updateChart (chart, musicPiece, factor) {
  chart.series.forEach(series => series.remove());
  chart.addSeries({
    showInLegend: false,
    data: musicPiece[0].map(note => [note[NOTE_ARRAY_DURATION], 0])
  });
  chart.update({
    xAxis: {
      reversed: factor < 1
    }
  });
}

function getMusicPart (factors, initialStep) {
  const part = [];
  let totalTime = 0.0;
  for (const factor of factors) {
    const duration = initialStep * factor;
    part.push([65, round(totalTime), round(duration)]);
    totalTime += duration;
  }

  return part;
}

/**
 * Generates constant notes with configurable time intervals (defined by a factor) between them.
 *
 * @param parent The DOM element to put all the controls (pitch, time sliders, etc.).
 */
function create (parent) {
  const container = UI.Div(parent).withClassName('timeStepsContainer');
  const timeStepsParamsContainer = UI.Div(container).withClassName('timeStepsParamsContainer');
  const row1 = UI.Div(timeStepsParamsContainer).withClassName('timeStepsParamsRow');
  const numStepsInput = UI.Slider(row1, 'Num. steps', 1, 50, [10], 1);
  const factorInput = UI.Slider(row1, 'Factor', 0.01, 2.0, [0.5], 0.01);
  const row2 = UI.Div(timeStepsParamsContainer).withClassName('timeStepsParamsRow');
  const initialStepInput = UI.Slider(row2, 'Initial step (s)', 0.1, 30.0, [4], 0.1);
  const totalTimeInput = UI.Slider(row2, 'Total time (s)', 1, 300.0, [20.0], 0.1);
  const chart = createChart(timeStepsParamsContainer);

  const musicPieceContainer = UI.Div(container).withClassName('timeStepsMusicPieceContainer');
  const musicPiece = MusicPiece(musicPieceContainer);

  // some stack overflow because of noUiSlider set/get values in UI.js; not debugging that...
  let updating = false;
  const musicPieceArray = [];

  function update (triggerInput) {
    if (updating) return;
    updating = true;

    const factors = Array.from({ length: numStepsInput.value }, (_, index) => Math.pow(factorInput.value, index));
    const sum = factors.reduce((a, b) => a + b);

    if (triggerInput === totalTimeInput) {
      initialStepInput.value = totalTimeInput.value / sum;
    } else {
      totalTimeInput.value = initialStepInput.value * sum;
    }

    musicPieceArray[0] = getMusicPart(factors, initialStepInput.value);
    updateChart(chart, musicPieceArray, factorInput.value);
    musicPiece.setArray(musicPieceArray);

    updating = false;
  }

  initialStepInput.withEventListener('update', () => update(initialStepInput));
  totalTimeInput.withEventListener('update', () => update(totalTimeInput));
  numStepsInput.withEventListener('update', update);
  factorInput.withEventListener('update', update);
  update(initialStepInput);
}

export default { create, INFO };
