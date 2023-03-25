import UI from './UI';
import './css/menu.css';

export default class Menu {
  constructor (parent) {
    this.rootContainer = UI.Div(parent).withClassName('menuRootContainer');
    this.menuContainer = UI.Div(this.rootContainer).withClassName('menuContainer closed');
    this.toggle = UI.Div(this.menuContainer).withClassName('menuToggle').withHtml('<span>☰</span>');
    this.contents = UI.Div(this.rootContainer).withClassName('menuContents');
    this.menuItems = UI.Div(this.menuContainer).withClassName('menuItems');
    this.menuItemToElement = new Map();

    this.toggle.withEventListener('mouseenter', (e) => this.menuContainer.domElement.classList.remove('closed'));
    this.menuContainer.withEventListener('mouseleave', (e) => this.menuContainer.domElement.classList.add('closed'));
  }

  add (text) {
    const contentElement = UI.Div(this.contents).withClassName('menuContent');
    const newMenuItem = UI.Div(this.menuItems).withHtml('<a href="#">' + text + '</a>').withClassName('menuItem');

    this.menuItemToElement.set(newMenuItem, contentElement);

    newMenuItem.withEventListener('click', () => {
      this.menuItemToElement.forEach((element, menuItem) => {
        element.domElement.style.display = menuItem === newMenuItem ? 'block' : 'none';
      });
      this.menuContainer.domElement.classList.add('closed');
    });

    contentElement.domElement.style.display = this.menuItemToElement.size === 1 ? 'block' : 'none';
    return contentElement;
  }
}
