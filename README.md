# android-servo-controller
A basic application for generating servo signals from an Android device.

This generates signals suitable for a servo from the left and right channels of an Android device's headphone socket. You will need some electronics to interface the headphone socket to a servo motor. An example circuit is given at http://www.srimech.com/simple-headphone-controlled-phone-robot.html.

This was written as a proof of concept only. It isn't good quality code. In particular the remote control option for it listens on port 6502 and is completely unsecured.

The tone-generating code is taken from http://marblemice.blogspot.co.uk/2010/04/generate-and-play-tone-in-android.html and was modified by Steve Pomeroy on StackOverflow.com.

To build the android app, after setting up the ADK as normal:

    android update project --path . --target 1
    ant debug

(Where '1' is a valid target, given the output from "android list targets")

Controller.java is an example Java application to send datagram packets to the Android device.
