/** @jsx React.DOM */
define([
    "jquery",
    "react",
    "lib/SetIntervalMixin"
], function ($,
             React,
             SetIntervalMixin) {

    function isEquivalent(a, b) {
        // Create arrays of property names
        var aProps = Object.getOwnPropertyNames(a);
        var bProps = Object.getOwnPropertyNames(b);

        // If number of properties is different,
        // objects are not equivalent
        if (aProps.length != bProps.length) {
            return false;
        }

        for (var i = 0; i < aProps.length; i++) {
            var propName = aProps[i];

            // If values of same property are not equal,
            // objects are not equivalent
            if (a[propName] !== b[propName]) {
                return false;
            }
        }

        // If we made it this far, objects
        // are considered equivalent
        return true;
    }

    return React.createClass({
        mixins: [SetIntervalMixin],
        getInitialState: function () {
            return {
                workflows: [],
                runningJobs: [],
                queuedJobs: [],
                pastJobs: []
            };
        },
        componentDidMount: function() {
            // load init data
            this.updateState();
            this.setInterval(this.updateState, 10000);
        },
        updateState: function() {
            $.get('/status', function (data) {
                if (!isEquivalent(this.state, data)) {
                    console.log(this.state);
                    console.log(data);
                    this.setState(data);
                } else {
                    console.log("!=");
                }
            }.bind(this));
        },
        renderjson: function(json) {
            return renderjson(json);
        },
        render: function () {
            return (
                <div className="wrapper">
                    <div className="row">
                        <div className=" col-sm-6">
                        <section className="panel">
                        <header className="panel-heading"><span className="label bg-danger pull-right">{this.state.workflows.length} total</span>Workflows</header>
                        <table className="table table-striped m-b-none text-sm">
                            <thead><tr><th>id</th><th>name</th><th>cron</th><th>actions</th></tr></thead>
                            <tbody>
                            {this.state.workflows.map(function(wf){
                                return <tr><td>{wf.id}</td><td>{wf.name}</td><td>{wf.cron.replace(/\+/gi,' ')}</td><td>{wf.actions.map(function(a){return <p>{a}</p>})}</td></tr>
                            }.bind(this))}
                            </tbody>
                        </table>
                        </section>
                        </div>
                        <div className=" col-sm-6">
                            <section className="panel">
                                <header className="panel-heading"><span className="label bg-danger pull-right">{this.state.runningJobs.length} total</span>Running Jobs</header>
                                <table className="table table-striped m-b-none text-sm">
                                    <thead><tr><th>id</th></tr></thead>
                                    <tbody>
                                {this.state.runningJobs.map(function(job){
                                    return <tr><td>{job}</td></tr>
                                }.bind(this))}
                                    </tbody>
                                </table>
                            </section>
                            <section className="panel">
                                <header className="panel-heading"><span className="label bg-danger pull-right">{this.state.queuedJobs.length} left</span>Jobs in Queue</header>
                                <table className="table table-striped m-b-none text-sm">
                                    <thead><tr><th>name</th></tr></thead>
                                    <tbody>
                                    {this.state.queuedJobs.map(function(job){
                                        return <tr><td>{job.name}</td></tr>
                                    }.bind(this))}
                                    </tbody>
                                </table>
                            </section>
                        </div>
                    </div>
                    <div className="row">
                        <div className=" col-sm-12">
                            <section className="panel">
                                <header className="panel-heading">Past Jobs</header>
                                <table className="table table-striped m-b-none text-sm">
                                    <thead><tr><th>name</th><th>result</th></tr></thead>
                                    <tbody>
                                    {this.state.pastJobs.map(function(job){
                                        return <tr><td>{job.name}</td><td>{job.status}</td></tr>
                                    }.bind(this))}
                                    </tbody>
                                </table>
                            </section>
                        </div>
                    </div>
                </div>
            )
        }
    });
});