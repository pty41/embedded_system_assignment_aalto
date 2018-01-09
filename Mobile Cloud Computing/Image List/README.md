# Image List

The objective of this assignment is to learn how to use a ListView to display images taken from the Internet.

## Task
Your task is to parse a JSON file given as an input by using an EditText. By parsing the file, you obtain a sequence of items, each containing a link to an image and the corresponding author’s name. These must be displayed in a ListView.

The JSON file structure is as follows.

```
[
    {photo:"", author:""},
    {photo:"", author:""},
    {photo:"", author:""},
    {photo:"", author:""},
    {photo:"", author:""}
]
```

Each row has the following fields:

| Field     | Description          | Type     |
| ----------|:--------------------:| --------:|
| photo     | Image url            | String   |
| author    | Photographer’s name  |   String |

Field	Description	Type
photo	Image url	String
author	Photographer’s name.	String

You can use libraries such [OkHttp](http://square.github.io/okhttp/) for fetching the JSON file, and [Picasso](http://square.github.io/picasso/) for showing the photos. You can also use any other libraries or write your own code if you prefer.

You can use following JSON file to test your application: [http://www.mocky.io/v2/59a94ceb100000200c3e0a78](http://www.mocky.io/v2/59a94ceb100000200c3e0a78)

![picture](https://github.com/pty41/2017_2018_course_assignment/blob/master/Mobile%20Cloud%20Computing/Image%20List/11.png)

The expected result is shown below:

![picture](https://github.com/pty41/2017_2018_course_assignment/blob/master/Mobile%20Cloud%20Computing/Image%20List/21.png)

