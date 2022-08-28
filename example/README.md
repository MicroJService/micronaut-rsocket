# RSocket Broker Example

1. run the [alibaba broker](https://github.com/alibaba/alibaba-rsocket-broker/tree/master/alibaba-broker-server).
2. run the rsocket-responder and rsocket-requester ervice.
3. call the api:
```shell
curl -X GET --location "http://localhost:1989/account/114514"
```