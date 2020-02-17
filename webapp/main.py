
from flask import Flask, render_template, request, redirect
import json
import logging
import os
from google.cloud import pubsub_v1


app = Flask(__name__)

app.config['PUBSUB_TOPIC'] = os.environ['PUBSUB_TOPIC']
app.config['GCLOUD_PROJECT'] = os.environ['GOOGLE_CLOUD_PROJECT']

publisher = pubsub_v1.PublisherClient()


def send_message(message, user_agent):
    topic_path = publisher.topic_path(app.config['GCLOUD_PROJECT'],
                                      app.config['PUBSUB_TOPIC'])
    data = json.dumps({
        "message": message,
        "user_agent": user_agent
    }).encode('utf-8')

    future = publisher.publish(topic_path, data)
    future.result()


@app.route('/', methods=['GET', 'POST'])
def index():
    if request.method == 'GET':
        return render_template('index.html')

    message = request.form.get('payload', ' -- EMPTY -- ')
    user_agent = request.headers.get('User-Agent')
    send_message(message, user_agent)

    return redirect('/', code=200)


@app.errorhandler(500)
def server_error(e):
    logging.exception('An error occurred during a request.')
    return """
    An internal error occurred: <pre>{}</pre>
    See logs for full stacktrace.
    """.format(e), 500


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=8080, debug=True)
