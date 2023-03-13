import './css/ui.css';
import noUiSlider from 'nouislider/dist/nouislider.min.mjs';
import 'nouislider/dist/nouislider.css';

// frameworks? pffft, please...
class Element {
  constructor (parent, tagName) {
    this.domElement = document.createElement(tagName);
    (parent.domElement || parent).appendChild(this.domElement);
  }

  withClassName (className) {
    this.domElement.className = className;
    return this;
  }

  addClassName (className) {
    this.domElement.classList.add(className);
    return this;
  }

  withText (text) {
    this.domElement.innerText = text;
    return this;
  }

  withHtml (html) {
    this.domElement.innerHTML = html;
    return this;
  }

  withEventListener (event, f) {
    if (this.domElement.noUiSlider) {
      this.domElement.noUiSlider.on(event, f);
    } else {
      this.domElement.addEventListener(event, f);
    }

    return this;
  }

  withAttribute (key, value) {
    this.domElement[key] = value;
    return this;
  }

  set value (value) {
    if (this.value === value) return;
    if (this.domElement.noUiSlider) {
      this.domElement.noUiSlider.set(value);
    } else {
      this.domElement.value = value;
    }
    return this;
  }

  get value () {
    if (this.domElement.noUiSlider) {
      return this.domElement.noUiSlider.get();
    }
    return this.domElement.value;
  }
}

function newSlider (parent, name, min, max, values, step) {
  const container = new Element(parent, 'div').withClassName('sliderRangeContainer');
  const slider = new Element(container, 'div').withClassName('sliderRange');
  const config = {
    start: values,
    step: step,
    connect: true,
    tooltips: true,
    range: { min, max }
  };
  if (Number.isInteger(step)) {
    config.format = {
      from: value => parseInt(value),
      to: value => parseInt(value)
    };
  }
  noUiSlider.create(slider.domElement, config);
  new Element(container, 'label')
    .withAttribute('for', name)
    .withText(name);
  return slider;
}

function newInputText (parent, name, value) {
  const container = new Element(parent, 'div').withClassName('inputTextContainer');
  new Element(container, 'label')
    .withAttribute('for', name)
    .withText(name);
  return new Element(container, 'input')
    .withAttribute('type', 'text')
    .withAttribute('name', name)
    .withAttribute('value', value);
}

export default {
  TextField: newInputText,
  TextArea: parent => new Element(parent, 'textArea')
    .withAttribute('spellcheck', false),
  Div: parent => new Element(parent, 'div'),
  Label: parent => new Element(parent, 'label'),
  Canvas: parent => new Element(parent, 'canvas'),
  Slider: newSlider,
  Frame: (parent, url) => new Element(parent, 'iframe').withAttribute('src', url),
  FileInput: parent => new Element(parent, 'input').withAttribute('type', 'file')
};
