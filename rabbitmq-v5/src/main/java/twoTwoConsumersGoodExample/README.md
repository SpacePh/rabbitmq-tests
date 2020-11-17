### This package contains a good example of how to declare consumers

In this example, we define one connection and then two channels, one per consumer. Each channel consumes messages from a different queue. In this way, we can process messages from different queues at the same time.
- Each channel uses one Thread at a time
