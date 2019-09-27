class Test {
    render(syntax) {

        return (
          <li className={`aaaaa ${root ? 'bbbbb' : ''}`}
              key={syntax}>
            <a onClick={this.handleClick.bind(this, syntax)}>
              {syntax.toUpperCase()}
            </a>
          </li>
        );
    }
}