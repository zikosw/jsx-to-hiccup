window.deps = {
    'react' : require('react'),
    'react-dom' : require('react-dom'),
    'react-dom-server': require('react-dom/server'),
    //'react-avatar' : require('react-avatar'),
    'acorn-jsx': require('acorn-jsx'),
    'babylon': require('babylon'),
    'react-codemirror2': require('react-codemirror2')
};

window.React = window.deps['react'];
window.ReactDOM = window.deps['react-dom'];
window.ReactDOMServer = window.deps['react-dom-server'];
