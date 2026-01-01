from flask import Flask
import requests

app = Flask(__name__)

@app.route("/")
def hello():
    r = requests.get("http://hello-world-1:5000/internal")
    return f"Service A →after commmit one more time {r.text}"

app.run(host="0.0.0.0", port=5000)

