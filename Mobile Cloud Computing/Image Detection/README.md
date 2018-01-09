# Image Detection

This assignment introduces you to the use of [Mobile Vision API](https://developers.google.com/vision/android/getting-started) in an Android application.

## Task
Your task is to create an application that recognizes objects given a source picture. The user interface should include an option to select the image from the gallery of the phone by clicking a button. Once the image is selected, it should be displayed on the screen as well as the number of faces recognized and if the image contains a barcode or not. Mobile Vision API has some limitations when objects are small, but we are not interested in such cases. The test cases will be high resolution pictures with clear faces or QR codes.

Your application should process images in a background thread to prevent blocking the User Interface thread. You should also consider proper resizing of images before displaying them, to prevent the application from running out of memory.


### Attention

Use the following Intent action and type to select a photo from the gallery. Our automated grading system relies on this to test your application with different photos.
```
Intent intent = new Intent();
intent.setType("image/*");
intent.setAction(Intent.ACTION_GET_CONTENT);
```

![picture](https://github.com/pty41/2017_2018_course_assignment/blob/master/Mobile%20Cloud%20Computing/Image%20Detection/12.png)

The expected result is shown below:

![picture](https://github.com/pty41/2017_2018_course_assignment/blob/master/Mobile%20Cloud%20Computing/Image%20Detection/22.png)
![picture](https://github.com/pty41/2017_2018_course_assignment/blob/master/Mobile%20Cloud%20Computing/Image%20Detection/3.png)

