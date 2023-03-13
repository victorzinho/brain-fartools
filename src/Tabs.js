import UI from './UI';
import './css/tabs.css';

function enableOrDisable (tabHeader, tabElement, enabled) {
  if (enabled) {
    tabElement.domElement.style.display = 'block';
    tabHeader.domElement.classList.add('selected');
  } else {
    tabElement.domElement.style.display = 'none';
    tabHeader.domElement.classList.remove('selected');
  }
}

export default class Tabs {
  constructor (parent) {
    this.container = UI.Div(parent).withClassName('tabsContainer');
    this.header = UI.Div(this.container).withClassName('tabsHeaderContainer');
    this.contents = UI.Div(this.container).withClassName('tabsContents');
    this.headerToElement = new Map();
  }

  add (text) {
    const isFirst = this.headerToElement.size === 0;

    const tabElement = UI.Div(this.contents).withClassName('tabContents');
    const tabHeader = UI.Div(this.header).withText(text).withClassName('tabsHeader');

    this.headerToElement.set(tabHeader, tabElement);

    tabHeader.withEventListener('click', () => this.headerToElement
      .forEach((element, header) => {
        enableOrDisable(header, element, header === tabHeader);
      }));

    enableOrDisable(tabHeader, tabElement, isFirst);
    return tabElement;
  }
}
