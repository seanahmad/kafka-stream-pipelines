# -*- coding: utf-8 -*-
# FIXME move this to a pygradle task: https://github.com/linkedin/pygradle/tree/master/examples/example-project

import sys
import dash
import dash_core_components as dcc
import dash_html_components as html
from dash.dependencies import Input, Output, State, Event
from plotly.graph_objs import *
from simple_rest_client.api import API

app = dash.Dash()
pipeline = 'demo-pipeline'
sourceTopic = 'demo-111'
foldTopic = 'demo-fold-111'

# create api instance
api = API(
    api_root_url='http://localhost:8080/api/v1/poll/%s/' % pipeline, # base api url
    params={}, # default params
    headers={'Content-Type': 'application/json'}, # default headers
    timeout=2, # default timeout in seconds
    append_slash=False, # append slash to final url
    json_encode_body=False, # encode body as json
)

api.add_resource(resource_name=sourceTopic)
api.add_resource(resource_name=foldTopic)

app.css.config.serve_locally = True
app.scripts.config.serve_locally = True
app.layout = html.Div(children=[
    html.H1(children='Kafka Demo Pipeline in Dash'),

    dcc.Graph(id='return-graph'),
    dcc.Interval(id='update-return', interval=2000, n_intervals=0),

    dcc.Graph(id='performance-graph'),
    dcc.Interval(id='update-performance', interval=2000, n_intervals=0),
])


@app.callback(Output('return-graph', 'figure'),
              [Input('update-return', 'n_intervals')])
def poll_for_returns(n):
    response = getattr(api, sourceTopic).list(body=None, params={'offset': 0, 'timeout': 900}, headers={}).body

    if response is not None and response['success'] and len(response['keys']) > 0:
        return Figure(
            data = [
                {'x': response['offsets'], 'y': [float(s) for s in response['values']], 'type': 'bar', 'name': 'Simulated Returns'}
            ],
            layout = {
                'title': 'Simulated Return Timeseries'
            }
        )
    else:
        return None

@app.callback(Output('performance-graph', 'figure'),
              [Input('update-performance', 'n_intervals')])
def poll_for_perfromance(n):
    print("-----------------\n%s\n----------------------" % n)
    response = getattr(api, foldTopic).list(body=None, params={'offset': 0, 'timeout': 900}, headers={}).body

    if response is not None and response['success'] and len(response['keys']) > 0:
        return Figure(
            data = [
                {'x': response['offsets'], 'y': [float(s) for s in response['values']], 'type': 'bar', 'name': 'Simulated Returns'}
            ],
            layout = {
                'title': 'Simulated Return Timeseries'
            }
        )
    else:
        return None

if __name__ == '__main__':
    app.run_server(debug=False)
