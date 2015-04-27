# ps2_alert_tracker
Android app that receives notifications when a new alert has started in
Planetside 2!

This is a work in progress but functions fairly well at the moment. It will
connect to the Sony Census server with a websocket library (Tyrus) and wait
for alerts. It also has functionality to fetch alerts on the fly. When a new
alert in received a notification is sent and if the main activity is opened
you can view a list of alerts along with information about the server
population and current continent control (updated every 10 minutes while the
alert is active).

Right now, this is only a mobile app. But I would like to eventually add a
wearable microapp.
