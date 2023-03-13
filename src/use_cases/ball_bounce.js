import UI from '../UI';
import { ChartConfig, Highcharts } from '../charts';
import './ball_bounce.css';
import MusicPiece from '../MusicPiece';

const INFO = {
  title: 'Bounce your balls',
  html: `
<p>This tool allows you to simulate the bouncing of a ball on the ground when thrown from a certain height.
  You can then export it to a MIDI file with constant pitch, velocity and duration. The only meaningful information there
  is the <i>note-on</i> events. Ideally you can do something beautiful with that?</p>
<p>It is possible to choose the amount of time the ball should be bouncing, together with the "bounciness".
  As you might expect, the "bounciness" is a very complex and precise physical
  concept (how much height the ball loses with each bounce, units are "this much"s), so extreme values might lead to
  weird results, specially at the end of the bouncing. Use it at your own risk.</p>
`
};

const TAU = 1; // contact time for bounce
const G = 9.81; // gravity!
const DT = 1; // time step

const INITIAL_RHO = 0.8;
const MIN_RHO = 0.5;
const MAX_RHO = 0.9;

const INITIAL_TOTAL_TIME_SECONDS = 10;
const MIN_TOTAL_TIME_SECONDS = 1;
const MAX_TOTAL_TIME_SECONDS = 120;

function round (number, to) {
  number = Math.round(number / to) * to;
  return Math.round((number + Number.EPSILON) * 100) / 100;
}

/**
 * [Formula](https://physics.stackexchange.com/questions/256468/model-formula-for-bouncing-ball)
 *
 * @param rho The "bounciness" of the ball. Something between 0.5 and 0.95 should be fine (see link for a better descrìption).
 * @param roundTo The
 * @param totalTimeInSeconds The amount of time to "fill" with the ball bouncing. Note that extreme values (
 * low bounciness and long time or viceversa) can lead to artifacts.
 *
 * @returns {{times: *[], heights: *[], bouncesAt: *[], bouncesEvery: *[]}} An object where times and heights can be used
 * to draw a chart; bouncesAt has the moments that the ball did bounce (in bounce); bouncesEvery shows the same information
 * but as a timestamp difference between two consecutive bounces.
 */
function bounce (rho, roundTo, totalTimeInSeconds) {
  const output = {
    heights: [],
    times: [],
    bouncesAt: [],
    bouncesEvery: []
  };

  let velocity = 0.0; // current velocity
  let time = 0.0; // current time
  const initialHeight = 1e5;
  let height = initialHeight;
  let maxHeightThisBounce = initialHeight;
  let inContact = false;
  let timeLastBounce = -Math.sqrt(2 * initialHeight / G); // time we would have launched to get to h0 at t=0
  let maxVelocityThisBounce = Math.sqrt(2 * maxHeightThisBounce * G);
  while (maxVelocityThisBounce > 0 && output.bouncesEvery
    .map(x => round(x * totalTimeInSeconds / time, roundTo))
    .filter(x => Math.abs(x - roundTo) < 0.01)
    .length < 3) {
    if (inContact) {
      time += TAU;
      maxVelocityThisBounce *= rho;
      velocity = maxVelocityThisBounce;
      inContact = false;
      height = 0.0;

      const bounceTime = time - (output.bouncesAt[output.bouncesAt.length - 1] || 0);
      output.bouncesEvery.push(bounceTime);
      output.bouncesAt.push(time);
    } else {
      const newHeight = height + (velocity * DT) - 0.5 * G * DT * DT;
      if (newHeight < 0) {
        // we just hit the ground
        inContact = true;
        time = timeLastBounce + 2 * Math.sqrt(2 * maxHeightThisBounce / G);
        timeLastBounce = time + TAU;
        height = 0.0;
      } else {
        time += DT;
        velocity -= G * DT;
        height = newHeight;
      }
    }
    maxHeightThisBounce = 0.5 * maxVelocityThisBounce * maxVelocityThisBounce / G;
    output.heights.push(height);
    output.times.push(time);
  }
  const scale = totalTimeInSeconds / time;
  output.times = output.times.map(x => round(x * scale, roundTo));
  output.bouncesAt = output.bouncesAt.map(x => round(x * scale, roundTo));
  output.bouncesEvery = output.bouncesEvery.map(x => round(x * scale, roundTo));
  return output;
}

function createChart (domElement) {
  return Highcharts.chart(domElement, ChartConfig.line());
}

/**
 * Simulates the bouncing of a ball.
 *
 * @param parent The DOM element to put all the controls (canvas, sliders to control params, etc.).
 */
function create (parent) {
  const ballBounceContainer = UI.Div(parent).withClassName('ballBounceContainer');

  const canvasContainer = UI.Div(ballBounceContainer).withClassName('ballBounceCanvas');
  const chartDomElement = UI.Div(canvasContainer);
  const inputContainer = UI.Div(ballBounceContainer).withClassName('ballBounceInputs');
  const rhoRange = UI.Slider(inputContainer, 'Bouncincess', MIN_RHO, MAX_RHO, INITIAL_RHO, 0.05)
    .addClassName('ballBounceRho');
  const timeRange = UI.Slider(inputContainer, 'Total time (s)', MIN_TOTAL_TIME_SECONDS, MAX_TOTAL_TIME_SECONDS, INITIAL_TOTAL_TIME_SECONDS, 1)
    .addClassName('ballBounceTime');

  const musicPieceContainer = UI.Div(parent).withClassName('musicPieceContainer');
  const musicPiece = MusicPiece(musicPieceContainer);

  let bounceData;// = bounce(INITIAL_RHO, 0.05, INITIAL_TOTAL_TIME_SECONDS);
  // setBounceArray(bounceData.bouncesAt);
  const chart = createChart(chartDomElement.domElement);

  function setBounceArray (bouncesAt) {
    musicPiece.setArray([bouncesAt.map(at => [65, at, 0.1])]);
  }

  const update = () => {
    const rho = rhoRange.value;
    const totalSeconds = timeRange.value;

    bounceData = bounce(rho, 0.05, totalSeconds);
    chart.series.forEach(series => series.remove());
    chart.addSeries({
      showInLegend: false,
      data: bounceData.heights
    });
    chart.update({
      xAxis: {
        categories: bounceData.times
      }
    });
    setBounceArray(bounceData.bouncesAt);
  };

  rhoRange.withEventListener('update', update);
  timeRange.withEventListener('update', update);
  update();
}

export default { create, INFO };
