#Run docker with rabbitmq

`docker run --rm --hostname my-rabbit --name rabbit-server -p 5672:5672 -p 15672:15672 rabbitmq:3.6.9-management`
