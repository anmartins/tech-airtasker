# Rate Limiter

This repo is ment to fullfil a challenge. The objective is to implement a rate limiter and keep the ability to extend its usecases without having to rebuild the entire app.

After some investigation about rate limiters in general, I found that they worked like a sempahore - enabling and disabling specific operations.

## Implementation

Since I went with kotlin, and JVM is a multithread environment, I decided to go for an approach that would be thread safe. The app is very simple and was created using the token bucket algorythm. 

My first approach received a thread pool and updated the available tokens after the period interval had passed, after some investigation I realised that a small improvement could be done by updating it lazily.


The request logic is mocked up only at the app level. The rate limiter is agnostic of it's usages.

## Testing steps

I'm assuming that the testers can download the code and run it locally, as I've created two ways of operating. One runs through a thread pool, while the other only uses the main thread.

The app assumes 3 arguments: period, requestLimit, time between requests and these can be configured on the main function available at the app.kt file.

#### Tools
 - SDKMAN // https://sdkman.io/install
 
 I've installed gradle and jvm through SDKMAN, with `sdk install gradle 8.0` and `sdk install 8.0.352-amzn`
 
 I've used jetbrains IntelIj to build and execute the app, but the same can be achieved using gradle and cmd, with `gradle run`. 
 
