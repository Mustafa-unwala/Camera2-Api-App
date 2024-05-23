# Camera App

This is a simple camera app for Android that captures images using the Camera2 API, displays a preview, and allows users to save or discard the images.
It also includes a main activity that displays all captured images with metadata and provides options to delete images.

## Features

- Capture images using a custom camera interface
- Preview captured images before saving
- Save or discard images from the preview screen
- View a list of all saved images with metadata (image number, date taken)
- Delete images from the main activity

## Installation

1. Clone the repository:

    ```bash
    git clone https://github.com/yourusername/camera-app.git
    ```

2. Open the project in Android Studio.

3. Build and run the project on an Android device or emulator.

## Usage

### Main Activity

- The main activity displays a list of all captured images.
- Each image item shows the image number and date taken.
- A floating action button is available to open the camera interface to capture a new image.
- Each image item has a delete icon to remove the image.

### Camera Activity

- The camera activity allows you to capture an image using the device's camera.
- Once the image is captured, it is passed to the preview activity.

### Preview Activity

- The preview activity shows the captured image and provides options to save or discard the image.
- If the save button is clicked, the image is saved, and the user is taken back to the main activity.
- If the discard button or back arrow is clicked, the image is discarded, and the user is taken back to the camera activity.

## Code Structure

- `MainActivity`: Displays a list of all captured images and provides options to capture new images and delete existing ones.
- `CameraActivity`: Custom camera interface using the Camera2 API to capture images.
- `PreviewActivity`: Displays a preview of the captured image and provides options to save or discard the image.
- `ImageAdapter`: RecyclerView adapter for displaying the list of captured images.

## Dependencies

- AndroidX libraries
- Camera2 API
