import { SnowglobePage } from './app.po';

describe('snowglobe App', () => {
  let page: SnowglobePage;

  beforeEach(() => {
    page = new SnowglobePage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
