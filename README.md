# Play REST API

This is the example project for [Making a REST API in Play](http://developer.lightbend.com/guides/play-rest-api/index.html).

## Appendix

### Planning
[Planning Board](https://github.com/kapit4n/play-news-api/projects/1)


### Running

You need to download and install sbt for this application to run.

Once you have sbt installed, the following at the command prompt will start up Play in development mode:

```
sbt run
```

Play will start up on the HTTP port at http://localhost:9000/.   You don't need to reploy or reload anything -- changing any source code while the server is running will automatically recompile and hot-reload the application on the next HTTP request. 

### Usage
#### GETS
```
GET /v1/news HTTP/1.1
Result
[
  {
    "id": "1",
    "link": "/v1/news/1",
    "title": "Title",
    "body": "body",
    "likes": 1
  },
  {
    "id": "2",
    "link": "/v1/news/2",
    "title": "news title",
    "body": "News body 1",
    "likes": 0
  }
]
GET /v1/news/1 HTTP/1.1
Result 
{
  "id": "1",
  "link": "/v1/news/1",
  "title": "Title",
  "body": "body",
  "likes": 1
}
GET /v1/news/1/comments HTTP/1.1
Result 
[
  {
    "id": "1",
    "link": "/v1/news/1/comments/1",
    "newsId": 1,
    "body": "News body 1",
    "likes": 0
  },
  {
    "id": "2",
    "link": "/v1/news/1/comments/2",
    "newsId": 1,
    "body": "Comments body 1",
    "likes": 0
  }
]
GET /v1/comments/1 HTTP/1.1
Result
{Return the comment with Id equal 1}
```
#### POSTS
```
POST /v1/news HTTP/1.1
Example:
{"title":"news title","body":"News body 1", likes: '2'}
POST /v1/news/1/comments HTTP/1.1
Example:
{"newsId":"2","body":"Comments body 1", "likes": "1"}
```

#### DELETE

```
DELETE http://localhost:9000/v1/news/1 http/1.1
{delete news with Id 1}
DELETE http://localhost:9000/v1/comments/1 http/1.1
{delete comment with Id 1}
```

### Load Testing

The best way to see what Play can do is to run a load test.  We've included Gatling in this test project for integrated load testing.

Start Play in production mode, by [staging the application](https://www.playframework.com/documentation/2.5.x/Deploying) and running the play script:s

```
sbt stage
cd target/universal/stage
bin/play-rest-api -Dplay.crypto.secret=testing
```

Then you'll start the Gatling load test up (it's already integrated into the project):

```
sbt gatling:test
```

For best results, start the gatling load test up on another machine so you do not have contending resources.  You can edit the [Gatling simulation](http://gatling.io/docs/2.2.2/general/simulation_structure.html#simulation-structure), and change the numbers as appropriate.

Once the test completes, you'll see an HTML file containing the load test chart:

```
 ./rest-api/target/gatling/gatlingspec-1472579540405/index.html
```

That will contain your load test results.
