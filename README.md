# Play Scala Akka Example

To see it in action, refer to [https://play-akka-scala-example.herokuapp.com](https://play-akka-scala-example.herokuapp.com). Open a couple of browser windows there and see the messages you get back.

---

This is an example backend service built using the Play! Framework, which combines the power of Scala and Akka. It uses the Actor pattern to handle requests from both HTTP and WebSocket clients.

When a client connects, the framework notifies the BossActor, which in turn spanwns a new WorkerActor to do some work for the client. The BossActor responds with the client ID of the connected client, which is then sent back as an HTTP response. The client then opens two WebSocket connections with the backend, which are handled by the MessengerActor.

The worker actor works in cycles, and notifies the BossActor for each completed work cycle. When the last cycle is completed, the BossActor kills the WorkerActor.

When the BossActor receives a "WorkCycleCompleted" message, it notifies the MessengerActor which pushes the information down the first WebSocket connection with the client identified by its ID.

The second WebSocket connection is used by the MessengerActor to notify the client about the total amount of active WorkerActors. 

![](https://github.com/ticofab/play-scala-akka-example/blob/master/docs/flow.png)


## LICENSE

This software is licensed under the Apache 2 license, quoted below.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with
the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
language governing permissions and limitations under the License.