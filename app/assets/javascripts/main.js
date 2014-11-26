requirejs.config({
    paths: {
        'underscore': '../lib/underscorejs/underscore',
        'jquery': '../lib/jquery/jquery',
        'react': '../lib/react/react-with-addons',
        'react-bootstrap': '../lib/react-bootstrap/react-bootstrap',
        'json': '../lib/json3/json3',
        'renderjson': '../lib/renderjson'
    },
    shim: {
        'underscore': { exports: '_' },
        'jquery': { exports: '$' },
        'json': { exports: 'JSON' },
        'bootstrap': { deps: ['jquery'] }
    },
    exclude: [
        "jquery",
        "underscore",
        "react",
        "bootstrap"
    ]
});

define("window", function () {
    return window;
});

require(["react", "components/App"],
    function (React, App) {
        React.renderComponent(App({}), document.getElementById("placeholder-app"));
    }
);