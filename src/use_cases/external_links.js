import UI from '../UI';
import './external_links.css';

function create (parent) {
  UI.Div(parent).withClassName('externalLinks').withHtml(`
<p>I can understand you want to leave this place. It makes me sad, but I get it :(<p>
<p>Here are some places to go:</p>
<ul>
<li><a href="https://github.com/victorzinho/brain-fartools/tree/master/src/main/java/victorzinho/music/usecases/atyla"
 target="_blank">Some code</a> to produce music from a bunch of sailboat lat/lon coordinates and a hexagonal grid with notes placed over 
some part of the world. Yup, I've done that, don't ask me why.</li>
<li>Some code to produce music from a shapefile coming from agriculture machinery, mapping values such as 
yield or speed to music parameters. And yes, I've done that too. My attempts to have code writing music for me are
endless. So are the failures. (TBD, need to push the code somewhere yet)</li>
<li><a href="https://louisbigo.com/hexachord" target="_blank">HexaChord</a>. According to the creator Louis Bigo: 
"<i>HexaChord is a computer-aided music analysis/composition software based on spatial representations of musical pitches generalizing the Tonnetz.</i>
Highly related to my sailboat hex-grid-over-the-north-sea adventure.</li>
<li>Bernat Cucarella Sifre's <a href="https://bernatcs.gumroad.com/l/hnsvl" target="_blank">SecToBPM</a>. A small tool to 
get the bpm for a given amount of bars and seconds.</li>
<li>Bernat Cucarella Sifre's <a href="https://bernatcs.gumroad.com/l/gzNcA" target="_blank">CurveData</a>. A tool to get
 a series of numbers from a highly customizable graph curve.</li>
</ul>`);
}

export default { create };
