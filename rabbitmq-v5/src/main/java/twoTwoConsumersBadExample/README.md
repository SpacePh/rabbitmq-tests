### This package contains a bad example of how to declare consumers

In this example, we define one connection and then one channel. The channel has two consumers. Since only one thread can run at a time by a channel, if we receive a message in the first consumer and start processing it, the second consumer can process any message if some arrives while the first consumer don't finish its job. 
